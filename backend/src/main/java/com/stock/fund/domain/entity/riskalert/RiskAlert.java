package com.stock.fund.domain.entity.riskalert;

import com.stock.fund.domain.entity.AggregateRoot;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 风险提醒聚合根
 * 系统自动检测股票/基金的涨跌幅风险，超阈值时创建
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class RiskAlert extends AggregateRoot<Long> {
    private Long userId;                    // 用户ID
    private String symbol;                  // 标的代码
    private String symbolType;               // 标的类型：STOCK/FUND
    private String symbolName;              // 标的名称
    private Double changePercent;           // 涨跌幅（%，正数=上涨，负数=下跌）
    private Double currentPrice;            // 触发时的价格
    private Double yesterdayClose;          // 昨日收盘价（计算基准）
    private Integer triggerCount;           // 当日累计触发次数
    private Boolean isRead;                // 是否已读
    private LocalDateTime triggeredAt;      // 最新触发时间
    private String triggerReason;           // 触发原因

    public RiskAlert() {
        this.isRead = false;
        this.triggerCount = 1;
        this.triggeredAt = LocalDateTime.now();
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public RiskAlert(Long userId, String symbol, String symbolType, String symbolName,
                     Double changePercent, Double currentPrice, Double yesterdayClose) {
        this();
        this.userId = userId;
        this.symbol = symbol;
        this.symbolType = symbolType;
        this.symbolName = symbolName;
        this.changePercent = changePercent;
        this.currentPrice = currentPrice;
        this.yesterdayClose = yesterdayClose;
    }

    /**
     * 增加触发计数
     */
    public void incrementTriggerCount() {
        this.triggerCount++;
        this.triggeredAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 标记为已读
     */
    public void markAsRead() {
        this.isRead = true;
        this.updatedAt = LocalDateTime.now();
    }
}
