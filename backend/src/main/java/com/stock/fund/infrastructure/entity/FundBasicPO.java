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
 * 基金基本信息实体
 * <p>
 * 数值精度规范:
 * - nav (最新净值): 4位小数 -> DECIMAL(10,4)
 * - fundSize (基金规模): 2位小数 -> DECIMAL(20,2)
 * - dayGrowth/weekGrowth/monthGrowth/yearGrowth (增长率): 2位小数 -> DECIMAL(10,2)
 */
@TableName("fund_basic")
@Data
public class FundBasicPO {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String fundCode;  // 基金代码

    private String name;      // 基金名称

    private String type;      // 基金类型

    private String manager;   // 基金经理

    private LocalDate establishmentDate; // 成立日期

    @TableField("fund_size")
    private BigDecimal fundSize;  // 基金规模 (2位小数)

    @TableField("nav")
    private BigDecimal nav;       // 最新净值 (4位小数)

    @TableField("day_growth")
    private BigDecimal dayGrowth; // 日增长率 (2位小数)

    @TableField("week_growth")
    private BigDecimal weekGrowth; // 周增长率 (2位小数)

    @TableField("month_growth")
    private BigDecimal monthGrowth; // 月增长率 (2位小数)

    @TableField("year_growth")
    private BigDecimal yearGrowth; // 年增长率 (2位小数)

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    // 构造函数
    public FundBasicPO() {}
}
