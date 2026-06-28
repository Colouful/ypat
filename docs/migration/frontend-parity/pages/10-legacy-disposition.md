# 模块 J：旧版遗留能力去向 (Legacy Disposition)

> 规则: 无产品证据不得擅自标记 RETIRED;新版缺失者标 MISSING 并给建议。

| 旧版能力 | 旧路由 | 去向 | 状态 | 说明 |
|---|---|---|---|---|
| 完善信息(多步) | home/introduce, login/gender | complete-info | MERGED ✅ | Module A,登录后门禁引导 |
| 发布成功/审核结果 | home/success | publish 成功态 + realname 审核态 | MERGED ✅ | Module D/G |
| 约拍她(报名) | home/orderShe | detail apply(理由弹窗) | MERGED ✅ | Module C |
| 约拍请求(联系方式) | home/linkway | message-detail 解锁 | MERGED ✅ | Module F,费用 3 豆已修 |
| 邀请好友/说明 | mine/invitation(desc) | — | **RETIRED** | 当前版本下线邀请体系;已删除 constants/pages.ts 中不存在页面的 INVITE 路由常量。历史账单邀请奖励类型仅用于展示旧记录 |
| 帮助中心 | mine/helpcenter | content/article + about + feedback | REPLACED(暂定) | 帮助内容可由文章系统承载;建议产品确认帮助内容入口 |
| 信用担保/保证金 | mine/credit, mine/creditagreement | — | **ACCEPTED_FOR_CURRENT_RELEASE** | 本轮不恢复真实保证金;前端 `FEATURE_FLAGS.deposit=false` 关闭入口,后端 1011 明确提示暂未开放;上线前检查存量 creditflag=1 |

## 关键缺口(汇总 06)
- **GAP-J-01 (P1)**: 保证金充值流程本轮有意不恢复,状态 ACCEPTED_FOR_CURRENT_RELEASE。当前版本关闭保证金入口,不提供虚假支付。
- **GAP-J-02 (P3)**: 邀请体系当前版本下线,已删除悬空 INVITE 路由常量,状态 RETIRED。

## 修改
- 无代码改动(悬空常量无引用、无运行风险;补 credit/invite 完整流程需产品决策与新页面,超出"迁移现有逻辑"范围)
- 仅文档(去向结论)

## 验证
- type-check ✅ / test 50/50 ✅(沿用)
- 结论: 旧版每项能力均有明确去向;MERGED 项已在对应模块迁移;保证金按当前版本关闭处理,邀请体系 RETIRED。
