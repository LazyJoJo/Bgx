package com.stock.fund.interfaces.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

/**
 * 基金净值查询参数
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "基金净值查询参数")
public class FundQuoteQueryRequest extends PageQueryRequest {
    
    /** 允许排序的字段列表（数据库字段名） */
    public static final List<String> ALLOWED_ORDER_FIELDS = Arrays.asList(
        "fund_code", "fund_name", "quote_date", "quote_time_only",
        "nav", "prev_net_value", "change_amount", "change_percent",
        "created_at"
    );
    
    @Schema(description = "基金代码（模糊查询）", example = "001")
    private String fundCode;
    
    @Schema(description = "基金名称（模糊查询）", example = "新能源")
    private String fundName;
    
    @Schema(description = "开始日期", example = "2026-03-01")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;
    
    @Schema(description = "结束日期", example = "2026-03-31")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate;
    
    /**
     * 获取安全的排序字段
     */
    public String getSafeOrderByForFundQuote() {
        return getSafeOrderBy(ALLOWED_ORDER_FIELDS);
    }
}
