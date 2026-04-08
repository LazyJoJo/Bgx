package com.stock.fund.domain.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import java.time.LocalDateTime;

/**
 * 数据采集目标实体
 * 用于管理需要采集的股票或基金
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class DataCollectionTarget extends AggregateRoot<Long> {
    private String code;                    //代码或基金代码
    private String name;                    //股名称或基金名称
    private String type;                    // 类型：STOCK(股票) 或 FUND(基金)
    private String market;                  //市：SH(上海)、SZ(深圳)、HK(港股)等
    private Boolean active;                  // 是否激活采集
    private String category;                // 分类标签
    private String description;             //描述信息
    private LocalDateTime lastCollectedTime; // 最后采集时间
    private LocalDateTime nextCollectionTime; // 下次采集时间
    private Integer collectionFrequency;    // 采集频率(分钟)
    private String dataSource;              // 数据源配置

    //构造函数
    public DataCollectionTarget() {}

    public DataCollectionTarget(String code, String name, String type) {
        this.code = code;
        this.name = name;
        this.type = type;
        this.active = true;
        this.collectionFrequency = 15; // 默认15分钟采集一次
    }

    /**
     *采集目标
     */
    public void activate() {
        this.active = true;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     *停采集目标
     */
    public void deactivate() {
        this.active = false;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 更新采集时间
     */
    public void updateCollectionTime() {
        this.lastCollectedTime = LocalDateTime.now();
        // 处理collectionFrequency为null的情况
        int frequency = (collectionFrequency != null) ? collectionFrequency : 15;
        this.nextCollectionTime = LocalDateTime.now().plusMinutes(frequency);
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 是否需要采集
     */
    public boolean shouldCollect() {
        return active && 
               (nextCollectionTime == null || LocalDateTime.now().isAfter(nextCollectionTime));
    }

    /**
     * 获取完整代码(带市场前缀)
     */
    public String getFullCode() {
        if (market != null && !market.isEmpty()) {
            return market + "." + code;
        }
        return code;
    }

    /**
     * 设置完整代码
     */
    public void setFullCode(String fullCode) {
        if (fullCode != null && fullCode.contains(".")) {
            String[] parts = fullCode.split("\\.", 2);
            this.market = parts[0];
            this.code = parts[1];
        } else {
            this.code = fullCode;
        }
    }
}