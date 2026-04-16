package com.stock.fund.application.service.subscription.dto;

import lombok.Data;
import java.util.List;

/**
 * 批量订阅ID请求
 */
@Data
public class BatchSubscriptionIdsRequest {
    private List<Long> ids;
}