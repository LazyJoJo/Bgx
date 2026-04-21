package com.stock.fund.application.service.riskalert.push.dto;

import cn.hutool.core.date.DateUtil;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 未读数变化 SSE 事件载荷
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UnreadCountPayload {

    private long unreadCount;
    private int delta;
    private String reason; // NEW_ALERT | MARK_READ
    private String timestamp;

    public static UnreadCountPayload of(long unreadCount, int delta, String reason) {
        return UnreadCountPayload.builder().unreadCount(unreadCount).delta(delta).reason(reason)
                .timestamp(DateUtil.now()).build();
    }
}