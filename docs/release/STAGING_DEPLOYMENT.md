# 预发环境部署指南

> 文档日期：2026-06-29 · 基线 SHA：b4af32a · 负责人：devops
> 入口脚本：[`scripts/deploy/deploy-staging.sh`](../../scripts/deploy/deploy-staging.sh)
> 前置检查：[`scripts/deploy/preflight.sh`](../../scripts/deploy/preflight.sh) · [`scripts/deploy/db-preflight.sh`](../../scripts/deploy/db-preflight.sh) · [`scripts/deploy/check-tls-expiry.sh`](../../scripts/deploy/check-tls-expiry.sh)
> Nginx 配置：[`scripts/deploy/install-nginx-config.sh`](../../scripts/deploy/install-nginx-config.sh)
> 数据库迁移：[`scripts/deploy/db-migrate-staging.sh`](../../scripts/deploy/db-migrate-staging.sh)
> FastDFS 数据迁移：[`scripts/deploy/migrate-fastdfs-data.sh`](../../scripts/deploy/migrate-fastdfs-data.sh)

## 1. 当前预发环境

- **域名**：`staging.example.invalid`（文档示例，真实值见运维记录，**禁止**将 `panghu.work` 复用为其他环境）
- **公网入口**：主机 Nginx 80 / 443（TLS 终止）
- **角色**：所有对外可见的服务在 staging 上**唯一存在**一份；与 production 物理隔离

> ⚠️ 文档中出现的 `panghu.work` 仅为历史占位示例，**禁止**作为 production 地址。

## 2. 部署架构

```
公网 80/443
  ↓
主机 Nginx (systemctl)
  ├─ TLS 终止
  ├─ 静态文件 (/var/www/<staging-domain>)
  └─ 反代
      ├─ /api/   → 127.0.0.1:8081 (wap)
      ├─ /admin/ → 127.0.0.1:8082 (system-web)
      └─ /files/ → 127.0.0.1:8888 (fastdfs-storage)

Docker 网络: ypat_ypat-net
  ├─ mysql:3306
  ├─ redis:6379
  ├─ eureka:8761
  ├─ restapi:9081
  ├─ wap:8081
  ├─ system-web:8082
  └─ fastdfs-tracker:22122 / fastdfs-storage:8080
```

## 3. 部署前置检查

依次执行：

```bash
# 1) 部署预检：分支、工作区、关键文件
./scripts/deploy/preflight.sh

# 2) 数据库预检：连通性、表数量、备份目录
./scripts/deploy/db-preflight.sh

# 3) TLS 证书有效期
./scripts/deploy/check-tls-expiry.sh /etc/nginx/ssl/<staging-domain>/<staging-domain>.crt 30

# 4) 当前健康状态
curl -sI https://<staging-domain>/ | head -3
```

任意一步失败 → **立即停止**，回看 [`STAGING_ROLLBACK.md`](STAGING_ROLLBACK.md)。

## 4. 部署流程

### 4.1 一键入口

```bash
# 登录预发服务器
ssh deploy@<staging-host>

# 拉取代码
cd /opt/ypat && git fetch && git checkout <commit-sha>

# 部署（脚本内置：构建前端 + 后端 + 镜像 + 切符号链接）
./scripts/deploy/deploy-staging.sh
```

脚本内部步骤概要（详见 [`deploy-staging.sh`](../../scripts/deploy/deploy-staging.sh)）：

1. 记录当前版本，写入 `releases/<timestamp>/deployment-info.txt`。
2. `pnpm install --frozen-lockfile && pnpm run build:h5:staging`。
3. 备份并复制新前端到 `releases/<timestamp>/frontend`。
4. `mvn -Ppre clean package -DskipTests=false` 构建后端。
5. 原子切换 `current` 符号链接；`previous` 指向上一个版本。
6. `docker compose -f docker-compose.staging.yml config` 校验。
7. `docker compose ... build && up -d`。
8. 输出 release 路径与 git SHA，便于回滚。

### 4.2 手工步骤（仅排障时使用）

```bash
# 前端构建
cd frontend
pnpm install --frozen-lockfile
pnpm run build:h5:staging

# 后端构建（pre profile）
cd ../backend
mvn clean package -Ppre -DskipTests=false

# 部署前端
mkdir -p /var/www/<staging-domain>
cp -r ../frontend/dist/build/h5/* /var/www/<staging-domain>/

# 数据库迁移（默认 --dry-run）
./scripts/deploy/db-migrate-staging.sh --dry-run
./scripts/deploy/db-migrate-staging.sh --execute    # 真正执行

# 主机 Nginx 配置
./scripts/deploy/install-nginx-config.sh
nginx -t && systemctl reload nginx

# Docker
docker compose -f docker-compose.staging.yml build
docker compose -f docker-compose.staging.yml up -d
docker compose -f backend/dev/fastdfs/docker-compose.staging.yml up -d
```

## 5. 部署后验证

```bash
# H5 入口
curl -sI https://<staging-domain>/

# API 健康
curl -sf https://<staging-domain>/api/banner/list | head -c 200

# 后台入口
curl -sI https://<staging-domain>/admin/

# 文件访问
curl -sI https://<staging-domain>/files/

# Eureka 注册
ssh -L 8761:127.0.0.1:8761 deploy@<staging-host>
# 浏览器访问 http://127.0.0.1:8761

# HSTS / TLS
curl -sI https://<staging-domain>/ | grep -i 'strict-transport-security'
openssl s_client -connect <staging-domain>:443 -servername <staging-domain> < /dev/null 2>/dev/null \
  | openssl x509 -noout -dates
```

## 6. 数据库迁移注意事项

- 默认 [`db-migrate-staging.sh`](../../scripts/deploy/db-migrate-staging.sh) 以 `--dry-run` 模式运行，仅打印待执行 SQL；**必须显式 `--execute` 才落库**。
- 执行前必须完成 [`db-preflight.sh`](../../scripts/deploy/db-preflight.sh) + 最近一次全库备份（`/opt/ypat-data/backups/mysql/`）。
- 表结构变更必须遵循 [`DATABASE_MIGRATION.md`](DATABASE_MIGRATION.md) 的 SHA256 + schema_migration 规范。

## 7. 密钥 / 环境变量注入

- 所有 `YPAT_*` 密钥通过部署平台的密钥管理系统注入到 `/opt/ypat/.env`（`chmod 600 root:root`），**禁止**写进 docker-compose 或镜像层。
- 密钥清单与轮换要求见 [`SECRET_MANAGEMENT.md`](SECRET_MANAGEMENT.md)。

## 8. 失败判定与回滚

触发下列任意一条必须立即停止部署并进入 [`STAGING_ROLLBACK.md`](STAGING_ROLLBACK.md)：

- 5xx 比例上升 > 1%
- 健康检查失败
- TLS 握手失败
- 数据库迁移报错
- 前端关键路径（首页 / API / admin / files）不可达

## 9. 相关文档

- 预发回滚：[`STAGING_ROLLBACK.md`](STAGING_ROLLBACK.md)
- 生产部署：[`PRODUCTION_DEPLOYMENT.md`](PRODUCTION_DEPLOYMENT.md)
- 环境隔离：[`ENVIRONMENT_ISOLATION.md`](ENVIRONMENT_ISOLATION.md)
- 密钥管理：[`SECRET_MANAGEMENT.md`](SECRET_MANAGEMENT.md)
- 数据库迁移：[`DATABASE_MIGRATION.md`](DATABASE_MIGRATION.md)
- 备份恢复：[`BACKUP_AND_RECOVERY.md`](BACKUP_AND_RECOVERY.md)
- 事故响应：[`INCIDENT_RESPONSE.md`](INCIDENT_RESPONSE.md)
- 环境变量清单：[`../config/ENVIRONMENT_VARIABLES.md`](../config/ENVIRONMENT_VARIABLES.md)
- 本地开发：[`../development/LOCAL_DEVELOPMENT.md`](../development/LOCAL_DEVELOPMENT.md)