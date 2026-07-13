-- rollback/V2__work_covering_index.sql
-- Reverse of V2__work_covering_index.sql.
--
-- Run ONLY if the migration has made things worse (rare).
-- Typical symptom: the work-list endpoint's P99 latency goes
-- up after the ALTER. Confirm via tools/ddl/verify-index.sh
-- and the work-list Grafana panel before rolling back.
--
-- Same MySQL 8 online DDL guarantees: ALGORITHM=INPLACE,
-- LOCK=NONE — DROP INDEX is online by default.

ALTER TABLE t_work
    DROP INDEX idx_city_status_pub,
    ALGORITHM = INPLACE,
    LOCK = NONE;