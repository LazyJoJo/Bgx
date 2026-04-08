package com.stock.fund.infrastructure.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.stock.fund.infrastructure.entity.FundBasicPO;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface FundBasicMapper extends BaseMapper<FundBasicPO> {
    FundBasicPO findByFundCode(@Param("fundCode") String fundCode);
    
    List<FundBasicPO> findByType(@Param("type") String type);
    
    List<FundBasicPO> findByManager(@Param("manager") String manager);
}