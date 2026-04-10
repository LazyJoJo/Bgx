import apiClient from './client'
import { PriceAlert, AlertHistory, ApiResponse } from '@/types'

// 创建提醒响应类型
export interface CreateAlertResponse {
  created: boolean
  alert: PriceAlert
  message: string
}

export const alertsApi = {
  // 获取用户提醒列表（后端需要 userId，暂时用 1 作为默认值）
  getAlerts: (userId: number = 1, params?: any) =>
    apiClient.get<ApiResponse<PriceAlert[]>>(`/alerts/user/${userId}`, { params }),

  // 获取提醒详情
  getAlert: (id: string) =>
    apiClient.get<ApiResponse<PriceAlert>>(`/alerts/${id}`),

  // 创建提醒
  createAlert: (data: any) =>
    apiClient.post<ApiResponse<CreateAlertResponse>>('/alerts', data),

  // 更新提醒
  updateAlert: (id: string, data: any) =>
    apiClient.put<ApiResponse<PriceAlert>>(`/alerts/${id}`, data),

  // 删除提醒
  deleteAlert: (id: string) => 
    apiClient.delete<ApiResponse<string>>(`/alerts/${id}`),

  // 获取用户激活的提醒列表
  getUserActiveAlerts: (userId: number = 1) => 
    apiClient.get<ApiResponse<PriceAlert[]>>(`/alerts/user/${userId}/active`),

  // 获取提醒历史
  getAlertHistory: (userId: number = 1, alertId?: number) => {
    if (alertId) {
      return apiClient.get<ApiResponse<AlertHistory[]>>(`/alerts/user/${userId}/alert/${alertId}/history`)
    }
    return apiClient.get<ApiResponse<AlertHistory[]>>(`/alerts/user/${userId}/history`)
  },

  // 激活提醒
  activateAlert: (id: string) => 
    apiClient.post<ApiResponse<string>>(`/alerts/${id}/activate`),

  // 停用提醒
  deactivateAlert: (id: string) =>
    apiClient.post<ApiResponse<string>>(`/alerts/${id}/deactivate`),

  // 批量创建提醒
  batchCreateAlert: (data: {
    userId: number
    symbols: string[]
    symbolType: string
    symbolName?: string
    alertType: string
    targetPrice?: number
    targetChangePercent?: number
    basePrice?: number
    status: boolean | string
  }) => {
    // 转换status为后端期望的格式
    const backendData = {
      ...data,
      status: typeof data.status === 'boolean' ? (data.status ? 'ACTIVE' : 'INACTIVE') : data.status
    }
    return apiClient.post<ApiResponse<any>>('/alerts/batch', backendData)
  },
}