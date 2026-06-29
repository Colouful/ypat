# 生产环境上线待办（PRODUCTION_LAUNCH_TODO.md）

> 文档日期：2026-06-29 · 基线 SHA：b4af32a · 维护：devops + 后端 + 前端

## 0. 状态声明

- ✅ **staging**：已部署（详见 [`STAGING_DEPLOYMENT.md`](STAGING_DEPLOYMENT.md)）
- ❌ **production：未建立**。任何"production 已部署 / 当前 production 在 HTTP IP"的说法均为**过时占位**，禁止作为事实陈述。
- 阻塞项见 §2；解除后依据 [`PRODUCTION_DEPLOYMENT.md`](PRODUCTION_DEPLOYMENT.md) 模板启动上线。

## 1. 为什么不能直接上线

- 后端 `system-wap` 基于 Spring Boot 1.5 + Java 8；详见 [`../architecture/ADR-JAVA-SPRING-UPGRADE.md`](../architecture/ADR-JAVA-SPRING-UPGRADE.md)。
- 微信支付 / 百度 OCR 等第三方平台密钥**未配置**到 production 通道。
- 数据库迁移链路**未自动化**（见 [`DATABASE_MIGRATION.md`](DATABASE_MIGRATION.md)）。
- 监控 / 告警 / 值班体系**仅覆盖 staging**。
- `frontend/.env.production` 只能写占位（`production.example.invalid`），真实域名未确定。

## 2. 阻塞项清单

### 2.1 基础设施

- [ ] **生产域名**：`api.<production-domain>` / `static.<production-domain>` / `<production-domain>` / `<admin-host>` —— 备案 + DNS + HSTS preload
- [ ] **生产服务器**：CPU / 内存 / 磁盘 / 带宽 / 防火墙策略
- [ ] **生产数据库**：独立实例（**禁止**复用 staging）；账号 + 强密码；PITR 配置
- [ ] **生产 Redis**：独立实例；AOF；强密码
- [ ] **生产 FastDFS**：独立 tracker / storage；异地副本
- [ ] **对象存储（可选）**：用于静态资源 / 备份异地镜像

### 2.2 网络 / 安全

- [ ] **TLS 证书**：公共 CA（Let's Encrypt / 商业 CA）；自动续期；OCSP stapling
- [ ] **Nginx**：维护页 / 限流 / CC 防护 / WAF
- [ ] **DDoS 防护**：CDN / 高防 IP
- [ ] **SSH**：仅密钥登录；堡垒机；4-eyes 双人确认

### 2.3 密钥 / 凭据

- [ ] KMS / Secret 系统开通 production 命名空间
- [ ] 微信支付 / 公众号 / 小程序 AppID + AppSecret + 商户号 + API 密钥
- [ ] 百度 OCR / 身份核验 / 登录 AK + SK
- [ ] 数据库 / Redis / RabbitMQ / SSO 独立强密码
- [ ] JWT signing key（与 staging 完全不同）
- [ ] 短信平台签名 + 模板 + 密钥（`YPAT_SMS_MOCK_ENABLED=false` 强制）
- [ ] 详见 [`SECRET_MANAGEMENT.md`](SECRET_MANAGEMENT.md) §1 分类表

### 2.4 应用配置

- [ ] `frontend/.env.production` 使用真实生产域名（不要写 staging IP / `panghu.work`）
- [ ] `frontend/src/config/env.ts` 启动校验通过（已拒 `panghu.work` 等串用域名）
- [ ] `backend/src/main/resources/pro/`（pro profile）资源完整
- [ ] Maven `<resources>` 隔离生效（见基线 SHA `b4af32a`）

### 2.5 数据库

- [ ] Flyway 引入（依赖 [`../architecture/ADR-JAVA-SPRING-UPGRADE.md`](../architecture/ADR-JAVA-SPRING-UPGRADE.md) 阶段 P3）
- [ ] 手工迁移脚本（`backend/dev/mysql/V*.sql`）全部完成 staging 验证
- [ ] 备份策略落地（[`BACKUP_AND_RECOVERY.md`](BACKUP_AND_RECOVERY.md) §2）
- [ ] PITR 演练 ≥ 1 次

### 2.6 监控 / 告警 / 值班

- [ ] APM（Pinpoint / SkyWalking）
- [ ] 指标（Prometheus + Grafana）
- [ ] 日志（ELK / Loki）
- [ ] 告警（值班电话 / 短信 / 飞书）
- [ ] 值班表（[`INCIDENT_RESPONSE.md`](INCIDENT_RESPONSE.md) §2）
- [ ] Runbook（[`INCIDENT_RESPONSE.md`](INCIDENT_RESPONSE.md) §6）

### 2.7 第三方平台

- [ ] 微信小程序后台配置 production request / uploadFile / downloadFile 合法域名
- [ ] 微信支付回调域名
- [ ] 微信公众号 OAuth 回调域名
- [ ] 百度开放平台应用包名 / 签名（移动端）

### 2.8 文档 / 合规

- [ ] 隐私政策 / 用户协议 / ICP 备案
- [ ] 数据保护合规（个人信息保护法）
- [ ] 渗透测试报告（≥ 1 次）

## 3. 解锁后的上线路径

```text
1. 完成 §2 全部阻塞项；devops + 业务方双方签字
2. 工单创建 → ≥ 2 名维护者 approve
3. staging 灰度验证 ≥ 24h
4. 维护窗口：执行生产部署（详见 PRODUCTION_DEPLOYMENT.md）
5. 验证：smoke test + 监控 + 业务冒烟
6. 维护窗口结束：通知业务方 + 客服
7. 30 天观察期 → 转交 SRE 长期运维
```

## 4. 变更历史

| 日期 | 变更 | 备注 |
| --- | --- | --- |
| 2026-06-29 | 重写：删除"已建立" / "HTTP IP" 等过时描述；明确阻塞项 | 本任务基线 `b4af32a` |
| 2026-06-28 | 上一版（已归档） | 保留作为历史 |

## 5. 相关文档

- 生产部署（模板）：[`PRODUCTION_DEPLOYMENT.md`](PRODUCTION_DEPLOYMENT.md)
- 生产回滚（模板）：[`PRODUCTION_ROLLBACK.md`](PRODUCTION_ROLLBACK.md)
- 环境隔离：[`ENVIRONMENT_ISOLATION.md`](ENVIRONMENT_ISOLATION.md)
- 密钥管理：[`SECRET_MANAGEMENT.md`](SECRET_MANAGEMENT.md)
- 数据库迁移：[`DATABASE_MIGRATION.md`](DATABASE_MIGRATION.md)
- 备份恢复：[`BACKUP_AND_RECOVERY.md`](BACKUP_AND_RECOVERY.md)
- 事故响应：[`INCIDENT_RESPONSE.md`](INCIDENT_RESPONSE.md)
- ADR：[`../architecture/ADR-JAVA-SPRING-UPGRADE.md`](../architecture/ADR-JAVA-SPRING-UPGRADE.md)
- 仓库根导航：[`../../DEPLOY_ENVS.md`](../../DEPLOY_ENVS.md)