package com.stock.fund.application.service.common.dto;

import lombok.Builder;
import lombok.Data;
import java.util.List;

/**
 * 通用分页响应
 * @param <T> 数据类型
 */
@Data
public class PageResponse<T> {
    private List<T> records;    // 数据列表
    private long total;          // 总条数
    private int page;            // 当前页
    private int size;            // 每页条数
    private int pages;           // 总页数

    public PageResponse() {}

    public PageResponse(List<T> records, long total, int page, int size, int pages) {
        this.records = records;
        this.total = total;
        this.page = page;
        this.size = size;
        this.pages = pages;
    }

    /**
     * 创建分页响应
     */
    public static <T> PageResponse<T> of(List<T> records, long total, int page, int size) {
        int pages = size > 0 ? (int) Math.ceil((double) total / size) : 0;
        return new PageResponse<>(records, total, page, size, pages);
    }
}
