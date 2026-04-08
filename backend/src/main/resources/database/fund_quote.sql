-- 基金净值表
CREATE TABLE IF NOT EXISTS fund_quote (
    id BIGSERIAL PRIMARY KEY,
    fund_code VARCHAR(20) NOT NULL,          -- 基金代码
    fund_name VARCHAR(100),                  -- 基金名称
    quote_time TIMESTAMP,                    -- 报价时间
    nav DECIMAL(10,4),                       -- 净值
    prev_net_value DECIMAL(10,4),            -- 昨日净值
    change_amount DECIMAL(10,4),             -- 涨跌额
    change_percent DECIMAL(5,2),             -- 涨跌幅(%)
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 创建索引
CREATE INDEX IF NOT EXISTS idx_fund_quote_fund_code ON fund_quote(fund_code);
CREATE INDEX IF NOT EXISTS idx_fund_quote_quote_time ON fund_quote(quote_time);
CREATE INDEX IF NOT EXISTS idx_fund_quote_created_at ON fund_quote(created_at);

-- 添加注释
COMMENT ON TABLE fund_quote IS '基金净值表';
COMMENT ON COLUMN fund_quote.fund_code IS '基金代码';
COMMENT ON COLUMN fund_quote.fund_name IS '基金名称';
COMMENT ON COLUMN fund_quote.quote_time IS '报价时间';
COMMENT ON COLUMN fund_quote.nav IS '单位净值';
COMMENT ON COLUMN fund_quote.prev_net_value IS '昨日净值';
COMMENT ON COLUMN fund_quote.change_amount IS '涨跌额';
COMMENT ON COLUMN fund_quote.change_percent IS '涨跌幅(%)';
COMMENT ON COLUMN fund_quote.created_at IS '创建时间';
COMMENT ON COLUMN fund_quote.updated_at IS '更新时间';

-- 更新时间戳触发器函数
CREATE OR REPLACE FUNCTION update_fund_quote_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

-- 创建更新时间戳触发器
DROP TRIGGER IF EXISTS update_fund_quote_updated_at ON fund_quote;
CREATE TRIGGER update_fund_quote_updated_at
    BEFORE UPDATE ON fund_quote
    FOR EACH ROW
    EXECUTE FUNCTION update_fund_quote_updated_at();