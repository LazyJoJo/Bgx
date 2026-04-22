package com.stock.fund.application.service.subscription.dto;

import java.math.BigDecimal;

import lombok.Data;

/**
 * 创建订阅请求
 */
@Data
public class CreateSubscriptionRequest {
    private Long userId;
    private String symbol;
    private String symbolType;
    private String symbolName;
    private String alertType; // PERCENT / AMOUNT - 监控类型
    private BigDecimal targetChangePercent;
    private Boolean isActive;
}