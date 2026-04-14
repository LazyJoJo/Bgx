package com.stock.fund.application.service.riskalert.dto;

import lombok.Data;
import java.util.List;

/**
 * 批量订阅标的请求（用于批量创建价格提醒以启用风险提醒）
 */
@Data
public class BatchSubscribeRequest {
    private Long userId;             // 用户ID
    private String symbolType;        // STOCK / FUND
    private List<String> symbols;     // 标的代码列表
    
    // 以下为价格提醒的配置参数，提供合理的默认值
    private String alertType;         // PRICE_ABOVE / PRICE_BELOW / PERCENTAGE_CHANGE
    private Double targetPrice;       // 目标价格（可选）
    private Double targetChangePercent; // 目标涨跌幅（可选）
    private Double basePrice;         // 基准价格（可选）
    private Boolean status = true;    // 是否启用，默认启用
}
