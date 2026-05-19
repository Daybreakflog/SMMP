-- 访客预约
CREATE TABLE visitor_appointments (
    id                      VARCHAR(36)   NOT NULL,
    visitor_name            VARCHAR(50)   NOT NULL,
    visitor_phone           VARCHAR(20)   NOT NULL,
    visitor_id_card         VARCHAR(30),
    purpose                 VARCHAR(200),
    visit_date              DATE          NOT NULL,
    expected_arrival_at     DATETIME(3),
    expected_departure_at   DATETIME(3),
    status                  VARCHAR(20)   NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING | APPROVED | REJECTED | CHECKED_IN | CHECKED_OUT',
    applicant_id            VARCHAR(36)   NOT NULL,
    approved_by             VARCHAR(36),
    approved_at             DATETIME(3),
    check_in_at             DATETIME(3),
    check_out_at            DATETIME(3),
    remark                  VARCHAR(500),
    created_at              DATETIME(3)   NOT NULL,
    updated_at              DATETIME(3)   NOT NULL,
    deleted                 TINYINT(1)    NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    INDEX idx_visitor_status (status),
    INDEX idx_visitor_applicant (applicant_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
