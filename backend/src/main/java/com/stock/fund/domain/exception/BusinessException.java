package com.stock.fund.domain.exception;

/**
 * 业务异常基类 用于封装业务层面的错误信息
 */
public class BusinessException extends RuntimeException {

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
