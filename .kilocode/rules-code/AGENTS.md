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

## Testing Rules

- 测试用例必须覆盖三类场景：前端格式校验（无效格式）、后端业务校验（格式正确但业务无效）、成功路径。禁止只测试成功路径。
- 测试数据禁止硬编码猜测值（如 `000001`）。必须通过真实 API 查询获取有效数据，或在 `beforeAll` 中通过 API 动态准备。
- Playwright 选择器优先使用 `data-testid`，禁止基于按钮文本的模糊匹配（`getByText`）。
- **逐个执行原则**：测试必须逐个运行（`--grep` 单条），遇到错误时立即终止。必须先解决当前测试用例的问题并使其通过，才能执行下一个测试用例。禁止批量运行后统一处理失败。

## Frontend State Rules

- 按钮的 `disabled` 属性**禁止**与异步 API 返回结果绑定。禁用条件只能是本地可验证的规则（如格式校验、必填校验、长度校验）。
- 后端业务校验结果必须通过错误提示（`message.error` / `toast` / 内联错误文案）展示给用户，而不是通过禁用按钮阻止用户操作。
- 异步查询失败后，UI 不得进入"死状态"（无提示且无法操作）。必须允许用户修改输入后重试。

## Acceptance Rules

- 自动化测试全部通过 ≠ 验收完成。必须额外执行边界条件检查：输入无效格式、输入业务无效数据、重复提交、网络超时场景。
- 验收时必须做至少 5 分钟的探索性测试：故意输入预期外的数据，确认后端返回错误时前端 UI 仍然可用。
- PRD 中未明确写出的边界条件，开发者和测试者必须主动提出并验证，不能默认"不需要处理"。

## Debugging Rules

- 修复测试失败时，必须先读取源码确认真实 UI 文本和 DOM 结构，禁止猜测按钮名称或文案。
- 后端日志中出现错误时，必须分析该错误是否会导致前端进入不可操作状态。
