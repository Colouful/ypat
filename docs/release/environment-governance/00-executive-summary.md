# Executive Summary — YPAT Three-Environment Configuration Governance

**日期**: 2026-06-29
**基线 SHA**: `f095578` (origin/main HEAD)
**任务分支**: `minimax/environment-governance`
**状态**: PR_READY

---

## 1. 任务范围

建立 YPAT development / staging / production 三环境的企业级配置、构建、部署、CI、回滚体系。

## 2. 关键决策

1. **三环境完全隔离**：网络、卷、域名、容器命名空间、密钥均独立
2. **production 模板就绪，但禁止自动部署**（缺少 production 基础设施）
3. **staging 用 HTTPS panghu.work / 82.156.14.216**（已有）
4. **统一使用 pnpm**（废弃 npm）
5. **统一使用 Docker Compose v2**（docker compose 子命令）
6. **fail-closed 校验**：production 启动校验关键变量、域名互窜、镜像 digest 固定
7. **不可变 release**：按时间戳 + Git SHA 命名 release 目录，原子切换 current symlink
8. **回滚只滚应用**：DB 必须单独回滚，不自动

## 3. 修复的关键问题

| 级别 | 问题 | 修复 |
|------|------|------|
| P0 | Maven pre/* 配置泄漏到 dev 包 | 加 `<exclude>pre/*</exclude>` |
| P0 | Dockerfile ENTRYPOINT + Compose command 冲突导致 java 启动失败 | 改用 SPRING_PROFILES_ACTIVE 环境变量 |
| P0 | 主机存在孤儿 java 进程伪装健康 | 部署脚本加孤儿清理 |
| P1 | production 引用 staging 域名（`.env.production` 含 www.panghu.work） | 删除 .env.production 改用 example 模板 |
| P1 | 后端 4 个 pom 没排除 pre/* | 加 exclude |
| P1 | pre/pro 共用 fdfs_client.properties 含 staging tracker | 按环境拆分 |
| P1 | Redis 无密码 | 加 requirepass |
| P1 | docker-compose.yml 公网暴露 mysql/redis/eureka | 改 127.0.0.1 |
| P1 | MySQL root 直接给应用 | 拆 root + 业务账号 |
| P1 | :latest 镜像 | 固定 digest 或版本 |
| P1 | preflight.sh 硬编码 codex/deploy-config-hardening 分支 | 接受 main / release/* / hotfix/* / SHA |
| P2 | production compose 不存在 | 新建 docker-compose.production.yml |
| P2 | deploy-staging.sh 路径用相对路径会丢失 | 改用绝对路径解析 |
| P2 | 没有 production 部署脚本 | 新建（必须 --confirm-production） |
| P2 | docs 散落，过时描述 | 重写 + 唯一入口 DEPLOY_ENVS.md |
| P2 | CI 不充分 | 拆为 frontend-dev/staging/production + backend-dev/pre/pro + compose + shell + security 多个 job |

## 4. 关键产物

### 4.1 Compose 文件
- `docker-compose.yml`（development，127.0.0.1 only，Redis 密码）
- `docker-compose.staging.yml`（显式 ypat-staging 命名空间）
- `docker-compose.production.yml`（TEMPLATE，fail-closed `${VAR:?...}`）
- `backend/dev/fastdfs/docker-compose.yml`（development）
- `backend/dev/fastdfs/docker-compose.staging.yml`
- `backend/dev/fastdfs/docker-compose.production.yml`（TEMPLATE）

### 4.2 环境变量模板
- `frontend/.env.{development,staging,production}.example`
- `backend/.env.{development,staging,production}.example`
- `.env.example`
- `scripts/config/validate-frontend-env.mjs`（fail-closed 校验）

### 4.3 部署脚本
- `scripts/deploy/preflight.sh`（已修复硬编码分支）
- `scripts/deploy/deploy-staging.sh`（不可变 release + 原子切换 + 自动回滚）
- `scripts/deploy/rollback-staging.sh`（回滚 + 列出 release）
- `scripts/deploy/preflight-production.sh`（必须 --confirm-production）
- `scripts/deploy/deploy-production.sh`（TEMPLATE，必须 4 必填 + 1 确认）
- `scripts/deploy/rollback-production.sh`（TEMPLATE，必须 --confirm-rollback）

### 4.4 Spring Boot 校验
- `backend/system-wap/src/main/java/com/ypat/config/EnvironmentConfigurationValidator.java`
- `backend/system-wap/src/main/resources/META-INF/spring.factories`（注册）

### 4.5 CI
- `.github/workflows/ci.yml`（9 个 job：三环境前端 + 三环境后端 + compose + shell + security）

### 4.6 文档
- `docs/development/LOCAL_DEVELOPMENT.md`
- `docs/release/STAGING_DEPLOYMENT.md`
- `docs/release/STAGING_ROLLBACK.md`
- `docs/release/PRODUCTION_DEPLOYMENT.md`
- `docs/release/PRODUCTION_ROLLBACK.md`
- `docs/release/SECRET_MANAGEMENT.md`
- `docs/release/DATABASE_MIGRATION.md`
- `docs/release/BACKUP_AND_RECOVERY.md`
- `docs/release/INCIDENT_RESPONSE.md`
- `docs/release/ENVIRONMENT_ISOLATION.md`
- `docs/release/PRODUCTION_LAUNCH_TODO.md`
- `docs/release/GITHUB_BRANCH_PROTECTION.md`
- `docs/config/ENVIRONMENT_VARIABLES.md`（唯一权威清单）
- `docs/architecture/ADR-JAVA-SPRING-UPGRADE.md`
- `.github/CODEOWNERS`
- `DEPLOY_ENVS.md`（导航）

## 5. 禁止的事（已落实）

- ❌ 自动部署 production
- ❌ 直接 push main
- ❌ force-push main
- ❌ 提交真实密钥 / 证书私钥
- ❌ `latest` 镜像
- ❌ production compose 引用 staging 域名/IP
- ❌ preflight.sh 硬编码分支
- ❌ Dockerfile ENTRYPOINT + Compose command 同时设置完整 java 命令
- ❌ 数据库自动回滚
- ❌ 跳过 CI / shellcheck / secret-scan

## 6. 必须由人工配置（不可自动化）

- ⚠️ GitHub Branch Protection Rules（`docs/release/GITHUB_BRANCH_PROTECTION.md`）
- ⚠️ production 独立域名、服务器、数据库、Redis、密钥
- ⚠️ production 真实部署（必须 owner 显式授权）
- ⚠️ 真实 release tag（preflight-production.sh 必须 --release-tag v1.x.x）

## 7. 最终结论

**代码状态**: PR_READY
**Development**: DEV_READY
**Staging**: STAGING_READY（注意：本次任务未在服务器执行 staging 部署，仅本地验证 compose + 脚本语法）
**Production**: PRODUCTION_TEMPLATE_READY

## 8. 后续动作

1. Owner review PR
2. CI 9 个 job 全绿
3. 合并到 main
4. 人工配置 GitHub Branch Protection
5. 生产部署需另行规划（基础设施未就绪）