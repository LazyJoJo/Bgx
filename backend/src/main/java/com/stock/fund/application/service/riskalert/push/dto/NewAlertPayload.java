package com.stock.fund.application.service.riskalert.push.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 新风险提醒 SSE 事件载荷 字段与前端 RiskAlert 类型完全对齐，确保推送数据可直接插入 Redux store
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NewAlertPayload {

    private Long id; // Database real ID
    private String symbol;
    private String symbolName;
    private String symbolType;
    private String date;
    private String status; // ACTIVE / CLEARED
    private BigDecimal latestChangePercent;
    private BigDecimal maxChangePercent; // Max change percent for the day, equals latestChangePercent when first
                                         // triggered
    private BigDecimal minChangePercent; // Min change percent for the day
    private BigDecimal currentPrice;
    private BigDecimal yesterdayClose;
    private String latestTriggeredAt;
    private boolean isRead;

    private String messageId; // Unique message ID for SSE lastEventId tracking

    @Builder.Default
    private List<RiskAlertDetailPayload> details = new ArrayList<>(); // Detail list, empty when newly created

    /**
     * 创建时间字符串（格式化）
     */
    public static String formatTime(LocalDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }
        return dateTime.atZone(ZoneId.of("Asia/Shanghai")).toInstant().toString();
    }
}