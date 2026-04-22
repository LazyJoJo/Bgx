package com.stock.fund.application.service.riskalert.push.impl;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.stock.fund.application.service.riskalert.push.RiskAlertPushService;
import com.stock.fund.application.service.riskalert.push.dto.AlertClearedPayload;
import com.stock.fund.application.service.riskalert.push.dto.InitPayload;
import com.stock.fund.application.service.riskalert.push.dto.RiskAlertSSEEvent;
import com.stock.fund.application.service.riskalert.push.dto.RiskClearedPayload;
import com.stock.fund.application.service.riskalert.push.dto.UnreadCountPayload;

import cn.hutool.core.date.DateUtil;
import lombok.RequiredArgsConstructor;

/**
 * 内存版风险提醒 SSE 推送服务实现
 * <p>
 * 使用 Caffeine Cache 管理用户的 SSE 连接： - 最大用户数限制：100,000 - 空闲超时：30分钟自动清理 -
 * 每个用户的连接数限制：5
 * <p>
 * Caffeine 会自动清理过期条目和超过最大容量的条目，防止内存泄漏
 */
@Service
@RequiredArgsConstructor
public class InMemoryRiskAlertPushService implements RiskAlertPushService {

    private static final Logger log = LoggerFactory.getLogger(InMemoryRiskAlertPushService.class);

    // Caffeine cache configuration for user emitter lists
    // - maximumSize: 最多缓存 100,000 个用户
    // - expireAfterAccess: 30分钟无访问则过期清理
    // - removalListener: 记录清理原因
    private static final Cache<Long, CopyOnWriteArrayList<SseEmitter>> userEmitters = Caffeine.newBuilder()
            .maximumSize(100_000).expireAfterAccess(30, TimeUnit.MINUTES).removalListener((userId, emitters, cause) -> {
                if (emitters != null && !emitters.isEmpty()) {
                    log.info("Caffeine removing user emitters: userId={}, cause={}, count={}", userId, cause,
                            emitters.size());
                    // Complete all remaining emitters before removal
                    emitters.forEach(emitter -> {
                        try {
                            emitter.complete();
                        } catch (Exception e) {
                            log.debug("Exception completing emitter during removal: userId={}", userId);
                        }
                    });
                }
            }).build();

    private final ObjectMapper objectMapper;

    @Override
    public void register(Long userId, SseEmitter emitter) {
        CopyOnWriteArrayList<SseEmitter> emitters = userEmitters.get(userId, k -> new CopyOnWriteArrayList<>());

        // AC-7: Connection limit control (FIFO eviction)
        synchronized (emitters) {
            while (emitters.size() >= MAX_CONNECTIONS_PER_USER) {
                SseEmitter oldest = emitters.get(0);
                emitters.remove(0);
                try {
                    oldest.complete();
                    log.warn(
                            "SSE connections exceeded limit, removing oldest connection: userId={}, count after removal={}",
                            userId, emitters.size());
                } catch (Exception e) {
                    log.warn("Exception closing old SSE connection: userId={}", userId, e);
                }
            }

            emitters.add(emitter);
        }

        // Auto-cleanup on connection close/timeout/error
        emitter.onCompletion(() -> removeEmitter(userId, emitter));
        emitter.onTimeout(() -> removeEmitter(userId, emitter));
        emitter.onError((e) -> removeEmitter(userId, emitter));

        log.info("SSE connection registered: userId={}, current connection count={}", userId, emitters.size());
    }

    private void removeEmitter(Long userId, SseEmitter emitter) {
        CopyOnWriteArrayList<SseEmitter> emitters = userEmitters.getIfPresent(userId);
        if (emitters != null) {
            synchronized (emitters) {
                emitters.remove(emitter);
                if (emitters.isEmpty()) {
                    // Invalidate from Caffeine cache to trigger cleanup
                    userEmitters.invalidate(userId);
                }
            }
            log.info("SSE connection removed: userId={}, remaining connection count={}", userId, emitters.size());
        }
    }

    @Override
    public void pushNewAlert(Long userId, Object payload) {
        CopyOnWriteArrayList<SseEmitter> emitters = userEmitters.getIfPresent(userId);
        if (emitters == null || emitters.isEmpty()) {
            log.warn("[SSE Push] No emitters found for userId={}, event not sent!", userId);
            return;
        }

        log.info("[SSE Push] pushNewAlert called for userId={}, emitter count={}", userId, emitters.size());
        RiskAlertSSEEvent<Object> event = RiskAlertSSEEvent.of("new_alert", payload);

        // Create a copy to avoid ConcurrentModificationException during send
        List<SseEmitter> emittersCopy = List.copyOf(emitters);
        for (SseEmitter emitter : emittersCopy) {
            try {
                String data = objectMapper.writeValueAsString(event);
                log.info("[SSE Push] Sending new_alert event to userId={}, data={}", userId, data);
                emitter.send(SseEmitter.event().name("new_alert").id(event.getMessageId()).data(data));
                log.info("[SSE Push] Successfully sent new_alert event to userId={}", userId);
            } catch (IOException e) {
                log.warn("[SSE Push] Failed to send, removing dead connection: userId={}, error={}", userId,
                        e.getMessage());
                removeEmitter(userId, emitter);
            }
        }
    }

    @Override
    public void pushUnreadCountChange(Long userId, UnreadCountPayload payload) {
        CopyOnWriteArrayList<SseEmitter> emitters = userEmitters.getIfPresent(userId);
        if (emitters == null || emitters.isEmpty()) {
            log.debug("No emitters for userId={} during unread_count_change", userId);
            return;
        }

        RiskAlertSSEEvent<UnreadCountPayload> event = RiskAlertSSEEvent.of("unread_count_change", payload);
        List<SseEmitter> emittersCopy = List.copyOf(emitters);

        for (SseEmitter emitter : emittersCopy) {
            try {
                String data = objectMapper.writeValueAsString(event);
                emitter.send(SseEmitter.event().name("unread_count_change").id(event.getMessageId()).data(data));
            } catch (IOException e) {
                log.warn("Failed to send unread_count_change SSE: userId={}, error={}", userId, e.getMessage());
                removeEmitter(userId, emitter);
            }
        }
        log.info("Sent unread_count_change SSE to userId={}, emitterCount={}, payload={}", userId, emitters.size(),
                payload);
    }

    @Override
    public void sendPing(Long userId) {
        CopyOnWriteArrayList<SseEmitter> emitters = userEmitters.getIfPresent(userId);
        if (emitters == null || emitters.isEmpty()) {
            return;
        }

        String pingData;
        try {
            pingData = objectMapper.writeValueAsString(Map.of("timestamp", DateUtil.now()));
        } catch (Exception e) {
            log.warn("Failed to serialize heartbeat data: userId={}, error={}", userId, e.getMessage());
            return;
        }

        List<SseEmitter> emittersCopy = List.copyOf(emitters);
        for (SseEmitter emitter : emittersCopy) {
            try {
                emitter.send(SseEmitter.event().name("ping").data(pingData));
            } catch (IOException e) {
                removeEmitter(userId, emitter);
            }
        }
    }

    @Override
    public void sendPingToAll() {
        String pingData;
        try {
            pingData = objectMapper.writeValueAsString(Map.of("timestamp", DateUtil.now()));
        } catch (Exception e) {
            log.warn("Failed to serialize heartbeat data: {}", e.getMessage());
            return;
        }

        // Get all user IDs and iterate - Caffeine's asMap() provides a weakly
        // consistent view
        userEmitters.asMap().forEach((userId, emitters) -> {
            List<SseEmitter> emittersCopy = List.copyOf(emitters);
            for (SseEmitter emitter : emittersCopy) {
                try {
                    emitter.send(SseEmitter.event().name("ping").data(pingData));
                } catch (IOException e) {
                    removeEmitter(userId, emitter);
                }
            }
        });
    }

    @Override
    public void pushRiskCleared(Long userId, RiskClearedPayload payload) {
        CopyOnWriteArrayList<SseEmitter> emitters = userEmitters.getIfPresent(userId);
        if (emitters == null || emitters.isEmpty()) {
            return;
        }

        RiskAlertSSEEvent<RiskClearedPayload> event = RiskAlertSSEEvent.of("risk_cleared", payload);
        List<SseEmitter> emittersCopy = List.copyOf(emitters);

        for (SseEmitter emitter : emittersCopy) {
            try {
                String data = objectMapper.writeValueAsString(event);
                emitter.send(SseEmitter.event().name("risk_cleared").id(event.getMessageId()).data(data));
            } catch (IOException e) {
                removeEmitter(userId, emitter);
            }
        }
    }

    @Override
    public void pushAlertCleared(Long userId, AlertClearedPayload payload) {
        CopyOnWriteArrayList<SseEmitter> emitters = userEmitters.getIfPresent(userId);
        if (emitters == null || emitters.isEmpty()) {
            return;
        }

        RiskAlertSSEEvent<AlertClearedPayload> event = RiskAlertSSEEvent.of("alert_cleared", payload);
        List<SseEmitter> emittersCopy = List.copyOf(emitters);

        for (SseEmitter emitter : emittersCopy) {
            try {
                String data = objectMapper.writeValueAsString(event);
                emitter.send(SseEmitter.event().name("alert_cleared").id(event.getMessageId()).data(data));
            } catch (IOException e) {
                removeEmitter(userId, emitter);
            }
        }
    }

    @Override
    public void sendInit(Long userId, InitPayload payload) {
        CopyOnWriteArrayList<SseEmitter> emitters = userEmitters.getIfPresent(userId);
        if (emitters == null || emitters.isEmpty()) {
            log.debug("No emitters for userId={} during sendInit", userId);
            return;
        }

        RiskAlertSSEEvent<InitPayload> event = RiskAlertSSEEvent.of("init", payload);
        List<SseEmitter> emittersCopy = List.copyOf(emitters);

        for (SseEmitter emitter : emittersCopy) {
            try {
                String data = objectMapper.writeValueAsString(event);
                emitter.send(SseEmitter.event().name("init").id(event.getMessageId()).data(data));
            } catch (IOException e) {
                removeEmitter(userId, emitter);
            }
        }
        log.info("Sent init event to userId={}, emitterCount={}, payload={}", userId, emitters.size(), payload);
    }

    @Override
    public int getOnlineUserCount() {
        return (int) userEmitters.estimatedSize();
    }

    @Override
    public int getUserConnectionCount(Long userId) {
        CopyOnWriteArrayList<SseEmitter> emitters = userEmitters.getIfPresent(userId);
        return emitters != null ? emitters.size() : 0;
    }

    /**
     * Get cache statistics for monitoring
     */
    public CacheStats getCacheStats() {
        return CacheStats.builder().estimatedSize(userEmitters.estimatedSize()).hitCount(0) // Caffeine doesn't expose
                                                                                            // hit count by default
                                                                                            // without recording enabled
                .missCount(0).evictionCount(0).build();
    }

    @lombok.Builder
    @lombok.Data
    public static class CacheStats {
        private long estimatedSize;
        private long hitCount;
        private long missCount;
        private long evictionCount;
    }
}