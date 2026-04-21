/**
 * 风险提醒 SSE Hook
 * 
 * <p>封装 SSE 连接状态和事件处理，提供 React 组件使用的接口
 */

import { useCallback, useEffect, useRef, useState } from 'react'
import { riskAlertSSEClient, type NewAlertPayload, type RiskClearedPayload, type UnreadCountChange } from '../services/sse/riskAlertSSE'
import { useAppDispatch } from '../store/hooks'
import { receiveRealtimeAlert, receiveRiskCleared, updateUnreadCount } from '../store/slices/riskAlertsSlice'

export interface UseRiskAlertSSEOptions {
    /** 是否启用 SSE */
    enabled?: boolean
    /** 用户 ID */
    userId: number
    /** 是否自动连接 */
    autoConnect?: boolean
}

export interface UseRiskAlertSSIReturn {
    /** 是否已连接 */
    isConnected: boolean
    /** 连接状态 */
    status: 'connecting' | 'connected' | 'disconnected' | 'error'
    /** 错误信息 */
    error: string | null
    /** 手动连接 */
    connect: () => void
    /** 手动断开 */
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
}: UseRiskAlertSSEOptions): UseRiskAlertSSIReturn {
    const dispatch = useAppDispatch()
    const [status, setStatus] = useState<'connecting' | 'connected' | 'disconnected' | 'error'>('disconnected')
    const [error, setError] = useState<string | null>(null)
    const isConnectedRef = useRef(false)

    // 处理新风险提醒事件
    const handleNewAlert = useCallback((data: NewAlertPayload) => {
        console.log('[useRiskAlertSSE] Received new_alert, dispatching to Redux:', data)
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
    const handleRiskCleared = useCallback((data: RiskClearedPayload) => {
        console.log('[useRiskAlertSSE] Received risk_cleared, dispatching to Redux:', data)
        dispatch(receiveRiskCleared(data))
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

        const unsubRiskCleared = riskAlertSSEClient.on('risk_cleared', (event) => {
            handleRiskCleared(event.data as RiskClearedPayload)
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

        // 自动连接
        if (autoConnect) {
            setStatus('connecting')
            riskAlertSSEClient.connect(userId)
        }

        return () => {
            // 取消订阅
            unsubNewAlert()
            unsubUnreadChange()
            unsubRiskCleared()
            unsubInit()
            unsubError()

            // 断开连接
            if (isConnectedRef.current) {
                riskAlertSSEClient.disconnect()
                isConnectedRef.current = false
            }
        }
    }, [enabled, userId, autoConnect, handleNewAlert, handleUnreadCountChange, handleRiskCleared])

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