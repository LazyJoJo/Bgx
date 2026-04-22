package com.stock.fund.application.service.riskalert.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 当日风险提醒汇总数据传输对象
 * <p>
 * 用于判断"暂时无风险"的汇总展示
 * <p>
 * 数值精度规范: - latestChangePercent (最新涨跌幅): 2位小数 - maxChangePercent (最大涨跌幅): 2位小数
 * - minChangePercent (最小涨跌幅): 2位小数 - currentPrice (当前价格): 2位小数 - yesterdayClose
 * (昨日收盘价): 2位小数
 */
public record RiskAlertTodaySummaryDTO(Long id, // 风险提醒ID
        String symbol, // 标的代码
        String symbolName, // 标的名称
        String symbolType, // 标的类型：STOCK/FUND
        LocalDate date, // 风险日期
        String status, // 状态：ACTIVE/CLEARED/NO_ALERT
        BigDecimal latestChangePercent, // 最新涨跌幅 (2位小数)
        BigDecimal maxChangePercent, // 最大涨跌幅 (2位小数)
        BigDecimal minChangePercent, // 最小涨跌幅 (2位小数)
        BigDecimal currentPrice, // 当前价格 (2位小数)
        BigDecimal yesterdayClose, // 昨日收盘价 (2位小数)
        Boolean isRead, // 是否已读
        Integer triggerCount, // 累计触发次数
        LocalDateTime latestTriggeredAt // 最新触发时间
) {
}