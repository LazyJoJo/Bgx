package com.stock.fund.application.service.alert.dto;

import lombok.Data;

/**
 * 创建提醒请求
 */
@Data
public class CreateAlertRequest {
    private Long userId;             // 用户ID
    private String symbolType;        // STOCK / FUND
    private String symbol;            // 标的代码
    private String symbolName;        // 标的名称
    private String alertType;        // PRICE_ABOVE / PRICE_BELOW / PERCENTAGE_CHANGE
    private Double targetPrice;       // 目标价格
    private Double targetChangePercent; // 目标涨跌幅
    private Double basePrice;        // 基准价格（用于涨跌幅计算）
    private Boolean status;           // 是否启用
}
