package com.stock.fund.application.service.riskalert.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.stock.fund.application.query.RiskAlertQueryService;
import com.stock.fund.application.service.riskalert.RiskAlertAppService;
import com.stock.fund.application.service.riskalert.dto.BatchCreateRiskAlertRequest;
import com.stock.fund.application.service.riskalert.dto.BatchCreateRiskAlertResponse;
import com.stock.fund.application.service.riskalert.dto.RiskAlertMergeDTO;
import com.stock.fund.application.service.riskalert.dto.RiskAlertPageResponse;
import com.stock.fund.application.service.riskalert.dto.RiskAlertQueryDTO;
import com.stock.fund.application.service.riskalert.dto.RiskAlertSummaryDTO;
import com.stock.fund.application.service.riskalert.push.RiskAlertPushService;
import com.stock.fund.application.service.riskalert.push.dto.NewAlertPayload;
import com.stock.fund.application.service.riskalert.push.dto.RiskClearedPayload;
import com.stock.fund.application.service.riskalert.push.dto.UnreadCountPayload;
import com.stock.fund.domain.entity.Fund;
import com.stock.fund.domain.entity.Stock;
import com.stock.fund.domain.entity.riskalert.RiskAlert;
import com.stock.fund.domain.entity.subscription.UserSubscription;
import com.stock.fund.domain.repository.FundQuoteRepository;
import com.stock.fund.domain.repository.FundRepository;
import com.stock.fund.domain.repository.RiskAlertQuery;
import com.stock.fund.domain.repository.RiskAlertRepository;
import com.stock.fund.domain.repository.StockQuoteRepository;
import com.stock.fund.domain.repository.StockRepository;
import com.stock.fund.domain.repository.subscription.UserSubscriptionRepository;
import com.stock.fund.domain.service.riskalert.RiskAlertDomainService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class RiskAlertAppServiceImpl implements RiskAlertAppService {

    private final RiskAlertRepository riskAlertRepository;
    private final StockQuoteRepository stockQuoteRepository;
    private final FundQuoteRepository fundQuoteRepository;
    private final StockRepository stockRepository;
    private final FundRepository fundRepository;
    private final UserSubscriptionRepository userSubscriptionRepository;
    private final RiskAlertDomainService riskAlertDomainService;
    private final RiskAlertQueryService riskAlertQueryService;
    private final RiskAlertPushService riskAlertPushService;

    @Override
    @Transactional
    public RiskAlert createOrUpdateRiskAlert(RiskAlert riskAlert) {
        // Check if exists by userId, symbol, alertDate, timePoint
        Optional<RiskAlert> existing = riskAlertRepository.findByUserIdAndSymbolAndAlertDateAndTimePoint(
                riskAlert.getUserId(), riskAlert.getSymbol(), riskAlert.getAlertDate(), riskAlert.getTimePoint());

        boolean isUpdate = existing.isPresent();
        RiskAlert savedAlert;

        if (isUpdate) {
            RiskAlert existingAlert = existing.get();
            existingAlert.setHasRisk(riskAlert.getHasRisk());
            existingAlert.setChangePercent(riskAlert.getChangePercent());
            existingAlert.setCurrentPrice(riskAlert.getCurrentPrice());
            existingAlert.setTriggeredAt(LocalDateTime.now());
            existingAlert.setIsRead(false); // Reset to unread on update
            log.info("Updating risk alert: symbol={}, date={}, timePoint={}", riskAlert.getSymbol(),
                    riskAlert.getAlertDate(), riskAlert.getTimePoint());
            savedAlert = riskAlertRepository.update(existingAlert);

            // Push new_alert (update existing entry: time, change percent, trigger count,
            // etc.)
            pushNewAlert(riskAlert.getUserId(), savedAlert);
            pushUnreadCountChange(riskAlert.getUserId(), savedAlert);
        } else {
            riskAlert.setIsRead(false);
            riskAlert.setTriggeredAt(LocalDateTime.now());
            log.info("Creating risk alert: symbol={}, date={}, timePoint={}", riskAlert.getSymbol(),
                    riskAlert.getAlertDate(), riskAlert.getTimePoint());
            savedAlert = riskAlertRepository.save(riskAlert);

            // Push new_alert (new alert: frontend needs complete data to render new card)
            pushNewAlert(riskAlert.getUserId(), savedAlert);
            pushUnreadCountChange(riskAlert.getUserId(), savedAlert);
        }

        return savedAlert;
    }

    /**
     * 推送新风险提醒到 SSE 客户端
     */
    private void pushNewAlert(Long userId, RiskAlert alert) {
        try {
            // Build detail list for the pushed alert (contains at least the current
            // trigger)
            java.util.List<com.stock.fund.application.service.riskalert.push.dto.RiskAlertDetailPayload> details = java.util.Collections
                    .singletonList(com.stock.fund.application.service.riskalert.push.dto.RiskAlertDetailPayload
                            .builder().id(alert.getId()).changePercent(alert.getChangePercent())
                            .currentPrice(alert.getCurrentPrice())
                            .triggeredAt(com.stock.fund.application.service.riskalert.push.dto.NewAlertPayload
                                    .formatTime(alert.getTriggeredAt()))
                            .triggerReason("PRICE_CHANGE").build());

            // Fallback date: use alertDate if available, otherwise use triggeredAt date
            String dateStr = alert.getAlertDate() != null ? alert.getAlertDate().toString()
                    : (alert.getTriggeredAt() != null ? alert.getTriggeredAt().toLocalDate().toString() : null);

            NewAlertPayload payload = NewAlertPayload.builder().id(alert.getId()) // 数据库真实 ID
                    .symbol(alert.getSymbol()).symbolName(alert.getSymbolName()).symbolType(alert.getSymbolType())
                    .date(dateStr).latestChangePercent(alert.getChangePercent())
                    .maxChangePercent(alert.getChangePercent()).currentPrice(alert.getCurrentPrice())
                    .yesterdayClose(alert.getYesterdayClose())
                    .latestTriggeredAt(NewAlertPayload.formatTime(alert.getTriggeredAt())).triggerCount(1).isRead(false) // TODO:
                                                                                                                         // RiskAlert
                                                                                                                         // entity
                                                                                                                         // lacks
                                                                                                                         // triggerCount
                                                                                                                         // field,
                                                                                                                         // always
                                                                                                                         // 1
                                                                                                                         // for
                                                                                                                         // now
                    .details(details).build();
            riskAlertPushService.pushNewAlert(userId, payload);
            log.debug("Pushed new alert SSE event: userId={}, symbol={}", userId, alert.getSymbol());
        } catch (Exception e) {
            log.warn("Failed to push new alert: userId={}, symbol={}, error={}", userId, alert.getSymbol(),
                    e.getMessage());
        }
    }

    /**
     * 推送未读数变化到 SSE 客户端
     */
    private void pushUnreadCountChange(Long userId, RiskAlert alert) {
        try {
            long unreadCount = riskAlertRepository.countUnreadByUserId(userId);
            UnreadCountPayload payload = UnreadCountPayload.of(unreadCount, 1, "NEW_ALERT");
            riskAlertPushService.pushUnreadCountChange(userId, payload);
            log.debug("Pushed unread count change SSE event: userId={}, unreadCount={}", userId, unreadCount);
        } catch (Exception e) {
            log.warn("Failed to push unread count change: userId={}, symbol={}, error={}", userId, alert.getSymbol(),
                    e.getMessage());
        }
    }

    @Override
    public RiskAlertPageResponse<RiskAlert> queryRiskAlerts(RiskAlertQueryDTO query) {
        // 构建查询对象
        RiskAlertQuery queryObj = RiskAlertQuery.builder().userId(query.getUserId()).startDate(query.getStartDate())
                .endDate(query.getEndDate()).page(query.getPage()).size(query.getSize()).sort(query.getSort()).build();

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
        // CQRS: 委托给查询服务处理复杂聚合查询
        return riskAlertQueryService.getRiskAlertsByDateRangeSummary(userId, startDate, endDate);
    }

    @Override
    public long getUnreadCount(Long userId) {
        return riskAlertRepository.countUnreadByUserId(userId);
    }

    @Override
    public int getTodayRiskAlertCount(Long userId) {
        LocalDate today = LocalDate.now();
        List<RiskAlert> todayAlerts = riskAlertRepository.findByUserIdAndDateRange(userId, today, today);

        // 按 symbol 去重，一个股票/基金算一条
        Set<String> symbols = todayAlerts.stream().map(RiskAlert::getSymbol).collect(Collectors.toSet());

        return symbols.size();
    }

    @Override
    @Transactional
    public void markAsRead(Long riskAlertId) {
        riskAlertRepository.findById(riskAlertId).ifPresent(alert -> {
            alert.markAsRead();
            riskAlertRepository.update(alert);
            log.info("Marked risk alert as read: id={}", riskAlertId);
        });
    }

    @Override
    @Transactional
    public void markAllAsRead(Long userId) {
        riskAlertRepository.markAllAsRead(userId);
        log.info("Marked all risk alerts as read for user: userId={}", userId);

        // 推送 unread_count_change 到 SSE 客户端
        pushMarkAllRead(userId);
    }

    /**
     * 推送全部已读未读数变化到 SSE 客户端
     */
    private void pushMarkAllRead(Long userId) {
        try {
            UnreadCountPayload payload = UnreadCountPayload.of(0, 0, "MARK_READ");
            riskAlertPushService.pushUnreadCountChange(userId, payload);
            log.debug("Pushed mark-all-read unread count change SSE event: userId={}", userId);
        } catch (Exception e) {
            log.warn("Failed to push mark-all-read unread count change: userId={}, error={}", userId, e.getMessage());
        }
    }

    @Override
    public Optional<RiskAlert> getById(Long id) {
        return riskAlertRepository.findById(id);
    }

    @Override
    @Transactional
    public void checkAndCreateRiskAlerts(String timePoint) {
        log.info("Starting risk alert detection, timePoint={}", timePoint);
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
                processSubscriptionRisk(subscription, currentPrice, timePoint);
            } catch (Exception e) {
                log.error("Failed to check subscription risk: symbol={}, type={}", subscription.getSymbol(),
                        subscription.getSymbolType(), e);
            }
        }

        log.info("Risk alert detection completed");
    }

    @Override
    @Transactional
    public void checkAndCreateRiskAlerts() {
        // 根据当前时间自动判断时间点
        LocalDateTime now = LocalDateTime.now();
        String timePoint = now.getHour() < 12 ? "11:30" : "14:30";
        log.info("Auto-determining risk alert time point: {}", timePoint);
        checkAndCreateRiskAlerts(timePoint);
    }

    @Override
    public List<RiskAlertMergeDTO> getMergedRiskAlerts(Long userId, Long cursor, int limit) {
        // CQRS: 委托给查询服务处理复杂聚合查询
        return riskAlertQueryService.getMergedRiskAlerts(userId, cursor, limit);
    }

    @Override
    public void deleteById(Long id) {
        riskAlertRepository.deleteById(id);
        log.info("Deleting risk alert: id={}", id);
    }

    private BigDecimal getCurrentPrice(String symbol, String symbolType) {
        try {
            if ("STOCK".equals(symbolType)) {
                var stocks = stockRepository.findAll();
                var stockMap = stocks.stream().collect(Collectors.toMap(Stock::getSymbol, s -> s));
                var stock = stockMap.get(symbol);
                if (stock != null) {
                    var quotes = stockQuoteRepository.findAllLatestQuotes();
                    var quote = quotes.stream().filter(q -> stock.getId().equals(q.getStockId())).findFirst()
                            .orElse(null);
                    return quote != null ? quote.getClose() : null;
                }
            } else if ("FUND".equals(symbolType)) {
                var quotes = fundQuoteRepository.findAllLatestQuotes();
                var quote = quotes.stream().filter(q -> symbol.equals(q.getFundCode())).findFirst().orElse(null);
                return quote != null ? quote.getNav() : null;
            }
        } catch (Exception e) {
            log.warn("Failed to get current price: symbol={}, type={}", symbol, symbolType, e);
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
                    var quote = quotes.stream().filter(q -> stock.getId().equals(q.getStockId())).findFirst()
                            .orElse(null);
                    if (quote != null && quote.getClose() != null && quote.getChange() != null) {
                        // 利用涨跌额反推昨日收盘价：prevClose = close - change
                        return quote.getClose().subtract(quote.getChange());
                    }
                }
            } else if ("FUND".equals(symbolType)) {
                var quotes = fundQuoteRepository.findAllLatestQuotes();
                var quote = quotes.stream().filter(q -> symbol.equals(q.getFundCode())).findFirst().orElse(null);
                return quote != null ? quote.getPrevNetValue() : null;
            }
        } catch (Exception e) {
            log.warn("Failed to get yesterday close price: symbol={}, type={}", symbol, symbolType, e);
        }
        return null;
    }

    @Override
    @Transactional
    public void processSubscriptionRisk(UserSubscription subscription, BigDecimal currentPrice, String timePoint) {
        log.info("Processing subscription-triggered risk: subscriptionId={}, symbol={}, currentPrice={}, timePoint={}",
                subscription.getId(), subscription.getSymbol(), currentPrice, timePoint);

        // 获取昨日收盘价
        BigDecimal yesterdayClose = getYesterdayClose(subscription.getSymbol(), subscription.getSymbolType());

        // 计算涨跌幅（用于记录）- 2位小数
        BigDecimal changePercent = BigDecimal.ZERO;
        boolean hasRisk = false;
        if (yesterdayClose != null && yesterdayClose.compareTo(BigDecimal.ZERO) != 0) {
            changePercent = currentPrice.subtract(yesterdayClose).divide(yesterdayClose, 4, RoundingMode.HALF_UP)
                    .multiply(new BigDecimal("100")).setScale(2, RoundingMode.HALF_UP);

            // 使用订阅的 targetChangePercent 判断是否触发风险
            if (subscription.getTargetChangePercent() != null) {
                hasRisk = changePercent.abs().compareTo(subscription.getTargetChangePercent()) >= 0;
            }
        }

        LocalDateTime now = LocalDateTime.now();

        if (!hasRisk) {
            // 风险条件不再满足，检查是否存在之前创建的风险提醒记录
            LocalDate today = now.toLocalDate();
            Optional<RiskAlert> existingAlert = riskAlertRepository.findByUserIdAndSymbolAndAlertDateAndTimePoint(
                    subscription.getUserId(), subscription.getSymbol(), today, timePoint);

            if (existingAlert.isPresent() && Boolean.TRUE.equals(existingAlert.get().getHasRisk())) {
                // 之前有风险提醒，现在风险消除了
                RiskAlert oldAlert = existingAlert.get();
                log.info("Risk cleared for symbol={}, was at {}%, now at {}%", subscription.getSymbol(),
                        oldAlert.getChangePercent(), changePercent);

                // 构建风险消除事件
                RiskClearedPayload clearedPayload = RiskClearedPayload.builder().id(oldAlert.getId())
                        .symbol(oldAlert.getSymbol()).symbolName(oldAlert.getSymbolName())
                        .symbolType(oldAlert.getSymbolType())
                        .date(oldAlert.getAlertDate() != null ? oldAlert.getAlertDate().toString() : today.toString())
                        .lastChangePercent(oldAlert.getChangePercent()).currentChangePercent(changePercent)
                        .currentPrice(currentPrice).latestTriggeredAt(NewAlertPayload.formatTime(now)).build();

                // 删除旧的风险提醒记录
                riskAlertRepository.deleteById(oldAlert.getId());

                // 推送风险消除事件
                riskAlertPushService.pushRiskCleared(subscription.getUserId(), clearedPayload);

                // 更新未读数
                long unreadCount = riskAlertRepository.countUnreadByUserId(subscription.getUserId());
                UnreadCountPayload unreadPayload = UnreadCountPayload.of(unreadCount, -1, "RISK_CLEARED");
                riskAlertPushService.pushUnreadCountChange(subscription.getUserId(), unreadPayload);
            }

            log.debug(
                    "Price did not meet alert condition, not creating risk record: symbol={}, currentPrice={}, changePercent={}",
                    subscription.getSymbol(), currentPrice, changePercent);
            return;
        }

        // 使用传入的时间点
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
            log.warn("Failed to get symbol name: symbol={}, type={}", symbol, symbolType, e);
        }
        return symbol;
    }

    @Override
    @Transactional
    public BatchCreateRiskAlertResponse batchCreateRiskAlerts(BatchCreateRiskAlertRequest request) {
        log.info("Starting batch create risk alerts: userId={}, symbolType={}, symbols={}", request.getUserId(),
                request.getSymbolType(), request.getSymbols());

        List<RiskAlert> successList = new ArrayList<>();
        List<BatchCreateRiskAlertResponse.FailedRiskAlert> failList = new ArrayList<>();

        if (request.getSymbols() == null || request.getSymbols().isEmpty()) {
            log.warn("Symbol list is empty");
            return BatchCreateRiskAlertResponse.builder().successCount(0).failCount(0).successList(successList)
                    .failList(failList).build();
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
                    failList.add(BatchCreateRiskAlertResponse.FailedRiskAlert.builder().symbol(symbol)
                            .reason("无法获取当前价格").build());
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
                            .divide(yesterdayClose, 4, RoundingMode.HALF_UP).multiply(new BigDecimal("100"))
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

                log.info("Successfully created risk alert: symbol={}, hasRisk={}, changePercent={}", symbol, hasRisk,
                        changePercent);

            } catch (Exception e) {
                log.error("Failed to create risk alert: symbol={}", symbol, e);
                failList.add(BatchCreateRiskAlertResponse.FailedRiskAlert.builder().symbol(symbol)
                        .reason("创建失败: " + e.getMessage()).build());
            }
        }

        BatchCreateRiskAlertResponse response = BatchCreateRiskAlertResponse.builder().successCount(successList.size())
                .failCount(failList.size()).successList(successList).failList(failList).build();

        log.info("Batch create risk alerts completed: success={}, failed={}", response.getSuccessCount(),
                response.getFailCount());

        return response;
    }
}
