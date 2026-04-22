package com.stock.fund.application.service.riskalert.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 风险提醒明细数据传输对象
 * <p>
 * 数值精度规范: - changePercent (涨跌幅): 2位小数 - currentPrice (当前价格): 2位小数
 */
public record RiskAlertDetailDTO(Long id, // 明细ID
        Long riskAlertId, // 关联的风险提醒ID
        String symbol, // 标的代码
        BigDecimal changePercent, // 涨跌幅 (2位小数)
        BigDecimal currentPrice, // 当前价格 (2位小数)
        LocalDateTime triggeredAt, // 触发时间
        String triggerReason, // 触发原因：PRICE_CHANGE / RISK_DETECTED / RISK_CLEARED
        String timePoint // 时间点：11:30 / 14:30
) {
}