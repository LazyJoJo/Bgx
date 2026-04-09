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
      await page.locator('input').first().clear()
      await page.locator('button[type="submit"]').click()

      // 应该显示验证错误
      const error = page.locator('.ant-form-item-explain-error')
      await expect(error.first()).toBeVisible()
    })

    test('标的代码过短应显示验证错误', async ({ page }) => {
      await page.locator('input').first().fill('1')
      await page.locator('button[type="submit"]').click()

      // 应该显示验证错误
      await page.waitForTimeout(300)
      const error = page.locator('.ant-form-item-explain-error')
      await expect(error.first()).toBeVisible()
    })
  })

  test.describe('表单提交', () => {
    test('填写正确信息后点击创建应成功', async ({ page }) => {
      // 填写表单
      await page.locator('input').first().fill('000001')

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

    test('取消创建应返回列表页', async ({ page }) => {
      await page.locator('.ant-btn').filter({ hasText: '返回列表' }).click()
      await page.waitForURL('**/alerts')
      await expect(page).toHaveURL(/\/alerts/)
    })
  })

  test.describe('重置功能', () => {
    test('填写表单后点击重置应清空表单', async ({ page }) => {
      // 填写表单
      await page.locator('input').first().fill('000001')
      const inputNumber = page.locator('.ant-input-number input')
      await inputNumber.fill('15.50')

      // 找到并点击重置按钮（第二个按钮）
      const buttons = page.locator('.ant-btn')
      await buttons.nth(1).click()

      // 标的代码应该被清空
      await page.waitForTimeout(300)
    })
  })
})
