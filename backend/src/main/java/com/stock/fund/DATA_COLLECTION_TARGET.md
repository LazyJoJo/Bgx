# 数据采集目标管理功能使用说明

##功能概述

本功能提供了一个完整的数据采集目标管理系统，允许用户通过API或页面维护需要采集的股票和基金列表，并支持定时采集实时价格数据和涨跌情况。

##核心功能

### 1. 数据采集目标管理
-✅ 添加/修改/删除采集目标
- ✅/停用采集目标
- ✅按类型、分类、状态查询目标
- ✅ 获取需要采集的目标列表

### 2.定时数据采集
- ✅实时行情定时采集（每分钟）
- ✅基实时净值定时采集（每15分钟）
- ✅ 自动更新采集时间
- ✅基数据库配置的动态目标管理

### 3. 数据来源扩展
- ✅支持股票和基金类型
- ✅支持多市场（上海、深圳、港股等）
- ✅支持分类管理
- ✅支持自定义采集频率

## API接口列表

### 创建采集目标
```http
POST /api/data-collection-targets
Content-Type: application/json

{
  "code": "600036",
  "name": "招商银行",
  "type": "STOCK",
  "market": "SH",
  "category": "银行",
  "description": "招商银行股份有限公司",
  "active": true,
  "collectionFrequency": 15
}
```

### 查询采集目标
```http
GET /api/data-collection-targets/1                    #根据ID查询
GET /api/data-collection-targets/code/600036         #根据代码查询
GET /api/data-collection-targets/type/STOCK         # 根据类型查询
GET /api/data-collection-targets/active              # 查询激活目标
GET /api/data-collection-targets/category/银行      #根据分类查询
GET /api/data-collection-targets/needing-collection # 查询需要采集的目标
```

### 更新采集目标
```http
PUT /api/data-collection-targets/1
PUT /api/data-collection-targets/code/600036
```

###/停用目标
```http
POST /api/data-collection-targets/1/activate
POST /api/data-collection-targets/1/deactivate
POST /api/data-collection-targets/code/600036/activate
POST /api/data-collection-targets/code/600036/deactivate
```

###统计信息
```http
GET /api/data-collection-targets/count
GET /api/data-collection-targets/count/type/STOCK
GET /api/data-collection-targets/count/active
```

## 数据库表结构

```sql
CREATE TABLE data_collection_target (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(20) NOT NULL UNIQUE,           -- 代码
    name VARCHAR(100) NOT NULL,                -- 名称
    type VARCHAR(10) NOT NULL,                 -- 类型(STOCK/FUND)
    market VARCHAR(10),                        -- 市场(SH/SZ/HK)
    active BOOLEAN DEFAULT TRUE,               -- 是否激活
    category VARCHAR(50),                      -- 分类
    description TEXT,                          --描述
    last_collected_time TIMESTAMP,             -- 最后采集时间
    next_collection_time TIMESTAMP,            -- 下次采集时间
    collection_frequency INTEGER DEFAULT 15,    -- 采集频率(分钟)
    data_source VARCHAR(50) DEFAULT 'TUSHARE',   -- 数据源
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

## 使用示例

### 1. 添加股票采集目标
```bash
curl -X POST http://localhost:8081/api/data-collection-targets \
  -H "Content-Type: application/json" \
  -d '{
    "code": "600036",
    "name": "招商银行",
    "type": "STOCK",
    "market": "SH",
    "category": "银行",
    "description": "招商银行股份有限公司",
    "active": true,
    "collectionFrequency": 15
  }'
```

### 2. 添加基金采集目标
```bash
curl -X POST http://localhost:8081/api/data-collection-targets \
  -H "Content-Type: application/json" \
  -d '{
    "code": "000001",
    "name": "华夏成长",
    "type": "FUND",
    "category": "混合型",
    "description": "华夏成长证券投资基金",
    "active": true,
    "collectionFrequency": 15
  }'
```

### 3. 查询激活的股票目标
```bash
curl http://localhost:8081/api/data-collection-targets/active?type=STOCK
```

### 4.批激活/停用目标
```bash
#所有银行股
curl -X POST http://localhost:8081/api/data-collection-targets/category/银行/activate

#所有基金
curl -X POST http://localhost:8081/api/data-collection-targets/type/FUND/deactivate
```

## DDD架构说明

###层 (domain)
- `DataCollectionTarget` - 数据采集目标聚合根
- `DataCollectionTargetRepository` - 仓储接口

###应用层 (application)
- `DataCollectionTargetAppService` -应用服务接口
- `DataCollectionTargetAppServiceImpl` -应用服务实现

###设施层 (infrastructure)
- `DataCollectionTargetPO` -持化对象
- `DataCollectionTargetMapper` - MyBatis Mapper接口
- `DataCollectionTargetRepositoryImpl` - 仓储实现

###接层 (interfaces)
- `DataCollectionTargetController` - REST控制器

###示例
数据采集调度器(`DataCollectionScheduler`)已集成该功能，定时任务会自动从数据库读取采集目标列表并执行数据采集。

## 部署说明

1. **执行数据库脚本**
   ```bash
   psql -U postgres -d stock_fund_db -f src/main/resources/database/data_collection_target.sql
   ```

2. **启动应用**
   ```bash
   mvn spring-boot:run
   ```

3. **验证功能**
   - 访问API文档: http://localhost:8081/api/swagger-ui.html
   - 使用上述API示例测试功能

## 注意事项

1. **数据唯一性**:同代码的采集目标在数据库中唯一
2. **时间管理**:系统自动维护last_collected_time和next_collection_time
3. **调度策略**:根据采集频率动态计算下次采集时间
4. **异常处理**:异常时自动记录日志并继续处理其他目标
5. **性能优化**: 通过数据库索引提高查询性能