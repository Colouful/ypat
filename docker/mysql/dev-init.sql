-- YPAT development MySQL init script
-- 创建业务账号（仅开发环境使用）

CREATE USER IF NOT EXISTS 'ypat_dev'@'%' IDENTIFIED BY 'ypat_dev_password_change_me';
GRANT SELECT, INSERT, UPDATE, DELETE ON ypat.* TO 'ypat_dev'@'%';
FLUSH PRIVILEGES;