/**
 * 风险提醒 SSE Hook
 * 
 * <p>封装 SSE 连接状态和事件处理，提供 React 组件使用的接口
 */

import { useCallback, useEffect, useRef, useState } from 'react'
import { riskAlertSSEClient, type AlertClearedPayload, type NewAlertPayload, type UnreadCountChange } from '../services/sse/riskAlertSSE'
import { useAppDispatch } from '../store/hooks'
import { receiveAlertCleared, receiveRealtimeAlert, syncMissedAlerts, updateUnreadCount } from '../store/slices/riskAlertsSlice'

export interface UseRiskAlertSSEOptions {
    /** 是否启用 SSE */
    enabled?: boolean
    /** 用户 ID */
    userId: number
    /** 是否自动连接 */
    autoConnect?: boolean
}

export interface UseRiskAlertSSEReturn {
    /** Whether SSE is connected */
    isConnected: boolean
    /** Connection status */
    status: 'connecting' | 'connected' | 'disconnected' | 'error'
    /** Error message */
    error: string | null
    /** Manual connect */
    connect: () => void
    /** Manual disconnect */
    disconnect: () => void
}

/**
 * 风险提醒 SSE Hook
 * 
 * <p>管理 SSE 连接生命周期，自动处理重连
 */
export function useRiskAlertSSE({
    enabled = true,
    userId,
    autoConnect = true
}: UseRiskAlertSSEOptions): UseRiskAlertSSEReturn {
    const dispatch = useAppDispatch()
    const [status, setStatus] = useState<'connecting' | 'connected' | 'disconnected' | 'error'>('disconnected')
    const [error, setError] = useState<string | null>(null)
    const isConnectedRef = useRef(false)

    // 处理新风险提醒事件
    const handleNewAlert = useCallback((data: NewAlertPayload) => {
        dispatch(receiveRealtimeAlert(data))
    }, [dispatch])

    // 处理未读数变化事件
    const handleUnreadCountChange = useCallback((data: UnreadCountChange) => {
        dispatch(updateUnreadCount({
            unreadCount: data.unreadCount,
            reason: data.reason
        }))
    }, [dispatch])

    // 处理风险解除事件
    const handleAlertCleared = useCallback((data: AlertClearedPayload) => {
        dispatch(receiveAlertCleared(data))
    }, [dispatch])

    // 初始化 SSE 连接
    useEffect(() => {
        if (!enabled || !userId) {
            return
        }

        // 订阅事件
        const unsubNewAlert = riskAlertSSEClient.on('new_alert', (event) => {
            handleNewAlert(event.data as NewAlertPayload)
        })

        const unsubUnreadChange = riskAlertSSEClient.on('unread_count_change', (event) => {
            handleUnreadCountChange(event.data as UnreadCountChange)
        })

        const unsubAlertCleared = riskAlertSSEClient.on('alert_cleared', (event) => {
            handleAlertCleared(event.data as AlertClearedPayload)
        })

        const unsubInit = riskAlertSSEClient.on('init', () => {
            setStatus('connected')
            setError(null)
            isConnectedRef.current = true
        })

        const unsubError = riskAlertSSEClient.on('error', (event) => {
            const errorData = event.data as { code?: string; message?: string }
            setStatus('error')
            setError(errorData.message || 'SSE connection error')
            isConnectedRef.current = false
        })

        // Listen for reconnected event - sync missed alerts after reconnection
        // NOTE: dispatch is from Redux toolkit and is a stable reference.
        // syncMissedAlerts is a thunk (async action), not a callback function,
        // so it doesn't need to be in the dependency array.
        // It is a stable reference that won't change between renders.
        const unsubReconnected = riskAlertSSEClient.on('reconnected', () => {
            setStatus('connected')
            setError(null)
            isConnectedRef.current = true
            // Reconnected after disconnection - sync missed alerts
            // Only sync if userId is valid to avoid unnecessary API calls
            // NOTE: We no longer call fetchRiskAlertUnreadCount() here because:
            // 1. SSE will push unread_count_change events after reconnection, providing the correct count
            // 2. fetchRiskAlertUnreadCount() may return stale data if backend hasn't committed new alerts
            // If the user sees stale data after reconnection, they can navigate to refresh
            if (userId) {
                dispatch(syncMissedAlerts())
            }
        })

        // 自动连接
        if (autoConnect) {
            setStatus('connecting')
            riskAlertSSEClient.connect(userId)
        }

        return () => {
            // 取消订阅
            unsubNewAlert()
            unsubUnreadChange()
            unsubAlertCleared()
            unsubInit()
            unsubError()
            unsubReconnected()

            // 断开连接
            if (isConnectedRef.current) {
                riskAlertSSEClient.disconnect()
                isConnectedRef.current = false
            }
        }
    }, [enabled, userId, autoConnect, handleNewAlert, handleUnreadCountChange, handleAlertCleared, dispatch])

    // 手动连接
    const connect = useCallback(() => {
        if (!isConnectedRef.current) {
            setStatus('connecting')
            riskAlertSSEClient.connect(userId)
        }
    }, [userId])

    // 手动断开
    const disconnect = useCallback(() => {
        if (isConnectedRef.current) {
            riskAlertSSEClient.disconnect()
            setStatus('disconnected')
            isConnectedRef.current = false
        }
    }, [])

    return {
        isConnected: status === 'connected',
        status,
        error,
        connect,
        disconnect
    }
}
