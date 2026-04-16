-- 用户订阅表 (user_subscription)
-- 用于存储用户的提醒订阅设置

CREATE TABLE IF NOT EXISTS user_subscription (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    symbol VARCHAR(20) NOT NULL,  -- 标的代码
    symbol_type VARCHAR(10) NOT NULL,  -- 标的类型：STOCK/FUND
    symbol_name VARCHAR(100),  -- 标的名称
    target_change_percent DECIMAL(10, 2),  -- 目标涨跌幅百分比
    is_active BOOLEAN DEFAULT TRUE,  -- 是否激活
    last_triggered TIMESTAMP WITHOUT TIME ZONE,  -- 最后触发时间
    description TEXT,  -- 订阅描述
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    deleted INTEGER DEFAULT 0
);

-- 索引
CREATE INDEX idx_user_subscription_user_id ON user_subscription(user_id);
CREATE INDEX idx_user_subscription_symbol ON user_subscription(symbol);
CREATE INDEX idx_user_subscription_user_active ON user_subscription(user_id, is_active) WHERE deleted = 0;
CREATE INDEX idx_user_subscription_user_symbol_type ON user_subscription(user_id, symbol, symbol_type) WHERE deleted = 0;

-- 注释
COMMENT ON TABLE user_subscription IS '用户订阅提醒表';
COMMENT ON COLUMN user_subscription.id IS '主键';
COMMENT ON COLUMN user_subscription.user_id IS '用户ID';
COMMENT ON COLUMN user_subscription.symbol IS '标的代码';
COMMENT ON COLUMN user_subscription.symbol_type IS '标的类型：STOCK/FUND';
COMMENT ON COLUMN user_subscription.symbol_name IS '标的名称';
COMMENT ON COLUMN user_subscription.target_change_percent IS '目标涨跌幅百分比';
COMMENT ON COLUMN user_subscription.is_active IS '是否激活';
COMMENT ON COLUMN user_subscription.last_triggered IS '最后触发时间';
COMMENT ON COLUMN user_subscription.description IS '订阅描述';