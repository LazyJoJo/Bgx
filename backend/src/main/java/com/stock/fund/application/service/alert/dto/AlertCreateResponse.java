package com.stock.fund.application.service.alert.dto;

import lombok.Builder;
import lombok.Data;
import java.util.List;

/**
 * 统一创建提醒响应
 */
@Data
@Builder
public class AlertCreateResponse {

    private String batchId;  // 批次ID

    private int totalCount;  // 总数量

    private int createdCount;  // 新创建数量

    private int existingCount;  // 已存在数量

    private int failureCount;  // 失败数量

    private List<CreatedAlertItem> createdList;  // 新创建的提醒

    private List<ExistingAlertItem> existingList;  // 已存在的提醒

    private List<FailureAlertItem> failureList;  // 失败的

    /**
     * 新创建的提醒项
     */
    @Data
    @Builder
    public static class CreatedAlertItem {
        private String symbol;  // 标的代码
        private String symbolName;  // 标的名称
        private Long alertId;  // 提醒ID
        private String createdAt;  // 创建时间
    }

    /**
     * 已存在的提醒项
     */
    @Data
    @Builder
    public static class ExistingAlertItem {
        private String symbol;  // 标的代码
        private String symbolName;  // 标的名称
        private Long alertId;  // 提醒ID
        private String alertType;  // 提醒类型
        private Double targetPrice;  // 目标价格
        private Double targetChangePercent;  // 目标涨跌幅
        private String status;  // 状态
        private String createdAt;  // 创建时间
    }

    /**
     * 失败的提醒项
     */
    @Data
    @Builder
    public static class FailureAlertItem {
        private String symbol;  // 标的代码
        private String reason;  // 失败原因
        private String errorCode;  // 错误码
    }
}
