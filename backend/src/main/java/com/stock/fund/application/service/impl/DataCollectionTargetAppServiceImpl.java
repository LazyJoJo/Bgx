package com.stock.fund.application.service.impl;

import com.stock.fund.application.service.DataCollectionTargetAppService;
import com.stock.fund.domain.entity.DataCollectionTarget;
import com.stock.fund.domain.repository.DataCollectionTargetRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@Transactional
public class DataCollectionTargetAppServiceImpl implements DataCollectionTargetAppService {

    @Autowired
    private DataCollectionTargetRepository dataCollectionTargetRepository;

    @Override
    public DataCollectionTarget createTarget(DataCollectionTarget target) {
        //验证代码是否已存在
        if (dataCollectionTargetRepository.findByCode(target.getCode()).isPresent()) {
            throw new IllegalArgumentException("采集目标代码已存在: " + target.getCode());
        }
        return dataCollectionTargetRepository.save(target);
    }

    @Override
    public DataCollectionTarget updateTarget(Long id, DataCollectionTarget target) {
        DataCollectionTarget existingTarget = dataCollectionTargetRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("采集目标不存在: " + id));
        
        // 更新字段
        existingTarget.setName(target.getName());
        existingTarget.setType(target.getType());
        existingTarget.setMarket(target.getMarket());
        existingTarget.setActive(target.getActive());
        existingTarget.setCategory(target.getCategory());
        existingTarget.setDescription(target.getDescription());
        existingTarget.setCollectionFrequency(target.getCollectionFrequency());
        existingTarget.setDataSource(target.getDataSource());
        
        return dataCollectionTargetRepository.save(existingTarget);
    }

    @Override
    public DataCollectionTarget updateTargetByCode(String code, DataCollectionTarget target) {
        DataCollectionTarget existingTarget = dataCollectionTargetRepository.findByCode(code)
            .orElseThrow(() -> new IllegalArgumentException("采集目标代码不存在: " + code));
        
        return updateTarget(existingTarget.getId(), target);
    }

    @Override
    public void deleteTarget(Long id) {
        dataCollectionTargetRepository.deleteById(id);
    }

    @Override
    public void deleteTargetByCode(String code) {
        dataCollectionTargetRepository.deleteByCode(code);
    }

    @Override
    public DataCollectionTarget getTargetById(Long id) {
        return dataCollectionTargetRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("采集目标不存在: " + id));
    }

    @Override
    public DataCollectionTarget getTargetByCode(String code) {
        return dataCollectionTargetRepository.findByCode(code)
            .orElseThrow(() -> new IllegalArgumentException("采集目标代码不存在: " + code));
    }

    @Override
    public List<DataCollectionTarget> getAllTargets() {
        return dataCollectionTargetRepository.findByActive(null); // null表示查询所有
    }

    @Override
    public List<DataCollectionTarget> getTargetsByType(String type) {
        return dataCollectionTargetRepository.findByType(type);
    }

    @Override
    public List<DataCollectionTarget> getActiveTargets() {
        return dataCollectionTargetRepository.findActiveTargets();
    }

    @Override
    public List<DataCollectionTarget> getTargetsByCategory(String category) {
        return dataCollectionTargetRepository.findByCategory(category);
    }

    @Override
    public List<DataCollectionTarget> getTargetsNeedingCollection() {
        return dataCollectionTargetRepository.findTargetsNeedingCollection();
    }

    @Override
    public void activateTarget(Long id) {
        DataCollectionTarget target = getTargetById(id);
        target.activate();
        dataCollectionTargetRepository.save(target);
    }

    @Override
    public void deactivateTarget(Long id) {
        DataCollectionTarget target = getTargetById(id);
        target.deactivate();
        dataCollectionTargetRepository.save(target);
    }

    @Override
    public void activateTargetByCode(String code) {
        DataCollectionTarget target = getTargetByCode(code);
        target.activate();
        dataCollectionTargetRepository.save(target);
    }

    @Override
    public void deactivateTargetByCode(String code) {
        DataCollectionTarget target = getTargetByCode(code);
        target.deactivate();
        dataCollectionTargetRepository.save(target);
    }

    @Override
    public long getTargetCount() {
        return dataCollectionTargetRepository.count();
    }

    @Override
    public long getTargetCountByType(String type) {
        return dataCollectionTargetRepository.countByType(type);
    }

    @Override
    public long getActiveTargetCount() {
        return dataCollectionTargetRepository.countByActive(true);
    }
}