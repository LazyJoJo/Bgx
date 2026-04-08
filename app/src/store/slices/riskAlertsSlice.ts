import { createAsyncThunk, createSlice } from '@reduxjs/toolkit'
import { RiskAlert } from '@/types'
import { riskAlertsApi } from '@services/api/riskAlerts'

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

export const { clearError, resetList } = riskAlertsSlice.actions
export default riskAlertsSlice.reducer
