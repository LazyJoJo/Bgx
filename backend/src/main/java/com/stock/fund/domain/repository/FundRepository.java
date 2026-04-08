package com.stock.fund.domain.repository;

import com.stock.fund.domain.entity.Fund;
import java.util.List;
import java.util.Optional;

/**
 * 基金仓储接口
 */
public interface FundRepository {
    Optional<Fund> findByFundCode(String fundCode);
    List<Fund> findAll();
    Fund save(Fund fund);
    void deleteById(Long id);
    List<Fund> findByType(String type);
    List<Fund> findByManager(String manager);
}