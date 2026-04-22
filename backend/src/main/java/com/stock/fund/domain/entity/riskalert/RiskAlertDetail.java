package com.stock.fund.domain.entity.riskalert;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.stock.fund.domain.entity.AggregateRoot;

/**
 * 风险提醒明细领域实体
 * <p>
 * 用于存储每次价格变动的快照记录，追踪涨跌幅变化过程
 */
public class RiskAlertDetail extends AggregateRoot<Long> {
    // Trigger reason constants
    public static final String REASON_PRICE_CHANGE = "PRICE_CHANGE";
    public static final String REASON_RISK_DETECTED = "RISK_DETECTED";
    public static final String REASON_RISK_CLEARED = "RISK_CLEARED";

    private Long riskAlertId; // 关联的风险提醒ID
    private String symbol; // 标的代码
    private BigDecimal changePercent; // 涨跌幅（%）（2位小数）
    private BigDecimal currentPrice; // 当前价格（2位小数）
    private LocalDateTime triggeredAt; // 触发时间
    private String triggerReason; // 触发原因：PRICE_CHANGE / RISK_DETECTED / RISK_CLEARED
    private String timePoint; // 时间点：11:30 / 14:30

    public RiskAlertDetail() {
        this.triggeredAt = LocalDateTime.now();
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public RiskAlertDetail(Long riskAlertId, String symbol, BigDecimal changePercent, BigDecimal currentPrice,
            LocalDateTime triggeredAt, String triggerReason, String timePoint) {
        this();
        this.riskAlertId = riskAlertId;
        this.symbol = symbol;
        this.changePercent = changePercent;
        this.currentPrice = currentPrice;
        this.triggeredAt = triggeredAt;
        this.triggerReason = triggerReason;
        this.timePoint = timePoint;
    }

    // Getters and Setters (Lombok @Data will generate these)

    public Long getRiskAlertId() {
        return riskAlertId;
    }

    public void setRiskAlertId(Long riskAlertId) {
        this.riskAlertId = riskAlertId;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public BigDecimal getChangePercent() {
        return changePercent;
    }

    public void setChangePercent(BigDecimal changePercent) {
        this.changePercent = changePercent;
    }

    public BigDecimal getCurrentPrice() {
        return currentPrice;
    }

    public void setCurrentPrice(BigDecimal currentPrice) {
        this.currentPrice = currentPrice;
    }

    public LocalDateTime getTriggeredAt() {
        return triggeredAt;
    }

    public void setTriggeredAt(LocalDateTime triggeredAt) {
        this.triggeredAt = triggeredAt;
    }

    public String getTriggerReason() {
        return triggerReason;
    }

    public void setTriggerReason(String triggerReason) {
        this.triggerReason = triggerReason;
    }

    public String getTimePoint() {
        return timePoint;
    }

    public void setTimePoint(String timePoint) {
        this.timePoint = timePoint;
    }
}
