package com.stock.fund.domain.exception;

/**
 * 资源未找到异常 用于当请求的资源不存在时
 */
public class ResourceNotFoundException extends BusinessException {

    public ResourceNotFoundException(String message) {
        super("RESOURCE_NOT_FOUND", message);
    }

    public ResourceNotFoundException(String resourceType, Object identifier) {
        super("RESOURCE_NOT_FOUND", String.format("%s not found: %s", resourceType, identifier));
    }
}
