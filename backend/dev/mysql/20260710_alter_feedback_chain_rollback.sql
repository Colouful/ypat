-- 手工回滚脚本：仅在确认本次上线需要回滚且已备份 t_feedback 数据后执行。
-- 校验：
--   SHOW CREATE TABLE `t_feedback`;
DROP INDEX `idx_feedback_type_credate` ON `t_feedback`;

ALTER TABLE `t_feedback`
  DROP COLUMN `handled_at`,
  DROP COLUMN `handled_by`,
  DROP COLUMN `handle_reason`,
  DROP COLUMN `pics`,
  DROP COLUMN `type`;
