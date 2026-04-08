import { createAsyncThunk, createSlice, PayloadAction } from '@reduxjs/toolkit'
import { notificationsApi } from '@services/api/riskAlerts'

interface NotificationTypes {
  RISK_ALERT?: number
  SYSTEM?: number
  [key: string]: number | undefined
}

interface NotificationState {
  totalUnreadCount: number
  breakdown: NotificationTypes
  loading: boolean
  error: string | null
}

const initialState: NotificationState = {
  totalUnreadCount: 0,
  breakdown: {},
  loading: false,
  error: null,
}

// 获取全局未读计数
export const fetchNotificationUnreadCount = createAsyncThunk(
  'notifications/fetchUnreadCount',
  async (_, { rejectWithValue }) => {
    try {
      const response = await notificationsApi.getUnreadCount()
      if (response.success) {
        return response.data
      }
      return rejectWithValue(response.message || '获取未读计数失败')
    } catch (error: any) {
      return rejectWithValue(error.message || '获取未读计数失败')
    }
  }
)

const notificationsSlice = createSlice({
  name: 'notifications',
  initialState,
  reducers: {
    // 更新单个类型的未读计数
    updateTypeCount: (
      state,
      action: PayloadAction<{ type: string; count: number }>
    ) => {
      const { type, count } = action.payload
      state.breakdown[type] = count
      // 重新计算总数
      state.totalUnreadCount = Object.values(state.breakdown).reduce(
        (sum: number, val) => sum + (val || 0),
        0
      )
    },
    // 增加未读计数
    incrementUnreadCount: (
      state,
      action: PayloadAction<{ type: string; delta: number }>
    ) => {
      const { type, delta } = action.payload
      state.breakdown[type] = (state.breakdown[type] || 0) + delta
      state.totalUnreadCount = Object.values(state.breakdown).reduce(
        (sum: number, val) => sum + (val || 0),
        0
      )
    },
    clearError: (state) => {
      state.error = null
    },
  },
  extraReducers: (builder) => {
    builder
      .addCase(fetchNotificationUnreadCount.pending, (state) => {
        state.loading = true
        state.error = null
      })
      .addCase(fetchNotificationUnreadCount.fulfilled, (state, action) => {
        state.loading = false
        state.totalUnreadCount = action.payload.total
        state.breakdown = action.payload.types
      })
      .addCase(fetchNotificationUnreadCount.rejected, (state, action) => {
        state.loading = false
        state.error = action.payload as string
      })
  },
})

export const { updateTypeCount, incrementUnreadCount, clearError } =
  notificationsSlice.actions
export default notificationsSlice.reducer
