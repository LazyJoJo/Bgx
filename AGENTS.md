# AGENTS.md

Fullstack: React/TypeScript frontend (`app/`) + Spring Boot backend (`backend/`).

## Build & Run Commands

### Frontend
```bash
cd app && npm run dev      # https://127.0.0.1:8080 (proxy to backend:9090)
npm run build             # tsc && vite build
npm run lint              # eslint
npm run test              # vitest
npm run test:run          # vitest run (CI)
```

### Backend
```bash
cd backend
mvn spring-boot:run                          # port 9090, SSL+HTTP/2
mvn test                                       # real PostgreSQL via testcontainers
mvn verify                                     # JaCoCo 80% line coverage enforced
```

### E2E (Playwright)
```bash
cd app && npx playwright test --grep "test name"   # single test, ignoreHTTPSErrors:true
```

## Architecture

- **Backend port**: 9090 (SSL/HTTP2 enabled)
- **Frontend dev**: 8080, self-signed cert (cert.pem/key.pem)
- **SSE**: `/api/risk-alerts/stream?userId={id}` — userId via query param, not header
- **State**: Redux Toolkit + Ant Design + dayjs (zh_CN locale)

## Critical Backend Rules

- **MyBatis-Plus only** — no JPA; use `BaseMapper`, `IService`, `insertBatchSomeColumn()`
- **Soft delete**: `deleted=1` is deleted, `0` is active — don't override with annotations
- **BigDecimal**: always use `compareTo()`, never `==` or `equals()`
- **Batch**: `BATCH_SIZE=1000` + `@Transactional` + `insertBatchSomeColumn()`
- **PageResponse**: list field is `records`, NOT `content` or `data`
- **JaCoCo**: 80% minimum line coverage required

## Critical Frontend Rules

- **ApiResponse**: `client.ts` pattern returns `{success, data, error}` — use this, NOT the `message` variant in `types/index.ts`
- **SSE dedup**: uses `symbol_date` composite key — do NOT refactor to use `id`
- **Auth fallback**: `Number(localStorage.getItem('userId')) || 1` is the fallback pattern — don't remove

## Testing Rules

- Test data: **no hardcoded guesses** (e.g. `000001`). Use API to fetch real data in `beforeAll`.
- Selectors: **prefer `data-testid`** — no fuzzy `getByText` matching.
- Run **one test at a time** (`--grep`). Stop on first failure.
- Coverage: **3 scenarios required** — frontend format validation, backend business validation (valid format but invalid entity), success path. Never only happy path.

## Button State Rule

`disabled` **must not** bind to async API results. Only local-validatable rules (format, required, length). Backend validation errors show via `message.error`/`toast`, not disabled buttons.


