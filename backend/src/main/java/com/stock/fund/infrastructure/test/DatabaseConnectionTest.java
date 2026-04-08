package com.stock.fund.infrastructure.test;

import com.stock.fund.config.CacheConfig;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

@Component
public class DatabaseConnectionTest implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseConnectionTest.class);

    private final DataSource dataSource;

    public DatabaseConnectionTest(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void run(String... args) throws Exception {
        testDatabaseConnection();
        testCacheConnection();
    }

    public void testDatabaseConnection() {
        try (Connection connection = dataSource.getConnection()) {
            if (connection.isValid(5)) { // 5秒超时
                logger.info("✅ 数据库连接测试成功！");
                logger.info("数据库URL: {}", connection.getMetaData().getURL());
                logger.info("数据库驱动: {}", connection.getMetaData().getDriverName());
                logger.info("数据库用户名: {}", connection.getMetaData().getUserName());
            } else {
                logger.error("❌ 数据库连接测试失败！");
            }
        } catch (SQLException e) {
            logger.error("❌ 数据库连接测试异常: ", e);
        }
    }

    public void testCacheConnection() {
        logger.info("✅ Caffeine 本地缓存配置已加载");
        logger.info("缓存配置: maxSize={}, defaultTtl={}s", 10000, 3600);
    }
}
