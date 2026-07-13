# Flyway Migration Catalogue

This directory will hold Flyway SQL migrations for backend-v2 once
PR-07b produces the real `V1__baseline_schema.sql`.

## Layout (target after PR-07b)

```
db/migration/mysql/
├── V1__baseline_schema.sql          ← committed in PR-07b
├── V2__work_covering_index.sql      ← PR-08
├── V3__favorite_unique_key.sql      ← PR-09
├── V4__work_media_storage_columns.sql ← PR-10
└── ...
```

## Naming

| Prefix | Meaning | Example |
|---|---|---|
| `V{n}__` | Versioned, forward-only | `V2__work_covering_index.sql` |
| `U{n}__` | Undo (requires Flyway Teams) | `U2__work_covering_index.sql` |
| `R__` | Repeatable, re-applied on checksum change | `R__seed_data.sql` |

YPAT does not currently license Flyway Teams, so `U{n}__` is not
in scope. Rollback goes through `git revert` of the migration
commit + a new forward migration that fixes the data, per
`docs/architecture/schema-migration.md`.

## Status

PR-07a (this branch) only adds the placeholder. The first real
migration lands in PR-07b once the production schema is dumped
via `tools/export-baseline-schema.sh`.

To trigger PR-07b manually:

```bash
export YPAT_MYSQL_PASSWORD='...'   # prod or staging MySQL
./tools/export-baseline-schema.sh
# Review the diff of db/migration/mysql/V1__baseline_schema.sql
# Open a PR titled "refactor(schema): PR-07b Flyway baseline"
```

Until then, **this directory must remain empty.** A stray `.sql`
file committed before the baseline will fail Flyway's checksum
validation the moment PR-07b flips `enabled: true`.