package com.stock.fund.domain.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 股票领域实体
 * <p>
 * 数值精度规范:
 * - totalShare/floatShare (股本): 2位小数
 * - pe/pb (市盈率/市净率): 2位小数
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class Stock extends AggregateRoot<Long> {
    private String symbol;        // 股票代码
    private String name;          // 股票名称
    private String industry;      // 所属行业
    private String market;        // 市场
    private LocalDate listingDate; // 上市日期
    private BigDecimal totalShare;    // 总股本 (2位小数)
    private BigDecimal floatShare;    // 流通股本 (2位小数)
    private BigDecimal pe;            // 市盈率 (2位小数)
    private BigDecimal pb;            // 市净率 (2位小数)

    // 构造函数
    public Stock() {}

    public Stock(String symbol, String name) {
        this.symbol = symbol;
        this.name = name;
    }
}