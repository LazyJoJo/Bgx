package com.stock.fund.application.service.riskalert.dto;

import lombok.Builder;
import lombok.Data;

/**
 * 未读数量响应
 */
@Data
@Builder
public class UnreadCountResponse {
    private int total;                    // 总未读数
    private int riskAlertCount;           // 风险提醒未读数
}
