package com.stock.fund.domain.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import java.time.LocalDate;

/**
 * 基金领域实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class Fund extends AggregateRoot<Long> {
    private String fundCode;      // 基金代码
    private String name;          // 基金名称
    private String type;          // 基金类型
    private String manager;       // 基金经理
    private LocalDate establishmentDate; // 成立日期
    private Double fundSize;      // 基金规模
    private Double nav;           // 最新净值
    private Double dayGrowth;     // 日增长率
    private Double weekGrowth;    // 周增长率
    private Double monthGrowth;   // 月增长率
    private Double yearGrowth;    // 年增长率

    // 构造函数
    public Fund() {}

    public Fund(String fundCode, String name) {
        this.fundCode = fundCode;
        this.name = name;
    }
}