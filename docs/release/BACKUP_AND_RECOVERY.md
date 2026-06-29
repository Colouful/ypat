# 备份与恢复（Backup & Recovery）

> 文档日期：2026-06-29 · 基线 SHA：b4af32a · 负责人：devops + DBA
> 数据库迁移：[`DATABASE_MIGRATION.md`](DATABASE_MIGRATION.md)
> 事故响应：[`INCIDENT_RESPONSE.md`](INCIDENT_RESPONSE.md)
> FastDFS 数据迁移：[`scripts/deploy/migrate-fastdfs-data.sh`](../../scripts/deploy/migrate-fastdfs-data.sh)

## 1. 范围

| 数据 | 备份策略 | RPO | RTO | 存储位置 |
| --- | --- | --- | --- | --- |
| MySQL（staging） | 每日全量 + binlog 增量 | 5 min | 30 min | `/opt/ypat-data/backups/mysql/` + 异地（OSS / S3） |
| MySQL（production） | 每日全量 + binlog 增量 + PITR | 1 min | 15 min | 同上，独立桶 |
| Redis（staging） | RDB 每日 + AOF | 1 h | 10 min | `/opt/ypat-data/backups/redis/` |
| Redis（production） | RDB 每日 + AOF + 异地副本 | 5 min | 10 min | 异地独立桶 |
| FastDFS（staging） | metadata 周全量 + 文件每日增量 | 24 h | 1 h | `/opt/ypat-data/fastdfs/` + OSS 镜像 |
| FastDFS（production） | metadata 周全量 + 文件每日增量 + 异地副本 | 1 h | 30 min | 异地独立桶 |
| 前端 H5 静态 | 每次发布即版本化（`/opt/ypat/releases/<ts>/frontend`） | 0 | 5 min | 主机本地 + 异地镜像 |

> RPO / RTO 为目标值，需每季度演练验证；演练记录归档至本目录 `recovery-drills/`。

## 2. MySQL

### 2.1 全量备份

```bash
# 每日凌晨 02:00 (crontab)
docker exec ypat-mysql mysqldump \
  -uroot -p"$YPAT_LOCAL_MYSQL_ROOT_PASSWORD" \
  --single-transaction --routines --triggers --events \
  --master-data=2 \
  ypat | gzip > /opt/ypat-data/backups/mysql/ypat_$(date +%Y%m%d_%H%M%S).sql.gz

# 上传异地
aws s3 cp /opt/ypat-data/backups/mysql/ypat_<ts>.sql.gz s3://ypat-backups-prod/mysql/
```

### 2.2 binlog 增量

- MySQL 8.0 默认开启 `binlog_format=ROW`。
- 每 5 分钟归档一次：

  ```bash
  mysqlbinlog --read-from-remote-server --host=<host> --user=repl \
    --password=$REPL_PASSWORD --raw --stop-never --result-file=/opt/ypat-data/backups/binlog/ \
    mysql-bin.000001
  ```

### 2.3 恢复

```bash
# 1) 全量恢复
gunzip -c /opt/ypat-data/backups/mysql/ypat_<ts>.sql.gz \
  | docker exec -i ypat-mysql mysql -uroot -p"$YPAT_LOCAL_MYSQL_ROOT_PASSWORD" ypat

# 2) binlog replay（PITR）
mysqlbinlog --stop-datetime="2026-06-29 11:30:00" /opt/ypat-data/backups/binlog/mysql-bin.* \
  | docker exec -i ypat-mysql mysql -uroot -p"$YPAT_LOCAL_MYSQL_ROOT_PASSWORD" ypat

# 3) 校验
./scripts/deploy/db-verify.sh
```

### 2.4 校验

```bash
# 行数 / 索引 / 外键
./scripts/deploy/db-verify.sh

# 业务冒烟（关键查询）
mysql -h 127.0.0.1 -P 3306 -uroot -p"$YPAT_LOCAL_MYSQL_ROOT_PASSWORD" ypat <<'SQL'
SELECT COUNT(*) FROM t_user;
SELECT COUNT(*) FROM t_ypat_info;
SELECT COUNT(*) FROM t_order;
SQL
```

## 3. Redis

### 3.1 备份

```bash
# RDB（自动 + 手动 BGSAVE）
docker exec ypat-redis redis-cli -a "$YPAT_LOCAL_REDIS_PASSWORD" BGSAVE

# AOF（推荐开启）
docker exec ypat-redis redis-cli -a "$YPAT_LOCAL_REDIS_PASSWORD" CONFIG SET appendonly yes
```

### 3.2 恢复

```bash
# 关闭 AOF / 替换 RDB
docker compose stop redis
cp /opt/ypat-data/backups/redis/dump_<ts>.rdb /opt/ypat-data/redis/dump.rdb
docker compose start redis
docker exec ypat-redis redis-cli -a "$YPAT_LOCAL_REDIS_PASSWORD" PING
```

### 3.3 注意

- Redis 用于会话 / 缓存 / 限流，恢复后**部分会话失效**属预期，业务需可降级（重新登录）。
- **禁止**把 Redis 当持久化主存储。

## 4. FastDFS

### 4.1 备份

```bash
# tracker metadata
tar czf /opt/ypat-data/backups/fastdfs/tracker_<ts>.tgz /opt/ypat-data/fastdfs/tracker

# storage data
rsync -a --delete /opt/ypat-data/fastdfs/storage/ \
  /opt/ypat-data/backups/fastdfs/storage_<ts>/

# 异地镜像
./scripts/deploy/migrate-fastdfs-data.sh --dry-run   # 仅打印
```

### 4.2 恢复

```bash
# 1) 停服
docker compose -f backend/dev/fastdfs/docker-compose.staging.yml down

# 2) 恢复数据
rm -rf /opt/ypat-data/fastdfs/storage
rsync -a /opt/ypat-data/backups/fastdfs/storage_<ts>/ /opt/ypat-data/fastdfs/storage/

# 3) 恢复 metadata
rm -rf /opt/ypat-data/fastdfs/tracker
tar xzf /opt/ypat-data/backups/fastdfs/tracker_<ts>.tgz -C /

# 4) 重启
docker compose -f backend/dev/fastdfs/docker-compose.staging.yml up -d
```

### 4.3 校验

```bash
# 抽样下载
curl -sI https://<staging-domain>/files/<group>/M00/<path>
# 比对 hash
sha256sum /opt/ypat-data/fastdfs/storage/data/<path>
```

## 5. 前端 H5 静态

- 每次发布自动备份到 `/opt/ypat/releases/<ts>/frontend/`（[`deploy-staging.sh`](../../scripts/deploy/deploy-staging.sh)）。
- 回滚即符号链接切换；详见 [`STAGING_ROLLBACK.md`](STAGING_ROLLBACK.md)。
- 历史版本保留 ≥ 30 天，超期自动清理（`releases/` cron）。

## 6. 异地副本

| 目标 | 用途 |
| --- | --- |
| OSS / S3 桶（独立账号） | 离线备份 + 灾难恢复 |
| 第二个可用区（同城 / 异地） | 热备（Redis / FastDFS） |

> 异地副本账号**与生产密钥隔离**（详见 [`SECRET_MANAGEMENT.md`](SECRET_MANAGEMENT.md)）。

## 7. 演练

每季度一次：

```text
1. 抽签一个 staging 备份（MySQL / Redis / FastDFS 任一）。
2. 在独立机器还原。
3. 跑 [`db-verify.sh`](../../scripts/deploy/db-verify.sh) + 业务冒烟。
4. 记录耗时 vs RTO 目标；偏差 > 30% 时升级为事故。
5. 归档演练报告到 `docs/release/recovery-drills/YYYY-Q<n>.md`。
```

## 8. 相关文档

- 数据库迁移：[`DATABASE_MIGRATION.md`](DATABASE_MIGRATION.md)
- 事故响应：[`INCIDENT_RESPONSE.md`](INCIDENT_RESPONSE.md)
- 密钥管理：[`SECRET_MANAGEMENT.md`](SECRET_MANAGEMENT.md)
- 预发部署：[`STAGING_DEPLOYMENT.md`](STAGING_DEPLOYMENT.md)
- 生产部署：[`PRODUCTION_DEPLOYMENT.md`](PRODUCTION_DEPLOYMENT.md)