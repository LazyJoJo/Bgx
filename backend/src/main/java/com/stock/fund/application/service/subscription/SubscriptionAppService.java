package com.stock.fund.application.service.subscription;

import com.stock.fund.application.service.subscription.dto.*;
import com.stock.fund.domain.entity.subscription.UserSubscription;
import java.util.List;
import java.util.Optional;

/**
 * 订阅应用服务接口
 */
public interface SubscriptionAppService {

    /**
     * 创建订阅
     * @param request 创建请求
     * @return 创建成功的订阅，如果已存在则返回已存在的订阅
     */
    CreateSubscriptionResponse createSubscription(CreateSubscriptionRequest request);

    /**
     * 批量创建订阅
     * @param request 批量创建请求
     * @return 批量创建结果
     */
    BatchCreateSubscriptionResponse batchCreateSubscription(BatchCreateSubscriptionRequest request);

    /**
     * 批量删除订阅
     * @param ids 订阅ID列表
     */
    void batchDeleteSubscription(List<Long> ids);

    /**
     * 批量启用订阅
     * @param ids 订阅ID列表
     */
    void batchActivateSubscription(List<Long> ids);

    /**
     * 批量停用订阅
     * @param ids 订阅ID列表
     */
    void batchDeactivateSubscription(List<Long> ids);

    /**
     * 更新订阅
     * @param subscriptionId 订阅ID
     * @param request 更新请求
     * @return 更新后的订阅
     */
    UserSubscription updateSubscription(Long subscriptionId, UpdateSubscriptionRequest request);

    /**
     * 删除订阅
     * @param subscriptionId 订阅ID
     */
    void deleteSubscription(Long subscriptionId);

    /**
     * 分页查询用户订阅
     * @param query 查询条件
     * @return 分页结果
     */
    SubscriptionPageResponse<UserSubscription> querySubscriptions(SubscriptionQueryDTO query);

    /**
     * 获取用户的所有订阅
     * @param userId 用户ID
     * @return 订阅列表
     */
    List<UserSubscription> getUserSubscriptions(Long userId);

    /**
     * 获取用户激活的订阅
     * @param userId 用户ID
     * @return 激活的订阅列表
     */
    List<UserSubscription> getUserActiveSubscriptions(Long userId);

    /**
     * 获取单个订阅
     * @param subscriptionId 订阅ID
     * @return 订阅
     */
    Optional<UserSubscription> getSubscriptionById(Long subscriptionId);

    /**
     * 启用订阅
     * @param subscriptionId 订阅ID
     */
    void activateSubscription(Long subscriptionId);

    /**
     * 停用订阅
     * @param subscriptionId 订阅ID
     */
    void deactivateSubscription(Long subscriptionId);

    /**
     * 检测重复订阅
     * @param request 重复检测请求
     * @return 重复检测结果
     */
    CheckDuplicatesResponse checkDuplicates(CheckDuplicatesRequest request);
}