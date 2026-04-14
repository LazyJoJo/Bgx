package com.stock.fund.application.service.alert.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

/**
 * 重复检测请求
 */
@Data
public class CheckDuplicatesRequest {
    
    @NotNull(message = "用户ID不能为空")
    private Long userId;
    
    @NotBlank(message = "标的类型不能为空")
    private String symbolType;  // STOCK / FUND
    
    @NotEmpty(message = "标的列表不能为空")
    @Size(max = 100, message = "单次最多检测100个标的")
    private List<String> symbols;
    
    @NotBlank(message = "提醒类型不能为空")
    private String alertType;  // PRICE_ABOVE / PRICE_BELOW / PERCENTAGE_CHANGE
}
