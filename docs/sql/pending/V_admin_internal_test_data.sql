-- ===========================================================================
-- YPAT 内测数据资源与数据标记 — 待执行 SQL (V_admin_internal_test_data)
-- ===========================================================================
-- 幂等说明：
--   * CREATE TABLE IF NOT EXISTS — 重复执行不会破坏已有资源表
--   * ADD COLUMN 使用 information_schema + PREPARE 守卫，列不存在时才添加
-- ===========================================================================

SET NAMES utf8mb4;
SET time_zone = '+08:00';

CREATE TABLE IF NOT EXISTS `t_internal_test_resource` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
  `media_type` VARCHAR(16) NOT NULL COMMENT '媒体类型：image/video',
  `usage_type` VARCHAR(16) NOT NULL COMMENT '用途：avatar/ypat/work',
  `style_code` VARCHAR(64) DEFAULT NULL COMMENT '风格编码',
  `url` VARCHAR(1024) NOT NULL COMMENT '资源地址',
  `title` VARCHAR(128) DEFAULT NULL COMMENT '标题',
  `description` VARCHAR(512) DEFAULT NULL COMMENT '描述',
  `profession` VARCHAR(32) DEFAULT NULL COMMENT '适用身份',
  `city` VARCHAR(64) DEFAULT NULL COMMENT '适用城市',
  `status` VARCHAR(16) NOT NULL DEFAULT 'enabled' COMMENT '状态：enabled/disabled',
  `sort_no` INT DEFAULT 0 COMMENT '排序号',
  `remark` VARCHAR(512) DEFAULT NULL COMMENT '备注',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_internal_test_resource_lookup` (`usage_type`, `media_type`, `style_code`, `status`),
  KEY `idx_internal_test_resource_sort` (`sort_no`, `id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='内测资源池';

SET @ddl := (
  SELECT IF(COUNT(*) = 0,
    'ALTER TABLE `t_user` ADD COLUMN `data_flag` VARCHAR(32) NOT NULL DEFAULT ''real'' COMMENT ''数据标记：real/internal_test''',
    'SELECT ''skip t_user.data_flag'''
  )
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 't_user'
    AND COLUMN_NAME = 'data_flag'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @ddl := (
  SELECT IF(COUNT(*) = 0,
    'ALTER TABLE `t_user` ADD COLUMN `internal_batch_no` VARCHAR(64) DEFAULT NULL COMMENT ''内测批次号''',
    'SELECT ''skip t_user.internal_batch_no'''
  )
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 't_user'
    AND COLUMN_NAME = 'internal_batch_no'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @ddl := (
  SELECT IF(COUNT(*) = 0,
    'ALTER TABLE `t_ypat_info` ADD COLUMN `data_flag` VARCHAR(32) NOT NULL DEFAULT ''real'' COMMENT ''数据标记：real/internal_test''',
    'SELECT ''skip t_ypat_info.data_flag'''
  )
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 't_ypat_info'
    AND COLUMN_NAME = 'data_flag'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @ddl := (
  SELECT IF(COUNT(*) = 0,
    'ALTER TABLE `t_ypat_info` ADD COLUMN `internal_batch_no` VARCHAR(64) DEFAULT NULL COMMENT ''内测批次号''',
    'SELECT ''skip t_ypat_info.internal_batch_no'''
  )
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 't_ypat_info'
    AND COLUMN_NAME = 'internal_batch_no'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @ddl := (
  SELECT IF(COUNT(*) = 0,
    'ALTER TABLE `t_work` ADD COLUMN `data_flag` VARCHAR(32) NOT NULL DEFAULT ''real'' COMMENT ''数据标记：real/internal_test''',
    'SELECT ''skip t_work.data_flag'''
  )
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 't_work'
    AND COLUMN_NAME = 'data_flag'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @ddl := (
  SELECT IF(COUNT(*) = 0,
    'ALTER TABLE `t_work` ADD COLUMN `internal_batch_no` VARCHAR(64) DEFAULT NULL COMMENT ''内测批次号''',
    'SELECT ''skip t_work.internal_batch_no'''
  )
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 't_work'
    AND COLUMN_NAME = 'internal_batch_no'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
