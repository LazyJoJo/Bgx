-- 更新基金净值表结构，将quote_time拆分为日期和时间字段
-- 注意：如果字段已存在则跳过，否则添加它们

-- 检查并添加日期和时间字段
DO $$ 
BEGIN
    -- 添加quote_date字段（如果不存在）
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                   WHERE table_name='fund_quote' AND column_name='quote_date') THEN
        ALTER TABLE fund_quote ADD COLUMN quote_date DATE; -- 报价日期
        COMMENT ON COLUMN fund_quote.quote_date IS '报价日期';
    END IF;
    
    -- 添加quote_time_only字段（如果不存在）
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                   WHERE table_name='fund_quote' AND column_name='quote_time_only') THEN
        ALTER TABLE fund_quote ADD COLUMN quote_time_only TIME; -- 报价时间(仅时分秒)
        COMMENT ON COLUMN fund_quote.quote_time_only IS '报价时间(时分秒)';
    END IF;
END $$;

-- 使用现有quote_time字段的数据填充新字段
UPDATE fund_quote SET quote_date = quote_time::date, quote_time_only = quote_time::time
WHERE quote_date IS NULL OR quote_time_only IS NULL;

-- 删除原有的quote_time索引
DROP INDEX IF EXISTS idx_fund_quote_quote_time;

-- 创建新的复合索引用于按基金代码和日期查找
CREATE INDEX IF NOT EXISTS idx_fund_quote_fund_code_date ON fund_quote(fund_code, quote_date);

-- 添加唯一约束以确保同一基金在同一天只有一条记录
-- 注意：这一步可能会因为现有数据冲突而失败，如有必要可以先清理重复数据
-- ALTER TABLE fund_quote ADD CONSTRAINT uk_fund_code_date UNIQUE (fund_code, quote_date);