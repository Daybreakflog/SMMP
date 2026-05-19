-- 活动
CREATE TABLE activities (
    id                 VARCHAR(36)   NOT NULL,
    title              VARCHAR(200)  NOT NULL,
    description        TEXT,
    location           VARCHAR(200),
    status             VARCHAR(20)   NOT NULL DEFAULT 'DRAFT' COMMENT 'DRAFT | PUBLISHED | CLOSED',
    author_id          VARCHAR(36)   NOT NULL,
    max_participants   INT           NOT NULL DEFAULT 0       COMMENT '0=不限',
    register_deadline  DATETIME(3),
    activity_start_at  DATETIME(3),
    activity_end_at    DATETIME(3),
    created_at         DATETIME(3)   NOT NULL,
    updated_at         DATETIME(3)   NOT NULL,
    deleted            TINYINT(1)    NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    INDEX idx_activities_status (status),
    INDEX idx_activities_author (author_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 活动报名
CREATE TABLE activity_registrations (
    id          VARCHAR(36)  NOT NULL,
    activity_id VARCHAR(36)  NOT NULL,
    user_id     VARCHAR(36)  NOT NULL,
    created_at  DATETIME(3)  NOT NULL,
    PRIMARY KEY (id),
    UNIQUE INDEX uk_activity_user (activity_id, user_id),
    INDEX idx_registrations_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
