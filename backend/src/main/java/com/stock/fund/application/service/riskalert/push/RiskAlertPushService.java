package com.stock.fund.application.service.riskalert.push;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.stock.fund.application.service.riskalert.push.dto.RiskClearedPayload;
import com.stock.fund.application.service.riskalert.push.dto.UnreadCountPayload;

/**
 * 风险提醒 SSE 推送服务接口
 */
public interface RiskAlertPushService {

    /** 单个用户最大 SSE 连接数（对应 AC-7） */
    int MAX_CONNECTIONS_PER_USER = 3;

    /**
     * 注册用户的 SSE 连接（允许多连接，最多 MAX_CONNECTIONS_PER_USER 条） 当连接数超过上限时，按 FIFO 移除最早的连接
     */
    void register(Long userId, SseEmitter emitter);

    /**
     * 向指定用户的所有活跃连接广播新风险提醒
     */
    void pushNewAlert(Long userId, Object payload);

    /**
     * 向指定用户的所有活跃连接广播未读数变化
     */
    void pushUnreadCountChange(Long userId, UnreadCountPayload payload);

    /**
     * 向指定用户的所有活跃连接广播风险消除事件
     */
    void pushRiskCleared(Long userId, RiskClearedPayload payload);

    /**
     * 向指定用户的所有活跃连接发送心跳
     */
    void sendPing(Long userId);

    /**
     * 向所有用户的所有活跃连接发送心跳（全局心跳任务调用）
     */
    void sendPingToAll();

    /**
     * 获取当前在线用户数（仅用于监控）
     */
    default int getOnlineUserCount() {
        return 0;
    }

    /**
     * 获取指定用户的活跃连接数（仅用于监控）
     */
    default int getUserConnectionCount(Long userId) {
        return 0;
    }
}