# 模块 J：旧版遗留能力去向 (Legacy Disposition)

> 规则: 无产品证据不得擅自标记 RETIRED;新版缺失者标 MISSING 并给建议。

| 旧版能力 | 旧路由 | 去向 | 状态 | 说明 |
|---|---|---|---|---|
| 完善信息(多步) | home/introduce, login/gender | complete-info | MERGED ✅ | Module A,登录后门禁引导 |
| 发布成功/审核结果 | home/success | publish 成功态 + realname 审核态 | MERGED ✅ | Module D/G |
| 约拍她(报名) | home/orderShe | detail apply(理由弹窗) | MERGED ✅ | Module C |
| 约拍请求(联系方式) | home/linkway | message-detail 解锁 | MERGED ✅ | Module F,费用 3 豆已修 |
| 邀请好友/说明 | mine/invitation(desc) | — | **MISSING** | 邀请返豆(INVITE_NEED_PPD)未迁移;constants/pages.ts INVITE 常量悬空但**无引用**(无运行风险)。建议: 产品确认是否保留邀请体系;保留则迁移,否则正式下线该常量 |
| 帮助中心 | mine/helpcenter | content/article + about + feedback | REPLACED(暂定) | 帮助内容可由文章系统承载;建议产品确认帮助内容入口 |
| 信用担保/保证金 | mine/credit, mine/creditagreement | — | **MISSING** | 保证金充值(order type=2,199元)未迁移;后端 1011 仅 toast"请先缴纳保证金"无支付入口;useAuth.requireCredit 定义未用。建议: 若存在要求保证金的约拍则必须补充值页,否则该类约拍报名会被卡住(P2) |

## 关键缺口(汇总 06)
- **GAP-J-01 (P2)**: 保证金充值流程缺失。影响: 报名 creditflag=1 的约拍时,后端返 1011,前端仅 toast,用户无法缴纳→流程中断。当前发布默认 creditflag='0'(不要求),故主路径不受阻;但任一要求保证金的存量约拍将无法报名。建议产品决策: 迁移 credit 充值页(order type=2)或确认下线保证金机制。
- **GAP-J-02 (P3)**: 邀请体系未迁移(含悬空 INVITE 常量,无引用)。产品决策。

## 修改
- 无代码改动(悬空常量无引用、无运行风险;补 credit/invite 完整流程需产品决策与新页面,超出"迁移现有逻辑"范围)
- 仅文档(去向结论)

## 验证
- type-check ✅ / test 50/50 ✅(沿用)
- 结论: 旧版每项能力均有明确去向;MERGED 项已在对应模块迁移;MISSING 项(邀请/保证金)为产品决策,其中保证金 GAP-J-01 列 P2 待决。
