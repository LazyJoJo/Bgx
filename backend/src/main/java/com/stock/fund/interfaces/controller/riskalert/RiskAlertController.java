package com.stock.fund.interfaces.controller.riskalert;

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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/risk-alerts")
@Tag(name = "风险提醒", description = "风险提醒相关接口")
public class RiskAlertController {

    @Autowired
    private RiskAlertAppService riskAlertAppService;

    @Autowired
    private SubscriptionAppService subscriptionAppService;

    // 默认用户ID（后续可扩展为从认证获取）
    private Long getUserId() {
        return 1L;
    }

    /**
     * 获取用户风险提醒列表（合并后的数据）
     * @param cursor 游标（时间戳毫秒），用于分页
     * @param limit 每页大小，默认20
     */
    @GetMapping("/user/{userId}")
    public ApiResponse<List<RiskAlertMergeDTO>> getUserRiskAlerts(
            @PathVariable Long userId,
            @RequestParam(required = false) Long cursor,
            @RequestParam(defaultValue = "20") int limit) {
        try {
            List<RiskAlertMergeDTO> alerts = riskAlertAppService.getMergedRiskAlerts(userId, cursor, limit);
            return ApiResponse.success("获取风险提醒列表成功", alerts);
        } catch (Exception e) {
            return ApiResponse.error("获取风险提醒列表失败: " + e.getMessage());
        }
    }

    /**
     * 获取今日风险提醒（按日期分组，每组默认只显示靠后的那条）
     */
    @GetMapping("/today")
    public ApiResponse<List<RiskAlertSummaryDTO>> getTodayRiskAlerts() {
        try {
            Long userId = getUserId();
            List<RiskAlertSummaryDTO> alerts = riskAlertAppService.getTodayRiskAlerts(userId);
            return ApiResponse.success("获取今日风险提醒成功", alerts);
        } catch (Exception e) {
            return ApiResponse.error("获取今日风险提醒失败: " + e.getMessage());
        }
    }

    /**
     * 获取用户未读风险提醒数量
     */
    @GetMapping("/user/{userId}/unread-count")
    public ApiResponse<Map<String, Object>> getUnreadCount(@PathVariable Long userId) {
        try {
            long count = riskAlertAppService.getUnreadCount(userId);
            Map<String, Object> result = Map.of(
                    "total", count,
                    "types", Map.of("RISK_ALERT", count)
            );
            return ApiResponse.success(result);
        } catch (Exception e) {
            return ApiResponse.error("获取未读数量失败: " + e.getMessage());
        }
    }

    /**
     * 标记单条风险提醒为已读
     */
    @PatchMapping("/{id}/read")
    public ApiResponse<String> markAsRead(@PathVariable Long id) {
        try {
            riskAlertAppService.markAsRead(id);
            return ApiResponse.success("标记已读成功");
        } catch (Exception e) {
            return ApiResponse.error("标记已读失败: " + e.getMessage());
        }
    }

    /**
     * 标记用户所有风险提醒为已读
     */
    @PostMapping("/user/{userId}/mark-read")
    public ApiResponse<String> markAllAsRead(@PathVariable Long userId) {
        try {
            riskAlertAppService.markAllAsRead(userId);
            return ApiResponse.success("标记已读成功");
        } catch (Exception e) {
            return ApiResponse.error("标记已读失败: " + e.getMessage());
        }
    }

    /**
     * 获取用户当天风险提醒数量（用于仪表盘显示，与已读/未读无关）
     * 一个股票/基金算一条，即使存在多个触发记录也只算一条
     */
    @GetMapping("/user/{userId}/today-count")
    public ApiResponse<Map<String, Object>> getTodayRiskAlertCount(@PathVariable Long userId) {
        try {
            int count = riskAlertAppService.getTodayRiskAlertCount(userId);
            return ApiResponse.success(Map.of("total", count));
        } catch (Exception e) {
            return ApiResponse.error("获取当天风险提醒数量失败: " + e.getMessage());
        }
    }

    /**
     * 手动触发风险提醒检测（用于测试）
     * 根据当前时间自动判断是11:30还是14:30
     */
    @PostMapping("/check")
    public ApiResponse<String> checkRiskAlerts() {
        try {
            // 根据当前时间判断时间点
            java.time.LocalDateTime now = java.time.LocalDateTime.now();
            String timePoint = now.getHour() < 12 ? "11:30" : "14:30";
            riskAlertAppService.checkAndCreateRiskAlerts(timePoint);
            return ApiResponse.success("风险检测完成, timePoint=" + timePoint);
        } catch (Exception e) {
            return ApiResponse.error("风险检测失败: " + e.getMessage());
        }
    }

    /**
     * 删除风险提醒（测试用，清除假数据）
     */
    @DeleteMapping("/{id}")
    public ApiResponse<String> deleteRiskAlert(@PathVariable Long id) {
        try {
            riskAlertAppService.deleteById(id);
            return ApiResponse.success("删除成功");
        } catch (Exception e) {
            return ApiResponse.error("删除失败: " + e.getMessage());
        }
    }

    /**
     * 批量订阅标的（批量创建订阅以启用风险提醒）
     * 用户选择股票或基金类型后，批量创建订阅，系统会自动检测并生成风险提醒
     */
    @Operation(summary = "批量订阅标的", description = "批量创建订阅以启用风险提醒监控")
    @PostMapping("/subscribe")
    public ApiResponse<BatchCreateSubscriptionResponse> batchSubscribe(@RequestBody BatchSubscribeRequest request) {
        try {
            // 设置默认值：涨跌幅监控
            Double targetChangePercent = request.getTargetChangePercent();
            if (targetChangePercent == null) {
                targetChangePercent = 1.0; // 默认1%涨跌幅
            }

            // 转换为订阅的批量创建请求
            BatchCreateSubscriptionRequest createRequest = new BatchCreateSubscriptionRequest();
            createRequest.setUserId(request.getUserId());
            createRequest.setSymbolType(request.getSymbolType());
            createRequest.setSymbols(request.getSymbols());
            createRequest.setTargetChangePercent(BigDecimal.valueOf(targetChangePercent));
            createRequest.setEnabled(true);

            // 调用订阅服务的批量创建方法
            BatchCreateSubscriptionResponse response = subscriptionAppService.batchCreateSubscription(createRequest);
            return ApiResponse.success("批量订阅完成", response);
        } catch (Exception e) {
            return ApiResponse.error("批量订阅失败: " + e.getMessage());
        }
    }
}