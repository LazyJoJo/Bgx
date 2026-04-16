package com.stock.fund.application.service.subscription.dto;

import lombok.Data;
import java.util.List;

/**
 * 检测重复请求
 */
@Data
public class CheckDuplicatesRequest {
    private Long userId;
    private List<String> symbols;
    private String symbolType;
    private String alertType;
}