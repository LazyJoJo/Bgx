# 数据库表结构设计文档

## 1. 数据库基本信息

- **数据库类型**: PostgreSQL
- **数据库名称**: stock_fund_db
- **连接信息**: 
  - 主机: localhost
  -端口: 5432
  - 用户名: postgres
  -密码: 168168

## 2.核心数据表设计

### 2.1 stock_basic (股票基础信息表)

|字段名 |数据类型 |约束 |说明 |
|------|--------|-----|-----|
|id |BIGSERIAL |PK |主键，自增 |
|symbol |VARCHAR(20) |NOT NULL, UNIQUE |股票代码 |
|name |VARCHAR(100) |NOT NULL |股票名称 |
|industry |VARCHAR(50) | |所属行业 |
|market |VARCHAR(20) | |市场 |
|listing_date |DATE | |上市日期 |
|total_share |DECIMAL(20,2) | |总股本 |
|float_share |DECIMAL(20,2) | |流通股本 |
|pe |DECIMAL(10,2) | |市盈率 |
|pb |DECIMAL(10,2) | |市净率 |
|created_at |TIMESTAMP |DEFAULT CURRENT_TIMESTAMP |创建时间 |
|updated_at |TIMESTAMP |DEFAULT CURRENT_TIMESTAMP |更新时间 |
|deleted |INTEGER |DEFAULT 0 |逻辑删除标记 |

### 2.2 fund_basic (基金基础信息表)

|字段名 |数据类型 |约束 |说明 |
|------|--------|-----|-----|
|id |BIGSERIAL |PK |主键，自增 |
|fund_code |VARCHAR(20) |NOT NULL, UNIQUE |基金代码 |
|name |VARCHAR(100) |NOT NULL |基金名称 |
|type |VARCHAR(50) | |基金类型 |
|manager |VARCHAR(100) | |基金经理 |
|establishment_date |DATE | |成立日期 |
|fund_size |DECIMAL(20,2) | |基金规模 |
|nav |DECIMAL(10,4) | |最新净值 |
|day_growth |DECIMAL(10,4) | |日增长率 |
|week_growth |DECIMAL(10,4) | |周增长率 |
|month_growth |DECIMAL(10,4) | |月增长率 |
|year_growth |DECIMAL(10,4) | |年增长率 |
|created_at |TIMESTAMP |DEFAULT CURRENT_TIMESTAMP |创建时间 |
|updated_at |TIMESTAMP |DEFAULT CURRENT_TIMESTAMP |更新时间 |
|deleted |INTEGER |DEFAULT 0 |逻辑删除标记 |

### 2.3 stock_quote (股票行情表)

|字段名 |数据类型 |约束 |说明 |
|------|--------|-----|-----|
|id |BIGSERIAL |PK |主键，自增 |
|stock_id |BIGINT |NOT NULL, FK |股票ID，关联stock_basic |
|timestamp |TIMESTAMP |NOT NULL |行情时间 |
|open |DECIMAL(10,2) |NOT NULL |开盘价 |
|high |DECIMAL(10,2) |NOT NULL |最高价 |
|low |DECIMAL(10,2) |NOT NULL |最低价 |
|close |DECIMAL(10,2) |NOT NULL |收盘价 |
|volume |BIGINT |NOT NULL |成交量 |
|amount |DECIMAL(20,2) | |成交额 |
|change |DECIMAL(10,2) | |涨跌额 |
|change_percent |DECIMAL(10,4) | |涨跌幅 |
|created_at |TIMESTAMP |DEFAULT CURRENT_TIMESTAMP |创建时间 |

### 2.4 fund_quote (基金行情表)

|字段名 |数据类型 |约束 |说明 |
|------|--------|-----|-----|
|id |BIGSERIAL |PK |主键，自增 |
|fund_id |BIGINT |NOT NULL, FK |基金ID，关联fund_basic |
|timestamp |TIMESTAMP |NOT NULL |净值时间 |
|nav |DECIMAL(10,4) |NOT NULL |净值 |
|acc_nav |DECIMAL(10,4) | |累计净值 |
|change |DECIMAL(10,4) | |涨跌额 |
|change_percent |DECIMAL(10,4) | |涨跌幅 |
|created_at |TIMESTAMP |DEFAULT CURRENT_TIMESTAMP |创建时间 |

### 2.5 price_alert (价格提醒规则表)

|字段名 |数据类型 |约束 |说明 |
|------|--------|-----|-----|
|id |BIGSERIAL |PK |主键，自增 |
|user_id |BIGINT |NOT NULL |用户ID |
|entity_code |VARCHAR(20) |NOT NULL |实体代码 |
|entity_type |VARCHAR(10) |NOT NULL |实体类型：stock/fund |
|entity_name |VARCHAR(100) | |实体名称 |
|alert_type |VARCHAR(10) |NOT NULL |提醒类型：上涨/下跌 |
|threshold |DECIMAL(10,4) |NOT NULL |触发阈值 |
|current_value |DECIMAL(10,4) | |当前值 |
|is_active |BOOLEAN |DEFAULT TRUE |是否激活 |
|last_triggered |TIMESTAMP | |最后触发时间 |
|description |TEXT | |提醒描述 |
|created_at |TIMESTAMP |DEFAULT CURRENT_TIMESTAMP |创建时间 |
|updated_at |TIMESTAMP |DEFAULT CURRENT_TIMESTAMP |更新时间 |
|deleted |INTEGER |DEFAULT 0 |逻辑删除标记 |

### 2.6 alert_history (提醒历史记录表)

|字段名 |数据类型 |约束 |说明 |
|------|--------|-----|-----|
|id |BIGSERIAL |PK |主键，自增 |
|user_id |BIGINT |NOT NULL |用户ID |
|alert_id |BIGINT |NOT NULL |提醒规则ID |
|entity_code |VARCHAR(20) |NOT NULL |实体代码 |
|entity_type |VARCHAR(10) |NOT NULL |实体类型：stock/fund |
|entity_name |VARCHAR(100) | |实体名称 |
|alert_type |VARCHAR(10) |NOT NULL |提醒类型：上涨/下跌 |
|threshold |DECIMAL(10,4) |NOT NULL |触发阈值 |
|current_value |DECIMAL(10,4) |NOT NULL |触发时的值 |
|triggered_at |TIMESTAMP |NOT NULL |触发时间 |
|trigger_reason |TEXT | |触发原因 |
|created_at |TIMESTAMP |DEFAULT CURRENT_TIMESTAMP |创建时间 |

## 3.设计

### 3.1 主要索引
```sql
--基础数据表索引
CREATE INDEX idx_stock_symbol ON stock_basic(symbol) WHERE deleted = 0;
CREATE INDEX idx_stock_industry ON stock_basic(industry) WHERE deleted = 0;
CREATE INDEX idx_fund_code ON fund_basic(fund_code) WHERE deleted = 0;
CREATE INDEX idx_fund_type ON fund_basic(type) WHERE deleted = 0;

--行数据表索引
CREATE INDEX idx_stock_quote_stock_timestamp ON stock_quote(stock_id, timestamp);
CREATE INDEX idx_fund_quote_fund_timestamp ON fund_quote(fund_id, timestamp);

-- 提醒功能表索引
CREATE INDEX idx_price_alert_user_entity ON price_alert(user_id, entity_code, entity_type) WHERE deleted = 0 AND is_active = TRUE;
CREATE INDEX idx_alert_history_user ON alert_history(user_id);
```

## 4. 数据完整性约束

### 4.1外约束
- `stock_quote.stock_id` → `stock_basic.id`
- `fund_quote.fund_id` → `fund_basic.id`
- `alert_history.alert_id` → `price_alert.id`

### 4.2性约束
- `stock_basic.symbol` (股票代码唯一)
- `fund_basic.fund_code` (基金代码唯一)
- `(stock_id, timestamp)`组唯一 (同一股票同一时间点数据唯一)
- `(fund_id, timestamp)`组唯一 (同一基金同一时间点数据唯一)

## 5.执行步骤

### 5.1 创建数据库
```sql
CREATE DATABASE stock_fund_db;
```

### 5.2执行表结构脚本
```bash
# 使用psql命令行工具
psql -U postgres -d stock_fund_db -f init_tables.sql

# 或者在数据库管理工具中执行SQL文件
```

### 5.3验证表结构
```sql
-- 查看所有表
\dt

-- 查看表结构
\d stock_basic
\d fund_basic
\d stock_quote
\d fund_quote
\d price_alert
\d alert_history
```

## 6. 数据初始化建议

### 6.1 基础数据
-预置主要股票和基金的基础信息
- 设置合理的默认提醒规则模板

### 6.2测试数据
-插入少量测试数据用于开发验证
-避在生产环境插入大量测试数据

## 7.维建议

### 7.1定维护
-监控表空间使用情况
-定期分析和优化索引
-清理历史提醒记录

### 7.2备策略
-定期备份数据库
- 重要数据变更前进行备份
-测试备份恢复流程