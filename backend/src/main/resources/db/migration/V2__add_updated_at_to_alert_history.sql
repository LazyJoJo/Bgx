-- 为 alert_history 表添加 updated_at 字段
ALTER TABLE alert_history ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP WITHOUT TIME ZONE;

-- 设置默认值为 created_at
UPDATE alert_history SET updated_at = created_at WHERE updated_at IS NULL;
