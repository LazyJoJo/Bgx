import apiClient from './client'
import { ApiResponse } from '@/types'

export const dashboardApi = {
  // 获取统计数据（使用采集目标的统计接口）
  getDashboardStats: async () => {
    // 并行调用多个统计接口
    const [stockCountRes, fundCountRes, activeCountRes] = await Promise.all([
      apiClient.get<ApiResponse<number>>('/data-collection-targets/count/type/STOCK'),
      apiClient.get<ApiResponse<number>>('/data-collection-targets/count/type/FUND'),
      apiClient.get<ApiResponse<number>>('/data-collection-targets/count/active')
    ])
    
    return {
      totalStocks: stockCountRes.data || 0,
      totalFunds: fundCountRes.data || 0,
      activeAlerts: activeCountRes.data || 0,
      triggeredAlerts: 0  // 这个需要后端新增接口，暂时返回 0
    }
  },

  // 获取最近提醒（使用用户 ID=1 的默认值）
  getRecentAlerts: (limit?: number) => 
    apiClient.get<ApiResponse<any[]>>(`/alerts/user/1?limit=${limit || 5}`),
}