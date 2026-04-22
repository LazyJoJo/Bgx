/**
 * 风险提醒 SSE 客户端封装
 * 
 * <p>封装 EventSource API，处理 SSE 连接、重连和事件分发
 */


export type SSEEventType = 'init' | 'ping' | 'new_alert' | 'unread_count_change' | 'error' | 'alert_cleared' | 'reconnected'

// Reconnection configuration constants
const INITIAL_RECONNECT_DELAY_MS = 1000
const MAX_RECONNECT_DELAY_MS = 30000
const RECONNECT_DELAY_JITTER_MS = 1000

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

export interface ReconnectedPayload {
    lastEventId: string | null
    disconnectDuration: number
}

// SSE 推送的新提醒载荷（与后端 NewAlertPayload 对齐）
export interface NewAlertPayload {
    id?: number // 数据库真实 ID（后端推送时提供）
    symbol: string
    symbolName: string
    symbolType: 'STOCK' | 'FUND'
    date: string
    status: 'ACTIVE' | 'CLEARED' | 'NO_ALERT'  // 跟踪状态
    latestChangePercent: number    // 当前最新涨跌幅
    maxChangePercent: number       // 当日最高涨幅
    minChangePercent: number       // 当日最低跌幅
    currentPrice: number
    yesterdayClose: number
    latestTriggeredAt: string
    isRead: boolean
    details?: any[]  // 触发次数用 details.length 计算
    messageId?: string
}

// SSE 推送的风险解除载荷（与后端 AlertClearedPayload 对齐）
// NOTE: Backend sends Long type for id, but JavaScript number can safely represent
// integers up to 2^53-1 (9007199254740991). Risk alert IDs are auto-increment BIGINT
// and should be well within JavaScript's safe integer range. Using number type here
// for convenience, but be aware of potential precision loss if IDs exceed safe range.
export interface AlertClearedPayload {
    id: number
    symbol: string
    symbolName: string
    symbolType: 'STOCK' | 'FUND'
    date: string
    status: 'CLEARED'  // 始终为 CLEARED
    lastChangePercent: number
    currentChangePercent: number
    maxChangePercent: number       // 保留当日最高涨幅
    minChangePercent: number       // 保留当日最低跌幅
    currentPrice: number
    latestTriggeredAt: string
    details?: any[]  // 触发次数用 details.length 计算
}

type EventCallback<T = unknown> = (event: SSEEvent<T>) => void

const SSE_BASE_URL = import.meta.env.VITE_API_BASE_URL || ''

export class RiskAlertSSEClient {
    private eventSource: EventSource | null = null
    private userId: number | null = null
    private reconnectDelay = INITIAL_RECONNECT_DELAY_MS
    private reconnectTimer: ReturnType<typeof setTimeout> | null = null
    private listeners: Map<SSEEventType, Set<EventCallback>> = new Map()
    private isConnecting = false
    private isManualClose = false
    private lastEventId: string | null = null
    private disconnectTime: number | null = null
    private isFirstConnect = true

    constructor() {
        // 初始化事件类型监听集合
        const eventTypes: SSEEventType[] = ['init', 'ping', 'new_alert', 'unread_count_change', 'error', 'alert_cleared', 'reconnected']
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
        const url = `${SSE_BASE_URL}/api/risk-alerts/stream?userId=${this.userId}${this.lastEventId ? `&lastEventId=${encodeURIComponent(this.lastEventId)}` : ''}`

        try {
            this.eventSource = new EventSource(url, { withCredentials: true })

            // 监听 open 事件
            this.eventSource.onopen = () => {
                this.isConnecting = false
                this.reconnectDelay = INITIAL_RECONNECT_DELAY_MS

                if (this.isFirstConnect) {
                    this.emit('init', { connected: true, timestamp: Date.now() })
                    this.isFirstConnect = false
                } else {
                    const disconnectDuration = this.disconnectTime ? Date.now() - this.disconnectTime : 0
                    this.emit('reconnected', { lastEventId: this.lastEventId, disconnectDuration })
                }
                this.disconnectTime = null
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
                this.disconnectTime = Date.now()
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
                const sseEvent = JSON.parse(event.data)
                const data: NewAlertPayload = sseEvent.payload ?? sseEvent
                // Update lastEventId if messageId is present
                if (data.messageId) {
                    this.lastEventId = data.messageId
                }
                this.emit('new_alert', data)
            } catch (e) {
                // Silently ignore parse errors for new_alert
            }
        })

        // 监听 unread_count_change 事件
        this.eventSource.addEventListener('unread_count_change', (event) => {
            try {
                const sseEvent = JSON.parse(event.data)
                const data: UnreadCountChange = sseEvent.payload ?? sseEvent
                // Update lastEventId if messageId is present
                if ((data as any).messageId) {
                    this.lastEventId = (data as any).messageId
                }
                this.emit('unread_count_change', data)
            } catch (e) {
                // Silently ignore parse errors for unread_count_change
            }
        })

        // 监听 alert_cleared 事件
        this.eventSource.addEventListener('alert_cleared', (event) => {
            try {
                const sseEvent = JSON.parse(event.data)
                const data: AlertClearedPayload = sseEvent.payload ?? sseEvent
                // Update lastEventId if messageId is present
                if ((data as any).messageId) {
                    this.lastEventId = (data as any).messageId
                }
                this.emit('alert_cleared', data)
            } catch (e) {
                // Silently ignore parse errors for alert_cleared
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
        this.isFirstConnect = true
        this.lastEventId = null
        this.disconnectTime = null
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

        // Exponential backoff with jitter to avoid thundering herd
        this.reconnectDelay = Math.min(
            this.reconnectDelay * 2 + Math.random() * RECONNECT_DELAY_JITTER_MS,
            MAX_RECONNECT_DELAY_MS
        )
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
