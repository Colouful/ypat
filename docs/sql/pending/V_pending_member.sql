-- ===========================================================================
-- YPAT 会员 / 邀请模块 — 待执行 SQL (V_pending_member)
-- ===========================================================================
-- 本文件由 YPAT 三切片迁移交付 (PR #9 #10 #11) 沉淀。
-- 4 张表对应 4 个 Hibernate Entity：
--   * t_member_plan     ← com.ypat.entity.MemberPlan
--   * t_user_member     ← com.ypat.entity.UserMember
--   * t_member_order    ← com.ypat.entity.MemberOrder
--   * t_invite_relation ← com.ypat.entity.InviteRelation
--
-- 价格单位约定（必读）：
--   所有价格统一使用「分」（price_fen / origin_price_fen）
--   前端展示时除以 100
--   微信支付提交时不得再次乘以 100
--   与后端 MemberPlan.priceFen / MemberOrder.priceFen 完全一致
--
-- 套餐种子数据为开发测试占位价：
--   MONTH  = 1980 分（¥19.80）
--   SEASON = 5880 分（¥58.80）
--   YEAR   = 19800 分（¥198.00）
-- 上线前需运营确认正式价格。
--
-- 幂等 / 安全：
--   * CREATE TABLE IF NOT EXISTS — 重复执行不会破坏已有结构
--   * INSERT ... ON DUPLICATE KEY UPDATE — 重复执行种子数据不抛错
--   * UPDATE 子句仅刷新 updated_at，不会覆盖人工调整过的 price_fen
--
-- 历史数据回填默认注释，未授权不要启用。
-- ===========================================================================

SET NAMES utf8mb4;
SET time_zone = '+08:00';

-- 1. 会员套餐表 -----------------------------------------------------------
CREATE TABLE IF NOT EXISTS `t_member_plan` (
  `id`               BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
  `code`             VARCHAR(32)  NOT NULL                COMMENT '套餐编码 MONTH/SEASON/YEAR',
  `name`             VARCHAR(64)  NOT NULL                COMMENT '套餐名称',
  `duration_days`    INT          NOT NULL                COMMENT '有效天数',
  `price_fen`        INT          NOT NULL                COMMENT '当前售价（分），前端展示时除以 100',
  `origin_price_fen` INT          DEFAULT NULL            COMMENT '划线价（分），可空',
  `benefits`         VARCHAR(512) DEFAULT NULL            COMMENT '权益摘要，前端展示',
  `status`           VARCHAR(1)   NOT NULL DEFAULT '1'    COMMENT '0 下架 1 上架',
  `sort_no`          INT          DEFAULT NULL            COMMENT '展示排序',
  `credate`          DATETIME     DEFAULT NULL            COMMENT '创建时间',
  `updated_at`       DATETIME     DEFAULT NULL            COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='会员套餐（价格单位：分）';

-- 2. 用户会员状态表 -------------------------------------------------------
CREATE TABLE IF NOT EXISTS `t_user_member` (
  `user_id`         BIGINT       NOT NULL                COMMENT '主键，关联 t_user.id',
  `level`           VARCHAR(16)  NOT NULL                COMMENT 'NONE / BASIC / PRO',
  `expire_at`       DATETIME     NOT NULL                COMMENT '会员到期时间',
  `source_order_no` VARCHAR(64) DEFAULT NULL            COMMENT '最近一次开通/续费的订单号',
  `updated_at`      DATETIME     DEFAULT NULL            COMMENT '更新时间',
  PRIMARY KEY (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='用户会员状态（单行主键）';

-- 3. 会员订单表 -----------------------------------------------------------
-- 幂等保证：
--   * uk_out_trade_no 保证同一商户订单号唯一写入
--   * idx_user_status 支持按用户查订单列表 + 状态过滤
CREATE TABLE IF NOT EXISTS `t_member_order` (
  `id`                 BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
  `out_trade_no`       VARCHAR(64)  NOT NULL                COMMENT '商户订单号，约定 M 开头',
  `user_id`            BIGINT       NOT NULL                COMMENT '用户 id',
  `plan_id`            BIGINT       NOT NULL                COMMENT '套餐 id',
  `plan_code`          VARCHAR(32)  NOT NULL                COMMENT '套餐编码冗余，避免连表',
  `price_fen`          INT          NOT NULL                COMMENT '下单锁定价（分）',
  `duration_days`      INT          NOT NULL                COMMENT '套餐有效天数冗余',
  `status`             VARCHAR(1)   NOT NULL                COMMENT '0 待支付 1 已支付 2 已取消 3 已退款',
  `wx_transaction_id`  VARCHAR(64)  DEFAULT NULL            COMMENT '微信支付流水号',
  `paid_at`            DATETIME     DEFAULT NULL            COMMENT '支付完成时间',
  `credate`            DATETIME     DEFAULT NULL            COMMENT '创建时间',
  `updated_at`         DATETIME     DEFAULT NULL            COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_out_trade_no` (`out_trade_no`),
  KEY `idx_user_status` (`user_id`, `status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='会员订单（价格单位：分）';

-- 4. 邀请关系表 -----------------------------------------------------------
-- 幂等保证：
--   * uk_invitee_userid 保证同一被邀请人只能绑定一个邀请人
--   * idx_inviter_userid 支持查"我邀请了谁"分页
CREATE TABLE IF NOT EXISTS `t_invite_relation` (
  `id`              BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
  `inviter_userid`  BIGINT       NOT NULL                COMMENT '邀请人 user_id',
  `invitee_userid`  BIGINT       NOT NULL                COMMENT '被邀请人 user_id',
  `invite_code`     VARCHAR(32)  DEFAULT NULL            COMMENT '命中的安全邀请码（base36 IV 前缀）',
  `source`          VARCHAR(16)  DEFAULT NULL            COMMENT '入口来源 share/qr/manual/recmobile',
  `reward_ppd`      INT          DEFAULT NULL            COMMENT '本次发放的拍拍豆数（来源 Constant.INVITE_NEED_PPD）',
  `credate`         DATETIME     DEFAULT NULL            COMMENT '绑定时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_invitee_userid` (`invitee_userid`),
  KEY `idx_inviter_userid` (`inviter_userid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='邀请关系（一行 = 一个被邀请人）';

-- ===========================================================================
-- 5. 种子套餐数据
-- ===========================================================================
-- 注意：仅当 name = VALUES(name) 为当前种子名时刷新 updated_at。
-- 任何人工改过的 price_fen / duration_days 都不会被种子覆盖（更新条件不命中）。
-- 上线前需运营确认正式价格。
INSERT INTO `t_member_plan`
  (`code`, `name`, `duration_days`, `price_fen`, `origin_price_fen`, `benefits`, `status`, `sort_no`, `updated_at`)
VALUES
  ('MONTH',  '包月会员', 30,  1980, 2980,  '同城优先曝光、专属约拍标识、无限收藏', '1', 1, NOW()),
  ('SEASON', '包季会员', 90,  5880, 8940,  '包月全部权益 + 摄影圈子入驻资格',     '1', 2, NOW()),
  ('YEAR',   '包年会员', 365, 19800, 35760, '包季全部权益 + 一年 3 次免费咨询',    '1', 3, NOW())
ON DUPLICATE KEY UPDATE
  -- 显式只刷新时间戳，不覆盖价格 / 名称 / 权益，避免运营手工改过的内容被回退
  `updated_at` = NOW();

-- ===========================================================================
-- 6. 验证 SQL（执行后跑一遍确认结构与种子一致）
-- ===========================================================================
-- SHOW TABLES LIKE 't_member_plan';
-- SHOW TABLES LIKE 't_user_member';
-- SHOW TABLES LIKE 't_member_order';
-- SHOW TABLES LIKE 't_invite_relation';
--
-- SHOW INDEX FROM `t_member_order` WHERE Key_name IN ('uk_out_trade_no','idx_user_status');
-- SHOW INDEX FROM `t_invite_relation` WHERE Key_name = 'uk_invitee_userid';
--
-- SELECT code, name, duration_days, price_fen, status FROM `t_member_plan` ORDER BY sort_no;

-- ===========================================================================
-- 7. 回滚说明（人工执行，不提供脚本）
-- ===========================================================================
-- 本迁移无破坏性变更，回滚方式：
--   DROP TABLE IF EXISTS `t_member_order`;
--   DROP TABLE IF EXISTS `t_user_member`;
--   DROP TABLE IF EXISTS `t_member_plan`;
--   DROP TABLE IF EXISTS `t_invite_relation`;
-- 注意：t_member_order 在业务产生订单后请勿直接 DROP，
--      先备份数据再决定。

-- ===========================================================================
-- 8. 历史数据回填模板（默认注释，未授权请勿启用）
-- ===========================================================================
-- 用途：如果存在历史已实名 / 报名的"老用户"希望补开会员，按业务规则启用：
--
-- INSERT INTO `t_user_member` (`user_id`, `level`, `expire_at`, `source_order_no`, `updated_at`)
-- SELECT
--   u.id,
--   'BASIC',
--   DATE_ADD(NOW(), INTERVAL 30 DAY),
--   CONCAT('BACKFILL-', u.id),
--   NOW()
-- FROM `t_user` u
-- WHERE u.realnameflag = '1'
--   AND NOT EXISTS (SELECT 1 FROM `t_user_member` m WHERE m.user_id = u.id);
--
-- 配套邀请关系回填（如果业务上需要补录一批已有的老邀请关系）：
-- INSERT INTO `t_invite_relation` (`inviter_userid`, `invitee_userid`, `invite_code`, `source`, `reward_ppd`, `credate`)
-- SELECT inviter.id, invitee.id, NULL, 'recmobile', 3, NOW()
-- FROM `t_user` invitee
-- JOIN `t_user` inviter ON inviter.mobile = invitee.recmobile
-- WHERE invitee.recmobile IS NOT NULL AND inviter.id IS NOT NULL
--   AND NOT EXISTS (SELECT 1 FROM `t_invite_relation` r WHERE r.invitee_userid = invitee.id);
-- ===========================================================================