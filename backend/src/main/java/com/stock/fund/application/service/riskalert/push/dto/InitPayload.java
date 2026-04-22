package com.stock.fund.application.service.riskalert.push.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * SSE 连接初始化事件载荷 在连接建立后立即发送，包含初始状态
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InitPayload {

    private long unreadCount; // 未读风险提醒数量
    private List<TodaySummaryItem> todaySummary; // 今日风险提醒汇总

    /**
     * 今日风险提醒汇总项
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TodaySummaryItem {
        private Long id;
        private String symbol;
        private String symbolName;
        private String symbolType;
        private String status; // ACTIVE / CLEARED
        private String latestChangePercent;
        private String latestTriggeredAt;
    }
}
