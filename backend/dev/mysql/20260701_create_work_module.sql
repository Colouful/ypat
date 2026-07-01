-- ============================================================================
-- YPAT 作品模块迁移脚本
-- 日期: 2026-07-01
-- 说明: 新建作品主模块（7 张表 + 1 个字段扩展 + 5 个索引 + 28 个标签种子）
-- 兼容: MySQL 5.7+ / 8.0+, 字符集 utf8mb4
-- 回滚: 20260701_rollback_work_module.sql
-- ============================================================================

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------------------------------------------------------
-- 1. 扩展 t_ypat_info：新增 is_nationwide 字段（约拍"全国"选项）
-- ----------------------------------------------------------------------------
ALTER TABLE `t_ypat_info`
  ADD COLUMN `is_nationwide` TINYINT(1) NOT NULL DEFAULT 0
    COMMENT '是否全国（0否/1是）' AFTER `area`;

CREATE INDEX `idx_ypat_nationwide` ON `t_ypat_info` (`is_nationwide`);

-- ----------------------------------------------------------------------------
-- 2. 修复现有缺失索引（性能优化）
-- ----------------------------------------------------------------------------
CREATE INDEX `idx_ypat_userid` ON `t_ypat_info` (`userid`);
CREATE INDEX `idx_ypat_status_pubdate` ON `t_ypat_info` (`status`, `pubdate`);
CREATE INDEX `idx_ypat_city` ON `t_ypat_info` (`city`);
CREATE INDEX `idx_ypat_img_ypatid` ON `t_ypat_img` (`ypatid`);
CREATE INDEX `idx_user_mobile` ON `t_user` (`mobile`);

-- ----------------------------------------------------------------------------
-- 3. 作品主表 t_work
-- ----------------------------------------------------------------------------
DROP TABLE IF EXISTS `t_work`;
CREATE TABLE `t_work` (
  `id`                BIGINT(20)      NOT NULL AUTO_INCREMENT         COMMENT '主键',
  `userid`            BIGINT(20)      NOT NULL                        COMMENT '发布者 userid',
  `description`       VARCHAR(500)    NOT NULL                        COMMENT '作品描述（5-500 字符）',
  `device`            VARCHAR(100)    DEFAULT NULL                    COMMENT '使用设备',
  `shoot_location`    VARCHAR(100)    DEFAULT NULL                    COMMENT '拍摄地点',
  `return_photo_flag` TINYINT(1)      NOT NULL DEFAULT 0              COMMENT '约拍返片（0否/1是）',
  `media_type`        VARCHAR(8)      NOT NULL DEFAULT '1'            COMMENT '媒体类型：1图片/2视频',
  `is_nationwide`     TINYINT(1)      NOT NULL DEFAULT 0              COMMENT '是否全国',
  `status`            VARCHAR(8)      NOT NULL DEFAULT '1'            COMMENT '状态：0暂存/1待审核/2审核通过/3审核未通过/4已下架',
  `audit_reason`      VARCHAR(255)    DEFAULT NULL                    COMMENT '审核理由',
  `read_count`        INT(11)         NOT NULL DEFAULT 0              COMMENT '阅读量',
  `like_count`        INT(11)         NOT NULL DEFAULT 0              COMMENT '点赞数',
  `favorite_count`    INT(11)         NOT NULL DEFAULT 0              COMMENT '收藏数',
  `publish_time`      DATETIME        DEFAULT NULL                    COMMENT '发布时间',
  `created_at`        DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at`        DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted_flag`      TINYINT(1)      NOT NULL DEFAULT 0              COMMENT '软删除标记（0未删/1已删）',
  `city`              VARCHAR(64)     DEFAULT NULL                    COMMENT '发布者城市（冗余）',
  `area`              VARCHAR(64)     DEFAULT NULL                    COMMENT '发布者地区（冗余）',
  PRIMARY KEY (`id`),
  KEY `idx_work_userid` (`userid`),
  KEY `idx_work_status_pubtime` (`status`, `publish_time`),
  KEY `idx_work_city` (`city`),
  KEY `idx_work_deleted` (`deleted_flag`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='作品主表';

-- ----------------------------------------------------------------------------
-- 4. 作品媒体表 t_work_media
-- ----------------------------------------------------------------------------
DROP TABLE IF EXISTS `t_work_media`;
CREATE TABLE `t_work_media` (
  `id`            BIGINT(20)    NOT NULL AUTO_INCREMENT              COMMENT '主键',
  `work_id`       BIGINT(20)    DEFAULT NULL                         COMMENT '作品 ID（NULL=孤儿媒体，待清理）',
  `user_id`       BIGINT(20)    NOT NULL                             COMMENT '上传者 userid（归属校验）',
  `type`          VARCHAR(8)    NOT NULL DEFAULT '1'                 COMMENT '1图片/2视频',
  `url`           VARCHAR(500)  NOT NULL                             COMMENT '媒体 URL',
  `file_size`     BIGINT(20)    NOT NULL DEFAULT 0                   COMMENT '文件大小（字节）',
  `mime`          VARCHAR(64)   DEFAULT NULL                         COMMENT 'MIME 类型',
  `width`         INT(11)       DEFAULT NULL                         COMMENT '宽度（像素）',
  `height`        INT(11)       DEFAULT NULL                         COMMENT '高度（像素）',
  `duration`      INT(11)       DEFAULT NULL                         COMMENT '视频时长（秒）',
  `sort_no`       INT(11)       NOT NULL DEFAULT 0                   COMMENT '排序序号',
  `upload_status` VARCHAR(8)    NOT NULL DEFAULT '1'                 COMMENT '上传状态：0上传中/1成功/2失败',
  `created_at`    DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP   COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_work_media_workid` (`work_id`),
  KEY `idx_work_media_userid_created` (`user_id`, `created_at`),
  KEY `idx_work_media_orphan` (`work_id`, `created_at`) COMMENT '用于孤儿媒体清理'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='作品媒体';

-- ----------------------------------------------------------------------------
-- 5. 作品标签字典表 t_work_tag
-- ----------------------------------------------------------------------------
DROP TABLE IF EXISTS `t_work_tag`;
CREATE TABLE `t_work_tag` (
  `id`         BIGINT(20)     NOT NULL AUTO_INCREMENT                COMMENT '主键',
  `code`       VARCHAR(32)    NOT NULL                               COMMENT '标签代码（拼音）',
  `name`       VARCHAR(32)    NOT NULL                               COMMENT '标签名称（中文）',
  `sort_no`    INT(11)        NOT NULL DEFAULT 0                      COMMENT '排序',
  `status`     TINYINT(1)     NOT NULL DEFAULT 1                      COMMENT '0禁用/1启用',
  `created_at` DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP      COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_work_tag_code` (`code`),
  KEY `idx_work_tag_status_sort` (`status`, `sort_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='作品标签字典';

-- ----------------------------------------------------------------------------
-- 6. 作品-标签关联表 t_work_tag_rel
-- ----------------------------------------------------------------------------
DROP TABLE IF EXISTS `t_work_tag_rel`;
CREATE TABLE `t_work_tag_rel` (
  `id`         BIGINT(20)  NOT NULL AUTO_INCREMENT                COMMENT '主键',
  `work_id`    BIGINT(20)  NOT NULL                               COMMENT '作品 ID',
  `tag_id`     BIGINT(20)  NOT NULL                               COMMENT '标签 ID',
  `created_at` DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP     COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_work_tag` (`work_id`, `tag_id`),
  KEY `idx_tag_rel_tagid` (`tag_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='作品-标签关联';

-- ----------------------------------------------------------------------------
-- 7. 作品点赞表 t_work_like
-- ----------------------------------------------------------------------------
DROP TABLE IF EXISTS `t_work_like`;
CREATE TABLE `t_work_like` (
  `id`         BIGINT(20)  NOT NULL AUTO_INCREMENT                COMMENT '主键',
  `work_id`    BIGINT(20)  NOT NULL                               COMMENT '作品 ID',
  `user_id`    BIGINT(20)  NOT NULL                               COMMENT '点赞 userid',
  `created_at` DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP     COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_work_like` (`work_id`, `user_id`),
  KEY `idx_user_created` (`user_id`, `created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='作品点赞';

-- ----------------------------------------------------------------------------
-- 8. 作品收藏表 t_work_favorite
-- ----------------------------------------------------------------------------
DROP TABLE IF EXISTS `t_work_favorite`;
CREATE TABLE `t_work_favorite` (
  `id`         BIGINT(20)  NOT NULL AUTO_INCREMENT                COMMENT '主键',
  `work_id`    BIGINT(20)  NOT NULL                               COMMENT '作品 ID',
  `user_id`    BIGINT(20)  NOT NULL                               COMMENT '收藏 userid',
  `created_at` DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP     COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_work_favorite` (`work_id`, `user_id`),
  KEY `idx_user_created` (`user_id`, `created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='作品收藏';

-- ----------------------------------------------------------------------------
-- 9. 作品投诉表 t_work_complain
-- ----------------------------------------------------------------------------
DROP TABLE IF EXISTS `t_work_complain`;
CREATE TABLE `t_work_complain` (
  `id`         BIGINT(20)     NOT NULL AUTO_INCREMENT                  COMMENT '主键',
  `work_id`    BIGINT(20)     NOT NULL                                 COMMENT '被投诉作品 ID',
  `user_id`    BIGINT(20)     NOT NULL                                 COMMENT '投诉人 userid',
  `reason`     VARCHAR(500)   NOT NULL                                 COMMENT '投诉理由（10-500 字符）',
  `contact`    VARCHAR(100)   DEFAULT NULL                             COMMENT '联系方式（可选）',
  `status`     VARCHAR(8)     NOT NULL DEFAULT '0'                     COMMENT '0待处理/1已处理',
  `created_at` DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP       COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_complain_workid` (`work_id`),
  KEY `idx_complain_userid_created` (`user_id`, `created_at`),
  KEY `idx_complain_status_created` (`status`, `created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='作品投诉';

-- ----------------------------------------------------------------------------
-- 10. 初始化 29 个标签字典（28 个截图要求 + 1 个"复古"补充）
-- ----------------------------------------------------------------------------
INSERT IGNORE INTO `t_work_tag` (`code`, `name`, `sort_no`, `status`) VALUES
  ('qinglv',   '情侣',     1,  1),
  ('shangwu',  '商务',     2,  1),
  ('minguo',   '民国',     3,  1),
  ('hanfu',    '汉服',     4,  1),
  ('yunzhao',  '孕照',     5,  1),
  ('ertong',   '儿童摄影', 6,  1),
  ('anhei',    '暗黑',     7,  1),
  ('qingxu',   '情绪',     8,  1),
  ('yejing',   '夜景',     9,  1),
  ('xiaoyuan', '校园',     10, 1),
  ('zhuangrong','妆容',    11, 1),
  ('gufeng',   '古风',     12, 1),
  ('taobao',   '淘宝',     13, 1),
  ('shishang', '时尚',     14, 1),
  ('hefu',     '和服',     15, 1),
  ('qipao',    '旗袍',     16, 1),
  ('hanxi',    '韩系',     17, 1),
  ('oumei',    '欧美',     18, 1),
  ('senxi',    '森系',     19, 1),
  ('shaonv',   '少女',     20, 1),
  ('baolilai', '宝丽来',   21, 1),
  ('qingxin',  '清新',     22, 1),
  ('hunli',    '婚礼',     23, 1),
  ('cosplay',  'cosplay',  24, 1),
  ('jiaopian', '胶片',     25, 1),
  ('heibai',   '黑白',     26, 1),
  ('jishi',    '纪实',     27, 1),
  ('rixi',     '日系',     28, 1),
  ('fugu',     '复古',     29, 1);

SET FOREIGN_KEY_CHECKS = 1;

-- ============================================================================
-- 验证
-- ============================================================================
SELECT 't_work created' AS info, COUNT(*) AS cnt FROM t_work;
SELECT 't_work_tag seeded' AS info, COUNT(*) AS cnt FROM t_work_tag WHERE status = 1;
