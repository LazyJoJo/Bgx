package com.stock.fund.infrastructure.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 数据采集目标PO实体
 * 对应数据库表 data_collection_target
 */
@TableName("data_collection_target")
@Data
public class DataCollectionTargetPO {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String code;                    //代码或基金代码
    private String name;                    //股名称或基金名称
    private String type;                    // 类型：STOCK 或 FUND
    private String market;                  //市
    private Boolean active;                  // 是否激活
    private String category;                // 分类标签
    private String description;             //描述
    private LocalDateTime last_collected_time; // 最后采集时间
    private LocalDateTime next_collection_time; // 下次采集时间
    private Integer collection_frequency;    // 采集频率(分钟)
    private String data_source;             // 数据源
    private LocalDateTime created_at;
    private LocalDateTime updated_at;
    
    //构造函数
    public DataCollectionTargetPO() {}
    
    public DataCollectionTargetPO(String code, String name, String type) {
        this.code = code;
        this.name = name;
        this.type = type;
        this.active = true;
        this.collection_frequency = 15;
        this.created_at = LocalDateTime.now();
        this.updated_at = LocalDateTime.now();
    }
}