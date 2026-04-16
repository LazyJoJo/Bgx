import { createAsyncThunk, createSlice, PayloadAction } from '@reduxjs/toolkit'
import { subscriptionsApi, Subscription, CreateSubscriptionRequest, BatchCreateSubscriptionRequest, BatchCreateSubscriptionResponse, SymbolType, SubscriptionStatus, AlertType } from '@services/api/subscriptions'

interface SubscriptionFilters {
  symbolType?: SymbolType
  status?: SubscriptionStatus
  alertType?: AlertType
  symbol?: string
}

interface SubscriptionState {
  list: Subscription[]
  selectedSubscription: Subscription | null
  loading: boolean
  error: string | null
  filters: SubscriptionFilters
  pagination: {
    total: number
    limit: number
    offset: number
  }
  // 批量操作相关状态
  batchCreate: {
    loading: boolean
    result: BatchCreateSubscriptionResponse | null
    error: string | null
  }
}

const initialState: SubscriptionState = {
  list: [],
  selectedSubscription: null,
  loading: false,
  error: null,
  filters: {},
  pagination: {
    total: 0,
    limit: 20,
    offset: 0,
  },
  batchCreate: {
    loading: false,
    result: null,
    error: null,
  },
}

// 获取用户订阅列表
export const fetchSubscriptions = createAsyncThunk(
  'subscriptions/fetchSubscriptions',
  async (filters: SubscriptionFilters | undefined, { rejectWithValue }) => {
    try {
      const userId = Number(localStorage.getItem('userId')) || 1
      const response = await subscriptionsApi.getSubscriptions(userId, filters)
      if (response.success) {
        return response.data
      }
      return rejectWithValue(response.message || '获取订阅列表失败')
    } catch (error: any) {
      return rejectWithValue(error.message || '获取订阅列表失败')
    }
  }
)

// 获取订阅详情
export const fetchSubscription = createAsyncThunk(
  'subscriptions/fetchSubscription',
  async (id: number, { rejectWithValue }) => {
    try {
      const response = await subscriptionsApi.getSubscription(id)
      if (response.success) {
        return response.data
      }
      return rejectWithValue(response.message || '获取订阅详情失败')
    } catch (error: any) {
      return rejectWithValue(error.message || '获取订阅详情失败')
    }
  }
)

// 创建订阅
export const createSubscription = createAsyncThunk(
  'subscriptions/createSubscription',
  async (data: CreateSubscriptionRequest, { rejectWithValue }) => {
    try {
      const response = await subscriptionsApi.createSubscription(data)
      if (response.success) {
        return response.data
      }
      return rejectWithValue(response.message || '创建订阅失败')
    } catch (error: any) {
      return rejectWithValue(error.message || '创建订阅失败')
    }
  }
)

// 批量创建订阅
export const batchCreateSubscriptions = createAsyncThunk(
  'subscriptions/batchCreate',
  async (data: BatchCreateSubscriptionRequest, { rejectWithValue }) => {
    try {
      const response = await subscriptionsApi.batchCreateSubscriptions(data)
      if (response.success) {
        return response.data
      }
      return rejectWithValue(response.message || '批量创建失败')
    } catch (error: any) {
      return rejectWithValue(error.response?.data?.message || '批量创建失败')
    }
  }
)

// 批量删除订阅
export const batchDeleteSubscriptions = createAsyncThunk(
  'subscriptions/batchDelete',
  async (ids: number[], { rejectWithValue }) => {
    try {
      const response = await subscriptionsApi.batchDeleteSubscriptions(ids)
      if (response.success) {
        return ids
      }
      return rejectWithValue(response.message || '批量删除失败')
    } catch (error: any) {
      return rejectWithValue(error.response?.data?.message || '批量删除失败')
    }
  }
)

// 批量启用订阅
export const batchActivateSubscriptions = createAsyncThunk(
  'subscriptions/batchActivate',
  async (ids: number[], { rejectWithValue }) => {
    try {
      const response = await subscriptionsApi.batchActivateSubscriptions(ids)
      if (response.success) {
        return ids
      }
      return rejectWithValue(response.message || '批量启用失败')
    } catch (error: any) {
      return rejectWithValue(error.response?.data?.message || '批量启用失败')
    }
  }
)

// 批量停用订阅
export const batchDeactivateSubscriptions = createAsyncThunk(
  'subscriptions/batchDeactivate',
  async (ids: number[], { rejectWithValue }) => {
    try {
      const response = await subscriptionsApi.batchDeactivateSubscriptions(ids)
      if (response.success) {
        return ids
      }
      return rejectWithValue(response.message || '批量停用失败')
    } catch (error: any) {
      return rejectWithValue(error.response?.data?.message || '批量停用失败')
    }
  }
)

// 更新订阅
export const updateSubscription = createAsyncThunk(
  'subscriptions/updateSubscription',
  async ({ id, data }: { id: number; data: Partial<CreateSubscriptionRequest> }, { rejectWithValue }) => {
    try {
      const response = await subscriptionsApi.updateSubscription(id, data)
      if (response.success) {
        return response.data
      }
      return rejectWithValue(response.message || '更新订阅失败')
    } catch (error: any) {
      return rejectWithValue(error.message || '更新订阅失败')
    }
  }
)

// 删除订阅
export const deleteSubscription = createAsyncThunk(
  'subscriptions/deleteSubscription',
  async (id: number, { rejectWithValue }) => {
    try {
      const response = await subscriptionsApi.deleteSubscription(id)
      if (response.success) {
        return id
      }
      return rejectWithValue(response.message || '删除订阅失败')
    } catch (error: any) {
      return rejectWithValue(error.message || '删除订阅失败')
    }
  }
)

// 启用订阅
export const activateSubscription = createAsyncThunk(
  'subscriptions/activate',
  async (id: number, { rejectWithValue }) => {
    try {
      const response = await subscriptionsApi.activateSubscription(id)
      if (response.success) {
        return { id, status: 'ACTIVE' }
      }
      return rejectWithValue(response.message || '启用订阅失败')
    } catch (error: any) {
      return rejectWithValue(error.message || '启用订阅失败')
    }
  }
)

// 停用订阅
export const deactivateSubscription = createAsyncThunk(
  'subscriptions/deactivate',
  async (id: number, { rejectWithValue }) => {
    try {
      const response = await subscriptionsApi.deactivateSubscription(id)
      if (response.success) {
        return { id, status: 'INACTIVE' }
      }
      return rejectWithValue(response.message || '停用订阅失败')
    } catch (error: any) {
      return rejectWithValue(error.message || '停用订阅失败')
    }
  }
)

const subscriptionsSlice = createSlice({
  name: 'subscriptions',
  initialState,
  reducers: {
    setSelectedSubscription: (state, action: PayloadAction<Subscription | null>) => {
      state.selectedSubscription = action.payload
    },
    setFilters: (state, action: PayloadAction<SubscriptionFilters>) => {
      state.filters = action.payload
    },
    clearFilters: (state) => {
      state.filters = {}
    },
    clearError: (state) => {
      state.error = null
    },
    clearBatchResult: (state) => {
      state.batchCreate.result = null
      state.batchCreate.error = null
    },
  },
  extraReducers: (builder) => {
    builder
      // fetchSubscriptions
      .addCase(fetchSubscriptions.pending, (state) => {
        state.loading = true
        state.error = null
      })
      .addCase(fetchSubscriptions.fulfilled, (state, action) => {
        state.loading = false
        state.list = action.payload as unknown as Subscription[]
      })
      .addCase(fetchSubscriptions.rejected, (state, action) => {
        state.loading = false
        state.error = action.payload as string
      })
      // fetchSubscription
      .addCase(fetchSubscription.fulfilled, (state, action) => {
        state.selectedSubscription = action.payload
      })
      // createSubscription
      .addCase(createSubscription.fulfilled, (state, action) => {
        state.list.unshift(action.payload)
      })
      // updateSubscription
      .addCase(updateSubscription.fulfilled, (state, action) => {
        const index = state.list.findIndex(s => s.id === action.payload.id)
        if (index !== -1) {
          state.list[index] = action.payload
        }
      })
      // deleteSubscription
      .addCase(deleteSubscription.fulfilled, (state, action) => {
        state.list = state.list.filter(s => s.id !== action.payload)
      })
      // activateSubscription
      .addCase(activateSubscription.fulfilled, (state, action) => {
        const index = state.list.findIndex(s => s.id === action.payload.id)
        if (index !== -1) {
          state.list[index].status = 'ACTIVE'
        }
      })
      // deactivateSubscription
      .addCase(deactivateSubscription.fulfilled, (state, action) => {
        const index = state.list.findIndex(s => s.id === action.payload.id)
        if (index !== -1) {
          state.list[index].status = 'INACTIVE'
        }
      })
      // batchDeleteSubscriptions
      .addCase(batchDeleteSubscriptions.fulfilled, (state, action) => {
        const deletedIds = action.payload
        state.list = state.list.filter(s => !deletedIds.includes(s.id))
      })
      // batchActivateSubscriptions
      .addCase(batchActivateSubscriptions.fulfilled, (state, action) => {
        const activatedIds = action.payload
        state.list.forEach(s => {
          if (activatedIds.includes(s.id)) {
            s.status = 'ACTIVE'
          }
        })
      })
      // batchDeactivateSubscriptions
      .addCase(batchDeactivateSubscriptions.fulfilled, (state, action) => {
        const deactivatedIds = action.payload
        state.list.forEach(s => {
          if (deactivatedIds.includes(s.id)) {
            s.status = 'INACTIVE'
          }
        })
      })
      // batchCreateSubscriptions
      .addCase(batchCreateSubscriptions.pending, (state) => {
        state.batchCreate.loading = true
        state.batchCreate.error = null
        state.batchCreate.result = null
      })
      .addCase(batchCreateSubscriptions.fulfilled, (state, action) => {
        state.batchCreate.loading = false
        state.batchCreate.result = action.payload
      })
      .addCase(batchCreateSubscriptions.rejected, (state, action) => {
        state.batchCreate.loading = false
        state.batchCreate.error = action.payload as string
      })
  },
})

export const { setSelectedSubscription, setFilters, clearFilters, clearError, clearBatchResult } = subscriptionsSlice.actions
export default subscriptionsSlice.reducer
