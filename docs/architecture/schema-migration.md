# Schema Migration Strategy

**Status**: PR-01a implemented. PR-01b next. Long-term: Flyway.

## Why this document exists

Until PR-01a the production `system-restapi` ran with
`spring.jpa.hibernate.ddl-auto: update`. That means Hibernate was
free to issue `ALTER TABLE` against the production MySQL on every
boot, based on whatever `@Entity` looked like in code at that moment.

That is unsafe for a financially-tracked app (wallet / payment /
identity land in this codebase), and it conflicts with the V1.0
upgrade plan §2.3.6. PR-01a is the first step to fix it.

## What PR-01a changed

| File | Before | After |
|---|---|---|
| `backend/system-restapi/src/main/resources/dev/application.yml` | `ddl-auto: update` | `ddl-auto: none` |
| `backend/system-restapi/src/main/resources/pre/application.yml` | `ddl-auto: update` | `ddl-auto: none` |
| `backend/system-restapi/src/main/resources/pro/application.yml` | `ddl-auto: update` | `ddl-auto: none` |
| `backend/system-wap/src/main/resources/dev/application.yml` | `ddl-auto: update` | `ddl-auto: none` (consistency; system-wap has no DataSource) |

system-sso already used `none` and was left alone.

## What this does NOT do yet

PR-01a stops the bleeding; it does not validate the schema. After
this PR lands there is a real risk: any new `@Entity` field will
silently fail at runtime with a Hibernate mapping exception
because the column no longer gets auto-created.

PR-01b is the second half of the fix. It will:

1. Run `tools/export-baseline-schema.sh` against the production DB
   to capture today's schema as `db/migration/mysql/V1__baseline_schema.sql`.
2. Run a Hibernate `SchemaValidator` snapshot to find every entity /
   DB drift we currently have (Hibernate did the diff silently all
   along; now we want it loud).
3. Generate `V2__*.sql` Flyway migrations that fix each drift one
   table at a time, with rollback SQL.
4. Flip `ddl-auto` from `none` to `validate` so any new drift fails
   the boot instead of silently corrupting data.
5. Enable Flyway (`enabled: true`, `baseline-on-migrate: true`,
   `baseline-version: 1`).

## Rollback

If PR-01a breaks a deploy, the immediate rollback is:

```bash
# On the affected profile's yml only:
sed -i 's/ddl-auto: none/ddl-auto: update/' \
  backend/system-restapi/src/main/resources/pro/application.yml
git revert <pr-01a-commit-sha>
```

This restores Hibernate's auto-`ALTER` behavior. It does NOT restore
any schema that PR-01a's `none` mode missed; whatever drift existed
between `@Entity` and the DB before PR-01a is still there.

## Long-term: Flyway

PR-07 will own the Flyway baseline. Every subsequent schema change
must:

1. Land as `db/migration/mysql/V{n}__description.sql`.
2. Include a paired `U{n}__description.sql` for undo (Flyway Teams)
   OR a documented manual rollback.
3. Be reviewed by both backend lead and DBA / SRE.
4. Pass `tools/check-ddl-auto.sh` in CI (the script enforces `none` /
   `validate` only).

Forbidden values in `application*.yml` (enforced by
`tools/check-ddl-auto.sh`):

- `ddl-auto: update`  ← auto-ALTER, banned
- `ddl-auto: create`  ← drops everything, banned
- `ddl-auto: create-drop`  ← drops everything on shutdown, banned

Allowed:

- `ddl-auto: none`     ← current state (PR-01a)
- `ddl-auto: validate` ← PR-01b target

## References

- V1.0 §2.3.6 — 数据库结构由 Hibernate 自动更新
- V1.1 §3.3 — Flyway 与在线 DDL
- V1.1 §1.2 第 5 项 — 强一致模块写主唯一性 (Flyway 与影子模式联动)
- PR plan: PR-01a (this PR), PR-01b (validate + Flyway), PR-07 (full baseline)