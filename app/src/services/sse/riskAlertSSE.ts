/**
 * 风险提醒 SSE 客户端封装
 * 
 * <p>封装 EventSource API，处理 SSE 连接、重连和事件分发
 */


export type SSEEventType = 'init' | 'ping' | 'new_alert' | 'unread_count_change' | 'error' | 'risk_cleared'

export interface SSEEvent<T = unknown> {
    type: SSEEventType
    data: T
}

export interface UnreadCountChange {
    unreadCount: number
    delta: number
    reason: 'NEW_ALERT' | 'MARK_READ' | 'RISK_CLEARED'
    timestamp: number
}

// SSE 推送的新提醒载荷（与后端 NewAlertPayload 对齐）
export interface NewAlertPayload {
    id?: number // 数据库真实 ID（后端推送时提供）
    symbol: string
    symbolName: string
    symbolType: 'STOCK' | 'FUND'
    date: string
    latestChangePercent: number
    maxChangePercent: number
    currentPrice: number
    yesterdayClose: number
    latestTriggeredAt: string
    triggerCount: number
    isRead: boolean
    details?: any[]
    messageId?: string
}

// SSE 推送的风险消除载荷（与后端 RiskClearedPayload 对齐）
export interface RiskClearedPayload {
    id: number
    symbol: string
    symbolName: string
    symbolType: 'STOCK' | 'FUND'
    date: string
    lastChangePercent: number
    currentChangePercent: number
    currentPrice: number
    latestTriggeredAt: string
}

type EventCallback<T = unknown> = (event: SSEEvent<T>) => void

const SSE_BASE_URL = import.meta.env.VITE_API_BASE_URL || ''

export class RiskAlertSSEClient {
    private eventSource: EventSource | null = null
    private userId: number | null = null
    private reconnectDelay = 1000
    private maxReconnectDelay = 30000
    private reconnectTimer: ReturnType<typeof setTimeout> | null = null
    private listeners: Map<SSEEventType, Set<EventCallback>> = new Map()
    private isConnecting = false
    private isManualClose = false

    constructor() {
        // 初始化事件类型监听集合
        const eventTypes: SSEEventType[] = ['init', 'ping', 'new_alert', 'unread_count_change', 'error', 'risk_cleared']
        eventTypes.forEach(type => this.listeners.set(type, new Set()))
    }

    /**
     * 连接到 SSE 端点
     */
    connect(userId: number): void {
        if (this.isConnecting || this.eventSource) {
            return
        }

        this.userId = userId
        this.isManualClose = false
        this.doConnect()
    }

    private doConnect(): void {
        if (!this.userId) return

        this.isConnecting = true
        const url = `${SSE_BASE_URL}/api/risk-alerts/stream?userId=${this.userId}`

        try {
            this.eventSource = new EventSource(url, { withCredentials: true })

            // 监听 open 事件
            this.eventSource.onopen = () => {
                this.isConnecting = false
                this.reconnectDelay = 1000 // 重置重连延迟

                this.emit('init', { connected: true, timestamp: Date.now() })
            }

            // 监听 all 事件（自定义事件）
            this.eventSource.onmessage = (event) => {
                try {
                    const parsed = JSON.parse(event.data)
                    // 根据 event 字段判断类型
                    if (parsed.event) {
                        this.emit(parsed.event as SSEEventType, parsed.data || parsed)
                    }
                } catch (e) {
                    // 忽略无法解析的消息
                }
            }

            // 监听 error 事件
            this.eventSource.onerror = () => {
                this.isConnecting = false
                this.closeEventSource()
                this.scheduleReconnect()
            }

            // 监听自定义事件
            this.setupCustomEventListeners()

        } catch (error) {
            this.isConnecting = false
            this.scheduleReconnect()
        }
    }

    private setupCustomEventListeners(): void {
        if (!this.eventSource) return

        // 监听 ping 事件
        this.eventSource.addEventListener('ping', (event) => {
            try {
                const data = JSON.parse(event.data)
                this.emit('ping', data)
            } catch (e) {
                this.emit('ping', { timestamp: Date.now() })
            }
        })

        // 监听 new_alert 事件
        this.eventSource.addEventListener('new_alert', (event) => {
            try {
                console.log('[SSE] new_alert event received:', event.data)
                const sseEvent = JSON.parse(event.data)
                const data: NewAlertPayload = sseEvent.payload ?? sseEvent
                console.log('[SSE] Parsed new_alert data:', data)
                this.emit('new_alert', data)
            } catch (e) {
                console.error('[SSE] Failed to parse new_alert event:', e)
                // Silently ignore parse errors for new_alert
            }
        })

        // 监听 unread_count_change 事件
        this.eventSource.addEventListener('unread_count_change', (event) => {
            try {
                const sseEvent = JSON.parse(event.data)
                const data: UnreadCountChange = sseEvent.payload ?? sseEvent
                this.emit('unread_count_change', data)
            } catch (e) {
                // Silently ignore parse errors for unread_count_change
            }
        })

        // 监听 risk_cleared 事件
        this.eventSource.addEventListener('risk_cleared', (event) => {
            try {
                console.log('[SSE] risk_cleared event received:', event.data)
                const sseEvent = JSON.parse(event.data)
                const data: RiskClearedPayload = sseEvent.payload ?? sseEvent
                console.log('[SSE] Parsed risk_cleared data:', data)
                this.emit('risk_cleared', data)
            } catch (e) {
                console.error('[SSE] Failed to parse risk_cleared event:', e)
            }
        })

        // 监听 error 事件
        this.eventSource.addEventListener('error', (event: MessageEvent) => {
            try {
                const sseEvent = JSON.parse(event.data)
                const data = sseEvent.payload ?? sseEvent
                this.emit('error', data)
            } catch (e) {
                this.emit('error', { message: 'SSE error occurred' })
            }
        })
    }

    /**
     * 断开 SSE 连接
     */
    disconnect(): void {
        this.isManualClose = true
        this.clearReconnectTimer()
        this.closeEventSource()
        this.userId = null
    }

    private closeEventSource(): void {
        if (this.eventSource) {
            this.eventSource.close()
            this.eventSource = null
        }
    }

    /**
     * 调度重连
     */
    private scheduleReconnect(): void {
        if (this.isManualClose || this.reconnectTimer) {
            return
        }

        this.reconnectTimer = setTimeout(() => {
            this.reconnectTimer = null
            this.doConnect()
        }, this.reconnectDelay)

        // 指数退避，最大 30 秒（添加 jitter 避免惊群效应）
        this.reconnectDelay = Math.min(this.reconnectDelay * 2 + Math.random() * 1000, this.maxReconnectDelay)
    }

    private clearReconnectTimer(): void {
        if (this.reconnectTimer) {
            clearTimeout(this.reconnectTimer)
            this.reconnectTimer = null
        }
    }

    /**
     * 订阅 SSE 事件
     */
    on<T = unknown>(eventType: SSEEventType, callback: EventCallback<T>): () => void {
        const listeners = this.listeners.get(eventType)
        if (listeners) {
            listeners.add(callback as EventCallback)
        }

        // 返回取消订阅函数
        return () => this.off(eventType, callback)
    }

    /**
     * 取消订阅 SSE 事件
     */
    off<T = unknown>(eventType: SSEEventType, callback: EventCallback<T>): void {
        const listeners = this.listeners.get(eventType)
        if (listeners) {
            listeners.delete(callback as EventCallback)
        }
    }

    /**
     * 触发事件
     */
    private emit<T>(eventType: SSEEventType, data: T): void {
        const listeners = this.listeners.get(eventType)
        if (listeners) {
            const event: SSEEvent<T> = { type: eventType, data }
            listeners.forEach(callback => {
                try {
                    callback(event)
                } catch (e) {
                    // Silently ignore callback errors to prevent cascading failures
                }
            })
        }
    }

    /**
     * 检查是否已连接
     */
    isConnected(): boolean {
        return this.eventSource !== null && !this.isConnecting
    }

    /**
     * 获取当前用户 ID
     */
    getUserId(): number | null {
        return this.userId
    }
}

// 单例 SSE 客户端
export const riskAlertSSEClient = new RiskAlertSSEClient()