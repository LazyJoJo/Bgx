package com.stock.fund.application.service.subscription.dto;

import lombok.Data;
import java.math.BigDecimal;

/**
 * 创建订阅请求
 */
@Data
public class CreateSubscriptionRequest {
    private Long userId;
    private String symbol;
    private String symbolType;
    private String symbolName;
    private BigDecimal targetChangePercent;
    private Boolean enabled;
}