-- YPAT 本地开发种子数据（可选，独立脚本）
--
-- ⚠️ 执行时机：必须在 restapi 首次启动、Hibernate ddl-auto=update 建好业务表之后。
--    docker-entrypoint-initdb.d/*.sql 是 MySQL 首次启动跑，那时候业务表还没建，
--    所以本文件 **不能** 挂进 initdb.d，只能作为独立脚本手动执行：
--
--    docker exec -i ypat-workspace-mysql-1 \
--        mysql -uroot -proot ypat < docker/mysql/dev-seed.sql
--
--    重复执行安全：所有 INSERT 都是 INSERT IGNORE + 固定主键 id，
--    重跑不会产生重复数据，也不会破坏已有内容。
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
INSERT IGNORE INTO t_product (id, name, currval, oldval, status) VALUES
    (1, '基础人像套餐（1小时）', 29900, 39900, 'up'),
    (2, '写真套餐（3小时）',    69900, 89900, 'up'),
    (3, '婚纱摄影套餐',        199900, 259900, 'up');

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
