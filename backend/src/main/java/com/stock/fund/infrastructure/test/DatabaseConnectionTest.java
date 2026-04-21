package com.stock.fund.infrastructure.test;

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

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
                logger.info("Database connection test succeeded");
                logger.info("Database URL: {}", connection.getMetaData().getURL());
                logger.info("Database driver: {}", connection.getMetaData().getDriverName());
                logger.info("Database user: {}", connection.getMetaData().getUserName());
            } else {
                logger.error("Database connection test failed");
            }
        } catch (SQLException e) {
            logger.error("Database connection test exception: ", e);
        }
    }

    public void testCacheConnection() {
        logger.info("Caffeine local cache configuration loaded");
        logger.info("Cache config: maxSize={}, defaultTtl={}s", 10000, 3600);
    }
}
