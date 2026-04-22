package com.stock.fund.interfaces.controller.riskalert;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.stock.fund.application.query.RiskAlertQueryService;
import com.stock.fund.application.service.riskalert.RiskAlertAppService;
import com.stock.fund.application.service.riskalert.push.RiskAlertPushService;
import com.stock.fund.application.service.riskalert.push.dto.InitPayload;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 风险提醒 SSE 流式推送端点
 * 
 * <p>
 * 前端通过 EventSource 连接此端点，接收实时风险提醒推送。
 * 
 * <p>
 * 端点: GET /api/risk-alerts/stream
 * <p>
 * 参数: userId (URL query parameter, 必填)
 * <p>
 * 事件类型: init, ping, new_alert, unread_count_change
 */
@Slf4j
@RestController
@RequestMapping("/api/risk-alerts")
@RequiredArgsConstructor
public class RiskAlertSSEController {

    private final RiskAlertPushService riskAlertPushService;
    private final RiskAlertAppService riskAlertAppService;
    private final RiskAlertQueryService riskAlertQueryService;

    /**
     * SSE streaming endpoint
     * 
     * @param userId User ID (URL parameter, required)
     * @return SSE emitter
     */
    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter stream(@RequestParam(required = false) Long userId) {

        // Verify userId (AC-7-1: empty userId returns 400)
        if (userId == null) {
            log.warn("[SSE Controller] SSE connection request missing userId parameter");
            try {
                // Return error and close connection
                SseEmitter emitter = new SseEmitter(0L);
                emitter.send(SseEmitter.event().name("error")
                        .data("{\"code\":\"MISSING_USER_ID\",\"message\":\"userId parameter is required\"}"));
                emitter.complete();
                return emitter;
            } catch (IOException e) {
                log.error("[SSE Controller] Failed to send error event", e);
                return new SseEmitter(0L);
            }
        }

        // AC-7-2: Auth check - verify userId matches current logged-in user
        // TODO: Replace with SecurityContext or JWT after integrating real auth system
        Long currentUserId = getCurrentUserId();
        if (currentUserId != null && !currentUserId.equals(userId)) {
            log.warn("[SSE Controller] SSE connection request userId mismatch: userId={}, currentUserId={}", userId,
                    currentUserId);
            try {
                SseEmitter emitter = new SseEmitter(0L);
                emitter.send(SseEmitter.event().name("error")
                        .data("{\"code\":\"UNAUTHORIZED\",\"message\":\"User ID mismatch\"}"));
                emitter.complete();
                return emitter;
            } catch (IOException e) {
                log.error("[SSE Controller] Failed to send error event", e);
                return new SseEmitter(0L);
            }
        }

        log.info("[SSE Controller] User {} establishing SSE connection", userId);

        // Create SSE emitter with 0 timeout (no auto-timeout)
        SseEmitter emitter = new SseEmitter(0L);
        log.info("[SSE Controller] Created SseEmitter for userId={}", userId);

        // Register to push service (handles connection limit FIFO eviction internally)
        riskAlertPushService.register(userId, emitter);
        log.info("[SSE Controller] Registered emitter for userId={}, online count: {}", userId,
                riskAlertPushService.getOnlineUserCount());

        // Send init event with real data (unread count + today summary)
        sendInitEvent(userId);

        // Register callbacks for connection cleanup
        emitter.onCompletion(() -> {
            log.info("[SSE Controller] SSE connection completed: userId={}", userId);
        });

        emitter.onTimeout(() -> {
            log.info("[SSE Controller] SSE connection timeout: userId={}", userId);
        });

        emitter.onError(e -> {
            log.warn("[SSE Controller] SSE connection error: userId={}, error={}", userId, e.getMessage());
        });

        return emitter;
    }

    /**
     * 获取当前在线用户数和连接数统计
     * 
     * <p>
     * 用于监控和调试
     */
    @GetMapping("/stream/stats")
    public Map<String, Object> getStats() {
        return Map.of("onlineUserCount", riskAlertPushService.getOnlineUserCount(), "timestamp",
                System.currentTimeMillis());
    }

    /**
     * 发送初始化事件
     * <p>
     * 在 SSE 连接建立后立即发送，包含未读数和今日汇总
     */
    private void sendInitEvent(Long userId) {
        try {
            // Get unread count
            long unreadCount = riskAlertAppService.getUnreadCount(userId);

            // Get today's summary
            var todaySummaryList = riskAlertQueryService.getTodaySummary(userId);
            List<InitPayload.TodaySummaryItem> todaySummary = todaySummaryList.stream()
                    .map(dto -> InitPayload.TodaySummaryItem.builder().id(dto.id()).symbol(dto.symbol())
                            .symbolName(dto.symbolName()).symbolType(dto.symbolType()).status(dto.status())
                            .latestChangePercent(
                                    dto.latestChangePercent() != null ? dto.latestChangePercent().toString() : null)
                            .latestTriggeredAt(
                                    dto.latestTriggeredAt() != null ? dto.latestTriggeredAt().toString() : null)
                            .build())
                    .collect(Collectors.toList());

            InitPayload payload = InitPayload.builder().unreadCount(unreadCount).todaySummary(todaySummary).build();

            riskAlertPushService.sendInit(userId, payload);
            log.info("[SSE Controller] Sent init event to userId={}, unreadCount={}, todaySummaryCount={}", userId,
                    unreadCount, todaySummary.size());
        } catch (Exception e) {
            log.error("[SSE Controller] Failed to send init event to userId={}", userId, e);
        }
    }

    /**
     * 获取当前登录用户 ID
     * 
     * <p>
     * TODO: 集成真实认证系统后，替换为从 SecurityContext 或 JWT 获取当前用户 ID 当前实现返回 null 表示未登录（mock
     * 环境）
     * 
     * @return 当前用户 ID，未登录返回 null
     */
    private Long getCurrentUserId() {
        // TODO: 集成真实认证系统后实现
        // 示例（Spring Security）: return ((CustomUserDetails)
        // SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getId();
        return null;
    }
}