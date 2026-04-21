package com.stock.fund.application.service.impl;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.stock.fund.application.service.DataProcessingAppService;
import com.stock.fund.domain.entity.Fund;
import com.stock.fund.domain.entity.FundQuote;
import com.stock.fund.domain.entity.Stock;
import com.stock.fund.domain.entity.StockQuote;
import com.stock.fund.domain.repository.FundQuoteRepository;
import com.stock.fund.domain.repository.FundRepository;
import com.stock.fund.domain.repository.StockQuoteRepository;
import com.stock.fund.domain.repository.StockRepository;

@Service
public class DataProcessingAppServiceImpl implements DataProcessingAppService {

    private static final Logger logger = LoggerFactory.getLogger(DataProcessingAppServiceImpl.class);

    @Autowired
    private StockRepository stockRepository;

    @Autowired
    private FundRepository fundRepository;

    @Autowired
    private StockQuoteRepository stockQuoteRepository;

    @Autowired
    private FundQuoteRepository fundQuoteRepository;

    @Override
    public void processStockBasics(List<Stock> stockBasics) {
        logger.info("Starting to process stock basics, total {} records", stockBasics.size());

        for (Stock stock : stockBasics) {
            // 检查股票是否已存在
            var existingStockOpt = stockRepository.findBySymbol(stock.getSymbol());
            if (existingStockOpt.isPresent()) {
                // 更新现有股票信息
                Stock existingStock = existingStockOpt.get();
                existingStock.setName(stock.getName());
                existingStock.setIndustry(stock.getIndustry());
                existingStock.setMarket(stock.getMarket());
                existingStock.setListingDate(stock.getListingDate());
                existingStock.setTotalShare(stock.getTotalShare());
                existingStock.setFloatShare(stock.getFloatShare());
                existingStock.setPe(stock.getPe());
                existingStock.setPb(stock.getPb());
                existingStock.setUpdatedAt(stock.getUpdatedAt());
                stockRepository.save(existingStock);
                logger.debug("Updating stock info: {}", stock.getSymbol());
            } else {
                // 创建新股票信息
                stockRepository.save(stock);
                logger.debug("Inserting stock info: {}", stock.getSymbol());
            }
        }

        logger.info("Stock basics processing completed");
    }

    @Override
    public void processStockQuotes(List<StockQuote> stockQuotes) {
        logger.info("Starting to process stock quotes, total {} records", stockQuotes.size());

        for (StockQuote stockQuote : stockQuotes) {
            // 保存行情数据
            stockQuoteRepository.save(stockQuote);
            logger.debug("Saving stock quote: {} - {}", stockQuote.getStockId(), stockQuote.getClose());
        }

        logger.info("Stock quotes processing completed");
    }

    @Override
    public void processStockQuote(StockQuote stockQuote) {
        logger.info("Processing single stock quote: {}", stockQuote.getStockId());
        // 保存行情数据
        stockQuoteRepository.save(stockQuote);
        logger.debug("Saving stock quote: {} - {}", stockQuote.getStockId(), stockQuote.getClose());
    }

    @Override
    public void processFundBasics(List<Fund> fundBasics) {
        logger.info("Starting to process fund basics, total {} records", fundBasics.size());

        for (Fund fund : fundBasics) {
            // 检查基金是否已存在
            var existingFundOpt = fundRepository.findByFundCode(fund.getFundCode());
            if (existingFundOpt.isPresent()) {
                // 更新现有基金信息
                Fund existingFund = existingFundOpt.get();
                existingFund.setName(fund.getName());
                existingFund.setType(fund.getType());
                existingFund.setManager(fund.getManager());
                existingFund.setEstablishmentDate(fund.getEstablishmentDate());
                existingFund.setFundSize(fund.getFundSize());
                existingFund.setNav(fund.getNav());
                existingFund.setDayGrowth(fund.getDayGrowth());
                existingFund.setWeekGrowth(fund.getWeekGrowth());
                existingFund.setMonthGrowth(fund.getMonthGrowth());
                existingFund.setYearGrowth(fund.getYearGrowth());
                existingFund.setUpdatedAt(fund.getUpdatedAt());
                fundRepository.save(existingFund);
                logger.debug("Updating fund info: {}", fund.getFundCode());
            } else {
                // 创建新基金信息
                fundRepository.save(fund);
                logger.debug("Inserting fund info: {}", fund.getFundCode());
            }
        }

        logger.info("Fund basics processing completed");
    }

    @Override
    public void processFundQuotes(List<FundQuote> fundQuotes) {
        logger.info("Starting to process fund NAV data, total {} records", fundQuotes.size());

        for (FundQuote fundQuote : fundQuotes) {
            // 保存净值数据
            fundQuoteRepository.save(fundQuote);
            logger.debug("Saving fund NAV: {} - {}", fundQuote.getFundCode(), fundQuote.getNav());
        }

        logger.info("Fund NAV data processing completed");
    }

    @Override
    public void processFundQuote(FundQuote fundQuote) {
        logger.info("Processing single fund NAV: {}", fundQuote.getFundCode());
        // 保存净值数据
        fundQuoteRepository.save(fundQuote);
        logger.debug("Saving fund NAV: {} - {}", fundQuote.getFundCode(), fundQuote.getNav());
    }
}