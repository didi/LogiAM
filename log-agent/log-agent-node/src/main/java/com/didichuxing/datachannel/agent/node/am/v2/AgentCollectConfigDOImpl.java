package com.didichuxing.datachannel.agent.node.am.v2;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.didichuxing.datachannel.agent.common.api.CollectType;
import com.didichuxing.datachannel.agent.common.api.FileMatchType;
import com.didichuxing.datachannel.agent.common.api.FileType;
import com.didichuxing.datachannel.agent.common.api.LogConfigConstants;
import com.didichuxing.datachannel.agent.common.api.ServiceSwitch;
import com.didichuxing.datachannel.agent.common.api.StandardLogType;
import com.didichuxing.datachannel.agent.common.configs.v2.AgentConfig;
import com.didichuxing.datachannel.agent.common.configs.v2.ErrorLogConfig;
import com.didichuxing.datachannel.agent.common.configs.v2.LimitConfig;
import com.didichuxing.datachannel.agent.common.configs.v2.MetricConfig;
import com.didichuxing.datachannel.agent.common.configs.v2.OffsetConfig;
import com.didichuxing.datachannel.agent.common.configs.v2.component.ChannelConfig;
import com.didichuxing.datachannel.agent.common.configs.v2.component.CommonConfig;
import com.didichuxing.datachannel.agent.common.configs.v2.component.EventMetricsConfig;
import com.didichuxing.datachannel.agent.common.configs.v2.component.ModelConfig;
import com.didichuxing.datachannel.agent.common.configs.v2.component.ModelLimitConfig;
import com.didichuxing.datachannel.agentmanager.common.bean.dto.logcollecttask.web.FileNameSuffixMatchRuleDTO;
import com.didichuxing.datachannel.agentmanager.common.bean.dto.logcollecttask.web.LogContentFilterRuleDTO;
import com.didichuxing.datachannel.agentmanager.common.bean.dto.logcollecttask.web.LogSliceRuleDTO;
import com.didichuxing.datachannel.agentmanager.common.bean.vo.agent.config.*;
import com.didichuxing.datachannel.agentmanager.common.enumeration.host.HostTypeEnum;
import com.didichuxing.datachannel.agentmanager.common.enumeration.logcollecttask.LogCollectTaskLimitPriorityLevelEnum;
import com.didichuxing.datachannel.agentmanager.common.enumeration.logcollecttask.LogCollectTaskStatusEnum;
import com.didichuxing.datachannel.agentmanager.common.enumeration.logcollecttask.LogCollectTaskTypeEnum;
import com.didichuxing.datachannel.agent.common.beans.LogPath;
import com.didichuxing.datachannel.agent.common.constants.Tags;
import com.didichuxing.datachannel.agent.sink.kafkaSink.KafkaTargetConfig;
import com.didichuxing.datachannel.agent.source.log.config.LogSourceConfig;
import com.didichuxing.datachannel.agent.source.log.config.MatchConfig;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * @author pengzhanglong
 * @date 2020/12/27 9:59
 * @desc
 */
public class AgentCollectConfigDOImpl extends AgentCollectConfigDO {

    /**
     * ???????????????????????? - ???
     */
    private static final Integer MODEL_LIMIT_LEVEL_HIGH   = 9;
    /**
     * ???????????????????????? - ???
     */
    private static final Integer MODEL_LIMIT_LEVEL_LOW    = 1;
    /**
     * ???????????????????????? - ???
     */
    private static final Integer MODEL_LIMIT_LEVEL_MIDDLE = 5;

    private static final Logger  LOGGER                   = LoggerFactory
                                                              .getLogger(AgentCollectConfigDOImpl.class);

    /**
     * ??????????????????AgentConfig???????????????
     *
     * @return ????????????????????????AgentConfig??????
     */
    public AgentConfig convertToAgentConfig() {
        AgentConfigDO agentConfigDO = getAgentConfiguration();
        String advancedConfigurationJsonString = agentConfigDO.getAdvancedConfigurationJsonString();
        AgentAdvancedConfiguration agentAdvancedConfiguration = null;
        if (StringUtils.isNotBlank(advancedConfigurationJsonString)) {
            agentAdvancedConfiguration = JSON.parseObject(advancedConfigurationJsonString, AgentAdvancedConfiguration.class);
        }
        if (null == agentAdvancedConfiguration) {//????????? ??? ????????????
            agentAdvancedConfiguration = new AgentAdvancedConfiguration();//??????????????????
        }
        AgentConfig agentConfig = new AgentConfig();
        /*
         * set agentConfig ?????????????????????
         */
        agentConfig.setHostname(agentConfigDO.getHostName());
        agentConfig.setVersion(agentConfigDO.getAgentConfigurationVersion());
        agentConfig.setSystemStatisticsStatus(agentAdvancedConfiguration.getSystemStatisticsStatus());
        /*
         * set offset config, use default offset config
         */
        agentConfig.setOffsetConfig(new OffsetConfig());
        /*
         * set error log config
         */
        ErrorLogConfig errorLogConfig = new ErrorLogConfig();//error ??????
        ReceiverConfigDO errorLogsReceiverConfigDO = agentConfigDO.getErrorLogsProducerConfiguration();
        errorLogConfig.setNameServer(errorLogsReceiverConfigDO.getNameServer());
        errorLogConfig.setProperties(errorLogsReceiverConfigDO.getProperties());
        errorLogConfig.setTopic(errorLogsReceiverConfigDO.getTopic());
        errorLogConfig.setSwitchConfig(ServiceSwitch.ON.getStatus());
        agentConfig.setErrorLogConfig(errorLogConfig);
        /*
         * set limit config
         */
        LimitConfig limitConfig = new LimitConfig();
        limitConfig.setCpuThreshold(agentConfigDO.getCpuLimitThreshold());
        limitConfig.setStartThreshold(agentAdvancedConfiguration.getAgentLimitStartThreshold());
        limitConfig.setMinThreshold(agentAdvancedConfiguration.getAgentLimitMinThreshold());
        agentConfig.setLimitConfig(limitConfig);
        /*
         * set metrics config
         */
        MetricConfig metricConfig = new MetricConfig();
        ReceiverConfigDO metricsReceiverConfigDO = agentConfigDO.getMetricsProducerConfiguration();
        metricConfig.setNameServer(metricsReceiverConfigDO.getNameServer());
        metricConfig.setProperties(metricsReceiverConfigDO.getProperties());
        metricConfig.setTopic(metricsReceiverConfigDO.getTopic());
        metricConfig.setSwitchConfig(ServiceSwitch.ON.getStatus());
        metricConfig.setTransfer(true);//TODO???
        agentConfig.setMetricConfig(metricConfig);
        /*
         * set model config
         */
        Map<HostInfoDO, List<LogCollectTaskConfiguration>> hostName2LogCollectTaskConfigurationMap = getHostName2LogCollectTaskConfigurationMap();
        List<ModelConfig> modelConfigs = new ArrayList<>();
        for (Map.Entry<HostInfoDO, List<LogCollectTaskConfiguration>> entry : hostName2LogCollectTaskConfigurationMap.entrySet()) {
            HostInfoDO hostInfoDO = entry.getKey();//?????????????????????
            List<LogCollectTaskConfiguration> logCollectTaskConfigurations = entry.getValue();//???????????????????????????????????????????????????
            for (LogCollectTaskConfiguration logCollectTaskConfiguration : logCollectTaskConfigurations) {
                ModelConfig modelConfig = buildModelConfig(hostInfoDO, logCollectTaskConfiguration);
                modelConfigs.add(modelConfig);
            }
        }
        agentConfig.setModelConfigs(modelConfigs);
        return agentConfig;
    }

    /**
     * ?????????????????? & ??????????????????????????????????????????????????????
     *
     * @param hostInfoDO                    ????????????
     * @param logCollectTaskConfiguration ????????????????????????
     * @return ?????????????????? & ?????????????????????????????????????????????????????????
     */
    private ModelConfig buildModelConfig(HostInfoDO hostInfoDO, LogCollectTaskConfiguration logCollectTaskConfiguration) {

        /*
         * ?????? ????????????????????????????????????
         */
        LogCollectTaskAdvancedConfiguration logCollectTaskAdvancedConfiguration = null;//????????????????????????????????????
        String advancedConfigurationJsonString = logCollectTaskConfiguration.getAdvancedConfigurationJsonString();
        if (StringUtils.isNotBlank(advancedConfigurationJsonString)) {
            logCollectTaskAdvancedConfiguration = JSON.parseObject(advancedConfigurationJsonString, LogCollectTaskAdvancedConfiguration.class);
        }
        if (null == logCollectTaskAdvancedConfiguration) {//????????? ??? ????????????
            logCollectTaskAdvancedConfiguration = new LogCollectTaskAdvancedConfiguration();//??????????????????
        }
        /*
         * ?????? modelConfig
         */
        ModelConfig modelConfig = new ModelConfig(Tags.TASK_LOG2KAFKA);
        modelConfig.setCollectType(CollectType.COLLECT_IN_NORMAL_SERVER.getStatus());
        modelConfig.setHostname(hostInfoDO.getHostName());
        modelConfig.setVersion(logCollectTaskConfiguration.getConfigurationVersion());
        /*
         * set channel config
         */
        ChannelConfig channelConfig = new ChannelConfig();
        channelConfig.setMaxBytes(logCollectTaskAdvancedConfiguration.getChannelMaxBytes());
        channelConfig.setMaxNum(logCollectTaskAdvancedConfiguration.getChannelMaxNum());
        modelConfig.setChannelConfig(channelConfig);
        /*
         * set common config
         */
        CommonConfig commonConfig = new CommonConfig();
        commonConfig.setEncodeType(logCollectTaskAdvancedConfiguration.getEncodeType());
        commonConfig.setModelId(logCollectTaskConfiguration.getLogCollectTaskId());
        commonConfig.setModelType(
                logCollectTaskConfiguration.getLogCollectTaskType().equals(LogCollectTaskTypeEnum.NORMAL_COLLECT.getCode()) ? LogConfigConstants.COLLECT_TYPE_PERIODICITY : LogConfigConstants.COLLECT_TYPE_TEMPORALITY
        );
        if (commonConfig.getModelType().equals(LogConfigConstants.COLLECT_TYPE_TEMPORALITY)) {
            commonConfig.setEndTime(new Date(logCollectTaskConfiguration.getCollectEndTimeBusiness()));
            commonConfig.setStartTime(new Date(logCollectTaskConfiguration.getCollectStartTimeBusiness()));
        }
        commonConfig.setStop(
                !logCollectTaskConfiguration.getLogCollectTaskStatus().equals(LogCollectTaskStatusEnum.RUNNING.getCode())
        );
        commonConfig.setVersion(logCollectTaskConfiguration.getConfigurationVersion());
        modelConfig.setCommonConfig(commonConfig);
        /*
         * set event metrics config
         */
        EventMetricsConfig eventMetricsConfig = new EventMetricsConfig();
        eventMetricsConfig.setOtherMetrics(logCollectTaskAdvancedConfiguration.getOtherMetrics());  // map??????
        eventMetricsConfig.setOtherEvents(logCollectTaskAdvancedConfiguration.getOtherEvents());   // map??????
        modelConfig.setEventMetricsConfig(eventMetricsConfig);
        /*
         * set limit config
         */
        ModelLimitConfig modelLimitConfig = new ModelLimitConfig();
        Integer limitPriority = logCollectTaskConfiguration.getLimitPriority();
        if (limitPriority.equals(LogCollectTaskLimitPriorityLevelEnum.HIGH.getCode())) {
            modelLimitConfig.setLevel(MODEL_LIMIT_LEVEL_HIGH);
        } else if (limitPriority.equals(LogCollectTaskLimitPriorityLevelEnum.MIDDLE.getCode())) {
            modelLimitConfig.setLevel(MODEL_LIMIT_LEVEL_MIDDLE);
        } else {
            modelLimitConfig.setLevel(MODEL_LIMIT_LEVEL_LOW);
        }
        modelLimitConfig.setStartThrehold(logCollectTaskAdvancedConfiguration.getStartThrehold());
        modelLimitConfig.setMinThreshold(logCollectTaskAdvancedConfiguration.getMinThreshold());
        modelConfig.setModelLimitConfig(modelLimitConfig);
        /*
         * set source config
         */
        LogSourceConfig logSourceConfig = new LogSourceConfig();
        /*
         * set match config
         */
        MatchConfig matchConfig = new MatchConfig();
        matchConfig.setFileType(FileType.File.getStatus());
        matchConfig.setBusinessType(StandardLogType.Normal.getType());
        matchConfig.setMatchType(FileMatchType.Regex.getStatus());
        String fileNameSuffixMatchRuleLogicJsonString = logCollectTaskConfiguration.getFileNameSuffixMatchRuleLogicJsonString();
        if (StringUtils.isNotBlank(fileNameSuffixMatchRuleLogicJsonString)) {
            FileNameSuffixMatchRuleDTO fileFilter = JSONObject.parseObject(fileNameSuffixMatchRuleLogicJsonString, FileNameSuffixMatchRuleDTO.class);  //????????????
            if (fileFilter != null) {
                matchConfig.setFileSuffix(fileFilter.getSuffixMatchRegular());
            }
        }
        logSourceConfig.setMatchConfig(matchConfig);
        /*
         * set log file content split rule logic
         */
        String logContentSliceRuleLogicJsonString = logCollectTaskConfiguration.getLogContentSliceRuleLogicJsonString();
        if (StringUtils.isNotBlank(logContentSliceRuleLogicJsonString)) {
            LogSliceRuleDTO logSliceRuleDTO = JSONObject.parseObject(logContentSliceRuleLogicJsonString, LogSliceRuleDTO.class);
            if (logSliceRuleDTO != null) {
                //??????????????????
                String timeFormat = logSliceRuleDTO.getSliceTimestampFormat();
                if (timeFormat != null && !timeFormat.equals("")) {
                    logSourceConfig.setTimeFormat(timeFormat);  //???????????????
                    logSourceConfig.setTimeFormatLength(timeFormat.replace("'", "").length());
                }
                String timeStartFlag = logSliceRuleDTO.getSliceTimestampPrefixString();
                if (timeStartFlag != null && !timeStartFlag.equals("")) {
                    logSourceConfig.setTimeStartFlag(timeStartFlag);  //??????????????????????????????
                }
                Integer timeStartFlagIndex = logSliceRuleDTO.getSliceTimestampPrefixStringIndex();
                if (timeStartFlagIndex != null) {
                    logSourceConfig.setTimeStartFlagIndex(timeStartFlagIndex); //????????????????????????????????????????????????index?????????1??????
                }
            }
        }
        //set log paths
        if (CollectionUtils.isNotEmpty(logCollectTaskConfiguration.getFileLogCollectPathList())) {//???????????????
            List<LogPath> logPaths = new ArrayList<>(logCollectTaskConfiguration.getFileLogCollectPathList().size());
            for (FileLogCollectPathConfigDO fileLogCollectPathConfigDO : logCollectTaskConfiguration.getFileLogCollectPathList()) {
                LogPath logPath = new LogPath();
                logPath.setLogModelId(logCollectTaskConfiguration.getLogCollectTaskId());
                logPath.setPath(fileLogCollectPathConfigDO.getPath());
                logPath.setPathId(fileLogCollectPathConfigDO.getPathId());
                if (hostInfoDO.getHostType().equals(HostTypeEnum.CONTAINER.getCode())) {//????????????case???????????? dockerPath
                    String containerPath = fileLogCollectPathConfigDO.getRealPath();//????????????
                    logPath.setDockerPath(containerPath);
                    logPath.setRealPath(containerPath);
                } else {//???????????????
                    logPath.setPath(fileLogCollectPathConfigDO.getPath());
                }
                logPaths.add(logPath);
            }
            logSourceConfig.setLogPaths(logPaths);
            FileLogCollectPathConfigDO fileLogCollectPathConfigDO = logCollectTaskConfiguration.getFileLogCollectPathList().get(0);

        }
        logSourceConfig.setMaxErrorLineNum(logCollectTaskAdvancedConfiguration.getMaxErrorLineNum());
        logSourceConfig.setMaxModifyTime(logCollectTaskAdvancedConfiguration.getMaxModifyTime());
        logSourceConfig.setMaxThreadNum(logCollectTaskAdvancedConfiguration.getMaxThreadNum());
        logSourceConfig.setOrderTimeMaxGap(logCollectTaskAdvancedConfiguration.getOrderTimeMaxGap());
        logSourceConfig.setReadFileType(logCollectTaskAdvancedConfiguration.getReadFileType());
        logSourceConfig.setReadTimeOut(logCollectTaskAdvancedConfiguration.getReadTimeOut());
        modelConfig.setSourceConfig(logSourceConfig);
        /*
         * set target config
         */
        KafkaTargetConfig targetConfig = new KafkaTargetConfig();
        targetConfig.setAsync(logCollectTaskAdvancedConfiguration.getAsync());
        ReceiverConfigDO kafkaClusterConfiguration = logCollectTaskConfiguration.getLogProducerConfiguration();
        targetConfig.setBootstrap(kafkaClusterConfiguration.getNameServer());
        targetConfig.setClusterId(kafkaClusterConfiguration.getReceiverId().intValue());
        String contentFilterJsonString = logCollectTaskConfiguration.getLogContentFilterRuleLogicJsonString();
        if (StringUtils.isNotBlank(contentFilterJsonString)) {
            LogContentFilterRuleDTO contentFilter = JSONObject.parseObject(contentFilterJsonString, LogContentFilterRuleDTO.class);
            if (contentFilter != null) {
                Integer filterOprType = contentFilter.getLogContentFilterType();
                if (filterOprType != null) {
                    targetConfig.setFilterOprType(filterOprType);
                }
                targetConfig.setFilterRule(contentFilter.getLogContentFilterExpression());
            }
        }
        targetConfig.setFlushBatchSize(logCollectTaskAdvancedConfiguration.getFlushBatchSize());
        targetConfig.setFlushBatchTimeThreshold(logCollectTaskAdvancedConfiguration.getFlushBatchTimeThreshold());
        targetConfig.setKeyFormat(logCollectTaskAdvancedConfiguration.getKeyFormat());
        targetConfig.setKeyStartFlag(logCollectTaskAdvancedConfiguration.getKeyStartFlag());
        targetConfig.setKeyStartFlagIndex(logCollectTaskAdvancedConfiguration.getKeyStartFlagIndex());
        targetConfig.setMaxContentSize(logCollectTaskAdvancedConfiguration.getMaxContentSize());
        targetConfig.setProperties(logCollectTaskConfiguration.getLogProducerConfiguration().getProperties());
        targetConfig.setRegularPartKey(logCollectTaskAdvancedConfiguration.getRegularPartKey());
        targetConfig.setSendBatchSize(logCollectTaskAdvancedConfiguration.getSendBatchSize());
        targetConfig.setSendBatchTimeThreshold(logCollectTaskAdvancedConfiguration.getSendBatchTimeThreshold());
        targetConfig.setTopic(logCollectTaskConfiguration.getLogProducerConfiguration().getTopic());
        targetConfig.setTransFormate(logCollectTaskAdvancedConfiguration.getTransFormate());
        targetConfig.setSinkNum(logCollectTaskAdvancedConfiguration.getSinkNum());
        modelConfig.setTargetConfig(targetConfig);

        return modelConfig;
    }
}
