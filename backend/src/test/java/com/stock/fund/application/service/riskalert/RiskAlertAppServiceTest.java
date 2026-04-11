package com.stock.fund.application.service.riskalert;

import com.stock.fund.application.service.riskalert.dto.RiskAlertMergeDTO;
import com.stock.fund.application.service.riskalert.impl.RiskAlertAppServiceImpl;
import com.stock.fund.application.service.riskalert.dto.RiskAlertPageResponse;
import com.stock.fund.application.service.riskalert.dto.RiskAlertQueryDTO;
import com.stock.fund.application.service.riskalert.dto.RiskAlertSummaryDTO;
import com.stock.fund.domain.entity.alert.PriceAlert;
import com.stock.fund.domain.entity.riskalert.RiskAlert;
import com.stock.fund.domain.repository.RiskAlertQuery;
import com.stock.fund.domain.repository.RiskAlertRepository;
import com.stock.fund.domain.service.riskalert.RiskAlertDomainService;
import com.stock.fund.application.service.alert.AlertAppService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * 风险提醒应用服务测试
 * TDD RED阶段：编写测试用例，验证业务逻辑
 */
@ExtendWith(MockitoExtension.class)
class RiskAlertAppServiceTest {

    @Mock
    private RiskAlertRepository riskAlertRepository;

    @Mock
    private AlertAppService alertAppService;

    @Mock
    private RiskAlertDomainService riskAlertDomainService;

    @InjectMocks
    private RiskAlertAppServiceImpl riskAlertAppService;

    private RiskAlert testRiskAlert;

    @BeforeEach
    void setUp() {
        testRiskAlert = new RiskAlert();
        testRiskAlert.setId(1L);
        testRiskAlert.setUserId(1L);
        testRiskAlert.setSymbol("000001");
        testRiskAlert.setSymbolType("STOCK");
        testRiskAlert.setSymbolName("平安银行");
        testRiskAlert.setAlertDate(LocalDate.now());
        testRiskAlert.setTimePoint("14:30");
        testRiskAlert.setHasRisk(true);
        testRiskAlert.setChangePercent(new BigDecimal("5.50"));
        testRiskAlert.setCurrentPrice(new BigDecimal("12.50"));
        testRiskAlert.setYesterdayClose(new BigDecimal("11.85"));
        testRiskAlert.setIsRead(false);
        testRiskAlert.setTriggeredAt(LocalDateTime.now());
    }

    // ==================== 创建或更新风险提醒测试 ====================

    @Test
    @DisplayName("创建风险提醒 - 新提醒应成功创建")
    void createOrUpdateRiskAlert_newAlert_shouldSave() {
        // given: 不存在相同条件的风险提醒
        when(riskAlertRepository.findByUserIdAndSymbolAndAlertDateAndTimePoint(
                any(), any(), any(), any())).thenReturn(Optional.empty());
        when(riskAlertRepository.save(any(RiskAlert.class))).thenAnswer(invocation -> {
            RiskAlert alert = invocation.getArgument(0);
            alert.setId(1L);
            return alert;
        });

        // when
        RiskAlert result = riskAlertAppService.createOrUpdateRiskAlert(testRiskAlert);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getSymbol()).isEqualTo("000001");
        verify(riskAlertRepository).save(any(RiskAlert.class));
    }

    @Test
    @DisplayName("创建风险提醒 - 更新已有提醒应更新并重置未读状态")
    void createOrUpdateRiskAlert_existingAlert_shouldUpdate() {
        // given: 已存在风险提醒
        RiskAlert existingAlert = new RiskAlert();
        existingAlert.setId(10L);
        existingAlert.setUserId(1L);
        existingAlert.setSymbol("000001");
        existingAlert.setSymbolType("STOCK");
        existingAlert.setAlertDate(LocalDate.now());
        existingAlert.setTimePoint("14:30");
        existingAlert.setIsRead(true);

        when(riskAlertRepository.findByUserIdAndSymbolAndAlertDateAndTimePoint(
                eq(1L), eq("000001"), any(), eq("14:30")))
                .thenReturn(Optional.of(existingAlert));
        when(riskAlertRepository.update(any(RiskAlert.class))).thenAnswer(i -> i.getArgument(0));

        // when
        RiskAlert result = riskAlertAppService.createOrUpdateRiskAlert(testRiskAlert);

        // then: 更新后应重置为未读
        assertThat(result.getIsRead()).isFalse();
        verify(riskAlertRepository).update(any(RiskAlert.class));
    }

    // ==================== 分页查询测试 ====================

    @Test
    @DisplayName("分页查询风险提醒 - 应返回正确分页数据")
    void queryRiskAlerts_shouldReturnPaginatedResults() {
        // given
        RiskAlertQueryDTO query = new RiskAlertQueryDTO();
        query.setUserId(1L);
        query.setPage(1);
        query.setSize(10);

        List<RiskAlert> alerts = Arrays.asList(testRiskAlert);
        when(riskAlertRepository.findByUserIdWithPage(any(RiskAlertQuery.class))).thenReturn(alerts);
        when(riskAlertRepository.countByUserId(any(RiskAlertQuery.class))).thenReturn(1L);

        // when
        RiskAlertPageResponse<RiskAlert> result = riskAlertAppService.queryRiskAlerts(query);

        // then
        assertThat(result.getRecords()).hasSize(1);
        assertThat(result.getTotal()).isEqualTo(1);
        assertThat(result.getPage()).isEqualTo(1);
        assertThat(result.getSize()).isEqualTo(10);
        assertThat(result.getPages()).isEqualTo(1);
    }

    @Test
    @DisplayName("分页查询风险提醒 - 无数据时返回空列表")
    void queryRiskAlerts_noData_shouldReturnEmptyList() {
        // given
        RiskAlertQueryDTO query = new RiskAlertQueryDTO();
        query.setUserId(999L);
        query.setPage(1);
        query.setSize(10);

        when(riskAlertRepository.findByUserIdWithPage(any(RiskAlertQuery.class))).thenReturn(List.of());
        when(riskAlertRepository.countByUserId(any(RiskAlertQuery.class))).thenReturn(0L);

        // when
        RiskAlertPageResponse<RiskAlert> result = riskAlertAppService.queryRiskAlerts(query);

        // then
        assertThat(result.getRecords()).isEmpty();
        assertThat(result.getTotal()).isZero();
        assertThat(result.getPages()).isZero();
    }

    // ==================== 今日风险提醒测试 ====================

    @Test
    @DisplayName("获取今日风险提醒 - 应返回按日期分组的汇总")
    void getTodayRiskAlerts_shouldReturnGroupedByDate() {
        // given
        when(riskAlertRepository.findByUserIdAndDateRange(eq(1L), any(), any()))
                .thenReturn(Arrays.asList(testRiskAlert));

        // when
        List<RiskAlertSummaryDTO> result = riskAlertAppService.getTodayRiskAlerts(1L);

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getItems()).hasSize(1);
    }

    @Test
    @DisplayName("获取今日风险提醒 - 同一symbol多条记录只保留时间靠后的")
    void getTodayRiskAlerts_sameSymbol_shouldKeepLatestTimePoint() {
        // given: 同一symbol有11:30和14:30两条记录
        RiskAlert alert1130 = createRiskAlert("000001", "11:30", 3.0);
        RiskAlert alert1430 = createRiskAlert("000001", "14:30", 5.0);

        when(riskAlertRepository.findByUserIdAndDateRange(eq(1L), any(), any()))
                .thenReturn(Arrays.asList(alert1130, alert1430));

        // when
        List<RiskAlertSummaryDTO> result = riskAlertAppService.getTodayRiskAlerts(1L);

        // then: 应只保留14:30的记录
        assertThat(result.get(0).getItems()).hasSize(1);
        assertThat(result.get(0).getItems().get(0).getTimePoint()).isEqualTo("14:30");
    }

    @Test
    @DisplayName("获取今日风险提醒 - 应正确计算未读数量")
    void getTodayRiskAlerts_shouldCalculateUnreadCount() {
        // given
        RiskAlert readAlert = createRiskAlert("000001", "14:30", 5.0);
        readAlert.setIsRead(true);
        RiskAlert unreadAlert = createRiskAlert("000002", "14:30", -3.0);
        unreadAlert.setIsRead(false);

        when(riskAlertRepository.findByUserIdAndDateRange(eq(1L), any(), any()))
                .thenReturn(Arrays.asList(readAlert, unreadAlert));

        // when
        List<RiskAlertSummaryDTO> result = riskAlertAppService.getTodayRiskAlerts(1L);

        // then
        assertThat(result.get(0).getUnreadCount()).isEqualTo(1);
    }

    // ==================== 未读数量测试 ====================

    @Test
    @DisplayName("获取未读数量 - 应返回正确数量")
    void getUnreadCount_shouldReturnCorrectCount() {
        // given
        when(riskAlertRepository.countUnreadByUserId(1L)).thenReturn(5L);

        // when
        long result = riskAlertAppService.getUnreadCount(1L);

        // then
        assertThat(result).isEqualTo(5);
    }

    // ==================== 今日风险提醒数量测试 ====================

    @Test
    @DisplayName("获取今日风险提醒数量 - 应按symbol去重")
    void getTodayRiskAlertCount_shouldDeduplicateBySymbol() {
        // given: 同一symbol有多条记录
        RiskAlert alert1 = createRiskAlert("000001", "11:30", 3.0);
        RiskAlert alert2 = createRiskAlert("000001", "14:30", 5.0);
        RiskAlert alert3 = createRiskAlert("000002", "14:30", -2.0);

        when(riskAlertRepository.findByUserIdAndDateRange(eq(1L), any(), any()))
                .thenReturn(Arrays.asList(alert1, alert2, alert3));

        // when
        int result = riskAlertAppService.getTodayRiskAlertCount(1L);

        // then: 应返回2（去重后）
        assertThat(result).isEqualTo(2);
    }

    // ==================== 标记已读测试 ====================

    @Test
    @DisplayName("标记单条已读 - 应成功更新")
    void markAsRead_shouldUpdateSuccessfully() {
        // given
        when(riskAlertRepository.findById(1L)).thenReturn(Optional.of(testRiskAlert));
        when(riskAlertRepository.update(any(RiskAlert.class))).thenReturn(testRiskAlert);

        // when
        riskAlertAppService.markAsRead(1L);

        // then
        verify(riskAlertRepository).update(any(RiskAlert.class));
    }

    @Test
    @DisplayName("标记单条已读 - 不存在的记录应忽略")
    void markAsRead_notFound_shouldIgnore() {
        // given
        when(riskAlertRepository.findById(999L)).thenReturn(Optional.empty());

        // when
        riskAlertAppService.markAsRead(999L);

        // then
        verify(riskAlertRepository, never()).update(any());
    }

    @Test
    @DisplayName("标记全部已读 - 应调用仓储批量更新")
    void markAllAsRead_shouldCallRepository() {
        // when
        riskAlertAppService.markAllAsRead(1L);

        // then
        verify(riskAlertRepository).markAllAsRead(1L);
    }

    // ==================== 根据ID获取测试 ====================

    @Test
    @DisplayName("根据ID获取风险提醒 - 存在时返回")
    void getById_existing_shouldReturn() {
        // given
        when(riskAlertRepository.findById(1L)).thenReturn(Optional.of(testRiskAlert));

        // when
        Optional<RiskAlert> result = riskAlertAppService.getById(1L);

        // then
        assertThat(result).isPresent();
        assertThat(result.get().getSymbol()).isEqualTo("000001");
    }

    @Test
    @DisplayName("根据ID获取风险提醒 - 不存在时返回空")
    void getById_notFound_shouldReturnEmpty() {
        // given
        when(riskAlertRepository.findById(999L)).thenReturn(Optional.empty());

        // when
        Optional<RiskAlert> result = riskAlertAppService.getById(999L);

        // then
        assertThat(result).isEmpty();
    }

    // ==================== 删除测试 ====================

    @Test
    @DisplayName("删除风险提醒 - 应调用仓储删除")
    void deleteById_shouldCallRepository() {
        // when
        riskAlertAppService.deleteById(1L);

        // then
        verify(riskAlertRepository).deleteById(1L);
    }

    // ==================== 合并风险提醒测试 ====================

    @Test
    @DisplayName("获取合并风险提醒 - 应按symbol+date分组")
    void getMergedRiskAlerts_shouldGroupBySymbolAndDate() {
        // given
        RiskAlert alert1 = createRiskAlert("000001", "11:30", 3.0);
        RiskAlert alert2 = createRiskAlert("000001", "14:30", 5.0);
        alert1.setTriggeredAt(LocalDateTime.now().minusHours(2));
        alert2.setTriggeredAt(LocalDateTime.now());

        when(riskAlertRepository.findByUserId(eq(1L), any(), anyInt()))
                .thenReturn(Arrays.asList(alert1, alert2));

        // when
        List<RiskAlertMergeDTO> result = riskAlertAppService.getMergedRiskAlerts(1L, null, 10);

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).triggerCount()).isEqualTo(2);
    }

    @Test
    @DisplayName("获取合并风险提醒 - 应计算最大涨跌幅")
    void getMergedRiskAlerts_shouldCalculateMaxChangePercent() {
        // given
        RiskAlert alert1 = createRiskAlert("000001", "11:30", 3.0);
        RiskAlert alert2 = createRiskAlert("000001", "14:30", 5.0);

        when(riskAlertRepository.findByUserId(eq(1L), any(), anyInt()))
                .thenReturn(Arrays.asList(alert1, alert2));

        // when
        List<RiskAlertMergeDTO> result = riskAlertAppService.getMergedRiskAlerts(1L, null, 10);

        // then
        assertThat(result.get(0).maxChangePercent()).isEqualByComparingTo(new BigDecimal("5.0"));
    }

    // ==================== 处理价格提醒触发的风险测试 ====================

    /**
     * 创建价格提醒的辅助方法（支持股票和基金）
     */
    private PriceAlert createPriceAlert(String symbol, String symbolType, String alertType, Double targetPrice, Double targetChangePercent, Double basePrice) {
        PriceAlert alert = new PriceAlert();
        alert.setId(1L);
        alert.setUserId(1L);
        alert.setSymbol(symbol);
        alert.setSymbolType(symbolType);
        alert.setSymbolName("STOCK".equals(symbolType) ? "测试股票" : "测试基金");
        alert.setAlertType(alertType);
        alert.setTargetPrice(targetPrice);
        alert.setTargetChangePercent(targetChangePercent);
        alert.setBasePrice(basePrice);
        alert.setStatus("ACTIVE");
        return alert;
    }

    /**
     * 创建股票价格提醒的辅助方法
     */
    private PriceAlert createStockAlert(String symbol, String alertType, Double targetPrice, Double targetChangePercent, Double basePrice) {
        return createPriceAlert(symbol, "STOCK", alertType, targetPrice, targetChangePercent, basePrice);
    }

    /**
     * 创建基金价格提醒的辅助方法
     */
    private PriceAlert createFundAlert(String fundCode, String alertType, Double targetPrice, Double targetChangePercent, Double basePrice) {
        return createPriceAlert(fundCode, "FUND", alertType, targetPrice, targetChangePercent, basePrice);
    }

    @Test
    @DisplayName("处理价格提醒触发的风险 - 价格超过目标价应创建风险记录")
    void processAlertTriggeredRisk_priceAboveTarget_shouldCreateAlert() {
        // given: 设置PRICE_ABOVE提醒，目标价11.0，当前价格11.5（超过目标价）
        PriceAlert alert = createStockAlert("000001", "PRICE_ABOVE", 11.0, null, null);
        when(riskAlertRepository.findByUserIdAndSymbolAndAlertDateAndTimePoint(
                any(), any(), any(), any())).thenReturn(Optional.empty());
        when(riskAlertRepository.save(any(RiskAlert.class))).thenAnswer(i -> i.getArgument(0));

        // when: 当前价格11.5超过目标价11.0
        riskAlertAppService.processAlertTriggeredRisk(alert, new BigDecimal("11.50"), new BigDecimal("10.95"), "14:30");

        // then: 应保存风险记录
        verify(riskAlertRepository).save(any(RiskAlert.class));
    }

    @Test
    @DisplayName("处理价格提醒触发的风险 - 价格未超过目标价不创建记录")
    void processAlertTriggeredRisk_priceBelowTarget_shouldNotCreate() {
        // given: 设置PRICE_ABOVE提醒，目标价12.0，当前价格11.5（未超过目标价）
        PriceAlert alert = createStockAlert("000001", "PRICE_ABOVE", 12.0, null, null);

        // when: 当前价格11.5未超过目标价12.0
        riskAlertAppService.processAlertTriggeredRisk(alert, new BigDecimal("11.50"), new BigDecimal("10.95"), "14:30");

        // then: 不应保存
        verify(riskAlertRepository, never()).save(any());
    }

    @Test
    @DisplayName("处理价格提醒触发的风险 - 价格低于目标价应创建风险记录")
    void processAlertTriggeredRisk_priceBelowTarget_shouldCreateAlert() {
        // given: 设置PRICE_BELOW提醒，目标价11.0，当前价格10.5（低于目标价）
        PriceAlert alert = createStockAlert("000001", "PRICE_BELOW", 11.0, null, null);
        when(riskAlertRepository.findByUserIdAndSymbolAndAlertDateAndTimePoint(
                any(), any(), any(), any())).thenReturn(Optional.empty());
        when(riskAlertRepository.save(any(RiskAlert.class))).thenAnswer(i -> i.getArgument(0));

        // when: 当前价格10.5低于目标价11.0
        riskAlertAppService.processAlertTriggeredRisk(alert, new BigDecimal("10.50"), new BigDecimal("10.95"), "14:30");

        // then: 应保存风险记录
        verify(riskAlertRepository).save(any(RiskAlert.class));
    }

    @Test
    @DisplayName("处理价格提醒触发的风险 - 涨跌幅超过阈值应创建风险记录")
    void processAlertTriggeredRisk_percentageChangeExceed_shouldCreateAlert() {
        // given: 设置PERCENTAGE_CHANGE提醒，阈值5%，基准价10.95，当前价格11.50（涨幅5.02%超过阈值）
        PriceAlert alert = createStockAlert("000001", "PERCENTAGE_CHANGE", null, 5.0, 10.95);
        when(riskAlertRepository.findByUserIdAndSymbolAndAlertDateAndTimePoint(
                any(), any(), any(), any())).thenReturn(Optional.empty());
        when(riskAlertRepository.save(any(RiskAlert.class))).thenAnswer(i -> i.getArgument(0));

        // when: 涨跌幅约5.02%超过阈值5%
        riskAlertAppService.processAlertTriggeredRisk(alert, new BigDecimal("11.50"), new BigDecimal("10.95"), "14:30");

        // then: 应保存风险记录
        verify(riskAlertRepository).save(any(RiskAlert.class));
    }

    @Test
    @DisplayName("处理价格提醒触发的风险 - 涨跌幅未超阈值不创建记录")
    void processAlertTriggeredRisk_percentageChangeBelow_shouldNotCreate() {
        // given: 设置PERCENTAGE_CHANGE提醒，阈值5%，基准价10.95，当前价格11.40（涨幅4.1%未超过阈值）
        PriceAlert alert = createStockAlert("000001", "PERCENTAGE_CHANGE", null, 5.0, 10.95);

        // when: 涨跌幅约4.1%未超过阈值5%
        riskAlertAppService.processAlertTriggeredRisk(alert, new BigDecimal("11.40"), new BigDecimal("10.95"), "14:30");

        // then: 不应保存
        verify(riskAlertRepository, never()).save(any());
    }

    @Test
    @DisplayName("处理价格提醒触发的风险 - 下午时间点应为14:30")
    void processAlertTriggeredRisk_afternoon_shouldSet1430TimePoint() {
        // given
        PriceAlert alert = createStockAlert("000001", "PRICE_ABOVE", 11.0, null, null);
        when(riskAlertRepository.findByUserIdAndSymbolAndAlertDateAndTimePoint(
                any(), any(), any(), any())).thenReturn(Optional.empty());
        when(riskAlertRepository.save(any(RiskAlert.class))).thenAnswer(i -> i.getArgument(0));

        // when: 显式传入时间点 14:30
        riskAlertAppService.processAlertTriggeredRisk(alert, new BigDecimal("11.50"), new BigDecimal("10.95"), "14:30");

        // then: 验证保存的风险提醒时间点为14:30
        verify(riskAlertRepository).save(argThat(riskAlert ->
            "14:30".equals(riskAlert.getTimePoint())
        ));
    }

    @Test
    @DisplayName("处理价格提醒触发的风险 - 上午时间点应为11:30")
    void processAlertTriggeredRisk_morning_shouldSet1130TimePoint() {
        // given
        PriceAlert alert = createStockAlert("000001", "PRICE_ABOVE", 11.0, null, null);
        when(riskAlertRepository.findByUserIdAndSymbolAndAlertDateAndTimePoint(
                any(), any(), any(), any())).thenReturn(Optional.empty());
        when(riskAlertRepository.save(any(RiskAlert.class))).thenAnswer(i -> i.getArgument(0));

        // when: 显式传入时间点 11:30
        riskAlertAppService.processAlertTriggeredRisk(alert, new BigDecimal("11.50"), new BigDecimal("10.95"), "11:30");

        // then: 验证保存的风险提醒时间点为11:30
        verify(riskAlertRepository).save(argThat(riskAlert ->
            "11:30".equals(riskAlert.getTimePoint())
        ));
    }

    // ==================== 基金类型风险提醒测试 ====================

    /**
     * AC-2.1: 仅对设置了价格提醒的股票/基金产生风险数据
     * AC-1.2: 基金类型提醒支持PRICE_ABOVE/PRICE_BELOW/PERCENTAGE_CHANGE三种类型
     */

    @Test
    @DisplayName("【基金】处理价格提醒触发的风险 - 基金净值超过目标价应创建风险记录")
    void processAlertTriggeredRisk_FUND_priceAboveTarget_shouldCreateAlert() {
        // given: 设置基金PRICE_ABOVE提醒，目标净值1.5000，当前净值1.5500（超过目标价）
        // 基金使用FUND类型，净值精度为4位小数
        PriceAlert alert = createFundAlert("000011", "PRICE_ABOVE", 1.5000, null, null);
        when(riskAlertRepository.findByUserIdAndSymbolAndAlertDateAndTimePoint(
                any(), any(), any(), any())).thenReturn(Optional.empty());
        when(riskAlertRepository.save(any(RiskAlert.class))).thenAnswer(i -> i.getArgument(0));

        // when: 当前净值1.5500超过目标净值1.5000
        riskAlertAppService.processAlertTriggeredRisk(alert, new BigDecimal("1.5500"), new BigDecimal("1.4800"), "14:30");

        // then: 应保存风险记录，且symbolType为FUND
        verify(riskAlertRepository).save(argThat(riskAlert ->
            "FUND".equals(riskAlert.getSymbolType()) && "000011".equals(riskAlert.getSymbol())
        ));
    }

    @Test
    @DisplayName("【基金】处理价格提醒触发的风险 - 基金净值低于目标价应创建风险记录")
    void processAlertTriggeredRisk_FUND_priceBelowTarget_shouldCreateAlert() {
        // given: 设置基金PRICE_BELOW提醒，目标净值1.5000，当前净值1.4500（低于目标价）
        PriceAlert alert = createFundAlert("000011", "PRICE_BELOW", 1.5000, null, null);
        when(riskAlertRepository.findByUserIdAndSymbolAndAlertDateAndTimePoint(
                any(), any(), any(), any())).thenReturn(Optional.empty());
        when(riskAlertRepository.save(any(RiskAlert.class))).thenAnswer(i -> i.getArgument(0));

        // when: 当前净值1.4500低于目标净值1.5000
        riskAlertAppService.processAlertTriggeredRisk(alert, new BigDecimal("1.4500"), new BigDecimal("1.4800"), "14:30");

        // then: 应保存风险记录
        verify(riskAlertRepository).save(any(RiskAlert.class));
    }

    @Test
    @DisplayName("【基金】处理价格提醒触发的风险 - 基金净值未超过目标价不创建记录")
    void processAlertTriggeredRisk_FUND_priceAboveTarget_shouldNotCreate() {
        // given: 设置基金PRICE_ABOVE提醒，目标净值1.5000，当前净值1.4800（未超过目标价）
        PriceAlert alert = createFundAlert("000011", "PRICE_ABOVE", 1.5000, null, null);

        // when: 当前净值1.4800未超过目标净值1.5000
        riskAlertAppService.processAlertTriggeredRisk(alert, new BigDecimal("1.4800"), new BigDecimal("1.4500"), "14:30");

        // then: 不应保存
        verify(riskAlertRepository, never()).save(any());
    }

    @Test
    @DisplayName("【基金】处理价格提醒触发的风险 - 基金涨跌幅超过阈值应创建风险记录")
    void processAlertTriggeredRisk_FUND_percentageChangeExceed_shouldCreateAlert() {
        // given: 设置基金PERCENTAGE_CHANGE提醒，阈值3%，基准净值1.4500，当前净值1.4935（涨幅3.0%达到阈值）
        PriceAlert alert = createFundAlert("000011", "PERCENTAGE_CHANGE", null, 3.0, 1.4500);
        when(riskAlertRepository.findByUserIdAndSymbolAndAlertDateAndTimePoint(
                any(), any(), any(), any())).thenReturn(Optional.empty());
        when(riskAlertRepository.save(any(RiskAlert.class))).thenAnswer(i -> i.getArgument(0));

        // when: 涨跌幅约3.0%达到阈值3%
        riskAlertAppService.processAlertTriggeredRisk(alert, new BigDecimal("1.4935"), new BigDecimal("1.4500"), "14:30");

        // then: 应保存风险记录
        verify(riskAlertRepository).save(any(RiskAlert.class));
    }

    @Test
    @DisplayName("【基金】处理价格提醒触发的风险 - 基金涨跌幅未超阈值不创建记录")
    void processAlertTriggeredRisk_FUND_percentageChangeBelow_shouldNotCreate() {
        // given: 设置基金PERCENTAGE_CHANGE提醒，阈值3%，基准净值1.4500，当前净值1.4700（涨幅1.38%未达阈值）
        PriceAlert alert = createFundAlert("000011", "PERCENTAGE_CHANGE", null, 3.0, 1.4500);

        // when: 涨跌幅约1.38%未达到阈值3%
        riskAlertAppService.processAlertTriggeredRisk(alert, new BigDecimal("1.4700"), new BigDecimal("1.4500"), "14:30");

        // then: 不应保存
        verify(riskAlertRepository, never()).save(any());
    }

    @Test
    @DisplayName("【基金】处理价格提醒触发的风险 - 基金涨跌幅超过阈值（下跌）应创建风险记录")
    void processAlertTriggeredRisk_FUND_percentageChangeExceed_downward_shouldCreateAlert() {
        // given: 设置基金PERCENTAGE_CHANGE提醒，阈值3%，基准净值1.4500，当前净值1.4000（跌幅-3.45%超过阈值）
        PriceAlert alert = createFundAlert("000011", "PERCENTAGE_CHANGE", null, 3.0, 1.4500);
        when(riskAlertRepository.findByUserIdAndSymbolAndAlertDateAndTimePoint(
                any(), any(), any(), any())).thenReturn(Optional.empty());
        when(riskAlertRepository.save(any(RiskAlert.class))).thenAnswer(i -> i.getArgument(0));

        // when: 跌幅约-3.45%超过阈值3%（绝对值比较）
        riskAlertAppService.processAlertTriggeredRisk(alert, new BigDecimal("1.4000"), new BigDecimal("1.4500"), "14:30");

        // then: 应保存风险记录
        verify(riskAlertRepository).save(any(RiskAlert.class));
    }

    // ==================== 日期范围查询测试 ====================

    @Test
    @DisplayName("获取日期范围风险提醒 - 应返回按日期分组的结果")
    void getRiskAlertsByDateRange_shouldReturnGroupedByDate() {
        // given
        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);

        RiskAlert todayAlert = createRiskAlert("000001", "14:30", 5.0);
        todayAlert.setAlertDate(today);
        RiskAlert yesterdayAlert = createRiskAlert("000002", "14:30", -3.0);
        yesterdayAlert.setAlertDate(yesterday);

        when(riskAlertRepository.findByUserIdAndDateRange(1L, yesterday, today))
                .thenReturn(Arrays.asList(todayAlert, yesterdayAlert));

        // when
        List<RiskAlertSummaryDTO> result = riskAlertAppService.getRiskAlertsByDateRange(1L, yesterday, today);

        // then: 应按日期分组（2天）
        assertThat(result).hasSize(2);
    }

    // ==================== 基金类型风险提醒功能测试 ====================

    /**
     * 创建基金风险提醒的辅助方法
     */
    private RiskAlert createFundRiskAlert(String fundCode, String timePoint, double changePercent) {
        RiskAlert alert = new RiskAlert();
        alert.setId((long) (fundCode.hashCode() + timePoint.hashCode()));
        alert.setUserId(1L);
        alert.setSymbol(fundCode);
        alert.setSymbolType("FUND");
        alert.setSymbolName(fundCode + "基金");
        alert.setAlertDate(LocalDate.now());
        alert.setTimePoint(timePoint);
        alert.setHasRisk(true);
        alert.setChangePercent(BigDecimal.valueOf(changePercent));
        alert.setCurrentPrice(BigDecimal.valueOf(1.5500));
        alert.setYesterdayClose(BigDecimal.valueOf(1.4800));
        alert.setIsRead(false);
        alert.setTriggeredAt(LocalDateTime.now());
        return alert;
    }

    @Test
    @DisplayName("【基金】分页查询风险提醒 - 应返回正确分页数据")
    void queryRiskAlerts_FUND_shouldReturnPaginatedResults() {
        // given
        RiskAlert fundAlert = createFundRiskAlert("000011", "14:30", 3.5);
        RiskAlertQueryDTO query = new RiskAlertQueryDTO();
        query.setUserId(1L);
        query.setPage(1);
        query.setSize(10);

        when(riskAlertRepository.findByUserIdWithPage(any(RiskAlertQuery.class))).thenReturn(Arrays.asList(fundAlert));
        when(riskAlertRepository.countByUserId(any(RiskAlertQuery.class))).thenReturn(1L);

        // when
        RiskAlertPageResponse<RiskAlert> result = riskAlertAppService.queryRiskAlerts(query);

        // then
        assertThat(result.getRecords()).hasSize(1);
        assertThat(result.getRecords().get(0).getSymbolType()).isEqualTo("FUND");
        assertThat(result.getRecords().get(0).getSymbol()).isEqualTo("000011");
    }

    @Test
    @DisplayName("【基金】获取今日风险提醒 - 应正确计算基金未读数量")
    void getTodayRiskAlerts_FUND_shouldCalculateUnreadCount() {
        // given: 股票和基金混合场景
        RiskAlert stockAlert = createRiskAlert("000001", "14:30", 5.0);
        stockAlert.setIsRead(false);
        RiskAlert fundAlert = createFundRiskAlert("000011", "14:30", 3.5);
        fundAlert.setIsRead(false);
        RiskAlert fundAlertRead = createFundRiskAlert("000022", "14:30", -2.0);
        fundAlertRead.setIsRead(true);

        when(riskAlertRepository.findByUserIdAndDateRange(eq(1L), any(), any()))
                .thenReturn(Arrays.asList(stockAlert, fundAlert, fundAlertRead));

        // when
        List<RiskAlertSummaryDTO> result = riskAlertAppService.getTodayRiskAlerts(1L);

        // then: 验证基金类型数据正确返回
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getItems()).hasSize(3);
        // 验证未读数量
        assertThat(result.get(0).getUnreadCount()).isEqualTo(2);
    }

    @Test
    @DisplayName("【基金】获取未读数量 - 应包含基金数据")
    void getUnreadCount_FUND_shouldIncludeFundData() {
        // given: 返回的未读数量包含基金
        when(riskAlertRepository.countUnreadByUserId(1L)).thenReturn(5L);

        // when
        long result = riskAlertAppService.getUnreadCount(1L);

        // then
        assertThat(result).isEqualTo(5);
    }

    @Test
    @DisplayName("【基金】标记单条已读 - 基金风险提醒应成功更新")
    void markAsRead_FUND_shouldUpdateSuccessfully() {
        // given: 基金风险提醒
        RiskAlert fundAlert = createFundRiskAlert("000011", "14:30", 3.5);
        when(riskAlertRepository.findById(fundAlert.getId())).thenReturn(Optional.of(fundAlert));
        when(riskAlertRepository.update(any(RiskAlert.class))).thenReturn(fundAlert);

        // when
        riskAlertAppService.markAsRead(fundAlert.getId());

        // then
        verify(riskAlertRepository).update(any(RiskAlert.class));
    }

    @Test
    @DisplayName("【基金】标记全部已读 - 应包含基金数据")
    void markAllAsRead_FUND_shouldCallRepository() {
        // when
        riskAlertAppService.markAllAsRead(1L);

        // then: 验证统一调用仓储
        verify(riskAlertRepository).markAllAsRead(1L);
    }

    @Test
    @DisplayName("【基金】删除风险提醒 - 基金风险提醒应成功删除")
    void deleteById_FUND_shouldCallRepository() {
        // given
        RiskAlert fundAlert = createFundRiskAlert("000011", "14:30", 3.5);

        // when
        riskAlertAppService.deleteById(fundAlert.getId());

        // then
        verify(riskAlertRepository).deleteById(fundAlert.getId());
    }

    @Test
    @DisplayName("【基金】获取合并风险提醒 - 应按symbol+date分组且包含基金")
    void getMergedRiskAlerts_FUND_shouldGroupBySymbolAndDate() {
        // given: 同一基金有多条记录（11:30和14:30）
        RiskAlert fundAlert1130 = createFundRiskAlert("000011", "11:30", 2.0);
        RiskAlert fundAlert1430 = createFundRiskAlert("000011", "14:30", 3.5);
        fundAlert1130.setTriggeredAt(LocalDateTime.now().minusHours(2));
        fundAlert1430.setTriggeredAt(LocalDateTime.now());

        when(riskAlertRepository.findByUserId(eq(1L), any(), anyInt()))
                .thenReturn(Arrays.asList(fundAlert1130, fundAlert1430));

        // when
        List<RiskAlertMergeDTO> result = riskAlertAppService.getMergedRiskAlerts(1L, null, 10);

        // then: 应合并为1条
        assertThat(result).hasSize(1);
        assertThat(result.get(0).symbol()).isEqualTo("000011");
        assertThat(result.get(0).symbolType()).isEqualTo("FUND");
        assertThat(result.get(0).triggerCount()).isEqualTo(2);
    }

    @Test
    @DisplayName("【基金】获取合并风险提醒 - 应计算基金最大涨跌幅")
    void getMergedRiskAlerts_FUND_shouldCalculateMaxChangePercent() {
        // given: 基金净值涨跌记录，明确设置不同triggeredAt确保排序稳定
        RiskAlert fundAlert1 = createFundRiskAlert("000011", "11:30", 2.0);
        fundAlert1.setTriggeredAt(LocalDateTime.now().minusHours(2)); // 较早触发
        RiskAlert fundAlert2 = createFundRiskAlert("000011", "14:30", 3.5);
        fundAlert2.setTriggeredAt(LocalDateTime.now()); // 较晚触发，changePercent=3.5

        when(riskAlertRepository.findByUserId(eq(1L), any(), anyInt()))
                .thenReturn(Arrays.asList(fundAlert1, fundAlert2));

        // when
        List<RiskAlertMergeDTO> result = riskAlertAppService.getMergedRiskAlerts(1L, null, 10);

        // then: 最大涨跌幅应为3.5%，latestChangePercent也应为3.5（按triggeredAt最新）
        assertThat(result.get(0).maxChangePercent()).isEqualByComparingTo(new BigDecimal("3.5"));
        assertThat(result.get(0).latestChangePercent()).isEqualByComparingTo(new BigDecimal("3.5"));
    }

    @Test
    @DisplayName("【基金】日期范围查询 - 应返回基金数据")
    void getRiskAlertsByDateRange_FUND_shouldReturnFundData() {
        // given
        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);

        RiskAlert fundAlert = createFundRiskAlert("000011", "14:30", 3.5);
        fundAlert.setAlertDate(today);

        when(riskAlertRepository.findByUserIdAndDateRange(1L, yesterday, today))
                .thenReturn(Arrays.asList(fundAlert));

        // when
        List<RiskAlertSummaryDTO> result = riskAlertAppService.getRiskAlertsByDateRange(1L, yesterday, today);

        // then: 应返回包含基金数据的分组
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getItems().get(0).getSymbolType()).isEqualTo("FUND");
    }

    @Test
    @DisplayName("【基金】获取今日风险提醒数量 - 应按symbol去重包含基金")
    void getTodayRiskAlertCount_FUND_shouldDeduplicateBySymbol() {
        // given: 同一基金有多条记录
        RiskAlert fundAlert1 = createFundRiskAlert("000011", "11:30", 2.0);
        RiskAlert fundAlert2 = createFundRiskAlert("000011", "14:30", 3.5);
        RiskAlert stockAlert = createRiskAlert("000001", "14:30", 5.0);

        when(riskAlertRepository.findByUserIdAndDateRange(eq(1L), any(), any()))
                .thenReturn(Arrays.asList(fundAlert1, fundAlert2, stockAlert));

        // when
        int result = riskAlertAppService.getTodayRiskAlertCount(1L);

        // then: 应返回2（基金000011去重为1，股票000001为1）
        assertThat(result).isEqualTo(2);
    }

    // ==================== Helper方法 ====================

    private RiskAlert createRiskAlert(String symbol, String timePoint, double changePercent) {
        RiskAlert alert = new RiskAlert();
        alert.setId((long) (symbol.hashCode() + timePoint.hashCode()));
        alert.setUserId(1L);
        alert.setSymbol(symbol);
        alert.setSymbolType("STOCK");
        alert.setSymbolName(symbol + "名称");
        alert.setAlertDate(LocalDate.now());
        alert.setTimePoint(timePoint);
        alert.setHasRisk(true);
        alert.setChangePercent(BigDecimal.valueOf(changePercent));
        alert.setCurrentPrice(BigDecimal.valueOf(12.0));
        alert.setYesterdayClose(BigDecimal.valueOf(11.4));
        alert.setIsRead(false);
        alert.setTriggeredAt(LocalDateTime.now());
        return alert;
    }
}
