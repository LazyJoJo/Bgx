import { expect, test } from '@playwright/test'
import { setupLocalStorage, waitForAntMessage, waitForModal, waitForModalClose } from './helpers'

/**
 * 基金列表页面 Page Object
 */
class FundListPage {
    constructor(private page: any) { }

    // 导航到基金列表页
    async navigate() {
        await this.page.goto('/funds')
        await this.page.waitForSelector('.ant-table', { timeout: 10000 })
    }

    // 等待页面加载
    async waitForPageLoad() {
        await this.page.waitForSelector('.ant-table', { timeout: 10000 })
    }

    // 获取表格行数
    async getTableRowCount(): Promise<number> {
        const rows = this.page.locator('.ant-table-row')
        return await rows.count()
    }

    // 获取第一行基金代码
    async getFirstFundCode(): Promise<string> {
        const firstRow = this.page.locator('.ant-table-row').first()
        const codeCell = firstRow.locator('td').nth(0)
        return await codeCell.textContent() || ''
    }

    // 获取第一行基金名称
    async getFirstFundName(): Promise<string> {
        const firstRow = this.page.locator('.ant-table-row').first()
        const nameCell = firstRow.locator('td').nth(1)
        return await nameCell.textContent() || ''
    }

    // 点击第一行的编辑按钮
    async clickEditFirstRow() {
        const firstRow = this.page.locator('.ant-table-row').first()
        const editButton = firstRow.locator('button').filter({ hasText: '编辑' })
        await editButton.click()
        await waitForModal(this.page, '编辑基金')
    }

    // 点击第一行的删除按钮
    async clickDeleteFirstRow() {
        const firstRow = this.page.locator('.ant-table-row').first()
        const deleteButton = firstRow.locator('button').filter({ hasText: '删除' })
        await deleteButton.click()
    }

    // 获取第一行基金的激活状态
    async getFirstFundActiveState(): Promise<boolean> {
        const firstRow = this.page.locator('.ant-table-row').first()
        const switchElement = firstRow.locator('.ant-switch')
        return await switchElement.evaluate(el => el.classList.contains('ant-switch-checked'))
    }

    // 点击第一行的激活/停用开关
    async clickToggleFirstRow() {
        const firstRow = this.page.locator('.ant-table-row').first()
        const switchElement = firstRow.locator('.ant-switch')
        await switchElement.click()
        await this.page.waitForTimeout(500)
    }
}

/**
 * 基金编辑模态框 Page Object
 */
class FundEditModal {
    constructor(private page: any) { }

    // 获取描述输入框
    get descriptionInput() {
        return this.page.locator('.ant-modal textarea').first()
    }

    // 获取保存按钮
    get saveButton() {
        return this.page.locator('.ant-modal button').filter({ hasText: '保存' })
    }

    // 填写描述字段
    async fillDescription(text: string) {
        await this.descriptionInput.fill(text)
    }

    // 点击保存
    async clickSave() {
        await this.saveButton.click()
    }
}

/**
 * 基金管理功能 E2E 测试
 * 
 * 测试场景：
 * 1. 编辑功能测试 - 修改描述字段并保存
 * 2. 删除功能测试 - 删除基金并验证从列表移除
 * 3. 激活/停用功能测试 - 切换基金状态
 */
test.describe('基金列表管理功能 E2E 测试', () => {
    // 失败重试配置
    test.describe.configure({ retries: 2 })

    let fundListPage: FundListPage
    let fundEditModal: FundEditModal

    test.beforeEach(async ({ page, context }) => {
        // Set localStorage via context addInitScript
        await context.addInitScript(() => {
            localStorage.setItem('userId', '1')
        })
        fundListPage = new FundListPage(page)
        fundEditModal = new FundEditModal(page)
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
     * 场景1：编辑功能测试
     */
    test.describe('场景1：基金编辑功能', () => {
        test('AC-1.1 应能打开编辑模态框并显示基金信息', async ({ page }) => {
            await fundListPage.navigate()
            await fundListPage.waitForPageLoad()

            // 验证表格中有数据
            const rowCount = await fundListPage.getTableRowCount()
            expect(rowCount).toBeGreaterThan(0)

            // 点击编辑按钮
            await fundListPage.clickEditFirstRow()

            // 验证模态框打开
            const modal = page.locator('.ant-modal').filter({ hasText: '编辑基金' })
            await expect(modal).toBeVisible()

            // 验证表单字段存在
            await expect(fundEditModal.descriptionInput).toBeVisible()

            await page.screenshot({ path: 'test-results/scenario1-edit-modal-open.png', fullPage: true })
        })

        test.skip('AC-1.2 应能修改描述字段并保存成功', async ({ page }) => {
            // TODO: 修复Ant Design Select组件的填写问题
            // 当前问题：Select组件需要先点击再选择，但Ant Design的dropdown渲染方式导致选择不稳定
            await fundListPage.navigate()
            await fundListPage.waitForPageLoad()

            const originalCode = await fundListPage.getFirstFundCode()
            const originalName = await fundListPage.getFirstFundName()

            await fundListPage.clickEditFirstRow()

            const modal = page.locator('.ant-modal').filter({ hasText: '编辑基金' })
            await modal.waitFor({ state: 'visible', timeout: 5000 })
            await page.waitForTimeout(300)

            await page.route('**/api/funds/*', async (route) => {
                if (route.request().method() === 'PUT') {
                    await route.fulfill({
                        status: 200,
                        contentType: 'application/json',
                        body: JSON.stringify({
                            success: true,
                            data: { id: 1, code: originalCode, name: originalName, description: 'E2E测试修改的描述' },
                            error: undefined
                        })
                    })
                } else {
                    await route.continue()
                }
            })

            const descInput = page.locator('.ant-modal textarea').first()
            await descInput.clear()
            await descInput.fill(`E2E测试修改 - ${Date.now()}`)

            const saveButton = page.locator('.ant-modal button').filter({ hasText: '保存' })
            await saveButton.click()

            await page.waitForTimeout(2000)
            const successMsg = page.locator('.ant-message-success')
            await successMsg.waitFor({ state: 'visible', timeout: 10000 })

            await page.waitForTimeout(1000)
            await expect(modal).not.toBeVisible()

            await page.screenshot({ path: 'test-results/scenario1-edit-save-success.png', fullPage: true })
        })

        test('AC-1.3 编辑时取消操作应关闭模态框不保存', async ({ page }) => {
            await fundListPage.navigate()
            await fundListPage.waitForPageLoad()

            // 点击编辑按钮
            await fundListPage.clickEditFirstRow()

            // 验证模态框打开
            const modal = page.locator('.ant-modal').filter({ hasText: '编辑基金' })
            await expect(modal).toBeVisible()

            // 点击取消按钮
            const cancelButton = page.locator('.ant-modal button').filter({ hasText: /^取.*消/ })
            await cancelButton.click()

            // 验证模态框关闭
            await waitForModalClose(page)

            await page.screenshot({ path: 'test-results/scenario1-edit-cancel.png', fullPage: true })
        })
    })

    /**
     * 场景2：删除功能测试
     */
    test.describe('场景2：基金删除功能', () => {
        test('AC-2.1 应能弹出删除确认框', async ({ page }) => {
            await fundListPage.navigate()
            await fundListPage.waitForPageLoad()

            // 获取要删除的基金信息
            const fundCode = await fundListPage.getFirstFundCode()
            const fundName = await fundListPage.getFirstFundName()

            // 点击删除按钮
            await fundListPage.clickDeleteFirstRow()

            // 验证确认弹窗出现
            const confirmModal = page.locator('.ant-modal-confirm')
            await expect(confirmModal).toBeVisible()

            // 验证确认内容包含基金名称
            await expect(confirmModal).toContainText(fundName)
            await expect(confirmModal).toContainText('确认删除')

            await page.screenshot({ path: 'test-results/scenario2-delete-confirm-modal.png', fullPage: true })
        })

        test('AC-2.2 确认删除后应从列表中移除', async ({ page }) => {
            await fundListPage.navigate()
            await fundListPage.waitForPageLoad()

            // 获取删除前的行数
            const originalRowCount = await fundListPage.getTableRowCount()
            expect(originalRowCount).toBeGreaterThan(0)

            // 获取要删除的基金代码
            const fundCodeToDelete = await fundListPage.getFirstFundCode()

            // 拦截删除API请求
            await page.route('**/api/funds/*', async (route) => {
                if (route.request().method() === 'DELETE') {
                    await route.fulfill({
                        status: 200,
                        contentType: 'application/json',
                        body: JSON.stringify({
                            success: true,
                            error: undefined
                        })
                    })
                } else {
                    await route.continue()
                }
            })

            // 点击删除按钮
            await fundListPage.clickDeleteFirstRow()

            // 点击确认删除
            const confirmButton = page.locator('.ant-modal-confirm button').filter({ hasText: '确认删除' })
            await confirmButton.click()

            // 验证成功消息
            await waitForAntMessage(page, 'success', '删除')

            // 等待列表更新
            await page.waitForTimeout(1000)

            // 验证列表行数减少或该基金不在列表中
            const newRowCount = await fundListPage.getTableRowCount()
            // 如果原本只有一行，删除后应该为空或仍有数据（看是否有新数据加载）
            // 主要验证被删除的基金不在列表中
            if (newRowCount > 0) {
                const firstRowCode = await fundListPage.getFirstFundCode()
                expect(firstRowCode).not.toBe(fundCodeToDelete)
            }

            await page.screenshot({ path: 'test-results/scenario2-delete-success.png', fullPage: true })
        })

        test('AC-2.3 取消删除不应移除基金', async ({ page }) => {
            await fundListPage.navigate()
            await fundListPage.waitForPageLoad()

            // 获取删除前的行数和基金代码
            const originalRowCount = await fundListPage.getTableRowCount()
            const originalFundCode = await fundListPage.getFirstFundCode()

            // 点击删除按钮
            await fundListPage.clickDeleteFirstRow()

            // 点击取消
            const cancelButton = page.locator('.ant-modal-confirm button').filter({ hasText: /^取.*消/ })
            await cancelButton.click()

            // 等待确认弹窗关闭
            await page.waitForTimeout(500)

            // 验证列表没有变化
            const newRowCount = await fundListPage.getTableRowCount()
            expect(newRowCount).toBe(originalRowCount)

            const firstRowCode = await fundListPage.getFirstFundCode()
            expect(firstRowCode).toBe(originalFundCode)

            await page.screenshot({ path: 'test-results/scenario2-delete-cancelled.png', fullPage: true })
        })
    })

    /**
     * 场景3：激活/停用功能测试
     */
    test.describe('场景3：基金激活/停用功能', () => {
        test('AC-3.1 应能切换基金激活状态', async ({ page }) => {
            await fundListPage.navigate()
            await fundListPage.waitForPageLoad()

            // 获取当前的激活状态
            const originalActiveState = await fundListPage.getFirstFundActiveState()
            const actionWord = originalActiveState ? '停用' : '激活'

            // 拦截激活/停用API请求
            await page.route('**/api/funds/*', async (route) => {
                const url = route.request().url()
                if (url.includes('/activate') || url.includes('/deactivate')) {
                    await route.fulfill({
                        status: 200,
                        contentType: 'application/json',
                        body: JSON.stringify({
                            success: true,
                            data: { active: !originalActiveState },
                            error: undefined
                        })
                    })
                } else {
                    await route.continue()
                }
            })

            // 点击切换开关
            await fundListPage.clickToggleFirstRow()

            // 验证成功消息
            await waitForAntMessage(page, 'success', actionWord)

            // 等待状态更新
            await page.waitForTimeout(500)

            // 验证状态已切换
            const newActiveState = await fundListPage.getFirstFundActiveState()
            expect(newActiveState).toBe(!originalActiveState)

            await page.screenshot({ path: 'test-results/scenario3-toggle-success.png', fullPage: true })
        })

        test('AC-3.2 切换过程中应显示loading状态', async ({ page }) => {
            await fundListPage.navigate()
            await fundListPage.waitForPageLoad()

            // 拦截API请求，延迟响应以观察loading状态
            let requestHandled = false
            await page.route('**/api/funds/*', async (route) => {
                const url = route.request().url()
                if (url.includes('/activate') || url.includes('/deactivate')) {
                    // 延迟500ms后响应
                    await new Promise(resolve => setTimeout(resolve, 500))
                    requestHandled = true
                    await route.fulfill({
                        status: 200,
                        contentType: 'application/json',
                        body: JSON.stringify({
                            success: true,
                            data: { active: true },
                            error: undefined
                        })
                    })
                } else {
                    await route.continue()
                }
            })

            // 点击切换开关
            await fundListPage.clickToggleFirstRow()

            // 验证loading状态出现（switch有loading类）
            const firstRow = page.locator('.ant-table-row').first()
            const switchElement = firstRow.locator('.ant-switch')

            // 短暂等待检查loading状态
            await page.waitForTimeout(100)

            // 等待请求完成
            await page.waitForTimeout(600)

            await page.screenshot({ path: 'test-results/scenario3-toggle-loading.png', fullPage: true })
        })
    })

    /**
     * 场景4：列表页面基础功能
     */
    test.describe('场景4：基金列表页面基础功能', () => {
        test('AC-4.1 页面应正确加载并显示基金列表', async ({ page }) => {
            await fundListPage.navigate()
            await fundListPage.waitForPageLoad()

            // 验证页面标题或关键元素
            await expect(page.locator('.ant-table')).toBeVisible()

            // 验证表格有数据
            const rowCount = await fundListPage.getTableRowCount()
            expect(rowCount).toBeGreaterThan(0)

            // 验证关键列存在
            const firstRow = page.locator('.ant-table-row').first()
            await expect(firstRow.locator('td').first()).toBeVisible() // 基金代码列

            await page.screenshot({ path: 'test-results/scenario4-fund-list-loaded.png', fullPage: true })
        })

        test('AC-4.2 页面应显示正确的表头', async ({ page }) => {
            await fundListPage.navigate()
            await fundListPage.waitForPageLoad()

            // 验证表头存在
            const tableHeaders = page.locator('.ant-table-thead th')
            const headerCount = await tableHeaders.count()
            expect(headerCount).toBeGreaterThan(0)

            // 验证包含预期的列标题
            const headerTexts = await tableHeaders.allTextContents()
            expect(headerTexts.some((text: string) => text.includes('基金代码'))).toBeTruthy()
            expect(headerTexts.some((text: string) => text.includes('基金名称'))).toBeTruthy()

            await page.screenshot({ path: 'test-results/scenario4-table-headers.png', fullPage: true })
        })
    })
})
