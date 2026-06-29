# Open Items — STATEFUL_MIGRATION_REQUIRED 迁移计划

部署被门禁阻断,以下迁移计划需人工审批方可执行。**严禁自行实施。**

## 阻断条件总览

| # | 条件 | 现状 | 需操作 |
| --- | --- | --- | --- |
| 1 | MySQL 使用新空 volume | 新 `ypat-staging-mysql-data` 空,旧 `ypat_mysql_data` 有 15 表 | 数据导出/导入 |
| 2 | Redis 使用新空 volume | 新 `ypat-staging-redis-data` 空 | (可选)导出/导入或冷启动 |
| 3 | Compose project 改变 | `ypat` → `ypat-staging` | 双栈短暂并行 → 切流 → 下线旧栈 |
| 4 | network 改变 | apps 在 `ypat-staging-net`;FastDFS 在 `ypat_ypat-net` (external) | 把 FastDFS 加入新 net,或反过来 |
| 5 | DB 业务账号不存在 | MySQL 仅 root@%,root@localhost | 创建业务账号 + 授权 |
| 6 | `.env` 缺 3 变量 | 缺 `YPAT_DB_USERNAME` / `YPAT_DB_PASSWORD` / `YPAT_DB_NAME` | 追加到 `/opt/ypat/.env` |
| 7 | FastDFS 指向新空目录 | 新 compose 仍 bind 到 `/opt/ypat-data/fastdfs/{tracker,storage}` | ✓ 安全,无变动 |

## 迁移计划

### Phase 0 — 锁定与备份 (强制)

```bash
# 0.1 标记 staging 维护窗口 (业务/产品确认)
# 0.2 备份 MySQL (全量,带 routines/triggers)
TIMESTAMP=$(date +%Y%m%d_%H%M%S)
docker exec ypat-mysql-1 sh -c 'mysqldump -u root -p"$MYSQL_ROOT_PASSWORD" \
  --single-transaction --routines --triggers --events ypat' \
  | gzip > /opt/ypat-data/backups/mysql/ypat_pre_migration_${TIMESTAMP}.sql.gz

# 0.3 校验
gzip -t /opt/ypat-data/backups/mysql/ypat_pre_migration_${TIMESTAMP}.sql.gz
sha256sum /opt/ypat-data/backups/mysql/ypat_pre_migration_${TIMESTAMP}.sql.gz \
  > /opt/ypat-data/backups/mysql/ypat_pre_migration_${TIMESTAMP}.sha256

# 0.4 FastDFS 数据快照 (rsync 到独立目录)
rsync -a --delete /opt/ypat-data/fastdfs/ \
  /opt/ypat-data/backups/fastdfs/${TIMESTAMP}/

# 0.5 Nginx 配置备份
mkdir -p /opt/ypat-data/backups/nginx/${TIMESTAMP}
cp -a /etc/nginx/conf.d/panghu.work.conf /opt/ypat-data/backups/nginx/${TIMESTAMP}/
```

### Phase 1 — 完善 `.env`

由人工补充以下变量到 `/opt/ypat/.env`(权限 600):

```dotenv
YPAT_DB_NAME=ypat
YPAT_DB_USERNAME=ypat_app          # 选择业务账号名
YPAT_DB_PASSWORD=<强密码,例如 openssl rand -base64 24>
```

操作后:
```bash
chmod 600 /opt/ypat/.env
grep -E '^YPAT_DB_' /opt/ypat/.env | sed 's/=.*/=SET/'
```

### Phase 2 — 启动新数据层 (仅 MySQL/Redis,不启动 app)

```bash
cd /opt/ypat-src/f7df2e2ba8477904fc045a4e07a625ece7e7ae38

# 2.1 拉起新 MySQL/Redis (project ypat-staging)
docker compose \
  --env-file /opt/ypat/.env \
  -f docker-compose.staging.yml \
  up -d mysql redis

# 2.2 等待新 MySQL 健康
docker compose --env-file /opt/ypat/.env -f docker-compose.staging.yml ps mysql
# 等待 healthy

# 2.3 导入数据
set -a; source /opt/ypat/.env; set +a
NEW_MYSQL=ypat-staging-mysql-1
zcat /opt/ypat-data/backups/mysql/ypat_pre_migration_${TIMESTAMP}.sql.gz \
  | docker exec -i ${NEW_MYSQL} \
    sh -c "mysql -u root -p\"${YPAT_LOCAL_MYSQL_ROOT_PASSWORD}\" ypat"

# 2.4 创建业务账号
docker exec -i ${NEW_MYSQL} \
  sh -c "mysql -u root -p\"${YPAT_LOCAL_MYSQL_ROOT_PASSWORD}\"" <<SQL
CREATE USER IF NOT EXISTS '${YPAT_DB_USERNAME}'@'%' IDENTIFIED BY '${YPAT_DB_PASSWORD}';
GRANT SELECT, INSERT, UPDATE, DELETE, EXECUTE, SHOW VIEW
  ON \`${YPAT_DB_NAME}\`.* TO '${YPAT_DB_USERNAME}'@'%';
FLUSH PRIVILEGES;
SQL

# 2.5 验证业务账号连接
docker exec -i ${NEW_MYSQL} \
  sh -c "mysql -u'${YPAT_DB_USERNAME}' -p'${YPAT_DB_PASSWORD}' -e 'SELECT 1 FROM ${YPAT_DB_NAME}.banner LIMIT 1;'"

# 2.6 验证表数量与旧库一致
NEW_COUNT=$(docker exec ${NEW_MYSQL} mysql -uroot -p"${YPAT_LOCAL_MYSQL_ROOT_PASSWORD}" \
  -Nse "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema='ypat'")
OLD_COUNT=$(docker exec ypat-mysql-1 mysql -uroot -p"${YPAT_LOCAL_MYSQL_ROOT_PASSWORD}" \
  -Nse "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema='ypat'")
echo "OLD_TABLES=${OLD_COUNT}  NEW_TABLES=${NEW_COUNT}"
# 必须一致
```

### Phase 3 — 网络桥接 (FastDFS)

新 app 在 `ypat-staging-net`,FastDFS 在 `ypat_ypat-net`。Java FastDFS Client 需直接 TCP 连接 tracker:22122 + storage:23000。

**方案 1 (推荐)**: 把 FastDFS 容器再连接到 `ypat-staging-net`

```bash
docker network connect ypat-staging-net ypat-fastdfs-tracker
docker network connect ypat-staging-net ypat-fastdfs-storage

# 验证
docker network inspect ypat-staging-net \
  --format '{{range .Containers}}{{.Name}}{{println}}{{end}}'
# 应列出 fastdfs-tracker / fastdfs-storage
```

不重启 FastDFS,数据无损,旧 stack 同时通过 `ypat_ypat-net` 仍可访问(虽然旧 apps 即将下线)。

**方案 2**: 改 FastDFS staging compose 把 network 改为 `ypat-staging-net`(需 PR,本轮先用方案 1)。

### Phase 4 — 部署新 app stack

```bash
cd /opt/ypat-src/f7df2e2ba8477904fc045a4e07a625ece7e7ae38

# 4.1 干跑预检
./scripts/deploy/preflight.sh --env staging --work-dir "$(pwd)"
# 必须全绿

# 4.2 部署 (含构建)
./scripts/deploy/deploy-staging.sh
```

deploy 脚本会:
- 在 `/opt/ypat-staging/releases/<timestamp>-<sha>/` 落盘不可变 release
- 切 `current` symlink
- `docker compose -f docker-compose.staging.yml up -d --no-deps wap restapi system-web eureka`
- 健康检查失败则自动回滚到 `previous`

### Phase 5 — Nginx 切流

新 app 容器端口监听 127.0.0.1:8081/8082/8761。Nginx 已反代 panghu.work。
新 `docker/nginx/panghu.work.conf` 与现网差 22 行,需 diff 后审慎合并。

不直接覆盖 Nginx 配置,而是:
```bash
diff -u /etc/nginx/conf.d/panghu.work.conf \
  /opt/ypat-src/f7df2e2.../docker/nginx/panghu.work.conf
```
对比后人工合并,`nginx -t` 校验,再 `systemctl reload nginx`。

### Phase 6 — 旧栈下线 (在新栈业务冒烟通过后)

```bash
# 6.1 停止旧 app 容器
cd /opt/ypat
docker compose stop wap restapi system-web eureka

# 6.2 验证新栈端到端正常 (后续节)

# 6.3 旧 MySQL 与 Redis 保留 1-2 周后再下,避免回滚需要
# 6.4 旧 ypat_ypat-net 保留: FastDFS 仍在其上
```

## 回滚

若 Phase 4 或 Phase 5 失败:

```bash
# 1. deploy-staging.sh 内置自动回滚 (切回 previous symlink + up --no-deps)
# 2. 手动: 拉起旧 app
cd /opt/ypat
docker compose up -d wap restapi system-web eureka

# 3. 新栈下线
cd /opt/ypat-src/f7df2e2ba8477904fc045a4e07a625ece7e7ae38
docker compose --env-file /opt/ypat/.env -f docker-compose.staging.yml down
# 注意: down 默认不删除 named volume → 新 mysql/redis 数据保留可二次尝试
```

如需彻底丢弃新栈:
```bash
docker compose --env-file /opt/ypat/.env -f docker-compose.staging.yml down -v
# -v 会删除 ypat-staging-mysql-data, ypat-staging-redis-data
```

## 待人工确认的事项

1. 业务账号命名: `ypat_app` 还是其他?
2. 业务账号权限范围: 上述 `SELECT,INSERT,UPDATE,DELETE,EXECUTE,SHOW VIEW` 是否覆盖应用需求? 是否需要 `CREATE TEMPORARY TABLES`、`LOCK TABLES`、`REFERENCES`?
3. 是否同步导出 Redis 数据(`SAVE` + 复制 `dump.rdb`)? 还是允许新 Redis 冷启动(可能造成短暂登录态丢失)?
4. Nginx 配置 22 行差异是否需要保留服务器侧特有内容(证书路径、限流参数等)?
5. 维护窗口时长安排(预计 30-60 分钟,含 Phase 0-6)。

## 不在本计划范围

- 旧 `/opt/ypat` 永久归档为 `/opt/ypat-legacy-<timestamp>`(本轮保留)
- Production 部署(明确 NOT_DEPLOYED)
- Branch Protection 启用(见 07,solo-dev 锁死风险)
