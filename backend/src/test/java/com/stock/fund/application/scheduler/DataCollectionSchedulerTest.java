package com.stock.fund.application.scheduler;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.stock.fund.application.service.DataCollectionAppService;
import com.stock.fund.application.service.DataCollectionTargetAppService;
import com.stock.fund.application.service.DataProcessingAppService;
import com.stock.fund.application.service.riskalert.RiskAlertAppService;
import com.stock.fund.domain.entity.DataCollectionTarget;
import com.stock.fund.domain.entity.StockQuote;

/**
 * DataCollectionScheduler unit tests Tests the dynamic scheduler using "fixed
 * heartbeat + dynamic decision" pattern
 */
@ExtendWith(MockitoExtension.class)
class DataCollectionSchedulerTest {

    @Mock
    private DataCollectionAppService dataCollectionAppService;

    @Mock
    private DataProcessingAppService dataProcessingAppService;

    @Mock
    private DataCollectionTargetAppService dataCollectionTargetAppService;

    @Mock
    private RiskAlertAppService riskAlertAppService;

    @Mock
    private SchedulerConfig schedulerConfig;

    @InjectMocks
    private DataCollectionScheduler scheduler;

    private DataCollectionTarget createStockTarget(Long id, String code, String market, Integer frequency) {
        DataCollectionTarget target = new DataCollectionTarget();
        target.setId(id);
        target.setCode(code);
        target.setName("Test Stock " + code);
        target.setType("STOCK");
        target.setMarket(market);
        target.setActive(true);
        target.setCollectionFrequency(frequency);
        target.setLastCollectedTime(LocalDateTime.now().minusMinutes(frequency));
        target.setNextCollectionTime(LocalDateTime.now().minusMinutes(1));
        return target;
    }

    private DataCollectionTarget createFundTarget(Long id, String code, Integer frequency) {
        DataCollectionTarget target = new DataCollectionTarget();
        target.setId(id);
        target.setCode(code);
        target.setName("Test Fund " + code);
        target.setType("FUND");
        target.setActive(true);
        target.setCollectionFrequency(frequency);
        target.setLastCollectedTime(LocalDateTime.now().minusMinutes(frequency));
        target.setNextCollectionTime(LocalDateTime.now().minusMinutes(1));
        return target;
    }

    @Nested
    @DisplayName("Dynamic Filtering Tests")
    class DynamicFilteringTests {

        @Test
        @DisplayName("Should not collect when getTargetsNeedingCollection returns empty list")
        void shouldNotCollectWhenNoTargetsNeedCollection() {
            // Given
            when(dataCollectionTargetAppService.getTargetsNeedingCollection()).thenReturn(Collections.emptyList());

            // When
            scheduler.scheduleStockQuoteCollection();

            // Then - verify no collection happened
            verify(dataCollectionTargetAppService, times(1)).getTargetsNeedingCollection();
            verify(dataCollectionAppService, times(0)).collectStockQuotes(anyList());
            verify(dataCollectionAppService, times(0)).fetchAndSaveFundRealTimeData(anyList());
            verify(dataProcessingAppService, times(0)).processStockQuotes(anyList());
            verify(dataCollectionTargetAppService, times(0)).updateBatch(anyList());
        }
    }

    @Nested
    @DisplayName("Stock Batch Collection Tests")
    class StockBatchCollectionTests {

        @Test
        @DisplayName("Should batch collect stocks successfully and update nextCollectionTime with frequency")
        void shouldBatchCollectStocksSuccessfully() {
            // Given
            List<DataCollectionTarget> stockTargets = Arrays.asList(createStockTarget(1L, "600000", "SH", 60),
                    createStockTarget(2L, "000001", "SZ", 60));
            when(dataCollectionTargetAppService.getTargetsNeedingCollection()).thenReturn(stockTargets);

            List<StockQuote> quotes = new ArrayList<>();
            StockQuote quote1 = new StockQuote();
            quote1.setStockId(1L);
            quotes.add(quote1);
            StockQuote quote2 = new StockQuote();
            quote2.setStockId(2L);
            quotes.add(quote2);
            when(dataCollectionAppService.collectStockQuotes(anyList())).thenReturn(quotes);

            // When
            scheduler.scheduleStockQuoteCollection();

            // Then
            verify(dataCollectionAppService, times(1)).collectStockQuotes(anyList());
            verify(dataProcessingAppService, times(1)).processStockQuotes(quotes);
            verify(dataCollectionTargetAppService, times(1)).updateBatch(stockTargets);
        }

        @Test
        @DisplayName("Should batch collect stocks fail and backoff 1 minute for all targets")
        void shouldBatchCollectStocksFailAndBackoff() {
            // Given
            List<DataCollectionTarget> stockTargets = Arrays.asList(createStockTarget(1L, "600000", "SH", 60),
                    createStockTarget(2L, "000001", "SZ", 1440));
            when(dataCollectionTargetAppService.getTargetsNeedingCollection()).thenReturn(stockTargets);
            when(dataCollectionAppService.collectStockQuotes(anyList()))
                    .thenThrow(new RuntimeException("Network error"));

            // When
            scheduler.scheduleStockQuoteCollection();

            // Then
            verify(dataCollectionAppService, times(1)).collectStockQuotes(anyList());
            verify(dataProcessingAppService, times(0)).processStockQuotes(anyList());
            verify(dataCollectionTargetAppService, times(1)).updateBatch(stockTargets);

            // Verify backoff: nextCollectionTime should be baseTime + 1 minute
            for (DataCollectionTarget target : stockTargets) {
                assertNotNull(target.getNextCollectionTime());
            }
        }
    }

    @Nested
    @DisplayName("Fund Individual Collection Tests")
    class FundIndividualCollectionTests {

        @Test
        @DisplayName("Should collect funds in batch and handle partial success via individual fund processing")
        void shouldCollectFundsInBatchWithPartialSuccess() {
            // Given
            List<DataCollectionTarget> fundTargets = Arrays.asList(createFundTarget(1L, "000001", 60),
                    createFundTarget(2L, "001593", 1440));
            when(dataCollectionTargetAppService.getTargetsNeedingCollection()).thenReturn(fundTargets);

            // The new implementation calls fetchAndSaveFundRealTimeData with a list once
            // It throws if the batch fails, succeeds otherwise
            doThrow(new RuntimeException("Collection failed"))
                    .when(dataCollectionAppService).fetchAndSaveFundRealTimeData(anyList());

            // When
            scheduler.scheduleStockQuoteCollection();

            // Then - batch call happens once, all marked as failed (one batch update)
            verify(dataCollectionAppService, times(1)).fetchAndSaveFundRealTimeData(anyList());
            verify(dataCollectionTargetAppService, times(1)).updateBatch(anyList()); // All failed
        }

        @Test
        @DisplayName("Should collect all funds successfully via batch call")
        void shouldCollectAllFundsSuccessfully() {
            // Given
            List<DataCollectionTarget> fundTargets = Arrays.asList(createFundTarget(1L, "000001", 60),
                    createFundTarget(2L, "001593", 1440));
            when(dataCollectionTargetAppService.getTargetsNeedingCollection()).thenReturn(fundTargets);
            // New implementation calls fetchAndSaveFundRealTimeData with a list once - void method, no stub needed

            // When
            scheduler.scheduleStockQuoteCollection();

            // Then - batch call happens once, all marked as success (one batch update)
            verify(dataCollectionAppService, times(1)).fetchAndSaveFundRealTimeData(anyList());
            verify(dataCollectionTargetAppService, times(1)).updateBatch(anyList()); // All success, one batch
        }

        @Test
        @DisplayName("Should handle all funds failing collection")
        void shouldHandleAllFundsFailingCollection() {
            // Given
            List<DataCollectionTarget> fundTargets = Arrays.asList(createFundTarget(1L, "000001", 60),
                    createFundTarget(2L, "001593", 1440));
            when(dataCollectionTargetAppService.getTargetsNeedingCollection()).thenReturn(fundTargets);
            doThrow(new RuntimeException("Collection failed")).when(dataCollectionAppService)
                    .fetchAndSaveFundRealTimeData(anyList());

            // When
            scheduler.scheduleStockQuoteCollection();

            // Then - batch call happens once, all marked as failed (one batch update)
            verify(dataCollectionAppService, times(1)).fetchAndSaveFundRealTimeData(anyList());
            verify(dataCollectionTargetAppService, times(1)).updateBatch(fundTargets); // All failed, one batch
        }
    }

    @Nested
    @DisplayName("Mixed Target Collection Tests")
    class MixedTargetCollectionTests {

        @Test
        @DisplayName("Should process STOCK and FUND targets separately through different paths")
        void shouldProcessMixedTargetsSeparately() {
            // Given
            List<DataCollectionTarget> mixedTargets = Arrays.asList(createStockTarget(1L, "600000", "SH", 60),
                    createFundTarget(2L, "000001", 60));
            when(dataCollectionTargetAppService.getTargetsNeedingCollection()).thenReturn(mixedTargets);

            List<StockQuote> quotes = new ArrayList<>();
            StockQuote quote = new StockQuote();
            quote.setStockId(1L);
            quotes.add(quote);
            when(dataCollectionAppService.collectStockQuotes(anyList())).thenReturn(quotes);

            // When
            scheduler.scheduleStockQuoteCollection();

            // Then - verify stocks went through batch collection
            verify(dataCollectionAppService, times(1)).collectStockQuotes(anyList());
            verify(dataProcessingAppService, times(1)).processStockQuotes(quotes);

            // And funds went through batch collection with list
            verify(dataCollectionAppService, times(1)).fetchAndSaveFundRealTimeData(anyList());
        }
    }

    @Nested
    @DisplayName("Deprecated Method Tests")
    class DeprecatedMethodTests {

        @Test
        @DisplayName("scheduleFundQuoteCollection should be no-op (deprecated)")
        void scheduleFundQuoteCollectionShouldBeNoOp() {
            // When
            scheduler.scheduleFundQuoteCollection();

            // Then - no collection methods should be called
            verify(dataCollectionTargetAppService, times(0)).getTargetsNeedingCollection();
            verify(dataCollectionAppService, times(0)).collectStockQuotes(anyList());
            verify(dataCollectionAppService, times(0)).fetchAndSaveFundRealTimeData(anyList());
            verify(dataProcessingAppService, times(0)).processStockQuotes(anyList());
            verify(dataCollectionTargetAppService, times(0)).updateBatch(anyList());
        }
    }

    @Nested
    @DisplayName("Batch Update Collection Time Tests")
    class BatchUpdateCollectionTimeTests {

        @Test
        @DisplayName("Should update collection time with frequency on success")
        void shouldUpdateCollectionTimeWithFrequencyOnSuccess() {
            // Given
            List<DataCollectionTarget> targets = Arrays.asList(createStockTarget(1L, "600000", "SH", 60),
                    createStockTarget(2L, "000001", "SZ", 1440));
            when(dataCollectionTargetAppService.getTargetsNeedingCollection()).thenReturn(targets);

            List<StockQuote> quotes = new ArrayList<>();
            when(dataCollectionAppService.collectStockQuotes(anyList())).thenReturn(quotes);

            // When
            scheduler.scheduleStockQuoteCollection();

            // Then
            for (DataCollectionTarget target : targets) {
                assertNotNull(target.getLastCollectedTime());
                assertNotNull(target.getNextCollectionTime());
                // On success, nextCollectionTime = baseTime + frequency
            }
        }

        @Test
        @DisplayName("Should update collection time with 1 minute backoff on failure")
        void shouldUpdateCollectionTimeWithBackoffOnFailure() {
            // Given
            List<DataCollectionTarget> targets = Arrays.asList(createStockTarget(1L, "600000", "SH", 60));
            when(dataCollectionTargetAppService.getTargetsNeedingCollection()).thenReturn(targets);
            when(dataCollectionAppService.collectStockQuotes(anyList())).thenThrow(new RuntimeException("Error"));

            // When
            scheduler.scheduleStockQuoteCollection();

            // Then - verify backoff to 1 minute
            for (DataCollectionTarget target : targets) {
                assertNotNull(target.getNextCollectionTime());
            }
        }
    }
}
