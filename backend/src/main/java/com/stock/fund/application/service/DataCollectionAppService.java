package com.stock.fund.application.service;

import com.stock.fund.domain.entity.Stock;
import com.stock.fund.domain.entity.Fund;
import com.stock.fund.domain.entity.StockQuote;
import com.stock.fund.domain.entity.FundQuote;
import com.stock.fund.domain.entity.DataCollectionTarget;

import java.util.List;

/**
 * 数据采集应用服务接口
 */
public interface DataCollectionAppService {
    /**
     * 采集股票基本信息列表
     */
    List<Stock> collectStockBasicList();

    /**
     * 采集单个股票的实时行情
     */
    StockQuote collectStockQuote(String symbol);

    /**
     * 采集多个股票的实时行情
     */
    List<StockQuote> collectStockQuotes(List<String> symbols);

    /**
     * 采集基金基本信息列表
     */
    List<Fund> collectFundBasicList();

    /**
     * 采集单个基金的实时净值
     */
    FundQuote collectFundQuote(String fundCode);

    /**
     * 采集多个基金的实时净值
     */
    List<FundQuote> collectFundQuotes(List<String> fundCodes);
    
    /**
     * 从外部API获取并保存基金实时数据
     */
    void fetchAndSaveFundRealTimeData();
    
    /**
     * 从外部API获取单个基金实时数据
     */
    FundQuote fetchFundRealTimeData(String fundCode);
    
    /**
     * 添加目标基金并自动获取实时数据
     * @param fundCode基金代码
     * @return DataCollectionTarget 采集目标对象，如果已存在则返回存在的对象
     */
    DataCollectionTarget addTargetFund(String fundCode);
}