-- V4__work_media_storage_columns.sql
-- PR-10: extend t_work_media with provider-agnostic storage metadata.
--
-- Current schema (system-domain WorkMedia):
--   id, work_id, user_id, type, url, file_size, mime,
--   width, height, duration, sort_no, upload_status, created_at
--
-- url is currently the FULL URL (V1.0 §2.3.5 calls this out).
-- That ties the row to the FastDFS host at write time and makes
-- any later bucket / CDN switch painful.
--
-- The new columns:
--   storage_provider    COS / FASTDFS — which adapter produced it
--   object_key          opaque, no host/bucket prefix
--   checksum_sha256     optional integrity hash; 64 hex chars
--
-- We KEEP `url` as a soft-compat field for now. New code does
-- not write it; existing rows keep their value. The
-- StorageUrlResolver (PR-11) reads storage_provider + object_key
-- and assembles a fresh URL on demand. A later PR will deprecate
-- url and finally drop it.
--
-- Online DDL:
--   ALGORITHM=INSTANT for ADD COLUMN with a DEFAULT in MySQL 8.
--   INSTANT means no table rewrite, no row locks — readers and
--   writers keep going. Confirmed by the MySQL 8 docs and the
--   YPAT production scale.
--
-- Rollback:
--   DROP COLUMN is NOT INSTANT in MySQL 8 — it copies the table.
--   See rollback/V4__work_media_storage_columns.sql for the
--   off-peak-window warning.

ALTER TABLE t_work_media
    ADD COLUMN storage_provider VARCHAR(16) NOT NULL DEFAULT 'FASTDFS'
        COMMENT 'which adapter owns this row (COS / FASTDFS)',
    ADD COLUMN object_key VARCHAR(255) NULL
        COMMENT 'opaque storage key, no host prefix',
    ADD COLUMN checksum_sha256 CHAR(64) NULL
        COMMENT 'optional integrity hash, lowercase hex',
    ALGORITHM = INSTANT;

-- Backfill object_key for rows that have url but no object_key.
-- Existing rows are FastDFS, so the URL we store is the full
-- public URL with the FastDFS host. The key inside the URL is
-- the path suffix after the group, e.g.
--   https://files.ypat.example.com/group1/M00/AB/CD/foo.jpg
--                       ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^= object_key
-- This SELECT builds the candidate. Operators review the
-- distinct values before the UPDATE; some rows may have legacy
-- URLs that don't fit the pattern and need manual handling.

UPDATE t_work_media
   SET object_key = SUBSTRING_INDEX(url, '/', -2)
 WHERE object_key IS NULL
   AND url IS NOT NULL
   AND url LIKE '%/%/%';

-- The UPDATE above is a normal DML — NOT online. For a large
-- table run it in batches (PR-11 follow-up brings the chunked
-- backfill job). For YPAT's current t_work_media size (<1M rows
-- typically) one shot is fine, but operators must do it during
-- the off-peak window or use pt-online-schema-change.