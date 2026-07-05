-- ===========================================================================
-- YPAT 会员系统重设计 — 待执行 SQL (V_member_system_redesign)
-- ===========================================================================
-- 警告：下方 ALTER TABLE ADD COLUMN 段落不是重复执行安全的。
-- 执行前请操作人员确认 t_member_plan / t_member_order 中尚不存在对应列；
-- 若已存在任一列，请先人工调整脚本或跳过对应列，避免 MySQL 重复加列失败。
-- ===========================================================================

SET NAMES utf8mb4;
SET time_zone = '+08:00';

ALTER TABLE `t_member_plan`
  ADD COLUMN `gift_ppd` INT DEFAULT 0 COMMENT '开通赠送拍拍豆',
  ADD COLUMN `level_code` VARCHAR(16) NOT NULL DEFAULT 'BASIC' COMMENT '绑定会员等级',
  ADD COLUMN `recommended` VARCHAR(1) NOT NULL DEFAULT '0' COMMENT '0 否 1 是';

ALTER TABLE `t_member_order`
  ADD COLUMN `plan_name_snapshot` VARCHAR(64) DEFAULT NULL COMMENT '套餐名称快照',
  ADD COLUMN `level_code_snapshot` VARCHAR(16) DEFAULT NULL COMMENT '等级快照',
  ADD COLUMN `origin_price_fen` INT DEFAULT NULL COMMENT '划线价快照',
  ADD COLUMN `gift_ppd` INT DEFAULT 0 COMMENT '赠送拍拍豆快照';

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

INSERT INTO `t_member_benefit_rule`
  (`level_code`, `scene`, `benefit_type`, `discount_ppd`, `min_actual_ppd`, `effective`, `status`, `description`, `updated_at`)
VALUES
  ('BASIC', 'SUBMIT_YPAT', 'PPD_DISCOUNT', 2, 0, '1', '1', '提交约拍会员减免', NOW())
ON DUPLICATE KEY UPDATE `updated_at` = NOW();
