import { Page, Locator, expect } from '@playwright/test'

/**
 * 批量订阅模态框 Page Object
 */
export class BatchSubscribeModal {
  readonly page: Page
  
  // 模态框
  readonly modal: Locator
  readonly modalTitle: Locator
  
  // 表单元素
  readonly symbolTypeSelect: Locator
  readonly symbolsSelect: Locator
  readonly selectedSymbolsArea: Locator
  
  // 按钮
  readonly okButton: Locator
  readonly cancelButton: Locator

  constructor(page: Page) {
    this.page = page
    this.modal = page.locator('.ant-modal').filter({ hasText: /批量订阅风险提醒/ })
    this.modalTitle = this.modal.locator('.ant-modal-title')
    this.symbolTypeSelect = this.modal.locator('.ant-form-item').filter({ hasText: '标的类型' }).locator('.ant-select').first()
    this.symbolsSelect = this.modal.locator('.ant-form-item').filter({ hasText: '选择标的' }).locator('.ant-select').first()
    this.selectedSymbolsArea = this.modal.locator('.ant-form-item').filter({ hasText: '已选标的' })
    this.okButton = this.modal.getByRole('button', { name: /批量订阅/ })
    this.cancelButton = this.modal.getByRole('button', { name: /取消/ })
  }

  /**
   * 等待模态框打开
   */
  async waitForOpen() {
    await expect(this.modal).toBeVisible({ timeout: 5000 })
  }

  /**
   * 等待模态框关闭
   */
  async waitForClose() {
    await expect(this.modal).not.toBeVisible({ timeout: 5000 })
  }

  /**
   * 选择标的类型
   */
  async selectSymbolType(type: 'STOCK' | 'FUND') {
    await this.symbolTypeSelect.click()
    await this.page.locator('.ant-select-dropdown .ant-select-item').filter({ hasText: type === 'STOCK' ? '股票' : '基金' }).click()
    await this.page.waitForTimeout(500)
  }

  /**
   * 搜索并选择标的
   */
  async searchAndSelectSymbols(keyword: string, count: number = 1) {
    await this.symbolsSelect.click()
    await this.page.keyboard.type(keyword)
    await this.page.waitForTimeout(500)
    
    for (let i = 0; i < count; i++) {
      const options = this.page.locator('.ant-select-dropdown .ant-select-item')
      const optionCount = await options.count()
      if (optionCount > i) {
        await options.nth(i).click()
        await this.page.waitForTimeout(200)
      }
    }
    await this.page.keyboard.press('Escape')
    await this.page.waitForTimeout(300)
  }

  /**
   * 点击订阅
   */
  async subscribe() {
    await this.okButton.click()
  }

  /**
   * 点击取消
   */
  async cancel() {
    await this.cancelButton.click()
    await this.waitForClose()
  }

  /**
   * 获取已选标的数量文本
   */
  async getSelectedCount(): Promise<number> {
    const tags = await this.selectedSymbolsArea.locator('.ant-tag').count()
    return tags
  }

  /**
   * 验证模态框包含说明文本
   */
  async hasDescription() {
    const alert = this.modal.locator('.ant-alert')
    await expect(alert).toBeVisible()
    await expect(alert).toContainText(/订阅说明/)
  }
}
