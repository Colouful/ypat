-- rollback/V4__work_media_storage_columns.sql
--
-- Reverse of V4__work_media_storage_columns.sql.
--
-- ⚠️  DROP COLUMN in MySQL 8 is NOT INSTANT — it copies the table.
-- Schedule during the off-peak window, or accept the IO spike.
--
-- Order matters: drop the backfilled object_key first so the
-- storage_provider column default isn't masking data.

ALTER TABLE t_work_media
    DROP COLUMN checksum_sha256;

ALTER TABLE t_work_media
    DROP COLUMN object_key;

ALTER TABLE t_work_media
    DROP COLUMN storage_provider;