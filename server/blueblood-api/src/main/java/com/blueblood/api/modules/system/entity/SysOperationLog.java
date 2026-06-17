package com.blueblood.api.modules.system.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.blueblood.api.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 系统操作日志。
 * 注意：操作日志不软删，deletedAt 字段由 BaseEntity 继承保留兼容，默认不写入。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_operation_log")
public class SysOperationLog extends BaseEntity {

    private Long userId;

    private String username;

    /** 业务模块：USER/TASK/FINANCE... */
    private String module;

    /** 动作描述 */
    private String action;

    /** HTTP 方法：GET/POST/PUT/DELETE */
    private String method;

    private String url;

    /** 请求参数(text) */
    private String params;

    private String ip;

    /** 耗时(毫秒) */
    private Integer costMs;

    /** SUCCESS / FAIL */
    private String status;

    /** 失败时的错误信息(text) */
    private String errorMsg;
}
