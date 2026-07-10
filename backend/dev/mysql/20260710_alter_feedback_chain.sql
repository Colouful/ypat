-- 手工运维脚本：正式环境执行前必须完成备份，并确认 t_feedback 当前结构。
-- 校验：
--   SHOW CREATE TABLE `t_feedback`;
--   SHOW INDEX FROM `t_feedback`;
ALTER TABLE `t_feedback`
  ADD COLUMN `type` varchar(32) NOT NULL DEFAULT 'other' AFTER `userid`,
  ADD COLUMN `pics` varchar(1000) DEFAULT NULL AFTER `contact`,
  ADD COLUMN `handle_reason` varchar(500) DEFAULT NULL AFTER `status`,
  ADD COLUMN `handled_by` bigint(20) DEFAULT NULL AFTER `handle_reason`,
  ADD COLUMN `handled_at` datetime DEFAULT NULL AFTER `handled_by`;

CREATE INDEX `idx_feedback_type_credate` ON `t_feedback` (`type`, `credate`);
