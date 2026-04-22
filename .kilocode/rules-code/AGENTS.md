# AGENTS.md - Code Mode

This file provides coding conventions, patterns, and implementation rules agents MUST follow when writing or modifying code in this repository.

## Frontend Code Rules

- **ApiResponse inconsistency**: `client.ts` returns `{success, data, error}`, but `types/index.ts` defines `{success, data, message}` — use the client.ts pattern for API calls
- **SSE deduplication**: Uses `symbol_date` composite key in `riskAlertSSE.ts`, NOT `id` — don't refactor to use `id`
- **Auth userId**: Hardcoded fallback `Number(localStorage.getItem('userId')) || 1` exists in multiple places — don't remove, it's the auth fallback pattern
- **Vite manualChunks dead code**: First `return` in `manualChunks` function always exits — splitting react/antd/echarts/redux never executes
- **Locale**: dayjs and Antd are configured for `zh_CN` locale — maintain this in all new UI code
- **Playwright tests**: Use `ignoreHTTPSErrors: true` for self-signed certs in test config

## Backend Code Rules

- **MyBatis-Plus only**: No JPA — use `BaseMapper`, `IService`, `insertBatchSomeColumn()` for batch inserts
- **Logic delete**: `deleted` field (1=deleted, 0=not) — don't override with soft delete annotations
- **BigDecimal**: Always use `compareTo()` for comparisons, never `==` or `equals()`
- **Batch inserts**: `BATCH_SIZE=1000`, must have `@Transactional`, use `insertBatchSomeColumn()`
- **PageResponse**: List field is `records`, NOT `content` or `data`
- **JaCoCo**: Minimum 80% line coverage required
