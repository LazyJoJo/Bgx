import { Page, Locator, expect } from '@playwright/test'

/**
 * 提醒设置页面 (/alerts) Page Object
 */
export class AlertListPage {
  readonly page: Page

  // 按钮
  readonly batchSubscribeButton: Locator
  readonly createAlertButton: Locator

  // 搜索和筛选
  readonly searchInput: Locator
  readonly symbolTypeFilter: Locator
  readonly statusFilter: Locator

  // 表格
  readonly table: Locator
  readonly tableRows: Locator

  // 批量订阅模态框
  readonly batchSubscribeModal: Locator

  constructor(page: Page) {
    this.page = page
    this.batchSubscribeButton = page.getByRole('button', { name: /批量订阅/ }).last()
    this.createAlertButton = page.getByRole('button', { name: /创建提醒/ }).last()
    this.searchInput = page.locator('.ant-input-search input').first()
    this.symbolTypeFilter = page.locator('.ant-select').first()
    this.statusFilter = page.locator('.ant-select').nth(1)
    this.table = page.locator('.ant-table')
    this.tableRows = page.locator('.ant-table-row')
    this.batchSubscribeModal = page.locator('.ant-modal').filter({ hasText: /批量订阅风险提醒/ })
  }

  /**
   * 导航到提醒列表页
   */
  async navigate() {
    await this.page.goto('/alerts')
    await this.page.evaluate(() => localStorage.setItem('userId', '1'))
    await this.page.waitForLoadState('networkidle')
  }

  /**
   * 验证页面加载完成
   */
  async waitForPageLoad() {
    await expect(this.table).toBeVisible()
    await expect(this.createAlertButton).toBeVisible()
    await expect(this.batchSubscribeButton).toBeVisible()
  }

  /**
   * 点击"创建提醒"按钮
   */
  async clickCreateAlert() {
    await this.createAlertButton.click()
    await this.page.waitForURL('**/alerts/create')
  }

  /**
   * 点击"批量订阅"按钮
   */
  async clickBatchSubscribe() {
    await this.batchSubscribeButton.click()
    await expect(this.batchSubscribeModal).toBeVisible()
  }

  /**
   * 搜索标的
   */
  async searchSymbol(keyword: string) {
    await this.searchInput.fill(keyword)
    await this.page.locator('.ant-input-search .ant-btn').first().click()
    await this.page.waitForTimeout(500)
  }

  /**
   * 筛选标的类型
   */
  async filterBySymbolType(type: 'STOCK' | 'FUND') {
    await this.symbolTypeFilter.click()
    await this.page.locator('.ant-select-dropdown .ant-select-item').filter({ hasText: type === 'STOCK' ? '股票' : '基金' }).click()
    await this.page.waitForTimeout(500)
  }

  /**
   * 获取表格行数
   */
  async getTableRowCount(): Promise<number> {
    return await this.tableRows.count()
  }

  /**
   * 点击第一行的删除按钮
   */
  async clickDeleteFirstRow() {
    const deleteButton = this.page.locator('.ant-btn-dangerous').filter({ hasText: '删除' }).first()
    await deleteButton.click()
  }

  /**
   * 确认删除
   */
  async confirmDelete() {
    const modal = this.page.locator('.ant-modal').filter({ hasText: /确认删除/ })
    await expect(modal).toBeVisible()
    await modal.locator('.ant-btn-primary').click()
    await this.page.waitForTimeout(500)
  }

  /**
   * 取消删除
   */
  async cancelDelete() {
    const modal = this.page.locator('.ant-modal').filter({ hasText: /确认删除/ })
    await expect(modal).toBeVisible()
    await modal.locator('.ant-btn-default').click()
  }

  /**
   * 截图
   */
  async screenshot(name: string) {
    await this.page.screenshot({ path: `test-results/${name}.png`, fullPage: true })
  }
}
