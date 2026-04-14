package com.stock.fund.application.service.alert.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
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

import com.stock.fund.application.service.DataCollectionAppService;
import com.stock.fund.application.service.alert.AlertAppService;
import com.stock.fund.application.service.alert.dto.*;
import com.stock.fund.domain.entity.DataCollectionTarget;
import com.stock.fund.domain.entity.alert.AlertHistory;
import com.stock.fund.domain.entity.alert.PriceAlert;
import com.stock.fund.domain.repository.DataCollectionTargetRepository;
import com.stock.fund.domain.repository.alert.AlertHistoryRepository;
import com.stock.fund.domain.repository.alert.PriceAlertQuery;
import com.stock.fund.domain.repository.alert.PriceAlertRepository;

@Service
public class AlertAppServiceImpl implements AlertAppService {

    private static final Logger logger = LoggerFactory.getLogger(AlertAppServiceImpl.class);

    @Autowired
    private PriceAlertRepository priceAlertRepository;

    @Autowired
    private AlertHistoryRepository alertHistoryRepository;

    @Autowired
    private DataCollectionAppService dataCollectionAppService;

    @Autowired
    private DataCollectionTargetRepository dataCollectionTargetRepository;

    @Override
    public CreateAlertResponse createAlert(CreateAlertRequest request) {
        logger.info("创建提醒: 用户ID={}, 标的代码={}, 提醒类型={}",
                request.getUserId(), request.getSymbol(), request.getAlertType());

        // 检查是否已存在相同标的的提醒
        Optional<PriceAlert> existing = priceAlertRepository.findByUserIdAndSymbolAndSymbolType(
                request.getUserId(), request.getSymbol(), request.getSymbolType());

        if (existing.isPresent()) {
            logger.info("该标的已存在提醒: ID={}", existing.get().getId());
            return CreateAlertResponse.existed(existing.get());
        }

        // 创建新提醒
        PriceAlert alert = new PriceAlert();
        alert.setUserId(request.getUserId());
        alert.setSymbol(request.getSymbol());
        alert.setSymbolName(request.getSymbolName());
        alert.setSymbolType(request.getSymbolType());
        alert.setAlertType(request.getAlertType());
        alert.setTargetPrice(request.getTargetPrice());
        alert.setTargetChangePercent(request.getTargetChangePercent());
        alert.setBasePrice(request.getBasePrice());
        alert.setStatus(request.getStatus() != null && request.getStatus() ? "ACTIVE" : "INACTIVE");

        PriceAlert saved = priceAlertRepository.save(alert);
        logger.info("提醒创建成功: ID={}", saved.getId());

        return CreateAlertResponse.created(saved);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AlertCreateResponse createAlertUnified(AlertCreateRequest request) {
        logger.info("统一创建提醒: 用户ID={}, symbol={}, symbols={}, 类型={}, 提醒类型={}",
                request.getUserId(), request.getSymbol(), request.getSymbols(),
                request.getSymbolType(), request.getAlertType());

        // 1. 参数校验
        validateUnifiedRequest(request);

        // 2. 决定使用单个还是批量逻辑
        if (request.getSymbol() != null && !request.getSymbol().isBlank()) {
            // 单个标的创建
            return createSingleAlert(request);
        } else if (request.getSymbols() != null && !request.getSymbols().isEmpty()) {
            // 批量标的创建
            return createBatchAlerts(request);
        } else {
            // 两者都为空
            return AlertCreateResponse.builder()
                    .batchId(generateBatchId())
                    .totalCount(0)
                    .createdCount(0)
                    .existingCount(0)
                    .failureCount(0)
                    .createdList(Collections.emptyList())
                    .existingList(Collections.emptyList())
                    .failureList(Collections.emptyList())
                    .build();
        }
    }

    // 单个标的创建
    private AlertCreateResponse createSingleAlert(AlertCreateRequest request) {
        // 检查是否已存在
        Optional<PriceAlert> existing = priceAlertRepository.findByUserIdAndSymbolAndSymbolType(
                request.getUserId(), request.getSymbol(), request.getSymbolType());

        if (existing.isPresent()) {
            PriceAlert alert = existing.get();
            return AlertCreateResponse.builder()
                    .batchId(generateBatchId())
                    .totalCount(1)
                    .createdCount(0)
                    .existingCount(1)
                    .failureCount(0)
                    .createdList(Collections.emptyList())
                    .existingList(Collections.singletonList(AlertCreateResponse.ExistingAlertItem.builder()
                            .symbol(alert.getSymbol())
                            .symbolName(alert.getSymbolName())
                            .alertId(alert.getId())
                            .alertType(alert.getAlertType())
                            .targetPrice(alert.getTargetPrice())
                            .targetChangePercent(alert.getTargetChangePercent())
                            .status(alert.getStatus())
                            .createdAt(alert.getCreatedAt() != null ? alert.getCreatedAt().toString() : null)
                            .build()))
                    .failureList(Collections.emptyList())
                    .build();
        }

        // 创建新提醒
        PriceAlert alert = buildPriceAlert(request, request.getSymbol());
        PriceAlert saved = priceAlertRepository.save(alert);

        return AlertCreateResponse.builder()
                .batchId(generateBatchId())
                .totalCount(1)
                .createdCount(1)
                .existingCount(0)
                .failureCount(0)
                .createdList(Collections.singletonList(AlertCreateResponse.CreatedAlertItem.builder()
                        .symbol(saved.getSymbol())
                        .symbolName(saved.getSymbolName())
                        .alertId(saved.getId())
                        .createdAt(saved.getCreatedAt() != null ? saved.getCreatedAt().toString() : null)
                        .build()))
                .existingList(Collections.emptyList())
                .failureList(Collections.emptyList())
                .build();
    }

    // 批量标的创建
    private AlertCreateResponse createBatchAlerts(AlertCreateRequest request) {
        List<String> symbols = request.getSymbols();

        // 1. 批量查询已存在的提醒
        List<PriceAlert> existingAlerts = priceAlertRepository
                .findByUserIdAndSymbolsAndSymbolTypeAndAlertType(
                        request.getUserId(),
                        symbols,
                        request.getSymbolType(),
                        request.getAlertType()
                );

        Set<String> existingSymbols = existingAlerts.stream()
                .map(PriceAlert::getSymbol)
                .collect(Collectors.toSet());

        // 2. 过滤出需要创建的标的
        List<String> symbolsToCreate = symbols.stream()
                .filter(symbol -> !existingSymbols.contains(symbol))
                .collect(Collectors.toList());

        // 3. 构建批量插入的实体列表
        List<PriceAlert> alertsToCreate = symbolsToCreate.stream()
                .map(symbol -> buildPriceAlert(request, symbol))
                .collect(Collectors.toList());

        // 4. 批量插入
        List<PriceAlert> createdAlerts = batchInsertAlerts(alertsToCreate);

        // 5. 构建响应
        List<AlertCreateResponse.CreatedAlertItem> createdList = createdAlerts.stream()
                .map(alert -> AlertCreateResponse.CreatedAlertItem.builder()
                        .symbol(alert.getSymbol())
                        .symbolName(alert.getSymbolName())
                        .alertId(alert.getId())
                        .createdAt(alert.getCreatedAt() != null ? alert.getCreatedAt().toString() : null)
                        .build())
                .collect(Collectors.toList());

        List<AlertCreateResponse.ExistingAlertItem> existingList = existingAlerts.stream()
                .map(alert -> AlertCreateResponse.ExistingAlertItem.builder()
                        .symbol(alert.getSymbol())
                        .symbolName(alert.getSymbolName())
                        .alertId(alert.getId())
                        .alertType(alert.getAlertType())
                        .targetPrice(alert.getTargetPrice())
                        .targetChangePercent(alert.getTargetChangePercent())
                        .status(alert.getStatus())
                        .createdAt(alert.getCreatedAt() != null ? alert.getCreatedAt().toString() : null)
                        .build())
                .collect(Collectors.toList());

        List<AlertCreateResponse.FailureAlertItem> failureList = symbols.stream()
                .filter(symbol -> !existingSymbols.contains(symbol) &&
                        createdAlerts.stream().noneMatch(a -> a.getSymbol().equals(symbol)))
                .map(symbol -> AlertCreateResponse.FailureAlertItem.builder()
                        .symbol(symbol)
                        .reason("创建失败")
                        .errorCode("SYSTEM_ERROR")
                        .build())
                .collect(Collectors.toList());

        return AlertCreateResponse.builder()
                .batchId(generateBatchId())
                .totalCount(symbols.size())
                .createdCount(createdAlerts.size())
                .existingCount(existingAlerts.size())
                .failureCount(failureList.size())
                .createdList(createdList)
                .existingList(existingList)
                .failureList(failureList)
                .build();
    }

    // 校验统一请求参数
    private void validateUnifiedRequest(AlertCreateRequest request) {
        // 批量时检查数量限制
        if (request.getSymbols() != null && request.getSymbols().size() > 100) {
            throw new IllegalArgumentException("单次最多选择100个标的");
        }

        // 价格类型校验
        if ("PRICE_ABOVE".equals(request.getAlertType()) || "PRICE_BELOW".equals(request.getAlertType())) {
            if (request.getTargetPrice() == null || request.getTargetPrice() <= 0) {
                throw new IllegalArgumentException("价格上限/下限时，目标价格必须大于0");
            }
        }

        // 涨跌幅类型校验
        if ("PERCENTAGE_CHANGE".equals(request.getAlertType())) {
            if (request.getTargetChangePercent() == null || request.getTargetChangePercent() == 0) {
                throw new IllegalArgumentException("涨跌幅类型时，涨跌幅不能为0");
            }
            if (Math.abs(request.getTargetChangePercent()) > 99) {
                throw new IllegalArgumentException("涨跌幅超出合理范围");
            }
        }
    }

    // 构建PriceAlert实体
    private PriceAlert buildPriceAlert(AlertCreateRequest request, String symbol) {
        PriceAlert alert = new PriceAlert();
        alert.setUserId(request.getUserId());
        alert.setSymbol(symbol);
        alert.setSymbolType(request.getSymbolType());
        alert.setAlertType(request.getAlertType());
        alert.setTargetPrice(request.getTargetPrice());
        alert.setTargetChangePercent(request.getTargetChangePercent());
        alert.setBasePrice(request.getBasePrice());
        alert.setStatus(request.getEnabled() != null && request.getEnabled() ? "ACTIVE" : "INACTIVE");

        // 根据symbol查询标的名称
        dataCollectionTargetRepository.findByCode(symbol)
                .ifPresent(target -> alert.setSymbolName(target.getName()));

        return alert;
    }

    @Override
    public BatchCreateAlertResponse batchCreateAlert(BatchCreateAlertRequest request) {
        logger.info("批量创建提醒: 标的数量={}, 类型={}",
                request.getSymbols().size(), request.getSymbolType());

        List<BatchCreateAlertResponse.SuccessAlert> successList = new ArrayList<>();
        List<BatchCreateAlertResponse.FailedAlert> failList = new ArrayList<>();

        for (String symbol : request.getSymbols()) {
            try {
                CreateAlertRequest singleRequest = new CreateAlertRequest();
                singleRequest.setUserId(request.getUserId());
                singleRequest.setSymbol(symbol);
                singleRequest.setSymbolName(request.getSymbolName());
                singleRequest.setSymbolType(request.getSymbolType());
                singleRequest.setAlertType(request.getAlertType());
                singleRequest.setTargetPrice(request.getTargetPrice());
                singleRequest.setTargetChangePercent(request.getTargetChangePercent());
                singleRequest.setBasePrice(request.getBasePrice());
                singleRequest.setStatus(request.getStatus());

                CreateAlertResponse response = createAlert(singleRequest);
                if (response.isCreated()) {
                    successList.add(BatchCreateAlertResponse.SuccessAlert.builder()
                            .alert(response.getAlert())
                            .status("CREATED")
                            .build());
                } else {
                    // 已存在，返回已有提醒也算成功
                    successList.add(BatchCreateAlertResponse.SuccessAlert.builder()
                            .alert(response.getAlert())
                            .status("ALREADY_EXISTS")
                            .build());
                }
            } catch (Exception e) {
                logger.error("批量创建提醒失败: symbol={}", symbol, e);
                failList.add(BatchCreateAlertResponse.FailedAlert.builder()
                        .symbol(symbol)
                        .reason(e.getMessage())
                        .build());
            }
        }

        logger.info("批量创建完成: 成功={}, 失败={}", successList.size(), failList.size());

        return BatchCreateAlertResponse.builder()
                .successCount(successList.size())
                .failCount(failList.size())
                .successList(successList)
                .failList(failList)
                .build();
    }

    @Override
    public PriceAlert updateAlert(Long alertId, UpdateAlertRequest request) {
        logger.info("更新提醒: ID={}", alertId);

        PriceAlert alert = priceAlertRepository.findById(alertId)
                .orElseThrow(() -> new IllegalArgumentException("提醒不存在: ID=" + alertId));

        if (request.getAlertType() != null) {
            alert.setAlertType(request.getAlertType());
        }
        if (request.getTargetPrice() != null) {
            alert.setTargetPrice(request.getTargetPrice());
        }
        if (request.getTargetChangePercent() != null) {
            alert.setTargetChangePercent(request.getTargetChangePercent());
        }
        if (request.getBasePrice() != null) {
            alert.setBasePrice(request.getBasePrice());
        }
        if (request.getStatus() != null) {
            alert.setStatus(request.getStatus() ? "ACTIVE" : "INACTIVE");
        }

        return priceAlertRepository.save(alert);
    }

    @Override
    public void deleteAlert(Long alertId) {
        logger.info("删除提醒: ID={}", alertId);
        priceAlertRepository.deleteById(alertId);
    }

    @Override
    public AlertPageResponse<PriceAlert> queryAlerts(AlertQueryDTO query) {
        logger.debug("分页查询提醒: userId={}, page={}, size={}", query.getUserId(), query.getPage(), query.getSize());

        // 构建查询对象
        PriceAlertQuery queryObj = PriceAlertQuery.builder()
                .userId(query.getUserId())
                .symbol(query.getSymbol())
                .symbolType(query.getSymbolType())
                .alertType(query.getAlertType())
                .status(query.getStatus())
                .page(query.getPage())
                .size(query.getSize())
                .sort(query.getSort())
                .build();

        List<PriceAlert> records = priceAlertRepository.findByUserIdWithPage(queryObj);

        long total = priceAlertRepository.countByUserId(queryObj);

        int pages = (int) Math.ceil((double) total / query.getSize());

        return AlertPageResponse.<PriceAlert>of(records, total, query.getPage(), query.getSize());
    }

    @Override
    public List<PriceAlert> getUserAlerts(Long userId) {
        logger.debug("获取用户提醒列表: 用户ID={}", userId);
        return priceAlertRepository.findByUserId(userId);
    }

    @Override
    public List<PriceAlert> getUserActiveAlerts(Long userId) {
        logger.debug("获取用户激活的提醒: 用户ID={}", userId);
        return priceAlertRepository.findByUserIdAndActive(userId, true);
    }

    @Override
    public Optional<PriceAlert> getAlertById(Long alertId) {
        return priceAlertRepository.findById(alertId);
    }

    @Override
    public Optional<PriceAlert> findExistingAlert(Long userId, String symbol, String symbolType) {
        return priceAlertRepository.findByUserIdAndSymbolAndSymbolType(userId, symbol, symbolType);
    }

    @Override
    public void activateAlert(Long alertId) {
        logger.info("激活提醒: ID={}", alertId);
        priceAlertRepository.findById(alertId).ifPresent(alert -> {
            alert.activate();
            priceAlertRepository.save(alert);
        });
    }

    @Override
    public void deactivateAlert(Long alertId) {
        logger.info("禁用提醒: ID={}", alertId);
        priceAlertRepository.findById(alertId).ifPresent(alert -> {
            alert.deactivate();
            priceAlertRepository.save(alert);
        });
    }

    @Override
    public List<AlertHistory> getAlertHistory(Long userId, Long alertId) {
        logger.debug("获取提醒历史: 用户ID={}, 提醒ID={}", userId, alertId);
        return alertHistoryRepository.findByUserIdAndAlertId(userId, alertId);
    }

    @Override
    public List<AlertHistory> getAllAlertHistory(Long userId) {
        logger.debug("获取用户所有提醒历史: 用户ID={}", userId);
        return alertHistoryRepository.findByUserId(userId);
    }

    @Override
    public void checkAndTriggerAlerts(String symbol, String symbolType, Double currentPrice) {
        List<PriceAlert> activeAlerts = priceAlertRepository.findActiveAlerts();
        logger.debug("检查提醒: 标的代码={}, 标的类型={}, 当前价格={}, 数量={}",
                symbol, symbolType, currentPrice, activeAlerts.size());

        for (PriceAlert alert : activeAlerts) {
            if (alert.getSymbol().equals(symbol) && alert.getSymbolType().equals(symbolType)) {
                if (alert.shouldTrigger(currentPrice)) {
                    triggerAlert(alert, currentPrice);
                }
            }
        }
    }

    @Override
    public void batchCheckAlerts() {
        logger.info("开始批量检查提醒");
        List<PriceAlert> activeAlerts = priceAlertRepository.findActiveAlerts();

        for (PriceAlert alert : activeAlerts) {
            try {
                Double currentPrice = getCurrentPrice(alert.getSymbol(), alert.getSymbolType());
                if (currentPrice != null && alert.shouldTrigger(currentPrice)) {
                    triggerAlert(alert, currentPrice);
                }
            } catch (Exception e) {
                logger.error("检查提醒失败: 标的代码={}, 类型={}",
                        alert.getSymbol(), alert.getSymbolType(), e);
            }
        }
        logger.info("批量检查提醒完成");
    }

    // 触发提醒
    private void triggerAlert(PriceAlert alert, Double currentPrice) {
        logger.info("触发提醒: ID={}, 标的代码={}, 当前价格={}",
                alert.getId(), alert.getSymbol(), currentPrice);

        // 记录触发历史
        AlertHistory history = AlertHistory.createFromAlert(alert, currentPrice,
                String.format("价格达到目标值 %.2f，当前价格 %.2f",
                        alert.getTargetPrice(), currentPrice));
        alertHistoryRepository.save(history);

        // 更新提醒状态
        alert.trigger();
        alert.setCurrentValue(currentPrice);
        priceAlertRepository.save(alert);

        logger.info("提醒触发成功: ID={}", alert.getId());
    }

    // 获取当前价格
    private Double getCurrentPrice(String symbol, String symbolType) {
        try {
            if ("STOCK".equals(symbolType)) {
                var stockQuote = dataCollectionAppService.collectStockQuote(symbol);
                return stockQuote != null ? stockQuote.getClose().doubleValue() : null;
            } else if ("FUND".equals(symbolType)) {
                var fundQuote = dataCollectionAppService.collectFundQuote(symbol);
                return fundQuote != null ? fundQuote.getNav().doubleValue() : null;
            }
        } catch (Exception e) {
            logger.error("获取实体价格失败: 代码={}, 类型={}", symbol, symbolType, e);
        }
        return null;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BatchCreateAlertResponseV2 batchCreateAlertV2(BatchCreateAlertRequestV2 request) {
        logger.info("批量创建提醒V2: 用户ID={}, 标的数量={}, 类型={}, 提醒类型={}",
                request.getUserId(), request.getSymbols().size(), request.getSymbolType(), request.getAlertType());

        // 1. 参数校验
        validateBatchRequestV2(request);

        // 2. 批量查询已存在的提醒（一次查询代替N次）
        List<PriceAlert> existingAlerts = priceAlertRepository
                .findByUserIdAndSymbolsAndSymbolTypeAndAlertType(
                        request.getUserId(),
                        request.getSymbols(),
                        request.getSymbolType(),
                        request.getAlertType()
                );

        Set<String> existingSymbols = existingAlerts.stream()
                .map(PriceAlert::getSymbol)
                .collect(Collectors.toSet());

        // 3. 过滤出需要创建的标的
        List<String> symbolsToCreate = request.getSymbols().stream()
                .filter(symbol -> !existingSymbols.contains(symbol))
                .collect(Collectors.toList());

        // 4. 构建批量插入的实体列表
        List<PriceAlert> alertsToCreate = symbolsToCreate.stream()
                .map(symbol -> buildPriceAlertV2(request, symbol))
                .collect(Collectors.toList());

        // 5. 批量插入
        List<PriceAlert> createdAlerts = batchInsertAlerts(alertsToCreate);

        // 6. 构建响应
        List<BatchCreateAlertResponseV2.SuccessItem> successList = buildSuccessListV2(createdAlerts, existingAlerts);
        List<BatchCreateAlertResponseV2.FailureItem> failList = buildFailureListV2(
                request.getSymbols(), createdAlerts, existingAlerts);

        return BatchCreateAlertResponseV2.builder()
                .batchId(generateBatchId())
                .totalCount(request.getSymbols().size())
                .successCount(successList.size())
                .failureCount(failList.size())
                .successList(successList)
                .failureList(failList)
                .build();
    }

    @Override
    public CheckDuplicatesResponse checkDuplicates(CheckDuplicatesRequest request) {
        logger.info("检测重复提醒: 用户ID={}, 标的数量={}, 类型={}, 提醒类型={}",
                request.getUserId(), request.getSymbols().size(), request.getSymbolType(), request.getAlertType());

        // 批量查询已存在的提醒
        List<PriceAlert> existingAlerts = priceAlertRepository
                .findByUserIdAndSymbolsAndSymbolTypeAndAlertType(
                        request.getUserId(),
                        request.getSymbols(),
                        request.getSymbolType(),
                        request.getAlertType()
                );

        // 按标的分组
        Map<String, List<PriceAlert>> alertsBySymbol = existingAlerts.stream()
                .collect(Collectors.groupingBy(PriceAlert::getSymbol));

        // 构建重复项列表
        List<CheckDuplicatesResponse.DuplicateItem> duplicates = alertsBySymbol.entrySet().stream()
                .map(entry -> {
                    String symbol = entry.getKey();
                    List<PriceAlert> alerts = entry.getValue();
                    PriceAlert firstAlert = alerts.get(0);

                    List<CheckDuplicatesResponse.ExistingAlert> existingAlertInfos = alerts.stream()
                            .map(alert -> CheckDuplicatesResponse.ExistingAlert.builder()
                                    .alertId(alert.getId())
                                    .alertType(alert.getAlertType())
                                    .targetPrice(alert.getTargetPrice())
                                    .targetChangePercent(alert.getTargetChangePercent())
                                    .createdAt(alert.getCreatedAt() != null ? alert.getCreatedAt().toString() : null)
                                    .status(alert.getStatus())
                                    .build())
                            .collect(Collectors.toList());

                    return CheckDuplicatesResponse.DuplicateItem.builder()
                            .symbol(symbol)
                            .symbolName(firstAlert.getSymbolName())
                            .existingAlerts(existingAlertInfos)
                            .build();
                })
                .collect(Collectors.toList());

        // 可创建的标的
        Set<String> existingSymbolSet = alertsBySymbol.keySet();
        List<String> availableSymbols = request.getSymbols().stream()
                .filter(symbol -> !existingSymbolSet.contains(symbol))
                .collect(Collectors.toList());

        return CheckDuplicatesResponse.builder()
                .checkedCount(request.getSymbols().size())
                .duplicateCount(duplicates.size())
                .duplicates(duplicates)
                .availableSymbols(availableSymbols)
                .build();
    }

    // 校验批量请求参数
    private void validateBatchRequestV2(BatchCreateAlertRequestV2 request) {
        if (request.getSymbols().size() > 100) {
            throw new IllegalArgumentException("单次最多选择100个标的");
        }

        if ("PRICE_ABOVE".equals(request.getAlertType()) || "PRICE_BELOW".equals(request.getAlertType())) {
            if (request.getTargetPrice() == null || request.getTargetPrice() <= 0) {
                throw new IllegalArgumentException("价格上限/下限时，目标价格必须大于0");
            }
        }

        if ("PERCENTAGE_CHANGE".equals(request.getAlertType())) {
            if (request.getTargetChangePercent() == null || request.getTargetChangePercent() == 0) {
                throw new IllegalArgumentException("涨跌幅类型时，涨跌幅不能为0");
            }
            if (Math.abs(request.getTargetChangePercent()) > 99) {
                throw new IllegalArgumentException("涨跌幅超出合理范围");
            }
        }
    }

    // 构建PriceAlert实体
    private PriceAlert buildPriceAlertV2(BatchCreateAlertRequestV2 request, String symbol) {
        PriceAlert alert = new PriceAlert();
        alert.setUserId(request.getUserId());
        alert.setSymbol(symbol);
        alert.setSymbolType(request.getSymbolType());
        alert.setAlertType(request.getAlertType());
        alert.setTargetPrice(request.getTargetPrice());
        alert.setTargetChangePercent(request.getTargetChangePercent());
        alert.setStatus("ACTIVE");

        // 根据symbol查询标的名称
        dataCollectionTargetRepository.findByCode(symbol)
                .ifPresent(target -> alert.setSymbolName(target.getName()));

        return alert;
    }

    // 批量插入提醒
    private List<PriceAlert> batchInsertAlerts(List<PriceAlert> alerts) {
        if (alerts == null || alerts.isEmpty()) {
            return Collections.emptyList();
        }
        return priceAlertRepository.saveAll(alerts);
    }

    // 构建成功列表
    private List<BatchCreateAlertResponseV2.SuccessItem> buildSuccessListV2(List<PriceAlert> createdAlerts, List<PriceAlert> existingAlerts) {
        List<BatchCreateAlertResponseV2.SuccessItem> successList = new ArrayList<>();

        // 新创建的
        for (PriceAlert alert : createdAlerts) {
            successList.add(BatchCreateAlertResponseV2.SuccessItem.builder()
                    .symbol(alert.getSymbol())
                    .symbolName(alert.getSymbolName())
                    .alertId(alert.getId())
                    .createdAt(alert.getCreatedAt() != null ? alert.getCreatedAt().toString() : null)
                    .build());
        }

        // 已存在的
        for (PriceAlert alert : existingAlerts) {
            successList.add(BatchCreateAlertResponseV2.SuccessItem.builder()
                    .symbol(alert.getSymbol())
                    .symbolName(alert.getSymbolName())
                    .alertId(alert.getId())
                    .createdAt(alert.getCreatedAt() != null ? alert.getCreatedAt().toString() : null)
                    .build());
        }

        return successList;
    }

    // 构建失败列表
    private List<BatchCreateAlertResponseV2.FailureItem> buildFailureListV2(
            List<String> allSymbols, List<PriceAlert> createdAlerts, List<PriceAlert> existingAlerts) {

        Set<String> successSymbols = new HashSet<>();
        createdAlerts.forEach(a -> successSymbols.add(a.getSymbol()));
        existingAlerts.forEach(a -> successSymbols.add(a.getSymbol()));

        return allSymbols.stream()
                .filter(symbol -> !successSymbols.contains(symbol))
                .map(symbol -> BatchCreateAlertResponseV2.FailureItem.builder()
                        .symbol(symbol)
                        .reason("创建失败")
                        .errorCode("SYSTEM_ERROR")
                        .build())
                .collect(Collectors.toList());
    }

    // 生成批次ID
    private String generateBatchId() {
        return "batch_" + System.currentTimeMillis() + "_" + UUID.randomUUID().toString().substring(0, 8);
    }
}
