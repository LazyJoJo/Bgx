package com.stock.fund.interfaces.controller.alert;

import com.stock.fund.application.service.alert.AlertAppService;
import com.stock.fund.domain.entity.alert.PriceAlert;
import com.stock.fund.domain.entity.alert.AlertHistory;
import com.stock.fund.interfaces.dto.response.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/alerts")
public class AlertController {
    
    @Autowired
    private AlertAppService alertAppService;

    @PostMapping
    public ApiResponse<PriceAlert> createAlert(@RequestBody PriceAlert alert) {
        try {
            PriceAlert createdAlert = alertAppService.createAlert(alert);
            return ApiResponse.success("提醒创建成功", createdAlert);
        } catch (Exception e) {
            return ApiResponse.error("创建提醒失败: " + e.getMessage());
        }
    }
    
    @PutMapping("/{id}")
    public ApiResponse<PriceAlert> updateAlert(@PathVariable Long id, @RequestBody PriceAlert alert) {
        try {
            PriceAlert updatedAlert = alertAppService.updateAlert(id, alert);
            return ApiResponse.success("提醒更新成功", updatedAlert);
        } catch (Exception e) {
            return ApiResponse.error("更新提醒失败: " + e.getMessage());
        }
    }
    
    @DeleteMapping("/{id}")
    public ApiResponse<String> deleteAlert(@PathVariable Long id) {
        try {
            alertAppService.deleteAlert(id);
            return ApiResponse.success("提醒删除成功");
        } catch (Exception e) {
            return ApiResponse.error("删除提醒失败: " + e.getMessage());
        }
    }
    
    @GetMapping("/user/{userId}")
    public ApiResponse<List<PriceAlert>> getUserAlerts(@PathVariable Long userId) {
        try {
            List<PriceAlert> alerts = alertAppService.getUserAlerts(userId);
            return ApiResponse.success("获取用户提醒列表成功", alerts);
        } catch (Exception e) {
            return ApiResponse.error("获取用户提醒列表失败: " + e.getMessage());
        }
    }
    
    @GetMapping("/user/{userId}/active")
    public ApiResponse<List<PriceAlert>> getUserActiveAlerts(@PathVariable Long userId) {
        try {
            List<PriceAlert> alerts = alertAppService.getUserActiveAlerts(userId);
            return ApiResponse.success("获取用户激活提醒列表成功", alerts);
        } catch (Exception e) {
            return ApiResponse.error("获取用户激活提醒列表失败: " + e.getMessage());
        }
    }
    
    @PostMapping("/{id}/activate")
    public ApiResponse<String> activateAlert(@PathVariable Long id) {
        try {
            alertAppService.activateAlert(id);
            return ApiResponse.success("提醒激活成功");
        } catch (Exception e) {
            return ApiResponse.error("提醒激活失败: " + e.getMessage());
        }
    }
    
    @PostMapping("/{id}/deactivate")
    public ApiResponse<String> deactivateAlert(@PathVariable Long id) {
        try {
            alertAppService.deactivateAlert(id);
            return ApiResponse.success("提醒成功");
        } catch (Exception e) {
            return ApiResponse.error("提醒失败: " + e.getMessage());
        }
    }
    
    @GetMapping("/user/{userId}/history")
    public ApiResponse<List<AlertHistory>> getAllAlertHistory(@PathVariable Long userId) {
        try {
            List<AlertHistory> histories = alertAppService.getAllAlertHistory(userId);
            return ApiResponse.success("获取用户提醒历史成功", histories);
        } catch (Exception e) {
            return ApiResponse.error("获取用户提醒历史失败: " + e.getMessage());
        }
    }
    
    @GetMapping("/user/{userId}/alert/{alertId}/history")
    public ApiResponse<List<AlertHistory>> getAlertHistory(@PathVariable Long userId, @PathVariable Long alertId) {
        try {
            List<AlertHistory> histories = alertAppService.getAlertHistory(userId, alertId);
            return ApiResponse.success("获取提醒历史成功", histories);
        } catch (Exception e) {
            return ApiResponse.error("获取提醒历史失败: " + e.getMessage());
        }
    }
    
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