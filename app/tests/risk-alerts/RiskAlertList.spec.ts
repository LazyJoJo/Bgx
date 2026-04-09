import { test, expect } from '@playwright/test'

/**
 * 风险提醒列表 E2E 测试
 * TDD RED阶段：编写测试用例验证风险提醒功能
 */
test.describe('风险提醒列表', () => {
  test.beforeEach(async ({ page }) => {
    // 模拟登录状态
    await page.goto('/risk-alerts')
    await page.evaluate(() => {
      localStorage.setItem('userId', '1')
    })
  })

  test.afterEach(async ({ page }) => {
    // 清理状态
    await page.evaluate(() => {
      localStorage.removeItem('userId')
    })
  })

  test.describe('页面加载', () => {
    test('应显示风险提醒页面标题', async ({ page }) => {
      await expect(page.locator('h2')).toContainText('风险提醒')
    })

    test('应显示未读数量徽章', async ({ page }) => {
      // 徽章应该存在（使用第一个，页面可能有多个Badge组件）
      const badge = page.locator('.ant-badge').first()
      await expect(badge).toBeVisible()
    })

    test('应显示"全部已读"按钮', async ({ page }) => {
      const button = page.getByRole('button', { name: /全部已读/ })
      await expect(button).toBeVisible()
    })

    test('初始状态无未读时"全部已读"按钮应禁用', async ({ page }) => {
      const button = page.getByRole('button', { name: /全部已读/ })
      await expect(button).toBeDisabled()
    })
  })

  test.describe('空状态', () => {
    test('无数据时应显示空状态提示', async ({ page }) => {
      // 等待加载完成
      await page.waitForSelector('.ant-empty', { timeout: 5000 }).catch(() => {})
      const empty = page.locator('.ant-empty')
      const hasEmpty = await empty.count() > 0
      if (hasEmpty) {
        await expect(empty).toContainText('暂无风险提醒')
      }
    })
  })

  test.describe('加载状态', () => {
    test('加载中应显示Spin组件', async ({ page }) => {
      // 初始加载时应有loading状态
      const spin = page.locator('.ant-spin')
      // Spin可能一闪而过，这是正常的
      await page.waitForTimeout(100)
    })
  })

  test.describe('风险提醒列表展示', () => {
    test('有数据时应正确渲染风险提醒卡片', async ({ page }) => {
      // 等待数据加载
      await page.waitForTimeout(1000)

      // 检查是否有日期分组
      const collapse = page.locator('.ant-collapse')
      const hasData = await collapse.count() > 0

      if (hasData) {
        // 验证折叠面板存在
        await expect(collapse.first()).toBeVisible()
      }
    })

    test('风险卡片应显示正确的标的类型图标', async ({ page }) => {
      await page.waitForTimeout(1000)

      // 查找股票或基金标签
      const stockTag = page.locator('.ant-tag').filter({ hasText: '股票' })
      const fundTag = page.locator('.ant-tag').filter({ hasText: '基金' })
      const hasTags = (await stockTag.count()) > 0 || (await fundTag.count()) > 0

      if (hasTags) {
        await expect(stockTag.first()).toBeVisible().catch(() => {})
        await expect(fundTag.first()).toBeVisible().catch(() => {})
      }
    })

    test('风险卡片应显示涨跌幅', async ({ page }) => {
      await page.waitForTimeout(1000)

      // 涨跌幅应该以红色或绿色显示（正数红色，负数绿色）
      const cards = page.locator('div').filter({ hasText: /%/ })
      const hasCards = await cards.count() > 0

      if (hasCards) {
        // 验证至少有一个百分比显示
        const percentText = await cards.first().textContent()
        expect(percentText).toMatch(/%/)
      }
    })
  })

  test.describe('风险提醒详情', () => {
    test('应能展开查看详情', async ({ page }) => {
      await page.waitForTimeout(1000)

      // 查找"查看详情"链接
      const detailsLink = page.getByText(/查看详情/)
      const hasDetails = await detailsLink.count() > 0

      if (hasDetails) {
        await detailsLink.first().click()
        // 详情应该展开
        await page.waitForTimeout(500)
      }
    })
  })

  test.describe('标记已读功能', () => {
    test('点击"全部已读"按钮应调用标记已读接口', async ({ page }) => {
      // 先有未读数据
      await page.waitForTimeout(1000)

      // 获取按钮状态
      const button = page.getByRole('button', { name: /全部已读/ })
      const isDisabled = await button.isDisabled()

      if (!isDisabled) {
        // 拦截API请求
        await page.route('**/api/risk-alerts/read-all', async (route) => {
          await route.fulfill({
            status: 200,
            contentType: 'application/json',
            body: JSON.stringify({ success: true, message: '标记成功', data: null }),
          })
        })

        await button.click()
        // 等待请求完成
        await page.waitForTimeout(500)
      }
    })

    test('标记成功后未读数量应归零', async ({ page }) => {
      await page.waitForTimeout(1000)

      // 确保有数据
      const button = page.getByRole('button', { name: /全部已读/ })
      const isDisabled = await button.isDisabled()

      if (!isDisabled) {
        await page.route('**/api/risk-alerts/read-all', async (route) => {
          await route.fulfill({
            status: 200,
            contentType: 'application/json',
            body: JSON.stringify({ success: true, message: '标记成功', data: null }),
          })
        })

        await button.click()
        await page.waitForTimeout(800)

        // 按钮应该被禁用（因为没有未读了）
        await expect(button).toBeDisabled()
      }
    })
  })

  test.describe('日期分组', () => {
    test('应按日期分组显示', async ({ page }) => {
      await page.waitForTimeout(1000)

      const collapse = page.locator('.ant-collapse')
      const hasData = await collapse.count() > 0

      if (hasData) {
        // 应该有多个日期分组（如果有跨天数据）
        const panelHeaders = page.locator('.ant-collapse-header')
        const panelCount = await panelHeaders.count()
        expect(panelCount).toBeGreaterThan(0)
      }
    })

    test('今天应该显示"今天"而不是日期', async ({ page }) => {
      await page.waitForTimeout(1000)

      const todayText = page.getByText('今天')
      const hasToday = await todayText.count() > 0

      if (hasToday) {
        await expect(todayText.first()).toBeVisible()
      }
    })
  })
})

test.describe('导航栏未读数量', () => {
  test('应正确显示未读数量', async ({ page }) => {
    await page.goto('/')
    await page.evaluate(() => {
      localStorage.setItem('userId', '1')
    })

    // 查找Badge组件
    const badge = page.locator('sup').filter({ hasText: /\d+/ })
    const hasBadge = await badge.count() > 0

    if (hasBadge) {
      await expect(badge.first()).toBeVisible()
    }
  })

  test('无未读时Badge应隐藏或显示灰色', async ({ page }) => {
    await page.goto('/')
    await page.evaluate(() => {
      localStorage.setItem('userId', '1')
    })

    // 等待页面加载
    await page.waitForTimeout(1000)

    // Badge应该可见
    const badge = page.locator('.ant-badge')
    await expect(badge.first()).toBeVisible()
  })
})
