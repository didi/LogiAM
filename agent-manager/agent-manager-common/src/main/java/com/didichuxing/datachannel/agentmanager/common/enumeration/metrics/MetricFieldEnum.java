package com.didichuxing.datachannel.agentmanager.common.enumeration.metrics;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 指标字段
 * @author william
 */
public enum MetricFieldEnum {

    /*********************************** 系统相关 ***********************************/

    /*
     * 基础信息
     */
//    HOST_NAME("host_name", "hostName", "主机名", MetricTypeEnum.SYSTEM_BASIC, AggregationCalcFunctionEnum.NONE,MetricValueTypeEnum.CURRENT),
//    IPS("ips", "ips", "IP列表", MetricTypeEnum.SYSTEM_BASIC, AggregationCalcFunctionEnum.NONE,MetricValueTypeEnum.CURRENT),
//    SYSTEM_NTP_OFFSET("system_ntp_offset","systemNtpOffset","系统时间偏移量", MetricTypeEnum.SYSTEM_BASIC, AggregationCalcFunctionEnum.MAX,MetricValueTypeEnum.CURRENT),
//    SYSTEM_UP_TIME("system_uptime", "systemUptime", "系统运行时间", MetricTypeEnum.SYSTEM_BASIC, AggregationCalcFunctionEnum.NONE,MetricValueTypeEnum.CURRENT),

    SYSTEM_STARTUP_TIME(
            1,
            "system_startup_time",
            "systemStartupTime",
            "系统启动时间",
            MetricTypeEnum.SYSTEM_BASIC,
            AggregationCalcFunctionEnum.NONE,
            MetricValueTypeEnum.CURRENT,
            MetricUnitEnum.TIMESTAMP_MILLISECOND,
            MetricUnitEnum.DATE_TIME,
            MetricDisplayTypeEnum.LABLE,
            false,
            null
    ),

    SYSTEM_NTP_OFFSET(
            1,
                    "system_ntp_offset",
                    "systemNtpOffset",
                    "源时钟与本地时钟的时间差 单位：毫秒",
            MetricTypeEnum.SYSTEM_BASIC,
            AggregationCalcFunctionEnum.MAX,
            MetricValueTypeEnum.CURRENT,
            MetricUnitEnum.TIMESTAMP_MILLISECOND,
            MetricUnitEnum.TIMESTAMP_SECOND,
            MetricDisplayTypeEnum.SINGLE_LINE_CHAT,
            true,
            null
    ),

    /*
     * os 相关
     */
//    SYSTEM_OS_TYPE("os_type", "osType", "系统操作系统类型", MetricTypeEnum.SYSTEM_OS, AggregationCalcFunctionEnum.NONE,MetricValueTypeEnum.CURRENT),
//    SYSTEM_OS_VERSION("os_version", "osVersion", "系统操作系统版本", MetricTypeEnum.SYSTEM_OS, AggregationCalcFunctionEnum.NONE,MetricValueTypeEnum.CURRENT),
//    SYSTEM_OS_KERNEL_VERSION("os_kernel_version", "osKernelVersion", "系统操作系统内核版本", MetricTypeEnum.SYSTEM_OS, AggregationCalcFunctionEnum.NONE,MetricValueTypeEnum.CURRENT),

    /*
     * process 相关
     *
     * TODO：
     *
     */

    /*
     * cpu 相关
     */
//    SYSTEM_CPU_CORES("cpu_cores","cpuCores","cpu核数", MetricTypeEnum.SYSTEM_CPU, AggregationCalcFunctionEnum.NONE,MetricValueTypeEnum.CURRENT),
    SYSTEM_CPU_UTIL(
            2,
            "system_cpu_util",
            "systemCpuUtil",
            "系统总体CPU使用率(单位：%)，使用率采用全核方式计数，如系统使用一颗核，则返回100，如使用两颗核，则返回200",
            MetricTypeEnum.SYSTEM_CPU,
            AggregationCalcFunctionEnum.MAX,
            MetricValueTypeEnum.STATISTICS,
            MetricUnitEnum.PERCENT,
            MetricUnitEnum.PERCENT,
            MetricDisplayTypeEnum.SINGLE_LINE_CHAT,
            true,
            null
    ),

    /*
     * memory 相关
     */
    SYSTEM_MEMORY_FREE(
            3,
            "system_memory_free",
            "systemMemFree",
            "系统空闲内存大小（单位：byte），当前值",
            MetricTypeEnum.SYSTEM_MEMORY,
            AggregationCalcFunctionEnum.MIN,
            MetricValueTypeEnum.CURRENT,
            MetricUnitEnum.BYTE,
            MetricUnitEnum.M_BYTE,
            MetricDisplayTypeEnum.SINGLE_LINE_CHAT,
            false,
            null
    ),

    /*
     * disk 相关
     */
    SYSTEM_DISK_BYTES_FREE(
            4,
            "system_disk_bytes_free",
            "systemDiskBytesFree",
            "磁盘余量大小（单位：MB）",
            MetricTypeEnum.SYSTEM_DISK,
            AggregationCalcFunctionEnum.MIN,
            MetricValueTypeEnum.CURRENT,
            MetricUnitEnum.BYTE,
            MetricUnitEnum.M_BYTE,
            MetricDisplayTypeEnum.MULTI_LINE_CHAT,
            false,
            SortTypeEnum.DESC
    ),

    /*
     * disk io 相关
     */
    SYSTEM_DISK_IO_IO_UTIL(
            5,
            "io_util",
            "systemIOUtil",
            "各磁盘I/O请求的时间百分比",
            MetricTypeEnum.SYSTEM_DISK_IO,
            AggregationCalcFunctionEnum.MAX,
            MetricValueTypeEnum.STATISTICS,
            MetricUnitEnum.PERCENT,
            MetricUnitEnum.PERCENT,
            MetricDisplayTypeEnum.MULTI_LINE_CHAT,
            false,
            SortTypeEnum.DESC
            ),

    SYSTEM_NET_CARD_SEND_BYTES_PS(
            6,
            "send_bytes/s",
            "systemNetCardsSendBytesPs",
            "各网卡每秒上行流量（单位：byte）",
            MetricTypeEnum.SYSTEM_NET_CARD,
            AggregationCalcFunctionEnum.MAX,
            MetricValueTypeEnum.STATISTICS,
            MetricUnitEnum.BYTE,
            MetricUnitEnum.M_BYTE,
            MetricDisplayTypeEnum.MULTI_LINE_CHAT,
            false,
            SortTypeEnum.DESC
    ),

    SYSTEM_NET_CARD_BAND_WIDTH(
            7,
            "band_width",
            "systemNetCardsBandWidth",
            "各网卡最大带宽（单位：byte）",
            MetricTypeEnum.SYSTEM_NET_CARD,
            AggregationCalcFunctionEnum.MAX,
            MetricValueTypeEnum.CURRENT,
            MetricUnitEnum.BYTE,
            MetricUnitEnum.M_BYTE,
            MetricDisplayTypeEnum.MULTI_LINE_CHAT,
            false,
            SortTypeEnum.DESC
    ),

    SYSTEM_NET_WORK_SEND_AND_RECEIVE_BYTES_PS(
            7,
            "system_network_send_and_receive_bytes_ps",
            "systemNetworkSendAndReceiveBytesPs",
            "系统网络每秒总流量（单位：byte）",
            MetricTypeEnum.SYSTEM_NET_WORK,
            AggregationCalcFunctionEnum.MAX,
            MetricValueTypeEnum.CURRENT,
            MetricUnitEnum.BYTE,
            MetricUnitEnum.M_BYTE,
            MetricDisplayTypeEnum.SINGLE_LINE_CHAT,
            false,
            null
    ),

    SYSTEM_NET_WORK_BAND_WIDTH_USED_PERCENT(
            7,
                    "system_net_work_band_width_used_percent",
                    "systemNetWorkBandWidthUsedPercent",
                    "系统网络带宽使用率（单位：%)",
            MetricTypeEnum.SYSTEM_NET_WORK,
            AggregationCalcFunctionEnum.MAX,
            MetricValueTypeEnum.CURRENT,
            MetricUnitEnum.PERCENT,
            MetricUnitEnum.PERCENT,
            MetricDisplayTypeEnum.SINGLE_LINE_CHAT,
            false,
                    null
    ),

    /*********************************** 当前进程相关 ***********************************/

    /*
     * 基础信息
     */
    PROCESS_START_UP_TIME(
            8,
            "process_startup_time",
            "procStartupTime",
            "当前进程启动时间",
            MetricTypeEnum.PROCESS_BASIC,
            AggregationCalcFunctionEnum.MAX,
            MetricValueTypeEnum.CURRENT,
            MetricUnitEnum.TIMESTAMP_MILLISECOND,
            MetricUnitEnum.DATE_TIME,
            MetricDisplayTypeEnum.LABLE,
            false,
            null
    ),

    /*
     * cpu 相关
     */
    PROCESS_CPU_UTIL(
            9,
            "process_cpu_util",
            "procCpuUtil",
            "当前进程cpu使用率(单位：%) 使用率采用全核方式计数，如进程使用一颗核，则返回100，如进程使用两颗核，则返回200",
            MetricTypeEnum.PROCESS_CPU,
            AggregationCalcFunctionEnum.MAX,
            MetricValueTypeEnum.STATISTICS,
            MetricUnitEnum.PERCENT,
            MetricUnitEnum.PERCENT,
            MetricDisplayTypeEnum.SINGLE_LINE_CHAT,
            true,
            null
    ),

    /*
     * memory 相关
     */
    PROCESS_MEMORY_USED(
            10,
            "process_memory_used",
            "procMemUsed",
            "当前进程内存使用量（单位：byte）当前值",
            MetricTypeEnum.PROCESS_MEMORY,
            AggregationCalcFunctionEnum.MAX,
            MetricValueTypeEnum.CURRENT,
            MetricUnitEnum.BYTE,
            MetricUnitEnum.M_BYTE,
            MetricDisplayTypeEnum.SINGLE_LINE_CHAT,
            true,
            null
    ),

    PROCESS_NET_WORK_SEND_BYTES_PS(
            16,
            "process_network_send_bytes_ps",
            "procNetworkSendBytesPs",
            "当前进程网络每秒上行流量",
            MetricTypeEnum.PROCESS_NET_WORK,
            AggregationCalcFunctionEnum.MAX,
            MetricValueTypeEnum.STATISTICS,
            MetricUnitEnum.BYTE,
            MetricUnitEnum.M_BYTE,
            MetricDisplayTypeEnum.SINGLE_LINE_CHAT,
            false,
            null
    ),

    PROCESS_NET_WORK_RECEIVE_BYTES_PS(
            16,
            "process_network_receive_bytes_ps",
            "procNetworkReceiveBytesPs",
            "当前进程网络每秒下行流量",
            MetricTypeEnum.PROCESS_NET_WORK,
            AggregationCalcFunctionEnum.MAX,
            MetricValueTypeEnum.STATISTICS,
            MetricUnitEnum.BYTE,
            MetricUnitEnum.M_BYTE,
            MetricDisplayTypeEnum.SINGLE_LINE_CHAT,
            false,
            null
    ),

    JVM_FULL_GC_COUNT(
            16,
            "process_jvm_full_gc_count",
            "jvmProcFullGcCount",
            "采样周期内 full gc 次数",
            MetricTypeEnum.PROCESS_GC,
            AggregationCalcFunctionEnum.SUM,
            MetricValueTypeEnum.CURRENT,
            MetricUnitEnum.NONE,
            MetricUnitEnum.NONE,
            MetricDisplayTypeEnum.SINGLE_LINE_CHAT,
            false,
            null
    ),

    PROCESS_FD_USED(
            12,
            "process_open_fd_count",
            "procOpenFdCount",
            "当前进程打开fd数量",
            MetricTypeEnum.PROCESS_FD,
            AggregationCalcFunctionEnum.MAX,
            MetricValueTypeEnum.CURRENT,
            MetricUnitEnum.NONE,
            MetricUnitEnum.NONE,
            MetricDisplayTypeEnum.SINGLE_LINE_CHAT,
            true,
            null
    ),

    /*********************************** agent 相关 ***********************************/

    AGENT_VERSION(
            11,
            "agent_version",
            "agentVersion",
            "agent版本号",
            MetricTypeEnum.AGENT_BUSINESS,
            null,
            MetricValueTypeEnum.CURRENT,
            MetricUnitEnum.NONE,
            MetricUnitEnum.NONE,
            MetricDisplayTypeEnum.LABLE,
            false,
            null
    ),

    AGENT_WRITE_COUNT(
            12,
            "write_count",
            "writeCount",
            "采样周期内出口采集条数 单位：条",
            MetricTypeEnum.AGENT_BUSINESS,
            AggregationCalcFunctionEnum.SUM,
            MetricValueTypeEnum.CURRENT,
            MetricUnitEnum.NONE,
            MetricUnitEnum.NONE,
            MetricDisplayTypeEnum.SINGLE_LINE_CHAT,
            true,
            null
    ),

    AGENT_WRITE_BYTES(
            12,
                    "write_bytes",
                    "writeBytes",
                    "采样周期内出口采集流量 单位：bytes",
            MetricTypeEnum.AGENT_BUSINESS,
            AggregationCalcFunctionEnum.SUM,
            MetricValueTypeEnum.CURRENT,
            MetricUnitEnum.BYTE,
            MetricUnitEnum.M_BYTE,
            MetricDisplayTypeEnum.SINGLE_LINE_CHAT,
            true,
            null
    ),

    AGENT_RUNNING_COLLECT_TASK_COUNT(
            12,
            "running_collect_task_num",
            "runningCollectTaskNum",
            "运行状态采集任务数",
            MetricTypeEnum.AGENT_BUSINESS,
            AggregationCalcFunctionEnum.MAX,
            MetricValueTypeEnum.CURRENT,
            MetricUnitEnum.NONE,
            MetricUnitEnum.NONE,
            MetricDisplayTypeEnum.SINGLE_LINE_CHAT,
            true,
            null
    ),

    AGENT_RUNNING_COLLECT_PATH_COUNT(
            12,
            "running_collect_path_num",
            "runningCollectPathNum",
            "运行状态采集路径数",
            MetricTypeEnum.AGENT_BUSINESS,
            AggregationCalcFunctionEnum.MAX,
            MetricValueTypeEnum.CURRENT,
            MetricUnitEnum.NONE,
            MetricUnitEnum.NONE,
            MetricDisplayTypeEnum.SINGLE_LINE_CHAT,
            true,
            null
    ),

    /*********************************** 采集任务相关 ***********************************/

    LOG_COLLECT_TASK_BUSINESS_TIMESTAMP(
            13,
            "business_time",
            "businessTimestamp",
            "采集业务时间",
            MetricTypeEnum.LOG_COLLECT_TASK_BUSINESS,
            AggregationCalcFunctionEnum.MIN,
            MetricValueTypeEnum.CURRENT,
            MetricUnitEnum.TIMESTAMP_MILLISECOND,
            MetricUnitEnum.DATE_TIME,
            MetricDisplayTypeEnum.LABLE,
            true,
            SortTypeEnum.DESC
    ),

    LOG_COLLECT_TASK_SEND_BYTES(
            14,
     "send_bytes",
     "sendBytes",
     "日志发送字节数",
            MetricTypeEnum.LOG_COLLECT_TASK_BUSINESS,
            AggregationCalcFunctionEnum.SUM,
            MetricValueTypeEnum.CURRENT,
            MetricUnitEnum.BYTE,
            MetricUnitEnum.M_BYTE,
            MetricDisplayTypeEnum.MULTI_LINE_CHAT,
            true,
            SortTypeEnum.DESC
    ),

    LOG_COLLECT_TASK_SEND_COUNT(
            14,
            "send_count",
            "sendCount",
            "采样周期内日志发送条数",
            MetricTypeEnum.LOG_COLLECT_TASK_BUSINESS,
            AggregationCalcFunctionEnum.SUM,
            MetricValueTypeEnum.CURRENT,
            MetricUnitEnum.BYTE,
            MetricUnitEnum.M_BYTE,
            MetricDisplayTypeEnum.MULTI_LINE_CHAT,
            true,
            SortTypeEnum.DESC
    ),

    LOG_COLLECT_TASK_READ_TIME_PER_EVENT(
            15,
            "read_time_per_event",
            "readTimePerEvent",
            "采样周期内单 log event 读取耗时 (单位：纳秒)",
            MetricTypeEnum.LOG_COLLECT_TASK_BUSINESS,
            AggregationCalcFunctionEnum.MAX,
            MetricValueTypeEnum.STATISTICS,
            MetricUnitEnum.TIMESTAMP_NANOSECOND,
            MetricUnitEnum.TIMESTAMP_NANOSECOND,
            MetricDisplayTypeEnum.MULTI_LINE_CHAT,
            false,
            SortTypeEnum.DESC
    ),

    LOG_COLLECT_TASK_MAX_BUSINESS_TIMESTAMP_DELAY(
            15,
            "max_business_timestamp_delay",
            "maxBusinessTimestampDelay",
            "数据业务时间最大延迟 （单位：秒）",
            MetricTypeEnum.LOG_COLLECT_TASK_BUSINESS,
            AggregationCalcFunctionEnum.MAX,
            MetricValueTypeEnum.CURRENT,
            MetricUnitEnum.TIMESTAMP_MILLISECOND,
            MetricUnitEnum.TIMESTAMP_SECOND,
            MetricDisplayTypeEnum.MULTI_LINE_CHAT,
            false,
            SortTypeEnum.DESC
    ),

    LOG_COLLECT_TASK_LIMIT_TIME(
            15,
            "limit_time",
            "limitTime",
            "采样周期内限流时长（单位：秒）",
            MetricTypeEnum.LOG_COLLECT_TASK_BUSINESS,
            AggregationCalcFunctionEnum.SUM,
            MetricValueTypeEnum.CURRENT,
            MetricUnitEnum.TIMESTAMP_MILLISECOND,
            MetricUnitEnum.TIMESTAMP_SECOND,
            MetricDisplayTypeEnum.MULTI_LINE_CHAT,
            false,
            SortTypeEnum.DESC
    )

    ;

    /**
     * 指标代码
     */
    private Integer code;

    /**
     * 指标名
     */
    private String metricName;

    /**
     * 存储系统字段名
     */
    private String fieldName;

    /**
     * 指标描述
     */
    private String description;

    /**
     * 指标类型（对应MetricTypeEnum.code）
     */
    private MetricTypeEnum metricType;

    /**
     * 该指标计算时采用的聚合函数枚举
     */
    private AggregationCalcFunctionEnum aggregationCalcFunction;

    /**
     * 指标值类型枚举
     */
    private MetricValueTypeEnum metricValueType;

    /**
     * 基础单位
     */
    private MetricUnitEnum baseUnit;

    /**
     * 显示单位
     */
    private MetricUnitEnum displayUnit;

    /**
     * 指标展示类型
     */
    private MetricDisplayTypeEnum metricDisplayType;

    /**
     * 是否默认选中
     */
    private boolean checked;

    private SortTypeEnum sortTypeEnum;

    MetricFieldEnum(
            Integer code,
            String metricName,
            String fieldName,
            String description,
            MetricTypeEnum metricType,
            AggregationCalcFunctionEnum aggregationCalcFunction,
            MetricValueTypeEnum metricValueType,
            MetricUnitEnum baseUnit,
            MetricUnitEnum displayUnit,
            MetricDisplayTypeEnum metricDisplayType,
            boolean checked,
            SortTypeEnum sortTypeEnum
            ) {
        this.code = code;
        this.metricName = metricName;
        this.fieldName = fieldName;
        this.description = description;
        this.metricType = metricType;
        this.aggregationCalcFunction = aggregationCalcFunction;
        this.metricValueType = metricValueType;
        this.baseUnit = baseUnit;
        this.displayUnit = displayUnit;
        this.metricDisplayType = metricDisplayType;
        this.checked = checked;
        this.sortTypeEnum = sortTypeEnum;
    }

    public String getMetricName() {
        return metricName;
    }

    public String getFieldName() {
        return fieldName;
    }

    public String getDescription() {
        return description;
    }

    public MetricTypeEnum getMetricType() {
        return metricType;
    }

    public AggregationCalcFunctionEnum getAggregationCalcFunction() {
        return aggregationCalcFunction;
    }

    public MetricValueTypeEnum getMetricValueType() {
        return metricValueType;
    }

    public MetricUnitEnum getBaseUnit() {
        return baseUnit;
    }

    public MetricUnitEnum getDisplayUnit() {
        return displayUnit;
    }

    public MetricDisplayTypeEnum getMetricDisplayType() {
        return metricDisplayType;
    }

    public Integer getCode() {
        return code;
    }

    public boolean isChecked() {
        return checked;
    }

    public SortTypeEnum getSortTypeEnum() {
        return sortTypeEnum;
    }

    /**
     * 根据指标代码返回对应指标枚举定义
     * @param metricCode 指标名
     * @return 如指标代码在指标枚举集存在，返回指标名对应指标枚举定义，如不存在，返回 null
     */
    public static MetricFieldEnum fromMetricCode(Integer metricCode) {
        for (MetricFieldEnum value : MetricFieldEnum.values()) {
            if (value.code.equals(metricCode)) {
                return value;
            }
        }
        return null;
    }

    /**
     * 根据给定指标类型获取属该指标类型的指标集
     * @param metricTypeEnum 指标类型
     * @return 返回根据给定指标类型获取属该指标类型的指标集
     */
    public static List<MetricFieldEnum> fromMetricTypeEnum(MetricTypeEnum metricTypeEnum) {
        List<MetricFieldEnum> result = new ArrayList<>();
        for (MetricFieldEnum value : MetricFieldEnum.values()) {
            if (value.metricType.equals(metricTypeEnum)) {
                result.add(value);
            }
        }
        return result;
    }

}
