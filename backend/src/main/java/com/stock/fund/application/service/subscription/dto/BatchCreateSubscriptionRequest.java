package com.stock.fund.application.service.subscription.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

/**
 * 批量创建订阅请求
 */
@Data
public class BatchCreateSubscriptionRequest {
    private Long userId;
    private List<String> symbols;
    private String symbolType;
    private String symbolName;
    private BigDecimal targetChangePercent;
    private Boolean enabled;
}