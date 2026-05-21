-- ============================================================
-- V12__index_optimization.sql  B9 索引优化
-- 补充 StatisticsMapper 聚合查询 + 常用列表查询缺失的索引
-- ============================================================

-- 工单：按 created_at 月度筛选（统计用），覆盖 status + deleted
ALTER TABLE work_orders ADD KEY idx_created_at (created_at);

-- 投诉：按 created_at 月度筛选（统计用）
ALTER TABLE complaints ADD KEY idx_created_at (created_at);

-- 公告：按 created_at 月度筛选（统计用）
ALTER TABLE announcements ADD KEY idx_created_at (created_at);

-- 账单：按 project_id 筛选（多项目隔离场景）
ALTER TABLE bill_items ADD KEY idx_fee_item (fee_item_id);

-- 通知：按 user_id + created_at 排序查询
ALTER TABLE notifications ADD KEY idx_user_created (user_id, created_at);

-- 支付流水：按 tenant_id 查询
ALTER TABLE payment_records ADD KEY idx_tenant (tenant_id);
