package com.stock.fund.application.service.riskalert.push.dto;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 风险消除 SSE 事件载荷 当订阅的风险条件不再满足时推送此事件
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RiskClearedPayload {

    private Long id; // 被清除的风险提醒 ID
    private String symbol;
    private String symbolName;
    private String symbolType;
    private String date;
    private BigDecimal lastChangePercent; // 消除前的涨跌幅
    private BigDecimal currentChangePercent; // 当前涨跌幅（已不再满足风险条件）
    private BigDecimal currentPrice;
    private String latestTriggeredAt;
}
