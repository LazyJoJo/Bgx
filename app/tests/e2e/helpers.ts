import { Page } from '@playwright/test'

/**
 * E2E 测试辅助工具
 */

/**
 * 等待并确认 Ant Design 消息提示
 */
export async function waitForAntMessage(page: Page, type: 'success' | 'error' | 'warning' | 'info', text?: string) {
  const messageSelector = `.ant-message-${type}`
  const message = page.locator(messageSelector)
  
  await message.waitFor({ state: 'visible', timeout: 5000 })
  
  if (text) {
    await expect(message).toContainText(text)
  }
  
  return message
}

/**
 * 模拟 localStorage 设置
 */
export async function setupLocalStorage(page: Page, data: Record<string, string>) {
  await page.evaluate((storageData) => {
    Object.entries(storageData).forEach(([key, value]) => {
      localStorage.setItem(key, value)
    })
  }, data)
}

/**
 * 清除 localStorage
 */
export async function clearLocalStorage(page: Page) {
  await page.evaluate(() => {
    localStorage.clear()
  })
}

/**
 * 等待 Ant Design 模态框打开
 */
export async function waitForModal(page: Page, titleText: string) {
  const modal = page.locator('.ant-modal').filter({ hasText: titleText })
  await modal.waitFor({ state: 'visible', timeout: 5000 })
  return modal
}

/**
 * 等待 Ant Design 模态框关闭
 */
export async function waitForModalClose(page: Page) {
  await page.locator('.ant-modal').waitFor({ state: 'hidden', timeout: 5000 })
}

/**
 * 选择 Ant Design Select 选项
 */
export async function selectAntOption(page: Page, selectSelector: string, optionText: string) {
  await page.locator(selectSelector).click()
  await page.locator(`.ant-select-dropdown .ant-select-item:has-text("${optionText}")`).click()
  await page.waitForTimeout(300)
}

/**
 * 填写 Ant Design InputNumber
 */
export async function fillInputNumber(page: Page, selector: string, value: number) {
  const input = page.locator(`${selector} input`)
  await input.fill(String(value))
  await page.waitForTimeout(200)
}

/**
 * 验证表格包含指定文本
 */
export async function verifyTableContains(page: Page, text: string) {
  await expect(page.locator('.ant-table').locator(`text=${text}`)).toBeVisible()
}

/**
 * 获取 Ant Design 表单错误消息
 */
export async function getFormErrors(page: Page): Promise<string[]> {
  const errors = page.locator('.ant-form-item-explain-error')
  const count = await errors.count()
  const errorTexts: string[] = []
  
  for (let i = 0; i < count; i++) {
    const text = await errors.nth(i).textContent()
    if (text) errorTexts.push(text)
  }
  
  return errorTexts
}

/**
 * 测试数据生成器
 */
export const TestData = {
  /**
   * 生成唯一的测试标的代码
   */
  generateSymbol(): string {
    return `TEST${Date.now()}`
  },
  
  /**
   * 生成批量测试标的
   */
  generateBatchSymbols(count: number): string[] {
    return Array.from({ length: count }, (_, i) => `TEST${Date.now()}${i}`)
  },
  
  /**
   * 模拟股票搜索结果
   */
  mockStockResults(keywords: string[] = ['600000', '600519', '000001']) {
    return {
      success: true,
      data: keywords.map((code, i) => ({
        id: i + 1,
        code,
        name: `测试股票${i + 1}`,
        type: 'STOCK',
        market: i % 2 === 0 ? 'SH' : 'SZ',
        active: true
      }))
    }
  },
  
  /**
   * 模拟基金搜索结果
   */
  mockFundResults(keywords: string[] = ['110001', '000001']) {
    return {
      success: true,
      data: keywords.map((code, i) => ({
        id: i + 1,
        code,
        name: `测试基金${i + 1}`,
        type: 'FUND',
        active: true
      }))
    }
  },
  
  /**
   * 模拟批量创建成功响应
   */
  mockBatchCreateSuccess(symbols: string[]) {
    return {
      success: true,
      data: {
        batchId: `batch-${Date.now()}`,
        totalCount: symbols.length,
        successCount: symbols.length,
        failureCount: 0,
        successList: symbols.map((symbol, i) => ({
          symbol,
          symbolName: `测试股票${i + 1}`,
          alertId: i + 1,
          createdAt: new Date().toISOString()
        })),
        failureList: []
      }
    }
  },
  
  /**
   * 模拟批量创建部分成功响应
   */
  mockBatchCreatePartialSuccess(symbols: string[], failureIndices: number[]) {
    const successList: any[] = []
    const failureList: any[] = []
    
    symbols.forEach((symbol, i) => {
      if (failureIndices.includes(i)) {
        failureList.push({
          symbol,
          symbolName: `测试股票${i + 1}`,
          reason: '标的已存在',
          errorCode: 'DUPLICATE'
        })
      } else {
        successList.push({
          symbol,
          symbolName: `测试股票${i + 1}`,
          alertId: i + 1,
          createdAt: new Date().toISOString()
        })
      }
    })
    
    return {
      success: true,
      data: {
        batchId: `batch-${Date.now()}`,
        totalCount: symbols.length,
        successCount: successList.length,
        failureCount: failureList.length,
        successList,
        failureList
      }
    }
  },
  
  /**
   * 模拟风险提醒数据
   */
  mockRiskAlerts(count: number = 3) {
    return {
      success: true,
      data: {
        list: Array.from({ length: count }, (_, i) => ({
          id: i + 1,
          symbol: `60000${i}`,
          symbolName: `测试股票${i + 1}`,
          symbolType: 'STOCK',
          date: new Date().toISOString().split('T')[0],
          triggerCount: i + 1,
          latestChangePercent: (i + 1) * 1.5,
          maxChangePercent: (i + 1) * 2.0,
          isRead: i % 2 === 0,
          details: [
            {
              id: 1,
              triggeredAt: new Date().toISOString(),
              changePercent: (i + 1) * 1.5
            }
          ]
        })),
        cursor: 'next-page-token',
        hasMore: count > 5
      }
    }
  }
}
