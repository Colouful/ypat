# YPAT 环境变量权威清单

**本文件是 YPAT 项目环境变量的唯一权威清单。**
其他文档、PR description、CHANGELOG 不得重复维护变量列表，只引用本文件。

> 详细 CSV 清单（含使用模块 / 必填性 / 敏感性 / 负责人 / 轮换要求）见：
> [`docs/release/environment-governance/artifacts/env-variable-inventory.csv`](../release/environment-governance/artifacts/env-variable-inventory.csv)

最后更新：2026-06-29
生成方式：从代码实际读取扫描（`grep -RhoE '\$\{[A-Z0-9_]+(:[^}]*)?\}'`）

---

## 1. 命名空间约定

- **YPAT_*** — YPAT 项目自有变量
- **VITE_*** — 前端 Vite 注入变量（仅前端）
- **SPRING_*** — Spring Boot 原生属性
- **YPAT_LOCAL_*** — 本地开发 compose 端口/密码
- **YPAT_FASTDFS_*** — FastDFS 镜像 digest 与数据目录
- **YPAT_FDFS_*** — FastDFS 业务 URL / tracker 地址

## 2. 三环境公共骨架

所有环境（dev / staging / production）必须存在的变量：

| 类别 | 变量 | dev | staging | production |
|------|------|-----|---------|------------|
| MySQL | `YPAT_MYSQL_URL` | 必填 | 必填 | 必填 |
| MySQL | `YPAT_MYSQL_USERNAME` | 必填 | 必填 | 必填 |
| MySQL | `YPAT_MYSQL_PASSWORD` | 必填 | 必填 | 必填 |
| MySQL | `YPAT_DB_NAME` | 必填 | 必填 | 必填 |
| MySQL | `YPAT_DB_USERNAME` | 必填 | 必填 | 必填 |
| MySQL | `YPAT_DB_PASSWORD` | 必填 | 必填 | 必填 |
| MySQL (运维) | `YPAT_LOCAL_MYSQL_ROOT_PASSWORD` | dev 占位 | 必填 | 必填 |
| Redis | `YPAT_REDIS_HOST` | 必填 | 必填 | 必填 |
| Redis | `YPAT_REDIS_PORT` | 必填 | 必填 | 必填 |
| Redis | `YPAT_REDIS_PASSWORD` | 必填 | 必填 | 必填 |
| Eureka | `YPAT_EUREKA_DEFAULT_ZONE` | 必填 | 必填 | 必填 |
| JWT/SSO | `YPAT_SSO_JWT_SIGNING_KEY` | dev 占位 | 必填 | 必填 |
| JWT/SSO | `YPAT_SSO_CLIENT0_SECRET` | dev 占位 | 必填 | 必填 |
| JWT/SSO | `YPAT_SSO_CLIENT1_SECRET` | dev 占位 | 必填 | 必填 |
| 微信小程序 | `YPAT_WX_APP_ID` | 可空 | 必填 | 必填 |
| 微信小程序 | `YPAT_WX_APP_SECRET` | 可空 | 必填 | 必填 |
| 微信公众号 | `YPAT_WX_PUB_APP_ID` | 可空 | 必填 | 必填 |
| 微信公众号 | `YPAT_WX_PUB_APP_SECRET` | 可空 | 必填 | 必填 |
| 微信支付 | `YPAT_WX_MCH_ID` | 可空 | 必填 | 必填 |
| 微信支付 | `YPAT_WX_PAY_KEY` | 可空 | 必填 | 必填 |
| 百度 OCR | `YPAT_BD_IDCARD_AK` / `YPAT_BD_IDCARD_SK` | 可空 | 必填 | 必填 |
| 百度 OCR | `YPAT_BD_IDMATCH_AK` / `YPAT_BD_IDMATCH_SK` | 可空 | 必填 | 必填 |
| 百度 OCR | `YPAT_WEB_BD_OCR_AK` / `YPAT_WEB_BD_OCR_SK` | 可空 | 必填 | 必填 |
| 百度 AI | `YPAT_BD_APP_KEY` / `YPAT_BD_APP_SECRET` | 可空 | 必填 | 必填 |
| 短信 | `YPAT_SMS_MOCK_ENABLED` | 可空 | — | — |
| 短信 | `YPAT_SMS_MOCK_PHONE` | 可空 | — | — |
| 短信 | `YPAT_SMS_MOCK_CODE` | 可空 | — | — |
| FastDFS | `YPAT_FDFS_PUBLIC_BASE_URL` | 必填 | 必填 | 必填 |
| FastDFS | `YPAT_FDFS_TRACKER_SERVERS` | 必填 | 必填 | 必填 |
| FastDFS | `YPAT_FASTDFS_IMAGE` | 必填 | 必填 | 必填 |
| FastDFS | `YPAT_FASTDFS_TRACKER_DATA_DIR` | 必填 | 必填 | 必填 |
| FastDFS | `YPAT_FASTDFS_STORAGE_DATA_DIR` | 必填 | 必填 | 必填 |
| 日志 | `YPAT_LOG_DIR` | 必填 | 必填 | 必填 |
| 端口 | `YPAT_LOCAL_WAP_PORT` / `YPAT_LOCAL_WEB_PORT` | 必填 | 必填 | 必填 |
| 端口 | `YPAT_LOCAL_RESTAPI_PORT` / `YPAT_LOCAL_EUREKA_PORT` | 必填 | 必填 | 必填 |
| Spring | `SPRING_PROFILES_ACTIVE` | dev | pre | pro |

> 完整字段（必填性 / 敏感性 / 负责人 / 轮换要求）见 CSV。

## 3. 密钥轮换要求

| 变量 | 轮换周期 | 触发条件 |
|------|---------|----------|
| `YPAT_SSO_JWT_SIGNING_KEY` | 90 天 | 强制 |
| `YPAT_SSO_CLIENT0_SECRET` / `YPAT_SSO_CLIENT1_SECRET` | 90 天 | 强制 |
| `YPAT_WX_APP_SECRET` / `YPAT_WX_PUB_APP_SECRET` / `YPAT_WX_PAY_KEY` | 90 天 | 强制 |
| `YPAT_BD_*` (AK/SK) | 90 天 | 强制 |
| `YPAT_LOCAL_MYSQL_ROOT_PASSWORD` | 90 天 | 强制 |
| `YPAT_LOCAL_REDIS_PASSWORD` | 90 天 | 强制 |
| `YPAT_DB_PASSWORD` | 90 天 | 强制 |

详见 [`SECRET_MANAGEMENT.md`](../release/SECRET_MANAGEMENT.md)

## 4. 禁止的占位符

部署门禁（`scripts/deploy/preflight.sh`）会拒绝以下占位符：

```
CHANGE_ME
placeholder
<placeholder>
TODO
example.invalid
```

## 5. 环境互窜检测

`scripts/deploy/preflight.sh` 会检测：

| 检测项 | 触发条件 |
|--------|----------|
| production 环境引用 `panghu.work` | 立即失败 |
| production 环境引用 `82.156.14.216` | 立即失败 |
| production 环境引用 `www.panghu.work` | 立即失败 |
| staging 环境引用 `localhost` | 立即失败 |
| 镜像使用 `:latest` 标签 | 立即失败 |

## 6. 注入方式

- **本地开发**：`.env`（gitignored）或 shell 环境变量
- **CI**：GitHub Actions secrets + workflow `env` 块
- **staging 服务器**：`/opt/ypat/.env`（权限 600）
- **production 服务器**（将来）：HashiCorp Vault / 阿里云 KMS / 腾讯云密钥管理系统

详见 [`SECRET_MANAGEMENT.md`](../release/SECRET_MANAGEMENT.md) 与 [`DEPLOY_ENVS.md`](../../DEPLOY_ENVS.md)。