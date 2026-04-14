package com.stock.fund.application.service.alert.dto;

import lombok.Builder;
import lombok.Data;
import java.util.List;

/**
 * 重复检测响应
 */
@Data
@Builder
public class CheckDuplicatesResponse {
    
    private int checkedCount;  // 检测数量
    
    private int duplicateCount;  // 重复数量
    
    private List<DuplicateItem> duplicates;  // 重复的提醒列表
    
    private List<String> availableSymbols;  // 可创建提醒的标的代码
    
    /**
     * 重复项
     */
    @Data
    @Builder
    public static class DuplicateItem {
        private String symbol;  // 标的代码
        private String symbolName;  // 标的名称
        private List<ExistingAlert> existingAlerts;  // 已存在的提醒列表
    }
    
    /**
     * 已存在的提醒
     */
    @Data
    @Builder
    public static class ExistingAlert {
        private Long alertId;  // 提醒ID
        private String alertType;  // 提醒类型
        private Double targetPrice;  // 目标价格
        private Double targetChangePercent;  // 目标涨跌幅
        private String createdAt;  // 创建时间
        private String status;  // 提醒状态
    }
}
