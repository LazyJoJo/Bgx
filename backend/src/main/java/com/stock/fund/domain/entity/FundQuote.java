package com.stock.fund.domain.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 *基金净值实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class FundQuote extends AggregateRoot<Long> {
    private String fundCode;                    // 基金代码
    private String fundName;                    // 基金名称
    private LocalDate quoteDate;               // 报价日期
    private LocalTime quoteTimeOnly;           // 报价时间(仅时分秒)
    private Double nav;                       // 单位净值
    private Double prevNetValue;           // 昨日净值
    private Double changeAmount;              // 涨跌额
    private Double changePercent;             // 涨跌幅(%)

    // 构造函数
    public FundQuote() {}

    public FundQuote(String fundCode) {
        this.fundCode = fundCode;
    }
}