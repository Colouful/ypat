# 本地开发指南

> 适用对象：YPAT 项目所有开发者。
> 目标：在本地机器上完整跑起 `development` 环境（后端微服务 + 前端 H5 + MySQL + Redis + FastDFS），便于日常开发、调试、冒烟。
> 文档日期：2026-06-29 · 基线 SHA：b4af32a · 负责人：frontend / backend / devops

## 1. 环境拓扑

`development` 环境是**纯本地**环境，所有服务跑在开发者机器上，**不应**触及任何 staging / production 资源。

```
localhost (macOS / Linux 开发者机器)
 ├─ pnpm dev:h5            → Vite 5173 (H5)
 ├─ pnpm dev:mp-weixin     → 微信开发者工具
 ├─ docker compose (docker-compose.yml)
 │   ├─ mysql  3307        → YPAT 数据库 + 测试库
 │   ├─ redis  6379        → 缓存 / 会话 / 限流
 │   ├─ eureka 8761        → 服务发现
 │   ├─ restapi 9081       → system-restapi
 │   ├─ wap     8081       → system-wap
 │   └─ system-web 8082    → 后台
 └─ docker compose (backend/dev/fastdfs/docker-compose.staging.yml，本地复用)
     ├─ fastdfs-tracker 22122
     └─ fastdfs-storage 23000 + nginx 8888
```

**关键约束**：

- 所有端口都以 `YPAT_LOCAL_*` 环境变量覆盖，默认值见 `docs/config/ENVIRONMENT_VARIABLES.md`。
- 所有密钥、密码通过 `backend/.env.development` / `frontend/.env.development` 注入，**禁止硬编码到代码或提交到仓库**。
- 本地环境**不得**连接 staging 或 production 数据库、Redis、对象存储。

## 2. 前置依赖

| 工具 | 版本 | 说明 |
| --- | --- | --- |
| Node.js | 20.x LTS | 前端构建运行 |
| pnpm | 9.x | 包管理（**不要混用 npm / yarn**） |
| Docker Desktop | 4.x+ | 本地 MySQL / Redis / FastDFS |
| Docker Compose | v2 (`docker compose`) | 容器编排 |
| Java JDK | 8 (1.8.0_xxx) | 后端构建运行（详见下文 §7） |
| Maven | 3.8+ | 后端构建 |
| 微信开发者工具 | 最新稳定版 | 调试 mp-weixin |

> **包管理器统一**：前端仓库根目录有 `packageManager` 字段锁定 pnpm。CI 和本地必须保持一致。

## 3. 克隆与初始化

```bash
# 1. 克隆仓库
git clone <repo-url> ypat-workspace
cd ypat-workspace

# 2. 切到目标分支
git checkout minimax/environment-governance

# 3. 复制环境变量模板
cp .env.example backend/.env.development
cp .env.example backend/.env.staging.example
cp .env.example backend/.env.production.example
cp frontend/.env.development.example frontend/.env.development
cp frontend/.env.staging.example frontend/.env.staging
cp frontend/.env.production.example frontend/.env.production
```

> 所有 `.example` 文件只包含占位符和示例域名（`production.example.invalid` / `staging.example.invalid`），**禁止**填入真实密钥。

## 4. 启动后端（Docker Compose）

```bash
# 在仓库根目录
docker compose --env-file backend/.env.development up -d
# 单独启动 FastDFS（与 staging 同源，便于本地验证）
docker compose --env-file backend/.env.development \
  -f backend/dev/fastdfs/docker-compose.staging.yml up -d

# 等待 MySQL 初始化完成（约 30s）
docker compose logs -f mysql | grep -i "ready for connections"

# 健康检查
curl -sf http://localhost:8761/actuator/health || echo "eureka not ready"
```

## 5. 启动后端微服务（IDE / Maven）

```bash
# 在 backend/ 目录
cd backend

# 编译（dev profile，激活 dev 配置）
mvn -Pdev clean install -DskipTests

# 单模块启动示例（system-wap）
cd system-wap
mvn -Pdev spring-boot:run

# 其它模块：system-restapi / system-web / system-sso / gateway
```

各模块的 `dev` Profile 资源位于 `src/main/resources/dev/`，**不会被 staging/production 镜像读到**（见 `docs/release/SECRET_MANAGEMENT.md`）。

## 6. 启动前端

### 6.1 H5

```bash
cd frontend
pnpm install --frozen-lockfile
pnpm run dev:h5                 # development
# 或
pnpm run dev:h5:staging         # 对接本地启的 staging 镜像（不推荐日常）
```

浏览器访问 <http://localhost:5173>。

### 6.2 微信小程序

```bash
cd frontend
pnpm install --frozen-lockfile
pnpm run dev:mp-weixin
# 产物在 dist/dev/mp-weixin，用微信开发者工具"导入项目"打开
```

## 7. Java 版本说明（重要）

> **当前 `system-wap` 基于 Spring Boot 1.5 + CGLIB 模块化限制，必须使用 Java 8 运行；测试可在 Java 17 通过。**
> 长期方案见 `docs/architecture/ADR-JAVA-SPRING-UPGRADE.md`。

```bash
# macOS（jenv 多版本管理）
brew install jenv
jenv install 8
jenv install 17
jenv local 8        # 在 backend/ 目录锁定为 Java 8

# 验证
java -version
mvn -v
```

**禁止**在 backend/ 直接 `java -version` 输出 17 但启动 system-wap，会出现：

```
InaccessibleObjectException: Unable to make protected ... accessible:
module java.base does not "opens java.lang" to unnamed module
```

## 8. 数据库 / Redis / FastDFS 调试

```bash
# MySQL
docker exec -it ypat-mysql mysql -uroot -p"${YPAT_LOCAL_MYSQL_ROOT_PASSWORD}" ypat

# Redis
docker exec -it ypat-redis redis-cli -a "${YPAT_LOCAL_REDIS_PASSWORD}"

# FastDFS 上传 / 查看文件
docker exec -it ypat-fastdfs-storage /bin/bash
# 容器内：/usr/bin/fdfs_upload_file /etc/fdfs/client.conf /tmp/test.png
```

> 调试完毕后请清理测试数据，**禁止**把开发数据同步到 staging。

## 9. 端口冲突排查

| 现象 | 排查 |
| --- | --- |
| `bind: address already in use` | 修改 `backend/.env.development` 中 `YPAT_LOCAL_*_PORT` |
| `npm run dev:h5` 端口被占 | 修改 `vite.config.ts` 中 `server.port` |
| FastDFS 上传失败 | 检查 `YPAT_FASTDFS_IMAGE` 是否包含 `@sha256:...` digest |

## 10. 常见任务速查

| 任务 | 命令 |
| --- | --- |
| 跑所有后端单元测试 | `cd backend && mvn -Pdev test` |
| 跑前端 lint + type-check | `cd frontend && pnpm run check` |
| 跑前端测试 | `cd frontend && pnpm run test` |
| 重置本地数据库 | `docker compose down -v && docker compose up -d mysql` |
| 查看 Eureka 注册情况 | 浏览器访问 <http://localhost:8761> |

## 11. 提交规范

提交前必须满足（CI 卡点）：

1. `git status` 干净。
2. `pnpm run check` 通过。
3. `mvn -Pdev test` 通过。
4. 没有真实密钥 / IP / 域名进入 diff。

详细规范见 `docs/release/GITHUB_BRANCH_PROTECTION.md` 与 `docs/release/SECRET_MANAGEMENT.md`。

## 12. 相关文档

- 环境变量清单：`docs/config/ENVIRONMENT_VARIABLES.md`
- 密钥管理：`docs/release/SECRET_MANAGEMENT.md`
- 数据库迁移：`docs/release/DATABASE_MIGRATION.md`
- 备份恢复：`docs/release/BACKUP_AND_RECOVERY.md`
- 事故响应：`docs/release/INCIDENT_RESPONSE.md`
- 三环境隔离矩阵：`docs/release/ENVIRONMENT_ISOLATION.md`
- Java 升级 ADR：`docs/architecture/ADR-JAVA-SPRING-UPGRADE.md`
- 部署到预发：`docs/release/STAGING_DEPLOYMENT.md`
- 部署到生产：`docs/release/PRODUCTION_DEPLOYMENT.md`