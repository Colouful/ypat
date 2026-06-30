-- ====================================================================
-- YPAT 会员模块 — 待执行 SQL (V_pending_member)
-- ====================================================================
-- 此文件待运维在生产 MySQL 上执行。当前仓库无 Flyway / Liquibase，
-- 新表由 Hibernate ddl-auto:update 自动创建；本脚本用于：
--   1. 校验生产库是否真的生成了 3 张表（项目 history 中曾因版本差漏建）
--   2. 补齐 ddl-auto 不会自动建的索引 / 唯一约束
--   3. 写入种子套餐数据
--
-- 执行人：DB 管理员
-- 关联 PR：#10（切片 2 邀请）/ #11（切片 3 会员）
-- ====================================================================

-- 1. 邀请关系表（PR #10）
CREATE TABLE IF NOT EXISTS `t_invite_relation` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `inviter_userid` BIGINT NOT NULL,
  `invitee_userid` BIGINT NOT NULL,
  `invite_code` VARCHAR(32) DEFAULT NULL,
  `source` VARCHAR(16) DEFAULT NULL,
  `reward_ppd` INT DEFAULT NULL,
  `credate` DATETIME DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_invitee_userid` (`invitee_userid`),
  KEY `idx_inviter_userid` (`inviter_userid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='邀请关系 — 一行 = 一个被邀请人';

-- 2. 会员套餐表（PR #11）
CREATE TABLE IF NOT EXISTS `t_member_plan` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `code` VARCHAR(32) NOT NULL,
  `name` VARCHAR(64) NOT NULL,
  `duration_days` INT NOT NULL,
  `price_fen` INT NOT NULL,
  `origin_price_fen` INT DEFAULT NULL,
  `benefits` VARCHAR(512) DEFAULT NULL,
  `status` VARCHAR(1) NOT NULL DEFAULT '1' COMMENT '0 下架 1 上架',
  `sort_no` INT DEFAULT NULL,
  `credate` DATETIME DEFAULT NULL,
  `updated_at` DATETIME DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='会员套餐';

-- 3. 用户会员状态表（PR #11）
CREATE TABLE IF NOT EXISTS `t_user_member` (
  `user_id` BIGINT NOT NULL,
  `level` VARCHAR(16) NOT NULL DEFAULT 'NONE',
  `expire_at` DATETIME NOT NULL,
  `source_order_no` VARCHAR(64) DEFAULT NULL,
  `updated_at` DATETIME DEFAULT NULL,
  PRIMARY KEY (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户会员状态（单行主键）';

-- 4. 会员订单表（PR #11）
CREATE TABLE IF NOT EXISTS `t_member_order` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `out_trade_no` VARCHAR(64) NOT NULL,
  `user_id` BIGINT NOT NULL,
  `plan_id` BIGINT NOT NULL,
  `plan_code` VARCHAR(32) NOT NULL,
  `price_fen` INT NOT NULL,
  `duration_days` INT NOT NULL,
  `status` VARCHAR(1) NOT NULL COMMENT '0 待支付 1 已支付 2 已取消 3 已退款',
  `wx_transaction_id` VARCHAR(64) DEFAULT NULL,
  `paid_at` DATETIME DEFAULT NULL,
  `credate` DATETIME DEFAULT NULL,
  `updated_at` DATETIME DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_out_trade_no` (`out_trade_no`),
  KEY `idx_user_status` (`user_id`, `status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='会员订单';

-- ====================================================================
-- 5. 种子套餐数据（按需启用；运营可随时通过管理后台修改）
-- ====================================================================
INSERT INTO `t_member_plan` (`code`, `name`, `duration_days`, `price_fen`, `origin_price_fen`, `benefits`, `status`, `sort_no`)
VALUES
  ('MONTH',   '包月会员',   30,  1980, 2980, '同城优先曝光、专属约拍标识、无限收藏', '1', 1),
  ('SEASON',  '包季会员',   90,  5880, 8940, '包月全部权益 + 摄影圈子入驻资格',   '1', 2),
  ('YEAR',    '包年会员',   365, 19800, 35760, '包季全部权益 + 一年 3 次免费咨询',  '1', 3)
ON DUPLICATE KEY UPDATE `updated_at` = NOW();

-- ====================================================================
-- 6. 状态机校验（可选，作为线上约定不依赖应用层）
-- ====================================================================
-- ALTER TABLE `t_member_order`
--   ADD CONSTRAINT `chk_status` CHECK (`status` IN ('0','1','2','3'));

-- ALTER TABLE `t_member_plan`
--   ADD CONSTRAINT `chk_plan_status` CHECK (`status` IN ('0','1'));

-- ====================================================================
-- 7. 历史回填（如有旧会员用户需要兼容）
-- ====================================================================
-- INSERT IGNORE INTO `t_user_member` (`user_id`, `level`, `expire_at`, `updated_at`)
--   SELECT id, 'BASIC', DATE_ADD(NOW(), INTERVAL 30 DAY), NOW()
--   FROM t_user
--   WHERE realnameflag = '1' AND regisdate < '2026-01-01';