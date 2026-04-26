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

## Test Authoring Rules — MUST FOLLOW

### UI Text Verification — NO GUESSING

When writing tests that interact with UI elements — buttons, Modal titles, form labels, table headers, placeholder text, or any user-visible text — **you MUST read the source component file first** to confirm the exact text. You are strictly forbidden from guessing or assuming text content.

- Examples of text that MUST be verified in source: button labels, `Modal` `title`/`okText`/`cancelText`, `Form.Item` `label`, `Input` `placeholder`, `message.success/error` strings, column `title` in `Table`
- The project uses `zh_CN` locale — UI text is predominantly Chinese. Do NOT assume English labels like "Add", "Edit", "Delete", "Save", "Cancel"
- After verifying source text, use **exact match** or **regex bounded to the verified substring** in selectors

### Selector Priority

1. **First priority**: `data-testid` attributes — add them to components if they don't exist
2. **Second priority**: `getByRole` with exact `name` that you have verified from source
3. **Forbidden**: fuzzy `getByText` with guessed text, unbounded regex, or text content you have not read from source

### Test Data — NO HARDCODED GUESSES

- **Never** hardcode business entity identifiers (e.g., fund codes like `000001`, stock codes, user IDs) based on assumption
- When real data is required, either:
  - Query the API/database in `beforeAll`/`setUp` to obtain valid identifiers, OR
  - Ask the user to provide a valid test data set
- Mock data in unit tests is allowed **only** for pure UI behavior tests where the exact value has no business meaning

### Button State Rule

`disabled` **must not** bind to async API results. Only local-validatable rules (format, required, length). Backend validation errors show via `message.error`/`toast`, not disabled buttons.

## Test Execution & Debugging Rules — MUST FOLLOW

### Stop on First Failure — NO EXCEPTIONS

When running tests, **you MUST stop immediately upon the first failure**. Do not continue running the remaining test suite.

- Frontend unit tests: use `--grep "test name"` to run a single test, or set `bail: 1` in vitest config
- Backend tests: use `-Dtest=ClassName#methodName` to run a single test method
- E2E tests: use `npx playwright test --grep "test name"` for single test execution
- **Rationale**: Continuing after a failure produces cascading false negatives, wastes time, and obscures the root cause

### No Blind Re-runs

You are strictly forbidden from re-running the entire test suite without first analyzing and fixing the first failure.

- Required workflow: run → first failure → **stop** → analyze logs/screenshots → identify root cause → fix → re-run **only the failed test** → pass → proceed to next test
- If a test fails twice with the same error, the issue is deterministic — do NOT retry again; debug it

### Test Failure Analysis Order

When a test fails, analyze in this sequence:

1. **Selector/text mismatch**: Check whether the test's selector text matches the actual source component text. If you wrote the test, verify you read the source file first
2. **Hardcoded data invalid**: Check if the test uses guessed hardcodes (fund codes, stock codes). Replace with API-fetched real data or ask the user
3. **Application bug**: Only after eliminating 1 and 2, consider that the application code itself is buggy

### Debug Logging

All debug log output MUST be in English, per project-wide logging rules.

