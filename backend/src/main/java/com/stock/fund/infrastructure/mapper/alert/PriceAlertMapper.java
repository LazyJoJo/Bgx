package com.stock.fund.infrastructure.mapper.alert;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.stock.fund.infrastructure.entity.alert.PriceAlertPO;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PriceAlertMapper extends BaseMapper<PriceAlertPO> {
    List<PriceAlertPO> findByUserId(@Param("userId") Long userId);
    
    List<PriceAlertPO> findByUserIdAndActive(@Param("userId") Long userId, @Param("active") Boolean active);
    
    List<PriceAlertPO> findActiveAlerts();
}