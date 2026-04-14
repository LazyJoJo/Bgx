package com.stock.fund.interfaces.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.stock.fund.application.service.DataCollectionAppService;
import com.stock.fund.application.service.DataCollectionTargetAppService;
import com.stock.fund.domain.entity.FundQuote;
import com.stock.fund.domain.entity.DataCollectionTarget;
import com.stock.fund.domain.repository.FundQuoteRepository;
import com.stock.fund.interfaces.dto.request.FundQuoteQueryRequest;
import com.stock.fund.interfaces.dto.response.ApiResponse;
import com.stock.fund.interfaces.dto.response.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.math.BigDecimal;
import java.util.HashMap;

@RestController
@RequestMapping("/api/fund-analysis")
@Tag(name = "基金分析", description = "基金实时行情分析和数据查询接口")
public class FundAnalysisController {

    @Autowired
    private DataCollectionAppService dataCollectionAppService;

    @Autowired
    private DataCollectionTargetAppService dataCollectionTargetAppService;

    @Autowired
    private FundQuoteRepository fundQuoteRepository;

    /**
     * 分页查询基金净值（支持条件搜索和排序）
     */
    @GetMapping("/quotes/page")
    @Operation(summary = "分页查询基金净值", description = "支持按基金代码、名称模糊查询和日期范围筛选，支持后端分页和多字段排序")
    public ApiResponse<PageResponse<FundQuote>> getQuotesPage(FundQuoteQueryRequest request) {
        try {
            // 获取安全的排序字段
            String orderBy = request.getSafeOrderByForFundQuote();
            String orderDirection = request.getSafeOrderDirection();
            
            IPage<FundQuote> page = fundQuoteRepository.findPageByCondition(
                request.getPageNum(),
                request.getPageSize(),
                request.getFundCode(),
                request.getFundName(),
                request.getStartDate(),
                request.getEndDate(),
                orderBy,
                orderDirection
            );
            
            return ApiResponse.success(PageResponse.from(page));
        } catch (Exception e) {
            return ApiResponse.error("查询基金净值失败：" + e.getMessage());
        }
    }

    /**
     * 从数据库获取所有基金的最新净值（默认加载）
     */
    @GetMapping("/quotes/latest")
    @Operation(summary = "获取所有基金最新净值", description = "从数据库获取所有基金的最新净值记录，数据由定时器更新")
    public ApiResponse<List<FundQuote>> getAllLatestQuotes() {
        try {
            List<FundQuote> quotes = fundQuoteRepository.findAllLatestQuotes();
            return ApiResponse.success(quotes);
        } catch (Exception e) {
            return ApiResponse.error("获取基金净值失败：" + e.getMessage());
        }
    }

    /**
     * 实时刷新 - 调用外部API获取最新数据并保存到数据库
     */
    @PostMapping("/quotes/refresh")
    @Operation(summary = "实时刷新基金行情", description = "从外部API获取所有基金的实时行情并保存到数据库（UPSERT逻辑）")
    public ApiResponse<List<FundQuote>> refreshQuotes() {
        try {
            // 1. 获取所有基金采集目标
            List<DataCollectionTarget> targets = dataCollectionTargetAppService.getTargetsByType("FUND");
            
            // 2. 逐个获取实时数据并保存
            List<FundQuote> quotes = new ArrayList<>();
            for (DataCollectionTarget target : targets) {
                try {
                    FundQuote quote = dataCollectionAppService.fetchFundRealTimeData(target.getCode());
                    if (quote != null) {
                        // 保存到数据库（UPSERT：存在则更新，不存在则插入）
                        fundQuoteRepository.save(quote);
                        quotes.add(quote);
                    }
                } catch (Exception e) {
                    // 单个基金获取失败不影响其他基金
                    FundQuote errorQuote = new FundQuote();
                    errorQuote.setFundCode(target.getCode());
                    errorQuote.setFundName(target.getName());
                    quotes.add(errorQuote);
                }
            }
            return ApiResponse.success(quotes);
        } catch (Exception e) {
            return ApiResponse.error("刷新基金行情失败：" + e.getMessage());
        }
    }

    /**
     * 获取单个基金的实时行情（从外部API）
     */
    @GetMapping("/quote/{code}")
    @Operation(summary = "获取单个基金实时行情", description = "从外部API获取指定基金的实时净值、涨跌额、涨跌幅等行情数据")
    public ApiResponse<FundQuote> getFundQuote(
            @Parameter(description = "基金代码", example = "000001") @PathVariable String code) {
        try {
            FundQuote quote = dataCollectionAppService.fetchFundRealTimeData(code);
            return ApiResponse.success(quote);
        } catch (Exception e) {
            return ApiResponse.error("获取基金行情失败：" + e.getMessage());
        }
    }

    /**
     * 获取单个基金的最新净值（从数据库）
     */
    @GetMapping("/quote/latest/{code}")
    @Operation(summary = "获取单个基金最新净值", description = "从数据库获取指定基金的最新净值记录")
    public ApiResponse<FundQuote> getLatestFundQuote(
            @Parameter(description = "基金代码", example = "000001") @PathVariable String code) {
        try {
            FundQuote quote = fundQuoteRepository.findLatestByFundCode(code);
            if (quote == null) {
                return ApiResponse.error("未找到基金净值数据：" + code);
            }
            return ApiResponse.success(quote);
        } catch (Exception e) {
            return ApiResponse.error("获取基金净值失败：" + e.getMessage());
        }
    }

    /**
     * 获取单个基金的历史净值
     */
    @GetMapping("/history/{code}")
    @Operation(summary = "获取基金历史净值", description = "获取指定基金的历史净值数据")
    public ApiResponse<List<FundQuote>> getFundHistory(
            @Parameter(description = "基金代码", example = "000001") @PathVariable String code,
            @Parameter(description = "天数", example = "30") @RequestParam(defaultValue = "30") Integer days) {
        try {
            List<FundQuote> history = fundQuoteRepository.findByFundCode(code);
            return ApiResponse.success(history);
        } catch (Exception e) {
            return ApiResponse.error("获取基金历史数据失败：" + e.getMessage());
        }
    }

    /**
     * 获取基金统计信息
     */
    @GetMapping("/statistics")
    @Operation(summary = "获取基金统计信息", description = "获取基金涨跌统计、市场概览等信息")
    public ApiResponse<Map<String, Object>> getFundStatistics() {
        try {
            List<FundQuote> quotes = fundQuoteRepository.findAllLatestQuotes();
            
            int totalFunds = quotes.size();
            int risingCount = 0;
            int fallingCount = 0;
            int flatCount = 0;
            
            for (FundQuote quote : quotes) {
                if (quote.getChangePercent() != null) {
                    int cmp = quote.getChangePercent().compareTo(BigDecimal.ZERO);
                    if (cmp > 0) {
                        risingCount++;
                    } else if (cmp < 0) {
                        fallingCount++;
                    } else {
                        flatCount++;
                    }
                }
            }
            
            Map<String, Object> statistics = new HashMap<>();
            statistics.put("totalFunds", totalFunds);
            statistics.put("risingCount", risingCount);
            statistics.put("fallingCount", fallingCount);
            statistics.put("flatCount", flatCount);
            return ApiResponse.success(statistics);
        } catch (Exception e) {
            return ApiResponse.error("获取基金统计信息失败：" + e.getMessage());
        }
    }
}
