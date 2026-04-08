-- PostgreSQL数据库表结构创建脚本 (最终版本)
-- 数据库名称: stock_fund_db
-- 项目:基金数据采集系统
-- 生成时间: 2026-03-18
--版本: 1.0

-- =====================================
-- 创建数据库（首次执行时取消注释）
-- =====================================
-- CREATE DATABASE stock_fund_db;

-- 使用数据库（在psql中执行时取消注释）
-- \c stock_fund_db;

-- =====================================
--基础数据表
-- =====================================

--基础信息表
CREATE TABLE IF NOT EXISTS stock_basic (
    id BIGSERIAL PRIMARY KEY,
    symbol VARCHAR(20) NOT NULL UNIQUE,        --代码
    name VARCHAR(100) NOT NULL,                --股名称
    industry VARCHAR(50),                      --所属行业
    market VARCHAR(20),                       -- 市场
    listing_date DATE,                        -- 上市日期
    total_share DECIMAL(20,2),               --总股本
    float_share DECIMAL(20,2),               --流通股本
    pe DECIMAL(10,2),                         --市率
    pb DECIMAL(10,2),                         --市率
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    deleted INTEGER DEFAULT 0                 -- 逻辑删除标记
);

--基金基础信息表
CREATE TABLE IF NOT EXISTS fund_basic (
    id BIGSERIAL PRIMARY KEY,
    fund_code VARCHAR(20) NOT NULL UNIQUE,     --基代码
    name VARCHAR(100) NOT NULL,                --基名称
    type VARCHAR(50),                          --基金类型
    manager VARCHAR(100),                     -- 基金经理
    establishment_date DATE,                  -- 成立日期
    fund_size DECIMAL(20,2),                  --基金规模
    nav DECIMAL(10,4),                        -- 最新净值
    day_growth DECIMAL(10,4),                 -- 日增长率
    week_growth DECIMAL(10,4),                --周
    month_growth DECIMAL(10,4),               -- 月增长率
    year_growth DECIMAL(10,4),                --年
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    deleted INTEGER DEFAULT 0                 -- 逻辑删除标记
);

-- =====================================
--行数据表
-- =====================================

--行情表
CREATE TABLE IF NOT EXISTS stock_quote (
    id BIGSERIAL PRIMARY KEY,
    stock_id BIGINT NOT NULL REFERENCES stock_basic(id),  --ID
    quote_time TIMESTAMP WITHOUT TIME ZONE NOT NULL,     --行时间
    open DECIMAL(10,2) NOT NULL,              -- 开盘价
    high DECIMAL(10,2) NOT NULL,              -- 最高价
    low DECIMAL(10,2) NOT NULL,               -- 最低价
    close DECIMAL(10,2) NOT NULL,             --收价
    volume BIGINT NOT NULL,                   -- 成交量
    amount DECIMAL(20,2),                     -- 成交额
    change DECIMAL(10,2),                     --额
    change_percent DECIMAL(10,4),             --
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(stock_id, quote_time)              --同一股票同一时间点数据唯一
);

--基行情表
CREATE TABLE IF NOT EXISTS fund_quote (
    id BIGSERIAL PRIMARY KEY,
    fund_id BIGINT NOT NULL REFERENCES fund_basic(id),   --基金ID
    quote_time TIMESTAMP WITHOUT TIME ZONE NOT NULL,     --净时间
    nav DECIMAL(10,4) NOT NULL,               --净
    acc_nav DECIMAL(10,4),                    --累净值
    change DECIMAL(10,4),                     -- 涨跌额
    change_percent DECIMAL(10,4),              --
跌幅
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(fund_id, quote_time)               --同一基金同一时间点数据唯一
);

-- =====================================
-- 用户提醒功能表
-- =====================================

-- 价格提醒规则表
CREATE TABLE IF NOT EXISTS price_alert (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,                  -- 用户ID
    entity_code VARCHAR(20) NOT NULL,         -- 实体代码或基金代码
    entity_type VARCHAR(10) NOT NULL,         -- 实体类型：stock/fund
    entity_name VARCHAR(100),                 -- 实体名称
    alert_type VARCHAR(10) NOT NULL,          -- 提醒类型：上涨/下跌
    threshold DECIMAL(10,4) NOT NULL,         --触发阈值
    current_value DECIMAL(10,4),             -- 当前值
    is_active BOOLEAN DEFAULT TRUE,           -- 是否激活
    last_triggered TIMESTAMP WITHOUT TIME ZONE,  -- 最后触发时间
    description TEXT,                         -- 提醒描述
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    deleted INTEGER DEFAULT 0                 -- 逻辑删除标记
);

-- 提醒历史记录表
CREATE TABLE IF NOT EXISTS alert_history (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,                  -- 用户ID
    alert_id BIGINT NOT NULL,                 -- 提醒规则ID
    entity_code VARCHAR(20) NOT NULL,         -- 实体代码
    entity_type VARCHAR(10) NOT NULL,        -- 实体类型：stock/fund
    entity_name VARCHAR(100),                 -- 实体名称
    alert_type VARCHAR(10) NOT NULL,         -- 提醒类型：上涨/下跌
    threshold DECIMAL(10,4) NOT NULL,         --触发阈值
    current_value DECIMAL(10,4) NOT NULL,    --触时的值
    triggered_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,   -- 触发时间
    trigger_reason TEXT,                      --触发原因
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- =====================================
--创建
-- =====================================

--基础数据表索引
CREATE INDEX IF NOT EXISTS idx_stock_symbol ON stock_basic(symbol) WHERE deleted = 0;
CREATE INDEX IF NOT EXISTS idx_stock_industry ON stock_basic(industry) WHERE deleted = 0;
CREATE INDEX IF NOT EXISTS idx_stock_market ON stock_basic(market) WHERE deleted = 0;

CREATE INDEX IF NOT EXISTS idx_fund_code ON fund_basic(fund_code) WHERE deleted = 0;
CREATE INDEX IF NOT EXISTS idx_fund_type ON fund_basic(type) WHERE deleted = 0;
CREATE INDEX IF NOT EXISTS idx_fund_manager ON fund_basic(manager) WHERE deleted = 0;

--行数据表索引
CREATE INDEX IF NOT EXISTS idx_stock_quote_stock_time ON stock_quote(stock_id, quote_time);
CREATE INDEX IF NOT EXISTS idx_stock_quote_time ON stock_quote(quote_time);
CREATE INDEX IF NOT EXISTS idx_fund_quote_fund_time ON fund_quote(fund_id, quote_time);
CREATE INDEX IF NOT EXISTS idx_fund_quote_time ON fund_quote(quote_time);

-- 提醒功能表索引
CREATE INDEX IF NOT EXISTS idx_price_alert_user_entity ON price_alert(user_id, entity_code, entity_type) 
    WHERE deleted = 0 AND is_active = TRUE;
CREATE INDEX IF NOT EXISTS idx_price_alert_active ON price_alert(is_active) WHERE deleted = 0;
CREATE INDEX IF NOT EXISTS idx_alert_history_user ON alert_history(user_id);
CREATE INDEX IF NOT EXISTS idx_alert_history_alert ON alert_history(alert_id);
CREATE INDEX IF NOT EXISTS idx_alert_history_triggered ON alert_history(triggered_at);

-- =====================================
-- 初始化数据（可选）
-- =====================================

--插入测试用的股票数据示例
-- INSERT INTO stock_basic (symbol, name, industry, market, listing_date, total_share, float_share, pe, pb) VALUES
-- ('000001', '平安银行', '银行', '深市', '1991-04-03', 19405918198, 19405918198, 5.2, 0.7),
-- ('600036', '招商银行', '银行', '沪市', '2002-04-09', 25219800000, 25219800000, 6.8, 1.2);

--插入测试用的基金数据示例
-- INSERT INTO fund_basic (fund_code, name, type, manager, establishment_date, fund_size, nav) VALUES
-- ('000001', '华夏成长混合', '混合型', '王泽实', '2001-12-18', 15800000000, 1.2345),
-- ('110001', '易方达平稳增长', '混合型', '陈志贤', '2002-08-23', 8900000000, 1.5678);

-- =====================================
--权设置（生产环境使用）
-- =====================================

-- GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA public TO your_user;
-- GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA public TO your_user;

-- =====================================
-- 查询验证语句
-- =====================================

--验证表创建
-- SELECT table_name FROM information_schema.tables WHERE table_schema = 'public' AND table_name IN 
-- ('stock_basic', 'fund_basic', 'stock_quote', 'fund_quote', 'price_alert', 'alert_history');

-- 查看表结构
-- \d stock_basic
-- \d fund_basic
-- \d stock_quote
-- \d fund_quote
-- \d price_alert
-- \d alert_history