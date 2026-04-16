import { configureStore } from '@reduxjs/toolkit'
import stocksReducer from './slices/stocksSlice'
import fundsReducer from './slices/fundsSlice'
import riskAlertsReducer from './slices/riskAlertsSlice'
import notificationsReducer from './slices/notificationsSlice'
import subscriptionsReducer from './slices/subscriptionsSlice'

export const store = configureStore({
  reducer: {
    stocks: stocksReducer,
    funds: fundsReducer,
    riskAlerts: riskAlertsReducer,
    notifications: notificationsReducer,
    subscriptions: subscriptionsReducer,
  },
})

export type RootState = ReturnType<typeof store.getState>
export type AppDispatch = typeof store.dispatch