package com.stock.fund.domain.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 行情实体
 * <p>
 * 数值精度规范:
 * - open/high/low/close (价格): 2位小数
 * - amount (成交额): 2位小数
 * - change (涨跌额): 2位小数
 * - changePercent (涨跌幅): 2位小数
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class StockQuote extends AggregateRoot<Long> {
    private Long stockId;                 // 关联的股票ID
    private LocalDateTime quoteTime;        //行时间
    private BigDecimal open;                    // 开盘价 (2位小数)
    private BigDecimal high;                    // 最高价 (2位小数)
    private BigDecimal low;                     // 最低价 (2位小数)
    private BigDecimal close;                   //收价 (2位小数)
    private Long volume;                    // 成交量
    private BigDecimal amount;                  // 成交额 (2位小数)
    private BigDecimal change;                  //涨跌额 (2位小数)
    private BigDecimal changePercent;           //涨跌幅 (2位小数)

    // 构造函数
    public StockQuote() {}

    public StockQuote(Long stockId) {
        this.stockId = stockId;
    }
}