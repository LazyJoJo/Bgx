package com.stock.fund.domain.entity.subscription;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.stock.fund.domain.entity.AggregateRoot;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 用户订阅实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class UserSubscription extends AggregateRoot<Long> {
    private Long userId; // 用户ID
    private String symbol; // 标的代码
    private String symbolType; // 标的类型：STOCK/FUND
    private String symbolName; // 标的名称
    private String alertType; // 监控类型：PERCENT（涨跌幅监控）/ AMOUNT（增减金额监控）
    private BigDecimal targetChangePercent; // 目标涨跌幅百分比
    private Boolean isActive; // 是否激活
    private LocalDateTime lastTriggered; // 最后触发时间
    private String description; // 订阅描述

    // 构造函数
    public UserSubscription() {
        this.isActive = true;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public UserSubscription(Long userId, String symbol, String symbolType) {
        this();
        this.userId = userId;
        this.symbol = symbol;
        this.symbolType = symbolType;
    }

    // 业务方法
    public void activate() {
        this.isActive = true;
        this.updatedAt = LocalDateTime.now();
    }

    public void deactivate() {
        this.isActive = false;
        this.updatedAt = LocalDateTime.now();
    }

    public void markTriggered() {
        this.lastTriggered = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 获取订阅状态字符串，用于 API 响应序列化 前端 Subscription.status 期望 'ACTIVE' | 'INACTIVE' |
     * 'TRIGGERED'
     */
    @JsonProperty("status")
    public String getStatus() {
        return Boolean.TRUE.equals(this.isActive) ? "ACTIVE" : "INACTIVE";
    }
}
