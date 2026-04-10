package com.stock.fund.infrastructure.entity.alert;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 价格提醒持久化对象
 */
@TableName("price_alert")
@Data
public class PriceAlertPO {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;              // 用户ID

    @TableField("entity_code")
    private String symbol;            // 标的代码（股票代码或基金代码）

    @TableField("entity_type")
    private String symbolType;        // 标的类型：STOCK / FUND

    @TableField("entity_name")
    private String symbolName;        // 标的名称

    private String alertType;        // 提醒类型：PRICE_ABOVE / PRICE_BELOW / PERCENTAGE_CHANGE

    @TableField("threshold")
    private BigDecimal targetPrice;       // 目标价格/阈值

    private BigDecimal targetChangePercent; // 目标涨跌幅百分比

    private BigDecimal basePrice;        // 基准价格（用于涨跌幅计算）

    private BigDecimal currentValue;      // 当前值

    @TableField("is_active")
    private Boolean isActive;            // 状态：true=ACTIVE / false=INACTIVE

    private LocalDateTime lastTriggered; // 最后触发时间

    private String description;       // 提醒描述

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
