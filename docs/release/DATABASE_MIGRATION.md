# 数据库迁移（Database Migration）

> 文档日期：2026-06-29 · 基线 SHA：b4af32a · 负责人：DBA + backend lead
> 迁移脚本：[`scripts/deploy/db-migrate-staging.sh`](../../scripts/deploy/db-migrate-staging.sh)
> 预检：[`scripts/deploy/db-preflight.sh`](../../scripts/deploy/db-preflight.sh) · 校验：[`scripts/deploy/db-verify.sh`](../../scripts/deploy/db-verify.sh)
> 备份恢复：[`BACKUP_AND_RECOVERY.md`](BACKUP_AND_RECOVERY.md)
> 事故响应：[`INCIDENT_RESPONSE.md`](INCIDENT_RESPONSE.md)

## 1. 现状

- 当前 YPAT **未引入** Flyway / Liquibase / Spring SQL 初始化（详见 [`PRODUCTION_LAUNCH_TODO.md`](PRODUCTION_LAUNCH_TODO.md)）。
- 历史 SQL 以手工方式维护在 `backend/dev/mysql/`，每次执行需 DBA 介入。
- 本规范目标：**统一迁移格式**（手工 + 可机读校验），为后续引入 Flyway 做铺垫。

## 2. 迁移脚本目录结构

```
backend/dev/mysql/
├── README.md                  # 本规范的精简版 + 当前未执行清单
├── V<version>__<description>.sql     # 新格式：版本 + 描述
└── archive/                          # 已执行的迁移（保留 ≥ 1 年）
```

命名约束：

- 版本号：`V<YYYYMMDD>.<seq>`（如 `V20260629.1`）。
- 描述：英文 / 中文均可，**简短**（≤ 60 字符），不要含空格（下划线替代）。
- 文件使用 **UTF-8** + LF 换行。

## 3. SHA256 + schema_migration 表（手工版）

> 不引入 Flyway 的最小可行方案。

### 3.1 schema_migration 表

```sql
CREATE TABLE IF NOT EXISTS schema_migration (
  version       VARCHAR(32)  NOT NULL PRIMARY KEY,
  description   VARCHAR(255) NOT NULL,
  script_sha256 CHAR(64)     NOT NULL,
  applied_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  applied_by   VARCHAR(64)  NOT NULL,
  env          VARCHAR(16)  NOT NULL,
  duration_ms  INT          NULL,
  INDEX idx_schema_migration_applied_at (applied_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

### 3.2 应用流程

```bash
# 1) 计算脚本 SHA256
sha256sum backend/dev/mysql/V20260629.1__add_feedback.sql

# 2) dry-run
./scripts/deploy/db-migrate-staging.sh --dry-run

# 3) 执行（脚本会：
#    - 比对 schema_migration 中同 version 的 SHA256 是否一致
#    - 在事务中应用 SQL
#    - 写入 schema_migration 行（含 applied_by / env / duration_ms）)
./scripts/deploy/db-migrate-staging.sh --execute

# 4) 校验
./scripts/deploy/db-verify.sh
```

### 3.3 幂等性

- 每条迁移脚本必须可重复执行而不出错：`CREATE TABLE IF NOT EXISTS`、`ALTER TABLE ... ADD COLUMN IF NOT EXISTS`（MySQL 8.0+ 支持）。
- MySQL 5.7 及以前：`SHOW COLUMNS / SHOW INDEX` 预判后再 DDL。

### 3.4 环境隔离

| env | schema_migration.env 取值 | 写入规则 |
| --- | --- | --- |
| development | `dev` | 任意时刻 |
| staging | `staging` | 仅 staging DBA |
| production | `production` | **仅维护窗口内**，双人确认 |

## 4. Flyway（推荐引入路径）

未来建议迁移到 Flyway（见 [`ADR-JAVA-SPRING-UPGRADE.md`](../architecture/ADR-JAVA-SPRING-UPGRADE.md)）：

1. 在 `backend-base` 添加 `flyway-mysql` 依赖。
2. 启用 `spring.flyway.enabled=true`（仅 staging / production）。
3. 迁移目录：
   - `src/main/resources/db/migration/V<ver>__<desc>.sql`
   - 启动时 Flyway 自动 baseline + apply。
4. 与现有 `schema_migration` 表并存期：Flyway 加 `baseline-on-migrate=true`，避免历史迁移被执行。

引入 Flyway 前需先完成 Spring Boot 升级（Spring Boot 1.5 → 3.x 与 Flyway 9+ 版本不兼容）。

## 5. 回滚

> 数据库回滚**永远独立**于代码回滚（见 [`STAGING_ROLLBACK.md`](STAGING_ROLLBACK.md) §5 / [`PRODUCTION_ROLLBACK.md`](PRODUCTION_ROLLBACK.md) §3.3）。

### 5.1 DDL 回滚

每个 `V*.sql` 必须配套 `U*.sql`（Undo）：

```
backend/dev/mysql/
├── V20260629.1__add_feedback.sql
└── U20260629.1__add_feedback.sql
```

Undo 脚本约束：

- 只能 `DROP` / 备份恢复，**不得**包含数据迁移。
- 执行前必须显式备份：

  ```sql
  CREATE TABLE t_feedback_backup_<ts> AS SELECT * FROM t_feedback;
  ```

### 5.2 DML 回滚

- DML（UPDATE / DELETE / INSERT）原则上**不可回滚**；必须依赖备份恢复。
- 大批量 DML 必须拆批（每批 ≤ 10k 行）并记录 checkpoint。

### 5.3 紧急回滚（PITR）

- staging：参考 [`BACKUP_AND_RECOVERY.md`](BACKUP_AND_RECOVERY.md) §"MySQL"。
- production：仅 DBA 在维护窗口内执行。

## 6. 危险操作清单

下列操作**禁止**在生产维护窗口外执行：

- `DROP TABLE` / `DROP DATABASE`
- `TRUNCATE TABLE`
- 大表 `ALTER`（如 `ALTER TABLE t_* ADD COLUMN` 在 > 1M 行的表上）
- 修改主键 / 外键
- 批量 `UPDATE` / `DELETE`（> 10k 行）

## 7. 迁移检查清单

- [ ] 已在本地 docker 销毁库验证（含字段、索引、约束）
- [ ] 已在 staging 验证（24h 灰度）
- [ ] 已配套 `U*.sql` 回滚脚本
- [ ] 已更新 `backend/dev/mysql/README.md`
- [ ] 已通过 [`db-verify.sh`](../../scripts/deploy/db-verify.sh) 行数 / 索引 / 外键校验
- [ ] 已通知业务方并完成业务冒烟

## 8. 相关文档

- 备份恢复：[`BACKUP_AND_RECOVERY.md`](BACKUP_AND_RECOVERY.md)
- 事故响应：[`INCIDENT_RESPONSE.md`](INCIDENT_RESPONSE.md)
- 预发部署：[`STAGING_DEPLOYMENT.md`](STAGING_DEPLOYMENT.md)
- 生产部署：[`PRODUCTION_DEPLOYMENT.md`](PRODUCTION_DEPLOYMENT.md)
- ADR：[`../architecture/ADR-JAVA-SPRING-UPGRADE.md`](../architecture/ADR-JAVA-SPRING-UPGRADE.md)