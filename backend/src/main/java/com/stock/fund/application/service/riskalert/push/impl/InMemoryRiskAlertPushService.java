package com.stock.fund.application.service.riskalert.push.impl;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stock.fund.application.service.riskalert.push.RiskAlertPushService;
import com.stock.fund.application.service.riskalert.push.dto.RiskAlertSSEEvent;
import com.stock.fund.application.service.riskalert.push.dto.RiskClearedPayload;
import com.stock.fund.application.service.riskalert.push.dto.UnreadCountPayload;

import cn.hutool.core.date.DateUtil;
import lombok.RequiredArgsConstructor;

/**
 * 内存版风险提醒 SSE 推送服务实现 使用 ConcurrentHashMap 管理用户的 SSE 连接
 */
@Service
@RequiredArgsConstructor
public class InMemoryRiskAlertPushService implements RiskAlertPushService {

    private static final Logger log = LoggerFactory.getLogger(InMemoryRiskAlertPushService.class);

    // userId -> all SseEmitter connections for this user
    private final Map<Long, CopyOnWriteArrayList<SseEmitter>> userEmitters = new ConcurrentHashMap<>();

    private final ObjectMapper objectMapper;

    @Override
    public void register(Long userId, SseEmitter emitter) {
        CopyOnWriteArrayList<SseEmitter> emitters = userEmitters.computeIfAbsent(userId,
                k -> new CopyOnWriteArrayList<>());

        // AC-7: Connection limit control (FIFO eviction)
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

        // Auto-cleanup on connection close/timeout/error
        emitter.onCompletion(() -> removeEmitter(userId, emitter));
        emitter.onTimeout(() -> removeEmitter(userId, emitter));
        emitter.onError((e) -> removeEmitter(userId, emitter));

        log.info("SSE connection registered: userId={}, current connection count={}", userId, emitters.size());
    }

    private void removeEmitter(Long userId, SseEmitter emitter) {
        CopyOnWriteArrayList<SseEmitter> emitters = userEmitters.get(userId);
        if (emitters != null) {
            emitters.remove(emitter);
            if (emitters.isEmpty()) {
                userEmitters.remove(userId);
            }
            log.info("SSE connection removed: userId={}, remaining connection count={}", userId,
                    userEmitters.getOrDefault(userId, new CopyOnWriteArrayList<>()).size());
        }
    }

    @Override
    public void pushNewAlert(Long userId, Object payload) {
        List<SseEmitter> emitters = userEmitters.getOrDefault(userId, new CopyOnWriteArrayList<>());
        log.info("[SSE Push] pushNewAlert called for userId={}, emitter count={}", userId, emitters.size());
        RiskAlertSSEEvent<Object> event = RiskAlertSSEEvent.of("new_alert", payload);

        for (SseEmitter emitter : emitters) {
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
        if (emitters.isEmpty()) {
            log.warn("[SSE Push] No emitters found for userId={}, event not sent!", userId);
        }
    }

    @Override
    public void pushUnreadCountChange(Long userId, UnreadCountPayload payload) {
        List<SseEmitter> emitters = userEmitters.getOrDefault(userId, new CopyOnWriteArrayList<>());
        RiskAlertSSEEvent<UnreadCountPayload> event = RiskAlertSSEEvent.of("unread_count_change", payload);

        for (SseEmitter emitter : emitters) {
            try {
                String data = objectMapper.writeValueAsString(event);
                emitter.send(SseEmitter.event().name("unread_count_change").id(event.getMessageId()).data(data));
            } catch (IOException e) {
                removeEmitter(userId, emitter);
            }
        }
    }

    @Override
    public void sendPing(Long userId) {
        List<SseEmitter> emitters = userEmitters.getOrDefault(userId, new CopyOnWriteArrayList<>());
        String pingData;
        try {
            pingData = objectMapper.writeValueAsString(Map.of("timestamp", DateUtil.now()));
        } catch (Exception e) {
            log.warn("Failed to serialize heartbeat data: userId={}, error={}", userId, e.getMessage());
            return;
        }

        for (SseEmitter emitter : emitters) {
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
        userEmitters.forEach((userId, emitters) -> {
            for (SseEmitter emitter : emitters) {
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
        List<SseEmitter> emitters = userEmitters.getOrDefault(userId, new CopyOnWriteArrayList<>());
        RiskAlertSSEEvent<RiskClearedPayload> event = RiskAlertSSEEvent.of("risk_cleared", payload);

        for (SseEmitter emitter : emitters) {
            try {
                String data = objectMapper.writeValueAsString(event);
                emitter.send(SseEmitter.event().name("risk_cleared").id(event.getMessageId()).data(data));
            } catch (IOException e) {
                removeEmitter(userId, emitter);
            }
        }
    }

    @Override
    public int getOnlineUserCount() {
        return userEmitters.size();
    }

    @Override
    public int getUserConnectionCount(Long userId) {
        CopyOnWriteArrayList<SseEmitter> emitters = userEmitters.get(userId);
        return emitters != null ? emitters.size() : 0;
    }
}