package com.stock.fund.domain.entity.alert;

import java.time.LocalDateTime;

import com.stock.fund.domain.entity.AggregateRoot;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 提醒实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class PriceAlert extends AggregateRoot<Long> {
    private Long userId; // 用户ID
    private String symbol; // 标的代码或基金代码
    private String symbolType; // 标的类型：stock/fund
    private String symbolName; // 标的名称
    private String alertType; // 提醒类型：PRICE_ABOVE/PRICE_BELOW/PERCENTAGE_CHANGE
    private Double targetPrice; // 目标价格
    private Double targetChangePercent; // 目标涨百分比
    private Double currentValue; // 当前值
    private String status; // 状态：ACTIVE/TRIGGERED/INACTIVE
    private LocalDateTime lastTriggered; // 最后触发时间
    private String description; // 提醒描述

    // 构造函数
    public PriceAlert() {
        this.status = "ACTIVE";
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public PriceAlert(Long userId, String symbol, String symbolType) {
        this();
        this.userId = userId;
        this.symbol = symbol;
        this.symbolType = symbolType;
    }

    // 业务方法
    public boolean shouldTrigger(Double currentPrice) {
        if (!"ACTIVE".equals(status) || currentPrice == null) {
            return false;
        }

        this.currentValue = currentPrice;

        if ("PRICE_ABOVE".equals(alertType)) {
            return currentPrice >= targetPrice;
        } else if ("PRICE_BELOW".equals(alertType)) {
            return currentPrice <= targetPrice;
        } else if ("PERCENTAGE_CHANGE".equals(alertType)) {
            // 涨跌幅逻辑需要根据当前价格计算
            return false; // 简化处理，实际需要根据历史价格计算
        }

        return false;
    }

    public void trigger() {
        this.lastTriggered = LocalDateTime.now();
        this.status = "TRIGGERED";
        this.updatedAt = LocalDateTime.now();
    }

    public void deactivate() {
        this.status = "INACTIVE";
        this.updatedAt = LocalDateTime.now();
    }

    public void activate() {
        this.status = "ACTIVE";
        this.updatedAt = LocalDateTime.now();
    }
}