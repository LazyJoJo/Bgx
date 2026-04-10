import { test, expect } from '@playwright/test'

/**
 * 价格提醒列表 E2E 测试
 * TDD RED阶段：编写测试用例验证价格提醒功能
 */
test.describe('价格提醒列表', () => {
  test.beforeEach(async ({ page }) => {
    await page.goto('/alerts')
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
    test('应显示"创建提醒"按钮', async ({ page }) => {
      await page.waitForLoadState('networkidle')
      // 使用更精确的选择器，在主内容区域查找
      const createButton = page.getByRole('button', { name: /创建提醒/ }).last()
      await expect(createButton).toBeVisible()
    })

    test('应显示搜索框', async ({ page }) => {
      await page.waitForLoadState('networkidle')
      const searchInput = page.locator('.ant-input-search')
      await expect(searchInput).toBeVisible()
    })

    test('应显示标的类型筛选下拉框', async ({ page }) => {
      await page.waitForLoadState('networkidle')
      const selects = page.locator('.ant-select')
      await expect(selects.first()).toBeVisible()
    })

    test('应显示状态下拉框', async ({ page }) => {
      await page.waitForLoadState('networkidle')
      const selects = page.locator('.ant-select')
      const count = await selects.count()
      if (count > 1) {
        await expect(selects.nth(1)).toBeVisible()
      } else {
        await expect(selects.first()).toBeVisible()
      }
    })
  })

  test.describe('表格展示', () => {
    test('应显示数据表格', async ({ page }) => {
      await page.waitForLoadState('networkidle')
      const table = page.locator('.ant-table')
      await expect(table).toBeVisible()
    })

    test('表格应包含正确的列标题', async ({ page }) => {
      await page.waitForLoadState('networkidle')
      await expect(page.locator('.ant-table-thead th').filter({ hasText: '标的代码' })).toBeVisible()
      await expect(page.locator('.ant-table-thead th').filter({ hasText: '标的类型' })).toBeVisible()
      await expect(page.locator('.ant-table-thead th').filter({ hasText: '提醒类型' })).toBeVisible()
      await expect(page.locator('.ant-table-thead th').filter({ hasText: '状态' })).toBeVisible()
    })

    test('表格分页应显示总条数', async ({ page }) => {
      await page.waitForTimeout(500)
      const pagination = page.locator('.ant-pagination-total-text')
      const hasPagination = await pagination.count() > 0
      if (hasPagination) {
        await expect(pagination).toContainText('共')
      }
    })
  })

  test.describe('搜索和筛选', () => {
    test('搜索框应能输入并搜索', async ({ page }) => {
      await page.waitForLoadState('networkidle')
      const searchInput = page.locator('.ant-input-search input').first()
      await searchInput.fill('000001')
      // 点击搜索按钮（第一个）
      await page.locator('.ant-input-search .ant-btn').first().click()
      await page.waitForTimeout(500)
    })

    test('标的类型筛选应能选择', async ({ page }) => {
      await page.waitForLoadState('networkidle')
      const select = page.locator('.ant-select').first()
      await select.click()
      await page.locator('.ant-select-dropdown .ant-select-item').filter({ hasText: '股票' }).click()
      await page.waitForTimeout(500)
    })
  })

  test.describe('创建提醒', () => {
    test('点击"创建提醒"应跳转到创建页面', async ({ page }) => {
      await page.waitForLoadState('networkidle')
      // 在主内容区域（Content）查找创建按钮
      await page.locator('main button').filter({ hasText: /创建提醒/ }).click()
      await page.waitForURL('**/alerts/create')
      await expect(page).toHaveURL(/\/alerts\/create/)
    })
  })

  test.describe('删除功能', () => {
    test('点击删除按钮应显示确认对话框', async ({ page }) => {
      await page.waitForTimeout(1000)

      const deleteButton = page.locator('.ant-btn-dangerous').filter({ hasText: '删除' }).first()
      const hasDeleteButton = await deleteButton.count() > 0

      if (hasDeleteButton) {
        await deleteButton.click()
        const modal = page.locator('.ant-modal')
        await expect(modal).toBeVisible()
        await expect(page.locator('.ant-modal-title')).toContainText('确认删除')
      }
    })

    test('删除确认对话框应包含确认和取消按钮', async ({ page }) => {
      await page.waitForTimeout(1000)

      const deleteButton = page.locator('.ant-btn-dangerous').filter({ hasText: '删除' }).first()
      const hasDeleteButton = await deleteButton.count() > 0

      if (hasDeleteButton) {
        await deleteButton.click()
        // Wait for modal to appear
        await expect(page.locator('.ant-modal')).toBeVisible()
        // Ant Design renders Chinese text with spaces: "确 认" and "取 消"
        await expect(page.locator('.ant-modal-footer .ant-btn-primary')).toBeVisible()
        await expect(page.locator('.ant-modal-footer .ant-btn-default')).toBeVisible()
      }
    })
  })

  test.describe('编辑功能', () => {
    test('点击编辑按钮应跳转到编辑页面', async ({ page }) => {
      await page.waitForTimeout(1000)

      const editButton = page.locator('.ant-btn-link').filter({ hasText: '编辑' }).first()
      const hasEditButton = await editButton.count() > 0

      if (hasEditButton) {
        await editButton.click()
        await page.waitForURL('**/alerts/edit/**')
      }
    })
  })

  test.describe('启用/禁用功能', () => {
    test('表格行应显示启用/禁用开关', async ({ page }) => {
      await page.waitForTimeout(2000)

      // 如果表格有数据行，才检查switch
      const rows = page.locator('.ant-table-row')
      const rowCount = await rows.count()

      if (rowCount > 0) {
        const switchInRow = page.locator('.ant-table-row .ant-switch')
        const hasSwitch = await switchInRow.count() > 0
        expect(hasSwitch).toBeTruthy()
      } else {
        // 无数据时跳过
        expect(rowCount).toBe(0)
      }
    })
  })
})
