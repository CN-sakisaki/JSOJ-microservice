package com.js.jsojbackendcommon.exception;

import com.js.jsojbackendcommon.common.BaseResponse;
import com.js.jsojbackendcommon.common.ErrorCode;
import com.js.jsojbackendcommon.common.ResultUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理器
 * @author sakisaki
 * @date 2025/2/22 14:29
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     *
     * @param businessException 业务异常
     * @return BaseResponse
     */
    @ExceptionHandler(BusinessException.class)
    public BaseResponse<?> businessExceptionHandler(BusinessException businessException) {
        log.error("BusinessException", businessException);
        return ResultUtils.error(businessException.getCode(), businessException.getMessage());
    }

    /**
     *
     * @param runtimeException 运行异常
     * @return BaseResponse
     */
    @ExceptionHandler(RuntimeException.class)
    public BaseResponse<?> runtimeExceptionHandler(RuntimeException runtimeException) {
        log.error("RuntimeException", runtimeException);
        return ResultUtils.error(ErrorCode.SYSTEM_ERROR, "系统错误");
    }
}
