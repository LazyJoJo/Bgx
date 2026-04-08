package com.stock.fund.infrastructure.entity.riskalert;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 风险提醒持久化对象
 */
@TableName("risk_alert")
@Data
public class RiskAlertPO {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;                      // 用户ID
    private String symbol;                    // 标的代码
    private String symbolType;                 // 标的类型：STOCK/FUND
    private String symbolName;                // 标的名称
    private Double changePercent;             // 涨跌幅（%）
    private Double currentPrice;              // 触发时的价格
    private Double yesterdayClose;            // 昨日收盘价
    private Integer triggerCount;             // 当日累计触发次数
    private Boolean isRead;                  // 是否已读
    private LocalDateTime triggeredAt;       // 最新触发时间
    private String triggerReason;            // 触发原因
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public RiskAlertPO() {}
}
