package com.didichuxing.datachannel.agentmanager.core.agent.manage;

import com.didichuxing.datachannel.agentmanager.common.bean.domain.agent.AgentDO;
import com.didichuxing.datachannel.agentmanager.common.enumeration.agent.AgentHealthLevelEnum;
import com.didichuxing.datachannel.agentmanager.core.ApplicationTests;
import com.didichuxing.datachannel.agentmanager.core.host.HostManageService;
import com.didichuxing.datachannel.agentmanager.core.kafkacluster.KafkaClusterManageService;
import com.didichuxing.datachannel.agentmanager.core.logcollecttask.manage.LogCollectTaskManageService;
import com.didichuxing.datachannel.agentmanager.core.service.ServiceManageService;
import com.didichuxing.datachannel.agentmanager.persistence.mysql.AgentVersionMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

//@Transactional
//@Rollback
public class AgentManageServiceTest extends ApplicationTests {

    @Autowired
    private AgentManageService agentManageService;

    @Autowired
    private HostManageService hostManageService;

    @Autowired
    private KafkaClusterManageService kafkaClusterManageService;

    @Autowired
    private AgentVersionMapper agentVersionMapper;

    @Autowired
    private ServiceManageService serviceManageService;

    @Autowired
    private LogCollectTaskManageService logCollectTaskManageService;

//    @Test
//    public void agentCreateCaseAgentRegisterTest() {
//        initData(false);
//        AgentDO agentDO = agentManageService.getById(agentDOCreated.getId()).getData();
//        assert agentDO.getHostName().equals(agentDOCreated.getHostName());
//        assert agentDO.getIp().equals(agentDOCreated.getIp());
//        assert agentDO.getCollectType().equals(agentDOCreated.getCollectType());
//        assert agentDO.getAgentVersionId().equals(agentDOCreated.getAgentVersionId());
//    }
//
//    @Test
//    public void agentUpdateTest() {
//
//        initData(false);
//        AgentDO agentDOBeforeUpdate = agentManageService.getById(agentDOCreated.getId()).getData();
//        assert agentDOBeforeUpdate.getConfigurationVersion() == 0;
//        agentDOBeforeUpdate.setErrorLogsSendTopic(UUID.randomUUID().toString());
//        agentDOBeforeUpdate.setErrorLogsSendReceiverId(errorLogsReceiverDO.getId());
//        agentDOBeforeUpdate.setMetricsSendTopic(UUID.randomUUID().toString());
//        agentDOBeforeUpdate.setMetricsSendReceiverId(metricsReceiverDO.getId());
//        agentDOBeforeUpdate.setAdvancedConfigurationJsonString(UUID.randomUUID().toString());
//        agentDOBeforeUpdate.setByteLimitThreshold(new Random().nextLong());
//        agentDOBeforeUpdate.setCpuLimitThreshold(new Random().nextInt());
//        agentManageService.updateAgent(agentDOBeforeUpdate, null);
//        AgentDO agentDOAfterUpdate = agentManageService.getById(agentDOCreated.getId()).getData();
//        assert agentDOAfterUpdate.getConfigurationVersion() == 1;
//        assert EqualsBuilder.reflectionEquals(agentDOAfterUpdate, agentDOBeforeUpdate, "configurationVersion", "operator", "modifyTime", "createTime");
//
//    }
//
//    @Test
//    public void agentHealthLevelUpdateTest() {
//        initData(false);
//        AgentDO agentDOBeforeUpdate = agentManageService.getById(agentDOCreated.getId()).getData();
//        Result updateAgentResult = agentManageService.updateAgentHealthLevel(agentDOCreated.getId(), AgentHealthLevelEnum.RED);
//        assert updateAgentResult.success();
//        AgentDO agentDOAfterUpdate = agentManageService.getById(agentDOCreated.getId()).getData();
//        assert agentDOAfterUpdate.getHealthLevel().equals(AgentHealthLevelEnum.RED.getCode());
//    }
//
//    @Test
//    public void agentRemoveTest() {
//
//        initData(false);
//        Result result = agentManageService.deleteAgentByHostName(agentDOCreated.getHostName(), true, true, null);
//        assert result.success();
//        assert agentManageService.getAgentByHostName(agentDOCreated.getHostName()) == null;
//
//    }
//
//    private String hostName = "????????????_1";
//    private String ip = "192.168.0.1";
//    private AgentDO agentDOCreated;
//    private ReceiverDO metricsReceiverDO;
//    private ReceiverDO errorLogsReceiverDO;
//
//    private void initData(boolean agentInstall) {
//
//        //?????????????????????
//        HostDO host = new HostDO();
//        host.setContainer(YesOrNoEnum.NO.getCode());
//        host.setDepartment("department_test");
//        host.setHostName(hostName);
//        host.setIp(ip);
//        host.setMachineZone("gz01");
//        host.setParentHostName(StringUtils.EMPTY);
//        Result<Long> result = hostManageService.createHost(host, null);
//        assert result.success();
//        assert result.getData() > 0;
//
//        //?????? agent version ??????
//        AgentVersionPO agentVersionPO = new AgentVersionPO();
//        agentVersionPO.setDescription(UUID.randomUUID().toString());
//        agentVersionPO.setFileMd5(UUID.randomUUID().toString());
//        agentVersionPO.setFileName(UUID.randomUUID().toString());
//        agentVersionPO.setFileType(1);
//        agentVersionPO.setVersion("agent_version_001");
//        agentVersionPO.setOperator(Constant.getOperator(null));
//        assert agentVersionMapper.insert(agentVersionPO) > 0;
//        assert agentVersionPO.getId() > 0;
//
//        //?????? agent ??????
//        agentDOCreated = new AgentDO();
//        agentDOCreated.setHostName(hostName);
//        agentDOCreated.setIp(ip);
//        agentDOCreated.setCollectType(AgentCollectTypeEnum.COLLECT_HOST_AND_CONTAINERS.getCode());
//        agentDOCreated.setAgentVersionId(agentVersionPO.getId());
//        agentDOCreated.setId(agentManageService.createAgent(agentDOCreated, agentInstall, null));
//
//        //?????? agent ???????????? errorlogs &  metrics ????????????????????????
//        metricsReceiverDO = new ReceiverDO();
//        metricsReceiverDO.setKafkaClusterBrokerConfiguration(UUID.randomUUID().toString());
//        metricsReceiverDO.setKafkaClusterId(new Random().nextLong());
//        metricsReceiverDO.setKafkaClusterName(UUID.randomUUID().toString());
//        metricsReceiverDO.setKafkaClusterProducerInitConfiguration(UUID.randomUUID().toString());
//        result = kafkaClusterManageService.createKafkaCluster(metricsReceiverDO, null);
//        assert result.success();
//        metricsReceiverDO.setId(result.getData());
//
//        errorLogsReceiverDO = new ReceiverDO();
//        errorLogsReceiverDO.setKafkaClusterBrokerConfiguration(UUID.randomUUID().toString());
//        errorLogsReceiverDO.setKafkaClusterId(new Random().nextLong());
//        errorLogsReceiverDO.setKafkaClusterName(UUID.randomUUID().toString());
//        errorLogsReceiverDO.setKafkaClusterProducerInitConfiguration(UUID.randomUUID().toString());
//        result = kafkaClusterManageService.createKafkaCluster(errorLogsReceiverDO, null);
//        assert result.success();
//        errorLogsReceiverDO.setId(result.getData());
//
//    }
//
//    @Test
//    public void testGetAgentsByAgentVersionId() {
//
//        initData(false);
//        Result<List<AgentDO>> result = agentManageService.getAgentsByAgentVersionId(agentDOCreated.getAgentVersionId());
//        assert result.success();
//        assert result.getData().size() == 1;
//
//    }
//
//    @Test
//    public void testGetLogCollectTaskListByAgentId() throws ParseException {
//
//        Long agentId = initTestGetAgentCollectConfigurationByHostNameWhenCollectHostOnlyData();
//        Result<List<LogCollectTaskDO>> logCollectTaskDOListResult = agentManageService.getLogCollectTaskListByAgentId(agentId);
//        assert logCollectTaskDOListResult.success();
//        assert logCollectTaskDOListResult.getData().size() == 8;
//
//    }
//
//    /**
//     * ????????????????????????????????? "testGetAgentCollectConfigurationByHostNameWhenCollectHostOnly" ???????????????AgentPO?????????????????????????????????????????????????????????
//     * 5??????????????????["????????????_1","????????????_2","????????????_3","????????????_4","????????????_5"]
//     * 4??????????????????????????????
//     *  1?????????????????????{????????????????????????_1 ip???192.168.0.1 ??????????????????["????????????_1","????????????_2"]}
//     *  3??????????????????{????????????????????????_1 ip???192.168.0.2 ??????????????????["????????????_3"]} {????????????????????????_2 ip???192.168.0.3 ??????????????????["????????????_4"]} {????????????????????????_3 ip???192.168.0.4 ??????????????????["????????????_5"]}
//     * 5??????????????????????????????????????????
//     *  {????????????????????????????????????????????????_1 ??????????????????["????????????_1","????????????_2"]}
//     *  {????????????????????????????????????????????????_2 ??????????????????["????????????_3"]}
//     *  {????????????????????????????????????????????????_3 ??????????????????["????????????_4"]}
//     *  {????????????????????????????????????????????????_4 ??????????????????["????????????_5"]}
//     *  {????????????????????????????????????????????????_5 ??????????????????["????????????_1","????????????_2","????????????_3","????????????_4","????????????_5"]}
//     * 1???Agent?????????????????????
//     *  {????????????????????????_1 ip???192.168.0.1 collect_type???0} -- ?????????????????????
//     *  @return ????????? agent id
//     *  @throws ParseException ??????????????????
//     */
//    private Long initTestGetAgentCollectConfigurationByHostNameWhenCollectHostOnlyData() throws ParseException {
//        initHostList();
//        initServiceAndServiceHostRelationList();
//        initReceivers();
//        initLogCollectTaskAndServiceLogCollectTaskRelationList();
//        return initAgentCollectHostOnly();
//    }
//
//    private static final Long metricsKafkaClusterId = 1L;
//    private static final Long errorLogsKafkaClusterId = 2L;
//    private static final Long logdataKafkaClusterId = 3L;
//    /**
//     * ?????????3???kafka????????????????????? metrics??????errorlogs??????log data ???
//     */
//    private void initReceivers() {
//
//        ReceiverDO metricsReceiverDO = new ReceiverDO();
//        metricsReceiverDO.setKafkaClusterBrokerConfiguration(UUID.randomUUID().toString());
//        metricsReceiverDO.setKafkaClusterId(metricsKafkaClusterId);
//        metricsReceiverDO.setKafkaClusterName(UUID.randomUUID().toString());
//        metricsReceiverDO.setKafkaClusterProducerInitConfiguration(UUID.randomUUID().toString());
//        assert kafkaClusterManageService.createKafkaCluster(metricsReceiverDO, null).success();
//
//        ReceiverDO errorLogsReceiverDO = new ReceiverDO();
//        errorLogsReceiverDO.setKafkaClusterBrokerConfiguration(UUID.randomUUID().toString());
//        errorLogsReceiverDO.setKafkaClusterId(errorLogsKafkaClusterId);
//        errorLogsReceiverDO.setKafkaClusterName(UUID.randomUUID().toString());
//        errorLogsReceiverDO.setKafkaClusterProducerInitConfiguration(UUID.randomUUID().toString());
//        assert kafkaClusterManageService.createKafkaCluster(errorLogsReceiverDO, null).success();
//
//        ReceiverDO logdataReceiverDO = new ReceiverDO();
//        logdataReceiverDO.setKafkaClusterBrokerConfiguration(UUID.randomUUID().toString());
//        logdataReceiverDO.setKafkaClusterId(logdataKafkaClusterId);
//        logdataReceiverDO.setKafkaClusterName(UUID.randomUUID().toString());
//        logdataReceiverDO.setKafkaClusterProducerInitConfiguration(UUID.randomUUID().toString());
//        assert kafkaClusterManageService.createKafkaCluster(logdataReceiverDO, null).success();
//
//    }
//
//    /**
//     * ?????????????????????????????????Agent??????
//     * {????????????????????????_1 ip???192.168.0.1 collect_type???0} -- ?????????????????????
//     * @return agent?????????????????????
//     */
//    private Long initAgentCollectHostOnly() {
//
//        AgentVersionPO agentVersionPO = new AgentVersionPO();
//        agentVersionPO.setDescription(UUID.randomUUID().toString());
//        agentVersionPO.setFileMd5(UUID.randomUUID().toString());
//        agentVersionPO.setFileName(UUID.randomUUID().toString());
//        agentVersionPO.setFileType(1);
//        agentVersionPO.setVersion("agent_version_001");
//        agentVersionPO.setOperator(Constant.getOperator(null));
//        assert agentVersionMapper.insert(agentVersionPO) > 0;
//        assert agentVersionPO.getId() > 0;
//
//        AgentDO agent = new AgentDO();
//        agent.setAdvancedConfigurationJsonString("advancedConfigurationJsonString");
//        agent.setByteLimitThreshold(1024 * 1024 * 9L);
//        agent.setCollectType(AgentCollectTypeEnum.COLLECT_HOST_AND_CONTAINERS.getCode());
//        agent.setCpuLimitThreshold(50);
//        agent.setHostName("????????????_1");
//        agent.setIp("192.168.0.1");
//        agent.setAgentVersionId(agentVersionPO.getId());
//        agent.setConfigurationVersion(AgentConstant.AGENT_CONFIGURATION_VERSION_INIT);
//        agent.setHealthLevel(AgentHealthLevelEnum.GREEN.getCode());
//        agent.setMetricsSendReceiverId(kafkaClusterManageService.getKafkaClusterByKafkaClusterId(metricsKafkaClusterId).getData().getId());
//        agent.setMetricsSendTopic("topic_metrics");
//        agent.setErrorLogsSendReceiverId(kafkaClusterManageService.getKafkaClusterByKafkaClusterId(errorLogsKafkaClusterId).getData().getId());
//        agent.setErrorLogsSendTopic("topic_error_logs");
//
//        Long result = agentManageService.createAgent(agent,true, null);
//        assert result > 0;
//        return result;
//    }
//
//    /**
//     * ????????? 5 ??????????????????????????? & ?????????????????? ?????????????????????????????????
//     *      *  {????????????????????????????????????????????????_1 ??????????????????["????????????_1","????????????_2"]}
//     *      *  {????????????????????????????????????????????????_2 ??????????????????["????????????_3"]}
//     *      *  {????????????????????????????????????????????????_3 ??????????????????["????????????_4"]}
//     *      *  {????????????????????????????????????????????????_4 ??????????????????["????????????_5"]}
//     *      *  {????????????????????????????????????????????????_5 ??????????????????["????????????_1","????????????_2","????????????_3","????????????_4","????????????_5"]}
//     */
//    private void initLogCollectTaskAndServiceLogCollectTaskRelationList() throws ParseException {
//
//        Result<ServiceDO> relationService1 = serviceManageService.getServiceByServiceName("????????????_1");
//        Result<ServiceDO> relationService2 = serviceManageService.getServiceByServiceName("????????????_2");
//        Result<ServiceDO> relationService3 = serviceManageService.getServiceByServiceName("????????????_3");
//        Result<ServiceDO> relationService4 = serviceManageService.getServiceByServiceName("????????????_4");
//        Result<ServiceDO> relationService5 = serviceManageService.getServiceByServiceName("????????????_5");
//
//        assert relationService1.success() && null != relationService1.getData();
//        assert relationService2.success() && null != relationService2.getData();
//        assert relationService3.success() && null != relationService3.getData();
//        assert relationService4.success() && null != relationService4.getData();
//        assert relationService5.success() && null != relationService5.getData();
//
//        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//
//        /*
//         *
//         * ?????? 5 ??????????????????????????????????????????
//         *      {????????????????????????????????????????????????_1 ??????????????????["????????????_1","????????????_2"]}
//         *      {????????????????????????????????????????????????_2 ??????????????????["????????????_3"]}
//         *      {????????????????????????????????????????????????_3 ??????????????????["????????????_4"]}
//         *      {????????????????????????????????????????????????_4 ??????????????????["????????????_5"]}
//         *      {????????????????????????????????????????????????_5 ??????????????????["????????????_1","????????????_2","????????????_3","????????????_4","????????????_5"]}
//         */
//
//        //logCollectTask1
//        LogCollectTaskDO logCollectTask1 = new LogCollectTaskDO();
//        logCollectTask1.setAdvancedConfigurationJsonString("advancedConfigurationJsonString");
//        logCollectTask1.setHostFilterRuleLogicJsonString("hostFilterRuleLogicJsonString");
//        logCollectTask1.setKafkaClusterId(kafkaClusterManageService.getKafkaClusterByKafkaClusterId(logdataKafkaClusterId).getData().getId());
//        logCollectTask1.setLimitPriority(LogCollectTaskLimitPriorityLevelEnum.HIGH.getCode());
//        logCollectTask1.setLogCollectTaskName("????????????????????????_1");
//        logCollectTask1.setLogCollectTaskRemark("????????????????????????_remark_" + UUID.randomUUID().toString());
//        logCollectTask1.setLogCollectTaskType(LogCollectTaskTypeEnum.NORMAL_COLLECT.getCode());
//        logCollectTask1.setLogCollectTaskStatus(YesOrNoEnum.YES.getCode());
//        logCollectTask1.setSendTopic("topic_test_" + UUID.randomUUID().toString());
//        logCollectTask1.setConfigurationVersion(0);
//        logCollectTask1.setOldDataFilterType(0);
//        logCollectTask1.setCollectStartTimeBusiness(System.currentTimeMillis());
//        logCollectTask1.setLogContentFilterRuleLogicJsonString(UUID.randomUUID().toString());
//
//        List<DirectoryLogCollectPathDO> directoryLogCollectPathList = new ArrayList<>();
//        DirectoryLogCollectPathDO directoryLogCollectPath = new DirectoryLogCollectPathDO();
//        directoryLogCollectPath.setCollectFilesFilterRegularPipelineJsonString("collectFilesFilterRegularPipelineJsonString");
//        directoryLogCollectPath.setDirectoryCollectDepth(1);
//        directoryLogCollectPath.setCharset("utf-8");
//        directoryLogCollectPath.setFdOffsetExpirationTimeMs(24 * 3600 * 1000L);
//        directoryLogCollectPath.setLogContentSliceRuleLogicJsonString("logContentSliceRuleLogicJsonString");
//        directoryLogCollectPath.setPath("/home/logger/dir/test/");
//        directoryLogCollectPath.setLogContentSliceRuleLogicJsonString("logContentSliceRuleLogicJsonString");
//        directoryLogCollectPath.setMaxBytesPerLogEvent(100L);
//        directoryLogCollectPathList.add(directoryLogCollectPath);
//        logCollectTask1.setDirectoryLogCollectPathList(directoryLogCollectPathList);
//
//        List<FileLogCollectPathDO> fileLogCollectPathList = new ArrayList<>();
//        FileLogCollectPathDO fileLogCollectPath = new FileLogCollectPathDO();
//        fileLogCollectPath.setFileNameSuffixMatchRuleLogicJsonString("collectFileSuffixMatchRuleLogicJsonString");
//        fileLogCollectPath.setCharset("utf-8");
//        fileLogCollectPath.setFdOffsetExpirationTimeMs(24 * 3600 * 1000L);
//        fileLogCollectPath.setLogContentSliceRuleLogicJsonString("logContentSliceRuleLogicJsonString");
//        fileLogCollectPath.setPath("/home/logger/file/test.log");
//        fileLogCollectPath.setMaxBytesPerLogEvent(1000L);
//        fileLogCollectPath.setLogContentSliceRuleLogicJsonString("logContentSliceRuleLogicJsonString");
//        fileLogCollectPath.setCollectDelayThresholdMs(1000L);
//        fileLogCollectPathList.add(fileLogCollectPath);
//        logCollectTask1.setFileLogCollectPathList(fileLogCollectPathList);
//
//        logCollectTask1.setServiceIdList(Arrays.asList(relationService1.getData().getId(), relationService2.getData().getId()));
//        Result<Long> result = logCollectTaskManageService.createLogCollectTask(logCollectTask1, null);
//        assert result.success();
//        assert result.getData() > 0;
//
//        //logCollectTask2
//        LogCollectTaskDO logCollectTask2 = new LogCollectTaskDO();
//        logCollectTask2.setAdvancedConfigurationJsonString("advancedConfigurationJsonString");
//        logCollectTask2.setCollectStartTimeBusiness(System.currentTimeMillis());
//        logCollectTask2.setCollectEndTimeBusiness(System.currentTimeMillis());
//        logCollectTask2.setHostFilterRuleLogicJsonString("hostFilterRuleLogicJsonString");
//        logCollectTask2.setKafkaClusterId(kafkaClusterManageService.getKafkaClusterByKafkaClusterId(logdataKafkaClusterId).getData().getId());
//        logCollectTask2.setLimitPriority(LogCollectTaskLimitPriorityLevelEnum.HIGH.getCode());
//        logCollectTask2.setLogCollectTaskName("????????????????????????_2");
//        logCollectTask2.setLogCollectTaskRemark("????????????????????????_remark_" + UUID.randomUUID().toString());
//        logCollectTask2.setLogCollectTaskType(LogCollectTaskTypeEnum.TIME_SCOPE_COLLECT.getCode());
//        logCollectTask2.setLogCollectTaskStatus(YesOrNoEnum.YES.getCode());
//        logCollectTask2.setSendTopic("topic_test_" + UUID.randomUUID().toString());
//        logCollectTask2.setConfigurationVersion(0);
//        logCollectTask2.setOldDataFilterType(2);
//        logCollectTask2.setCollectStartTimeBusiness(System.currentTimeMillis());
//        logCollectTask2.setCollectEndTimeBusiness(System.currentTimeMillis());
//        logCollectTask2.setLogContentFilterRuleLogicJsonString(UUID.randomUUID().toString());
//        logCollectTask2.setLogCollectTaskExecuteTimeoutMs(1024L);
//
//        List<DirectoryLogCollectPathDO> directoryLogCollectPathList2 = new ArrayList<>();
//        DirectoryLogCollectPathDO directoryLogCollectPath2 = new DirectoryLogCollectPathDO();
//        directoryLogCollectPath2.setCollectFilesFilterRegularPipelineJsonString("collectFilesFilterRegularPipelineJsonString");
//        directoryLogCollectPath2.setDirectoryCollectDepth(9);
//        directoryLogCollectPath2.setCharset("utf-8");
//        directoryLogCollectPath2.setFdOffsetExpirationTimeMs(24 * 3600 * 1000L);
//        directoryLogCollectPath2.setLogContentSliceRuleLogicJsonString("logContentSliceRuleLogicJsonString");
//        directoryLogCollectPath2.setPath("/home/logger/dir/test/");
//        directoryLogCollectPath2.setLogContentSliceRuleLogicJsonString("logContentSliceRuleLogicJsonString");
//        directoryLogCollectPath2.setMaxBytesPerLogEvent(100L);
//        directoryLogCollectPathList2.add(directoryLogCollectPath2);
//        logCollectTask2.setDirectoryLogCollectPathList(directoryLogCollectPathList2);
//
//        List<FileLogCollectPathDO> fileLogCollectPathList2 = new ArrayList<>();
//        FileLogCollectPathDO fileLogCollectPath2 = new FileLogCollectPathDO();
//        fileLogCollectPath2.setFileNameSuffixMatchRuleLogicJsonString("collectFileSuffixMatchRuleLogicJsonString");
//        fileLogCollectPath2.setCharset("utf-8");
//        fileLogCollectPath2.setFdOffsetExpirationTimeMs(24 * 3600 * 1000L);
//        fileLogCollectPath2.setLogContentSliceRuleLogicJsonString("logContentSliceRuleLogicJsonString");
//        fileLogCollectPath2.setPath("/home/logger/file/test.log");
//        fileLogCollectPath2.setMaxBytesPerLogEvent(1000L);
//        fileLogCollectPath2.setLogContentSliceRuleLogicJsonString("logContentSliceRuleLogicJsonString");
//        fileLogCollectPath2.setCollectDelayThresholdMs(1000L);
//        fileLogCollectPathList2.add(fileLogCollectPath2);
//        logCollectTask2.setFileLogCollectPathList(fileLogCollectPathList2);
//
//        logCollectTask2.setServiceIdList(Arrays.asList(relationService3.getData().getId()));
//
//        result = logCollectTaskManageService.createLogCollectTask(logCollectTask2, null);
//        assert result.success();
//        assert result.getData() > 0;
//
//        //logCollectTask3
//        LogCollectTaskDO logCollectTask3 = new LogCollectTaskDO();
//        logCollectTask3.setAdvancedConfigurationJsonString("advancedConfigurationJsonString");
//        logCollectTask3.setCollectStartTimeBusiness(System.currentTimeMillis());
//        logCollectTask3.setCollectEndTimeBusiness(System.currentTimeMillis());
//        logCollectTask3.setHostFilterRuleLogicJsonString("hostFilterRuleLogicJsonString");
//        logCollectTask3.setKafkaClusterId(kafkaClusterManageService.getKafkaClusterByKafkaClusterId(logdataKafkaClusterId).getData().getId());
//        logCollectTask3.setLimitPriority(LogCollectTaskLimitPriorityLevelEnum.HIGH.getCode());
//        logCollectTask3.setLogCollectTaskName("????????????????????????_3");
//        logCollectTask3.setLogCollectTaskRemark("????????????????????????_remark_" + UUID.randomUUID().toString());
//        logCollectTask3.setLogCollectTaskType(LogCollectTaskTypeEnum.TIME_SCOPE_COLLECT.getCode());
//        logCollectTask3.setLogCollectTaskStatus(YesOrNoEnum.YES.getCode());
//        logCollectTask3.setSendTopic("topic_test_" + UUID.randomUUID().toString());
//        logCollectTask3.setConfigurationVersion(0);
//        logCollectTask3.setOldDataFilterType(2);
//        logCollectTask3.setCollectStartTimeBusiness(System.currentTimeMillis());
//        logCollectTask3.setCollectEndTimeBusiness(System.currentTimeMillis());
//        logCollectTask3.setLogContentFilterRuleLogicJsonString(UUID.randomUUID().toString());
//        logCollectTask3.setLogCollectTaskExecuteTimeoutMs(1024L);
//
//        List<DirectoryLogCollectPathDO> directoryLogCollectPathList3 = new ArrayList<>();
//        DirectoryLogCollectPathDO directoryLogCollectPath3 = new DirectoryLogCollectPathDO();
//        directoryLogCollectPath3.setCollectFilesFilterRegularPipelineJsonString("collectFilesFilterRegularPipelineJsonString");
//        directoryLogCollectPath3.setDirectoryCollectDepth(9);
//        directoryLogCollectPath3.setCharset("utf-8");
//        directoryLogCollectPath3.setFdOffsetExpirationTimeMs(24 * 3600 * 1000L);
//        directoryLogCollectPath3.setLogContentSliceRuleLogicJsonString("logContentSliceRuleLogicJsonString");
//        directoryLogCollectPath3.setPath("/home/logger/dir/test/");
//        directoryLogCollectPath3.setLogContentSliceRuleLogicJsonString("logContentSliceRuleLogicJsonString");
//        directoryLogCollectPath3.setMaxBytesPerLogEvent(100L);
//        directoryLogCollectPathList3.add(directoryLogCollectPath3);
//        logCollectTask3.setDirectoryLogCollectPathList(directoryLogCollectPathList3);
//
//        List<FileLogCollectPathDO> fileLogCollectPathList3 = new ArrayList<>();
//        FileLogCollectPathDO fileLogCollectPath3 = new FileLogCollectPathDO();
//        fileLogCollectPath3.setFileNameSuffixMatchRuleLogicJsonString("collectFileSuffixMatchRuleLogicJsonString");
//        fileLogCollectPath3.setCharset("utf-8");
//        fileLogCollectPath3.setFdOffsetExpirationTimeMs(24 * 3600 * 1000L);
//        fileLogCollectPath3.setLogContentSliceRuleLogicJsonString("logContentSliceRuleLogicJsonString");
//        fileLogCollectPath3.setPath("/home/logger/file/test.log");
//        fileLogCollectPath3.setMaxBytesPerLogEvent(1000L);
//        fileLogCollectPath3.setLogContentSliceRuleLogicJsonString("logContentSliceRuleLogicJsonString");
//        fileLogCollectPath3.setCollectDelayThresholdMs(1000L);
//        fileLogCollectPathList3.add(fileLogCollectPath3);
//        logCollectTask3.setFileLogCollectPathList(fileLogCollectPathList3);
//
//        logCollectTask3.setServiceIdList(Arrays.asList(relationService4.getData().getId()));
//
//        result = logCollectTaskManageService.createLogCollectTask(logCollectTask3, null);
//        assert result.success();
//        assert result.getData() > 0;
//
//        //logCollectTask4
//        LogCollectTaskDO logCollectTask4 = new LogCollectTaskDO();
//        logCollectTask4.setAdvancedConfigurationJsonString("advancedConfigurationJsonString");
//        logCollectTask4.setCollectStartTimeBusiness(System.currentTimeMillis());
//        logCollectTask4.setCollectEndTimeBusiness(System.currentTimeMillis());
//        logCollectTask4.setHostFilterRuleLogicJsonString("hostFilterRuleLogicJsonString");
//        logCollectTask4.setKafkaClusterId(kafkaClusterManageService.getKafkaClusterByKafkaClusterId(logdataKafkaClusterId).getData().getId());
//        logCollectTask4.setLimitPriority(LogCollectTaskLimitPriorityLevelEnum.HIGH.getCode());
//        logCollectTask4.setLogCollectTaskName("????????????????????????_4");
//        logCollectTask4.setLogCollectTaskRemark("????????????????????????_remark_" + UUID.randomUUID().toString());
//        logCollectTask4.setLogCollectTaskType(LogCollectTaskTypeEnum.TIME_SCOPE_COLLECT.getCode());
//        logCollectTask4.setLogCollectTaskStatus(YesOrNoEnum.YES.getCode());
//        logCollectTask4.setSendTopic("topic_test_" + UUID.randomUUID().toString());
//        logCollectTask4.setConfigurationVersion(0);
//        logCollectTask4.setOldDataFilterType(2);
//        logCollectTask4.setCollectStartTimeBusiness(System.currentTimeMillis());
//        logCollectTask4.setCollectEndTimeBusiness(System.currentTimeMillis());
//        logCollectTask4.setLogContentFilterRuleLogicJsonString(UUID.randomUUID().toString());
//        logCollectTask4.setLogCollectTaskExecuteTimeoutMs(1024L);
//
//        List<DirectoryLogCollectPathDO> directoryLogCollectPathList4 = new ArrayList<>();
//        DirectoryLogCollectPathDO directoryLogCollectPath4 = new DirectoryLogCollectPathDO();
//        directoryLogCollectPath4.setCollectFilesFilterRegularPipelineJsonString("collectFilesFilterRegularPipelineJsonString");
//        directoryLogCollectPath4.setDirectoryCollectDepth(9);
//        directoryLogCollectPath4.setCharset("utf-8");
//        directoryLogCollectPath4.setFdOffsetExpirationTimeMs(24 * 3600 * 1000L);
//        directoryLogCollectPath4.setLogContentSliceRuleLogicJsonString("logContentSliceRuleLogicJsonString");
//        directoryLogCollectPath4.setPath("/home/logger/dir/test/");
//        directoryLogCollectPath4.setLogContentSliceRuleLogicJsonString("logContentSliceRuleLogicJsonString");
//        directoryLogCollectPath4.setMaxBytesPerLogEvent(100L);
//        directoryLogCollectPathList4.add(directoryLogCollectPath4);
//        logCollectTask4.setDirectoryLogCollectPathList(directoryLogCollectPathList4);
//
//        List<FileLogCollectPathDO> fileLogCollectPathList4 = new ArrayList<>();
//        FileLogCollectPathDO fileLogCollectPath4 = new FileLogCollectPathDO();
//        fileLogCollectPath4.setFileNameSuffixMatchRuleLogicJsonString("collectFileSuffixMatchRuleLogicJsonString");
//        fileLogCollectPath4.setCharset("utf-8");
//        fileLogCollectPath4.setFdOffsetExpirationTimeMs(24 * 3600 * 1000L);
//        fileLogCollectPath4.setLogContentSliceRuleLogicJsonString("logContentSliceRuleLogicJsonString");
//        fileLogCollectPath4.setPath("/home/logger/file/test.log");
//        fileLogCollectPath4.setMaxBytesPerLogEvent(1000L);
//        fileLogCollectPath4.setLogContentSliceRuleLogicJsonString("logContentSliceRuleLogicJsonString");
//        fileLogCollectPath4.setCollectDelayThresholdMs(1000L);
//        fileLogCollectPathList4.add(fileLogCollectPath4);
//        logCollectTask4.setFileLogCollectPathList(fileLogCollectPathList4);
//
//        logCollectTask4.setServiceIdList(Arrays.asList(relationService5.getData().getId()));
//
//        result = logCollectTaskManageService.createLogCollectTask(logCollectTask4, null);
//        assert result.success();
//        assert result.getData() > 0;
//
//        //logCollectTask5
//        LogCollectTaskDO logCollectTask5 = new LogCollectTaskDO();
//        logCollectTask5.setAdvancedConfigurationJsonString("advancedConfigurationJsonString");
//        logCollectTask5.setCollectStartTimeBusiness(System.currentTimeMillis());
//        logCollectTask5.setCollectEndTimeBusiness(System.currentTimeMillis());
//        logCollectTask5.setHostFilterRuleLogicJsonString("hostFilterRuleLogicJsonString");
//        logCollectTask5.setKafkaClusterId(kafkaClusterManageService.getKafkaClusterByKafkaClusterId(logdataKafkaClusterId).getData().getId());
//        logCollectTask5.setLimitPriority(LogCollectTaskLimitPriorityLevelEnum.HIGH.getCode());
//        logCollectTask5.setLogCollectTaskName("????????????????????????_5");
//        logCollectTask5.setLogCollectTaskRemark("????????????????????????_remark_" + UUID.randomUUID().toString());
//        logCollectTask5.setLogCollectTaskType(LogCollectTaskTypeEnum.TIME_SCOPE_COLLECT.getCode());
//        logCollectTask5.setLogCollectTaskStatus(YesOrNoEnum.YES.getCode());
//        logCollectTask5.setSendTopic("topic_test_" + UUID.randomUUID().toString());
//        logCollectTask5.setConfigurationVersion(0);
//        logCollectTask5.setOldDataFilterType(2);
//        logCollectTask5.setCollectStartTimeBusiness(System.currentTimeMillis());
//        logCollectTask5.setCollectEndTimeBusiness(System.currentTimeMillis());
//        logCollectTask5.setLogContentFilterRuleLogicJsonString(UUID.randomUUID().toString());
//        logCollectTask5.setLogCollectTaskExecuteTimeoutMs(1024L);
//
//        List<DirectoryLogCollectPathDO> directoryLogCollectPathList5 = new ArrayList<>();
//        DirectoryLogCollectPathDO directoryLogCollectPath5 = new DirectoryLogCollectPathDO();
//        directoryLogCollectPath5.setCollectFilesFilterRegularPipelineJsonString("collectFilesFilterRegularPipelineJsonString");
//        directoryLogCollectPath5.setDirectoryCollectDepth(9);
//        directoryLogCollectPath5.setCharset("utf-8");
//        directoryLogCollectPath5.setFdOffsetExpirationTimeMs(24 * 3600 * 1000L);
//        directoryLogCollectPath5.setLogContentSliceRuleLogicJsonString("logContentSliceRuleLogicJsonString");
//        directoryLogCollectPath5.setPath("/home/logger/dir/test/");
//        directoryLogCollectPath5.setLogContentSliceRuleLogicJsonString("logContentSliceRuleLogicJsonString");
//        directoryLogCollectPath5.setMaxBytesPerLogEvent(100L);
//        directoryLogCollectPathList5.add(directoryLogCollectPath5);
//        logCollectTask5.setDirectoryLogCollectPathList(directoryLogCollectPathList5);
//
//        List<FileLogCollectPathDO> fileLogCollectPathList5 = new ArrayList<>();
//        FileLogCollectPathDO fileLogCollectPath5 = new FileLogCollectPathDO();
//        fileLogCollectPath5.setFileNameSuffixMatchRuleLogicJsonString("collectFileSuffixMatchRuleLogicJsonString");
//        fileLogCollectPath5.setCharset("utf-8");
//        fileLogCollectPath5.setFdOffsetExpirationTimeMs(24 * 3600 * 1000L);
//        fileLogCollectPath5.setLogContentSliceRuleLogicJsonString("logContentSliceRuleLogicJsonString");
//        fileLogCollectPath5.setPath("/home/logger/file/test.log");
//        fileLogCollectPath5.setMaxBytesPerLogEvent(1000L);
//        fileLogCollectPath5.setLogContentSliceRuleLogicJsonString("logContentSliceRuleLogicJsonString");
//        fileLogCollectPath5.setCollectDelayThresholdMs(1000L);
//        fileLogCollectPathList5.add(fileLogCollectPath5);
//        logCollectTask5.setFileLogCollectPathList(fileLogCollectPathList5);
//
//        logCollectTask5.setServiceIdList(Arrays.asList(relationService1.getData().getId(), relationService2.getData().getId(), relationService3.getData().getId(), relationService4.getData().getId(), relationService5.getData().getId()));
//
//        result = logCollectTaskManageService.createLogCollectTask(logCollectTask5, null);
//        assert result.success();
//        assert result.getData() > 0;
//
//    }
//
//    /**
//     *
//     * ????????? 5 ??????????????????["????????????_1","????????????_2","????????????_3","????????????_4","????????????_5"]
//     *
//     * 1?????????????????????{????????????????????????_1 ip???192.168.0.1 ??????????????????["????????????_1","????????????_2"]}
//     * 3??????????????????
//     *  {????????????????????????_1 ip???192.168.0.2 ??????????????????["????????????_3"]}
//     *  {????????????????????????_2 ip???192.168.0.3 ??????????????????["????????????_4"]}
//     *  {????????????????????????_3 ip???192.168.0.4 ??????????????????["????????????_5"]}
//     *
//     */
//    private void initServiceAndServiceHostRelationList() {
//
//        HostDO ????????????_1 = hostManageService.getHostByHostName("????????????_1");
//        HostDO ????????????_1 = hostManageService.getHostByHostName("????????????_1");
//        HostDO ????????????_2 = hostManageService.getHostByHostName("????????????_2");
//        HostDO ????????????_3 = hostManageService.getHostByHostName("????????????_3");
//
//        ServiceDO ????????????_1 = new ServiceDO();
//        ????????????_1.setServicename("????????????_1");
//        ????????????_1.setHostIdList(Arrays.asList(????????????_1.getId()));
//        Result<Long> result = serviceManageService.createService(????????????_1, Constant.getOperator(null));
//        assert result.success();
//        assert result.getData() > 0;
//
//        ServiceDO ????????????_2 = new ServiceDO();
//        ????????????_2.setServicename("????????????_2");
//        ????????????_2.setHostIdList(Arrays.asList(????????????_1.getId()));
//        result = serviceManageService.createService(????????????_2, Constant.getOperator(null));
//        assert result.success();
//        assert result.getData() > 0;
//
//        ServiceDO ????????????_3 = new ServiceDO();
//        ????????????_3.setServicename("????????????_3");
//        ????????????_3.setHostIdList(Arrays.asList(????????????_1.getId()));
//        result = serviceManageService.createService(????????????_3, Constant.getOperator(null));
//        assert result.success();
//        assert result.getData() > 0;
//
//        ServiceDO ????????????_4 = new ServiceDO();
//        ????????????_4.setServicename("????????????_4");
//        ????????????_4.setHostIdList(Arrays.asList(????????????_2.getId()));
//        result = serviceManageService.createService(????????????_4, Constant.getOperator(null));
//        assert result.success();
//        assert result.getData() > 0;
//
//        ServiceDO ????????????_5 = new ServiceDO();
//        ????????????_5.setServicename("????????????_5");
//        ????????????_5.setHostIdList(Arrays.asList(????????????_3.getId()));
//        result = serviceManageService.createService(????????????_5, Constant.getOperator(null));
//        assert result.success();
//        assert result.getData() > 0;
//
//    }
//
//    /**
//     * ????????? 4 ??????????????? & ?????? ?????????????????????????????????
//     *      *  1?????????????????????{????????????????????????_1 ip???192.168.0.1 ??????????????????["????????????_1","????????????_2"]}
//     *      *  3??????????????????{????????????????????????_1 ip???192.168.0.2 ??????????????????["????????????_3"]} {????????????????????????_2 ip???192.168.0.3 ??????????????????["????????????_4"]} {????????????????????????_3 ip???192.168.0.4 ??????????????????["????????????_5"]}
//     */
//    private void initHostList() {
//
//        //?????????????????????
//        HostDO host = new HostDO();
//        host.setContainer(YesOrNoEnum.NO.getCode());
//        host.setDepartment("department_test");
//        host.setHostName("????????????_1");
//        host.setIp("192.168.0.1");
//        host.setMachineZone("gz01");
//        host.setParentHostName(StringUtils.EMPTY);
//        Result<Long> result = hostManageService.createHost(host, null);
//        assert result.success();
//        assert result.getData() > 0;
//
//        //???????????????????????????????????????????????????????????????????????????
//        HostDO container1 = new HostDO();
//        container1.setContainer(YesOrNoEnum.YES.getCode());
//        container1.setDepartment("department_test");
//        container1.setHostName("????????????_1");
//        container1.setIp("192.168.0.2");
//        container1.setMachineZone("gz01");
//        container1.setParentHostName("????????????_1");
//        result = hostManageService.createHost(container1, null);
//        assert result.success();
//        assert result.getData() > 0;
//
//        HostDO container2 = new HostDO();
//        container2.setContainer(YesOrNoEnum.YES.getCode());
//        container2.setDepartment("department_test");
//        container2.setHostName("????????????_2");
//        container2.setIp("192.168.0.3");
//        container2.setMachineZone("gz01");
//        container2.setParentHostName("????????????_1");
//        result = hostManageService.createHost(container2, null);
//        assert result.success();
//        assert result.getData() > 0;
//
//        HostDO container3 = new HostDO();
//        container3.setContainer(YesOrNoEnum.YES.getCode());
//        container3.setDepartment("department_test");
//        container3.setHostName("????????????_3");
//        container3.setIp("192.168.0.4");
//        container3.setMachineZone("gz01");
//        container3.setParentHostName("????????????_1");
//        result = hostManageService.createHost(container3, null);
//        assert result.success();
//        assert result.getData() > 0;
//
//    }
//
//    @Test
//    public void testGetAll() {
//
//        initData(false);
//        Result<List<AgentDO>> agentDOListResult = agentManageService.list();
//        assert agentDOListResult.success();
//        assert agentDOListResult.getData().size() > 0;
//
//    }
//

}
