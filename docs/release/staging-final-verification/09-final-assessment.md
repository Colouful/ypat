# 最终评估

## 摘要

| 类别 | 结果 |
| --- | --- |
| CI | `CI_VERIFIED` |
| Staging | `NOT_READY` |
| Production | `NOT_DEPLOYED` |
| Branch Protection | `MANUAL_REQUIRED` |
| 旧工作区取证 | 完成,无丢失 |
| 干净 main clone | 完成,detached HEAD = f7df2e2 |

## Git & CI

| 项 | 值 |
| --- | --- |
| 最新 main | f7df2e2ba8477904fc045a4e07a625ece7e7ae38 |
| PR #7 | MERGED 2026-06-29T09:34:50Z |
| Merge commit | f7df2e2 |
| CI Run | 28362618060 |
| 成功 Job | 10 |
| 失败 Job | 0 |
| Branch Protection | 未启用 (MANUAL_REQUIRED) |

## 旧服务器工作区

| 项 | 值 |
| --- | --- |
| 旧 HEAD | 13fb747 |
| Tracked 修改 | 12 |
| Untracked 条目 | 12 (展开 21 个文件) |
| 备份目录 | `/opt/ypat-data/backups/repository-state/20260629_180034` |
| Patch 校验 | 反向应用失败 = 预期(diff 已应用) |
| Bundle 校验 | OK |
| TAR 校验 | OK |
| SHA256 | OK |
| 是否清理旧目录 | 否 |

## 状态数据兼容性

| 项目 | 旧配置 | 新配置 | 是否安全 |
| --- | --- | --- | --- |
| Compose project | `ypat` | `ypat-staging` | ❌ 不安全 — 创建并行 stack |
| MySQL volume | `ypat_mysql_data` (15 表) | `ypat-staging-mysql-data` (空) | ❌ 需 dump/restore |
| Redis volume | `ypat_redis_data` (有数据) | `ypat-staging-redis-data` (空) | ⚠ 缓存丢失可接受,但 session 受影响 |
| FastDFS mount | `/opt/ypat-data/fastdfs/{tracker,storage}` | 相同路径 | ✅ 安全 |
| Network | `ypat_ypat-net` | `ypat-staging-net` + FastDFS 仍在旧 net | ❌ Java FDFS client 无法跨网络解析 |
| 数据库账号 | 仅 root@%,root@localhost | 需要 `${YPAT_DB_USERNAME}` | ❌ 不存在 |
| `/opt/ypat/.env` 必填变量 | 14 项 SET,3 项 MISSING | 需补 YPAT_DB_NAME / USERNAME / PASSWORD | ❌ 缺 |

## 部署

| 项 | 值 |
| --- | --- |
| 部署源码目录 | `/opt/ypat-src/f7df2e2ba8477904fc045a4e07a625ece7e7ae38` (干净 detached HEAD,clean worktree) |
| 部署 SHA | **未部署** |
| 开始时间 | — |
| 结束时间 | — |
| 前端 release | — |
| 后端镜像 | — |
| 是否回滚 | — |

## 验证

| 项目 | 结果 |
| --- | --- |
| Docker entrypoint | 未验证(未部署) |
| Spring Profile | 未验证 |
| Redis | 旧栈 ✓ NOAUTH/PONG,WAP PASSWORD_SET |
| MySQL | 旧栈 ✓ 15 表;新栈未启动 |
| FastDFS | 旧栈 ✓ tracker/storage 运行 |
| 端口 | 未验证 |
| HTTPS | 未验证 |
| API | 未验证 |
| 业务冒烟 | 未验证 |

## 最终状态

```
CI:         CI_VERIFIED
Staging:    NOT_READY
Production: NOT_DEPLOYED
```

阻断原因: 7 项硬停止条件中 6 项触发(详见 08-open-items.md)。
迁移计划已起草,待业务/产品/运维三方人工审批后执行。

## 决策保留

- 不部署 staging,直到 STATEFUL_MIGRATION_REQUIRED 与 DB_ACCOUNT_MIGRATION_REQUIRED 全部解除。
- 不打 staging tag,直到部署完成且业务冒烟通过。
- 不创建 Branch Protection,直到 solo-dev 锁死方案确定(选项 A 立即可启用,选项 B 需先加 collaborator)。
- 旧 `/opt/ypat` 保留至少 1 周,以备回滚或追溯。
