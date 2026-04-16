package com.stock.fund.domain.repository.subscription;

import lombok.Builder;
import lombok.Data;

/**
 * 用户订阅查询条件（domain层查询对象）
 */
@Data
@Builder
public class UserSubscriptionQuery {
    private Long userId;
    private String symbol;
    private String symbolType;
    private String status;
    private int page;
    private int size;
    private String sort;
}