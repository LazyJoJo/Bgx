package com.stock.fund.domain.entity.riskalert;

import com.stock.fund.domain.entity.AggregateRoot;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 风险提醒聚合根
 * 系统自动检测股票/基金的涨跌幅风险，超阈值时创建
 *
 * 记录规则：
 * - 每个时间点(11:30/14:30)独立判断，有风险才记录
 * - 同一标的同一日期只有一条记录（更新最新），但历史保留
 * - 汇总显示取时间靠后的那条
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class RiskAlert extends AggregateRoot<Long> {
    private Long userId;                    // 用户ID
    private String symbol;                   // 标的代码
    private String symbolType;              // 标的类型：STOCK/FUND
    private String symbolName;              // 标的名称
    private LocalDate alertDate;           // 风险日期
    private String timePoint;              // 时间点：11:30 / 14:30
    private Boolean hasRisk;               // 是否有风险
    private Double changePercent;           // 涨跌幅（%，正数=上涨，负数=下跌）
    private Double currentPrice;            // 触发时的价格
    private Double yesterdayClose;          // 昨日收盘价（计算基准）
    private Boolean isRead;                // 是否已读
    private LocalDateTime triggeredAt;      // 触发时间

    public RiskAlert() {
        this.isRead = false;
        this.hasRisk = true;
        this.triggeredAt = LocalDateTime.now();
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public RiskAlert(Long userId, String symbol, String symbolType, String symbolName,
                     LocalDate alertDate, String timePoint,
                     Boolean hasRisk, Double changePercent,
                     Double currentPrice, Double yesterdayClose) {
        this();
        this.userId = userId;
        this.symbol = symbol;
        this.symbolType = symbolType;
        this.symbolName = symbolName;
        this.alertDate = alertDate;
        this.timePoint = timePoint;
        this.hasRisk = hasRisk;
        this.changePercent = changePercent;
        this.currentPrice = currentPrice;
        this.yesterdayClose = yesterdayClose;
    }

    /**
     * 标记为已读
     */
    public void markAsRead() {
        this.isRead = true;
        this.updatedAt = LocalDateTime.now();
    }
}
