package com.stock.fund.application.service.alert.dto;

import lombok.Data;
import java.util.List;

/**
 * 批量创建提醒请求
 */
@Data
public class BatchCreateAlertRequest {
    private Long userId;             // 用户ID
    private String symbolType;        // STOCK / FUND
    private List<String> symbols;     // 标的代码列表
    private String symbolName;        // 标的名称（可选）
    private String alertType;        // PRICE_ABOVE / PRICE_BELOW / PERCENTAGE_CHANGE
    private Double targetPrice;       // 目标价格
    private Double targetChangePercent; // 目标涨跌幅
    private Double basePrice;        // 基准价格
    private Boolean status;           // 是否启用
}
