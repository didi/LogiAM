package com.didichuxing.datachannel.agentmanager.persistence.mysql;

import com.didichuxing.datachannel.agentmanager.common.bean.po.metrics.MetricsDiskIOPO;
import com.didichuxing.datachannel.agentmanager.common.bean.po.metrics.MetricsDiskIOTopPO;
import com.didichuxing.datachannel.agentmanager.common.bean.vo.metrics.MetricPoint;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository(value = "metricsDiskIODAO")
public interface MetricsDiskIOPOMapper {

    int insert(MetricsDiskIOPO record);

    int insertSelective(MetricsDiskIOPO record);

    MetricsDiskIOPO selectByPrimaryKey(Long id);

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
     *  device：磁盘设备名
     *  startTime：心跳开始时间戳
     *  endTime：心跳结束时间戳
     */
    List<MetricPoint> getSingleChatNonStatisticByDevice(Map<String, Object> params);

    /**
     * @param params
     *  function：聚合函数名
     *  fieldName：字段名
     *  hostName：agent宿主机主机名
     *  device：磁盘设备名
     *  startTime：心跳开始时间戳
     *  endTime：心跳结束时间戳
     */
    List<MetricPoint> getSingleChatStatisticByDevice(Map<String, Object> params);

    /**
     * @param params
     *  function：聚合函数名
     *  fieldName：排序字段名
     *  hostName：agent宿主机主机名
     *  sortTime：排序时间戳（精度：分钟）
     *  topN：前n条记录
     *  sortType：排序方式 desc、asc
     */
    List<MetricsDiskIOTopPO> getTopNDiskDevice(Map<String, Object> params);

    List<MetricsDiskIOPO> selectAll();

    /**
     * 删除给定心跳时间戳之前所有指标数据
     * @param heartBeatTime 心跳时间戳
     */
    void deleteByLtHeartbeatTime(Long heartBeatTime);

}