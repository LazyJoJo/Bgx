package com.stock.fund.interfaces.dto.response;

import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.util.List;

/**
 * 通用分页响应
 */
@Data
@Schema(description = "通用分页响应")
public class PageResponse<T> {
    
    @Schema(description = "数据列表")
    private List<T> records;
    
    @Schema(description = "总记录数")
    private Long total;
    
    @Schema(description = "当前页码")
    private Long pageNum;
    
    @Schema(description = "每页大小")
    private Long pageSize;
    
    @Schema(description = "总页数")
    private Long pages;
    
    /**
     * 从 MyBatis-Plus IPage 转换
     */
    public static <T> PageResponse<T> from(IPage<T> page) {
        PageResponse<T> response = new PageResponse<>();
        response.setRecords(page.getRecords());
        response.setTotal(page.getTotal());
        response.setPageNum(page.getCurrent());
        response.setPageSize(page.getSize());
        response.setPages(page.getPages());
        return response;
    }
    
    /**
     * 从 MyBatis-Plus IPage 转换（带类型转换）
     */
    public static <T, R> PageResponse<R> from(IPage<T> page, List<R> records) {
        PageResponse<R> response = new PageResponse<>();
        response.setRecords(records);
        response.setTotal(page.getTotal());
        response.setPageNum(page.getCurrent());
        response.setPageSize(page.getSize());
        response.setPages(page.getPages());
        return response;
    }
}
