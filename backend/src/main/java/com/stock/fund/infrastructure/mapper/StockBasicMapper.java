package com.stock.fund.infrastructure.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.stock.fund.infrastructure.entity.StockBasicPO;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface StockBasicMapper extends BaseMapper<StockBasicPO> {
    int insertBatchSomeColumn(List<StockBasicPO> entityList);

    StockBasicPO findBySymbol(@Param("symbol") String symbol);

    List<StockBasicPO> findByIndustry(@Param("industry") String industry);

    List<StockBasicPO> findByMarket(@Param("market") String market);

    @Select("<script>" +
            "SELECT * FROM stock_basic WHERE symbol IN " +
            "<foreach item='item' index='index' open='(' separator=',' close=')' collection='symbols'>" +
            "#{item}" +
            "</foreach>" +
            "</script>")
    List<StockBasicPO> findBySymbols(@Param("symbols") List<String> symbols);
}