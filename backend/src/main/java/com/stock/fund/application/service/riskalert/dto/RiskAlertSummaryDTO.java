package com.stock.fund.application.service.riskalert.dto;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * 风险提醒汇总数据（按日期分组）
 * <p>
 * 数值精度规范:
 * - changePercent (涨跌幅): 2位小数
 * - currentPrice (当前价格): 2位小数
 * - yesterdayClose (昨日收盘价): 2位小数
 */
@Data
@Builder
public class RiskAlertSummaryDTO {
    private LocalDate alertDate;           // 日期
    private int totalCount;               // 当日风险数据总条数
    private int unreadCount;              // 当日未读条数
    private List<RiskAlertItemDTO> items; // 风险数据列表

    @Data
    @Builder
    public static class RiskAlertItemDTO {
        private Long id;                   // 风险记录ID
        private String symbol;             // 标的代码
        private String symbolType;         // 标的类型
        private String symbolName;         // 标的名称
        private String timePoint;          // 时间点：11:30 / 14:30
        private Boolean hasRisk;           // 是否有风险
        private BigDecimal changePercent;      // 涨跌幅 (2位小数)
        private BigDecimal currentPrice;        // 当前价格 (2位小数)
        private BigDecimal yesterdayClose;     // 昨日收盘价 (2位小数)
        private Boolean isRead;            // 是否已读
        private String triggeredAt;        // 触发时间
    }
}
