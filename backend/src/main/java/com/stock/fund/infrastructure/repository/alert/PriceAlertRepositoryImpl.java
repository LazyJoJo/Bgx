package com.stock.fund.infrastructure.repository.alert;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.stock.fund.domain.entity.alert.PriceAlert;
import com.stock.fund.domain.repository.alert.PriceAlertRepository;
import com.stock.fund.infrastructure.entity.alert.PriceAlertPO;
import com.stock.fund.infrastructure.mapper.alert.PriceAlertMapper;

@Repository
public class PriceAlertRepositoryImpl implements PriceAlertRepository {

    @Autowired
    private PriceAlertMapper priceAlertMapper;

    @Override
    public Optional<PriceAlert> findById(Long id) {
        PriceAlertPO po = priceAlertMapper.selectById(id);
        return po != null ? Optional.of(mapToDomainEntity(po)) : Optional.empty();
    }

    @Override
    public List<PriceAlert> findByUserId(Long userId) {
        List<PriceAlertPO> pos = priceAlertMapper.findByUserId(userId);
        return pos.stream().map(this::mapToDomainEntity).collect(Collectors.toList());
    }

    @Override
    public List<PriceAlert> findByUserIdAndActive(Long userId, Boolean active) {
        List<PriceAlertPO> pos = priceAlertMapper.findByUserIdAndActive(userId, active);
        return pos.stream().map(this::mapToDomainEntity).collect(Collectors.toList());
    }

    @Override
    public List<PriceAlert> findActiveAlerts() {
        List<PriceAlertPO> pos = priceAlertMapper.findActiveAlerts();
        return pos.stream().map(this::mapToDomainEntity).collect(Collectors.toList());
    }

    @Override
    public PriceAlert save(PriceAlert alert) {
        PriceAlertPO po = mapToPO(alert);
        if (alert.getId() == null) {
            priceAlertMapper.insert(po);
            alert.setId(po.getId());
        } else {
            priceAlertMapper.updateById(po);
        }
        return alert;
    }

    @Override
    public void deleteById(Long id) {
        priceAlertMapper.deleteById(id);
    }

    @Override
    public List<PriceAlert> saveAll(List<PriceAlert> alerts) {
        for (PriceAlert alert : alerts) {
            save(alert);
        }
        return alerts;
    }

    // 私方法：将PO转换为领域实体
    private PriceAlert mapToDomainEntity(PriceAlertPO po) {
        PriceAlert alert = new PriceAlert();
        alert.setId(po.getId());
        alert.setUserId(po.getUserId());
        // alert.setEntityCode(po.getEntityCode());
        // alert.setEntityType(po.getEntityType());
        // alert.setEntityName(po.getEntityName());
        // alert.setAlertType(po.getAlertType());
        // alert.setThreshold(po.getThreshold());
        // alert.setCurrentValue(po.getCurrentValue());
        // alert.setIsActive(po.getIsActive());
        alert.setLastTriggered(po.getLastTriggered());
        alert.setDescription(po.getDescription());
        alert.setCreatedAt(po.getCreatedAt());
        alert.setUpdatedAt(po.getUpdatedAt());
        return alert;
    }

    // 私有方法：将领域实体转换为PO
    private PriceAlertPO mapToPO(PriceAlert alert) {
        PriceAlertPO po = new PriceAlertPO();
        po.setId(alert.getId());
        po.setUserId(alert.getUserId());
        // po.setEntityCode(alert.getEntityCode());
        // po.setEntityType(alert.getEntityType());
        // po.setEntityName(alert.getEntityName());
        // po.setAlertType(alert.getAlertType());
        // po.setThreshold(alert.getThreshold());
        // po.setCurrentValue(alert.getCurrentValue());
        // po.setIsActive(alert.getIsActive());
        po.setLastTriggered(alert.getLastTriggered());
        po.setDescription(alert.getDescription());
        po.setCreatedAt(alert.getCreatedAt());
        po.setUpdatedAt(alert.getUpdatedAt());
        return po;
    }
}