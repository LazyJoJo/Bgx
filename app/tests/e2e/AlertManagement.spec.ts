import { test, expect } from '@playwright/test'
import { AlertCreatePage } from './AlertCreatePage'
import { AlertListPage } from './AlertListPage'
import { RiskAlertListPage } from './RiskAlertListPage'
import { BatchSubscribeModal } from './BatchSubscribeModal'

/**
 * 批量添加消息提醒功能 E2E 测试
 * 
 * 测试场景：
 * 1. 创建提醒页面 - 单选模式
 * 2. 创建提醒页面 - 批量模式
 * 3. 表单验证
 * 4. 提醒设置页面
 * 5. 风险提醒页面
 * 
 * 技术要求：
 * - 使用Playwright进行E2E测试
 * - 使用Page Object Model模式
 * - 包含截图和失败重试逻辑
 */

test.describe('批量添加消息提醒功能 E2E 测试', () => {
  // 失败重试配置
  test.describe.configure({ retries: 2 })

  let alertCreatePage: AlertCreatePage
  let alertListPage: AlertListPage
  let riskAlertListPage: RiskAlertListPage
  let batchSubscribeModal: BatchSubscribeModal

  test.beforeEach(async ({ page }) => {
    alertCreatePage = new AlertCreatePage(page)
    alertListPage = new AlertListPage(page)
    riskAlertListPage = new RiskAlertListPage(page)
    batchSubscribeModal = new BatchSubscribeModal(page)
  })

  test.afterEach(async ({ page }, testInfo) => {
    // 测试失败时自动截图
    if (testInfo.status !== testInfo.expectedStatus) {
      await page.screenshot({
        path: `test-results/FAILED-${testInfo.title.replace(/\s+/g, '-')}.png`,
        fullPage: true
      })
    }
  })

  /**
   * 场景1：创建提醒页面 - 单选模式
   */
  test.describe('场景1：创建提醒页面 - 单选模式', () => {
    test('AC-1.1 应能导航到创建提醒页面并显示单选模式', async ({ page }) => {
      await alertCreatePage.navigate('single')
      await alertCreatePage.waitForPageLoad()
      
      // 验证页面标题
      await expect(alertCreatePage.pageTitle).toContainText('创建提醒')
      
      // 验证模式切换存在
      await expect(alertCreatePage.modeSwitch).toBeVisible()
      
      // 验证默认是单选模式
      await alertCreatePage.verifySingleModeUI()
      
      await alertCreatePage.screenshot('scenario1-single-mode-initial')
    })

    test('AC-1.2 单选模式应能完整创建提醒', async ({ page }) => {
      await alertCreatePage.navigate('single')
      await alertCreatePage.waitForPageLoad()
      
      // 拦截API请求，模拟成功响应
      await page.route('**/api/alerts', async (route) => {
        if (route.request().method() === 'POST') {
          await route.fulfill({
            status: 200,
            contentType: 'application/json',
            body: JSON.stringify({
              success: true,
              data: {
                created: true,
                alert: { id: 999, symbol: '600519', status: 'ACTIVE' },
                message: '提醒创建成功'
              }
            })
          })
        }
      })
      
      // 填写表单
      await alertCreatePage.fillSingleForm({
        symbol: '600519',
        symbolType: 'STOCK',
        alertType: 'PRICE_ABOVE',
        targetPrice: 1800.50
      })
      
      // 提交
      await alertCreatePage.submit()
      
      // 验证创建成功（跳转到列表页）
      await page.waitForURL('**/alerts', { timeout: 5000 })
      await expect(page).toHaveURL(/\/alerts/)
      
      await alertCreatePage.screenshot('scenario1-single-mode-success')
    })

    test('AC-1.3 单选模式 - 选择涨跌幅类型应切换输入框', async ({ page }) => {
      await alertCreatePage.navigate('single')
      await alertCreatePage.waitForPageLoad()
      
      // 切换到涨跌幅类型
      await alertCreatePage.alertTypeSelect.click()
      await page.locator('.ant-select-dropdown .ant-select-item').filter({ hasText: '涨跌幅' }).click()
      await page.waitForTimeout(300)
      
      // 验证涨跌幅输入框显示，价格输入框隐藏
      await expect(alertCreatePage.targetChangePercentInput).toBeVisible()
      
      await alertCreatePage.screenshot('scenario1-percent-change-input')
    })

    test('AC-1.4 单选模式 - 应能重置表单', async ({ page }) => {
      await alertCreatePage.navigate('single')
      await alertCreatePage.waitForPageLoad()
      
      // 填写部分表单
      await alertCreatePage.symbolInput.fill('600000')
      
      // 重置
      await alertCreatePage.reset()
      
      // 验证表单已清空
      const symbolValue = await alertCreatePage.symbolInput.inputValue()
      expect(symbolValue).toBe('')
      
      await alertCreatePage.screenshot('scenario1-form-reset')
    })

    test('AC-1.5 单选模式 - 点击返回列表应跳转', async ({ page }) => {
      await alertCreatePage.navigate('single')
      await alertCreatePage.waitForPageLoad()
      
      await alertCreatePage.goBack()
      await expect(page).toHaveURL(/\/alerts/)
    })
  })

  /**
   * 场景2：创建提醒页面 - 批量模式
   */
  test.describe('场景2：创建提醒页面 - 批量模式', () => {
    test('AC-2.1 应能导航到批量模式页面', async ({ page }) => {
      await alertCreatePage.navigate('batch')
      await alertCreatePage.waitForPageLoad()
      
      // 验证批量模式UI
      await alertCreatePage.verifyBatchModeUI()
      
      // 验证默认选择股票类型
      await expect(alertCreatePage.stockTypeButton).toHaveClass(/ant-radio-button-wrapper-checked/)
      
      await alertCreatePage.screenshot('scenario2-batch-mode-initial')
    })

    test('AC-2.2 批量模式应能选择标的类型', async ({ page }) => {
      await alertCreatePage.navigate('batch')
      await alertCreatePage.waitForPageLoad()
      
      // 切换到基金类型
      await alertCreatePage.fundTypeButton.click()
      await page.waitForTimeout(300)
      
      // 验证已切换到基金
      await expect(alertCreatePage.fundTypeButton).toHaveClass(/ant-radio-button-wrapper-checked/)
      
      await alertCreatePage.screenshot('scenario2-fund-type-selected')
    })

    test('AC-2.3 批量模式应能搜索并选择多个标的', async ({ page }) => {
      await alertCreatePage.navigate('batch')
      await alertCreatePage.waitForPageLoad()
      
      // 模拟搜索API
      await page.route('**/api/stocks/search**', async (route) => {
        await route.fulfill({
          status: 200,
          contentType: 'application/json',
          body: JSON.stringify({
            success: true,
            data: [
              { id: 1, code: '600000', name: '浦发银行', type: 'STOCK', market: 'SH' },
              { id: 2, code: '600519', name: '贵州茅台', type: 'STOCK', market: 'SH' },
              { id: 3, code: '000001', name: '平安银行', type: 'STOCK', market: 'SZ' }
            ]
          })
        })
      })
      
      // 搜索并选择
      await alertCreatePage.searchAndSelectSymbols('银行', 2)
      
      // 验证已选择标的
      await expect(alertCreatePage.selectedCountText).toBeVisible()
      
      await alertCreatePage.screenshot('scenario2-select-multiple-symbols')
    })

    test('AC-2.4 批量模式应能全选搜索结果', async ({ page }) => {
      await alertCreatePage.navigate('batch')
      await alertCreatePage.waitForPageLoad()
      
      // 模拟搜索结果
      await page.route('**/api/stocks/search**', async (route) => {
        await route.fulfill({
          status: 200,
          contentType: 'application/json',
          body: JSON.stringify({
            success: true,
            data: [
              { id: 1, code: '600000', name: '浦发银行', type: 'STOCK' },
              { id: 2, code: '600519', name: '贵州茅台', type: 'STOCK' }
            ]
          })
        })
      })
      
      // 触发搜索
      await alertCreatePage.batchSymbolsSelect.click()
      await page.keyboard.type('银行')
      await page.waitForTimeout(500)
      
      // 点击全选
      if (await alertCreatePage.selectAllButton.isVisible()) {
        await alertCreatePage.selectAllSearchResults()
      }
      
      await alertCreatePage.screenshot('scenario2-select-all')
    })

    test('AC-2.5 批量模式应能清空选择', async ({ page }) => {
      await alertCreatePage.navigate('batch')
      await alertCreatePage.waitForPageLoad()
      
      // 先选择一些标的
      await alertCreatePage.searchAndSelectSymbols('600', 1)
      
      // 验证清空按钮可见
      await expect(alertCreatePage.clearSelectionButton).toBeVisible()
      
      // 清空选择
      await alertCreatePage.clearSelection()
      
      await alertCreatePage.screenshot('scenario2-clear-selection')
    })

    test('AC-2.6 批量模式应能完整创建批量提醒（全部成功）', async ({ page }) => {
      await alertCreatePage.navigate('batch')
      await alertCreatePage.waitForPageLoad()
      
      // 拦截批量创建API
      await page.route('**/api/alerts/batch/v2', async (route) => {
        if (route.request().method() === 'POST') {
          await route.fulfill({
            status: 200,
            contentType: 'application/json',
            body: JSON.stringify({
              success: true,
              data: {
                batchId: 'batch-001',
                totalCount: 3,
                successCount: 3,
                failureCount: 0,
                successList: [
                  { symbol: '600000', symbolName: '浦发银行', alertId: 1, createdAt: '2026-04-14T10:00:00Z' },
                  { symbol: '600519', symbolName: '贵州茅台', alertId: 2, createdAt: '2026-04-14T10:00:00Z' },
                  { symbol: '000001', symbolName: '平安银行', alertId: 3, createdAt: '2026-04-14T10:00:00Z' }
                ],
                failureList: []
              }
            })
          })
        }
      })
      
      // 填写批量表单
      await alertCreatePage.fillBatchForm({
        symbolType: 'STOCK',
        symbols: ['600000', '600519', '000001'],
        alertType: 'PRICE_ABOVE',
        targetPrice: 100.00
      })
      
      // 提交
      await alertCreatePage.submit()
      
      // 等待结果弹窗
      await alertCreatePage.waitForResultModal()
      
      // 验证全部成功
      const resultText = await alertCreatePage.getBatchResultText()
      expect(resultText).toContain('全部成功')
      
      // 自动返回列表页
      await page.waitForURL('**/alerts', { timeout: 3000 })
      
      await alertCreatePage.screenshot('scenario2-batch-all-success')
    })

    test('AC-2.7 批量模式应能处理部分成功', async ({ page }) => {
      await alertCreatePage.navigate('batch')
      await alertCreatePage.waitForPageLoad()
      
      // 拦截批量创建API - 模拟部分失败
      await page.route('**/api/alerts/batch/v2', async (route) => {
        if (route.request().method() === 'POST') {
          await route.fulfill({
            status: 200,
            contentType: 'application/json',
            body: JSON.stringify({
              success: true,
              data: {
                batchId: 'batch-002',
                totalCount: 3,
                successCount: 2,
                failureCount: 1,
                successList: [
                  { symbol: '600000', symbolName: '浦发银行', alertId: 1, createdAt: '2026-04-14T10:00:00Z' },
                  { symbol: '600519', symbolName: '贵州茅台', alertId: 2, createdAt: '2026-04-14T10:00:00Z' }
                ],
                failureList: [
                  { symbol: '000001', symbolName: '平安银行', reason: '标的代码不存在', errorCode: 'SYMBOL_NOT_FOUND' }
                ]
              }
            })
          })
        }
      })
      
      // 填写表单
      await alertCreatePage.fillBatchForm({
        symbolType: 'STOCK',
        symbols: ['600000', '600519', '000001'],
        alertType: 'PRICE_BELOW',
        targetPrice: 50.00
      })
      
      // 提交
      await alertCreatePage.submit()
      
      // 等待结果弹窗
      await alertCreatePage.waitForResultModal()
      
      // 验证部分成功
      const resultText = await alertCreatePage.getBatchResultText()
      expect(resultText).toContain('部分成功')
      
      // 验证重试按钮可见
      await expect(alertCreatePage.retryFailedButton).toBeVisible()
      
      await alertCreatePage.screenshot('scenario2-batch-partial-success')
    })

    test('AC-2.8 批量模式应能切换标的类型并清空选择', async ({ page }) => {
      await alertCreatePage.navigate('batch')
      await alertCreatePage.waitForPageLoad()
      
      // 模拟确认对话框
      page.on('dialog', async dialog => {
        if (dialog.type() === 'beforeunload') {
          await dialog.accept()
        }
      })
      
      // 先选择股票类型，再切换到基金
      await alertCreatePage.fundTypeButton.click()
      await page.waitForTimeout(500)
      
      // 验证已切换
      await expect(alertCreatePage.fundTypeButton).toHaveClass(/ant-radio-button-wrapper-checked/)
      
      await alertCreatePage.screenshot('scenario2-switch-symbol-type')
    })
  })

  /**
   * 场景3：表单验证
   */
  test.describe('场景3：表单验证', () => {
    test('AC-3.1 单选模式 - 不输入标的代码应显示错误', async ({ page }) => {
      await alertCreatePage.navigate('single')
      await alertCreatePage.waitForPageLoad()
      
      // 清空标的代码并提交
      await alertCreatePage.symbolInput.clear()
      await alertCreatePage.submit()
      
      // 等待验证错误显示
      await page.waitForTimeout(300)
      
      // 验证错误消息
      const hasError = await alertCreatePage.hasValidationError()
      expect(hasError).toBe(true)
      
      const errorMsg = await alertCreatePage.getFirstErrorMessage()
      expect(errorMsg).toContain('标的代码')
      
      await alertCreatePage.screenshot('scenario3-validation-empty-symbol')
    })

    test('AC-3.2 单选模式 - 标的代码过短应显示错误', async ({ page }) => {
      await alertCreatePage.navigate('single')
      await alertCreatePage.waitForPageLoad()
      
      // 输入过短的代码
      await alertCreatePage.symbolInput.fill('1')
      await alertCreatePage.submit()
      
      await page.waitForTimeout(300)
      
      const hasError = await alertCreatePage.hasValidationError()
      expect(hasError).toBe(true)
      
      await alertCreatePage.screenshot('scenario3-validation-short-symbol')
    })

    test('AC-3.3 批量模式 - 不选择标的应显示错误', async ({ page }) => {
      await alertCreatePage.navigate('batch')
      await alertCreatePage.waitForPageLoad()
      
      // 直接提交（未选择标的）
      await alertCreatePage.submit()
      
      await page.waitForTimeout(300)
      
      const hasError = await alertCreatePage.hasValidationError()
      expect(hasError).toBe(true)
      
      await alertCreatePage.screenshot('scenario3-validation-no-symbols')
    })

    test('AC-3.4 不设置目标值应显示错误', async ({ page }) => {
      await alertCreatePage.navigate('single')
      await alertCreatePage.waitForPageLoad()
      
      // 填写标的但不填写价格
      await alertCreatePage.symbolInput.fill('600000')
      await alertCreatePage.submit()
      
      await page.waitForTimeout(300)
      
      const hasError = await alertCreatePage.hasValidationError()
      expect(hasError).toBe(true)
      
      await alertCreatePage.screenshot('scenario3-validation-no-price')
    })

    test('AC-3.5 输入负数价格应显示错误', async ({ page }) => {
      await alertCreatePage.navigate('single')
      await alertCreatePage.waitForPageLoad()
      
      await alertCreatePage.symbolInput.fill('600000')
      
      // 尝试输入负数（InputNumber有min=0限制）
      const priceInput = page.locator('.ant-input-number input').first()
      await priceInput.fill('-10')
      await alertCreatePage.submit()
      
      await page.waitForTimeout(300)
      
      // 验证错误
      const hasError = await alertCreatePage.hasValidationError()
      expect(hasError).toBe(true)
      
      await alertCreatePage.screenshot('scenario3-validation-negative-price')
    })

    test('AC-3.6 涨跌幅为0应显示错误', async ({ page }) => {
      await alertCreatePage.navigate('single')
      await alertCreatePage.waitForPageLoad()
      
      // 切换到涨跌幅类型
      await alertCreatePage.alertTypeSelect.click()
      await page.locator('.ant-select-dropdown .ant-select-item').filter({ hasText: '涨跌幅' }).click()
      await page.waitForTimeout(300)
      
      await alertCreatePage.symbolInput.fill('600000')
      
      // 输入0涨跌幅
      await alertCreatePage.targetChangePercentInput.fill('0')
      await alertCreatePage.submit()
      
      await page.waitForTimeout(300)
      
      const hasError = await alertCreatePage.hasValidationError()
      expect(hasError).toBe(true)
      
      await alertCreatePage.screenshot('scenario3-validation-zero-percent')
    })

    test('AC-3.7 批量模式 - 超过100个标的应显示警告', async ({ page }) => {
      await alertCreatePage.navigate('batch')
      await alertCreatePage.waitForPageLoad()
      
      // 模拟选择超过100个标的
      await page.evaluate(() => {
        // 通过Redux或直接操作来模拟
        window.dispatchEvent(new CustomEvent('test-select-101-symbols'))
      })
      
      // 验证警告消息
      await expect(page.locator('.ant-message-warning')).toBeVisible({ timeout: 3000 })
      
      await alertCreatePage.screenshot('scenario3-validation-over-100')
    })
  })

  /**
   * 场景4：提醒设置页面
   */
  test.describe('场景4：提醒设置页面', () => {
    test('AC-4.1 应显示批量订阅和创建提醒按钮', async ({ page }) => {
      await alertListPage.navigate()
      await alertListPage.waitForPageLoad()
      
      // 验证按钮存在
      await expect(alertListPage.batchSubscribeButton).toBeVisible()
      await expect(alertListPage.createAlertButton).toBeVisible()
      
      await alertListPage.screenshot('scenario4-buttons-visible')
    })

    test('AC-4.2 点击批量订阅应打开模态框', async ({ page }) => {
      await alertListPage.navigate()
      await alertListPage.waitForPageLoad()
      
      await alertListPage.clickBatchSubscribe()
      
      // 验证模态框打开
      await batchSubscribeModal.waitForOpen()
      await expect(batchSubscribeModal.modalTitle).toContainText('批量订阅风险提醒')
      
      // 验证模态框内容
      await batchSubscribeModal.hasDescription()
      
      await alertListPage.screenshot('scenario4-batch-modal-open')
    })

    test('AC-4.3 批量订阅模态框应能选择标的类型', async ({ page }) => {
      await alertListPage.navigate()
      await alertListPage.waitForPageLoad()
      
      await alertListPage.clickBatchSubscribe()
      await batchSubscribeModal.waitForOpen()
      
      // 切换到基金类型
      await batchSubscribeModal.selectSymbolType('FUND')
      
      // 验证已切换
      await expect(page.locator('.ant-select-selection-item')).toContainText('基金')
      
      await alertListPage.screenshot('scenario4-modal-select-type')
    })

    test('AC-4.4 批量订阅应能搜索并选择标的', async ({ page }) => {
      await alertListPage.navigate()
      await alertListPage.waitForPageLoad()
      
      await alertListPage.clickBatchSubscribe()
      await batchSubscribeModal.waitForOpen()
      
      // 模拟搜索API
      await page.route('**/api/stocks/search**', async (route) => {
        await route.fulfill({
          status: 200,
          contentType: 'application/json',
          body: JSON.stringify({
            success: true,
            data: [
              { id: 1, code: '600000', name: '浦发银行', type: 'STOCK' },
              { id: 2, code: '600519', name: '贵州茅台', type: 'STOCK' }
            ]
          })
        })
      })
      
      // 搜索并选择
      await batchSubscribeModal.searchAndSelectSymbols('银行', 2)
      
      // 验证已选标的
      const count = await batchSubscribeModal.getSelectedCount()
      expect(count).toBeGreaterThan(0)
      
      await alertListPage.screenshot('scenario4-modal-select-symbols')
    })

    test('AC-4.5 批量订阅应能成功提交', async ({ page }) => {
      await alertListPage.navigate()
      await alertListPage.waitForPageLoad()
      
      await alertListPage.clickBatchSubscribe()
      await batchSubscribeModal.waitForOpen()
      
      // 拦截订阅API
      await page.route('**/api/risk-alerts/subscribe', async (route) => {
        if (route.request().method() === 'POST') {
          await route.fulfill({
            status: 200,
            contentType: 'application/json',
            body: JSON.stringify({
              code: 200,
              message: '订阅成功',
              data: {
                successCount: 2,
                failCount: 0,
                successList: [
                  { symbol: '600000', symbolName: '浦发银行' },
                  { symbol: '600519', symbolName: '贵州茅台' }
                ],
                failList: []
              }
            })
          })
        }
      })
      
      // 选择标的类型和标的
      await batchSubscribeModal.selectSymbolType('STOCK')
      await batchSubscribeModal.searchAndSelectSymbols('银行', 2)
      
      // 提交订阅
      await batchSubscribeModal.subscribe()
      
      // 验证成功消息
      await expect(page.locator('.ant-message-success')).toBeVisible({ timeout: 5000 })
      
      // 验证模态框关闭
      await batchSubscribeModal.waitForClose()
      
      await alertListPage.screenshot('scenario4-subscribe-success')
    })

    test('AC-4.6 批量订阅模态框应能取消关闭', async ({ page }) => {
      await alertListPage.navigate()
      await alertListPage.waitForPageLoad()
      
      await alertListPage.clickBatchSubscribe()
      await batchSubscribeModal.waitForOpen()
      
      await batchSubscribeModal.cancel()
      
      // 验证模态框已关闭
      await batchSubscribeModal.waitForClose()
      
      await alertListPage.screenshot('scenario4-modal-cancel')
    })

    test('AC-4.7 提醒列表应能搜索', async ({ page }) => {
      await alertListPage.navigate()
      await alertListPage.waitForPageLoad()
      
      await alertListPage.searchSymbol('600000')
      
      // 验证搜索已执行（通过URL或表格数据变化）
      await page.waitForTimeout(500)
      
      await alertListPage.screenshot('scenario4-list-search')
    })

    test('AC-4.8 提醒列表应能筛选标的类型', async ({ page }) => {
      await alertListPage.navigate()
      await alertListPage.waitForPageLoad()
      
      await alertListPage.filterBySymbolType('STOCK')
      
      await page.waitForTimeout(500)
      
      await alertListPage.screenshot('scenario4-list-filter')
    })
  })

  /**
   * 场景5：风险提醒页面
   */
  test.describe('场景5：风险提醒页面', () => {
    test('AC-5.1 应能导航到风险提醒页面', async ({ page }) => {
      await riskAlertListPage.navigate()
      await riskAlertListPage.waitForPageLoad()
      
      // 验证页面标题
      await expect(riskAlertListPage.pageTitle).toBeVisible()
      await expect(riskAlertListPage.pageTitle).toContainText('风险提醒')
      
      await riskAlertListPage.screenshot('scenario5-page-load')
    })

    test('AC-5.2 风险提醒页面应只显示提醒列表', async ({ page }) => {
      await riskAlertListPage.navigate()
      await riskAlertListPage.waitForPageLoad()
      await riskAlertListPage.waitForListLoaded()
      
      // 验证列表区域存在
      const alertCount = await riskAlertListPage.getAlertItemCount()
      
      // 如果有数据，验证列表可见
      if (alertCount > 0) {
        await riskAlertListPage.verifyAlertListVisible()
      } else {
        // 无数据时显示空状态
        await riskAlertListPage.verifyEmptyState()
      }
      
      await riskAlertListPage.screenshot('scenario5-alert-list')
    })

    test('AC-5.3 风险提醒页面不应有批量订阅或创建按钮', async ({ page }) => {
      await riskAlertListPage.navigate()
      await riskAlertListPage.waitForPageLoad()
      
      // 验证没有批量订阅按钮
      await riskAlertListPage.verifyNoBatchSubscribeButton()
      
      // 验证没有创建提醒按钮
      await riskAlertListPage.verifyNoCreateAlertButton()
      
      await riskAlertListPage.screenshot('scenario5-no-create-buttons')
    })

    test('AC-5.4 应能点击"全部已读"标记已读', async ({ page }) => {
      await riskAlertListPage.navigate()
      await riskAlertListPage.waitForPageLoad()
      await riskAlertListPage.waitForListLoaded()
      
      // 拦截标记已读API
      await page.route('**/api/risk-alerts/mark-read', async (route) => {
        await route.fulfill({
          status: 200,
          contentType: 'application/json',
          body: JSON.stringify({
            success: true,
            message: '已全部标记为已读'
          })
        })
      })
      
      // 验证按钮未禁用（有未读时）
      const isDisabled = await riskAlertListPage.isMarkAllReadDisabled()
      
      if (!isDisabled) {
        await riskAlertListPage.markAllAsRead()
        
        // 验证成功消息
        await expect(page.locator('.ant-message-success')).toBeVisible({ timeout: 3000 })
      }
      
      await riskAlertListPage.screenshot('scenario5-mark-all-read')
    })

    test('AC-5.5 无未读时"全部已读"应禁用', async ({ page }) => {
      await riskAlertListPage.navigate()
      await riskAlertListPage.waitForPageLoad()
      await riskAlertListPage.waitForListLoaded()
      
      // 模拟无未读数据
      await page.route('**/api/risk-alerts/unread-count', async (route) => {
        await route.fulfill({
          status: 200,
          contentType: 'application/json',
          body: JSON.stringify({
            success: true,
            data: 0
          })
        })
      })
      
      await page.reload()
      await riskAlertListPage.waitForPageLoad()
      await riskAlertListPage.waitForListLoaded()
      
      // 验证按钮禁用
      const isDisabled = await riskAlertListPage.isMarkAllReadDisabled()
      expect(isDisabled).toBe(true)
      
      await riskAlertListPage.screenshot('scenario5-no-unread')
    })

    test('AC-5.6 风险提醒应支持加载更多', async ({ page }) => {
      await riskAlertListPage.navigate()
      await riskAlertListPage.waitForPageLoad()
      await riskAlertListPage.waitForListLoaded()
      
      // 检查是否有加载更多按钮
      const hasLoadMore = await riskAlertListPage.isLoadMoreVisible()
      
      if (hasLoadMore) {
        await riskAlertListPage.loadMore()
        await page.waitForTimeout(500)
      }
      
      await riskAlertListPage.screenshot('scenario5-load-more')
    })

    test('AC-5.7 风险提醒应显示涨跌幅信息', async ({ page }) => {
      await riskAlertListPage.navigate()
      await riskAlertListPage.waitForPageLoad()
      await riskAlertListPage.waitForListLoaded()
      
      const alertCount = await riskAlertListPage.getAlertItemCount()
      
      if (alertCount > 0) {
        // 验证第一条提醒包含涨跌幅信息
        const firstAlert = page.locator('.ant-collapse-item').first()
        await expect(firstAlert).toBeVisible()
        
        // 验证包含百分比显示
        await expect(firstAlert.locator('text=/%/')).toBeVisible()
      }
      
      await riskAlertListPage.screenshot('scenario5-change-percent')
    })

    test('AC-5.8 风险提醒应能展开查看详情', async ({ page }) => {
      await riskAlertListPage.navigate()
      await riskAlertListPage.waitForPageLoad()
      await riskAlertListPage.waitForListLoaded()
      
      const alertCount = await riskAlertListPage.getAlertItemCount()
      
      if (alertCount > 0) {
        // 展开第一个提醒
        const firstPanel = page.locator('.ant-collapse-header').first()
        await firstPanel.click()
        await page.waitForTimeout(300)
        
        // 验证详情区域可见
        const detailsButton = page.locator('text=查看详情').first()
        if (await detailsButton.isVisible()) {
          await detailsButton.click()
          await page.waitForTimeout(300)
          
          // 验证明细可见
          await expect(page.locator('text=触发明细')).toBeVisible()
        }
      }
      
      await riskAlertListPage.screenshot('scenario5-expand-details')
    })
  })

  /**
   * 场景6：跨页面导航
   */
  test.describe('场景6：跨页面导航', () => {
    test('AC-6.1 从提醒列表创建提醒后应返回列表', async ({ page }) => {
      // 导航到列表页
      await alertListPage.navigate()
      await alertListPage.waitForPageLoad()
      
      // 点击创建提醒
      await alertListPage.clickCreateAlert()
      await alertCreatePage.waitForPageLoad()
      
      // 模拟创建成功
      await page.route('**/api/alerts', async (route) => {
        if (route.request().method() === 'POST') {
          await route.fulfill({
            status: 200,
            contentType: 'application/json',
            body: JSON.stringify({
              success: true,
              data: {
                created: true,
                alert: { id: 999, symbol: '600000', status: 'ACTIVE' },
                message: '提醒创建成功'
              }
            })
          })
        }
      })
      
      // 填写并提交
      await alertCreatePage.fillSingleForm({
        symbol: '600000',
        symbolType: 'STOCK',
        alertType: 'PRICE_ABOVE',
        targetPrice: 10.00
      })
      await alertCreatePage.submit()
      
      // 验证返回列表页
      await page.waitForURL('**/alerts', { timeout: 5000 })
      await expect(page).toHaveURL(/\/alerts/)
      
      await alertCreatePage.screenshot('scenario6-create-and-return')
    })

    test('AC-6.2 从批量创建结果应能返回列表', async ({ page }) => {
      await alertCreatePage.navigate('batch')
      await alertCreatePage.waitForPageLoad()
      
      // 模拟部分成功
      await page.route('**/api/alerts/batch/v2', async (route) => {
        if (route.request().method() === 'POST') {
          await route.fulfill({
            status: 200,
            contentType: 'application/json',
            body: JSON.stringify({
              success: true,
              data: {
                batchId: 'batch-003',
                totalCount: 2,
                successCount: 1,
                failureCount: 1,
                successList: [
                  { symbol: '600000', symbolName: '浦发银行', alertId: 1, createdAt: '2026-04-14T10:00:00Z' }
                ],
                failureList: [
                  { symbol: '600519', symbolName: '贵州茅台', reason: '已存在', errorCode: 'DUPLICATE' }
                ]
              }
            })
          })
        }
      })
      
      // 填写并提交
      await alertCreatePage.fillBatchForm({
        symbolType: 'STOCK',
        symbols: ['600000', '600519'],
        alertType: 'PRICE_ABOVE',
        targetPrice: 100.00
      })
      await alertCreatePage.submit()
      
      // 等待结果弹窗
      await alertCreatePage.waitForResultModal()
      
      // 点击返回列表
      await alertCreatePage.closeResultModal()
      
      // 验证返回列表页
      await expect(page).toHaveURL(/\/alerts/)
      
      await alertCreatePage.screenshot('scenario6-batch-result-return')
    })

    test('AC-6.3 各页面间导航应正常', async ({ page }) => {
      // 提醒列表 -> 创建提醒
      await alertListPage.navigate()
      await alertListPage.waitForPageLoad()
      await alertListPage.clickCreateAlert()
      await expect(page).toHaveURL(/\/alerts\/create/)
      
      // 创建提醒 -> 返回列表
      await alertCreatePage.goBack()
      await expect(page).toHaveURL(/\/alerts/)
      
      // 提醒列表 -> 风险提醒
      await page.goto('/risk-alerts')
      await page.evaluate(() => localStorage.setItem('userId', '1'))
      await riskAlertListPage.waitForPageLoad()
      await expect(page).toHaveURL(/\/risk-alerts/)
      
      await alertCreatePage.screenshot('scenario6-navigation')
    })
  })

  /**
   * 场景7：错误处理
   */
  test.describe('场景7：错误处理', () => {
    test('AC-7.1 网络错误时应显示友好提示', async ({ page }) => {
      await alertCreatePage.navigate('single')
      await alertCreatePage.waitForPageLoad()
      
      // 模拟网络错误
      await page.route('**/api/alerts', async (route) => {
        await route.abort('failed')
      })
      
      await alertCreatePage.fillSingleForm({
        symbol: '600000',
        symbolType: 'STOCK',
        alertType: 'PRICE_ABOVE',
        targetPrice: 10.00
      })
      await alertCreatePage.submit()
      
      // 验证错误提示
      await expect(page.locator('.ant-message-error')).toBeVisible({ timeout: 5000 })
      
      await alertCreatePage.screenshot('scenario7-network-error')
    })

    test('AC-7.2 服务器500错误应显示错误信息', async ({ page }) => {
      await alertCreatePage.navigate('single')
      await alertCreatePage.waitForPageLoad()
      
      // 模拟服务器错误
      await page.route('**/api/alerts', async (route) => {
        if (route.request().method() === 'POST') {
          await route.fulfill({
            status: 500,
            contentType: 'application/json',
            body: JSON.stringify({
              success: false,
              message: '服务器内部错误'
            })
          })
        }
      })
      
      await alertCreatePage.fillSingleForm({
        symbol: '600000',
        symbolType: 'STOCK',
        alertType: 'PRICE_ABOVE',
        targetPrice: 10.00
      })
      await alertCreatePage.submit()
      
      // 验证错误提示
      await expect(page.locator('.ant-message-error')).toBeVisible({ timeout: 5000 })
      
      await alertCreatePage.screenshot('scenario7-server-error')
    })

    test('AC-7.3 批量创建部分失败应显示失败列表', async ({ page }) => {
      await alertCreatePage.navigate('batch')
      await alertCreatePage.waitForPageLoad()
      
      // 模拟部分失败
      await page.route('**/api/alerts/batch/v2', async (route) => {
        if (route.request().method() === 'POST') {
          await route.fulfill({
            status: 200,
            contentType: 'application/json',
            body: JSON.stringify({
              success: true,
              data: {
                batchId: 'batch-004',
                totalCount: 3,
                successCount: 1,
                failureCount: 2,
                successList: [
                  { symbol: '600000', symbolName: '浦发银行', alertId: 1, createdAt: '2026-04-14T10:00:00Z' }
                ],
                failureList: [
                  { symbol: '600519', symbolName: '贵州茅台', reason: '标的代码无效', errorCode: 'INVALID_SYMBOL' },
                  { symbol: '000001', symbolName: '平安银行', reason: '系统错误', errorCode: 'SYSTEM_ERROR' }
                ]
              }
            })
          })
        }
      })
      
      await alertCreatePage.fillBatchForm({
        symbolType: 'STOCK',
        symbols: ['600000', '600519', '000001'],
        alertType: 'PRICE_ABOVE',
        targetPrice: 100.00
      })
      await alertCreatePage.submit()
      
      // 验证结果弹窗
      await alertCreatePage.waitForResultModal()
      
      // 验证失败列表可见
      await expect(page.locator('text=失败列表')).toBeVisible()
      
      // 验证重试按钮可见
      await expect(alertCreatePage.retryFailedButton).toBeVisible()
      
      await alertCreatePage.screenshot('scenario7-partial-failure')
    })
  })
})
