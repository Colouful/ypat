# tools/ddl/

Pre-Flyway DDL scripts. Lives here for now because backend-v2's
Flyway integration (PR-07a) is still `enabled: false` and the
real V1__baseline_schema.sql lands in PR-07b.

## Why this directory exists

Until Flyway is enabled, schema changes have nowhere official to
land. Two options:

1. Commit SQL into `backend-v2/db/migration/mysql/` directly.
   This breaks Flyway's checksum logic — the moment PR-07b
   flips `enabled: true`, every pre-baseline file becomes a
   "missing from baseline" error.
2. Park SQL in `tools/ddl/` until PR-07b lands. After PR-07b,
   each script is moved to `db/migration/mysql/V{n}__*.sql`
   and becomes part of the migration catalogue.

This directory follows option 2.

## Layout

```
tools/ddl/
├── README.md                                    this file
├── V2__work_covering_index.sql                  PR-08
├── rollback/V2__work_covering_index.sql         PR-08 rollback
└── verify-index.sh                              PR-08 EXPLAIN check
```

`V{n}__` mirrors the Flyway naming convention so a future
`mv` into the catalogue is mechanical.

## Execution model

These scripts are **not** run by Maven, CI, or any automated
process. They are operator-driven:

```bash
# 1. Confirm target DB is reachable.
mysqladmin ping -h "$YPAT_MYSQL_HOST" -u root -p

# 2. Pre-flight: check the index doesn't already exist.
mysql -h "$YPAT_MYSQL_HOST" -u root -p \
  -e "SHOW INDEX FROM t_work WHERE Key_name='idx_city_status_pub';"

# 3. Apply. ALGORITHM=INPLACE, LOCK=NONE lets MySQL 8 add
# the index without blocking readers. On a small (<1M rows)
# table this is sub-second; on a large table expect a few
# seconds of elevated CPU.
mysql -h "$YPAT_MYSQL_HOST" -u root -p ypat \
  < tools/ddl/V2__work_covering_index.sql

# 4. Verify with EXPLAIN.
bash tools/ddl/verify-index.sh
```

Every script must have a paired file under `rollback/` that
reverses the change. Rollback is run only when EXPLAIN shows the
index is hurting more than helping — usually never.

## After PR-07b

When the Flyway baseline lands, every `tools/ddl/V{n}__*.sql` is
moved to `backend-v2/db/migration/mysql/V{n}__*.sql` in a
follow-up PR. The new path ships Flyway-managed; this directory
goes read-only.

## What this directory must NOT contain

- DML (INSERT/UPDATE/DELETE). Migrations are structural.
- Data backfills. Those are app-level jobs, not SQL.
- Idempotent CREATE TABLE statements. MySQL DDL has its own
  online concerns; double-runs are operator error.