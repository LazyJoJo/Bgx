package com.stock.fund.infrastructure.entity.alert;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@TableName("alert_history")
@Data
public class AlertHistoryPO {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long userId;              // 用户ID
    
    private Long alertId;              // 提醒ID
    
    private String entityCode;        // 实体代码
    
    private String entityType;        // 实体类型：stock/fund
    
    private String entityName;       // 实体名称
    
    private String alertType;        // 提醒类型：上涨/下跌
    
    private Double threshold;         //阈
    
    private Double currentValue;      //触发时的值
    
    private LocalDateTime triggeredAt; //触发时间
    
    private String triggerReason;    //触发原因
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;

    //构造函数
    public AlertHistoryPO() {}
}