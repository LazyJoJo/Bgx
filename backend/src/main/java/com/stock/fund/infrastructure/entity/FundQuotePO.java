package com.stock.fund.infrastructure.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.LocalTime;

@TableName("fund_quote")
@Data
public class FundQuotePO {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String fundCode; // 基金代码

    private String fundName; // 基金名称

    private LocalDate quoteDate; // 报价日期

    private LocalTime quoteTimeOnly; // 报价时间(仅时分秒)

    private Double nav;           // 单位净值

    private Double prevNetValue;  // 昨日净值

    private Double changeAmount;  // 涨跌额

    private Double changePercent; // 涨跌幅(%)

    private LocalDateTime createdAt;

    //构造函数
    public FundQuotePO() {}
}