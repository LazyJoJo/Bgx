package com.stock.fund.infrastructure.mapper.alert;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.stock.fund.infrastructure.entity.alert.AlertHistoryPO;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AlertHistoryMapper extends BaseMapper<AlertHistoryPO> {
    List<AlertHistoryPO> findByUserId(@Param("userId") Long userId);
    
    List<AlertHistoryPO> findByUserIdAndAlertId(@Param("userId") Long userId, @Param("alertId") Long alertId);
    
    List<AlertHistoryPO> findByUserIdAndDateRange(
        @Param("userId") Long userId,
        @Param("start") LocalDateTime start,
        @Param("end") LocalDateTime end
    );
}