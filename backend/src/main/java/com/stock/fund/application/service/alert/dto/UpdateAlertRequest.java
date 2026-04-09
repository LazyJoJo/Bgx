package com.stock.fund.application.service.alert.dto;

import lombok.Data;

/**
 * 更新提醒请求
 */
@Data
public class UpdateAlertRequest {
    private String alertType;        // PRICE_ABOVE / PRICE_BELOW / PERCENTAGE_CHANGE
    private Double targetPrice;       // 目标价格
    private Double targetChangePercent; // 目标涨跌幅
    private Double basePrice;        // 基准价格
    private Boolean status;           // 是否启用
}
