package com.stock.fund.application.service.riskalert.dto;

import com.stock.fund.domain.entity.riskalert.RiskAlert;
import lombok.Builder;
import lombok.Data;
import java.util.List;

/**
 * 批量创建风险提醒响应
 */
@Data
@Builder
public class BatchCreateRiskAlertResponse {
    private int successCount;                    // 成功数量
    private int failCount;                       // 失败数量
    private List<RiskAlert> successList;        // 成功的提醒列表
    private List<FailedRiskAlert> failList;     // 失败的列表

    @Data
    @Builder
    public static class FailedRiskAlert {
        private String symbol;                   // 标的代码
        private String reason;                   // 失败原因
    }
}
