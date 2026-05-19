ALTER TABLE notifications
    ADD COLUMN channel  VARCHAR(16)  NOT NULL DEFAULT 'IN_APP'   COMMENT 'IN_APP|SMS|PUSH'        AFTER is_read,
    ADD COLUMN status   VARCHAR(16)  NOT NULL DEFAULT 'PENDING'  COMMENT 'PENDING|SENT|FAILED'    AFTER channel,
    ADD COLUMN read_at  DATETIME(3)                              COMMENT '已读时间'                AFTER status;
