package com.stock.fund.application.service.riskalert.push.dto;

import java.math.BigDecimal;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 风险解除 SSE 事件载荷 当订阅的风险条件不再满足时推送此事件
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlertClearedPayload {

    private Long id; // 被清除的风险提醒 ID
    private String symbol;
    private String symbolName;
    private String symbolType;
    private String date;
    private String status; // 始终为 CLEARED
    private BigDecimal lastChangePercent; // 解除前的涨跌幅
    private BigDecimal currentChangePercent; // 当前涨跌幅（已恢复）
    private BigDecimal maxChangePercent; // 当日最高涨幅（保留）
    private BigDecimal minChangePercent; // 当日最低跌幅（保留）
    private BigDecimal currentPrice;
    private String latestTriggeredAt;
    private List<RiskAlertDetailPayload> details; // 明细列表，前端用 details.length 作为触发次数
}
