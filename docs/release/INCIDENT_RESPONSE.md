# 事故响应（Incident Response）

> 文档日期：2026-06-29 · 基线 SHA：b4af32a · 负责人：SRE on-call
> 配套：[`BACKUP_AND_RECOVERY.md`](BACKUP_AND_RECOVERY.md) · [`SECRET_MANAGEMENT.md`](SECRET_MANAGEMENT.md) · [`STAGING_ROLLBACK.md`](STAGING_ROLLBACK.md) · [`PRODUCTION_ROLLBACK.md`](PRODUCTION_ROLLBACK.md)

## 1. 分级

| 级别 | 影响 | 响应时效 | 触发条件 |
| --- | --- | --- | --- |
| **P0** | 全站不可用 / 数据损坏 / 资金风险 | 5 min | 关键接口 5xx > 50%，或数据丢失 / 泄露 |
| **P1** | 核心功能不可用 | 15 min | 登录 / 支付 / 实名失败 |
| **P2** | 非核心功能异常 | 1 h | 单业务模块异常 / 性能下降 |
| **P3** | 体验问题 | 24 h | UI 异常 / 边缘场景 |

## 2. 值班与升级

| 时段 | 一线 | 二线 | 三线（升级） |
| --- | --- | --- | --- |
| 工作日 | devops on-call | backend lead | SRE + 总监 |
| 周末 / 节假日 | devops on-call ×2 | SRE on-call | 总监 + CEO |

> 值班表存放在部署平台的 Secret 中；每月底轮换。

## 3. 应急流程

### 3.1 发现（0–5 min）

```text
1. 监控 / 告警 / 用户反馈触发
2. on-call 5 min 内确认并分级
3. 在值班群发布"事故开头"模板：
   - 时间
   - 环境（staging / production）
   - 影响范围
   - 初步假设
   - 当前动作（缓解 / 回滚 / 等待排查）
```

### 3.2 缓解（5–30 min）

按故障类型选择：

| 类型 | 缓解动作 |
| --- | --- |
| 前端错误 | [`STAGING_ROLLBACK.md`](STAGING_ROLLBACK.md) §4.2.1 / [`PRODUCTION_ROLLBACK.md`](PRODUCTION_ROLLBACK.md) §3 |
| 后端错误 | 回滚镜像（保留 DB） |
| 数据库异常 | **停止写流量**（Nginx 503 / API 限流）；DBA 评估 PITR |
| 第三方平台（微信 / 百度 / 支付） | 切换 mock / 关闭依赖功能；通知用户 |
| 性能 / 容量 | 扩容 / 限流 / 降级 |

### 3.3 沟通

| 对象 | 时点 | 渠道 |
| --- | --- | --- |
| 业务方 | 触发 + 缓解完成 | 值班群 + 工单 |
| 用户 | P0 / P1 触发 + 恢复 | 维护页 / 公告 / 客服 |
| 监管 / 平台 | 数据泄露 / 资金风险 | 法务 + 合规对接 |

### 3.4 恢复

参见 [`BACKUP_AND_RECOVERY.md`](BACKUP_AND_RECOVERY.md)；恢复后逐项校验：

- [ ] 关键 API 2xx
- [ ] 数据库行数 / 索引 / 关键业务指标正常
- [ ] 监控无新增 ERROR
- [ ] 用户侧冒烟（登录 / 支付 / 实名）
- [ ] 值班 30 min 观察无回滚

## 4. 时间线（强制记录）

事故开始 5 min 内由 on-call 维护：

```
2026-06-29 11:00 +08:00  [DISCOVER]   监控告警：API 5xx > 50%
2026-06-29 11:03 +08:00  [CONFIRM]    on-call 确认 P0
2026-06-29 11:05 +08:00  [MITIGATE]   触发 rollback-staging.sh 20260629_103000
2026-06-29 11:08 +08:00  [VERIFY]     5xx < 0.5%
2026-06-29 11:10 +08:00  [RESOLVE]    恢复
2026-06-29 12:00 +08:00  [POSTMORTEM] 复盘会
```

模板见 §7。

## 5. 复盘（Postmortem）

### 5.1 时点

- P0 / P1：24 h 内出复盘。
- P2 / P3：一周内出复盘。

### 5.2 必含

- 时间线（按 §4）
- 根因（5 Whys）
- 影响面（用户数 / 资金 / 数据）
- 缓解 vs 根因 vs 检测 各自耗时
- action items（含 owner / deadline）
- 归档到 `docs/release/incidents/YYYYMMDD-<short>.md`

### 5.3 文化

- **blameless**：聚焦系统而非个人。
- 行动项必须有 owner + deadline；下季度复盘未关闭升级。

## 6. 常见事故 Runbook

### 6.1 MySQL 连接耗尽

```bash
docker exec ypat-mysql mysqladmin -uroot -p"$YPAT_LOCAL_MYSQL_ROOT_PASSWORD" processlist -v | head
# 找到长事务 / 锁等待 → kill
```

### 6.2 Redis OOM

```bash
docker exec ypat-redis redis-cli -a "$YPAT_LOCAL_REDIS_PASSWORD" INFO memory
docker exec ypat-redis redis-cli -a "$YPAT_LOCAL_REDIS_PASSWORD" --bigkeys
# 临时：CONFIG SET maxmemory-policy allkeys-lru
```

### 6.3 FastDFS 上传失败

```bash
docker logs ypat-fastdfs-storage --tail=200
docker exec ypat-fastdfs-storage /usr/bin/fdfs_monitor /etc/fdfs/client.conf
```

### 6.4 TLS 证书过期

参见 [`scripts/deploy/check-tls-expiry.sh`](../../scripts/deploy/check-tls-expiry.sh)；续期脚本参考 Caddy / certbot 文档。

### 6.5 微信 / 百度 / 支付平台异常

- 立即在平台后台查看 API 状态。
- 切换 mock / 关闭功能（[`SECRET_MANAGEMENT.md`](SECRET_MANAGEMENT.md) §6）。
- 通过客服公告用户。

## 7. 事故时间线模板

复制以下内容到值班群 / 文档：

```text
== 事故时间线 ==
环境：staging | production
级别：P0 | P1 | P2 | P3
值班：<姓名>
开始：YYYY-MM-DD HH:MM +TZ
结束：YYYY-MM-DD HH:MM +TZ

HH:MM [DISCOVER]   <监控 / 告警 / 用户反馈>
HH:MM [CONFIRM]    <确认现象 + 分级>
HH:MM [MITIGATE]   <缓解动作>
HH:MM [ROOT_CAUSE] <根因>
HH:MM [VERIFY]     <验证>
HH:MM [RESOLVE]    <结束>
HH:MM [POSTMORTEM] <复盘链接>
```

## 8. 相关文档

- 备份恢复：[`BACKUP_AND_RECOVERY.md`](BACKUP_AND_RECOVERY.md)
- 预发回滚：[`STAGING_ROLLBACK.md`](STAGING_ROLLBACK.md)
- 生产回滚：[`PRODUCTION_ROLLBACK.md`](PRODUCTION_ROLLBACK.md)
- 密钥管理：[`SECRET_MANAGEMENT.md`](SECRET_MANAGEMENT.md)
- 数据库迁移：[`DATABASE_MIGRATION.md`](DATABASE_MIGRATION.md)
- 分支保护：[`GITHUB_BRANCH_PROTECTION.md`](GITHUB_BRANCH_PROTECTION.md)