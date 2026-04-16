package com.stock.fund.application.service.subscription.dto;

import lombok.Data;

/**
 * 订阅查询DTO
 */
@Data
public class SubscriptionQueryDTO {
    private Long userId;
    private String symbol;
    private String symbolType;
    private String status;
    private int page;
    private int size;
    private String sort;
}