package com.stock.fund.domain.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 基金领域实体
 * <p>
 * 数值精度规范:
 * - nav (最新净值): 4位小数
 * - fundSize (基金规模): 2位小数
 * - dayGrowth/weekGrowth/monthGrowth/yearGrowth (增长率): 2位小数
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class Fund extends AggregateRoot<Long> {
    private String fundCode;      // 基金代码
    private String name;          // 基金名称
    private String type;          // 基金类型
    private String manager;       // 基金经理
    private LocalDate establishmentDate; // 成立日期
    private BigDecimal fundSize;  // 基金规模 (2位小数)
    private BigDecimal nav;       // 最新净值 (4位小数)
    private BigDecimal dayGrowth;     // 日增长率 (2位小数)
    private BigDecimal weekGrowth;    // 周增长率 (2位小数)
    private BigDecimal monthGrowth;   // 月增长率 (2位小数)
    private BigDecimal yearGrowth;    // 年增长率 (2位小数)

    // 构造函数
    public Fund() {}

    public Fund(String fundCode, String name) {
        this.fundCode = fundCode;
        this.name = name;
    }
}