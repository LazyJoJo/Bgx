package com.stock.fund.domain.repository;

import com.stock.fund.domain.entity.StockQuote;
import java.time.LocalDateTime;
import java.util.List;

/**
 *行情仓储接口
 */
public interface StockQuoteRepository {
    List<StockQuote> findByStockId(Long stockId);
    List<StockQuote> findByStockIdAndQuoteTimeBetween(Long stockId, LocalDateTime start, LocalDateTime end);
    StockQuote save(StockQuote stockQuote);
    List<StockQuote> saveAll(List<StockQuote> stockQuotes);

    /**
     * 获取所有股票的最新行情记录
     * 每个股票只返回最新的一条记录
     */
    List<StockQuote> findAllLatestQuotes();
}