-- xxl-job 专用数据库（物业库 property_db 由 Flyway 管理，见 B1 里程碑）
CREATE DATABASE IF NOT EXISTS `xxl_job`
    DEFAULT CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE `xxl_job`;

CREATE TABLE IF NOT EXISTS `xxl_job_lock` (
    `lock_name` varchar(50) NOT NULL COMMENT '锁名称',
    PRIMARY KEY (`lock_name`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

CREATE TABLE IF NOT EXISTS `xxl_job_group` (
    `id`           int          NOT NULL AUTO_INCREMENT,
    `app_name`     varchar(64)  NOT NULL COMMENT '执行器 AppName',
    `title`        varchar(12)  NOT NULL COMMENT '执行器名称',
    `address_type` tinyint      NOT NULL DEFAULT '0' COMMENT '0=自动注册 1=手动录入',
    `address_list` text COMMENT '执行器地址列表（逗号分隔）',
    `update_time`  datetime     DEFAULT NULL,
    PRIMARY KEY (`id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

CREATE TABLE IF NOT EXISTS `xxl_job_info` (
    `id`                        int          NOT NULL AUTO_INCREMENT,
    `job_group`                 int          NOT NULL COMMENT '执行器主键 ID',
    `job_desc`                  varchar(255) NOT NULL,
    `add_time`                  datetime     DEFAULT NULL,
    `update_time`               datetime     DEFAULT NULL,
    `author`                    varchar(64)  DEFAULT NULL,
    `alarm_email`               varchar(255) DEFAULT NULL,
    `schedule_type`             varchar(50)  NOT NULL DEFAULT 'NONE',
    `schedule_conf`             varchar(128) DEFAULT NULL,
    `misfire_strategy`          varchar(50)  NOT NULL DEFAULT 'DO_NOTHING',
    `executor_route_strategy`   varchar(50)  DEFAULT NULL,
    `executor_handler`          varchar(255) DEFAULT NULL,
    `executor_param`            varchar(512) DEFAULT NULL,
    `executor_block_strategy`   varchar(50)  DEFAULT NULL,
    `executor_timeout`          int          NOT NULL DEFAULT '0',
    `executor_fail_retry_count` int          NOT NULL DEFAULT '0',
    `glue_type`                 varchar(50)  NOT NULL,
    `glue_source`               mediumtext,
    `glue_remark`               varchar(128) DEFAULT NULL,
    `glue_updatetime`           datetime     DEFAULT NULL,
    `child_jobid`               varchar(255) DEFAULT NULL,
    `trigger_status`            tinyint      NOT NULL DEFAULT '0',
    `trigger_last_time`         bigint       NOT NULL DEFAULT '0',
    `trigger_next_time`         bigint       NOT NULL DEFAULT '0',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

CREATE TABLE IF NOT EXISTS `xxl_job_log` (
    `id`                        bigint  NOT NULL AUTO_INCREMENT,
    `job_group`                 int     NOT NULL,
    `job_id`                    int     NOT NULL,
    `executor_address`          varchar(255) DEFAULT NULL,
    `executor_handler`          varchar(255) DEFAULT NULL,
    `executor_param`            varchar(512) DEFAULT NULL,
    `executor_sharding_param`   varchar(20)  DEFAULT NULL,
    `executor_fail_retry_count` int     NOT NULL DEFAULT '0',
    `trigger_time`              datetime     DEFAULT NULL,
    `trigger_code`              int     NOT NULL,
    `trigger_msg`               text,
    `handle_time`               datetime     DEFAULT NULL,
    `handle_code`               int     NOT NULL,
    `handle_msg`                text,
    `alarm_status`              tinyint NOT NULL DEFAULT '0',
    PRIMARY KEY (`id`),
    KEY `I_trigger_time` (`trigger_time`),
    KEY `I_handle_code` (`handle_code`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

CREATE TABLE IF NOT EXISTS `xxl_job_logglue` (
    `id`          int          NOT NULL AUTO_INCREMENT,
    `job_id`      int          NOT NULL,
    `glue_type`   varchar(50)  DEFAULT NULL,
    `glue_source` mediumtext,
    `glue_remark` varchar(128) NOT NULL,
    `add_time`    datetime     DEFAULT NULL,
    `update_time` datetime     DEFAULT NULL,
    PRIMARY KEY (`id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

CREATE TABLE IF NOT EXISTS `xxl_job_registry` (
    `id`             int          NOT NULL AUTO_INCREMENT,
    `registry_group` varchar(50)  NOT NULL,
    `registry_key`   varchar(255) NOT NULL,
    `registry_value` varchar(255) NOT NULL,
    `update_time`    datetime     DEFAULT NULL,
    PRIMARY KEY (`id`),
    KEY `I_g_k_v` (`registry_group`, `registry_key`, `registry_value`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

CREATE TABLE IF NOT EXISTS `xxl_job_user` (
    `id`         int         NOT NULL AUTO_INCREMENT,
    `username`   varchar(50) NOT NULL COMMENT '账号',
    `password`   varchar(50) NOT NULL COMMENT '密码',
    `role`       tinyint     NOT NULL COMMENT '0=普通用户 1=管理员',
    `permission` varchar(255) DEFAULT NULL COMMENT '执行器 ID 列表（逗号分隔）',
    PRIMARY KEY (`id`),
    UNIQUE KEY `I_username` (`username`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

-- 初始数据
INSERT IGNORE INTO `xxl_job_user`(`username`, `password`, `role`, `permission`)
VALUES ('admin', 'e10adc3949ba59abbe56e057f20f883e', 1, NULL);

INSERT IGNORE INTO `xxl_job_lock`(`lock_name`)
VALUES ('schedule_lock');
