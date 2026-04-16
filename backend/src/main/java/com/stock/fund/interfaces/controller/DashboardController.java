package com.stock.fund.interfaces.controller;

import com.stock.fund.application.service.riskalert.RiskAlertAppService;
import com.stock.fund.application.service.subscription.SubscriptionAppService;
import com.stock.fund.interfaces.dto.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 仪表盘控制器
 * 提供仪表盘相关的数据聚合接口
 */
@Tag(name = "仪表盘", description = "仪表盘统计接口")
@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    @Autowired
    private RiskAlertAppService riskAlertAppService;

    @Autowired
    private SubscriptionAppService subscriptionAppService;

    // 默认用户ID（后续可扩展为从认证获取）
    private Long getUserId() {
        return 1L;
    }

    /**
     * 获取仪表盘统计数据
     * 返回用户的关键指标汇总
     */
    @Operation(summary = "获取仪表盘统计", description = "获取用户的关键指标汇总，包括订阅数、风险提醒数等")
    @GetMapping("/stats")
    public ApiResponse<Map<String, Object>> getDashboardStats() {
        try {
            Long userId = getUserId();

            // 获取用户订阅数量
            int subscriptionCount = subscriptionAppService.getUserSubscriptions(userId).size();

            // 获取用户激活的订阅数量
            int activeSubscriptionCount = subscriptionAppService.getUserActiveSubscriptions(userId).size();

            // 获取今日风险提醒数量
            int todayRiskAlertCount = riskAlertAppService.getTodayRiskAlertCount(userId);

            // 获取未读风险提醒数量
            long unreadRiskAlertCount = riskAlertAppService.getUnreadCount(userId);

            Map<String, Object> stats = new HashMap<>();
            stats.put("subscriptionCount", subscriptionCount);
            stats.put("activeSubscriptionCount", activeSubscriptionCount);
            stats.put("todayRiskAlertCount", todayRiskAlertCount);
            stats.put("unreadRiskAlertCount", unreadRiskAlertCount);

            return ApiResponse.success(stats);
        } catch (Exception e) {
            return ApiResponse.error("获取仪表盘统计失败: " + e.getMessage());
        }
    }
}