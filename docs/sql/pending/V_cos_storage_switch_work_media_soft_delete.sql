ALTER TABLE t_work_media
    ADD COLUMN deleted_at DATETIME NULL COMMENT '软删除时间' AFTER created_at;

CREATE INDEX idx_t_work_media_work_deleted_sort
    ON t_work_media (work_id, deleted_at, sort_no);
