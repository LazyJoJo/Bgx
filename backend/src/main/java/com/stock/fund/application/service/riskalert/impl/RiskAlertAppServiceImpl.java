package com.stock.fund.application.service.riskalert.impl;

import com.stock.fund.application.service.riskalert.RiskAlertAppService;
import com.stock.fund.application.service.riskalert.dto.*;
import com.stock.fund.domain.entity.Fund;
import com.stock.fund.domain.entity.Stock;
import com.stock.fund.domain.entity.subscription.UserSubscription;
import com.stock.fund.domain.entity.riskalert.RiskAlert;
import com.stock.fund.domain.repository.FundQuoteRepository;
import com.stock.fund.domain.repository.FundRepository;
import com.stock.fund.domain.repository.RiskAlertQuery;
import com.stock.fund.domain.repository.RiskAlertRepository;
import com.stock.fund.domain.repository.StockQuoteRepository;
import com.stock.fund.domain.repository.StockRepository;
import com.stock.fund.domain.repository.subscription.UserSubscriptionRepository;
import com.stock.fund.domain.service.riskalert.RiskAlertDomainService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class RiskAlertAppServiceImpl implements RiskAlertAppService {

    private static final Logger logger = LoggerFactory.getLogger(RiskAlertAppServiceImpl.class);

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
    private UserSubscriptionRepository userSubscriptionRepository;

    @Autowired
    private RiskAlertDomainService riskAlertDomainService;

    @Override
    @Transactional
    public RiskAlert createOrUpdateRiskAlert(RiskAlert riskAlert) {
        // 根据 userId, symbol, alertDate, timePoint 判断是否存在
        Optional<RiskAlert> existing = riskAlertRepository
                .findByUserIdAndSymbolAndAlertDateAndTimePoint(
                        riskAlert.getUserId(),
                        riskAlert.getSymbol(),
                        riskAlert.getAlertDate(),
                        riskAlert.getTimePoint());

        if (existing.isPresent()) {
            RiskAlert existingAlert = existing.get();
            existingAlert.setHasRisk(riskAlert.getHasRisk());
            existingAlert.setChangePercent(riskAlert.getChangePercent());
            existingAlert.setCurrentPrice(riskAlert.getCurrentPrice());
            existingAlert.setTriggeredAt(LocalDateTime.now());
            existingAlert.setIsRead(false); // 更新时重置为未读
            logger.info("更新风险提醒: symbol={}, date={}, timePoint={}",
                    riskAlert.getSymbol(), riskAlert.getAlertDate(), riskAlert.getTimePoint());
            return riskAlertRepository.update(existingAlert);
        } else {
            riskAlert.setIsRead(false);
            riskAlert.setTriggeredAt(LocalDateTime.now());
            logger.info("创建风险提醒: symbol={}, date={}, timePoint={}",
                    riskAlert.getSymbol(), riskAlert.getAlertDate(), riskAlert.getTimePoint());
            return riskAlertRepository.save(riskAlert);
        }
    }

    @Override
    public RiskAlertPageResponse<RiskAlert> queryRiskAlerts(RiskAlertQueryDTO query) {
        // 构建查询对象
        RiskAlertQuery queryObj = RiskAlertQuery.builder()
                .userId(query.getUserId())
                .startDate(query.getStartDate())
                .endDate(query.getEndDate())
                .page(query.getPage())
                .size(query.getSize())
                .sort(query.getSort())
                .build();

        List<RiskAlert> records = riskAlertRepository.findByUserIdWithPage(queryObj);

        long total = riskAlertRepository.countByUserId(queryObj);

        int pages = (int) Math.ceil((double) total / query.getSize());

        return RiskAlertPageResponse.<RiskAlert>of(records, total, query.getPage(), query.getSize());
    }

    @Override
    public List<RiskAlertSummaryDTO> getTodayRiskAlerts(Long userId) {
        LocalDate today = LocalDate.now();
        return getRiskAlertsByDateRange(userId, today, today);
    }

    @Override
    public List<RiskAlertSummaryDTO> getRiskAlertsByDateRange(Long userId, LocalDate startDate, LocalDate endDate) {
        List<RiskAlert> alerts = riskAlertRepository.findByUserIdAndDateRange(
                userId, startDate, endDate);

        // 按日期分组
        Map<LocalDate, List<RiskAlert>> byDate = alerts.stream()
                .collect(Collectors.groupingBy(RiskAlert::getAlertDate));

        List<RiskAlertSummaryDTO> result = new ArrayList<>();

        for (Map.Entry<LocalDate, List<RiskAlert>> entry : byDate.entrySet()) {
            LocalDate date = entry.getKey();
            List<RiskAlert> dateAlerts = entry.getValue();

            // 按 symbol 分组，每组取时间靠后的那条
            Map<String, List<RiskAlert>> bySymbol = dateAlerts.stream()
                    .collect(Collectors.groupingBy(RiskAlert::getSymbol));

            List<RiskAlertSummaryDTO.RiskAlertItemDTO> items = new ArrayList<>();
            int unreadCount = 0;

            for (Map.Entry<String, List<RiskAlert>> symbolEntry : bySymbol.entrySet()) {
                List<RiskAlert> symbolAlerts = symbolEntry.getValue();
                // 按时间倒序，取第一条（时间最靠后的）
                symbolAlerts.sort(Comparator.comparing(RiskAlert::getTimePoint).reversed());
                RiskAlert latest = symbolAlerts.get(0);

                RiskAlertSummaryDTO.RiskAlertItemDTO item = RiskAlertSummaryDTO.RiskAlertItemDTO.builder()
                        .id(latest.getId())
                        .symbol(latest.getSymbol())
                        .symbolType(latest.getSymbolType())
                        .symbolName(latest.getSymbolName())
                        .timePoint(latest.getTimePoint())
                        .hasRisk(latest.getHasRisk())
                        .changePercent(latest.getChangePercent())
                        .currentPrice(latest.getCurrentPrice())
                        .yesterdayClose(latest.getYesterdayClose())
                        .isRead(latest.getIsRead())
                        .triggeredAt(latest.getTriggeredAt() != null ? latest.getTriggeredAt().toString() : null)
                        .build();
                items.add(item);

                if (!Boolean.TRUE.equals(latest.getIsRead())) {
                    unreadCount++;
                }
            }

            RiskAlertSummaryDTO summary = RiskAlertSummaryDTO.builder()
                    .alertDate(date)
                    .totalCount(items.size())
                    .unreadCount(unreadCount)
                    .items(items)
                    .build();
            result.add(summary);
        }

        // 按日期倒序排列
        result.sort(Comparator.comparing(RiskAlertSummaryDTO::getAlertDate).reversed());

        return result;
    }

    @Override
    public long getUnreadCount(Long userId) {
        return riskAlertRepository.countUnreadByUserId(userId);
    }

    @Override
    public int getTodayRiskAlertCount(Long userId) {
        LocalDate today = LocalDate.now();
        List<RiskAlert> todayAlerts = riskAlertRepository.findByUserIdAndDateRange(
                userId, today, today);

        // 按 symbol 去重，一个股票/基金算一条
        Set<String> symbols = todayAlerts.stream()
                .map(RiskAlert::getSymbol)
                .collect(Collectors.toSet());

        return symbols.size();
    }

    @Override
    @Transactional
    public void markAsRead(Long riskAlertId) {
        riskAlertRepository.findById(riskAlertId).ifPresent(alert -> {
            alert.markAsRead();
            riskAlertRepository.update(alert);
            logger.info("标记风险提醒为已读: id={}", riskAlertId);
        });
    }

    @Override
    @Transactional
    public void markAllAsRead(Long userId) {
        riskAlertRepository.markAllAsRead(userId);
        logger.info("标记用户所有风险提醒为已读: userId={}", userId);
    }

    @Override
    public Optional<RiskAlert> getById(Long id) {
        return riskAlertRepository.findById(id);
    }

    @Override
    @Transactional
    public void checkAndCreateRiskAlerts(String timePoint) {
        logger.info("开始风险提醒检测, timePoint={}", timePoint);
        LocalDateTime now = LocalDateTime.now();

        // 检查所有激活的订阅
        List<UserSubscription> activeSubscriptions = userSubscriptionRepository.findActiveSubscriptions();

        for (UserSubscription subscription : activeSubscriptions) {
            try {
                // 获取当前价格
                BigDecimal currentPrice = getCurrentPrice(subscription.getSymbol(), subscription.getSymbolType());
                if (currentPrice == null) {
                    continue;
                }

                // 获取昨日收盘价
                BigDecimal yesterdayClose = getYesterdayClose(subscription.getSymbol(), subscription.getSymbolType());
                if (yesterdayClose == null) {
                    yesterdayClose = currentPrice; // 估算
                }

                // 处理风险（根据订阅的 targetChangePercent 判断）
                processSubscriptionRisk(subscription, currentPrice, yesterdayClose, timePoint);
            } catch (Exception e) {
                logger.error("检查订阅风险失败: symbol={}, type={}",
                        subscription.getSymbol(), subscription.getSymbolType(), e);
            }
        }

        logger.info("风险提醒检测完成");
    }

    @Override
    @Transactional
    public void checkAndCreateRiskAlerts() {
        // 根据当前时间自动判断时间点
        LocalDateTime now = LocalDateTime.now();
        String timePoint = now.getHour() < 12 ? "11:30" : "14:30";
        logger.info("自动判断风险提醒时间点: {}", timePoint);
        checkAndCreateRiskAlerts(timePoint);
    }

    @Override
    public List<RiskAlertMergeDTO> getMergedRiskAlerts(Long userId, Long cursor, int limit) {
        LocalDateTime cursorTime = cursor != null ?
                LocalDateTime.ofEpochSecond(cursor / 1000, 0, java.time.ZoneOffset.ofHours(8)) :
                LocalDateTime.now();

        List<RiskAlert> alerts = riskAlertRepository.findByUserId(userId, cursorTime.toLocalDate(), limit);

        // 按 symbol + date 分组
        Map<String, List<RiskAlert>> grouped = alerts.stream()
                .collect(Collectors.groupingBy(alert ->
                        alert.getSymbol() + "_" + alert.getAlertDate()));

        List<RiskAlertMergeDTO> result = new ArrayList<>();

        for (Map.Entry<String, List<RiskAlert>> entry : grouped.entrySet()) {
            List<RiskAlert> groupAlerts = entry.getValue();

            // 按触发时间倒序排列（处理null情况）
            groupAlerts.sort(Comparator.comparing(
                    RiskAlert::getTriggeredAt,
                    Comparator.nullsLast(Comparator.reverseOrder())));

            RiskAlert latest = groupAlerts.get(0);
            // 按时间点倒序（14:30 > 11:30），处理null情况
            groupAlerts.sort(Comparator.comparing(
                    RiskAlert::getTimePoint,
                    Comparator.nullsLast(Comparator.reverseOrder())));

            // 计算最大涨跌幅
            BigDecimal maxChangePercent = groupAlerts.stream()
                    .map(RiskAlert::getChangePercent)
                    .filter(Objects::nonNull)
                    .max(BigDecimal::compareTo)
                    .orElse(latest.getChangePercent());

            // 构建明细列表
            List<RiskAlertMergeDTO.RiskAlertDetailDTO> details = groupAlerts.stream()
                    .map(alert -> new RiskAlertMergeDTO.RiskAlertDetailDTO(
                            alert.getId(),
                            alert.getChangePercent(),
                            alert.getCurrentPrice(),
                            alert.getTriggeredAt(),
                            null))
                    .collect(Collectors.toList());

            // 如果alertDate为null，使用triggeredAt的日期作为fallback
            java.time.LocalDate alertDate = latest.getAlertDate() != null ?
                    latest.getAlertDate() : latest.getTriggeredAt().toLocalDate();

            result.add(new RiskAlertMergeDTO(
                    latest.getId(),
                    latest.getSymbol(),
                    latest.getSymbolType(),
                    latest.getSymbolName(),
                    alertDate,
                    groupAlerts.size(),
                    maxChangePercent,
                    latest.getChangePercent(),
                    latest.getCurrentPrice(),
                    latest.getYesterdayClose(),
                    latest.getIsRead(),
                    latest.getTriggeredAt(),
                    details));
        }

        // 按最新触发时间倒序（处理null情况）
        result.sort(Comparator.comparing(
                RiskAlertMergeDTO::latestTriggeredAt,
                Comparator.nullsLast(Comparator.reverseOrder())));

        return result;
    }

    @Override
    public void deleteById(Long id) {
        riskAlertRepository.deleteById(id);
        logger.info("删除风险提醒: id={}", id);
    }

    private BigDecimal getCurrentPrice(String symbol, String symbolType) {
        try {
            if ("STOCK".equals(symbolType)) {
                var stocks = stockRepository.findAll();
                var stockMap = stocks.stream().collect(Collectors.toMap(Stock::getSymbol, s -> s));
                var stock = stockMap.get(symbol);
                if (stock != null) {
                    var quotes = stockQuoteRepository.findAllLatestQuotes();
                    var quote = quotes.stream()
                            .filter(q -> stock.getId().equals(q.getStockId()))
                            .findFirst()
                            .orElse(null);
                    return quote != null ? quote.getClose() : null;
                }
            } else if ("FUND".equals(symbolType)) {
                var quotes = fundQuoteRepository.findAllLatestQuotes();
                var quote = quotes.stream()
                        .filter(q -> symbol.equals(q.getFundCode()))
                        .findFirst()
                        .orElse(null);
                return quote != null ? quote.getNav() : null;
            }
        } catch (Exception e) {
            logger.warn("获取当前价格失败: symbol={}, type={}", symbol, symbolType, e);
        }
        return null;
    }

    private BigDecimal getYesterdayClose(String symbol, String symbolType) {
        try {
            if ("STOCK".equals(symbolType)) {
                var stocks = stockRepository.findAll();
                var stockMap = stocks.stream().collect(Collectors.toMap(Stock::getSymbol, s -> s));
                var stock = stockMap.get(symbol);
                if (stock != null) {
                    var quotes = stockQuoteRepository.findAllLatestQuotes();
                    var quote = quotes.stream()
                            .filter(q -> stock.getId().equals(q.getStockId()))
                            .findFirst()
                            .orElse(null);
                    return quote != null ? quote.getClose() : null; // 简化处理
                }
            } else if ("FUND".equals(symbolType)) {
                var quotes = fundQuoteRepository.findAllLatestQuotes();
                var quote = quotes.stream()
                        .filter(q -> symbol.equals(q.getFundCode()))
                        .findFirst()
                        .orElse(null);
                return quote != null ? quote.getPrevNetValue() : null;
            }
        } catch (Exception e) {
            logger.warn("获取昨日收盘价失败: symbol={}, type={}", symbol, symbolType, e);
        }
        return null;
    }

    @Override
    @Transactional
    public void processSubscriptionRisk(UserSubscription subscription, BigDecimal currentPrice, BigDecimal yesterdayClose, String timePoint) {
        logger.info("处理订阅触发的风险: subscriptionId={}, symbol={}, currentPrice={}, timePoint={}",
                subscription.getId(), subscription.getSymbol(), currentPrice, timePoint);

        // 计算涨跌幅（用于记录）- 2位小数
        BigDecimal changePercent = BigDecimal.ZERO;
        boolean hasRisk = false;
        if (yesterdayClose != null && yesterdayClose.compareTo(BigDecimal.ZERO) != 0) {
            changePercent = currentPrice.subtract(yesterdayClose)
                    .divide(yesterdayClose, 4, RoundingMode.HALF_UP)
                    .multiply(new BigDecimal("100"))
                    .setScale(2, RoundingMode.HALF_UP);

            // 使用订阅的 targetChangePercent 判断是否触发风险
            if (subscription.getTargetChangePercent() != null) {
                hasRisk = Math.abs(changePercent.doubleValue()) >= subscription.getTargetChangePercent();
            }
        }

        if (!hasRisk) {
            logger.debug("价格未达到提醒条件，不创建风险记录: symbol={}, currentPrice={}, changePercent={}",
                    subscription.getSymbol(), currentPrice, changePercent);
            return;
        }

        // 使用传入的时间点
        LocalDateTime now = LocalDateTime.now();

        // 创建风险记录
        RiskAlert riskAlert = new RiskAlert();
        riskAlert.setUserId(subscription.getUserId());
        riskAlert.setSymbol(subscription.getSymbol());
        riskAlert.setSymbolType(subscription.getSymbolType());
        riskAlert.setAlertDate(now.toLocalDate());
        riskAlert.setTimePoint(timePoint);
        riskAlert.setHasRisk(true);
        riskAlert.setChangePercent(changePercent);
        riskAlert.setCurrentPrice(currentPrice);
        riskAlert.setYesterdayClose(yesterdayClose);

        // 获取标的名称（优先使用订阅中已保存的名称）
        String symbolName = subscription.getSymbolName();
        if (symbolName == null || symbolName.isEmpty()) {
            symbolName = fetchSymbolName(subscription.getSymbol(), subscription.getSymbolType());
        }
        riskAlert.setSymbolName(symbolName);

        createOrUpdateRiskAlert(riskAlert);
    }

    private String fetchSymbolName(String symbol, String symbolType) {
        try {
            if ("STOCK".equals(symbolType)) {
                var stock = stockRepository.findBySymbol(symbol);
                return stock.map(Stock::getName).orElse(symbol);
            } else if ("FUND".equals(symbolType)) {
                var fund = fundRepository.findByFundCode(symbol);
                return fund.map(Fund::getName).orElse(symbol);
            }
        } catch (Exception e) {
            logger.warn("获取标的名称失败: symbol={}, type={}", symbol, symbolType, e);
        }
        return symbol;
    }

    @Override
    @Transactional
    public BatchCreateRiskAlertResponse batchCreateRiskAlerts(BatchCreateRiskAlertRequest request) {
        logger.info("开始批量创建风险提醒: userId={}, symbolType={}, symbols={}", 
                request.getUserId(), request.getSymbolType(), request.getSymbols());
        
        List<RiskAlert> successList = new ArrayList<>();
        List<BatchCreateRiskAlertResponse.FailedRiskAlert> failList = new ArrayList<>();
        
        if (request.getSymbols() == null || request.getSymbols().isEmpty()) {
            logger.warn("标的列表为空");
            return BatchCreateRiskAlertResponse.builder()
                    .successCount(0)
                    .failCount(0)
                    .successList(successList)
                    .failList(failList)
                    .build();
        }
        
        // 获取当前时间点
        LocalDateTime now = LocalDateTime.now();
        String timePoint = now.getHour() < 12 ? "11:30" : "14:30";
        
        // 遍历每个标的，检测风险并创建风险提醒
        for (String symbol : request.getSymbols()) {
            try {
                // 获取当前价格
                BigDecimal currentPrice = getCurrentPrice(symbol, request.getSymbolType());
                if (currentPrice == null) {
                    failList.add(BatchCreateRiskAlertResponse.FailedRiskAlert.builder()
                            .symbol(symbol)
                            .reason("无法获取当前价格")
                            .build());
                    continue;
                }
                
                // 获取昨日收盘价
                BigDecimal yesterdayClose = getYesterdayClose(symbol, request.getSymbolType());
                if (yesterdayClose == null) {
                    yesterdayClose = currentPrice; // 估算
                }
                
                // 计算涨跌幅
                BigDecimal changePercent = BigDecimal.ZERO;
                if (yesterdayClose.compareTo(BigDecimal.ZERO) != 0) {
                    changePercent = currentPrice.subtract(yesterdayClose)
                            .divide(yesterdayClose, 4, RoundingMode.HALF_UP)
                            .multiply(new BigDecimal("100"))
                            .setScale(2, RoundingMode.HALF_UP);
                }
                
                // 判断是否有风险（涨跌幅超过1%）
                boolean hasRisk = Math.abs(changePercent.doubleValue()) >= 1.0;
                
                // 获取标的名称
                String symbolName = fetchSymbolName(symbol, request.getSymbolType());
                
                // 创建风险提醒
                RiskAlert riskAlert = new RiskAlert();
                riskAlert.setUserId(request.getUserId());
                riskAlert.setSymbol(symbol);
                riskAlert.setSymbolType(request.getSymbolType());
                riskAlert.setSymbolName(symbolName);
                riskAlert.setAlertDate(now.toLocalDate());
                riskAlert.setTimePoint(timePoint);
                riskAlert.setHasRisk(hasRisk);
                riskAlert.setChangePercent(changePercent);
                riskAlert.setCurrentPrice(currentPrice);
                riskAlert.setYesterdayClose(yesterdayClose);
                riskAlert.setIsRead(false);
                riskAlert.setTriggeredAt(now);
                
                RiskAlert saved = createOrUpdateRiskAlert(riskAlert);
                successList.add(saved);
                
                logger.info("成功创建风险提醒: symbol={}, hasRisk={}, changePercent={}", 
                        symbol, hasRisk, changePercent);
                
            } catch (Exception e) {
                logger.error("创建风险提醒失败: symbol={}", symbol, e);
                failList.add(BatchCreateRiskAlertResponse.FailedRiskAlert.builder()
                        .symbol(symbol)
                        .reason("创建失败: " + e.getMessage())
                        .build());
            }
        }
        
        BatchCreateRiskAlertResponse response = BatchCreateRiskAlertResponse.builder()
                .successCount(successList.size())
                .failCount(failList.size())
                .successList(successList)
                .failList(failList)
                .build();
        
        logger.info("批量创建风险提醒完成: 成功={}, 失败={}", 
                response.getSuccessCount(), response.getFailCount());
        
        return response;
    }
}
