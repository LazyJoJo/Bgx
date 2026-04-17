package com.stock.fund.application.service.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.stock.fund.application.service.DataCollectionAppService;
import com.stock.fund.domain.entity.DataCollectionTarget;
import com.stock.fund.domain.entity.Fund;
import com.stock.fund.domain.entity.FundQuote;
import com.stock.fund.domain.entity.Stock;
import com.stock.fund.domain.entity.StockQuote;
import com.stock.fund.domain.repository.DataCollectionTargetRepository;
import com.stock.fund.domain.repository.FundQuoteRepository;
import com.stock.fund.domain.repository.FundRepository;
import com.stock.fund.domain.repository.StockQuoteRepository;
import com.stock.fund.domain.repository.StockRepository;
import com.stock.fund.infrastructure.client.SinaFundApiClient;
import com.stock.fund.infrastructure.converter.FundQuoteConverter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class DataCollectionAppServiceImpl implements DataCollectionAppService {

    private final StockRepository stockRepository;
    private final FundRepository fundRepository;
    private final StockQuoteRepository stockQuoteRepository;
    private final FundQuoteRepository fundQuoteRepository;
    private final DataCollectionTargetRepository dataCollectionTargetRepository;
    private final SinaFundApiClient sinaFundApiClient;
    private final FundQuoteConverter fundQuoteConverter;

    @Override
    public List<Stock> collectStockBasicList() {
        // 模拟从数据源获取股票基本信息
        // 实际实现中这里会调用外部API如Tushare等
        return List.of(createSampleStock("600000", "浦发银行", "银行", "沪市"), createSampleStock("600519", "贵州茅台", "白酒", "沪市"),
                createSampleStock("000001", "平安银行", "银行", "深市"));
    }

    @Override
    public StockQuote collectStockQuote(String symbol) {
        // 模拟从数据源获取股票实时行情
        // 实际实现中这里会调用外部API如Tushare等
        Stock stock = stockRepository.findBySymbol(symbol).orElseThrow(() -> new RuntimeException("股票不存在: " + symbol));

        StockQuote quote = new StockQuote();
        quote.setStockId(stock.getId());
        quote.setQuoteTime(LocalDateTime.now());
        quote.setOpen(new BigDecimal("100.00"));
        quote.setHigh(new BigDecimal("105.00"));
        quote.setLow(new BigDecimal("98.00"));
        quote.setClose(new BigDecimal("102.50"));
        quote.setVolume(1000000L);
        quote.setAmount(new BigDecimal("102500000.00"));
        quote.setChange(new BigDecimal("2.50"));
        quote.setChangePercent(new BigDecimal("2.50"));

        return quote;
    }

    @Override
    public List<StockQuote> collectStockQuotes(List<String> symbols) {
        return symbols.stream().map(this::collectStockQuote).collect(Collectors.toList());
    }

    @Override
    public List<Fund> collectFundBasicList() {
        // 模拟从数据源获取基金基本信息
        // 实际实现中这里会调用外部API如Tushare等
        return List.of(createSampleFund("000001", "华夏成长混合", "混合型", "王明"),
                createSampleFund("000011", "易方达价值精选", "混合型", "李华"),
                createSampleFund("110011", "易方达中小盘混合", "混合型", "张伟"));
    }

    @Override
    public FundQuote collectFundQuote(String fundCode) {
        // 从外部数据源获取基金实时净值
        // 实际实现中这里会调用外部API
        Fund fund = fundRepository.findByFundCode(fundCode)
                .orElseThrow(() -> new RuntimeException("基金不存在: " + fundCode));

        FundQuote quote = new FundQuote();
        quote.setFundCode(fund.getFundCode());
        quote.setFundName(fund.getName());
        LocalDateTime now = LocalDateTime.now();
        quote.setQuoteDate(now.toLocalDate());
        quote.setQuoteTimeOnly(now.toLocalTime());
        quote.setNav(fund.getNav());
        // 计算昨日净值 = nav * 0.99，保留4位小数
        BigDecimal prevNetValue = fund.getNav().multiply(new BigDecimal("0.99")).setScale(4, RoundingMode.HALF_UP);
        quote.setPrevNetValue(prevNetValue);
        quote.setChangeAmount(fund.getDayGrowth().setScale(4, RoundingMode.HALF_UP));
        quote.setChangePercent(fund.getDayGrowth().setScale(2, RoundingMode.HALF_UP));

        return quote;
    }

    @Override
    public List<FundQuote> collectFundQuotes(List<String> fundCodes) {
        return fundCodes.stream().map(this::collectFundQuote).collect(Collectors.toList());
    }

    @Override
    public void fetchAndSaveFundRealTimeData() {
        // 从数据库获取所有活跃的基金采集目标
        List<DataCollectionTarget> fundTargets = dataCollectionTargetRepository.findByTypeAndActive("FUND", true);

        if (fundTargets.isEmpty()) {
            log.info("没有找到活跃的基金采集目标");
            return;
        }

        log.info("找到 {} 个活跃基金采集目标", fundTargets.size());

        for (DataCollectionTarget target : fundTargets) {
            try {
                log.info("正在处理基金目标: {} - {}", target.getCode(), target.getName());

                // 获取基金实时数据
                FundQuote newFundQuote = fetchFundRealTimeData(target.getCode());

                if (newFundQuote != null) {
                    // 检查是否已存在同一天的相同基金代码的记录
                    FundQuote existingQuote = fundQuoteRepository.findByFundCodeAndQuoteDate(newFundQuote.getFundCode(),
                            newFundQuote.getQuoteDate());

                    if (existingQuote != null) {
                        // 如果存在同一天的记录，更新现有记录
                        existingQuote.setFundName(newFundQuote.getFundName());
                        existingQuote.setQuoteDate(newFundQuote.getQuoteDate());
                        existingQuote.setQuoteTimeOnly(newFundQuote.getQuoteTimeOnly());
                        existingQuote.setNav(newFundQuote.getNav());
                        existingQuote.setPrevNetValue(newFundQuote.getPrevNetValue());
                        existingQuote.setChangeAmount(newFundQuote.getChangeAmount());
                        existingQuote.setChangePercent(newFundQuote.getChangePercent());

                        fundQuoteRepository.save(existingQuote);
                        log.info("更新基金数据: {} 日期: {} 时间: {} 净值: {} 涨跌幅: {}% 涨跌额: {}", target.getCode(),
                                existingQuote.getQuoteDate(), existingQuote.getQuoteTimeOnly(), existingQuote.getNav(),
                                existingQuote.getChangePercent(), existingQuote.getChangeAmount());
                    } else {
                        // 如果不存在同一天的记录，保存新记录
                        fundQuoteRepository.save(newFundQuote);
                        log.info("新增基金数据: {} 日期: {} 时间: {} 净值: {} 涨跌幅: {}% 涨跌额: {}", target.getCode(),
                                newFundQuote.getQuoteDate(), newFundQuote.getQuoteTimeOnly(), newFundQuote.getNav(),
                                newFundQuote.getChangePercent(), newFundQuote.getChangeAmount());
                    }
                } else {
                    log.warn("未能获取到基金 {} 的实时数据", target.getCode());
                }

            } catch (Exception e) {
                log.error("处理基金目标 {} 时发生异常: {}", target.getCode(), e.getMessage(), e);
            }
        }

        log.info("基金实时数据更新完成");
    }

    @Override
    public FundQuote fetchFundRealTimeData(String fundCode) {
        try {
            java.util.Map<String, String> fundData = sinaFundApiClient.fetchFundData(fundCode);

            if (fundData != null && !fundData.isEmpty()) {
                // 创建一个临时的DataCollectionTarget对象用于转换
                DataCollectionTarget target = new DataCollectionTarget();
                target.setCode(fundCode);
                // 尝试从数据中获取基金名称，如果无法获取则使用基金代码作为名称
                target.setName(fundData.getOrDefault("name", fundCode));

                // 将数据转换为FundQuote实体（使用专门的转换器）
                return fundQuoteConverter.toFundQuote(target, fundData);
            }
        } catch (Exception e) {
            log.error("获取基金 {} 数据时发生异常: {}", fundCode, e.getMessage(), e);
        }

        return null;
    }

    private Stock createSampleStock(String symbol, String name, String industry, String market) {
        Stock stock = new Stock();
        stock.setSymbol(symbol);
        stock.setName(name);
        stock.setIndustry(industry);
        stock.setMarket(market);
        stock.setListingDate(LocalDate.of(2000, 1, 1));
        stock.setTotalShare(new BigDecimal("1000000.00"));
        stock.setFloatShare(new BigDecimal("1000000.00"));
        stock.setPe(new BigDecimal("15.00"));
        stock.setPb(new BigDecimal("1.50"));
        return stock;
    }

    private Fund createSampleFund(String fundCode, String name, String type, String manager) {
        Fund fund = new Fund();
        fund.setFundCode(fundCode);
        fund.setName(name);
        fund.setType(type);
        fund.setManager(manager);
        fund.setEstablishmentDate(LocalDate.of(2005, 1, 1));
        fund.setFundSize(new BigDecimal("100.00"));
        fund.setNav(new BigDecimal("2.2567"));
        fund.setDayGrowth(new BigDecimal("0.30"));
        fund.setWeekGrowth(new BigDecimal("1.10"));
        fund.setMonthGrowth(new BigDecimal("2.80"));
        fund.setYearGrowth(new BigDecimal("14.50"));
        return fund;
    }

    @Override
    @Transactional
    public DataCollectionTarget addTargetFund(String fundCode) {
        try {
            // 1. 检查目标是否已存在
            Optional<DataCollectionTarget> existingTargetOpt = dataCollectionTargetRepository.findByCode(fundCode);
            if (existingTargetOpt.isPresent()) {
                log.info("基金目标已存在: {}，直接返回已存在的配置", fundCode);
                return existingTargetOpt.get();
            }

            log.info("开始添加目标基金: {}", fundCode);

            // 2. 获取基金实时数据
            FundQuote fundQuote = fetchFundRealTimeData(fundCode);
            if (fundQuote == null) {
                throw new RuntimeException("无法获取基金 " + fundCode + " 的实时数据");
            }

            // 3. 从实时数据中提取基础信息（使用专门的转换器）
            Fund fundBasic = fundQuoteConverter.toFundBasicInfo(fundQuote);

            // 4. 保存到fund_basic表
            fundRepository.save(fundBasic);
            log.info("基金基础信息已保存到fund_basic表: {}", fundCode);

            // 5. 保存实时数据到fund_quote表
            fundQuoteRepository.save(fundQuote);
            log.info("基金实时数据已保存到fund_quote表: {}", fundCode);

            // 6. 创建数据采集目标配置
            DataCollectionTarget target = new DataCollectionTarget();
            target.setCode(fundCode);
            target.setName(fundQuote.getFundName());
            target.setType("FUND");
            target.setActive(true);
            target.setCollectionFrequency(15); // 默认15分钟采集一次
            target.setDataSource("SINA_API");

            // 7. 保存到data_collection_target表
            DataCollectionTarget savedTarget = dataCollectionTargetRepository.save(target);
            log.info("数据采集目标已保存到data_collection_target表: {}", fundCode);

            return savedTarget;

        } catch (Exception e) {
            log.error("添加目标基金 {} 时发生异常: {}", fundCode, e.getMessage(), e);
            throw new RuntimeException("添加目标基金失败: " + e.getMessage(), e);
        }
    }
}
