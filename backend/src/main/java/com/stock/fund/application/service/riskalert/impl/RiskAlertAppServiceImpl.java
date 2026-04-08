package com.stock.fund.application.service.riskalert.impl;

import com.stock.fund.application.service.riskalert.RiskAlertAppService;
import com.stock.fund.application.service.riskalert.dto.RiskAlertMergeDTO;
import com.stock.fund.domain.entity.Fund;
import com.stock.fund.domain.entity.FundQuote;
import com.stock.fund.domain.entity.Stock;
import com.stock.fund.domain.entity.StockQuote;
import com.stock.fund.domain.entity.riskalert.RiskAlert;
import com.stock.fund.domain.repository.FundQuoteRepository;
import com.stock.fund.domain.repository.FundRepository;
import com.stock.fund.domain.repository.RiskAlertRepository;
import com.stock.fund.domain.repository.StockQuoteRepository;
import com.stock.fund.domain.repository.StockRepository;
import com.stock.fund.domain.service.riskalert.RiskAlertDomainService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class RiskAlertAppServiceImpl implements RiskAlertAppService {

    private static final Logger logger = LoggerFactory.getLogger(RiskAlertAppServiceImpl.class);

    // 默认用户ID（后续可扩展为多用户）
    private static final Long DEFAULT_USER_ID = 1L;

    @Autowired
    private RiskAlertRepository riskAlertRepository;

    @Autowired
    private StockQuoteRepository stockQuoteRepository;

    @Autowired
    private FundQuoteRepository fundQuoteRepository;

    @Autowired
    private StockRepository stockRepository;

    @Autowired
    private FundRepository fundRepository;

    @Autowired
    private RiskAlertDomainService riskAlertDomainService;

    @Override
    @Transactional
    public void checkAndCreateRiskAlerts() {
        logger.info("开始风险提醒检测");
        LocalDateTime now = LocalDateTime.now();

        // 检查股票风险
        checkStockRiskAlerts(now);

        // 检查基金风险
        checkFundRiskAlerts(now);

        logger.info("风险提醒检测完成");
    }

    private void checkStockRiskAlerts(LocalDateTime now) {
        List<StockQuote> latestQuotes = stockQuoteRepository.findAllLatestQuotes();
        logger.debug("获取到 {} 条股票最新行情", latestQuotes.size());

        // 获取所有股票的基本信息
        List<Stock> allStocks = stockRepository.findAll();
        Map<Long, Stock> stockMap = allStocks.stream()
                .collect(Collectors.toMap(Stock::getId, s -> s));

        for (StockQuote quote : latestQuotes) {
            if (quote.getChangePercent() == null) {
                continue;
            }

            double changePercent = quote.getChangePercent();

            if (riskAlertDomainService.shouldTriggerAlert(changePercent)) {
                Stock stock = stockMap.get(quote.getStockId());
                if (stock == null) {
                    continue;
                }

                String symbol = stock.getSymbol();
                String symbolName = stock.getName();
                double currentPrice = quote.getClose() != null ? quote.getClose() : 0.0;

                // 计算昨日收盘价
                double yesterdayClose = calculateYesterdayClose(currentPrice, changePercent);

                // 检查是否当日已存在该股票的风险提醒
                Optional<RiskAlert> existingAlert = riskAlertRepository.findByUserIdAndSymbolAndDate(
                        DEFAULT_USER_ID, symbol, now);

                if (existingAlert.isPresent()) {
                    // 更新已有提醒的触发次数
                    RiskAlert alert = existingAlert.get();
                    alert.incrementTriggerCount();
                    alert.setChangePercent(changePercent);
                    alert.setCurrentPrice(currentPrice);
                    alert.setTriggerReason(riskAlertDomainService.generateTriggerReason(
                            symbol, "STOCK", changePercent, currentPrice));
                    riskAlertRepository.update(alert);
                    logger.debug("更新股票风险提醒: symbol={}, triggerCount={}", symbol, alert.getTriggerCount());
                } else {
                    // 创建新提醒
                    RiskAlert alert = new RiskAlert(
                            DEFAULT_USER_ID, symbol, "STOCK", symbolName,
                            changePercent, currentPrice, yesterdayClose);
                    alert.setTriggerReason(riskAlertDomainService.generateTriggerReason(
                            symbol, "STOCK", changePercent, currentPrice));
                    riskAlertRepository.save(alert);
                    logger.info("创建股票风险提醒: symbol={}, changePercent={}", symbol, changePercent);
                }
            }
        }
    }

    private void checkFundRiskAlerts(LocalDateTime now) {
        List<FundQuote> latestQuotes = fundQuoteRepository.findAllLatestQuotes();
        logger.debug("获取到 {} 条基金最新净值", latestQuotes.size());

        for (FundQuote quote : latestQuotes) {
            if (quote.getChangePercent() == null) {
                continue;
            }

            double changePercent = quote.getChangePercent();

            if (riskAlertDomainService.shouldTriggerAlert(changePercent)) {
                String symbol = quote.getFundCode();
                String symbolName = quote.getFundName();
                double currentPrice = quote.getNav() != null ? quote.getNav() : 0.0;
                double yesterdayClose = quote.getPrevNetValue() != null ? quote.getPrevNetValue() : currentPrice;

                // 检查是否当日已存在该基金的风险提醒
                Optional<RiskAlert> existingAlert = riskAlertRepository.findByUserIdAndSymbolAndDate(
                        DEFAULT_USER_ID, symbol, now);

                if (existingAlert.isPresent()) {
                    // 更新已有提醒的触发次数
                    RiskAlert alert = existingAlert.get();
                    alert.incrementTriggerCount();
                    alert.setChangePercent(changePercent);
                    alert.setCurrentPrice(currentPrice);
                    alert.setTriggerReason(riskAlertDomainService.generateTriggerReason(
                            symbol, "FUND", changePercent, currentPrice));
                    riskAlertRepository.update(alert);
                    logger.debug("更新基金风险提醒: symbol={}, triggerCount={}", symbol, alert.getTriggerCount());
                } else {
                    // 创建新提醒
                    RiskAlert alert = new RiskAlert(
                            DEFAULT_USER_ID, symbol, "FUND", symbolName,
                            changePercent, currentPrice, yesterdayClose);
                    alert.setTriggerReason(riskAlertDomainService.generateTriggerReason(
                            symbol, "FUND", changePercent, currentPrice));
                    riskAlertRepository.save(alert);
                    logger.info("创建基金风险提醒: symbol={}, changePercent={}", symbol, changePercent);
                }
            }
        }
    }

    /**
     * 根据当前价格和涨跌幅计算昨日收盘价
     */
    private double calculateYesterdayClose(double currentPrice, double changePercent) {
        if (changePercent == 0) {
            return currentPrice;
        }
        // yesterdayClose = currentPrice / (1 + changePercent/100)
        return currentPrice / (1 + changePercent / 100);
    }

    @Override
    public long getUnreadCount(Long userId) {
        return riskAlertRepository.countUnreadByUserId(userId);
    }

    @Override
    @Transactional
    public void markAllAsRead(Long userId) {
        riskAlertRepository.markAllAsRead(userId);
        logger.info("标记用户所有风险提醒为已读: userId={}", userId);
    }

    @Override
    public List<RiskAlertMergeDTO> getMergedRiskAlerts(Long userId, Long cursor, int limit) {
        LocalDateTime cursorTime = cursor != null ?
                LocalDateTime.ofEpochSecond(cursor / 1000, 0, java.time.ZoneOffset.ofHours(8)) :
                LocalDateTime.now();

        List<RiskAlert> alerts = riskAlertRepository.findByUserId(userId, cursorTime, limit);

        // 按 symbol + date 分组
        Map<String, List<RiskAlert>> grouped = alerts.stream()
                .collect(Collectors.groupingBy(alert ->
                        alert.getSymbol() + "_" + alert.getTriggeredAt().toLocalDate()));

        List<RiskAlertMergeDTO> result = new ArrayList<>();

        for (Map.Entry<String, List<RiskAlert>> entry : grouped.entrySet()) {
            List<RiskAlert> groupAlerts = entry.getValue();

            // 按触发时间倒序排列
            groupAlerts.sort(Comparator.comparing(RiskAlert::getTriggeredAt).reversed());

            RiskAlert latest = groupAlerts.get(0);
            RiskAlert first = groupAlerts.get(groupAlerts.size() - 1); // 最早触发的那条

            // 计算最大涨跌幅
            double maxChangePercent = groupAlerts.stream()
                    .mapToDouble(RiskAlert::getChangePercent)
                    .max()
                    .orElse(latest.getChangePercent());

            // 构建明细列表
            List<RiskAlertMergeDTO.RiskAlertDetailDTO> details = groupAlerts.stream()
                    .map(alert -> new RiskAlertMergeDTO.RiskAlertDetailDTO(
                            alert.getId(),
                            alert.getChangePercent(),
                            alert.getCurrentPrice(),
                            alert.getTriggeredAt(),
                            alert.getTriggerReason()))
                    .collect(Collectors.toList());

            result.add(new RiskAlertMergeDTO(
                    latest.getId(),
                    latest.getSymbol(),
                    latest.getSymbolType(),
                    latest.getSymbolName(),
                    latest.getTriggeredAt().toLocalDate(),
                    groupAlerts.size(),
                    maxChangePercent,
                    latest.getChangePercent(),
                    latest.getCurrentPrice(),
                    latest.getYesterdayClose(),
                    latest.getIsRead(),
                    latest.getTriggeredAt(),
                    details));
        }

        // 按最新触发时间倒序
        result.sort(Comparator.comparing(RiskAlertMergeDTO::latestTriggeredAt).reversed());

        return result;
    }

    @Override
    public void deleteById(Long id) {
        riskAlertRepository.deleteById(id);
        logger.info("删除风险提醒: id={}", id);
    }
}
