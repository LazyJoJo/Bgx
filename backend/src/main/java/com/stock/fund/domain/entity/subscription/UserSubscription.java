package com.stock.fund.domain.entity.subscription;

import com.stock.fund.domain.entity.AggregateRoot;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.time.LocalDateTime;

/**
 * 用户订阅实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class UserSubscription extends AggregateRoot<Long> {
    private Long userId;                         // 用户ID
    private String symbol;                        // 标的代码
    private String symbolType;                   // 标的类型：STOCK/FUND
    private String symbolName;                   // 标的名称
    private Double targetChangePercent;           // 目标涨跌幅百分比
    private Boolean isActive;                    // 是否激活
    private LocalDateTime lastTriggered;         // 最后触发时间
    private String description;                   // 订阅描述

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
}