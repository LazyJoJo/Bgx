package com.stock.fund.application.service.riskalert.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 风险提醒合并后的数据传输对象
 * 用于前端展示，同一标的+同一天的多条提醒合并为一条
 * <p>
 * 数值精度规范:
 * - maxChangePercent (最大涨跌幅): 2位小数
 * - latestChangePercent (最新涨跌幅): 2位小数
 * - currentPrice (当前价格): 2位小数
 * - yesterdayClose (昨日收盘价): 2位小数
 */
public record RiskAlertMergeDTO(
    Long id,               // 合并后代表的风险提醒ID（取最新一条的ID）
    String symbol,          // 标的代码
    String symbolType,      // 标的类型：STOCK/FUND
    String symbolName,      // 标的名称
    LocalDate date,        // 触发日期
    Integer triggerCount,  // 当日累计触发次数
    BigDecimal maxChangePercent, // 最大涨跌幅（当日）(2位小数)
    BigDecimal latestChangePercent, // 最新涨跌幅 (2位小数)
    BigDecimal currentPrice,    // 当前价格 (2位小数)
    BigDecimal yesterdayClose,  // 昨日收盘价 (2位小数)
    Boolean isRead,        // 是否已读
    LocalDateTime latestTriggeredAt, // 最新触发时间
    List<RiskAlertDetailDTO> details // 当日所有明细，可展开
) {
    /**
     * 风险提醒明细
     */
    public record RiskAlertDetailDTO(
        Long id,
        BigDecimal changePercent,
        BigDecimal currentPrice,
        LocalDateTime triggeredAt,
        String triggerReason
    ) {}
}
