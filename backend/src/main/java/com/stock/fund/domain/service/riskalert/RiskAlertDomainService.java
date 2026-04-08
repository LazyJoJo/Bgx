package com.stock.fund.domain.service.riskalert;

import com.stock.fund.domain.entity.StockQuote;
import com.stock.fund.domain.entity.FundQuote;
import org.springframework.stereotype.Service;

/**
 * 风险提醒领域服务
 * 负责涨跌幅计算逻辑和阈值判断
 */
@Service
public class RiskAlertDomainService {

    /**
     * 风险提醒触发阈值（%）
     * 涨跌幅超过此值则触发提醒
     */
    private static final double RISK_ALERT_THRESHOLD = 1.0;

    /**
     * 计算涨跌幅
     * changePercent = (currentPrice - yesterdayClose) / yesterdayClose * 100
     */
    public double calculateChangePercent(Double currentPrice, Double yesterdayClose) {
        if (currentPrice == null || yesterdayClose == null || yesterdayClose == 0) {
            return 0.0;
        }
        return ((currentPrice - yesterdayClose) / yesterdayClose) * 100;
    }

    /**
     * 判断是否应该触发风险提醒
     * 涨跌幅绝对值超过阈值（1%）时触发
     */
    public boolean shouldTriggerAlert(double changePercent) {
        return Math.abs(changePercent) >= RISK_ALERT_THRESHOLD;
    }

    /**
     * 根据股票行情判断是否触发风险提醒
     */
    public boolean shouldTriggerAlert(StockQuote quote) {
        if (quote == null || quote.getClose() == null || quote.getChangePercent() == null) {
            return false;
        }
        return shouldTriggerAlert(quote.getChangePercent());
    }

    /**
     * 根据基金净值判断是否触发风险提醒
     */
    public boolean shouldTriggerAlert(FundQuote quote) {
        if (quote == null || quote.getNav() == null || quote.getChangePercent() == null) {
            return false;
        }
        return shouldTriggerAlert(quote.getChangePercent());
    }

    /**
     * 生成触发原因描述
     */
    public String generateTriggerReason(String symbol, String symbolType, double changePercent, Double currentPrice) {
        String direction = changePercent > 0 ? "上涨" : "下跌";
        return String.format("%s %s 涨幅 %+.2f%%，当前价格 %.2f，触发风险提醒",
                symbolType.equals("STOCK") ? "股票" : "基金",
                symbol,
                changePercent,
                currentPrice != null ? currentPrice : 0.0);
    }

    /**
     * 获取风险提醒阈值
     */
    public double getRiskAlertThreshold() {
        return RISK_ALERT_THRESHOLD;
    }
}
