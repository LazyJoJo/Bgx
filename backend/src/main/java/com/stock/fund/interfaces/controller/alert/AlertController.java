package com.stock.fund.interfaces.controller.alert;

import com.stock.fund.application.service.alert.AlertAppService;
import com.stock.fund.application.service.alert.dto.*;
import com.stock.fund.domain.entity.alert.PriceAlert;
import com.stock.fund.domain.entity.alert.AlertHistory;
import com.stock.fund.interfaces.dto.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 提醒接口
 */
@Tag(name = "价格提醒", description = "价格提醒相关接口")
@RestController
@RequestMapping("/api/alerts")
public class AlertController {

    @Autowired
    private AlertAppService alertAppService;

    @Operation(summary = "创建提醒", description = "创建新的价格提醒，如果同一标的已存在则返回已有提醒")
    @PostMapping
    public ApiResponse<CreateAlertResponse> createAlert(@RequestBody CreateAlertRequest request) {
        try {
            CreateAlertResponse response = alertAppService.createAlert(request);
            return ApiResponse.success(response.getMessage(), response);
        } catch (Exception e) {
            return ApiResponse.error("创建提醒失败: " + e.getMessage());
        }
    }

    @Operation(summary = "批量创建提醒", description = "批量创建价格提醒")
    @PostMapping("/batch")
    public ApiResponse<BatchCreateAlertResponse> batchCreateAlert(@RequestBody BatchCreateAlertRequest request) {
        try {
            BatchCreateAlertResponse response = alertAppService.batchCreateAlert(request);
            return ApiResponse.success("批量创建完成", response);
        } catch (Exception e) {
            return ApiResponse.error("批量创建提醒失败: " + e.getMessage());
        }
    }

    @Operation(summary = "更新提醒", description = "更新已有提醒的参数")
    @PutMapping("/{id}")
    public ApiResponse<PriceAlert> updateAlert(@PathVariable Long id, @RequestBody UpdateAlertRequest request) {
        try {
            PriceAlert updatedAlert = alertAppService.updateAlert(id, request);
            return ApiResponse.success("提醒更新成功", updatedAlert);
        } catch (Exception e) {
            return ApiResponse.error("更新提醒失败: " + e.getMessage());
        }
    }

    @Operation(summary = "删除提醒", description = "删除指定的提醒")
    @DeleteMapping("/{id}")
    public ApiResponse<String> deleteAlert(@PathVariable Long id) {
        try {
            alertAppService.deleteAlert(id);
            return ApiResponse.success("提醒删除成功");
        } catch (Exception e) {
            return ApiResponse.error("删除提醒失败: " + e.getMessage());
        }
    }

    @Operation(summary = "查询提醒列表", description = "分页查询用户的提醒列表，支持筛选和搜索")
    @GetMapping
    public ApiResponse<AlertPageResponse<PriceAlert>> queryAlerts(AlertQueryDTO query) {
        try {
            AlertPageResponse<PriceAlert> response = alertAppService.queryAlerts(query);
            return ApiResponse.success("查询成功", response);
        } catch (Exception e) {
            return ApiResponse.error("查询提醒列表失败: " + e.getMessage());
        }
    }

    @Operation(summary = "获取单个提醒", description = "根据ID获取提醒详情")
    @GetMapping("/{id}")
    public ApiResponse<PriceAlert> getAlertById(@PathVariable Long id) {
        try {
            return alertAppService.getAlertById(id)
                    .map(alert -> ApiResponse.success("获取成功", alert))
                    .orElse(ApiResponse.error("提醒不存在"));
        } catch (Exception e) {
            return ApiResponse.error("获取提醒失败: " + e.getMessage());
        }
    }

    @Operation(summary = "获取用户所有提醒", description = "获取指定用户的所有提醒")
    @GetMapping("/user/{userId}")
    public ApiResponse<List<PriceAlert>> getUserAlerts(@PathVariable Long userId) {
        try {
            List<PriceAlert> alerts = alertAppService.getUserAlerts(userId);
            return ApiResponse.success("获取用户提醒列表成功", alerts);
        } catch (Exception e) {
            return ApiResponse.error("获取用户提醒列表失败: " + e.getMessage());
        }
    }

    @Operation(summary = "获取用户激活的提醒", description = "获取指定用户已激活的提醒")
    @GetMapping("/user/{userId}/active")
    public ApiResponse<List<PriceAlert>> getUserActiveAlerts(@PathVariable Long userId) {
        try {
            List<PriceAlert> alerts = alertAppService.getUserActiveAlerts(userId);
            return ApiResponse.success("获取用户激活提醒列表成功", alerts);
        } catch (Exception e) {
            return ApiResponse.error("获取用户激活提醒列表失败: " + e.getMessage());
        }
    }

    @Operation(summary = "启用提醒", description = "激活指定的提醒")
    @PatchMapping("/{id}/activate")
    public ApiResponse<String> activateAlert(@PathVariable Long id) {
        try {
            alertAppService.activateAlert(id);
            return ApiResponse.success("提醒激活成功");
        } catch (Exception e) {
            return ApiResponse.error("提醒激活失败: " + e.getMessage());
        }
    }

    @Operation(summary = "禁用提醒", description = "禁用指定的提醒")
    @PatchMapping("/{id}/deactivate")
    public ApiResponse<String> deactivateAlert(@PathVariable Long id) {
        try {
            alertAppService.deactivateAlert(id);
            return ApiResponse.success("提醒禁用成功");
        } catch (Exception e) {
            return ApiResponse.error("提醒禁用失败: " + e.getMessage());
        }
    }

    @Operation(summary = "获取用户提醒历史", description = "获取指定用户的所有提醒历史")
    @GetMapping("/user/{userId}/history")
    public ApiResponse<List<AlertHistory>> getAllAlertHistory(@PathVariable Long userId) {
        try {
            List<AlertHistory> histories = alertAppService.getAllAlertHistory(userId);
            return ApiResponse.success("获取用户提醒历史成功", histories);
        } catch (Exception e) {
            return ApiResponse.error("获取用户提醒历史失败: " + e.getMessage());
        }
    }

    @Operation(summary = "获取单条提醒历史", description = "获取指定提醒的历史记录")
    @GetMapping("/user/{userId}/alert/{alertId}/history")
    public ApiResponse<List<AlertHistory>> getAlertHistory(@PathVariable Long userId, @PathVariable Long alertId) {
        try {
            List<AlertHistory> histories = alertAppService.getAlertHistory(userId, alertId);
            return ApiResponse.success("获取提醒历史成功", histories);
        } catch (Exception e) {
            return ApiResponse.error("获取提醒历史失败: " + e.getMessage());
        }
    }

    @Operation(summary = "批量检查提醒", description = "触发批量检查所有激活的提醒")
    @PostMapping("/check-batch")
    public ApiResponse<String> checkBatchAlerts() {
        try {
            alertAppService.batchCheckAlerts();
            return ApiResponse.success("批处理提醒成功");
        } catch (Exception e) {
            return ApiResponse.error("批处理提醒失败: " + e.getMessage());
        }
    }
}
