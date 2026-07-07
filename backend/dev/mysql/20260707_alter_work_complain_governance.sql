ALTER TABLE `t_work_complain`
  ADD COLUMN `pics` VARCHAR(1000) DEFAULT NULL COMMENT '投诉证据 URL，逗号分隔' AFTER `contact`,
  ADD COLUMN `handle_reason` VARCHAR(500) DEFAULT NULL COMMENT '后台处理备注' AFTER `status`;
