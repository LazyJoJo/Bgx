package com.stock.fund.domain.repository.alert;

import lombok.Builder;
import lombok.Data;

/**
 * 价格提醒查询条件（domain层查询对象）
 */
@Data
@Builder
public class PriceAlertQuery {
    private Long userId;
    private String symbol;
    private String symbolType;
    private String alertType;
    private String status;
    private int page;
    private int size;
    private String sort;
}
