package com.stock.fund.application.scheduler;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 定时任务配置类
 * 用于管理所有定时任务的调度配置
 */
@Configuration
@ConfigurationProperties(prefix = "data.collection.schedule")
public class SchedulerConfig {
    
    // 股票基本信息采集定时任务
    private String stockBasicCron = "0 0 2 * * ?";
    
    // 基金基本信息采集定时任务
    private String fundBasicCron = "0 0 3 * * ?";
    
    // 股票实时行情采集定时任务
    private String stockQuoteCron = "0 */1 9-15 * * MON-FRI";
    
    // 基金实时净值采集定时任务
    private String fundQuoteCron = "0 */15 9-15 * * MON-FRI";
    
    // 交易结束后数据采集定时任务
    private String dailyCollectionCron = "0 0 16 * * MON-FRI";
    
    // 提醒检查定时任务
    private String alertCheckCron = "0 */1 * * * ?";

    // Getters and Setters
    public String getStockBasicCron() {
        return stockBasicCron;
    }

    public void setStockBasicCron(String stockBasicCron) {
        this.stockBasicCron = stockBasicCron;
    }

    public String getFundBasicCron() {
        return fundBasicCron;
    }

    public void setFundBasicCron(String fundBasicCron) {
        this.fundBasicCron = fundBasicCron;
    }

    public String getStockQuoteCron() {
        return stockQuoteCron;
    }

    public void setStockQuoteCron(String stockQuoteCron) {
        this.stockQuoteCron = stockQuoteCron;
    }

    public String getFundQuoteCron() {
        return fundQuoteCron;
    }

    public void setFundQuoteCron(String fundQuoteCron) {
        this.fundQuoteCron = fundQuoteCron;
    }

    public String getDailyCollectionCron() {
        return dailyCollectionCron;
    }

    public void setDailyCollectionCron(String dailyCollectionCron) {
        this.dailyCollectionCron = dailyCollectionCron;
    }

    public String getAlertCheckCron() {
        return alertCheckCron;
    }

    public void setAlertCheckCron(String alertCheckCron) {
        this.alertCheckCron = alertCheckCron;
    }
}