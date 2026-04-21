import type { RiskAlert } from '@/types'
import { createAsyncThunk, createSlice, type PayloadAction } from '@reduxjs/toolkit'
import { riskAlertsApi } from '@services/api/riskAlerts'
import type { NewAlertPayload, RiskClearedPayload } from '@services/sse/riskAlertSSE'

// Re-export for convenience
export type { NewAlertPayload } from '@services/sse/riskAlertSSE'

// SSE 推送的未读数变化载荷
interface UnreadCountPayload {
  unreadCount: number
  reason: 'NEW_ALERT' | 'MARK_READ' | 'RISK_CLEARED'
}

interface RiskAlertState {
  list: RiskAlert[]
  unreadCount: number
  loading: boolean
  hasMore: boolean
  cursor: number | null
  error: string | null
}

const initialState: RiskAlertState = {
  list: [],
  unreadCount: 0,
  loading: false,
  hasMore: true,
  cursor: null,
  error: null,
}

// 获取用户风险提醒列表（分页）
export const fetchRiskAlerts = createAsyncThunk(
  'riskAlerts/fetchRiskAlerts',
  async ({ cursor, limit = 20 }: { cursor?: number; limit?: number } = {}, { rejectWithValue }) => {
    try {
      const userId = Number(localStorage.getItem('userId')) || 1
      const response = await riskAlertsApi.getRiskAlerts(userId, cursor, limit)
      if (response.success) {
        return {
          data: response.data,
          hasMore: response.data.length === limit,
        }
      }
      return rejectWithValue(response.message || '获取风险提醒列表失败')
    } catch (error: any) {
      return rejectWithValue(error.message || '获取风险提醒列表失败')
    }
  }
)

// 获取未读数量
export const fetchRiskAlertUnreadCount = createAsyncThunk(
  'riskAlerts/fetchUnreadCount',
  async (_, { rejectWithValue }) => {
    try {
      const userId = Number(localStorage.getItem('userId')) || 1
      const response = await riskAlertsApi.getUnreadCount(userId)
      if (response.success) {
        return response.data.total
      }
      return rejectWithValue(response.message || '获取未读数量失败')
    } catch (error: any) {
      return rejectWithValue(error.message || '获取未读数量失败')
    }
  }
)

// 标记所有已读
export const markRiskAlertsAsRead = createAsyncThunk(
  'riskAlerts/markAllAsRead',
  async (_, { rejectWithValue }) => {
    try {
      const userId = Number(localStorage.getItem('userId')) || 1
      const response = await riskAlertsApi.markAllAsRead(userId)
      if (response.success) {
        return true
      }
      return rejectWithValue(response.message || '标记已读失败')
    } catch (error: any) {
      return rejectWithValue(error.message || '标记已读失败')
    }
  }
)

const riskAlertsSlice = createSlice({
  name: 'riskAlerts',
  initialState,
  reducers: {
    clearError: (state) => {
      state.error = null
    },
    resetList: (state) => {
      state.list = []
      state.cursor = null
      state.hasMore = true
    },
    // SSE 推送：接收实时新提醒
    receiveRealtimeAlert: (state, action: PayloadAction<NewAlertPayload>) => {
      console.log('[riskAlertsSlice] receiveRealtimeAlert called with:', action.payload)
      const newAlert = action.payload

      // Fallback date: use latestTriggeredAt date if date is missing
      const dateFallback = newAlert.date || (newAlert.latestTriggeredAt
        ? newAlert.latestTriggeredAt.split('T')[0]
        : new Date().toISOString().split('T')[0])

      // 检查是否已存在该提醒（按 symbol + date 去重）
      const key = `${newAlert.symbol}_${dateFallback}`
      const existingIndex = state.list.findIndex((a) => `${a.symbol}_${a.date}` === key)
      console.log('[riskAlertsSlice] Deduplication check - key:', key, ', existingIndex:', existingIndex)

      if (existingIndex >= 0) {
        // 已存在则更新
        state.list[existingIndex] = {
          ...state.list[existingIndex],
          ...newAlert,
          date: dateFallback,
          isRead: false // 重置为未读
        }
        console.log('[riskAlertsSlice] Updated existing alert at index:', existingIndex)
      } else {
        // 新增到列表头部（使用后端传来的真实 ID）
        const newRiskAlert: RiskAlert = {
          id: newAlert.id ?? (Date.now() * 1000 + Math.floor(Math.random() * 1000)),
          symbol: newAlert.symbol,
          symbolName: newAlert.symbolName,
          symbolType: newAlert.symbolType,
          date: dateFallback,
          latestChangePercent: newAlert.latestChangePercent,
          maxChangePercent: newAlert.maxChangePercent,
          currentPrice: newAlert.currentPrice,
          yesterdayClose: newAlert.yesterdayClose,
          latestTriggeredAt: newAlert.latestTriggeredAt,
          triggerCount: newAlert.triggerCount,
          isRead: false,
          details: newAlert.details || []
        }
        state.list = [newRiskAlert, ...state.list]
        console.log('[riskAlertsSlice] Added new alert, total list length:', state.list.length)
      }
    },
    // SSE 推送：接收风险解除事件 - 从列表中移除已解除的风险提醒
    receiveRiskCleared: (state, action: PayloadAction<RiskClearedPayload>) => {
      const cleared = action.payload
      console.log('[riskAlertsSlice] receiveRiskCleared:', cleared)

      // 从列表中移除该 symbol 的风险提醒（通过 symbol + date 匹配）
      state.list = state.list.filter(item => {
        // 检查是否是同一 symbol 的风险提醒
        const symbolMatch = item.symbol === cleared.symbol &&
          item.symbolType === cleared.symbolType
        // 日期匹配（使用触发日期）
        const dateMatch = item.date === cleared.date

        if (symbolMatch && dateMatch) {
          console.log('[riskAlertsSlice] Removing cleared alert:', item.symbol, item.date)
        }
        return !(symbolMatch && dateMatch)
      })

      // 减少未读数（如果之前有未读）
      if (state.unreadCount && state.unreadCount > 0) {
        state.unreadCount = state.unreadCount - 1
      }
    },
    // SSE 推送：更新未读数
    updateUnreadCount: (state, action: PayloadAction<UnreadCountPayload>) => {
      const { unreadCount, reason } = action.payload
      state.unreadCount = unreadCount

      // 如果是 MARK_READ，需要更新列表中的已读状态
      if (reason === 'MARK_READ') {
        state.list = state.list.map((alert) => ({ ...alert, isRead: true }))
      }
    },
  },
  extraReducers: (builder) => {
    builder
      // fetchRiskAlerts
      .addCase(fetchRiskAlerts.pending, (state) => {
        state.loading = true
        state.error = null
      })
      .addCase(fetchRiskAlerts.fulfilled, (state, action) => {
        state.loading = false
        const newAlerts = action.payload.data
        if (action.payload.data.length > 0) {
          // 设置新的 cursor 为最后一条的触发时间戳
          const lastAlert = newAlerts[newAlerts.length - 1]
          state.cursor = new Date(lastAlert.latestTriggeredAt).getTime()
        }
        state.hasMore = action.payload.hasMore
        // 合并到列表（避免重复）
        const existingIds = new Set(state.list.map((a) => `${a.symbol}_${a.date}`))
        const filtered = newAlerts.filter(
          (a) => !existingIds.has(`${a.symbol}_${a.date}`)
        )
        state.list = [...state.list, ...filtered]
      })
      .addCase(fetchRiskAlerts.rejected, (state, action) => {
        state.loading = false
        state.error = action.payload as string
      })
      // fetchRiskAlertUnreadCount
      .addCase(fetchRiskAlertUnreadCount.fulfilled, (state, action) => {
        state.unreadCount = action.payload
      })
      // markRiskAlertsAsRead
      .addCase(markRiskAlertsAsRead.fulfilled, (state) => {
        state.unreadCount = 0
        // 标记所有为已读
        state.list = state.list.map((alert) => ({ ...alert, isRead: true }))
      })
  },
})

export const { clearError, resetList, receiveRealtimeAlert, receiveRiskCleared, updateUnreadCount } = riskAlertsSlice.actions
export default riskAlertsSlice.reducer
