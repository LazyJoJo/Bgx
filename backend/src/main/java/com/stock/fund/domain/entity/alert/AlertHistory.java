package com.stock.fund.domain.entity.alert;

import java.time.LocalDateTime;

import com.stock.fund.domain.entity.AggregateRoot;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 提醒历史实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class AlertHistory extends AggregateRoot<Long> {
    private Long userId; // 用户ID
    private Long alertId; // 提醒ID
    private String symbol; // 标的代码
    private String symbolType; // 标的类型：STOCK/FUND
    private String symbolName; // 标的名称
    private String alertType; // 提醒类型：PRICE_ABOVE/PRICE_BELOW/PERCENTAGE_CHANGE
    private Double targetPrice; // 目标价格
    private Double currentValue; // 触发时的值
    private LocalDateTime triggeredAt; // 触发时间
    private String triggerReason; // 触发原因

    // 构造函数
    public AlertHistory() {
        this.triggeredAt = LocalDateTime.now();
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public AlertHistory(Long userId, Long alertId, String symbol, String symbolType) {
        this();
        this.userId = userId;
        this.alertId = alertId;
        this.symbol = symbol;
        this.symbolType = symbolType;
    }

    // 工厂方法
    public static AlertHistory createFromAlert(PriceAlert alert, Double currentValue, String reason) {
        AlertHistory history = new AlertHistory();
        history.setUserId(alert.getUserId());
        history.setAlertId(alert.getId());
        history.setSymbol(alert.getSymbol());
        history.setSymbolName(alert.getSymbolName());
        history.setSymbolType(alert.getSymbolType());
        history.setAlertType(alert.getAlertType());
        history.setTargetPrice(alert.getTargetPrice());
        history.setCurrentValue(currentValue);
        history.setTriggerReason(reason);
        return history;
    }
}