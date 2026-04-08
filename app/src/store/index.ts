import { configureStore } from '@reduxjs/toolkit'
import stocksReducer from './slices/stocksSlice'
import fundsReducer from './slices/fundsSlice'
import alertsReducer from './slices/alertsSlice'
import riskAlertsReducer from './slices/riskAlertsSlice'
import notificationsReducer from './slices/notificationsSlice'

export const store = configureStore({
  reducer: {
    stocks: stocksReducer,
    funds: fundsReducer,
    alerts: alertsReducer,
    riskAlerts: riskAlertsReducer,
    notifications: notificationsReducer,
  },
})

export type RootState = ReturnType<typeof store.getState>
export type AppDispatch = typeof store.dispatch