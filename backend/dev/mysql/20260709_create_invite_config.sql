CREATE TABLE IF NOT EXISTS `t_invite_config` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `enabled` varchar(1) NOT NULL DEFAULT '1',
  `reward_ppd` int(11) NOT NULL DEFAULT 3,
  `rule_text` varchar(300) NOT NULL DEFAULT '好友通过你的邀请码注册后，自动到账 3 拍拍豆。',
  `share_title` varchar(120) NOT NULL DEFAULT '好友邀请你加入爱去拍，找摄影师、找模特更方便',
  `landing_title` varchar(160) NOT NULL DEFAULT '我正在使用爱去拍，找摄影师、找模特特别方便，推荐你也来体验。',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
