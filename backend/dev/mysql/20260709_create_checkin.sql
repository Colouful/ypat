CREATE TABLE IF NOT EXISTS `t_checkin_rule` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `enabled` varchar(1) NOT NULL DEFAULT '1' COMMENT '是否启用: 1启用 0关闭',
  `reward_ppd` int(11) NOT NULL DEFAULT 1 COMMENT '每日奖励拍豆数',
  `confirm_title` varchar(64) NOT NULL DEFAULT '每日签到' COMMENT '确认弹窗标题',
  `confirm_content` varchar(256) NOT NULL DEFAULT '签到成功可获得 1 拍豆' COMMENT '确认弹窗内容',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='签到规则表';

CREATE TABLE IF NOT EXISTS `t_checkin_record` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `userid` bigint(20) NOT NULL COMMENT '用户ID',
  `checkin_date` varchar(10) NOT NULL COMMENT '签到日期, Asia/Shanghai, yyyy-MM-dd',
  `reward_ppd` int(11) NOT NULL DEFAULT 0 COMMENT '当次奖励拍豆数',
  `record_id` bigint(20) DEFAULT NULL COMMENT '对应t_record.id',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uniq_user_checkin_date` (`userid`, `checkin_date`),
  KEY `idx_checkin_date` (`checkin_date`),
  KEY `idx_checkin_userid` (`userid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='签到记录表';

INSERT INTO `t_checkin_rule` (`id`, `enabled`, `reward_ppd`, `confirm_title`, `confirm_content`, `created_at`, `updated_at`)
SELECT 1, '1', 1, '每日签到', '签到成功可获得 1 拍豆', NOW(), NOW()
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `t_checkin_rule` WHERE `id` = 1);
