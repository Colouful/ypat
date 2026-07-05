# 会员系统上线检查清单

SQL 发布包：`docs/sql/release/2026-07-05-member-system-redesign/`

- [ ] 校验发布包 sha256 与 `MANIFEST.md` 一致
- [ ] 测试库执行 `001_V_pending_member.sql`
- [ ] 测试库执行 `002_V_member_system_redesign.sql`
- [ ] 生产库执行前完成数据库备份
- [ ] 生产库执行 `001_V_pending_member.sql`
- [ ] 生产库执行 `002_V_member_system_redesign.sql`
- [ ] 验证 `GET /member/plans` 未登录返回套餐列表
- [ ] 验证 `GET /member/status` 未登录返回 401
- [ ] 后台配置 BASIC 提交约拍优惠
- [ ] 后台上架至少一个会员套餐
- [ ] 小程序会员页显示套餐
- [ ] 提交约拍页显示原价、优惠、实扣
- [ ] 微信支付回调重复发送不会重复发豆
- [ ] 会员入口开关可关闭
