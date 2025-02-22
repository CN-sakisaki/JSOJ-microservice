package com.js.jsojbackendcommon.exception;


import com.js.jsojbackendcommon.common.ErrorCode;
import lombok.Getter;

/**
 * 自定义异常类
 * @author sakisaki
 * @date 2025/2/22 14:29
 */
@Getter
public class BusinessException extends RuntimeException {

    /**
     * 错误码
     */
    private final int code;

    /**
     *
     * @param code 状态码
     * @param message 异常信息
     */
    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }

    /**
     *
     * @param errorCode 自定义状态码
     */
    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.code = errorCode.getCode();
    }

    /**
     *
     * @param errorCode 自定义状态码
     * @param message 异常信息
     */
    public BusinessException(ErrorCode errorCode, String message) {
        super(message);
        this.code = errorCode.getCode();
    }

}
