-- 手工回滚脚本：仅在确认本次上线需要回滚且已备份 t_feedback 数据后执行。
-- 执行前备份：
--   CREATE TABLE `t_feedback_backup_yyyymmddhhmmss` AS SELECT * FROM `t_feedback`;
-- 回滚：
DROP TABLE IF EXISTS `t_feedback`;
