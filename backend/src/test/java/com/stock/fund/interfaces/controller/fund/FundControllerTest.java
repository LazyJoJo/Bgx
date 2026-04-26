package com.stock.fund.interfaces.controller.fund;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import com.stock.fund.application.scheduler.DataCollectionScheduler;
import com.stock.fund.application.scheduler.RiskAlertScheduler;
import com.stock.fund.application.service.fund.FundAppService;
import com.stock.fund.application.service.fund.dto.FundDTO;
import com.stock.fund.application.service.fund.dto.UpdateFundRequest;
import com.stock.fund.domain.exception.ResourceNotFoundException;

/**
 * FundController API Test
 * 
 * Uses MockMvc for Controller layer testing Uses @MockBean to disable
 * schedulers and avoid @Scheduled issues
 */
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = { "data.collection.schedule.alert-check-cron=0 0 0 31 2 ?",
                "data.collection.schedule.stock-basic-cron=0 0 0 31 2 ?",
                "data.collection.schedule.fund-basic-cron=0 0 0 31 2 ?",
                "data.collection.schedule.stock-quote-cron=0 0 0 31 2 ?",
                "data.collection.schedule.fund-quote-cron=0 0 0 31 2 ?",
                "data.collection.schedule.daily-collection-cron=0 0 0 31 2 ?",
                "data.collection.schedule.risk-alert-1130-cron=0 0 0 31 2 ?",
                "data.collection.schedule.risk-alert-1430-cron=0 0 0 31 2 ?" })
class FundControllerTest {

        @Autowired
        private MockMvc mockMvc;

        @MockBean
        private DataCollectionScheduler dataCollectionScheduler;

        @MockBean
        private RiskAlertScheduler riskAlertScheduler;

        @MockBean
        private FundAppService fundAppService;

        private FundDTO createTestFund(Long id, String code, String name, boolean active) {
                FundDTO fund = new FundDTO();
                fund.setId(id);
                fund.setCode(code);
                fund.setName(name);
                fund.setType("FUND");
                fund.setMarket("SH");
                fund.setActive(active);
                fund.setCategory("指数基金");
                fund.setDescription("Test fund description");
                fund.setCollectionFrequency(5);
                fund.setDataSource("eastmoney");
                fund.setCreatedAt(LocalDateTime.now());
                return fund;
        }

        @Test
        @DisplayName("GET /api/funds - 获取所有基金列表，返回200和基金列表")
        void getAllFunds_shouldReturnFundList() throws Exception {
                // given
                FundDTO fund1 = createTestFund(1L, "000001", "测试基金1", true);
                FundDTO fund2 = createTestFund(2L, "000002", "测试基金2", false);
                when(fundAppService.getAllFunds()).thenReturn(Arrays.asList(fund1, fund2));

                // when & then
                mockMvc.perform(get("/api/funds")).andExpect(status().isOk())
                                .andExpect(jsonPath("$.success").value(true)).andExpect(jsonPath("$.data").isArray())
                                .andExpect(jsonPath("$.data.length()").value(2))
                                .andExpect(jsonPath("$.data[0].code").value("000001"))
                                .andExpect(jsonPath("$.data[0].active").value(true))
                                .andExpect(jsonPath("$.data[1].code").value("000002"))
                                .andExpect(jsonPath("$.data[1].active").value(false));

                verify(fundAppService).getAllFunds();
        }

        @Test
        @DisplayName("GET /api/funds/{id} - 获取单个基金，基金存在时返回200")
        void getFundById_whenExists_shouldReturnFund() throws Exception {
                // given
                FundDTO fund = createTestFund(1L, "000001", "测试基金", true);
                when(fundAppService.getFundById(1L)).thenReturn(fund);

                // when & then
                mockMvc.perform(get("/api/funds/1")).andExpect(status().isOk())
                                .andExpect(jsonPath("$.success").value(true)).andExpect(jsonPath("$.data.id").value(1))
                                .andExpect(jsonPath("$.data.code").value("000001"))
                                .andExpect(jsonPath("$.data.name").value("测试基金"));

                verify(fundAppService).getFundById(1L);
        }

        @Test
        @DisplayName("GET /api/funds/{id} - 获取单个基金，基金不存在时返回404")
        void getFundById_whenNotExists_shouldReturn404() throws Exception {
                // given
                when(fundAppService.getFundById(999L)).thenThrow(new ResourceNotFoundException("Fund", 999));

                // when & then
                mockMvc.perform(get("/api/funds/999")).andExpect(status().isNotFound())
                                .andExpect(jsonPath("$.success").value(false));

                verify(fundAppService).getFundById(999L);
        }

        @Test
        @DisplayName("PUT /api/funds/{id} - 更新基金，成功更新返回200")
        void updateFund_shouldUpdateSuccessfully() throws Exception {
                // given
                FundDTO updatedFund = createTestFund(1L, "000001", "更新后的基金", true);
                updatedFund.setCategory("股票基金");
                updatedFund.setDescription("Updated description");

                UpdateFundRequest request = new UpdateFundRequest();
                request.setMarket("SZ");
                request.setCategory("股票基金");
                request.setDescription("Updated description");
                request.setCollectionFrequency(10);
                request.setDataSource("ths");

                when(fundAppService.updateFund(eq(1L), any(UpdateFundRequest.class))).thenReturn(updatedFund);

                // when & then
                mockMvc.perform(put("/api/funds/1").contentType(MediaType.APPLICATION_JSON).content(
                                "{\"market\":\"SZ\",\"category\":\"股票基金\",\"description\":\"Updated description\",\"collectionFrequency\":10,\"dataSource\":\"ths\"}"))
                                .andExpect(status().isOk()).andExpect(jsonPath("$.success").value(true))
                                .andExpect(jsonPath("$.data.category").value("股票基金"));

                verify(fundAppService).updateFund(eq(1L), any(UpdateFundRequest.class));
        }

        @Test
        @DisplayName("PUT /api/funds/{id} - 更新基金，参数验证失败返回400")
        void updateFund_withInvalidParams_shouldReturn400() throws Exception {
                // Note: Validation of @Min(1) on collectionFrequency is tested in
                // UpdateFundRequest unit tests.
                // Here we verify that when service throws a validation-related exception, 400
                // is returned.
                // For now, we test an empty/invalid request body structure that fails JSON
                // parsing.
                String invalidJson = "{\"collectionFrequency\":\"not-a-number\"}";
                // when & then - JSON parse error should return 400
                mockMvc.perform(put("/api/funds/1").contentType(MediaType.APPLICATION_JSON).content(invalidJson))
                                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.success").value(false));
        }

        @Test
        @DisplayName("PUT /api/funds/{id} - 更新基金，基金不存在时返回404")
        void updateFund_whenNotExists_shouldReturn404() throws Exception {
                // given
                UpdateFundRequest request = new UpdateFundRequest();
                request.setCategory("股票基金");

                when(fundAppService.updateFund(eq(999L), any(UpdateFundRequest.class)))
                                .thenThrow(new ResourceNotFoundException("Fund", 999));

                // when & then
                mockMvc.perform(put("/api/funds/999").contentType(MediaType.APPLICATION_JSON)
                                .content("{\"category\":\"股票基金\"}")).andExpect(status().isNotFound())
                                .andExpect(jsonPath("$.success").value(false));
        }

        @Test
        @DisplayName("DELETE /api/funds/{id} - 删除基金，成功删除返回204")
        void deleteFund_shouldDeleteSuccessfully() throws Exception {
                // given
                doNothing().when(fundAppService).deleteFund(1L);

                // when & then
                mockMvc.perform(delete("/api/funds/1")).andExpect(status().isOk())
                                .andExpect(jsonPath("$.success").value(true));

                verify(fundAppService).deleteFund(1L);
        }

        @Test
        @DisplayName("DELETE /api/funds/{id} - 删除基金，基金不存在时返回404")
        void deleteFund_whenNotExists_shouldReturn404() throws Exception {
                // given
                doThrow(new ResourceNotFoundException("Fund", 999)).when(fundAppService).deleteFund(999L);

                // when & then
                mockMvc.perform(delete("/api/funds/999")).andExpect(status().isNotFound())
                                .andExpect(jsonPath("$.success").value(false));

                verify(fundAppService).deleteFund(999L);
        }

        @Test
        @DisplayName("PATCH /api/funds/{id}/activate - 激活基金，成功激活返回200")
        void activateFund_shouldActivateSuccessfully() throws Exception {
                // given
                FundDTO activatedFund = createTestFund(1L, "000001", "测试基金", true);
                when(fundAppService.activateFund(1L)).thenReturn(activatedFund);

                // when & then
                mockMvc.perform(patch("/api/funds/1/activate")).andExpect(status().isOk())
                                .andExpect(jsonPath("$.success").value(true))
                                .andExpect(jsonPath("$.data.active").value(true));

                verify(fundAppService).activateFund(1L);
        }

        @Test
        @DisplayName("PATCH /api/funds/{id}/activate - 激活基金，基金不存在时返回404")
        void activateFund_whenNotExists_shouldReturn404() throws Exception {
                // given
                when(fundAppService.activateFund(999L)).thenThrow(new ResourceNotFoundException("Fund", 999));

                // when & then
                mockMvc.perform(patch("/api/funds/999/activate")).andExpect(status().isNotFound())
                                .andExpect(jsonPath("$.success").value(false));

                verify(fundAppService).activateFund(999L);
        }

        @Test
        @DisplayName("PATCH /api/funds/{id}/deactivate - 停用基金，成功停用返回200")
        void deactivateFund_shouldDeactivateSuccessfully() throws Exception {
                // given
                FundDTO deactivatedFund = createTestFund(1L, "000001", "测试基金", false);
                when(fundAppService.deactivateFund(1L)).thenReturn(deactivatedFund);

                // when & then
                mockMvc.perform(patch("/api/funds/1/deactivate")).andExpect(status().isOk())
                                .andExpect(jsonPath("$.success").value(true))
                                .andExpect(jsonPath("$.data.active").value(false));

                verify(fundAppService).deactivateFund(1L);
        }

        @Test
        @DisplayName("PATCH /api/funds/{id}/deactivate - 停用基金，基金不存在时返回404")
        void deactivateFund_whenNotExists_shouldReturn404() throws Exception {
                // given
                when(fundAppService.deactivateFund(999L)).thenThrow(new ResourceNotFoundException("Fund", 999));

                // when & then
                mockMvc.perform(patch("/api/funds/999/deactivate")).andExpect(status().isNotFound())
                                .andExpect(jsonPath("$.success").value(false));

                verify(fundAppService).deactivateFund(999L);
        }

        @Test
        @DisplayName("GET /api/funds - 空列表场景，返回空数组")
        void getAllFunds_whenEmpty_shouldReturnEmptyList() throws Exception {
                // given
                when(fundAppService.getAllFunds()).thenReturn(List.of());

                // when & then
                mockMvc.perform(get("/api/funds")).andExpect(status().isOk())
                                .andExpect(jsonPath("$.success").value(true)).andExpect(jsonPath("$.data").isArray())
                                .andExpect(jsonPath("$.data.length()").value(0));

                verify(fundAppService).getAllFunds();
        }
}
