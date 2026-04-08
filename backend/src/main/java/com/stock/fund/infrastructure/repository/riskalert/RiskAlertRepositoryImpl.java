package com.stock.fund.infrastructure.repository.riskalert;

import com.stock.fund.domain.entity.riskalert.RiskAlert;
import com.stock.fund.domain.repository.RiskAlertRepository;
import com.stock.fund.infrastructure.entity.riskalert.RiskAlertPO;
import com.stock.fund.infrastructure.mapper.riskalert.RiskAlertMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
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
    public List<RiskAlert> findByUserId(Long userId, LocalDateTime cursor, int limit) {
        List<RiskAlertPO> pos = riskAlertMapper.findByUserIdWithCursor(userId, cursor, limit);
        return pos.stream().map(this::mapToDomain).collect(Collectors.toList());
    }

    @Override
    public Optional<RiskAlert> findByUserIdAndSymbolAndDate(Long userId, String symbol, LocalDateTime date) {
        // 将日期转换为当天开始和结束时间
        LocalDateTime startOfDay = date.toLocalDate().atStartOfDay();
        LocalDateTime endOfDay = date.toLocalDate().atTime(LocalTime.MAX);
        RiskAlertPO po = riskAlertMapper.findByUserIdAndSymbolAndDate(userId, symbol, startOfDay);
        return po != null ? Optional.of(mapToDomain(po)) : Optional.empty();
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
    public List<RiskAlert> findLatestByUserId(Long userId, int limit) {
        List<RiskAlertPO> pos = riskAlertMapper.findByUserIdWithCursor(userId, LocalDateTime.now(), limit);
        return pos.stream().map(this::mapToDomain).collect(Collectors.toList());
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
        alert.setChangePercent(po.getChangePercent());
        alert.setCurrentPrice(po.getCurrentPrice());
        alert.setYesterdayClose(po.getYesterdayClose());
        alert.setTriggerCount(po.getTriggerCount());
        alert.setIsRead(po.getIsRead());
        alert.setTriggeredAt(po.getTriggeredAt());
        alert.setTriggerReason(po.getTriggerReason());
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
        po.setChangePercent(alert.getChangePercent());
        po.setCurrentPrice(alert.getCurrentPrice());
        po.setYesterdayClose(alert.getYesterdayClose());
        po.setTriggerCount(alert.getTriggerCount());
        po.setIsRead(alert.getIsRead());
        po.setTriggeredAt(alert.getTriggeredAt());
        po.setTriggerReason(alert.getTriggerReason());
        po.setCreatedAt(alert.getCreatedAt());
        po.setUpdatedAt(alert.getUpdatedAt());
        return po;
    }
}
