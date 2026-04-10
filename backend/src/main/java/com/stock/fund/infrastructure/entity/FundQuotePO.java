package com.stock.fund.infrastructure.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * 基金净值实体
 * <p>
 * 数值精度规范:
 * - nav (单位净值): 4位小数 -> DECIMAL(10,4)
 * - prevNetValue (昨日净值): 4位小数 -> DECIMAL(10,4)
 * - changeAmount (涨跌额): 4位小数 -> DECIMAL(10,4)
 * - changePercent (涨跌幅): 2位小数 -> DECIMAL(10,2)
 */
@TableName("fund_quote")
@Data
public class FundQuotePO {
    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("fund_code")
    private String fundCode; // 基金代码

    @TableField("fund_name")
    private String fundName; // 基金名称

    @TableField("quote_date")
    private LocalDate quoteDate; // 报价日期

    @TableField("quote_time_only")
    private LocalTime quoteTimeOnly; // 报价时间(仅时分秒)

    @TableField("nav")
    private BigDecimal nav;           // 单位净值 (4位小数)

    @TableField("prev_net_value")
    private BigDecimal prevNetValue;  // 昨日净值 (4位小数)

    @TableField("change_amount")
    private BigDecimal changeAmount;  // 涨跌额 (4位小数)

    @TableField("change_percent")
    private BigDecimal changePercent; // 涨跌幅(%) (2位小数)

    private LocalDateTime createdAt;

    //构造函数
    public FundQuotePO() {}
}
