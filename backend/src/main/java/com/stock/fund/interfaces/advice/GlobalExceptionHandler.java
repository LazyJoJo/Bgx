package com.stock.fund.interfaces.advice;

import com.stock.fund.interfaces.dto.response.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

/**
 * 全局异常处理器
 * 统一处理 Controller 层的异常，返回统一的响应格式
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * 处理业务异常
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusinessException(BusinessException e) {
        logger.warn("业务异常: {} - {}", e.getCode(), e.getMessage());
        ApiResponse<Void> response = new ApiResponse<>(false, "[" + e.getCode() + "] " + e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * 处理数据采集异常
     */
    @ExceptionHandler(DataCollectionException.class)
    public ResponseEntity<ApiResponse<Void>> handleDataCollectionException(DataCollectionException e) {
        logger.error("数据采集异常: {}", e.getMessage(), e);
        ApiResponse<Void> response = ApiResponse.error("数据采集失败: " + e.getMessage());
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
    }

    /**
     * 处理参数验证异常
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleValidationException(
            MethodArgumentNotValidException e) {
        Map<String, String> errors = new HashMap<>();
        e.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        logger.warn("参数验证失败: {}", errors);
        ApiResponse<Map<String, String>> response = new ApiResponse<>();
        response.setSuccess(false);
        response.setMessage("参数验证失败");
        response.setData(errors);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * 处理运行时异常
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiResponse<Void>> handleRuntimeException(RuntimeException e) {
        logger.error("运行时异常: {}", e.getMessage(), e);
        ApiResponse<Void> response = ApiResponse.error("系统内部错误，请稍后重试");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    /**
     * 处理所有未捕获的异常
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleException(Exception e) {
        logger.error("未处理的异常: {}", e.getMessage(), e);
        ApiResponse<Void> response = ApiResponse.error("系统发生未知错误");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    /**
     * 业务异常类
     */
    public static class BusinessException extends RuntimeException {
        private String code = "BUSINESS_ERROR";

        public BusinessException(String message) {
            super(message);
        }

        public BusinessException(String code, String message) {
            super(message);
            this.code = code;
        }

        public String getCode() {
            return code;
        }
    }

    /**
     * 数据采集异常类
     */
    public static class DataCollectionException extends RuntimeException {
        public DataCollectionException(String message) {
            super(message);
        }

        public DataCollectionException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
