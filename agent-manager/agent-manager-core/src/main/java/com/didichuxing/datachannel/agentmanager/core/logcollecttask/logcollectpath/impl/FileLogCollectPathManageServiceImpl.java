package com.didichuxing.datachannel.agentmanager.core.logcollecttask.logcollectpath.impl;

import com.alibaba.fastjson.JSON;
import com.didichuxing.datachannel.agentmanager.common.bean.common.CheckResult;
import com.didichuxing.datachannel.agentmanager.common.bean.domain.logcollecttask.FileLogCollectPathDO;
import com.didichuxing.datachannel.agentmanager.common.bean.po.logcollecttask.FileLogCollectPathPO;
import com.didichuxing.datachannel.agentmanager.common.constant.CommonConstant;
import com.didichuxing.datachannel.agentmanager.common.enumeration.ErrorCodeEnum;
import com.didichuxing.datachannel.agentmanager.common.exception.ServiceException;
import com.didichuxing.datachannel.agentmanager.core.logcollecttask.logcollectpath.FileLogCollectPathManageService;
import com.didichuxing.datachannel.agentmanager.core.logcollecttask.manage.LogCollectTaskManageService;
import com.didichuxing.datachannel.agentmanager.persistence.mysql.FileLogCollectPathMapper;
import com.didichuxing.datachannel.agentmanager.thirdpart.logcollecttask.logcollectpath.extension.FileLogCollectPathManageServiceExtension;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@org.springframework.stereotype.Service
public class FileLogCollectPathManageServiceImpl implements FileLogCollectPathManageService {

    @Autowired
    private FileLogCollectPathManageServiceExtension fileLogCollectPathManageServiceExtension;

    @Autowired
    private LogCollectTaskManageService logCollectTaskManageService;

    @Autowired
    private FileLogCollectPathMapper fileLogCollectPathDAO;

    @Override
    @Transactional
    public Long createFileLogCollectPath(FileLogCollectPathDO fileLogCollectPath, String operator) {
        return this.createFileLogCollectPathProcess(fileLogCollectPath, operator);
    }

    @Override
    public List<FileLogCollectPathDO> getAllFileLogCollectPathByLogCollectTaskId(Long logCollectTaskId) {
        List<FileLogCollectPathPO> fileLogCollectPathPOPOList = fileLogCollectPathDAO.selectByLogCollectTaskId(logCollectTaskId);
        if(CollectionUtils.isEmpty(fileLogCollectPathPOPOList)) {
            return new ArrayList<>();
        }
        return fileLogCollectPathManageServiceExtension.fileLogCollectPathPOPOList2FileLogCollectPathDOList(fileLogCollectPathPOPOList);
    }

    @Override
    @Transactional
    public void deleteFileLogCollectPath(Long id, String operator) {
        fileLogCollectPathDAO.deleteByPrimaryKey(id);
    }

    @Override
    @Transactional
    public void updateFileLogCollectPath(FileLogCollectPathDO fileLogCollectPathDO, String operator) {
        FileLogCollectPathPO fileLogCollectPathPO = fileLogCollectPathManageServiceExtension.fileLogCollectPath2FileLogCollectPathPO(fileLogCollectPathDO);
        fileLogCollectPathPO.setOperator(CommonConstant.getOperator(operator));
        fileLogCollectPathDAO.updateByPrimaryKey(fileLogCollectPathPO);
    }

    @Override
    @Transactional
    public void deleteByLogCollectTaskId(Long logCollectTaskId) {
        fileLogCollectPathDAO.deleteByLogCollectTaskId(logCollectTaskId);
    }

    @Override
    public Long countAll() {
        return fileLogCollectPathDAO.countAll();
    }

    /**
     * ?????????????????????????????????????????????
     * @param fileLogCollectPath ??????????????????????????????????????????
     * @param operator ?????????
     * @return ??????????????????????????????????????????????????? id ???
     * @throws ServiceException ???????????????????????????????????????
     */
    private Long createFileLogCollectPathProcess(FileLogCollectPathDO fileLogCollectPath, String operator) throws ServiceException {
        CheckResult checkResult = this.fileLogCollectPathManageServiceExtension.checkCreateParameterFileLogCollectPath(fileLogCollectPath);
        if(!checkResult.getCheckResult()) {
            throw new ServiceException(
                    checkResult.getMessage(),
                    checkResult.getCode()
            );
        }
        if(null == logCollectTaskManageService.getById(fileLogCollectPath.getLogCollectTaskId())) {
            throw new ServiceException(
                    String.format("????????????????????????id???fileLogCollectPath??????={%s}??????logCollectTaskId????????????LogCollectTask??????", JSON.toJSONString(fileLogCollectPath)),
                    ErrorCodeEnum.ILLEGAL_PARAMS.getCode()
            );
        }
        FileLogCollectPathPO fileLogCollectPathPO = fileLogCollectPathManageServiceExtension.fileLogCollectPath2FileLogCollectPathPO(fileLogCollectPath);
        fileLogCollectPathPO.setOperator(CommonConstant.getOperator(operator));
        fileLogCollectPathDAO.insert(fileLogCollectPathPO);
        return fileLogCollectPathPO.getId();
    }

}
