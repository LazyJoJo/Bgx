package com.stock.fund.infrastructure.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.stock.fund.infrastructure.entity.DataCollectionTargetPO;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface DataCollectionTargetMapper extends BaseMapper<DataCollectionTargetPO> {
    DataCollectionTargetPO findByCode(@Param("code") String code);
    
    List<DataCollectionTargetPO> findByType(@Param("type") String type);
    
    List<DataCollectionTargetPO> findByActive(@Param("active") Boolean active);
    
    List<DataCollectionTargetPO> findByTypeAndActive(
        @Param("type") String type, 
        @Param("active") Boolean active
    );
    
    List<DataCollectionTargetPO> findByCategory(@Param("category") String category);
    
    List<DataCollectionTargetPO> findActiveTargets();
    
    List<DataCollectionTargetPO> findTargetsNeedingCollection();
    
    void deleteByCode(@Param("code") String code);
}