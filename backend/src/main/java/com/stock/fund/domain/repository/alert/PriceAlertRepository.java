package com.stock.fund.domain.repository.alert;

import com.stock.fund.domain.entity.alert.PriceAlert;
import java.util.List;
import java.util.Optional;

/**
 * 提醒仓储接口
 */
public interface PriceAlertRepository {
    Optional<PriceAlert> findById(Long id);
    List<PriceAlert> findByUserId(Long userId);
    List<PriceAlert> findByUserIdAndActive(Long userId, Boolean active);
    List<PriceAlert> findActiveAlerts();
    PriceAlert save(PriceAlert alert);
    void deleteById(Long id);
    List<PriceAlert> saveAll(List<PriceAlert> alerts);
}