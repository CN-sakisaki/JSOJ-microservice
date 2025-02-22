package com.js.jsojbackendcommon.common;

import lombok.Data;

import java.io.Serializable;

/**
 * 通用返回类
 * @author sakisaki
 * @date 2025/2/22 14:31
 */
@Data
public class BaseResponse<T> implements Serializable {

    /**
     * 状态码
     */
    private int code;

    /**
     * 数据集
     */
    private T data;

    /**
     * 状态信息
     */
    private String message;

    public BaseResponse(int code, T data, String message) {
        this.code = code;
        this.data = data;
        this.message = message;
    }

    public BaseResponse(int code, T data) {
        this(code, data, "");
    }

    public BaseResponse(ErrorCode errorCode) {
        this(errorCode.getCode(), null, errorCode.getMessage());
    }
}
