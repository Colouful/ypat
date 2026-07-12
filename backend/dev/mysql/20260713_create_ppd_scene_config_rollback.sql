-- 手工回滚脚本：执行前必须备份并核对 t_ppd_scene_config、t_member_benefit_rule、t_record 数据。
-- 仅删除本迁移按固定“默认减免”说明新增且仍为默认值的规则，已被运营修改的数据需人工确认。
DELETE FROM `t_member_benefit_rule`
WHERE `benefit_type` = 'PPD_DISCOUNT'
  AND `discount_ppd` = 0
  AND `min_actual_ppd` = 0
  AND `effective` = '1'
  AND `status` = '1'
  AND `description` IN (
    '发布约拍高级会员默认减免', '发布约拍专业会员默认减免',
    '发起约拍申请基础会员默认减免', '发起约拍申请高级会员默认减免', '发起约拍申请专业会员默认减免',
    '查看联系方式基础会员默认减免', '查看联系方式高级会员默认减免', '查看联系方式专业会员默认减免'
  );

ALTER TABLE `t_record`
  DROP COLUMN `description`,
  DROP COLUMN `scene`;

DROP TABLE IF EXISTS `t_ppd_scene_config`;
