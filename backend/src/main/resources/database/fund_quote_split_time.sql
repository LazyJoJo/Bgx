-- 修改基金净值表，将quote_time拆分为日期和时间字段
-- 添加新的date和time字段
ALTER TABLE fund_quote ADD COLUMN quote_date DATE; -- 报价日期
ALTER TABLE fund_quote ADD COLUMN quote_time_only TIME; -- 报价时间(仅时分秒)

-- 使用现有quote_time字段的数据填充新字段
UPDATE fund_quote SET quote_date = quote_time::date, quote_time_only = quote_time::time;

-- 删除原有的quote_time索引
DROP INDEX IF EXISTS idx_fund_quote_quote_time;

-- 创建新的索引
CREATE INDEX IF NOT EXISTS idx_fund_quote_fund_code_date ON fund_quote(fund_code, quote_date);

-- 可选择性地保留原quote_time字段作为兼容性考虑，或删除它
-- ALTER TABLE fund_quote DROP COLUMN quote_time; 

-- 添加字段注释
COMMENT ON COLUMN fund_quote.quote_date IS '报价日期';
COMMENT ON COLUMN fund_quote.quote_time_only IS '报价时间(时分秒)';