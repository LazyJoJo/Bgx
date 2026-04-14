package com.stock.fund.application.service.alert.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.stock.fund.application.service.DataCollectionAppService;
import com.stock.fund.application.service.alert.AlertAppService;
import com.stock.fund.application.service.alert.dto.*;
import com.stock.fund.domain.entity.alert.AlertHistory;
import com.stock.fund.domain.entity.alert.PriceAlert;
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
    public BatchCreateAlertResponse batchCreateAlert(BatchCreateAlertRequest request) {
        logger.info("批量创建提醒: 标的数量={}, 类型={}",
                request.getSymbols().size(), request.getSymbolType());

        List<PriceAlert> successList = new ArrayList<>();
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
                    successList.add(response.getAlert());
                } else {
                    // 已存在，返回已有提醒也算成功
                    successList.add(response.getAlert());
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
}
