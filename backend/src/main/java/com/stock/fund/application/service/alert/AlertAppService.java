package com.stock.fund.application.service.alert;

import com.stock.fund.application.service.alert.dto.*;
import com.stock.fund.domain.entity.alert.PriceAlert;
import com.stock.fund.domain.entity.alert.AlertHistory;
import java.util.List;
import java.util.Optional;

/**
 * 提醒应用服务接口
 */
public interface AlertAppService {
    /**
     * 创建提醒
     * @param request 创建请求
     * @return 创建成功的提醒，如果已存在则返回已存在的提醒
     */
    CreateAlertResponse createAlert(CreateAlertRequest request);

    /**
     * 统一创建提醒（支持单个或批量）
     * @param request 创建请求
     * @return 统一创建结果
     */
    AlertCreateResponse createAlertUnified(AlertCreateRequest request);

    /**
     * 批量创建提醒（优化版）
     * @param request 批量创建请求
     * @return 批量创建结果
     */
    BatchCreateAlertResponse batchCreateAlert(BatchCreateAlertRequest request);

    /**
     * 批量创建提醒V2（匹配新API契约）
     * @param request 批量创建请求V2
     * @return 批量创建结果V2
     */
    BatchCreateAlertResponseV2 batchCreateAlertV2(BatchCreateAlertRequestV2 request);

    /**
     * 检测重复提醒
     * @param request 重复检测请求
     * @return 重复检测结果
     */
    CheckDuplicatesResponse checkDuplicates(CheckDuplicatesRequest request);

    /**
     * 更新提醒
     * @param alertId 提醒ID
     * @param request 更新请求
     * @return 更新后的提醒
     */
    PriceAlert updateAlert(Long alertId, UpdateAlertRequest request);

    /**
     * 删除提醒
     * @param alertId 提醒ID
     */
    void deleteAlert(Long alertId);

    /**
     * 分页查询用户提醒
     * @param query 查询条件
     * @return 分页结果
     */
    AlertPageResponse<PriceAlert> queryAlerts(AlertQueryDTO query);

    /**
     * 获取用户的所有提醒
     * @param userId 用户ID
     * @return 提醒列表
     */
    List<PriceAlert> getUserAlerts(Long userId);

    /**
     * 获取用户激活的提醒
     * @param userId 用户ID
     * @return 激活的提醒列表
     */
    List<PriceAlert> getUserActiveAlerts(Long userId);

    /**
     * 获取单个提醒
     * @param alertId 提醒ID
     * @return 提醒
     */
    Optional<PriceAlert> getAlertById(Long alertId);

    /**
     * 检查用户是否已设置过该标的的提醒
     * @param userId 用户ID
     * @param symbol 标的代码
     * @param symbolType 标的类型
     * @return 已存在的提醒（如果存在）
     */
    Optional<PriceAlert> findExistingAlert(Long userId, String symbol, String symbolType);

    /**
     * 启用提醒
     * @param alertId 提醒ID
     */
    void activateAlert(Long alertId);

    /**
     * 禁用提醒
     * @param alertId 提醒ID
     */
    void deactivateAlert(Long alertId);

    /**
     * 获取提醒历史
     * @param userId 用户ID
     * @param alertId 提醒ID
     * @return 历史列表
     */
    List<AlertHistory> getAlertHistory(Long userId, Long alertId);

    /**
     * 获取用户的所有提醒历史
     * @param userId 用户ID
     * @return 历史列表
     */
    List<AlertHistory> getAllAlertHistory(Long userId);

    /**
     * 检查并触发提醒
     * @param symbol 标的代码
     * @param symbolType 标的类型
     * @param currentPrice 当前价格
     */
    void checkAndTriggerAlerts(String symbol, String symbolType, Double currentPrice);

    /**
     * 批量检查提醒
     */
    void batchCheckAlerts();
}
