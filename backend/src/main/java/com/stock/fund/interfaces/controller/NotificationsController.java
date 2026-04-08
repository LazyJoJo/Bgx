package com.stock.fund.interfaces.controller;

import com.stock.fund.application.service.riskalert.RiskAlertAppService;
import com.stock.fund.interfaces.dto.response.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 全局通知控制器
 * 提供所有类型通知的未读计数聚合
 */
@RestController
@RequestMapping("/api/notifications")
public class NotificationsController {

    @Autowired
    private RiskAlertAppService riskAlertAppService;

    // 默认用户ID（后续可扩展为从认证获取）
    private Long getUserId() {
        return 1L;
    }

    /**
     * 获取所有通知类型的未读计数
     * 返回结构：{ total: number, types: { RISK_ALERT: number, SYSTEM: number, ... } }
     */
    @GetMapping("/unread-count")
    public ApiResponse<Map<String, Object>> getUnreadCount() {
        try {
            Long userId = getUserId();

            // 获取风险提醒未读数
            long riskAlertCount = riskAlertAppService.getUnreadCount(userId);

            // TODO: 后续可以添加其他通知类型的计数
            // long systemCount = systemNotificationService.getUnreadCount(userId);

            Map<String, Object> types = new HashMap<>();
            types.put("RISK_ALERT", riskAlertCount);
            // types.put("SYSTEM", systemCount);

            long total = riskAlertCount; // + systemCount;

            Map<String, Object> result = Map.of(
                    "total", total,
                    "types", types
            );

            return ApiResponse.success(result);
        } catch (Exception e) {
            return ApiResponse.error("获取未读计数失败: " + e.getMessage());
        }
    }
}
