package com.stock.fund.infrastructure.mapper.riskalert;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.stock.fund.infrastructure.entity.riskalert.RiskAlertPO;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface RiskAlertMapper extends BaseMapper<RiskAlertPO> {

    /**
     * 根据用户ID查询风险提醒（按时间倒序，带游标）
     */
    List<RiskAlertPO> findByUserIdWithCursor(@Param("userId") Long userId,
                                              @Param("cursor") LocalDateTime cursor,
                                              @Param("limit") int limit);

    /**
     * 根据用户ID和标的代码+日期查询已存在的风险提醒
     */
    RiskAlertPO findByUserIdAndSymbolAndDate(@Param("userId") Long userId,
                                              @Param("symbol") String symbol,
                                              @Param("date") LocalDateTime date);

    /**
     * 根据用户ID和标的代码+日期+时间点查询
     */
    RiskAlertPO findByUserIdAndSymbolAndAlertDateAndTimePoint(@Param("userId") Long userId,
                                                               @Param("symbol") String symbol,
                                                               @Param("alertDate") LocalDate alertDate,
                                                               @Param("timePoint") String timePoint);

    /**
     * 根据用户ID和日期范围查询
     */
    List<RiskAlertPO> findByUserIdAndDateRange(@Param("userId") Long userId,
                                               @Param("startDate") LocalDate startDate,
                                               @Param("endDate") LocalDate endDate);

    /**
     * 分页查询用户风险提醒
     */
    List<RiskAlertPO> findByUserIdWithPage(@Param("userId") Long userId,
                                            @Param("startDate") LocalDate startDate,
                                            @Param("endDate") LocalDate endDate,
                                            @Param("page") int page,
                                            @Param("size") int size,
                                            @Param("sort") String sort);

    /**
     * 统计用户风险提醒数量
     */
    long countByUserId(@Param("userId") Long userId,
                       @Param("startDate") LocalDate startDate,
                       @Param("endDate") LocalDate endDate);

    /**
     * 获取用户未读风险提醒数量
     */
    long countUnreadByUserId(@Param("userId") Long userId);

    /**
     * 标记用户所有风险提醒为已读
     */
    void markAllAsRead(@Param("userId") Long userId);
}
