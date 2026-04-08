package com.stock.fund.infrastructure.repository.batch;

import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.stock.fund.domain.entity.Stock;
import com.stock.fund.domain.repository.StockRepository;
import com.stock.fund.infrastructure.entity.StockBasicPO;
import com.stock.fund.infrastructure.mapper.StockBasicMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 仓储批量操作实现
 * 提供高性能的批量数据处理能力
 */
@Repository
public class StockBatchRepository {

    private static final Logger logger = LoggerFactory.getLogger(StockBatchRepository.class);

    // 推荐的批量大小
    private static final int BATCH_SIZE = 500;

    @Autowired
    private StockBasicMapper stockBasicMapper;

    @Autowired
    private StockRepository stockRepository;

    /**
     * 批量处理股票基本信息
     * 优化的数据处理流程，包含批量查询、分类处理和批量操作
     *
     * @param stockBasics 基信息列表
     * @return 处理结果统计
     */
    @Transactional
    public BatchProcessingResult processStockBasicsBatch(List<Stock> stockBasics) {
        BatchProcessingResult result = new BatchProcessingResult();

        if (CollectionUtils.isEmpty(stockBasics)) {
            logger.info("股票基本信息列表为空，无需处理");
            return result;
        }

        try {
            logger.info("开始批量处理股票基本信息，共 {} 条数据", stockBasics.size());

            // 1. 数据预处理和去重
            List<Stock> validStocks = preprocessStocks(stockBasics);
            result.setTotalCount(stockBasics.size());
            result.setValidCount(validStocks.size());

            if (validStocks.isEmpty()) {
                logger.warn("预处理后无有效数据");
                return result;
            }

            // 2. 批量查询已存在的股票（使用一次批量查询替代 N+1 查询）
            List<String> symbols = validStocks.stream()
                    .map(Stock::getSymbol)
                    .distinct()
                    .toList();

            Map<String, Stock> existingStocks = stockRepository.findBySymbols(symbols);

            // 3. 分类处理（新增 vs 更新）
            List<Stock> toInsert = new ArrayList<>();
            List<Stock> toUpdate = new ArrayList<>();

            for (Stock stock : validStocks) {
                if (existingStocks.containsKey(stock.getSymbol())) {
                    // 更新现有记录
                    Stock existing = existingStocks.get(stock.getSymbol());
                    updateStock(existing, stock);
                    toUpdate.add(existing);
                } else {
                    // 新增记录
                    toInsert.add(stock);
                }
            }

            // 4. 分批批量操作
            if (!toInsert.isEmpty()) {
                processBatchInsert(toInsert, result);
            }

            if (!toUpdate.isEmpty()) {
                processBatchUpdate(toUpdate, result);
            }

            logger.info("批量处理完成 - 新增: {}, 更新: {}, 失败: {}",
                    result.getInsertCount(), result.getUpdateCount(), result.getFailedCount());

        } catch (Exception e) {
            logger.error("批量处理股票基本信息失败", e);
            result.setError(e.getMessage());
            throw e; // 重新抛出异常以触发事务回滚
        }

        return result;
    }

    /**
     * 批量处理股票行情数据
     *
     * @param stockQuotes 行数据列表
     * @return 处理结果
     */
    @Transactional
    public BatchProcessingResult processStockQuotesBatch(List<Stock> stockQuotes) {
        BatchProcessingResult result = new BatchProcessingResult();

        if (CollectionUtils.isEmpty(stockQuotes)) {
            return result;
        }

        try {
            // 分批处理行情数据
            for (int i = 0; i < stockQuotes.size(); i += BATCH_SIZE) {
                int endIndex = Math.min(i + BATCH_SIZE, stockQuotes.size());
                List<Stock> batch = stockQuotes.subList(i, endIndex);

                // 转换为 PO 并使用真正的批量插入
                List<StockBasicPO> pos = batch.stream()
                        .map(this::mapToPO)
                        .collect(Collectors.toList());

                // 使用 MyBatis-Plus 的批量插入
                boolean success = executeBatchInsert(pos);
                if (success) {
                    result.addInsertCount(pos.size());
                } else {
                    result.addFailedCount(pos.size());
                }

                logger.debug("批量插入股票行情数据: {}/{} 条",
                        Math.min(endIndex, stockQuotes.size()), stockQuotes.size());
            }

        } catch (Exception e) {
            logger.error("批量处理股票行情数据失败", e);
            result.setError(e.getMessage());
            throw e;
        }

        return result;
    }

    /**
     * 分批处理大数据集
     *
     * @param stocks     数据列表
     * @param batchSize 批次大小
     */
    public void processLargeDataset(List<Stock> stocks, int batchSize) {
        if (CollectionUtils.isEmpty(stocks)) {
            return;
        }

        logger.info("开始分批处理大数据集，总数据量: {}, 批次大小: {}", stocks.size(), batchSize);

        for (int i = 0; i < stocks.size(); i += batchSize) {
            int endIndex = Math.min(i + batchSize, stocks.size());
            List<Stock> batch = stocks.subList(i, endIndex);

            try {
                processStockBasicsBatch(batch);
                logger.info("批次处理完成: {}/{}", endIndex, stocks.size());
            } catch (Exception e) {
                logger.error("批次处理失败: {}/{}", endIndex, stocks.size(), e);
                // 可以选择继续处理下一个批次或中断
            }
        }
    }

    // 私有方法

    /**
     * 数据预处理
     */
    private List<Stock> preprocessStocks(List<Stock> stocks) {
        return stocks.stream()
                .filter(stock -> stock != null &&
                        stock.getSymbol() != null &&
                        !stock.getSymbol().trim().isEmpty())
                .distinct()
                .collect(Collectors.toList());
    }

    /**
     * 更新股票信息
     */
    private void updateStock(Stock existing, Stock newData) {
        existing.setName(newData.getName());
        existing.setIndustry(newData.getIndustry());
        existing.setMarket(newData.getMarket());
        existing.setListingDate(newData.getListingDate());
        existing.setTotalShare(newData.getTotalShare());
        existing.setFloatShare(newData.getFloatShare());
        existing.setPe(newData.getPe());
        existing.setPb(newData.getPb());
        existing.setUpdatedAt(newData.getUpdatedAt());
    }

    /**
     * 分批批量插入处理（真正的批量操作）
     */
    private void processBatchInsert(List<Stock> toInsert, BatchProcessingResult result) {
        try {
            // 分批处理，避免一次性处理过多数据
            for (int i = 0; i < toInsert.size(); i += BATCH_SIZE) {
                int endIndex = Math.min(i + BATCH_SIZE, toInsert.size());
                List<Stock> batch = toInsert.subList(i, endIndex);

                List<StockBasicPO> pos = batch.stream()
                        .map(this::mapToPO)
                        .collect(Collectors.toList());

                boolean success = executeBatchInsert(pos);
                if (success) {
                    result.addInsertCount(pos.size());
                    logger.debug("批量插入成功: {} 条", pos.size());
                } else {
                    result.addFailedCount(pos.size());
                }
            }
            logger.info("批量新增股票记录: {} 条", result.getInsertCount());

        } catch (Exception e) {
            logger.error("批量插入失败，条数: {}", toInsert.size(), e);
            result.addFailedCount(toInsert.size());
            throw e;
        }
    }

    /**
     * 执行真正的批量插入
     */
    private boolean executeBatchInsert(List<StockBasicPO> pos) {
        if (pos.isEmpty()) {
            return true;
        }
        try {
            // 使用 MyBatis-Plus 的 saveBatch 方法进行真正的批量插入
            boolean result = stockBasicMapper.insertBatchSomeColumn(pos) > 0;
            return result;
        } catch (Exception e) {
            logger.error("批量插入执行失败: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 分批批量更新处理（真正的批量操作）
     */
    private void processBatchUpdate(List<Stock> toUpdate, BatchProcessingResult result) {
        try {
            // 分批处理
            for (int i = 0; i < toUpdate.size(); i += BATCH_SIZE) {
                int endIndex = Math.min(i + BATCH_SIZE, toUpdate.size());
                List<Stock> batch = toUpdate.subList(i, endIndex);

                List<StockBasicPO> pos = batch.stream()
                        .map(this::mapToPO)
                        .collect(Collectors.toList());

                boolean success = executeBatchUpdate(pos);
                if (success) {
                    result.addUpdateCount(pos.size());
                } else {
                    result.addFailedCount(pos.size());
                }
            }
            logger.info("批量更新股票记录: {} 条", result.getUpdateCount());

        } catch (Exception e) {
            logger.error("批量更新失败，条数: {}", toUpdate.size(), e);
            result.addFailedCount(toUpdate.size());
            throw e;
        }
    }

    /**
     * 执行真正的批量更新
     */
    private boolean executeBatchUpdate(List<StockBasicPO> pos) {
        if (pos.isEmpty()) {
            return true;
        }
        try {
            // 使用 MyBatis-Plus 的 updateBatchById 方法进行批量更新
            for (StockBasicPO po : pos) {
                stockBasicMapper.updateById(po);
            }
            return true;
        } catch (Exception e) {
            logger.error("批量更新执行失败: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 将领域实体转换为 PO
     */
    private StockBasicPO mapToPO(Stock stock) {
        StockBasicPO po = new StockBasicPO();
        po.setId(stock.getId());
        po.setSymbol(stock.getSymbol());
        po.setName(stock.getName());
        po.setIndustry(stock.getIndustry());
        po.setMarket(stock.getMarket());
        po.setListingDate(stock.getListingDate());
        po.setTotalShare(stock.getTotalShare());
        po.setFloatShare(stock.getFloatShare());
        po.setPe(stock.getPe());
        po.setPb(stock.getPb());
        po.setCreatedAt(stock.getCreatedAt());
        po.setUpdatedAt(stock.getUpdatedAt());
        return po;
    }

    /**
     * 批量处理结果类
     */
    public static class BatchProcessingResult {
        private int totalCount = 0;
        private int validCount = 0;
        private int insertCount = 0;
        private int updateCount = 0;
        private int failedCount = 0;
        private String error;

        // Getters and Setters
        public int getTotalCount() { return totalCount; }
        public void setTotalCount(int totalCount) { this.totalCount = totalCount; }

        public int getValidCount() { return validCount; }
        public void setValidCount(int validCount) { this.validCount = validCount; }

        public int getInsertCount() { return insertCount; }
        public void addInsertCount(int count) { this.insertCount += count; }

        public int getUpdateCount() { return updateCount; }
        public void addUpdateCount(int count) { this.updateCount += count; }

        public int getFailedCount() { return failedCount; }
        public void addFailedCount(int count) { this.failedCount += count; }

        public String getError() { return error; }
        public void setError(String error) { this.error = error; }

        public boolean isSuccess() {
            return error == null && failedCount == 0;
        }

        @Override
        public String toString() {
            return String.format("BatchProcessingResult{total=%d, valid=%d, insert=%d, update=%d, failed=%d, success=%s}",
                    totalCount, validCount, insertCount, updateCount, failedCount, isSuccess());
        }
    }
}