# YPAT（爱去拍）摄影约拍平台

YPAT（爱去拍）是面向摄影师、模特、妆造师、修图师等摄影行业从业者的约拍撮合平台，覆盖约拍发布、约拍申请、实名认证、消息通知、微信支付、后台审核等核心流程。

本文档是仓库根入口，只保留当前开发最常用的信息。更细的环境变量、部署、回滚、事故处理、数据库迁移等内容见 [DEPLOY_ENVS.md](DEPLOY_ENVS.md) 和 `docs/` 目录。

## 当前状态

| 环境 | 状态 | 入口 |
| --- | --- | --- |
| development（本地开发环境） | 常态使用 | [docs/development/LOCAL_DEVELOPMENT.md](docs/development/LOCAL_DEVELOPMENT.md) |
| staging（预发环境） | 已部署 | [docs/release/STAGING_DEPLOYMENT.md](docs/release/STAGING_DEPLOYMENT.md) |
| production（生产环境） | 尚未建立 | [docs/release/PRODUCTION_LAUNCH_TODO.md](docs/release/PRODUCTION_LAUNCH_TODO.md) |

> production（生产环境）仍是上线待办状态。不要把模板文档、示例域名或本地 Docker（容器化运行环境）当成生产已部署事实。

## 仓库结构

```text
ypat-workspace/
├── backend/                    # Spring Cloud（Java 微服务）业务后端
│   ├── system-wap/             # 小程序 / H5 业务 API
│   ├── system-restapi/         # REST API（通用接口服务）
│   ├── system-web/             # 管理后台后端
│   ├── system-sso/             # SSO（单点登录）服务
│   ├── system-domain/          # 领域层
│   ├── system-object/          # DTO（数据传输对象）
│   └── system-security/        # 安全认证模块
├── backend-base/               # 基础服务：Eureka / Config / Hystrix / Turbine / Zipkin
├── frontend/                   # 新版 UniApp（跨端前端框架）小程序 / H5
├── frontend-admin/             # 新版管理后台前端
├── frontend-website/           # 官网 / 展示站
├── 91pai-master/               # 旧版小程序，仅作兼容和参考
├── docker-compose.yml          # 本地 development Docker Compose（容器编排）
├── docker-compose.override.yml # 本地后端 jar 快速重启覆盖配置
├── docker-compose.staging.yml  # staging（预发环境）
├── docker-compose.production.yml # production（生产模板）
├── scripts/                    # 本地启动、部署、检查脚本
└── docs/                       # 开发、部署、架构、迁移、发布文档
```

## 技术栈

### 后端

- Java 8
- Spring Boot 1.5.x + Spring Cloud Edgware
- Maven（Java 构建工具）
- MySQL、Redis
- Eureka（服务发现）
- FastDFS / Tencent COS（对象存储，按环境配置）
- 微信登录、微信支付、消息通知

### 前端

- `frontend/`：UniApp（跨端前端框架）+ Vue 3 + TypeScript + Vite
- `frontend-admin/`：Vue 3 + TypeScript + Vite
- 包管理统一使用 pnpm（高性能包管理器），不要混用 npm/yarn

### 本地运行

- Docker Desktop（本地容器运行环境）
- Docker Compose（容器编排工具）
- 微信开发者工具（调试 `mp-weixin` 微信小程序）

## 快速开始

### 1. 准备环境变量

```bash
cd /Users/lizhenwei/workspace/vueworkspace/ypat-workspace
cp .env.example .env
```

按实际本地环境修改 `.env`。真实密钥、微信支付证书、数据库密码不能提交到 Git（版本控制系统）。

环境变量权威说明见 [docs/config/ENVIRONMENT_VARIABLES.md](docs/config/ENVIRONMENT_VARIABLES.md)。

### 2. 首次启动本地 Docker 后端环境

```bash
cd /Users/lizhenwei/workspace/vueworkspace/ypat-workspace

# 本地 override 会让 restapi / wap 直接复制 target jar，因此首次启动前先打包
cd backend
mvn -pl system-restapi,system-wap -am package -DskipTests -B
cd ..

docker compose up -d
docker compose ps
```

默认会启动：

- `mysql`
- `redis`
- `eureka`
- `restapi`
- `wap`
- `system-web`
- `admin-web`
- `nginx`

本地端口、健康检查、FastDFS（文件存储）等完整说明见 [docs/development/LOCAL_DEVELOPMENT.md](docs/development/LOCAL_DEVELOPMENT.md)。

### 3. 修改后端代码后的最快重启方式

本地后端已支持“本机 Maven 打 jar，Docker 只复制 jar 并重启对应服务”的轻量流程。
首次 `docker compose up -d` 成功后，日常改代码只需要跑下面的脚本。

```bash
cd /Users/lizhenwei/workspace/vueworkspace/ypat-workspace

# 只改了 backend/system-restapi/
scripts/restart-docker-backend.sh restapi

# 只改了 backend/system-wap/
scripts/restart-docker-backend.sh wap

# 改了公共模块，或不确定影响哪个服务
scripts/restart-docker-backend.sh all
```

判断规则：

- 改 `backend/system-restapi/`：重启 `restapi`
- 改 `backend/system-wap/`：重启 `wap`
- 改 `backend/system-domain/`、`system-object/`、`system-security/`、`system-sso/`：重启 `all`

详细说明见 [docs/deploy/LOCAL_BUILD_AND_DOCKER.md](docs/deploy/LOCAL_BUILD_AND_DOCKER.md)。

### 4. 启动新版小程序 / H5

```bash
cd frontend
pnpm install --frozen-lockfile

# H5（浏览器调试）
pnpm run dev:h5

# 微信小程序
pnpm run dev:mp-weixin
```

微信小程序产物位于 `frontend/dist/dev/mp-weixin`，用微信开发者工具导入该目录。

### 5. 启动管理后台前端

```bash
cd frontend-admin
pnpm install --frozen-lockfile
pnpm run dev
```

## 常用命令

### 后端

```bash
# 打包 restapi
cd backend
mvn -pl system-restapi -am package -DskipTests -B

# 打包 wap
cd backend
mvn -pl system-wap -am package -DskipTests -B

# 同时打包 restapi + wap
cd backend
mvn -pl system-restapi,system-wap -am package -DskipTests -B
```

### 前端

```bash
# 新版 UniApp 前端
cd frontend
pnpm run type-check
pnpm run lint
pnpm run test
pnpm run build:mp-weixin

# 管理后台前端
cd frontend-admin
pnpm run type-check
pnpm run lint:check
pnpm run test
pnpm run build
```

### Docker

```bash
# 查看本地容器
docker compose ps

# 查看后端日志
docker compose logs -f restapi wap

# 重启指定服务，不动依赖服务
docker compose up -d --build --no-deps wap
```

## 核心业务模块

- 用户：微信登录、资料管理、实名认证、会员状态
- 约拍：发布、列表、详情、申请、收藏、审核
- 支付：微信支付、保证金 / 拍拍豆、订单回调、账单流水
- 内容：Banner（轮播图）、文章、活动配置
- 后台：实名审核、用户管理、约拍审核、配置管理

具体接口、表结构和迁移进度以代码、数据库迁移脚本、`docs/migration/` 和 `docs/release/` 为准，不在根 README 重复维护。

## 重要文档

| 主题 | 文档 |
| --- | --- |
| 本地开发 | [docs/development/LOCAL_DEVELOPMENT.md](docs/development/LOCAL_DEVELOPMENT.md) |
| 本地后端快速重启 | [docs/deploy/LOCAL_BUILD_AND_DOCKER.md](docs/deploy/LOCAL_BUILD_AND_DOCKER.md) |
| 环境变量 | [docs/config/ENVIRONMENT_VARIABLES.md](docs/config/ENVIRONMENT_VARIABLES.md) |
| 环境 / 部署导航 | [DEPLOY_ENVS.md](DEPLOY_ENVS.md) |
| staging 部署 | [docs/release/STAGING_DEPLOYMENT.md](docs/release/STAGING_DEPLOYMENT.md) |
| staging 回滚 | [docs/release/STAGING_ROLLBACK.md](docs/release/STAGING_ROLLBACK.md) |
| production 上线待办 | [docs/release/PRODUCTION_LAUNCH_TODO.md](docs/release/PRODUCTION_LAUNCH_TODO.md) |
| 密钥管理 | [docs/release/SECRET_MANAGEMENT.md](docs/release/SECRET_MANAGEMENT.md) |
| 数据库迁移 | [docs/release/DATABASE_MIGRATION.md](docs/release/DATABASE_MIGRATION.md) |
| 事故复盘 | [docs/deploy/LESSONS.md](docs/deploy/LESSONS.md) |
| Java / Spring 升级 ADR | [docs/architecture/ADR-JAVA-SPRING-UPGRADE.md](docs/architecture/ADR-JAVA-SPRING-UPGRADE.md) |

## 开发注意事项

1. 本地开发优先使用 `docker-compose.yml` + `.env`，不要连接 staging / production 数据库。
2. 后端运行必须按 Java 8 约束处理，详见 [docs/architecture/ADR-JAVA-SPRING-UPGRADE.md](docs/architecture/ADR-JAVA-SPRING-UPGRADE.md)。
3. 前端统一使用 pnpm（高性能包管理器）。
4. 真实密钥、证书、私有域名、数据库密码不能提交。
5. 旧版 `91pai-master/` 只作为兼容参考，优先维护新版 `frontend/`。

## 提交流程建议

提交前按改动范围做最小必要验证：

- 后端代码：执行对应模块 Maven（Java 构建工具）打包或测试
- 新版小程序：执行 `pnpm run type-check`、`pnpm run lint`、相关测试或目标构建
- 管理后台：执行 `pnpm run type-check`、`pnpm run lint:check`、相关测试或目标构建
- Docker 配置：执行 `docker compose config --quiet`

不要把本地 `.env`、证书、构建产物、调试日志提交到仓库。
