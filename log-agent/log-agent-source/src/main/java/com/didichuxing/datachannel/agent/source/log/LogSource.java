package com.didichuxing.datachannel.agent.source.log;

import java.io.File;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import com.didichuxing.datachannel.agent.common.api.CollectLocation;
import com.didichuxing.datachannel.agent.common.api.CollectType;
import com.didichuxing.datachannel.agent.common.api.FileType;
import com.didichuxing.datachannel.agent.common.api.LogConfigConstants;
import com.didichuxing.datachannel.agent.common.loggather.LogGather;
import org.apache.commons.lang3.StringUtils;

import com.didichuxing.datachannel.agent.common.beans.LogPath;
import com.didichuxing.datachannel.agent.common.configs.v2.component.ComponentConfig;
import com.didichuxing.datachannel.agent.common.configs.v2.component.EventMetricsConfig;
import com.didichuxing.datachannel.agent.common.configs.v2.component.ModelConfig;
import com.didichuxing.datachannel.agent.engine.bean.Event;
import com.didichuxing.datachannel.agent.engine.bean.LogEvent;
import com.didichuxing.datachannel.agent.engine.source.AbstractSource;
import com.didichuxing.datachannel.agent.engine.utils.CommonUtils;
import com.didichuxing.datachannel.agent.engine.utils.TimeUtils;
import com.didichuxing.datachannel.agent.source.log.beans.FileNode;
import com.didichuxing.datachannel.agent.source.log.beans.WorkingFileNode;
import com.didichuxing.datachannel.agent.source.log.config.LogSourceConfig;
import com.didichuxing.datachannel.agent.source.log.metrics.FileMetricsFields;
import com.didichuxing.datachannel.agent.source.log.metrics.FileStatistic;
import com.didichuxing.datachannel.agent.source.log.monitor.RealTimeFileMonitor;
import com.didichuxing.datachannel.agent.source.log.monitor.ScheduleFileMonitor;
import com.didichuxing.datachannel.agent.source.log.offset.FileOffSet;
import com.didichuxing.datachannel.agent.source.log.offset.OffsetManager;
import com.didichuxing.datachannel.agent.source.log.utils.EventParser;
import com.didichuxing.datachannel.agent.source.log.utils.FileReader;
import com.didichuxing.datachannel.agent.source.log.utils.FileUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @description: log source
 * @author: huangjw
 * @Date: 19/7/2 14:21
 */
public class LogSource extends AbstractSource {

    private static final Logger      LOGGER           = LoggerFactory.getLogger(LogSource.class
                                                          .getName());
    private final Object             lock             = new Object();
    private int                      num;

    private LogPath                  logPath;

    Map<String, FileNode>            relatedFileNodeMap;
    List<WorkingFileNode>            collectingFileNodeList;
    Map<String, WorkingFileNode>     collectingFileNodeMap;

    private String                   parentPath;
    private String                   dockerParentPath;
    private String                   masterFileName;
    private String                   parentPathDirKey;

    private LogSourceConfig          logSourceConfig;
    private ModelConfig              modelConfig;

    private volatile boolean         isStopping       = false;

    private volatile WorkingFileNode curWFileNode;

    private static final int         OPEN_RETRY_TIMES = 3;

    FileReader                       fileReader;
    EventParser                      eventParser;

    /**
     * ???????????????????????????????????????
     */
    private volatile long            maxLogTime       = 0L;

    /**
     * ????????????????????????
     */
    private volatile boolean         isMatchStandard  = true;

    /**
     * 3??????
     */
    private static final Long        THREE_MINS       = 3 * 60 * 1000L;

    public LogSource(ModelConfig config, LogPath logPath){
        super(config.getSourceConfig());
        this.logPath = logPath;
        this.logSourceConfig = (LogSourceConfig) this.sourceConfig;
        this.modelConfig = config;
        String masterFile = this.logPath.getRealPath();
        this.masterFileName = FileUtils.getMasterFile(masterFile);
        this.parentPath = FileUtils.getPathDir(this.logPath.getPath());
        if (StringUtils.isNotBlank(parentPath)) {
            this.parentPathDirKey = FileUtils.getFileKeyByAttrs(this.parentPath);
        }
        this.dockerParentPath = FileUtils.getPathDir(this.logPath.getDockerPath());

        this.relatedFileNodeMap = new ConcurrentHashMap<>();
        this.collectingFileNodeMap = new ConcurrentHashMap<>();
        this.collectingFileNodeList = new ArrayList<>(2);
        bulidUniqueKey();
    }

    @Override
    public boolean init(ComponentConfig config) {
        LOGGER.info("begin to init logSource. config is " + this.sourceConfig);
        try {
            configure(config);

            prepare();

            // ?????????????????????
            synchronized (lock) {
                try {
                    List<FileNode> fileNodes = getFileNodes();
                    if (modelConfig.getCommonConfig().getModelType() == LogConfigConstants.COLLECT_TYPE_PERIODICITY) {
                        addToPeriodicityCollect(fileNodes);
                    } else if (modelConfig.getCommonConfig().getModelType() == LogConfigConstants.COLLECT_TYPE_TEMPORALITY) {
                        addToTemporalityCollect(fileNodes);
                    }
                } catch (Exception e) {
                    LogGather.recordErrorLog("logSource error", "init logSource error!", e);
                }
            }

            if (logSourceConfig.getMatchConfig().getFileType() == FileType.File.getStatus()) {
                // ??????offset
                Set<String> masterFiles = new HashSet<>();
                masterFiles.add(logPath.getRealPath());
                OffsetManager.sync(null, logPath.getLogModelId(), logPath.getPathId(), masterFiles);
            }
            return true;
        } catch (Exception e) {
            LogGather.recordErrorLog("logSource error", "init logSource error!", e);
            return false;
        }
    }

    /**
     * 1. ???????????????????????????????????????????????????????????????????????????????????????
     * 2. ?????????????????????offset??????
     */
    private void prepare() {

        // ??????????????????dockerPath???????????????case
        boolean needToCopyOffsetBetweenLogModeId = false;
        if (this.modelConfig.getSourceLogModeId() != 0) {
            needToCopyOffsetBetweenLogModeId = true;
        }

        if (needToCopyOffsetBetweenLogModeId) {
            OffsetManager.copyOffsetBetweenLogModeId(
                StringUtils.isNotBlank(modelConfig.getHostname()) ? modelConfig.getHostname()
                                                                    + CommonUtils
                                                                        .getHOSTNAMESUFFIX() : "",
                logPath, this.modelConfig.getSourceLogModeId());
        }
    }

    private Map<String, FileNode> getFileNodesToCopy() {
        Long modelId = logPath.getLogModelId();
        Long logPathId = logPath.getPathId();
        List<File> relatedFiles = FileUtils.getRelatedFiles(logPath.getRealPath(),
                                                            this.logSourceConfig.getMatchConfig());
        Map<String, FileNode> fileNodeMap = new HashMap<>();
        if (relatedFiles == null || relatedFiles.size() == 0) {
            LOGGER.warn("there is no any file which need to be collected.");
            return null;
        }

        for (File file : relatedFiles) {
            // ??????????????????
            if (System.currentTimeMillis() - file.lastModified() > logSourceConfig.getMaxModifyTime()) {
                continue;
            }
            String fileKey = FileUtils.getFileKeyByAttrs(file);
            fileNodeMap.put(fileKey, new FileNode(modelId, logPathId, fileKey, file.lastModified(), file.getParent(),
                                                  file.getName(), file.length(), file));
        }
        return fileNodeMap;
    }

    @Override
    public void configure(ComponentConfig config) {
        fileReader = new FileReader(this);
        eventParser = new EventParser();
    }

    public void addToPeriodicityCollect(List<FileNode> fileNodes) {
        if (fileNodes != null && fileNodes.size() != 0) {
            for (FileNode fileNode : fileNodes) {
                relatedFileNodeMap.put(fileNode.getNodeKey(), fileNode);
            }

            for (FileNode fileNode : fileNodes) {
                if (fileNode.getNeedCollect()) {
                    if (checkStandardLogType(fileNode)) {
                        appendFile(fileNode, false);
                    }
                }
            }

            // ??????????????????????????????????????????????????????????????????????????????????????????????????????
            if (collectingFileNodeList.size() == 0) {
                LOGGER.warn("there is no any vaild file to collect. filtered. logPath is "
                            + logPath);
                isMatchStandard = false;
                ScheduleFileMonitor.INSTANCE.unregister(this);
            }
        }
    }

    public void addToTemporalityCollect(List<FileNode> fileNodes) {
        if (modelConfig.getCommonConfig().getStartTime() != null
            && modelConfig.getCommonConfig().getEndTime() != null) {
            if (fileNodes != null && fileNodes.size() != 0) {

                // ???nodes??????modifyTime??????, ????????????
                FileUtils.sortByMTime(fileNodes);

                // ????????????????????????lastModifyTime>startTime ?????? ?????????lastModifyTime > endTime
                for (FileNode fileNode : fileNodes) {
                    if (fileNode.getModifyTime() < modelConfig.getCommonConfig().getStartTime()
                        .getTime()) {
                        continue;
                    }

                    fileNode.setOffset(0L);
                    if (checkStandardLogType(fileNode)) {
                        appendFile(fileNode);
                    }

                    if (fileNode.getModifyTime() > modelConfig.getCommonConfig().getEndTime()
                        .getTime()) {
                        break;
                    }
                }
            }
        } else {
            LogGather.recordErrorLog("LogSource error",
                "params is not right.type is " + LogConfigConstants.COLLECT_TYPE_TEMPORALITY
                        + ",startTime is " + modelConfig.getCommonConfig().getStartTime()
                        + ", endTime is " + modelConfig.getCommonConfig().getEndTime());
        }
    }

    /**
     * ????????????match?????????type
     *
     * @param fileNode need to check
     * @return result if standard files
     */
    public boolean checkStandardLogType(FileNode fileNode) {
        if (!FileUtils.checkStandard(new File(fileNode.getAbsolutePath()),
            logSourceConfig.getMatchConfig())) {
            LOGGER.warn("fileNode is not match the standard.ignore!" + " file is "
                        + fileNode.getAbsolutePath() + ",required StandardLogType is "
                        + logSourceConfig.getMatchConfig());
            return false;
        }
        return true;
    }

    /**
     * ?????????????????????????????????curFileNode
     * @param fileNode
     */
    public void appendFile(FileNode fileNode) {
        if (appendFile(fileNode, false)) {
            refreshCurWFN();
            LOGGER.info("success to refresh current working fileNode. fileNode is "
                        + (curWFileNode == null ? null : curWFileNode.getFileNode()));
        }
    }

    public boolean appendFile(FileNode fileNode, boolean toSort) {
        LOGGER.info("appendFile fileNode to collect. fileNode is " + fileNode);
        if (isStopping) {
            LOGGER.warn("logSource is stopping. ignore this fileNode!");
            return false;
        }

        if (!isMatchStandard) {
            RealTimeFileMonitor.INSTANCE.register(this);
            isMatchStandard = true;
        }

        // ????????????????????????
        if (!checkToAdd(fileNode)) {
            return false;
        }

        synchronized (lock) {
            if (fileNode.getFileOffSet() == null) {
                FileOffSet fileOffSet = null;
                fileOffSet = OffsetManager.getFileOffset(logPath.getLogModelId(),
                    logPath.getPathId(), logPath.getRealPath(), fileNode.getParentPath(),
                    fileNode.getFileKey(), fileNode.getFile());
                fileOffSet.setFileName(fileNode.getFileName());
                fileOffSet.setLastModifyTime(fileNode.getModifyTime());
                fileNode.setFileOffSet(fileOffSet);
            }

            // ???????????????????????????????????????offset????????????MD5?????????,?????????Inode????????????????????????????????????????????????????????????
            if (StringUtils.isBlank(fileNode.getFileOffSet().getFileHeadMd5())) {
                fileNode.getFileOffSet().setFileHeadMd5(
                    FileUtils.getFileNodeHeadMd5(fileNode.getFile()));
            }

            if (fileNode.getOffset() > fileNode.getLength()) {
                LOGGER.info("appendFile:the file length is less than offset???offset:"
                            + fileNode.getOffset() + ",fileNode is " + fileNode.toString());
                fileNode.setOffset(0L);
                fileNode.setOffsetTimeStamp(0L);
            }

            if (!collectingFileNodeMap.containsKey(fileNode.getNodeKey())) {
                relatedFileNodeMap.put(fileNode.getNodeKey(), fileNode);

                WorkingFileNode wfn = new WorkingFileNode(fileNode, this);
                collectingFileNodeMap.put(wfn.getUniqueKey(), wfn);
                collectingFileNodeList.add(wfn);
                if (toSort) {
                    FileUtils.sortWFNByMTime(collectingFileNodeList);
                }

                // ?????????????????? ??? ?????????
                if (!wfn.isFileOpen()
                    && modelConfig.getCommonConfig().getModelType() != LogConfigConstants.COLLECT_TYPE_TEMPORALITY) {
                    wfn.open(logSourceConfig.getCollectLocation(), OPEN_RETRY_TIMES);
                }

                LOGGER.info("add fileNode success. collectingFileNodeMap size is "
                            + collectingFileNodeMap.size() + ", relatedFileNodeMap size is "
                            + relatedFileNodeMap.size());
                return true;
            } else {
                // MD5????????????????????????,????????????map??????????????????CurWFN
                String curFileNodeMd5 = FileUtils.getFileNodeHeadMd5(fileNode.getFile());
                String oldFileNodeMd5 = collectingFileNodeMap.get(fileNode.getNodeKey())
                    .getFileNode().getFileOffSet().getFileHeadMd5();
                // ???MD5???????????????????????????????????????????????????MD5??????????????????fileOffset??????MD5????????????
                if (fileNode.getFile().exists() && StringUtils.isNotBlank(oldFileNodeMd5)
                    && !curFileNodeMd5.equals(oldFileNodeMd5)) {
                    LOGGER
                        .warn("appendFile:Inode reuse, need to set offset to 0 and flush map! logPath is "
                              + this.logPath + ",fileName is " + fileNode.getFileName());
                    // ??????????????????
                    collectingFileNodeMap.get(fileNode.getNodeKey()).close();

                    // ??????relatedFileNodeMap
                    relatedFileNodeMap.remove(fileNode.getNodeKey());
                    relatedFileNodeMap.put(fileNode.getNodeKey(), fileNode);

                    // ??????collectingFileNodeMap
                    collectingFileNodeMap.remove(fileNode.getNodeKey());
                    collectingFileNodeMap.put(fileNode.getNodeKey(), new WorkingFileNode(fileNode,
                        this));

                    // ??????collectingFileNodeList
                    Iterator<WorkingFileNode> iterator = collectingFileNodeList.iterator();
                    while (iterator.hasNext()) {
                        WorkingFileNode wfn = iterator.next();
                        if (wfn.getUniqueKey().equals(fileNode.getNodeKey())) {
                            collectingFileNodeList.remove(wfn);
                            collectingFileNodeList.add(new WorkingFileNode(fileNode, this));
                        }
                    }

                    // ???????????????????????????????????????0?????????????????????????????????
                    WorkingFileNode newWfn = collectingFileNodeMap.get(fileNode.getNodeKey());
                    if (!newWfn.isFileOpen()
                        && modelConfig.getCommonConfig().getModelType() != LogConfigConstants.COLLECT_TYPE_TEMPORALITY) {
                        newWfn.open(0, OPEN_RETRY_TIMES);
                    }
                    return true;
                }
                LOGGER.warn("file is already in collecting. ignore! logPath is " + this.logPath
                            + ",fileName is " + fileNode.getFileName());
                return false;
            }
        }
    }

    /**
     * ??????????????????
     * @param fileNode
     */
    private boolean checkToAdd(FileNode fileNode) {
        if (getModelConfig().getCommonConfig().getModelType() != LogConfigConstants.COLLECT_TYPE_TEMPORALITY) {
            if (collectingFileNodeMap.size() >= this.logSourceConfig.getMaxThreadNum()) {
                WorkingFileNode needToDelete = null;
                long earliestModifyTime = 0;
                int i = 0;
                for (WorkingFileNode wfn : collectingFileNodeMap.values()) {
                    if (i == 0) {
                        earliestModifyTime = wfn.getFileNode().getModifyTime();
                        needToDelete = wfn;
                    } else {
                        if (wfn.getFileNode().getModifyTime() < earliestModifyTime) {
                            earliestModifyTime = wfn.getFileNode().getModifyTime();
                            needToDelete = wfn;
                        }
                    }
                    i++;
                }
                if (fileNode.getModifyTime() < earliestModifyTime) {
                    LOGGER.info("file is not in lastest " + this.logSourceConfig.getMaxThreadNum()
                                + " files. ignore. file is " + fileNode);
                    return false;
                }

                if (needToDelete != null) {
                    needToDelete.close();
                    release(needToDelete);
                    LogGather.recordErrorLog(
                        "LogSource error",
                        "collectingFileNodeMap'size is too large ["
                                + (collectingFileNodeMap.size() + 1)
                                + "] which means collection delay. logId is "
                                + logPath.getLogModelId() + ", pathId is " + logPath.getPathId());
                }
            }
        }
        return true;
    }

    /**
     * ???????????????????????????
     *
     * @return vaild files to collect
     */
    public List<FileNode> getFileNodes() {
        LOGGER.info("begin to get fileNodes. logPath is " + logPath);
        Long modelId = logPath.getLogModelId();
        Long logPathId = logPath.getPathId();

        List<File> relatedFiles = FileUtils.getRelatedFiles(logPath.getRealPath(),
                                                            this.logSourceConfig.getMatchConfig());

        List<FileNode> fileNodeList = new ArrayList<>();
        if (relatedFiles == null || relatedFiles.size() == 0) {
            LOGGER.warn("there is no any file which need to be collected.");
            return null;
        }

        for (File file : relatedFiles) {
            // ??????????????????
            if (System.currentTimeMillis() - file.lastModified() > logSourceConfig.getMaxModifyTime()) {
                continue;
            }
            fileNodeList.add(new FileNode(modelId, logPathId, FileUtils.getFileKeyByAttrs(file), file.lastModified(),
                                          file.getParent(), file.getName(), file.length(), file));
        }

        // ???????????????????????????????????????????????????????????????????????????
        if (fileNodeList.size() == 0) {
            LOGGER.warn("there is no any fileNode which need to be collected.");
            return fileNodeList;
        }

        // ??????offsetMap???????????????offset??????????????????????????????hostname,offset?????????????????????????????????
        boolean isNewTask = !OffsetManager.checkOffsetInfoExit(modelId, logPathId, logPath.getRealPath());

        // ????????????????????????????????????
        OffsetManager.getOffsetInfo(modelId, logPathId, logPath.getRealPath(), fileNodeList);

        // ????????????????????????????????????
        FileUtils.sortByMTimeDesc(fileNodeList);

        if (modelConfig.getCommonConfig().getModelType() == LogConfigConstants.COLLECT_TYPE_TEMPORALITY) {
            // ????????????
            for (FileNode fileNode : fileNodeList) {
                fileNode.setOffset(0L);
                fileNode.getFileOffSet().setLastModifyTime(0L);
                fileNode.setNeedCollect(true);
            }
        } else {
            // ????????????
            boolean haveNeedCollectFile = false;
            int count = 0;
            for (FileNode fileNode : fileNodeList) {
                if (!fileNode.getNeedCollect()) {
                    continue;
                }

                haveNeedCollectFile = true;
                // count++;
                // if (count > logSourceConfig.getValidLatestFiles()) {
                // // ????????????????????????????????????????????????????????????
                // fileNode.getFileOffSet().setLastModifyTime(0L);
                // fileNode.setNeedCollect(false);
                // }
            }

            // ????????????offset???????????????????????????????????????
            if (!haveNeedCollectFile) {
                LOGGER.info("this is a new task. collect latest file! logPath is " + logPath);
                FileNode node = fileNodeList.get(0);
                node.setNeedCollect(true);
                // ?????????????????????????????????????????????????????????
                if (isNewTask) {
                    if (modelConfig.getCollectType() == CollectType.COLLECT_IN_NORMAL_SERVER.getStatus()) {
                        if (logSourceConfig.getCollectLocation() == CollectLocation.Earliest.getLocation()) {
                            node.getFileOffSet().setOffSet(0L);
                        } else {
                            // ??????????????????1G,
                            // ?????????????????????????????????30????????????
                            // ???????????????0???????????????????????????;
                            File file = new File(node.getAbsolutePath());
                            if (file.length() < 1024 * 1024 * 1024
                                    && System.currentTimeMillis() - file.lastModified() < 3 * 60 * 60 * 1000L) {
                                LOGGER.info("file is less than 1G. set fileNode's offset to 0. fileNode is " + node);
                                node.getFileOffSet().setOffSet(0L);
                            } else {
                                node.getFileOffSet().setOffSet(file.length());
                            }
                        }
                    } else {
                        // ??????????????????????????????????????????????????????????????????????????????????????????????????????
                        // ?????????????????????????????????????????????????????????????????????????????????????????????offset???0?????????????????????
                        node.setOffset(0L);
                        if (fileNodeList.size() >= 2) {
                            LOGGER.info("This is a new task that is collected on the ddcloud and requires one more file to be collected forward???fileNode is"
                                    + node);
                            FileNode preNode = fileNodeList.get(1);
                            preNode.setNeedCollect(true);
                            preNode.getFileOffSet().setOffSet(0L);
                        }
                    }
                }
            }
        }

        return fileNodeList;
    }

    /**
     * ????????????
     *
     * @param fns files which need to be checked
     */
    public void checkFile(Map<String/* smallKey */, FileNode> fns) {
        Set<String/* bigKey */> fnsDeletes = new HashSet<>();
        List<FileNode> needToAddFns = new ArrayList<>();
        String pathDir = FileUtils.getPathDir(logPath.getRealPath());
        Long modelId = logPath.getLogModelId();
        Long logPathId = logPath.getPathId();
        Set<String> vaildFileNodeSet = new HashSet<>();
        boolean collectingFileChanged = false;
        for (FileNode fileNode : fns.values()) {
            // ?????????????????????
            if (System.currentTimeMillis() - fileNode.getModifyTime() > logSourceConfig.getMaxModifyTime()) {
                continue;
            }

            boolean macthResult = FileUtils.match(new File(fileNode.getAbsolutePath()), logPath.getRealPath(),
                                                  logSourceConfig.getMatchConfig());
            if (!macthResult) {
                continue;
            }

            // ?????????????????????MD5??????????????????????????????????????????????????????????????????????????????????????????1K???
            if (FileUtils.getFileNodeHeadMd5(fileNode.getFile()).equals(LogConfigConstants.MD5_FAILED_TAG)) {
                continue;
            }

            fileNode.setModelId(modelId);
            fileNode.setPathId(logPathId);

            vaildFileNodeSet.add(fileNode.getNodeKey());

            // ???????????? ?????? ????????????????????????
            if (relatedFileNodeMap.get(fileNode.getNodeKey()) == null
                || ((relatedFileNodeMap.get(fileNode.getNodeKey()) != null
                     && collectingFileNodeMap.get(fileNode.getNodeKey()) == null
                     && (relatedFileNodeMap.get(fileNode.getNodeKey()).getModifyTime() < fileNode.getModifyTime()
                         || relatedFileNodeMap.get(fileNode.getNodeKey()).getOffset() < fileNode.getLength())))) {
                FileNode newFileNode = fileNode.clone();
                newFileNode.setModelId(modelId);
                newFileNode.setPathId(logPathId);

                FileOffSet fileOffSet;
                fileOffSet = OffsetManager.getFileOffset(logPath.getLogModelId(), logPath.getPathId(),
                                                             logPath.getRealPath(), fileNode.getParentPath(),
                                                             fileNode.getFileKey(), fileNode.getFile());

                fileOffSet.setFileName(fileNode.getFileName());
                fileOffSet.setLastModifyTime(fileNode.getModifyTime());
                newFileNode.setFileOffSet(fileOffSet);

                needToAddFns.add(newFileNode);
                continue;
            }

            String fileName = fileNode.getFileName();
            Long modifyTime = fileNode.getModifyTime();

            // collectingFileNodeMap??????????????????????????????????????????????????????
            WorkingFileNode wfn = collectingFileNodeMap.get(fileNode.getNodeKey());
            if (curWFileNode != null && wfn != null && curWFileNode.getUniqueKey().equals(wfn.getUniqueKey())) {
                curWFileNode.getFileNode().setFileName(fileName);
                curWFileNode.getFileNode().setModifyTime(modifyTime);
            }

            // collectingFileNodeMap??????????????????????????????????????????????????????
            if (wfn != null && wfn.isFileEnd() && wfn.getFileNode().getOffset() < fileNode.getLength()) {
                wfn.setFileEnd(false);
                if (curWFileNode != null && !curWFileNode.getUniqueKey().equals(wfn.getUniqueKey())) {
                    // ?????????????????????????????????curWFileNode
                    collectingFileChanged = true;
                }
            }

            // ????????????????????????????????????case
            if (wfn != null && wfn.getFileNode().getOffset() < fileNode.getLength()) {
                wfn.setFileEnd(false);
                if (curWFileNode != null && !curWFileNode.getUniqueKey().equals(wfn.getUniqueKey())
                    && modifyTime - THREE_MINS < curWFileNode.getModifyTime()) {
                    // ?????????????????????????????????curWFileNode
                    collectingFileChanged = true;
                }
            }

            if (relatedFileNodeMap.containsKey(fileNode.getNodeKey())) {
                // ??????????????????????????????
                relatedFileNodeMap.get(fileNode.getNodeKey()).setFileName(fileName);
                relatedFileNodeMap.get(fileNode.getNodeKey()).setModifyTime(modifyTime);
            }

            if (collectingFileNodeMap.containsKey(fileNode.getNodeKey())) {
                // ??????????????????????????????
                collectingFileNodeMap.get(fileNode.getNodeKey()).getFileNode().setFileName(fileName);
                collectingFileNodeMap.get(fileNode.getNodeKey()).getFileNode().setModifyTime(modifyTime);
            }
        }

        for (String key : relatedFileNodeMap.keySet()) {
            if (!vaildFileNodeSet.contains(key)) {
                // ??????????????????????????????????????????unCollectFiles??????filenode
                fnsDeletes.add(key);
            }
        }

        if (fnsDeletes.size() != 0) {
            for (String key : fnsDeletes) {
                relatedFileNodeMap.remove(key);
                if (collectingFileNodeMap.get(key) != null) {
                    release(collectingFileNodeMap.get(key));
                }

                // ????????????offset
                // key???nodekey: logModId + UNDERLINE_SEPARATOR + pathId+
                // UNDERLINE_SEPARATOR + fileKey
                String fileKey = FileUtils.getFileKeyFromNodeKey(key);
                OffsetManager.removeFileOffset(modelId, logPathId, logPath.getRealPath(), fileKey);
            }
        }

        if (needToAddFns.size() != 0) {
            // ????????????????????????????????????
            String dir = FileUtils.getPathDir(logPath.getRealPath());
            String newKey = FileUtils.getFileKeyByAttrs(dir);
            if (parentPathDirKey != null && !this.parentPathDirKey.equals(newKey)) {
                RealTimeFileMonitor.INSTANCE.replaceWatchKey(dir);
                parentPathDirKey = newKey;
            }
        }

        for (FileNode fileNode : needToAddFns) {
            if (checkStandardLogType(fileNode)) {
                appendFile(fileNode);
            }
        }

        if (collectingFileChanged) {
            refreshCurWFN();
            LOGGER.info("success to refresh current working fileNode. fileNode is "
                        + (curWFileNode == null ? null : curWFileNode.getFileNode()));
        }
    }

    @Override
    public void bulidUniqueKey() {
        setUniqueKey(logPath.getPathId() + "_" + logPath.getPath());
    }

    @Override
    public boolean onChange(ComponentConfig newOne) {
        LOGGER.info("begin to change logSouce config. logPath is " + this.logPath + ", newOne is "
                    + newOne);
        try {
            this.modelConfig = (ModelConfig) newOne;
            this.sourceConfig = this.modelConfig.getSourceConfig();
            this.logSourceConfig = (LogSourceConfig) this.sourceConfig;
            return true;
        } catch (Exception e) {
            LogGather.recordErrorLog("logSource error", "onChange error! new config is " + newOne,
                e);
        }
        return false;
    }

    @Override
    public boolean start() {
        isStopping = false;
        return true;
    }

    @Override
    public Event tryGetEvent() {
        if (collectingFileNodeList.size() == 0) {
            return null;
        }

        if (curWFileNode == null) {
            refreshCurWFN();
            if (curWFileNode == null) {
                return null;
            }
        }

        LogEvent event = null;
        try {
            synchronized (lock) {
                // release?????????????????????null
                if (curWFileNode == null) {
                    return null;
                }
                if (!curWFileNode.isFileOpen()) {
                    curWFileNode.open(logSourceConfig.getCollectLocation(), OPEN_RETRY_TIMES);
                    if (!curWFileNode.isFileOpen()) {
                        curWFileNode = null;
                        return null;
                    }
                }

                // ??????
                event = fileReader.readEvent(curWFileNode);
                if (event != null) {
                    curWFileNode.setCurOffset(event.getOffset());
                }
                // ??????
                eventParser.parse(this, curWFileNode, event);
                curWFileNode.whetherToFileEnd(event);
                if (curWFileNode.isFileEnd()) {
                    // ??????????????????
                    curWFileNode = null;
                }
            }
        } catch (Exception e) {
            LogGather.recordErrorLog("LogSource error",
                "check file is collected to end error. file is " + curWFileNode.getFileNode(), e);
        }

        return event;
    }

    public void closeFiles() {
        List<WorkingFileNode> needToRelease = new ArrayList<>();
        if (getModelConfig().getCommonConfig().getModelType() == LogConfigConstants.COLLECT_TYPE_TEMPORALITY) {
            for (WorkingFileNode wfn : collectingFileNodeList) {
                if (wfn.isFileEnd()) {
                    wfn.syncFile();
                }

                // ?????????wfn?????????????????????
                if (!wfn.isFileOpen()) {
                    continue;
                }

                if (wfn.checkFileCollectEnd()) {
                    // ??????????????????????????????????????????????????????????????????
                    // ???????????????????????????????????????
                    needToRelease.add(wfn);
                }
            }
        } else {
            if (collectingFileNodeMap.size() <= logSourceConfig.getValidLatestFiles()) {
                return;
            }

            // ????????????
            FileUtils.sortWFNByMTime(collectingFileNodeList);
            int i = 0;
            for (WorkingFileNode wfn : collectingFileNodeList) {
                if (wfn.isFileEnd()) {
                    wfn.syncFile();
                }
                if (i < collectingFileNodeList.size() - logSourceConfig.getValidLatestFiles()) {
                    if (wfn.isFileEnd() && wfn.checkFileCollectEnd()) {
                        // ??????????????????????????????????????????????????????????????????
                        // ???????????????????????????????????????
                        needToRelease.add(wfn);
                    }
                }
                i++;
            }
        }

        for (WorkingFileNode wfn : needToRelease) {
            release(wfn);
        }
    }

    private void release(WorkingFileNode wfn) {
        LOGGER.info("begin to release the working fileNode. fileNode is " + wfn);
        synchronized (lock) {
            wfn.close();
            String uniqueKey = wfn.getUniqueKey();
            collectingFileNodeMap.remove(uniqueKey);
            List<WorkingFileNode> newOne = new ArrayList<>();
            for (WorkingFileNode w : collectingFileNodeList) {
                if (!w.getUniqueKey().equals(wfn.getUniqueKey())) {
                    newOne.add(w);
                }
            }
            collectingFileNodeList.clear();
            collectingFileNodeList = newOne;
            // ??????refresh
            if (curWFileNode != null && curWFileNode.getUniqueKey().equals(wfn.getUniqueKey())) {
                curWFileNode = null;
            }
        }
        LOGGER.info("release fileNode success");
    }

    /**
     * ??????????????????woke fileNode
     */
    private void refreshCurWFN() {
        synchronized (lock) {
            if (!collectingFileNodeList.isEmpty()) {
                FileUtils.sortWFNByMTime(this.collectingFileNodeList);
                WorkingFileNode result = null;
                List<WorkingFileNode> readyToCollectList = new ArrayList<>();
                for (WorkingFileNode wfn : this.collectingFileNodeList) {
                    if (!wfn.isFileEnd()) {
                        readyToCollectList.add(wfn);
                    }
                }

                try {
                    if (!readyToCollectList.isEmpty()) {
                        if (modelConfig.getCommonConfig().getModelType() == LogConfigConstants.COLLECT_TYPE_PERIODICITY) {
                            long lag = 0L;
                            long modifyTime = 0L;
                            for (int i = 0; i < readyToCollectList.size(); i++) {
                                if (i + 1 > logSourceConfig.getValidLatestFiles()) {
                                    break;
                                }
                                WorkingFileNode wfn = readyToCollectList.get(i);
                                if (i == 0) {
                                    modifyTime = wfn.getModifyTime();
                                    result = wfn;
                                    lag = wfn.getFileNode().getFile().length() - wfn.getFileNode().getOffset();
                                } else {
                                    // ?????????????????????????????????????????????case
                                    if (modifyTime != 0L && wfn.getModifyTime() - modifyTime < 5 * 1000L) {
                                        long minute = TimeUtils.getSpecificMinute(wfn.getModifyTime());
                                        // ???i????????????0?????????????????????????????????????????????0?????????59???????????????????????????????????????
                                        if (minute > 1 && minute < 59) {
                                            modifyTime = wfn.getModifyTime();
                                            if (lag < wfn.getFileNode().getFile().length()
                                                      - wfn.getFileNode().getOffset()) {
                                                result = wfn;
                                            }
                                        }
                                    }
                                }
                            }
                        } else {
                            result = readyToCollectList.get(0);
                        }
                    }
                } catch (Exception e) {
                    LOGGER.error("refreshCurWFN error. uniqueKey is " + uniqueKey, e);
                }

                if (result == null) {
                    // ????????????????????????,???????????????????????????
                    result = collectingFileNodeList.get(collectingFileNodeList.size() - 1);
                }
                curWFileNode = result;
            }
        }
    }

    @Override
    public boolean stop(boolean force) {
        LOGGER.info("begin to stop logSource. logPath is " + logPath);
        isStopping = true;
        close();
        collectingFileNodeList.clear();
        collectingFileNodeMap.clear();
        relatedFileNodeMap.clear();
        return true;
    }

    @Override
    public boolean delete() {
        if (!isStopping) {
            stop(true);
        }
        OffsetManager.removeLogModel4Path(logPath.getLogModelId(), logPath.getPathId(),
            logPath.getRealPath(), null);
        return false;
    }

    @Override
    public boolean specialDelete(Object object) {
        if (!isStopping) {
            stop(true);
        }

        if (logPath != null) {
            OffsetManager.removeLogModel4Path(logPath.getLogModelId(), logPath.getPathId(),
                logPath.getRealPath(), null);
        }
        return true;
    }

    private void close() {
        for (WorkingFileNode wfd : collectingFileNodeList) {
            wfd.close();
        }
    }

    public void syncOffset() {
        LOGGER.info("sync offset. logPath is " + logPath);
        if (collectingFileNodeList != null) {
            for (WorkingFileNode wfn : collectingFileNodeList) {
                wfn.seek(wfn.getFileNode().getOffset());
            }
        }
    }

    @Override
    public Map<String, Object> metric() {
        Map<String, Object> ret = new HashMap<>();

        ret.put(FileMetricsFields.PREFIX_TYPE, "log");

        ret.put(FileMetricsFields.PATH_STR, logPath.getPath());
        ret.put(FileMetricsFields.PATH_ID_STR, logPath.getPathId());
        ret.put(FileMetricsFields.IS_FILE_EXIST, collectingFileNodeMap.size() != 0);
        ret.put(FileMetricsFields.MASTER_FILE, masterFileName);

        List<FileStatistic> collectFiles = new ArrayList<>();
        String latestFileName = "null";
        Long latestLogTime = 0L;
        String logTimeStr = "";

        // ??????????????????
        for (Map.Entry<String, WorkingFileNode> entry : collectingFileNodeMap.entrySet()) {
            WorkingFileNode wfn = entry.getValue();
            FileNode fileNode = wfn.getFileNode();
            long fileLength = 0L;
            try {
                if (wfn.getIn() != null) {
                    fileLength = wfn.getIn().length();
                } else {
                    fileLength = wfn.getFileNode().getFile().length();
                }
            } catch (Exception e) {
                LOGGER.error("get source metrics error. wfn is " + wfn, e);
            }
            FileStatistic fileStatistic = new FileStatistic(fileNode.getFileName(),
                                                            wfn.isFileEnd() && wfn.checkFileCollectEnd(),
                                                            fileNode.getModifyTime(), wfn.getIsFileOrder(),
                                                            wfn.getIsVaildTimeConfig(), wfn.getLatestLogTime(),
                                                            fileLength == 0L ? 0L : fileNode.getOffset() * 100
                                                                                    / fileLength);

            collectFiles.add(fileStatistic);
            if (wfn.getLatestLogTime() != null && latestLogTime < wfn.getLatestLogTime()) {
                latestLogTime = wfn.getLatestLogTime();
                logTimeStr = wfn.getLatestLogTimeStr();
            }
        }

        // ???????????????
        File latestFile = FileUtils.getLatestRelatedFile(FileUtils.getPathDir(logPath.getRealPath()),
                                                         FileUtils.getMasterFile(logPath.getRealPath()),
                                                         logSourceConfig.getMatchConfig());
        if (latestFile != null) {
            latestFileName = latestFile.getName();
            ret.put(FileMetricsFields.LATEST_MODIFY_TIME, latestFile.lastModified());
        }

        // metrics??????
        EventMetricsConfig eventMetricsConfig = modelConfig.getEventMetricsConfig();
        if (eventMetricsConfig != null && eventMetricsConfig.getOtherMetrics() != null
            && eventMetricsConfig.getOtherMetrics().size() != 0) {
            for (Map.Entry<String, String> entry : eventMetricsConfig.getOtherMetrics().entrySet()) {
                if (org.apache.commons.lang.StringUtils.isNotBlank(entry.getValue())
                    && org.apache.commons.lang.StringUtils.isNotBlank(entry.getKey())) {
                    ret.put(entry.getKey(), entry.getValue());
                }
            }
        }

        ret.put(FileMetricsFields.COLLECT_FILE_NAMES_STR, collectFiles);
        ret.put(FileMetricsFields.LATEST_FILE_NAME_STR, latestFileName);
        ret.put(FileMetricsFields.MAX_TIME_GAP_STR, maxLogTime);
        ret.put(FileMetricsFields.LATEST_LOG_TIME_STR, logTimeStr);
        ret.put(FileMetricsFields.LATEST_LOG_TIME, latestLogTime);
        ret.put(FileMetricsFields.RELATED_FILES, relatedFileNodeMap.size());
        ret.put(FileMetricsFields.LOG_PATH_KEY, logPath.getLogPathKey());

        resetMaxLogTime();

        return ret;
    }

    public LogPath getLogPath() {
        return logPath;
    }

    public void setLogPath(LogPath logPath) {
        this.logPath = logPath;
    }

    public int getNum() {
        return num;
    }

    public void setNum(int num) {
        this.num = num;
    }

    public LogSourceConfig getLogSourceConfig() {
        return logSourceConfig;
    }

    public void setLogSourceConfig(LogSourceConfig logSourceConfig) {
        this.logSourceConfig = logSourceConfig;
    }

    public ModelConfig getModelConfig() {
        return modelConfig;
    }

    public void setModelConfig(ModelConfig modelConfig) {
        this.modelConfig = modelConfig;
    }

    public WorkingFileNode getCurWFileNode() {
        return curWFileNode;
    }

    public void setCurWFileNode(WorkingFileNode curWFileNode) {
        this.curWFileNode = curWFileNode;
    }

    public Map<String, WorkingFileNode> getCollectingFileNodeMap() {
        return collectingFileNodeMap;
    }

    public void setCollectingFileNodeMap(Map<String, WorkingFileNode> collectingFileNodeMap) {
        this.collectingFileNodeMap = collectingFileNodeMap;
    }

    public String getMasterFileName() {
        return masterFileName;
    }

    public void setMasterFileName(String masterFileName) {
        this.masterFileName = masterFileName;
    }

    public String getParentPath() {
        return parentPath;
    }

    public void setParentPath(String parentPath) {
        this.parentPath = parentPath;
    }

    public String getDockerParentPath() {
        return dockerParentPath;
    }

    public void setDockerParentPath(String dockerParentPath) {
        this.dockerParentPath = dockerParentPath;
    }

    public Long getLogTime() {
        return eventParser.getLastTimestamp();
    }

    public void resetMaxLogTime() {
        this.maxLogTime = 0;
    }

    public Map<String, FileNode> getRelatedFileNodeMap() {
        return relatedFileNodeMap;
    }

    public void setRelatedFileNodeMap(Map<String, FileNode> relatedFileNodeMap) {
        this.relatedFileNodeMap = relatedFileNodeMap;
    }

    public List<WorkingFileNode> getCollectingFileNodeList() {
        return collectingFileNodeList;
    }

    public void setCollectingFileNodeList(List<WorkingFileNode> collectingFileNodeList) {
        this.collectingFileNodeList = collectingFileNodeList;
    }

    public void setMaxGapLogTime(Long logTime) {
        if (logTime == null) {
            return;
        }
        Long gap = System.currentTimeMillis() - logTime;
        if (gap > maxLogTime) {
            maxLogTime = gap;
        }
    }

}
