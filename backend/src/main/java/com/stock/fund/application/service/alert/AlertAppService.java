package com.stock.fund.application.service.alert;

import com.stock.fund.domain.entity.alert.PriceAlert;
import com.stock.fund.domain.entity.alert.AlertHistory;
import java.util.List;

/**
 * 提醒应用服务接口
 */
public interface AlertAppService {
    /**
     * 创建提醒
     */
    PriceAlert createAlert(PriceAlert alert);

    /**
     * 更新提醒
     */
    PriceAlert updateAlert(Long alertId, PriceAlert alert);

    /**
     * 删除提醒
     */
    void deleteAlert(Long alertId);

    /**
     * 获取用户的所有提醒
     */
    List<PriceAlert> getUserAlerts(Long userId);

    /**
     * 获取用户激活的提醒
     */
    List<PriceAlert> getUserActiveAlerts(Long userId);

    /**
     *提醒
     */
    void activateAlert(Long alertId);

    /**
     *提醒
     */
    void deactivateAlert(Long alertId);

    /**
     * 获取提醒历史
     */
    List<AlertHistory> getAlertHistory(Long userId, Long alertId);

    /**
     * 获取用户的所有提醒历史
     */
    List<AlertHistory> getAllAlertHistory(Long userId);

    /**
     *检查并触发提醒
     */
    void checkAndTriggerAlerts(String entityCode, String entityType, Double currentPrice);

    /**
     *批检查提醒
     */
    void batchCheckAlerts();
}