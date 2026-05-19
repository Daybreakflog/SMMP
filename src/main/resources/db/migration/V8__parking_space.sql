-- 车位管理
CREATE TABLE parking_spaces (
    id              VARCHAR(36)   NOT NULL,
    space_no        VARCHAR(30)   NOT NULL,
    zone            VARCHAR(30)   NOT NULL,
    status          VARCHAR(20)   NOT NULL DEFAULT 'AVAILABLE' COMMENT 'AVAILABLE | OCCUPIED | MAINTENANCE',
    owner_id        VARCHAR(36),
    owner_name      VARCHAR(64),
    vehicle_plate   VARCHAR(20),
    remark          VARCHAR(500),
    created_at      DATETIME(3)   NOT NULL,
    updated_at      DATETIME(3)   NOT NULL,
    deleted         TINYINT(1)    NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE INDEX uk_zone_space_no (zone, space_no),
    INDEX idx_parking_status (status),
    INDEX idx_parking_owner (owner_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
