package com.stock.fund.application.scheduler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.stock.fund.application.service.riskalert.RiskAlertAppService;

/**
 * 风险提醒调度器 仅在11:30和14:30（工作日）触发风险提醒检测
 */
@Component
public class RiskAlertScheduler {

    private static final Logger logger = LoggerFactory.getLogger(RiskAlertScheduler.class);

    @Autowired
    private RiskAlertAppService riskAlertAppService;

    @Autowired
    private SchedulerConfig schedulerConfig;

    /**
     * 上午11:30触发风险提醒检测（工作日）
     */
    @Scheduled(cron = "${data.collection.schedule.risk-alert-1130-cron}")
    public void triggerRiskAlertCheckAt1130() {
        logger.info("Starting 11:30 risk alert check");
        try {
            riskAlertAppService.checkAndCreateRiskAlerts("11:30");
            logger.info("11:30 risk alert check completed");
        } catch (Exception e) {
            logger.error("11:30 risk alert check failed", e);
        }
    }

    /**
     * 下午14:30触发风险提醒检测（工作日）
     */
    @Scheduled(cron = "${data.collection.schedule.risk-alert-1430-cron}")
    public void triggerRiskAlertCheckAt1430() {
        logger.info("Starting 14:30 risk alert check");
        try {
            riskAlertAppService.checkAndCreateRiskAlerts("14:30");
            logger.info("14:30 risk alert check completed");
        } catch (Exception e) {
            logger.error("14:30 risk alert check failed", e);
        }
    }
}
