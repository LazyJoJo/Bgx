import { createSlice, createAsyncThunk } from '@reduxjs/toolkit'
import { Stock } from '@/types'
import { stocksApi } from '@services/api/stocks'

interface StockState {
  list: Stock[]
  selectedStock: Stock | null
  loading: boolean
  error: string | null
}

const initialState: StockState = {
  list: [],
  selectedStock: null,
  loading: false,
  error: null
}

export const fetchStocks = createAsyncThunk(
  'stocks/fetchStocks',
  async (params?: any) => {
    const response = await stocksApi.getStockTargets(params)
    return response.data
  }
)

export const fetchStockDetail = createAsyncThunk(
  'stocks/fetchStockDetail',
  async (symbol: string) => {
    const response = await stocksApi.getStockDetail(symbol)
    return response.data
  }
)

const stocksSlice = createSlice({
  name: 'stocks',
  initialState,
  reducers: {
    setSelectedStock: (state, action) => {
      state.selectedStock = action.payload
    },
    clearError: (state) => {
      state.error = null
    }
  },
  extraReducers: (builder) => {
    builder
      .addCase(fetchStocks.pending, (state) => {
        state.loading = true
        state.error = null
      })
      .addCase(fetchStocks.fulfilled, (state, action) => {
        state.loading = false
        state.list = action.payload
      })
      .addCase(fetchStocks.rejected, (state, action) => {
        state.loading = false
        state.error = action.error.message || '获取股票数据失败'
      })
      .addCase(fetchStockDetail.pending, (state) => {
        state.loading = true
        state.error = null
      })
      .addCase(fetchStockDetail.fulfilled, (state, action) => {
        state.loading = false
        state.selectedStock = action.payload
      })
      .addCase(fetchStockDetail.rejected, (state, action) => {
        state.loading = false
        state.error = action.error.message || '获取股票详情失败'
      })
  }
})

export const { setSelectedStock, clearError } = stocksSlice.actions
export default stocksSlice.reducer