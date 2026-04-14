package com.stock.fund.application.service.alert.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

/**
 * 批量创建提醒请求（匹配新API契约）
 */
@Data
public class BatchCreateAlertRequestV2 {
    
    @NotNull(message = "用户ID不能为空")
    private Long userId;
    
    @NotBlank(message = "标的类型不能为空")
    private String symbolType;  // STOCK / FUND
    
    @NotEmpty(message = "标的列表不能为空")
    @Size(max = 100, message = "单次最多选择100个标的")
    private List<String> symbols;
    
    @NotBlank(message = "提醒类型不能为空")
    private String alertType;  // PRICE_ABOVE / PRICE_BELOW / PERCENTAGE_CHANGE
    
    private Double targetPrice;  // 目标价格（PRICE_ABOVE/PRICE_BELOW时必填）
    
    private Double targetChangePercent;  // 目标涨跌幅（PERCENTAGE_CHANGE时必填）
    
    private List<String> notifyChannels;  // 通知渠道，默认["PUSH"]
    
    @Size(max = 200, message = "备注信息最多200字符")
    private String remark;
}
