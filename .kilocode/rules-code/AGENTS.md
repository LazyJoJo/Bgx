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


