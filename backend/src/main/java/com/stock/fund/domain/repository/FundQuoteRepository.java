package com.stock.fund.domain.repository;

import com.stock.fund.domain.entity.FundQuote;
import com.baomidou.mybatisplus.core.metadata.IPage;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.List;

/**
 *基金净值仓储接口
 */
public interface FundQuoteRepository {
    List<FundQuote> findByFundCode(String fundCode);
    
    /**
     * 保存基金净值（UPSERT逻辑：根据fund_code和quote_date判断存在则更新，不存在则插入）
     */
    FundQuote save(FundQuote fundQuote);
    List<FundQuote> saveAll(List<FundQuote> fundQuotes);
    
    FundQuote findLatestByFundCode(String fundCode);
    
    /**
     * 根据基金代码和日期查找最新的基金净值记录
     */
    FundQuote findByFundCodeAndQuoteDate(String fundCode, LocalDate quoteDate);
    
    /**
     * 获取所有基金的最新净值记录
     * 每个基金只返回最新的一条记录
     */
    List<FundQuote> findAllLatestQuotes();
    
    /**
     * 分页查询基金净值（支持条件搜索）
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @param fundCode 基金代码（模糊查询）
     * @param fundName 基金名称（模糊查询）
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @param orderBy 排序字段
     * @param orderDirection 排序方向（ASC/DESC）
     * @return 分页结果
     */
    IPage<FundQuote> findPageByCondition(int pageNum, int pageSize, String fundCode, String fundName, LocalDate startDate, LocalDate endDate, String orderBy, String orderDirection);
}