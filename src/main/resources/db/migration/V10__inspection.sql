CREATE TABLE inspection_plans (
    id            VARCHAR(36)   NOT NULL,
    name          VARCHAR(200)  NOT NULL,
    description   VARCHAR(1000),
    route         VARCHAR(1000),
    frequency     VARCHAR(20)   NOT NULL COMMENT 'DAILY | WEEKLY | MONTHLY',
    status        VARCHAR(20)   NOT NULL DEFAULT 'DRAFT' COMMENT 'DRAFT | ACTIVE | DISABLED',
    created_at    DATETIME(3)   NOT NULL,
    updated_at    DATETIME(3)   NOT NULL,
    deleted       TINYINT(1)    NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE INDEX uk_plan_name (name),
    INDEX idx_plan_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE inspection_tasks (
    id              VARCHAR(36)   NOT NULL,
    plan_id         VARCHAR(36)   NOT NULL,
    assignee_id     VARCHAR(36)   NOT NULL,
    assignee_name   VARCHAR(64)   NOT NULL,
    status          VARCHAR(20)   NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING | IN_PROGRESS | COMPLETED',
    scheduled_at    DATETIME(3)   NOT NULL,
    started_at      DATETIME(3),
    completed_at    DATETIME(3),
    result          VARCHAR(2000),
    remark          VARCHAR(500),
    created_at      DATETIME(3)   NOT NULL,
    updated_at      DATETIME(3)   NOT NULL,
    deleted         TINYINT(1)    NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    INDEX idx_task_plan (plan_id),
    INDEX idx_task_assignee (assignee_id),
    INDEX idx_task_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
