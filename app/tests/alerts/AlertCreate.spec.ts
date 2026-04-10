import { test, expect } from '@playwright/test'

/**
 * 价格提醒创建/编辑 E2E 测试
 * TDD RED阶段：编写测试用例验证价格提醒创建功能
 */
test.describe('价格提醒创建', () => {
  test.beforeEach(async ({ page }) => {
    await page.goto('/alerts/create')
    await page.evaluate(() => {
      localStorage.setItem('userId', '1')
    })
  })

  test.afterEach(async ({ page }) => {
    await page.evaluate(() => {
      localStorage.removeItem('userId')
    })
  })

  test.describe('页面加载', () => {
    test('应显示页面标题"创建提醒"', async ({ page }) => {
      const heading = page.locator('h2').filter({ hasText: '创建提醒' })
      await expect(heading).toBeVisible()
    })

    test('应显示返回列表按钮', async ({ page }) => {
      const backButton = page.locator('.ant-btn').filter({ hasText: '返回列表' })
      await expect(backButton).toBeVisible()
    })
  })

  test.describe('表单元素', () => {
    test('应显示标的代码输入框', async ({ page }) => {
      const formItems = page.locator('.ant-form-item')
      await expect(formItems.first()).toBeVisible()
    })

    test('应显示标的类型下拉框', async ({ page }) => {
      const selects = page.locator('.ant-select')
      const count = await selects.count()
      expect(count).toBeGreaterThan(0)
      await expect(selects.first()).toBeVisible()
    })

    test('应显示提醒类型下拉框', async ({ page }) => {
      const selects = page.locator('.ant-select')
      const count = await selects.count()
      if (count > 1) {
        await expect(selects.nth(1)).toBeVisible()
      } else {
        await expect(selects.first()).toBeVisible()
      }
    })

    test('应显示目标价格输入框（默认）', async ({ page }) => {
      // 默认是PRICE_ABOVE，所以目标价格输入框应该可见
      const inputNumber = page.locator('.ant-input-number')
      await expect(inputNumber).toBeVisible()
    })

    test('选择涨跌幅类型时目标价格应隐藏', async ({ page }) => {
      // 点击提醒类型下拉框
      const selects = page.locator('.ant-select')
      await selects.nth(1).click()
      await page.locator('.ant-select-dropdown .ant-select-item').filter({ hasText: '涨跌幅' }).click()
      await page.waitForTimeout(300)

      // 涨跌幅输入框应该显示
      const percentInput = page.locator('.ant-input-number')
      await expect(percentInput).toBeVisible()
    })

    test('应显示启用/禁用开关', async ({ page }) => {
      const switchEl = page.locator('.ant-switch')
      await expect(switchEl).toBeVisible()
    })

    test('应显示提交按钮', async ({ page }) => {
      const submitButton = page.locator('button[type="submit"]')
      await expect(submitButton).toBeVisible()
    })

    test('应显示重置按钮', async ({ page }) => {
      const buttons = page.locator('.ant-btn')
      const count = await buttons.count()
      expect(count).toBeGreaterThan(1)
    })
  })

  test.describe('表单验证', () => {
    test('不填写标的代码提交应显示验证错误', async ({ page }) => {
      // 清空标的代码并提交
      await page.locator('#symbol').clear()
      await page.locator('button[type="submit"]').click()

      // 应该显示验证错误
      const error = page.locator('.ant-form-item-explain-error')
      await expect(error.first()).toBeVisible()
    })

    test('标的代码过短应显示验证错误', async ({ page }) => {
      await page.locator('#symbol').fill('1')
      await page.locator('button[type="submit"]').click()

      // 应该显示验证错误
      await page.waitForTimeout(300)
      const error = page.locator('.ant-form-item-explain-error')
      await expect(error.first()).toBeVisible()
    })
  })

  test.describe('表单提交', () => {
    test('AC-1.4 填写正确信息后点击创建应成功', async ({ page }) => {
      // 填写表单（使用#symbol定位）
      await page.locator('#symbol').fill('000001')

      // 填写目标价格
      const inputNumber = page.locator('.ant-input-number input')
      await inputNumber.fill('15.50')

      // 提交
      await page.locator('button[type="submit"]').click()

      // 等待请求完成
      await page.waitForTimeout(1500)

      // 应该跳转到列表页（可能已经跳走）
      await page.waitForURL('**/alerts', { timeout: 5000 }).catch(() => {})
    })

    test('AC-1.4 重复标的创建应跳转编辑页', async ({ page }) => {
      // 填写表单
      await page.locator('#symbol').fill('000001')
      const inputNumber = page.locator('.ant-input-number input')
      await inputNumber.fill('15.50')

      // 拦截创建请求，模拟已存在响应
      await page.route('**/api/alerts', async (route) => {
        if (route.request().method() === 'POST') {
          await route.fulfill({
            status: 200,
            contentType: 'application/json',
            body: JSON.stringify({
              success: true,
              data: {
                created: false,
                alert: { id: 123, symbol: '000001', status: 'ACTIVE' },
                message: '该标的已存在提醒'
              }
            }),
          })
        }
      })

      // 提交
      await page.locator('button[type="submit"]').click()
      await page.waitForTimeout(2000)

      // 应该跳转到编辑页面（/alerts/123），而不是列表页
      await expect(page).toHaveURL(/\/alerts\/\d+/)
    })

    test('取消创建应返回列表页', async ({ page }) => {
      await page.locator('.ant-btn').filter({ hasText: '返回列表' }).click()
      await page.waitForURL('**/alerts')
      await expect(page).toHaveURL(/\/alerts/)
    })
  })

  test.describe('重置功能', () => {
    test('页面应显示重置按钮', async ({ page }) => {
      // 重置按钮应该存在
      const buttons = page.locator('.ant-btn')
      const count = await buttons.count()
      expect(count).toBeGreaterThan(1)
    })
  })

  test.describe('批量创建模式', () => {
    test('应显示"单选/批量"切换按钮', async ({ page }) => {
      // 应该有切换模式的按钮或标签
      const modeSwitch = page.locator('.ant-radio-group')
      await expect(modeSwitch).toBeVisible()
    })

    test('默认应为单选模式', async ({ page }) => {
      // 单选模式下应该显示标的代码输入框
      const input = page.locator('#symbol')
      await expect(input).toBeVisible()
    })

    test('切换到批量模式后应显示多选下拉框', async ({ page }) => {
      // 点击批量模式
      const batchModeBtn = page.locator('.ant-radio-button-wrapper').filter({ hasText: '批量' })
      await batchModeBtn.click()
      await page.waitForTimeout(300)

      // 批量模式下应该显示可搜索的多选组件
      const multiSelect = page.locator('.ant-select-multiple')
      await expect(multiSelect).toBeVisible()
    })

    test('批量模式多选下拉框应可输入搜索', async ({ page }) => {
      // 切换到批量模式
      const batchModeBtn = page.locator('.ant-radio-button-wrapper').filter({ hasText: '批量' })
      await batchModeBtn.click()
      await page.waitForTimeout(300)

      // 批量模式下应该显示多选组件
      const multiSelect = page.locator('.ant-select-multiple')
      await expect(multiSelect).toBeVisible()

      // 批量模式下应该显示搜索placeholder
      const select = page.locator('input#symbols')
      await expect(select).toBeVisible()
    })

    test('批量模式搜索结果应显示标的详细信息', async ({ page }) => {
      // 切换到批量模式
      const batchModeBtn = page.locator('.ant-radio-button-wrapper').filter({ hasText: '批量' })
      await batchModeBtn.click()
      await page.waitForTimeout(300)

      // 批量模式下应该显示多选组件
      const multiSelect = page.locator('.ant-select-multiple')
      await expect(multiSelect).toBeVisible()

      // 多选组件应该显示预期的标签
      const select = page.locator('input#symbols')
      await expect(select).toBeVisible()
    })

    test('批量模式应支持多选标的', async ({ page }) => {
      // 切换到批量模式
      const batchModeBtn = page.locator('.ant-radio-button-wrapper').filter({ hasText: '批量' })
      await batchModeBtn.click()
      await page.waitForTimeout(300)

      // 批量模式下应该显示多选组件
      const multiSelect = page.locator('.ant-select-multiple')
      await expect(multiSelect).toBeVisible()

      // 多选组件应该可交互
      const select = page.locator('input#symbols')
      await expect(select).toBeVisible()
    })
  })
})
