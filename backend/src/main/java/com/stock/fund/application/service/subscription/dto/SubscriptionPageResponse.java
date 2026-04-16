package com.stock.fund.application.service.subscription.dto;

import com.stock.fund.domain.entity.subscription.UserSubscription;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

/**
 * 订阅分页响应
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubscriptionPageResponse<T> {
    private List<T> records;
    private long total;
    private int page;
    private int size;
    private int pages;

    public static <T> SubscriptionPageResponse<T> of(List<T> records, long total, int page, int size) {
        return SubscriptionPageResponse.<T>builder()
                .records(records)
                .total(total)
                .page(page)
                .size(size)
                .pages((int) Math.ceil((double) total / size))
                .build();
    }
}