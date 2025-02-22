package com.js.jsojbackendcommon.common;

/**
 * 返回工具类
 * @author sakisaki
 * @date 2025/2/22 14:32
 */
public class ResultUtils {

    /**
     * 成功
     *
     * @param data 返回的数据集
     * @return BaseResponse
     */
    public static <T> BaseResponse<T> success(T data) {
        return new BaseResponse<>(0, data, "ok");
    }

    /**
     * 失败
     *
     * @param errorCode 自定义状态码
     * @return BaseResponse
     */
    public static BaseResponse<?> error(ErrorCode errorCode) {
        return new BaseResponse<>(errorCode);
    }

    /**
     * 失败
     *
     * @param code 状态码
     * @param message 状态信息
     * @return BaseResponse
     */
    public static BaseResponse<?> error(int code, String message) {
        return new BaseResponse<>(code, null, message);
    }

    /**
     * 失败
     *
     * @param errorCode 自定义状态码
     * @param message 状态信息
     * @return BaseResponse
     */
    public static BaseResponse<?> error(ErrorCode errorCode, String message) {
        return new BaseResponse<>(errorCode.getCode(), null, message);
    }
}
