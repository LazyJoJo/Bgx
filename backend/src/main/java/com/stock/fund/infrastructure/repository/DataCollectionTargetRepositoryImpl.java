package com.stock.fund.infrastructure.repository;

import com.stock.fund.domain.entity.DataCollectionTarget;
import com.stock.fund.domain.repository.DataCollectionTargetRepository;
import com.stock.fund.infrastructure.entity.DataCollectionTargetPO;
import com.stock.fund.infrastructure.mapper.DataCollectionTargetMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
public class DataCollectionTargetRepositoryImpl implements DataCollectionTargetRepository {

    @Autowired
    private DataCollectionTargetMapper dataCollectionTargetMapper;

    @Override
    public Optional<DataCollectionTarget> findById(Long id) {
        DataCollectionTargetPO po = dataCollectionTargetMapper.selectById(id);
        return po != null ? Optional.of(mapToDomainEntity(po)) : Optional.empty();
    }

    @Override
    public Optional<DataCollectionTarget> findByCode(String code) {
        DataCollectionTargetPO po = dataCollectionTargetMapper.findByCode(code);
        return po != null ? Optional.of(mapToDomainEntity(po)) : Optional.empty();
    }

    @Override
    public List<DataCollectionTarget> findByType(String type) {
        List<DataCollectionTargetPO> pos = dataCollectionTargetMapper.findByType(type);
        return pos.stream().map(this::mapToDomainEntity).collect(Collectors.toList());
    }

    @Override
    public List<DataCollectionTarget> findByActive(Boolean active) {
        List<DataCollectionTargetPO> pos = dataCollectionTargetMapper.findByActive(active);
        return pos.stream().map(this::mapToDomainEntity).collect(Collectors.toList());
    }

    @Override
    public List<DataCollectionTarget> findByTypeAndActive(String type, Boolean active) {
        List<DataCollectionTargetPO> pos = dataCollectionTargetMapper.findByTypeAndActive(type, active);
        return pos.stream().map(this::mapToDomainEntity).collect(Collectors.toList());
    }

    @Override
    public List<DataCollectionTarget> findByCategory(String category) {
        List<DataCollectionTargetPO> pos = dataCollectionTargetMapper.findByCategory(category);
        return pos.stream().map(this::mapToDomainEntity).collect(Collectors.toList());
    }

    @Override
    public List<DataCollectionTarget> findActiveTargets() {
        List<DataCollectionTargetPO> pos = dataCollectionTargetMapper.findActiveTargets();
        return pos.stream().map(this::mapToDomainEntity).collect(Collectors.toList());
    }

    @Override
    public List<DataCollectionTarget> findTargetsNeedingCollection() {
        List<DataCollectionTargetPO> pos = dataCollectionTargetMapper.findTargetsNeedingCollection();
        return pos.stream().map(this::mapToDomainEntity).collect(Collectors.toList());
    }

    @Override
    public List<DataCollectionTarget> search(String keyword) {
        List<DataCollectionTargetPO> pos = dataCollectionTargetMapper.search(keyword);
        return pos.stream().map(this::mapToDomainEntity).collect(Collectors.toList());
    }

    @Override
    public List<DataCollectionTarget> searchByType(String type, String keyword) {
        List<DataCollectionTargetPO> pos = dataCollectionTargetMapper.searchByType(type, keyword);
        return pos.stream().map(this::mapToDomainEntity).collect(Collectors.toList());
    }

    @Override
    public DataCollectionTarget save(DataCollectionTarget target) {
        DataCollectionTargetPO po = mapToPO(target);
        if (target.getId() == null) {
            dataCollectionTargetMapper.insert(po);
            target.setId(po.getId());
        } else {
            dataCollectionTargetMapper.updateById(po);
        }
        return target;
    }

    @Override
    public List<DataCollectionTarget> saveAll(List<DataCollectionTarget> targets) {
        for (DataCollectionTarget target : targets) {
            save(target);
        }
        return targets;
    }

    @Override
    public void deleteById(Long id) {
        dataCollectionTargetMapper.deleteById(id);
    }

    @Override
    public void deleteByCode(String code) {
        dataCollectionTargetMapper.deleteByCode(code);
    }

    @Override
    public long count() {
        return dataCollectionTargetMapper.selectCount(null);
    }

    @Override
    public long countByType(String type) {
        return dataCollectionTargetMapper.selectCount(
            new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<DataCollectionTargetPO>()
                .eq("type", type)
        );
    }

    @Override
    public long countByActive(Boolean active) {
        return dataCollectionTargetMapper.selectCount(
            new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<DataCollectionTargetPO>()
                .eq("active", active)
        );
    }

    //私有方法：将PO转换为领域实体
    private DataCollectionTarget mapToDomainEntity(DataCollectionTargetPO po) {
        DataCollectionTarget target = new DataCollectionTarget();
        target.setId(po.getId());
        target.setCode(po.getCode());
        target.setName(po.getName());
        target.setType(po.getType());
        target.setMarket(po.getMarket());
        target.setActive(po.getActive());
        target.setCategory(po.getCategory());
        target.setDescription(po.getDescription());
        target.setLastCollectedTime(po.getLast_collected_time());
        target.setNextCollectionTime(po.getNext_collection_time());
        target.setCollectionFrequency(po.getCollection_frequency());
        target.setDataSource(po.getData_source());
        target.setCreatedAt(po.getCreated_at());
        target.setUpdatedAt(po.getUpdated_at());
        return target;
    }

    //私有方法：将领域实体转换为PO
    private DataCollectionTargetPO mapToPO(DataCollectionTarget target) {
        DataCollectionTargetPO po = new DataCollectionTargetPO();
        po.setId(target.getId());
        po.setCode(target.getCode());
        po.setName(target.getName());
        po.setType(target.getType());
        po.setMarket(target.getMarket());
        po.setActive(target.getActive());
        po.setCategory(target.getCategory());
        po.setDescription(target.getDescription());
        po.setLast_collected_time(target.getLastCollectedTime());
        po.setNext_collection_time(target.getNextCollectionTime());
        po.setCollection_frequency(target.getCollectionFrequency());
        po.setData_source(target.getDataSource());
        po.setCreated_at(target.getCreatedAt());
        po.setUpdated_at(target.getUpdatedAt());
        return po;
    }
}