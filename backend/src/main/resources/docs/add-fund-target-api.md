# 添加目标基金API文档

##接说明
该接口用于添加新的基金采集目标，系统会自动获取基金的实时数据并存储到相应的表中。

## 请求地址
```
POST /api/data-collection-targets/createByCode
```

## 请求参数
| 参数名 | 类型 |必 | 说明 |
|--------|------|------|------|
| code | String | 是 |基代码 |

## 响应格式
```json
{
  "success": true,
  "message": "基金目标添加成功",
  "data": {
    "id": 123,
    "code": "000001",
    "name": "华夏成长混合",
    "type": "FUND",
    "market": null,
    "active": true,
    "category": null,
    "description": null,
    "lastCollectedTime": null,
    "nextCollectionTime": null,
    "collectionFrequency": 15,
    "dataSource": "SINA_API"
  }
}
```

##功能说明
调用此接口会执行以下操作：

1. **检查目标是否存在**：首先检查data_collection_target表中是否已存在该基金代码的配置
2. **如果目标已存在**：直接返回已存在的配置信息，不再重复添加
3. **如果目标不存在**：执行以下步骤
   - **获取实时数据**：通过外部API获取指定基金代码的实时净值数据
   - **提取基础信息**：从实时数据中提取基金的基础信息（包括基金名称）
   - **存储到fund_basic表**：将基金基础信息存储到基金基础信息表
   - **存储到fund_quote表**：将实时净值数据存储到基金净值表
   - **创建采集目标**：在data_collection_target表中创建采集目标配置

## 数据流向
```
用户输入(fundCode) 
    ↓
检查data_collection_target表中是否已存在
    ↓
如果存在 → 直接返回已有配置
    ↓
如果不存在 → 调用外部API获取实时数据（包含基金名称）
    ↓
提取基础信息 →存到fund_basic表
    ↓
存储实时数据 →存储到fund_quote表
    ↓
创建采集目标 →存储到data_collection_target表
```

## 示例请求
```bash
curl -X POST "http://localhost:8080/api/data-collection-targets/createByCode?code=000001"
```

## 注意事项
-基代码必须是有效的基金代码
- 基名称会自动从API返回的数据中获取
- 如果基金目标已存在，系统会直接返回已有的配置，不会重复添加
- 采集频率默认为15分钟
- 数据源默认为SINA_API
- 该基金会被设置为激活状态，参与定时数据采集
- 整个操作在事务中执行，确保数据一致性