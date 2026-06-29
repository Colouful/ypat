-- YPAT production MySQL init script (TEMPLATE ONLY)
--
-- 严禁部署此 init.sql 到任何位置，除非：
--   1. 已确认 production 数据库服务器
--   2. 已确认 YPAT_DB_USERNAME / YPAT_DB_PASSWORD 注入方式
--   3. 已确认 YPAT_LOCAL_MYSQL_ROOT_PASSWORD 注入方式
--
-- 业务账号策略：
--   - ypat_app：最小权限（SELECT/INSERT/UPDATE/DELETE）
--   - ypat_migrate：可执行 DDL（仅用于 schema 迁移）
--   - root：仅用于运维，不直接给应用使用

-- 业务账号（应用运行时使用）
CREATE USER IF NOT EXISTS 'ypat_app'@'%' IDENTIFIED BY 'CHANGE_ME_YPAT_APP_PASSWORD';
GRANT SELECT, INSERT, UPDATE, DELETE ON ypat.* TO 'ypat_app'@'%';

-- 迁移账号（仅 schema migration 使用）
CREATE USER IF NOT EXISTS 'ypat_migrate'@'%' IDENTIFIED BY 'CHANGE_ME_YPAT_MIGRATE_PASSWORD';
GRANT SELECT, INSERT, UPDATE, DELETE, CREATE, DROP, ALTER, INDEX, REFERENCES ON ypat.* TO 'ypat_migrate'@'%';

FLUSH PRIVILEGES;