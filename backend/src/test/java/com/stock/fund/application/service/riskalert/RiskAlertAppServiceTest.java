package com.stock.fund.application.service.riskalert;

import com.stock.fund.application.service.riskalert.dto.RiskAlertMergeDTO;
import com.stock.fund.application.service.riskalert.impl.RiskAlertAppServiceImpl;
import com.stock.fund.application.service.riskalert.dto.RiskAlertPageResponse;
import com.stock.fund.application.service.riskalert.dto.RiskAlertQueryDTO;
import com.stock.fund.application.service.riskalert.dto.RiskAlertSummaryDTO;
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
        testRiskAlert.setChangePercent(5.5);
        testRiskAlert.setCurrentPrice(12.50);
        testRiskAlert.setYesterdayClose(11.85);
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
        assertThat(result.get(0).maxChangePercent()).isEqualTo(5.0);
    }

    // ==================== 处理价格提醒触发的风险测试 ====================

    @Test
    @DisplayName("处理价格提醒触发的风险 - 涨跌幅超过阈值应创建风险记录")
    void processAlertTriggeredRisk_exceedThreshold_shouldCreateAlert() {
        // given: 涨跌幅5%超过阈值
        when(riskAlertDomainService.shouldTriggerAlert(anyDouble())).thenReturn(true);
        when(riskAlertRepository.findByUserIdAndSymbolAndAlertDateAndTimePoint(
                any(), any(), any(), any())).thenReturn(Optional.empty());
        when(riskAlertRepository.save(any(RiskAlert.class))).thenAnswer(i -> i.getArgument(0));

        // when: 下午时间触发
        riskAlertAppService.processAlertTriggeredRisk(1L, "000001", "STOCK", 11.50, 10.95);

        // then
        verify(riskAlertRepository).save(any(RiskAlert.class));
    }

    @Test
    @DisplayName("处理价格提醒触发的风险 - 涨跌幅未超阈值不创建记录")
    void processAlertTriggeredRisk_belowThreshold_shouldNotCreate() {
        // given: 涨跌幅2%未超过阈值
        when(riskAlertDomainService.shouldTriggerAlert(anyDouble())).thenReturn(false);

        // when
        riskAlertAppService.processAlertTriggeredRisk(1L, "000001", "STOCK", 11.18, 10.95);

        // then: 不应保存
        verify(riskAlertRepository, never()).save(any());
    }

    @Test
    @DisplayName("处理价格提醒触发的风险 - 下午时间点应为14:30")
    void processAlertTriggeredRisk_afternoon_shouldSet1430TimePoint() {
        // given
        when(riskAlertDomainService.shouldTriggerAlert(anyDouble())).thenReturn(true);
        when(riskAlertRepository.findByUserIdAndSymbolAndAlertDateAndTimePoint(
                any(), any(), any(), any())).thenReturn(Optional.empty());
        when(riskAlertRepository.save(any(RiskAlert.class))).thenAnswer(i -> i.getArgument(0));

        // when
        riskAlertAppService.processAlertTriggeredRisk(1L, "000001", "STOCK", 11.50, 10.95);

        // then: 验证保存的风险提醒时间点为14:30
        verify(riskAlertRepository).save(argThat(alert ->
            "14:30".equals(alert.getTimePoint())
        ));
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
        alert.setChangePercent(changePercent);
        alert.setCurrentPrice(12.0);
        alert.setYesterdayClose(11.4);
        alert.setIsRead(false);
        alert.setTriggeredAt(LocalDateTime.now());
        return alert;
    }
}
