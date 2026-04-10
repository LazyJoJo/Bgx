import apiClient from './client'
import { ApiResponse } from '@/types'

export const stocksApi = {
  // 获取股票采集目标列表（替代原来的 getStocks）
  getStockTargets: (params?: any) => 
    apiClient.get<ApiResponse<any[]>>('/data-collection-targets/type/STOCK', { params }),

  // 获取股票详情（按代码查询）
  getStockDetail: (symbol: string) => 
    apiClient.get<ApiResponse<any>>(`/data-collection-targets/code/${symbol}`),

  // 创建股票采集目标
  createStockTarget: (data: any) => 
    apiClient.post<ApiResponse<any>>('/data-collection-targets', data),

  // 更新股票采集目标
  updateStockTarget: (id: number, data: any) => 
    apiClient.put<ApiResponse<any>>(`/data-collection-targets/${id}`, data),

  // 删除股票采集目标
  deleteStockTarget: (id: number) => 
    apiClient.delete<ApiResponse<string>>(`/data-collection-targets/${id}`),

  // 激活股票采集目标
  activateStockTarget: (id: number) => 
    apiClient.post<ApiResponse<string>>(`/data-collection-targets/${id}/activate`),

  // 停用股票采集目标
  deactivateStockTarget: (id: number) =>
    apiClient.post<ApiResponse<string>>(`/data-collection-targets/${id}/deactivate`),

  // 搜索采集目标（股票和基金）
  searchTargets: (keyword: string, type?: string) =>
    apiClient.get<ApiResponse<any[]>>('/data-collection-targets/search', { params: { keyword, type } }),
}