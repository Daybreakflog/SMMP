CREATE TABLE facilities (
    id                    VARCHAR(36)   NOT NULL,
    name                  VARCHAR(200)  NOT NULL,
    category              VARCHAR(20)   NOT NULL COMMENT 'ELEVATOR | FIRE_EQUIPMENT | WATER_SUPPLY | ELECTRICAL | OTHER',
    location              VARCHAR(200),
    status                VARCHAR(20)   NOT NULL DEFAULT 'NORMAL' COMMENT 'NORMAL | MAINTENANCE | SCRAPPED',
    install_date          DATE,
    last_maintenance_at   DATETIME(3),
    remark                VARCHAR(500),
    created_at            DATETIME(3)   NOT NULL,
    updated_at            DATETIME(3)   NOT NULL,
    deleted               TINYINT(1)    NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE INDEX uk_category_name (category, name),
    INDEX idx_facility_status (status),
    INDEX idx_facility_category (category)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE facility_maintenance_records (
    id              VARCHAR(36)    NOT NULL,
    facility_id     VARCHAR(36)    NOT NULL,
    type            VARCHAR(20)    NOT NULL COMMENT 'ROUTINE | REPAIR | INSPECTION',
    description     VARCHAR(1000)  NOT NULL,
    maintained_by   VARCHAR(64),
    maintained_at   DATETIME(3)    NOT NULL,
    cost            DECIMAL(12,2),
    remark          VARCHAR(500),
    created_at      DATETIME(3)    NOT NULL,
    updated_at      DATETIME(3)    NOT NULL,
    deleted         TINYINT(1)     NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    INDEX idx_fmr_facility (facility_id),
    INDEX idx_fmr_type (type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
