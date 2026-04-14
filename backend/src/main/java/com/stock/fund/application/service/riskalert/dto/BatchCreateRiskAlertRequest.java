package com.stock.fund.application.service.riskalert.dto;

import lombok.Data;
import java.util.List;

/**
 * 批量创建风险提醒请求
 */
@Data
public class BatchCreateRiskAlertRequest {
    private Long userId;              // 用户ID（必填）
    private String symbolType;        // 标的类型：STOCK / FUND（必填）
    private List<String> symbols;     // 标的代码列表（必填）
}
