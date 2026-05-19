-- ============================================================
-- V1__init.sql  智能物业管理系统 · 全库 DDL
-- 作者：property-backend
-- 说明：36 张表，含 2 张 RANGE 分区表（bills / op_logs）
-- ============================================================

SET NAMES utf8mb4;
SET time_zone = '+08:00';

-- ============================================================
-- 资源域：项目 / 楼栋 / 单元
-- ============================================================

CREATE TABLE projects (
    id          VARCHAR(32)  NOT NULL COMMENT '项目 ID',
    name        VARCHAR(64)  NOT NULL COMMENT '项目名称',
    address     VARCHAR(255) NOT NULL COMMENT '项目地址',
    manager_id  VARCHAR(32)           COMMENT '负责人用户 ID',
    status      VARCHAR(16)  NOT NULL DEFAULT 'ACTIVE' COMMENT 'ACTIVE|INACTIVE',
    deleted     TINYINT(1)   NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    created_at  DATETIME(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    updated_at  DATETIME(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    PRIMARY KEY (id),
    KEY idx_status (status)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '项目表';

CREATE TABLE buildings (
    id          VARCHAR(32) NOT NULL COMMENT '楼栋 ID',
    name        VARCHAR(64) NOT NULL COMMENT '楼栋名称',
    floor_count INT         NOT NULL DEFAULT 1 COMMENT '总楼层数',
    project_id  VARCHAR(32) NOT NULL COMMENT '所属项目 ID',
    status      VARCHAR(16) NOT NULL DEFAULT 'ACTIVE' COMMENT 'ACTIVE|INACTIVE',
    deleted     TINYINT(1)  NOT NULL DEFAULT 0,
    created_at  DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    updated_at  DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    PRIMARY KEY (id),
    KEY idx_project (project_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '楼栋表';

CREATE TABLE units (
    id          VARCHAR(32)    NOT NULL COMMENT '单元 ID',
    no          VARCHAR(32)    NOT NULL COMMENT '房号（如 A-101）',
    floor       INT            NOT NULL COMMENT '所在楼层',
    area        DECIMAL(10, 2) NOT NULL COMMENT '建筑面积（㎡）',
    type        VARCHAR(16)    NOT NULL DEFAULT 'APARTMENT' COMMENT 'APARTMENT|OFFICE|SHOP',
    room_count  INT                     DEFAULT 0 COMMENT '居室数',
    status      VARCHAR(16)    NOT NULL DEFAULT 'VACANT' COMMENT 'VACANT|OCCUPIED|RESERVED',
    building_id VARCHAR(32)    NOT NULL COMMENT '所属楼栋 ID',
    project_id  VARCHAR(32)    NOT NULL COMMENT '所属项目 ID',
    deleted     TINYINT(1)     NOT NULL DEFAULT 0,
    created_at  DATETIME(3)    NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    updated_at  DATETIME(3)    NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    PRIMARY KEY (id),
    KEY idx_building (building_id),
    KEY idx_project_status (project_id, status)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '房间/单元表';

-- ============================================================
-- 租户域：租户 / 附件 / 入住历史 / 合同
-- ============================================================

CREATE TABLE tenants (
    id                 VARCHAR(32)  NOT NULL COMMENT '租户 ID',
    type               VARCHAR(16)  NOT NULL COMMENT 'PERSONAL | COMPANY',
    name               VARCHAR(64)  NOT NULL COMMENT '租户姓名/公司名',
    id_card            VARCHAR(32)            COMMENT '身份证号（个人）',
    phone              VARCHAR(16)  NOT NULL  COMMENT '联系手机',
    social_credit_code VARCHAR(32)            COMMENT '统一社会信用代码（公司）',
    contact_name       VARCHAR(32)            COMMENT '联系人姓名（公司）',
    contact_phone      VARCHAR(16)            COMMENT '联系人电话（公司）',
    bank_account       VARCHAR(64)            COMMENT '银行账号（退押金用）',
    status             VARCHAR(16)  NOT NULL  DEFAULT 'ACTIVE' COMMENT 'ACTIVE|INACTIVE|BLACKLIST',
    deleted            TINYINT(1)   NOT NULL  DEFAULT 0,
    created_at         DATETIME(3)  NOT NULL  DEFAULT CURRENT_TIMESTAMP(3),
    updated_at         DATETIME(3)  NOT NULL  DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    PRIMARY KEY (id),
    UNIQUE KEY uk_phone (phone, deleted),
    UNIQUE KEY uk_id_card (id_card, deleted),
    UNIQUE KEY uk_social_credit (social_credit_code, deleted),
    KEY idx_status (status)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '租户表';

CREATE TABLE tenant_attachments (
    id          VARCHAR(32)  NOT NULL COMMENT '附件 ID',
    tenant_id   VARCHAR(32)  NOT NULL COMMENT '所属租户 ID',
    type        VARCHAR(32)  NOT NULL COMMENT 'ID_CARD|LICENSE|CONTRACT_PDF|OTHER',
    name        VARCHAR(255) NOT NULL COMMENT '文件原始名',
    url         VARCHAR(512) NOT NULL COMMENT 'OSS URL',
    size        BIGINT       NOT NULL DEFAULT 0 COMMENT '文件大小（字节）',
    deleted     TINYINT(1)   NOT NULL DEFAULT 0,
    created_at  DATETIME(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    updated_at  DATETIME(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    PRIMARY KEY (id),
    KEY idx_tenant (tenant_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '租户附件表';

CREATE TABLE tenant_units (
    id           VARCHAR(32) NOT NULL COMMENT '历史记录 ID',
    tenant_id    VARCHAR(32) NOT NULL COMMENT '租户 ID',
    unit_id      VARCHAR(32) NOT NULL COMMENT '单元 ID',
    contract_id  VARCHAR(32)          COMMENT '关联合同 ID',
    check_in_at  DATETIME(3) NOT NULL COMMENT '入住时间',
    check_out_at DATETIME(3)          COMMENT '退租时间（NULL=当前在住）',
    created_at   DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    PRIMARY KEY (id),
    KEY idx_tenant (tenant_id),
    KEY idx_unit (unit_id),
    KEY idx_contract (contract_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '租户入住历史表';

CREATE TABLE contracts (
    id           VARCHAR(32)    NOT NULL COMMENT '合同 ID',
    no           VARCHAR(32)    NOT NULL COMMENT '合同编号',
    tenant_id    VARCHAR(32)    NOT NULL COMMENT '租户 ID',
    unit_id      VARCHAR(32)    NOT NULL COMMENT '单元 ID',
    status       VARCHAR(16)    NOT NULL DEFAULT 'DRAFT'
        COMMENT 'DRAFT|PENDING|ACTIVE|EXPIRED|TERMINATED',
    start_date   DATE           NOT NULL COMMENT '合同开始日期',
    end_date     DATE           NOT NULL COMMENT '合同结束日期',
    monthly_rent DECIMAL(12, 2) NOT NULL COMMENT '月租金（元）',
    deposit      DECIMAL(12, 2)          COMMENT '押金（元）',
    pdf_url      VARCHAR(512)            COMMENT '合同 PDF URL',
    signed_at    DATETIME(3)             COMMENT '签署时间',
    terminated_at DATETIME(3)            COMMENT '终止时间',
    deleted      TINYINT(1)     NOT NULL DEFAULT 0,
    created_at   DATETIME(3)    NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    updated_at   DATETIME(3)    NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    PRIMARY KEY (id),
    UNIQUE KEY uk_no (no),
    KEY idx_tenant (tenant_id),
    KEY idx_unit (unit_id),
    KEY idx_status_end_date (status, end_date)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '合同表';

-- ============================================================
-- 费用域：费用项 / 阶梯 / 账单
-- ============================================================

CREATE TABLE fee_items (
    id          VARCHAR(32)    NOT NULL COMMENT '费用项 ID',
    name        VARCHAR(64)    NOT NULL COMMENT '费用项名称',
    type        VARCHAR(16)    NOT NULL COMMENT 'FIXED|BY_AREA|BY_METER|TIERED',
    fixed_amount DECIMAL(12, 2)         COMMENT 'FIXED 类型固定金额',
    unit_price  DECIMAL(12, 4)          COMMENT 'BY_AREA/BY_METER 单价',
    project_id  VARCHAR(32)             COMMENT '所属项目（NULL=全局）',
    status      VARCHAR(16)    NOT NULL DEFAULT 'ACTIVE' COMMENT 'ACTIVE|INACTIVE',
    deleted     TINYINT(1)     NOT NULL DEFAULT 0,
    created_at  DATETIME(3)    NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    updated_at  DATETIME(3)    NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    PRIMARY KEY (id),
    KEY idx_project_status (project_id, status)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '费用项表';

CREATE TABLE fee_tiers (
    id          VARCHAR(32)    NOT NULL COMMENT '阶梯 ID',
    fee_item_id VARCHAR(32)    NOT NULL COMMENT '所属费用项 ID（TIERED 类型）',
    min_qty     DECIMAL(12, 4) NOT NULL COMMENT '阶梯下限（含）',
    max_qty     DECIMAL(12, 4)          COMMENT '阶梯上限（NULL=无上限）',
    unit_price  DECIMAL(12, 4) NOT NULL COMMENT '该阶梯单价',
    created_at  DATETIME(3)    NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    PRIMARY KEY (id),
    KEY idx_fee_item (fee_item_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '阶梯费用表';

-- 注意：bills 按 due_date 分区，PRIMARY KEY 必须包含 due_date
CREATE TABLE bills (
    id           VARCHAR(32)    NOT NULL COMMENT '账单 ID',
    no           VARCHAR(32)    NOT NULL COMMENT '账单编号',
    period       CHAR(7)        NOT NULL COMMENT '账期 YYYY-MM',
    contract_id  VARCHAR(32)    NOT NULL COMMENT '合同 ID',
    tenant_id    VARCHAR(32)    NOT NULL COMMENT '租户 ID',
    unit_id      VARCHAR(32)    NOT NULL COMMENT '单元 ID',
    project_id   VARCHAR(32)    NOT NULL COMMENT '项目 ID',
    total_amount DECIMAL(12, 2) NOT NULL COMMENT '应收总金额',
    paid_amount  DECIMAL(12, 2) NOT NULL DEFAULT 0 COMMENT '已收金额',
    status       VARCHAR(16)    NOT NULL DEFAULT 'UNPAID'
        COMMENT 'UNPAID|PARTIAL|PAID|OVERDUE|VOID',
    due_date     DATE           NOT NULL COMMENT '缴费截止日（分区键）',
    paid_at      DATETIME(3)             COMMENT '全额支付时间',
    voided_at    DATETIME(3)             COMMENT '作废时间',
    deleted      TINYINT(1)     NOT NULL DEFAULT 0,
    created_at   DATETIME(3)    NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    updated_at   DATETIME(3)    NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    PRIMARY KEY (id, due_date),           -- 分区表 PK 必须含分区列
    KEY idx_no (no),                      -- 不做 UNIQUE（分区限制）
    KEY idx_status_due (status, due_date),
    KEY idx_tenant_period (tenant_id, period),
    KEY idx_contract (contract_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '账单表'
PARTITION BY RANGE (TO_DAYS(due_date)) (
    PARTITION p202601 VALUES LESS THAN (TO_DAYS('2026-02-01')),
    PARTITION p202602 VALUES LESS THAN (TO_DAYS('2026-03-01')),
    PARTITION p202603 VALUES LESS THAN (TO_DAYS('2026-04-01')),
    PARTITION p202604 VALUES LESS THAN (TO_DAYS('2026-05-01')),
    PARTITION p202605 VALUES LESS THAN (TO_DAYS('2026-06-01')),
    PARTITION p202606 VALUES LESS THAN (TO_DAYS('2026-07-01')),
    PARTITION p202607 VALUES LESS THAN (TO_DAYS('2026-08-01')),
    PARTITION p202608 VALUES LESS THAN (TO_DAYS('2026-09-01')),
    PARTITION p202609 VALUES LESS THAN (TO_DAYS('2026-10-01')),
    PARTITION p202610 VALUES LESS THAN (TO_DAYS('2026-11-01')),
    PARTITION p202611 VALUES LESS THAN (TO_DAYS('2026-12-01')),
    PARTITION p202612 VALUES LESS THAN (TO_DAYS('2027-01-01')),
    PARTITION p202701 VALUES LESS THAN (TO_DAYS('2027-02-01')),
    PARTITION p202702 VALUES LESS THAN (TO_DAYS('2027-03-01')),
    PARTITION p202703 VALUES LESS THAN (TO_DAYS('2027-04-01')),
    PARTITION p202704 VALUES LESS THAN (TO_DAYS('2027-05-01')),
    PARTITION p202705 VALUES LESS THAN (TO_DAYS('2027-06-01')),
    PARTITION p202706 VALUES LESS THAN (TO_DAYS('2027-07-01')),
    PARTITION pmax    VALUES LESS THAN MAXVALUE
);

CREATE TABLE bill_items (
    id           VARCHAR(32)    NOT NULL COMMENT '账单明细 ID',
    bill_id      VARCHAR(32)    NOT NULL COMMENT '账单 ID',
    fee_item_id  VARCHAR(32)    NOT NULL COMMENT '费用项 ID',
    fee_item_name VARCHAR(64)   NOT NULL COMMENT '费用项名称快照',
    type         VARCHAR(16)    NOT NULL COMMENT 'FIXED|BY_AREA|BY_METER|TIERED',
    quantity     DECIMAL(12, 4)          COMMENT '数量（面积/度数等）',
    unit_price   DECIMAL(12, 4)          COMMENT '单价快照',
    amount       DECIMAL(12, 2) NOT NULL COMMENT '小计',
    meter_start  DECIMAL(12, 4)          COMMENT '本期抄表起值',
    meter_end    DECIMAL(12, 4)          COMMENT '本期抄表止值',
    created_at   DATETIME(3)    NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    PRIMARY KEY (id),
    KEY idx_bill (bill_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '账单明细表';

CREATE TABLE bill_payments (
    id          VARCHAR(32)    NOT NULL COMMENT '收款记录 ID',
    bill_id     VARCHAR(32)    NOT NULL COMMENT '账单 ID',
    amount      DECIMAL(12, 2) NOT NULL COMMENT '本次收款金额',
    method      VARCHAR(16)    NOT NULL COMMENT 'WECHAT|ALIPAY|BANK|CASH',
    external_id VARCHAR(64)             COMMENT '第三方支付流水号',
    paid_at     DATETIME(3)    NOT NULL COMMENT '收款时间',
    created_at  DATETIME(3)    NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    PRIMARY KEY (id),
    KEY idx_bill (bill_id),
    UNIQUE KEY uk_external (external_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '账单收款记录';

CREATE TABLE bill_logs (
    id          VARCHAR(32)  NOT NULL COMMENT '日志 ID',
    bill_id     VARCHAR(32)  NOT NULL COMMENT '账单 ID',
    action      VARCHAR(32)  NOT NULL COMMENT 'CREATE|PAID|VOID|OVERDUE|NOTE',
    content     VARCHAR(512)          COMMENT '日志内容',
    operator_id VARCHAR(32)           COMMENT '操作人 ID',
    created_at  DATETIME(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    PRIMARY KEY (id),
    KEY idx_bill (bill_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '账单操作日志';

-- ============================================================
-- 财务域：支付流水 / 认领 / 认领规则 / 对账 / 关账
-- ============================================================

CREATE TABLE payment_records (
    id            VARCHAR(32)    NOT NULL COMMENT '流水 ID',
    amount        DECIMAL(12, 2) NOT NULL COMMENT '到账金额',
    channel       VARCHAR(16)    NOT NULL COMMENT 'WECHAT|ALIPAY|BANK',
    external_id   VARCHAR(64)    NOT NULL COMMENT '第三方平台流水号',
    tenant_id     VARCHAR(32)             COMMENT '已关联租户 ID',
    status        VARCHAR(16)    NOT NULL DEFAULT 'PENDING'
        COMMENT 'PENDING|MATCHED|PARTIAL|UNMATCHED',
    received_at   DATETIME(3)    NOT NULL COMMENT '到账时间',
    reconciled_at DATETIME(3)             COMMENT '对账完成时间',
    claimed_at    DATETIME(3)             COMMENT '认领完成时间',
    remark        VARCHAR(512)            COMMENT '备注（银行摘要）',
    deleted       TINYINT(1)     NOT NULL DEFAULT 0,
    created_at    DATETIME(3)    NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    updated_at    DATETIME(3)    NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    PRIMARY KEY (id),
    UNIQUE KEY uk_external (external_id, channel),
    KEY idx_status (status),
    KEY idx_received_at (received_at)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '支付流水表';

CREATE TABLE claims (
    id                VARCHAR(32)    NOT NULL COMMENT '认领关系 ID',
    payment_record_id VARCHAR(32)    NOT NULL COMMENT '支付流水 ID',
    bill_id           VARCHAR(32)    NOT NULL COMMENT '账单 ID',
    amount            DECIMAL(12, 2) NOT NULL COMMENT '认领金额',
    auto_claimed      TINYINT(1)     NOT NULL DEFAULT 0 COMMENT '是否规则自动认领',
    created_at        DATETIME(3)    NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    PRIMARY KEY (id),
    KEY idx_payment (payment_record_id),
    KEY idx_bill (bill_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '认领关系表';

CREATE TABLE claim_rules (
    id             VARCHAR(32)  NOT NULL COMMENT '规则 ID',
    name           VARCHAR(64)  NOT NULL COMMENT '规则名称',
    priority       INT          NOT NULL DEFAULT 0 COMMENT '优先级（值越大越先执行）',
    condition_json JSON         NOT NULL COMMENT '匹配条件 JSON',
    action_json    JSON         NOT NULL COMMENT '执行动作 JSON',
    enabled        TINYINT(1)   NOT NULL DEFAULT 1,
    project_id     VARCHAR(32)           COMMENT '适用项目（NULL=全局）',
    created_at     DATETIME(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    updated_at     DATETIME(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    PRIMARY KEY (id),
    KEY idx_project_priority (project_id, priority DESC)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '智能认领规则表';

CREATE TABLE reconciliations (
    id               VARCHAR(32)    NOT NULL COMMENT '对账 ID',
    period           DATE           NOT NULL COMMENT '对账日期（日维度）',
    project_id       VARCHAR(32)    NOT NULL COMMENT '项目 ID',
    channel          VARCHAR(16)    NOT NULL COMMENT '支付渠道',
    total_amount     DECIMAL(12, 2) NOT NULL COMMENT '平台到账总额',
    matched_amount   DECIMAL(12, 2) NOT NULL DEFAULT 0 COMMENT '已匹配金额',
    unmatched_count  INT            NOT NULL DEFAULT 0 COMMENT '未匹配笔数',
    status           VARCHAR(16)    NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING|DONE|EXCEPTION',
    reconciled_at    DATETIME(3)             COMMENT '对账完成时间',
    created_at       DATETIME(3)    NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    PRIMARY KEY (id),
    UNIQUE KEY uk_period_project_channel (period, project_id, channel)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '日对账表';

CREATE TABLE closes (
    id            VARCHAR(32)    NOT NULL COMMENT '关账 ID',
    period        CHAR(7)        NOT NULL COMMENT '关账周期 YYYY-MM',
    project_id    VARCHAR(32)    NOT NULL COMMENT '项目 ID',
    closed_by     VARCHAR(32)    NOT NULL COMMENT '关账操作人 ID',
    closed_at     DATETIME(3)    NOT NULL COMMENT '关账时间',
    total_revenue DECIMAL(14, 2) NOT NULL COMMENT '本期总收入',
    notes         VARCHAR(512)            COMMENT '备注',
    created_at    DATETIME(3)    NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    PRIMARY KEY (id),
    UNIQUE KEY uk_period_project (period, project_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '关账记录表';

-- ============================================================
-- 工单域：工单 / 时间线 / 消息 / 附件
-- ============================================================

CREATE TABLE work_orders (
    id            VARCHAR(32)  NOT NULL COMMENT '工单 ID',
    no            VARCHAR(32)  NOT NULL COMMENT '工单编号',
    category      VARCHAR(16)  NOT NULL COMMENT 'REPAIR|CLEAN|MOVE|OTHER',
    title         VARCHAR(128) NOT NULL COMMENT '工单标题',
    description   TEXT                  COMMENT '详细描述',
    status        VARCHAR(16)  NOT NULL DEFAULT 'PENDING'
        COMMENT 'PENDING|ASSIGNED|IN_PROGRESS|DONE|CLOSED|CANCELLED',
    priority      VARCHAR(16)  NOT NULL DEFAULT 'NORMAL' COMMENT 'LOW|NORMAL|HIGH|URGENT',
    tenant_id     VARCHAR(32)           COMMENT '发起租户 ID',
    unit_id       VARCHAR(32)           COMMENT '关联单元 ID',
    project_id    VARCHAR(32)  NOT NULL COMMENT '项目 ID',
    maintainer_id VARCHAR(32)           COMMENT '维修人员 ID',
    sla_due_at    DATETIME(3)           COMMENT 'SLA 截止时间',
    completed_at  DATETIME(3)           COMMENT '完成时间',
    rated_at      DATETIME(3)           COMMENT '评价时间',
    rating        TINYINT               COMMENT '评分 1-5',
    rating_text   VARCHAR(255)          COMMENT '评价文字',
    images        JSON                  COMMENT '图片 URL 列表',
    deleted       TINYINT(1)   NOT NULL DEFAULT 0,
    created_at    DATETIME(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    updated_at    DATETIME(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    PRIMARY KEY (id),
    UNIQUE KEY uk_no (no),
    KEY idx_status_created (status, created_at),
    KEY idx_maintainer_status (maintainer_id, status),
    KEY idx_sla (sla_due_at, status),
    KEY idx_project (project_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '工单表';

CREATE TABLE wo_timelines (
    id            VARCHAR(32)  NOT NULL COMMENT '时间线 ID',
    work_order_id VARCHAR(32)  NOT NULL COMMENT '工单 ID',
    action        VARCHAR(32)  NOT NULL COMMENT 'CREATE|ASSIGN|START|COMPLETE|RATE|CANCEL',
    content       VARCHAR(512)          COMMENT '操作说明',
    operator_id   VARCHAR(32)           COMMENT '操作人 ID',
    created_at    DATETIME(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    PRIMARY KEY (id),
    KEY idx_wo (work_order_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '工单时间线';

CREATE TABLE wo_messages (
    id            VARCHAR(32)  NOT NULL COMMENT '消息 ID',
    work_order_id VARCHAR(32)  NOT NULL COMMENT '工单 ID',
    sender_id     VARCHAR(32)  NOT NULL COMMENT '发送人 ID',
    content       TEXT         NOT NULL COMMENT '消息内容',
    type          VARCHAR(16)  NOT NULL DEFAULT 'TEXT' COMMENT 'TEXT|IMAGE|FILE',
    created_at    DATETIME(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    PRIMARY KEY (id),
    KEY idx_wo_created (work_order_id, created_at)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '工单聊天消息';

CREATE TABLE wo_attachments (
    id            VARCHAR(32)  NOT NULL COMMENT '附件 ID',
    work_order_id VARCHAR(32)  NOT NULL COMMENT '工单 ID',
    name          VARCHAR(255) NOT NULL COMMENT '文件名',
    url           VARCHAR(512) NOT NULL COMMENT '文件 URL',
    size          BIGINT       NOT NULL DEFAULT 0 COMMENT '文件大小（字节）',
    created_at    DATETIME(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    PRIMARY KEY (id),
    KEY idx_wo (work_order_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '工单附件表';

-- ============================================================
-- 投诉域：投诉 / 时间线 / 申诉
-- ============================================================

CREATE TABLE complaints (
    id          VARCHAR(32)  NOT NULL COMMENT '投诉 ID',
    no          VARCHAR(32)  NOT NULL COMMENT '投诉编号',
    title       VARCHAR(128) NOT NULL COMMENT '投诉标题',
    content     TEXT         NOT NULL COMMENT '投诉内容',
    category    VARCHAR(32)  NOT NULL COMMENT 'NOISE|LEAK|FACILITY|SERVICE|OTHER',
    status      VARCHAR(16)  NOT NULL DEFAULT 'PENDING'
        COMMENT 'PENDING|HANDLING|RESOLVED|CLOSED',
    tenant_id   VARCHAR(32)  NOT NULL COMMENT '投诉人租户 ID',
    unit_id     VARCHAR(32)           COMMENT '涉及单元 ID',
    project_id  VARCHAR(32)  NOT NULL COMMENT '项目 ID',
    handler_id  VARCHAR(32)           COMMENT '处理人 ID',
    resolved_at DATETIME(3)           COMMENT '解决时间',
    deleted     TINYINT(1)   NOT NULL DEFAULT 0,
    created_at  DATETIME(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    updated_at  DATETIME(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    PRIMARY KEY (id),
    UNIQUE KEY uk_no (no),
    KEY idx_status (status),
    KEY idx_project (project_id),
    KEY idx_tenant (tenant_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '投诉表';

CREATE TABLE complaint_timelines (
    id           VARCHAR(32)  NOT NULL COMMENT '时间线 ID',
    complaint_id VARCHAR(32)  NOT NULL COMMENT '投诉 ID',
    action       VARCHAR(32)  NOT NULL COMMENT 'SUBMIT|ASSIGN|REPLY|RESOLVE|CLOSE',
    content      VARCHAR(512)          COMMENT '操作说明',
    operator_id  VARCHAR(32)           COMMENT '操作人 ID',
    created_at   DATETIME(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    PRIMARY KEY (id),
    KEY idx_complaint (complaint_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '投诉时间线';

CREATE TABLE appeals (
    id           VARCHAR(32)  NOT NULL COMMENT '申诉 ID',
    complaint_id VARCHAR(32)  NOT NULL COMMENT '关联投诉 ID',
    reason       TEXT         NOT NULL COMMENT '申诉理由',
    result       VARCHAR(16)            COMMENT 'UPHELD|REJECTED|SETTLED',
    result_note  VARCHAR(512)           COMMENT '处理说明',
    appealed_at  DATETIME(3)  NOT NULL  COMMENT '申诉提交时间',
    resolved_at  DATETIME(3)            COMMENT '申诉处理时间',
    created_at   DATETIME(3)  NOT NULL  DEFAULT CURRENT_TIMESTAMP(3),
    PRIMARY KEY (id),
    KEY idx_complaint (complaint_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '申诉表';

-- ============================================================
-- 系统域：用户 / 角色 / 权限 / RBAC 关联
-- ============================================================

CREATE TABLE users (
    id            VARCHAR(32)  NOT NULL COMMENT '用户 ID',
    username      VARCHAR(32)  NOT NULL COMMENT '登录账号',
    password_hash VARCHAR(72)  NOT NULL COMMENT 'BCrypt 哈希（cost=10）',
    name          VARCHAR(32)  NOT NULL COMMENT '真实姓名',
    phone         VARCHAR(16)  NOT NULL COMMENT '手机号',
    avatar        VARCHAR(512)          COMMENT '头像 URL',
    status        VARCHAR(16)  NOT NULL DEFAULT 'ACTIVE' COMMENT 'ACTIVE|INACTIVE|LOCKED',
    project_id    VARCHAR(32)           COMMENT '归属项目（NULL=全局管理员）',
    last_login_at DATETIME(3)           COMMENT '最后登录时间',
    deleted       TINYINT(1)   NOT NULL DEFAULT 0,
    created_at    DATETIME(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    updated_at    DATETIME(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    PRIMARY KEY (id),
    UNIQUE KEY uk_username (username, deleted),
    UNIQUE KEY uk_phone (phone, deleted)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '用户表';

CREATE TABLE roles (
    code        VARCHAR(32)  NOT NULL COMMENT '角色码（如 SuperAdmin）',
    name        VARCHAR(32)  NOT NULL COMMENT '角色显示名',
    description VARCHAR(255)          COMMENT '角色描述',
    built_in    TINYINT(1)   NOT NULL DEFAULT 0 COMMENT '1=内置不可删',
    PRIMARY KEY (code)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '角色表';

CREATE TABLE permissions (
    code    VARCHAR(64) NOT NULL COMMENT '权限码（如 tenant:view）',
    name    VARCHAR(32) NOT NULL COMMENT '权限显示名',
    `group` VARCHAR(32) NOT NULL COMMENT '权限分组',
    PRIMARY KEY (code)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '权限码表';

CREATE TABLE role_permissions (
    role_code       VARCHAR(32) NOT NULL COMMENT '角色码',
    permission_code VARCHAR(64) NOT NULL COMMENT '权限码',
    PRIMARY KEY (role_code, permission_code)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '角色权限关联表';

CREATE TABLE user_roles (
    user_id   VARCHAR(32) NOT NULL COMMENT '用户 ID',
    role_code VARCHAR(32) NOT NULL COMMENT '角色码',
    PRIMARY KEY (user_id, role_code)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '用户角色关联表';

-- 操作日志（按月 RANGE 分区）
CREATE TABLE op_logs (
    id         BIGINT       NOT NULL AUTO_INCREMENT COMMENT '日志 ID',
    actor_id   VARCHAR(32)  NOT NULL COMMENT '操作人 ID',
    actor_name VARCHAR(32)  NOT NULL COMMENT '操作人姓名',
    action     VARCHAR(32)  NOT NULL COMMENT 'CREATE|UPDATE|DELETE|LOGIN|LOGOUT',
    target     VARCHAR(255) NOT NULL COMMENT '操作对象（格式：类型:ID）',
    diff       JSON                  COMMENT '变更前后 JSON diff',
    ip         VARCHAR(45)           COMMENT '请求 IP',
    user_agent VARCHAR(512)          COMMENT 'User-Agent',
    trace_id   VARCHAR(64)           COMMENT '链路追踪 ID',
    at         DATETIME(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3) COMMENT '操作时间（分区键）',
    PRIMARY KEY (id, at),
    KEY idx_actor_at (actor_id, at),
    KEY idx_action_at (action, at),
    KEY idx_trace (trace_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '操作审计日志'
PARTITION BY RANGE (TO_DAYS(at)) (
    PARTITION p202601 VALUES LESS THAN (TO_DAYS('2026-02-01')),
    PARTITION p202602 VALUES LESS THAN (TO_DAYS('2026-03-01')),
    PARTITION p202603 VALUES LESS THAN (TO_DAYS('2026-04-01')),
    PARTITION p202604 VALUES LESS THAN (TO_DAYS('2026-05-01')),
    PARTITION p202605 VALUES LESS THAN (TO_DAYS('2026-06-01')),
    PARTITION p202606 VALUES LESS THAN (TO_DAYS('2026-07-01')),
    PARTITION p202607 VALUES LESS THAN (TO_DAYS('2026-08-01')),
    PARTITION p202608 VALUES LESS THAN (TO_DAYS('2026-09-01')),
    PARTITION p202609 VALUES LESS THAN (TO_DAYS('2026-10-01')),
    PARTITION p202610 VALUES LESS THAN (TO_DAYS('2026-11-01')),
    PARTITION p202611 VALUES LESS THAN (TO_DAYS('2026-12-01')),
    PARTITION p202612 VALUES LESS THAN (TO_DAYS('2027-01-01')),
    PARTITION pmax    VALUES LESS THAN MAXVALUE
);

-- ============================================================
-- 系统域：消息模板 / 通知 / 文件 / 公告 / 导出任务
-- ============================================================

CREATE TABLE message_templates (
    id          VARCHAR(32)  NOT NULL COMMENT '模板 ID',
    code        VARCHAR(64)  NOT NULL COMMENT '模板业务码（如 BILL_DUE_REMIND）',
    name        VARCHAR(64)  NOT NULL COMMENT '模板名称',
    type        VARCHAR(16)  NOT NULL COMMENT 'SMS|EMAIL|PUSH|WECHAT',
    title       VARCHAR(128)          COMMENT '消息标题（EMAIL/PUSH 使用）',
    content     TEXT         NOT NULL COMMENT '模板内容（支持 {{变量}} 占位）',
    params_json JSON                  COMMENT '参数说明 JSON',
    enabled     TINYINT(1)   NOT NULL DEFAULT 1,
    created_at  DATETIME(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    updated_at  DATETIME(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    PRIMARY KEY (id),
    UNIQUE KEY uk_code_type (code, type)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '消息模板表';

CREATE TABLE notifications (
    id          VARCHAR(32)  NOT NULL COMMENT '通知 ID',
    user_id     VARCHAR(32)  NOT NULL COMMENT '接收用户 ID',
    type        VARCHAR(32)  NOT NULL COMMENT 'BILL|WORKORDER|COMPLAINT|SYSTEM',
    title       VARCHAR(128) NOT NULL COMMENT '通知标题',
    content     TEXT         NOT NULL COMMENT '通知内容',
    is_read     TINYINT(1)   NOT NULL DEFAULT 0 COMMENT '是否已读',
    target_id   VARCHAR(32)           COMMENT '关联业务对象 ID',
    target_type VARCHAR(32)           COMMENT '关联业务类型（bill/workorder...）',
    created_at  DATETIME(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    PRIMARY KEY (id),
    KEY idx_user_read (user_id, is_read),
    KEY idx_created (created_at)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '站内通知表';

CREATE TABLE uploads (
    id         VARCHAR(32)  NOT NULL COMMENT '上传记录 ID',
    name       VARCHAR(255) NOT NULL COMMENT '文件原始名',
    size       BIGINT       NOT NULL DEFAULT 0 COMMENT '文件大小（字节）',
    mime_type  VARCHAR(64)           COMMENT 'MIME 类型',
    url        VARCHAR(512) NOT NULL COMMENT '文件访问 URL',
    oss_key    VARCHAR(512) NOT NULL COMMENT 'OSS Object Key',
    creator_id VARCHAR(32)           COMMENT '上传用户 ID',
    created_at DATETIME(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    PRIMARY KEY (id),
    KEY idx_creator (creator_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '文件上传记录表';

CREATE TABLE announcements (
    id           VARCHAR(32)  NOT NULL COMMENT '公告 ID',
    title        VARCHAR(128) NOT NULL COMMENT '公告标题',
    content      TEXT         NOT NULL COMMENT '公告内容（富文本）',
    type         VARCHAR(16)  NOT NULL DEFAULT 'NOTICE' COMMENT 'NOTICE|MAINTENANCE|ACTIVITY',
    status       VARCHAR(16)  NOT NULL DEFAULT 'DRAFT' COMMENT 'DRAFT|PUBLISHED|EXPIRED',
    author_id    VARCHAR(32)  NOT NULL COMMENT '发布人 ID',
    project_id   VARCHAR(32)           COMMENT '适用项目（NULL=全局）',
    published_at DATETIME(3)           COMMENT '发布时间',
    expired_at   DATETIME(3)           COMMENT '过期时间',
    deleted      TINYINT(1)   NOT NULL DEFAULT 0,
    created_at   DATETIME(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    updated_at   DATETIME(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    PRIMARY KEY (id),
    KEY idx_status_published (status, published_at),
    KEY idx_project (project_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '公告表';

CREATE TABLE export_tasks (
    id          VARCHAR(32)  NOT NULL COMMENT '导出任务 ID',
    type        VARCHAR(32)  NOT NULL COMMENT 'BILL|TENANT|CONTRACT|WORKORDER|COMPLAINT',
    params_json JSON                  COMMENT '查询参数快照',
    status      VARCHAR(16)  NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING|RUNNING|DONE|FAILED',
    file_url    VARCHAR(512)          COMMENT '生成文件 OSS URL',
    operator_id VARCHAR(32)  NOT NULL COMMENT '操作人 ID',
    started_at  DATETIME(3)           COMMENT '开始处理时间',
    finished_at DATETIME(3)           COMMENT '完成时间',
    error_msg   VARCHAR(512)          COMMENT '失败原因',
    created_at  DATETIME(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    PRIMARY KEY (id),
    KEY idx_operator_status (operator_id, status),
    KEY idx_created (created_at)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '异步导出任务表';
