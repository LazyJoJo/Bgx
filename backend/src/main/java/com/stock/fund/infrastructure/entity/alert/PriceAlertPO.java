package com.stock.fund.infrastructure.entity.alert;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@TableName("price_alert")
@Data
public class PriceAlertPO {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long userId;              // 用户ID
    
    private String entityCode;        // 实体代码
    
    private String entityType;        // 实体类型：stock/fund
    
    private String entityName;       // 实体名称
    
    private String alertType;        // 提醒类型：上涨/下跌
    
    private Double threshold;         //阈
    
    private Double currentValue;      // 当前值
    
    private Boolean isActive;         // 是否激活
    
    private LocalDateTime lastTriggered; // 最后触发时间
    
    private String description;      // 提醒描述
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;

    //构造函数
    public PriceAlertPO() {}
}