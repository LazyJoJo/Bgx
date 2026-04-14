package com.stock.fund.application.service.alert.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

/**
 * 统一创建提醒请求
 * 支持单个标的(symbol)或批量标的(symbols)创建
 */
@Data
public class AlertCreateRequest {

    @NotNull(message = "用户ID不能为空")
    private Long userId;

    @NotBlank(message = "标的类型不能为空")
    private String symbolType;  // STOCK / FUND

    // 单个标的（可选，与symbols二选一）
    private String symbol;

    // 批量标的列表（可选，与symbol二选一，优先使用symbol）
    @Size(max = 100, message = "单次最多选择100个标的")
    private List<String> symbols;

    @NotBlank(message = "提醒类型不能为空")
    private String alertType;  // PRICE_ABOVE / PRICE_BELOW / PERCENTAGE_CHANGE

    private Double targetPrice;  // 目标价格（PRICE_ABOVE/PRICE_BELOW时使用）

    private Double targetChangePercent;  // 目标涨跌幅（PERCENTAGE_CHANGE时使用）

    private Double basePrice;  // 基准价格（用于涨跌幅计算）

    private Boolean enabled;  // 是否启用，默认true

    private List<String> notifyChannels;  // 通知渠道

    @Size(max = 200, message = "备注信息最多200字符")
    private String remark;
}
