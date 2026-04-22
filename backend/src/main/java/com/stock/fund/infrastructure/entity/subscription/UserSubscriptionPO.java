package com.stock.fund.infrastructure.entity.subscription;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;

/**
 * 用户订阅持久化对象 用于存储用户的提醒订阅设置
 */
@TableName("user_subscription")
@Data
public class UserSubscriptionPO {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId; // 用户ID

    @TableField("symbol")
    private String symbol; // 标的代码

    @TableField("symbol_type")
    private String symbolType; // 标的类型：STOCK / FUND

    @TableField("symbol_name")
    private String symbolName; // 标的名称

    @TableField("alert_type")
    private String alertType; // 监控类型：PERCENT（涨跌幅监控）/ AMOUNT（增减金额监控）

    @TableField("target_change_percent")
    private BigDecimal targetChangePercent; // 目标涨跌幅百分比

    @TableField("is_active")
    private Boolean isActive; // 状态：true=ACTIVE / false=INACTIVE

    private LocalDateTime lastTriggered; // 最后触发时间

    private String description; // 订阅描述

    private LocalDateTime createdAt; // 创建时间

    private LocalDateTime updatedAt; // 更新时间
}