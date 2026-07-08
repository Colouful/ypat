-- ===========================================================================
-- YPAT 微信支付 APIv3 保证金/会员统一支付 — 待执行 SQL
-- ===========================================================================
-- 幂等说明：
--   * CREATE TABLE IF NOT EXISTS 重复执行不会破坏已有结构
--   * t_member_order 新增列使用 information_schema 守卫，避免重复 ADD COLUMN
--   * 本脚本不包含任何真实密钥、Token 或证书内容
-- ===========================================================================

SET NAMES utf8mb4;
SET time_zone = '+08:00';

CREATE TABLE IF NOT EXISTS `t_deposit_config` (
  `id`                    BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
  `enabled`               VARCHAR(1)   NOT NULL DEFAULT '1'    COMMENT '0 关闭 1 开启',
  `amount_fen`            INT          NOT NULL                COMMENT '正式保证金金额（分）',
  `test_enabled`          VARCHAR(1)   NOT NULL DEFAULT '0'    COMMENT '0 关闭 1 开启测试金额',
  `test_amount_fen`       INT          NOT NULL DEFAULT 1      COMMENT '测试保证金金额（分）',
  `display_amount_fen`    INT          NOT NULL                COMMENT '前端展示金额（分）',
  `refund_wait_days`      INT          NOT NULL DEFAULT 0      COMMENT '可退等待天数',
  `early_refund_fee_rate` INT          NOT NULL DEFAULT 0      COMMENT '提前退款手续费率，万分比',
  `agreement_summary`     VARCHAR(512) DEFAULT NULL            COMMENT '保证金协议摘要',
  `updated_at`            DATETIME     DEFAULT NULL            COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='保证金配置';

CREATE TABLE IF NOT EXISTS `t_deposit_order` (
  `id`             BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
  `out_trade_no`   VARCHAR(64)  NOT NULL                COMMENT '商户订单号',
  `user_id`        BIGINT       NOT NULL                COMMENT '用户 id',
  `amount_fen`     INT          NOT NULL                COMMENT '支付金额（分）',
  `channel`        VARCHAR(16)  NOT NULL                COMMENT '支付渠道 MINIAPP/H5',
  `status`         VARCHAR(16)  NOT NULL                COMMENT 'PENDING/PAID/FAILED/CLOSED/REFUNDED',
  `prepay_id`      VARCHAR(128) DEFAULT NULL            COMMENT '微信预支付 id',
  `transaction_id` VARCHAR(64)  DEFAULT NULL            COMMENT '微信支付订单号',
  `paid_at`        DATETIME     DEFAULT NULL            COMMENT '支付完成时间',
  `created_at`     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at`     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
  `version`        INT          NOT NULL DEFAULT 0      COMMENT '乐观锁版本号',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_out_trade_no` (`out_trade_no`),
  KEY `idx_user_status` (`user_id`, `status`),
  KEY `idx_status_updated_at` (`status`, `updated_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='保证金订单';

CREATE TABLE IF NOT EXISTS `t_payment_order` (
  `id`                 BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键',
  `payment_no`         VARCHAR(64)   NOT NULL                COMMENT '统一支付单号',
  `business_type`      VARCHAR(16)   NOT NULL                COMMENT 'DEPOSIT/MEMBER',
  `business_order_no`  VARCHAR(64)   NOT NULL                COMMENT '业务订单号',
  `out_trade_no`       VARCHAR(64)   NOT NULL                COMMENT '微信商户订单号',
  `user_id`            BIGINT        NOT NULL                COMMENT '用户 id',
  `channel`            VARCHAR(16)   NOT NULL                COMMENT '支付渠道 MINIAPP/H5',
  `amount_fen`         INT           NOT NULL                COMMENT '支付金额（分）',
  `status`             VARCHAR(16)   NOT NULL                COMMENT 'PENDING/PAID/FAILED/CLOSED/REFUNDED',
  `prepay_id`          VARCHAR(128)  DEFAULT NULL            COMMENT '微信预支付 id',
  `h5_url`             VARCHAR(1024) DEFAULT NULL            COMMENT 'H5 支付跳转地址',
  `transaction_id`     VARCHAR(64)   DEFAULT NULL            COMMENT '微信支付订单号',
  `wechat_trade_state` VARCHAR(32)   DEFAULT NULL            COMMENT '微信交易状态',
  `notify_event_id`    VARCHAR(128)  DEFAULT NULL            COMMENT '微信回调事件 id',
  `notify_digest`      VARCHAR(128)  DEFAULT NULL            COMMENT '回调摘要，用于幂等去重',
  `paid_at`            DATETIME      DEFAULT NULL            COMMENT '支付完成时间',
  `created_at`         DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at`         DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
  `version`            INT           NOT NULL DEFAULT 0      COMMENT '乐观锁版本号',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_payment_no` (`payment_no`),
  UNIQUE KEY `uk_out_trade_no` (`out_trade_no`),
  KEY `idx_payment_order_business` (`business_type`, `business_order_no`),
  KEY `idx_user_status` (`user_id`, `status`),
  KEY `idx_payment_order_user_created` (`user_id`, `created_at`),
  KEY `idx_status_updated_at` (`status`, `updated_at`),
  KEY `idx_notify_event_id` (`notify_event_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='统一支付订单';

INSERT INTO `t_deposit_config`
  (`id`, `enabled`, `amount_fen`, `test_enabled`, `test_amount_fen`, `display_amount_fen`,
   `refund_wait_days`, `early_refund_fee_rate`, `agreement_summary`, `updated_at`)
VALUES
  (1, '1', 19900, '1', 1, 1, 90, 15, '保证金用于平台履约保障，满足协议约定后可按规则申请退还。', NOW())
ON DUPLICATE KEY UPDATE
  `updated_at` = NOW();

SET @ddl := (
  SELECT IF(COUNT(*) = 1,
    'UPDATE `t_deposit_order` SET `created_at` = NOW() WHERE `created_at` IS NULL',
    'SELECT ''skip backfill t_deposit_order.created_at'''
  )
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 't_deposit_order'
    AND COLUMN_NAME = 'created_at'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @ddl := (
  SELECT IF(COUNT(*) = 1,
    'ALTER TABLE `t_deposit_order` MODIFY COLUMN `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT ''创建时间''',
    'SELECT ''skip normalize t_deposit_order.created_at'''
  )
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 't_deposit_order'
    AND COLUMN_NAME = 'created_at'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @ddl := (
  SELECT IF(COUNT(*) = 1,
    'UPDATE `t_deposit_order` SET `updated_at` = NOW() WHERE `updated_at` IS NULL',
    'SELECT ''skip backfill t_deposit_order.updated_at'''
  )
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 't_deposit_order'
    AND COLUMN_NAME = 'updated_at'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @ddl := (
  SELECT IF(COUNT(*) = 1,
    'ALTER TABLE `t_deposit_order` MODIFY COLUMN `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT ''更新时间''',
    'SELECT ''skip normalize t_deposit_order.updated_at'''
  )
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 't_deposit_order'
    AND COLUMN_NAME = 'updated_at'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @ddl := (
  SELECT IF(COUNT(*) = 0,
    'ALTER TABLE `t_deposit_order` ADD COLUMN `version` INT NOT NULL DEFAULT 0 COMMENT ''乐观锁版本号''',
    'SELECT ''skip t_deposit_order.version'''
  )
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 't_deposit_order'
    AND COLUMN_NAME = 'version'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @ddl := (
  SELECT IF(COUNT(*) = 1,
    'UPDATE `t_payment_order` SET `created_at` = NOW() WHERE `created_at` IS NULL',
    'SELECT ''skip backfill t_payment_order.created_at'''
  )
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 't_payment_order'
    AND COLUMN_NAME = 'created_at'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @ddl := (
  SELECT IF(COUNT(*) = 1,
    'ALTER TABLE `t_payment_order` MODIFY COLUMN `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT ''创建时间''',
    'SELECT ''skip normalize t_payment_order.created_at'''
  )
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 't_payment_order'
    AND COLUMN_NAME = 'created_at'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @ddl := (
  SELECT IF(COUNT(*) = 1,
    'UPDATE `t_payment_order` SET `updated_at` = NOW() WHERE `updated_at` IS NULL',
    'SELECT ''skip backfill t_payment_order.updated_at'''
  )
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 't_payment_order'
    AND COLUMN_NAME = 'updated_at'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @ddl := (
  SELECT IF(COUNT(*) = 1,
    'ALTER TABLE `t_payment_order` MODIFY COLUMN `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT ''更新时间''',
    'SELECT ''skip normalize t_payment_order.updated_at'''
  )
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 't_payment_order'
    AND COLUMN_NAME = 'updated_at'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @ddl := (
  SELECT IF(COUNT(*) = 0,
    'ALTER TABLE `t_payment_order` ADD COLUMN `version` INT NOT NULL DEFAULT 0 COMMENT ''乐观锁版本号''',
    'SELECT ''skip t_payment_order.version'''
  )
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 't_payment_order'
    AND COLUMN_NAME = 'version'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @ddl := (
  SELECT IF(COUNT(*) > 0,
    'ALTER TABLE `t_payment_order` DROP INDEX `uk_business_order`',
    'SELECT ''skip drop t_payment_order.uk_business_order'''
  )
  FROM information_schema.STATISTICS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 't_payment_order'
    AND INDEX_NAME = 'uk_business_order'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @ddl := (
  SELECT IF(COUNT(*) = 0,
    'ALTER TABLE `t_payment_order` ADD KEY `idx_payment_order_business` (`business_type`, `business_order_no`)',
    'SELECT ''skip t_payment_order.idx_payment_order_business'''
  )
  FROM information_schema.STATISTICS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 't_payment_order'
    AND INDEX_NAME = 'idx_payment_order_business'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @ddl := (
  SELECT IF(COUNT(*) = 0,
    'ALTER TABLE `t_payment_order` ADD KEY `idx_payment_order_user_created` (`user_id`, `created_at`)',
    'SELECT ''skip t_payment_order.idx_payment_order_user_created'''
  )
  FROM information_schema.STATISTICS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 't_payment_order'
    AND INDEX_NAME = 'idx_payment_order_user_created'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @ddl := (
  SELECT IF(COUNT(*) = 0,
    'ALTER TABLE `t_member_order` ADD COLUMN `channel` VARCHAR(16) DEFAULT NULL COMMENT ''支付渠道 MINIAPP/H5''',
    'SELECT ''skip t_member_order.channel'''
  )
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 't_member_order'
    AND COLUMN_NAME = 'channel'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @ddl := (
  SELECT IF(COUNT(*) = 0,
    'ALTER TABLE `t_member_order` ADD COLUMN `prepay_id` VARCHAR(128) DEFAULT NULL COMMENT ''微信预支付 id''',
    'SELECT ''skip t_member_order.prepay_id'''
  )
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 't_member_order'
    AND COLUMN_NAME = 'prepay_id'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @ddl := (
  SELECT IF(COUNT(*) = 0,
    'ALTER TABLE `t_member_order` ADD COLUMN `version` INT NOT NULL DEFAULT 0 COMMENT ''乐观锁版本号''',
    'SELECT ''skip t_member_order.version'''
  )
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 't_member_order'
    AND COLUMN_NAME = 'version'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
