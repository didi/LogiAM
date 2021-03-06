package com.didichuxing.datachannel.agentmanager.core.agent.operation.task.impl;

import com.alibaba.fastjson.JSON;
import com.didichuxing.datachannel.agentmanager.common.bean.common.CheckResult;
import com.didichuxing.datachannel.agentmanager.common.bean.domain.agent.AgentDO;
import com.didichuxing.datachannel.agentmanager.common.bean.domain.agent.operationtask.AgentOperationSubTaskDO;
import com.didichuxing.datachannel.agentmanager.common.bean.domain.agent.operationtask.AgentOperationTaskDO;
import com.didichuxing.datachannel.agentmanager.common.bean.common.Result;
import com.didichuxing.datachannel.agentmanager.common.bean.domain.agent.operationtask.AgentOperationTaskPaginationQueryConditionDO;
import com.didichuxing.datachannel.agentmanager.common.bean.domain.agent.version.AgentVersionDO;
import com.didichuxing.datachannel.agentmanager.common.bean.domain.host.HostDO;
import com.didichuxing.datachannel.agentmanager.common.bean.po.agent.operationtask.AgentOperationTaskPO;
import com.didichuxing.datachannel.agentmanager.common.constant.CommonConstant;
import com.didichuxing.datachannel.agentmanager.common.enumeration.ErrorCodeEnum;
import com.didichuxing.datachannel.agentmanager.common.enumeration.agent.AgentCollectTypeEnum;
import com.didichuxing.datachannel.agentmanager.common.enumeration.agent.AgentOperationTaskStatusEnum;
import com.didichuxing.datachannel.agentmanager.common.exception.ServiceException;
import com.didichuxing.datachannel.agentmanager.common.util.ConvertUtil;
import com.didichuxing.datachannel.agentmanager.core.agent.manage.AgentManageService;
import com.didichuxing.datachannel.agentmanager.core.agent.operation.task.AgentOperationSubTaskManageService;
import com.didichuxing.datachannel.agentmanager.core.agent.operation.task.AgentOperationTaskManageService;
import com.didichuxing.datachannel.agentmanager.core.agent.version.AgentVersionManageService;
import com.didichuxing.datachannel.agentmanager.core.host.HostManageService;
import com.didichuxing.datachannel.agentmanager.persistence.mysql.AgentOperationTaskMapper;
import com.didichuxing.datachannel.agentmanager.remote.operation.task.RemoteOperationTaskService;
import com.didichuxing.datachannel.agentmanager.remote.operation.task.common.AgentOperationTaskCreation;
import com.didichuxing.datachannel.agentmanager.remote.operation.task.common.AgentOperationTaskLog;
import com.didichuxing.datachannel.agentmanager.remote.operation.task.common.enumeration.AgentOperationTaskActionEnum;
import com.didichuxing.datachannel.agentmanager.remote.operation.task.common.enumeration.AgentOperationTaskStateEnum;
import com.didichuxing.datachannel.agentmanager.remote.operation.task.common.enumeration.AgentOperationTaskSubStateEnum;
import com.didichuxing.datachannel.agentmanager.remote.operation.task.common.enumeration.AgentOperationTaskTypeEnum;
import com.didichuxing.datachannel.agentmanager.remote.operation.task.n9e.RemoteN9eOperationTaskServiceImpl;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * @author huqidong
 * @date 2020-09-21
 *  Agent?????????????????????????????????
 */
@org.springframework.stereotype.Service
public class AgentOperationTaskManageServiceImpl implements AgentOperationTaskManageService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AgentOperationTaskManageServiceImpl.class);

    @Autowired
    private RemoteOperationTaskService remoteOperationTaskService;

    @Autowired
    private AgentManageService agentManageService;

    @Autowired
    private HostManageService hostManageService;

    @Autowired
    private AgentVersionManageService agentVersionManageService;

    @Autowired
    private AgentOperationTaskMapper agentOperationTaskDAO;

    @Autowired
    private AgentOperationSubTaskManageService agentOperationSubTaskManageService;

    @Override
    @Transactional
    public Long createAgentOperationTask(AgentOperationTaskDO agentOperationTask, String operator) {
        /*
         * ??????????????????
         */
        if(null == agentOperationTask) {
            throw new ServiceException(
                    "??????agentOperationTask??????????????????",
                    ErrorCodeEnum.ILLEGAL_PARAMS.getCode()
            );
        }
        if(null == agentOperationTask.getTaskType()) {
            throw new ServiceException(
                    "??????agentOperationTask??????.taskType?????????????????????",
                    ErrorCodeEnum.ILLEGAL_PARAMS.getCode()
            );
        }
        /*
         * ?????? agentOperationTask.taskName ???????????????????????????prd?????????
         */
        agentOperationTask.setTaskName(UUID.randomUUID().toString());
        /*
         * ?????? agent ???????????????????????????????????????????????????
         */
        Long taskId;
        String operateTypeDesc = "";//?????????????????????????????????????????????
        String operateObject = "";//???????????????????????????/?????????
        if(agentOperationTask.getTaskType().equals(AgentOperationTaskTypeEnum.INSTALL.getCode())) {//??????
            taskId = handleCreateAgentInstallOperationTask(agentOperationTask, operator);
            operateTypeDesc = AgentOperationTaskTypeEnum.INSTALL.getMessage();
            if(agentOperationTask.getHostIdList().size() > 1) {
                operateObject = "??????";
            } else {
                operateObject = hostManageService.getById(agentOperationTask.getHostIdList().get(0)).getHostName();
            }
        } else if(agentOperationTask.getTaskType().equals(AgentOperationTaskTypeEnum.UPGRADE.getCode())) {//??????
            taskId = handleCreateAgentUpgradeOperationTask(agentOperationTask, operator);
            operateTypeDesc = AgentOperationTaskTypeEnum.UPGRADE.getMessage();
            if(agentOperationTask.getAgentIdList().size() > 1) {
                operateObject = "??????";
            } else {
                operateObject = agentManageService.getById(agentOperationTask.getAgentIdList().get(0)).getHostName();
            }
        } else if(agentOperationTask.getTaskType().equals(AgentOperationTaskTypeEnum.UNINSTALL.getCode())) {//??????
            taskId = handleCreateAgentUninstallOperationTask(agentOperationTask, operator);
            operateTypeDesc = AgentOperationTaskTypeEnum.UNINSTALL.getMessage();
            if(agentOperationTask.getAgentIdList().size() > 1) {
                operateObject = "??????";
            } else {
                operateObject = agentManageService.getById(agentOperationTask.getAgentIdList().get(0)).getHostName();
            }
        } else {
            throw new ServiceException(
                    String.format("class=AgentOperationTaskManageServiceImpl||method=createAgentOperationTask||msg={?????????agent????????????={%d}}", agentOperationTask.getTaskType()),
                    ErrorCodeEnum.SYSTEM_INTERNAL_ERROR.getCode()
            );
        }
        String taskName = String.format("%s%sAgent??????", operateObject, operateTypeDesc);
        /*
         * ??????taskId????????????????????????????????????????????????
         */
        AgentOperationTaskPO agentOperationTaskPO = new AgentOperationTaskPO();
        agentOperationTaskPO.setId(taskId);
        agentOperationTaskPO.setTaskName(taskName);
        agentOperationTaskDAO.updateTaskNameByPrimaryKey(agentOperationTaskPO);
        return taskId;
    }

    /**
     * ??????agent????????????
     * @param agentOperationTaskDO agent ??????????????????
     * @param operator ?????????
     * @throws ServiceException ???????????????????????????????????????
     */
    private Long handleCreateAgentUpgradeOperationTask(AgentOperationTaskDO agentOperationTaskDO, String operator) throws ServiceException {
        /*
         * ????????????
         */
        CheckResult checkResult = checkUpgradeParameter(agentOperationTaskDO);
        if(!checkResult.getCheckResult()) {
            throw new ServiceException(checkResult.getMessage(), checkResult.getCode());
        }
        /*
         * ???????????????agent????????????
         */
        List<Long> agentIdList = agentOperationTaskDO.getAgentIdList();
        Set<String> hostNameSet = new HashSet<>();//???????????????set??????????????????host???????????????agent
        List<String> hostIpList = new ArrayList<>(agentIdList.size());
        for (Long agentId : agentIdList) {
            AgentDO agentDO = agentManageService.getById(agentId);
            if(null == agentDO) {
                throw new ServiceException(String.format("Agent={id=%d}?????????????????????", agentId), ErrorCodeEnum.AGENT_NOT_EXISTS.getCode());
            }
            if(StringUtils.isBlank(agentDO.getHostName())) {
                throw new ServiceException(
                        String.format("Agent??????={id={%d}}??????hostName???????????????", agentId),
                        ErrorCodeEnum.SYSTEM_INTERNAL_ERROR.getCode()
                );
            } else {
                if(hostNameSet.add(agentDO.getHostName())) {
                    hostIpList.add(hostManageService.getHostByHostName(agentDO.getHostName()).getIp());
                }
            }
        }
        /*
         * ???????????????????????????agent??????????????????
         */
        Long externalTaskId = commitRemoteAgentUpgradeTask(agentOperationTaskDO, hostIpList);
        /*
         * ???????????????????????????????????????
         */
        if(!remoteOperationTaskService.actionTask(externalTaskId, AgentOperationTaskActionEnum.START)) {//????????????
            throw new ServiceException(
                    String.format("???????????????RemoteOperationTaskService.actionTask(externalTaskId={%d}, AgentOperationTaskActionEnum.START)????????????", externalTaskId),
                    ErrorCodeEnum.SYSTEM_INTERNAL_ERROR.getCode()
            );
        }
        /*
         * ??????agent???????????????????????????
         */
        agentOperationTaskDO.setExternalAgentTaskId(externalTaskId);
        agentOperationTaskDO.setOperator(CommonConstant.getOperator(operator));
        AgentOperationTaskPO agentOperationTaskPO = agentOperationTaskDO2AgentOperationTaskPOUpgrade(agentOperationTaskDO, hostNameSet.size());
        agentOperationTaskDAO.insert(agentOperationTaskPO);
        saveAgentOperationSubTask(hostNameSet, agentOperationTaskPO.getId(), AgentOperationTaskTypeEnum.UPGRADE, operator);
        return agentOperationTaskPO.getId();
    }

    /**
     * ??????agent???????????????????????????????????????agent??????????????????????????????????????????agent????????????id
     * @param agentOperationTaskDO agent??????????????????
     * @param hostNameList
     * @return ??????????????????agent????????????id
     * @throws ServiceException ???????????????????????????????????????
     */
    private Long commitRemoteAgentUpgradeTask(AgentOperationTaskDO agentOperationTaskDO, List<String> hostNameList) throws ServiceException {
        AgentOperationTaskCreation agentOperationTaskCreation = new AgentOperationTaskCreation();
        agentOperationTaskCreation.setTaskType(agentOperationTaskDO.getTaskType());
        agentOperationTaskCreation.setTaskName(agentOperationTaskDO.getTaskName());
        agentOperationTaskCreation.setHostList(hostNameList);
        //set about agent version
        Long agentVerisonId = agentOperationTaskDO.getTargetAgentVersionId();
        AgentVersionDO agentVersionDO = agentVersionManageService.getById(agentVerisonId);
        if(null == agentVersionDO) {
            throw new ServiceException(
                    String.format("?????????Agent???AgentVersion={id=%d}?????????????????????", agentVerisonId),
                    ErrorCodeEnum.AGENT_VERSION_NOT_EXISTS.getCode()
            );
        }
        String downloadUrl = agentVersionManageService.getAgentInstallFileDownloadUrl(agentVerisonId);
        agentOperationTaskCreation.setAgentPackageDownloadUrl(downloadUrl);
        agentOperationTaskCreation.setAgentPackageMd5(agentVersionDO.getFileMd5());
        agentOperationTaskCreation.setAgentPackageName(agentVersionDO.getFileName());
        Result<Long> externalTaskIdResult = remoteOperationTaskService.createTask(agentOperationTaskCreation);
        if(externalTaskIdResult.failed()) {
            throw new ServiceException(externalTaskIdResult.getMessage(), externalTaskIdResult.getCode());
        }
        Long externalTaskId = externalTaskIdResult.getData();
        return externalTaskId;
    }

    /**
     * ??????agent???????????????????????????????????????agent??????????????????????????????????????????agent????????????id
     * @param agentOperationTaskDO agent??????????????????
     * @return ??????????????????agent????????????id
     * @throws ServiceException ???????????????????????????????????????
     */
    private Long commitRemoteAgentUninstallTask(AgentOperationTaskDO agentOperationTaskDO, List<String> hostNameList) throws ServiceException {
        AgentOperationTaskCreation agentOperationTaskCreation = new AgentOperationTaskCreation();
        agentOperationTaskCreation.setTaskType(agentOperationTaskDO.getTaskType());
        agentOperationTaskCreation.setTaskName(agentOperationTaskDO.getTaskName());
        agentOperationTaskCreation.setHostList(hostNameList);
        Result<Long> externalTaskIdResult = remoteOperationTaskService.createTask(agentOperationTaskCreation);
        if(externalTaskIdResult.failed()) {
            throw new ServiceException(externalTaskIdResult.getMessage(), externalTaskIdResult.getCode());
        }
        Long externalTaskId = externalTaskIdResult.getData();
        return externalTaskId;
    }

    /**
     * ???AgentOperationTaskDO???????????????agent??????case??????AgentOperationTaskPO??????
     * @param agentOperationTaskDO ?????????AgentOperationTaskDO??????
     * @return ?????????AgentOperationTaskDO??????????????????agent??????case??????AgentOperationTaskPO??????
     */
    private AgentOperationTaskPO agentOperationTaskDO2AgentOperationTaskPOUpgrade(AgentOperationTaskDO agentOperationTaskDO, Integer hostsNumber) {
        AgentOperationTaskPO agentOperationTaskPO = new AgentOperationTaskPO();
        //???????????????????????????????????????
        agentOperationTaskPO.setTaskStatus(AgentOperationTaskStatusEnum.RUNNING.getCode());
        agentOperationTaskPO.setTaskType(agentOperationTaskDO.getTaskType());
        agentOperationTaskPO.setTaskName(agentOperationTaskDO.getTaskName());
        agentOperationTaskPO.setExternalAgentTaskId(agentOperationTaskDO.getExternalAgentTaskId());
        agentOperationTaskPO.setHostsNumber(hostsNumber);
        agentOperationTaskPO.setOperator(agentOperationTaskDO.getOperator());
        agentOperationTaskPO.setTaskStartTime(new Date());
        agentOperationTaskPO.setTargetAgentVersionId(agentOperationTaskDO.getTargetAgentVersionId());
        return agentOperationTaskPO;
    }

    /**
     * ???AgentOperationTaskDO???????????????agent??????case??????AgentOperationTaskPO??????
     * @param agentOperationTaskDO ?????????AgentOperationTaskDO??????
     * @return ?????????AgentOperationTaskDO??????????????????agent??????case??????AgentOperationTaskPO??????
     */
    private AgentOperationTaskPO agentOperationTaskDO2AgentOperationTaskPOUninstall(AgentOperationTaskDO agentOperationTaskDO, Integer hostsNumber) {
        AgentOperationTaskPO agentOperationTaskPO = new AgentOperationTaskPO();
        //???????????????????????????????????????
        agentOperationTaskPO.setTaskStatus(AgentOperationTaskStatusEnum.RUNNING.getCode());
        agentOperationTaskPO.setTaskType(agentOperationTaskDO.getTaskType());
        agentOperationTaskPO.setTaskName(agentOperationTaskDO.getTaskName());
        agentOperationTaskPO.setExternalAgentTaskId(agentOperationTaskDO.getExternalAgentTaskId());
        agentOperationTaskPO.setHostsNumber(hostsNumber);
        agentOperationTaskPO.setOperator(agentOperationTaskDO.getOperator());
        agentOperationTaskPO.setTaskStartTime(new Date());
        return agentOperationTaskPO;
    }

    /**
     * ??????agent??????????????????????????????
     * @return ????????????
     */
    private CheckResult checkUpgradeParameter(AgentOperationTaskDO agentOperationTaskDO) {
        if(CollectionUtils.isEmpty(agentOperationTaskDO.getAgentIdList())) {
            return new CheckResult(
                    false,
                    ErrorCodeEnum.ILLEGAL_PARAMS.getCode(),
                    "??????agentIdList????????????"
            );
        }
        if(null == agentOperationTaskDO.getTargetAgentVersionId() || agentOperationTaskDO.getTargetAgentVersionId() <= 0) {
            return new CheckResult(
                    false,
                    ErrorCodeEnum.ILLEGAL_PARAMS.getCode(),
                    "??????targetAgentVersionId?????????????????????????????????0"
            );
        }
        return new CheckResult(true);
    }

    /**
     * ??????agent??????????????????????????????
     * @return ????????????
     */
    private CheckResult checkUninstallParameter(AgentOperationTaskDO agentOperationTaskDO) {
        if(CollectionUtils.isEmpty(agentOperationTaskDO.getAgentIdList())) {
            return new CheckResult(
                    false,
                    ErrorCodeEnum.ILLEGAL_PARAMS.getCode(),
                    "??????agentIdList????????????"
            );
        }
        return new CheckResult(true);
    }

    /**
     * ??????Agent????????????
     * @param agentOperationTaskDO ?????? agent ??????????????? AgentOperationTaskDO ??????
     * @param operator ?????????
     * @return ????????????id
     * @throws ServiceException ???????????????????????????????????????
     */
    private Long handleCreateAgentUninstallOperationTask(AgentOperationTaskDO agentOperationTaskDO, String operator) throws ServiceException {
        /*
         * ????????????
         */
        CheckResult checkResult = checkUninstallParameter(agentOperationTaskDO);
        if(!checkResult.getCheckResult()) {
            throw new ServiceException(checkResult.getMessage(), checkResult.getCode());
        }
        /*
         * ???????????????agent????????????
         */
        List<Long> agentIdList = agentOperationTaskDO.getAgentIdList();
        Set<String> hostNameSet = new HashSet<>();//???????????????set??????????????????host???????????????agent
        List<String> hostIpList = new ArrayList<>(agentIdList.size());
        for (Long agentId : agentIdList) {
            AgentDO agentDO = agentManageService.getById(agentId);
            if(null == agentDO) {
                throw new ServiceException(String.format("Agent={id=%d}?????????????????????", agentId), ErrorCodeEnum.HOST_NOT_EXISTS.getCode());
            }
            if(StringUtils.isBlank(agentDO.getHostName())) {
                throw new ServiceException(
                        String.format("Agent??????={id={%d}}??????hostName???????????????", agentId),
                        ErrorCodeEnum.SYSTEM_INTERNAL_ERROR.getCode()
                );
            } else {
                if(hostNameSet.add(agentDO.getHostName())) {
                    hostIpList.add(hostManageService.getHostByHostName(agentDO.getHostName()).getIp());
                }
            }
        }
        /*
         * ???????????????????????????agent??????????????????
         */
        Long externalTaskId = commitRemoteAgentUninstallTask(agentOperationTaskDO, hostIpList);
        /*
         * ???????????????????????????????????????
         */
        if(!remoteOperationTaskService.actionTask(externalTaskId, AgentOperationTaskActionEnum.START)) {//????????????
            throw new ServiceException(
                    String.format("???????????????RemoteOperationTaskService.actionTask(externalTaskId={%d}, AgentOperationTaskActionEnum.START)????????????", externalTaskId),
                    ErrorCodeEnum.SYSTEM_INTERNAL_ERROR.getCode()
            );
        }
        /*
         * ??????agent???????????????????????????
         */
        agentOperationTaskDO.setExternalAgentTaskId(externalTaskId);
        agentOperationTaskDO.setOperator(CommonConstant.getOperator(operator));
        AgentOperationTaskPO agentOperationTaskPO = agentOperationTaskDO2AgentOperationTaskPOUninstall(agentOperationTaskDO, hostNameSet.size());
        agentOperationTaskDAO.insert(agentOperationTaskPO);
        saveAgentOperationSubTask(hostNameSet, agentOperationTaskPO.getId(), AgentOperationTaskTypeEnum.UNINSTALL, operator);
        return agentOperationTaskPO.getId();
    }

    /**
     * ??????agent????????????
     * @param agentOperationTaskDO agent ??????????????????
     * @param operator ?????????
     * @throws ServiceException ???????????????????????????????????????
     */
    private Long handleCreateAgentInstallOperationTask(AgentOperationTaskDO agentOperationTaskDO, String operator) throws ServiceException {
        /*
         * ????????????
         */
        CheckResult checkResult = checkInstallParameter(agentOperationTaskDO);
        if(!checkResult.getCheckResult()) {
            throw new ServiceException(checkResult.getMessage(), checkResult.getCode());
        }
        /*
         * ???????????????agent??????????????????????????????????????????agent
         */
        List<Long> hostIdList = agentOperationTaskDO.getHostIdList();
        List<String> hostNameList = new ArrayList<>(hostIdList.size());
        List<String> hostIpList = new ArrayList<>(hostIdList.size());
        for (Long hostId : hostIdList) {
            HostDO hostDO = hostManageService.getById(hostId);
            if(null == hostDO) {
                throw new ServiceException(String.format("?????????Agent?????????={id=%d}?????????????????????", hostId), ErrorCodeEnum.HOST_NOT_EXISTS.getCode());
            }
            AgentDO agentDO = agentManageService.getAgentByHostName(hostDO.getHostName());
            if(null != agentDO) {
                throw new ServiceException(String.format("?????????Agent?????????={id=%d}????????????Agent={id=%d}", hostId, agentDO.getId()), ErrorCodeEnum.AGENT_EXISTS_IN_HOST_WHEN_AGENT_INSTALL.getCode());
            }
            if(StringUtils.isBlank(hostDO.getHostName())) {
                throw new ServiceException(
                        String.format("Host??????={id={%d}}??????hostName???????????????", hostId),
                        ErrorCodeEnum.SYSTEM_INTERNAL_ERROR.getCode()
                );
            } else {
                hostNameList.add(hostDO.getHostName());
                hostIpList.add(hostDO.getIp());
            }
        }
        /*
         * ???????????????????????????agent??????????????????
         */
        Long externalTaskId = commitRemoteAgentInstallTask(agentOperationTaskDO, hostIpList);
        /*
         * ???????????????????????????????????????
         * TODO???remoteOperationTaskService ??????
         */
        if(!remoteOperationTaskService.actionTask(externalTaskId, AgentOperationTaskActionEnum.START)) {//????????????
            throw new ServiceException(
                    String.format("???????????????RemoteOperationTaskService.actionTask(externalTaskId={%d}, AgentOperationTaskActionEnum.START)????????????", externalTaskId),
                    ErrorCodeEnum.SYSTEM_INTERNAL_ERROR.getCode()
            );
        }
        /*
         * ??????agent???????????????????????????
         */
        agentOperationTaskDO.setExternalAgentTaskId(externalTaskId);
        agentOperationTaskDO.setOperator(CommonConstant.getOperator(operator));
        AgentOperationTaskPO agentOperationTaskPO = agentOperationTaskDO2AgentOperationTaskPOInstall(agentOperationTaskDO);
        agentOperationTaskDAO.insert(agentOperationTaskPO);
        saveAgentOperationSubTask(hostNameList, agentOperationTaskPO.getId(), AgentOperationTaskTypeEnum.INSTALL, operator);
        return agentOperationTaskPO.getId();
    }

    /**
     * ????????????????????????agent????????????
     * @param hostNameList ????????????????????????
     * @param agentOperationTaskId AgentOperationTask ?????? id
     * @param agentOperationTaskTypeEnum AgentOperationTask ??????
     * @param operator ?????????
     * @throws ServiceException ???????????????????????????????????????
     */
    private void saveAgentOperationSubTask(Collection<String> hostNameList, Long agentOperationTaskId, AgentOperationTaskTypeEnum agentOperationTaskTypeEnum, String operator) throws ServiceException {
        for (String hostName : hostNameList) {
            AgentOperationSubTaskDO agentOperationSubTaskDO = new AgentOperationSubTaskDO();
            HostDO hostDO = hostManageService.getHostByHostName(hostName);
            if(null == hostDO) {
                throw new ServiceException(
                        String.format("Host={hostName=%s}?????????????????????", hostName),
                        ErrorCodeEnum.HOST_NOT_EXISTS.getCode()
                );
            }
            agentOperationSubTaskDO.setAgentOperationTaskId(agentOperationTaskId);
            agentOperationSubTaskDO.setContainer(hostDO.getContainer());
            agentOperationSubTaskDO.setHostName(hostName);
            agentOperationSubTaskDO.setIp(hostDO.getIp());
            agentOperationSubTaskDO.setTaskStartTime(new Date());
            if(!agentOperationTaskTypeEnum.getCode().equals(AgentOperationTaskTypeEnum.INSTALL.getCode())) {//??? "??????" agent ?????? ??????
                AgentDO agentDO = agentManageService.getAgentByHostName(hostName);
                if(null == agentDO) {
                    throw new ServiceException(
                            String.format("Agent={hostName=%s}?????????????????????", hostName),
                            ErrorCodeEnum.AGENT_NOT_EXISTS.getCode()
                    );
                }
                agentOperationSubTaskDO.setSourceAgentVersionId(agentDO.getAgentVersionId());
            }
            agentOperationSubTaskManageService.createAgentOperationSubTask(agentOperationSubTaskDO, CommonConstant.getOperator(operator));
        }
    }

    /**
     * ???AgentOperationTaskDO???????????????agent??????case??????AgentOperationTaskPO??????
     * @param agentOperationTaskDO ?????????AgentOperationTaskDO??????
     * @return ?????????AgentOperationTaskDO??????????????????agent??????case??????AgentOperationTaskPO??????
     */
    private AgentOperationTaskPO agentOperationTaskDO2AgentOperationTaskPOInstall(AgentOperationTaskDO agentOperationTaskDO) {
        AgentOperationTaskPO agentOperationTaskPO = new AgentOperationTaskPO();
        //???????????????????????????????????????
        agentOperationTaskPO.setTaskStatus(AgentOperationTaskStatusEnum.RUNNING.getCode());
        agentOperationTaskPO.setTaskType(agentOperationTaskDO.getTaskType());
        agentOperationTaskPO.setTaskName(agentOperationTaskDO.getTaskName());
        agentOperationTaskPO.setExternalAgentTaskId(agentOperationTaskDO.getExternalAgentTaskId());
        agentOperationTaskPO.setHostsNumber(agentOperationTaskDO.getHostIdList().size());
        agentOperationTaskPO.setTargetAgentVersionId(agentOperationTaskDO.getTargetAgentVersionId());
        agentOperationTaskPO.setOperator(agentOperationTaskDO.getOperator());
        agentOperationTaskPO.setTaskStartTime(new Date());
        return agentOperationTaskPO;
    }

    /**
     * ??????agent???????????????????????????????????????agent??????????????????????????????????????????agent????????????id
     * @param agentOperationTaskDO agent??????????????????
     * @param hostNameList
     * @return ??????????????????agent????????????id
     * @throws ServiceException ???????????????????????????????????????
     */
    private Long commitRemoteAgentInstallTask(AgentOperationTaskDO agentOperationTaskDO, List<String> hostNameList) throws ServiceException {
        AgentOperationTaskCreation agentOperationTaskCreation = new AgentOperationTaskCreation();
        agentOperationTaskCreation.setTaskType(agentOperationTaskDO.getTaskType());
        agentOperationTaskCreation.setTaskName(agentOperationTaskDO.getTaskName());
        agentOperationTaskCreation.setHostList(hostNameList);
        //set about agent version
        Long agentVerisonId = agentOperationTaskDO.getTargetAgentVersionId();
        AgentVersionDO agentVersionDO = agentVersionManageService.getById(agentVerisonId);
        if(null == agentVersionDO) {
            throw new ServiceException(
                    String.format("?????????Agent???AgentVersion={id=%d}?????????????????????", agentVerisonId),
                    ErrorCodeEnum.AGENT_VERSION_NOT_EXISTS.getCode()
            );
        }
        String downloadUrl = agentVersionManageService.getAgentInstallFileDownloadUrl(agentVerisonId);
        agentOperationTaskCreation.setAgentPackageDownloadUrl(downloadUrl);
        agentOperationTaskCreation.setAgentPackageMd5(agentVersionDO.getFileMd5());
        agentOperationTaskCreation.setAgentPackageName(agentVersionDO.getFileName());
        Result<Long> externalTaskIdResult = remoteOperationTaskService.createTask(agentOperationTaskCreation);
        if(externalTaskIdResult.failed()) {
            throw new ServiceException(externalTaskIdResult.getMessage(), externalTaskIdResult.getCode());
        }
        Long externalTaskId = externalTaskIdResult.getData();
        return externalTaskId;
    }

    /**
     * ??????agent??????????????????????????????
     * @return ????????????
     */
    private CheckResult checkInstallParameter(AgentOperationTaskDO agentOperationTaskDO) {
        if(CollectionUtils.isEmpty(agentOperationTaskDO.getHostIdList())) {
            return new CheckResult(
                    false,
                    ErrorCodeEnum.ILLEGAL_PARAMS.getCode(),
                    "??????hostIdList????????????"
            );
        }
        if(null == agentOperationTaskDO.getTargetAgentVersionId() || agentOperationTaskDO.getTargetAgentVersionId() <= 0) {
            return new CheckResult(
                    false,
                    ErrorCodeEnum.ILLEGAL_PARAMS.getCode(),
                    "??????targetAgentVersionId?????????????????????????????????0"
            );
        }
        return new CheckResult(true);
    }

    @Override
    public AgentOperationTaskDO getById(Long taskId) {
        AgentOperationTaskPO agentOperationTaskPO = null;
        agentOperationTaskPO = agentOperationTaskDAO.selectByPrimaryKey(taskId);
        if(null == agentOperationTaskPO) {
            return null;
        } else {
            return ConvertUtil.obj2Obj(agentOperationTaskPO, AgentOperationTaskDO.class);
        }
    }

    @Override
    public String getTaskLog(Long taskId, String hostname) {
        AgentOperationTaskDO agentOperationTaskDO = getById(taskId);
        if(null == agentOperationTaskDO) {
            throw new ServiceException(String.format("AgentOperationTask={id=%d}?????????????????????", taskId), ErrorCodeEnum.AGENT_OPERATION_TASK_NOT_EXISTS.getCode());
        }
        Result<AgentOperationTaskLog> agentOperationTaskLogResult = remoteOperationTaskService.getTaskLog(agentOperationTaskDO.getExternalAgentTaskId(), hostname);
        if(agentOperationTaskLogResult.failed()) {
            throw new ServiceException(agentOperationTaskLogResult.getMessage(), agentOperationTaskLogResult.getCode());
        }
        AgentOperationTaskLog agentOperationTaskLog = agentOperationTaskLogResult.getData();
        return agentOperationTaskLog.getStdout();
    }

    @Override
    public List<AgentOperationTaskDO> paginationQueryByConditon(AgentOperationTaskPaginationQueryConditionDO agentOperationTaskPaginationQueryConditionDO) {
        String column = agentOperationTaskPaginationQueryConditionDO.getSortColumn();
        if (column != null) {
            for (char c : column.toCharArray()) {
                if (!Character.isLetter(c) && c != '_') {
                    return Collections.emptyList();
                }
            }
        }
        List<AgentOperationTaskPO> agentOperationTaskPOList = agentOperationTaskDAO.paginationQueryByConditon(agentOperationTaskPaginationQueryConditionDO);
        if(CollectionUtils.isEmpty(agentOperationTaskPOList)) {
            return new ArrayList<>();
        }
        return ConvertUtil.list2List(agentOperationTaskPOList, AgentOperationTaskDO.class);
    }

    @Override
    public Integer queryCountByCondition(AgentOperationTaskPaginationQueryConditionDO agentOperationTaskPaginationQueryConditionDO) {
        return agentOperationTaskDAO.queryCountByConditon(agentOperationTaskPaginationQueryConditionDO);
    }

    @Override
    public Map<String, AgentOperationTaskSubStateEnum> getTaskResultByExternalTaskId(Long externalTaskId) {
        Result<Map<String, AgentOperationTaskSubStateEnum>> result = remoteOperationTaskService.getTaskResult(externalTaskId);
        if(result.failed()) {
            throw new ServiceException(result.getMessage(), result.getCode());
        } else {
            return result.getData();
        }
    }

    @Override
    public void updateAgentOperationTasks() {

        /*
         * ????????????????????????????????????"FINISHED" agent operation task list
         */
        List<AgentOperationTaskPO> agentOperationTaskPOList = agentOperationTaskDAO.selectByTaskStatus(AgentOperationTaskStateEnum.RUNNING.getCode());
        agentOperationTaskPOList.addAll(agentOperationTaskDAO.selectByTaskStatus(AgentOperationTaskStateEnum.BLOCKED.getCode()));

        /*
         * ??????????????????????????????????????? agent operation task ???????????? agent operation task ?????????"?????????"????????????????????? ?????????????????????????????? agent ??????
         * ?????? agent operation task ??????????????????????????????????????????????????? sub agent operation task ????????????????????????????????? tb_agent_operation_task???tb_agent_operation_sub_task ????????????
         */
        for (AgentOperationTaskPO agentOperationTaskPO : agentOperationTaskPOList) {
            Result<AgentOperationTaskStateEnum> agentOperationTaskStateEnumResult = remoteOperationTaskService.getTaskExecuteState(agentOperationTaskPO.getExternalAgentTaskId());
            if(agentOperationTaskStateEnumResult.failed()) {
                throw new ServiceException(agentOperationTaskStateEnumResult.getMessage(), agentOperationTaskStateEnumResult.getCode());
            } else {
                Result<Map<String, AgentOperationTaskSubStateEnum>> hostName2AgentOperationTaskSubStateEnumMapResult = remoteOperationTaskService.getTaskResult(agentOperationTaskPO.getExternalAgentTaskId());
                AgentOperationTaskStateEnum agentOperationTaskStateEnum = agentOperationTaskStateEnumResult.getData();
                if(hostName2AgentOperationTaskSubStateEnumMapResult.failed()) {
                    throw new ServiceException(hostName2AgentOperationTaskSubStateEnumMapResult.getMessage(), hostName2AgentOperationTaskSubStateEnumMapResult.getCode());
                } else {
                    Map<String, AgentOperationTaskSubStateEnum> hostName2AgentOperationTaskSubStateEnumMap = hostName2AgentOperationTaskSubStateEnumMapResult.getData();
                    //TODO?????????????????? hostName2AgentOperationTaskSubStateEnumMap.key ??? ip
                    Map<String, AgentOperationSubTaskDO> hostName2AgentOperationSubTaskDOMapInLocal = agentOperationSubTaskDOList2Map(agentOperationSubTaskManageService.getByAgentOperationTaskId(agentOperationTaskPO.getId()));
                    for(Map.Entry<String, AgentOperationTaskSubStateEnum> entry : hostName2AgentOperationTaskSubStateEnumMap.entrySet()) {
                        String hostName = entry.getKey();//?????????
                        AgentOperationTaskSubStateEnum agentOperationTaskSubStateEnum = entry.getValue();//???????????? agent ???????????? sub state
                        AgentOperationSubTaskDO agentOperationSubTaskDOInLocal = hostName2AgentOperationSubTaskDOMapInLocal.get(hostName);
                        if(null == agentOperationSubTaskDOInLocal) {
                            LOGGER.error(
                                    String.format("???????????????????????????AgentOperationSubTaskDO[hostName=%s]??????????????????????????????", hostName)
                            );
                            continue;
                        }
                        if(AgentOperationTaskStateEnum.FINISHED.getCode().equals(agentOperationTaskStateEnum.getCode())) {//??????????????????
                            //?????????????????? ?????????????????????????????? agent ??????
                            //TODO?????????????????? hostName ??? ip
                            HostDO hostDO = hostManageService.getHostByIp(hostName).get(0);
                            if(null == hostDO) {
                                throw new ServiceException(
                                        String.format("Host={ip=%s}?????????????????????", hostName),
                                        ErrorCodeEnum.HOST_NOT_EXISTS.getCode()
                                );
                            } else {
                                try {
                                    actionAgent(agentOperationTaskPO, hostDO.getHostName());
                                } catch (ServiceException ex) {
                                    if(!ex.getServiceExceptionCode().equals(ErrorCodeEnum.AGENT_EXISTS_IN_HOST_WHEN_AGENT_CREATE.getCode())) {
                                        throw ex;
                                    }
                                }
                            }
                        }
                        if(agentOperationTaskSubStateEnum.finish()) {//????????? TODO???
                            //?????? sub agent operation task ?????? task_end_time ????????????????????????
                            agentOperationSubTaskDOInLocal.setTaskEndTime(new Date());
                        }
                        //?????? agentOperationTask ????????????????????? sub agent operation task ????????????????????????????????? tb_agent_operation_sub_task ????????????
                        agentOperationSubTaskDOInLocal.setExecuteStatus(agentOperationTaskSubStateEnum.getCode());
                        agentOperationSubTaskManageService.updateAgentOperationSubTask(agentOperationSubTaskDOInLocal);
                    }
                    //?????? agent operation task ????????????????????? "FINISHED"?????????????????????????????? tb_agent_operation_task ?????????????????? task_end_time ???
                    if((AgentOperationTaskStateEnum.FINISHED.getCode().equals(agentOperationTaskStateEnum.getCode()))) {
                        agentOperationTaskPO.setTaskEndTime(new Date());
                    }
                    agentOperationTaskPO.setTaskStatus(agentOperationTaskStateEnum.getCode());
                    agentOperationTaskDAO.updateByPrimaryKey(agentOperationTaskPO);
                }
            }
        }

    }

    @Override
    public boolean unfinishedAgentOperationTaskExistsByAgentVersionId(Long agentVersionId) {
        List<AgentOperationTaskPO> agentOperationTaskPOList = agentOperationTaskDAO.selectByAgentVersionId(agentVersionId);
        if(CollectionUtils.isNotEmpty(agentOperationTaskPOList)) {
            for (AgentOperationTaskPO agentOperationTaskPO : agentOperationTaskPOList) {
                if(!AgentOperationTaskStatusEnum.isFinished(agentOperationTaskPO.getTaskStatus())) {
                    return true;
                }
            }
            return false;
        } else {
            return false;
        }
    }

    @Override
    public AgentOperationTaskDO agent2AgentOperationTaskUnInstall(AgentDO agentDO) {

        //TODO???

        return null;

    }

    /**
     * ?????????AgentOperationSubTaskDO?????????????????? hostName : AgentOperationSubTaskDO ?????????
     * @param agentOperationSubTaskDOList AgentOperationSubTaskDO ?????????
     * @return ???????????????AgentOperationSubTaskDO????????????????????? hostName : AgentOperationSubTaskDO ?????????
     */
    private Map<String, AgentOperationSubTaskDO> agentOperationSubTaskDOList2Map(List<AgentOperationSubTaskDO> agentOperationSubTaskDOList) {
        Map<String, AgentOperationSubTaskDO> hostName2AgentOperationSubTaskDOMapInLocal = new HashMap<>();
        for (AgentOperationSubTaskDO agentOperationSubTaskDO : agentOperationSubTaskDOList) {
            //TODO?????????????????? hostName 2 ip
            HostDO hostDO = hostManageService.getHostByHostName(agentOperationSubTaskDO.getHostName());
            if(null != hostDO) {
                hostName2AgentOperationSubTaskDOMapInLocal.put(hostDO.getIp(), agentOperationSubTaskDO);
            }
        }
        return hostName2AgentOperationSubTaskDOMapInLocal;
    }

    /**
     * ?????????????????? ?????????????????????????????? agent ??????
     * @param agentOperationTaskPO AgentOperationTaskPO ??????
     * @param hostName ?????????
     */
    private void actionAgent(AgentOperationTaskPO agentOperationTaskPO, String hostName) {
        if(AgentOperationTaskTypeEnum.INSTALL.getCode().equals(agentOperationTaskPO.getTaskType())) {
            /*
             * ????????????Agent??????
             */
            HostDO hostDO = hostManageService.getHostByHostName(hostName);
            if(null == hostDO) {
                throw new ServiceException(
                        String.format("Host={hostName=%s}?????????????????????", hostName),
                        ErrorCodeEnum.HOST_NOT_EXISTS.getCode()
                );
            }
            AgentDO agentDO = new AgentDO();
            agentDO.setHostName(hostDO.getHostName());
            agentDO.setIp(hostDO.getIp());
            agentDO.setCollectType(AgentCollectTypeEnum.COLLECT_HOST_AND_CONTAINERS.getCode());
            agentDO.setAgentVersionId(agentOperationTaskPO.getTargetAgentVersionId());
            agentManageService.createAgent(agentDO, null);
        } else if(AgentOperationTaskTypeEnum.UNINSTALL.getCode().equals(agentOperationTaskPO.getTaskType())) {
            /*
             * ????????????Agent??????
             */
            agentManageService.deleteAgentByHostName(hostName, false, false, null);
        } else if(AgentOperationTaskTypeEnum.UPGRADE.getCode().equals(agentOperationTaskPO.getTaskType())) {
            /*
             * ????????????Agent???????????? agent version id
             */
            AgentDO agentDO = agentManageService.getAgentByHostName(hostName);
            if(null == agentDO) {
                throw new ServiceException(
                        String.format("Agent={hostName=%s}?????????????????????", hostName),
                        ErrorCodeEnum.AGENT_NOT_EXISTS.getCode()
                );
            }
            agentDO.setAgentVersionId(agentOperationTaskPO.getTargetAgentVersionId());
            agentManageService.updateAgent(agentDO, null);
        } else {
            throw new ServiceException(
                    String.format("AgentOperationTaskPO??????={%s}???taskType?????????={%d}???????????????????????????????????????AgentOperationTaskTypeEnum", JSON.toJSONString(agentOperationTaskPO), agentOperationTaskPO.getTaskType()),
                    ErrorCodeEnum.SYSTEM_INTERNAL_ERROR.getCode()
            );
        }
    }

}
