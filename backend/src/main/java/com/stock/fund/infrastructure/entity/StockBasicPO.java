package com.stock.fund.infrastructure.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@TableName("stock_basic")
@Data
public class StockBasicPO {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String symbol;  // 股票代码
    
    private String name;    // 股票名称
    
    private String industry; // 所属行业
    
    private String market;  // 市场
    
    private LocalDate listingDate; // 上市日期
    
    private Double totalShare; // 总股本
    
    private Double floatShare; // 流通股本
    
    private Double pe;      // 市盈率
    
    private Double pb;      // 市净率
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;

    // 构造函数
    public StockBasicPO() {}
}