# Current State Audit — 治理任务开始前的快照

**日期**: 2026-06-29
**基线**: `f095578` (origin/main)

## 1. Git 状态

```
branch: main2
HEAD: f095578 feat: 修改协议
recent commits:
  7aaa1a9 docs(deploy): document staging isolation, deploy, and rollback
  fe63b29 build(deploy): add staging deploy/rollback/verify scripts
  b3898e1 fix(security): remove public Eureka and broad CORS; fix network name
  31b6bdf fix(docker): restrict ports and enable redis auth
  f2a142b fix(storage): separate FastDFS internal and public addresses; add pre profile
  f7a0388 fix(env): enforce https for staging and production
  a3ddc29 feat: 新增 UniApp 预发/生产环境构建命令 + 文档完善
  7c25eee feat: 添加预发环境配置（panghu.work）
```

## 2. 已知问题

### 2.1 P0 — Maven Profile 资源泄漏
- `backend/system-wap/pom.xml`, `system-web/pom.xml`, `system-restapi/pom.xml`, `system-sso/pom.xml` 的 `<excludes>` 只排除 `dev/*` 和 `pro/*`，**没排除 `pre/*`**
- 后果：用 `-Pdev` 编译的 jar 包含 pre/ 资源，导致 dev 包包含预发配置

### 2.2 P1 — production 引用 staging
- `frontend/.env.production` 含 `https://www.panghu.work/api`（staging 域名）
- `frontend/.env.staging` HTTP（已被 PR5 改为 HTTPS 但需校验）

### 2.3 P1 — Redis 无密码
- `docker-compose.yml` Redis 没 `--requirepass`
- pre/pro compose 未注入 SPRING_REDIS_PASSWORD 到应用

### 2.4 P1 — 公网暴露端口
- `docker-compose.yml`: MySQL 3306、Redis 6379、Eureka 8761、REST 9081、WAP 8081、Web 8082 都绑定 `0.0.0.0`
- 仅 nginx 80/443 应该公网开放

### 2.5 P1 — Dockerfile ENTRYPOINT + Compose command 冲突
- 4 个 Dockerfile 都是 `ENTRYPOINT ["java", ..., "-jar", "app.jar"]`
- Compose 又用 `command: ["java", ..., "-jar", "app.jar"]`
- 实际启动命令被拼成 `java -D... -jar app.jar java -D... -jar app.jar`
- Java 把第二个 `java` 当成 main class 启动 → 卡死

### 2.6 P1 — FastDFS 配置复用
- pre/ 和 pro/ fdfs_client.properties 内容相同（tracker 都是 `fastdfs-tracker:22122`）
- production 应该由环境变量注入，不应该有默认 staging tracker

### 2.7 P2 — 重复 volumes 声明
- `docker-compose.yml` 末尾有第二个 `volumes:` 块（driver: bridge 错误配置）

### 2.8 P2 — `mysql_data`/`redis_data` driver: bridge 错误
- Named volume 不应该指定 `driver: bridge`

### 2.9 P2 — preflight.sh 硬编码分支
- 检查 `if [ "${CURRENT_BRANCH}" != "codex/deploy-config-hardening" ]`
- 不接受 main / release/* / hotfix/* / SHA

### 2.10 P2 — 没有 production compose
- 仅有 `docker-compose.yml` + `docker-compose.staging.yml`
- 没有 `docker-compose.production.yml`

### 2.11 P2 — 没有 production 部署脚本
- 仅 `deploy-staging.sh` + `rollback-staging.sh`
- 没有 `preflight-production.sh` + `deploy-production.sh` + `rollback-production.sh`

### 2.12 P2 — 数据库账号无最小权限
- 应用直接用 MySQL root
- 没有业务账号（仅 SELECT/INSERT/UPDATE/DELETE）

### 2.13 P2 — 镜像 digest 未固定
- `mysql:8.0`, `redis:7-alpine`, `nginx:alpine` 等使用 latest 或未固定补丁版本

### 2.14 P2 — 文档过时
- `PRODUCTION_LAUNCH_TODO.md` 含"当前 production 已建立"过时描述
- 多文档分散维护变量清单

### 2.15 P2 — CI 不充分
- 仅有基础 frontend / backend / security
- 缺 staging/production 环境隔离校验
- 缺 compose 校验
- 缺 production 域名互窜检测

## 3. 服务器状态

### 3.1 当前 staging 运行情况
- 服务器：82.156.14.216
- 域名：panghu.work (SSL 已配)
- 容器：6 个 ypat 容器 + 2 个 FastDFS 容器
- 状态：healthy 但部分容器使用孤儿 java 进程（PR5 部署事故遗留）
- 本次任务未实际部署（仅验证配置和脚本语法）

### 3.2 production 状态
- 不存在
- 没有任何 production 基础设施

## 4. 仓库结构

```
/
├── .github/
│   ├── CODEOWNERS (新)
│   └── workflows/
│       └── ci.yml (重写)
├── backend/
│   ├── .env.{development,staging,production}.example (新)
│   ├── system-{restapi,wap,web,sso}/
│   │   ├── pom.xml (加 <exclude>pre/*</exclude>)
│   │   └── src/main/resources/{dev,pre,pro}/
│   └── dev/fastdfs/
│       └── docker-compose.{yml,staging.yml,production.yml}
├── docker-compose.yml (重写)
├── docker-compose.staging.yml (重写)
├── docker-compose.production.yml (新)
├── frontend/
│   ├── .env.{development,staging,production}.example
│   └── src/config/env.ts (加 production 拒绝 staging 域名)
├── docs/
│   ├── architecture/ADR-JAVA-SPRING-UPGRADE.md (新)
│   ├── config/ENVIRONMENT_VARIABLES.md (新)
│   ├── development/LOCAL_DEVELOPMENT.md (新)
│   └── release/
│       ├── ENVIRONMENT_ISOLATION.md (重写)
│       ├── PRODUCTION_LAUNCH_TODO.md (重写)
│       ├── STAGING_{DEPLOYMENT,ROLLBACK}.md (重写)
│       ├── PRODUCTION_{DEPLOYMENT,ROLLBACK}.md (新)
│       ├── SECRET_MANAGEMENT.md (新)
│       ├── DATABASE_MIGRATION.md (新)
│       ├── BACKUP_AND_RECOVERY.md (新)
│       ├── INCIDENT_RESPONSE.md (新)
│       ├── GITHUB_BRANCH_PROTECTION.md (新)
│       └── environment-governance/ (evidence)
└── scripts/
    ├── config/
    │   └── validate-frontend-env.mjs (新)
    └── deploy/
        ├── preflight.sh (重写)
        ├── deploy-staging.sh (重写)
        ├── rollback-staging.sh (重写)
        ├── preflight-production.sh (新)
        ├── deploy-production.sh (新)
        └── rollback-production.sh (新)
```