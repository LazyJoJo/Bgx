package com.stock.fund.infrastructure.repository.subscription;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.stock.fund.domain.entity.subscription.UserSubscription;
import com.stock.fund.domain.repository.subscription.UserSubscriptionQuery;
import com.stock.fund.domain.repository.subscription.UserSubscriptionRepository;
import com.stock.fund.infrastructure.convert.UserSubscriptionStructMapper;
import com.stock.fund.infrastructure.entity.subscription.UserSubscriptionPO;
import com.stock.fund.infrastructure.mapper.subscription.UserSubscriptionMapper;

import lombok.RequiredArgsConstructor;

/**
 * 用户订阅仓储实现
 */
@Repository
@RequiredArgsConstructor
public class UserSubscriptionRepositoryImpl implements UserSubscriptionRepository {

    private final UserSubscriptionMapper userSubscriptionMapper;
    @Qualifier("userSubscriptionStructMapperImpl")
    private final UserSubscriptionStructMapper structMapper;

    @Override
    public Optional<UserSubscription> findById(Long id) {
        UserSubscriptionPO po = userSubscriptionMapper.selectById(id);
        return Optional.ofNullable(po).map(structMapper::toEntity);
    }

    @Override
    public List<UserSubscription> findByUserId(Long userId) {
        return structMapper.toEntityList(userSubscriptionMapper.findByUserId(userId));
    }

    @Override
    public List<UserSubscription> findByUserIdAndActive(Long userId, Boolean active) {
        return structMapper.toEntityList(userSubscriptionMapper.findByUserIdAndActive(userId, active));
    }

    @Override
    public List<UserSubscription> findActiveSubscriptions() {
        return structMapper.toEntityList(userSubscriptionMapper.findActiveSubscriptions());
    }

    @Override
    @Transactional
    public UserSubscription save(UserSubscription subscription) {
        UserSubscriptionPO po = structMapper.toPO(subscription);
        if (subscription.getId() == null) {
            userSubscriptionMapper.insert(po);
            subscription.setId(po.getId());
        } else {
            // MapStruct now copies id automatically (removed ignore=true)
            userSubscriptionMapper.updateById(po);
        }
        return subscription;
    }

    @Override
    public void deleteById(Long id) {
        userSubscriptionMapper.deleteById(id);
    }

    @Override
    public List<UserSubscription> saveAll(List<UserSubscription> subscriptions) {
        if (subscriptions == null || subscriptions.isEmpty()) {
            return subscriptions;
        }
        List<UserSubscriptionPO> poList = structMapper.toPOList(subscriptions);
        userSubscriptionMapper.insertBatch(poList);
        return buildSubscriptionsWithId(subscriptions, poList);
    }

    @Override
    public Optional<UserSubscription> findByUserIdAndSymbolAndSymbolType(Long userId, String symbol,
            String symbolType) {
        UserSubscriptionPO po = userSubscriptionMapper.findByUserIdAndSymbolAndSymbolType(userId, symbol, symbolType);
        return Optional.ofNullable(po).map(structMapper::toEntity);
    }

    @Override
    public List<UserSubscription> findByUserIdAndSymbolsAndSymbolType(Long userId, List<String> symbols,
            String symbolType) {
        return structMapper
                .toEntityList(userSubscriptionMapper.findByUserIdAndSymbolsAndSymbolType(userId, symbols, symbolType));
    }

    @Override
    public List<UserSubscription> batchInsert(List<UserSubscription> subscriptions) {
        if (subscriptions == null || subscriptions.isEmpty()) {
            return subscriptions;
        }
        List<UserSubscriptionPO> poList = structMapper.toPOList(subscriptions);
        userSubscriptionMapper.insertBatch(poList);
        return buildSubscriptionsWithId(subscriptions, poList);
    }

    /**
     * 构建带 ID 的订阅列表副本（遵循不可变性原则，不修改原始输入）
     */
    private List<UserSubscription> buildSubscriptionsWithId(List<UserSubscription> subscriptions,
            List<UserSubscriptionPO> poList) {
        List<UserSubscription> result = new ArrayList<>(subscriptions.size());
        for (int i = 0; i < subscriptions.size(); i++) {
            UserSubscription original = subscriptions.get(i);
            UserSubscription copy = new UserSubscription();
            copy.setUserId(original.getUserId());
            copy.setSymbol(original.getSymbol());
            copy.setSymbolType(original.getSymbolType());
            copy.setSymbolName(original.getSymbolName());
            copy.setTargetChangePercent(original.getTargetChangePercent());
            copy.setIsActive(original.getIsActive());
            copy.setLastTriggered(original.getLastTriggered());
            copy.setDescription(original.getDescription());
            copy.setId(poList.get(i).getId());
            result.add(copy);
        }
        return result;
    }

    @Override
    public List<UserSubscription> findByUserIdWithPage(UserSubscriptionQuery query) {
        int offset = query.getPage() * query.getSize();
        return structMapper.toEntityList(userSubscriptionMapper.findByUserIdWithPage(query.getUserId(),
                query.getSymbol(), query.getSymbolType(), query.getStatus(), offset, query.getSize(), query.getSort()));
    }

    @Override
    public long countByUserId(UserSubscriptionQuery query) {
        return userSubscriptionMapper.countByUserId(query.getUserId(), query.getSymbol(), query.getSymbolType(),
                query.getStatus());
    }

    @Override
    public void batchUpdateActive(List<Long> ids, Boolean active) {
        userSubscriptionMapper.batchUpdateActive(ids, active);
    }
}
