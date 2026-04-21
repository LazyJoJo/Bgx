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

import com.stock.fund.application.service.riskalert.dto.RiskAlertMergeDTO;
import com.stock.fund.application.service.riskalert.dto.RiskAlertSummaryDTO;
import com.stock.fund.domain.entity.riskalert.RiskAlert;
import com.stock.fund.domain.repository.RiskAlertRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * RiskAlert 查询服务 - CQRS 模式
 * 
 * 将复杂的聚合查询逻辑从应用服务中分离出来， 按照 DDD 的 CQRS 模式，查询操作不经过完整的领域模型转换， 直接返回扁平化的
 * DTO，减少不必要的对象转换开销。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RiskAlertQueryService {

        private final RiskAlertRepository riskAlertRepository;

        /**
         * 获取今日风险提醒摘要（按日期和标的分组）
         */
        public List<RiskAlertSummaryDTO> getTodayRiskAlertSummary(Long userId) {
                LocalDate today = LocalDate.now();
                return getRiskAlertsByDateRangeSummary(userId, today, today);
        }

        /**
         * 获取日期范围内的风险提醒摘要（按日期和标的分组） 这是 getRiskAlertsByDateRange 的查询端实现
         */
        public List<RiskAlertSummaryDTO> getRiskAlertsByDateRangeSummary(Long userId, LocalDate startDate,
                        LocalDate endDate) {

                List<RiskAlert> alerts = riskAlertRepository.findByUserIdAndDateRange(userId, startDate, endDate);

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

                // 按日期倒序排列
                result.sort(Comparator.comparing(RiskAlertSummaryDTO::getAlertDate).reversed());

                return result;
        }

        /**
         * 获取合并后的风险提醒列表（按标的+日期分组） 这是 getMergedRiskAlerts 的查询端实现
         */
        public List<RiskAlertMergeDTO> getMergedRiskAlerts(Long userId, Long cursor, int limit) {
                LocalDate cursorDate = null;
                if (cursor != null) {
                        LocalDateTime cursorTime = LocalDateTime.ofEpochSecond(cursor / 1000, 0, ZoneOffset.ofHours(8));
                        cursorDate = cursorTime.toLocalDate();
                }

                List<RiskAlert> alerts = riskAlertRepository.findByUserId(userId, cursorDate, limit);

                // 按 symbol + date 分组
                Map<String, List<RiskAlert>> grouped = alerts.stream().collect(
                                Collectors.groupingBy(alert -> alert.getSymbol() + "_" + alert.getAlertDate()));

                List<RiskAlertMergeDTO> result = new ArrayList<>();

                for (Map.Entry<String, List<RiskAlert>> entry : grouped.entrySet()) {
                        List<RiskAlert> groupAlerts = entry.getValue();

                        // 按触发时间倒序排列（处理null情况）
                        groupAlerts.sort(Comparator.comparing(RiskAlert::getTriggeredAt,
                                        Comparator.nullsLast(Comparator.reverseOrder())));

                        RiskAlert latest = groupAlerts.get(0);
                        // 按时间点倒序（14:30 > 11:30），处理null情况
                        groupAlerts.sort(Comparator.comparing(RiskAlert::getTimePoint,
                                        Comparator.nullsLast(Comparator.reverseOrder())));

                        // 计算最大涨跌幅
                        BigDecimal maxChangePercent = groupAlerts.stream().map(RiskAlert::getChangePercent)
                                        .filter(Objects::nonNull).max(BigDecimal::compareTo)
                                        .orElse(latest.getChangePercent());

                        // 构建明细列表
                        List<RiskAlertMergeDTO.RiskAlertDetailDTO> details = groupAlerts.stream()
                                        .map(alert -> new RiskAlertMergeDTO.RiskAlertDetailDTO(alert.getId(),
                                                        alert.getChangePercent(), alert.getCurrentPrice(),
                                                        alert.getTriggeredAt(), null))
                                        .collect(Collectors.toList());

                        // 如果alertDate为null，使用triggeredAt的日期作为fallback
                        java.time.LocalDate alertDate = latest.getAlertDate() != null ? latest.getAlertDate()
                                        : latest.getTriggeredAt().toLocalDate();

                        result.add(new RiskAlertMergeDTO(latest.getId(), latest.getSymbol(), latest.getSymbolType(),
                                        latest.getSymbolName(), alertDate, groupAlerts.size(), maxChangePercent,
                                        latest.getChangePercent(), latest.getCurrentPrice(), latest.getYesterdayClose(),
                                        latest.getIsRead(), latest.getTriggeredAt(), details));
                }

                // 按最新触发时间倒序（处理null情况）
                result.sort(Comparator.comparing(RiskAlertMergeDTO::latestTriggeredAt,
                                Comparator.nullsLast(Comparator.reverseOrder())));

                return result;
        }
}
