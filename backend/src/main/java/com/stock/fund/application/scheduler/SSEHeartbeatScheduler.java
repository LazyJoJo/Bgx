package com.stock.fund.application.scheduler;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.stock.fund.application.service.riskalert.push.RiskAlertPushService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * SSE 心跳调度器
 * 
 * <p>
 * 定期向所有活跃的 SSE 连接发送心跳，保持连接活跃。 避免浏览器/代理在静默期后关闭连接。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SSEHeartbeatScheduler {

    private final RiskAlertPushService riskAlertPushService;

    /**
     * 每 30 秒发送一次全局心跳
     */
    @Scheduled(fixedRate = 30000)
    public void sendHeartbeat() {
        int onlineCount = riskAlertPushService.getOnlineUserCount();
        if (onlineCount > 0) {
            log.debug("Sending SSE global heartbeat, online user count: {}", onlineCount);
            riskAlertPushService.sendPingToAll();
        }
    }
}