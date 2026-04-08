package com.stock.fund.infrastructure.repository.alert;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.stock.fund.domain.entity.alert.AlertHistory;
import com.stock.fund.domain.repository.alert.AlertHistoryRepository;
import com.stock.fund.infrastructure.entity.alert.AlertHistoryPO;
import com.stock.fund.infrastructure.mapper.alert.AlertHistoryMapper;

@Repository
public class AlertHistoryRepositoryImpl implements AlertHistoryRepository {

    @Autowired
    private AlertHistoryMapper alertHistoryMapper;

    @Override
    public List<AlertHistory> findByUserId(Long userId) {
        List<AlertHistoryPO> pos = alertHistoryMapper.findByUserId(userId);
        return pos.stream().map(this::mapToDomainEntity).collect(Collectors.toList());
    }

    @Override
    public List<AlertHistory> findByUserIdAndAlertId(Long userId, Long alertId) {
        List<AlertHistoryPO> pos = alertHistoryMapper.findByUserIdAndAlertId(userId, alertId);
        return pos.stream().map(this::mapToDomainEntity).collect(Collectors.toList());
    }

    @Override
    public List<AlertHistory> findByUserIdAndDateRange(Long userId, LocalDateTime start, LocalDateTime end) {
        List<AlertHistoryPO> pos = alertHistoryMapper.findByUserIdAndDateRange(userId, start, end);
        return pos.stream().map(this::mapToDomainEntity).collect(Collectors.toList());
    }

    @Override
    public AlertHistory save(AlertHistory history) {
        AlertHistoryPO po = mapToPO(history);
        if (history.getId() == null) {
            alertHistoryMapper.insert(po);
            history.setId(po.getId());
        } else {
            alertHistoryMapper.updateById(po);
        }
        return history;
    }

    @Override
    public List<AlertHistory> saveAll(List<AlertHistory> histories) {
        for (AlertHistory history : histories) {
            save(history);
        }
        return histories;
    }

    // 私方法：将PO转换为领域实体
    private AlertHistory mapToDomainEntity(AlertHistoryPO po) {
        AlertHistory history = new AlertHistory();
        history.setId(po.getId());
        history.setUserId(po.getUserId());
        history.setAlertId(po.getAlertId());
        // history.setEntityCode(po.getEntityCode());
        // history.setEntityType(po.getEntityType());
        // history.setEntityName(po.getEntityName());
        // history.setAlertType(po.getAlertType());
        // history.setThreshold(po.getThreshold());
        // history.setCurrentValue(po.getCurrentValue());
        // history.setTriggeredAt(po.getTriggeredAt());
        history.setTriggerReason(po.getTriggerReason());
        history.setCreatedAt(po.getCreatedAt());
        history.setUpdatedAt(po.getUpdatedAt());
        return history;
    }

    // 私有方法：将领域实体转换为PO
    private AlertHistoryPO mapToPO(AlertHistory history) {
        AlertHistoryPO po = new AlertHistoryPO();
        po.setId(history.getId());
        po.setUserId(history.getUserId());
        po.setAlertId(history.getAlertId());
        // po.setEntityCode(history.getEntityCode());
        // po.setEntityType(history.getEntityType());
        // po.setEntityName(history.getEntityName());
        // po.setAlertType(history.getAlertType());
        // po.setThreshold(history.getThreshold());
        po.setCurrentValue(history.getCurrentValue());
        po.setTriggeredAt(history.getTriggeredAt());
        po.setTriggerReason(history.getTriggerReason());
        po.setCreatedAt(history.getCreatedAt());
        po.setUpdatedAt(history.getUpdatedAt());

        return po;
    }
}