package com.stock.fund.application.service.riskalert.dto;

import lombok.Data;
import java.time.LocalDate;

/**
 * 风险提醒查询条件
 */
@Data
public class RiskAlertQueryDTO {
    private Long userId;             // 用户ID
    private LocalDate startDate;     // 开始日期
    private LocalDate endDate;       // 结束日期
    private String symbol;          // 标的代码
    private int page = 1;           // 页码，默认1
    private int size = 20;          // 每页条数，默认20
    private String sort = "alertDate,desc,timePoint,desc"; // 排序
}
