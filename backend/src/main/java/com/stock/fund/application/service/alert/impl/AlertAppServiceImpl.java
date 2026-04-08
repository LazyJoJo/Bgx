package com.stock.fund.application.service.alert.impl;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.stock.fund.application.service.DataCollectionAppService;
import com.stock.fund.application.service.alert.AlertAppService;
import com.stock.fund.domain.entity.alert.AlertHistory;
import com.stock.fund.domain.entity.alert.PriceAlert;
import com.stock.fund.domain.repository.alert.AlertHistoryRepository;
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
    public PriceAlert createAlert(PriceAlert alert) {
        logger.info("创建提醒: 用户ID={}, 标的代码={}, 提醒类型={}", alert.getUserId(), alert.getSymbol(), alert.getAlertType());
        return priceAlertRepository.save(alert);
    }

    @Override
    public PriceAlert updateAlert(Long alertId, PriceAlert alert) {
        alert.setId(alertId);
        logger.info("更新提醒: ID={}", alertId);
        return priceAlertRepository.save(alert);
    }

    @Override
    public void deleteAlert(Long alertId) {
        logger.info("删除提醒: ID={}", alertId);
        priceAlertRepository.deleteById(alertId);
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
    public void activateAlert(Long alertId) {
        logger.info("激活提醒: ID={}", alertId);
        priceAlertRepository.findById(alertId).ifPresent(alert -> {
            alert.activate();
            priceAlertRepository.save(alert);
        });
    }

    @Override
    public void deactivateAlert(Long alertId) {
        logger.info("提醒: ID={}", alertId);
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
        logger.debug("检查提醒: 标的代码={}, 标的类型={}, 当前价格={},数量={}", symbol, symbolType, currentPrice, activeAlerts.size());

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
                logger.error("检查提醒失败: 标的代码={}, 类型={}", alert.getSymbol(), alert.getSymbolType(), e);
            }
        }
        logger.info("批量检查提醒完成");
    }

    // 私有方法：触发提醒
    private void triggerAlert(PriceAlert alert, Double currentPrice) {
        logger.info("触发提醒: ID={}, 标的代码={}, 当前价格={}", alert.getId(), alert.getSymbol(), currentPrice);

        // 记录触发历史
        AlertHistory history = AlertHistory.createFromAlert(alert, currentPrice,
                String.format("价格达到目标值 %.2f，当前价格 %.2f", alert.getTargetPrice(), currentPrice));
        alertHistoryRepository.save(history);

        // 更新提醒状态
        alert.trigger();
        alert.setCurrentValue(currentPrice);
        priceAlertRepository.save(alert);

        // TODO:这里可以发送实时通知到前端WebSocket
        logger.info("提醒触发成功: ID={}", alert.getId());
    }

    // 私有方法：获取当前价格
    private Double getCurrentPrice(String symbol, String symbolType) {
        try {
            if ("STOCK".equals(symbolType)) {
                // 从数据采集服务获取股票最新行情
                var stockQuote = dataCollectionAppService.collectStockQuote(symbol);
                return stockQuote != null ? stockQuote.getClose() : null;
            } else if ("FUND".equals(symbolType)) {
                // 从数据采集服务获取基金最新净值
                var fundQuote = dataCollectionAppService.collectFundQuote(symbol);
                return fundQuote != null ? fundQuote.getNav() : null;
            }
        } catch (Exception e) {
            logger.error("获取实体价格失败: 代码={}, 类型={}", symbol, symbolType, e);
        }
        return null;
    }
}