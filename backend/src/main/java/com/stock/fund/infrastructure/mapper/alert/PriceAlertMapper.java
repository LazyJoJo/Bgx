package com.stock.fund.infrastructure.mapper.alert;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.stock.fund.infrastructure.entity.alert.PriceAlertPO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

/**
 * 价格提醒 Mapper
 */
@Mapper
@Repository
public interface PriceAlertMapper extends BaseMapper<PriceAlertPO> {

    /**
     * 根据用户ID查询
     */
    List<PriceAlertPO> findByUserId(@Param("userId") Long userId);

    /**
     * 根据用户ID和激活状态查询
     */
    List<PriceAlertPO> findByUserIdAndActive(@Param("userId") Long userId, @Param("active") Boolean active);

    /**
     * 查询所有激活的提醒
     */
    List<PriceAlertPO> findActiveAlerts();

    /**
     * 根据用户ID、标的代码、标的类型查询（检查重复）
     */
    PriceAlertPO findByUserIdAndSymbolAndSymbolType(@Param("userId") Long userId,
                                                    @Param("symbol") String symbol,
                                                    @Param("symbolType") String symbolType);

    /**
     * 批量查询用户已存在的标的
     *
     * @param userId 用户ID
     * @param symbols 标的代码列表
     * @param symbolType 标的类型
     * @return 已存在的提醒列表
     */
    List<PriceAlertPO> findByUserIdAndSymbolsAndSymbolType(@Param("userId") Long userId,
                                                           @Param("symbols") List<String> symbols,
                                                           @Param("symbolType") String symbolType);

    /**
     * 批量查询用户已存在的提醒（包含提醒类型）
     *
     * @param userId 用户ID
     * @param symbols 标的代码列表
     * @param symbolType 标的类型
     * @param alertType 提醒类型
     * @return 已存在的提醒列表
     */
    List<PriceAlertPO> findByUserIdAndSymbolsAndSymbolTypeAndAlertType(@Param("userId") Long userId,
                                                                        @Param("symbols") List<String> symbols,
                                                                        @Param("symbolType") String symbolType,
                                                                        @Param("alertType") String alertType);

    /**
     * 分页查询用户提醒
     */
    List<PriceAlertPO> findByUserIdWithPage(@Param("userId") Long userId,
                                            @Param("symbol") String symbol,
                                            @Param("symbolType") String symbolType,
                                            @Param("alertType") String alertType,
                                            @Param("status") String status,
                                            @Param("page") int page,
                                            @Param("size") int size,
                                            @Param("sort") String sort);

    /**
     * 统计用户提醒数量
     */
    long countByUserId(@Param("userId") Long userId,
                       @Param("symbol") String symbol,
                       @Param("symbolType") String symbolType,
                       @Param("alertType") String alertType,
                       @Param("status") String status);
}
