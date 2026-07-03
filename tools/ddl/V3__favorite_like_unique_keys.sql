-- V3__favorite_like_unique_keys.sql
-- PR-09: idempotency guarantees for like / favorite.
--
-- YPAT keeps like and favorite as two separate tables
-- (t_work_like and t_work_favorite), each shaped
-- (id, work_id, user_id, created_at). V1.1 §3.2 imagined a
-- single t_favorite with a biz_type column; that hypothesis
-- turned out not to match the schema. This file adds the
-- unique keys that match the actual two-table layout.
--
-- What the keys do:
--   The current WorkService code does
--     if (workLikeRepository.existsByWorkIdAndUserId(...)) {
--       throw new SysException("already liked");
--     }
--     workLikeRepository.save(...);
--     workRepository.incrLikeCount(...);
--   Under concurrent requests the SELECT and the INSERT are
--   not atomic: two threads can both see "not yet liked" and
--   both INSERT. One wins; the loser hits DataIntegrityViolation.
--   The UNIQUE KEY (work_id, user_id) makes the database
--   enforce the invariant. The Service layer catches the
--   exception and treats it as success (idempotent).
--
-- Online DDL:
--   ALGORITHM=INPLACE, LOCK=NONE on MySQL 8.0+ for ADD UNIQUE
--   INDEX. Readers and writers stay online.
--
-- Pre-flight:
--   Deduplicate existing rows FIRST. If two rows already exist
--   for the same (work_id, user_id) the ALTER will fail with
--   "Duplicate entry". The dedup script lives at
--   tools/ddl/rollback/V3__favorite_like_unique_keys.sql as a
--   one-way operation; read it before running this migration.
--
-- Rollback:
--   See rollback/V3__favorite_like_unique_keys.sql

ALTER TABLE t_work_like
    ADD UNIQUE INDEX uk_work_like_user_work (work_id, user_id),
    ALGORITHM = INPLACE,
    LOCK = NONE;

ALTER TABLE t_work_favorite
    ADD UNIQUE INDEX uk_work_favorite_user_work (work_id, user_id),
    ALGORITHM = INPLACE,
    LOCK = NONE;