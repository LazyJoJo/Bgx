# 基金管理 API 文档

## 概述

本文档描述基金管理的前端 API 接口，基于 `/api/funds` 路径。

> **注意**：系统同时存在旧的 `/api/data-collection-targets` 路径（面向采集目标管理），前端日常基金 CRUD 操作已统一迁移至 `/api/funds`。

## API 基础信息

| 项目 | 内容 |
|------|------|
| Base URL | `/api/funds` |
| 认证 | 无特别要求（继承全局认证） |
| 内容类型 | `application/json` |

## 接口列表

### 1. 获取基金列表

```
GET /api/funds
```

**响应**:

```json
{
  "success": true,
  "data": [
    {
      "id": 1,
      "code": "001593",
      "name": "天弘创业板ETF联接C",
      "type": "FUND",
      "active": true,
      "market": "SZ",
      "category": "INDEX",
      "description": "",
      "collectionFrequency": 15,
      "dataSource": "SINA",
      "createdAt": "2026-04-23T09:33:00"
    }
  ]
}
```

---

### 2. 获取单个基金详情

```
GET /api/funds/{id}
```

| 参数 | 类型 | 位置 | 必填 | 说明 |
|------|------|------|------|------|
| id | number | path | 是 | 基金目标 ID |

**响应**:

```json
{
  "success": true,
  "data": {
    "id": 1,
    "code": "001593",
    "name": "天弘创业板ETF联接C",
    "type": "FUND",
    "active": true,
    "market": "SZ",
    "category": "INDEX",
    "description": "",
    "collectionFrequency": 15,
    "dataSource": "SINA"
  }
}
```

---

### 3. 创建基金（通过采集目标接口）

```
POST /api/data-collection-targets/createByCode
```

| 参数 | 类型 | 位置 | 必填 | 说明 |
|------|------|------|------|------|
| code | string | query | 是 | 基金代码 |

**响应**:

```json
{
  "success": true,
  "data": {
    "id": 1,
    "code": "001593",
    "name": "天弘创业板ETF联接C"
  }
}
```

---

### 4. 更新基金

```
PUT /api/funds/{id}
```

| 参数 | 类型 | 位置 | 必填 | 说明 |
|------|------|------|------|------|
| id | number | path | 是 | 基金目标 ID |

**请求体**（仅包含可编辑字段，部分更新）：

```json
{
  "market": "SZ",
  "category": "INDEX",
  "description": "更新后的描述",
  "collectionFrequency": 30,
  "dataSource": "SINA"
}
```

**响应**:

```json
{
  "success": true,
  "data": {
    "id": 1,
    "code": "001593",
    "name": "天弘创业板ETF联接C",
    "type": "FUND",
    "active": true,
    "market": "SZ",
    "category": "INDEX",
    "description": "更新后的描述",
    "collectionFrequency": 30,
    "dataSource": "SINA"
  }
}
```

> **说明**：后端采用部分更新策略，仅更新请求体中提供的非 null 字段。`code`、`name`、`type`、`active` 等字段不在 `UpdateFundRequest` 中，不可通过此接口修改。

---

### 5. 删除基金

```
DELETE /api/funds/{id}
```

| 参数 | 类型 | 位置 | 必填 | 说明 |
|------|------|------|------|------|
| id | number | path | 是 | 基金目标 ID |

**响应**:

```json
{
  "success": true,
  "data": "基金删除成功"
}
```

---

### 6. 激活基金

```
PATCH /api/funds/{id}/activate
```

**响应**：返回更新后的 `FundDTO`。

---

### 7. 停用基金

```
PATCH /api/funds/{id}/deactivate
```

**响应**：返回更新后的 `FundDTO`。

---

## 前端 API 调用封装

实际封装位于 [`app/src/services/api/funds.ts`](app/src/services/api/funds.ts:1)：

```typescript
import { ApiResponse, Fund, FundUpdateRequest } from '@/types'
import apiClient from './client'

export const fundsApi = {
  // 获取所有基金列表
  getAllFunds: () =>
    apiClient.get<ApiResponse<Fund[]>>('/funds'),

  // 获取基金详情（按ID）
  getFundById: (id: number) =>
    apiClient.get<ApiResponse<Fund>>(`/funds/${id}`),

  // 更新基金（部分更新）
  updateFund: (id: number, data: FundUpdateRequest) =>
    apiClient.put<ApiResponse<Fund>>(`/funds/${id}`, data),

  // 删除基金
  deleteFund: (id: number) =>
    apiClient.delete<ApiResponse<string>>(`/funds/${id}`),

  // 激活基金
  activateFund: (id: number) =>
    apiClient.patch<ApiResponse<Fund>>(`/funds/${id}/activate`),

  // 停用基金
  deactivateFund: (id: number) =>
    apiClient.patch<ApiResponse<Fund>>(`/funds/${id}/deactivate`),

  // 采集目标兼容接口（创建基金）
  addFundTarget: (fundCode: string) =>
    apiClient.post<ApiResponse<any>>('/data-collection-targets/createByCode', null, { params: { code: fundCode } }),
}
```

---

## 错误处理

| 错误码 | 说明 | 处理方式 |
|--------|------|----------|
| 400 | 参数错误/校验失败 | 显示后端返回的 `message` 内容 |
| 401 | 未授权 | 跳转登录页 |
| 404 | 基金不存在 | 刷新列表 |
| 500 | 服务器错误 | 显示通用错误提示 |

---

## 类型定义

```typescript
// 基金类型
export interface Fund {
  id: string | number
  code: string
  name: string
  type?: string
  market?: string
  category?: string
  description?: string
  collectionFrequency?: number  // 单位为分钟，与后端 Integer 对齐
  dataSource?: string
  active?: boolean
  createdAt?: string
  updatedAt?: string
}

// 更新请求（仅可编辑字段）
export interface FundUpdateRequest {
  market?: string
  category?: string
  description?: string
  collectionFrequency?: number  // 单位为分钟
  dataSource?: string
}

// API 响应格式（与 client.ts 保持一致）
export interface ApiResponse<T> {
  success: boolean
  data?: T
  error?: string
}
```

---

## 使用示例

### 更新基金

```typescript
import { fundsApi } from '@/services/api/funds'
import { message } from 'antd'

const handleUpdate = async (fund: Fund) => {
  try {
    const response = await fundsApi.updateFund(Number(fund.id), {
      market: fund.market,
      category: fund.category,
      description: fund.description,
      collectionFrequency: 30,
      dataSource: fund.dataSource,
    })

    if (response.success) {
      message.success('基金更新成功')
      dispatch(fetchAllFunds())
    }
  } catch (error: any) {
    message.error(error?.message || '更新失败')
  }
}
```

### 删除基金

```typescript
import { fundsApi } from '@/services/api/funds'
import { Modal } from 'antd'

const handleDelete = (fund: Fund) => {
  Modal.confirm({
    title: '确认删除',
    content: `确定要删除基金 "${fund.name}" (${fund.code}) 吗？此操作不可恢复。`,
    okText: '确认删除',
    okType: 'danger',
    cancelText: '取消',
    onOk: async () => {
      try {
        await fundsApi.deleteFund(Number(fund.id))
        message.success('删除成功')
        dispatch(fetchAllFunds())
      } catch (error: any) {
        message.error(error?.message || '删除失败')
      }
    }
  })
}
```

---

## 编辑页字段说明

### 可编辑字段（编辑页显示）

| 字段 | 说明 | 表单类型 | 必填 |
|------|------|----------|------|
| `market` | 市场（SH/SZ等） | Select | 是 |
| `category` | 分类标签 | Select | 是 |
| `description` | 描述信息 | TextArea | 否 |
| `collectionFrequency` | 采集频率（分钟） | Select | 是 |
| `dataSource` | 数据源配置 | Select | 是 |

#### 采集频率选项（数值型，与后端 Integer 对齐）

| value | label |
|-------|-------|
| 1440 | 每日 |
| 60 | 每小时 |
| 15 | 每15分钟 |
| 5 | 每5分钟 |
| 1 | 实时（每分钟） |

### 不显示字段（编辑页隐藏）

| 字段 | 原因 |
|------|------|
| `type` | 编辑页就是基金页面，不需要显示类型 |

### 不可编辑字段（只读展示）

| 字段 | 原因 |
|------|------|
| `id` | 主键，系统自动生成 |
| `code` | 基金代码，创建后不可修改 |
| `name` | 基金名称，来自外部数据源，不可手动编辑 |
| `createdAt` | 创建时间，系统自动记录 |
| `lastCollectedTime` | 最后采集时间，系统自动更新 |
| `nextCollectionTime` | 下次采集时间，系统自动计算 |

---

## 文档变更记录

| 日期 | 变更内容 |
|------|----------|
| 2026-04-26 | 重构文档：Base URL 由 `/api/data-collection-targets` 修正为 `/api/funds`；`collectionFrequency` 类型由 `string` 修正为 `number`；ApiResponse 统一为 `client.ts` 的 `{success, data?, error?}` 格式；更新前端封装示例为实际的 `fundsApi` 对象；修正编辑字段的表单类型；明确后端采用部分更新策略。 |
