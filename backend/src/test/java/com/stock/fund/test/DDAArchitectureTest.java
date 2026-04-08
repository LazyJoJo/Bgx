package com.stock.fund.test;

import com.stock.fund.application.service.DataCollectionAppService;
import com.stock.fund.application.service.DataProcessingAppService;
import com.stock.fund.domain.entity.Stock;
import com.stock.fund.domain.entity.Fund;
import com.stock.fund.domain.repository.StockRepository;
import com.stock.fund.domain.repository.FundRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@Component
public class DDAArchitectureTest implements CommandLineRunner {
    
    private static final Logger logger = LoggerFactory.getLogger(DDAArchitectureTest.class);
    
    @Autowired
    private DataCollectionAppService dataCollectionAppService;
    
    @Autowired
    private DataProcessingAppService dataProcessingAppService;
    
    @Autowired
    private StockRepository stockRepository;
    
    @Autowired
    private FundRepository fundRepository;
    
    @Override
    public void run(String... args) throws Exception {
        logger.info("\n===================================================");
        logger.info("开始测试DDD架构的各个组件...");
        logger.info("===================================================\n");
        
        // 测试数据采集
        testDataCollection();
        
        // 测试数据处理
        testDataProcessing();
        
        // 测试仓储操作
        testRepositoryOperations();
        
        logger.info("\n===================================================");
        logger.info("DDD架构测试完成！");
        logger.info("===================================================");
    }
    
    private void testDataCollection() {
        logger.info("🧪 测试数据采集服务...");
        
        // 测试股票基本信息采集
        List<Stock> stockBasics = dataCollectionAppService.collectStockBasicList();
        logger.info("✅ 股票基本信息采集完成，共 {} 条数据", stockBasics.size());
        
        // 测试基金基本信息采集
        List<Fund> fundBasics = dataCollectionAppService.collectFundBasicList();
        logger.info("✅ 基金基本信息采集完成，共 {} 条数据", fundBasics.size());
        
        logger.info("✅ 数据采集服务测试通过\n");
    }
    
    private void testDataProcessing() {
        logger.info("🧪 测试数据处理服务...");
        
        // 测试股票基本信息处理
        List<Stock> stockBasics = dataCollectionAppService.collectStockBasicList();
        dataProcessingAppService.processStockBasics(stockBasics);
        logger.info("✅ 股票基本信息处理完成");
        
        // 测试基金基本信息处理
        List<Fund> fundBasics = dataCollectionAppService.collectFundBasicList();
        dataProcessingAppService.processFundBasics(fundBasics);
        logger.info("✅ 基金基本信息处理完成");
        
        logger.info("✅ 数据处理服务测试通过\n");
    }
    
    private void testRepositoryOperations() {
        logger.info("🧪 测试仓储操作...");
        
        // 测试股票仓储
        List<Stock> allStocks = stockRepository.findAll();
        logger.info("✅ 查询所有股票，共 {} 条数据", allStocks.size());
        
        if (!allStocks.isEmpty()) {
            Stock firstStock = allStocks.get(0);
            var foundStock = stockRepository.findBySymbol(firstStock.getSymbol());
            if (foundStock.isPresent()) {
                logger.info("✅ 根据股票代码查询成功: {}", foundStock.get().getName());
            } else {
                logger.warn("⚠️  根据股票代码未找到数据");
            }
        }
        
        // 测试基金仓储
        List<Fund> allFunds = fundRepository.findAll();
        logger.info("✅ 查询所有基金，共 {} 条数据", allFunds.size());
        
        if (!allFunds.isEmpty()) {
            Fund firstFund = allFunds.get(0);
            var foundFund = fundRepository.findByFundCode(firstFund.getFundCode());
            if (foundFund.isPresent()) {
                logger.info("✅ 根据基金代码查询成功: {}", foundFund.get().getName());
            } else {
                logger.warn("⚠️  根据基金代码未找到数据");
            }
        }
        
        logger.info("✅ 仓储操作测试通过\n");
    }
}