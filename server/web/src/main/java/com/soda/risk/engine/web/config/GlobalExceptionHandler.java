package com.soda.risk.engine.web.config;

import com.soda.risk.engine.api.dto.Response;
import com.soda.risk.engine.common.enums.CodeEnum;
import com.soda.risk.engine.common.exception.BizException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理器
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BizException.class)
    public Response<?> handleBizException(BizException e) {
        log.warn("BizException: code={}, msg={}", e.getCode(), e.getMessage());
        return Response.fail(e.getCode(), e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public Response<?> handleException(Exception e) {
        log.error("Unexpected exception: {}", e.getMessage(), e);
        return Response.fail(-1, e.getMessage() != null ? e.getMessage() : "系统异常");
    }
}
