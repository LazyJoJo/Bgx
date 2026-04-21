package com.stock.fund.interfaces.advice;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.stock.fund.domain.exception.BusinessException;
import com.stock.fund.domain.exception.DataCollectionException;
import com.stock.fund.domain.exception.ResourceNotFoundException;
import com.stock.fund.interfaces.dto.response.ApiResponse;

/**
 * 全局异常处理器 统一处理 Controller 层的异常，返回统一的响应格式
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * 处理业务异常
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusinessException(BusinessException e) {
        logger.warn("Business exception: {} - {}", e.getCode(), e.getMessage());
        ApiResponse<Void> response = new ApiResponse<>(false, "[" + e.getCode() + "] " + e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * 处理资源未找到异常
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleResourceNotFoundException(ResourceNotFoundException e) {
        logger.warn("Resource not found: {} - {}", e.getCode(), e.getMessage());
        ApiResponse<Void> response = new ApiResponse<>(false, "[" + e.getCode() + "] " + e.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    /**
     * 处理数据采集异常
     */
    @ExceptionHandler(DataCollectionException.class)
    public ResponseEntity<ApiResponse<Void>> handleDataCollectionException(DataCollectionException e) {
        logger.error("Data collection exception: {}", e.getMessage(), e);
        ApiResponse<Void> response = ApiResponse.error("Data collection failed: " + e.getMessage());
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

        logger.warn("Parameter validation failed: {}", errors);
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
        // 检查是否是已知的业务异常类型，避免重复处理
        if (e instanceof BusinessException) {
            // BusinessException 已被上面的 handler 处理，这里不会到达
            // 但为了安全起见，检查一下
            ApiResponse<Void> response = new ApiResponse<>(false,
                    "[" + ((BusinessException) e).getCode() + "] " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
        logger.error("Runtime exception: {}", e.getMessage(), e);
        ApiResponse<Void> response = ApiResponse.error("Internal system error, please try again later");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    /**
     * 处理所有未捕获的异常
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleException(Exception e) {
        // 检查是否是已知的业务异常类型
        if (e instanceof BusinessException) {
            ApiResponse<Void> response = new ApiResponse<>(false,
                    "[" + ((BusinessException) e).getCode() + "] " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
        logger.error("Unhandled exception: {}", e.getMessage(), e);
        ApiResponse<Void> response = ApiResponse.error("Unknown system error occurred");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}
