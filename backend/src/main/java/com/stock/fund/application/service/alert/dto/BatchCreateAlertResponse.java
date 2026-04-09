package com.stock.fund.application.service.alert.dto;

import com.stock.fund.domain.entity.alert.PriceAlert;
import lombok.Builder;
import lombok.Data;
import java.util.List;

/**
 * 批量创建提醒响应
 */
@Data
@Builder
public class BatchCreateAlertResponse {
    private int successCount;         // 成功数量
    private int failCount;            // 失败数量
    private List<PriceAlert> successList;  // 成功的提醒列表
    private List<FailedAlert> failList;     // 失败的列表

    @Data
    @Builder
    public static class FailedAlert {
        private String symbol;         // 标的代码
        private String reason;         // 失败原因
    }
}
