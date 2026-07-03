# 预发环境回滚指南

> 文档日期：2026-07-03 · 基线 SHA：fdd03799 · 负责人：devops
> 入口脚本：[`scripts/deploy/rollback-staging.sh`](../../scripts/deploy/rollback-staging.sh)
> 部署入口：[`scripts/deploy/deploy-staging.sh`](../../scripts/deploy/deploy-staging.sh) · 详见 [`STAGING_DEPLOYMENT.md`](STAGING_DEPLOYMENT.md)
> 事故复盘：[`../deploy/LESSONS.md`](../deploy/LESSONS.md)

## 1. 范围声明（必读）

本指南**只覆盖**：

- ✅ 前端 H5 静态产物
- ✅ 后端 Docker 镜像
- ✅ 主机 Nginx 配置

本指南**不覆盖**：

- ❌ **数据库 schema / 数据变更** —— 永远不通过此脚本回滚
- ❌ 密钥 / 环境变量轮换 —— 见 [`SECRET_MANAGEMENT.md`](SECRET_MANAGEMENT.md)
- ❌ 微信支付 / OCR / 短信平台配置 —— 这些由对应平台后台处理

> **数据库回滚必须独立执行**：见 [`DATABASE_MIGRATION.md`](DATABASE_MIGRATION.md) §"回滚"，并经过 DBA 评估后才能恢复数据。

## 2. 触发条件

满足任一条件进入回滚流程：

- 健康检查连续 3 次失败
- 错误率 > 1% 持续 5 分钟
- 关键业务接口 5xx
- 前端 404 / 白屏率异常
- 主机 Nginx `nginx -t` 失败

## 3. 回滚准备

```bash
# 1) 查看最近 5 个发布版本
ls -1t /opt/ypat/releases/ | head -5

# 2) 查看每个版本的部署信息（git SHA、时间、上一版本）
for d in $(ls -1t /opt/ypat/releases/ | head -5); do
  echo "==== $d ===="
  cat /opt/ypat/releases/$d/deployment-info.txt
done

# 3) 当前运行版本
readlink /opt/ypat/current
readlink /opt/ypat/previous

# 4) 数据库 / 文件备份目录
ls -lt /opt/ypat-data/backups/mysql/ | head -5
ls -lt /opt/ypat-data/backups/frontend/ | head -5
```

## 4. 回滚流程

### 4.1 一键回滚（推荐）

```bash
# 回滚到指定版本（时间戳 YYYYMMDD_HHMMSS）
./scripts/deploy/rollback-staging.sh 20250629_120000
```

脚本行为：

1. 校验 `releases/<version>` 目录存在；打印 `deployment-info.txt`。
2. 交互式确认（输入 `yes`）。
3. **回滚前端**：从 `releases/<version>/frontend` 恢复到 `/var/www/<staging-domain>`。
4. **回滚后端镜像**：`docker compose down && up -d`（使用 compose 当前 pin 镜像）。

> 脚本不会修改数据库；DB schema / 数据完全独立。

### 4.2 分项回滚（排障）

#### 4.2.1 仅前端

```bash
BACKUP=/opt/ypat-data/backups/frontend/<timestamp>
rm -rf /var/www/<staging-domain>
mkdir -p /var/www/<staging-domain>
cp -r ${BACKUP}/* /var/www/<staging-domain>/
nginx -s reload
```

#### 4.2.2 仅后端镜像

```bash
# 1) 找到上一个稳定版本的镜像 tag（在 deployment-info.txt 或镜像仓库）
docker images | grep ypat

# 2) 修改 docker-compose.staging.yml 的 image: <prev-tag>
# 3) 重建并重启
docker compose -f docker-compose.staging.yml up -d
```

#### 4.2.3 仅主机 Nginx

```bash
# 查看历史配置
ls -lt /etc/nginx/conf.d/<staging-domain>.conf.bak.* | head -5

# 恢复并校验
cp /etc/nginx/conf.d/<staging-domain>.conf.bak.<timestamp> \
   /etc/nginx/conf.d/<staging-domain>.conf
nginx -t
systemctl reload nginx
```

## 5. 数据库回滚（独立流程）

> ⚠️ **不在 `rollback-staging.sh` 范围内**。数据库回滚由 DBA 主导。

```text
1. 业务方确认影响范围（受影响表 / 时间窗口）。
2. 在维护窗口内执行：
   - 备份当前数据库：
       docker exec ypat-mysql mysqldump -uroot -p"$YPAT_LOCAL_MYSQL_ROOT_PASSWORD" \
         --single-transaction --routines --triggers ypat \
         | gzip > /opt/ypat-data/backups/mysql/pre_rollback_$(date +%s).sql.gz
   - 评估 PITR（Point-In-Time Recovery） vs 全量恢复。
   - 若仅 schema 变更：执行 [`DATABASE_MIGRATION.md`](DATABASE_MIGRATION.md) 中的逆向 SQL。
3. 恢复后执行 [`db-verify.sh`](../../scripts/deploy/db-verify.sh)。
4. 通知业务方验证。
```

## 6. 回滚后验证

```bash
# 前端
curl -sI https://<staging-domain>/

# API
curl -sf https://<staging-domain>/api/banner/list | head -c 200

# 后台
curl -sI https://<staging-domain>/admin/

# 文件
curl -sI https://<staging-domain>/files/

# 容器
docker compose -f docker-compose.staging.yml ps

# Nginx
nginx -t && systemctl status nginx
```

## 7. 回滚检查清单

- [ ] 已确认触发条件（非误报）
- [ ] 已通知业务方 / 值班
- [ ] 已记录目标版本 git SHA
- [ ] 已备份当前数据库（即便不回滚 DB）
- [ ] 已执行 `rollback-staging.sh <version>` 或分项回滚
- [ ] 已逐项验证（前端 / API / admin / files / 容器）
- [ ] 已写入事故时间线（见 [`INCIDENT_RESPONSE.md`](INCIDENT_RESPONSE.md)）
- [ ] 已通知业务方恢复完成

## 8. 失败兜底

若 `rollback-staging.sh` 自身失败：

1. 立即停止后续操作。
2. 在值班群同步进展。
3. 按 [`INCIDENT_RESPONSE.md`](INCIDENT_RESPONSE.md) 升级处理。

## 9. 相关文档

- 事故复盘：[`../deploy/LESSONS.md`](../deploy/LESSONS.md) — **必读**
- 预发部署：[`STAGING_DEPLOYMENT.md`](STAGING_DEPLOYMENT.md)
- 数据库迁移：[`DATABASE_MIGRATION.md`](DATABASE_MIGRATION.md)
- 备份恢复：[`BACKUP_AND_RECOVERY.md`](BACKUP_AND_RECOVERY.md)
- 事故响应：[`INCIDENT_RESPONSE.md`](INCIDENT_RESPONSE.md)
- 环境隔离：[`ENVIRONMENT_ISOLATION.md`](ENVIRONMENT_ISOLATION.md)