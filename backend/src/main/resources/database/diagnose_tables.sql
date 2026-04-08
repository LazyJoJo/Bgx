-- 数据库表结构诊断脚本
-- 用于检查现有表结构和字段

-- 1. 检查所有相关表是否存在
SELECT 
    table_name,
    table_type,
    table_schema
FROM information_schema.tables 
WHERE table_schema = 'public' 
AND table_name IN ('stock_basic', 'fund_basic', 'stock_quote', 'fund_quote', 'price_alert', 'alert_history')
ORDER BY table_name;

-- 2. 检查stock_quote表的字段结构
SELECT 
    column_name,
    data_type,
    is_nullable,
    column_default
FROM information_schema.columns 
WHERE table_name = 'stock_quote' 
AND table_schema = 'public'
ORDER BY ordinal_position;

-- 3. 检查fund_quote表的字段结构
SELECT 
    column_name,
    data_type,
    is_nullable,
    column_default
FROM information_schema.columns 
WHERE table_name = 'fund_quote' 
AND table_schema = 'public'
ORDER BY ordinal_position;

-- 4. 检查约束信息
SELECT 
    tc.table_name,
    tc.constraint_name,
    tc.constraint_type,
    kcu.column_name
FROM information_schema.table_constraints tc
LEFT JOIN information_schema.key_column_usage kcu 
    ON tc.constraint_name = kcu.constraint_name
    AND tc.table_schema = kcu.table_schema
WHERE tc.table_schema = 'public'
AND tc.table_name IN ('stock_quote', 'fund_quote')
ORDER BY tc.table_name, tc.constraint_name;

-- 5. 检查索引信息
SELECT 
    indexname,
    tablename,
    indexdef
FROM pg_indexes 
WHERE tablename IN ('stock_quote', 'fund_quote')
AND schemaname = 'public'
ORDER BY tablename, indexname;

-- 6. 检查表中的数据量
SELECT 'stock_quote' as table_name, COUNT(*) as row_count FROM stock_quote
UNION ALL
SELECT 'fund_quote' as table_name, COUNT(*) as row_count FROM fund_quote
UNION ALL
SELECT 'stock_basic' as table_name, COUNT(*) as row_count FROM stock_basic
UNION ALL
SELECT 'fund_basic' as table_name, COUNT(*) as row_count FROM fund_basic;