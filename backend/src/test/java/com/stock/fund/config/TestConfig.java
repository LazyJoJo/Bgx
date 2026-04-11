package com.stock.fund.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

/**
 * 测试配置
 * 禁用调度任务以支持 Controller 单元测试
 */
@TestConfiguration
public class TestConfig {

    @Bean
    @Primary
    public TaskScheduler taskScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(1);
        scheduler.setAwaitTerminationSeconds(1);
        scheduler.setWaitForTasksToCompleteOnShutdown(true);
        return scheduler;
    }
}
