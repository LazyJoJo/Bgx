package com.stock.fund.infrastructure.entity.riskalert;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;

/**
 * 风险提醒明细持久化对象
 * <p>
 * 用于存储每次价格变动的快照记录，追踪涨跌幅变化过程
 * <p>
 * 数值精度规范: - changePercent (涨跌幅): 2位小数 -> DECIMAL(10,2) - currentPrice (当前价格):
 * 2位小数 -> DECIMAL(10,2)
 */
@TableName("risk_alert_detail")
@Data
public class RiskAlertDetailPO {
    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("risk_alert_id")
    private Long riskAlertId; // 关联的风险提醒ID

    private String symbol; // 标的代码

    @TableField("change_percent")
    private BigDecimal changePercent; // 涨跌幅（%）（2位小数）

    @TableField("current_price")
    private BigDecimal currentPrice; // 当前价格（2位小数）

    @TableField("triggered_at")
    private LocalDateTime triggeredAt; // 触发时间

    @TableField("trigger_reason")
    private String triggerReason; // 触发原因：PRICE_CHANGE / RISK_DETECTED / RISK_CLEARED

    @TableField("time_point")
    private String timePoint; // 时间点：11:30 / 14:30

    public RiskAlertDetailPO() {
    }

    public RiskAlertDetailPO(Long riskAlertId, String symbol, BigDecimal changePercent, BigDecimal currentPrice,
            LocalDateTime triggeredAt, String triggerReason, String timePoint) {
        this.riskAlertId = riskAlertId;
        this.symbol = symbol;
        this.changePercent = changePercent;
        this.currentPrice = currentPrice;
        this.triggeredAt = triggeredAt;
        this.triggerReason = triggerReason;
        this.timePoint = timePoint;
    }
}
