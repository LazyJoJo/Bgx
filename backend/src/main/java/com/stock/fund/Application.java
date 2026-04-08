package com.stock.fund;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(exclude = {
    // 禁用 Kafka 自动配置
    org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration.class,
    // Session 自动配置以避免冲突
    org.springframework.boot.autoconfigure.session.SessionAutoConfiguration.class
})
@ComponentScan(basePackages = "com.stock.fund")
@MapperScan("com.stock.fund.infrastructure.mapper")
@EnableScheduling
public class Application {

    public static void main(String[] args) {
        SpringApplication application = new SpringApplication(Application.class);
        application.setDefaultProperties(java.util.Collections.singletonMap("spring.main.allow-bean-definition-overriding", "true"));
        application.run(args);
    }

}