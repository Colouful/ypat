# 密钥管理（Secret Management）

> 文档日期：2026-06-29 · 基线 SHA：b4af32a · 负责人：devops + backend lead
> 权威清单：[`docs/release/environment-governance/artifacts/env-variable-inventory.csv`](environment-governance/artifacts/env-variable-inventory.csv)
> 环境变量总览：[`../config/ENVIRONMENT_VARIABLES.md`](../config/ENVIRONMENT_VARIABLES.md)

## 1. 分类与轮换基线

| 类别 | 示例变量 | 90 天强制轮换 | 备注 |
| --- | --- | --- | --- |
| 数据库密码 | `YPAT_LOCAL_MYSQL_ROOT_PASSWORD`, `YPAT_MYSQL_PASSWORD` | ✅ | 失效影响所有读写 |
| 缓存 / 队列密码 | `YPAT_LOCAL_REDIS_PASSWORD`, `YPAT_RABBITMQ_PASSWORD` | ✅ | Redis AUTH 强制 |
| 微信平台 | `YPAT_WX_APP_ID`, `YPAT_WX_APP_SECRET`, `YPAT_WX_MCH_ID`, `YPAT_WX_PAY_KEY`, `YPAT_WX_PUB_APP_ID`, `YPAT_WX_PUB_APP_SECRET` | 按微信平台策略 | 平台侧泄露需立即轮换 |
| 百度开放平台 | `YPAT_BD_APP_KEY`, `YPAT_BD_APP_SECRET`, `YPAT_BD_IDCARD_AK`, `YPAT_BD_IDCARD_SK`, `YPAT_BD_IDMATCH_AK`, `YPAT_BD_IDMATCH_SK`, `YPAT_WEB_BD_OCR_AK`, `YPAT_WEB_BD_OCR_SK` | ✅ 90d | ID OCR / 身份核验 / 登录 |
| Eureka / SSO | `YPAT_SSO_JWT_SIGNING_KEY`, `YPAT_SSO_CLIENT0_SECRET`, `YPAT_SSO_CLIENT1_SECRET` | ✅ 90d | 影响 token 验证 |
| TLS 私钥 | `/etc/nginx/ssl/<domain>.key` | 与证书周期一致 | 私钥泄露必须立即吊销 |
| FastDFS | tracker / storage 连接 | 否（固定 digest） | 见 [`SECRET_MANAGEMENT.md`](SECRET_MANAGEMENT.md) §5 |

> 标记 `yes` 的变量必须在 [`env-variable-inventory.csv`](environment-governance/artifacts/env-variable-inventory.csv) 中"轮换要求"列体现。本表作为治理基线，**禁止**任何环境使用默认值。

## 2. 90 天强制轮换

### 2.1 触发

- 每 90 天由密钥管理员发起轮换工单（月初例行）。
- 任意疑似泄露（CI 扫描命中 / 离职员工交接 / 第三方平台公告）→ 立即轮换，不等 90 天。

### 2.2 流程

1. 在 KMS / 部署平台生成新值；记录版本号 `vYYYYMMDD`。
2. 写入 staging → 验证关键接口（OAuth / 支付回调 / OCR）→ 观察 ≥ 24h。
3. 写入 production 双活：
   - **保守方案**：维护窗口内一次性切换；老值保留 7 天以便回滚。
   - **灰度方案**：双写 24h 后切读，最后切写（适用 JWT signing key）。
4. 在 `env-variable-inventory.csv` 中更新版本号与负责人。
5. 通知依赖方（微信 / 百度 / 短信平台是否需要回调配置变更）。

### 2.3 不允许

- ❌ 在 staging 与 production 使用相同值。
- ❌ 在 dev 与 staging 使用相同值（开发可以本地 mock）。
- ❌ 把生产密钥回写到 staging 调试。

## 3. 密钥保管

### 3.1 存储

| 环境 | 存放方式 |
| --- | --- |
| development | `.env.development`，权限 `chmod 600`；**禁止**提交到 Git |
| staging | 部署平台 Secret / 服务器 `/opt/ypat/.env`，权限 `chmod 600 root:root` |
| production | KMS / 部署平台 Secret，**禁止**写入镜像层、compose 明文、文档 |

### 3.2 流转

- 仅通过部署平台 / KMS 注入；CI / 开发者本地**不得**接触 production 密钥明文。
- 微信 / 百度等第三方凭据变更必须由对接负责人 + devops 双重确认。

### 3.3 Git 防护

- `.gitignore` 排除 `frontend/.env.*`、`backend/.env.*`（仅 `.example` 入库）。
- CI 卡点扫描：
  - 真实 IP / 域名 / 邮箱
  - 私钥、keystore、`.pem`
  - 高熵字符串（启发式）
- 命中 → PR 阻塞；详见 [`GITHUB_BRANCH_PROTECTION.md`](GITHUB_BRANCH_PROTECTION.md)。

## 4. 应急响应（疑似泄露）

```text
1. 立即冻结可疑密钥（平台侧禁用 / 删除）。
2. 在 KMS 中标记 incident；签发新值。
3. staging → production 顺序切换（同 §2.2）。
4. 审计日志：访问记录、调用记录、地理位置。
5. 24h 内出事故报告（[`INCIDENT_RESPONSE.md`](INCIDENT_RESPONSE.md)）。
6. 若涉及第三方平台，触发对应平台的事件响应流程。
```

## 5. FastDFS 镜像与文件存储密钥

- `YPAT_FASTDFS_IMAGE` 通过固定 digest（`@sha256:...`）锁定，**不视为密钥**，但禁止随意变更。
- FastDFS 内部通信 token（如启用）单独管理；当前部署未启用。

## 6. 责任矩阵

| 角色 | 责任 |
| --- | --- |
| devops | KMS / 服务器 Secret 注入、轮换执行 |
| backend lead | 后端密钥清单、staging 验证 |
| frontend lead | 前端 `VITE_*` / 微信 AppID 配置 |
| DBA | 数据库密码轮换与备份验证 |
| 安全 / 合规 | 事故响应、对外报告 |

## 7. 相关文档

- 环境变量总览：[`../config/ENVIRONMENT_VARIABLES.md`](../config/ENVIRONMENT_VARIABLES.md)
- 变量清单（CSV）：[`environment-governance/artifacts/env-variable-inventory.csv`](environment-governance/artifacts/env-variable-inventory.csv)
- 数据库迁移：[`DATABASE_MIGRATION.md`](DATABASE_MIGRATION.md)
- 备份恢复：[`BACKUP_AND_RECOVERY.md`](BACKUP_AND_RECOVERY.md)
- 事故响应：[`INCIDENT_RESPONSE.md`](INCIDENT_RESPONSE.md)
- 分支保护：[`GITHUB_BRANCH_PROTECTION.md`](GITHUB_BRANCH_PROTECTION.md)
- 本地开发：[`../development/LOCAL_DEVELOPMENT.md`](../development/LOCAL_DEVELOPMENT.md)