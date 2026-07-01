-- ============================================================================
-- YPAT 作品模块回滚脚本
-- 日期: 2026-07-01
-- 说明: 回滚 20260701_create_work_module.sql 的所有变更
-- 警告: 将删除 t_work_* 全部 7 张表（包含已上传数据），执行前请备份
-- ============================================================================

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------------------------------------------------------
-- 1. 删除 7 张作品相关表
-- ----------------------------------------------------------------------------
DROP TABLE IF EXISTS `t_work_complain`;
DROP TABLE IF EXISTS `t_work_favorite`;
DROP TABLE IF EXISTS `t_work_like`;
DROP TABLE IF EXISTS `t_work_tag_rel`;
DROP TABLE IF EXISTS `t_work_tag`;
DROP TABLE IF EXISTS `t_work_media`;
DROP TABLE IF EXISTS `t_work`;

-- ----------------------------------------------------------------------------
-- 2. 移除新增字段
-- ----------------------------------------------------------------------------
ALTER TABLE `t_ypat_info` DROP COLUMN `is_nationwide`;

-- ----------------------------------------------------------------------------
-- 3. 移除新增索引
-- ----------------------------------------------------------------------------
DROP INDEX `idx_ypat_nationwide` ON `t_ypat_info`;
DROP INDEX `idx_ypat_userid` ON `t_ypat_info`;
DROP INDEX `idx_ypat_status_pubdate` ON `t_ypat_info`;
DROP INDEX `idx_ypat_city` ON `t_ypat_info`;
DROP INDEX `idx_ypat_img_ypatid` ON `t_ypat_img`;
DROP INDEX `idx_user_mobile` ON `t_user`;

SET FOREIGN_KEY_CHECKS = 1;

-- ============================================================================
-- 验证
-- ============================================================================
SELECT 't_work tables dropped' AS info;
