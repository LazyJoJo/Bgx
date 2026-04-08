package com.stock.fund.domain.repository.alert;

import com.stock.fund.domain.entity.alert.AlertHistory;
import java.util.List;

/**
 * 提醒历史仓储接口
 */
public interface AlertHistoryRepository {
    List<AlertHistory> findByUserId(Long userId);
    List<AlertHistory> findByUserIdAndAlertId(Long userId, Long alertId);
    List<AlertHistory> findByUserIdAndDateRange(Long userId, java.time.LocalDateTime start, java.time.LocalDateTime end);
    AlertHistory save(AlertHistory history);
    List<AlertHistory> saveAll(List<AlertHistory> histories);
}