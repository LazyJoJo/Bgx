package com.stock.fund.application.service.subscription.dto;

import com.stock.fund.domain.entity.subscription.UserSubscription;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 创建订阅响应
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateSubscriptionResponse {
    private boolean created;
    private String message;
    private UserSubscription subscription;

    public static CreateSubscriptionResponse created(UserSubscription subscription) {
        return CreateSubscriptionResponse.builder()
                .created(true)
                .message("订阅创建成功")
                .subscription(subscription)
                .build();
    }

    public static CreateSubscriptionResponse existed(UserSubscription subscription) {
        return CreateSubscriptionResponse.builder()
                .created(false)
                .message("该标的已存在订阅")
                .subscription(subscription)
                .build();
    }
}