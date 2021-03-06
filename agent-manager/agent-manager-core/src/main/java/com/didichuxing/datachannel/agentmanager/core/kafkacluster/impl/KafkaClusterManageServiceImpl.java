package com.didichuxing.datachannel.agentmanager.core.kafkacluster.impl;

import com.alibaba.fastjson.JSON;
import com.didichuxing.datachannel.agentmanager.common.bean.common.CheckResult;
import com.didichuxing.datachannel.agentmanager.common.bean.common.ListCompareResult;
import com.didichuxing.datachannel.agentmanager.common.bean.common.Pair;
import com.didichuxing.datachannel.agentmanager.common.bean.domain.agent.AgentDO;
import com.didichuxing.datachannel.agentmanager.common.bean.domain.logcollecttask.LogCollectTaskDO;
import com.didichuxing.datachannel.agentmanager.common.bean.domain.receiver.ReceiverDO;
import com.didichuxing.datachannel.agentmanager.common.bean.domain.receiver.ReceiverPaginationQueryConditionDO;
import com.didichuxing.datachannel.agentmanager.common.bean.po.receiver.KafkaClusterPO;
import com.didichuxing.datachannel.agentmanager.common.constant.CommonConstant;
import com.didichuxing.datachannel.agentmanager.common.enumeration.ErrorCodeEnum;
import com.didichuxing.datachannel.agentmanager.common.enumeration.operaterecord.ModuleEnum;
import com.didichuxing.datachannel.agentmanager.common.enumeration.operaterecord.OperationEnum;
import com.didichuxing.datachannel.agentmanager.common.exception.ServiceException;
import com.didichuxing.datachannel.agentmanager.common.util.*;
import com.didichuxing.datachannel.agentmanager.core.agent.manage.AgentManageService;
import com.didichuxing.datachannel.agentmanager.core.common.OperateRecordService;
import com.didichuxing.datachannel.agentmanager.core.kafkacluster.KafkaClusterManageService;
import com.didichuxing.datachannel.agentmanager.core.logcollecttask.manage.LogCollectTaskManageService;
import com.didichuxing.datachannel.agentmanager.persistence.mysql.KafkaClusterMapper;
import com.didichuxing.datachannel.agentmanager.remote.kafkacluster.RemoteKafkaClusterService;
import com.didichuxing.datachannel.agentmanager.thirdpart.kafkacluster.extension.KafkaClusterManageServiceExtension;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * @author huqidong
 * @date 2020-09-21
 * kafka ??????????????????????????????
 */
@org.springframework.stereotype.Service
public class KafkaClusterManageServiceImpl implements KafkaClusterManageService {

    private static final Logger LOGGER = LoggerFactory.getLogger(KafkaClusterManageServiceImpl.class);

    @Autowired
    private KafkaClusterMapper kafkaClusterDAO;

    @Autowired
    private RemoteKafkaClusterService remoteKafkaClusterService;

    @Autowired
    private KafkaClusterManageServiceExtension kafkaClusterManageServiceExtension;

    @Autowired
    private OperateRecordService operateRecordService;

    @Autowired
    private LogCollectTaskManageService logCollectTaskManageService;

    @Autowired
    private AgentManageService agentManageService;

    private ReceiverDOComparator comparator = new ReceiverDOComparator();

    @Override
    public ReceiverDO getKafkaClusterByKafkaClusterId(Long kafkaClusterId) {
        KafkaClusterPO kafkaClusterPO = kafkaClusterDAO.selectByKafkaClusterId(kafkaClusterId);
        if (null != kafkaClusterPO) {
            return kafkaClusterManageServiceExtension.kafkaClusterPO2KafkaCluster(kafkaClusterPO);
        } else {
            return null;
        }
    }

    @Override
    public ReceiverDO[] getDefaultReceivers() {
        List<KafkaClusterPO> kafkaClusterPOList = kafkaClusterDAO.list();
        if (CollectionUtils.isEmpty(kafkaClusterPOList)) {
            return null;
        }
        ReceiverDO metricReceiver = null;
        ReceiverDO errorLogReceiver = null;
        for (KafkaClusterPO kafkaClusterPO : kafkaClusterPOList) {
            if (!StringUtils.isBlank(kafkaClusterPO.getAgentMetricsTopic())) {
                metricReceiver = ConvertUtil.obj2Obj(kafkaClusterPO, ReceiverDO.class);
            }
            if (!StringUtils.isBlank(kafkaClusterPO.getAgentErrorLogsTopic())) {
                errorLogReceiver = ConvertUtil.obj2Obj(kafkaClusterPO, ReceiverDO.class);
            }
        }
        return new ReceiverDO[]{metricReceiver, errorLogReceiver};
    }

    @Override
    public List<ReceiverDO> list() {
        List<KafkaClusterPO> kafkaClusterPOList = kafkaClusterDAO.list();
        if (CollectionUtils.isEmpty(kafkaClusterPOList)) {
            return new ArrayList<>();
        } else {
            return kafkaClusterManageServiceExtension.kafkaClusterPOList2ReceiverDOList(kafkaClusterPOList);
        }
    }

    @Override
    @Transactional
    public Long createKafkaCluster(ReceiverDO kafkaCluster, String operator) {
        return handleCreateKafkaCluster(kafkaCluster, operator);
    }

    /**
     * ?????? KafkaClusterPO ????????????
     *
     * @param kafkaClusterDO ????????? KafkaClusterPO ??????
     * @param operator       ?????????
     * @return ???????????????????????? true????????? false????????????
     * @throws ServiceException ???????????????????????????????????????
     */
    private Long handleCreateKafkaCluster(ReceiverDO kafkaClusterDO, String operator) throws ServiceException {
        /*
         * ?????? ??????
         */
        CheckResult checkResult = kafkaClusterManageServiceExtension.checkCreateParameterKafkaCluster(kafkaClusterDO);
        if (!checkResult.getCheckResult()) {
            throw new ServiceException(checkResult.getMessage(), checkResult.getCode());
        }
        /*
         * ?????? kafkaClusterName & kafkaClusterBrokerConfiguration ???????????????????????????
         */
        KafkaClusterPO kafkaClusterPO = kafkaClusterDAO.selectByKafkaClusterName(kafkaClusterDO.getKafkaClusterName());
        if (null != kafkaClusterPO) {
            throw new ServiceException(
                    String.format("??????????????????kafkaClusterName={%s}???KafkaCluster??????", kafkaClusterDO.getKafkaClusterName()),
                    ErrorCodeEnum.KAFKA_CLUSTER_NAME_DUPLICATE.getCode()
            );
        }

        /*
         * agent error logs ????????? topic ???????????????????????????????????????????????? topic ????????????
         */
        if(StringUtils.isNotBlank(kafkaClusterDO.getAgentErrorLogsTopic())) {
            KafkaClusterPO agentErrorLogsTopicExistsKafkaCluster = kafkaClusterDAO.getAgentErrorLogsTopicExistsKafkaCluster();
            if(null != agentErrorLogsTopicExistsKafkaCluster) {
                throw new ServiceException(
                        String.format("????????????????????? agent errorlogs ????????? topic ??? kafkacluster={%s}", JSON.toJSONString(agentErrorLogsTopicExistsKafkaCluster)),
                        ErrorCodeEnum.KAFKA_CLUSTER_CREATE_OR_UPDATE_FAILED_CAUSE_BY_AGENT_ERROR_LOGS_TOPIC_EXISTS.getCode()
                );
            }
        }
        /*
         * agent metrics ????????? topic ???????????????????????????????????????????????? topic ????????????
         */
        if(StringUtils.isNotBlank(kafkaClusterDO.getAgentMetricsTopic())) {
            KafkaClusterPO agentMetricsTopicExistsKafkaCluster = kafkaClusterDAO.getAgentMetricsTopicExistsKafkaCluster();
            if(null != agentMetricsTopicExistsKafkaCluster) {
                throw new ServiceException(
                        String.format("????????????????????? agent metrics ????????? topic ??? kafkacluster={%s}", JSON.toJSONString(agentMetricsTopicExistsKafkaCluster)),
                        ErrorCodeEnum.KAFKA_CLUSTER_CREATE_OR_UPDATE_FAILED_CAUSE_BY_AGENT_METRICS_TOPIC_EXISTS.getCode()
                );
            }
        }

//        kafkaClusterPO = kafkaClusterDAO.selectByKafkaClusterBrokerConfiguration(kafkaClusterDO.getKafkaClusterBrokerConfiguration());
//        if(null != kafkaClusterPO) {
//            throw new ServiceException(
//                    String.format("??????????????????kafkaClusterBrokerConfiguration={%s}???KafkaCluster??????", kafkaClusterDO.getKafkaClusterBrokerConfiguration()),
//                    ErrorCodeEnum.KAFKA_CLUSTER_BROKER_CONFIGURATION_DUPLICATE.getCode()
//            );
//        }
        /*
         * ????????? kafkaCluster
         */
        kafkaClusterPO = kafkaClusterManageServiceExtension.kafkaCluster2KafkaClusterPO(kafkaClusterDO);
        kafkaClusterPO.setOperator(CommonConstant.getOperator(operator));
        kafkaClusterDAO.insert(kafkaClusterPO);
        /*
         * ????????????????????????
         */
        operateRecordService.save(
                ModuleEnum.RECEIVER,
                OperationEnum.ADD,
                kafkaClusterPO.getId(),
                String.format("??????KafkaCluster={%s}??????????????????KafkaCluster??????id={%d}", JSON.toJSONString(kafkaClusterDO), kafkaClusterPO.getId()),
                operator
        );
        return kafkaClusterPO.getId();
    }

    @Override
    @Transactional
    public void updateKafkaCluster(ReceiverDO kafkaCluster, String operator) {
        this.handleUpdateKafkaCluster(kafkaCluster, operator);
    }

    /**
     * ???????????? KafkaClusterPO ??????
     *
     * @param kafkaClusterDO ????????? KafkaClusterPO ??????
     * @param operator       ?????????
     * @return ?????????????????? true????????? false????????????
     * @throws ServiceException ???????????????????????????????????????
     */
    private void handleUpdateKafkaCluster(ReceiverDO kafkaClusterDO, String operator) throws ServiceException {

        /*
         * ?????? ??????
         */
        CheckResult checkResult = kafkaClusterManageServiceExtension.checkModifyParameterKafkaCluster(kafkaClusterDO);
        if (!checkResult.getCheckResult()) {
            throw new ServiceException(checkResult.getMessage(), checkResult.getCode());
        }
        /*
         * ???????????????KafkaCluster??????????????????????????????
         */
        ReceiverDO sourceReceiverDO = getById(kafkaClusterDO.getId());
        if (null == sourceReceiverDO) {
            throw new ServiceException(
                    String.format("?????????KafkaCluster??????={id=%d}?????????????????????", kafkaClusterDO.getId()),
                    ErrorCodeEnum.KAFKA_CLUSTER_NOT_EXISTS.getCode()
            );
        }
        /*
         * ??????????????? KafkaCluster ???????????? kafkaClusterName & kafkaClusterBrokerConfiguration ???????????????????????????
         */
        if (!sourceReceiverDO.getKafkaClusterName().equals(kafkaClusterDO.getKafkaClusterName())) {
            KafkaClusterPO kafkaClusterPO = kafkaClusterDAO.selectByKafkaClusterName(kafkaClusterDO.getKafkaClusterName());
            if (null != kafkaClusterPO) {
                throw new ServiceException(
                        String.format("??????????????????kafkaClusterName={%s}???KafkaCluster??????", kafkaClusterDO.getKafkaClusterName()),
                        ErrorCodeEnum.KAFKA_CLUSTER_NAME_DUPLICATE.getCode()
                );
            }
        }

        /*
         * agent error logs ????????? topic ???????????????????????????????????????????????? topic ????????????
         */
        if(StringUtils.isNotBlank(kafkaClusterDO.getAgentErrorLogsTopic())) {
            KafkaClusterPO agentErrorLogsTopicExistsKafkaCluster = kafkaClusterDAO.getAgentErrorLogsTopicExistsKafkaCluster();
            if(null != agentErrorLogsTopicExistsKafkaCluster && !agentErrorLogsTopicExistsKafkaCluster.getId().equals(kafkaClusterDO.getId())) {
                throw new ServiceException(
                        String.format("????????????????????? agent errorlogs ????????? topic ??? kafkacluster={%s}", JSON.toJSONString(agentErrorLogsTopicExistsKafkaCluster)),
                        ErrorCodeEnum.KAFKA_CLUSTER_CREATE_OR_UPDATE_FAILED_CAUSE_BY_AGENT_ERROR_LOGS_TOPIC_EXISTS.getCode()
                );
            }
        }
        /*
         * agent metrics ????????? topic ???????????????????????????????????????????????? topic ????????????
         */
        if(StringUtils.isNotBlank(kafkaClusterDO.getAgentMetricsTopic())) {
            KafkaClusterPO agentMetricsTopicExistsKafkaCluster = kafkaClusterDAO.getAgentMetricsTopicExistsKafkaCluster();
            if(null != agentMetricsTopicExistsKafkaCluster && !agentMetricsTopicExistsKafkaCluster.getId().equals(kafkaClusterDO.getId())) {
                throw new ServiceException(
                        String.format("????????????????????? agent metrics ????????? topic ??? kafkacluster={%s}", JSON.toJSONString(agentMetricsTopicExistsKafkaCluster)),
                        ErrorCodeEnum.KAFKA_CLUSTER_CREATE_OR_UPDATE_FAILED_CAUSE_BY_AGENT_METRICS_TOPIC_EXISTS.getCode()
                );
            }
        }

//        if(!sourceReceiverDO.getKafkaClusterBrokerConfiguration().equals(kafkaClusterDO.getKafkaClusterBrokerConfiguration())) {
//            KafkaClusterPO kafkaClusterPO = kafkaClusterDAO.selectByKafkaClusterBrokerConfiguration(kafkaClusterDO.getKafkaClusterBrokerConfiguration());
//            if(null != kafkaClusterPO) {
//                throw new ServiceException(
//                        String.format("??????????????????kafkaClusterBrokerConfiguration={%s}???KafkaCluster??????", kafkaClusterDO.getKafkaClusterBrokerConfiguration()),
//                        ErrorCodeEnum.KAFKA_CLUSTER_BROKER_CONFIGURATION_DUPLICATE.getCode()
//                );
//            }
//        }

        /*
         * ??????KafkaCluster?????????db
         */
        ReceiverDO persistReceiver = kafkaClusterManageServiceExtension.updateKafkaCluster(sourceReceiverDO, kafkaClusterDO);
        KafkaClusterPO kafkaClusterPO = kafkaClusterManageServiceExtension.kafkaCluster2KafkaClusterPO(persistReceiver);
        kafkaClusterPO.setOperator(CommonConstant.getOperator(operator));
        kafkaClusterDAO.updateByPrimaryKey(kafkaClusterPO);
        /*
         * ????????????????????????
         */
        operateRecordService.save(
                ModuleEnum.RECEIVER,
                OperationEnum.EDIT,
                kafkaClusterDO.getId(),
                String.format("??????KafkaCluster={%s}??????????????????KafkaCluster??????id={%d}", JSON.toJSONString(kafkaClusterDO), kafkaClusterDO.getId()),
                operator
        );
    }

    @Override
    @Transactional
    public void deleteKafkaClusterById(Long id, boolean ignoreLogCollectTaskAndAgentRelationCheck, String operator) {
        this.handleRemoveKafkaClusterById(id, ignoreLogCollectTaskAndAgentRelationCheck, operator);
    }

    /**
     * ?????? id ???????????? KafkaClusterPO ??????
     *
     * @param id                                        ????????? id
     * @param ignoreLogCollectTaskAndAgentRelationCheck ?????????????????????kafkaCluster???????????????LogCollectTask & Agent
     * @param operator                                  ?????????
     * @throws ServiceException ???????????????????????????????????????
     *
     * todo ????????????????????????????????????
     */
    private void handleRemoveKafkaClusterById(Long id, boolean ignoreLogCollectTaskAndAgentRelationCheck, String operator) throws ServiceException {
        if (null == id) {
            throw new ServiceException(
                    "??????id????????????",
                    ErrorCodeEnum.ILLEGAL_PARAMS.getCode()
            );
        }
        KafkaClusterPO kafkaClusterPO = kafkaClusterDAO.selectByPrimaryKey(id);
        if (null == kafkaClusterPO) {
            throw new ServiceException(
                    String.format("??????id={%d}??????KafkaCluster?????????????????????????????????????????????id???{%d}???KafkaCluster??????", id, id),
                    ErrorCodeEnum.KAFKA_CLUSTER_NOT_EXISTS.getCode()
            );
        }
        if (!ignoreLogCollectTaskAndAgentRelationCheck) {
            /*
             * ???????????????kafkaCluster????????????????????? logcollecttask & agent ????????????????????????????????????????????????
             */
            List<LogCollectTaskDO> logCollectTaskDOList = logCollectTaskManageService.getLogCollectTaskListByKafkaClusterId(id);
            if(CollectionUtils.isNotEmpty(logCollectTaskDOList)) {
                throw new ServiceException(
                        String.format("?????????KafkaCluster={id=%d}??????{%d}????????????LogCollectTask?????????????????????", id, logCollectTaskDOList.size()),
                        ErrorCodeEnum.KAFKA_CLUSTER_DELETE_FAILED_CAUSE_BY_RELA_LOGCOLLECTTASK_EXISTS.getCode()
                );
            }
            List<AgentDO> agentDOList = agentManageService.getAgentListByKafkaClusterId(id);
            if(CollectionUtils.isNotEmpty(agentDOList)) {
                throw new ServiceException(
                        String.format("?????????KafkaCluster={id=%d}??????{%d}????????????Agent?????????????????????", id, agentDOList.size()),
                        ErrorCodeEnum.KAFKA_CLUSTER_DELETE_FAILED_CAUSE_BY_RELA_AGENT_EXISTS.getCode()
                );
            }
        }
        /*
         * ????????????kafkacluster??????
         */
        kafkaClusterDAO.deleteByPrimaryKey(kafkaClusterPO.getId());
        /*
         * ????????????????????????
         */
        operateRecordService.save(
                ModuleEnum.RECEIVER,
                OperationEnum.DELETE,
                id,
                String.format("??????KafkaCluster??????={id={%d}}", id),
                operator
        );
    }

    @Override
    public void pullKafkaClusterListFromRemoteAndMergeKafkaClusterInLocal() {
        long startTime = System.currentTimeMillis();//use to lo
        /*
         * ?????????????????????
         */
        List<ReceiverDO> remoteList = remoteKafkaClusterService.getKafkaClustersFromRemote();
        long getRemoteListTime = System.currentTimeMillis() - startTime;//???????????????????????????
        /*
         * ?????????????????????
         */
        long getLocalListStartTime = System.currentTimeMillis();
        List<ReceiverDO> localList = list();
        long getLocalListTime = System.currentTimeMillis() - getLocalListStartTime;//?????????????????????????????????
        /*
         * ????????????????????????????????????????????????????????????????????????????????????????????????
         */
        long compareStartTime = System.currentTimeMillis();
        ListCompareResult<ReceiverDO> listCompareResult = ListCompareUtil.compare(localList, remoteList, comparator);
        long compareTime = System.currentTimeMillis() - compareStartTime;
        /*
         * ??????????????????????????????????????????????????????????????????????????????????????????????????????????????????
         */
        long persistStartTime = System.currentTimeMillis();
        int createSuccessCount = 0, removeScucessCount = 0, modifiedSuccessCount = 0;//???????????????????????????????????????????????????
        //????????????????????????
        List<ReceiverDO> createList = listCompareResult.getCreateList();
        for (ReceiverDO receiverDO : createList) {
            Long savedId = this.createKafkaCluster(receiverDO, null);
            if (savedId > 0) {
                createSuccessCount++;
            } else {
                LOGGER.error(
                        String.format("class=KafkaClusterManageServiceImpl||method=pullKafkaClusterListFromRemoteAndMergeKafkaClusterInLocal||errMsg={%s}",
                                String.format("????????????KafkaCluster={%s}??????", JSON.toJSONString(receiverDO)))
                );
            }
        }
        //????????????????????????
        List<ReceiverDO> modifyList = listCompareResult.getModifyList();
        for (ReceiverDO receiverDO : modifyList) {
            this.updateKafkaCluster(receiverDO, null);
            modifiedSuccessCount++;
        }
        //????????????????????????
        List<ReceiverDO> removeList = listCompareResult.getRemoveList();
        for (ReceiverDO receiverDO : removeList) {
            this.deleteKafkaClusterById(receiverDO.getId(), true, null);
            removeScucessCount++;
        }
        long persistTime = System.currentTimeMillis() - persistStartTime;
        /*
         * ????????????
         */
        String logInfo = String.format(
                "class=KafkaClusterManageServiceImpl||method=pullKafkaClusterListFromRemoteAndMergeKafkaClusterInLocal||remoteListSize={%d}||localListSize={%d}||" +
                        "total-cost-time={%d}||getRemoteList-cost-time={%d}||getLocalList-cost-time={%d}||compareRemoteListAndLocalList-cost-time={%d}||persistList-cost-time={%d}||" +
                        "???????????????={%d}||???????????????={%d}||???????????????={%d}||???????????????={%d}||???????????????={%d}||???????????????={%d}||???????????????={%d}||???????????????={%d}||???????????????={%d}",
                remoteList.size(),
                localList.size(),
                System.currentTimeMillis() - startTime,
                getRemoteListTime,
                getLocalListTime,
                compareTime,
                persistTime,
                listCompareResult.getCreateList().size(),
                createSuccessCount,
                (listCompareResult.getCreateList().size() - createSuccessCount),
                listCompareResult.getRemoveList().size(),
                removeScucessCount,
                (listCompareResult.getRemoveList().size() - removeScucessCount),
                listCompareResult.getModifyList().size(),
                modifiedSuccessCount,
                (listCompareResult.getModifyList().size() - modifiedSuccessCount)
        );
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info(logInfo);
        }
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(
                    String.format(
                            "remoteList={%s}||localHostList={%s}",
                            JSON.toJSONString(remoteList),
                            JSON.toJSONString(localList)
                    )
            );
        }
    }

    @Override
    public ReceiverDO getById(Long receiverId) {
        if (receiverId == null || receiverId <= 0) {
            return null;
        }
        KafkaClusterPO kafkaClusterPO = kafkaClusterDAO.selectByPrimaryKey(receiverId);
        if (null != kafkaClusterPO) {
            return kafkaClusterManageServiceExtension.kafkaClusterPO2KafkaCluster(kafkaClusterPO);
        } else {
            return null;
        }
    }

    @Override
    public List<ReceiverDO> paginationQueryByCondition(ReceiverPaginationQueryConditionDO query) {
        String column = query.getSortColumn();
        if (column != null) {
            for (char c : column.toCharArray()) {
                if (!Character.isLetter(c) && c != '_') {
                    return Collections.emptyList();
                }
            }
        }
        List<KafkaClusterPO> kafkaClusterPOList = kafkaClusterDAO.paginationQueryByConditon(query);
        return kafkaClusterManageServiceExtension.kafkaClusterPOList2ReceiverDOList(kafkaClusterPOList);
    }

    @Override
    public Integer queryCountByCondition(ReceiverPaginationQueryConditionDO receiverPaginationQueryConditionDO) {
        return kafkaClusterDAO.queryCountByConditon(receiverPaginationQueryConditionDO);
    }

    @Override
    public List<String> listTopics(Long receiverId) {
        ReceiverDO receiverDO = getById(receiverId);
        if (null == receiverDO) {
            throw new ServiceException(
                    String.format("?????????topic??????????????????={receiverId=%d}?????????????????????", receiverDO),
                    ErrorCodeEnum.KAFKA_CLUSTER_NOT_EXISTS.getCode()
            );
        }
        //TODO????????? kafka-manager ????????????????????? topic ???
        return null;
    }

    @Override
    public boolean checkTopicLimitExists(Long kafkaClusterId, String topic) {
        /*
         * ??????kafkaClusterId????????????kafkaCluster??????
         */
        ReceiverDO receiverDO = getById(kafkaClusterId);
        /*
         * ??????kafkaCluster?????????????????????kafka-manager??????????????????????????????"????????????"????????????????????????kafka-manager??????????????????????????????????????????
         * ????????????kafkaCluster?????????????????????kafka-manager????????? ??????????????????kafkaCluster.kafkaClusterId ??????????????? & > 0
         */
        if (null == receiverDO) {
            throw new ServiceException(
                    String.format("KafkaCluster={id=%d}?????????????????????", kafkaClusterId),
                    ErrorCodeEnum.KAFKA_CLUSTER_NOT_EXISTS.getCode()
            );
        } else {
            return kafkaClusterManageServiceExtension.checkTopicLimitExists(receiverDO, topic);
        }
    }

    /**
     * ?????????????????????receiverDO?????????Broker????????????????????????
     *
     * @param receiverDO ????????????????????????
     * @return true???receiverDO?????????Broker???????????? false???receiverDO?????????Broker?????????????????????????????????
     */
    private boolean checkKafkaBrokerConnectivity(ReceiverDO receiverDO) {
        List<Pair<String, Integer>> brokerIp2PortPairList = kafkaClusterManageServiceExtension.getBrokerIp2PortPairList(receiverDO);
        for (Pair<String, Integer> brokerIp2PortPair : brokerIp2PortPairList) {
            if (!NetworkUtil.connect(brokerIp2PortPair.getKey(), brokerIp2PortPair.getValue())) {
                return false;
            }
        }
        return true;
    }

    /**
     * ????????????errorLogsSendTopic????????????receiverDO??????KafkaCluster????????????
     *
     * @param receiverDO ???????????????
     * @param topic      ???
     * @return true????????? false????????????
     */
    private boolean checkTopicExists(ReceiverDO receiverDO, String topic) {
        Long extenalKafkaClusteId = receiverDO.getKafkaClusterId();
        if (null == extenalKafkaClusteId || 0 >= extenalKafkaClusteId) {
            throw new ServiceException(
                    String.format("KafkaCluster={id=%d}?????????KafkaManager????????????????????????KafkaManager???????????????KafkaCluster", receiverDO.getId()),
                    ErrorCodeEnum.KAFKA_CLUSTER_NOT_ORIGINATED_FROM_KAFKA_MANAGER.getCode()
            );
        }
        Set<String> topicList = remoteKafkaClusterService.getTopicsByKafkaClusterId(extenalKafkaClusteId);
        if (CollectionUtils.isEmpty(topicList)) {
            return false;
        } else {
            return topicList.contains(topic);
        }
    }

    @Override
    public boolean checkConnectivity(Long kafkaClusterId, String topic) {
        ReceiverDO receiverDO = getById(kafkaClusterId);
        if (null == receiverDO) {
            throw new ServiceException(
                    String.format("KafkaCluster={id=%d}?????????????????????", kafkaClusterId),
                    ErrorCodeEnum.KAFKA_CLUSTER_NOT_EXISTS.getCode()
            );
        }
        /*
         * ??????receiverDO.kafkaClusterBrokerConfiguration??????kafkaClusterBrokerConfiguration????????????ip/hostname : port??????????????????ok
         */
        boolean kafkaBrokerConnectivityCheckResult = checkKafkaBrokerConnectivity(receiverDO);
        /*
         * ??????errorLogsSendTopic???errorLogsSendReceiverId??????KafkaCluster????????????
         */
        boolean topicExists = checkTopicExists(receiverDO, topic);
        return topicExists && kafkaBrokerConnectivityCheckResult;
    }

    @Override
    public ReceiverDO getAgentErrorLogsTopicExistsReceiver() {
        KafkaClusterPO kafkaClusterPO = kafkaClusterDAO.getAgentErrorLogsTopicExistsKafkaCluster();
        if(null != kafkaClusterPO) {
            return kafkaClusterManageServiceExtension.kafkaClusterPO2KafkaCluster(kafkaClusterPO);
        } else {
            return null;
        }
    }

    @Override
    public ReceiverDO getAgentMetricsTopicExistsReceiver() {
        KafkaClusterPO kafkaClusterPO = kafkaClusterDAO.getAgentMetricsTopicExistsKafkaCluster();
        if(null != kafkaClusterPO) {
            return kafkaClusterManageServiceExtension.kafkaClusterPO2KafkaCluster(kafkaClusterPO);
        } else {
            return null;
        }
    }

    class ReceiverDOComparator implements Comparator<ReceiverDO, Long> {
        @Override
        public Long getKey(ReceiverDO kafkaCluster) {
            return kafkaCluster.getKafkaClusterId();
        }

        @Override
        public boolean compare(ReceiverDO t1, ReceiverDO t2) {
            return t1.getKafkaClusterName().equals(t2.getKafkaClusterName()) &&
                    t1.getKafkaClusterBrokerConfiguration().equals(t2.getKafkaClusterBrokerConfiguration());
        }

        @Override
        public ReceiverDO getModified(ReceiverDO source, ReceiverDO target) {
            source.setKafkaClusterName(target.getKafkaClusterName());
            source.setKafkaClusterBrokerConfiguration(target.getKafkaClusterBrokerConfiguration());
            return source;
        }
    }

}
