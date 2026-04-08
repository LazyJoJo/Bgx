package com.stock.fund.domain.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import java.time.LocalDateTime;

/**
 *行情实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class StockQuote extends AggregateRoot<Long> {
    private Long stockId;                 // 关联的股票ID
    private LocalDateTime quoteTime;        //行时间
    private Double open;                    // 开盘价
    private Double high;                    // 最高价
    private Double low;                     // 最低价
    private Double close;                   //收价
    private Long volume;                    // 成交量
    private Double amount;                  // 成交额
    private Double change;                  //涨额
    private Double changePercent;           //涨

    // 构造函数
    public StockQuote() {}

    public StockQuote(Long stockId) {
        this.stockId = stockId;
    }
}