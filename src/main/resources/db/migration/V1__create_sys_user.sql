CREATE TABLE IF NOT EXISTS `sys_user`
(
    `id`            BIGINT       NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
    `username`      VARCHAR(50)  NOT NULL COMMENT 'Login name',
    `password_hash` VARCHAR(100) NOT NULL COMMENT 'BCrypt password hash',
    `display_name`  VARCHAR(80)  NOT NULL COMMENT 'Display name',
    `email`         VARCHAR(120)          DEFAULT NULL COMMENT 'Email address',
    `phone`         VARCHAR(20)           DEFAULT NULL COMMENT 'Phone number',
    `status`        TINYINT      NOT NULL DEFAULT 1 COMMENT '0 disabled, 1 enabled',
    `deleted`       TINYINT      NOT NULL DEFAULT 0 COMMENT 'Logical delete flag',
    `created_at`    DATETIME(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    `updated_at`    DATETIME(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_sys_user_username` (`username`),
    KEY `idx_sys_user_display_name` (`display_name`),
    KEY `idx_sys_user_status` (`status`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT = 'System users';
