package com.stock.fund.application.service.subscription.impl;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.stock.fund.application.service.subscription.SubscriptionAppService;
import com.stock.fund.application.service.subscription.dto.BatchCreateSubscriptionRequest;
import com.stock.fund.application.service.subscription.dto.BatchCreateSubscriptionResponse;
import com.stock.fund.application.service.subscription.dto.CheckDuplicatesRequest;
import com.stock.fund.application.service.subscription.dto.CheckDuplicatesResponse;
import com.stock.fund.application.service.subscription.dto.CreateSubscriptionRequest;
import com.stock.fund.application.service.subscription.dto.CreateSubscriptionResponse;
import com.stock.fund.application.service.subscription.dto.SubscriptionPageResponse;
import com.stock.fund.application.service.subscription.dto.SubscriptionQueryDTO;
import com.stock.fund.application.service.subscription.dto.UpdateSubscriptionRequest;
import com.stock.fund.domain.entity.subscription.UserSubscription;
import com.stock.fund.domain.repository.DataCollectionTargetRepository;
import com.stock.fund.domain.repository.subscription.UserSubscriptionQuery;
import com.stock.fund.domain.repository.subscription.UserSubscriptionRepository;

/**
 * 订阅应用服务实现
 */
@Service
public class SubscriptionAppServiceImpl implements SubscriptionAppService {

    private static final Logger logger = LoggerFactory.getLogger(SubscriptionAppServiceImpl.class);

    @Autowired
    private UserSubscriptionRepository userSubscriptionRepository;

    @Autowired
    private DataCollectionTargetRepository dataCollectionTargetRepository;

    @Override
    public CreateSubscriptionResponse createSubscription(CreateSubscriptionRequest request) {
        logger.info("Creating subscription: userId={}, symbol={}", request.getUserId(), request.getSymbol());

        // 检查是否已存在相同标的的订阅
        Optional<UserSubscription> existing = userSubscriptionRepository
                .findByUserIdAndSymbolAndSymbolType(request.getUserId(), request.getSymbol(), request.getSymbolType());

        if (existing.isPresent()) {
            logger.info("Subscription already exists for symbol: ID={}", existing.get().getId());
            return CreateSubscriptionResponse.existed(existing.get());
        }

        // 创建新订阅
        UserSubscription subscription = new UserSubscription();
        subscription.setUserId(request.getUserId());
        subscription.setSymbol(request.getSymbol());
        subscription.setSymbolType(request.getSymbolType());
        subscription.setSymbolName(request.getSymbolName());
        // 直接设置 BigDecimal（不再转换为 Double）
        if (request.getTargetChangePercent() != null) {
            subscription.setTargetChangePercent(request.getTargetChangePercent());
        }
        subscription.setIsActive(request.getIsActive() != null && request.getIsActive() ? true : false);

        // 根据symbol查询标的名称
        if (subscription.getSymbolName() == null || subscription.getSymbolName().isEmpty()) {
            dataCollectionTargetRepository.findByCode(request.getSymbol())
                    .ifPresent(target -> subscription.setSymbolName(target.getName()));
        }

        UserSubscription saved = userSubscriptionRepository.save(subscription);
        logger.info("Subscription created successfully: ID={}", saved.getId());

        return CreateSubscriptionResponse.created(saved);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BatchCreateSubscriptionResponse batchCreateSubscription(BatchCreateSubscriptionRequest request) {
        logger.info("Batch creating subscriptions: userId={}, symbolCount={}, type={}, isActive={}",
                request.getUserId(), request.getSymbols().size(), request.getSymbolType(), request.getIsActive());

        // 1. 校验参数
        validateBatchRequest(request);

        // 2. 批量查询已存在的订阅
        List<UserSubscription> existingSubscriptions = userSubscriptionRepository.findByUserIdAndSymbolsAndSymbolType(
                request.getUserId(), request.getSymbols(), request.getSymbolType());

        Set<String> existingSymbols = existingSubscriptions.stream().map(UserSubscription::getSymbol)
                .collect(Collectors.toSet());

        // 3. 过滤出需要创建的标的
        List<String> symbolsToCreate = request.getSymbols().stream().filter(symbol -> !existingSymbols.contains(symbol))
                .collect(Collectors.toList());

        // 4. 构建批量插入的实体列表
        List<UserSubscription> subscriptionsToCreate = symbolsToCreate.stream()
                .map(symbol -> buildSubscription(request, symbol)).collect(Collectors.toList());

        // 5. 批量插入
        List<UserSubscription> createdSubscriptions = userSubscriptionRepository.batchInsert(subscriptionsToCreate);

        // 6. 构建响应
        List<BatchCreateSubscriptionResponse.CreatedItem> createdList = createdSubscriptions.stream()
                .map(sub -> BatchCreateSubscriptionResponse.CreatedItem.builder().symbol(sub.getSymbol())
                        .symbolName(sub.getSymbolName()).subscriptionId(sub.getId())
                        .createdAt(sub.getCreatedAt() != null ? sub.getCreatedAt().toString() : null).build())
                .collect(Collectors.toList());

        List<BatchCreateSubscriptionResponse.ExistingItem> existingList = existingSubscriptions.stream()
                .map(sub -> BatchCreateSubscriptionResponse.ExistingItem.builder().symbol(sub.getSymbol())
                        .symbolName(sub.getSymbolName()).subscriptionId(sub.getId())
                        .createdAt(sub.getCreatedAt() != null ? sub.getCreatedAt().toString() : null).build())
                .collect(Collectors.toList());

        List<BatchCreateSubscriptionResponse.FailureItem> failureList = request.getSymbols().stream()
                .filter(symbol -> !existingSymbols.contains(symbol)
                        && createdSubscriptions.stream().noneMatch(s -> s.getSymbol().equals(symbol)))
                .map(symbol -> BatchCreateSubscriptionResponse.FailureItem.builder().symbol(symbol).reason("创建失败")
                        .errorCode("SYSTEM_ERROR").build())
                .collect(Collectors.toList());

        return BatchCreateSubscriptionResponse.builder().batchId(generateBatchId())
                .totalCount(request.getSymbols().size()).createdCount(createdSubscriptions.size())
                .existingCount(existingSubscriptions.size()).failureCount(failureList.size()).createdList(createdList)
                .existingList(existingList).failureList(failureList).build();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchDeleteSubscription(List<Long> ids) {
        logger.info("Batch deleting subscriptions: IDs={}", ids);
        for (Long id : ids) {
            userSubscriptionRepository.deleteById(id);
        }
        logger.info("Batch deletion completed");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchActivateSubscription(List<Long> ids) {
        logger.info("Batch enabling subscriptions: IDs={}", ids);
        userSubscriptionRepository.batchUpdateActive(ids, true);
        logger.info("Batch enabling completed");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchDeactivateSubscription(List<Long> ids) {
        logger.info("Batch disabling subscriptions: IDs={}", ids);
        userSubscriptionRepository.batchUpdateActive(ids, false);
        logger.info("Batch disabling completed");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserSubscription updateSubscription(Long subscriptionId, UpdateSubscriptionRequest request) {
        logger.info("Updating subscription: ID={}", subscriptionId);

        UserSubscription subscription = userSubscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new IllegalArgumentException("订阅不存在: ID=" + subscriptionId));

        if (request.getTargetChangePercent() != null) {
            // 直接设置 BigDecimal（不再转换为 Double）
            subscription.setTargetChangePercent(request.getTargetChangePercent());
        }
        if (request.getIsActive() != null) {
            subscription.setIsActive(request.getIsActive());
        }
        if (request.getDescription() != null) {
            subscription.setDescription(request.getDescription());
        }

        return userSubscriptionRepository.save(subscription);
    }

    @Override
    public void deleteSubscription(Long subscriptionId) {
        logger.info("Deleting subscription: ID={}", subscriptionId);
        userSubscriptionRepository.deleteById(subscriptionId);
    }

    @Override
    public SubscriptionPageResponse<UserSubscription> querySubscriptions(SubscriptionQueryDTO query) {
        logger.debug("Paginated subscription query: userId={}, page={}, size={}", query.getUserId(), query.getPage(),
                query.getSize());

        UserSubscriptionQuery queryObj = UserSubscriptionQuery.builder().userId(query.getUserId())
                .symbol(query.getSymbol()).symbolType(query.getSymbolType()).status(query.getStatus())
                .page(query.getPage()).size(query.getSize()).sort(query.getSort()).build();

        List<UserSubscription> records = userSubscriptionRepository.findByUserIdWithPage(queryObj);
        long total = userSubscriptionRepository.countByUserId(queryObj);

        return SubscriptionPageResponse.of(records, total, query.getPage(), query.getSize());
    }

    @Override
    public List<UserSubscription> getUserSubscriptions(Long userId) {
        logger.debug("Getting user subscriptions: userId={}", userId);
        return userSubscriptionRepository.findByUserId(userId);
    }

    @Override
    public List<UserSubscription> getUserSubscriptions(Long userId, UserSubscriptionQuery query) {
        logger.debug("Getting user subscriptions: userId={}, symbol={}, symbolType={}, status={}", userId,
                query.getSymbol(), query.getSymbolType(), query.getStatus());
        return userSubscriptionRepository.findByUserIdWithPage(query);
    }

    @Override
    public List<UserSubscription> getUserActiveSubscriptions(Long userId) {
        logger.debug("Getting user active subscriptions: userId={}", userId);
        return userSubscriptionRepository.findByUserIdAndActive(userId, true);
    }

    @Override
    public Optional<UserSubscription> getSubscriptionById(Long subscriptionId) {
        return userSubscriptionRepository.findById(subscriptionId);
    }

    @Override
    public void activateSubscription(Long subscriptionId) {
        logger.info("Enabling subscription: ID={}", subscriptionId);
        userSubscriptionRepository.findById(subscriptionId).ifPresent(subscription -> {
            subscription.activate();
            userSubscriptionRepository.save(subscription);
        });
    }

    @Override
    public void deactivateSubscription(Long subscriptionId) {
        logger.info("Disabling subscription: ID={}", subscriptionId);
        userSubscriptionRepository.findById(subscriptionId).ifPresent(subscription -> {
            subscription.deactivate();
            userSubscriptionRepository.save(subscription);
        });
    }

    @Override
    public CheckDuplicatesResponse checkDuplicates(CheckDuplicatesRequest request) {
        logger.info("Checking duplicate subscriptions: userId={}, symbolCount={}, type={}", request.getUserId(),
                request.getSymbols().size(), request.getSymbolType());

        // 批量查询已存在的订阅
        List<UserSubscription> existingSubscriptions = userSubscriptionRepository.findByUserIdAndSymbolsAndSymbolType(
                request.getUserId(), request.getSymbols(), request.getSymbolType());

        // 按标的分组
        Map<String, List<UserSubscription>> subsBySymbol = existingSubscriptions.stream()
                .collect(Collectors.groupingBy(UserSubscription::getSymbol));

        // 构建重复项列表
        List<CheckDuplicatesResponse.DuplicateItem> duplicates = subsBySymbol.entrySet().stream().map(entry -> {
            String symbol = entry.getKey();
            List<UserSubscription> subs = entry.getValue();
            UserSubscription firstSub = subs.get(0);

            List<CheckDuplicatesResponse.ExistingSubscription> existingSubInfos = subs.stream()
                    .map(sub -> CheckDuplicatesResponse.ExistingSubscription.builder().subscriptionId(sub.getId())
                            .createdAt(sub.getCreatedAt() != null ? sub.getCreatedAt().toString() : null)
                            .status(sub.getIsActive() != null && sub.getIsActive() ? "ACTIVE" : "INACTIVE").build())
                    .collect(Collectors.toList());

            return CheckDuplicatesResponse.DuplicateItem.builder().symbol(symbol).symbolName(firstSub.getSymbolName())
                    .existingSubscriptions(existingSubInfos).build();
        }).collect(Collectors.toList());

        // 可创建的标的
        Set<String> existingSymbolSet = subsBySymbol.keySet();
        List<String> availableSymbols = request.getSymbols().stream()
                .filter(symbol -> !existingSymbolSet.contains(symbol)).collect(Collectors.toList());

        return CheckDuplicatesResponse.builder().checkedCount(request.getSymbols().size())
                .duplicateCount(duplicates.size()).duplicates(duplicates).availableSymbols(availableSymbols).build();
    }

    // 校验批量请求参数
    private void validateBatchRequest(BatchCreateSubscriptionRequest request) {
        if (request.getSymbols() != null && request.getSymbols().size() > 100) {
            throw new IllegalArgumentException("单次最多选择100个标的");
        }

        if (request.getTargetChangePercent() != null) {
            if (request.getTargetChangePercent().compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("目标涨跌幅必须大于0");
            }
            if (request.getTargetChangePercent().compareTo(new BigDecimal("99")) > 0) {
                throw new IllegalArgumentException("目标涨跌幅超出合理范围");
            }
        }
    }

    // 构建订阅实体
    private UserSubscription buildSubscription(BatchCreateSubscriptionRequest request, String symbol) {
        logger.info("Building subscription: symbol={}, request.isActive={}", symbol, request.getIsActive());
        UserSubscription subscription = new UserSubscription();
        subscription.setUserId(request.getUserId());
        subscription.setSymbol(symbol);
        subscription.setSymbolType(request.getSymbolType());
        // 直接设置 BigDecimal（不再转换为 Double）
        if (request.getTargetChangePercent() != null) {
            subscription.setTargetChangePercent(request.getTargetChangePercent());
        }
        subscription.setIsActive(request.getIsActive() != null && request.getIsActive());
        subscription.setDescription(request.getSymbolName());

        // 根据symbol查询标的名称
        dataCollectionTargetRepository.findByCode(symbol)
                .ifPresent(target -> subscription.setSymbolName(target.getName()));

        return subscription;
    }

    // 生成批次ID
    private String generateBatchId() {
        return "sub_" + System.currentTimeMillis() + "_" + UUID.randomUUID().toString().substring(0, 8);
    }
}