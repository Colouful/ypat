-- 实名认证审核费配置：默认 1 分，已存在字段时不覆盖后台配置。
SET @ddl := (
  SELECT IF(COUNT(*) = 0,
    'ALTER TABLE `t_deposit_config` ADD COLUMN `realname_audit_fee_fen` INT NOT NULL DEFAULT 1 COMMENT ''实名认证审核费（分）'' AFTER `display_amount_fen`',
    'SELECT ''skip t_deposit_config.realname_audit_fee_fen'''
  )
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 't_deposit_config'
    AND COLUMN_NAME = 'realname_audit_fee_fen'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

UPDATE `t_deposit_config`
SET `realname_audit_fee_fen` = 1
WHERE `realname_audit_fee_fen` IS NULL;

SET @ddl := (
  SELECT IF(COUNT(*) = 1,
    'ALTER TABLE `t_deposit_config` MODIFY COLUMN `realname_audit_fee_fen` INT NOT NULL DEFAULT 1 COMMENT ''实名认证审核费（分）''',
    'SELECT ''skip normalize t_deposit_config.realname_audit_fee_fen'''
  )
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 't_deposit_config'
    AND COLUMN_NAME = 'realname_audit_fee_fen'
    AND (IS_NULLABLE <> 'NO' OR COLUMN_DEFAULT IS NULL OR COLUMN_DEFAULT <> '1')
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
