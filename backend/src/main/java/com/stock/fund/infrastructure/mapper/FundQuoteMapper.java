package com.stock.fund.infrastructure.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.stock.fund.infrastructure.entity.FundQuotePO;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface FundQuoteMapper extends BaseMapper<FundQuotePO> {
    List<FundQuotePO> findByFundCode(@Param("fundCode") String fundCode);
    
    FundQuotePO findLatestByFundCode(@Param("fundCode") String fundCode);
    
    FundQuotePO findByFundCodeAndQuoteDate(
        @Param("fundCode") String fundCode,
        @Param("quoteDate") LocalDate quoteDate
    );
    
    /**
     * 获取所有基金的最新净值记录
     * 每个基金只返回最新的一条记录
     */
    List<FundQuotePO> findAllLatestQuotes();
    
    /**
     * 分页查询基金净值（支持条件搜索）
     * @param page 分页参数
     * @param fundCode 基金代码（模糊查询）
     * @param fundName 基金名称（模糊查询）
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @param orderBy 排序字段
     * @param orderDirection 排序方向（ASC/DESC）
     * @return 分页结果
     */
    IPage<FundQuotePO> findPageByCondition(
        Page<FundQuotePO> page,
        @Param("fundCode") String fundCode,
        @Param("fundName") String fundName,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate,
        @Param("orderBy") String orderBy,
        @Param("orderDirection") String orderDirection
    );
}