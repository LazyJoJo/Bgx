package com.stock.fund.domain.entity;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 聚合根基类
 */
@Data
public abstract class AggregateRoot<ID> {
    protected ID id;
    protected LocalDateTime createdAt;
    protected LocalDateTime updatedAt;

}
