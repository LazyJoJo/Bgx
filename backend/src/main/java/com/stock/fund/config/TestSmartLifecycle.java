package com.stock.fund.config;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

@Component
public class TestSmartLifecycle implements ApplicationListener<ApplicationReadyEvent> {

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        // 应用启动完成后执行
        System.out.println("==============应用启动完成==============");
        System.out.println("缓存服务: Caffeine (本地缓存)");
    }
}
