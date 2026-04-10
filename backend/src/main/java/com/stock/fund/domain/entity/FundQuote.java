package com.stock.fund.domain.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * 基金净值实体
 * <p>
 * 数值精度规范:
 * - nav (单位净值): 4位小数
 * - prevNetValue (昨日净值): 4位小数
 * - changeAmount (涨跌额): 4位小数
 * - changePercent (涨跌幅): 2位小数
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class FundQuote extends AggregateRoot<Long> {
    private String fundCode;                    // 基金代码
    private String fundName;                    // 基金名称
    private LocalDate quoteDate;               // 报价日期
    private LocalTime quoteTimeOnly;           // 报价时间(仅时分秒)
    private BigDecimal nav;                       // 单位净值 (4位小数)
    private BigDecimal prevNetValue;           // 昨日净值 (4位小数)
    private BigDecimal changeAmount;              // 涨跌额 (4位小数)
    private BigDecimal changePercent;             // 涨跌幅(%) (2位小数)

    // 构造函数
    public FundQuote() {}

    public FundQuote(String fundCode) {
        this.fundCode = fundCode;
    }
}