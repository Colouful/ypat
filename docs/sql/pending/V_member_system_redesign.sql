-- ===========================================================================
-- YPAT 会员系统重设计 — 待执行 SQL (V_member_system_redesign)
-- ===========================================================================
-- 幂等说明：
--   * CREATE TABLE IF NOT EXISTS — 重复执行不会破坏已有结构
--   * INSERT ... ON DUPLICATE KEY UPDATE — 重复执行种子数据不抛错
--   * ADD COLUMN / MODIFY COLUMN / ADD INDEX 使用 information_schema + PREPARE 守卫，
--     支持列不存在时创建、列已存在时收敛到目标定义
-- ===========================================================================

SET NAMES utf8mb4;
SET time_zone = '+08:00';

SET @ddl := (
  SELECT IF(COUNT(*) = 0,
    'ALTER TABLE `t_member_plan` ADD COLUMN `gift_ppd` INT DEFAULT 0 COMMENT ''开通赠送拍拍豆''',
    'SELECT ''skip t_member_plan.gift_ppd'''
  )
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 't_member_plan'
    AND COLUMN_NAME = 'gift_ppd'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @ddl := (
  SELECT IF(COUNT(*) = 0,
    'ALTER TABLE `t_member_plan` ADD COLUMN `level_code` VARCHAR(16) NOT NULL DEFAULT ''BASIC'' COMMENT ''绑定会员等级''',
    'SELECT ''skip t_member_plan.level_code'''
  )
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 't_member_plan'
    AND COLUMN_NAME = 'level_code'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @ddl := (
  SELECT IF(COUNT(*) = 0,
    'ALTER TABLE `t_member_plan` ADD COLUMN `recommended` VARCHAR(1) NOT NULL DEFAULT ''0'' COMMENT ''0 否 1 是''',
    'SELECT ''skip t_member_plan.recommended'''
  )
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 't_member_plan'
    AND COLUMN_NAME = 'recommended'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @ddl := (
  SELECT IF(COUNT(*) = 0,
    'ALTER TABLE `t_member_order` ADD COLUMN `plan_name_snapshot` VARCHAR(64) DEFAULT NULL COMMENT ''套餐名称快照''',
    'SELECT ''skip t_member_order.plan_name_snapshot'''
  )
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 't_member_order'
    AND COLUMN_NAME = 'plan_name_snapshot'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @ddl := (
  SELECT IF(COUNT(*) = 0,
    'ALTER TABLE `t_member_order` ADD COLUMN `level_code_snapshot` VARCHAR(16) DEFAULT NULL COMMENT ''等级快照''',
    'SELECT ''skip t_member_order.level_code_snapshot'''
  )
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 't_member_order'
    AND COLUMN_NAME = 'level_code_snapshot'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @ddl := (
  SELECT IF(COUNT(*) = 0,
    'ALTER TABLE `t_member_order` ADD COLUMN `origin_price_fen` INT DEFAULT NULL COMMENT ''划线价快照''',
    'SELECT ''skip t_member_order.origin_price_fen'''
  )
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 't_member_order'
    AND COLUMN_NAME = 'origin_price_fen'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @ddl := (
  SELECT IF(COUNT(*) = 0,
    'ALTER TABLE `t_member_order` ADD COLUMN `gift_ppd` INT DEFAULT 0 COMMENT ''赠送拍拍豆快照''',
    'SELECT ''skip t_member_order.gift_ppd'''
  )
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 't_member_order'
    AND COLUMN_NAME = 'gift_ppd'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @ddl := (
  SELECT IF(COUNT(*) = 1,
    'ALTER TABLE `t_member_plan` MODIFY COLUMN `gift_ppd` INT DEFAULT 0 COMMENT ''开通赠送拍拍豆''',
    'SELECT ''skip normalize t_member_plan.gift_ppd'''
  )
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 't_member_plan'
    AND COLUMN_NAME = 'gift_ppd'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @ddl := (
  SELECT IF(COUNT(*) = 1,
    'UPDATE `t_member_plan` SET `level_code` = ''BASIC'' WHERE `level_code` IS NULL',
    'SELECT ''skip backfill t_member_plan.level_code'''
  )
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 't_member_plan'
    AND COLUMN_NAME = 'level_code'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @ddl := (
  SELECT IF(COUNT(*) = 1,
    'ALTER TABLE `t_member_plan` MODIFY COLUMN `level_code` VARCHAR(16) NOT NULL DEFAULT ''BASIC'' COMMENT ''绑定会员等级''',
    'SELECT ''skip normalize t_member_plan.level_code'''
  )
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 't_member_plan'
    AND COLUMN_NAME = 'level_code'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @ddl := (
  SELECT IF(COUNT(*) = 1,
    'UPDATE `t_member_plan` SET `recommended` = ''0'' WHERE `recommended` IS NULL',
    'SELECT ''skip backfill t_member_plan.recommended'''
  )
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 't_member_plan'
    AND COLUMN_NAME = 'recommended'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @ddl := (
  SELECT IF(COUNT(*) = 1,
    'ALTER TABLE `t_member_plan` MODIFY COLUMN `recommended` VARCHAR(1) NOT NULL DEFAULT ''0'' COMMENT ''0 否 1 是''',
    'SELECT ''skip normalize t_member_plan.recommended'''
  )
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 't_member_plan'
    AND COLUMN_NAME = 'recommended'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @ddl := (
  SELECT IF(COUNT(*) = 1,
    'ALTER TABLE `t_member_order` MODIFY COLUMN `plan_name_snapshot` VARCHAR(64) DEFAULT NULL COMMENT ''套餐名称快照''',
    'SELECT ''skip normalize t_member_order.plan_name_snapshot'''
  )
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 't_member_order'
    AND COLUMN_NAME = 'plan_name_snapshot'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @ddl := (
  SELECT IF(COUNT(*) = 1,
    'ALTER TABLE `t_member_order` MODIFY COLUMN `level_code_snapshot` VARCHAR(16) DEFAULT NULL COMMENT ''等级快照''',
    'SELECT ''skip normalize t_member_order.level_code_snapshot'''
  )
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 't_member_order'
    AND COLUMN_NAME = 'level_code_snapshot'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @ddl := (
  SELECT IF(COUNT(*) = 1,
    'ALTER TABLE `t_member_order` MODIFY COLUMN `origin_price_fen` INT DEFAULT NULL COMMENT ''划线价快照''',
    'SELECT ''skip normalize t_member_order.origin_price_fen'''
  )
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 't_member_order'
    AND COLUMN_NAME = 'origin_price_fen'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @ddl := (
  SELECT IF(COUNT(*) = 1,
    'ALTER TABLE `t_member_order` MODIFY COLUMN `gift_ppd` INT DEFAULT 0 COMMENT ''赠送拍拍豆快照''',
    'SELECT ''skip normalize t_member_order.gift_ppd'''
  )
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 't_member_order'
    AND COLUMN_NAME = 'gift_ppd'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

CREATE TABLE IF NOT EXISTS `t_member_benefit_rule` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `level_code` VARCHAR(16) NOT NULL,
  `scene` VARCHAR(32) NOT NULL,
  `benefit_type` VARCHAR(32) NOT NULL,
  `discount_ppd` INT NOT NULL DEFAULT 0,
  `min_actual_ppd` INT NOT NULL DEFAULT 0,
  `effective` VARCHAR(1) NOT NULL DEFAULT '0',
  `status` VARCHAR(1) NOT NULL DEFAULT '1',
  `description` VARCHAR(256) DEFAULT NULL,
  `updated_at` DATETIME DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_level_scene_type` (`level_code`, `scene`, `benefit_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='会员权益规则';

CREATE TABLE IF NOT EXISTS `t_member_operation_log` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL,
  `operator_id` BIGINT DEFAULT NULL,
  `action_type` VARCHAR(32) NOT NULL,
  `reason` VARCHAR(256) DEFAULT NULL,
  `before_value` VARCHAR(1024) DEFAULT NULL,
  `after_value` VARCHAR(1024) DEFAULT NULL,
  `source_order_no` VARCHAR(64) DEFAULT NULL,
  `created_at` DATETIME NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_user_created_at` (`user_id`, `created_at`),
  KEY `idx_operator_created_at` (`operator_id`, `created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='会员操作日志';

DROP PROCEDURE IF EXISTS `ypat_member_ensure_column`;
DELIMITER //
CREATE PROCEDURE `ypat_member_ensure_column`(
  IN p_table_name VARCHAR(64),
  IN p_column_name VARCHAR(64),
  IN p_add_sql TEXT,
  IN p_backfill_sql TEXT,
  IN p_modify_sql TEXT
)
BEGIN
  DECLARE column_count INT DEFAULT 0;

  SELECT COUNT(*) INTO column_count
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = p_table_name
    AND COLUMN_NAME = p_column_name;

  IF column_count = 0 THEN
    SET @ddl := p_add_sql;
    PREPARE stmt FROM @ddl;
    EXECUTE stmt;
    DEALLOCATE PREPARE stmt;
  END IF;

  IF p_backfill_sql IS NOT NULL AND p_backfill_sql <> '' THEN
    SET @ddl := p_backfill_sql;
    PREPARE stmt FROM @ddl;
    EXECUTE stmt;
    DEALLOCATE PREPARE stmt;
  END IF;

  SET @ddl := p_modify_sql;
  PREPARE stmt FROM @ddl;
  EXECUTE stmt;
  DEALLOCATE PREPARE stmt;
END//
DELIMITER ;

CALL `ypat_member_ensure_column`(
  't_member_benefit_rule',
  'level_code',
  'ALTER TABLE `t_member_benefit_rule` ADD COLUMN `level_code` VARCHAR(16) NOT NULL DEFAULT ''BASIC''',
  'UPDATE `t_member_benefit_rule` SET `level_code` = ''BASIC'' WHERE `level_code` IS NULL',
  'ALTER TABLE `t_member_benefit_rule` MODIFY COLUMN `level_code` VARCHAR(16) NOT NULL'
);

CALL `ypat_member_ensure_column`(
  't_member_benefit_rule',
  'scene',
  'ALTER TABLE `t_member_benefit_rule` ADD COLUMN `scene` VARCHAR(32) NOT NULL DEFAULT ''SUBMIT_YPAT''',
  'UPDATE `t_member_benefit_rule` SET `scene` = ''SUBMIT_YPAT'' WHERE `scene` IS NULL',
  'ALTER TABLE `t_member_benefit_rule` MODIFY COLUMN `scene` VARCHAR(32) NOT NULL'
);

CALL `ypat_member_ensure_column`(
  't_member_benefit_rule',
  'benefit_type',
  'ALTER TABLE `t_member_benefit_rule` ADD COLUMN `benefit_type` VARCHAR(32) NOT NULL DEFAULT ''PPD_DISCOUNT''',
  'UPDATE `t_member_benefit_rule` SET `benefit_type` = ''PPD_DISCOUNT'' WHERE `benefit_type` IS NULL',
  'ALTER TABLE `t_member_benefit_rule` MODIFY COLUMN `benefit_type` VARCHAR(32) NOT NULL'
);

CALL `ypat_member_ensure_column`(
  't_member_benefit_rule',
  'discount_ppd',
  'ALTER TABLE `t_member_benefit_rule` ADD COLUMN `discount_ppd` INT NOT NULL DEFAULT 0',
  'UPDATE `t_member_benefit_rule` SET `discount_ppd` = 0 WHERE `discount_ppd` IS NULL',
  'ALTER TABLE `t_member_benefit_rule` MODIFY COLUMN `discount_ppd` INT NOT NULL DEFAULT 0'
);

CALL `ypat_member_ensure_column`(
  't_member_benefit_rule',
  'min_actual_ppd',
  'ALTER TABLE `t_member_benefit_rule` ADD COLUMN `min_actual_ppd` INT NOT NULL DEFAULT 0',
  'UPDATE `t_member_benefit_rule` SET `min_actual_ppd` = 0 WHERE `min_actual_ppd` IS NULL',
  'ALTER TABLE `t_member_benefit_rule` MODIFY COLUMN `min_actual_ppd` INT NOT NULL DEFAULT 0'
);

CALL `ypat_member_ensure_column`(
  't_member_benefit_rule',
  'effective',
  'ALTER TABLE `t_member_benefit_rule` ADD COLUMN `effective` VARCHAR(1) NOT NULL DEFAULT ''0''',
  'UPDATE `t_member_benefit_rule` SET `effective` = ''0'' WHERE `effective` IS NULL',
  'ALTER TABLE `t_member_benefit_rule` MODIFY COLUMN `effective` VARCHAR(1) NOT NULL DEFAULT ''0'''
);

CALL `ypat_member_ensure_column`(
  't_member_benefit_rule',
  'status',
  'ALTER TABLE `t_member_benefit_rule` ADD COLUMN `status` VARCHAR(1) NOT NULL DEFAULT ''1''',
  'UPDATE `t_member_benefit_rule` SET `status` = ''1'' WHERE `status` IS NULL',
  'ALTER TABLE `t_member_benefit_rule` MODIFY COLUMN `status` VARCHAR(1) NOT NULL DEFAULT ''1'''
);

CALL `ypat_member_ensure_column`(
  't_member_benefit_rule',
  'description',
  'ALTER TABLE `t_member_benefit_rule` ADD COLUMN `description` VARCHAR(256) DEFAULT NULL',
  '',
  'ALTER TABLE `t_member_benefit_rule` MODIFY COLUMN `description` VARCHAR(256) DEFAULT NULL'
);

CALL `ypat_member_ensure_column`(
  't_member_benefit_rule',
  'updated_at',
  'ALTER TABLE `t_member_benefit_rule` ADD COLUMN `updated_at` DATETIME DEFAULT NULL',
  '',
  'ALTER TABLE `t_member_benefit_rule` MODIFY COLUMN `updated_at` DATETIME DEFAULT NULL'
);

CALL `ypat_member_ensure_column`(
  't_member_operation_log',
  'user_id',
  'ALTER TABLE `t_member_operation_log` ADD COLUMN `user_id` BIGINT NOT NULL DEFAULT 0',
  'UPDATE `t_member_operation_log` SET `user_id` = 0 WHERE `user_id` IS NULL',
  'ALTER TABLE `t_member_operation_log` MODIFY COLUMN `user_id` BIGINT NOT NULL'
);

CALL `ypat_member_ensure_column`(
  't_member_operation_log',
  'operator_id',
  'ALTER TABLE `t_member_operation_log` ADD COLUMN `operator_id` BIGINT DEFAULT NULL',
  '',
  'ALTER TABLE `t_member_operation_log` MODIFY COLUMN `operator_id` BIGINT DEFAULT NULL'
);

CALL `ypat_member_ensure_column`(
  't_member_operation_log',
  'action_type',
  'ALTER TABLE `t_member_operation_log` ADD COLUMN `action_type` VARCHAR(32) NOT NULL DEFAULT ''UNKNOWN''',
  'UPDATE `t_member_operation_log` SET `action_type` = ''UNKNOWN'' WHERE `action_type` IS NULL',
  'ALTER TABLE `t_member_operation_log` MODIFY COLUMN `action_type` VARCHAR(32) NOT NULL'
);

CALL `ypat_member_ensure_column`(
  't_member_operation_log',
  'reason',
  'ALTER TABLE `t_member_operation_log` ADD COLUMN `reason` VARCHAR(256) DEFAULT NULL',
  '',
  'ALTER TABLE `t_member_operation_log` MODIFY COLUMN `reason` VARCHAR(256) DEFAULT NULL'
);

CALL `ypat_member_ensure_column`(
  't_member_operation_log',
  'before_value',
  'ALTER TABLE `t_member_operation_log` ADD COLUMN `before_value` VARCHAR(1024) DEFAULT NULL',
  '',
  'ALTER TABLE `t_member_operation_log` MODIFY COLUMN `before_value` VARCHAR(1024) DEFAULT NULL'
);

CALL `ypat_member_ensure_column`(
  't_member_operation_log',
  'after_value',
  'ALTER TABLE `t_member_operation_log` ADD COLUMN `after_value` VARCHAR(1024) DEFAULT NULL',
  '',
  'ALTER TABLE `t_member_operation_log` MODIFY COLUMN `after_value` VARCHAR(1024) DEFAULT NULL'
);

CALL `ypat_member_ensure_column`(
  't_member_operation_log',
  'source_order_no',
  'ALTER TABLE `t_member_operation_log` ADD COLUMN `source_order_no` VARCHAR(64) DEFAULT NULL',
  '',
  'ALTER TABLE `t_member_operation_log` MODIFY COLUMN `source_order_no` VARCHAR(64) DEFAULT NULL'
);

CALL `ypat_member_ensure_column`(
  't_member_operation_log',
  'created_at',
  'ALTER TABLE `t_member_operation_log` ADD COLUMN `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP',
  'UPDATE `t_member_operation_log` SET `created_at` = NOW() WHERE `created_at` IS NULL',
  'ALTER TABLE `t_member_operation_log` MODIFY COLUMN `created_at` DATETIME NOT NULL'
);

DROP PROCEDURE IF EXISTS `ypat_member_ensure_column`;

SET @ddl := (
  SELECT IF(COUNT(*) = 0,
    'ALTER TABLE `t_member_benefit_rule` ADD UNIQUE KEY `uk_level_scene_type` (`level_code`, `scene`, `benefit_type`)',
    'SELECT ''skip index t_member_benefit_rule.uk_level_scene_type'''
  )
  FROM information_schema.STATISTICS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 't_member_benefit_rule'
    AND INDEX_NAME = 'uk_level_scene_type'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @ddl := (
  SELECT IF(COUNT(*) = 0,
    'ALTER TABLE `t_member_operation_log` ADD INDEX `idx_user_created_at` (`user_id`, `created_at`)',
    'SELECT ''skip index t_member_operation_log.idx_user_created_at'''
  )
  FROM information_schema.STATISTICS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 't_member_operation_log'
    AND INDEX_NAME = 'idx_user_created_at'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @ddl := (
  SELECT IF(COUNT(*) = 0,
    'ALTER TABLE `t_member_operation_log` ADD INDEX `idx_operator_created_at` (`operator_id`, `created_at`)',
    'SELECT ''skip index t_member_operation_log.idx_operator_created_at'''
  )
  FROM information_schema.STATISTICS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 't_member_operation_log'
    AND INDEX_NAME = 'idx_operator_created_at'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

INSERT INTO `t_member_benefit_rule`
  (`level_code`, `scene`, `benefit_type`, `discount_ppd`, `min_actual_ppd`, `effective`, `status`, `description`, `updated_at`)
VALUES
  ('BASIC', 'SUBMIT_YPAT', 'PPD_DISCOUNT', 2, 0, '1', '1', '提交约拍会员减免', NOW())
ON DUPLICATE KEY UPDATE `updated_at` = NOW();
