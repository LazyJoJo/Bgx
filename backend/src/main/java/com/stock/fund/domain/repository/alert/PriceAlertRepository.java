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

    /**
     * 查找同一用户是否已存在相同标的的提醒
     */
    Optional<PriceAlert> findByUserIdAndSymbolAndSymbolType(Long userId, String symbol, String symbolType);

    /**
     * 分页查询用户的提醒（使用查询对象避免参数过多）
     */
    List<PriceAlert> findByUserIdWithPage(PriceAlertQuery query);

    /**
     * 统计用户的提醒数量（使用查询对象避免参数过多）
     */
    long countByUserId(PriceAlertQuery query);
}
