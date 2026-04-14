import apiClient from './client'
import { PriceAlert, AlertHistory, ApiResponse } from '@/types'

// 创建提醒响应类型
export interface CreateAlertResponse {
  created: boolean
  alert: PriceAlert
  message: string
}

// 批量创建提醒请求
export interface BatchCreateAlertRequest {
  userId: number
  symbolType: 'STOCK' | 'FUND'
  symbols: string[]
  alertType: 'PRICE_ABOVE' | 'PRICE_BELOW' | 'PERCENTAGE_CHANGE'
  targetPrice?: number
  targetChangePercent?: number
  notifyChannels?: string[]
  remark?: string
}

// 批量创建提醒响应
export interface BatchCreateAlertResponse {
  batchId: string
  totalCount: number
  successCount: number
  failureCount: number
  successList: Array<{
    symbol: string
    symbolName: string
    alertId: number
    createdAt: string
  }>
  failureList: Array<{
    symbol: string
    symbolName: string
    reason: string
    errorCode: string
  }>
}

// 重复检测请求
export interface CheckDuplicatesRequest {
  userId: number
  symbolType: 'STOCK' | 'FUND'
  symbols: string[]
  alertType: 'PRICE_ABOVE' | 'PRICE_BELOW' | 'PERCENTAGE_CHANGE'
}

// 重复检测响应
export interface CheckDuplicatesResponse {
  checkedCount: number
  duplicateCount: number
  duplicates: Array<{
    symbol: string
    symbolName: string
    existingAlerts: Array<{
      alertId: number
      alertType: string
      targetPrice?: number
      targetChangePercent?: number
      createdAt: string
      status: string
    }>
  }>
  availableSymbols: string[]
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
  batchCreateAlert: (data: BatchCreateAlertRequest) => {
    return apiClient.post<ApiResponse<BatchCreateAlertResponse>>('/alerts/batch/v2', data)
  },

  // 检测重复提醒
  checkDuplicates: (data: CheckDuplicatesRequest) => {
    return apiClient.post<ApiResponse<CheckDuplicatesResponse>>('/alerts/check-duplicates', data)
  },
}