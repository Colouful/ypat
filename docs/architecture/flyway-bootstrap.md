# Flyway Bootstrap

**Status**: PR-07a landed. Flyway dependencies and configuration
in place, but `enabled: false` until PR-07b produces the real
baseline.

## What PR-07a does

Adds Flyway 8.x (Spring Boot 2.7.18 BOM-managed) to backend-v2:

- `flyway-core`
- `flyway-mysql` (required by Flyway 8.2+ for MySQL targets)

Both in `compile` scope so the JAR ships in the boot image.
Configuration in `application.yml` keeps Flyway **disabled** for
now:

```yaml
spring:
  flyway:
    enabled: false
    # baseline-on-migrate: true   ← set in PR-07b
    # baseline-version: 1          ← set in PR-07b
    # locations: classpath:db/migration
    # sql-migration-suffixes: .sql
    # validate-on-migrate: true   ← set in PR-07b
```

`ddl-auto: none` stays set for both Hibernate and the legacy
modules (PR-01a), so even with Flyway enabled there's no
competing schema-mutation path.

## Why disabled until PR-07b

Flyway's `migrate` step requires at least one `V{n}__*.sql` in
the configured `locations`. Enabling with an empty catalogue
causes:

1. **Fresh DB**: Flyway sees zero migrations and refuses to
   create the `flyway_schema_history` table → boot fails.
2. **Non-empty DB** (with `baseline-on-migrate=true`): Flyway
   baselines at version 0 and creates `flyway_schema_history`
   with no recorded migrations. Confusing and very hard to
   unwind if a real baseline lands later.

So we keep the engine on the classpath but the switch off,
exactly so PR-07b can flip `enabled: true` the moment
`V1__baseline_schema.sql` is committed.

## What's in the migration directory

`backend-v2/db/migration/mysql/README.md` documents the
catalogue layout and naming conventions. The directory is empty
on purpose; only `README.md` is committed.

## Operator runbook (for PR-07b)

To produce the baseline (requires prod or staging MySQL
credentials):

```bash
export YPAT_MYSQL_HOST='...'
export YPAT_MYSQL_PORT=3306
export YPAT_MYSQL_DB=ypat
export YPAT_MYSQL_USERNAME='...'
export YPAT_MYSQL_PASSWORD='...'      # required, no default

cd /path/to/worktree
./tools/export-baseline-schema.sh
# → writes db/migration/mysql/V1__baseline_schema.sql

# Review the diff, then update backend-v2/src/main/resources/application.yml:
#   spring.flyway.enabled: true
#   spring.flyway.baseline-on-migrate: true
#   spring.flyway.baseline-version: 1

# PR-07b commit message:
#   refactor(schema): PR-07b Flyway baseline
```

The `export-baseline-schema.sh` script lives at the worktree
root and was added by PR-01a. PR-07b's only job is to run it,
review the output, and flip the switch.

## What PR-07a deliberately does NOT do

- Does NOT introduce a real baseline. The directory is empty
  by design.
- Does NOT enable Flyway. The first enable happens in PR-07b.
- Does NOT change Hibernate `ddl-auto`. That flip is PR-01b
  (validate + drift fixup), and it depends on PR-07b being
  merged first.
- Does NOT add Testcontainers-based migration tests. The first
  Flyway smoke test lands with PR-08 (work covering index).