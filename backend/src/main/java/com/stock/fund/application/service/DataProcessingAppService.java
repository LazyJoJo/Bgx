package com.stock.fund.application.service;

import com.stock.fund.domain.entity.Stock;
import com.stock.fund.domain.entity.Fund;
import com.stock.fund.domain.entity.StockQuote;
import com.stock.fund.domain.entity.FundQuote;

import java.util.List;

/**
 * 数据处理应用服务接口
 */
public interface DataProcessingAppService {
    /**
     * 处理股票基本信息列表
     */
    void processStockBasics(List<Stock> stockBasics);

    /**
     * 处理股票行情数据
     */
    void processStockQuotes(List<StockQuote> stockQuotes);

    /**
     * 处理单个股票的实时行情
     */
    void processStockQuote(StockQuote stockQuote);

    /**
     * 处理基金基本信息列表
     */
    void processFundBasics(List<Fund> fundBasics);

    /**
     * 处理基金净值数据
     */
    void processFundQuotes(List<FundQuote> fundQuotes);

    /**
     * 处理单个基金的实时净值
     */
    void processFundQuote(FundQuote fundQuote);
}