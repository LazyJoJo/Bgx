package com.stock.fund.domain.repository;

import com.stock.fund.domain.entity.riskalert.RiskAlert;

import java.time.LocalDateTime;
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
    List<RiskAlert> findByUserId(Long userId, LocalDateTime cursor, int limit);

    /**
     * 根据用户ID和标的代码+日期查询已存在的风险提醒
     * 用于判断当日是否已存在该标的的风险提醒
     */
    Optional<RiskAlert> findByUserIdAndSymbolAndDate(Long userId, String symbol, LocalDateTime date);

    /**
     * 获取用户未读风险提醒数量
     */
    long countUnreadByUserId(Long userId);

    /**
     * 标记用户所有风险提醒为已读
     */
    void markAllAsRead(Long userId);

    /**
     * 根据用户ID查询最新的风险提醒（用于游标分页）
     */
    List<RiskAlert> findLatestByUserId(Long userId, int limit);

    /**
     * 根据ID删除
     */
    void deleteById(Long id);
}
