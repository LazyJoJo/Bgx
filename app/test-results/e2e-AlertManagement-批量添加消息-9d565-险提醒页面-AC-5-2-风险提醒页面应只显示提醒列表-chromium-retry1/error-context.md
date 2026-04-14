# Instructions

- Following Playwright test failed.
- Explain why, be concise, respect Playwright best practices.
- Provide a snippet of code with the fix, if possible.

# Test info

- Name: e2e\AlertManagement.spec.ts >> 批量添加消息提醒功能 E2E 测试 >> 场景5：风险提醒页面 >> AC-5.2 风险提醒页面应只显示提醒列表
- Location: tests\e2e\AlertManagement.spec.ts:698:5

# Error details

```
Error: expect(locator).toBeVisible() failed

Locator: locator('.ant-collapse')
Expected: visible
Error: strict mode violation: locator('.ant-collapse') resolved to 3 elements:
    1) <div class="ant-collapse ant-collapse-icon-position-end ant-collapse-ghost css-dev-only-do-not-override-mncuj7">…</div> aka locator('div').filter({ hasText: '-04-133 条提醒' }).nth(5)
    2) <div class="ant-collapse ant-collapse-icon-position-end ant-collapse-ghost css-dev-only-do-not-override-mncuj7">…</div> aka locator('div').filter({ hasText: '-04-091 条提醒' }).nth(5)
    3) <div class="ant-collapse ant-collapse-icon-position-end ant-collapse-ghost css-dev-only-do-not-override-mncuj7">…</div> aka locator('div').filter({ hasText: '-04-086 条提醒' }).nth(5)

Call log:
  - Expect "toBeVisible" with timeout 5000ms
  - waiting for locator('.ant-collapse')

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
            - generic [ref=e85]:
              - text: "3"
              - generic [ref=e86]: "4"
              - generic [ref=e87]: "5"
              - generic [ref=e88]: "6"
        - img "user" [ref=e90] [cursor=pointer]:
          - img [ref=e91]
    - main [ref=e93]:
      - generic [ref=e94]:
        - generic [ref=e95]:
          - generic [ref=e96]:
            - heading "warning 风险提醒" [level=2] [ref=e98]:
              - img "warning" [ref=e99]:
                - img [ref=e100]
              - text: 风险提醒
            - superscript [ref=e104]:
              - generic [ref=e106]: "3"
          - button "check-circle 全部已读" [ref=e109] [cursor=pointer]:
            - img "check-circle" [ref=e111]:
              - img [ref=e112]
            - generic [ref=e115]: 全部已读
        - generic [ref=e116]:
          - button "collapsed 2026-04-13 3 条提醒" [ref=e119] [cursor=pointer]:
            - img "collapsed" [ref=e121]:
              - img [ref=e122]
            - generic [ref=e125]:
              - generic [ref=e127]: 2026-04-13
              - generic [ref=e128]: 3 条提醒
          - button "collapsed 2026-04-09 1 条提醒" [ref=e131] [cursor=pointer]:
            - img "collapsed" [ref=e133]:
              - img [ref=e134]
            - generic [ref=e137]:
              - generic [ref=e139]: 2026-04-09
              - generic [ref=e140]: 1 条提醒
          - button "collapsed 2026-04-08 6 条提醒" [ref=e143] [cursor=pointer]:
            - img "collapsed" [ref=e145]:
              - img [ref=e146]
            - generic [ref=e149]:
              - generic [ref=e151]: 2026-04-08
              - generic [ref=e152]: 6 条提醒
```

# Test source

```ts
  1   | import { Page, Locator, expect } from '@playwright/test'
  2   | 
  3   | /**
  4   |  * 风险提醒页面 (/risk-alerts) Page Object
  5   |  */
  6   | export class RiskAlertListPage {
  7   |   readonly page: Page
  8   |   
  9   |   // 标题和按钮
  10  |   readonly pageTitle: Locator
  11  |   readonly markAllReadButton: Locator
  12  |   readonly unreadBadge: Locator
  13  |   
  14  |   // 列表
  15  |   readonly alertList: Locator
  16  |   readonly alertItems: Locator
  17  |   readonly emptyState: Locator
  18  |   readonly loadingSpinner: Locator
  19  |   readonly loadMoreButton: Locator
  20  | 
  21  |   constructor(page: Page) {
  22  |     this.page = page
  23  |     this.pageTitle = page.locator('h2').filter({ hasText: /风险提醒/ })
  24  |     this.markAllReadButton = page.getByRole('button', { name: /全部已读/ })
  25  |     this.unreadBadge = page.locator('.ant-badge-count').first()
  26  |     this.alertList = page.locator('.ant-collapse')
  27  |     this.alertItems = page.locator('.ant-collapse-item')
  28  |     this.emptyState = page.locator('.ant-empty')
  29  |     this.loadingSpinner = page.locator('.ant-spin')
  30  |     this.loadMoreButton = page.getByRole('button', { name: /加载更多/ })
  31  |   }
  32  | 
  33  |   /**
  34  |    * 导航到风险提醒页
  35  |    */
  36  |   async navigate() {
  37  |     await this.page.goto('/risk-alerts')
  38  |     await this.page.evaluate(() => localStorage.setItem('userId', '1'))
  39  |     await this.page.waitForLoadState('networkidle')
  40  |   }
  41  | 
  42  |   /**
  43  |    * 等待页面加载完成
  44  |    */
  45  |   async waitForPageLoad() {
  46  |     await expect(this.pageTitle).toBeVisible()
  47  |   }
  48  | 
  49  |   /**
  50  |    * 等待列表加载完成
  51  |    */
  52  |   async waitForListLoaded() {
  53  |     await expect(this.loadingSpinner).not.toBeVisible({ timeout: 5000 })
  54  |   }
  55  | 
  56  |   /**
  57  |    * 点击"全部已读"按钮
  58  |    */
  59  |   async markAllAsRead() {
  60  |     await this.markAllReadButton.click()
  61  |     await this.page.waitForTimeout(500)
  62  |   }
  63  | 
  64  |   /**
  65  |    * 验证"全部已读"按钮是否禁用
  66  |    */
  67  |   async isMarkAllReadDisabled(): Promise<boolean> {
  68  |     return await this.markAllReadButton.isDisabled()
  69  |   }
  70  | 
  71  |   /**
  72  |    * 验证页面不包含批量订阅按钮
  73  |    */
  74  |   async verifyNoBatchSubscribeButton() {
  75  |     const batchButton = this.page.getByRole('button', { name: /批量订阅/ })
  76  |     await expect(batchButton).toHaveCount(0)
  77  |   }
  78  | 
  79  |   /**
  80  |    * 验证页面不包含创建提醒按钮
  81  |    */
  82  |   async verifyNoCreateAlertButton() {
  83  |     const createButton = this.page.getByRole('button', { name: /创建提醒/ })
  84  |     await expect(createButton).toHaveCount(0)
  85  |   }
  86  | 
  87  |   /**
  88  |    * 验证提醒列表显示
  89  |    */
  90  |   async verifyAlertListVisible() {
> 91  |     await expect(this.alertList).toBeVisible()
      |                                  ^ Error: expect(locator).toBeVisible() failed
  92  |   }
  93  | 
  94  |   /**
  95  |    * 验证空状态
  96  |    */
  97  |   async verifyEmptyState() {
  98  |     await expect(this.emptyState).toBeVisible()
  99  |     await expect(this.emptyState).toContainText(/暂无风险提醒/)
  100 |   }
  101 | 
  102 |   /**
  103 |    * 获取提醒项数量
  104 |    */
  105 |   async getAlertItemCount(): Promise<number> {
  106 |     return await this.alertItems.count()
  107 |   }
  108 | 
  109 |   /**
  110 |    * 点击加载更多
  111 |    */
  112 |   async loadMore() {
  113 |     await this.loadMoreButton.click()
  114 |     await this.page.waitForTimeout(500)
  115 |   }
  116 | 
  117 |   /**
  118 |    * 验证加载更多按钮可见性
  119 |    */
  120 |   async isLoadMoreVisible(): Promise<boolean> {
  121 |     return await this.loadMoreButton.isVisible()
  122 |   }
  123 | 
  124 |   /**
  125 |    * 截图
  126 |    */
  127 |   async screenshot(name: string) {
  128 |     await this.page.screenshot({ path: `test-results/${name}.png`, fullPage: true })
  129 |   }
  130 | }
  131 | 
```