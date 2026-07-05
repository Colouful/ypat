# t_work 覆盖索引 (PR-08)

**Status**: PR-08 implemented. SQL staged in `tools/ddl/` until
PR-07b flips Flyway on.

## Why

The work list is the single highest-traffic read path in the
app. V1.0 §2.3.2 and V1.1 §3.1 both flag the same problem:

```
WHERE city = ? AND status = ? AND deleted_flag = 0
ORDER BY publish_time DESC, id DESC
LIMIT 20
```

without a matching index MySQL falls back to:

1. Scan all rows for the city/status filter.
2. Sort the survivors by publish_time.
3. Take 20.

That's a `Using filesort` in `EXPLAIN` and a non-trivial CPU
spike every time the city/status cohort grows.

## The index

```sql
ALTER TABLE t_work
    ADD INDEX idx_city_status_pub (
        city,
        status,
        deleted_flag,
        publish_time DESC,
        id DESC
    ),
    ALGORITHM = INPLACE,
    LOCK = NONE;
```

- `(city, status, deleted_flag)` matches the WHERE clause
  exactly. MySQL stops reading as soon as it sees a row that
  doesn't match.
- `(publish_time DESC, id DESC)` matches the ORDER BY. MySQL 8
  supports descending index columns natively, so the optimizer
  walks the index backward — no filesort.
- `id DESC` is the tiebreaker that mirrors the existing cursor
  pagination pattern from V1.1 §3.1.

## Online DDL guarantees

`ALGORITHM = INPLACE, LOCK = NONE` is supported on MySQL 8.0+
for `ADD INDEX`. Readers and writers stay online for the entire
operation. The cost is CPU (for the sort) and temporary disk
(proportional to the index size).

For YPAT's current `t_work` size this is sub-second. If the
table grows past ~5M rows, swap to `gh-ost` for throttling —
documented in `tools/ddl/README.md`.

## Verification

Run `tools/ddl/verify-index.sh` after applying. Expected
output:

```
key: idx_city_status_pub
Extra: Backward index scan; no 'Using filesort'
rows: bounded
```

If you see `Using filesort` or `key=NULL`:

1. Run `ANALYZE TABLE t_work;` — stale cardinality stats can
   hide a viable index.
2. Re-check the WHERE clause matches the index column order
   (city first, then status, then deleted_flag, then
   publish_time, then id).
3. Roll back with `tools/ddl/rollback/V2__work_covering_index.sql`.

## Rollback

```bash
mysql -h "$YPAT_MYSQL_HOST" -u root -p ypat \
  < tools/ddl/rollback/V2__work_covering_index.sql
```

Also `ALGORITHM=INPLACE, LOCK=NONE` — DROP INDEX is online by
default on MySQL 8.

## Performance target

The V1.0 / V1.1 success criterion is **P99 < 200 ms** for the
work-list endpoint after this index is in place. Track via the
existing Grafana panel; alert if P99 > 250 ms for 15 min.

## Not in scope for PR-08

- Cursor pagination (replacing OFFSET). That belongs with the
  work-read migration (PR-11) where the actual query lives in
  the new code.
- A second index on `(userid, status, publish_time DESC)` for
  the "my works" view. Defer until we see that view's hot.
- Tag filtering. PR-11's work-list migration will move tag
  filtering to a proper JOIN, not `findAll()` + in-memory
  filter (V1.0 §2.3.2).
- `t_work_media` indexes. Different table, different access
  pattern. PR-10 may add some.