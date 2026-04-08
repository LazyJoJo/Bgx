package com.stock.fund.infrastructure.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

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
    
    private Double fundSize;  // 基金规模
    
    private Double nav;       // 最新净值
    
    private Double dayGrowth; // 日增长率
    
    private Double weekGrowth; // 周增长率
    
    private Double monthGrowth; // 月增长率
    
    private Double yearGrowth; // 年增长率
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;

    // 构造函数
    public FundBasicPO() {}
}