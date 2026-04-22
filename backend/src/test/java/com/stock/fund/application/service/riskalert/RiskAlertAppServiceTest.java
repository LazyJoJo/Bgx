package com.stock.fund.application.service.riskalert;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.stock.fund.application.query.RiskAlertQueryService;
import com.stock.fund.application.service.riskalert.dto.RiskAlertMergeDTO;
import com.stock.fund.application.service.riskalert.dto.RiskAlertPageResponse;
import com.stock.fund.application.service.riskalert.dto.RiskAlertQueryDTO;
import com.stock.fund.application.service.riskalert.dto.RiskAlertSummaryDTO;
import com.stock.fund.application.service.riskalert.impl.RiskAlertAppServiceImpl;
import com.stock.fund.domain.entity.Stock;
import com.stock.fund.domain.entity.StockQuote;
import com.stock.fund.domain.entity.riskalert.RiskAlert;
import com.stock.fund.domain.entity.subscription.UserSubscription;
import com.stock.fund.domain.repository.FundQuoteRepository;
import com.stock.fund.domain.repository.FundRepository;
import com.stock.fund.domain.repository.RiskAlertQuery;
import com.stock.fund.domain.repository.RiskAlertRepository;
import com.stock.fund.domain.repository.StockQuoteRepository;
import com.stock.fund.domain.repository.StockRepository;
import com.stock.fund.domain.repository.subscription.UserSubscriptionRepository;
import com.stock.fund.domain.service.riskalert.RiskAlertDomainService;

/**
 * 风险提醒应用服务测试 TDD RED阶段：编写测试用例，验证业务逻辑
 */
@ExtendWith(MockitoExtension.class)
class RiskAlertAppServiceTest {

        @Mock
        private RiskAlertRepository riskAlertRepository;

        @Mock
        private UserSubscriptionRepository userSubscriptionRepository;

        @Mock
        private RiskAlertDomainService riskAlertDomainService;

        @Mock
        private StockRepository stockRepository;

        @Mock
        private StockQuoteRepository stockQuoteRepository;

        @Mock
        private FundRepository fundRepository;

        @Mock
        private FundQuoteRepository fundQuoteRepository;

        @Mock
        private RiskAlertQueryService riskAlertQueryService;

        @Mock
        private com.stock.fund.domain.repository.RiskAlertDetailRepository riskAlertDetailRepository;

        @Mock
        private com.stock.fund.application.service.riskalert.push.RiskAlertPushService riskAlertPushService;

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
                when(riskAlertRepository.findByUserIdAndSymbolAndAlertDateAndTimePoint(any(), any(), any(), any()))
                                .thenReturn(Optional.empty());
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
                existingAlert.setId(1L);
                existingAlert.setUserId(1L);
                existingAlert.setSymbol("000001");
                existingAlert.setSymbolType("STOCK");
                existingAlert.setAlertDate(LocalDate.now());
                existingAlert.setTimePoint("14:30");
                existingAlert.setHasRisk(false);
                existingAlert.setIsRead(true);

                when(riskAlertRepository.findByUserIdAndSymbolAndAlertDateAndTimePoint(eq(1L), eq("000001"), any(),
                                eq("14:30"))).thenReturn(Optional.of(existingAlert));
                when(riskAlertRepository.update(any(RiskAlert.class))).thenAnswer(i -> i.getArgument(0));

                // when
                RiskAlert result = riskAlertAppService.createOrUpdateRiskAlert(testRiskAlert);

                // then
                assertThat(result.getHasRisk()).isTrue();
                assertThat(result.getIsRead()).isFalse(); // 更新后应重置为未读
                verify(riskAlertRepository).update(any(RiskAlert.class));
        }

        // ==================== 订阅触发的风险测试 ====================

        /**
         * 创建用户订阅的辅助方法
         */
        private UserSubscription createSubscription(Long userId, String symbol, String symbolType,
                        Double targetChangePercent) {
                UserSubscription subscription = new UserSubscription();
                subscription.setId(1L);
                subscription.setUserId(userId);
                subscription.setSymbol(symbol);
                subscription.setSymbolType(symbolType);
                subscription.setSymbolName(symbol + "名称");
                subscription.setTargetChangePercent(BigDecimal.valueOf(targetChangePercent));
                subscription.setIsActive(true);
                return subscription;
        }

        @Test
        @DisplayName("处理订阅触发的风险 - 涨跌幅超过阈值应创建风险记录")
        void processSubscriptionRisk_changeExceedThreshold_shouldCreateAlert() {
                // given: 设置订阅，阈值5%，当前价格11.50，涨跌额0.55（涨幅约5.02%超过阈值）
                UserSubscription subscription = createSubscription(1L, "000001", "STOCK", 5.0);

                // Mock stock and quote for getYesterdayClose
                Stock stock = new Stock();
                stock.setId(1L);
                stock.setSymbol("000001");
                when(stockRepository.findAll()).thenReturn(List.of(stock));

                // close=11.50（当前价），change=0.55（涨跌额），prevClose = 11.50 - 0.55 = 10.95
                StockQuote quote = new StockQuote(1L);
                quote.setClose(new BigDecimal("11.50"));
                quote.setChange(new BigDecimal("0.55"));
                when(stockQuoteRepository.findAllLatestQuotes()).thenReturn(List.of(quote));

                when(riskAlertRepository.findByUserIdAndSymbolAndAlertDateAndTimePoint(any(), any(), any(), any()))
                                .thenReturn(Optional.empty());
                when(riskAlertRepository.save(any(RiskAlert.class))).thenAnswer(i -> i.getArgument(0));

                // when: 涨跌幅约5.02%超过阈值5%
                riskAlertAppService.processSubscriptionRisk(subscription, new BigDecimal("11.50"), "14:30");

                // then: 应保存风险记录
                verify(riskAlertRepository).save(any(RiskAlert.class));
        }

        @Test
        @DisplayName("处理订阅触发的风险 - 涨跌幅未超阈值不创建记录")
        void processSubscriptionRisk_changeBelowThreshold_shouldNotCreate() {
                // given: 设置订阅，阈值5%，当前价格11.40，涨跌额0.45（涨幅约4.11%未超过阈值）
                UserSubscription subscription = createSubscription(1L, "000001", "STOCK", 5.0);

                // Mock stock and quote for getYesterdayClose
                Stock stock = new Stock();
                stock.setId(1L);
                stock.setSymbol("000001");
                when(stockRepository.findAll()).thenReturn(List.of(stock));

                // close=11.40（当前价），change=0.45（涨跌额），prevClose = 11.40 - 0.45 = 10.95
                StockQuote quote = new StockQuote(1L);
                quote.setClose(new BigDecimal("11.40"));
                quote.setChange(new BigDecimal("0.45"));
                when(stockQuoteRepository.findAllLatestQuotes()).thenReturn(List.of(quote));

                // when: 涨跌幅约4.11%未超过阈值5%
                riskAlertAppService.processSubscriptionRisk(subscription, new BigDecimal("11.40"), "14:30");

                // then: 不应保存
                verify(riskAlertRepository, never()).save(any());
        }

        @Test
        @DisplayName("处理订阅触发的风险 - 跌幅超过阈值应创建风险记录")
        void processSubscriptionRisk_negativeChangeExceedThreshold_shouldCreateAlert() {
                // given: 设置订阅，阈值3%，当前价格10.60，涨跌额-0.65（跌幅约5.86%超过阈值）
                UserSubscription subscription = createSubscription(1L, "000001", "STOCK", 3.0);

                // Mock stock and quote for getYesterdayClose
                Stock stock = new Stock();
                stock.setId(1L);
                stock.setSymbol("000001");
                when(stockRepository.findAll()).thenReturn(List.of(stock));

                // close=10.60（当前价），change=-0.65（下跌），prevClose = 10.60 - (-0.65) = 11.25
                StockQuote quote = new StockQuote(1L);
                quote.setClose(new BigDecimal("10.60"));
                quote.setChange(new BigDecimal("-0.65"));
                when(stockQuoteRepository.findAllLatestQuotes()).thenReturn(List.of(quote));

                when(riskAlertRepository.findByUserIdAndSymbolAndAlertDateAndTimePoint(any(), any(), any(), any()))
                                .thenReturn(Optional.empty());
                when(riskAlertRepository.save(any(RiskAlert.class))).thenAnswer(i -> i.getArgument(0));

                // when: 跌幅约5.86%超过阈值3%（绝对值比较）
                riskAlertAppService.processSubscriptionRisk(subscription, new BigDecimal("10.60"), "14:30");

                // then: 应保存风险记录
                verify(riskAlertRepository).save(any(RiskAlert.class));
        }

        @Test
        @DisplayName("批量检查风险提醒 - 应处理所有激活订阅")
        void checkAndCreateRiskAlerts_shouldProcessAllActiveSubscriptions() {
                // given: 两个激活的订阅
                UserSubscription sub1 = createSubscription(1L, "000001", "STOCK", 5.0);
                UserSubscription sub2 = createSubscription(1L, "000002", "STOCK", 3.0);
                when(userSubscriptionRepository.findActiveSubscriptions()).thenReturn(Arrays.asList(sub1, sub2));

                // Mock stock repository and quotes for getCurrentPrice
                Stock stock1 = new Stock();
                stock1.setId(1L);
                stock1.setSymbol("000001");
                Stock stock2 = new Stock();
                stock2.setId(2L);
                stock2.setSymbol("000002");
                when(stockRepository.findAll()).thenReturn(Arrays.asList(stock1, stock2));

                // Provide mock quotes with close prices so getCurrentPrice returns non-null
                StockQuote quote1 = new StockQuote(1L);
                quote1.setClose(new BigDecimal("11.50"));
                StockQuote quote2 = new StockQuote(2L);
                quote2.setClose(new BigDecimal("10.50"));
                when(stockQuoteRepository.findAllLatestQuotes()).thenReturn(Arrays.asList(quote1, quote2));

                // when
                riskAlertAppService.checkAndCreateRiskAlerts("14:30");

                // then: 应处理两个订阅
                verify(userSubscriptionRepository).findActiveSubscriptions();
        }

        // ==================== 分页查询风险提醒测试 ====================

        @Test
        @DisplayName("分页查询风险提醒 - 应返回正确分页数据")
        void queryRiskAlerts_shouldReturnPaginatedResults() {
                // given
                RiskAlertQueryDTO query = new RiskAlertQueryDTO();
                query.setUserId(1L);
                query.setPage(0);
                query.setSize(10);

                when(riskAlertRepository.findByUserIdWithPage(any(RiskAlertQuery.class)))
                                .thenReturn(Arrays.asList(testRiskAlert));
                when(riskAlertRepository.countByUserId(any(RiskAlertQuery.class))).thenReturn(1L);

                // when
                RiskAlertPageResponse<RiskAlert> result = riskAlertAppService.queryRiskAlerts(query);

                // then
                assertThat(result.getRecords()).hasSize(1);
                assertThat(result.getTotal()).isEqualTo(1);
                assertThat(result.getPage()).isEqualTo(0);
                assertThat(result.getSize()).isEqualTo(10);
        }

        // ==================== 获取风险提醒列表测试 ====================

        /**
         * 创建股票风险提醒的辅助方法
         */
        private RiskAlert createRiskAlert(String symbol, String timePoint, double changePercent) {
                RiskAlert alert = new RiskAlert();
                alert.setId((long) (symbol.hashCode() + timePoint.hashCode()));
                alert.setUserId(1L);
                alert.setSymbol(symbol);
                alert.setSymbolType("STOCK");
                alert.setSymbolName(symbol + "股票");
                alert.setAlertDate(LocalDate.now());
                alert.setTimePoint(timePoint);
                alert.setHasRisk(true);
                alert.setChangePercent(BigDecimal.valueOf(changePercent));
                alert.setCurrentPrice(BigDecimal.valueOf(12.50));
                alert.setYesterdayClose(BigDecimal.valueOf(11.85));
                alert.setIsRead(false);
                alert.setTriggeredAt(LocalDateTime.now());
                return alert;
        }

        @Test
        @DisplayName("获取今日风险提醒 - 应按日期分组")
        void getTodayRiskAlerts_shouldGroupByDate() {
                // given: 现在委托给 RiskAlertQueryService，需要 mock query service
                RiskAlert alert1 = createRiskAlert("000001", "11:30", 5.0);
                RiskAlert alert2 = createRiskAlert("000001", "14:30", 5.5); // 同一股票，不同时间点
                RiskAlert alert3 = createRiskAlert("000002", "14:30", 3.0);

                List<RiskAlertSummaryDTO.RiskAlertItemDTO> items = Arrays.asList(RiskAlertSummaryDTO.RiskAlertItemDTO
                                .builder().id(1L).symbol("000001").symbolType("STOCK").timePoint("14:30").hasRisk(true)
                                .changePercent(BigDecimal.valueOf(5.5)).isRead(false).build(),
                                RiskAlertSummaryDTO.RiskAlertItemDTO.builder().id(2L).symbol("000002")
                                                .symbolType("STOCK").timePoint("14:30").hasRisk(true)
                                                .changePercent(BigDecimal.valueOf(3.0)).isRead(false).build());
                RiskAlertSummaryDTO summary = RiskAlertSummaryDTO.builder().alertDate(LocalDate.now()).totalCount(2)
                                .unreadCount(2).items(items).build();

                when(riskAlertQueryService.getRiskAlertsByDateRangeSummary(eq(1L), any(), any()))
                                .thenReturn(Arrays.asList(summary));

                // when
                List<RiskAlertSummaryDTO> result = riskAlertAppService.getTodayRiskAlerts(1L);

                // then: 按symbol分组，每组取靠后的时间点
                assertThat(result).hasSize(1); // 只有今天的
                assertThat(result.get(0).getItems()).hasSize(2); // 2个不同的股票
        }

        @Test
        @DisplayName("获取今日风险提醒 - 无风险数据时返回空列表")
        void getTodayRiskAlerts_noData_shouldReturnEmptyList() {
                // given: 现在委托给 RiskAlertQueryService
                when(riskAlertQueryService.getRiskAlertsByDateRangeSummary(eq(1L), any(), any()))
                                .thenReturn(Arrays.asList());

                // when
                List<RiskAlertSummaryDTO> result = riskAlertAppService.getTodayRiskAlerts(1L);

                // then
                assertThat(result).isEmpty();
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
                assertThat(result).isEqualTo(5L);
        }

        @Test
        @DisplayName("标记单条已读 - 应更新为已读状态")
        void markAsRead_shouldUpdateToRead() {
                // given
                RiskAlert existingAlert = new RiskAlert();
                existingAlert.setId(1L);
                existingAlert.setIsRead(false);

                when(riskAlertRepository.findById(1L)).thenReturn(Optional.of(existingAlert));
                when(riskAlertRepository.update(any(RiskAlert.class))).thenAnswer(i -> i.getArgument(0));

                // when
                riskAlertAppService.markAsRead(1L);

                // then
                assertThat(existingAlert.getIsRead()).isTrue();
                verify(riskAlertRepository).update(existingAlert);
        }

        @Test
        @DisplayName("标记全部已读 - 应更新所有未读记录")
        void markAllAsRead_shouldUpdateAllUnreadRecords() {
                // given
                doNothing().when(riskAlertRepository).markAllAsRead(1L);

                // when
                riskAlertAppService.markAllAsRead(1L);

                // then
                verify(riskAlertRepository).markAllAsRead(1L);
        }

        // ==================== 按日期范围查询测试 ====================

        @Test
        @DisplayName("按日期范围查询 - 应返回指定范围内的数据")
        void getRiskAlertsByDateRange_shouldReturnDataInRange() {
                // given: 现在委托给 RiskAlertQueryService
                LocalDate today = LocalDate.now();
                LocalDate yesterday = today.minusDays(1);

                List<RiskAlertSummaryDTO.RiskAlertItemDTO> todayItems = Arrays
                                .asList(RiskAlertSummaryDTO.RiskAlertItemDTO.builder().id(1L).symbol("000001")
                                                .symbolType("STOCK").timePoint("14:30").hasRisk(true)
                                                .changePercent(BigDecimal.valueOf(5.0)).isRead(false).build());
                List<RiskAlertSummaryDTO.RiskAlertItemDTO> yesterdayItems = Arrays
                                .asList(RiskAlertSummaryDTO.RiskAlertItemDTO.builder().id(2L).symbol("000001")
                                                .symbolType("STOCK").timePoint("11:30").hasRisk(true)
                                                .changePercent(BigDecimal.valueOf(3.0)).isRead(false).build());

                RiskAlertSummaryDTO todaySummary = RiskAlertSummaryDTO.builder().alertDate(today).totalCount(1)
                                .unreadCount(1).items(todayItems).build();
                RiskAlertSummaryDTO yesterdaySummary = RiskAlertSummaryDTO.builder().alertDate(yesterday).totalCount(1)
                                .unreadCount(1).items(yesterdayItems).build();

                when(riskAlertQueryService.getRiskAlertsByDateRangeSummary(eq(1L), eq(yesterday), eq(today)))
                                .thenReturn(Arrays.asList(todaySummary, yesterdaySummary));

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

                when(riskAlertRepository.findByUserIdWithPage(any(RiskAlertQuery.class)))
                                .thenReturn(Arrays.asList(fundAlert));
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
                // given: 现在委托给 RiskAlertQueryService
                List<RiskAlertSummaryDTO.RiskAlertItemDTO> items = Arrays.asList(RiskAlertSummaryDTO.RiskAlertItemDTO
                                .builder().id(1L).symbol("000001").symbolType("STOCK").timePoint("14:30").hasRisk(true)
                                .changePercent(BigDecimal.valueOf(5.0)).isRead(false).build(),
                                RiskAlertSummaryDTO.RiskAlertItemDTO.builder().id(2L).symbol("000011")
                                                .symbolType("FUND").timePoint("14:30").hasRisk(true)
                                                .changePercent(BigDecimal.valueOf(3.5)).isRead(false).build(),
                                RiskAlertSummaryDTO.RiskAlertItemDTO.builder().id(3L).symbol("000022")
                                                .symbolType("FUND").timePoint("14:30").hasRisk(true)
                                                .changePercent(BigDecimal.valueOf(-2.0)).isRead(true).build());
                RiskAlertSummaryDTO summary = RiskAlertSummaryDTO.builder().alertDate(LocalDate.now()).totalCount(3)
                                .unreadCount(2).items(items).build();

                when(riskAlertQueryService.getRiskAlertsByDateRangeSummary(eq(1L), any(), any()))
                                .thenReturn(Arrays.asList(summary));

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

                // then: 包含基金的未读数量
                assertThat(result).isEqualTo(5L);
        }

        // ==================== 合并风险提醒测试 ====================

        @Test
        @DisplayName("获取合并后的风险提醒列表 - 应按时间和symbol分组")
        void getMergedRiskAlerts_shouldGroupBySymbolAndDate() {
                // given: 现在委托给 RiskAlertQueryService
                List<RiskAlertMergeDTO.RiskAlertDetailDTO> details1 = Arrays
                                .asList(new RiskAlertMergeDTO.RiskAlertDetailDTO(1L, BigDecimal.valueOf(5.0),
                                                BigDecimal.valueOf(11.5), LocalDateTime.now(), null));
                RiskAlertMergeDTO merged1 = new RiskAlertMergeDTO(1L, "000001", "STOCK", "平安银行", LocalDate.now(),
                                "ACTIVE", 2, BigDecimal.valueOf(5.5), BigDecimal.valueOf(4.0), BigDecimal.valueOf(5.5),
                                BigDecimal.valueOf(11.5), BigDecimal.valueOf(10.95), false, LocalDateTime.now(),
                                details1);

                List<RiskAlertMergeDTO.RiskAlertDetailDTO> details2 = Arrays
                                .asList(new RiskAlertMergeDTO.RiskAlertDetailDTO(2L, BigDecimal.valueOf(3.0),
                                                BigDecimal.valueOf(10.3), LocalDateTime.now(), null));
                RiskAlertMergeDTO merged2 = new RiskAlertMergeDTO(2L, "000002", "STOCK", "平安银行2", LocalDate.now(),
                                "ACTIVE", 1, BigDecimal.valueOf(3.0), BigDecimal.valueOf(2.0), BigDecimal.valueOf(3.0),
                                BigDecimal.valueOf(10.3), BigDecimal.valueOf(10.0), false, LocalDateTime.now(),
                                details2);

                when(riskAlertQueryService.getMergedRiskAlerts(eq(1L), any(), any(Integer.class)))
                                .thenReturn(Arrays.asList(merged1, merged2));

                // when
                List<RiskAlertMergeDTO> result = riskAlertAppService.getMergedRiskAlerts(1L, null, 10);

                // then: 合并后应为2条（000001和000002各一条，取最新的时间和最大涨跌幅）
                assertThat(result).hasSize(2);
        }

        @Test
        @DisplayName("获取合并后的风险提醒列表 - 应计算最大涨跌幅")
        void getMergedRiskAlerts_shouldCalculateMaxChangePercent() {
                // given: 现在委托给 RiskAlertQueryService
                List<RiskAlertMergeDTO.RiskAlertDetailDTO> details = Arrays.asList(
                                new RiskAlertMergeDTO.RiskAlertDetailDTO(1L, BigDecimal.valueOf(5.0),
                                                BigDecimal.valueOf(11.5), LocalDateTime.now(), null),
                                new RiskAlertMergeDTO.RiskAlertDetailDTO(2L, BigDecimal.valueOf(5.5),
                                                BigDecimal.valueOf(11.5), LocalDateTime.now(), null));
                RiskAlertMergeDTO merged = new RiskAlertMergeDTO(1L, "000001", "STOCK", "平安银行", LocalDate.now(),
                                "ACTIVE", 2, BigDecimal.valueOf(5.5), BigDecimal.valueOf(4.5), BigDecimal.valueOf(5.5),
                                BigDecimal.valueOf(11.5), BigDecimal.valueOf(10.95), false, LocalDateTime.now(),
                                details);

                when(riskAlertQueryService.getMergedRiskAlerts(eq(1L), any(), any(Integer.class)))
                                .thenReturn(Arrays.asList(merged));

                // when
                List<RiskAlertMergeDTO> result = riskAlertAppService.getMergedRiskAlerts(1L, null, 10);

                // then: 最大涨跌幅应为5.5%
                assertThat(result).hasSize(1);
                assertThat(result.get(0).maxChangePercent()).isEqualByComparingTo(BigDecimal.valueOf(5.5));
        }

        // ==================== 删除风险提醒测试 ====================

        @Test
        @DisplayName("删除风险提醒 - 应调用仓储删除方法")
        void deleteById_shouldCallRepositoryDelete() {
                // given
                doNothing().when(riskAlertRepository).deleteById(1L);

                // when
                riskAlertAppService.deleteById(1L);

                // then
                verify(riskAlertRepository).deleteById(1L);
        }

        // ==================== 批量创建风险提醒测试 ====================

        @Test
        @DisplayName("批量创建风险提醒 - 应处理所有标的")
        void batchCreateRiskAlerts_shouldProcessAllSymbols() {
                // given
                com.stock.fund.application.service.riskalert.dto.BatchCreateRiskAlertRequest request = new com.stock.fund.application.service.riskalert.dto.BatchCreateRiskAlertRequest();
                request.setUserId(1L);
                request.setSymbolType("STOCK");
                request.setSymbols(Arrays.asList("000001", "000002"));

                when(riskAlertRepository.findByUserIdAndSymbolAndAlertDateAndTimePoint(any(), any(), any(), any()))
                                .thenReturn(Optional.empty());
                when(riskAlertRepository.save(any(RiskAlert.class))).thenAnswer(i -> i.getArgument(0));

                // Mock stock repository and quotes for getCurrentPrice
                Stock stock1 = new Stock();
                stock1.setId(1L);
                stock1.setSymbol("000001");
                Stock stock2 = new Stock();
                stock2.setId(2L);
                stock2.setSymbol("000002");
                when(stockRepository.findAll()).thenReturn(Arrays.asList(stock1, stock2));

                // Provide mock quotes with close prices so getCurrentPrice returns non-null
                StockQuote quote1 = new StockQuote(1L);
                quote1.setClose(new BigDecimal("11.50"));
                StockQuote quote2 = new StockQuote(2L);
                quote2.setClose(new BigDecimal("10.50"));
                when(stockQuoteRepository.findAllLatestQuotes()).thenReturn(Arrays.asList(quote1, quote2));

                // when
                var result = riskAlertAppService.batchCreateRiskAlerts(request);

                // then
                assertThat(result.getSuccessCount()).isEqualTo(2);
                assertThat(result.getFailCount()).isEqualTo(0);
        }
}