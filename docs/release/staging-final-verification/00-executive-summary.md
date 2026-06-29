# YPAT Staging 最终验证 — 执行摘要

## 时间

- 触发: 2026-06-29
- CI 验证 commit: `f7df2e2`
- 服务器: 82.156.14.216

## 状态总览

| 阶段 | 状态 |
| --- | --- |
| PR #7 合并 | ✅ MERGED → `f7df2e2` |
| CI Run 28362618060 | ✅ 10/10 success |
| 旧 `/opt/ypat` 取证备份 | ✅ 完成,12 MB,校验通过 |
| 旧修改分类 | ✅ A=17 / B=5 / C=0 / D=12 |
| 干净 main clone | ✅ `/opt/ypat-src/f7df2e2…` |
| Compose 兼容性核对 | ❌ STATEFUL_MIGRATION_REQUIRED |
| DB 账号兼容性 | ❌ DB_ACCOUNT_MIGRATION_REQUIRED |
| Branch Protection | ⏸ MANUAL_REQUIRED (solo-dev lockout 风险) |
| Staging 部署 | ⛔ 被门禁阻断 |
| Production 部署 | NOT_DEPLOYED |

## 最终状态

```
CI       = CI_VERIFIED
Staging  = NOT_READY
Prod     = NOT_DEPLOYED
```

## 阻断原因

新 `docker-compose.staging.yml` 引入了完全隔离的 staging 栈 (project=`ypat-staging`,
卷=`ypat-staging-mysql-data`/`ypat-staging-redis-data`,网络=`ypat-staging-net`)。
直接 `up -d` 会创建空 MySQL / 空 Redis,并与现有 FastDFS 网络隔离。

具体触发的硬停止条件:

1. MySQL 使用新空 volume
2. Redis 使用新空 volume
3. Compose project name 改变 (`ypat` → `ypat-staging`)
4. network 改变导致服务无法通信 (apps 在 `ypat-staging-net`,FastDFS 在 `ypat_ypat-net`)
5. 数据库业务账号未在任何 MySQL 实例中存在 (仅 root@%,root@localhost)
6. `/opt/ypat/.env` 缺失 `YPAT_DB_USERNAME` / `YPAT_DB_PASSWORD` / `YPAT_DB_NAME`

## 已完成的保全动作

- `/opt/ypat-data/backups/repository-state/20260629_180034/` — 完整取证 (patch / bundle / 快照 / SHA256SUMS)
- 旧 `/opt/ypat` 工作区**未触碰** (无 reset / stash / clean / pull)
- `/opt/ypat-src/<verified-main-sha>/` — 全新干净 main clone

## 下一步

见 [08-open-items.md](./08-open-items.md) 的迁移计划。**禁止在迁移计划经人工审批前部署。**
