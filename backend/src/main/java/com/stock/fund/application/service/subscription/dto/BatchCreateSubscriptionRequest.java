package com.stock.fund.application.service.subscription.dto;

import java.math.BigDecimal;
import java.util.List;

import lombok.Data;

/**
 * 批量创建订阅请求
 */
@Data
public class BatchCreateSubscriptionRequest {
    private Long userId;
    private List<String> symbols;
    private String symbolType;
    private String symbolName;
    private String alertType; // PERCENT / AMOUNT - 监控类型
    private BigDecimal targetChangePercent;
    private Boolean isActive;
}