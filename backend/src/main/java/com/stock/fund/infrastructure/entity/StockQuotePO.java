package com.stock.fund.infrastructure.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 股票行情实体
 * <p>
 * 数值精度规范:
 * - open/high/low/close (价格): 2位小数 -> DECIMAL(10,2)
 * - amount (成交额): 2位小数 -> DECIMAL(20,2)
 * - change (涨跌额): 2位小数 -> DECIMAL(10,2)
 * - changePercent (涨跌幅): 2位小数 -> DECIMAL(10,2)
 */
@TableName("stock_quote")
@Data
public class StockQuotePO {
    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("stock_id")
    private Long stockId;    //股ID

    @TableField("quote_time")
    private LocalDateTime quoteTime; //行时间

    private BigDecimal open;     // 开盘价 (2位小数)

    private BigDecimal high;     // 最高价 (2位小数)

    private BigDecimal low;      // 最低价 (2位小数)

    private BigDecimal close;    // 收盘价 (2位小数)

    private Long volume;     // 成交量

    private BigDecimal amount;   // 成交额 (2位小数)

    private BigDecimal change;   //涨跌额 (2位小数)

    @TableField("change_percent")
    private BigDecimal changePercent; //涨跌幅 (2位小数)

    private LocalDateTime createdAt;

    //构造函数
    public StockQuotePO() {}
}
