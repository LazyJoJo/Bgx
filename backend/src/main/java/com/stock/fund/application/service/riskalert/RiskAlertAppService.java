package com.stock.fund.application.service.riskalert;

import com.stock.fund.application.service.riskalert.dto.*;
import com.stock.fund.domain.entity.subscription.UserSubscription;
import com.stock.fund.domain.entity.riskalert.RiskAlert;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * 风险提醒应用服务接口
 */
public interface RiskAlertAppService {

    /**
     * 批量检查并创建风险提醒
     * 遍历所有股票和基金的最新行情，超过阈值则创建风险提醒
     * @param timePoint 时间点，传入 "11:30" 或 "14:30"
     */
    void checkAndCreateRiskAlerts(String timePoint);

    /**
     * 批量检查并创建风险提醒（自动判断时间点）
     * 根据当前时间自动判断是11:30还是14:30
     * 用于数据采集后触发风险检测
     */
    void checkAndCreateRiskAlerts();

    /**
     * 创建或更新风险提醒
     * 根据(userId, symbol, alertDate, timePoint)判断是否已存在
     * 如果存在则更新，不存在则创建
     */
    RiskAlert createOrUpdateRiskAlert(RiskAlert riskAlert);

    /**
     * 分页查询风险提醒
     */
    RiskAlertPageResponse<RiskAlert> queryRiskAlerts(RiskAlertQueryDTO query);

    /**
     * 获取今日风险提醒（按日期分组）
     */
    List<RiskAlertSummaryDTO> getTodayRiskAlerts(Long userId);

    /**
     * 获取指定日期范围的风险提醒（按日期分组）
     */
    List<RiskAlertSummaryDTO> getRiskAlertsByDateRange(Long userId, LocalDate startDate, LocalDate endDate);

    /**
     * 获取合并后的风险提醒列表
     */
    List<RiskAlertMergeDTO> getMergedRiskAlerts(Long userId, Long cursor, int limit);

    /**
     * 获取未读数量
     */
    long getUnreadCount(Long userId);

    /**
     * 获取用户今日风险数据条数（用于仪表盘显示）
     * 注意：一个股票/基金算一条，即使存在多个触发记录
     */
    int getTodayRiskAlertCount(Long userId);

    /**
     * 标记单条已读
     */
    void markAsRead(Long riskAlertId);

    /**
     * 标记全部已读
     */
    void markAllAsRead(Long userId);

    /**
     * 根据ID获取风险提醒
     */
    Optional<RiskAlert> getById(Long id);

    /**
     * 根据ID删除风险提醒
     */
    void deleteById(Long id);

    /**
     * 处理订阅触发的风险
     * 根据用户设置的 UserSubscription 判断是否产生风险记录
     * @param subscription 用户设置的订阅，使用 targetChangePercent 判断是否触发
     * @param currentPrice 当前价格
     * @param yesterdayClose 昨日收盘价
     * @param timePoint 时间点，传入 "11:30" 或 "14:30"
     */
    void processSubscriptionRisk(UserSubscription subscription, BigDecimal currentPrice, BigDecimal yesterdayClose, String timePoint);

    /**
     * 批量创建风险提醒
     * 立即检测指定标的的当前风险状态并创建风险提醒记录
     * @param request 批量创建请求
     * @return 批量创建结果
     */
    BatchCreateRiskAlertResponse batchCreateRiskAlerts(BatchCreateRiskAlertRequest request);
}
