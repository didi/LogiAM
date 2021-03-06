package com.didichuxing.datachannel.agentmanager.thirdpart.logcollecttask.manage.extension.impl;

import com.alibaba.fastjson.JSON;
import com.didichuxing.datachannel.agentmanager.common.bean.domain.logcollecttask.FileLogCollectPathDO;
import com.didichuxing.datachannel.agentmanager.common.bean.domain.logcollecttask.LogCollectTaskDO;
import com.didichuxing.datachannel.agentmanager.common.bean.po.logcollecttask.LogCollectTaskPO;
import com.didichuxing.datachannel.agentmanager.common.enumeration.ErrorCodeEnum;
import com.didichuxing.datachannel.agentmanager.common.enumeration.logcollecttask.LogCollectTaskLimitPriorityLevelEnum;
import com.didichuxing.datachannel.agentmanager.common.enumeration.logcollecttask.LogCollectTaskOldDataFilterTypeEnum;
import com.didichuxing.datachannel.agentmanager.common.enumeration.logcollecttask.LogCollectTaskTypeEnum;
import com.didichuxing.datachannel.agentmanager.common.exception.ServiceException;
import com.didichuxing.datachannel.agentmanager.common.bean.common.CheckResult;
import com.didichuxing.datachannel.agentmanager.common.util.ConvertUtil;
import com.didichuxing.datachannel.agentmanager.thirdpart.logcollecttask.manage.extension.LogCollectTaskManageServiceExtension;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

@org.springframework.stereotype.Service
public class DefaultLogCollectTaskManageServiceExtensionImpl implements LogCollectTaskManageServiceExtension {

    @Override
    public LogCollectTaskPO logCollectTask2LogCollectTaskPO(LogCollectTaskDO logCollectTask) throws ServiceException {
        try {
            LogCollectTaskPO logCollectTaskPO = ConvertUtil.obj2Obj(logCollectTask, LogCollectTaskPO.class, "directoryLogCollectPathList", "fileLogCollectPathList", "serviceIdList");
            return logCollectTaskPO;
        } catch (Exception ex) {
            throw new ServiceException(
                    String.format(
                            "class=DefaultLogCollectTaskManageServiceExtensionImpl||method=logCollectTask2LogCollectTaskPO||msg={%s}",
                            String.format("LogCollectTask??????{%s}?????????LogCollectTaskPO????????????", JSON.toJSONString(logCollectTask))
                    ),
                    ex,
                    ErrorCodeEnum.SYSTEM_INTERNAL_ERROR.getCode()
            );
        }
    }

    @Override
    public CheckResult checkCreateParameterLogCollectTask(LogCollectTaskDO logCollectTaskDO) {
        if(CollectionUtils.isEmpty(logCollectTaskDO.getFileLogCollectPathList()) && CollectionUtils.isEmpty(logCollectTaskDO.getDirectoryLogCollectPathList())) {
            return new CheckResult(false, ErrorCodeEnum.ILLEGAL_PARAMS.getCode(), "??????????????????????????????????????????????????????????????????????????????????????????");
        }
        if(null == logCollectTaskDO.getLogCollectTaskType()) {
            return new CheckResult(false, ErrorCodeEnum.ILLEGAL_PARAMS.getCode(), "logCollectTaskType????????????");
        }
        if(!LogCollectTaskTypeEnum.NORMAL_COLLECT.getCode().equals(logCollectTaskDO.getLogCollectTaskType()) && !LogCollectTaskTypeEnum.TIME_SCOPE_COLLECT.getCode().equals(logCollectTaskDO.getLogCollectTaskType())) {
            return new CheckResult(false, ErrorCodeEnum.ILLEGAL_PARAMS.getCode(), "logCollectTaskType?????????????????????????????????[0,1]");
        }
        if(LogCollectTaskTypeEnum.TIME_SCOPE_COLLECT.getCode().equals(logCollectTaskDO.getLogCollectTaskType())) {
            if (null == logCollectTaskDO.getCollectStartTimeBusiness() || logCollectTaskDO.getCollectStartTimeBusiness().equals(0L)) {
                return new CheckResult(false, ErrorCodeEnum.ILLEGAL_PARAMS.getCode(), "??????????????????????????????????????????????????? collectStartTimeBusiness ????????????????????? & ??????0");
            }
            if (null == logCollectTaskDO.getCollectEndTimeBusiness() && logCollectTaskDO.getCollectEndTimeBusiness() <= logCollectTaskDO.getCollectStartTimeBusiness()) {
                return new CheckResult(false, ErrorCodeEnum.ILLEGAL_PARAMS.getCode(), "??????????????????????????????????????????????????? collectStartTimeBusiness ????????????????????? & ?????? collectStartTimeBusiness ?????????");
            }
            /*if (null == logCollectTaskDO.getLogCollectTaskExecuteTimeoutMs() || logCollectTaskDO.getLogCollectTaskExecuteTimeoutMs().equals(0L)) {
                return new CheckResult(false, ErrorCodeEnum.ILLEGAL_PARAMS.getCode(), "logCollectTaskExecuteTimeoutMs????????????????????? & ??????0");
            }*/
        }
        if(null == logCollectTaskDO.getOldDataFilterType()) {
            return new CheckResult(false, ErrorCodeEnum.ILLEGAL_PARAMS.getCode(), "oldDataFilterType????????????");
        }
        if(LogCollectTaskTypeEnum.NORMAL_COLLECT.getCode().equals(logCollectTaskDO.getLogCollectTaskType())) {
            if(!logCollectTaskDO.getOldDataFilterType().equals(LogCollectTaskOldDataFilterTypeEnum.NO.getCode()) && (null == logCollectTaskDO.getCollectStartTimeBusiness() || logCollectTaskDO.getCollectStartTimeBusiness().equals(0))) {
                return new CheckResult(false, ErrorCodeEnum.ILLEGAL_PARAMS.getCode(), "??????????????????????????????????????????????????? collectStartTimeBusiness ?????????????????? & ??????0");
            }
        }
        if(CollectionUtils.isEmpty(logCollectTaskDO.getServiceIdList())) {
            return new CheckResult(false, ErrorCodeEnum.ILLEGAL_PARAMS.getCode(), "serviceIdList?????????????????????????????????????????????Service");
        }
        if(null == logCollectTaskDO.getKafkaClusterId() || logCollectTaskDO.getKafkaClusterId().equals(0L)) {
            return new CheckResult(false, ErrorCodeEnum.ILLEGAL_PARAMS.getCode(), "kafkaClusterId????????????????????? & ??????0");
        }
        if(StringUtils.isBlank(logCollectTaskDO.getSendTopic())) {
            return new CheckResult(false, ErrorCodeEnum.ILLEGAL_PARAMS.getCode(), "sendTopic?????????????????????");
        }
        if(null == logCollectTaskDO.getLimitPriority()) {
            return new CheckResult(false, ErrorCodeEnum.ILLEGAL_PARAMS.getCode(), "limitPriority????????????");
        }
        if(!LogCollectTaskLimitPriorityLevelEnum.HIGH.getCode().equals(logCollectTaskDO.getLimitPriority()) && !LogCollectTaskLimitPriorityLevelEnum.MIDDLE.getCode().equals(logCollectTaskDO.getLimitPriority()) && !LogCollectTaskLimitPriorityLevelEnum.LOW.getCode().equals(logCollectTaskDO.getLimitPriority())) {
            return new CheckResult(false, ErrorCodeEnum.ILLEGAL_PARAMS.getCode(), "limitPriority?????????????????????????????????[0,1,2]");
        }
        if(StringUtils.isBlank(logCollectTaskDO.getLogCollectTaskName())) {
            return new CheckResult(false, ErrorCodeEnum.ILLEGAL_PARAMS.getCode(), "logCollectTaskName?????????????????????");
        }
        /*if(null == logCollectTaskDO.getCollectDelayThresholdMs() || logCollectTaskDO.getCollectDelayThresholdMs().equals(0L)) {
            return new CheckResult(false, ErrorCodeEnum.ILLEGAL_PARAMS.getCode(), "collectDelayThresholdMs????????????????????? & ??????0");
        }*/
        if(StringUtils.isBlank(logCollectTaskDO.getFileNameSuffixMatchRuleLogicJsonString())) {
            return new CheckResult(false, ErrorCodeEnum.ILLEGAL_PARAMS.getCode(), "FileLogCollectPathDO.fileNameSuffixMatchRuleLogicJsonString?????????????????????");
        }
        //???????????????????????????????????????
        if(CollectionUtils.isNotEmpty(logCollectTaskDO.getFileLogCollectPathList())) {
            for (FileLogCollectPathDO fileLogCollectPathDO : logCollectTaskDO.getFileLogCollectPathList()) {
                if(StringUtils.isBlank(fileLogCollectPathDO.getPath())) {
                    return new CheckResult(false, ErrorCodeEnum.ILLEGAL_PARAMS.getCode(), "FileLogCollectPathDO.path?????????????????????");
                }
            }
        }
        //TODO??????????????????????????????????????????
        return new CheckResult(true);
    }

    @Override
    public LogCollectTaskDO logCollectTaskPO2LogCollectTaskDO(LogCollectTaskPO logCollectTaskPO) throws ServiceException {
        LogCollectTaskDO logCollectTask = null;
        try {
            logCollectTask = ConvertUtil.obj2Obj(logCollectTaskPO, LogCollectTaskDO.class);
        } catch (Exception ex) {
            throw new ServiceException(
                    String.format(
                            "class=DefaultLogCollectTaskManageServiceExtensionImpl||method=logCollectTaskPO2LogCollectTaskDO||msg={%s}",
                            String.format("LogCollectTaskPO??????{%s}?????????LogCollectTask???????????????????????????%s", JSON.toJSONString(logCollectTaskPO), ex.getMessage())
                    ),
                    ex,
                    ErrorCodeEnum.SYSTEM_INTERNAL_ERROR.getCode()
            );
        }
        if(null == logCollectTask) {
            throw new ServiceException(
                    String.format(
                            "class=DefaultLogCollectTaskManageServiceExtensionImpl||method=logCollectTaskPO2LogCollectTaskDO||msg={%s}",
                            String.format("LogCollectTaskPO??????{%s}?????????LogCollectTask????????????", JSON.toJSONString(logCollectTaskPO))
                    ),
                    ErrorCodeEnum.SYSTEM_INTERNAL_ERROR.getCode()
            );
        }
        return logCollectTask;
    }

    @Override
    public CheckResult checkUpdateParameterLogCollectTask(LogCollectTaskDO logCollectTaskDO) {
        if(null == logCollectTaskDO.getId() || logCollectTaskDO.getId().equals(0)) {
            return new CheckResult(false, ErrorCodeEnum.ILLEGAL_PARAMS.getCode(), "id???????????? & ??????0");
        }
        if(CollectionUtils.isEmpty(logCollectTaskDO.getFileLogCollectPathList()) && CollectionUtils.isEmpty(logCollectTaskDO.getDirectoryLogCollectPathList())) {
            return new CheckResult(false, ErrorCodeEnum.ILLEGAL_PARAMS.getCode(), "??????????????????????????????????????????????????????????????????????????????????????????");
        }
        if(null == logCollectTaskDO.getLogCollectTaskType()) {
            return new CheckResult(false, ErrorCodeEnum.ILLEGAL_PARAMS.getCode(), "logCollectTaskType????????????");
        }
        if(!LogCollectTaskTypeEnum.NORMAL_COLLECT.getCode().equals(logCollectTaskDO.getLogCollectTaskType()) && !LogCollectTaskTypeEnum.TIME_SCOPE_COLLECT.getCode().equals(logCollectTaskDO.getLogCollectTaskType())) {
            return new CheckResult(false, ErrorCodeEnum.ILLEGAL_PARAMS.getCode(), "logCollectTaskType?????????????????????????????????[0,1]");
        }
        if(LogCollectTaskTypeEnum.TIME_SCOPE_COLLECT.getCode().equals(logCollectTaskDO.getLogCollectTaskType())) {
            if(null == logCollectTaskDO.getCollectStartTimeBusiness() || logCollectTaskDO.getCollectStartTimeBusiness().equals(0)) {
                return new CheckResult(false, ErrorCodeEnum.ILLEGAL_PARAMS.getCode(), "??????????????????????????????????????????????????? collectStartTimeBusiness ????????????????????? & ??????0");
            }
            if(null == logCollectTaskDO.getCollectEndTimeBusiness() && logCollectTaskDO.getCollectEndTimeBusiness().longValue() <= logCollectTaskDO.getCollectStartTimeBusiness().longValue()) {
                return new CheckResult(false, ErrorCodeEnum.ILLEGAL_PARAMS.getCode(), "??????????????????????????????????????????????????? collectStartTimeBusiness ????????????????????? & ?????? collectStartTimeBusiness ?????????");
            }
            /*if(null == logCollectTaskDO.getLogCollectTaskExecuteTimeoutMs() || logCollectTaskDO.getLogCollectTaskExecuteTimeoutMs().equals(0)) {
                return new CheckResult(false, ErrorCodeEnum.ILLEGAL_PARAMS.getCode(), "logCollectTaskExecuteTimeoutMs????????????????????? & ??????0");
            }*/
        }
        if(null == logCollectTaskDO.getOldDataFilterType()) {
            return new CheckResult(false, ErrorCodeEnum.ILLEGAL_PARAMS.getCode(), "oldDataFilterType????????????");
        }
        if(LogCollectTaskTypeEnum.NORMAL_COLLECT.getCode().equals(logCollectTaskDO.getLogCollectTaskType())) {
            if(!logCollectTaskDO.getOldDataFilterType().equals(LogCollectTaskOldDataFilterTypeEnum.NO) && (null == logCollectTaskDO.getCollectStartTimeBusiness() || logCollectTaskDO.getCollectStartTimeBusiness().equals(0))) {
                return new CheckResult(false, ErrorCodeEnum.ILLEGAL_PARAMS.getCode(), "??????????????????????????????????????????????????? collectStartTimeBusiness ?????????????????? & ??????0");
            }
        }
        if(CollectionUtils.isEmpty(logCollectTaskDO.getServiceIdList())) {
            return new CheckResult(false, ErrorCodeEnum.ILLEGAL_PARAMS.getCode(), "serviceIdList?????????????????????????????????????????????Service");
        }
        if(null == logCollectTaskDO.getKafkaClusterId() || logCollectTaskDO.getKafkaClusterId().equals(0)) {
            return new CheckResult(false, ErrorCodeEnum.ILLEGAL_PARAMS.getCode(), "kafkaClusterId????????????????????? & ??????0");
        }
        if(StringUtils.isBlank(logCollectTaskDO.getSendTopic())) {
            return new CheckResult(false, ErrorCodeEnum.ILLEGAL_PARAMS.getCode(), "sendTopic?????????????????????");
        }
        if(null == logCollectTaskDO.getLimitPriority()) {
            return new CheckResult(false, ErrorCodeEnum.ILLEGAL_PARAMS.getCode(), "limitPriority????????????");
        }
        if(!LogCollectTaskLimitPriorityLevelEnum.HIGH.getCode().equals(logCollectTaskDO.getLimitPriority()) && !LogCollectTaskLimitPriorityLevelEnum.MIDDLE.getCode().equals(logCollectTaskDO.getLimitPriority()) && !LogCollectTaskLimitPriorityLevelEnum.LOW.getCode().equals(logCollectTaskDO.getLimitPriority())) {
            return new CheckResult(false, ErrorCodeEnum.ILLEGAL_PARAMS.getCode(), "limitPriority?????????????????????????????????[0,1,2]");
        }
        if(StringUtils.isBlank(logCollectTaskDO.getLogCollectTaskName())) {
            return new CheckResult(false, ErrorCodeEnum.ILLEGAL_PARAMS.getCode(), "logCollectTaskName?????????????????????");
        }
        if(null == logCollectTaskDO.getCollectDelayThresholdMs() || logCollectTaskDO.getCollectDelayThresholdMs().equals(0)) {
            return new CheckResult(false, ErrorCodeEnum.ILLEGAL_PARAMS.getCode(), "FileLogCollectPathDO.collectDelayThresholdMs???????????? & ??????0");
        }
        if(StringUtils.isBlank(logCollectTaskDO.getFileNameSuffixMatchRuleLogicJsonString())) {
            return new CheckResult(false, ErrorCodeEnum.ILLEGAL_PARAMS.getCode(), "FileLogCollectPathDO.fileNameSuffixMatchRuleLogicJsonString?????????????????????");
        }
        //???????????????????????????????????????
        if(CollectionUtils.isNotEmpty(logCollectTaskDO.getFileLogCollectPathList())) {
            for (FileLogCollectPathDO fileLogCollectPathDO : logCollectTaskDO.getFileLogCollectPathList()) {

                if(StringUtils.isBlank(fileLogCollectPathDO.getPath())) {
                    return new CheckResult(false, ErrorCodeEnum.ILLEGAL_PARAMS.getCode(), "FileLogCollectPathDO.path?????????????????????");
                }
            }
        }
        //TODO??????????????????????????????????????????
        return new CheckResult(true);
    }

    @Override
    public LogCollectTaskDO updateLogCollectTask(LogCollectTaskDO source, LogCollectTaskDO target) throws ServiceException {
        if(StringUtils.isNotBlank(target.getAdvancedConfigurationJsonString())) {
            source.setAdvancedConfigurationJsonString(target.getAdvancedConfigurationJsonString());
        }
        if(null != target.getCollectEndTimeBusiness()) {
            source.setCollectEndTimeBusiness(target.getCollectEndTimeBusiness());
        }
        if(null != target.getCollectStartTimeBusiness()) {
            source.setCollectStartTimeBusiness(target.getCollectStartTimeBusiness());
        }
        if(StringUtils.isNotBlank(target.getHostFilterRuleLogicJsonString())) {
            source.setHostFilterRuleLogicJsonString(target.getHostFilterRuleLogicJsonString());
        }
        if(null != target.getKafkaClusterId()) {
            source.setKafkaClusterId(target.getKafkaClusterId());
        }
        if(null != target.getLimitPriority()) {
            source.setLimitPriority(target.getLimitPriority());
        }
        if(null != target.getLogCollectTaskExecuteTimeoutMs()) {
            source.setLogCollectTaskExecuteTimeoutMs(target.getLogCollectTaskExecuteTimeoutMs());
        }
        if(StringUtils.isNotBlank(target.getLogCollectTaskName())) {
            source.setLogCollectTaskName(target.getLogCollectTaskName());
        }
        if(StringUtils.isNotBlank(target.getLogCollectTaskRemark())) {
            source.setLogCollectTaskRemark(target.getLogCollectTaskRemark());
        }
        if(null != target.getLogCollectTaskType()) {
            source.setLogCollectTaskType(target.getLogCollectTaskType());
        }
        if(null != target.getOldDataFilterType()) {
            source.setOldDataFilterType(target.getOldDataFilterType());
        }
        if(StringUtils.isNotBlank(target.getSendTopic())) {
            source.setSendTopic(target.getSendTopic());
        }
        if(StringUtils.isNotBlank(target.getLogContentFilterRuleLogicJsonString())) {
            source.setLogContentFilterRuleLogicJsonString(target.getLogContentFilterRuleLogicJsonString());
        }
        if(null != target.getLogCollectTaskFinishTime()) {
            source.setLogCollectTaskFinishTime(target.getLogCollectTaskFinishTime());
        }
        if (!StringUtils.isBlank(target.getLogContentSliceRuleLogicJsonString()) && !"null".equals(target.getLogContentSliceRuleLogicJsonString())) {
            source.setLogContentSliceRuleLogicJsonString(target.getLogContentSliceRuleLogicJsonString());
        }
        if (!StringUtils.isBlank(target.getFileNameSuffixMatchRuleLogicJsonString()) && !"null".equals(target.getFileNameSuffixMatchRuleLogicJsonString())) {
            source.setFileNameSuffixMatchRuleLogicJsonString(target.getFileNameSuffixMatchRuleLogicJsonString());
        }
        if (!StringUtils.isBlank(target.getKafkaProducerConfiguration())) {
            source.setKafkaProducerConfiguration(target.getKafkaProducerConfiguration());
        }
        return source;
    }

    @Override
    public List<LogCollectTaskDO> logCollectTaskPOList2LogCollectTaskDOList(List<LogCollectTaskPO> logCollectTaskPOList) {
        return ConvertUtil.list2List(logCollectTaskPOList, LogCollectTaskDO.class);
    }

}
