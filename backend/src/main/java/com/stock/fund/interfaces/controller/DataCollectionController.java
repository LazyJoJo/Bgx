package com.stock.fund.interfaces.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.stock.fund.application.service.DataCollectionAppService;
import com.stock.fund.application.service.DataProcessingAppService;
import com.stock.fund.interfaces.dto.response.ApiResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/data")
@Tag(name = "数据采集", description = "股票、基金数据的实时采集接口")
public class DataCollectionController {

    private static final Logger logger = LoggerFactory.getLogger(DataCollectionController.class);

    @Autowired
    private DataCollectionAppService dataCollectionAppService;

    @Autowired
    private DataProcessingAppService dataProcessingAppService;

    @PostMapping("/stocks")
    @Operation(summary = "采集股票基本信息", description = "批量采集所有股票的基本信息并入库，包括股票代码、名称、交易所等")
    public ApiResponse<String> collectStocks() {
        logger.info("Starting stock basics data collection");
        var stockBasics = dataCollectionAppService.collectStockBasicList();
        dataProcessingAppService.processStockBasics(stockBasics);
        return ApiResponse.success("股票基本信息采集成功，共 " + stockBasics.size() + " 条数据");
    }

    @PostMapping("/quote/{symbol}")
    @Operation(summary = "采集单只股票行情", description = "采集指定股票的实时行情数据，包括收盘价、成交量、涨跌幅等")
    public ApiResponse<String> collectQuote(@PathVariable String symbol) {
        logger.info("Starting stock quote collection: {}", symbol);
        var stockQuote = dataCollectionAppService.collectStockQuote(symbol);
        dataProcessingAppService.processStockQuote(stockQuote);
        return ApiResponse.success("股票行情采集成功: " + symbol + " - " + stockQuote.getClose());
    }

    @PostMapping("/funds")
    @Operation(summary = "采集基金基本信息", description = "批量采集所有基金的基本信息并入库，包括基金代码、名称、类型等")
    public ApiResponse<String> collectFunds() {
        logger.info("Starting fund basics data collection");
        var fundBasics = dataCollectionAppService.collectFundBasicList();
        dataProcessingAppService.processFundBasics(fundBasics);
        return ApiResponse.success("基金基本信息采集成功，共 " + fundBasics.size() + " 条数据");
    }

    @PostMapping("/fund-quote/{fundCode}")
    @Operation(summary = "采集基金净值", description = "采集指定基金的最新净值数据，包括单位净值、累计净值、日增长率等")
    public ApiResponse<String> collectFundQuote(@PathVariable String fundCode) {
        logger.info("Starting fund NAV collection: {}", fundCode);
        var fundQuote = dataCollectionAppService.collectFundQuote(fundCode);
        dataProcessingAppService.processFundQuote(fundQuote);
        return ApiResponse.success("基金净值采集成功: " + fundCode + " - " + fundQuote.getNav());
    }
}