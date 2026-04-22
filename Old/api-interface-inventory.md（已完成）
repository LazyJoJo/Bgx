# 项目 API 接口清单

> 生成日期：2026-04-17
> 扫描范围：后端 `backend/src/main/java/com/stock/fund/interfaces/controller` + 前端 `app/src/services/api`

---

## 一、统计概览

| 类别 | 数量 |
|------|------|
| 后端已定义接口 | 71 |
| 前端已调用接口 | 38 |
| ✅ 活跃接口（前后端对齐） | 37 |
| ⚠️ 后端存在但前端未调用 | 33 |
| 🔴 前端调用但后端不存在 | 0 |

---

## 二、活跃接口（前后端均存在）

### 2.1 仪表盘 / Dashboard

| 方法 | 路径 | 前端调用位置 | 说明 |
|------|------|-------------|------|
| `GET` | `/api/data-collection-targets/count/type/STOCK` | `dashboard.ts` | 统计股票数量 |
| `GET` | `/api/data-collection-targets/count/type/FUND` | `dashboard.ts` | 统计基金数量 |
| `GET` | `/api/data-collection-targets/count/active` | `dashboard.ts` | 统计活跃目标数 |

> **注：** 前端已改用 `/api/dashboard/stats` 聚合接口获取统计数据。

### 2.2 采集目标管理 / DataCollectionTarget

| 方法 | 路径 | 前端调用位置 | 说明 |
|------|------|-------------|------|
| `GET` | `/api/data-collection-targets/type/{type}` | `stocks.ts`, `funds.ts` | 按类型查询采集目标 |
| `GET` | `/api/data-collection-targets/code/{code}` | `stocks.ts`, `funds.ts` | 按代码查询采集目标 |
| `POST` | `/api/data-collection-targets/createByCode` | `funds.ts` | 根据代码创建采集目标 |
| `PUT` | `/api/data-collection-targets/{id}` | `stocks.ts`, `funds.ts` | 更新采集目标 |
| `DELETE` | `/api/data-collection-targets/{id}` | `stocks.ts`, `funds.ts` | 删除采集目标 |
| `POST` | `/api/data-collection-targets/{id}/activate` | `stocks.ts`, `funds.ts` | 激活采集目标 |
| `POST` | `/api/data-collection-targets/{id}/deactivate` | `stocks.ts`, `funds.ts` | 停用采集目标 |
| `GET` | `/api/data-collection-targets/search` | `stocks.ts` | 搜索采集目标 |

### 2.3 基金分析 / FundAnalysis

| 方法 | 路径 | 前端调用位置 | 说明 |
|------|------|-------------|------|
| `GET` | `/api/fund-analysis/quotes/page` | `funds.ts` | 分页查询基金净值 |
| `GET` | `/api/fund-analysis/quotes/latest` | `funds.ts` | 获取所有基金最新净值 |
| `POST` | `/api/fund-analysis/quotes/refresh` | `funds.ts` | 实时刷新基金行情 |
| `GET` | `/api/fund-analysis/quote/{code}` | `funds.ts` | 获取单个基金实时行情 |
| `GET` | `/api/fund-analysis/quote/latest/{code}` | `funds.ts` | 获取单个基金最新净值 |
| `GET` | `/api/fund-analysis/history/{code}` | `funds.ts` | 获取基金历史净值 |
| `GET` | `/api/fund-analysis/statistics` | `funds.ts` | 获取基金统计信息 |

### 2.4 风险提醒 / RiskAlert

| 方法 | 路径 | 前端调用位置 | 说明 |
|------|------|-------------|------|
| `GET` | `/api/risk-alerts/user/{userId}` | `riskAlerts.ts` | 获取用户风险提醒列表 |
| `GET` | `/api/risk-alerts/user/{userId}/unread-count` | `riskAlerts.ts` | 获取未读风险提醒数量 |
| `POST` | `/api/risk-alerts/user/{userId}/mark-read` | `riskAlerts.ts` | 标记所有风险提醒为已读 |
| `POST` | `/api/risk-alerts/check` | `riskAlerts.ts` | 手动触发风险检测 |
| `GET` | `/api/risk-alerts/user/{userId}/today-count` | `riskAlerts.ts`, `Dashboard.tsx` | 获取当天风险提醒数量 |
| `POST` | `/api/risk-alerts/subscribe` | `BatchSubscribeModal.tsx` | 批量订阅标的 |

### 2.5 通知 / Notifications

| 方法 | 路径 | 前端调用位置 | 说明 |
|------|------|-------------|------|
| `GET` | `/api/notifications/unread-count` | `riskAlerts.ts` | 获取全局未读通知计数 |

### 2.6 订阅管理 / Subscription

| 方法 | 路径 | 前端调用位置 | 说明 |
|------|------|-------------|------|
| `GET` | `/api/subscriptions/user/{userId}` | `subscriptions.ts`, `Dashboard.tsx` | 获取用户所有订阅 |
| `GET` | `/api/subscriptions/{id}` | `subscriptions.ts` | 获取单个订阅详情 |
| `POST` | `/api/subscriptions` | `subscriptions.ts` | 创建订阅 |
| `POST` | `/api/subscriptions/batch` | `subscriptions.ts` | 批量创建订阅 |
| `DELETE` | `/api/subscriptions/batch` | `subscriptions.ts` | 批量删除订阅 |
| `PATCH` | `/api/subscriptions/batch/activate` | `subscriptions.ts` | 批量启用订阅 |
| `PATCH` | `/api/subscriptions/batch/deactivate` | `subscriptions.ts` | 批量停用订阅 |
| `PUT` | `/api/subscriptions/{id}` | `subscriptions.ts` | 更新订阅 |
| `DELETE` | `/api/subscriptions/{id}` | `subscriptions.ts` | 删除订阅 |
| `PATCH` | `/api/subscriptions/{id}/activate` | `subscriptions.ts` | 启用订阅 |
| `PATCH` | `/api/subscriptions/{id}/deactivate` | `subscriptions.ts` | 停用订阅 |
| `POST` | `/api/subscriptions/check-duplicates` | `subscriptions.ts` | 检测重复订阅 |

---

## 三、后端存在但前端未调用的接口

### 3.1 仪表盘

| 方法 | 路径 | 说明 | 建议 |
|------|------|------|------|
| `GET` | `/api/dashboard/stats` | 聚合仪表盘统计 | 前端可直接使用此接口替代多个独立调用 |

### 3.2 数据采集（调度器/后台用）

| 方法 | 路径 | 说明 | 建议 |
|------|------|------|------|
| `POST` | `/api/data/stocks` | 采集股票基本信息 | 后台/调度器使用，保留 |
| `POST` | `/api/data/quote/{symbol}` | 采集单只股票行情 | 后台/调度器使用，保留 |
| `POST` | `/api/data/funds` | 采集基金基本信息 | 后台/调度器使用，保留 |
| `POST` | `/api/data/fund-quote/{fundCode}` | 采集基金净值 | 后台/调度器使用，保留 |

### 3.3 采集目标管理（扩展接口）

| 方法 | 路径 | 说明 | 建议 |
|------|------|------|------|
| `PUT` | `/api/data-collection-targets/code/{code}` | 按代码更新 | 前端目前只使用 ID 更新，可保留 |
| `DELETE` | `/api/data-collection-targets/code/{code}` | 按代码删除 | 前端目前只使用 ID 删除，可保留 |
| `GET` | `/api/data-collection-targets` | 查询所有采集目标 | 前端目前按类型查询，可保留 |
| `GET` | `/api/data-collection-targets/category/{category}` | 按分类查询 | 前端未使用，可保留 |
| `GET` | `/api/data-collection-targets/needing-collection` | 查询需要采集的目标 | 调度器使用，保留 |
| `POST` | `/api/data-collection-targets/code/{code}/activate` | 按代码激活 | 前端未使用，可保留 |
| `POST` | `/api/data-collection-targets/code/{code}/deactivate` | 按代码停用 | 前端未使用，可保留 |
| `GET` | `/api/data-collection-targets/count` | 获取总数 | 前端未使用，可保留 |

### 3.4 风险提醒（扩展接口）

| 方法 | 路径 | 说明 | 建议 |
|------|------|------|------|
| `GET` | `/api/risk-alerts/user/{userId}/today` | 获取今日风险提醒 | 前端目前使用 `/user/{userId}` 已满足需求 |
| `PATCH` | `/api/risk-alerts/{id}/read` | 标记单条已读 | 前端目前只使用 mark-all-read |
| `DELETE` | `/api/risk-alerts/{id}` | 删除单条风险提醒 | 前端未提供删除功能 |

### 3.5 健康检查（运维/监控用）

| 方法 | 路径 | 说明 | 建议 |
|------|------|------|------|
| `GET` | `/health` | 服务健康检查 | 运维使用，保留 |
| `GET` | `/health/ping` | 简单心跳 | 运维/探针使用，保留 |
| `GET` | `/health/cache` | 缓存状态 | 运维使用，保留 |

### 3.6 缓存测试（调试用）

| 方法 | 路径 | 说明 | 建议 |
|------|------|------|------|
| `POST` | `/api/cache/set` | 设置缓存 | 调试用，保留 |
| `POST` | `/api/cache/set-expire` | 设置带过期缓存 | 调试用，保留 |
| `GET` | `/api/cache/get` | 获取缓存 | 调试用，保留 |
| `DELETE` | `/api/cache/delete` | 删除缓存 | 调试用，保留 |
| `GET` | `/api/cache/exists` | 检查缓存存在 | 调试用，保留 |
| `GET` | `/api/cache/stats` | 获取缓存统计 | 调试用，保留 |
| `POST` | `/api/cache/clear` | 清空所有缓存 | 调试用，保留 |
| `POST` | `/api/cache/clear/stock` | 清空股票缓存 | 调试用，保留 |
| `POST` | `/api/cache/clear/fund` | 清空基金缓存 | 调试用，保留 |

---

## 四、接口废弃历史

以下接口已随模块重构物理删除，前后端均不再存在：

| 原路径前缀 | 所属模块 | 删除原因 |
|-----------|---------|---------|
| `/api/alerts/**` | price_alert / alert_history | 被 `user_subscription` + `risk_alert` 新架构替代 |

---

## 五、建议行动项汇总

| 优先级 | 行动项 | 相关文件 | 状态 |
|--------|--------|---------|------|
| 🟢 待定 | 评估风险提醒单条已读操作 | `app/src/pages/risk-alerts/RiskAlertList.tsx`, `app/src/services/api/riskAlerts.ts` | 按产品需求决定是否对接 UI |
| 🟢 待定 | 评估风险提醒单条删除操作 | `app/src/pages/risk-alerts/RiskAlertList.tsx`, `app/src/services/api/riskAlerts.ts` | 按产品需求决定是否对接 |
