package com.stock.fund.interfaces.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.util.List;

/**
 * 通用分页查询参数
 */
@Data
@Schema(description = "通用分页查询参数")
public class PageQueryRequest {
    
    @Schema(description = "页码", example = "1")
    private Integer pageNum = 1;
    
    @Schema(description = "每页大小", example = "20")
    private Integer pageSize = 20;
    
    @Schema(description = "排序字段", example = "createdAt")
    private String orderBy;
    
    @Schema(description = "排序方向：ASC升序，DESC降序", example = "DESC", allowableValues = {"ASC", "DESC"})
    private String orderDirection = "DESC";
    
    /**
     * 获取数据库排序字段（防止SQL注入）
     * 只允许特定字段排序
     */
    public String getSafeOrderBy(List<String> allowedFields) {
        if (orderBy == null || orderBy.isEmpty()) {
            return null;
        }
        // 驼峰转下划线
        String underscoreField = camelToUnderscore(orderBy);
        if (allowedFields.contains(underscoreField)) {
            return underscoreField;
        }
        return null;
    }
    
    /**
     * 获取安全的排序方向
     */
    public String getSafeOrderDirection() {
        if ("ASC".equalsIgnoreCase(orderDirection)) {
            return "ASC";
        }
        return "DESC";
    }
    
    /**
     * 驼峰转下划线
     */
    private String camelToUnderscore(String camelCase) {
        if (camelCase == null) {
            return null;
        }
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < camelCase.length(); i++) {
            char c = camelCase.charAt(i);
            if (Character.isUpperCase(c)) {
                result.append("_").append(Character.toLowerCase(c));
            } else {
                result.append(c);
            }
        }
        return result.toString();
    }
}
