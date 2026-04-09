package com.stock.fund.application.service.alert.dto;

import com.stock.fund.application.service.common.dto.PageResponse;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.util.List;

/**
 * 分页响应
 * @deprecated 请使用 {@link PageResponse}
 */
@Deprecated
@Data
@EqualsAndHashCode(callSuper = true)
public class AlertPageResponse<T> extends PageResponse<T> {
    public AlertPageResponse() {}

    public AlertPageResponse(List<T> records, long total, int page, int size, int pages) {
        super(records, total, page, size, pages);
    }

    public static <T> AlertPageResponse<T> of(List<T> records, long total, int page, int size) {
        int pages = size > 0 ? (int) Math.ceil((double) total / size) : 0;
        return new AlertPageResponse<>(records, total, page, size, pages);
    }
}
