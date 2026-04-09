package com.stock.fund.domain.repository;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDate;

/**
 * 风险提醒查询条件（domain层查询对象）
 */
@Data
@Builder
public class RiskAlertQuery {
    private Long userId;
    private LocalDate startDate;
    private LocalDate endDate;
    private int page;
    private int size;
    private String sort;
}
