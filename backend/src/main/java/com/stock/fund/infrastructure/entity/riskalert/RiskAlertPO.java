package com.stock.fund.infrastructure.entity.riskalert;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;

/**
 * 风险提醒持久化对象
 * <p>
 * 数值精度规范: - changePercent (涨跌幅): 2位小数 -> DECIMAL(10,2) - currentPrice (当前价格):
 * 2位小数 -> DECIMAL(10,2) - yesterdayClose (昨日收盘价): 2位小数 -> DECIMAL(10,2) -
 * maxChangePercent (当日最高涨跌幅): 2位小数 -> DECIMAL(10,2) - minChangePercent
 * (当日最低涨跌幅): 2位小数 -> DECIMAL(10,2)
 */
@TableName("risk_alert")
@Data
public class RiskAlertPO {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId; // 用户ID
    private String symbol; // 标的代码
    private String symbolType; // 标的类型：STOCK/FUND
    private String symbolName; // 标的名称
    private LocalDate alertDate; // 风险日期
    private String timePoint; // 时间点：11:30 / 14:30
    private Boolean hasRisk; // 是否有风险

    @TableField("change_percent")
    private BigDecimal changePercent; // 涨跌幅（%）（2位小数）

    @TableField("current_price")
    private BigDecimal currentPrice; // 触发时的价格（2位小数）

    @TableField("yesterday_close")
    private BigDecimal yesterdayClose; // 昨日收盘价（2位小数）

    @TableField("is_read")
    private Boolean isRead; // 是否已读
    private LocalDateTime triggeredAt; // 触发时间
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // === New fields for risk alert redesign ===

    /** 状态：ACTIVE（跟踪中）/ CLEARED（已解除）/ NO_ALERT（无风险） */
    private String status;

    @TableField("max_change_percent")
    private BigDecimal maxChangePercent; // 当日最高涨跌幅（2位小数）

    @TableField("min_change_percent")
    private BigDecimal minChangePercent; // 当日最低涨跌幅（2位小数）

    @TableField("latest_detail_id")
    private Long latestDetailId; // 最新明细ID

    /** 监控类型：PERCENT（涨跌幅监控）/ AMOUNT（增减金额监控） */
    @TableField("alert_type")
    private String alertType;

    // === Associations ===

    /** 最新明细对象（非数据库字段，用于关联查询） */
    @TableField(exist = false)
    private RiskAlertDetailPO latestDetail;

    /** 明细列表（非数据库字段，用于一对多关联） */
    @TableField(exist = false)
    private List<RiskAlertDetailPO> details;

    public RiskAlertPO() {
    }
}
