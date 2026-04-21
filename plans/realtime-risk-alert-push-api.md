# 实时风险提醒推送 — API 接口文档

> 本文档定义 SSE（Server-Sent Events）推送相关的新增/修改接口，与现有 REST API 完全兼容。

---

## 1. 接口概览

| 方法 | 路径 | 协议 | 说明 |
|------|------|------|------|
| `GET` | `/api/risk-alerts/stream` | SSE | 建立用户专属推送流 |

现有 REST 接口保持不变：

| 方法 | 路径 | 说明 |
|------|------|------|
| `GET` | `/api/risk-alerts/user/{userId}` | 分页获取风险提醒列表 |
| `GET` | `/api/risk-alerts/user/{userId}/unread-count` | 获取未读数量 |
| `POST` | `/api/risk-alerts/user/{userId}/mark-read` | 标记全部已读 |
| `PATCH` | `/api/risk-alerts/{id}/read` | 标记单条已读 |

---

## 2. SSE 推送流

### 2.1 建立连接

```http
GET /api/risk-alerts/stream HTTP/1.1
Host: localhost:8080
Accept: text/event-stream
Authorization: Bearer {token}
Cache-Control: no-cache
```

**响应头**：

```http
HTTP/1.1 200 OK
Content-Type: text/event-stream; charset=UTF-8
Cache-Control: no-cache
Connection: keep-alive
X-Accel-Buffering: no
```

> `X-Accel-Buffering: no` 用于告知 Nginx 等反向代理不要缓存 SSE 响应。

**查询参数**：

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `userId` | `number` | 是 | 当前用户 ID（后续接入 JWT 后可从 Token 解析，此参数废弃） |

**连接生命周期**：

- 连接建立后，服务端首先发送一次 `init` 事件，携带当前未读数量。
- 之后连接保持长连接，等待服务端推送事件。
- 服务端每 **30 秒**发送一次 `ping` 心跳事件。
- 连接超时：**30 分钟**（可配置），超时后客户端自动重连。

---

### 2.2 事件格式

SSE 使用标准 `data:` 行传递 JSON，事件类型通过 `event:` 行标识。

#### 通用信封结构

```
event: {eventType}
id: {messageId}
data: {jsonPayload}

```

#### 事件类型清单

| 事件类型 | 触发时机 | 说明 |
|----------|----------|------|
| `init` | 连接建立后 | 携带当前未读数，用于前端初始化 |
| `ping` | 每 30 秒 | 心跳保活 |
| `new_alert` | 新的风险提醒产生 | 推送完整提醒数据 |
| `unread_count_change` | 未读数量变化 | 仅推送数量变化（减少场景） |
| `risk_cleared` | 风险解除 | 之前有风险的标的恢复正常，风险提醒移除 |

#### 2.2.1 `init` — 连接初始化

```
event: init
data: {"unreadCount":5,"timestamp":"2026-04-20T11:30:05+08:00"}

```

#### 2.2.2 `ping` — 心跳

```
event: ping
data: {"timestamp":"2026-04-20T11:30:30+08:00"}

```

#### 2.2.3 `new_alert` — 新风险提醒

```
event: new_alert
data: {"symbol":"000001","symbolName":"平安银行","symbolType":"STOCK","date":"2026-04-20","latestChangePercent":-2.35,"latestTriggeredAt":"2026-04-20T11:30:00+08:00","triggerCount":1,"isRead":false}

```

**payload 字段说明**：

| 字段 | 类型 | 说明 |
|------|------|------|
| `symbol` | `string` | 标的代码 |
| `symbolName` | `string` | 标的名称 |
| `symbolType` | `string` | `STOCK` / `FUND` |
| `date` | `string` | 提醒日期（YYYY-MM-DD） |
| `latestChangePercent` | `number` | 最新涨跌幅（%），保留 2 位小数 |
| `latestTriggeredAt` | `string` | 最新触发时间（ISO 8601） |
| `triggerCount` | `number` | 当日该标的触发次数 |
| `isRead` | `boolean` | 是否已读 |

#### 2.2.4 `unread_count_change` — 未读数变化

```
event: unread_count_change
data: {"unreadCount":6,"delta":1,"reason":"NEW_ALERT","timestamp":"2026-04-20T11:30:05+08:00"}

```

| 字段 | 类型 | 说明 |
|------|------|------|
| `unreadCount` | `number` | 变化后的未读总数 |
| `delta` | `number` | 变化量（+1 / -N） |
| `reason` | `string` | 变化原因：`NEW_ALERT` / `MARK_READ` / `RISK_CLEARED` |

#### 2.2.5 `risk_cleared` — 风险解除

```
event: risk_cleared
data: {"id":123,"symbol":"000001","symbolName":"平安银行","symbolType":"STOCK","date":"2026-04-20","lastChangePercent":-2.35,"currentChangePercent":-0.65,"currentPrice":12.35,"latestTriggeredAt":"2026-04-20T11:30:00+08:00"}

```

| 字段 | 类型 | 说明 |
|------|------|------|
| `id` | `number` | 被解除的风险提醒 ID |
| `symbol` | `string` | 标的代码 |
| `symbolName` | `string` | 标的名称 |
| `symbolType` | `string` | `STOCK` / `FUND` |
| `date` | `string` | 提醒日期（YYYY-MM-DD） |
| `lastChangePercent` | `number` | 解除前的涨跌幅（%） |
| `currentChangePercent` | `number` | 当前涨跌幅（已恢复到安全范围） |
| `currentPrice` | `number` | 当前价格 |
| `latestTriggeredAt` | `string` | 最后触发时间（ISO 8601） |

---

## 3. 后端接口定义

### 3.1 DTO

#### `RiskAlertSSEEvent<T>`

```java
public class RiskAlertSSEEvent<T> {
    private String eventType;   // init | ping | new_alert | unread_count_change | risk_cleared
    private String messageId;   // UUID
    private T payload;
    private Instant timestamp;
}
```

#### `NewAlertPayload`

```java
public class NewAlertPayload {
    private String symbol;
    private String symbolName;
    private String symbolType;
    private String date;
    private BigDecimal latestChangePercent;
    private BigDecimal maxChangePercent;   // 当日最大涨跌幅，首次触发时等于 latestChangePercent
    private BigDecimal currentPrice;
    private BigDecimal yesterdayClose;
    private Instant latestTriggeredAt;
    private int triggerCount;
    private boolean isRead;
    private List<RiskAlertDetailPayload> details; // 明细列表，新建时为空列表
}
```

#### `RiskAlertDetailPayload`

```java
public class RiskAlertDetailPayload {
    private Long id;
    private BigDecimal changePercent;
    private BigDecimal currentPrice;
    private Instant triggeredAt;
    private String triggerReason;
}
```

> **字段对齐说明**：`NewAlertPayload` 和 `RiskAlertDetailPayload` 的字段需与前端 `RiskAlert` / `RiskAlertDetail` 类型完全对齐，确保推送数据可直接插入 Redux store 和列表渲染，无需二次请求补齐字段。

#### `UnreadCountPayload`

```java
public class UnreadCountPayload {
    private long unreadCount;
    private int delta;
    private String reason;
    private Instant timestamp;
}
```

#### `RiskClearedPayload`

```java
public class RiskClearedPayload {
    private Long id;
    private String symbol;
    private String symbolName;
    private String symbolType;
    private String date;
    private BigDecimal lastChangePercent;      // 解除前的涨跌幅
    private BigDecimal currentChangePercent;  // 当前涨跌幅（已恢复）
    private BigDecimal currentPrice;
    private String latestTriggeredAt;
}
```

### 3.2 Controller

#### `RiskAlertSSEController`

```java
@RestController
@RequestMapping("/api/risk-alerts")
@Tag(name = "风险提醒推送", description = "SSE 实时推送接口")
public class RiskAlertSSEController {

    @Autowired
    private RiskAlertPushService pushService;

    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(summary = "订阅风险提醒推送流", description = "建立 SSE 长连接，接收实时风险提醒")
    public ResponseEntity<SseEmitter> stream(
            @RequestParam(required = false) Long userId,
            @RequestParam(defaultValue = "1800000") Long timeout) {
        // AC-7-1: userId 必填校验
        if (userId == null) {
            return ResponseEntity.badRequest().build();
        }

        // AC-7-2: 身份冒用校验（当前为 mock 环境，实际应从 JWT Token 解析 userId）
        Long currentUserId = getCurrentUserId();
        if (currentUserId != null && !currentUserId.equals(userId)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        SseEmitter emitter = new SseEmitter(timeout);
        pushService.register(userId, emitter);
        return ResponseEntity.ok(emitter);
    }

    private Long getCurrentUserId() {
        // TODO: 接入 JWT 后从 SecurityContext 解析
        return null;
    }
}
```

> **注意**：当前 mock 环境下 `getCurrentUserId()` 返回 `null`，表示不执行身份冒用校验。接入真实登录态后应返回当前登录用户的 ID。

### 3.3 推送服务接口

```java
public interface RiskAlertPushService {

    /** 单个用户最大 SSE 连接数（对应 AC-7） */
    int MAX_CONNECTIONS_PER_USER = 3;

    /**
     * 注册用户的 SSE 连接（允许多连接，最多 {@link #MAX_CONNECTIONS_PER_USER} 条）
     * 当连接数超过上限时，按 FIFO 移除最早的连接
     */
    void register(Long userId, SseEmitter emitter);

    /**
     * 向指定用户的所有活跃连接广播新风险提醒
     */
    void pushNewAlert(Long userId, NewAlertPayload payload);

    /**
     * 向指定用户的所有活跃连接广播未读数变化
     */
    void pushUnreadCountChange(Long userId, UnreadCountPayload payload);

    /**
     * 向指定用户的所有活跃连接广播风险解除事件
     */
    void pushRiskCleared(Long userId, RiskClearedPayload payload);

    /**
     * 向指定用户的所有活跃连接发送心跳
     */
    void sendPing(Long userId);

    /**
     * 向所有用户的所有活跃连接发送心跳（全局心跳任务调用）
     */
    void sendPingToAll();
}
```

#### 单机内存实现（支持多连接）

```java
@Service
@Slf4j
public class InMemoryRiskAlertPushService implements RiskAlertPushService {

    // userId -> 该用户的所有 SseEmitter 连接
    private final ConcurrentHashMap<Long, CopyOnWriteArrayList<SseEmitter>> userEmitters = new ConcurrentHashMap<>();

    @Override
    public void register(Long userId, SseEmitter emitter) {
        CopyOnWriteArrayList<SseEmitter> emitters = userEmitters.computeIfAbsent(userId, k -> new CopyOnWriteArrayList<>());

        // AC-7: 连接数上限控制（FIFO 驱逐）
        while (emitters.size() >= MAX_CONNECTIONS_PER_USER) {
            SseEmitter oldest = emitters.get(0);
            emitters.remove(0);
            try {
                oldest.complete(); // 强制关闭最早建立的连接
                log.warn("SSE 连接数超限，移除最早连接: userId={}, 移除后连接数={}", userId, emitters.size());
            } catch (Exception e) {
                log.warn("关闭旧 SSE 连接异常: userId={}", userId);
            }
        }

        emitters.add(emitter);

        // 连接关闭/超时/异常时自动清理
        emitter.onCompletion(() -> removeEmitter(userId, emitter));
        emitter.onTimeout(() -> removeEmitter(userId, emitter));
        emitter.onError((e) -> removeEmitter(userId, emitter));

        log.info("SSE 连接注册: userId={}, 当前连接数={}", userId, emitters.size());
    }

    private void removeEmitter(Long userId, SseEmitter emitter) {
        CopyOnWriteArrayList<SseEmitter> emitters = userEmitters.get(userId);
        if (emitters != null) {
            emitters.remove(emitter);
            if (emitters.isEmpty()) {
                userEmitters.remove(userId);
            }
            log.info("SSE 连接移除: userId={}, 剩余连接数={}", userId,
                userEmitters.getOrDefault(userId, new CopyOnWriteArrayList<>()).size());
        }
    }

    @Override
    public void pushNewAlert(Long userId, NewAlertPayload payload) {
        List<SseEmitter> emitters = userEmitters.getOrDefault(userId, new CopyOnWriteArrayList<>());
        RiskAlertSSEEvent<NewAlertPayload> event = new RiskAlertSSEEvent<>("new_alert", payload);

        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(SseEmitter.event()
                    .name("new_alert")
                    .id(UUID.randomUUID().toString())
                    .data(event));
            } catch (Exception e) {
                log.warn("推送失败，移除失效连接: userId={}", userId);
                removeEmitter(userId, emitter);
            }
        }
    }

    @Override
    public void pushUnreadCountChange(Long userId, UnreadCountPayload payload) {
        List<SseEmitter> emitters = userEmitters.getOrDefault(userId, new CopyOnWriteArrayList<>());
        RiskAlertSSEEvent<UnreadCountPayload> event = new RiskAlertSSEEvent<>("unread_count_change", payload);

        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(SseEmitter.event()
                    .name("unread_count_change")
                    .id(UUID.randomUUID().toString())
                    .data(event));
            } catch (Exception e) {
                removeEmitter(userId, emitter);
            }
        }
    }

    @Override
    public void sendPing(Long userId) {
        List<SseEmitter> emitters = userEmitters.getOrDefault(userId, new CopyOnWriteArrayList<>());
        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(SseEmitter.event()
                    .name("ping")
                    .data("{\"timestamp\":\"" + Instant.now() + "\"}"));
            } catch (Exception e) {
                removeEmitter(userId, emitter);
            }
        }
    }

    @Override
    public void sendPingToAll() {
        userEmitters.forEach((userId, emitters) -> {
            for (SseEmitter emitter : emitters) {
                try {
                    emitter.send(SseEmitter.event()
                        .name("ping")
                        .data("{\"timestamp\":\"" + Instant.now() + "\"}"));
                } catch (Exception e) {
                    removeEmitter(userId, emitter);
                }
            }
        });
    }
}
```

> **为什么选择 `CopyOnWriteArrayList`**：遍历推送时可能同时发生连接注册/注销，`CopyOnWriteArrayList` 保证遍历安全，避免 `ConcurrentModificationException`。风险提醒推送频率极低（一天两次），写操作开销可忽略。

---

## 4. 前端接口封装

### 4.1 `riskAlertSSE.ts`

```typescript
const SSE_URL = '/api/risk-alerts/stream'

export interface SSEEvent<T = unknown> {
  eventType: string
  payload: T
  timestamp: string
}

export interface RiskAlertDetail {
  id: number
  changePercent: number
  currentPrice: number
  triggeredAt: string
  triggerReason: string
}

export interface NewAlertEvent {
  symbol: string
  symbolName: string
  symbolType: 'STOCK' | 'FUND'
  date: string
  latestChangePercent: number
  maxChangePercent: number      // 当日最大涨跌幅
  currentPrice: number
  yesterdayClose: number
  latestTriggeredAt: string
  triggerCount: number
  isRead: boolean
  details: RiskAlertDetail[]    // 明细列表，新建时为空数组
}

export interface UnreadCountEvent {
  unreadCount: number
  delta: number
  reason: string
}

class RiskAlertSSEClient {
  private eventSource: EventSource | null = null
  private reconnectTimer: ReturnType<typeof setTimeout> | null = null
  private url: string

  constructor(userId: number) {
    this.url = `${SSE_URL}?userId=${userId}`
  }

  connect(onMessage: (event: SSEEvent) => void) {
    if (this.eventSource) {
      this.disconnect()
    }

    this.eventSource = new EventSource(this.url)

    this.eventSource.onmessage = (e) => {
      // SSE 标准 message，本方案主要使用命名事件
      try {
        const payload = JSON.parse(e.data)
        onMessage({ eventType: 'message', payload, timestamp: new Date().toISOString() })
      } catch {
        // ignore
      }
    }

    this.eventSource.addEventListener('init', (e: MessageEvent) => {
      onMessage({ eventType: 'init', payload: JSON.parse(e.data), timestamp: new Date().toISOString() })
    })

    this.eventSource.addEventListener('new_alert', (e: MessageEvent) => {
      onMessage({ eventType: 'new_alert', payload: JSON.parse(e.data), timestamp: new Date().toISOString() })
    })

    this.eventSource.addEventListener('unread_count_change', (e: MessageEvent) => {
      onMessage({ eventType: 'unread_count_change', payload: JSON.parse(e.data), timestamp: new Date().toISOString() })
    })

    this.eventSource.addEventListener('ping', () => {
      // 心跳，无需处理
    })

    this.eventSource.onerror = () => {
      // 连接出错，延迟重连
      this.eventSource?.close()
      this.scheduleReconnect(onMessage)
    }

    this.eventSource.onopen = () => {
      if (this.reconnectTimer) {
        clearTimeout(this.reconnectTimer)
        this.reconnectTimer = null
      }
    }
  }

  private scheduleReconnect(onMessage: (event: SSEEvent) => void) {
    this.reconnectTimer = setTimeout(() => {
      this.connect(onMessage)
    }, 3000)
  }

  disconnect() {
    if (this.reconnectTimer) {
      clearTimeout(this.reconnectTimer)
      this.reconnectTimer = null
    }
    this.eventSource?.close()
    this.eventSource = null
  }
}

export const createRiskAlertSSEClient = (userId: number) => new RiskAlertSSEClient(userId)
```

### 4.2 `useRiskAlertSSE.ts`

```typescript
import { useEffect, useRef } from 'react'
import { useAppDispatch } from '@store/hooks'
import { receiveRealtimeAlert, updateUnreadCount } from '@store/slices/riskAlertsSlice'
import { createRiskAlertSSEClient, SSEEvent } from '@services/sse/riskAlertSSE'

export function useRiskAlertSSE(userId: number) {
  const dispatch = useAppDispatch()
  const clientRef = useRef<ReturnType<typeof createRiskAlertSSEClient> | null>(null)

  useEffect(() => {
    const client = createRiskAlertSSEClient(userId)
    clientRef.current = client

    client.connect((event: SSEEvent) => {
      switch (event.eventType) {
        case 'init':
          dispatch(updateUnreadCount(event.payload.unreadCount))
          break
        case 'new_alert':
          dispatch(receiveRealtimeAlert(event.payload))
          break
        case 'unread_count_change':
          dispatch(updateUnreadCount(event.payload.unreadCount))
          break
      }
    })

    return () => {
      client.disconnect()
      clientRef.current = null
    }
  }, [userId, dispatch])
}
```

### 4.3 Redux Slice 新增 Action

```typescript
// riskAlertsSlice.ts 新增 reducer

// 接收实时推送的新提醒
receiveRealtimeAlert: (state, action: PayloadAction<RiskAlert>) => {
  const newAlert = action.payload
  const alertId = `${newAlert.symbol}_${newAlert.date}`

  // 避免重复
  const exists = state.list.some((a) => `${a.symbol}_${a.date}` === alertId)
  if (!exists) {
    state.list = [newAlert, ...state.list]
    state.unreadCount += 1
  }
},

// 直接更新未读数量
updateUnreadCount: (state, action: PayloadAction<number>) => {
  state.unreadCount = action.payload
}
```

---

## 5. 心跳任务配置（Spring Scheduler）

### 5.1 `SSEHeartbeatScheduler`

```java
@Component
@Slf4j
public class SSEHeartbeatScheduler {

    @Autowired
    private RiskAlertPushService pushService;

    /**
     * 每 30 秒发送一次全局心跳 ping（对应 AC-1-2）
     * 防止 Nginx/防火墙切断空闲 SSE 连接
     */
    @Scheduled(fixedRate = 30000)
    public void sendGlobalPing() {
        pushService.sendPingToAll();
    }
}
```

> **为什么用全局心跳**：SSE 连接在交易时段外可能长时间无数据推送（如夜间、周末），心跳确保连接不被中间件切断。

---

## 附录 A：SSE 连接数上限与注意事项

### A.1 各层级限制

| 层级 | 默认限制 | 说明 |
|------|----------|------|
| **Tomcat 线程池** | 200 | `server.tomcat.threads.max`，每个 SSE 连接占用一个线程 |
| **Tomcat 连接数** | 8192 | `server.tomcat.max-connections`，Tomcat NIO 模式下的总连接上限 |
| **Linux 文件描述符** | 1024 | `ulimit -n`，每个连接消耗一个 fd |
| **Nginx worker** | 512 / 1024 | `worker_connections`，需根据并发量调整 |
| **浏览器同源** | 6 | HTTP/1.1 下浏览器对同一域名的并发连接数限制，但 SSE 是**长连接复用**，不额外消耗 |

### A.2 针对本系统的容量估算

当前系统特征：

- 用户量：目前为单用户/少量内部用户（从代码中 `localStorage.getItem('userId') || 1` 推断）。
- 单个用户最大连接数：假设用户打开 10 个标签页 + 2 台设备 = **12 条连接**。
- 即使扩展到 100 个用户，总连接数约为 `100 × 12 = 1200`，远低于 Tomcat 默认上限。

**结论**：在当前规模下，默认配置即可支撑，无需特别扩容。

### A.3 生产环境调优建议

若未来用户量增长到千级/万级，需调整以下配置：

#### 1. Tomcat 线程池（`application.yml`）

```yaml
server:
  tomcat:
    threads:
      max: 1000          # 最大工作线程数（默认 200）
    max-connections: 10000  # 最大连接数（默认 8192）
    accept-count: 1000   # 等待队列长度
```

> **注意**：Tomcat 9+ 使用 NIO/NIO2，线程数 != 连接数。一个线程可管理多个连接，但 SSE 的 `SseEmitter` 会阻塞线程直到连接关闭，因此 SSE 连接数约等于活跃线程数。

#### 2. Linux 文件描述符

```bash
# 查看当前限制
ulimit -n

# 临时修改
ulimit -n 65535

# 永久修改 /etc/security/limits.conf
* soft nofile 65535
* hard nofile 65535
```

#### 3. Nginx 配置

```nginx
# /etc/nginx/nginx.conf
events {
    worker_connections 4096;  # 默认 512/1024
    use epoll;
    multi_accept on;
}

# 针对 SSE 的 location
location /api/risk-alerts/stream {
    proxy_pass http://backend;
    proxy_http_version 1.1;
    proxy_set_header Connection '';
    proxy_buffering off;
    proxy_cache off;
    proxy_read_timeout 3600s;
    proxy_send_timeout 3600s;
}
```

#### 4. JVM 内存

每个 `SseEmitter` 实例占用内存较小（约 1-5KB），但 10,000 条连接累计约 50MB。一般无需特别调整堆内存，按常规 `-Xms1g -Xmx2g` 即可。

### A.4 单用户连接数保护

为防止极端情况（用户无限刷新导致连接泄漏），建议在 `register` 方法中增加**单用户连接数上限**：

```java
private static final int MAX_EMITTERS_PER_USER = 3;

@Override
public void register(Long userId, SseEmitter emitter) {
    CopyOnWriteArrayList<SseEmitter> emitters = userEmitters.computeIfAbsent(
        userId, k -> new CopyOnWriteArrayList<>()
    );

    // 保护：单用户连接数上限（限制为 3，防止标签页无限刷新）
    if (emitters.size() >= MAX_EMITTERS_PER_USER) {
        // 移除最早建立的连接（FIFO）
        SseEmitter oldest = emitters.remove(0);
        oldest.complete();
        log.warn("用户 {} 连接数超限，关闭最早连接", userId);
    }

    emitters.add(emitter);
    // ... 注册回调
}
```

### A.5 连接泄漏防护

即使前端正常关闭，也可能因网络异常导致后端无法感知连接断开。建议增加**定时清理任务**：

```java
@Scheduled(fixedRate = 60000) // 每分钟执行
public void cleanupDeadEmitters() {
    userEmitters.forEach((userId, emitters) -> {
        // 主动发送 ping，发送失败的连接会被 onError 回调自动清理
        sendPing(userId);
    });
}
```

### A.6 浏览器 HTTP/1.1 连接限制与影响

#### 限制详情

| 项目 | 限制 |
|------|------|
| 同源最大并发连接数 | **6**（Chrome / Firefox / Edge 的 HTTP/1.1 默认值） |
| 跨域支持 | 支持，但需服务端配置 CORS |
| 自动重连 | 默认开启，断开后 3 秒左右自动重连 |
| 手动关闭 | 调用 `eventSource.close()` 后不再重连 |

#### 核心问题：SSE 会占用 AJAX 的配额吗？

**会的，但影响有限。**

浏览器对**同一域名**（协议 + 主机 + 端口）的并发连接总数限制为 6 条。SSE 是长期保持的 HTTP 连接，会占用其中 1 个槽位。这意味着：

- 如果用户在**单个标签页**中保持 1 条 SSE 连接，同时发起普通 API 请求（如 `GET /api/risk-alerts/user/1`），API 请求是短暂的（请求-响应后连接立即释放），不会与 SSE 产生冲突。
- 如果用户打开**多个标签页**（如 6 个），每个标签页各建立 1 条 SSE 连接，**6 个槽位全部被 SSE 占满**。此时第 7 个标签页中的任何请求（包括新的 API 请求或第 7 条 SSE 连接）会被浏览器**排队阻塞**，直到前面的某个连接释放。

#### 关键澄清：这是客户端限制，不是服务端限制

**HTTP/1.1 的 6 连接限制是浏览器（客户端）的限制，不是服务端（Tomcat/Nginx）的限制。**

- **客户端限制**：Chrome/Firefox/Edge 等浏览器为了遵循 HTTP/1.1 规范，对**单个浏览器实例**向**同一域名**的并发 TCP 连接数限制为 6 条。这是浏览器内部的连接池策略。
- **服务端限制**：Tomcat `max-connections`（默认 8192）、Nginx `worker_connections`（默认 512/1024）是服务端能接受的连接总数，可以支撑数万用户同时在线。

**这意味着**：
- 用户 A 的浏览器 → 最多 6 条并发连接到 `example.com`
- 用户 B 的浏览器 → 最多 6 条并发连接到 `example.com`
- 服务端的总连接数 = 用户 A 的连接 + 用户 B 的连接 + ...（不受 6 限制）

**因此，多个用户同时打开页面不会导致服务器推送问题。** 6 连接限制只影响单个用户在自己的浏览器内同时打开多个标签页的场景。

#### 本系统的实际风险评估

| 场景 | 并发连接数（单个浏览器内） | 是否安全 |
|------|---------------------------|----------|
| 单标签页 | 1 SSE + 1~2 短暂 API | ✅ 安全 |
| 3 标签页（我们的上限） | 3 SSE + 1~2 短暂 API | ✅ 安全，总并发通常 ≤ 5 |
| 6+ 标签页 | 6 SSE + API 请求 | ⚠️ 第 7 个请求会被浏览器排队 |

**结论**：由于我们已将单用户 SSE 连接上限限制为 **3**，在正常使用场景下（3 个标签页以内），HTTP/1.1 的 6 连接限制**不会导致 API 请求被阻塞**。

#### 彻底消除限制的三种方案

若未来需要支持更多标签页或更高并发，可采用以下方案：

**方案 1：启用 HTTP/2（推荐）**

HTTP/2 支持**多路复用（Multiplexing）**，单个 TCP 连接内可同时承载无限数量的请求/响应流，彻底摆脱浏览器 6 连接限制。

```nginx
# Nginx 开启 HTTP/2
server {
    listen 443 ssl http2;
    # ...
}
```

Spring Boot 内嵌 Tomcat 9+ 默认支持 HTTP/2，只需确保 Nginx/LoadBalancer 前端开启 `http2` 即可。这是**最优雅、改动最小**的解决方案。

**方案 2：SSE 与 API 使用不同子域名**

将 SSE 端点独立到子域名，浏览器视为不同 Origin，各自拥有 6 个槽位：

```
API 请求：  https://api.example.com/api/risk-alerts/user/1
SSE 连接：  https://sse.example.com/api/risk-alerts/stream
```

**方案 3：使用 SharedWorker + BroadcastChannel**

通过一个 SharedWorker 建立唯一 SSE 连接，多个标签页通过 `BroadcastChannel` 共享推送消息。此方案复杂度高，仅当方案 1 和 2 不可行时考虑。

#### 生产环境建议

- **短期**：保持单用户 SSE 上限 3，配合 HTTP/1.1 完全够用。
- **中长期**：在 Nginx/LoadBalancer 层开启 **HTTP/2**，一劳永逸解决连接限制问题。

### A.7 总结 checklist

部署 SSE 推送前检查：

- [ ] Tomcat 线程池 `max-threads` >= 预期并发连接数
- [ ] Linux `ulimit -n` >= 预期并发连接数 × 1.5
- [ ] Nginx `worker_connections` >= 预期并发连接数
- [ ] Nginx 针对 SSE location 关闭 `proxy_buffering`
- [ ] 代码中实现了 `onCompletion` / `onTimeout` / `onError` 自动清理
- [ ] 单用户连接数有上限保护（如 3 条）
- [ ] 有心跳或定时清理机制防止连接泄漏

---

## 5. 错误处理

### 5.1 服务端错误

| 场景 | HTTP 状态 | 行为 |
|------|-----------|------|
| 未携带 `userId` | `400 Bad Request` | 返回 JSON 错误，不建立 SSE |
| `userId` 无效 | `401 Unauthorized` | 返回 JSON 错误，前端跳转登录 |
| 服务端异常 | `500 Internal Server Error` | 关闭连接，前端 3 秒后重连 |
| 连接超时 | — | 正常关闭，EventSource 自动重连 |

### 5.2 客户端错误

| 场景 | 处理 |
|------|------|
| 网络中断 | EventSource 自动触发 `onerror`，3 秒后重连 |
| 服务端返回非 200 | 关闭当前连接，3 秒后重连 |
| JSON 解析失败 | 丢弃该条消息，记录 warn，不影响后续接收 |

---

## 6. Nginx 反向代理配置（如适用）

若前端通过 Nginx 代理到后端，需添加以下配置以支持 SSE：

```nginx
location /api/risk-alerts/stream {
    proxy_pass http://backend;
    proxy_http_version 1.1;
    proxy_set_header Connection '';
    proxy_buffering off;
    proxy_cache off;
    proxy_read_timeout 3600s;
    proxy_send_timeout 3600s;
}
```

---

## 7. 扩展预留：Kafka 多实例广播

> ⚠️ **当前未实现**：以下为多实例部署时的扩展方案，当前系统为单机部署，无需此改动。

当系统从单机扩展到多实例时，推送架构演进如下：

```
┌─────────────────┐     ┌─────────────────┐
│   Instance A    │     │   Instance B    │
│ ┌─────────────┐ │     │ ┌─────────────┐ │
│ │ User-1 SSE  │ │     │ │ User-2 SSE  │ │
│ │ User-3 SSE  │ │     │ │ User-4 SSE  │ │
│ └─────────────┘ │     │ └─────────────┘ │
│        ▲        │     │        ▲        │
│        │        │     │        │        │
│   Kafka Consumer │     │   Kafka Consumer │
│        ▲        │     │        ▲        │
└────────┼────────┘     └────────┼────────┘
         │                       │
         └───────────┬───────────┘
                     ▼
              ┌─────────────┐
              │ Kafka Topic │
              │ risk-alert- │
              │   created   │
              └──────▲──────┘
                     │
              ┌──────┴──────┐
              │  Producer   │
              │  (任意实例)  │
              └─────────────┘
```

**改动点**：

1. `RiskAlertAppServiceImpl` 创建风险提醒后，发送 Kafka 消息：

```java
kafkaTemplate.send("risk-alert-created", new RiskAlertCreatedMessage(userId, payload));
```

2. 各实例消费消息，仅当 `userId` 连接存在于本实例时才推送：

```java
@KafkaListener(topics = "risk-alert-created")
public void onRiskAlertCreated(RiskAlertCreatedMessage message) {
    pushService.pushNewAlert(message.getUserId(), message.getPayload());
}
```

3. `InMemoryRiskAlertPushService` 无需修改，天然适配。
