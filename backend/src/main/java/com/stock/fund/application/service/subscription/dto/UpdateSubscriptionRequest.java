package com.stock.fund.application.service.subscription.dto;

import lombok.Data;
import java.math.BigDecimal;

/**
 * 更新订阅请求
 */
@Data
public class UpdateSubscriptionRequest {
    private BigDecimal targetChangePercent;
    private Boolean isActive;
    private String description;
}