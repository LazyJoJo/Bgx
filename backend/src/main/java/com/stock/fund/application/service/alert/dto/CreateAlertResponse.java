package com.stock.fund.application.service.alert.dto;

import com.stock.fund.domain.entity.alert.PriceAlert;
import lombok.Builder;
import lombok.Data;

/**
 * 创建提醒响应
 */
@Data
@Builder
public class CreateAlertResponse {
    private boolean created;         // 是否新创建（false表示已存在）
    private PriceAlert alert;         // 提醒对象
    private String message;           // 提示信息

    public static CreateAlertResponse created(PriceAlert alert) {
        return CreateAlertResponse.builder()
                .created(true)
                .alert(alert)
                .message("提醒创建成功")
                .build();
    }

    public static CreateAlertResponse existed(PriceAlert alert) {
        return CreateAlertResponse.builder()
                .created(false)
                .alert(alert)
                .message("该标的已存在提醒，已返回已有提醒")
                .build();
    }
}
