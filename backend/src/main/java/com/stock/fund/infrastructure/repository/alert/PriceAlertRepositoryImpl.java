package com.stock.fund.infrastructure.repository.alert;

import java.math.BigDecimal;
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
        // BigDecimal -> Double conversion
        alert.setTargetPrice(po.getTargetPrice() != null ? po.getTargetPrice().doubleValue() : null);
        alert.setTargetChangePercent(po.getTargetChangePercent() != null ? po.getTargetChangePercent().doubleValue() : null);
        alert.setBasePrice(po.getBasePrice() != null ? po.getBasePrice().doubleValue() : null);
        alert.setCurrentValue(po.getCurrentValue() != null ? po.getCurrentValue().doubleValue() : null);
        // is_active (Boolean) -> status (String)
        alert.setStatus(po.getIsActive() != null && po.getIsActive() ? "ACTIVE" : "INACTIVE");
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
        // Double -> BigDecimal conversion
        po.setTargetPrice(alert.getTargetPrice() != null ? BigDecimal.valueOf(alert.getTargetPrice()) : null);
        po.setTargetChangePercent(alert.getTargetChangePercent() != null ? BigDecimal.valueOf(alert.getTargetChangePercent()) : null);
        po.setBasePrice(alert.getBasePrice() != null ? BigDecimal.valueOf(alert.getBasePrice()) : null);
        po.setCurrentValue(alert.getCurrentValue() != null ? BigDecimal.valueOf(alert.getCurrentValue()) : null);
        // status (String) -> is_active (Boolean)
        po.setIsActive("ACTIVE".equals(alert.getStatus()) || "TRIGGERED".equals(alert.getStatus()));
        po.setLastTriggered(alert.getLastTriggered());
        po.setDescription(alert.getDescription());
        po.setCreatedAt(alert.getCreatedAt());
        po.setUpdatedAt(alert.getUpdatedAt());
        return po;
    }
}
