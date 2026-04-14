# Instructions

- Following Playwright test failed.
- Explain why, be concise, respect Playwright best practices.
- Provide a snippet of code with the fix, if possible.

# Test info

- Name: e2e\AlertManagement.spec.ts >> 批量添加消息提醒功能 E2E 测试 >> 场景5：风险提醒页面 >> AC-5.4 应能点击"全部已读"标记已读
- Location: tests\e2e\AlertManagement.spec.ts:730:5

# Error details

```
Error: expect(locator).toBeVisible() failed

Locator: locator('.ant-message-success')
Expected: visible
Timeout: 3000ms
Error: element(s) not found

Call log:
  - Expect "toBeVisible" with timeout 3000ms
  - waiting for locator('.ant-message-success')

```

```
Error: ENOENT: no such file or directory, open 'D:\workspace\Bgx\app\test-results\FAILED-AC-5.4-应能点击"全部已读"标记已读.png'
```

# Page snapshot

```yaml
- generic [ref=e3]:
  - complementary [ref=e4]:
    - generic [ref=e5]:
      - generic [ref=e6]: 股票基金系统
      - menu [ref=e7]:
        - menuitem "dashboard 仪表盘" [ref=e8] [cursor=pointer]:
          - img "dashboard" [ref=e9]:
            - img [ref=e10]
          - generic [ref=e12]: 仪表盘
        - menuitem "stock 股票管理" [ref=e13] [cursor=pointer]:
          - img "stock" [ref=e14]:
            - img [ref=e15]
          - generic [ref=e17]: 股票管理
        - menuitem "fund 基金管理" [ref=e18] [cursor=pointer]:
          - img "fund" [ref=e19]:
            - img [ref=e20]
          - generic [ref=e22]: 基金管理
        - menuitem "bell 提醒设置" [ref=e23] [cursor=pointer]:
          - img "bell" [ref=e24]:
            - img [ref=e25]
          - generic [ref=e27]: 提醒设置
        - menuitem "warning 风险提醒" [ref=e28] [cursor=pointer]:
          - img "warning" [ref=e29]:
            - img [ref=e30]
          - generic [ref=e32]: 风险提醒
        - separator [ref=e33]
        - menuitem "bar-chart 数据分析" [ref=e34] [cursor=pointer]:
          - img "bar-chart" [ref=e35]:
            - img [ref=e36]
          - generic [ref=e38]: 数据分析
        - separator [ref=e39]
        - menuitem "setting 系统设置" [ref=e40] [cursor=pointer]:
          - img "setting" [ref=e41]:
            - img [ref=e42]
          - generic [ref=e44]: 系统设置
        - menuitem "user 个人中心" [ref=e45] [cursor=pointer]:
          - img "user" [ref=e46]:
            - img [ref=e47]
          - generic [ref=e49]: 个人中心
  - generic [ref=e50]:
    - banner [ref=e51]:
      - button "menu-fold" [ref=e53] [cursor=pointer]:
        - img "menu-fold" [ref=e55]:
          - img [ref=e56]
      - menu [ref=e59]:
        - menuitem "dashboard 仪表盘" [ref=e60] [cursor=pointer]:
          - img "dashboard" [ref=e61]:
            - img [ref=e62]
          - text: 仪表盘
        - menuitem "stock 股票" [ref=e64] [cursor=pointer]:
          - img "stock" [ref=e65]:
            - img [ref=e66]
          - text: 股票
        - menuitem "fund 基金" [ref=e68] [cursor=pointer]:
          - img "fund" [ref=e69]:
            - img [ref=e70]
          - text: 基金
        - menuitem "bell 提醒" [ref=e72] [cursor=pointer]:
          - img "bell" [ref=e73]:
            - img [ref=e74]
          - text: 提醒
        - menuitem [disabled]:
          - img:
            - img
      - generic [ref=e76]:
        - generic [ref=e77]:
          - button "bell" [ref=e78] [cursor=pointer]:
            - img "bell" [ref=e80]:
              - img [ref=e81]
          - superscript [ref=e83]:
            - generic [ref=e85]: "3"
        - img "user" [ref=e87] [cursor=pointer]:
          - img [ref=e88]
    - main [ref=e90]:
      - generic [ref=e91]:
        - generic [ref=e92]:
          - heading "warning 风险提醒" [level=2] [ref=e95]:
            - img "warning" [ref=e96]:
              - img [ref=e97]
            - text: 风险提醒
          - button "check-circle 全部已读" [disabled] [ref=e101]:
            - generic:
              - img "check-circle":
                - img
            - generic: 全部已读
        - generic [ref=e102]:
          - button "collapsed 2026-04-13 3 条提醒" [ref=e105] [cursor=pointer]:
            - img "collapsed" [ref=e107]:
              - img [ref=e108]
            - generic [ref=e111]:
              - generic [ref=e113]: 2026-04-13
              - generic [ref=e114]: 3 条提醒
          - button "collapsed 2026-04-09 1 条提醒" [ref=e117] [cursor=pointer]:
            - img "collapsed" [ref=e119]:
              - img [ref=e120]
            - generic [ref=e123]:
              - generic [ref=e125]: 2026-04-09
              - generic [ref=e126]: 1 条提醒
          - button "collapsed 2026-04-08 6 条提醒" [ref=e129] [cursor=pointer]:
            - img "collapsed" [ref=e131]:
              - img [ref=e132]
            - generic [ref=e135]:
              - generic [ref=e137]: 2026-04-08
              - generic [ref=e138]: 6 条提醒
```

# Test source

```ts
  1   | import { test, expect } from '@playwright/test'
  2   | import { AlertCreatePage } from './AlertCreatePage'
  3   | import { AlertListPage } from './AlertListPage'
  4   | import { RiskAlertListPage } from './RiskAlertListPage'
  5   | import { BatchSubscribeModal } from './BatchSubscribeModal'
  6   | 
  7   | /**
  8   |  * 批量添加消息提醒功能 E2E 测试
  9   |  * 
  10  |  * 测试场景：
  11  |  * 1. 创建提醒页面 - 单选模式
  12  |  * 2. 创建提醒页面 - 批量模式
  13  |  * 3. 表单验证
  14  |  * 4. 提醒设置页面
  15  |  * 5. 风险提醒页面
  16  |  * 
  17  |  * 技术要求：
  18  |  * - 使用Playwright进行E2E测试
  19  |  * - 使用Page Object Model模式
  20  |  * - 包含截图和失败重试逻辑
  21  |  */
  22  | 
  23  | test.describe('批量添加消息提醒功能 E2E 测试', () => {
  24  |   // 失败重试配置
  25  |   test.describe.configure({ retries: 2 })
  26  | 
  27  |   let alertCreatePage: AlertCreatePage
  28  |   let alertListPage: AlertListPage
  29  |   let riskAlertListPage: RiskAlertListPage
  30  |   let batchSubscribeModal: BatchSubscribeModal
  31  | 
  32  |   test.beforeEach(async ({ page }) => {
  33  |     alertCreatePage = new AlertCreatePage(page)
  34  |     alertListPage = new AlertListPage(page)
  35  |     riskAlertListPage = new RiskAlertListPage(page)
  36  |     batchSubscribeModal = new BatchSubscribeModal(page)
  37  |   })
  38  | 
  39  |   test.afterEach(async ({ page }, testInfo) => {
  40  |     // 测试失败时自动截图
  41  |     if (testInfo.status !== testInfo.expectedStatus) {
> 42  |       await page.screenshot({
      |       ^ Error: ENOENT: no such file or directory, open 'D:\workspace\Bgx\app\test-results\FAILED-AC-5.4-应能点击"全部已读"标记已读.png'
  43  |         path: `test-results/FAILED-${testInfo.title.replace(/\s+/g, '-')}.png`,
  44  |         fullPage: true
  45  |       })
  46  |     }
  47  |   })
  48  | 
  49  |   /**
  50  |    * 场景1：创建提醒页面 - 单选模式
  51  |    */
  52  |   test.describe('场景1：创建提醒页面 - 单选模式', () => {
  53  |     test('AC-1.1 应能导航到创建提醒页面并显示单选模式', async ({ page }) => {
  54  |       await alertCreatePage.navigate('single')
  55  |       await alertCreatePage.waitForPageLoad()
  56  |       
  57  |       // 验证页面标题
  58  |       await expect(alertCreatePage.pageTitle).toContainText('创建提醒')
  59  |       
  60  |       // 验证模式切换存在
  61  |       await expect(alertCreatePage.modeSwitch).toBeVisible()
  62  |       
  63  |       // 验证默认是单选模式
  64  |       await alertCreatePage.verifySingleModeUI()
  65  |       
  66  |       await alertCreatePage.screenshot('scenario1-single-mode-initial')
  67  |     })
  68  | 
  69  |     test('AC-1.2 单选模式应能完整创建提醒', async ({ page }) => {
  70  |       await alertCreatePage.navigate('single')
  71  |       await alertCreatePage.waitForPageLoad()
  72  |       
  73  |       // 拦截API请求，模拟成功响应
  74  |       await page.route('**/api/alerts', async (route) => {
  75  |         if (route.request().method() === 'POST') {
  76  |           await route.fulfill({
  77  |             status: 200,
  78  |             contentType: 'application/json',
  79  |             body: JSON.stringify({
  80  |               success: true,
  81  |               data: {
  82  |                 created: true,
  83  |                 alert: { id: 999, symbol: '600519', status: 'ACTIVE' },
  84  |                 message: '提醒创建成功'
  85  |               }
  86  |             })
  87  |           })
  88  |         }
  89  |       })
  90  |       
  91  |       // 填写表单
  92  |       await alertCreatePage.fillSingleForm({
  93  |         symbol: '600519',
  94  |         symbolType: 'STOCK',
  95  |         alertType: 'PRICE_ABOVE',
  96  |         targetPrice: 1800.50
  97  |       })
  98  |       
  99  |       // 提交
  100 |       await alertCreatePage.submit()
  101 |       
  102 |       // 验证创建成功（跳转到列表页）
  103 |       await page.waitForURL('**/alerts', { timeout: 5000 })
  104 |       await expect(page).toHaveURL(/\/alerts/)
  105 |       
  106 |       await alertCreatePage.screenshot('scenario1-single-mode-success')
  107 |     })
  108 | 
  109 |     test('AC-1.3 单选模式 - 选择涨跌幅类型应切换输入框', async ({ page }) => {
  110 |       await alertCreatePage.navigate('single')
  111 |       await alertCreatePage.waitForPageLoad()
  112 |       
  113 |       // 切换到涨跌幅类型
  114 |       await alertCreatePage.alertTypeSelect.click()
  115 |       await page.locator('.ant-select-dropdown .ant-select-item').filter({ hasText: '涨跌幅' }).click()
  116 |       await page.waitForTimeout(300)
  117 |       
  118 |       // 验证涨跌幅输入框显示，价格输入框隐藏
  119 |       await expect(alertCreatePage.targetChangePercentInput).toBeVisible()
  120 |       
  121 |       await alertCreatePage.screenshot('scenario1-percent-change-input')
  122 |     })
  123 | 
  124 |     test('AC-1.4 单选模式 - 应能重置表单', async ({ page }) => {
  125 |       await alertCreatePage.navigate('single')
  126 |       await alertCreatePage.waitForPageLoad()
  127 |       
  128 |       // 填写部分表单
  129 |       await alertCreatePage.symbolInput.fill('600000')
  130 |       
  131 |       // 重置
  132 |       await alertCreatePage.reset()
  133 |       
  134 |       // 验证表单已清空
  135 |       const symbolValue = await alertCreatePage.symbolInput.inputValue()
  136 |       expect(symbolValue).toBe('')
  137 |       
  138 |       await alertCreatePage.screenshot('scenario1-form-reset')
  139 |     })
  140 | 
  141 |     test('AC-1.5 单选模式 - 点击返回列表应跳转', async ({ page }) => {
  142 |       await alertCreatePage.navigate('single')
```