package com.stock.fund.interfaces.controller.riskalert;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.stock.fund.application.service.riskalert.RiskAlertAppService;
import com.stock.fund.application.service.riskalert.dto.BatchSubscribeRequest;
import com.stock.fund.application.service.riskalert.dto.RiskAlertMergeDTO;
import com.stock.fund.application.service.riskalert.dto.RiskAlertSummaryDTO;
import com.stock.fund.application.service.subscription.SubscriptionAppService;
import com.stock.fund.application.service.subscription.dto.BatchCreateSubscriptionRequest;
import com.stock.fund.application.service.subscription.dto.BatchCreateSubscriptionResponse;
import com.stock.fund.interfaces.dto.response.ApiResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/risk-alerts")
@Tag(name = "风险提醒", description = "风险提醒相关接口")
public class RiskAlertController {

    @Autowired
    private RiskAlertAppService riskAlertAppService;

    @Autowired
    private SubscriptionAppService subscriptionAppService;

    /**
     * 获取用户风险提醒列表（合并后的数据）
     */
    @GetMapping("/user/{userId}")
    public ApiResponse<List<RiskAlertMergeDTO>> getUserRiskAlerts(@PathVariable Long userId,
            @RequestParam(required = false) Long cursor, @RequestParam(defaultValue = "20") int limit) {
        List<RiskAlertMergeDTO> alerts = riskAlertAppService.getMergedRiskAlerts(userId, cursor, limit);
        return ApiResponse.success("获取风险提醒列表成功", alerts);
    }

    /**
     * 获取今日风险提醒（按日期分组，每组默认只显示靠后的那条）
     */
    @GetMapping("/user/{userId}/today")
    public ApiResponse<List<RiskAlertSummaryDTO>> getTodayRiskAlerts(@PathVariable Long userId) {
        List<RiskAlertSummaryDTO> alerts = riskAlertAppService.getTodayRiskAlerts(userId);
        return ApiResponse.success("获取今日风险提醒成功", alerts);
    }

    /**
     * 获取用户未读风险提醒数量
     */
    @GetMapping("/user/{userId}/unread-count")
    public ApiResponse<Map<String, Object>> getUnreadCount(@PathVariable Long userId) {
        long count = riskAlertAppService.getUnreadCount(userId);
        Map<String, Object> result = Map.of("total", count, "types", Map.of("RISK_ALERT", count));
        return ApiResponse.success(result);
    }

    /**
     * 标记单条风险提醒为已读
     */
    @PatchMapping("/{id}/read")
    public ApiResponse<String> markAsRead(@PathVariable Long id) {
        riskAlertAppService.markAsRead(id);
        return ApiResponse.success("标记已读成功");
    }

    /**
     * 标记用户所有风险提醒为已读
     */
    @PostMapping("/user/{userId}/mark-read")
    public ApiResponse<String> markAllAsRead(@PathVariable Long userId) {
        riskAlertAppService.markAllAsRead(userId);
        return ApiResponse.success("标记已读成功");
    }

    /**
     * 获取用户当天风险提醒数量
     */
    @GetMapping("/user/{userId}/today-count")
    public ApiResponse<Map<String, Object>> getTodayRiskAlertCount(@PathVariable Long userId) {
        int count = riskAlertAppService.getTodayRiskAlertCount(userId);
        return ApiResponse.success(Map.of("total", count));
    }

    /**
     * 手动触发风险提醒检测（用于测试）
     */
    @PostMapping("/check")
    public ApiResponse<String> checkRiskAlerts() {
        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        String timePoint = now.getHour() < 12 ? "11:30" : "14:30";
        riskAlertAppService.checkAndCreateRiskAlerts(timePoint);
        return ApiResponse.success("风险检测完成, timePoint=" + timePoint);
    }

    /**
     * 删除风险提醒
     */
    @DeleteMapping("/{id}")
    public ApiResponse<String> deleteRiskAlert(@PathVariable Long id) {
        riskAlertAppService.deleteById(id);
        return ApiResponse.success("删除成功");
    }

    /**
     * 批量订阅标的（批量创建订阅以启用风险提醒）
     */
    @Operation(summary = "批量订阅标的", description = "批量创建订阅以启用风险提醒监控")
    @PostMapping("/subscribe")
    public ApiResponse<BatchCreateSubscriptionResponse> batchSubscribe(@RequestBody BatchSubscribeRequest request) {
        Double targetChangePercent = request.getTargetChangePercent();
        if (targetChangePercent == null) {
            targetChangePercent = 1.0;
        }

        BatchCreateSubscriptionRequest createRequest = new BatchCreateSubscriptionRequest();
        createRequest.setUserId(request.getUserId());
        createRequest.setSymbolType(request.getSymbolType());
        createRequest.setSymbols(request.getSymbols());
        createRequest.setTargetChangePercent(BigDecimal.valueOf(targetChangePercent));
        createRequest.setEnabled(true);

        BatchCreateSubscriptionResponse response = subscriptionAppService.batchCreateSubscription(createRequest);
        return ApiResponse.success("批量订阅完成", response);
    }
}
