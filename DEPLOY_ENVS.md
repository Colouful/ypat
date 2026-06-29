# 部署导航（DEPLOY_ENVS.md）

> 文档日期：2026-06-29 · 基线 SHA：b4af32a · 维护：devops
> **本文档只做导航**，不重复维护变量清单、命令清单；详情进入对应专题文档。

## 1. 三环境一览

| 环境 | 状态 | 入口 |
| --- | --- | --- |
| development | ✅ 常态 | [`docs/development/LOCAL_DEVELOPMENT.md`](docs/development/LOCAL_DEVELOPMENT.md) |
| staging | ✅ 已部署 | [`docs/release/STAGING_DEPLOYMENT.md`](docs/release/STAGING_DEPLOYMENT.md) |
| production | ❌ **未建立** | [`docs/release/PRODUCTION_LAUNCH_TODO.md`](docs/release/PRODUCTION_LAUNCH_TODO.md) |

> ⚠️ production 尚未建立。任何"production 已部署" / "当前 production 在 HTTP IP"的说法均为**过时占位**，禁止作为事实陈述。

## 2. 关键文档

| 主题 | 路径 |
| --- | --- |
| 环境变量（权威） | [`docs/config/ENVIRONMENT_VARIABLES.md`](docs/config/ENVIRONMENT_VARIABLES.md) |
| 变量清单（CSV） | [`docs/release/environment-governance/artifacts/env-variable-inventory.csv`](docs/release/environment-governance/artifacts/env-variable-inventory.csv) |
| 治理审计 | [`docs/release/environment-governance/`](docs/release/environment-governance/) |
| 三环境隔离矩阵 | [`docs/release/ENVIRONMENT_ISOLATION.md`](docs/release/ENVIRONMENT_ISOLATION.md) |
| 预发部署 | [`docs/release/STAGING_DEPLOYMENT.md`](docs/release/STAGING_DEPLOYMENT.md) |
| 预发回滚 | [`docs/release/STAGING_ROLLBACK.md`](docs/release/STAGING_ROLLBACK.md) |
| 生产部署（模板） | [`docs/release/PRODUCTION_DEPLOYMENT.md`](docs/release/PRODUCTION_DEPLOYMENT.md) |
| 生产回滚（模板） | [`docs/release/PRODUCTION_ROLLBACK.md`](docs/release/PRODUCTION_ROLLBACK.md) |
| 生产上线待办 | [`docs/release/PRODUCTION_LAUNCH_TODO.md`](docs/release/PRODUCTION_LAUNCH_TODO.md) |
| 密钥管理 | [`docs/release/SECRET_MANAGEMENT.md`](docs/release/SECRET_MANAGEMENT.md) |
| 数据库迁移 | [`docs/release/DATABASE_MIGRATION.md`](docs/release/DATABASE_MIGRATION.md) |
| 备份与恢复 | [`docs/release/BACKUP_AND_RECOVERY.md`](docs/release/BACKUP_AND_RECOVERY.md) |
| 事故响应 | [`docs/release/INCIDENT_RESPONSE.md`](docs/release/INCIDENT_RESPONSE.md) |
| GitHub 分支保护 | [`docs/release/GITHUB_BRANCH_PROTECTION.md`](docs/release/GITHUB_BRANCH_PROTECTION.md) |
| 本地开发 | [`docs/development/LOCAL_DEVELOPMENT.md`](docs/development/LOCAL_DEVELOPMENT.md) |
| Java / Spring 升级 ADR | [`docs/architecture/ADR-JAVA-SPRING-UPGRADE.md`](docs/architecture/ADR-JAVA-SPRING-UPGRADE.md) |

## 3. 常用脚本

`scripts/deploy/`：

- `preflight.sh` — 部署前 Git / 分支 / 文件预检
- `db-preflight.sh` / `db-migrate-staging.sh` / `db-verify.sh` — 数据库
- `deploy-staging.sh` / `rollback-staging.sh` — 一键部署 / 回滚
- `install-nginx-config.sh` — 主机 Nginx 配置（含自动备份）
- `get-fastdfs-digest.sh` — 锁定 FastDFS 镜像 digest
- `migrate-fastdfs-data.sh` — FastDFS 数据迁移
- `check-tls-expiry.sh` — TLS 证书有效期

> 脚本本身**禁止**声称 production 已部署；模板脚本入口契约见 [`PRODUCTION_DEPLOYMENT.md`](docs/release/PRODUCTION_DEPLOYMENT.md) §2。

## 4. 角色与责任

| 角色 | 责任 |
| --- | --- |
| devops | 部署 / 监控 / 密钥 / 备份 |
| backend lead | 后端服务 / 数据库迁移 |
| frontend lead | 前端构建 / 微信小程序后台 |
| DBA | 数据库架构 / 备份 / 恢复 |
| SRE on-call | 事故响应（[`INCIDENT_RESPONSE.md`](docs/release/INCIDENT_RESPONSE.md)） |

## 5. 变更原则

1. 任何变量 / 端口 / 命令变更 → 先更新 CSV，再回填文档。
2. 任何密钥 / 真实 IP / 域名变更 → 走 [`SECRET_MANAGEMENT.md`](docs/release/SECRET_MANAGEMENT.md) 流程。
3. 任何部署流程变更 → 同步更新 `.sh` + 对应 `.md`，并在 PR 中互相引用。
4. CODEOWNERS 详见 [`.github/CODEOWNERS`](.github/CODEOWNERS)。