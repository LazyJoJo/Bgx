package com.stock.fund.infrastructure.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.stock.fund.infrastructure.entity.StockQuotePO;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface StockQuoteMapper extends BaseMapper<StockQuotePO> {
    List<StockQuotePO> findByStockId(@Param("stockId") Long stockId);

    List<StockQuotePO> findByStockIdAndQuoteTimeBetween(
        @Param("stockId") Long stockId,
        @Param("start") LocalDateTime start,
        @Param("end") LocalDateTime end
    );

    /**
     * 获取所有股票的最新行情记录
     * 每个股票只返回最新的一条记录
     */
    List<StockQuotePO> findAllLatestQuotes();
}