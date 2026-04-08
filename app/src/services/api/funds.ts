import apiClient from './client'
import { ApiResponse } from '@/types'

// 基金实时行情数据类型
export interface FundQuoteData {
  fundCode: string
  fundName: string
  quoteDate: string
  quoteTimeOnly: string
  nav: number
  prevNetValue: number
  changeAmount: number
  changePercent: number
}

export const fundsApi = {
  // 获取基金采集目标列表（替代原来的 getFunds）
  getFundTargets: (params?: any) => 
    apiClient.get<ApiResponse<any[]>>('/data-collection-targets/type/FUND', { params }),

  // 获取基金详情（按代码查询）
  getFundDetail: (fundCode: string) => 
    apiClient.get<ApiResponse<any>>(`/data-collection-targets/code/${fundCode}`),

  // 创建基金采集目标
  createFundTarget: (data: any) => 
    apiClient.post<ApiResponse<any>>('/data-collection-targets', data),

  // 更新基金采集目标
  updateFundTarget: (id: number, data: any) => 
    apiClient.put<ApiResponse<any>>(`/data-collection-targets/${id}`, data),

  // 删除基金采集目标
  deleteFundTarget: (id: number) => 
    apiClient.delete<ApiResponse<string>>(`/data-collection-targets/${id}`),

  // 激活基金采集目标
  activateFundTarget: (id: number) => 
    apiClient.post<ApiResponse<string>>(`/data-collection-targets/${id}/activate`),

  // 停用基金采集目标
  deactivateFundTarget: (id: number) => 
    apiClient.post<ApiResponse<string>>(`/data-collection-targets/${id}/deactivate`),

  // 快速添加基金采集目标
  addFundTarget: (fundCode: string) => 
    apiClient.post<ApiResponse<any>>('/data-collection-targets/add-fund', null, { params: { fundCode } }),

  // ========== 基金分析相关接口 ==========

  // 分页查询基金净值（支持条件搜索、后端分页和排序）
  getQuotesPage: (params: {
    pageNum?: number
    pageSize?: number
    fundCode?: string
    fundName?: string
    startDate?: string
    endDate?: string
    orderBy?: string
    orderDirection?: 'ASC' | 'DESC'
  }) => 
    apiClient.get<ApiResponse<{records: FundQuoteData[], total: number, pageNum: number, pageSize: number, pages: number}>>('/fund-analysis/quotes/page', { params }),

  // 从数据库获取所有基金最新净值（默认加载）
  getLatestQuotes: () => 
    apiClient.get<ApiResponse<FundQuoteData[]>>('/fund-analysis/quotes/latest'),

  // 实时刷新基金行情（调用外部API并保存到数据库）
  refreshQuotes: () => 
    apiClient.post<ApiResponse<FundQuoteData[]>>('/fund-analysis/quotes/refresh'),

  // 获取单个基金实时行情（从外部API）
  getFundQuote: (fundCode: string) => 
    apiClient.get<ApiResponse<FundQuoteData>>(`/fund-analysis/quote/${fundCode}`),

  // 获取单个基金最新净值（从数据库）
  getLatestFundQuote: (fundCode: string) => 
    apiClient.get<ApiResponse<FundQuoteData>>(`/fund-analysis/quote/latest/${fundCode}`),

  // 获取基金历史净值
  getFundHistory: (fundCode: string, days: number = 30) => 
    apiClient.get<ApiResponse<FundQuoteData[]>>(`/fund-analysis/history/${fundCode}`, { params: { days } }),

  // 获取基金统计信息
  getFundStatistics: () => 
    apiClient.get<ApiResponse<{ totalFunds: number, risingCount: number, fallingCount: number, flatCount: number }>>('/fund-analysis/statistics'),
}