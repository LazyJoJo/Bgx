package com.stock.fund.application.service.impl;

import com.stock.fund.application.service.DataCollectionAppService;
import com.stock.fund.domain.entity.Stock;
import com.stock.fund.domain.entity.Fund;
import com.stock.fund.domain.entity.StockQuote;
import com.stock.fund.domain.entity.FundQuote;
import com.stock.fund.domain.entity.DataCollectionTarget;
import com.stock.fund.domain.repository.StockRepository;
import com.stock.fund.domain.repository.FundRepository;
import com.stock.fund.domain.repository.StockQuoteRepository;
import com.stock.fund.domain.repository.FundQuoteRepository;
import com.stock.fund.domain.repository.DataCollectionTargetRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DataCollectionAppServiceImpl implements DataCollectionAppService {

    @Autowired
    private StockRepository stockRepository;
    
    @Autowired
    private FundRepository fundRepository;
    
    @Autowired
    private StockQuoteRepository stockQuoteRepository;
    
    @Autowired
    private FundQuoteRepository fundQuoteRepository;
    
    @Autowired
    private DataCollectionTargetRepository dataCollectionTargetRepository;
    
    @Autowired
    private OkHttpClient httpClient;

    @Override
    public List<Stock> collectStockBasicList() {
        // 模拟从数据源获取股票基本信息
        // 实际实现中这里会调用外部API如Tushare等
        return List.of(
            createSampleStock("600000", "浦发银行", "银行", "沪市"),
            createSampleStock("600519", "贵州茅台", "白酒", "沪市"),
            createSampleStock("000001", "平安银行", "银行", "深市")
        );
    }

    @Override
    public StockQuote collectStockQuote(String symbol) {
        // 模拟从数据源获取股票实时行情
        // 实际实现中这里会调用外部API如Tushare等
        Stock stock = stockRepository.findBySymbol(symbol)
            .orElseThrow(() -> new RuntimeException("股票不存在: " + symbol));
        
        StockQuote quote = new StockQuote();
        quote.setStockId(stock.getId());
        quote.setQuoteTime(LocalDateTime.now());
        quote.setOpen(100.00);
        quote.setHigh(105.00);
        quote.setLow(98.00);
        quote.setClose(102.50);
        quote.setVolume(1000000L);
        quote.setAmount(102500000.00);
        quote.setChange(2.50);
        quote.setChangePercent(2.50);
        
        return quote;
    }

    @Override
    public List<StockQuote> collectStockQuotes(List<String> symbols) {
        return symbols.stream()
            .map(this::collectStockQuote)
            .collect(Collectors.toList());
    }

    @Override
    public List<Fund> collectFundBasicList() {
        // 模拟从数据源获取基金基本信息
        // 实际实现中这里会调用外部API如Tushare等
        return List.of(
            createSampleFund("000001", "华夏成长混合", "混合型", "王明"),
            createSampleFund("000011", "易方达价值精选", "混合型", "李华"),
            createSampleFund("110011", "易方达中小盘混合", "混合型", "张伟")
        );
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
        quote.setPrevNetValue(fund.getNav() * 0.99); // 计算昨日净值（示例逻辑）
        quote.setChangeAmount(fund.getDayGrowth());
        quote.setChangePercent(fund.getDayGrowth());
        
        return quote;
    }

    @Override
    public List<FundQuote> collectFundQuotes(List<String> fundCodes) {
        return fundCodes.stream()
            .map(this::collectFundQuote)
            .collect(Collectors.toList());
    }

    @Override
    public void fetchAndSaveFundRealTimeData() {
        // 从数据库获取所有活跃的基金采集目标
        List<DataCollectionTarget> fundTargets = dataCollectionTargetRepository.findByTypeAndActive("FUND", true);

        if (fundTargets.isEmpty()) {
            System.out.println("没有找到活跃的基金采集目标");
            return;
        }

        System.out.println("找到 " + fundTargets.size() + " 个活跃基金采集目标");

        for (DataCollectionTarget target : fundTargets) {
            try {
                System.out.println("正在处理基金目标: " + target.getCode() + " - " + target.getName());

                // 获取基金实时数据
                FundQuote newFundQuote = fetchFundRealTimeData(target.getCode());
                
                if (newFundQuote != null) {
                    // 检查是否已存在同一天的相同基金代码的记录
                    FundQuote existingQuote = fundQuoteRepository.findByFundCodeAndQuoteDate(
                        newFundQuote.getFundCode(), newFundQuote.getQuoteDate());
                    
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
                        System.out.println("更新基金数据: " + target.getCode() + 
                                         " 日期: " + existingQuote.getQuoteDate() +
                                         " 时间: " + existingQuote.getQuoteTimeOnly() +
                                         " 净值: " + existingQuote.getNav() + 
                                         " 涨跌幅: " + existingQuote.getChangePercent() + "%" +
                                         " 涨跌额: " + existingQuote.getChangeAmount());
                    } else {
                        // 如果不存在同一天的记录，保存新记录
                        fundQuoteRepository.save(newFundQuote);
                        System.out.println("新增基金数据: " + target.getCode() + 
                                         " 日期: " + newFundQuote.getQuoteDate() +
                                         " 时间: " + newFundQuote.getQuoteTimeOnly() +
                                         " 净值: " + newFundQuote.getNav() + 
                                         " 涨跌幅: " + newFundQuote.getChangePercent() + "%" +
                                         " 涨跌额: " + newFundQuote.getChangeAmount());
                    }
                } else {
                    System.out.println("未能获取到基金 " + target.getCode() + " 的实时数据");
                }

            } catch (Exception e) {
                System.err.println("处理基金目标 " + target.getCode() + " 时发生异常: " + e.getMessage());
                e.printStackTrace();
            }
        }

        System.out.println("基金实时数据更新完成");
    }

    @Override
    public FundQuote fetchFundRealTimeData(String fundCode) {
        try {
            Map<String, String> fundData = fetchFundRealTimeDataFromAPI(fundCode);
            
            if (fundData != null && !fundData.isEmpty()) {
                // 创建一个临时的DataCollectionTarget对象用于转换
                DataCollectionTarget target = new DataCollectionTarget();
                target.setCode(fundCode);
                // 尝试从数据中获取基金名称，如果无法获取则使用基金代码作为名称
                target.setName(fundData.getOrDefault("name", fundCode));
                
                // 将数据转换为FundQuote实体
                return convertToFundQuote(target, fundData);
            }
        } catch (Exception e) {
            System.err.println("获取基金 " + fundCode + " 数据时发生异常: " + e.getMessage());
            e.printStackTrace();
        }
        
        return null;
    }
    
    /**
     * 从外部API获取基金实时数据
     * @param fundCode 基金代码
     * @return 基金数据Map
     */
    private Map<String, String> fetchFundRealTimeDataFromAPI(String fundCode) {
        String urlStr = String.format(
                "https://hq.sinajs.cn/list=fu_%s",
                fundCode
        );

        Request request = new Request.Builder()
                .url(urlStr)
                .addHeader("User-Agent", 
                    "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                .addHeader("Referer", "https://finance.sina.com.cn/")
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                System.err.println("请求基金数据失败，HTTP Code: " + response.code());
                return null;
            }
            
            ResponseBody body = response.body();
            if (body == null) {
                System.err.println("响应体为空");
                return null;
            }
            
            // 获取原始字节数组以处理可能的编码问题
            byte[] bytes = body.bytes();
            // 根据新浪API特性，使用GBK编码处理中文
            String content = new String(bytes, java.nio.charset.Charset.forName("GBK"));
            
            // 解析基金数据
            return parseFundData(content, fundCode);

        } catch (Exception e) {
            System.err.println("获取基金 " + fundCode + " 数据时发生异常: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * 解析基金数据
     * @param response 响应数据
     * @param fundCode 基金代码
     * @return 解析后的基金数据Map
     */
    private Map<String, String> parseFundData(String response, String fundCode) {
        try {
            if (response == null || response.trim().isEmpty()) {
                return null;
            }

            // 响应格式通常是 var hq_str_fu_{code}="name,date,time,net_value,growth_rate,other_data";
            String[] lines = response.split(";");
            for (String line : lines) {
                line = line.trim();
                if (line.startsWith("var hq_str_")) {
                    int start = line.indexOf('"');
                    int end = line.lastIndexOf('"');
                    if (start != -1 && end != -1 && start < end) {
                        String data = line.substring(start + 1, end);
                        String[] fields = data.split(",");

                        if (fields.length >= 5) {  // 至少需要基金名称、时间、净值、昨日净值
                            java.util.HashMap<String, String> fundData = new java.util.HashMap<>();
                            fundData.put("fundCode", fundCode);
                            fundData.put("name", fields[0]);           // 基金名称
                            fundData.put("time", fields[1]);           // 时间
                            fundData.put("netValue", fields[2]);       // 净值
                            fundData.put("oldNetValue", fields[3]);    // 昨日净值
                            return fundData;
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("解析基金数据时发生异常: " + e.getMessage());
            e.printStackTrace();
        }

        return null;
    }
    
    /**
     * 将基金数据转换为FundQuote实体
     * @param target 采集目标
     * @param fundData 基金数据
     * @return FundQuote实体
     */
    private FundQuote convertToFundQuote(DataCollectionTarget target, Map<String, String> fundData) {
        FundQuote fundQuote = new FundQuote();
        
        // 设置基金代码
        fundQuote.setFundCode(target.getCode());
        
        // 设置基金名称
        fundQuote.setFundName(target.getName());
        
        // 解析净值
        double nav = 0.0;
        try {
            nav = Double.parseDouble(fundData.get("netValue"));
            fundQuote.setNav(nav);
        } catch (NumberFormatException | NullPointerException e) {
            fundQuote.setNav(0.0);
        }
        
        // 从API获取的昨日净值计算涨跌幅
        double oldNetValue = 0.0;
        try {
            oldNetValue = Double.parseDouble(fundData.get("oldNetValue"));
            double changePercent = ((nav - oldNetValue) / oldNetValue) * 100.0;
            fundQuote.setChangePercent(changePercent);
            fundQuote.setPrevNetValue(oldNetValue); // 昨日净值
        } catch (NumberFormatException | NullPointerException e) {
            // 如果无法计算涨跌幅，设为0
            fundQuote.setChangePercent(0.0);
        }
        
        // 计算涨跌额 (当前净值 - 昨日净值)
        double changeAmount = nav - oldNetValue;
        fundQuote.setChangeAmount(changeAmount);
        
        // 使用当前系统日期，但使用外部接口提供的具体时间值
        String timeStr = fundData.get("time");
        
        LocalDate currentDate = LocalDate.now(); // 日期总是使用当前系统日期
        LocalTime specificTime = LocalTime.now(); // 默认使用当前时间
        
        if (timeStr != null && !timeStr.trim().isEmpty()) {
            // 尝试解析API返回的时间字符串
            try {
                // 根据API返回的实际格式解析时间
                // timeStr 格式可能是 HH:MM:SS 或 HH:MM
                
                // 根据时间字符串的格式进行解析
                if (timeStr.contains(":")) {
                    // 如果时间包含冒号，按标准时间格式解析
                    specificTime = LocalTime.parse(timeStr.split("\\.")[0]); // 移除毫秒部分
                } else {
                    // 如果时间不包含冒号，可能是HHMMSS格式
                    if (timeStr.length() == 6) {
                        int hour = Integer.parseInt(timeStr.substring(0, 2));
                        int minute = Integer.parseInt(timeStr.substring(2, 4));
                        int second = Integer.parseInt(timeStr.substring(4, 6));
                        specificTime = LocalTime.of(hour, minute, second);
                    }
                    // 如果无法从API解析时间，保持使用当前时间
                }
            } catch (Exception e) {
                // 如果解析API时间失败，使用当前时间
                System.out.println("无法解析API返回的具体时间数据，使用系统当前时间: " + e.getMessage());
                specificTime = LocalTime.now();
            }
        }
        
        fundQuote.setQuoteDate(currentDate);
        fundQuote.setQuoteTimeOnly(specificTime);
        
        return fundQuote;
    }

    private Stock createSampleStock(String symbol, String name, String industry, String market) {
        Stock stock = new Stock();
        stock.setSymbol(symbol);
        stock.setName(name);
        stock.setIndustry(industry);
        stock.setMarket(market);
        stock.setListingDate(LocalDate.of(2000, 1, 1));
        stock.setTotalShare(1000000.00);
        stock.setFloatShare(1000000.00);
        stock.setPe(15.0);
        stock.setPb(1.5);
        return stock;
    }

    private Fund createSampleFund(String fundCode, String name, String type, String manager) {
        Fund fund = new Fund();
        fund.setFundCode(fundCode);
        fund.setName(name);
        fund.setType(type);
        fund.setManager(manager);
        fund.setEstablishmentDate(LocalDate.of(2005, 1, 1));
        fund.setFundSize(100.00);
        fund.setNav(2.2567);
        fund.setDayGrowth(0.30);
        fund.setWeekGrowth(1.10);
        fund.setMonthGrowth(2.80);
        fund.setYearGrowth(14.50);
        return fund;
    }
    
    @Override
    @Transactional
    public DataCollectionTarget addTargetFund(String fundCode) {
        try {
            // 1. 检查目标是否已存在
            Optional<DataCollectionTarget> existingTargetOpt = dataCollectionTargetRepository.findByCode(fundCode);
            if (existingTargetOpt.isPresent()) {
                System.out.println("基金目标已存在: " + fundCode + "，直接返回已存在的配置");
                return existingTargetOpt.get();
            }
            
            System.out.println("开始添加目标基金: " + fundCode);
            
            // 2. 获取基金实时数据
            FundQuote fundQuote = fetchFundRealTimeData(fundCode);
            if (fundQuote == null) {
                throw new RuntimeException("无法获取基金 " + fundCode + " 的实时数据");
            }
            
            // 3. 从实时数据中提取基础信息
            Fund fundBasic = extractFundBasicInfo(fundQuote);
            
            // 4. 保存到fund_basic表
            fundRepository.save(fundBasic);
            System.out.println("基金基础信息已保存到fund_basic表: " + fundCode);
            
            // 5. 保存实时数据到fund_quote表
            fundQuoteRepository.save(fundQuote);
            System.out.println("基金实时数据已保存到fund_quote表: " + fundCode);
            
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
            System.out.println("数据采集目标已保存到data_collection_target表: " + fundCode);
            
            return savedTarget;
            
        } catch (Exception e) {
            System.err.println("添加目标基金 " + fundCode + " 时发生异常: " + e.getMessage());
            throw new RuntimeException("添加目标基金失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 从基金实时数据中提取基础信息
     * @param fundQuote基金实时数据
     * @return Fund基金基础信息
     */
    private Fund extractFundBasicInfo(FundQuote fundQuote) {
        Fund fund = new Fund();
        fund.setFundCode(fundQuote.getFundCode());
        
        // 使用从API获取的基金名称
        fund.setName(fundQuote.getFundName());
        
        // 从实时数据中提取净值作为基础信息
        fund.setNav(fundQuote.getNav());
        fund.setDayGrowth(fundQuote.getChangePercent());
        
        // 设置默认值
        fund.setType("未知"); // 实时数据中通常不包含基金类型
        fund.setManager("未知"); // 实时数据中通常不包含基金经理
        fund.setEstablishmentDate(LocalDate.now()); // 使用当前日期作为默认成立日期
        fund.setFundSize(0.0); // 实时数据中通常不包含基金规模
        fund.setWeekGrowth(0.0);
        fund.setMonthGrowth(0.0);
        fund.setYearGrowth(0.0);
        
        return fund;
    }
}