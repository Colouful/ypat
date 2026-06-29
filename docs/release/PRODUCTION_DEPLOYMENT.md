# 生产环境部署指南（模板）

> 文档日期：2026-06-29 · 基线 SHA：b4af32a · 负责人：devops
> ⚠️ **当前 production 尚未建立**。本文档为模板，**禁止**据此声称 production 已部署。

## 0. 状态声明

- ✅ staging：已部署（详见 [`STAGING_DEPLOYMENT.md`](STAGING_DEPLOYMENT.md)）
- ❌ **production：未建立** —— 域名 / 服务器 / 数据库 / 密钥 / SSL 证书 / 监控全部阻塞
- 待办清单：[`PRODUCTION_LAUNCH_TODO.md`](PRODUCTION_LAUNCH_TODO.md)

> 文档中出现的所有地址仅为**示例占位**：`production.example.invalid`。
> 任何真实 production 域名 / IP / 密钥**严禁**写进文档或提交。

## 1. 强制安全门

任何 production 操作必须满足**全部**条件，缺一不可：

| # | 条件 | 卡点 |
| --- | --- | --- |
| 1 | 显式传入 `--confirm-production` 标志 | 脚本拒绝执行 |
| 2 | 当前分支为 `main`（或约定的 release 分支） | 脚本拒绝执行 |
| 3 | 工作区干净 | CI / preflight 拒绝 |
| 4 | 目标 git SHA 与部署平台 / 工单中记录一致 | 部署前必须 diff 显示 |
| 5 | 至少 2 名维护者在线（4-eyes 原则） | 值班群确认 |
| 6 | 已完成 [`STAGING_DEPLOYMENT.md`](STAGING_DEPLOYMENT.md) 并验证 ≥ 24h | 工单附 staging 验证报告 |
| 7 | 数据库全量备份 + 验证 | [`BACKUP_AND_RECOVERY.md`](BACKUP_AND_RECOVERY.md) §3 |
| 8 | 监控 / 告警 / 值班表就绪 | 见 [`INCIDENT_RESPONSE.md`](INCIDENT_RESPONSE.md) |

> 缺任意一条 → 取消发布，回到 staging 继续验证。

## 2. 部署入口（占位脚本契约）

production 部署脚本契约（**未实现**；以下为预期接口）：

```bash
# 强制确认标志；缺失将立即退出并打印 OPS_BLOCKED
./scripts/deploy/deploy-production.sh \
  --confirm-production \
  --target=<commit-sha> \
  --compose-file=docker-compose.production.yml \
  --backup-before
```

脚本预期行为（与 staging 一致但更严格）：

1. 解析 `--confirm-production`，缺失则退出非 0。
2. 校验 `--target` SHA 在允许的 git ref 中。
3. 全量备份：DB / Redis RDB / FastDFS metadata。
4. `pnpm run build:h5:production` + `mvn -Ppro package`。
5. 写 `releases/<timestamp>/deployment-info.txt`，包含操作者、双人确认码、SHA。
6. `docker compose -f docker-compose.production.yml build && up -d`。
7. 主机 Nginx reload（[`install-nginx-config.sh`](../../scripts/deploy/install-nginx-config.sh)）。
8. 输出 smoke test 用例 + 监控 dashboard URL。

## 3. 生产环境隔离矩阵

| 维度 | staging | production |
| --- | --- | --- |
| 域名 | `staging.example.invalid`（示例） | `production.example.invalid`（示例） |
| 服务器 | staging 主机（独立） | production 主机（独立；**禁止复用**） |
| 数据库 | ypat_staging | ypat_production |
| Redis | ypat-staging | ypat-production |
| FastDFS | /opt/ypat-data/fastdfs/staging | /opt/ypat-data/fastdfs/production |
| 密钥 | staging 独立凭据 | production 独立凭据 |
| TLS 证书 | staging CA | 公共 CA（Let's Encrypt / 商业 CA） |
| 监控 / 告警 | staging 通道 | production 通道（独立） |
| 值班 | devops on-call | SRE + devops 双值班 |

> 完整隔离矩阵：[`ENVIRONMENT_ISOLATION.md`](ENVIRONMENT_ISOLATION.md)

## 4. 部署流程（模板）

### 4.1 预发布检查（24h 前）

- [ ] staging 已灰度验证 ≥ 24h
- [ ] 工单已创建并由 ≥ 2 名维护者 approve
- [ ] 数据库迁移 SQL 已通过 staging 验证（[`DATABASE_MIGRATION.md`](DATABASE_MIGRATION.md)）
- [ ] 监控 / 告警 / 值班表就绪
- [ ] 备份就绪并演练过一次恢复

### 4.2 维护窗口开始

1. 通知业务方 / 客服 / 用户进入维护窗口。
2. 停止写流量（Nginx 维护页 / API 503）。
3. 执行数据库备份：

   ```bash
   ./scripts/deploy/backup-database.sh --env=production --retention=30
   ```

4. 校验备份完整性（见 [`BACKUP_AND_RECOVERY.md`](BACKUP_AND_RECOVERY.md)）。

### 4.3 部署

```bash
# 仅在维护窗口 + 4-eyes + 全量备份就绪后执行
./scripts/deploy/deploy-production.sh \
  --confirm-production \
  --target=<commit-sha> \
  --backup-before
```

### 4.4 部署后验证

- 主页 / 关键 API / admin / files 全部 2xx
- 监控无新增 ERROR
- 日志采样无异常
- HSTS / TLS 握手验证
- 微信小程序后台已配置新域名（request / uploadFile / downloadFile）

### 4.5 维护窗口结束

1. 通知业务方 / 客服恢复。
2. 启动写流量（移除 503 维护页）。
3. 值班 30 分钟保持在线观察。

## 5. 密钥管理

- production 密钥**必须**来自 KMS / 部署平台 Secret，绝不写进 compose / 镜像 / 仓库。
- 90 天强制轮换；详见 [`SECRET_MANAGEMENT.md`](SECRET_MANAGEMENT.md)。
- 操作者必须使用独立 production IAM 账号，与 staging / development 隔离。

## 6. 回滚

立即进入 [`PRODUCTION_ROLLBACK.md`](PRODUCTION_ROLLBACK.md)：

- 前端 / 镜像 / Nginx：与 staging 相同（`rollback-production.sh`）
- **数据库：永远独立**，由 DBA 主导

## 7. 事故响应

任何 P0 / P1 → 见 [`INCIDENT_RESPONSE.md`](INCIDENT_RESPONSE.md)。

## 8. 相关文档

- 生产回滚：[`PRODUCTION_ROLLBACK.md`](PRODUCTION_ROLLBACK.md)
- 生产待办：[`PRODUCTION_LAUNCH_TODO.md`](PRODUCTION_LAUNCH_TODO.md)
- 环境隔离：[`ENVIRONMENT_ISOLATION.md`](ENVIRONMENT_ISOLATION.md)
- 密钥管理：[`SECRET_MANAGEMENT.md`](SECRET_MANAGEMENT.md)
- 数据库迁移：[`DATABASE_MIGRATION.md`](DATABASE_MIGRATION.md)
- 备份恢复：[`BACKUP_AND_RECOVERY.md`](BACKUP_AND_RECOVERY.md)
- 事故响应：[`INCIDENT_RESPONSE.md`](INCIDENT_RESPONSE.md)
- 环境变量清单：[`../config/ENVIRONMENT_VARIABLES.md`](../config/ENVIRONMENT_VARIABLES.md)
- 分支保护：[`GITHUB_BRANCH_PROTECTION.md`](GITHUB_BRANCH_PROTECTION.md)