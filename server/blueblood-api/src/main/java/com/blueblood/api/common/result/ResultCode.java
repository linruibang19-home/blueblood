package com.blueblood.api.common.result;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 全局状态码定义。
 * 约定：0 成功；401/403/4xx 客户端错误；5xx 服务端错误；1xxxx 业务错误。
 */
@Getter
@AllArgsConstructor
public enum ResultCode {

    SUCCESS(0, "成功"),

    // 通用客户端错误
    BAD_REQUEST(400, "请求参数错误"),
    UNAUTHORIZED(401, "未登录或登录已过期"),
    FORBIDDEN(403, "无权限访问"),
    NOT_FOUND(404, "资源不存在"),
    METHOD_NOT_ALLOWED(405, "请求方法不支持"),
    REQUEST_TIMEOUT(408, "请求超时"),

    // 通用服务端错误
    SERVER_ERROR(500, "服务器内部错误"),
    SERVICE_UNAVAILABLE(503, "服务暂不可用"),

    // 业务错误 1xxxx
    PARAM_VALIDATE_FAILED(10001, "参数校验失败"),
    BUSINESS_ERROR(10002, "业务处理失败"),
    DATA_NOT_FOUND(10003, "数据不存在"),
    DATA_DUPLICATED(10004, "数据已存在"),
    OPERATION_FAILED(10005, "操作失败"),

    // 认证相关 2xxxx
    LOGIN_FAILED(20001, "账号或密码错误"),
    ACCOUNT_DISABLED(20002, "账号已禁用"),
    TOKEN_INVALID(20003, "令牌无效"),
    TOKEN_EXPIRED(20004, "令牌已过期"),
    ACCOUNT_EXISTS(20005, "账号已存在"),

    // 权限相关 3xxxx
    NO_PERMISSION(30001, "权限不足");

    private final int code;
    private final String message;
}
