# AGENTS.md - Debug Mode

This file provides debugging workflows, log locations, and diagnostic guidance for agents troubleshooting issues in this repository.

## Diagnostic Entry Points

- **Backend logs**: Check Spring Boot application logs for `RiskAlertAppServiceImpl`, `InMemoryRiskAlertPushService`, and scheduler execution traces
- **Frontend console**: SSE connection errors appear in browser console; check the Network tab for `/api/risk-alerts/stream` requests
- **Database**: Verify PostgreSQL connection and query results directly when data appears missing

## Common Debug Workflows

### SSE Connection Issues
1. Confirm `userId` is passed as a **query param**, not a header — the endpoint is `/api/risk-alerts/stream?userId={id}`
2. Check that the emitter timeout is set to `0` (manual lifecycle management)
3. Verify the max 5 connections per user limit — FIFO eviction occurs when exceeded
4. Confirm `SSEHeartbeatScheduler` is running (pings every 30s)

### Risk Alerts Not Triggering
1. Verify `RiskAlertScheduler` runs **only at 11:30 and 14:30 on working days** — it will not fire outside these windows
2. Check `DataCollectionScheduler` is populating market data (Stock Basic 2AM, Fund Basic 3AM, quotes during trading hours)
3. Confirm risk alert status flow: `NO_ALERT → ACTIVE → CLEARED`

### Batch Insert Failures
1. Verify the method is annotated with `@Transactional`
2. Confirm `insertBatchSomeColumn()` is used with `BATCH_SIZE=1000`
3. Check `deleted` field logic — `deleted=1` means deleted, `0` means active; ensure queries account for this

### ApiResponse Mismatch
1. Check which interface the endpoint actually returns — `client.ts` uses `error` field, `types/index.ts` uses `message` field
2. Prefer the `client.ts` pattern when writing new API calls

### Cache Issues
1. Check the 4 Caffeine caches: `generalCache`, `stockCache`, `fundCache`, `apiCache`
2. TTL is 3600s, maxSize 10000 — verify eviction isn't causing missing data
