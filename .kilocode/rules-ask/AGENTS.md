# AGENTS.md - Ask Mode

This file provides architectural context and reference material for agents explaining or analyzing how this repository works.

## System Architecture

- **Fullstack**: React/TypeScript frontend (`app/`) + Spring Boot backend (`backend/`)
- **State Management**: Redux Toolkit handles client-side state
- **Data Access**: CQRS pattern — `RiskAlertQueryService` handles complex reads separately from command operations (`RiskAlertAppService`)
- **Persistence**: MyBatis-Plus with PostgreSQL; soft deletes via `deleted` field (1=deleted, 0=active)

## Frontend Design

- **UI Library**: Ant Design + dayjs, configured for `zh_CN` locale
- **Real-time Updates**: SSE client with exponential backoff (1s→30s) and jitter; deduplication uses `symbol_date` composite key rather than `id`
- **Testing**: Playwright E2E tests with `ignoreHTTPSErrors: true` for self-signed certificates

## Backend Design

- **Real-time Push**: SSE managed by `InMemoryRiskAlertPushService`; supports 100k users capacity with 30min idle timeout per user
- **Caching**: 4 Caffeine caches (`generalCache`, `stockCache`, `fundCache`, `apiCache`) with TTL 3600s and maxSize 10000
- **Risk Alert Lifecycle**: Status flows `NO_ALERT → ACTIVE → CLEARED`

## Scheduling

- **DataCollectionScheduler**: Market data collection (Stock Basic 2AM, Fund Basic 3AM, quote intervals during trading hours)
- **RiskAlertScheduler**: Evaluation only at 11:30 and 14:30 on working days
- **Test Safety**: All scheduler crons set to non-executing dates in tests to prevent side effects
