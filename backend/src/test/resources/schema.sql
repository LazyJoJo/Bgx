-- H2 Test Schema
DROP TABLE IF EXISTS alert_history;
DROP TABLE IF EXISTS price_alert;
DROP TABLE IF EXISTS fund_quote;
DROP TABLE IF EXISTS stock_quote;
DROP TABLE IF EXISTS fund_basic;
DROP TABLE IF EXISTS stock_basic;

-- 股票基础信息表
CREATE TABLE stock_basic (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    symbol VARCHAR(20) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    industry VARCHAR(50),
    market VARCHAR(20),
    listing_date DATE,
    total_share DECIMAL(20,2),
    float_share DECIMAL(20,2),
    pe DECIMAL(10,2),
    pb DECIMAL(10,2),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted INTEGER DEFAULT 0
);

-- 基金基础信息表
CREATE TABLE fund_basic (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    fund_code VARCHAR(20) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    type VARCHAR(50),
    manager VARCHAR(100),
    establishment_date DATE,
    fund_size DECIMAL(20,2),
    nav DECIMAL(10,4),
    day_growth DECIMAL(10,4),
    week_growth DECIMAL(10,4),
    month_growth DECIMAL(10,4),
    year_growth DECIMAL(10,4),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted INTEGER DEFAULT 0
);

-- 股票行情表
CREATE TABLE stock_quote (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    stock_id BIGINT NOT NULL,
    quote_time TIMESTAMP NOT NULL,
    open DECIMAL(10,2) NOT NULL,
    high DECIMAL(10,2) NOT NULL,
    low DECIMAL(10,2) NOT NULL,
    close DECIMAL(10,2) NOT NULL,
    volume BIGINT NOT NULL,
    amount DECIMAL(20,2),
    change DECIMAL(10,2),
    change_percent DECIMAL(10,4),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(stock_id, quote_time)
);

-- 基金行情表
CREATE TABLE fund_quote (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    fund_id BIGINT NOT NULL,
    quote_time TIMESTAMP NOT NULL,
    nav DECIMAL(10,4) NOT NULL,
    acc_nav DECIMAL(10,4),
    change DECIMAL(10,4),
    change_percent DECIMAL(10,4),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(fund_id, quote_time)
);

-- 价格提醒表
CREATE TABLE price_alert (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    entity_code VARCHAR(20) NOT NULL,
    entity_type VARCHAR(20) NOT NULL,
    entity_name VARCHAR(100),
    alert_type VARCHAR(20) NOT NULL,
    threshold DECIMAL(10,4),
    target_change_percent DECIMAL(10,4),
    base_price DECIMAL(10,4),
    current_value DECIMAL(10,4),
    is_active BOOLEAN DEFAULT TRUE,
    last_triggered TIMESTAMP,
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted INTEGER DEFAULT 0
);

-- 提醒历史记录表
CREATE TABLE alert_history (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    alert_id BIGINT NOT NULL,
    entity_code VARCHAR(20) NOT NULL,
    entity_type VARCHAR(10) NOT NULL,
    entity_name VARCHAR(100),
    alert_type VARCHAR(10) NOT NULL,
    threshold DECIMAL(10,4) NOT NULL,
    current_value DECIMAL(10,4) NOT NULL,
    triggered_at TIMESTAMP NOT NULL,
    trigger_reason TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 索引
CREATE INDEX idx_stock_symbol ON stock_basic(symbol);
CREATE INDEX idx_fund_code ON fund_basic(fund_code);
CREATE INDEX idx_price_alert_user_entity ON price_alert(user_id, entity_code, entity_type);
CREATE INDEX idx_price_alert_active ON price_alert(is_active);
CREATE INDEX idx_alert_history_user ON alert_history(user_id);
