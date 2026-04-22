import { ApiResponse } from '@/types'
import apiClient from './client'

// 订阅类型
export type SymbolType = 'STOCK' | 'FUND'
export type AlertType = 'PRICE_ABOVE' | 'PRICE_BELOW' | 'PERCENTAGE_CHANGE'
export type SubscriptionStatus = 'ACTIVE' | 'INACTIVE' | 'TRIGGERED'

// 订阅接口
export interface Subscription {
  id: number
  userId: number
  symbol: string
  symbolName: string
  symbolType: SymbolType
  alertType: AlertType
  targetPrice?: number
  targetChangePercent?: number
  currentPrice?: number
  status: SubscriptionStatus
  remark?: string
  validFrom?: string
  validUntil?: string
  createdAt: string
  updatedAt: string
}

// 创建订阅请求
export interface CreateSubscriptionRequest {
  userId: number
  symbol: string
  symbolName?: string
  symbolType: SymbolType
  alertType: AlertType
  targetPrice?: number
  targetChangePercent?: number
  remark?: string
  validFrom?: string
  validUntil?: string
  isActive?: boolean
}

// 批量创建订阅请求
export interface BatchCreateSubscriptionRequest {
  userId: number
  symbolType: SymbolType
  symbols: string[]
  symbolNames?: string[]
  alertType: AlertType
  targetPrice?: number
  targetChangePercent?: number
  remark?: string
  isActive?: boolean
}

// 批量创建响应
export interface BatchCreateSubscriptionResponse {
  batchId: string
  totalCount: number
  successCount: number
  failureCount: number
  successList: Array<{
    symbol: string
    symbolName: string
    subscriptionId: number
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
  symbolType: SymbolType
  symbols: string[]
  alertType: AlertType
}

// 重复检测响应
export interface CheckDuplicatesResponse {
  checkedCount: number
  duplicateCount: number
  duplicates: Array<{
    symbol: string
    symbolName: string
    existingSubscriptions: Array<{
      subscriptionId: number
      alertType: string
      targetPrice?: number
      targetChangePercent?: number
      createdAt: string
      status: string
    }>
  }>
  availableSymbols: string[]
}

export const subscriptionsApi = {
  // 获取用户订阅列表
  getSubscriptions: (userId: number, params?: {
    symbolType?: SymbolType
    status?: SubscriptionStatus
    alertType?: AlertType
    symbol?: string
    limit?: number
    offset?: number
  }) => {
    return apiClient.get<ApiResponse<Subscription[]>>(`/subscriptions/user/${userId}`, { params })
  },

  // 获取订阅详情
  getSubscription: (id: number) => {
    return apiClient.get<ApiResponse<Subscription>>(`/subscriptions/${id}`)
  },

  // 创建订阅
  createSubscription: (data: CreateSubscriptionRequest) => {
    return apiClient.post<ApiResponse<Subscription>>('/subscriptions', data)
  },

  // 批量创建订阅
  batchCreateSubscriptions: (data: BatchCreateSubscriptionRequest) => {
    return apiClient.post<ApiResponse<BatchCreateSubscriptionResponse>>('/subscriptions/batch', data)
  },

  // 批量删除订阅
  batchDeleteSubscriptions: (ids: number[]) => {
    return apiClient.delete<ApiResponse<{ deletedCount: number }>>('/subscriptions/batch', { data: { ids } })
  },

  // 批量启用订阅
  batchActivateSubscriptions: (ids: number[]) => {
    return apiClient.patch<ApiResponse<{ activatedCount: number }>>('/subscriptions/batch/activate', { ids })
  },

  // 批量停用订阅
  batchDeactivateSubscriptions: (ids: number[]) => {
    return apiClient.patch<ApiResponse<{ deactivatedCount: number }>>('/subscriptions/batch/deactivate', { ids })
  },

  // 更新订阅
  updateSubscription: (id: number, data: Partial<CreateSubscriptionRequest>) => {
    return apiClient.put<ApiResponse<Subscription>>(`/subscriptions/${id}`, data)
  },

  // 删除订阅
  deleteSubscription: (id: number) => {
    return apiClient.delete<ApiResponse<string>>(`/subscriptions/${id}`)
  },

  // 启用订阅
  activateSubscription: (id: number) => {
    return apiClient.patch<ApiResponse<string>>(`/subscriptions/${id}/activate`)
  },

  // 停用订阅
  deactivateSubscription: (id: number) => {
    return apiClient.patch<ApiResponse<string>>(`/subscriptions/${id}/deactivate`)
  },

  // 重复检测
  checkDuplicates: (data: CheckDuplicatesRequest) => {
    return apiClient.post<ApiResponse<CheckDuplicatesResponse>>('/subscriptions/check-duplicates', data)
  },
}
