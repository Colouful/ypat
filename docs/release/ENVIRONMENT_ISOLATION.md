# 三环境隔离矩阵（ENVIRONMENT_ISOLATION.md）

> 文档日期：2026-06-29 · 基线 SHA：b4af32a · 维护：devops + DBA + backend lead + frontend lead
> 配套：[`SECRET_MANAGEMENT.md`](SECRET_MANAGEMENT.md) · [`../config/ENVIRONMENT_VARIABLES.md`](../config/ENVIRONMENT_VARIABLES.md)

## 0. 状态声明

| 环境 | 状态 |
| --- | --- |
| development | ✅ 常态（开发者本地） |
| staging | ✅ 已部署 |
| production | ❌ **未建立** —— 详见 [`PRODUCTION_LAUNCH_TODO.md`](PRODUCTION_LAUNCH_TODO.md) |

> ⚠️ 文档中所有 production 地址仅为**示例占位**：`production.example.invalid`。
> 任何"production 已部署" / "当前 production 在 HTTP IP / panghu.work"的说法均为**过时占位**。

## 1. 隔离矩阵

> ✅ = 必须隔离；🔁 = 同源不同凭据；🚫 = 禁止共享。

| 维度 | dev | staging | production |
| --- | --- | --- | --- |
| **域名** | localhost | `staging.example.invalid`（示例） | `production.example.invalid`（示例） |
| **HTTPS** | 否（HTTP） | ✅ 强制 | ✅ 强制 |
| **服务器 / 集群** | 开发者本机 | staging 主机 | production 主机 / 集群 |
| **Docker 网络** | 本地 compose net | `ypat_ypat-net` | `ypat_ypat-net-prod` |
| **数据库（实例）** | docker mysql 3307 | ypat_mysql_staging | ypat_mysql_production 🚫 |
| **数据库账号** | root / 本地弱密码 | staging 独立账号 | production 强密码 + 最小权限 🚫 |
| **Redis（实例）** | docker redis 6379 | ypat_redis_staging | ypat_redis_production 🚫 |
| **FastDFS（实例）** | docker fastdfs | ypat_fastdfs_staging | ypat_fastdfs_production 🚫 |
| **FastDFS 公开域** | http://localhost:8888 | https://<staging-domain>/files | https://<production-domain>/files 🚫 |
| **静态资源目录** | /var/www/_dev | /var/www/<staging-domain> | /var/www/<production-domain> 🚫 |
| **环境变量文件** | `.env.development` | `.env.staging` + `.env.staging.example` | `.env.production` + KMS 🚫 |
| **Maven Profile** | `dev` | `pre` | `pro` 🚫 |
| **Maven 资源过滤** | 只加载 `dev/` | 只加载 `pre/`（参见基线 `b4af32a`） | 只加载 `pro/` 🚫 |
| **Git 分支** | feature/* | `minimax/environment-governance` / `main`（staging 镜像） | `main` 唯一 |
| **Eureka 注册中心** | dev | staging | production 🚫 |
| **RabbitMQ** | 不使用 | ypat_rabbitmq_staging | ypat_rabbitmq_production 🚫 |
| **TLS 证书** | 自签 | staging CA | 公共 CA 🚫 |
| **密钥 / 凭据** | mock | staging 独立 | production 独立 + KMS 🚫 |
| **监控 / 告警通道** | 不接入 | staging 通道 | production 通道（独立）🚫 |
| **日志通道** | 本地 | staging 通道 | production 通道（独立）🚫 |
| **备份目的地** | 无 | `/opt/ypat-data/backups/` + 异地 OSS | 异地独立 OSS 🚫 |
| **值班表** | 无 | devops | SRE + devops 双值班 🚫 |
| **微信小程序后台域名** | 不配置 | staging request/uploadFile/downloadFile | production request/uploadFile/downloadFile 🚫 |
| **微信支付回调** | 不配置 | staging 回调 | production 回调 🚫 |

## 2. 强制隔离规则

### 2.1 域名与地址

- **禁止** staging 与 production 共用任何域名（包括子域）。
- **禁止** production 沿用 staging IP（HTTP 或 HTTPS）。
- **禁止** 在生产代码 / compose / 文档示例中写 `panghu.work` / `www.panghu.work` / `82.156.14.216` 等真实 staging 串。
- 示例占位：统一使用 `production.example.invalid` / `staging.example.invalid`。

### 2.2 资源过滤（Maven Profile）

`backend/pom.xml` / `backend-base/pom.xml` 已修复（基线 `b4af32a`）：

```xml
<resources>
  <resource>
    <directory>src/main/resources</directory>
    <excludes>
      <exclude>pre/**</exclude>
      <exclude>pro/**</exclude>
    </excludes>
  </resource>
  <!-- dev profile 才会包含 dev/** -->
  <resource>
    <directory>src/main/resources/dev</directory>
    <filtering>true</filtering>
  </resource>
</resources>
```

> 同理 `pre` / `pro` Profile。修复前 dev 构建会**泄露** pre 目录密钥；修复后必须显式 Profile 才加载。

### 2.3 前端启动校验

`frontend/src/config/env.ts` 已在 `production` / `staging` 模式下拒绝：

```ts
const FORBIDDEN_PRODUCTION_HOSTS = [
  'panghu.work',
  'www.panghu.work',
  'localhost',
  '127.0.0.1',
]
```

构建时会立即抛错。**禁止**通过修改源码绕过。

### 2.4 CI 扫描

- 真实 IP / 域名 / 邮箱 / 高熵字符串扫描 → PR 阻塞。
- `.env.*` 文件（非 `.example`）误提交 → 立即报警。
- 详见 [`GITHUB_BRANCH_PROTECTION.md`](GITHUB_BRANCH_PROTECTION.md)。

## 3. 切换检查清单

### 3.1 dev → staging

- [ ] 已提交本地代码并推送到 `minimax/environment-governance`
- [ ] CI 全绿
- [ ] `.env.staging` / `.env.staging.example` 已更新（占位即可）
- [ ] 已在 staging 预跑 [`scripts/deploy/preflight.sh`](../../scripts/deploy/preflight.sh)

### 3.2 staging → production

> ⚠️ 当前 production 未建立；本节为**未来模板**。

- [ ] §1 矩阵逐项 ✅（特别是 §2.1 / §2.4）
- [ ] [`PRODUCTION_LAUNCH_TODO.md`](PRODUCTION_LAUNCH_TODO.md) §2 全部阻塞项已解除
- [ ] 工单 ≥ 2 名维护者 approve
- [ ] staging 灰度 ≥ 24h

### 3.3 production → staging 回退（紧急）

- [ ] 仅当 staging 仍可用时执行
- [ ] 通知业务方 + 用户
- [ ] DNS 切回 staging
- [ ] 回退原因写入 [`INCIDENT_RESPONSE.md`](INCIDENT_RESPONSE.md) 时间线

## 4. 禁止事项

- ❌ staging 与 production 共用 KMS 命名空间
- ❌ 用 staging 凭据登录 production 机器
- ❌ 在 production 代码中保留 `// TODO: replace staging url` 注释
- ❌ 把 staging 数据库 dump 恢复到 production
- ❌ 把 production 备份导入 staging 调试（除非脱敏）

## 5. 相关文档

- 环境变量总览：[`../config/ENVIRONMENT_VARIABLES.md`](../config/ENVIRONMENT_VARIABLES.md)
- 变量清单（CSV）：[`environment-governance/artifacts/env-variable-inventory.csv`](environment-governance/artifacts/env-variable-inventory.csv)
- 密钥管理：[`SECRET_MANAGEMENT.md`](SECRET_MANAGEMENT.md)
- 预发部署：[`STAGING_DEPLOYMENT.md`](STAGING_DEPLOYMENT.md)
- 生产部署：[`PRODUCTION_DEPLOYMENT.md`](PRODUCTION_DEPLOYMENT.md)
- 生产待办：[`PRODUCTION_LAUNCH_TODO.md`](PRODUCTION_LAUNCH_TODO.md)
- 仓库根导航：[`../../DEPLOY_ENVS.md`](../../DEPLOY_ENVS.md)
- 本地开发：[`../development/LOCAL_DEVELOPMENT.md`](../development/LOCAL_DEVELOPMENT.md)