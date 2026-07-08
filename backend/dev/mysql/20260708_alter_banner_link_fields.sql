ALTER TABLE `t_banner`
  ADD COLUMN `jumpflag` VARCHAR(1) NOT NULL DEFAULT '0' COMMENT '是否可跳转：0否，1是' AFTER `status`,
  ADD COLUMN `jumptype` VARCHAR(20) DEFAULT NULL COMMENT '跳转类型：miniapp小程序页面，web外部地址' AFTER `jumpflag`,
  ADD COLUMN `jumpurl` VARCHAR(500) DEFAULT NULL COMMENT '跳转目标：小程序页面路径或外部URL' AFTER `jumptype`;
