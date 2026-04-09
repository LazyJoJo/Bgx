package com.stock.fund.domain.repository;

import com.stock.fund.domain.entity.riskalert.RiskAlert;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * 风险提醒仓储接口
 */
public interface RiskAlertRepository {

    /**
     * 根据ID查询
     */
    Optional<RiskAlert> findById(Long id);

    /**
     * 保存风险提醒
     */
    RiskAlert save(RiskAlert riskAlert);

    /**
     * 更新风险提醒
     */
    RiskAlert update(RiskAlert riskAlert);

    /**
     * 根据用户ID查询风险提醒（分页）
     */
    List<RiskAlert> findByUserId(Long userId, LocalDate cursor, int limit);

    /**
     * 根据用户ID、标的代码、日期查询已存在的风险提醒
     */
    Optional<RiskAlert> findByUserIdAndSymbolAndAlertDateAndTimePoint(
            Long userId, String symbol, LocalDate alertDate, String timePoint);

    /**
     * 根据用户ID和日期范围查询
     */
    List<RiskAlert> findByUserIdAndDateRange(Long userId, LocalDate startDate, LocalDate endDate);

    /**
     * 分页查询用户风险提醒（使用查询对象避免参数过多）
     */
    List<RiskAlert> findByUserIdWithPage(RiskAlertQuery query);

    /**
     * 统计用户风险提醒数量（使用查询对象避免参数过多）
     */
    long countByUserId(RiskAlertQuery query);

    /**
     * 获取用户未读风险提醒数量
     */
    long countUnreadByUserId(Long userId);

    /**
     * 标记用户所有风险提醒为已读
     */
    void markAllAsRead(Long userId);

    /**
     * 根据ID删除
     */
    void deleteById(Long id);
}
