package com.stock.fund.domain.repository.alert;

import com.stock.fund.domain.entity.alert.PriceAlert;
import java.util.List;
import java.util.Optional;
import java.util.Set;

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
     * 批量查询用户已存在的标的
     *
     * @param userId 用户ID
     * @param symbols 标的代码列表
     * @param symbolType 标的类型
     * @return 已存在的提醒列表
     */
    List<PriceAlert> findByUserIdAndSymbolsAndSymbolType(Long userId, List<String> symbols, String symbolType);

    /**
     * 批量查询用户已存在的提醒（包含提醒类型）
     *
     * @param userId 用户ID
     * @param symbols 标的代码列表
     * @param symbolType 标的类型
     * @param alertType 提醒类型
     * @return 已存在的提醒列表
     */
    List<PriceAlert> findByUserIdAndSymbolsAndSymbolTypeAndAlertType(
            Long userId, List<String> symbols, String symbolType, String alertType);

    /**
     * 批量插入提醒
     * 
     * @param alerts 提醒列表
     * @return 保存后的提醒列表
     */
    List<PriceAlert> batchInsert(List<PriceAlert> alerts);

    /**
     * 分页查询用户的提醒（使用查询对象避免参数过多）
     */
    List<PriceAlert> findByUserIdWithPage(PriceAlertQuery query);

    /**
     * 统计用户的提醒数量（使用查询对象避免参数过多）
     */
    long countByUserId(PriceAlertQuery query);
}
