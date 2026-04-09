package com.stock.fund.infrastructure.repository.riskalert;

import com.stock.fund.domain.entity.riskalert.RiskAlert;
import com.stock.fund.domain.repository.RiskAlertQuery;
import com.stock.fund.domain.repository.RiskAlertRepository;
import com.stock.fund.infrastructure.entity.riskalert.RiskAlertPO;
import com.stock.fund.infrastructure.mapper.riskalert.RiskAlertMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
public class RiskAlertRepositoryImpl implements RiskAlertRepository {

    @Autowired
    private RiskAlertMapper riskAlertMapper;

    @Override
    public Optional<RiskAlert> findById(Long id) {
        RiskAlertPO po = riskAlertMapper.selectById(id);
        return po != null ? Optional.of(mapToDomain(po)) : Optional.empty();
    }

    @Override
    public RiskAlert save(RiskAlert riskAlert) {
        RiskAlertPO po = mapToPO(riskAlert);
        if (riskAlert.getId() == null) {
            riskAlertMapper.insert(po);
            riskAlert.setId(po.getId());
        } else {
            riskAlertMapper.updateById(po);
        }
        return riskAlert;
    }

    @Override
    public RiskAlert update(RiskAlert riskAlert) {
        RiskAlertPO po = mapToPO(riskAlert);
        riskAlertMapper.updateById(po);
        return riskAlert;
    }

    @Override
    public List<RiskAlert> findByUserId(Long userId, LocalDate cursor, int limit) {
        LocalDateTime cursorTime = cursor != null ? cursor.atStartOfDay() : LocalDateTime.now();
        List<RiskAlertPO> pos = riskAlertMapper.findByUserIdWithCursor(userId, cursorTime, limit);
        return pos.stream().map(this::mapToDomain).collect(Collectors.toList());
    }

    @Override
    public Optional<RiskAlert> findByUserIdAndSymbolAndAlertDateAndTimePoint(
            Long userId, String symbol, LocalDate alertDate, String timePoint) {
        RiskAlertPO po = riskAlertMapper.findByUserIdAndSymbolAndAlertDateAndTimePoint(
                userId, symbol, alertDate, timePoint);
        return po != null ? Optional.of(mapToDomain(po)) : Optional.empty();
    }

    @Override
    public List<RiskAlert> findByUserIdAndDateRange(Long userId, LocalDate startDate, LocalDate endDate) {
        List<RiskAlertPO> pos = riskAlertMapper.findByUserIdAndDateRange(userId, startDate, endDate);
        return pos.stream().map(this::mapToDomain).collect(Collectors.toList());
    }

    @Override
    public List<RiskAlert> findByUserIdWithPage(RiskAlertQuery query) {
        List<RiskAlertPO> pos = riskAlertMapper.findByUserIdWithPage(
                query.getUserId(), query.getStartDate(), query.getEndDate(),
                query.getPage(), query.getSize(), query.getSort());
        return pos.stream().map(this::mapToDomain).collect(Collectors.toList());
    }

    @Override
    public long countByUserId(RiskAlertQuery query) {
        return riskAlertMapper.countByUserId(query.getUserId(), query.getStartDate(), query.getEndDate());
    }

    @Override
    public long countUnreadByUserId(Long userId) {
        return riskAlertMapper.countUnreadByUserId(userId);
    }

    @Override
    @Transactional
    public void markAllAsRead(Long userId) {
        riskAlertMapper.markAllAsRead(userId);
    }

    @Override
    public void deleteById(Long id) {
        riskAlertMapper.deleteById(id);
    }

    private RiskAlert mapToDomain(RiskAlertPO po) {
        RiskAlert alert = new RiskAlert();
        alert.setId(po.getId());
        alert.setUserId(po.getUserId());
        alert.setSymbol(po.getSymbol());
        alert.setSymbolType(po.getSymbolType());
        alert.setSymbolName(po.getSymbolName());
        alert.setAlertDate(po.getAlertDate());
        alert.setTimePoint(po.getTimePoint());
        alert.setHasRisk(po.getHasRisk());
        alert.setChangePercent(po.getChangePercent());
        alert.setCurrentPrice(po.getCurrentPrice());
        alert.setYesterdayClose(po.getYesterdayClose());
        alert.setIsRead(po.getIsRead());
        alert.setTriggeredAt(po.getTriggeredAt());
        alert.setCreatedAt(po.getCreatedAt());
        alert.setUpdatedAt(po.getUpdatedAt());
        return alert;
    }

    private RiskAlertPO mapToPO(RiskAlert alert) {
        RiskAlertPO po = new RiskAlertPO();
        po.setId(alert.getId());
        po.setUserId(alert.getUserId());
        po.setSymbol(alert.getSymbol());
        po.setSymbolType(alert.getSymbolType());
        po.setSymbolName(alert.getSymbolName());
        po.setAlertDate(alert.getAlertDate());
        po.setTimePoint(alert.getTimePoint());
        po.setHasRisk(alert.getHasRisk());
        po.setChangePercent(alert.getChangePercent());
        po.setCurrentPrice(alert.getCurrentPrice());
        po.setYesterdayClose(alert.getYesterdayClose());
        po.setIsRead(alert.getIsRead());
        po.setTriggeredAt(alert.getTriggeredAt());
        po.setCreatedAt(alert.getCreatedAt());
        po.setUpdatedAt(alert.getUpdatedAt());
        return po;
    }
}
