import { createSlice, createAsyncThunk } from '@reduxjs/toolkit'
import { Fund } from '@/types'
import { fundsApi } from '@services/api/funds'

interface FundState {
  list: Fund[]
  selectedFund: Fund | null
  loading: boolean
  error: string | null
}

const initialState: FundState = {
  list: [],
  selectedFund: null,
  loading: false,
  error: null
}

export const fetchFunds = createAsyncThunk(
  'funds/fetchFunds',
  async (params?: any) => {
    const response = await fundsApi.getFundTargets(params)
    return response.data
  }
)

export const fetchFundDetail = createAsyncThunk(
  'funds/fetchFundDetail',
  async (fundCode: string) => {
    const response = await fundsApi.getFundDetail(fundCode)
    return response.data
  }
)

const fundsSlice = createSlice({
  name: 'funds',
  initialState,
  reducers: {
    setSelectedFund: (state, action) => {
      state.selectedFund = action.payload
    },
    clearError: (state) => {
      state.error = null
    }
  },
  extraReducers: (builder) => {
    builder
      .addCase(fetchFunds.pending, (state) => {
        state.loading = true
        state.error = null
      })
      .addCase(fetchFunds.fulfilled, (state, action) => {
        state.loading = false
        state.list = action.payload
      })
      .addCase(fetchFunds.rejected, (state, action) => {
        state.loading = false
        state.error = action.error.message || '获取基金数据失败'
      })
      .addCase(fetchFundDetail.pending, (state) => {
        state.loading = true
        state.error = null
      })
      .addCase(fetchFundDetail.fulfilled, (state, action) => {
        state.loading = false
        state.selectedFund = action.payload
      })
      .addCase(fetchFundDetail.rejected, (state, action) => {
        state.loading = false
        state.error = action.error.message || '获取基金详情失败'
      })
  }
})

export const { setSelectedFund, clearError } = fundsSlice.actions
export default fundsSlice.reducer