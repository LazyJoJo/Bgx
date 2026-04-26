# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Fullstack application: React/TypeScript frontend (`app/`) + Spring Boot backend (`backend/`). The system collects and manages stock/fund data, with risk alert monitoring capabilities.

## Build & Run Commands

### Frontend (`app/`)
```bash
npm run dev          # Vite dev server on https://127.0.0.1:8080 (proxies to backend:9090)
npm run build        # TypeScript check + Vite build
npm run lint         # ESLint
npm run test         # vitest (watch mode)
npm run test:run     # vitest run (CI mode)
npm run test:coverage # vitest with coverage
```

### Backend (`backend/`)
```bash
mvn spring-boot:run   # Runs on port 9090 (SSL/HTTP2 enabled)
mvn test              # Integration tests with real PostgreSQL via testcontainers
mvn verify            # JaCoCo 80% line coverage enforced
```

### E2E (Playwright)
```bash
cd app && npx playwright test --grep "test name"   # Single test, ignores HTTPS errors
```

## Architecture

### Backend (Spring Boot 3.1.5, Java 21)
- **Port**: 9090 with SSL and HTTP/2 enabled
- **Database**: PostgreSQL with MyBatis-Plus (NOT JPA)
- **Key packages**:
  - `application/` - Application services, schedulers, commands, queries
  - `domain/` - Domain entities
  - `infrastructure/` - Mappers, external API clients (Tushare, Sina, EastMoney)
  - `interfaces/` - Controllers, DTOs, advice
  - `config/` - Spring configuration
  - `aspect/` - AOP aspects
- **Cache**: Caffeine (local in-memory cache)
- **External APIs**: Tushare, Sina, EastMoney for stock/fund data
- **Schedulers**: Daily data collection at 2AM/3AM, quote collection every 1-15 minutes during market hours, risk alert checks

### Frontend (React 18, TypeScript, Vite)
- **Dev server**: 8080 with self-signed certificate (cert.pem/key.pem)
- **State**: Redux Toolkit
- **UI**: Ant Design components
- **Date handling**: dayjs with zh_CN locale
- **Charts**: ECharts
- **API**: Axios with `/api` base URL proxied to backend

### API Communication
- Backend serves SSE stream at `/api/risk-alerts/stream?userId={id}` (userId via query param, NOT header)
- Frontend uses `ApiResponse<T>` pattern from `services/api/client.ts`: `{success, data?, error?}`

## Critical Backend Rules

- **MyBatis-Plus only** — no JPA; use `BaseMapper`, `IService`, `insertBatchSomeColumn()`
- **Soft delete**: `deleted=1` is deleted, `0` is active — don't override with `@TableLogic`
- **BigDecimal**: always use `compareTo()`, never `==` or `equals()`
- **Batch operations**: `BATCH_SIZE=1000` + `@Transactional` + `insertBatchSomeColumn()`
- **PageResponse**: list field is `records`, NOT `content` or `data`
- **JaCoCo**: 80% minimum line coverage required on `mvn verify`

## Critical Frontend Rules

- **ApiResponse**: Use `client.ts` pattern `{success, data, error}` — do NOT use the `message` variant in `types/index.ts`
- **SSE dedup**: Uses `symbol_date` composite key — do NOT refactor to use `id`
- **Auth fallback**: `Number(localStorage.getItem('userId')) || 1` is the fallback pattern — don't remove
- **Button disabled state**: Must not bind to async API results. Only local-validatable rules (format, required, length). Backend errors show via `message.error`/`toast`, not disabled buttons.

## Testing Rules

- **Test data**: No hardcoded guesses (e.g., stock code `000001`). Use API to fetch real data in `beforeAll`.
- **Selectors**: Prefer `data-testid` — no fuzzy `getByText` matching.
- **Run one test at a time**: Use `--grep`. Stop on first failure.
- **Coverage scenarios**: 3 required — frontend format validation, backend business validation (valid format but invalid entity), success path. Never only happy path.

## Database Schema Notes

- Soft delete is field-based (`deleted` column), not annotation-based
- MyBatis-Plus global config handles logic delete value/values
- Mapper XML files in `backend/src/main/resources/mapper/` for complex queries

## Key Files

- `backend/src/main/resources/application.yml` - Main Spring configuration
- `app/src/services/api/client.ts` - API client with interceptors
- `app/src/store/` - Redux store and slices
- `app/src/types/index.ts` - TypeScript type definitions (Stock, Fund, RiskAlert, etc.)
