# 批量添加消息提醒功能 - E2E 测试

## 概述

本目录包含批量添加消息提醒功能的完整 E2E 测试套件，使用 Playwright 框架编写。

## 测试场景

### 场景 1：创建提醒页面 - 单选模式
- ✅ 页面加载和UI验证
- ✅ 完整创建流程
- ✅ 提醒类型切换（价格/涨跌幅）
- ✅ 表单重置
- ✅ 返回列表导航

### 场景 2：创建提醒页面 - 批量模式
- ✅ 页面加载和UI验证
- ✅ 标的类型选择（股票/基金）
- ✅ 搜索并选择多个标的
- ✅ 全选搜索结果
- ✅ 清空选择
- ✅ 批量创建（全部成功）
- ✅ 批量创建（部分成功）
- ✅ 切换标的类型

### 场景 3：表单验证
- ✅ 单选模式 - 空标的代码
- ✅ 单选模式 - 标的代码过短
- ✅ 批量模式 - 未选择标的
- ✅ 未设置目标值
- ✅ 负数价格
- ✅ 涨跌幅为0
- ✅ 超过100个标的限制

### 场景 4：提醒设置页面
- ✅ 批量订阅和创建按钮
- ✅ 批量订阅模态框
- ✅ 标的类型选择
- ✅ 搜索和选择标的
- ✅ 批量订阅提交
- ✅ 取消模态框
- ✅ 列表搜索
- ✅ 列表筛选

### 场景 5：风险提醒页面
- ✅ 页面加载
- ✅ 提醒列表显示
- ✅ 无批量订阅/创建按钮
- ✅ 全部已读功能
- ✅ 无未读时按钮禁用
- ✅ 加载更多
- ✅ 涨跌幅信息
- ✅ 展开查看详情

### 场景 6：跨页面导航
- ✅ 创建提醒后返回列表
- ✅ 批量创建结果返回列表
- ✅ 页面间导航

### 场景 7：错误处理
- ✅ 网络错误
- ✅ 服务器500错误
- ✅ 批量创建部分失败

## 文件结构

```
tests/e2e/
├── AlertListPage.ts          # 提醒列表页 Page Object
├── AlertCreatePage.ts        # 创建提醒页 Page Object
├── RiskAlertListPage.ts      # 风险提醒页 Page Object
├── BatchSubscribeModal.ts    # 批量订阅模态框 Page Object
├── helpers.ts                # 测试辅助工具
├── index.ts                  # 导出索引
└── AlertManagement.spec.ts   # 主测试文件
```

## 运行测试

### 运行所有 E2E 测试
```bash
cd app
npx playwright test tests/e2e/AlertManagement.spec.ts
```

### 运行特定场景
```bash
# 只运行单选模式测试
npx playwright test -g "场景1"

# 只运行批量模式测试
npx playwright test -g "场景2"

# 只运行表单验证测试
npx playwright test -g "场景3"
```

### 以有头模式运行（查看浏览器）
```bash
npx playwright test tests/e2e/AlertManagement.spec.ts --headed
```

### 生成测试报告
```bash
npx playwright test tests/e2e/AlertManagement.spec.ts --reporter=html
npx playwright show-report
```

### 调试单个测试
```bash
npx playwright test tests/e2e/AlertManagement.spec.ts --debug
```

## Page Object Model

### AlertListPage
提醒设置页面 (`/alerts`) 的页面对象

**主要方法：**
- `navigate()` - 导航到页面
- `waitForPageLoad()` - 等待页面加载
- `clickCreateAlert()` - 点击创建提醒
- `clickBatchSubscribe()` - 点击批量订阅
- `searchSymbol(keyword)` - 搜索标的
- `filterBySymbolType(type)` - 筛选标的类型

### AlertCreatePage
创建提醒页面 (`/alerts/create`) 的页面对象

**主要方法：**
- `navigate(mode)` - 导航到页面（single/batch）
- `switchToSingleMode()` - 切换到单选模式
- `switchToBatchMode()` - 切换到批量模式
- `fillSingleForm(data)` - 填写单条表单
- `fillBatchForm(data)` - 填写批量表单
- `searchAndSelectSymbols(keyword, count)` - 搜索并选择标的
- `submit()` - 提交表单
- `waitForResultModal()` - 等待结果弹窗

### RiskAlertListPage
风险提醒页面 (`/risk-alerts`) 的页面对象

**主要方法：**
- `navigate()` - 导航到页面
- `markAllAsRead()` - 全部已读
- `verifyNoBatchSubscribeButton()` - 验证无批量订阅按钮
- `verifyNoCreateAlertButton()` - 验证无创建按钮

### BatchSubscribeModal
批量订阅模态框的页面对象

**主要方法：**
- `waitForOpen()` - 等待模态框打开
- `selectSymbolType(type)` - 选择标的类型
- `searchAndSelectSymbols(keyword, count)` - 搜索并选择标的
- `subscribe()` - 提交订阅
- `cancel()` - 取消

## 测试配置

### 重试机制
- 每个测试用例失败时自动重试 2 次
- 配置位置：`test.describe.configure({ retries: 2 })`

### 截图
- 测试失败时自动截图到 `test-results/` 目录
- 手动截图：`await page.screenshot({ path: 'test-results/name.png' })`

### 超时设置
- 默认操作超时：5000ms
- API 响应超时：5000ms
- 页面加载超时：根据网络状态自动调整

## Mock API

测试中使用 `page.route()` 拦截和模拟 API 请求：

```typescript
// 模拟单条创建成功
await page.route('**/api/alerts', async (route) => {
  if (route.request().method() === 'POST') {
    await route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify({
        success: true,
        data: { created: true, alert: { id: 999 }, message: '创建成功' }
      })
    })
  }
})

// 模拟批量创建
await page.route('**/api/alerts/batch/v2', async (route) => {
  await route.fulfill({
    status: 200,
    body: JSON.stringify(TestData.mockBatchCreateSuccess(['600000', '600519']))
  })
})
```

## 测试数据

使用 `TestData` 工具类生成测试数据：

```typescript
import { TestData } from './helpers'

// 生成唯一标的代码
const symbol = TestData.generateSymbol()

// 生成批量标的
const symbols = TestData.generateBatchSymbols(5)

// 模拟API响应
const mockResponse = TestData.mockStockResults(['600000', '600519'])
```

## CI/CD 集成

### GitHub Actions 示例
```yaml
- name: Run E2E Tests
  run: |
    cd app
    npm install
    npx playwright install chromium
    npx playwright test tests/e2e/AlertManagement.spec.ts --reporter=junit
  env:
    CI: true
```

## 常见问题

### Q: 测试在本地通过但在 CI 失败？
A: 检查以下几点：
1. 确保 Playwright 浏览器已安装：`npx playwright install`
2. 检查网络请求超时设置
3. 增加等待时间：`await page.waitForTimeout(1000)`

### Q: 如何调试失败的测试？
A: 使用以下命令：
```bash
npx playwright test tests/e2e/AlertManagement.spec.ts --debug
```

### Q: 如何查看测试截图？
A: 截图保存在 `test-results/` 目录，失败测试会自动截图。

## 维护指南

### 添加新测试用例
1. 在对应的 `test.describe` 块中添加
2. 使用 Page Object 方法而非直接操作 DOM
3. 添加截图便于调试
4. 遵循命名规范：`AC-{场景}.{用例} 描述`

### 更新 Page Object
1. 当页面UI变化时，只更新 Page Object 文件
2. 测试文件无需修改
3. 保持方法语义清晰

### 性能优化
1. 复用 Page Object 实例
2. 减少不必要的 `waitForTimeout`
3. 使用 `waitForURL` 代替固定等待
4. 并行运行独立测试

## 覆盖率目标

| 指标 | 目标 | 当前 |
|------|------|------|
| 场景覆盖 | 100% | 100% |
| 用例数量 | > 30 | 37 |
| 关键路径覆盖 | 100% | 100% |
| 错误场景覆盖 | 100% | 100% |

## 版本历史

- **v1.0** (2026-04-14): 初始版本，包含 7 个场景 37 个测试用例
