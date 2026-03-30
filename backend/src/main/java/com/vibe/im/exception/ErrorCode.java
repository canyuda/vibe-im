package com.vibe.im.exception;

/**
 * 错误码枚举
 *
 * @author AI Assistant
 */
public enum ErrorCode {
    /**
     * 成功
     */
    SUCCESS(200, "成功"),

    /**
     * 参数错误
     */
    INVALID_PARAMETER(400, "参数错误"),

    /**
     * 认证失败
     */
    AUTHENTICATION_FAILED(401, "认证失败"),

    /**
     * 用户不存在
     */
    USER_NOT_FOUND(404, "用户不存在"),

    /**
     * 用户已存在
     */
    USER_ALREADY_EXISTS(409, "用户已存在"),

    /**
     * 消息不存在
     */
    MESSAGE_NOT_FOUND(404, "消息不存在"),

    /**
     * 服务器内部错误
     */
    INTERNAL_ERROR(500, "服务器内部错误");

    private final int code;
    private final String message;

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
