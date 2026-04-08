package com.stock.fund.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * SpringDoc OpenAPI 配置类
 *
 * 访问地址：
 * - Swagger UI: http://localhost:9090/api/swagger-ui.html
 * - OpenAPI JSON: http://localhost:9090/api/v3/api-docs
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("股票基金数据采集系统 API")
                        .version("1.0.0")
                        .description("""
                                ## 系统简介

                                提供股票、基金数据的实时采集、处理和管理功能。

                                ### 核心功能
                                - **数据采集**: 支持股票基本信息、实时行情、基金净值等数据采集
                                - **目标管理**: 动态管理数据采集目标，支持增删改查和激活/停用控制
                                - **健康检查**: 服务状态监控和缓存服务检测
                                - **缓存测试**: 基于 Caffeine 的本地缓存操作测试

                                ### 技术栈
                                - Spring Boot 3.1.5
                                - MyBatis-Plus
                                - PostgreSQL
                                - Caffeine (本地缓存)
                                - Kafka

                                ### 使用说明
                                1. 所有接口统一返回 ApiResponse 格式
                                2. POST /api/data/* 接口用于触发数据采集
                                3. /api/data-collection-targets 接口用于管理采集目标
                                """)
                        .contact(new Contact()
                                .name("Stock Fund Team")
                                .email("support@stockfund.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0")));
    }
}
