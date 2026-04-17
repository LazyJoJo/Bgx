package com.stock.fund.infrastructure.mapper.subscription;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.stock.fund.infrastructure.entity.subscription.UserSubscriptionPO;

/**
 * 用户订阅 Mapper
 */
@Mapper
@Repository
public interface UserSubscriptionMapper extends BaseMapper<UserSubscriptionPO> {

    /**
     * 根据用户ID查询
     */
    List<UserSubscriptionPO> findByUserId(@Param("userId") Long userId);

    /**
     * 根据用户ID和激活状态查询
     */
    List<UserSubscriptionPO> findByUserIdAndActive(@Param("userId") Long userId, @Param("active") Boolean active);

    /**
     * 查询所有激活的订阅
     */
    List<UserSubscriptionPO> findActiveSubscriptions();

    /**
     * 根据用户ID、标的代码、标的类型查询（检查重复）
     */
    UserSubscriptionPO findByUserIdAndSymbolAndSymbolType(@Param("userId") Long userId, @Param("symbol") String symbol,
            @Param("symbolType") String symbolType);

    /**
     * 批量查询用户已存在的标的
     *
     * @param userId     用户ID
     * @param symbols    标的代码列表
     * @param symbolType 标的类型
     * @return 已存在的订阅列表
     */
    List<UserSubscriptionPO> findByUserIdAndSymbolsAndSymbolType(@Param("userId") Long userId,
            @Param("symbols") List<String> symbols, @Param("symbolType") String symbolType);

    /**
     * 分页查询用户订阅
     */
    List<UserSubscriptionPO> findByUserIdWithPage(@Param("userId") Long userId, @Param("symbol") String symbol,
            @Param("symbolType") String symbolType, @Param("status") String status, @Param("page") int page,
            @Param("size") int size, @Param("sort") String sort);

    /**
     * 统计用户订阅数量
     */
    long countByUserId(@Param("userId") Long userId, @Param("symbol") String symbol,
            @Param("symbolType") String symbolType, @Param("status") String status);

    /**
     * 批量更新激活状态
     */
    void batchUpdateActive(@Param("ids") List<Long> ids, @Param("active") Boolean active);

    /**
     * 批量插入订阅
     */
    int insertBatch(@Param("list") List<UserSubscriptionPO> list);
}
