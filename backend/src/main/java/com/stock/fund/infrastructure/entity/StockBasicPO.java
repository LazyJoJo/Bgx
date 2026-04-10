package com.stock.fund.infrastructure.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 股票基本信息实体
 * <p>
 * 数值精度规范:
 * - totalShare/floatShare (股本): 2位小数 -> DECIMAL(20,2)
 * - pe/pb (市盈率/市净率): 2位小数 -> DECIMAL(10,2)
 */
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

    @TableField("total_share")
    private BigDecimal totalShare; // 总股本 (2位小数)

    @TableField("float_share")
    private BigDecimal floatShare; // 流通股本 (2位小数)

    private BigDecimal pe;      // 市盈率 (2位小数)

    private BigDecimal pb;      // 市净率 (2位小数)

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    // 构造函数
    public StockBasicPO() {}
}
