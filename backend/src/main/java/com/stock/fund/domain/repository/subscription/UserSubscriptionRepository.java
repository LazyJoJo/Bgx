package com.stock.fund.domain.repository.subscription;

import com.stock.fund.domain.entity.subscription.UserSubscription;
import java.util.List;
import java.util.Optional;

/**
 * 用户订阅仓储接口
 */
public interface UserSubscriptionRepository {
    Optional<UserSubscription> findById(Long id);
    List<UserSubscription> findByUserId(Long userId);
    List<UserSubscription> findByUserIdAndActive(Long userId, Boolean active);
    List<UserSubscription> findActiveSubscriptions();
    UserSubscription save(UserSubscription subscription);
    void deleteById(Long id);
    List<UserSubscription> saveAll(List<UserSubscription> subscriptions);

    /**
     * 查找同一用户是否已存在相同标的的订阅
     */
    Optional<UserSubscription> findByUserIdAndSymbolAndSymbolType(Long userId, String symbol, String symbolType);

    /**
     * 批量查询用户已存在的标的
     */
    List<UserSubscription> findByUserIdAndSymbolsAndSymbolType(Long userId, List<String> symbols, String symbolType);

    /**
     * 批量插入订阅
     */
    List<UserSubscription> batchInsert(List<UserSubscription> subscriptions);

    /**
     * 分页查询用户的订阅
     */
    List<UserSubscription> findByUserIdWithPage(UserSubscriptionQuery query);

    /**
     * 统计用户的订阅数量
     */
    long countByUserId(UserSubscriptionQuery query);

    /**
     * 批量更新激活状态
     */
    void batchUpdateActive(List<Long> ids, Boolean active);
}