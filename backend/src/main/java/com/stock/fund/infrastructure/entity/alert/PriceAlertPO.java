package com.stock.fund.infrastructure.entity.alert;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
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

    private String symbol;            // 标的代码（股票代码或基金代码）

    private String symbolType;        // 标的类型：STOCK / FUND

    private String symbolName;        // 标的名称

    private String alertType;        // 提醒类型：PRICE_ABOVE / PRICE_BELOW / PERCENTAGE_CHANGE

    private Double targetPrice;       // 目标价格

    private Double targetChangePercent; // 目标涨跌幅百分比

    private Double basePrice;        // 基准价格（用于涨跌幅计算）

    private Double currentValue;      // 当前值

    private String status;            // 状态：ACTIVE / TRIGGERED / INACTIVE

    private LocalDateTime lastTriggered; // 最后触发时间

    private String description;       // 提醒描述

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
