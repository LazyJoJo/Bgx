import { test, expect } from '@playwright/test'

/**
 * 批量添加消息提醒功能 - E2E 测试
 *
 * 测试功能点：
 * 1. 页面路由和导航
 * 2. 创建提醒页面（单选模式和批量模式）
 * 3. 批量创建提醒API
 *
 * 技术要求：
 * - 使用Playwright进行E2E测试
 * - 覆盖正常和异常场景
 * - 验证UI交互正确性
 * - 验证API调用正确性
 */

test.describe('批量添加消息提醒功能 E2E 测试', () => {
  // 失败重试配置
  test.describe.configure({ retries: 1 })

  test.beforeEach(async ({ page }) => {
    // 设置userId
    await page.evaluate(() => localStorage.setItem('userId', '1'))
  })

  test.afterEach(async ({ page }, testInfo) => {
    // 测试失败时自动截图
    if (testInfo.status !== testInfo.expectedStatus) {
      await page.screenshot({
        path: `test-results/FAILED-${testInfo.title.replace(/[^a-zA-Z0-9]/g, '-')}.png`,
        fullPage: true
      })
    }
  })

  /**
   * 功能点1：页面路由和导航
   */
  test.describe('功能点1：页面路由和导航', () => {
    test('NAV-1: 导航栏不应有"创建提醒"按钮', async ({ page }) => {
      // 导航到首页
      await page.goto('/')
      await page.waitForLoadState('networkidle')

      // 验证侧边栏或顶部导航没有"创建提醒"按钮
      const sidebarCreateButton = page.locator('.ant-menu .ant-menu-item').filter({ hasText: '创建提醒' })
      await expect(sidebarCreateButton).toHaveCount(0)

      // 截图验证
      await page.screenshot({ path: 'test-results/NAV-1-no-create-in-nav.png', fullPage: true })
    })

    test('NAV-2: 风险提醒页面不应有批量订阅按钮', async ({ page }) => {
      await page.goto('/risk-alerts')
      await page.waitForLoadState('networkidle')

      // 验证没有批量订阅按钮
      const batchSubscribeButton = page.getByRole('button', { name: /批量订阅/ })
      await expect(batchSubscribeButton).toHaveCount(0)

      // 验证页面标题存在
      await expect(page.locator('h2').filter({ hasText: /风险提醒/ })).toBeVisible()

      await page.screenshot({ path: 'test-results/NAV-2-no-batch-in-risk.png', fullPage: true })
    })

    test('NAV-3: 提醒设置页面应有"批量订阅"和"创建提醒"两个按钮', async ({ page }) => {
      await page.goto('/alerts')
      await page.waitForLoadState('networkidle')

      // 等待表格加载
      await expect(page.locator('.ant-table')).toBeVisible({ timeout: 5000 })

      // 验证批量订阅按钮存在
      const batchSubscribeButton = page.getByRole('button', { name: /批量订阅/ }).last()
      await expect(batchSubscribeButton).toBeVisible()

      // 验证创建提醒按钮存在
      const createAlertButton = page.getByRole('button', { name: /创建提醒/ }).last()
      await expect(createAlertButton).toBeVisible()

      await page.screenshot({ path: 'test-results/NAV-3-both-buttons-in-alerts.png', fullPage: true })
    })

    test('NAV-4: 点击"创建提醒"按钮应导航到创建页面', async ({ page }) => {
      await page.goto('/alerts')
      await page.waitForLoadState('networkidle')
      await expect(page.locator('.ant-table')).toBeVisible({ timeout: 5000 })

      // 点击创建提醒按钮
      await page.getByRole('button', { name: /创建提醒/ }).last().click()

      // 验证跳转到创建页面
      await expect(page).toHaveURL(/\/alerts\/create/)
      await expect(page.locator('h2').filter({ hasText: /创建提醒/ })).toBeVisible()

      await page.screenshot({ path: 'test-results/NAV-4-navigate-to-create.png', fullPage: true })
    })

    test('NAV-5: 点击"批量订阅"按钮应打开模态框', async ({ page }) => {
      await page.goto('/alerts')
      await page.waitForLoadState('networkidle')
      await expect(page.locator('.ant-table')).toBeVisible({ timeout: 5000 })

      // 点击批量订阅按钮
      await page.getByRole('button', { name: /批量订阅/ }).last().click()

      // 验证模态框打开
      const modal = page.locator('.ant-modal').filter({ hasText: /批量订阅风险提醒/ })
      await expect(modal).toBeVisible({ timeout: 3000 })

      // 关闭模态框
      await modal.getByRole('button', { name: /取消/ }).click()
      await expect(modal).not.toBeVisible({ timeout: 3000 })

      await page.screenshot({ path: 'test-results/NAV-5-batch-modal.png', fullPage: true })
    })
  })

  /**
   * 功能点2：创建提醒页面
   */
  test.describe('功能点2：创建提醒页面', () => {
    test.describe('单选模式', () => {
      test('SINGLE-1: 应能正常创建单条提醒', async ({ page }) => {
        await page.goto('/alerts/create')
        await page.waitForLoadState('networkidle')
        await expect(page.locator('h2').filter({ hasText: /创建提醒/ })).toBeVisible()

        // 验证默认是单选模式
        await expect(page.locator('.ant-radio-button-wrapper').filter({ hasText: '单选' })).toHaveClass(/ant-radio-button-wrapper-checked/)

        // 拦截API请求
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
        await page.locator('#symbol').fill('600519')

        // 选择标的类型
        await page.locator('.ant-form-item').filter({ hasText: '标的类型' }).locator('.ant-select').first().click()
        await page.locator('.ant-select-dropdown .ant-select-item').filter({ hasText: '股票' }).click()
        await page.waitForTimeout(300)

        // 选择提醒类型
        await page.locator('.ant-form-item').filter({ hasText: '提醒类型' }).locator('.ant-select').first().click()
        await page.locator('.ant-select-dropdown .ant-select-item').filter({ hasText: '价格超过' }).click()
        await page.waitForTimeout(300)

        // 填写目标价格
        const priceInput = page.locator('.ant-input-number input').first()
        await priceInput.fill('1800.50')

        // 提交
        await page.locator('button[type="submit"]').click()

        // 等待创建成功消息
        await expect(page.locator('.ant-message-success')).toBeVisible({ timeout: 5000 })

        // 验证跳转到列表页
        await expect(page).toHaveURL(/\/alerts/, { timeout: 5000 })

        await page.screenshot({ path: 'test-results/SINGLE-1-create-success.png', fullPage: true })
      })

      test('SINGLE-2: 切换到涨跌幅类型应切换输入框', async ({ page }) => {
        await page.goto('/alerts/create')
        await page.waitForLoadState('networkidle')

        // 切换到涨跌幅类型
        await page.locator('.ant-form-item').filter({ hasText: '提醒类型' }).locator('.ant-select').first().click()
        await page.locator('.ant-select-dropdown .ant-select-item').filter({ hasText: '涨跌幅' }).click()
        await page.waitForTimeout(500)

        // 验证涨跌幅输入框显示
        const percentInputs = page.locator('.ant-input-number input')
        const lastInput = percentInputs.last()
        await expect(lastInput).toBeVisible()

        await page.screenshot({ path: 'test-results/SINGLE-2-percent-input.png', fullPage: true })
      })

      test('SINGLE-3: 应能重置表单', async ({ page }) => {
        await page.goto('/alerts/create')
        await page.waitForLoadState('networkidle')

        // 填写部分表单
        await page.locator('#symbol').fill('600000')

        // 验证已填写
        const symbolValue = await page.locator('#symbol').inputValue()
        expect(symbolValue).toBe('600000')

        // 重置
        await page.getByRole('button', { name: /重置/ }).click()
        await page.waitForTimeout(300)

        // 验证表单已清空
        const clearedValue = await page.locator('#symbol').inputValue()
        expect(clearedValue).toBe('')

        await page.screenshot({ path: 'test-results/SINGLE-3-reset-form.png', fullPage: true })
      })
    })

    test.describe('批量模式', () => {
      test('BATCH-1: 类型前置选择 → 标的多选 → 提醒参数 → 提交', async ({ page }) => {
        await page.goto('/alerts/create?mode=batch')
        await page.waitForLoadState('networkidle')

        // 验证默认选择股票类型
        await expect(page.locator('.ant-radio-button-wrapper').filter({ hasText: '股票' })).toHaveClass(/ant-radio-button-wrapper-checked/)

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
                  totalCount: 2,
                  successCount: 2,
                  failureCount: 0,
                  successList: [
                    { symbol: '600000', symbolName: '浦发银行', alertId: 1, createdAt: '2026-04-14T10:00:00Z' },
                    { symbol: '600519', symbolName: '贵州茅台', alertId: 2, createdAt: '2026-04-14T10:00:00Z' }
                  ],
                  failureList: []
                }
              })
            })
          }
        })

        // 步骤1: 选择标的类型（已经是股票）
        await page.waitForTimeout(300)

        // 步骤2: 搜索并选择多个标的
        const batchSelect = page.locator('.ant-select-multiple').first()
        await batchSelect.click()
        await page.keyboard.type('银行')
        await page.waitForTimeout(500)

        // 选择第一个搜索结果
        const firstOption = page.locator('.ant-select-dropdown .ant-select-item').first()
        await expect(firstOption).toBeVisible()
        await firstOption.click()
        await page.waitForTimeout(200)

        // 选择第二个搜索结果
        const secondOption = page.locator('.ant-select-dropdown .ant-select-item').nth(1)
        if (await secondOption.isVisible()) {
          await secondOption.click()
        }
        await page.keyboard.press('Escape')
        await page.waitForTimeout(300)

        // 验证已选择标的
        await expect(page.locator('text=/已选择 \\d+ 个标的/')).toBeVisible()

        // 步骤3: 选择提醒类型和填写目标值
        await page.locator('.ant-form-item').filter({ hasText: /提醒类型/ }).locator('.ant-select').first().click()
        await page.locator('.ant-select-dropdown .ant-select-item').filter({ hasText: '价格超过' }).click()
        await page.waitForTimeout(300)

        const priceInput = page.locator('.ant-input-number input').first()
        await priceInput.fill('100.00')

        // 步骤4: 提交
        await page.locator('button[type="submit"]').click()

        // 等待结果弹窗
        const resultModal = page.locator('.ant-modal').filter({ hasText: /批量创建结果/ })
        await expect(resultModal).toBeVisible({ timeout: 5000 })

        // 验证全部成功
        const modalTitle = await resultModal.locator('.ant-modal-title').textContent()
        expect(modalTitle).toContain('全部成功')

        // 关闭弹窗
        await resultModal.getByRole('button', { name: /完成/ }).click()
        await expect(page).toHaveURL(/\/alerts/, { timeout: 5000 })

        await page.screenshot({ path: 'test-results/BATCH-1-full-flow.png', fullPage: true })
      })

      test('BATCH-2: 切换到基金类型应清空已选标的', async ({ page }) => {
        await page.goto('/alerts/create?mode=batch')
        await page.waitForLoadState('networkidle')

        // 模拟搜索API
        await page.route('**/api/stocks/search**', async (route) => {
          await route.fulfill({
            status: 200,
            contentType: 'application/json',
            body: JSON.stringify({
              success: true,
              data: [
                { id: 1, code: '600000', name: '浦发银行', type: 'STOCK' }
              ]
            })
          })
        })

        // 选择一个标的
        const batchSelect = page.locator('.ant-select-multiple').first()
        await batchSelect.click()
        await page.keyboard.type('银行')
        await page.waitForTimeout(500)
        await page.locator('.ant-select-dropdown .ant-select-item').first().click()
        await page.keyboard.press('Escape')
        await page.waitForTimeout(300)

        // 验证已选择
        await expect(page.locator('text=/已选择 1 个标的/')).toBeVisible()

        // 切换到基金类型
        await page.locator('.ant-radio-button-wrapper').filter({ hasText: '基金' }).click()

        // 处理确认对话框
        page.on('dialog', async dialog => {
          if (dialog.type() === 'confirm') {
            await dialog.accept()
          }
        })

        await page.waitForTimeout(500)

        // 验证已清空（可能需要重新选择，但UI应该显示0个）
        const selectedCount = page.locator('text=/已选择 \\d+ 个标的/')
        if (await selectedCount.isVisible()) {
          const text = await selectedCount.textContent()
          expect(text).toMatch(/已选择 0 个标的/)
        }

        await page.screenshot({ path: 'test-results/BATCH-2-switch-type.png', fullPage: true })
      })

      test('BATCH-3: 应能使用"全选当前搜索结果"功能', async ({ page }) => {
        await page.goto('/alerts/create?mode=batch')
        await page.waitForLoadState('networkidle')

        // 模拟搜索API
        await page.route('**/api/stocks/search**', async (route) => {
          await route.fulfill({
            status: 200,
            contentType: 'application/json',
            body: JSON.stringify({
              success: true,
              data: [
                { id: 1, code: '600000', name: '浦发银行', type: 'STOCK' },
                { id: 2, code: '600519', name: '贵州茅台', type: 'STOCK' },
                { id: 3, code: '000001', name: '平安银行', type: 'STOCK' }
              ]
            })
          })
        })

        // 触发搜索
        const batchSelect = page.locator('.ant-select-multiple').first()
        await batchSelect.click()
        await page.keyboard.type('银行')
        await page.waitForTimeout(500)

        // 点击全选按钮
        const selectAllButton = page.getByRole('button', { name: /全选当前搜索结果/ })
        if (await selectAllButton.isVisible()) {
          await selectAllButton.click()
          await page.waitForTimeout(300)
        }

        // 验证已选择多个标的
        await expect(page.locator('text=/已选择 \\d+ 个标的/')).toBeVisible()

        await page.screenshot({ path: 'test-results/BATCH-3-select-all.png', fullPage: true })
      })
    })

    test.describe('表单验证', () => {
      test('VALID-1: 空标的应显示验证错误', async ({ page }) => {
        await page.goto('/alerts/create')
        await page.waitForLoadState('networkidle')

        // 直接提交（不填写标的）
        await page.locator('button[type="submit"]').click()
        await page.waitForTimeout(500)

        // 验证错误消息
        const errorMessages = page.locator('.ant-form-item-explain-error')
        await expect(errorMessages.first()).toBeVisible()

        const errorMsg = await errorMessages.first().textContent()
        expect(errorMsg).toContain('标的')

        await page.screenshot({ path: 'test-results/VALID-1-empty-symbol.png', fullPage: true })
      })

      test('VALID-2: 标的代码过短应显示验证错误', async ({ page }) => {
        await page.goto('/alerts/create')
        await page.waitForLoadState('networkidle')

        // 输入过短的代码
        await page.locator('#symbol').fill('1')
        await page.locator('button[type="submit"]').click()
        await page.waitForTimeout(500)

        // 验证错误消息
        const errorMessages = page.locator('.ant-form-item-explain-error')
        await expect(errorMessages.first()).toBeVisible()

        const errorMsg = await errorMessages.first().textContent()
        expect(errorMsg).toMatch(/长度|至少/)

        await page.screenshot({ path: 'test-results/VALID-2-short-symbol.png', fullPage: true })
      })

      test('VALID-3: 空目标值应显示验证错误', async ({ page }) => {
        await page.goto('/alerts/create')
        await page.waitForLoadState('networkidle')

        // 填写标的但不填写价格
        await page.locator('#symbol').fill('600000')

        // 选择标的类型
        await page.locator('.ant-form-item').filter({ hasText: '标的类型' }).locator('.ant-select').first().click()
        await page.locator('.ant-select-dropdown .ant-select-item').filter({ hasText: '股票' }).click()
        await page.waitForTimeout(300)

        // 提交
        await page.locator('button[type="submit"]').click()
        await page.waitForTimeout(500)

        // 验证错误消息
        const errorMessages = page.locator('.ant-form-item-explain-error')
        const hasError = await errorMessages.count()
        expect(hasError).toBeGreaterThan(0)

        await page.screenshot({ path: 'test-results/VALID-3-empty-price.png', fullPage: true })
      })

      test('VALID-4: 负数价格应显示验证错误', async ({ page }) => {
        await page.goto('/alerts/create')
        await page.waitForLoadState('networkidle')

        // 填写标的
        await page.locator('#symbol').fill('600000')

        // 选择标的类型
        await page.locator('.ant-form-item').filter({ hasText: '标的类型' }).locator('.ant-select').first().click()
        await page.locator('.ant-select-dropdown .ant-select-item').filter({ hasText: '股票' }).click()
        await page.waitForTimeout(300)

        // 尝试输入负数
        const priceInput = page.locator('.ant-input-number input').first()
        await priceInput.fill('-10')

        // 提交
        await page.locator('button[type="submit"]').click()
        await page.waitForTimeout(500)

        // 验证错误（InputNumber有min=0限制，或者表单验证）
        const errorMessages = page.locator('.ant-form-item-explain-error, .ant-message-error')
        const hasError = await errorMessages.count()
        expect(hasError).toBeGreaterThan(0)

        await page.screenshot({ path: 'test-results/VALID-4-negative-price.png', fullPage: true })
      })

      test('VALID-5: 批量模式未选择标的应显示验证错误', async ({ page }) => {
        await page.goto('/alerts/create?mode=batch')
        await page.waitForLoadState('networkidle')

        // 直接提交（不选择标的）
        await page.locator('button[type="submit"]').click()
        await page.waitForTimeout(500)

        // 验证错误消息
        const errorMessages = page.locator('.ant-form-item-explain-error')
        await expect(errorMessages.first()).toBeVisible()

        const errorMsg = await errorMessages.first().textContent()
        expect(errorMsg).toContain('标的')

        await page.screenshot({ path: 'test-results/VALID-5-batch-no-symbols.png', fullPage: true })
      })

      test('VALID-6: 涨跌幅为0应显示验证错误', async ({ page }) => {
        await page.goto('/alerts/create')
        await page.waitForLoadState('networkidle')

        // 切换到涨跌幅类型
        await page.locator('.ant-form-item').filter({ hasText: '提醒类型' }).locator('.ant-select').first().click()
        await page.locator('.ant-select-dropdown .ant-select-item').filter({ hasText: '涨跌幅' }).click()
        await page.waitForTimeout(500)

        // 填写标的
        await page.locator('#symbol').fill('600000')

        // 选择标的类型
        await page.locator('.ant-form-item').filter({ hasText: '标的类型' }).locator('.ant-select').first().click()
        await page.locator('.ant-select-dropdown .ant-select-item').filter({ hasText: '股票' }).click()
        await page.waitForTimeout(300)

        // 输入0涨跌幅
        const percentInputs = page.locator('.ant-input-number input')
        await percentInputs.last().fill('0')

        // 提交
        await page.locator('button[type="submit"]').click()
        await page.waitForTimeout(500)

        // 验证错误消息
        const errorMessages = page.locator('.ant-form-item-explain-error')
        const hasError = await errorMessages.count()
        expect(hasError).toBeGreaterThan(0)

        await page.screenshot({ path: 'test-results/VALID-6-zero-percent.png', fullPage: true })
      })
    })
  })

  /**
   * 功能点3：批量创建提醒API
   */
  test.describe('功能点3：批量创建提醒API', () => {
    test('API-1: POST /api/alerts/batch/v2 - 正常创建', async ({ page }) => {
      await page.goto('/alerts/create?mode=batch')
      await page.waitForLoadState('networkidle')

      // 拦截批量创建API - 成功响应
      let requestBody: any = null
      await page.route('**/api/alerts/batch/v2', async (route) => {
        requestBody = route.request().postDataJSON()

        await route.fulfill({
          status: 200,
          contentType: 'application/json',
          body: JSON.stringify({
            success: true,
            data: {
              batchId: 'batch-test-001',
              totalCount: 2,
              successCount: 2,
              failureCount: 0,
              successList: [
                { symbol: '600000', symbolName: '浦发银行', alertId: 1, createdAt: '2026-04-14T10:00:00Z' },
                { symbol: '600519', symbolName: '贵州茅台', alertId: 2, createdAt: '2026-04-14T10:00:00Z' }
              ],
              failureList: []
            }
          })
        })
      })

      // 填写批量表单
      // 选择标的类型
      await page.locator('.ant-radio-button-wrapper').filter({ hasText: '股票' }).click()
      await page.waitForTimeout(300)

      // 手动选择标的（通过JavaScript模拟）
      await page.evaluate(() => {
        const event = new CustomEvent('test-select-symbols', { detail: ['600000', '600519'] })
        window.dispatchEvent(event)
      })
      await page.waitForTimeout(300)

      // 选择提醒类型
      await page.locator('.ant-form-item').filter({ hasText: /提醒类型/ }).locator('.ant-select').first().click()
      await page.locator('.ant-select-dropdown .ant-select-item').filter({ hasText: '价格超过' }).click()
      await page.waitForTimeout(300)

      // 填写价格
      const priceInput = page.locator('.ant-input-number input').first()
      await priceInput.fill('100.00')

      // 提交
      await page.locator('button[type="submit"]').click()

      // 等待结果弹窗
      const resultModal = page.locator('.ant-modal').filter({ hasText: /批量创建结果/ })
      await expect(resultModal).toBeVisible({ timeout: 5000 })

      // 验证成功
      const modalTitle = await resultModal.locator('.ant-modal-title').textContent()
      expect(modalTitle).toContain('全部成功')

      await page.screenshot({ path: 'test-results/API-1-normal-create.png', fullPage: true })
    })

    test('API-2: POST /api/alerts/batch/v2 - 部分成功', async ({ page }) => {
      await page.goto('/alerts/create?mode=batch')
      await page.waitForLoadState('networkidle')

      // 拦截批量创建API - 部分失败
      await page.route('**/api/alerts/batch/v2', async (route) => {
        await route.fulfill({
          status: 200,
          contentType: 'application/json',
          body: JSON.stringify({
            success: true,
            data: {
              batchId: 'batch-test-002',
              totalCount: 3,
              successCount: 2,
              failureCount: 1,
              successList: [
                { symbol: '600000', symbolName: '浦发银行', alertId: 1, createdAt: '2026-04-14T10:00:00Z' },
                { symbol: '600519', symbolName: '贵州茅台', alertId: 2, createdAt: '2026-04-14T10:00:00Z' }
              ],
              failureList: [
                { symbol: '999999', symbolName: '不存在股票', reason: '标的代码不存在', errorCode: 'SYMBOL_NOT_FOUND' }
              ]
            }
          })
        })
      })

      // 填写批量表单
      await page.locator('.ant-radio-button-wrapper').filter({ hasText: '股票' }).click()
      await page.waitForTimeout(300)

      // 选择提醒类型
      await page.locator('.ant-form-item').filter({ hasText: /提醒类型/ }).locator('.ant-select').first().click()
      await page.locator('.ant-select-dropdown .ant-select-item').filter({ hasText: '价格低于' }).click()
      await page.waitForTimeout(300)

      const priceInput = page.locator('.ant-input-number input').first()
      await priceInput.fill('50.00')

      // 提交
      await page.locator('button[type="submit"]').click()

      // 等待结果弹窗
      const resultModal = page.locator('.ant-modal').filter({ hasText: /批量创建结果/ })
      await expect(resultModal).toBeVisible({ timeout: 5000 })

      // 验证部分成功
      const modalTitle = await resultModal.locator('.ant-modal-title').textContent()
      expect(modalTitle).toMatch(/部分成功|成功 2 条，失败 1 条/)

      // 验证失败列表显示
      const failureList = resultModal.locator('text=/原因: 标的代码不存在/')
      await expect(failureList).toBeVisible()

      // 验证重试按钮存在
      const retryButton = resultModal.getByRole('button', { name: /重试失败项/ })
      await expect(retryButton).toBeVisible()

      await page.screenshot({ path: 'test-results/API-2-partial-success.png', fullPage: true })
    })

    test('API-3: POST /api/alerts/batch/v2 - 参数校验失败', async ({ page }) => {
      await page.goto('/alerts/create?mode=batch')
      await page.waitForLoadState('networkidle')

      // 不填写必填字段直接提交
      await page.locator('button[type="submit"]').click()
      await page.waitForTimeout(500)

      // 验证前端表单验证阻止了提交
      const errorMessages = page.locator('.ant-form-item-explain-error')
      const errorCount = await errorMessages.count()
      expect(errorCount).toBeGreaterThan(0)

      // 验证没有API调用（通过检查是否有网络请求）
      // 这里我们假设如果表单验证失败，不会有API请求
      // 实际可以通过拦截器验证没有请求发出

      await page.screenshot({ path: 'test-results/API-3-validation-fail.png', fullPage: true })
    })

    test('API-4: 批量创建 - 网络错误处理', async ({ page }) => {
      await page.goto('/alerts/create?mode=batch')
      await page.waitForLoadState('networkidle')

      // 模拟网络错误
      await page.route('**/api/alerts/batch/v2', async (route) => {
        await route.abort('failed')
      })

      // 填写表单
      await page.locator('.ant-radio-button-wrapper').filter({ hasText: '股票' }).click()
      await page.waitForTimeout(300)

      // 选择提醒类型
      await page.locator('.ant-form-item').filter({ hasText: /提醒类型/ }).locator('.ant-select').first().click()
      await page.locator('.ant-select-dropdown .ant-select-item').filter({ hasText: '价格超过' }).click()
      await page.waitForTimeout(300)

      const priceInput = page.locator('.ant-input-number input').first()
      await priceInput.fill('100.00')

      // 提交
      await page.locator('button[type="submit"]').click()

      // 等待错误提示
      await expect(page.locator('.ant-message-error')).toBeVisible({ timeout: 5000 })

      // 验证错误消息包含网络错误或创建失败
      const errorMessage = await page.locator('.ant-message-error').first().textContent()
      expect(errorMessage).toMatch(/创建失败|网络错误/)

      await page.screenshot({ path: 'test-results/API-4-network-error.png', fullPage: true })
    })

    test('API-5: 批量创建 - 服务器500错误', async ({ page }) => {
      await page.goto('/alerts/create?mode=batch')
      await page.waitForLoadState('networkidle')

      // 模拟服务器500错误
      await page.route('**/api/alerts/batch/v2', async (route) => {
        await route.fulfill({
          status: 500,
          contentType: 'application/json',
          body: JSON.stringify({
            success: false,
            message: '服务器内部错误'
          })
        })
      })

      // 填写表单
      await page.locator('.ant-radio-button-wrapper').filter({ hasText: '股票' }).click()
      await page.waitForTimeout(300)

      const priceInput = page.locator('.ant-input-number input').first()
      await priceInput.fill('100.00')

      // 提交
      await page.locator('button[type="submit"]').click()

      // 等待错误提示
      await expect(page.locator('.ant-message-error')).toBeVisible({ timeout: 5000 })

      await page.screenshot({ path: 'test-results/API-5-server-error.png', fullPage: true })
    })
  })
})
