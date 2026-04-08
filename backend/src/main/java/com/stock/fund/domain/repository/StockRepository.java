package com.stock.fund.domain.repository;

import com.stock.fund.domain.entity.Stock;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 股票仓储接口
 */
public interface StockRepository {
    Optional<Stock> findBySymbol(String symbol);
    List<Stock> findAll();
    Stock save(Stock stock);
    void deleteById(Long id);
    List<Stock> findByIndustry(String industry);
    List<Stock> findByMarket(String market);

    /**
     * 批量根据股票代码查询
     * @param symbols 股票代码列表
     * @return 股票Map，key为股票代码
     */
    Map<String, Stock> findBySymbols(List<String> symbols);
}