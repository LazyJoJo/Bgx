package com.stock.fund.infrastructure.repository.riskalert;

import com.stock.fund.domain.entity.riskalert.RiskAlert;
import com.stock.fund.domain.repository.RiskAlertRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * RiskAlertRepository 集成测试
 * 使用 Testcontainers 连接真实 PostgreSQL 数据库
 */
@Testcontainers
@SpringBootTest
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class RiskAlertRepositoryIT {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16")
            .withDatabaseName("stock_fund_test")
            .withUsername("test")
            .withPassword("test");

    @Autowired
    private RiskAlertRepository riskAlertRepository;

    private static Long testUserId = 1L;

    @BeforeAll
    static void beforeAll() {
        System.setProperty("spring.datasource.url", postgres.getJdbcUrl());
        System.setProperty("spring.datasource.username", postgres.getUsername());
        System.setProperty("spring.datasource.password", postgres.getPassword());
    }

    @AfterEach
    void afterEach() {
        // 数据会在事务回滚后自动清理
    }

    @Test
    @Order(1)
    @DisplayName("保存风险提醒 - STOCK类型")
    void save_stockAlert_shouldPersist() {
        // given
        RiskAlert alert = createRiskAlert("000001", "STOCK", "14:30",
                new BigDecimal("5.50"), new BigDecimal("12.50"), new BigDecimal("11.80"));

        // when
        RiskAlert saved = riskAlertRepository.save(alert);

        // then
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getSymbol()).isEqualTo("000001");
        assertThat(saved.getSymbolType()).isEqualTo("STOCK");
    }

    @Test
    @Order(2)
    @DisplayName("保存风险提醒 - FUND类型")
    void save_fundAlert_shouldPersist() {
        // given
        RiskAlert alert = createRiskAlert("000011", "FUND", "14:30",
                new BigDecimal("3.50"), new BigDecimal("1.5500"), new BigDecimal("1.4800"));

        // when
        RiskAlert saved = riskAlertRepository.save(alert);

        // then
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getSymbol()).isEqualTo("000011");
        assertThat(saved.getSymbolType()).isEqualTo("FUND");
    }

    @Test
    @Order(3)
    @DisplayName("根据用户ID和日期范围查询")
    void findByUserIdAndDateRange_shouldReturnAlerts() {
        // given
        RiskAlert alert1 = createRiskAlert("000001", "STOCK", "11:30",
                new BigDecimal("3.0"), new BigDecimal("12.00"), new BigDecimal("11.65"));
        RiskAlert alert2 = createRiskAlert("000011", "FUND", "14:30",
                new BigDecimal("2.5"), new BigDecimal("1.5000"), new BigDecimal("1.4634"));
        riskAlertRepository.save(alert1);
        riskAlertRepository.save(alert2);

        LocalDate today = LocalDate.now();

        // when
        List<RiskAlert> alerts = riskAlertRepository.findByUserIdAndDateRange(
                testUserId, today, today);

        // then
        assertThat(alerts).hasSize(2);
    }

    @Test
    @Order(4)
    @DisplayName("根据用户ID、标的、日期、时间点查询")
    void findByUserIdAndSymbolAndAlertDateAndTimePoint_shouldFindAlert() {
        // given
        RiskAlert alert = createRiskAlert("000001", "STOCK", "14:30",
                new BigDecimal("5.50"), new BigDecimal("12.50"), new BigDecimal("11.80"));
        RiskAlert saved = riskAlertRepository.save(alert);

        // when
        Optional<RiskAlert> found = riskAlertRepository
                .findByUserIdAndSymbolAndAlertDateAndTimePoint(
                        testUserId, "000001", LocalDate.now(), "14:30");

        // then
        assertThat(found).isPresent();
        assertThat(found.get().getId()).isEqualTo(saved.getId());
    }

    @Test
    @Order(5)
    @DisplayName("更新风险提醒")
    void update_shouldModifyAlert() {
        // given
        RiskAlert alert = createRiskAlert("000001", "STOCK", "14:30",
                new BigDecimal("5.50"), new BigDecimal("12.50"), new BigDecimal("11.80"));
        RiskAlert saved = riskAlertRepository.save(alert);

        // when
        saved.setChangePercent(new BigDecimal("6.00"));
        saved.setIsRead(true);
        riskAlertRepository.update(saved);

        // then
        Optional<RiskAlert> found = riskAlertRepository.findById(saved.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getChangePercent()).isEqualByComparingTo(new BigDecimal("6.00"));
        assertThat(found.get().getIsRead()).isTrue();
    }

    @Test
    @Order(6)
    @DisplayName("标记全部已读")
    void markAllAsRead_shouldUpdateAllAlerts() {
        // given
        RiskAlert alert1 = createRiskAlert("000001", "STOCK", "14:30",
                new BigDecimal("5.50"), new BigDecimal("12.50"), new BigDecimal("11.80"));
        RiskAlert alert2 = createRiskAlert("000011", "FUND", "14:30",
                new BigDecimal("3.50"), new BigDecimal("1.5500"), new BigDecimal("1.4800"));
        alert1.setIsRead(false);
        alert2.setIsRead(false);
        riskAlertRepository.save(alert1);
        riskAlertRepository.save(alert2);

        // when
        riskAlertRepository.markAllAsRead(testUserId);

        // then
        List<RiskAlert> alerts = riskAlertRepository.findByUserIdAndDateRange(
                testUserId, LocalDate.now(), LocalDate.now());
        assertThat(alerts).allMatch(RiskAlert::getIsRead);
    }

    @Test
    @Order(7)
    @DisplayName("统计未读数量")
    void countUnreadByUserId_shouldReturnCount() {
        // given
        RiskAlert alert1 = createRiskAlert("000001", "STOCK", "11:30",
                new BigDecimal("3.0"), new BigDecimal("12.00"), new BigDecimal("11.65"));
        RiskAlert alert2 = createRiskAlert("000011", "FUND", "14:30",
                new BigDecimal("2.5"), new BigDecimal("1.5000"), new BigDecimal("1.4634"));
        alert1.setIsRead(false);
        alert2.setIsRead(true);
        riskAlertRepository.save(alert1);
        riskAlertRepository.save(alert2);

        // when
        long unreadCount = riskAlertRepository.countUnreadByUserId(testUserId);

        // then
        assertThat(unreadCount).isEqualTo(1);
    }

    @Test
    @Order(8)
    @DisplayName("分页查询用户风险提醒")
    void findByUserIdWithPage_shouldReturnPaginatedAlerts() {
        // given
        for (int i = 0; i < 25; i++) {
            RiskAlert alert = createRiskAlert("00000" + i, "STOCK", "14:30",
                    new BigDecimal("5.50"), new BigDecimal("12.50"), new BigDecimal("11.80"));
            riskAlertRepository.save(alert);
        }

        com.stock.fund.domain.repository.RiskAlertQuery query =
                com.stock.fund.domain.repository.RiskAlertQuery.builder()
                        .userId(testUserId)
                        .page(1)
                        .size(10)
                        .build();

        // when
        List<RiskAlert> alerts = riskAlertRepository.findByUserIdWithPage(query);
        long total = riskAlertRepository.countByUserId(query);

        // then
        assertThat(alerts).hasSize(10);
        assertThat(total).isEqualTo(25);
    }

    @Test
    @Order(9)
    @DisplayName("删除风险提醒")
    void deleteById_shouldRemoveAlert() {
        // given
        RiskAlert alert = createRiskAlert("000001", "STOCK", "14:30",
                new BigDecimal("5.50"), new BigDecimal("12.50"), new BigDecimal("11.80"));
        RiskAlert saved = riskAlertRepository.save(alert);

        // when
        riskAlertRepository.deleteById(saved.getId());

        // then
        Optional<RiskAlert> found = riskAlertRepository.findById(saved.getId());
        assertThat(found).isEmpty();
    }

    @Test
    @Order(10)
    @DisplayName("STOCK和FUND混合查询")
    void findByUserIdAndDateRange_mixedTypes_shouldReturnBoth() {
        // given
        RiskAlert stockAlert = createRiskAlert("000001", "STOCK", "14:30",
                new BigDecimal("5.50"), new BigDecimal("12.50"), new BigDecimal("11.80"));
        RiskAlert fundAlert1 = createRiskAlert("000011", "FUND", "11:30",
                new BigDecimal("2.5"), new BigDecimal("1.5000"), new BigDecimal("1.4634"));
        RiskAlert fundAlert2 = createRiskAlert("000022", "FUND", "14:30",
                new BigDecimal("-1.5"), new BigDecimal("1.4000"), new BigDecimal("1.4215"));
        riskAlertRepository.save(stockAlert);
        riskAlertRepository.save(fundAlert1);
        riskAlertRepository.save(fundAlert2);

        LocalDate today = LocalDate.now();

        // when
        List<RiskAlert> alerts = riskAlertRepository.findByUserIdAndDateRange(
                testUserId, today, today);

        // then
        assertThat(alerts).hasSize(3);
        assertThat(alerts).anyMatch(a -> "STOCK".equals(a.getSymbolType()));
        assertThat(alerts).anyMatch(a -> "FUND".equals(a.getSymbolType()) && "000011".equals(a.getSymbol()));
        assertThat(alerts).anyMatch(a -> "FUND".equals(a.getSymbolType()) && "000022".equals(a.getSymbol()));
    }

    private RiskAlert createRiskAlert(String symbol, String symbolType, String timePoint,
                                      BigDecimal changePercent, BigDecimal currentPrice, BigDecimal yesterdayClose) {
        RiskAlert alert = new RiskAlert();
        alert.setUserId(testUserId);
        alert.setSymbol(symbol);
        alert.setSymbolType(symbolType);
        alert.setSymbolName(symbol + "名称");
        alert.setAlertDate(LocalDate.now());
        alert.setTimePoint(timePoint);
        alert.setHasRisk(true);
        alert.setChangePercent(changePercent);
        alert.setCurrentPrice(currentPrice);
        alert.setYesterdayClose(yesterdayClose);
        alert.setIsRead(false);
        alert.setTriggeredAt(LocalDateTime.now());
        return alert;
    }
}
