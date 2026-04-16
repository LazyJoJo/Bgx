package com.stock.fund.interfaces.controller.riskalert;

import com.stock.fund.application.scheduler.DataCollectionScheduler;
import com.stock.fund.application.scheduler.RiskAlertScheduler;
import com.stock.fund.application.service.riskalert.RiskAlertAppService;
import com.stock.fund.application.service.riskalert.dto.RiskAlertMergeDTO;
import com.stock.fund.application.service.riskalert.dto.RiskAlertSummaryDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * RiskAlertController API 测试
 * 使用 MockMvc 进行 Controller 层测试
 * 通过 @MockBean 禁用调度器避免 @Scheduled 问题
 */
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
    "data.collection.schedule.alert-check-cron=0 0 0 31 2 ?",
    "data.collection.schedule.stock-basic-cron=0 0 0 31 2 ?",
    "data.collection.schedule.fund-basic-cron=0 0 0 31 2 ?",
    "data.collection.schedule.stock-quote-cron=0 0 0 31 2 ?",
    "data.collection.schedule.fund-quote-cron=0 0 0 31 2 ?",
    "data.collection.schedule.daily-collection-cron=0 0 0 31 2 ?",
    "data.collection.schedule.risk-alert-1130-cron=0 0 0 31 2 ?",
    "data.collection.schedule.risk-alert-1430-cron=0 0 0 31 2 ?"
})
class RiskAlertControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DataCollectionScheduler dataCollectionScheduler;

    @MockBean
    private RiskAlertScheduler riskAlertScheduler;

    @MockBean
    private RiskAlertAppService riskAlertAppService;

    @Test
    @DisplayName("GET /api/risk-alerts/user/{userId} - 获取用户风险提醒列表")
    void getUserRiskAlerts_shouldReturnMergedAlerts() throws Exception {
        // given
        RiskAlertMergeDTO stockAlert = new RiskAlertMergeDTO(
                1L, "000001", "STOCK", "平安银行",
                LocalDate.now(), 2,
                new BigDecimal("5.50"), new BigDecimal("3.20"),
                new BigDecimal("12.50"), new BigDecimal("11.80"),
                false, LocalDateTime.now(), List.of()
        );
        RiskAlertMergeDTO fundAlert = new RiskAlertMergeDTO(
                2L, "000011", "FUND", "测试基金",
                LocalDate.now(), 1,
                new BigDecimal("3.50"), new BigDecimal("3.50"),
                new BigDecimal("1.5500"), new BigDecimal("1.4800"),
                false, LocalDateTime.now(), List.of()
        );
        when(riskAlertAppService.getMergedRiskAlerts(eq(1L), any(), anyInt()))
                .thenReturn(Arrays.asList(stockAlert, fundAlert));

        // when & then
        mockMvc.perform(get("/api/risk-alerts/user/1")
                        .param("limit", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].symbol").value("000001"))
                .andExpect(jsonPath("$.data[0].symbolType").value("STOCK"))
                .andExpect(jsonPath("$.data[1].symbol").value("000011"))
                .andExpect(jsonPath("$.data[1].symbolType").value("FUND"));
    }

    @Test
    @DisplayName("GET /api/risk-alerts/today - 获取今日风险提醒")
    void getTodayRiskAlerts_shouldReturnGroupedAlerts() throws Exception {
        // given
        RiskAlertSummaryDTO.RiskAlertItemDTO item1 = RiskAlertSummaryDTO.RiskAlertItemDTO.builder()
                .id(1L).symbol("000001").symbolType("STOCK").symbolName("平安银行")
                .timePoint("14:30").hasRisk(true)
                .changePercent(new BigDecimal("5.50"))
                .currentPrice(new BigDecimal("12.50"))
                .yesterdayClose(new BigDecimal("11.80"))
                .isRead(false)
                .triggeredAt("2026-04-11 14:30:00")
                .build();

        RiskAlertSummaryDTO.RiskAlertItemDTO item2 = RiskAlertSummaryDTO.RiskAlertItemDTO.builder()
                .id(2L).symbol("000011").symbolType("FUND").symbolName("测试基金")
                .timePoint("14:30").hasRisk(true)
                .changePercent(new BigDecimal("3.50"))
                .currentPrice(new BigDecimal("1.5500"))
                .yesterdayClose(new BigDecimal("1.4800"))
                .isRead(false)
                .triggeredAt("2026-04-11 14:30:00")
                .build();

        RiskAlertSummaryDTO summary = RiskAlertSummaryDTO.builder()
                .alertDate(LocalDate.now())
                .totalCount(2)
                .unreadCount(2)
                .items(Arrays.asList(item1, item2))
                .build();

        when(riskAlertAppService.getTodayRiskAlerts(1L))
                .thenReturn(List.of(summary));

        // when & then
        mockMvc.perform(get("/api/risk-alerts/today"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].items.length()").value(2))
                .andExpect(jsonPath("$.data[0].items[0].symbolType").value("STOCK"))
                .andExpect(jsonPath("$.data[0].items[1].symbolType").value("FUND"));
    }

    @Test
    @DisplayName("GET /api/risk-alerts/user/{userId}/unread-count - 获取未读数量")
    void getUnreadCount_shouldReturnCount() throws Exception {
        // given
        when(riskAlertAppService.getUnreadCount(1L)).thenReturn(5L);

        // when & then
        mockMvc.perform(get("/api/risk-alerts/user/1/unread-count"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.total").value(5));
    }

    @Test
    @DisplayName("PATCH /api/risk-alerts/{id}/read - 标记单条已读")
    void markAsRead_shouldMarkAlertAsRead() throws Exception {
        // given
        doNothing().when(riskAlertAppService).markAsRead(1L);

        // when & then
        mockMvc.perform(patch("/api/risk-alerts/1/read"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(riskAlertAppService).markAsRead(1L);
    }

    @Test
    @DisplayName("POST /api/risk-alerts/user/{userId}/mark-read - 标记全部已读")
    void markAllAsRead_shouldMarkAllAlertsAsRead() throws Exception {
        // given
        doNothing().when(riskAlertAppService).markAllAsRead(1L);

        // when & then
        mockMvc.perform(post("/api/risk-alerts/user/1/mark-read"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(riskAlertAppService).markAllAsRead(1L);
    }

    @Test
    @DisplayName("GET /api/risk-alerts/user/{userId}/today-count - 获取今日风险提醒数量")
    void getTodayRiskAlertCount_shouldReturnCount() throws Exception {
        // given
        when(riskAlertAppService.getTodayRiskAlertCount(1L)).thenReturn(3);

        // when & then
        mockMvc.perform(get("/api/risk-alerts/user/1/today-count"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.total").value(3));
    }

    @Test
    @DisplayName("POST /api/risk-alerts/check - 手动触发风险检测")
    void checkRiskAlerts_shouldTriggerCheck() throws Exception {
        // given
        doNothing().when(riskAlertAppService).checkAndCreateRiskAlerts(anyString());

        // when & then - 验证响应成功且服务被调用
        mockMvc.perform(post("/api/risk-alerts/check"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(riskAlertAppService).checkAndCreateRiskAlerts(anyString());
    }

    @Test
    @DisplayName("DELETE /api/risk-alerts/{id} - 删除风险提醒")
    void deleteRiskAlert_shouldDeleteAlert() throws Exception {
        // given
        doNothing().when(riskAlertAppService).deleteById(1L);

        // when & then
        mockMvc.perform(delete("/api/risk-alerts/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(riskAlertAppService).deleteById(1L);
    }

    @Test
    @DisplayName("GET /api/risk-alerts/user/{userId} - cursor分页查询")
    void getUserRiskAlerts_withCursor_shouldReturnPaginatedAlerts() throws Exception {
        // given
        RiskAlertMergeDTO alert = new RiskAlertMergeDTO(
                10L, "000002", "STOCK", "招商银行",
                LocalDate.now(), 1,
                new BigDecimal("2.30"), new BigDecimal("2.30"),
                new BigDecimal("35.80"), new BigDecimal("35.00"),
                false, LocalDateTime.now(), List.of()
        );
        when(riskAlertAppService.getMergedRiskAlerts(eq(1L), eq(1234567890L), anyInt()))
                .thenReturn(List.of(alert));

        // when & then
        mockMvc.perform(get("/api/risk-alerts/user/1")
                        .param("cursor", "1234567890")
                        .param("limit", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].symbol").value("000002"));
    }
}
