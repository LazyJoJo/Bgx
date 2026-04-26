# 基金管理 API 文档

## 概述

本文档描述基金采集目标管理的前端 API 接口（后端已实现，前端需要调用）。

## API 基础信息

| 项目 | 内容 |
|------|------|
| Base URL | `/api/data-collection-targets` |
| 认证 | 无特别要求（继承全局认证） |
| 内容类型 | `application/json` |

## 接口列表

### 1. 获取基金列表

```
GET /api/data-collection-targets/type/{type}
```

| 参数 | 类型 | 位置 | 必填 | 说明 |
|------|------|------|------|------|
| type | string | path | 是 | 目标类型，固定为 `FUND` |

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
      "description": "",
      "createdAt": "2026-04-23T09:33:00"
    }
  ]
}
```

---

### 2. 获取单个基金详情

```
GET /api/data-collection-targets/{id}
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
    "description": ""
  }
}
```

---

### 3. 创建基金（已有）

```
POST /api/data-collection-targets/createByCode
```

| 参数 | 类型 | 位置 | 必填 | 说明 |
|------|------|------|------|------|
| code | string | body | 是 | 基金代码 |

**请求体**:

```json
{
  "code": "001593"
}
```

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

### 4. 更新基金 ⚠️ 需要前端调用

```
PUT /api/data-collection-targets/{id}
```

| 参数 | 类型 | 位置 | 必填 | 说明 |
|------|------|------|------|------|
| id | number | path | 是 | 基金目标 ID |

**请求体**:

```json
{
  "code": "001593",
  "name": "天弘创业板ETF联接C",
  "type": "FUND",
  "active": true,
  "description": "更新后的描述",
  "collectionFrequency": 30,
  "market": "SH"
}
```

**响应**:

```json
{
  "success": true,
  "data": null
}
```

---

### 5. 删除基金 ⚠️ 需要前端调用

```
DELETE /api/data-collection-targets/{id}
```

| 参数 | 类型 | 位置 | 必填 | 说明 |
|------|------|------|------|------|
| id | number | path | 是 | 基金目标 ID |

**响应**:

```json
{
  "success": true,
  "data": "Delete success"
}
```

---

## 前端 API 调用封装

已在 `app/src/services/api/funds.ts` 中定义：

```typescript
import { get, post, put, del } from './client'
import type { ApiResponse, Fund, CreateFundResponse, UpdateFundRequest } from '@/types'

// 获取基金列表
export const getFunds = (): Promise<ApiResponse<Fund[]>> => {
  return get('/data-collection-targets/type/FUND')
}

// 获取基金详情
export const getFundDetail = (fundCode: string): Promise<ApiResponse<Fund>> => {
  return get(`/data-collection-targets/code/${fundCode}`)
}

// 创建基金
export const createFundTarget = (code: string): Promise<ApiResponse<CreateFundResponse>> => {
  return post('/data-collection-targets/createByCode', { code })
}

// 更新基金 ⚠️ 需调用
export const updateFundTarget = (id: number, data: UpdateFundRequest): Promise<ApiResponse<any>> => {
  return put(`/data-collection-targets/${id}`, data)
}

// 删除基金 ⚠️ 需调用
export const deleteFundTarget = (id: number): Promise<ApiResponse<string>> => {
  return del(`/data-collection-targets/${id}`)
}
```

---

## 错误处理

| 错误码 | 说明 | 处理方式 |
|--------|------|----------|
| 400 | 参数错误 | 显示错误信息 |
| 404 | 基金不存在 | 刷新列表 |
| 500 | 服务器错误 | 显示通用错误提示 |

---

## 类型定义

```typescript
// 基金类型
export interface Fund {
  id: number
  code: string
  name: string
  type: 'FUND' | 'STOCK'
  active: boolean
  description?: string
  createdAt?: string
}

// 更新请求（可编辑字段）
export interface UpdateFundRequest {
  code: string
  name: string
  type: string
  active: boolean
  description?: string
  collectionFrequency?: number
  market?: string
}

// 创建响应
export interface CreateFundResponse {
  id: number
  code: string
  name: string
}

// API 响应格式
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
import { updateFundTarget } from '@/services/api/funds'
import { message } from 'antd'

const handleUpdate = async (fund: Fund) => {
  try {
    const response = await updateFundTarget(fund.id, {
      code: fund.code,
      name: fund.name,
      type: 'FUND',
      active: fund.active,
      description: fund.description,
      collectionFrequency: 30,
      market: 'SH'
    })
    
    if (response.success) {
      message.success('基金更新成功')
      // 刷新列表
      dispatch(fetchFunds())
    } else {
      message.error(response.error || '更新失败')
    }
  } catch (error) {
    message.error('网络错误，请重试')
  }
}
```

### 删除基金

```typescript
import { deleteFundTarget } from '@/services/api/funds'
import { Modal } from 'antd'
import { ExclamationCircleOutlined } from '@ant-design/icons'

const handleDelete = (fund: Fund) => {
  Modal.confirm({
    title: '确认删除',
    icon: <ExclamationCircleOutlined />,
    content: `确定要删除基金 "${fund.name}" 吗？此操作不可恢复。`,
    okText: '确认',
    cancelText: '取消',
    okButtonProps: { danger: true },
    onOk: async () => {
      try {
        const response = await deleteFundTarget(fund.id)
        
        if (response.success) {
          message.success('删除成功')
          // 刷新列表
          dispatch(fetchFunds())
        } else {
          message.error(response.error || '删除失败')
        }
      } catch (error) {
        message.error('网络错误，请重试')
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
| `market` | 市场（SH/SZ/HK等） | Select | 否 |
| `category` | 分类标签 | Input | 否 |
| `description` | 描述信息 | TextArea | 否 |
| `collectionFrequency` | 采集频率（分钟） | InputNumber | 否 |

### 不显示字段（编辑页隐藏）

| 字段 | 原因 |
|------|------|
| `type` | 编辑页就是基金页面，不需要显示类型 |
| `dataSource` | 数据源默认是新浪财经，编辑页不需要修改 |

### 不可编辑字段

| 字段 | 原因 |
|------|------|
| `id` | 主键，系统自动生成 |
| `code` | 基金代码，创建后不可修改 |
| `name` | 基金名称，来自外部数据源 |
| `createdAt` | 创建时间，系统自动记录 |
| `lastCollectedTime` | 最后采集时间，系统自动更新 |
| `nextCollectionTime` | 下次采集时间，系统自动计算 |
