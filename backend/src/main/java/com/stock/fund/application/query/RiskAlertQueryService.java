package com.stock.fund.application.query;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.stock.fund.application.service.riskalert.dto.RiskAlertDetailDTO;
import com.stock.fund.application.service.riskalert.dto.RiskAlertMergeDTO;
import com.stock.fund.application.service.riskalert.dto.RiskAlertSummaryDTO;
import com.stock.fund.application.service.riskalert.dto.RiskAlertTodaySummaryDTO;
import com.stock.fund.domain.entity.riskalert.RiskAlert;
import com.stock.fund.domain.entity.riskalert.RiskAlertDetail;
import com.stock.fund.domain.repository.RiskAlertDetailRepository;
import com.stock.fund.domain.repository.RiskAlertRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * RiskAlert Query Service - CQRS Pattern
 * 
 * Separates complex aggregate queries from application services. Following DDD
 * CQRS pattern, query operations do not go through full domain model
 * transformation, directly returning flattened DTOs to reduce unnecessary
 * object conversion overhead.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RiskAlertQueryService {

        private final RiskAlertRepository riskAlertRepository;
        private final RiskAlertDetailRepository riskAlertDetailRepository;

        /**
         * Get today's risk alert summary (grouped by date and symbol)
         */
        public List<RiskAlertSummaryDTO> getTodayRiskAlertSummary(Long userId) {
                LocalDate today = LocalDate.now();
                return getRiskAlertsByDateRangeSummary(userId, today, today);
        }

        /**
         * Get risk alert summary for date range (grouped by date and symbol) This is
         * the query-side implementation for getRiskAlertsByDateRange
         */
        public List<RiskAlertSummaryDTO> getRiskAlertsByDateRangeSummary(Long userId, LocalDate startDate,
                        LocalDate endDate) {

                List<RiskAlert> alerts = riskAlertRepository.findByUserIdAndDateRange(userId, startDate, endDate);

                // Filter by status if needed (for merged query, only get ACTIVE/CLEARED alerts,
                // not NO_ALERT)
                alerts = alerts.stream().filter(a -> "ACTIVE".equals(a.getStatus()) || "CLEARED".equals(a.getStatus()))
                                .collect(Collectors.toList());

                // Group by date
                Map<LocalDate, List<RiskAlert>> byDate = alerts.stream()
                                .collect(Collectors.groupingBy(RiskAlert::getAlertDate));

                List<RiskAlertSummaryDTO> result = new ArrayList<>();

                for (Map.Entry<LocalDate, List<RiskAlert>> entry : byDate.entrySet()) {
                        LocalDate date = entry.getKey();
                        List<RiskAlert> dateAlerts = entry.getValue();

                        // Group by symbol, take the latest by timePoint
                        Map<String, List<RiskAlert>> bySymbol = dateAlerts.stream()
                                        .collect(Collectors.groupingBy(RiskAlert::getSymbol));

                        List<RiskAlertSummaryDTO.RiskAlertItemDTO> items = new ArrayList<>();
                        int unreadCount = 0;

                        for (Map.Entry<String, List<RiskAlert>> symbolEntry : bySymbol.entrySet()) {
                                List<RiskAlert> symbolAlerts = symbolEntry.getValue();
                                // Sort by timePoint descending, take first (latest)
                                symbolAlerts.sort(Comparator.comparing(RiskAlert::getTimePoint).reversed());
                                RiskAlert latest = symbolAlerts.get(0);

                                RiskAlertSummaryDTO.RiskAlertItemDTO item = RiskAlertSummaryDTO.RiskAlertItemDTO
                                                .builder().id(latest.getId()).symbol(latest.getSymbol())
                                                .symbolType(latest.getSymbolType()).symbolName(latest.getSymbolName())
                                                .timePoint(latest.getTimePoint()).hasRisk(latest.getHasRisk())
                                                .changePercent(latest.getChangePercent())
                                                .currentPrice(latest.getCurrentPrice())
                                                .yesterdayClose(latest.getYesterdayClose()).isRead(latest.getIsRead())
                                                .triggeredAt(latest.getTriggeredAt() != null
                                                                ? latest.getTriggeredAt().toString()
                                                                : null)
                                                .build();
                                items.add(item);

                                if (!Boolean.TRUE.equals(latest.getIsRead())) {
                                        unreadCount++;
                                }
                        }

                        RiskAlertSummaryDTO summary = RiskAlertSummaryDTO.builder().alertDate(date)
                                        .totalCount(items.size()).unreadCount(unreadCount).items(items).build();
                        result.add(summary);
                }

                // Sort by date descending
                result.sort(Comparator.comparing(RiskAlertSummaryDTO::getAlertDate).reversed());

                return result;
        }

        /**
         * Get merged risk alert list (grouped by symbol + date) This is the query-side
         * implementation for getMergedRiskAlerts
         */
        public List<RiskAlertMergeDTO> getMergedRiskAlerts(Long userId, Long cursor, int limit) {
                LocalDate cursorDate = null;
                if (cursor != null) {
                        LocalDateTime cursorTime = LocalDateTime.ofEpochSecond(cursor / 1000, 0, ZoneOffset.ofHours(8));
                        cursorDate = cursorTime.toLocalDate();
                }

                List<RiskAlert> alerts = riskAlertRepository.findByUserId(userId, cursorDate, limit);

                // Filter out NO_ALERT status and only keep ACTIVE/CLEARED
                alerts = alerts.stream().filter(a -> "ACTIVE".equals(a.getStatus()) || "CLEARED".equals(a.getStatus()))
                                .collect(Collectors.toList());

                // Group by symbol + date
                Map<String, List<RiskAlert>> grouped = alerts.stream().collect(
                                Collectors.groupingBy(alert -> alert.getSymbol() + "_" + alert.getAlertDate()));

                List<RiskAlertMergeDTO> result = new ArrayList<>();

                for (Map.Entry<String, List<RiskAlert>> entry : grouped.entrySet()) {
                        List<RiskAlert> groupAlerts = entry.getValue();

                        // Sort by triggeredAt descending (handle nulls)
                        groupAlerts.sort(Comparator.comparing(RiskAlert::getTriggeredAt,
                                        Comparator.nullsLast(Comparator.reverseOrder())));

                        RiskAlert latest = groupAlerts.get(0);
                        // Sort by timePoint descending (14:30 > 11:30), handle nulls
                        groupAlerts.sort(Comparator.comparing(RiskAlert::getTimePoint,
                                        Comparator.nullsLast(Comparator.reverseOrder())));

                        // Calculate max/min change percent
                        BigDecimal maxChangePercent = groupAlerts.stream().map(RiskAlert::getMaxChangePercent)
                                        .filter(Objects::nonNull).max(BigDecimal::compareTo)
                                        .orElse(latest.getChangePercent());

                        BigDecimal minChangePercent = groupAlerts.stream().map(RiskAlert::getMinChangePercent)
                                        .filter(Objects::nonNull).min(BigDecimal::compareTo)
                                        .orElse(latest.getChangePercent());

                        // Build detail list from risk_alert_detail table
                        List<RiskAlertDetail> detailEntities = riskAlertDetailRepository
                                        .findByRiskAlertId(latest.getId());
                        List<RiskAlertMergeDTO.RiskAlertDetailDTO> details = detailEntities.stream()
                                        .map(detail -> new RiskAlertMergeDTO.RiskAlertDetailDTO(detail.getId(),
                                                        detail.getChangePercent(), detail.getCurrentPrice(),
                                                        detail.getTriggeredAt(), detail.getTriggerReason()))
                                        .collect(Collectors.toList());

                        // If alertDate is null, use triggeredAt date as fallback
                        java.time.LocalDate alertDate = latest.getAlertDate() != null ? latest.getAlertDate()
                                        : latest.getTriggeredAt().toLocalDate();

                        result.add(new RiskAlertMergeDTO(latest.getId(), latest.getSymbol(), latest.getSymbolType(),
                                        latest.getSymbolName(), alertDate, latest.getStatus(), groupAlerts.size(),
                                        maxChangePercent, minChangePercent, latest.getChangePercent(),
                                        latest.getCurrentPrice(), latest.getYesterdayClose(), latest.getIsRead(),
                                        latest.getTriggeredAt(), details));
                }

                // Sort by latest triggered time descending (handle nulls)
                result.sort(Comparator.comparing(RiskAlertMergeDTO::latestTriggeredAt,
                                Comparator.nullsLast(Comparator.reverseOrder())));

                return result;
        }

        /**
         * Get details for a specific risk alert
         */
        public List<RiskAlertDetail> getRiskAlertDetails(Long riskAlertId) {
                return riskAlertDetailRepository.findByRiskAlertId(riskAlertId);
        }

        /**
         * Get today's risk alert summary for user (used to determine "no risk
         * temporarily")
         */
        public List<RiskAlertTodaySummaryDTO> getTodaySummary(Long userId) {
                LocalDate today = LocalDate.now();
                List<RiskAlert> alerts = riskAlertRepository.findByUserIdAndDateRange(userId, today, today);

                // Filter: only get ACTIVE/CLEARED alerts (not NO_ALERT)
                alerts = alerts.stream().filter(a -> "ACTIVE".equals(a.getStatus()) || "CLEARED".equals(a.getStatus()))
                                .collect(Collectors.toList());

                // Group by symbol (each symbol only has one summary entry per day)
                Map<String, List<RiskAlert>> bySymbol = alerts.stream()
                                .collect(Collectors.groupingBy(RiskAlert::getSymbol));

                List<RiskAlertTodaySummaryDTO> result = new ArrayList<>();

                for (Map.Entry<String, List<RiskAlert>> entry : bySymbol.entrySet()) {
                        List<RiskAlert> symbolAlerts = entry.getValue();

                        // Sort by timePoint descending, take the latest
                        symbolAlerts.sort(Comparator.comparing(RiskAlert::getTimePoint,
                                        Comparator.nullsLast(Comparator.reverseOrder())));
                        RiskAlert latest = symbolAlerts.get(0);

                        // Calculate trigger count from details
                        List<RiskAlertDetail> details = riskAlertDetailRepository.findByRiskAlertId(latest.getId());
                        int triggerCount = details.size();

                        result.add(new RiskAlertTodaySummaryDTO(latest.getId(), latest.getSymbol(),
                                        latest.getSymbolName(), latest.getSymbolType(),
                                        latest.getAlertDate() != null ? latest.getAlertDate() : today,
                                        latest.getStatus(), latest.getChangePercent(), latest.getMaxChangePercent(),
                                        latest.getMinChangePercent(), latest.getCurrentPrice(),
                                        latest.getYesterdayClose(), latest.getIsRead(), triggerCount,
                                        latest.getTriggeredAt()));
                }

                return result;
        }

        /**
         * Get alert detail list for a specific risk alert
         */
        public List<RiskAlertDetailDTO> getAlertDetailList(Long alertId) {
                List<RiskAlertDetail> details = riskAlertDetailRepository.findByRiskAlertId(alertId);
                return details.stream()
                                .map(detail -> new RiskAlertDetailDTO(detail.getId(), detail.getRiskAlertId(),
                                                detail.getSymbol(), detail.getChangePercent(), detail.getCurrentPrice(),
                                                detail.getTriggeredAt(), detail.getTriggerReason(),
                                                detail.getTimePoint()))
                                .collect(Collectors.toList());
        }
}
