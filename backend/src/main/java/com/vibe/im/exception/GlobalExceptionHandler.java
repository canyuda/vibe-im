package com.vibe.im.exception;

import java.time.Instant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理器
 * 统一处理应用中的所有异常，返回标准化的错误响应
 *
 * @author AI Assistant
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * 错误响应记录
     *
     * @param code      错误码
     * @param message   错误消息
     * @param timestamp 时间戳
     */
    public record ErrorResponse(int code, String message, Instant timestamp) {
    }

    /**
     * 处理业务异常
     *
     * @param ex 业务异常
     * @return 错误响应
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(BusinessException ex) {
        log.error("Business exception occurred: code={}, message={}",
            ex.getCode(), ex.getMessage(), ex);

        ErrorResponse errorResponse = new ErrorResponse(
            ex.getCode(),
            ex.getMessage(),
            Instant.now()
        );

        return ResponseEntity
            .status(ex.getCode())
            .body(errorResponse);
    }

    /**
     * 处理所有未捕获的异常
     *
     * @param ex 异常
     * @return 错误响应
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception ex) {
        log.error("Unexpected exception occurred: {}", ex.getMessage(), ex);

        ErrorResponse errorResponse = new ErrorResponse(
            ErrorCode.INTERNAL_ERROR.getCode(),
            ErrorCode.INTERNAL_ERROR.getMessage(),
            Instant.now()
        );

        return ResponseEntity
            .status(ErrorCode.INTERNAL_ERROR.getCode())
            .body(errorResponse);
    }
}
