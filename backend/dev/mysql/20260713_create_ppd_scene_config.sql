SET NAMES utf8mb4;
SET time_zone = '+08:00';

CREATE TABLE IF NOT EXISTS `t_ppd_scene_config` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `scene` varchar(32) NOT NULL COMMENT '拍豆使用场景编码',
  `original_ppd` int(11) NOT NULL DEFAULT 0 COMMENT '基础消耗拍豆',
  `description` varchar(256) DEFAULT NULL COMMENT '中文说明',
  `version` bigint(20) NOT NULL DEFAULT 0 COMMENT '乐观锁版本',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_ppd_scene` (`scene`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='拍豆场景配置表';

INSERT INTO `t_ppd_scene_config` (`scene`, `original_ppd`, `description`, `version`, `updated_at`)
SELECT 'SUBMIT_YPAT', 3, '发布约拍基础消耗', 0, NOW() FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `t_ppd_scene_config` WHERE `scene` = 'SUBMIT_YPAT');
INSERT INTO `t_ppd_scene_config` (`scene`, `original_ppd`, `description`, `version`, `updated_at`)
SELECT 'APPLY_YPAT', 3, '发起约拍申请基础消耗', 0, NOW() FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `t_ppd_scene_config` WHERE `scene` = 'APPLY_YPAT');
INSERT INTO `t_ppd_scene_config` (`scene`, `original_ppd`, `description`, `version`, `updated_at`)
SELECT 'VIEW_CONTACT', 3, '查看联系方式基础消耗', 0, NOW() FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `t_ppd_scene_config` WHERE `scene` = 'VIEW_CONTACT');

ALTER TABLE `t_record`
  ADD COLUMN `scene` varchar(32) DEFAULT NULL COMMENT '拍豆使用场景编码',
  ADD COLUMN `description` varchar(256) DEFAULT NULL COMMENT '拍豆流水中文说明';

INSERT INTO `t_member_benefit_rule`
  (`level_code`, `scene`, `benefit_type`, `discount_ppd`, `min_actual_ppd`, `effective`, `status`, `description`, `updated_at`)
SELECT levels.level_code, scenes.scene, 'PPD_DISCOUNT', 0, 0, '1', '1',
       CONCAT(scenes.scene_name, levels.level_name, '默认减免'), NOW()
FROM (
  SELECT 'BASIC' AS level_code, '基础会员' AS level_name
  UNION ALL SELECT 'PLUS', '高级会员'
  UNION ALL SELECT 'PRO', '专业会员'
) levels
CROSS JOIN (
  SELECT 'SUBMIT_YPAT' AS scene, '发布约拍' AS scene_name
  UNION ALL SELECT 'APPLY_YPAT', '发起约拍申请'
  UNION ALL SELECT 'VIEW_CONTACT', '查看联系方式'
) scenes
WHERE NOT (levels.level_code = 'BASIC' AND scenes.scene = 'SUBMIT_YPAT')
  AND NOT EXISTS (
    SELECT 1 FROM `t_member_benefit_rule` existing
    WHERE existing.`level_code` = levels.level_code
      AND existing.`scene` = scenes.scene
      AND existing.`benefit_type` = 'PPD_DISCOUNT'
  );
