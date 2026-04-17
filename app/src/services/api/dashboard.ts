import { ApiResponse } from '@/types'
import apiClient from './client'

export const dashboardApi = {
  // 获取统计数据（聚合调用 /api/dashboard/stats 获取订阅和风险提醒，
  // 并行调用 data-collection-targets 获取股票基金数量）
  getDashboardStats: async () => {
    // 并行调用：dashboard stats + 采集目标统计
    const [dashboardStatsRes, stockCountRes, fundCountRes] = await Promise.all([
      apiClient.get<ApiResponse<{
        subscriptionCount: number
        activeSubscriptionCount: number
        todayRiskAlertCount: number
        unreadRiskAlertCount: number
      }>>('/dashboard/stats'),
      apiClient.get<ApiResponse<number>>('/data-collection-targets/count/type/STOCK'),
      apiClient.get<ApiResponse<number>>('/data-collection-targets/count/type/FUND')
    ])

    const dashboardStats = dashboardStatsRes.data || {
      subscriptionCount: 0,
      activeSubscriptionCount: 0,
      todayRiskAlertCount: 0,
      unreadRiskAlertCount: 0
    }


    return {
      totalStocks: stockCountRes.data || 0,
      totalFunds: fundCountRes.data || 0,
      // activeAlerts: 活跃订阅数（来自 dashboardStats）
      activeAlerts: dashboardStats.activeSubscriptionCount,
      // triggeredAlerts: 今日触发风险提醒数（映射自 todayRiskAlertCount）
      // 注意：此字段与后端 todayRiskAlertCount 对应，非未读提醒数
      triggeredAlerts: dashboardStats.todayRiskAlertCount
    }
  },
}