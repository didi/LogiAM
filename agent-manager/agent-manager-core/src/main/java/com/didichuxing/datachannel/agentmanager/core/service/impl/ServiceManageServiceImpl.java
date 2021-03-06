package com.didichuxing.datachannel.agentmanager.core.service.impl;

import com.alibaba.fastjson.JSON;
import com.didichuxing.datachannel.agentmanager.common.bean.domain.host.HostDO;
import com.didichuxing.datachannel.agentmanager.common.bean.domain.logcollecttask.LogCollectTaskDO;
import com.didichuxing.datachannel.agentmanager.common.bean.domain.service.ServiceDO;
import com.didichuxing.datachannel.agentmanager.common.bean.domain.service.ServicePaginationQueryConditionDO;
import com.didichuxing.datachannel.agentmanager.common.bean.domain.service.ServicePaginationRecordDO;
import com.didichuxing.datachannel.agentmanager.common.bean.po.service.ServicePO;
import com.didichuxing.datachannel.agentmanager.common.bean.po.service.ServiceHostPO;
import com.didichuxing.datachannel.agentmanager.common.bean.po.service.ServiceProjectPO;
import com.didichuxing.datachannel.agentmanager.common.constant.CommonConstant;
import com.didichuxing.datachannel.agentmanager.common.enumeration.ErrorCodeEnum;
import com.didichuxing.datachannel.agentmanager.common.enumeration.SourceEnum;
import com.didichuxing.datachannel.agentmanager.common.enumeration.operaterecord.ModuleEnum;
import com.didichuxing.datachannel.agentmanager.common.enumeration.operaterecord.OperationEnum;
import com.didichuxing.datachannel.agentmanager.common.enumeration.service.ServiceTypeEnum;
import com.didichuxing.datachannel.agentmanager.common.exception.ServiceException;
import com.didichuxing.datachannel.agentmanager.common.bean.common.CheckResult;
import com.didichuxing.datachannel.agentmanager.common.util.ConvertUtil;
import com.didichuxing.datachannel.agentmanager.core.common.OperateRecordService;
import com.didichuxing.datachannel.agentmanager.core.logcollecttask.manage.LogCollectTaskManageService;
import com.didichuxing.datachannel.agentmanager.core.service.ServiceLogCollectTaskManageService;
import com.didichuxing.datachannel.agentmanager.core.service.ServiceProjectManageService;
import com.didichuxing.datachannel.agentmanager.persistence.mysql.ServiceMapper;
import com.didichuxing.datachannel.agentmanager.core.service.ServiceHostManageService;
import com.didichuxing.datachannel.agentmanager.core.service.ServiceManageService;
import com.didichuxing.datachannel.agentmanager.thirdpart.service.extension.ServiceManageServiceExtension;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@org.springframework.stereotype.Service
public class ServiceManageServiceImpl implements ServiceManageService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceManageServiceImpl.class);@Autowired
    private ServiceMapper serviceDAO;

    @Autowired
    private ServiceManageServiceExtension serviceManageServiceExtension;

    @Autowired
    private ServiceHostManageService serviceHostManageService;

    @Autowired
    private ServiceLogCollectTaskManageService serviceLogCollectTaskManageService;

    @Autowired
    private OperateRecordService operateRecordService;

    @Autowired
    private LogCollectTaskManageService logCollectTaskManageService;

    @Autowired
    private ServiceProjectManageService serviceProjectManageService;

    @Override
    @Transactional
    public Long createService(ServiceDO service, String operator) {
        return handleCreateService(service, operator);
    }

    @Override
    public ServiceDO getServiceByServiceName(String serviceName) {
        ServicePO servicePO = serviceDAO.selectByServiceName(serviceName);
        if(null == servicePO) {
            return null;
        } else {
            return serviceManageServiceExtension.service2ServiceDO(servicePO);
        }
    }

    @Override
    @Transactional
    public void updateService(ServiceDO serviceDO, String operator) {
        handleUpdateService(serviceDO, operator);
    }

    @Override
    @Transactional
    public void deleteService(Long id, boolean cascadeDeleteHostAndLogCollectTaskRelation, String operator) {
        handleDeleteService(id, cascadeDeleteHostAndLogCollectTaskRelation, operator);
    }

    @Override
    public List<ServiceDO> list() {
        List<ServicePO> servicePOList = serviceDAO.list();
        if(CollectionUtils.isNotEmpty(servicePOList)) {
            return serviceManageServiceExtension.servicePOList2serviceDOList(servicePOList);
        } else {
            return new ArrayList<>();
        }
    }

    @Override
    public ServiceDO getServiceById(Long id) {
        ServicePO servicePO = serviceDAO.selectByPrimaryKey(id);
        if(null == servicePO) {
            return null;
        } else {
            return serviceManageServiceExtension.service2ServiceDO(servicePO);
        }
    }

    @Override
    public List<ServiceDO> getServicesByHostId(Long hostId) {
        List<ServicePO> servicePOList = serviceDAO.selectByHostId(hostId);
        if(CollectionUtils.isEmpty(servicePOList)) {
            return new ArrayList<>();
        } else {
            return serviceManageServiceExtension.servicePOList2serviceDOList(servicePOList);
        }
    }

    @Override
    public List<ServiceDO> getServicesByProjectId(Long projectId) {
        List<ServicePO> servicePOList = serviceDAO.selectByProjectId(projectId);
        if(CollectionUtils.isEmpty(servicePOList)) {
            return new ArrayList<>();
        } else {
            return serviceManageServiceExtension.servicePOList2serviceDOList(servicePOList);
        }
    }

    @Override
    public List<ServiceDO> getServicesByLogCollectTaskId(Long logCollectTaskId) {
        List<ServicePO> servicePOList = serviceDAO.selectByLogCollectTaskId(logCollectTaskId);
        if(CollectionUtils.isEmpty(servicePOList)) {
            return new ArrayList<>();
        } else {
            return serviceManageServiceExtension.servicePOList2serviceDOList(servicePOList);
        }
    }

    @Override
    public List<ServicePaginationRecordDO> paginationQueryByConditon(ServicePaginationQueryConditionDO query) {
        String column = query.getSortColumn();
        if (column != null) {
            for (char c : column.toCharArray()) {
                if (!Character.isLetter(c) && c != '_') {
                    return Collections.emptyList();
                }
            }
        }
        return serviceDAO.paginationQueryByConditon(query);
    }

    @Override
    public Integer queryCountByCondition(ServicePaginationQueryConditionDO servicePaginationQueryConditionDO) {
        return serviceDAO.queryCountByCondition(servicePaginationQueryConditionDO);
    }

    @Override
    public Long countAll() {
        return serviceDAO.countAll();
    }

    /**
     * @param serviceName2ProjectServiceNameMap ??????????????? ~ ????????????????????????????????????????????????
     * @param serviceDOListInLocal ?????????????????????????????????
     * @return ?????? serviceName2ProjectServiceNameMap & serviceDOListInLocal ?????? "???????????? id ~ ?????????????????????????????? id " ???????????????
     */
    private List<ServiceProjectPO> buildServiceProjectRelation(Map<String, String> serviceName2ProjectServiceNameMap, List<ServiceDO> serviceDOListInLocal) {
        List<ServiceProjectPO> serviceProjectPOList = new ArrayList<>();
        Map<String, ServiceDO> serviceName2ServiceDOMap = new HashMap<>();
        for (ServiceDO serviceDO : serviceDOListInLocal) {
            serviceName2ServiceDOMap.put(serviceDO.getServicename(), serviceDO);
        }
        for (Map.Entry<String, String> entry : serviceName2ProjectServiceNameMap.entrySet()) {
            String serviceName = entry.getKey();
            String projectServiceName = entry.getValue();
            ServiceDO serviceDO = serviceName2ServiceDOMap.get(serviceName);
            ServiceDO projectServiceDO = serviceName2ServiceDOMap.get(projectServiceName);
            if(null != projectServiceDO && null != serviceDO) {
                ServiceProjectPO serviceProjectPO = new ServiceProjectPO(serviceDO.getId(), projectServiceDO.getExtenalServiceId());
                serviceProjectPOList.add(serviceProjectPO);
            }
        }
        return serviceProjectPOList;
    }

    /**
     * ?????????????????? serviceDOList ????????????????????? ~ ?????????????????????????????????????????????????????????
     * @param serviceDOList ???????????????
     * @return ???????????????????????? serviceDOList ???????????????????????? ~ ?????????????????????????????????????????????????????????
     */
    private Map<String, String> buildServiceName2ProjectServiceNameRelation(List<ServiceDO> serviceDOList) {
        Map<String, String> serviceName2ProjectServiceNameMap = new HashMap<>();
        if(CollectionUtils.isNotEmpty(serviceDOList)) {
            Map<Long, ServiceDO> externalServiceId2ServiceDOMap = new HashMap<>();
            for (ServiceDO serviceDO : serviceDOList) {
                externalServiceId2ServiceDOMap.put(serviceDO.getExtenalServiceId(), serviceDO);
            }
            for (ServiceDO serviceDO : serviceDOList) {
                ServiceDO projectServiceDO = getProjectServiceDO(serviceDO, externalServiceId2ServiceDOMap);
                if(null != projectServiceDO) {
                    serviceName2ProjectServiceNameMap.put(serviceDO.getServicename(), projectServiceDO.getServicename());
                }
            }
            return serviceName2ProjectServiceNameMap;
        } else {
            return serviceName2ProjectServiceNameMap;
        }
    }

    /**
     * ????????????????????????????????????????????????
     * @param serviceDO ????????????
     * @param externalServiceId2ServiceDOMap externalServiceId : serviceDO ????????????
     * @return ???????????????????????????????????????????????? ???????????????????????????????????????????????? return null
     */
    private ServiceDO getProjectServiceDO(ServiceDO serviceDO, Map<Long, ServiceDO> externalServiceId2ServiceDOMap) {
        if(null == externalServiceId2ServiceDOMap || null == serviceDO) {
            return null;
        } else {
            String cate = serviceDO.getCate();
            if(StringUtils.isNotBlank(cate)) {
                if(cate.equals(ServiceTypeEnum.??????.getDescription())) {
                    return serviceDO;
                } else if(ServiceTypeEnum.subOfProject(cate)) {//???????????????????????????
                    Long pid = serviceDO.getPid();
                    if(null != pid) {
                        ServiceDO parentServiceDO = externalServiceId2ServiceDOMap.get(pid);
                        return getProjectServiceDO(parentServiceDO, externalServiceId2ServiceDOMap);
                    } else {
                        return null;
                    }
                } else {
                    return null;
                }

            } else {
                return null;
            }
        }
    }

    /**
     * ????????????
     * @param serviceId ?????????????????????id
     * @param cascadeDeleteHostAndLogCollectTaskRelation ?????????????????? Service & LogCollectTask ????????????
     * @param operator ?????????
     * @throws ServiceException ???????????????????????????????????????
     */
    private void handleDeleteService(Long serviceId, boolean cascadeDeleteHostAndLogCollectTaskRelation, String operator) throws ServiceException {
        /*
         * ???????????????service???????????????????????????
         */
        if(null == getServiceById(serviceId)) {
            throw new ServiceException(
                    String.format("?????????Service??????={id=%d}?????????????????????", serviceId),
                    ErrorCodeEnum.SERVICE_NOT_EXISTS.getCode()
            );
        }
        /*
         * ??????????????? service & logcollecttask ????????????
         * ??????????????????????????????????????? service ?????????????????? logcollecttask
         */
        if(cascadeDeleteHostAndLogCollectTaskRelation) {//????????????
            /*
             * ????????????-??????????????????????????????
             */
            serviceLogCollectTaskManageService.removeServiceLogCollectTaskByServiceId(serviceId);
        } else {//?????????????????????????????????
            List<LogCollectTaskDO> logCollectTaskDOList = logCollectTaskManageService.getLogCollectTaskListByServiceId(serviceId);
            if(CollectionUtils.isNotEmpty(logCollectTaskDOList)) {//?????????service????????????logcollecttask
                throw new ServiceException(
                        String.format("?????????Service??????{%d}????????????LogCollectTask?????????????????????LogCollectTask & Service????????????", logCollectTaskDOList.size()),
                        ErrorCodeEnum.SERVICE_DELETE_FAILED_CAUSE_BY_RELA_LOGCOLLECTTASK_EXISTS.getCode()
                );
            }
        }
        /*
         * ????????????-??????????????????
         */
        serviceHostManageService.deleteServiceHostByServiceId(serviceId);
        /*
         * ???????????? - ??????????????????
         */
        serviceProjectManageService.deleteByServiceId(serviceId);
        /*
         * ??????????????????
         */
        serviceDAO.deleteByPrimaryKey(serviceId);
        /*
         * ????????????????????????
         */
        operateRecordService.save(
                ModuleEnum.SERVICE,
                OperationEnum.DELETE,
                serviceId,
                String.format("??????Service??????={id={%d}}", serviceId),
                operator
        );
    }

    /**
     * ????????????????????????????????? Service & Host ????????????
     * @param serviceDO ?????????????????????
     * @param operator ?????????
     * @throws ServiceException ???????????????????????????????????????
     */
    private void handleUpdateService(ServiceDO serviceDO, String operator) throws ServiceException {
        /*
         * ????????????
         */
        CheckResult checkResult = serviceManageServiceExtension.checkUpdateParameterService(serviceDO);
        if(!checkResult.getCheckResult()) {
            throw new ServiceException(
                    checkResult.getMessage(),
                    checkResult.getCode()
            );
        }
        /*
         * ???????????????????????????????????????????????????
         */
        if(null == getServiceById(serviceDO.getId())) {
            throw new ServiceException(
                    String.format("?????????Service??????={id=%d}?????????????????????", serviceDO.getId()),
                    ErrorCodeEnum.SERVICE_NOT_EXISTS.getCode()
            );
        }
        /*
         * ?????? servicePO & host ????????????
         */
        serviceHostManageService.deleteServiceHostByServiceId(serviceDO.getId());
        List<Long> hostIdList = serviceDO.getHostIdList();
        if(CollectionUtils.isNotEmpty(hostIdList)) {
            saveServiceHostRelation(serviceDO.getId(), hostIdList);
        }
        /*
         * ????????????????????????
         */
        operateRecordService.save(
                ModuleEnum.SERVICE,
                OperationEnum.EDIT,
                serviceDO.getId(),
                String.format("??????Service={%s}??????????????????Service??????id={%d}", JSON.toJSONString(serviceDO), serviceDO.getId()),
                operator
        );
    }

    /**
     * ???????????? serviceDO ????????????
     * @param serviceDO ????????? serviceDO ??????
     * @param operator ?????????
     * @return ?????????????????? serviceDO ???????????? id ???
     * @throws ServiceException ???????????????????????????????????????
     */
    private Long handleCreateService(ServiceDO serviceDO, String operator) throws ServiceException {
        /*
         * ????????????
         */
        CheckResult checkResult = serviceManageServiceExtension.checkCreateParameterService(serviceDO);
        if(!checkResult.getCheckResult()) {
            throw new ServiceException(
                    checkResult.getMessage(),
                    checkResult.getCode()
            );
        }
        /*
         * ???????????????Service????????????serviceName???????????????????????????
         */
        if(null != getServiceByServiceName(serviceDO.getServicename())) {
            throw new ServiceException(
                    String.format("?????????Service????????????serviceName={%s}?????????????????????", serviceDO.getServicename()),
                    ErrorCodeEnum.SERVICE_NAME_DUPLICATE.getCode()
            );
        }
        /*
         * ????????? service
         */
        ServicePO servicePO = serviceManageServiceExtension.serviceDO2Service(serviceDO);
        servicePO.setOperator(CommonConstant.getOperator(operator));
        serviceDAO.insert(servicePO);
        Long serviceId = servicePO.getId();
        /*
         * ?????? service & host ????????????
         */
        if(CollectionUtils.isNotEmpty(serviceDO.getHostIdList())) {
            saveServiceHostRelation(serviceId, serviceDO.getHostIdList());
        }
        /*
         * ?????? service & project ????????????
         */
        if(CollectionUtils.isNotEmpty(serviceDO.getProjectIdList())) {
            saveServiceProjectRelation(serviceId, serviceDO.getProjectIdList());
        }
        /*
         * ????????????????????????
         */
        operateRecordService.save(
                ModuleEnum.SERVICE,
                OperationEnum.ADD,
                serviceId,
                String.format("??????Service={%s}??????????????????Service??????id={%d}", JSON.toJSONString(serviceDO), serviceId),
                operator
        );
        return serviceId;
    }

    /**
     * ???????????? serviceId & projectIdList ????????????????????? & ??????????????????
     * @param serviceId ??????id
     * @param projectIdList ??????id???
     */
    private void saveServiceProjectRelation(Long serviceId, List<Long> projectIdList) {
        List<ServiceProjectPO> serviceProjectPOList = new ArrayList<>(projectIdList.size());
        for (Long projectId : projectIdList) {
            serviceProjectPOList.add(new ServiceProjectPO(serviceId, projectId));
        }
        serviceProjectManageService.createServiceProjectList( serviceProjectPOList );
    }

    /**
     * ???????????? serviceId & hostIdList ????????????????????? & ??????????????????
     * @param serviceId ??????id
     * @param hostIdList ??????id???
     */
    private void saveServiceHostRelation(Long serviceId, List<Long> hostIdList) {
        List<ServiceHostPO> serviceHostPOList = new ArrayList<>(hostIdList.size());
        for (Long hostId : hostIdList) {
            serviceHostPOList.add(new ServiceHostPO(serviceId, hostId));
        }
        serviceHostManageService.createServiceHostList( serviceHostPOList );
    }

}
