package com.stock.fund.application.service.alert;

import com.stock.fund.domain.entity.alert.AlertHistory;
import com.stock.fund.domain.entity.alert.PriceAlert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 提醒应用服务测试 - TDD RED阶段
 *
 * 用户故事：
 * 作为用户，我希望设置价格提醒，以便在股票/基金达到目标价格时收到通知
 *
 * 测试场景：
 * 1. 创建提醒 - 用户可以创建不同类型的提醒（价格超过/低于/涨跌幅）
 * 2. 更新提醒 - 用户可以修改已有提醒
 * 3. 删除提醒 - 用户可以删除不需要的提醒
 * 4. 启用/禁用提醒 - 用户可以控制提醒的激活状态
 * 5. 触发提醒 - 当价格达到条件时提醒自动触发
 * 6. 提醒历史 - 系统记录所有触发的提醒
 */
public class AlertAppServiceTest {

    private PriceAlert createTestAlert() {
        PriceAlert alert = new PriceAlert();
        alert.setId(1L);
        alert.setUserId(100L);
        alert.setSymbol("000001");
        alert.setSymbolType("STOCK");
        alert.setSymbolName("平安银行");
        alert.setAlertType("PRICE_ABOVE");
        alert.setTargetPrice(15.50);
        alert.setStatus("ACTIVE");
        alert.setCreatedAt(LocalDateTime.now());
        alert.setUpdatedAt(LocalDateTime.now());
        return alert;
    }

    @Nested
    @DisplayName("提醒创建测试")
    class CreateAlertTests {

        @Test
        @DisplayName("创建价格超过提醒")
        void createPriceAboveAlert_shouldCreateSuccessfully() {
            // Given
            PriceAlert alert = new PriceAlert();
            alert.setUserId(100L);
            alert.setSymbol("000001");
            alert.setSymbolType("STOCK");
            alert.setAlertType("PRICE_ABOVE");
            alert.setTargetPrice(15.50);

            // When
            alert.setStatus("ACTIVE");

            // Then
            assertNotNull(alert);
            assertEquals("ACTIVE", alert.getStatus());
            assertEquals("PRICE_ABOVE", alert.getAlertType());
            assertEquals(15.50, alert.getTargetPrice());
        }

        @Test
        @DisplayName("创建价格低于提醒")
        void createPriceBelowAlert_shouldCreateSuccessfully() {
            // Given
            PriceAlert alert = new PriceAlert();
            alert.setUserId(100L);
            alert.setSymbol("000001");
            alert.setSymbolType("STOCK");
            alert.setAlertType("PRICE_BELOW");
            alert.setTargetPrice(10.00);

            // When
            alert.setStatus("ACTIVE");

            // Then
            assertNotNull(alert);
            assertEquals("PRICE_BELOW", alert.getAlertType());
            assertEquals(10.00, alert.getTargetPrice());
        }

        @Test
        @DisplayName("创建涨跌幅提醒")
        void createPercentageChangeAlert_shouldCreateSuccessfully() {
            // Given
            PriceAlert alert = new PriceAlert();
            alert.setUserId(100L);
            alert.setSymbol("000001");
            alert.setSymbolType("STOCK");
            alert.setAlertType("PERCENTAGE_CHANGE");
            alert.setTargetChangePercent(5.0);

            // When
            alert.setStatus("ACTIVE");

            // Then
            assertNotNull(alert);
            assertEquals("PERCENTAGE_CHANGE", alert.getAlertType());
            assertEquals(5.0, alert.getTargetChangePercent());
        }

        @Test
        @DisplayName("涨跌幅达到目标百分比时应触发")
        void percentageChangeAboveTarget_shouldTrigger() {
            // Given
            PriceAlert alert = createTestAlert();
            alert.setAlertType("PERCENTAGE_CHANGE");
            alert.setTargetChangePercent(5.0);
            alert.setBasePrice(100.0); // 基准价格100元
            alert.setCurrentValue(105.0); // 涨幅5%

            // When
            boolean shouldTrigger = alert.shouldTrigger(105.0);

            // Then
            assertTrue(shouldTrigger, "涨跌幅达到5%应该触发提醒");
        }

        @Test
        @DisplayName("新创建提醒状态应为ACTIVE")
        void newAlert_shouldHaveActiveStatus() {
            // When
            PriceAlert alert = new PriceAlert();

            // Then
            assertEquals("ACTIVE", alert.getStatus());
        }
    }

    @Nested
    @DisplayName("提醒触发逻辑测试")
    class TriggerAlertTests {

        @Test
        @DisplayName("价格超过目标价时应触发")
        void priceAboveTarget_shouldTrigger() {
            // Given
            PriceAlert alert = createTestAlert();
            alert.setAlertType("PRICE_ABOVE");
            alert.setTargetPrice(15.50);

            // When
            boolean shouldTrigger = alert.shouldTrigger(16.00);

            // Then
            assertTrue(shouldTrigger, "价格16.00超过目标价15.50应该触发");
        }

        @Test
        @DisplayName("价格等于目标价时应触发")
        void priceEqualToTarget_shouldTrigger() {
            // Given
            PriceAlert alert = createTestAlert();
            alert.setAlertType("PRICE_ABOVE");
            alert.setTargetPrice(15.50);

            // When
            boolean shouldTrigger = alert.shouldTrigger(15.50);

            // Then
            assertTrue(shouldTrigger, "价格等于目标价应该触发");
        }

        @Test
        @DisplayName("价格低于目标价时不应触发")
        void priceBelowTarget_shouldNotTrigger() {
            // Given
            PriceAlert alert = createTestAlert();
            alert.setAlertType("PRICE_ABOVE");
            alert.setTargetPrice(15.50);

            // When
            boolean shouldTrigger = alert.shouldTrigger(15.00);

            // Then
            assertFalse(shouldTrigger, "价格低于目标价不应触发");
        }

        @Test
        @DisplayName("价格低于目标价时应触发价格下限提醒")
        void priceBelowTargetForPriceBelowAlert_shouldTrigger() {
            // Given
            PriceAlert alert = createTestAlert();
            alert.setAlertType("PRICE_BELOW");
            alert.setTargetPrice(10.00);

            // When
            boolean shouldTrigger = alert.shouldTrigger(9.50);

            // Then
            assertTrue(shouldTrigger, "价格低于下限应该触发");
        }

        @Test
        @DisplayName("非ACTIVE状态的提醒不应触发")
        void inactiveAlert_shouldNotTrigger() {
            // Given
            PriceAlert alert = createTestAlert();
            alert.setStatus("INACTIVE");
            alert.setAlertType("PRICE_ABOVE");
            alert.setTargetPrice(15.50);

            // When
            boolean shouldTrigger = alert.shouldTrigger(16.00);

            // Then
            assertFalse(shouldTrigger, "禁用的提醒不应触发");
        }

        @Test
        @DisplayName("触发后状态应变为TRIGGERED")
        void triggerAlert_shouldChangeStatusToTriggered() {
            // Given
            PriceAlert alert = createTestAlert();

            // When
            alert.trigger();

            // Then
            assertEquals("TRIGGERED", alert.getStatus());
            assertNotNull(alert.getLastTriggered());
        }

        @Test
        @DisplayName("null价格不应触发提醒")
        void nullPrice_shouldNotTrigger() {
            // Given
            PriceAlert alert = createTestAlert();

            // When
            boolean shouldTrigger = alert.shouldTrigger(null);

            // Then
            assertFalse(shouldTrigger);
        }
    }

    @Nested
    @DisplayName("提醒激活/停用测试")
    class ActivateDeactivateTests {

        @Test
        @DisplayName("activate应将状态设为ACTIVE")
        void activateAlert_shouldSetStatusToActive() {
            // Given
            PriceAlert alert = createTestAlert();
            alert.setStatus("INACTIVE");

            // When
            alert.activate();

            // Then
            assertEquals("ACTIVE", alert.getStatus());
        }

        @Test
        @DisplayName("deactivate应将状态设为INACTIVE")
        void deactivateAlert_shouldSetStatusToInactive() {
            // Given
            PriceAlert alert = createTestAlert();
            alert.setStatus("ACTIVE");

            // When
            alert.deactivate();

            // Then
            assertEquals("INACTIVE", alert.getStatus());
        }
    }

    @Nested
    @DisplayName("提醒历史记录测试")
    class AlertHistoryTests {

        @Test
        @DisplayName("应能从提醒创建历史记录")
        void createHistoryFromAlert_shouldCaptureAllFields() {
            // Given
            PriceAlert alert = createTestAlert();
            alert.setId(1L);
            alert.setAlertType("PRICE_ABOVE");
            alert.setTargetPrice(15.50);
            Double currentValue = 16.00;
            String reason = "价格达到目标";

            // When
            AlertHistory history = AlertHistory.createFromAlert(alert, currentValue, reason);

            // Then
            assertNotNull(history);
            assertEquals(alert.getUserId(), history.getUserId());
            assertEquals(alert.getId(), history.getAlertId());
            assertEquals(alert.getSymbol(), history.getSymbol());
            assertEquals(alert.getSymbolType(), history.getSymbolType());
            assertEquals(alert.getAlertType(), history.getAlertType());
            assertEquals(alert.getTargetPrice(), history.getTargetPrice());
            assertEquals(currentValue, history.getCurrentValue());
            assertEquals(reason, history.getTriggerReason());
            assertNotNull(history.getTriggeredAt());
        }

        @Test
        @DisplayName("新创建的AlertHistory应有当前时间戳")
        void newAlertHistory_shouldHaveCurrentTimestamp() {
            // When
            AlertHistory history = new AlertHistory();

            // Then
            assertNotNull(history.getTriggeredAt());
        }
    }

    @Nested
    @DisplayName("提醒实体字段测试")
    class AlertEntityTests {

        @Test
        @DisplayName("PriceAlert应能设置所有必要字段")
        void priceAlert_shouldHoldAllFields() {
            // Given
            PriceAlert alert = new PriceAlert();
            alert.setId(1L);
            alert.setUserId(100L);
            alert.setSymbol("000001");
            alert.setSymbolName("平安银行");
            alert.setSymbolType("STOCK");
            alert.setAlertType("PRICE_ABOVE");
            alert.setTargetPrice(15.50);
            alert.setCurrentValue(15.75);
            alert.setStatus("ACTIVE");
            alert.setDescription("股价突破15.5元");

            // Then
            assertEquals(1L, alert.getId());
            assertEquals(100L, alert.getUserId());
            assertEquals("000001", alert.getSymbol());
            assertEquals("平安银行", alert.getSymbolName());
            assertEquals("STOCK", alert.getSymbolType());
            assertEquals("PRICE_ABOVE", alert.getAlertType());
            assertEquals(15.50, alert.getTargetPrice());
            assertEquals(15.75, alert.getCurrentValue());
            assertEquals("ACTIVE", alert.getStatus());
            assertEquals("股价突破15.5元", alert.getDescription());
        }

        @Test
        @DisplayName("PriceAlert构造函数应正确初始化默认值")
        void priceAlertConstructor_shouldInitializeDefaults() {
            // When
            PriceAlert alert = new PriceAlert(100L, "000001", "STOCK");

            // Then
            assertEquals(100L, alert.getUserId());
            assertEquals("000001", alert.getSymbol());
            assertEquals("STOCK", alert.getSymbolType());
            assertEquals("ACTIVE", alert.getStatus());
            assertNotNull(alert.getCreatedAt());
            assertNotNull(alert.getUpdatedAt());
        }
    }

    @Nested
    @DisplayName("边界条件测试")
    class EdgeCaseTests {

        @Test
        @DisplayName("精确到小数点后两位的目标价格")
        void targetPrice_withTwoDecimalPlaces() {
            // Given
            PriceAlert alert = createTestAlert();
            alert.setTargetPrice(15.67);

            // When
            boolean shouldTrigger = alert.shouldTrigger(15.68);

            // Then
            assertTrue(shouldTrigger);
        }

        @Test
        @DisplayName("极大的目标价格")
        void veryLargeTargetPrice() {
            // Given
            PriceAlert alert = createTestAlert();
            alert.setTargetPrice(999999.99);

            // When
            boolean shouldTrigger = alert.shouldTrigger(1000000.00);

            // Then
            assertTrue(shouldTrigger);
        }

        @Test
        @DisplayName("极小的目标价格")
        void verySmallTargetPrice() {
            // Given
            PriceAlert alert = createTestAlert();
            alert.setAlertType("PRICE_BELOW");
            alert.setTargetPrice(0.01);

            // When
            boolean shouldTrigger = alert.shouldTrigger(0.005);

            // Then
            assertTrue(shouldTrigger);
        }

        @Test
        @DisplayName("负数目标价格（跌至负数）")
        void negativeTargetPrice() {
            // Given
            PriceAlert alert = createTestAlert();
            alert.setAlertType("PRICE_BELOW");
            alert.setTargetPrice(-5.00);

            // When
            boolean shouldTrigger = alert.shouldTrigger(-6.00);

            // Then
            assertTrue(shouldTrigger);
        }
    }
}
