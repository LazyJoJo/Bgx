package com.stock.fund.domain.entity.riskalert;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import com.stock.fund.domain.entity.AggregateRoot;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 风险提醒聚合根 系统自动检测股票/基金的涨跌幅风险，超阈值时创建
 * <p>
 * 数值精度规范: - changePercent (涨跌幅): 2位小数 - currentPrice (当前价格): 2位小数 -
 * yesterdayClose (昨日收盘价): 2位小数
 *
 * 记录规则： - 每个时间点(11:30/14:30)独立判断，有风险才记录 - 同一标的同一日期只有一条记录（更新最新），但历史保留 -
 * 汇总显示取时间靠后的那条
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class RiskAlert extends AggregateRoot<Long> {
    // Status constants
    public static final String STATUS_ACTIVE = "ACTIVE";
    public static final String STATUS_CLEARED = "CLEARED";
    public static final String STATUS_NO_ALERT = "NO_ALERT";

    private Long userId; // 用户ID
    private String symbol; // 标的代码
    private String symbolType; // 标的类型：STOCK/FUND
    private String symbolName; // 标的名称
    private LocalDate alertDate; // 风险日期
    private String timePoint; // 时间点：11:30 / 14:30
    private Boolean hasRisk; // 是否有风险
    private BigDecimal changePercent; // 涨跌幅（%，正数=上涨，负数=下跌）(2位小数)
    private BigDecimal currentPrice; // 触发时的价格 (2位小数)
    private BigDecimal yesterdayClose; // 昨日收盘价（计算基准）(2位小数)
    private Boolean isRead; // 是否已读
    private LocalDateTime triggeredAt; // 触发时间

    // === New fields for risk alert redesign ===

    /** 状态：ACTIVE（跟踪中）/ CLEARED（已解除）/ NO_ALERT（无风险） */
    private String status;

    /** 当日最高涨跌幅（2位小数） */
    private BigDecimal maxChangePercent;

    /** 当日最低涨跌幅（2位小数） */
    private BigDecimal minChangePercent;

    /** 最新明细ID */
    private Long latestDetailId;

    /** 监控类型：PERCENT（涨跌幅监控）/ AMOUNT（增减金额监控） */
    private String alertType;

    public RiskAlert() {
        this.isRead = false;
        this.hasRisk = true;
        this.triggeredAt = LocalDateTime.now();
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        // Default status is ACTIVE - new alerts start as active
        // Service layer explicitly sets status before saving
        this.status = STATUS_ACTIVE;
        this.maxChangePercent = BigDecimal.ZERO;
        this.minChangePercent = BigDecimal.ZERO;
        this.alertType = "PERCENT"; // Default to percent-based monitoring
    }

    public RiskAlert(Long userId, String symbol, String symbolType, String symbolName, LocalDate alertDate,
            String timePoint, Boolean hasRisk, BigDecimal changePercent, BigDecimal currentPrice,
            BigDecimal yesterdayClose) {
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
