import apiClient from './client'
import { RiskAlert, NotificationUnreadCount, ApiResponse } from '@/types'

export const riskAlertsApi = {
  // 获取用户风险提醒列表（合并后的数据）
  getRiskAlerts: (userId: number = 1, cursor?: number, limit: number = 20) => {
    const params: Record<string, number> = { limit }
    if (cursor) {
      params['cursor'] = cursor
    }
    return apiClient.get<ApiResponse<RiskAlert[]>>(`/risk-alerts/user/${userId}`, { params })
  },

  // 获取用户未读风险提醒数量
  getUnreadCount: (userId: number = 1) =>
    apiClient.get<ApiResponse<NotificationUnreadCount>>(`/risk-alerts/user/${userId}/unread-count`),

  // 标记所有风险提醒为已读
  markAllAsRead: (userId: number = 1) =>
    apiClient.post<ApiResponse<string>>(`/risk-alerts/user/${userId}/mark-read`),

  // 手动触发风险检测（测试用）
  checkRiskAlerts: () =>
    apiClient.post<ApiResponse<string>>('/risk-alerts/check'),
}

// 全局通知未读计数接口
export const notificationsApi = {
  getUnreadCount: () =>
    apiClient.get<ApiResponse<NotificationUnreadCount>>('/notifications/unread-count'),
}
