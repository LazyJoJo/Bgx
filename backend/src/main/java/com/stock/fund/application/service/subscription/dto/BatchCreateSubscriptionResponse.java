package com.stock.fund.application.service.subscription.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

/**
 * 批量创建订阅响应
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BatchCreateSubscriptionResponse {
    private String batchId;
    private int totalCount;
    private int createdCount;
    private int existingCount;
    private int failureCount;
    private List<CreatedItem> createdList;
    private List<ExistingItem> existingList;
    private List<FailureItem> failureList;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreatedItem {
        private String symbol;
        private String symbolName;
        private Long subscriptionId;
        private String createdAt;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ExistingItem {
        private String symbol;
        private String symbolName;
        private Long subscriptionId;
        private String createdAt;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FailureItem {
        private String symbol;
        private String reason;
        private String errorCode;
    }
}