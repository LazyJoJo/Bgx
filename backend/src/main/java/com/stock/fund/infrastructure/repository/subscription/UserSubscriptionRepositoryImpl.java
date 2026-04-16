package com.stock.fund.infrastructure.repository.subscription;

import com.stock.fund.domain.entity.subscription.UserSubscription;
import com.stock.fund.domain.repository.subscription.UserSubscriptionQuery;
import com.stock.fund.domain.repository.subscription.UserSubscriptionRepository;
import com.stock.fund.infrastructure.entity.subscription.UserSubscriptionPO;
import com.stock.fund.infrastructure.mapper.subscription.UserSubscriptionMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 用户订阅仓储实现
 */
@Repository
public class UserSubscriptionRepositoryImpl implements UserSubscriptionRepository {

    @Autowired
    private UserSubscriptionMapper userSubscriptionMapper;

    @Override
    public Optional<UserSubscription> findById(Long id) {
        UserSubscriptionPO po = userSubscriptionMapper.selectById(id);
        return Optional.ofNullable(po).map(this::toEntity);
    }

    @Override
    public List<UserSubscription> findByUserId(Long userId) {
        return userSubscriptionMapper.findByUserId(userId).stream()
                .map(this::toEntity)
                .collect(Collectors.toList());
    }

    @Override
    public List<UserSubscription> findByUserIdAndActive(Long userId, Boolean active) {
        return userSubscriptionMapper.findByUserIdAndActive(userId, active).stream()
                .map(this::toEntity)
                .collect(Collectors.toList());
    }

    @Override
    public List<UserSubscription> findActiveSubscriptions() {
        return userSubscriptionMapper.findActiveSubscriptions().stream()
                .map(this::toEntity)
                .collect(Collectors.toList());
    }

    @Override
    public UserSubscription save(UserSubscription subscription) {
        UserSubscriptionPO po = toPO(subscription);
        if (subscription.getId() == null) {
            userSubscriptionMapper.insert(po);
            subscription.setId(po.getId());
        } else {
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
        List<UserSubscriptionPO> poList = subscriptions.stream()
                .map(this::toPO)
                .collect(Collectors.toList());
        userSubscriptionMapper.insertBatch(poList);
        for (int i = 0; i < subscriptions.size(); i++) {
            subscriptions.get(i).setId(poList.get(i).getId());
        }
        return subscriptions;
    }

    @Override
    public Optional<UserSubscription> findByUserIdAndSymbolAndSymbolType(Long userId, String symbol, String symbolType) {
        UserSubscriptionPO po = userSubscriptionMapper.findByUserIdAndSymbolAndSymbolType(userId, symbol, symbolType);
        return Optional.ofNullable(po).map(this::toEntity);
    }

    @Override
    public List<UserSubscription> findByUserIdAndSymbolsAndSymbolType(Long userId, List<String> symbols, String symbolType) {
        return userSubscriptionMapper.findByUserIdAndSymbolsAndSymbolType(userId, symbols, symbolType).stream()
                .map(this::toEntity)
                .collect(Collectors.toList());
    }

    @Override
    public List<UserSubscription> batchInsert(List<UserSubscription> subscriptions) {
        if (subscriptions == null || subscriptions.isEmpty()) {
            return subscriptions;
        }
        List<UserSubscriptionPO> poList = subscriptions.stream()
                .map(this::toPO)
                .collect(Collectors.toList());
        userSubscriptionMapper.insertBatch(poList);
        for (int i = 0; i < subscriptions.size(); i++) {
            subscriptions.get(i).setId(poList.get(i).getId());
        }
        return subscriptions;
    }

    @Override
    public List<UserSubscription> findByUserIdWithPage(UserSubscriptionQuery query) {
        int offset = query.getPage() * query.getSize();
        return userSubscriptionMapper.findByUserIdWithPage(
                query.getUserId(),
                query.getSymbol(),
                query.getSymbolType(),
                query.getStatus(),
                offset,
                query.getSize(),
                query.getSort()
        ).stream().map(this::toEntity).collect(Collectors.toList());
    }

    @Override
    public long countByUserId(UserSubscriptionQuery query) {
        return userSubscriptionMapper.countByUserId(
                query.getUserId(),
                query.getSymbol(),
                query.getSymbolType(),
                query.getStatus()
        );
    }

    @Override
    public void batchUpdateActive(List<Long> ids, Boolean active) {
        userSubscriptionMapper.batchUpdateActive(ids, active);
    }

    // Entity -> PO 转换
    private UserSubscriptionPO toPO(UserSubscription entity) {
        UserSubscriptionPO po = new UserSubscriptionPO();
        po.setId(entity.getId());
        po.setUserId(entity.getUserId());
        po.setSymbol(entity.getSymbol());
        po.setSymbolType(entity.getSymbolType());
        po.setSymbolName(entity.getSymbolName());
        // Double -> BigDecimal 转换
        if (entity.getTargetChangePercent() != null) {
            po.setTargetChangePercent(BigDecimal.valueOf(entity.getTargetChangePercent()));
        }
        po.setIsActive(entity.getIsActive());
        po.setLastTriggered(entity.getLastTriggered());
        po.setDescription(entity.getDescription());
        po.setCreatedAt(entity.getCreatedAt());
        po.setUpdatedAt(entity.getUpdatedAt());
        return po;
    }

    // PO -> Entity 转换
    private UserSubscription toEntity(UserSubscriptionPO po) {
        UserSubscription entity = new UserSubscription();
        entity.setId(po.getId());
        entity.setUserId(po.getUserId());
        entity.setSymbol(po.getSymbol());
        entity.setSymbolType(po.getSymbolType());
        entity.setSymbolName(po.getSymbolName());
        // BigDecimal -> Double 转换
        if (po.getTargetChangePercent() != null) {
            entity.setTargetChangePercent(po.getTargetChangePercent().doubleValue());
        }
        entity.setIsActive(po.getIsActive());
        entity.setLastTriggered(po.getLastTriggered());
        entity.setDescription(po.getDescription());
        entity.setCreatedAt(po.getCreatedAt());
        entity.setUpdatedAt(po.getUpdatedAt());
        return entity;
    }
}