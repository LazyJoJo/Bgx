package com.stock.fund.application.service.alert;

import com.stock.fund.application.service.alert.dto.BatchCreateAlertRequest;
import com.stock.fund.application.service.alert.dto.BatchCreateAlertResponse;
import com.stock.fund.application.service.alert.dto.CreateAlertRequest;
import com.stock.fund.application.service.alert.dto.CreateAlertResponse;
import com.stock.fund.application.service.alert.impl.AlertAppServiceImpl;
import com.stock.fund.domain.entity.alert.PriceAlert;
import com.stock.fund.domain.repository.alert.PriceAlertRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 批量创建提醒功能单元测试
 */
@ExtendWith(MockitoExtension.class)
class AlertAppServiceBatchTest {

    @Mock
    private PriceAlertRepository priceAlertRepository;

    @InjectMocks
    private AlertAppServiceImpl alertAppService;

    private PriceAlert createAlert(Long id, Long userId, String symbol, String symbolType) {
        PriceAlert alert = new PriceAlert();
        alert.setId(id);
        alert.setUserId(userId);
        alert.setSymbol(symbol);
        alert.setSymbolType(symbolType);
        alert.setSymbolName("Test");
        alert.setAlertType("PRICE_ABOVE");
        alert.setTargetPrice(50.0);
        alert.setStatus("ACTIVE");
        return alert;
    }

    @Test
    @DisplayName("批量创建 - 单条直接创建")
    void testSingleCreate() {
        // Given
        CreateAlertRequest request = new CreateAlertRequest();
        request.setUserId(999L);
        request.setSymbol("TEST001");
        request.setSymbolType("STOCK");
        request.setAlertType("PRICE_ABOVE");
        request.setTargetPrice(50.0);
        request.setStatus(true);

        when(priceAlertRepository.findByUserIdAndSymbolAndSymbolType(eq(999L), eq("TEST001"), eq("STOCK")))
                .thenReturn(Optional.empty());
        when(priceAlertRepository.save(any(PriceAlert.class))).thenAnswer(invocation -> {
            PriceAlert saved = invocation.getArgument(0);
            saved.setId(1L);
            return saved;
        });

        // When
        CreateAlertResponse response = alertAppService.createAlert(request);

        // Then
        assertNotNull(response);
        assertTrue(response.isCreated());
        assertNotNull(response.getAlert());
        assertEquals("TEST001", response.getAlert().getSymbol());
        verify(priceAlertRepository).save(any(PriceAlert.class));
    }

    @Test
    @DisplayName("批量创建 - 正常批量创建多个提醒")
    void testBatchCreate_Success() {
        // Given
        BatchCreateAlertRequest request = new BatchCreateAlertRequest();
        request.setUserId(1L);
        request.setSymbolType("STOCK");
        request.setSymbols(Arrays.asList("600000", "600519"));
        request.setAlertType("PRICE_ABOVE");
        request.setTargetPrice(15.5000);
        request.setStatus(true);

        when(priceAlertRepository.findByUserIdAndSymbolAndSymbolType(anyLong(), anyString(), anyString()))
                .thenReturn(Optional.empty());
        when(priceAlertRepository.save(any(PriceAlert.class))).thenAnswer(invocation -> {
            PriceAlert saved = invocation.getArgument(0);
            saved.setId(System.nanoTime());
            return saved;
        });

        // When
        BatchCreateAlertResponse response = alertAppService.batchCreateAlert(request);

        // Then
        assertNotNull(response);
        assertEquals(2, response.getSuccessCount());
        assertEquals(0, response.getFailCount());
        assertEquals(2, response.getSuccessList().size());
        verify(priceAlertRepository, times(2)).save(any(PriceAlert.class));
    }

    @Test
    @DisplayName("批量创建 - 空symbols列表")
    void testBatchCreate_EmptySymbols() {
        // Given
        BatchCreateAlertRequest request = new BatchCreateAlertRequest();
        request.setUserId(1L);
        request.setSymbolType("STOCK");
        request.setSymbols(Collections.emptyList());
        request.setAlertType("PRICE_ABOVE");
        request.setTargetPrice(100.0);
        request.setStatus(true);

        // When
        BatchCreateAlertResponse response = alertAppService.batchCreateAlert(request);

        // Then
        assertNotNull(response);
        assertEquals(0, response.getSuccessCount());
        assertEquals(0, response.getFailCount());
        verify(priceAlertRepository, never()).save(any());
    }

    @Test
    @DisplayName("批量创建 - 标的已存在时返回已有提醒")
    void testBatchCreate_ExistingAlert() {
        // Given
        BatchCreateAlertRequest request = new BatchCreateAlertRequest();
        request.setUserId(1L);
        request.setSymbolType("STOCK");
        request.setSymbols(Arrays.asList("600000"));
        request.setAlertType("PRICE_ABOVE");
        request.setTargetPrice(15.5000);
        request.setStatus(true);

        PriceAlert existingAlert = createAlert(100L, 1L, "600000", "STOCK");
        when(priceAlertRepository.findByUserIdAndSymbolAndSymbolType(eq(1L), eq("600000"), eq("STOCK")))
                .thenReturn(Optional.of(existingAlert));

        // When
        BatchCreateAlertResponse response = alertAppService.batchCreateAlert(request);

        // Then
        assertNotNull(response);
        assertEquals(1, response.getSuccessCount());
        assertEquals(0, response.getFailCount());
        // 已存在的提醒也被计入成功，但不调用save
        verify(priceAlertRepository, never()).save(any());
    }
}
