package com.stock.fund.application.service.alert;

import com.stock.fund.application.service.alert.dto.*;
import com.stock.fund.application.service.alert.impl.AlertAppServiceImpl;
import com.stock.fund.domain.entity.alert.PriceAlert;
import com.stock.fund.domain.repository.alert.PriceAlertRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 统一创建提醒接口测试
 * 测试单个标的和批量标的创建逻辑
 */
@ExtendWith(MockitoExtension.class)
class AlertAppServiceUnifiedTest {

    @Mock
    private PriceAlertRepository priceAlertRepository;

    @InjectMocks
    private AlertAppServiceImpl alertAppService;

    private PriceAlert createTestAlert(Long id, String symbol, String symbolType, String alertType) {
        PriceAlert alert = new PriceAlert();
        alert.setId(id);
        alert.setUserId(1L);
        alert.setSymbol(symbol);
        alert.setSymbolType(symbolType);
        alert.setSymbolName("测试股票" + symbol);
        alert.setAlertType(alertType);
        alert.setTargetPrice(100.0);
        alert.setStatus("ACTIVE");
        alert.setCreatedAt(LocalDateTime.now());
        return alert;
    }

    // ==================== 统一创建接口测试 ====================

    @Test
    @DisplayName("统一创建 - 单个标的创建成功")
    void testUnifiedCreate_SingleSymbolSuccess() {
        // Given
        AlertCreateRequest request = new AlertCreateRequest();
        request.setUserId(1L);
        request.setSymbolType("STOCK");
        request.setSymbol("600000");
        request.setAlertType("PRICE_ABOVE");
        request.setTargetPrice(50.0);
        request.setEnabled(true);

        when(priceAlertRepository.findByUserIdAndSymbolAndSymbolType(
                eq(1L), eq("600000"), eq("STOCK")))
                .thenReturn(Optional.empty());

        when(priceAlertRepository.save(any(PriceAlert.class))).thenAnswer(invocation -> {
            PriceAlert alert = invocation.getArgument(0);
            alert.setId(1L);
            alert.setCreatedAt(LocalDateTime.now());
            return alert;
        });

        // When
        AlertCreateResponse response = alertAppService.createAlertUnified(request);

        // Then
        assertNotNull(response);
        assertEquals(1, response.getTotalCount());
        assertEquals(1, response.getCreatedCount());
        assertEquals(0, response.getExistingCount());
        assertEquals(0, response.getFailureCount());
        assertNotNull(response.getBatchId());
        assertEquals(1, response.getCreatedList().size());
        assertEquals("600000", response.getCreatedList().get(0).getSymbol());
    }

    @Test
    @DisplayName("统一创建 - 单个标的已存在")
    void testUnifiedCreate_SingleSymbolExists() {
        // Given
        AlertCreateRequest request = new AlertCreateRequest();
        request.setUserId(1L);
        request.setSymbolType("STOCK");
        request.setSymbol("600000");
        request.setAlertType("PRICE_ABOVE");
        request.setTargetPrice(50.0);

        PriceAlert existingAlert = createTestAlert(100L, "600000", "STOCK", "PRICE_ABOVE");
        when(priceAlertRepository.findByUserIdAndSymbolAndSymbolType(
                eq(1L), eq("600000"), eq("STOCK")))
                .thenReturn(Optional.of(existingAlert));

        // When
        AlertCreateResponse response = alertAppService.createAlertUnified(request);

        // Then
        assertNotNull(response);
        assertEquals(1, response.getTotalCount());
        assertEquals(0, response.getCreatedCount());
        assertEquals(1, response.getExistingCount());
        assertEquals(0, response.getFailureCount());
        assertEquals(1, response.getExistingList().size());
        assertEquals(100L, response.getExistingList().get(0).getAlertId());
    }

    @Test
    @DisplayName("统一创建 - 批量标的全部创建成功")
    void testUnifiedCreate_BatchAllSuccess() {
        // Given
        AlertCreateRequest request = new AlertCreateRequest();
        request.setUserId(1L);
        request.setSymbolType("STOCK");
        request.setSymbols(Arrays.asList("600000", "600519", "000001"));
        request.setAlertType("PRICE_ABOVE");
        request.setTargetPrice(50.0);
        request.setEnabled(true);

        when(priceAlertRepository.findByUserIdAndSymbolsAndSymbolTypeAndAlertType(
                eq(1L), anyList(), eq("STOCK"), eq("PRICE_ABOVE")))
                .thenReturn(Collections.emptyList());

        when(priceAlertRepository.saveAll(anyList())).thenAnswer(invocation -> {
            List<PriceAlert> alerts = invocation.getArgument(0);
            for (int i = 0; i < alerts.size(); i++) {
                alerts.get(i).setId((long) (i + 1));
                alerts.get(i).setCreatedAt(LocalDateTime.now());
            }
            return alerts;
        });

        // When
        AlertCreateResponse response = alertAppService.createAlertUnified(request);

        // Then
        assertNotNull(response);
        assertEquals(3, response.getTotalCount());
        assertEquals(3, response.getCreatedCount());
        assertEquals(0, response.getExistingCount());
        assertEquals(0, response.getFailureCount());
        assertNotNull(response.getBatchId());
        assertTrue(response.getBatchId().startsWith("batch_"));
    }

    @Test
    @DisplayName("统一创建 - 批量标的部分已存在")
    void testUnifiedCreate_BatchPartialExists() {
        // Given
        AlertCreateRequest request = new AlertCreateRequest();
        request.setUserId(1L);
        request.setSymbolType("STOCK");
        request.setSymbols(Arrays.asList("600000", "600519", "000001"));
        request.setAlertType("PRICE_ABOVE");
        request.setTargetPrice(50.0);
        request.setEnabled(true);

        // 600000已存在
        PriceAlert existingAlert = createTestAlert(100L, "600000", "STOCK", "PRICE_ABOVE");
        when(priceAlertRepository.findByUserIdAndSymbolsAndSymbolTypeAndAlertType(
                eq(1L), anyList(), eq("STOCK"), eq("PRICE_ABOVE")))
                .thenReturn(Arrays.asList(existingAlert));

        when(priceAlertRepository.saveAll(anyList())).thenAnswer(invocation -> {
            List<PriceAlert> alerts = invocation.getArgument(0);
            for (int i = 0; i < alerts.size(); i++) {
                alerts.get(i).setId((long) (i + 200));
                alerts.get(i).setCreatedAt(LocalDateTime.now());
            }
            return alerts;
        });

        // When
        AlertCreateResponse response = alertAppService.createAlertUnified(request);

        // Then
        assertNotNull(response);
        assertEquals(3, response.getTotalCount());
        assertEquals(2, response.getCreatedCount());  // 新创建2个
        assertEquals(1, response.getExistingCount());  // 已存在1个
        assertEquals(0, response.getFailureCount());
        assertEquals(2, response.getCreatedList().size());
        assertEquals(1, response.getExistingList().size());
    }

    @Test
    @DisplayName("统一创建 - 批量标的参数校验失败（超过100个）")
    void testUnifiedCreate_BatchExceedLimit() {
        // Given
        AlertCreateRequest request = new AlertCreateRequest();
        request.setUserId(1L);
        request.setSymbolType("STOCK");
        // 创建101个标的
        request.setSymbols(java.util.stream.IntStream.range(0, 101)
                .mapToObj(i -> "S" + i)
                .toList());
        request.setAlertType("PRICE_ABOVE");
        request.setTargetPrice(50.0);

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            alertAppService.createAlertUnified(request);
        });
    }

    @Test
    @DisplayName("统一创建 - 批量标的参数校验失败（价格为负数）")
    void testUnifiedCreate_NegativePrice() {
        // Given
        AlertCreateRequest request = new AlertCreateRequest();
        request.setUserId(1L);
        request.setSymbolType("STOCK");
        request.setSymbols(Arrays.asList("600000"));
        request.setAlertType("PRICE_ABOVE");
        request.setTargetPrice(-10.0);

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            alertAppService.createAlertUnified(request);
        });
    }

    @Test
    @DisplayName("统一创建 - 涨跌幅类型校验（涨跌幅为0）")
    void testUnifiedCreate_ZeroChangePercent() {
        // Given
        AlertCreateRequest request = new AlertCreateRequest();
        request.setUserId(1L);
        request.setSymbolType("STOCK");
        request.setSymbols(Arrays.asList("600000"));
        request.setAlertType("PERCENTAGE_CHANGE");
        request.setTargetChangePercent(0.0);

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            alertAppService.createAlertUnified(request);
        });
    }

    @Test
    @DisplayName("统一创建 - 涨跌幅类型校验（涨跌幅超范围）")
    void testUnifiedCreate_ChangePercentOutOfRange() {
        // Given
        AlertCreateRequest request = new AlertCreateRequest();
        request.setUserId(1L);
        request.setSymbolType("STOCK");
        request.setSymbols(Arrays.asList("600000"));
        request.setAlertType("PERCENTAGE_CHANGE");
        request.setTargetChangePercent(150.0);  // 超过99%

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            alertAppService.createAlertUnified(request);
        });
    }

    @Test
    @DisplayName("统一创建 - symbol和symbols都为空")
    void testUnifiedCreate_BothSymbolAndSymbolsEmpty() {
        // Given
        AlertCreateRequest request = new AlertCreateRequest();
        request.setUserId(1L);
        request.setSymbolType("STOCK");
        request.setAlertType("PRICE_ABOVE");
        request.setTargetPrice(50.0);
        // symbol和symbols都为空

        // When
        AlertCreateResponse response = alertAppService.createAlertUnified(request);

        // Then
        assertNotNull(response);
        assertEquals(0, response.getTotalCount());
        assertEquals(0, response.getCreatedCount());
        assertEquals(0, response.getExistingCount());
        assertEquals(0, response.getFailureCount());
    }

    @Test
    @DisplayName("统一创建 - symbol优先于symbols")
    void testUnifiedCreate_SymbolPriorityOverSymbols() {
        // Given - 如果同时提供了symbol和symbols，应该使用symbol
        AlertCreateRequest request = new AlertCreateRequest();
        request.setUserId(1L);
        request.setSymbolType("STOCK");
        request.setSymbol("600000");  // 单个标的
        request.setSymbols(Arrays.asList("600001", "600002"));  // 批量标的（应该被忽略）
        request.setAlertType("PRICE_ABOVE");
        request.setTargetPrice(50.0);

        when(priceAlertRepository.findByUserIdAndSymbolAndSymbolType(
                eq(1L), eq("600000"), eq("STOCK")))
                .thenReturn(Optional.empty());

        when(priceAlertRepository.save(any(PriceAlert.class))).thenAnswer(invocation -> {
            PriceAlert alert = invocation.getArgument(0);
            alert.setId(1L);
            alert.setCreatedAt(LocalDateTime.now());
            return alert;
        });

        // When
        AlertCreateResponse response = alertAppService.createAlertUnified(request);

        // Then - 应该只处理symbol，忽略symbols
        assertNotNull(response);
        assertEquals(1, response.getTotalCount());
        assertEquals(1, response.getCreatedCount());
        assertEquals("600000", response.getCreatedList().get(0).getSymbol());
    }

    @Test
    @DisplayName("统一创建 - FUND类型提醒")
    void testUnifiedCreate_FundTypeAlert() {
        // Given
        AlertCreateRequest request = new AlertCreateRequest();
        request.setUserId(1L);
        request.setSymbolType("FUND");
        request.setSymbol("000001");
        request.setAlertType("PRICE_ABOVE");
        request.setTargetPrice(1.5);
        request.setEnabled(true);

        when(priceAlertRepository.findByUserIdAndSymbolAndSymbolType(
                eq(1L), eq("000001"), eq("FUND")))
                .thenReturn(Optional.empty());

        when(priceAlertRepository.save(any(PriceAlert.class))).thenAnswer(invocation -> {
            PriceAlert alert = invocation.getArgument(0);
            alert.setId(1L);
            alert.setCreatedAt(LocalDateTime.now());
            return alert;
        });

        // When
        AlertCreateResponse response = alertAppService.createAlertUnified(request);

        // Then
        assertNotNull(response);
        assertEquals(1, response.getTotalCount());
        assertEquals(1, response.getCreatedCount());
        assertEquals("FUND", request.getSymbolType());
    }
}
