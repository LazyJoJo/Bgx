import { Page, Locator, expect } from '@playwright/test'

/**
 * 创建提醒页面 (/alerts/create) Page Object
 */
export class AlertCreatePage {
  readonly page: Page
  
  // 标题和按钮
  readonly pageTitle: Locator
  readonly backButton: Locator
  readonly submitButton: Locator
  readonly resetButton: Locator
  
  // 模式切换
  readonly modeSwitch: Locator
  readonly singleModeButton: Locator
  readonly batchModeButton: Locator
  
  // 单选模式表单元素
  readonly symbolInput: Locator
  readonly symbolTypeSelect: Locator
  
  // 批量模式表单元素
  readonly batchSymbolTypeRadio: Locator
  readonly stockTypeButton: Locator
  readonly fundTypeButton: Locator
  readonly batchSymbolsSelect: Locator
  readonly selectAllButton: Locator
  readonly clearSelectionButton: Locator
  readonly selectedCountText: Locator
  
  // 通用表单元素
  readonly alertTypeSelect: Locator
  readonly targetPriceInput: Locator
  readonly targetChangePercentInput: Locator
  readonly statusSwitch: Locator
  
  // 结果弹窗
  readonly resultModal: Locator
  readonly resultModalTitle: Locator
  readonly retryFailedButton: Locator
  readonly resultModalBackButton: Locator
  
  // 错误提示
  readonly errorMessages: Locator

  constructor(page: Page) {
    this.page = page
    this.pageTitle = page.locator('h2').filter({ hasText: /创建提醒|编辑提醒/ })
    this.backButton = page.getByRole('button', { name: /返回列表/ })
    this.submitButton = page.locator('button[type="submit"]')
    this.resetButton = page.getByRole('button', { name: /重置/ })
    
    this.modeSwitch = page.locator('.ant-radio-group')
    this.singleModeButton = this.modeSwitch.locator('.ant-radio-button-wrapper').filter({ hasText: '单选' })
    this.batchModeButton = this.modeSwitch.locator('.ant-radio-button-wrapper').filter({ hasText: '批量' })
    
    this.symbolInput = page.locator('#symbol')
    this.symbolTypeSelect = page.locator('.ant-form-item').filter({ hasText: '标的类型' }).locator('.ant-select').first()
    
    this.batchSymbolTypeRadio = page.locator('.ant-form-item').filter({ hasText: /选择标的类型/ }).locator('.ant-radio-group')
    this.stockTypeButton = this.batchSymbolTypeRadio.locator('.ant-radio-button-wrapper').filter({ hasText: '股票' })
    this.fundTypeButton = this.batchSymbolTypeRadio.locator('.ant-radio-button-wrapper').filter({ hasText: '基金' })
    this.batchSymbolsSelect = page.locator('.ant-select-multiple').first()
    this.selectAllButton = page.getByRole('button', { name: /全选当前搜索结果/ })
    this.clearSelectionButton = page.getByRole('button', { name: /清空选择/ })
    this.selectedCountText = page.locator('text=/已选择 \\d+ 个标的/')
    
    this.alertTypeSelect = page.locator('.ant-form-item').filter({ hasText: /提醒类型/ }).locator('.ant-select').first()
    this.targetPriceInput = page.locator('.ant-input-number input').first()
    this.targetChangePercentInput = page.locator('.ant-input-number input').last()
    this.statusSwitch = page.locator('.ant-switch')
    
    this.resultModal = page.locator('.ant-modal').filter({ hasText: /批量创建结果/ })
    this.resultModalTitle = this.resultModal.locator('.ant-modal-title')
    this.retryFailedButton = this.resultModal.getByRole('button', { name: /重试失败项/ })
    this.resultModalBackButton = this.resultModal.getByRole('button', { name: /完成|返回列表/ })
    
    this.errorMessages = page.locator('.ant-form-item-explain-error')
  }

  /**
   * 导航到创建提醒页面
   */
  async navigate(mode?: 'single' | 'batch') {
    const url = mode === 'batch' ? '/alerts/create?mode=batch' : '/alerts/create'
    await this.page.goto(url)
    await this.page.evaluate(() => localStorage.setItem('userId', '1'))
    await this.page.waitForLoadState('networkidle')
  }

  /**
   * 等待页面加载完成
   */
  async waitForPageLoad() {
    await expect(this.pageTitle).toBeVisible()
    await expect(this.submitButton).toBeVisible()
  }

  /**
   * 切换到单选模式
   */
  async switchToSingleMode() {
    await this.singleModeButton.click()
    await this.page.waitForTimeout(300)
  }

  /**
   * 切换到批量模式
   */
  async switchToBatchMode() {
    await this.batchModeButton.click()
    await this.page.waitForTimeout(300)
  }

  /**
   * 填写单条提醒表单
   */
  async fillSingleForm(data: {
    symbol: string
    symbolType?: 'STOCK' | 'FUND'
    alertType?: 'PRICE_ABOVE' | 'PRICE_BELOW' | 'PERCENTAGE_CHANGE'
    targetPrice?: number
    targetChangePercent?: number
  }) {
    await this.symbolInput.fill(data.symbol)
    
    if (data.symbolType) {
      await this.symbolTypeSelect.click()
      await this.page.locator('.ant-select-dropdown .ant-select-item').filter({ hasText: data.symbolType === 'STOCK' ? '股票' : '基金' }).click()
    }
    
    if (data.alertType) {
      await this.alertTypeSelect.click()
      const alertTypeMap = {
        'PRICE_ABOVE': '价格超过',
        'PRICE_BELOW': '价格低于',
        'PERCENTAGE_CHANGE': '涨跌幅'
      }
      await this.page.locator('.ant-select-dropdown .ant-select-item').filter({ hasText: alertTypeMap[data.alertType] }).click()
      await this.page.waitForTimeout(300)
    }
    
    if (data.alertType !== 'PERCENTAGE_CHANGE' && data.targetPrice) {
      await this.targetPriceInput.fill(String(data.targetPrice))
    }
    
    if (data.alertType === 'PERCENTAGE_CHANGE' && data.targetChangePercent) {
      await this.targetChangePercentInput.fill(String(data.targetChangePercent))
    }
  }

  /**
   * 填写批量提醒表单
   */
  async fillBatchForm(data: {
    symbolType: 'STOCK' | 'FUND'
    symbols: string[]
    alertType?: 'PRICE_ABOVE' | 'PRICE_BELOW' | 'PERCENTAGE_CHANGE'
    targetPrice?: number
    targetChangePercent?: number
  }) {
    // 选择标的类型
    if (data.symbolType === 'STOCK') {
      await this.stockTypeButton.click()
    } else {
      await this.fundTypeButton.click()
    }
    await this.page.waitForTimeout(300)
    
    // 选择标的
    await this.batchSymbolsSelect.click()
    for (const symbol of data.symbols) {
      await this.page.keyboard.type(symbol)
      await this.page.waitForTimeout(300)
      // 选择第一个搜索结果
      const firstOption = this.page.locator('.ant-select-dropdown .ant-select-item').first()
      if (await firstOption.isVisible()) {
        await firstOption.click()
        await this.page.waitForTimeout(200)
      }
    }
    await this.page.keyboard.press('Escape')
    await this.page.waitForTimeout(300)
    
    // 选择提醒类型
    if (data.alertType) {
      await this.alertTypeSelect.click()
      const alertTypeMap = {
        'PRICE_ABOVE': '价格超过',
        'PRICE_BELOW': '价格低于',
        'PERCENTAGE_CHANGE': '涨跌幅'
      }
      await this.page.locator('.ant-select-dropdown .ant-select-item').filter({ hasText: alertTypeMap[data.alertType] }).click()
      await this.page.waitForTimeout(300)
    }
    
    // 填写目标值
    if (data.alertType !== 'PERCENTAGE_CHANGE' && data.targetPrice) {
      await this.targetPriceInput.fill(String(data.targetPrice))
    }
    
    if (data.alertType === 'PERCENTAGE_CHANGE' && data.targetChangePercent) {
      await this.targetChangePercentInput.fill(String(data.targetChangePercent))
    }
  }

  /**
   * 搜索并选择标的（批量模式）
   */
  async searchAndSelectSymbols(keyword: string, count: number = 1) {
    await this.batchSymbolsSelect.click()
    await this.page.keyboard.type(keyword)
    await this.page.waitForTimeout(500)
    
    // 选择指定数量的结果
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
   * 点击全选按钮
   */
  async selectAllSearchResults() {
    await this.selectAllButton.click()
    await this.page.waitForTimeout(300)
  }

  /**
   * 清空选择
   */
  async clearSelection() {
    await this.clearSelectionButton.click()
    await this.page.waitForTimeout(300)
  }

  /**
   * 提交表单
   */
  async submit() {
    await this.submitButton.click()
  }

  /**
   * 重置表单
   */
  async reset() {
    await this.resetButton.click()
    await this.page.waitForTimeout(300)
  }

  /**
   * 点击返回列表
   */
  async goBack() {
    await this.backButton.click()
    await this.page.waitForURL('**/alerts')
  }

  /**
   * 等待创建成功提示
   */
  async waitForSuccessMessage() {
    await expect(this.page.locator('.ant-message-success')).toBeVisible({ timeout: 5000 })
  }

  /**
   * 等待创建失败提示
   */
  async waitForErrorMessage() {
    await expect(this.page.locator('.ant-message-error, .ant-form-item-explain-error').first()).toBeVisible({ timeout: 5000 })
  }

  /**
   * 获取第一个错误消息
   */
  async getFirstErrorMessage(): Promise<string> {
    return await this.errorMessages.first().textContent()
  }

  /**
   * 等待结果弹窗
   */
  async waitForResultModal() {
    await expect(this.resultModal).toBeVisible({ timeout: 5000 })
  }

  /**
   * 获取批量创建结果文本
   */
  async getBatchResultText(): Promise<string> {
    const title = await this.resultModalTitle.textContent()
    return title || ''
  }

  /**
   * 点击重试失败项
   */
  async retryFailed() {
    await this.retryFailedButton.click()
    await this.page.waitForTimeout(500)
  }

  /**
   * 关闭结果弹窗并返回列表
   */
  async closeResultModal() {
    await this.resultModalBackButton.click()
    await this.page.waitForURL('**/alerts')
  }

  /**
   * 验证批量模式UI元素
   */
  async verifyBatchModeUI() {
    await expect(this.batchSymbolTypeRadio).toBeVisible()
    await expect(this.batchSymbolsSelect).toBeVisible()
    await expect(this.stockTypeButton).toBeVisible()
    await expect(this.fundTypeButton).toBeVisible()
  }

  /**
   * 验证单选模式UI元素
   */
  async verifySingleModeUI() {
    await expect(this.symbolInput).toBeVisible()
    await expect(this.symbolTypeSelect).toBeVisible()
  }

  /**
   * 验证提交按钮状态
   */
  async isSubmitButtonEnabled(): Promise<boolean> {
    return await this.submitButton.isEnabled()
  }

  /**
   * 验证表单验证错误
   */
  async hasValidationError(): Promise<boolean> {
    const count = await this.errorMessages.count()
    return count > 0
  }

  /**
   * 截图
   */
  async screenshot(name: string) {
    await this.page.screenshot({ path: `test-results/${name}.png`, fullPage: true })
  }
}
