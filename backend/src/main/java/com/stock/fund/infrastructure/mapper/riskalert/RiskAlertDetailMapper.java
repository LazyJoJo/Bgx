package com.stock.fund.infrastructure.mapper.riskalert;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.stock.fund.infrastructure.entity.riskalert.RiskAlertDetailPO;

/**
 * 风险提醒明细 MyBatis Mapper
 */
@Repository
public interface RiskAlertDetailMapper extends BaseMapper<RiskAlertDetailPO> {

    /**
     * 根据 risk_alert_id 查询所有明细
     */
    List<RiskAlertDetailPO> findByRiskAlertId(@Param("riskAlertId") Long riskAlertId);

    /**
     * 根据 risk_alert_id 删除所有明细
     */
    void deleteByRiskAlertId(@Param("riskAlertId") Long riskAlertId);
}
