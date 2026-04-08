package com.stock.fund.application.service.riskalert;

import com.stock.fund.application.service.riskalert.dto.RiskAlertMergeDTO;
import java.util.List;

/**
 * 风险提醒应用服务接口
 */
public interface RiskAlertAppService {

    /**
     * 批量检查并创建风险提醒
     * 遍历所有股票和基金的最新行情，超过阈值则创建风险提醒
     */
    void checkAndCreateRiskAlerts();

    /**
     * 获取用户未读风险提醒数量
     */
    long getUnreadCount(Long userId);

    /**
     * 标记用户所有风险提醒为已读
     */
    void markAllAsRead(Long userId);

    /**
     * 获取用户风险提醒列表（合并后的数据，用于展示）
     * @param userId 用户ID
     * @param cursor 游标（时间戳），用于分页
     * @param limit 每页大小
     * @return 合并后的风险提醒列表
     */
    List<RiskAlertMergeDTO> getMergedRiskAlerts(Long userId, Long cursor, int limit);

    /**
     * 根据ID删除风险提醒
     */
    void deleteById(Long id);
}
