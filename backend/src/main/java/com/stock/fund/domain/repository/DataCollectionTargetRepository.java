package com.stock.fund.domain.repository;

import com.stock.fund.domain.entity.DataCollectionTarget;
import java.util.List;
import java.util.Optional;

/**
 * 数据采集目标仓储接口
 */
public interface DataCollectionTargetRepository {
    Optional<DataCollectionTarget> findById(Long id);
    Optional<DataCollectionTarget> findByCode(String code);
    List<DataCollectionTarget> findByType(String type);
    List<DataCollectionTarget> findByActive(Boolean active);
    List<DataCollectionTarget> findByTypeAndActive(String type, Boolean active);
    List<DataCollectionTarget> findByCategory(String category);
    List<DataCollectionTarget> findActiveTargets();
    List<DataCollectionTarget> findTargetsNeedingCollection();
    DataCollectionTarget save(DataCollectionTarget target);
    List<DataCollectionTarget> saveAll(List<DataCollectionTarget> targets);
    void deleteById(Long id);
    void deleteByCode(String code);
    long count();
    long countByType(String type);
    long countByActive(Boolean active);
}