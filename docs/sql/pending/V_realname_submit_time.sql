-- 实名认证最近提交时间：历史数据保持 NULL，不做不准确回填。
SET @ddl := (
  SELECT IF(COUNT(*) = 0,
    'ALTER TABLE `t_user` ADD COLUMN `realname_submit_at` DATETIME NULL COMMENT ''实名认证最近提交时间'' AFTER `regisdate`',
    'SELECT ''skip t_user.realname_submit_at'''
  )
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 't_user'
    AND COLUMN_NAME = 'realname_submit_at'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @ddl := (
  SELECT IF(COUNT(*) = 0,
    'ALTER TABLE `t_user` ADD INDEX `idx_user_realname_review` (`status`, `realname_submit_at`, `id`)',
    'SELECT ''skip idx_user_realname_review'''
  )
  FROM information_schema.STATISTICS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 't_user'
    AND INDEX_NAME = 'idx_user_realname_review'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
