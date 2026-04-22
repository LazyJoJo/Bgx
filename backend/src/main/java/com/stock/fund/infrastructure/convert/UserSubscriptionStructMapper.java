package com.stock.fund.infrastructure.convert;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

import com.stock.fund.domain.entity.subscription.UserSubscription;
import com.stock.fund.infrastructure.entity.subscription.UserSubscriptionPO;

/**
 * UserSubscription 实体与 PO 之间的 MapStruct 映射器
 * 
 * 使用 MapStruct 实现类型安全的对象映射，替代手写的 toPO/toEntity 转换。 命名使用 StructMapper 后缀以避免与
 * MyBatis-Plus 的 Mapper 接口冲突。
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface UserSubscriptionStructMapper {

    /**
     * 实体转 PO 注意：createdAt、updatedAt 由数据库自动管理，设置为 ignore 但 id 不应该被忽略 - 因为 update
     * 操作需要 id 字段 MapStruct 会自动复制 id 字段的值
     */
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    UserSubscriptionPO toPO(UserSubscription entity);

    /**
     * PO 转实体
     */
    UserSubscription toEntity(UserSubscriptionPO po);

    /**
     * PO列表转实体列表
     */
    List<UserSubscription> toEntityList(List<UserSubscriptionPO> pos);

    /**
     * 实体列表转 PO 列表
     */
    List<UserSubscriptionPO> toPOList(List<UserSubscription> entities);
}
