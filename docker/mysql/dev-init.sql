-- YPAT development MySQL init script
-- 创建业务账号（仅开发环境使用）
--
-- 强制会话字符集为 utf8mb4，防止 mysql 客户端默认 latin1 导致中文 double-encoded。
SET NAMES utf8mb4;
--
-- 注意事项：
-- 1. 必须显式指定 mysql_native_password 认证插件：
--    项目用 Spring Boot 1.5.9 + mysql-connector-java 5.1.x，不支持
--    MySQL 8 默认的 caching_sha2_password 插件，否则连接时会报
--    "Unable to load authentication plugin 'caching_sha2_password'"
-- 2. dev 账号必须有 DDL 权限：
--    Hibernate 配置 ddl-auto=update，启动时会自动建表 / 加字段，
--    只授 DML 权限（SELECT/INSERT/UPDATE/DELETE）会导致启动失败：
--    "Error creating bean 'entityManagerFactory' ... Table not found"

CREATE USER IF NOT EXISTS 'ypat_dev'@'%'
    IDENTIFIED WITH mysql_native_password BY 'ypat_dev_password_change_me';

ALTER USER 'ypat_dev'@'%'
    IDENTIFIED WITH mysql_native_password BY 'ypat_dev_password_change_me';

GRANT SELECT, INSERT, UPDATE, DELETE, CREATE, ALTER, DROP, INDEX, REFERENCES
    ON ypat.* TO 'ypat_dev'@'%';

FLUSH PRIVILEGES;