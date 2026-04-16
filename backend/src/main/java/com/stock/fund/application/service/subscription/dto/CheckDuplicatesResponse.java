package com.stock.fund.application.service.subscription.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

/**
 * 检测重复响应
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CheckDuplicatesResponse {
    private int checkedCount;
    private int duplicateCount;
    private List<DuplicateItem> duplicates;
    private List<String> availableSymbols;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DuplicateItem {
        private String symbol;
        private String symbolName;
        private List<ExistingSubscription> existingSubscriptions;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ExistingSubscription {
        private Long subscriptionId;
        private String createdAt;
        private String status;
    }
}