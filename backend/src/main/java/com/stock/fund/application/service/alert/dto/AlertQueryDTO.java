package com.stock.fund.application.service.alert.dto;

import lombok.Data;

/**
 * 提醒查询条件
 */
@Data
public class AlertQueryDTO {
    private Long userId;             // 用户ID
    private String symbol;           // 标的代码（模糊搜索）
    private String symbolType;      // 标的类型：STOCK / FUND
    private String alertType;       // 提醒类型
    private String status;          // 状态：ACTIVE / INACTIVE / TRIGGERED
    private int page = 1;           // 页码，默认1
    private int size = 20;          // 每页条数，默认20
    private String sort = "createdAt,desc"; // 排序，默认创建时间降序
}
