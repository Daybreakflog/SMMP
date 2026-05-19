-- 投票 / 问卷
CREATE TABLE polls (
    id          VARCHAR(36)   NOT NULL,
    title       VARCHAR(200)  NOT NULL,
    description TEXT,
    type        VARCHAR(20)   NOT NULL DEFAULT 'SINGLE_CHOICE' COMMENT 'SINGLE_CHOICE | MULTIPLE_CHOICE',
    status      VARCHAR(20)   NOT NULL DEFAULT 'DRAFT'         COMMENT 'DRAFT | PUBLISHED | CLOSED',
    author_id   VARCHAR(36)   NOT NULL,
    deadline    DATETIME(3),
    created_at  DATETIME(3)   NOT NULL,
    updated_at  DATETIME(3)   NOT NULL,
    deleted     TINYINT(1)    NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    INDEX idx_polls_status (status),
    INDEX idx_polls_author (author_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE poll_options (
    id          VARCHAR(36)   NOT NULL,
    poll_id     VARCHAR(36)   NOT NULL,
    content     VARCHAR(500)  NOT NULL,
    sort_order  INT           NOT NULL DEFAULT 0,
    created_at  DATETIME(3)   NOT NULL,
    PRIMARY KEY (id),
    INDEX idx_poll_options_poll (poll_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE poll_votes (
    id          VARCHAR(36)   NOT NULL,
    poll_id     VARCHAR(36)   NOT NULL,
    option_id   VARCHAR(36)   NOT NULL,
    user_id     VARCHAR(36)   NOT NULL,
    created_at  DATETIME(3)   NOT NULL,
    PRIMARY KEY (id),
    INDEX idx_poll_votes_user_poll (poll_id, user_id),
    INDEX idx_poll_votes_option (option_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
