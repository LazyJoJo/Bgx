import { Page, Locator, expect } from '@playwright/test'

/**
 * 风险提醒页面 (/risk-alerts) Page Object
 */
export class RiskAlertListPage {
  readonly page: Page
  
  // 标题和按钮
  readonly pageTitle: Locator
  readonly markAllReadButton: Locator
  readonly unreadBadge: Locator
  
  // 列表
  readonly alertList: Locator
  readonly alertItems: Locator
  readonly emptyState: Locator
  readonly loadingSpinner: Locator
  readonly loadMoreButton: Locator

  constructor(page: Page) {
    this.page = page
    this.pageTitle = page.locator('h2').filter({ hasText: /风险提醒/ })
    this.markAllReadButton = page.getByRole('button', { name: /全部已读/ })
    this.unreadBadge = page.locator('.ant-badge-count').first()
    this.alertList = page.locator('.ant-collapse')
    this.alertItems = page.locator('.ant-collapse-item')
    this.emptyState = page.locator('.ant-empty')
    this.loadingSpinner = page.locator('.ant-spin')
    this.loadMoreButton = page.getByRole('button', { name: /加载更多/ })
  }

  /**
   * 导航到风险提醒页
   */
  async navigate() {
    await this.page.goto('/risk-alerts')
    await this.page.evaluate(() => localStorage.setItem('userId', '1'))
    await this.page.waitForLoadState('networkidle')
  }

  /**
   * 等待页面加载完成
   */
  async waitForPageLoad() {
    await expect(this.pageTitle).toBeVisible()
  }

  /**
   * 等待列表加载完成
   */
  async waitForListLoaded() {
    await expect(this.loadingSpinner).not.toBeVisible({ timeout: 5000 })
  }

  /**
   * 点击"全部已读"按钮
   */
  async markAllAsRead() {
    await this.markAllReadButton.click()
    await this.page.waitForTimeout(500)
  }

  /**
   * 验证"全部已读"按钮是否禁用
   */
  async isMarkAllReadDisabled(): Promise<boolean> {
    return await this.markAllReadButton.isDisabled()
  }

  /**
   * 验证页面不包含批量订阅按钮
   */
  async verifyNoBatchSubscribeButton() {
    const batchButton = this.page.getByRole('button', { name: /批量订阅/ })
    await expect(batchButton).toHaveCount(0)
  }

  /**
   * 验证页面不包含创建提醒按钮
   */
  async verifyNoCreateAlertButton() {
    const createButton = this.page.getByRole('button', { name: /创建提醒/ })
    await expect(createButton).toHaveCount(0)
  }

  /**
   * 验证提醒列表显示
   */
  async verifyAlertListVisible() {
    await expect(this.alertList).toBeVisible()
  }

  /**
   * 验证空状态
   */
  async verifyEmptyState() {
    await expect(this.emptyState).toBeVisible()
    await expect(this.emptyState).toContainText(/暂无风险提醒/)
  }

  /**
   * 获取提醒项数量
   */
  async getAlertItemCount(): Promise<number> {
    return await this.alertItems.count()
  }

  /**
   * 点击加载更多
   */
  async loadMore() {
    await this.loadMoreButton.click()
    await this.page.waitForTimeout(500)
  }

  /**
   * 验证加载更多按钮可见性
   */
  async isLoadMoreVisible(): Promise<boolean> {
    return await this.loadMoreButton.isVisible()
  }

  /**
   * 截图
   */
  async screenshot(name: string) {
    await this.page.screenshot({ path: `test-results/${name}.png`, fullPage: true })
  }
}
