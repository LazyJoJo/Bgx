// 类型定义 - 与后端 Stock 实体对应
export interface Stock {
  id: string | number
  symbol: string
  name: string
  industry?: string
  market?: string
  listingDate?: string
  totalShare?: number
  floatShare?: number
  pe?: number
  pb?: number
  currentPrice?: number
  change?: number
  changePercent?: number
  volume?: number
  createdAt?: string
  updatedAt?: string
}

// 基金类型定义 - 与后端 Fund 实体对应
export interface Fund {
  id: string | number
  fundCode: string
  name: string
  type?: string
  manager?: string
  establishmentDate?: string
  fundSize?: number
  nav?: number
  dayGrowth?: number
  weekGrowth?: number
  monthGrowth?: number
  yearGrowth?: number
  currentNav?: number
  dailyChange?: number
  dailyChangePercent?: number
  annualizedReturn?: number
  riskLevel?: string
  fundManager?: string
  createdAt?: string
  updatedAt?: string
}

// 股票行情类型定义
export interface StockQuote {
  id: string | number
  stockId: string
  open: number
  close: number
  high: number
  low: number
  volume: number
  amount: number
  quoteTime: string
  createdAt?: string
}

// 基金净值类型定义
export interface FundQuote {
  id: string | number
  fundCode: string
  nav: number
  accumulatedNav: number
  dayGrowth: number
  quoteDate: string
  createdAt?: string
}

// 提醒类型定义 - 与后端 PriceAlert 实体对应
export interface PriceAlert {
  id: string
  symbol: string
  symbolType: 'STOCK' | 'FUND'
  alertType: 'PRICE_ABOVE' | 'PRICE_BELOW' | 'PERCENTAGE_CHANGE'
  targetPrice?: number
  targetChangePercent?: number
  currentPrice?: number
  status: 'ACTIVE' | 'TRIGGERED' | 'INACTIVE'
  userId: string | number
  createdAt: string
  updatedAt: string
}

// 提醒历史类型定义 - 与后端 AlertHistory 实体对应
export interface AlertHistory {
  id: string
  alertId: string
  symbol: string
  symbolType: 'STOCK' | 'FUND'
  alertType: 'PRICE_ABOVE' | 'PRICE_BELOW' | 'PERCENTAGE_CHANGE'
  targetPrice?: number
  targetChangePercent?: number
  triggeredPrice: number
  triggeredAt: string
  message: string
}

// API 响应类型
export interface ApiResponse<T> {
  success: boolean
  message: string
  data: T
}

export interface PageResponse<T> {
  records: T[]
  total: number
  size: number
  current: number
  pages: number
}

// Dashboard 统计数据
export interface DashboardStats {
  totalStocks: number
  totalFunds: number
  activeAlerts: number
  triggeredAlerts: number
}

// 筛选条件类型
export interface FilterOptions {
  symbol?: string
  symbolType?: 'STOCK' | 'FUND'
  status?: 'ACTIVE' | 'TRIGGERED' | 'INACTIVE'
  alertType?: 'PRICE_ABOVE' | 'PRICE_BELOW' | 'PERCENTAGE_CHANGE'
  dateRange?: [string, string]
}

// 图表数据类型
export interface ChartData {
  time: string
  value: number
  volume?: number
}

export interface KLineData {
  time: string
  open: number
  close: number
  high: number
  low: number
  volume: number
}

// 风险提醒明细
export interface RiskAlertDetail {
  id: number
  changePercent: number
  currentPrice: number
  triggeredAt: string
  triggerReason: string
}

// 风险提醒（合并后的数据）
export interface RiskAlert {
  id: number
  symbol: string
  symbolType: 'STOCK' | 'FUND'
  symbolName: string
  date: string
  triggerCount: number
  maxChangePercent: number
  latestChangePercent: number
  currentPrice: number
  yesterdayClose: number
  isRead: boolean
  latestTriggeredAt: string
  details: RiskAlertDetail[]
}

// 通知未读计数响应
export interface NotificationUnreadCount {
  total: number
  types: {
    RISK_ALERT?: number
    SYSTEM?: number
    [key: string]: number | undefined
  }
}