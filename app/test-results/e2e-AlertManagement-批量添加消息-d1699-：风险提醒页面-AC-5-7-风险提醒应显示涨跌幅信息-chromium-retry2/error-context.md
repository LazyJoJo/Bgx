# Instructions

- Following Playwright test failed.
- Explain why, be concise, respect Playwright best practices.
- Provide a snippet of code with the fix, if possible.

# Test info

- Name: e2e\AlertManagement.spec.ts >> 批量添加消息提醒功能 E2E 测试 >> 场景5：风险提醒页面 >> AC-5.7 风险提醒应显示涨跌幅信息
- Location: tests\e2e\AlertManagement.spec.ts:804:5

# Error details

```
Error: expect(locator).toBeVisible() failed

Locator: locator('.ant-collapse-item').first().locator('text=/%/')
Expected: visible
Timeout: 5000ms
Error: element(s) not found

Call log:
  - Expect "toBeVisible" with timeout 5000ms
  - waiting for locator('.ant-collapse-item').first().locator('text=/%/')

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
        - button "bell" [ref=e78] [cursor=pointer]:
          - img "bell" [ref=e80]:
            - img [ref=e81]
        - img "user" [ref=e84] [cursor=pointer]:
          - img [ref=e85]
    - main [ref=e87]:
      - generic [ref=e88]:
        - generic [ref=e89]:
          - heading "warning 风险提醒" [level=2] [ref=e92]:
            - img "warning" [ref=e93]:
              - img [ref=e94]
            - text: 风险提醒
          - button "check-circle 全部已读" [disabled] [ref=e98]:
            - generic:
              - img "check-circle":
                - img
            - generic: 全部已读
        - generic [ref=e99]:
          - button "collapsed 2026-04-13 3 条提醒" [ref=e102] [cursor=pointer]:
            - img "collapsed" [ref=e104]:
              - img [ref=e105]
            - generic [ref=e108]:
              - generic [ref=e110]: 2026-04-13
              - generic [ref=e111]: 3 条提醒
          - button "collapsed 2026-04-09 1 条提醒" [ref=e114] [cursor=pointer]:
            - img "collapsed" [ref=e116]:
              - img [ref=e117]
            - generic [ref=e120]:
              - generic [ref=e122]: 2026-04-09
              - generic [ref=e123]: 1 条提醒
          - button "collapsed 2026-04-08 6 条提醒" [ref=e126] [cursor=pointer]:
            - img "collapsed" [ref=e128]:
              - img [ref=e129]
            - generic [ref=e132]:
              - generic [ref=e134]: 2026-04-08
              - generic [ref=e135]: 6 条提醒
```

# Test source

```ts
  717 |     test('AC-5.3 风险提醒页面不应有批量订阅或创建按钮', async ({ page }) => {
  718 |       await riskAlertListPage.navigate()
  719 |       await riskAlertListPage.waitForPageLoad()
  720 |       
  721 |       // 验证没有批量订阅按钮
  722 |       await riskAlertListPage.verifyNoBatchSubscribeButton()
  723 |       
  724 |       // 验证没有创建提醒按钮
  725 |       await riskAlertListPage.verifyNoCreateAlertButton()
  726 |       
  727 |       await riskAlertListPage.screenshot('scenario5-no-create-buttons')
  728 |     })
  729 | 
  730 |     test('AC-5.4 应能点击"全部已读"标记已读', async ({ page }) => {
  731 |       await riskAlertListPage.navigate()
  732 |       await riskAlertListPage.waitForPageLoad()
  733 |       await riskAlertListPage.waitForListLoaded()
  734 |       
  735 |       // 拦截标记已读API
  736 |       await page.route('**/api/risk-alerts/mark-read', async (route) => {
  737 |         await route.fulfill({
  738 |           status: 200,
  739 |           contentType: 'application/json',
  740 |           body: JSON.stringify({
  741 |             success: true,
  742 |             message: '已全部标记为已读'
  743 |           })
  744 |         })
  745 |       })
  746 |       
  747 |       // 验证按钮未禁用（有未读时）
  748 |       const isDisabled = await riskAlertListPage.isMarkAllReadDisabled()
  749 |       
  750 |       if (!isDisabled) {
  751 |         await riskAlertListPage.markAllAsRead()
  752 |         
  753 |         // 验证成功消息
  754 |         await expect(page.locator('.ant-message-success')).toBeVisible({ timeout: 3000 })
  755 |       }
  756 |       
  757 |       await riskAlertListPage.screenshot('scenario5-mark-all-read')
  758 |     })
  759 | 
  760 |     test('AC-5.5 无未读时"全部已读"应禁用', async ({ page }) => {
  761 |       await riskAlertListPage.navigate()
  762 |       await riskAlertListPage.waitForPageLoad()
  763 |       await riskAlertListPage.waitForListLoaded()
  764 |       
  765 |       // 模拟无未读数据
  766 |       await page.route('**/api/risk-alerts/unread-count', async (route) => {
  767 |         await route.fulfill({
  768 |           status: 200,
  769 |           contentType: 'application/json',
  770 |           body: JSON.stringify({
  771 |             success: true,
  772 |             data: 0
  773 |           })
  774 |         })
  775 |       })
  776 |       
  777 |       await page.reload()
  778 |       await riskAlertListPage.waitForPageLoad()
  779 |       await riskAlertListPage.waitForListLoaded()
  780 |       
  781 |       // 验证按钮禁用
  782 |       const isDisabled = await riskAlertListPage.isMarkAllReadDisabled()
  783 |       expect(isDisabled).toBe(true)
  784 |       
  785 |       await riskAlertListPage.screenshot('scenario5-no-unread')
  786 |     })
  787 | 
  788 |     test('AC-5.6 风险提醒应支持加载更多', async ({ page }) => {
  789 |       await riskAlertListPage.navigate()
  790 |       await riskAlertListPage.waitForPageLoad()
  791 |       await riskAlertListPage.waitForListLoaded()
  792 |       
  793 |       // 检查是否有加载更多按钮
  794 |       const hasLoadMore = await riskAlertListPage.isLoadMoreVisible()
  795 |       
  796 |       if (hasLoadMore) {
  797 |         await riskAlertListPage.loadMore()
  798 |         await page.waitForTimeout(500)
  799 |       }
  800 |       
  801 |       await riskAlertListPage.screenshot('scenario5-load-more')
  802 |     })
  803 | 
  804 |     test('AC-5.7 风险提醒应显示涨跌幅信息', async ({ page }) => {
  805 |       await riskAlertListPage.navigate()
  806 |       await riskAlertListPage.waitForPageLoad()
  807 |       await riskAlertListPage.waitForListLoaded()
  808 |       
  809 |       const alertCount = await riskAlertListPage.getAlertItemCount()
  810 |       
  811 |       if (alertCount > 0) {
  812 |         // 验证第一条提醒包含涨跌幅信息
  813 |         const firstAlert = page.locator('.ant-collapse-item').first()
  814 |         await expect(firstAlert).toBeVisible()
  815 |         
  816 |         // 验证包含百分比显示
> 817 |         await expect(firstAlert.locator('text=/%/')).toBeVisible()
      |                                                      ^ Error: expect(locator).toBeVisible() failed
  818 |       }
  819 |       
  820 |       await riskAlertListPage.screenshot('scenario5-change-percent')
  821 |     })
  822 | 
  823 |     test('AC-5.8 风险提醒应能展开查看详情', async ({ page }) => {
  824 |       await riskAlertListPage.navigate()
  825 |       await riskAlertListPage.waitForPageLoad()
  826 |       await riskAlertListPage.waitForListLoaded()
  827 |       
  828 |       const alertCount = await riskAlertListPage.getAlertItemCount()
  829 |       
  830 |       if (alertCount > 0) {
  831 |         // 展开第一个提醒
  832 |         const firstPanel = page.locator('.ant-collapse-header').first()
  833 |         await firstPanel.click()
  834 |         await page.waitForTimeout(300)
  835 |         
  836 |         // 验证详情区域可见
  837 |         const detailsButton = page.locator('text=查看详情').first()
  838 |         if (await detailsButton.isVisible()) {
  839 |           await detailsButton.click()
  840 |           await page.waitForTimeout(300)
  841 |           
  842 |           // 验证明细可见
  843 |           await expect(page.locator('text=触发明细')).toBeVisible()
  844 |         }
  845 |       }
  846 |       
  847 |       await riskAlertListPage.screenshot('scenario5-expand-details')
  848 |     })
  849 |   })
  850 | 
  851 |   /**
  852 |    * 场景6：跨页面导航
  853 |    */
  854 |   test.describe('场景6：跨页面导航', () => {
  855 |     test('AC-6.1 从提醒列表创建提醒后应返回列表', async ({ page }) => {
  856 |       // 导航到列表页
  857 |       await alertListPage.navigate()
  858 |       await alertListPage.waitForPageLoad()
  859 |       
  860 |       // 点击创建提醒
  861 |       await alertListPage.clickCreateAlert()
  862 |       await alertCreatePage.waitForPageLoad()
  863 |       
  864 |       // 模拟创建成功
  865 |       await page.route('**/api/alerts', async (route) => {
  866 |         if (route.request().method() === 'POST') {
  867 |           await route.fulfill({
  868 |             status: 200,
  869 |             contentType: 'application/json',
  870 |             body: JSON.stringify({
  871 |               success: true,
  872 |               data: {
  873 |                 created: true,
  874 |                 alert: { id: 999, symbol: '600000', status: 'ACTIVE' },
  875 |                 message: '提醒创建成功'
  876 |               }
  877 |             })
  878 |           })
  879 |         }
  880 |       })
  881 |       
  882 |       // 填写并提交
  883 |       await alertCreatePage.fillSingleForm({
  884 |         symbol: '600000',
  885 |         symbolType: 'STOCK',
  886 |         alertType: 'PRICE_ABOVE',
  887 |         targetPrice: 10.00
  888 |       })
  889 |       await alertCreatePage.submit()
  890 |       
  891 |       // 验证返回列表页
  892 |       await page.waitForURL('**/alerts', { timeout: 5000 })
  893 |       await expect(page).toHaveURL(/\/alerts/)
  894 |       
  895 |       await alertCreatePage.screenshot('scenario6-create-and-return')
  896 |     })
  897 | 
  898 |     test('AC-6.2 从批量创建结果应能返回列表', async ({ page }) => {
  899 |       await alertCreatePage.navigate('batch')
  900 |       await alertCreatePage.waitForPageLoad()
  901 |       
  902 |       // 模拟部分成功
  903 |       await page.route('**/api/alerts/batch/v2', async (route) => {
  904 |         if (route.request().method() === 'POST') {
  905 |           await route.fulfill({
  906 |             status: 200,
  907 |             contentType: 'application/json',
  908 |             body: JSON.stringify({
  909 |               success: true,
  910 |               data: {
  911 |                 batchId: 'batch-003',
  912 |                 totalCount: 2,
  913 |                 successCount: 1,
  914 |                 failureCount: 1,
  915 |                 successList: [
  916 |                   { symbol: '600000', symbolName: '浦发银行', alertId: 1, createdAt: '2026-04-14T10:00:00Z' }
  917 |                 ],
```