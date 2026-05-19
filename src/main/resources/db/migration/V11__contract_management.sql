-- B26: 合同管理模块 — 扩展 contracts 表
ALTER TABLE contracts
    ADD COLUMN title            VARCHAR(200)   NULL     COMMENT '合同标题' AFTER no,
    ADD COLUMN type             VARCHAR(32)    NULL     COMMENT 'LEASE|PROPERTY_SERVICE|PARKING|OTHER' AFTER title,
    ADD COLUMN tenant_name      VARCHAR(64)    NULL     COMMENT '租户名称' AFTER tenant_id,
    ADD COLUMN amount           DECIMAL(14, 2) NULL     COMMENT '合同金额' AFTER end_date,
    ADD COLUMN reject_reason    VARCHAR(500)   NULL     COMMENT '驳回原因' AFTER status,
    ADD COLUMN terminate_reason VARCHAR(500)   NULL     COMMENT '终止原因' AFTER reject_reason,
    ADD COLUMN remark           VARCHAR(500)   NULL     COMMENT '备注' AFTER terminate_reason;

-- 更新 status 枚举注释
ALTER TABLE contracts
    MODIFY COLUMN status VARCHAR(20) NOT NULL DEFAULT 'DRAFT'
        COMMENT 'DRAFT|PENDING_APPROVAL|ACTIVE|TERMINATED|EXPIRED';
