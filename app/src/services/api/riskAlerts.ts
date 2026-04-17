import { ApiResponse, NotificationUnreadCount, RiskAlert } from '@/types'
import apiClient from './client'

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

  // 标记单条风险提醒为已读
  markAsRead: (id: number) =>
    apiClient.patch<ApiResponse<string>>(`/risk-alerts/${id}/read`),

  // 手动触发风险检测（测试用）
  checkRiskAlerts: () =>
    apiClient.post<ApiResponse<string>>('/risk-alerts/check'),

  // 获取用户当天风险提醒数量（用于仪表盘，与已读/未读无关）
  getTodayRiskAlertCount: (userId: number = 1) =>
    apiClient.get<ApiResponse<{ total: number }>>(`/risk-alerts/user/${userId}/today-count`),
}

// 全局通知未读计数接口
export const notificationsApi = {
  getUnreadCount: () =>
    apiClient.get<ApiResponse<NotificationUnreadCount>>('/notifications/unread-count'),
}
