-- 数据采集目标表
CREATE TABLE IF NOT EXISTS data_collection_target (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(20) NOT NULL UNIQUE,           --代码或基金代码
    name VARCHAR(100) NOT NULL,                --名称或基金名称
    type VARCHAR(10) NOT NULL,                 -- 类型：STOCK(股票) 或 FUND(基金)
    market VARCHAR(10),                        --市：SH(上海)、SZ(深圳)、HK(港股)等
    active BOOLEAN DEFAULT TRUE,               -- 是否激活采集
    category VARCHAR(50),                      -- 分类标签
    description TEXT,                          --描述信息
    last_collected_time TIMESTAMP,             -- 最后采集时间
    next_collection_time TIMESTAMP,            -- 下次采集时间
    collection_frequency INTEGER DEFAULT 15,    -- 采集频率(分钟)
    data_source VARCHAR(50) DEFAULT 'TUSHARE',   -- 数据源配置
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 创建索引
CREATE INDEX IF NOT EXISTS idx_data_collection_target_type ON data_collection_target(type);
CREATE INDEX IF NOT EXISTS idx_data_collection_target_active ON data_collection_target(active);
CREATE INDEX IF NOT EXISTS idx_data_collection_target_category ON data_collection_target(category);
CREATE INDEX IF NOT EXISTS idx_data_collection_target_next_collection ON data_collection_target(next_collection_time);
CREATE INDEX IF NOT EXISTS idx_data_collection_target_code ON data_collection_target(code);

-- 添加注释
COMMENT ON TABLE data_collection_target IS '数据采集目标表';
COMMENT ON COLUMN data_collection_target.code IS '股票代码或基金代码';
COMMENT ON COLUMN data_collection_target.name IS '股票名称或基金名称';
COMMENT ON COLUMN data_collection_target.type IS '类型：STOCK(股票) 或 FUND(基金)';
COMMENT ON COLUMN data_collection_target.market IS '市场：SH(上海)、SZ(深圳)、HK(港股)等';
COMMENT ON COLUMN data_collection_target.active IS '是否激活采集';
COMMENT ON COLUMN data_collection_target.category IS '分类标签';
COMMENT ON COLUMN data_collection_target.description IS '描述信息';
COMMENT ON COLUMN data_collection_target.last_collected_time IS '最后采集时间';
COMMENT ON COLUMN data_collection_target.next_collection_time IS '下次采集时间';
COMMENT ON COLUMN data_collection_target.collection_frequency IS '采集频率(分钟)';
COMMENT ON COLUMN data_collection_target.data_source IS '数据源配置';

--插入示例数据
INSERT INTO data_collection_target (code, name, type, market, category, description)
SELECT '600000', '浦发银行', 'STOCK', 'SH', '银行', '上海浦东发展银行股份有限公司'
WHERE NOT EXISTS (SELECT 1 FROM data_collection_target WHERE code = '600000')
UNION ALL
SELECT '600519', '贵州茅台', 'STOCK', 'SH', '白酒', '贵州茅台酒股份有限公司'
WHERE NOT EXISTS (SELECT 1 FROM data_collection_target WHERE code = '600519')
UNION ALL
SELECT '000001', '平安银行', 'STOCK', 'SZ', '银行', '平安银行股份有限公司'
WHERE NOT EXISTS (SELECT 1 FROM data_collection_target WHERE code = '000001')
UNION ALL
SELECT '000002', '华夏成长', 'FUND', NULL, '混合型', '华夏成长证券投资基金'
WHERE NOT EXISTS (SELECT 1 FROM data_collection_target WHERE code = '000002')
UNION ALL
SELECT '110011', '易方达中小盘', 'FUND', NULL, '股票型', '易方达中小盘混合型证券投资基金'
WHERE NOT EXISTS (SELECT 1 FROM data_collection_target WHERE code = '110011');

-- 更新时间戳触发器函数
CREATE OR REPLACE FUNCTION update_data_collection_target_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

-- 创建更新时间戳触发器
DROP TRIGGER IF EXISTS update_data_collection_target_updated_at ON data_collection_target;
CREATE TRIGGER update_data_collection_target_updated_at
    BEFORE UPDATE ON data_collection_target
    FOR EACH ROW
    EXECUTE FUNCTION update_data_collection_target_updated_at();