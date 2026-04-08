package com.stock.fund.domain.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import java.time.LocalDate;

/**
 * 股票领域实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class Stock extends AggregateRoot<Long> {
    private String symbol;        // 股票代码
    private String name;          // 股票名称
    private String industry;      // 所属行业
    private String market;        // 市场
    private LocalDate listingDate; // 上市日期
    private Double totalShare;    // 总股本
    private Double floatShare;    // 流通股本
    private Double pe;            // 市盈率
    private Double pb;            // 市净率

    // 构造函数
    public Stock() {}

    public Stock(String symbol, String name) {
        this.symbol = symbol;
        this.name = name;
    }
}