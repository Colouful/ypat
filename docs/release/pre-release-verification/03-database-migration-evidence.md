# 数据库 Migration 验证

更新时间：2026-06-28 15:10 +0800

## 迁移机制结论

当前项目未发现 Flyway(Flyway 数据库迁移工具)、Liquibase(Liquibase 数据库迁移工具) 或 Spring SQL 初始化自动执行链路。`backend/dev/mysql/20260628_create_feedback.sql` 和 `backend/dev/mysql/20260628_drop_feedback_rollback.sql` 是手工运维 SQL(sql 结构化查询语言) 脚本，不会随应用启动自动执行。

生产执行仍需 DBA 或运维在目标环境手工执行，并在执行前备份。不得把本地 Docker 验证视为生产 migration 已完成。

## 本地验证环境

| 项目 | 值 |
| --- | --- |
| 数据库 | Docker MySQL 8.0 |
| 测试库 | `ypat_migration_test` |
| 容器 | 临时容器，执行后已清理 |
| 数据 | 人工插入一条测试反馈记录，无真实用户数据 |

## 执行结果

| 步骤 | 结果 | 证据 |
| --- | --- | --- |
| 执行前库确认 | 通过 | `SELECT DATABASE()` 返回 `ypat_migration_test`。 |
| 建表脚本 | 通过 | 成功创建 `t_feedback`。 |
| 字段校验 | 通过 | 包含 `id`、`userid`、`content`、`contact`、`status`、`credate`、`upddate`。 |
| 索引校验 | 通过 | 包含 `PRIMARY`、`idx_feedback_userid`、`idx_feedback_status_credate`。 |
| 字符集 | 通过 | `utf8mb4`。 |
| 幂等执行 | 通过 | 重复执行建表脚本未失败。 |
| 测试数据插入 | 通过 | 插入 1 条测试记录。 |
| 回滚前备份 | 通过 | 创建 `t_feedback_backup_test`，记录数 1。 |
| 回滚脚本 | 通过 | `t_feedback` 被删除，备份表保留。 |

原始证据：`docs/release/pre-release-verification/artifacts/migration-docker.log`。

## 生产执行要求

- 执行人：生产 DBA 或具备生产数据库变更权限的后端负责人。
- 执行环境：目标生产数据库主库。
- 执行时间：正式发布维护窗口内，应用发布前完成。
- 备份要求：执行前完成库级备份，或至少备份即将新增/回滚的 `t_feedback` 数据。
- 回滚条件：仅在确认本次上线回滚且备份完成后执行回滚脚本。

## 风险

- 生产不自动执行 migration；如果运维漏执行，意见反馈接口上线后会因缺表失败。
- 回滚脚本包含 `DROP TABLE`，禁止在生产或共享环境直接试跑。
