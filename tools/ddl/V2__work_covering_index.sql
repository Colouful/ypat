-- V2__work_covering_index.sql
-- PR-08: covering index for the work-list hot path.
--
-- Query pattern (V1.0 §2.3.2, V1.1 §3.1):
--   SELECT ... FROM t_work
--   WHERE city = ? AND status = ? AND deleted_flag = 0
--   ORDER BY publish_time DESC, id DESC
--   LIMIT 20
--
-- Current state:
--   - t_work has the default PRIMARY KEY (id) and likely a
--     non-optimal secondary index for status / city / publish_time
--     individually, forcing filesort on the work list.
--   - EXPLAIN before this migration shows "Using filesort" and
--     "Using where" with no index coverage.
--
-- After this migration:
--   - The covering index matches WHERE clause + ORDER BY exactly,
--     so MySQL 8 can do a backward index scan, no filesort.
--   - The DDL uses ALGORITHM=INPLACE, LOCK=NONE — supported on
--     MySQL 8.0+ for index additions. Readers and writers stay
--     online for the entire ALTER.
--
-- Risk:
--   - On a large t_work table, INPLACE ALTER costs CPU and
--     temporary disk for sort. Schedule in the off-peak window
--     even though LOCK=NONE means it does not block writers.
--   - If the table has more than ~5M rows, prefer gh-ost to
--     throttle. For YPAT's current scale MySQL native is fine.
--
-- Rollback: see rollback/V2__work_covering_index.sql

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