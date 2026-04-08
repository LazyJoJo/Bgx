package com.stock.fund.infrastructure.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@TableName("stock_quote")
@Data
public class StockQuotePO {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long stockId;    //股ID
    
    private LocalDateTime quoteTime; //行时间
    
    private Double open;     // 开盘价
    
    private Double high;     // 最高价
    
    private Double low;      // 最低价
    
    private Double close;    // 收盘价
    
    private Long volume;     // 成交量
    
    private Double amount;   // 成交额
    
    private Double change;   //额
    
    private Double changePercent; //
    
    private LocalDateTime createdAt;

    //构造函数
    public StockQuotePO() {}
}