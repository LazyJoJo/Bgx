package com.stock.fund.infrastructure.repository.riskalert;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Repository;

import com.stock.fund.domain.entity.riskalert.RiskAlertDetail;
import com.stock.fund.domain.repository.RiskAlertDetailRepository;
import com.stock.fund.infrastructure.entity.riskalert.RiskAlertDetailPO;
import com.stock.fund.infrastructure.mapper.riskalert.RiskAlertDetailMapper;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class RiskAlertDetailRepositoryImpl implements RiskAlertDetailRepository {

    private final RiskAlertDetailMapper detailMapper;

    @Override
    public RiskAlertDetail save(RiskAlertDetail detail) {
        RiskAlertDetailPO po = mapToPO(detail);
        if (detail.getId() == null) {
            detailMapper.insert(po);
            detail.setId(po.getId());
        } else {
            detailMapper.updateById(po);
        }
        return detail;
    }

    @Override
    public Optional<RiskAlertDetail> findById(Long id) {
        RiskAlertDetailPO po = detailMapper.selectById(id);
        return po != null ? Optional.of(mapToDomain(po)) : Optional.empty();
    }

    @Override
    public List<RiskAlertDetail> findByRiskAlertId(Long riskAlertId) {
        List<RiskAlertDetailPO> pos = detailMapper.findByRiskAlertId(riskAlertId);
        return pos.stream().map(this::mapToDomain).collect(Collectors.toList());
    }

    @Override
    public void deleteByRiskAlertId(Long riskAlertId) {
        detailMapper.deleteByRiskAlertId(riskAlertId);
    }

    private RiskAlertDetail mapToDomain(RiskAlertDetailPO po) {
        RiskAlertDetail detail = new RiskAlertDetail();
        detail.setId(po.getId());
        detail.setRiskAlertId(po.getRiskAlertId());
        detail.setSymbol(po.getSymbol());
        detail.setChangePercent(po.getChangePercent());
        detail.setCurrentPrice(po.getCurrentPrice());
        detail.setTriggeredAt(po.getTriggeredAt());
        detail.setTriggerReason(po.getTriggerReason());
        detail.setTimePoint(po.getTimePoint());
        return detail;
    }

    private RiskAlertDetailPO mapToPO(RiskAlertDetail detail) {
        RiskAlertDetailPO po = new RiskAlertDetailPO();
        po.setId(detail.getId());
        po.setRiskAlertId(detail.getRiskAlertId());
        po.setSymbol(detail.getSymbol());
        po.setChangePercent(detail.getChangePercent());
        po.setCurrentPrice(detail.getCurrentPrice());
        po.setTriggeredAt(detail.getTriggeredAt());
        po.setTriggerReason(detail.getTriggerReason());
        po.setTimePoint(detail.getTimePoint());
        return po;
    }
}
