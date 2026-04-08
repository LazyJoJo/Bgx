package com.stock.fund.application.scheduler;

import com.stock.fund.application.service.DataCollectionAppService;
import com.stock.fund.application.service.DataCollectionTargetAppService;
import com.stock.fund.application.service.DataProcessingAppService;
import com.stock.fund.application.service.riskalert.RiskAlertAppService;
import com.stock.fund.domain.entity.DataCollectionTarget;
import com.stock.fund.domain.entity.Fund;
import com.stock.fund.domain.entity.FundQuote;
import com.stock.fund.domain.entity.Stock;
import com.stock.fund.domain.entity.StockQuote;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 数据采集调度器
 *负责定时从数据源采集股票和基金数据
 */
@Component
public class DataCollectionScheduler {

    private static final Logger logger = LoggerFactory.getLogger(DataCollectionScheduler.class);

    @Autowired
    private DataCollectionAppService dataCollectionAppService;

    @Autowired
    private DataProcessingAppService dataProcessingAppService;

    @Autowired
    private DataCollectionTargetAppService dataCollectionTargetAppService;

    @Autowired
    private RiskAlertAppService riskAlertAppService;

    @Autowired
    private SchedulerConfig schedulerConfig;
    
    /**
     *定时采集股票基本信息
     *每天凌晨2点执行
     */
    @Scheduled(cron = "${data.collection.schedule.stock-basic-cron}")
    public void scheduleStockBasicCollection() {
        List<Stock> stockBasics = dataCollectionAppService.collectStockBasicList();
        dataProcessingAppService.processStockBasics(stockBasics);
    }
    
    /**
     *定时采集基金基本信息
     * 每天凌晨3点执行
     */
    @Scheduled(cron = "${data.collection.schedule.fund-basic-cron}")
    public void scheduleFundBasicCollection() {
        List<Fund> fundBasics = dataCollectionAppService.collectFundBasicList();
        dataProcessingAppService.processFundBasics(fundBasics);
    }
    
    /**
     *定时采集股票实时行情
     *每分钟执行一次（交易时间内）
     */
    @Scheduled(cron = "${data.collection.schedule.stock-quote-cron}")
    public void scheduleStockQuoteCollection() {
        // 从数据采集目标表获取需要监控的股票列表
        List<DataCollectionTarget> stockTargets =
            dataCollectionTargetAppService.getTargetsByType("STOCK");

        List<String> symbols = stockTargets.stream()
            .filter(target -> target.getActive())
            .map(target -> target.getFullCode())
            .toList();

        if (!symbols.isEmpty()) {
            List<StockQuote> stockQuotes = dataCollectionAppService.collectStockQuotes(symbols);
            dataProcessingAppService.processStockQuotes(stockQuotes);

            // 更新采集时间
            stockTargets.forEach(target -> {
                target.updateCollectionTime();
                dataCollectionTargetAppService.updateTargetByCode(target.getCode(), target);
            });
        }

        // 执行风险提醒检测（股票和基金都会检测）
        try {
            riskAlertAppService.checkAndCreateRiskAlerts();
        } catch (Exception e) {
            logger.error("风险提醒检测失败", e);
        }
    }
    
    /**
     * 定时采集基金实时净值
     *每15分钟执行一次（交易时间内）
     */
    @Scheduled(cron = "${data.collection.schedule.fund-quote-cron}")
    public void scheduleFundQuoteCollection() {
        // 使用数据采集应用服务获取并保存基金实时数据
        dataCollectionAppService.fetchAndSaveFundRealTimeData();

        // 执行风险提醒检测
        try {
            riskAlertAppService.checkAndCreateRiskAlerts();
        } catch (Exception e) {
            logger.error("风险提醒检测失败", e);
        }
    }
    
    /**
     * 在非交易时间采集全天数据
     * 每天16点执行（交易结束后）
     */
    @Scheduled(cron = "${data.collection.schedule.daily-collection-cron}")
    public void scheduleDailyCollection() {
        // 采集股票基本信息
        List<Stock> stockBasics = dataCollectionAppService.collectStockBasicList();
        dataProcessingAppService.processStockBasics(stockBasics);
        
        // 采集基金基本信息
        List<Fund> fundBasics = dataCollectionAppService.collectFundBasicList();
        dataProcessingAppService.processFundBasics(fundBasics);
    }
}