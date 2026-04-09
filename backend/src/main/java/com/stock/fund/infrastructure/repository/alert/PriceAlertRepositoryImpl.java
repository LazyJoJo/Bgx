package com.stock.fund.infrastructure.repository.alert;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.stock.fund.domain.entity.alert.PriceAlert;
import com.stock.fund.domain.repository.alert.PriceAlertQuery;
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

    @Override
    public Optional<PriceAlert> findByUserIdAndSymbolAndSymbolType(Long userId, String symbol, String symbolType) {
        PriceAlertPO po = priceAlertMapper.findByUserIdAndSymbolAndSymbolType(userId, symbol, symbolType);
        return po != null ? Optional.of(mapToDomainEntity(po)) : Optional.empty();
    }

    @Override
    public List<PriceAlert> findByUserIdWithPage(PriceAlertQuery query) {
        List<PriceAlertPO> pos = priceAlertMapper.findByUserIdWithPage(
                query.getUserId(), query.getSymbol(), query.getSymbolType(),
                query.getAlertType(), query.getStatus(),
                query.getPage(), query.getSize(), query.getSort());
        return pos.stream().map(this::mapToDomainEntity).collect(Collectors.toList());
    }

    @Override
    public long countByUserId(PriceAlertQuery query) {
        return priceAlertMapper.countByUserId(
                query.getUserId(), query.getSymbol(), query.getSymbolType(),
                query.getAlertType(), query.getStatus());
    }

    // 将PO转换为领域实体
    private PriceAlert mapToDomainEntity(PriceAlertPO po) {
        PriceAlert alert = new PriceAlert();
        alert.setId(po.getId());
        alert.setUserId(po.getUserId());
        alert.setSymbol(po.getSymbol());
        alert.setSymbolType(po.getSymbolType());
        alert.setSymbolName(po.getSymbolName());
        alert.setAlertType(po.getAlertType());
        alert.setTargetPrice(po.getTargetPrice());
        alert.setTargetChangePercent(po.getTargetChangePercent());
        alert.setBasePrice(po.getBasePrice());
        alert.setCurrentValue(po.getCurrentValue());
        alert.setStatus(po.getStatus());
        alert.setLastTriggered(po.getLastTriggered());
        alert.setDescription(po.getDescription());
        alert.setCreatedAt(po.getCreatedAt());
        alert.setUpdatedAt(po.getUpdatedAt());
        return alert;
    }

    // 将领域实体转换为PO
    private PriceAlertPO mapToPO(PriceAlert alert) {
        PriceAlertPO po = new PriceAlertPO();
        po.setId(alert.getId());
        po.setUserId(alert.getUserId());
        po.setSymbol(alert.getSymbol());
        po.setSymbolType(alert.getSymbolType());
        po.setSymbolName(alert.getSymbolName());
        po.setAlertType(alert.getAlertType());
        po.setTargetPrice(alert.getTargetPrice());
        po.setTargetChangePercent(alert.getTargetChangePercent());
        po.setBasePrice(alert.getBasePrice());
        po.setCurrentValue(alert.getCurrentValue());
        po.setStatus(alert.getStatus());
        po.setLastTriggered(alert.getLastTriggered());
        po.setDescription(alert.getDescription());
        po.setCreatedAt(alert.getCreatedAt());
        po.setUpdatedAt(alert.getUpdatedAt());
        return po;
    }
}
