-- YPAT 本地开发种子数据（可选，独立脚本）
--
-- 强制会话字符集为 utf8mb4，防止 mysql 客户端默认 latin1 导致中文 double-encoded。
SET NAMES utf8mb4;
--
-- ⚠️ 执行时机：必须在 restapi 首次启动、Hibernate ddl-auto=update 建好业务表之后。
--    docker-entrypoint-initdb.d/*.sql 是 MySQL 首次启动跑，那时候业务表还没建，
--    所以本文件 **不能** 挂进 initdb.d，只能作为独立脚本手动执行：
--
--    docker exec -i ypat-workspace-mysql-1 \
--        mysql -uroot -proot ypat < docker/mysql/dev-seed.sql
--
--    重复执行安全：大多数 INSERT 使用 INSERT IGNORE + 固定主键 id；
--    t_product 会通过 ON DUPLICATE KEY UPDATE 收敛到当前拍豆充值套餐配置。
--
-- ⚠️ 安全约束：仅本地/开发环境使用。禁止在 staging/production 执行。
--
-- 内置账号（管理后台登录）：
--   手机号：13800000000
--   密码 ：admin@123
--   MD5(admin@123).toUpperCase() = E6E061838856BF47E1DE730719FB2609
--
--   注意：当前 t_user 表无 role/is_admin 字段，任何用户都能登管理后台，
--         这本身是安全隐患，见 IMPROVEMENT.md "已知待解决问题" 待办。

-- ---------- 1. 管理员用户 ----------
INSERT IGNORE INTO t_user (
    id, mobile, password, name, nickname,
    status, regisdate, realnameflag, creditflag, ppd
) VALUES (
    1,
    '13800000000',
    'E6E061838856BF47E1DE730719FB2609',
    'Admin',
    '管理员',
    'zc',
    NOW(),
    'yes',
    'yes',
    0
);

-- ---------- 2. Banner ----------
INSERT IGNORE INTO t_banner (id, title, imgpath, status, userid, credate) VALUES
    (1, '欢迎使用 YPAT',        'https://picsum.photos/seed/banner1/1200/400', 'up',   1, NOW()),
    (2, '找到心仪的摄影师',      'https://picsum.photos/seed/banner2/1200/400', 'up',   1, NOW()),
    (3, '春季约拍优惠中',        'https://picsum.photos/seed/banner3/1200/400', 'down', 1, NOW());

-- ---------- 3. Product ----------
SET @product_recommended_exists := (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 't_product'
      AND COLUMN_NAME = 'recommended'
);
SET @product_recommended_sql := IF(
    @product_recommended_exists = 0,
    'ALTER TABLE t_product ADD COLUMN recommended varchar(1) DEFAULT ''0''',
    'SELECT 1'
);
PREPARE product_recommended_stmt FROM @product_recommended_sql;
EXECUTE product_recommended_stmt;
DEALLOCATE PREPARE product_recommended_stmt;

INSERT INTO t_product (id, name, currval, oldval, status, recommended) VALUES
    (1, '10拍豆',  10,  990, '0', '0'),
    (2, '30拍豆',  30, 2690, '0', '1'),
    (3, '60拍豆',  60, 4990, '0', '0'),
    (4, '100拍豆', 100, 7990, '0', '0')
ON DUPLICATE KEY UPDATE
    name = VALUES(name),
    currval = VALUES(currval),
    oldval = VALUES(oldval),
    status = VALUES(status),
    recommended = VALUES(recommended);

-- ---------- 4. Article ----------
INSERT IGNORE INTO t_article (id, title, describ, content, imgpath, plat, flag, status, readtimes, credate, userid) VALUES
    (1, '10 分钟看懂约拍流程',    '新手必读',  '内容略...',
        'https://picsum.photos/seed/article1/800/400',
        'h5', 'notice', 'up',  0, NOW(), 1),
    (2, '2026 春季新玩法',        '春季推荐', '内容略...',
        'https://picsum.photos/seed/article2/800/400',
        'h5', 'topic',  'up',  0, NOW(), 1),
    (3, '如何选到靠谱的摄影师',   '选摄指南', '内容略...',
        'https://picsum.photos/seed/article3/800/400',
        'h5', 'guide',  'up',  0, NOW(), 1);

-- ---------- 5. 校验（帮助运维/开发确认） ----------
SELECT 'seed done'         AS status,
       (SELECT COUNT(*) FROM t_user)    AS users,
       (SELECT COUNT(*) FROM t_banner)  AS banners,
       (SELECT COUNT(*) FROM t_product) AS products,
       (SELECT COUNT(*) FROM t_article) AS articles;
