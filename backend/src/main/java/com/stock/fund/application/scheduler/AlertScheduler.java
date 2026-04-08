package com.stock.fund.application.scheduler;

import com.stock.fund.application.service.alert.AlertAppService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 提醒检查调度器
 */
@Component
public class AlertScheduler {
    
    private static final Logger logger = LoggerFactory.getLogger(AlertScheduler.class);
    
    @Autowired
    private AlertAppService alertAppService;
    
    @Autowired
    private SchedulerConfig schedulerConfig;
    
    /**
     *定时检查提醒
     *每分钟执行一次
     */
    @Scheduled(cron = "${data.collection.schedule.alert-check-cron}")
    public void scheduleAlertCheck() {
        try {
            logger.debug("开始定时检查提醒");
            alertAppService.batchCheckAlerts();
            logger.debug("定时检查提醒完成");
        } catch (Exception e) {
            logger.error("定时检查提醒失败", e);
        }
    }
}