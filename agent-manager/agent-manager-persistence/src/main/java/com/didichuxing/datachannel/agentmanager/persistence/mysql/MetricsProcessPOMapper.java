package com.didichuxing.datachannel.agentmanager.persistence.mysql;

import com.didichuxing.datachannel.agentmanager.common.bean.po.metrics.MetricsLogCollectTaskTopPO;
import com.didichuxing.datachannel.agentmanager.common.bean.po.metrics.MetricsProcessPO;
import com.didichuxing.datachannel.agentmanager.common.bean.vo.metrics.MetricPoint;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository(value = "metricsProcessDAO")
public interface MetricsProcessPOMapper {

    int insert(MetricsProcessPO record);

    int insertSelective(MetricsProcessPO record);

    MetricsProcessPO selectByPrimaryKey(Long id);

    /**
     * @param params
     *  fieldName：字段名
     *  hostName：agent宿主机主机名
     *  startTime：心跳开始时间戳
     *  endTime：心跳结束时间戳
     */
    Object getLast(Map<String, Object> params);

    /**
     * @param params
     *  function：聚合函数名
     *  fieldName：字段名
     *  hostName：agent宿主机主机名
     *  startTime：心跳开始时间戳
     *  endTime：心跳结束时间戳
     */
    List<MetricPoint> getSingleChatNonStatistic(Map<String, Object> params);

    /**
     * @param params
     *  function：聚合函数名
     *  fieldName：字段名
     *  hostName：agent宿主机主机名
     *  startTime：心跳开始时间戳
     *  endTime：心跳结束时间戳
     */
    List<MetricPoint> getSingleChatStatistic(Map<String, Object> params);

    /**
     * @param params
     *  function：聚合函数名
     *  fieldName：字段名
     *  startTime：心跳开始时间戳
     *  endTime：心跳结束时间戳
     */
    Double getSumMetricAllAgents(Map<String, Object> params);

    /**
     * @param params
     *  function：聚合函数名
     *  fieldName：字段名
     *  sortTime：排序时间戳（精度：分钟）
     *  topN：前n条记录
     *  sortType：排序方式 desc、asc
     *  sortTimeField：排序字段名
     */
    List<MetricsLogCollectTaskTopPO> getTopNByMetricPerHostName(Map<String, Object> params);

    /**
     * @param params
     *  function：聚合函数名
     *  fieldName：字段名
     *  hostName：主机名
     *  startTime：心跳开始时间戳
     *  endTime：心跳结束时间戳
     */
    List<MetricPoint> getSingleChatStatisticByHostName(Map<String, Object> params);

    /**
     * @param params
     *  function：聚合函数名
     *  fieldName：字段名
     *  hostName：主机名
     *  startTime：心跳开始时间戳
     *  endTime：心跳结束时间戳
     */
    List<MetricPoint> getSingleChatNonStatisticByHostName(Map<String, Object> params);

    /**
     * 删除给定心跳时间戳之前所有指标数据
     * @param heartBeatTime 心跳时间戳
     */
    void deleteByLtHeartbeatTime(Long heartBeatTime);

    /**
     * @param params
     *  hostName：主机名
     * @return 返回最后一个 process 指标数据
     */
    MetricsProcessPO getLastRecord(Map<String, Object> params);

    /**
     * @param params
     *  hostName：日志采集任务运行主机名
     *  startTime：心跳开始时间（>）
     *  endTime：心跳结束时间(<=)
     *  function：聚合函数名
     *  fieldName：聚合字段名
     */
    Object getAggregationQueryPerHostNameFromMetricsProcess(Map<String, Object> params);

}