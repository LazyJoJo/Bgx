import { AlertHistory, PriceAlert } from '@/types'
import { createAsyncThunk, createSlice } from '@reduxjs/toolkit'
import { alertsApi } from '@services/api/alerts'

interface AlertState {
  list: PriceAlert[]
  history: AlertHistory[]
  selectedAlert: PriceAlert | null
  loading: boolean
  error: string | null
}

const initialState: AlertState = {
  list: [],
  history: [],
  selectedAlert: null,
  loading: false,
  error: null
}

export const fetchAlerts = createAsyncThunk(
  'alerts/fetchAlerts',
  async (params?: any) => {
    // 从localStorage获取用户ID，如果没有则使用默认值1
    const userId = localStorage.getItem('userId') || '1'
    const response = await alertsApi.getAlerts(Number(userId), params)
    return response.data
  }
)

export const fetchAlertHistory = createAsyncThunk(
  'alerts/fetchAlertHistory',
  async () => {
    const userId = localStorage.getItem('userId') || '1'
    const response = await alertsApi.getAlertHistory(Number(userId))
    return response.data
  }
)

export const createAlert = createAsyncThunk(
  'alerts/createAlert',
  async (alertData: any) => {
    const response = await alertsApi.createAlert(alertData)
    return response.data
  }
)

export const updateAlert = createAsyncThunk(
  'alerts/updateAlert',
  async ({ id, data }: { id: string; data: any }) => {
    const response = await alertsApi.updateAlert(id, data)
    return response.data
  }
)

export const deleteAlert = createAsyncThunk(
  'alerts/deleteAlert',
  async (id: string) => {
    await alertsApi.deleteAlert(id)
    return id
  }
)

const alertsSlice = createSlice({
  name: 'alerts',
  initialState,
  reducers: {
    setSelectedAlert: (state, action) => {
      state.selectedAlert = action.payload
    },
    clearError: (state) => {
      state.error = null
    }
  },
  extraReducers: (builder) => {
    builder
      .addCase(fetchAlerts.pending, (state) => {
        state.loading = true
        state.error = null
      })
      .addCase(fetchAlerts.fulfilled, (state, action) => {
        state.loading = false
        state.list = action.payload
      })
      .addCase(fetchAlerts.rejected, (state, action) => {
        state.loading = false
        state.error = action.error.message || '获取提醒列表失败'
      })
      .addCase(fetchAlertHistory.fulfilled, (state, action) => {
        state.history = action.payload
      })
      .addCase(createAlert.fulfilled, (state, action) => {
        state.list.push(action.payload)
      })
      .addCase(updateAlert.fulfilled, (state, action) => {
        const index = state.list.findIndex(alert => alert.id === action.payload.id)
        if (index !== -1) {
          state.list[index] = action.payload
        }
      })
      .addCase(deleteAlert.fulfilled, (state, action) => {
        state.list = state.list.filter(alert => alert.id !== action.payload)
      })
  }
})

export const { setSelectedAlert, clearError } = alertsSlice.actions
export default alertsSlice.reducer