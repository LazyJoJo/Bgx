package com.stock.fund.application.service.alert.dto;

import lombok.Builder;
import lombok.Data;
import java.util.List;

/**
 * 批量创建提醒响应（匹配新API契约）
 */
@Data
@Builder
public class BatchCreateAlertResponseV2 {
    
    private String batchId;  // 批次ID
    
    private int totalCount;  // 总数量
    
    private int successCount;  // 成功数量
    
    private int failureCount;  // 失败数量
    
    private List<SuccessItem> successList;  // 成功列表
    
    private List<FailureItem> failureList;  // 失败列表
    
    /**
     * 成功项
     */
    @Data
    @Builder
    public static class SuccessItem {
        private String symbol;  // 标的代码
        private String symbolName;  // 标的名称
        private Long alertId;  // 提醒ID
        private String createdAt;  // 创建时间
    }
    
    /**
     * 失败项
     */
    @Data
    @Builder
    public static class FailureItem {
        private String symbol;  // 标的代码
        private String symbolName;  // 标的名称
        private String reason;  // 失败原因
        private String errorCode;  // 错误码
    }
}
