# AGENTS.md - Architect Mode

This file provides architectural constraints, design decisions, and performance/scalability guidance for this repository.

## Technology Constraints

- **MyBatis-Plus over JPA**: Explicitly chosen for query flexibility; no JPA usage permitted
- **PostgreSQL**: Primary database; tests run against real PostgreSQL (not H2)
- **SSL/HTTP/2**: Backend requires SSL with HTTP/2 enabled on port 9090

## Caching Strategy

- **4 Caffeine caches**: `generalCache`, `stockCache`, `fundCache`, `apiCache`
- **Configuration**: TTL 3600s, maxSize 10000
- **SSE Connection Cache**: `InMemoryRiskAlertPushService` uses Caffeine to track connections (100k users capacity, 30min idle timeout per user)

## SSE Architecture

- **Connection Management**: `InMemoryRiskAlertPushService` with manual lifecycle (emitter timeout=0)
- **Scalability Limit**: Max 5 connections per user with FIFO eviction when exceeded
- **Heartbeat**: `SSEHeartbeatScheduler` pings every 30s to keep connections alive

## Scheduling Architecture

- **DataCollectionScheduler**: Market data ingestion — basics at 2AM/3AM, quotes at intervals during trading hours
- **RiskAlertScheduler**: Business-hours-only evaluation (11:30 and 14:30 on working days) to align with market timing
- **Test Isolation**: All scheduler crons set to non-executing dates in tests to prevent flakiness

## Data Layer Decisions

- **Batch Operations**: `BATCH_SIZE=1000` with `@Transactional` and `insertBatchSomeColumn()` for performance
- **Pagination**: `PageResponse` uses `records` field for consistency across the API
- **Soft Deletes**: `deleted` field (1=deleted, 0=active) — standard MyBatis-Plus approach, no custom override

## Frontend Architecture

- **State Management**: Redux Toolkit for predictable client state
- **API Patterns**: Two `ApiResponse` interfaces exist historically; new code should follow the `client.ts` pattern (`error` field)
- **Build**: Vite with known `manualChunks` dead code — do not attempt to fix without architectural review
