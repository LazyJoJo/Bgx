package com.stock.fund.application.service.riskalert.push.dto;

import cn.hutool.core.date.DateUtil;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * SSE 事件信封 用于统一封装所有 SSE 事件的数据结构
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RiskAlertSSEEvent<T> {

    private String eventType; // init | ping | new_alert | unread_count_change
    private String messageId; // UUID
    private T payload;
    private String timestamp;

    public static <T> RiskAlertSSEEvent<T> of(String eventType, T payload) {
        return RiskAlertSSEEvent.<T>builder().eventType(eventType).messageId(java.util.UUID.randomUUID().toString())
                .payload(payload).timestamp(DateUtil.now()).build();
    }
}