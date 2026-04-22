package com.stock.fund.domain.repository;

import java.util.List;
import java.util.Optional;

import com.stock.fund.domain.entity.riskalert.RiskAlertDetail;

/**
 * 风险提醒明细仓储接口
 */
public interface RiskAlertDetailRepository {

    /**
     * 保存风险提醒明细
     */
    RiskAlertDetail save(RiskAlertDetail detail);

    /**
     * 根据ID查询
     */
    Optional<RiskAlertDetail> findById(Long id);

    /**
     * 根据 risk_alert_id 查询所有明细
     */
    List<RiskAlertDetail> findByRiskAlertId(Long riskAlertId);

    /**
     * 根据 risk_alert_id 删除所有明细
     */
    void deleteByRiskAlertId(Long riskAlertId);
}
