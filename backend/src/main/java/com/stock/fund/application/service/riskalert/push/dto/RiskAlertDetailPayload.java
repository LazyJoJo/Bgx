package com.stock.fund.application.service.riskalert.push.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 风险提醒明细 SSE 事件载荷
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RiskAlertDetailPayload {

    private Long id;
    private BigDecimal changePercent;
    private BigDecimal currentPrice;
    private String triggeredAt;
    private String triggerReason;

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