# 生产环境回滚指南（模板）

> 文档日期：2026-06-29 · 基线 SHA：b4af32a · 负责人：devops
> ⚠️ **当前 production 尚未建立**。本文档为模板。
> 文档中所有 production 地址仅为**示例占位**：`production.example.invalid`。

## 0. 范围声明（必读）

**只回滚**：

- ✅ 前端 H5 静态产物
- ✅ 后端 Docker 镜像
- ✅ 主机 Nginx 配置

**不通过此流程回滚**：

- ❌ **数据库 schema / 数据** —— 由 DBA 独立评估
- ❌ 微信 / 百度 / 支付平台密钥
- ❌ 第三方 SaaS 配置

## 1. 强制安全门

任何 production 回滚必须满足**全部**条件：

| # | 条件 |
| --- | --- |
| 1 | 显式传入 `--confirm-production` 标志 |
| 2 | 至少 2 名维护者在线（4-eyes） |
| 3 | 已通知值班群 + 业务方 |
| 4 | 当前故障已记录到 [`INCIDENT_RESPONSE.md`](INCIDENT_RESPONSE.md) 时间线 |
| 5 | 数据库全量备份已就绪（即便不回滚 DB） |

## 2. 回滚入口（占位脚本契约）

```bash
# 强制确认；缺失则退出非 0
./scripts/deploy/rollback-production.sh \
  --confirm-production \
  --target=<commit-sha-or-release-tag>
```

## 3. 回滚流程

### 3.1 决策

| 故障类型 | 决策 |
| --- | --- |
| 前端白屏 / 404 | 仅回滚前端 |
| API 5xx | 回滚后端镜像 |
| 数据库迁移后异常 | **不回滚代码**，由 DBA 评估数据回滚（独立流程） |
| TLS / HTTPS 异常 | 仅回滚主机 Nginx |
| 微信小程序审核驳回 | 业务侧处理，不在回滚范围 |

### 3.2 一键回滚

```bash
./scripts/deploy/rollback-production.sh \
  --confirm-production \
  --target=<release-tag>
```

脚本行为（与 staging 一致但更严格）：

1. 校验 `--confirm-production`。
2. 双人确认（脚本读取 stdin 二次密码 / 一次性 token）。
3. 备份当前 release 状态（前端 + 镜像清单）。
4. 切换 `current` 符号链接。
5. `docker compose -f docker-compose.production.yml up -d`。
6. `nginx -t && systemctl reload nginx`。
7. 输出 smoke test 列表 + 监控 dashboard。

### 3.3 数据库回滚（独立流程）

> ⚠️ **永远不**通过 `rollback-production.sh` 回滚数据库。

```text
1. DBA 评估故障时间窗口 + 影响范围。
2. 立即停止写流量（Nginx 503 / API 网关熔断）。
3. 全量备份当前 DB（保留事故现场）。
4. 选择回滚策略：
   - PITR（Point-In-Time Recovery）
   - 全量恢复 + binlog replay
   - 逆向 DDL + DML（见 [`DATABASE_MIGRATION.md`](DATABASE_MIGRATION.md) §"回滚"）
5. 在维护窗口内执行；恢复后逐表校验行数 + 关键业务指标。
6. 通知业务方逐项验证。
```

## 4. 回滚后验证

- [ ] 主页 / 关键 API / admin / files 全部 2xx
- [ ] 监控 ERROR 无新增
- [ ] TLS 握手 + HSTS 正常
- [ ] 微信小程序后台域名仍合法（回滚前后域名不变）
- [ ] 业务方关键路径冒烟通过（登录 / 支付 / 实名 / 报名 / 消息）

## 5. 事故复盘

回滚成功后必须：

1. 24h 内完成事故复盘（[`INCIDENT_RESPONSE.md`](INCIDENT_RESPONSE.md) §"复盘"）。
2. 在仓库 issue 中归档 RCA + action items。
3. 更新 [`PRODUCTION_LAUNCH_TODO.md`](PRODUCTION_LAUNCH_TODO.md) 与 [`BACKUP_AND_RECOVERY.md`](BACKUP_AND_RECOVERY.md)。

## 6. 相关文档

- 生产部署：[`PRODUCTION_DEPLOYMENT.md`](PRODUCTION_DEPLOYMENT.md)
- 数据库迁移：[`DATABASE_MIGRATION.md`](DATABASE_MIGRATION.md)
- 备份恢复：[`BACKUP_AND_RECOVERY.md`](BACKUP_AND_RECOVERY.md)
- 事故响应：[`INCIDENT_RESPONSE.md`](INCIDENT_RESPONSE.md)
- 密钥管理：[`SECRET_MANAGEMENT.md`](SECRET_MANAGEMENT.md)
- 环境隔离：[`ENVIRONMENT_ISOLATION.md`](ENVIRONMENT_ISOLATION.md)