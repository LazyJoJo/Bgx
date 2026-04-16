package com.stock.fund.interfaces.controller.subscription;

import com.stock.fund.application.service.subscription.SubscriptionAppService;
import com.stock.fund.application.service.subscription.dto.*;
import com.stock.fund.domain.entity.subscription.UserSubscription;
import com.stock.fund.interfaces.dto.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

/**
 * 订阅接口
 */
@Tag(name = "订阅管理", description = "用户订阅提醒相关接口")
@RestController
@RequestMapping("/api/subscriptions")
public class SubscriptionController {

    @Autowired
    private SubscriptionAppService subscriptionAppService;

    @Operation(summary = "创建订阅", description = "创建新的订阅，如果同一标的已存在则返回已有订阅")
    @PostMapping
    public ApiResponse<CreateSubscriptionResponse> createSubscription(@RequestBody CreateSubscriptionRequest request) {
        try {
            CreateSubscriptionResponse response = subscriptionAppService.createSubscription(request);
            return ApiResponse.success(response.getMessage(), response);
        } catch (Exception e) {
            return ApiResponse.error("创建订阅失败: " + e.getMessage());
        }
    }

    @Operation(summary = "批量创建订阅", description = "批量创建订阅")
    @PostMapping("/batch")
    public ApiResponse<BatchCreateSubscriptionResponse> batchCreateSubscription(@RequestBody BatchCreateSubscriptionRequest request) {
        try {
            BatchCreateSubscriptionResponse response = subscriptionAppService.batchCreateSubscription(request);
            return ApiResponse.success("批量创建完成", response);
        } catch (IllegalArgumentException e) {
            return ApiResponse.error("参数错误: " + e.getMessage());
        } catch (Exception e) {
            return ApiResponse.error("批量创建订阅失败: " + e.getMessage());
        }
    }

    @Operation(summary = "批量删除订阅", description = "批量删除订阅")
    @DeleteMapping("/batch")
    public ApiResponse<String> batchDeleteSubscription(@RequestBody BatchSubscriptionIdsRequest request) {
        try {
            subscriptionAppService.batchDeleteSubscription(request.getIds());
            return ApiResponse.success("批量删除完成");
        } catch (Exception e) {
            return ApiResponse.error("批量删除订阅失败: " + e.getMessage());
        }
    }

    @Operation(summary = "批量启用订阅", description = "批量启用订阅")
    @PatchMapping("/batch/activate")
    public ApiResponse<String> batchActivateSubscription(@RequestBody BatchSubscriptionIdsRequest request) {
        try {
            subscriptionAppService.batchActivateSubscription(request.getIds());
            return ApiResponse.success("批量启用完成");
        } catch (Exception e) {
            return ApiResponse.error("批量启用订阅失败: " + e.getMessage());
        }
    }

    @Operation(summary = "批量停用订阅", description = "批量停用订阅")
    @PatchMapping("/batch/deactivate")
    public ApiResponse<String> batchDeactivateSubscription(@RequestBody BatchSubscriptionIdsRequest request) {
        try {
            subscriptionAppService.batchDeactivateSubscription(request.getIds());
            return ApiResponse.success("批量停用完成");
        } catch (Exception e) {
            return ApiResponse.error("批量停用订阅失败: " + e.getMessage());
        }
    }

    @Operation(summary = "检测重复订阅", description = "检测哪些标的已存在订阅")
    @PostMapping("/check-duplicates")
    public ApiResponse<CheckDuplicatesResponse> checkDuplicates(@Valid @RequestBody CheckDuplicatesRequest request) {
        try {
            CheckDuplicatesResponse response = subscriptionAppService.checkDuplicates(request);
            return ApiResponse.success("检测完成", response);
        } catch (IllegalArgumentException e) {
            return ApiResponse.error("参数错误: " + e.getMessage());
        } catch (Exception e) {
            return ApiResponse.error("检测失败: " + e.getMessage());
        }
    }

    @Operation(summary = "更新订阅", description = "更新已有订阅的参数")
    @PutMapping("/{id}")
    public ApiResponse<UserSubscription> updateSubscription(@PathVariable Long id, @RequestBody UpdateSubscriptionRequest request) {
        try {
            UserSubscription updatedSubscription = subscriptionAppService.updateSubscription(id, request);
            return ApiResponse.success("订阅更新成功", updatedSubscription);
        } catch (IllegalArgumentException e) {
            return ApiResponse.error("订阅不存在: " + e.getMessage());
        } catch (Exception e) {
            return ApiResponse.error("更新订阅失败: " + e.getMessage());
        }
    }

    @Operation(summary = "删除订阅", description = "删除指定的订阅")
    @DeleteMapping("/{id}")
    public ApiResponse<String> deleteSubscription(@PathVariable Long id) {
        try {
            subscriptionAppService.deleteSubscription(id);
            return ApiResponse.success("订阅删除成功");
        } catch (Exception e) {
            return ApiResponse.error("删除订阅失败: " + e.getMessage());
        }
    }

    @Operation(summary = "查询订阅列表", description = "分页查询用户的订阅列表，支持筛选和搜索")
    @GetMapping
    public ApiResponse<SubscriptionPageResponse<UserSubscription>> querySubscriptions(SubscriptionQueryDTO query) {
        try {
            SubscriptionPageResponse<UserSubscription> response = subscriptionAppService.querySubscriptions(query);
            return ApiResponse.success("查询成功", response);
        } catch (Exception e) {
            return ApiResponse.error("查询订阅列表失败: " + e.getMessage());
        }
    }

    @Operation(summary = "获取单个订阅", description = "根据ID获取订阅详情")
    @GetMapping("/{id}")
    public ApiResponse<UserSubscription> getSubscriptionById(@PathVariable Long id) {
        try {
            return subscriptionAppService.getSubscriptionById(id)
                    .map(subscription -> ApiResponse.success("获取成功", subscription))
                    .orElse(ApiResponse.error("订阅不存在"));
        } catch (Exception e) {
            return ApiResponse.error("获取订阅失败: " + e.getMessage());
        }
    }

    @Operation(summary = "获取用户所有订阅", description = "获取指定用户的所有订阅")
    @GetMapping("/user/{userId}")
    public ApiResponse<List<UserSubscription>> getUserSubscriptions(@PathVariable Long userId) {
        try {
            List<UserSubscription> subscriptions = subscriptionAppService.getUserSubscriptions(userId);
            return ApiResponse.success("获取用户订阅列表成功", subscriptions);
        } catch (Exception e) {
            return ApiResponse.error("获取用户订阅列表失败: " + e.getMessage());
        }
    }

    @Operation(summary = "启用订阅", description = "激活指定的订阅")
    @PatchMapping("/{id}/activate")
    public ApiResponse<String> activateSubscription(@PathVariable Long id) {
        try {
            subscriptionAppService.activateSubscription(id);
            return ApiResponse.success("订阅激活成功");
        } catch (Exception e) {
            return ApiResponse.error("订阅激活失败: " + e.getMessage());
        }
    }

    @Operation(summary = "停用订阅", description = "停用指定的订阅")
    @PatchMapping("/{id}/deactivate")
    public ApiResponse<String> deactivateSubscription(@PathVariable Long id) {
        try {
            subscriptionAppService.deactivateSubscription(id);
            return ApiResponse.success("订阅停用成功");
        } catch (Exception e) {
            return ApiResponse.error("订阅停用失败: " + e.getMessage());
        }
    }
}