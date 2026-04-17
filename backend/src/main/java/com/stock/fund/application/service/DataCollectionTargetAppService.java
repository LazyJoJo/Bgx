package com.stock.fund.application.service;

import java.util.List;

import com.stock.fund.domain.entity.DataCollectionTarget;

/**
 * 数据采集目标应用服务接口
 */
public interface DataCollectionTargetAppService {
    DataCollectionTarget createTargetByCode(String code);

    DataCollectionTarget updateTarget(Long id, DataCollectionTarget target);

    DataCollectionTarget updateTargetByCode(String code, DataCollectionTarget target);

    void deleteTarget(Long id);

    void deleteTargetByCode(String code);

    DataCollectionTarget getTargetById(Long id);

    DataCollectionTarget getTargetByCode(String code);

    List<DataCollectionTarget> getAllTargets();

    List<DataCollectionTarget> getTargetsByType(String type);

    List<DataCollectionTarget> getActiveTargets();

    List<DataCollectionTarget> getTargetsByCategory(String category);

    List<DataCollectionTarget> getTargetsNeedingCollection();

    List<DataCollectionTarget> searchTargets(String type, String keyword);

    void activateTarget(Long id);

    void deactivateTarget(Long id);

    void activateTargetByCode(String code);

    void deactivateTargetByCode(String code);

    long getTargetCount();

    long getTargetCountByType(String type);

    long getActiveTargetCount();
}