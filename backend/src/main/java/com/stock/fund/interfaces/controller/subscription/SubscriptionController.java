package com.stock.fund.interfaces.controller.subscription;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.stock.fund.application.service.subscription.SubscriptionAppService;
import com.stock.fund.application.service.subscription.dto.BatchCreateSubscriptionRequest;
import com.stock.fund.application.service.subscription.dto.BatchCreateSubscriptionResponse;
import com.stock.fund.application.service.subscription.dto.BatchSubscriptionIdsRequest;
import com.stock.fund.application.service.subscription.dto.CheckDuplicatesRequest;
import com.stock.fund.application.service.subscription.dto.CheckDuplicatesResponse;
import com.stock.fund.application.service.subscription.dto.CreateSubscriptionRequest;
import com.stock.fund.application.service.subscription.dto.CreateSubscriptionResponse;
import com.stock.fund.application.service.subscription.dto.SubscriptionPageResponse;
import com.stock.fund.application.service.subscription.dto.SubscriptionQueryDTO;
import com.stock.fund.application.service.subscription.dto.UpdateSubscriptionRequest;
import com.stock.fund.domain.entity.subscription.UserSubscription;
import com.stock.fund.interfaces.dto.response.ApiResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

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
    public ApiResponse<CreateSubscriptionResponse> createSubscription(
            @RequestBody @Valid CreateSubscriptionRequest request) {
        CreateSubscriptionResponse response = subscriptionAppService.createSubscription(request);
        return ApiResponse.success(response.getMessage(), response);
    }

    @Operation(summary = "批量创建订阅", description = "批量创建订阅")
    @PostMapping("/batch")
    public ApiResponse<BatchCreateSubscriptionResponse> batchCreateSubscription(
            @RequestBody @Valid BatchCreateSubscriptionRequest request) {
        BatchCreateSubscriptionResponse response = subscriptionAppService.batchCreateSubscription(request);
        return ApiResponse.success("批量创建完成", response);
    }

    @Operation(summary = "批量删除订阅", description = "批量删除订阅")
    @DeleteMapping("/batch")
    public ApiResponse<String> batchDeleteSubscription(@RequestBody BatchSubscriptionIdsRequest request) {
        subscriptionAppService.batchDeleteSubscription(request.getIds());
        return ApiResponse.success("批量删除完成");
    }

    @Operation(summary = "批量启用订阅", description = "批量启用订阅")
    @PatchMapping("/batch/activate")
    public ApiResponse<String> batchActivateSubscription(@RequestBody BatchSubscriptionIdsRequest request) {
        subscriptionAppService.batchActivateSubscription(request.getIds());
        return ApiResponse.success("批量启用完成");
    }

    @Operation(summary = "批量停用订阅", description = "批量停用订阅")
    @PatchMapping("/batch/deactivate")
    public ApiResponse<String> batchDeactivateSubscription(@RequestBody BatchSubscriptionIdsRequest request) {
        subscriptionAppService.batchDeactivateSubscription(request.getIds());
        return ApiResponse.success("批量停用完成");
    }

    @Operation(summary = "检测重复订阅", description = "检测哪些标的已存在订阅")
    @PostMapping("/check-duplicates")
    public ApiResponse<CheckDuplicatesResponse> checkDuplicates(@Valid @RequestBody CheckDuplicatesRequest request) {
        CheckDuplicatesResponse response = subscriptionAppService.checkDuplicates(request);
        return ApiResponse.success("检测完成", response);
    }

    @Operation(summary = "更新订阅", description = "更新已有订阅的参数")
    @PutMapping("/{id}")
    public ApiResponse<UserSubscription> updateSubscription(@PathVariable Long id,
            @RequestBody @Valid UpdateSubscriptionRequest request) {
        UserSubscription updatedSubscription = subscriptionAppService.updateSubscription(id, request);
        return ApiResponse.success("订阅更新成功", updatedSubscription);
    }

    @Operation(summary = "删除订阅", description = "删除指定的订阅")
    @DeleteMapping("/{id}")
    public ApiResponse<String> deleteSubscription(@PathVariable Long id) {
        subscriptionAppService.deleteSubscription(id);
        return ApiResponse.success("订阅删除成功");
    }

    @Operation(summary = "查询订阅列表", description = "分页查询用户的订阅列表，支持筛选和搜索")
    @GetMapping
    public ApiResponse<SubscriptionPageResponse<UserSubscription>> querySubscriptions(SubscriptionQueryDTO query) {
        SubscriptionPageResponse<UserSubscription> response = subscriptionAppService.querySubscriptions(query);
        return ApiResponse.success("查询成功", response);
    }

    @Operation(summary = "获取单个订阅", description = "根据ID获取订阅详情")
    @GetMapping("/{id}")
    public ApiResponse<UserSubscription> getSubscriptionById(@PathVariable Long id) {
        return subscriptionAppService.getSubscriptionById(id)
                .map(subscription -> ApiResponse.success("获取成功", subscription)).orElse(ApiResponse.error("订阅不存在"));
    }

    @Operation(summary = "获取用户所有订阅", description = "获取指定用户的所有订阅，支持按标的、类型、状态筛选")
    @GetMapping("/user/{userId}")
    public ApiResponse<List<UserSubscription>> getUserSubscriptions(@PathVariable Long userId,
            @RequestParam(required = false) String symbol, @RequestParam(required = false) String symbolType,
            @RequestParam(required = false) String status, @RequestParam(required = false) String alertType) {
        com.stock.fund.domain.repository.subscription.UserSubscriptionQuery query = com.stock.fund.domain.repository.subscription.UserSubscriptionQuery
                .builder().userId(userId).symbol(symbol).symbolType(symbolType).status(status).page(0).size(10000)
                .build();
        List<UserSubscription> subscriptions = subscriptionAppService.getUserSubscriptions(userId, query);
        return ApiResponse.success("获取用户订阅列表成功", subscriptions);
    }

    @Operation(summary = "启用订阅", description = "激活指定的订阅")
    @PatchMapping("/{id}/activate")
    public ApiResponse<String> activateSubscription(@PathVariable Long id) {
        subscriptionAppService.activateSubscription(id);
        return ApiResponse.success("订阅激活成功");
    }

    @Operation(summary = "停用订阅", description = "停用指定的订阅")
    @PatchMapping("/{id}/deactivate")
    public ApiResponse<String> deactivateSubscription(@PathVariable Long id) {
        subscriptionAppService.deactivateSubscription(id);
        return ApiResponse.success("订阅停用成功");
    }
}
