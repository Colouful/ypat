-- rollback/V3__favorite_like_unique_keys.sql
--
-- Reverse of V3__favorite_like_unique_keys.sql.
--
-- Run ONLY if the migration has made things worse (rare).
-- DROP INDEX is online on MySQL 8 by default (ALGORITHM=INPLACE,
-- LOCK=NONE). The order of DROP matters when keys share columns;
-- these two don't, so order is irrelevant.

ALTER TABLE t_work_like
    DROP INDEX uk_work_like_user_work,
    ALGORITHM = INPLACE,
    LOCK = NONE;

ALTER TABLE t_work_favorite
    DROP INDEX uk_work_favorite_user_work,
    ALGORITHM = INPLACE,
    LOCK = NONE;

-- ──────────────────────────────────────────────────────────────
-- Pre-migration dedup (run BEFORE applying V3, not after).
-- If two duplicate rows already exist for the same
-- (work_id, user_id) the ADD UNIQUE will fail with
-- "Duplicate entry". Delete the older duplicates manually:
--
--   DELETE l1 FROM t_work_like l1
--   INNER JOIN t_work_like l2
--     ON l1.work_id = l2.work_id
--    AND l1.user_id = l2.user_id
--    AND l1.id > l2.id;     -- keep the smallest id
--
--   DELETE f1 FROM t_work_favorite f1
--   INNER JOIN t_work_favorite f2
--     ON f1.work_id = f2.work_id
--    AND f1.user_id = f2.user_id
--    AND f1.id > f2.id;
--
-- Inspect first:
--
--   SELECT work_id, user_id, COUNT(*) cnt
--     FROM t_work_like
--    GROUP BY work_id, user_id
--   HAVING cnt > 1;
--
--   SELECT work_id, user_id, COUNT(*) cnt
--     FROM t_work_favorite
--    GROUP BY work_id, user_id
--   HAVING cnt > 1;
-- ──────────────────────────────────────────────────────────────