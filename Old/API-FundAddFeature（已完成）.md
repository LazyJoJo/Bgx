# 基金添加功能 API 文档

## 1. 现有 API 梳理

### 1.1 采集目标相关 API


| 方法   | 路径                                        | 描述                 | 状态       |
| ------ | ------------------------------------------- | -------------------- | ---------- |
| POST   | `/api/data-collection-targets/createByCode` | 根据代码创建采集目标 | **已实现** |
| GET    | `/api/data-collection-targets/type/{type}`  | 按类型查询采集目标   | 已实现     |
| GET    | `/api/data-collection-targets/code/{code}`  | 按代码查询采集目标   | 已实现     |
| PUT    | `/api/data-collection-targets/{id}`         | 更新采集目标         | 已实现     |
| DELETE | `/api/data-collection-targets/{id}`         | 删除采集目标         | 已实现     |

### 1.2 基金分析相关 API


| 方法 | 路径                                    | 描述                 | 状态   |
| ---- | --------------------------------------- | -------------------- | ------ |
| GET  | `/api/fund-analysis/quotes/latest`      | 获取所有基金最新净值 | 已实现 |
| GET  | `/api/fund-analysis/quotes/page`        | 分页查询基金净值     | 已实现 |
| GET  | `/api/fund-analysis/quote/{fundCode}`   | 获取单个基金实时行情 | 已实现 |
| GET  | `/api/fund-analysis/history/{fundCode}` | 获取基金历史净值     | 已实现 |

---

## 2. 核心 API：创建采集目标

### 2.1 接口信息


| 属性             | 值                                                        |
| ---------------- | --------------------------------------------------------- |
| **接口路径**     | `POST /api/data-collection-targets/createByCode`          |
| **请求参数**     | `code` (query string)                                     |
| **Content-Type** | `application/json`                                        |
| **后端服务**     | `DataCollectionTargetController.createTargetByCode()`     |
| **业务逻辑**     | `DataCollectionTargetAppServiceImpl.createTargetByCode()` |

### 2.2 请求示例

```http
POST /api/data-collection-targets/createByCode?code=000001
Content-Type: application/json
```

### 2.3 请求参数


| 参数名 | 类型   | 位置  | 必填 | 描述                     |
| ------ | ------ | ----- | ---- | ------------------------ |
| code   | string | query | 是   | 基金代码，6 位数字字符串 |

### 2.4 响应格式

#### 成功响应 (HTTP 200)

```json
{
  "success": true,
  "message": "采集目标创建成功",
  "data": {
    "id": 1,
    "code": "000001",
    "name": "上证指数",
    "type": "FUND",
    "active": true,
    "category": null,
    "description": null,
    "market": null,
    "collectionFrequency": null,
    "dataSource": null,
    "createdAt": "2024-01-15T10:30:00",
    "updatedAt": "2024-01-15T10:30:00"
  }
}
```

#### 失败响应 - 无效代码 (HTTP 500)

```json
{
  "success": false,
  "message": "无法获取有效的基金数据，请确认是否为有效的基金代码（此接口仅支持基金类型）",
  "data": null
}
```

#### 失败响应 - 基金已存在 (HTTP 200)

> **注意**：根据代码分析，已存在的基金会直接返回，不会报错。

```json
{
  "success": true,
  "message": "采集目标创建成功",
  "data": {
    "id": 1,
    "code": "000001",
    "name": "上证指数",
    "type": "FUND",
    "active": true,
    ...
  }
}
```

---

## 3. API 调用流程

### 3.1 前端调用流程

```
FundList.tsx
    │
    ├── 用户点击"添加基金"按钮
    │       │
    │       ▼
    │   FundCreateModal.tsx (弹窗打开)
    │       │
    │       ▼
    │   用户输入基金代码
    │       │
    │       ▼
    │   点击"确认添加"
    │       │
    │       ▼
    │   fundsApi.addFundTarget(code)  ◀─── 已有 API
    │       │
    │       ▼
    │   apiClient.post('/data-collection-targets/createByCode', null, { params: { code } })
    │       │
    │       ▼
    │   后端返回响应
    │       │
    │       ▼
    │   成功：关闭弹窗 + 刷新列表 + message.success
    │   失败：message.error 显示错误信息
```

### 3.2 后端处理流程

```
POST /api/data-collection-targets/createByCode?code=000001
        │
        ▼
DataCollectionTargetController.createTargetByCode()
        │
        ▼
DataCollectionTargetAppService.createTargetByCode()
        │
        ├── 1. 验证代码非空
        │
        ├── 2. 查询是否已存在
        │       │
        │       ├── 已存在 → 直接返回（幂等）
        │       │
        │       └── 不存在 → 继续
        │
        ├── 3. 调用 FundDataFetcher.fetchFundDataWithRetry(code)
        │       │
        │       └── 调用新浪 API 获取基金信息
        │
        ├── 4. 成功获取数据 → 保存到数据库
        │       │
        │       ├── fund_basic (基金基本信息)
        │       ├── fund_quote (基金净值)
        │       └── data_collection_target (采集目标)
        │
        └── 5. 返回创建的采集目标
```

---

## 4. 错误码定义

### 4.1 后端错误


| 错误码 | HTTP 状态码 | 错误消息                                                                     | 说明         |
| ------ | ----------- | ---------------------------------------------------------------------------- | ------------ |
| -      | 200         | "采集目标创建成功"                                                           | 成功         |
| -      | 404         | "无法获取有效的基金数据，请确认是否为有效的基金代码（此接口仅支持基金类型）" | 无效基金代码 |
| -      | 400         | "基金代码不能为空"                                                           | 代码为空     |

### 4.2 前端错误


| 错误场景 | 错误消息           | 处理方式     |
| -------- | ------------------ | ------------ |
| 网络错误 | "网络错误，请重试" | 显示错误提示 |
