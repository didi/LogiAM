package com.didichuxing.datachannel.agentmanager.core.agent.version.impl;

import com.alibaba.fastjson.JSON;
import com.didichuxing.datachannel.agentmanager.common.bean.common.CheckResult;
import com.didichuxing.datachannel.agentmanager.common.bean.domain.agent.AgentDO;
import com.didichuxing.datachannel.agentmanager.common.bean.domain.agent.version.AgentVersionDO;
import com.didichuxing.datachannel.agentmanager.common.bean.domain.agent.version.AgentVersionPaginationQueryConditionDO;
import com.didichuxing.datachannel.agentmanager.common.bean.dto.agent.version.AgentVersionDTO;
import com.didichuxing.datachannel.agentmanager.common.bean.po.agent.version.AgentVersionPO;
import com.didichuxing.datachannel.agentmanager.common.constant.CommonConstant;
import com.didichuxing.datachannel.agentmanager.common.enumeration.ErrorCodeEnum;
import com.didichuxing.datachannel.agentmanager.common.enumeration.operaterecord.ModuleEnum;
import com.didichuxing.datachannel.agentmanager.common.enumeration.operaterecord.OperationEnum;
import com.didichuxing.datachannel.agentmanager.common.exception.ServiceException;
import com.didichuxing.datachannel.agentmanager.core.agent.manage.AgentManageService;
import com.didichuxing.datachannel.agentmanager.core.agent.operation.task.AgentOperationSubTaskManageService;
import com.didichuxing.datachannel.agentmanager.core.agent.operation.task.AgentOperationTaskManageService;
import com.didichuxing.datachannel.agentmanager.core.agent.version.AgentVersionManageService;
import com.didichuxing.datachannel.agentmanager.core.common.OperateRecordService;
import com.didichuxing.datachannel.agentmanager.persistence.mysql.AgentVersionMapper;
import com.didichuxing.datachannel.agentmanager.remote.storage.AbstractStorageService;
import com.didichuxing.datachannel.agentmanager.thirdpart.agent.version.AgentVersionManageServiceExtension;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@org.springframework.stereotype.Service
public class AgentVersionManageServiceImpl implements AgentVersionManageService {

    @Autowired
    private AgentVersionManageServiceExtension agentVersionManageServiceExtension;

    @Autowired
    private AgentVersionMapper agentVersionDAO;

    @Autowired
    private AbstractStorageService giftStorageService;

    @Autowired
    private AgentManageService agentManageService;

    @Autowired
    private OperateRecordService operateRecordService;

    @Autowired
    private AgentOperationSubTaskManageService agentOperationSubTaskManageService;

    @Autowired
    private AgentOperationTaskManageService agentOperationTaskManageService;

    @Override
    @Transactional
    public Long createAgentVersion(AgentVersionDTO agentVersionDTO, String operator) {
        /*
         * ????????????
         */
        CheckResult checkResult = agentVersionManageServiceExtension.checkCreateParameterAgentVersion(agentVersionDTO);
        if(!checkResult.getCheckResult()) {
            throw new ServiceException(checkResult.getMessage(), checkResult.getCode());
        }
        /*
         * ??????agent????????????????????????????????????
         */
        AgentVersionPO agentVersionPO = agentVersionDAO.selectByfileMd5(agentVersionDTO.getFileMd5());
        if(null != agentVersionPO) {
            throw new ServiceException(
                    String.format("Agent????????????={fileMd5=%s}?????????????????????", agentVersionDTO.getFileMd5()),
                    ErrorCodeEnum.AGENT_PACKAGE_FILE_EXISTS.getCode()
            );
        }
        /*
         * ????????????
         */
        uploadAgentFile(agentVersionDTO);
        /*
         * ?????? AgentVersion ??????
         */
        return this.handleCreateAgentVersion(agentVersionDTO, operator);
    }

    @Override
    @Transactional
    public void updateAgentVersion(AgentVersionDTO agentVersionDTO, String operator) {
        /*
         * ????????????
         */
        CheckResult checkResult = agentVersionManageServiceExtension.checkUpdateParameterAgentVersion(agentVersionDTO);
        if(!checkResult.getCheckResult()) {
            throw new ServiceException(checkResult.getMessage(), checkResult.getCode());
        }
        /*
         * ?????? AgentVersion ??????
         */
        this.handleUpdateAgentVersion(agentVersionDTO, operator);//?????? agent ??????
    }

    @Override
    @Transactional
    public void deleteAgentVersion(Long agentVersionId, String operator) {
        handleDeleteAgentVersion(agentVersionId, operator);
    }

    @Override
    public List<AgentVersionDO> paginationQueryByConditon(AgentVersionPaginationQueryConditionDO query) {
        String column = query.getSortColumn();
        if (column != null) {
            for (char c : column.toCharArray()) {
                if (!Character.isLetter(c) && c != '_') {
                    return Collections.emptyList();
                }
            }
        }
        List<AgentVersionPO> agentVersionPOList = agentVersionDAO.paginationQueryByConditon(query);
        if(CollectionUtils.isEmpty(agentVersionPOList)) {
            return Collections.emptyList();
        }
        return agentVersionManageServiceExtension.agentVersionPOList2AgentVersionDOList(agentVersionPOList);
    }

    @Override
    public Integer queryCountByCondition(AgentVersionPaginationQueryConditionDO agentVersionPaginationQueryConditionDO) {
        return agentVersionDAO.queryCountByConditon(agentVersionPaginationQueryConditionDO);
    }

    @Override
    public String getAgentInstallFileDownloadUrl(Long agentVersionId) {
        AgentVersionDO agentVersionDO = getById(agentVersionId);
        if(null == agentVersionDO) {
            throw new ServiceException(
                    String.format("AgentVersion={id=%d}?????????????????????", agentVersionId),
                    ErrorCodeEnum.AGENT_VERSION_NOT_EXISTS.getCode()
            );
        }
        String fileName = agentVersionDO.getFileName();
        String fileMd5 = agentVersionDO.getFileMd5();
        String downloadUrl = giftStorageService.getDownloadUrl(fileName, fileMd5);
        downloadUrl = downloadUrl.substring(0, downloadUrl.indexOf("?"));//?????????????????????????????? ??????????????? ??? ??? sql ??????????????????????????????????????????
        return downloadUrl;
    }

    /**
     * ?????? id ???????????? agentVersion ??????
     * @param agentVersionId ????????? agentVersion ?????? id ???
     * @param operator ?????????
     * @return ??????????????????
     * @throws ServiceException ?????????????????????????????????????????????
     */
    private void handleDeleteAgentVersion(Long agentVersionId, String operator) throws ServiceException {
        /*
         * ??????????????? agentVersion ????????????????????????
         */
        AgentVersionDO agentVersionDO = getById(agentVersionId);
        if(null == agentVersionDO) {
            throw new ServiceException(
                    String.format("????????? AgentVersion={id=%d}?????????????????????", agentVersionId),
                    ErrorCodeEnum.AGENT_VERSION_NOT_EXISTS.getCode()
            );
        }
        /*
         * ??????????????? agentVersion ????????? agent ?????????
         */
        List<AgentDO> relationAgentDOList = agentManageService.getAgentsByAgentVersionId(agentVersionId);
        if(CollectionUtils.isNotEmpty(relationAgentDOList)) {
            throw new ServiceException(
                    String.format("????????? AgentVersion={id=%d}???????????????????????????Agent={%s}????????????????????? AgentVersion ", agentVersionId, JSON.toJSONString(relationAgentDOList)),
                    ErrorCodeEnum.AGENT_VERSION_RELATION_EXISTS.getCode()
            );
        }
        /*
         * ??????????????? agentVersion ?????????????????? AgentOperationSubTask & AgentOperationTask ?????????
         */
        boolean agentOperationTaskExistsByAgentVersionId = agentOperationTaskManageService.unfinishedAgentOperationTaskExistsByAgentVersionId(agentVersionId);
        boolean agentOperationSubTaskExistsByAgentVersionId = agentOperationSubTaskManageService.unfinishedAgentOperationSubTaskExistsByAgentVersionId(agentVersionId);
        if(agentOperationTaskExistsByAgentVersionId) {
            throw new ServiceException(
                    String.format("?????????AgentVersion={id=%d}???????????????????????????AgentOperationTask?????????????????????AgentVersion ", agentVersionId),
                    ErrorCodeEnum.AGENT_VERSION_RELATION_EXISTS.getCode()
            );
        }
        if(agentOperationSubTaskExistsByAgentVersionId) {
            throw new ServiceException(
                    String.format("?????????AgentVersion={id=%d}???????????????????????????AgentOperationSubTask?????????????????????AgentVersion ", agentVersionId),
                    ErrorCodeEnum.AGENT_VERSION_RELATION_EXISTS.getCode()
            );
        }
        /*
         * ?????? agentVersion
         */
        agentVersionDAO.deleteByPrimaryKey(agentVersionId);
        /*
         * ????????????????????????
         */
        /*operateRecordService.save(
                ModuleEnum.AGENT_VERSION,
                OperationEnum.DELETE,
                agentVersionId,
                String.format("??????KafkaCluster??????={id={%d}}", agentVersionId),
                operator
        );*/
    }

    @Override
    public AgentVersionDO getByVersion(String version) {
        AgentVersionPO agentVersionPO = agentVersionDAO.selectByVersion(version);
        if(agentVersionPO == null) {
            return null;
        }
        return agentVersionManageServiceExtension.agentVersionPO2AgentVersionDO(agentVersionPO);
    }

    @Override
    public AgentVersionDO getById(Long agentVersionId) {
        AgentVersionPO agentVersionPO = agentVersionDAO.selectByPrimaryKey(agentVersionId);
        if(agentVersionPO == null) {
            return null;
        }
        return agentVersionManageServiceExtension.agentVersionPO2AgentVersionDO(agentVersionPO);
    }

    @Override
    public List<AgentVersionDO> list() {
        List<AgentVersionPO> agentVersionPOList = agentVersionDAO.selectAll();
        if(CollectionUtils.isEmpty(agentVersionPOList)) {
            return new ArrayList<>();
        }
        return agentVersionManageServiceExtension.agentVersionPOList2AgentVersionDOList(agentVersionPOList);
    }

    /**
     * ?????? AgentVersion
     * @param agentVersionDTO ????????? AgentVersion ??????
     * @param operator ?????????
     * @return ?????????AgentVersion?????? id
     * @throws ServiceException ???????????????????????????????????????
     */
    private Long handleCreateAgentVersion(AgentVersionDTO agentVersionDTO, String operator) throws ServiceException {
        /*
         * ???????????????AgentVersion??????version???????????????????????????
         */
        String agentVersion = agentVersionDTO.getAgentVersion();
        AgentVersionDO agentVersionDO = getByVersion(agentVersion);
        if(null != agentVersionDO) {
            throw new ServiceException(
                    String.format("?????????AgentVersion??????version={%s}?????????????????????", agentVersion),
                    ErrorCodeEnum.AGENT_VERSION_DUPLICATE.getCode()
            );
        }
        /*
         * ??????AgentVersion
         */
        AgentVersionPO agentVersionPO = agentVersionManageServiceExtension.AgentVersionDTO2AgentVersionPO(agentVersionDTO);
        agentVersionPO.setOperator(CommonConstant.getOperator(operator));
        agentVersionDAO.insert(agentVersionPO);
        /*
         * ????????????????????????
         */
        /*operateRecordService.save(
                ModuleEnum.AGENT_VERSION,
                OperationEnum.ADD,
                agentVersionPO.getId(),
                String.format("??????AgentVersion={%s}??????????????????AgentVersion??????id={%d}", JSON.toJSONString(agentVersionPO), agentVersionPO.getId()),
                operator
        );*/
        return agentVersionPO.getId();
    }

    /**
     * ??????agent????????????
     * @param agentVersionDTO ??????agent???????????????AgentVersionDTO??????
     * @throws ServiceException ??????????????????????????????????????????
     */
    private void uploadAgentFile(AgentVersionDTO agentVersionDTO) throws ServiceException {
        try {
            if (!giftStorageService.upload(
                    agentVersionDTO.getAgentPackageName(),
                    agentVersionDTO.getFileMd5(),
                    agentVersionDTO.getUploadFile())
            ) {
                throw new ServiceException(
                        String.format("??????={%s}????????????", agentVersionDTO.getAgentPackageName()),
                        ErrorCodeEnum.FILE_UPLOAD_FAILED.getCode()
                );
            }
        } catch (Exception ex) {
            throw new ServiceException(
                    String.format("??????={%s}????????????,????????????%s", agentVersionDTO.getAgentPackageName(), ex.getMessage()),
                    ErrorCodeEnum.FILE_UPLOAD_FAILED.getCode()
            );
        }
    }

    /**
     * ????????????AgentVersionDTO??????
     * @param agentVersionDTO ?????????AgentVersionDTO??????
     * @param operator ?????????
     * @throws ServiceException ???????????????????????????????????????
     */
    private void handleUpdateAgentVersion(AgentVersionDTO agentVersionDTO, String operator) throws ServiceException {
        Long agentVersionId = agentVersionDTO.getId();
        AgentVersionPO agentVersionPO = agentVersionDAO.selectByPrimaryKey(agentVersionId);
        if(null == agentVersionPO) {
            throw new ServiceException(
                    String.format("?????????AgentVersion??????={id=%d}?????????????????????", agentVersionId),
                    ErrorCodeEnum.AGENT_VERSION_NOT_EXISTS.getCode()
            );
        }
        agentVersionPO.setDescription(agentVersionDTO.getAgentVersionDescription());
        agentVersionPO.setOperator(CommonConstant.getOperator(operator));
        agentVersionDAO.updateByPrimaryKey(agentVersionPO);
        /*
         * ????????????????????????
         */
        /*operateRecordService.save(
                ModuleEnum.AGENT_VERSION,
                OperationEnum.EDIT,
                agentVersionDTO.getId(),
                String.format("??????AgentVersion={%s}??????????????????AgentVersion??????id={%d}", JSON.toJSONString(agentVersionPO), agentVersionDTO.getId()),
                operator
        );*/
    }

}
