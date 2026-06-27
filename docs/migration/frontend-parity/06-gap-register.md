# 06 差异登记册 (Gap Register)

> 分级: P0 资金/越权/安全 | P1 核心流程中断 | P2 状态/边界/降级 | P3 文案/轻交互
> 状态: OPEN 待处理 | FIXING 处理中 | FIXED 已修复并验证 | ACCEPTED 接受 | BACKEND 后端问题(不在本任务改动)

| ID | 级别 | 模块 | 标题 | 证据 | 处理方案 | 状态 |
|---|---|---|---|---|---|---|
| GAP-AUTH-01 | P0/P1 | A | 微信登录被硬编码测试账号绕过 | frontend/src/stores/user.ts:70-72 `login()` 恒调 `loginByPhoneInternal('18888888888','888888')` | 恢复真实微信 code 登录(#ifdef MP-WEIXIN: uni.login→/user/code→/user/login w/ encryptedData,iv);保留 H5 手机号登录;测试开关仅 dev | OPEN |
| GAP-API-01 | P1 | E | 我的列表用 POST,后端 GET-only → 405 | request POST vs MypatInfoController.java:48/90/97/112 `@GetMapping` | getMyPublishList/getMyFavoriteList/getMyReceivedList/getMySentList 改 GET+query;加回归测试 | **FIXED** |
| TEST-FIX-01 | P2 | A | mapBackendResponse 将 res:'' 强制转 null(基线测试 RED) | request.test.ts:26 | extractDataField 保留 falsy,顶层空响应仍 null | **FIXED** |
| TC-FIX-01 | P2 | B | type-check RED: GetSettingSuccess 类型名不存在 | home/index.vue:307/316 | 改 GetSettingSuccessResult | **FIXED** |
| GAP-API-02 | P2 | B | /user/findByCityAndProfess wap 不存在 | UserController 无该端点;/service 内部唯一调用点 YpatInfoController:175 已注释 | 确认前端调用点,移除或改用 /ypat 列表 | OPEN |
| GAP-API-03 | P1 | 平台 | 生产 HTTPS 强校验 vs http 生产地址 | env.ts:24-26 throw;.env.production http://82.156.14.216:8088 | 决策:生产应上 HTTPS;短期 IP 预发可放宽为 warn(待用户确认),不静默删校验 | OPEN |
| GAP-AUTH-02 | P2 | A | 新旧 storage key 不一致,升级丢登录态 | UNI_LOCAL_token vs ypat_token | 暂定强制重登(更安全);如需无感再做幂等迁移 | OPEN |
| GAP-STATE-01 | P2 | B/D | 无 uni.getLocation,经纬度/定位城市缺失 | 全局无 getLocation;YpatSubmitParams 有 lng/lat 类型但未填充 | 首页定位城市 + 发布定位补齐(带降级) | OPEN |
| GAP-FAV-01 | ? | E | 旧版收藏只增无取消,新版若有"取消收藏"将无对应后端端点 | 后端仅 /my/ypat/sc/add(只增),无删除 | 审计 my-favorite 页;若有取消需后端支持→暂禁用或确认 | OPEN |
| GAP-NAV-01 | ? | B | discover 页不在 tabBar,入口未知 | new pages.json tabBar 无 discover | 审计 home/discover 跳转入口 | OPEN |

## 待确认清单 (UNKNOWN, 需模块审计中关闭)
- 模块C: 报名(orderShe)/联系方式(linkway) 是否完整内联到 detail/message-detail,业务规则(实名/信用/余额/约拍理由≥6字)是否保留。
- 模块D: 发布表单字段(target/chargeway/patstyle/patdate不过期/省市必填/收费金额)是否完整;草稿恢复;图片 base64 ≤9。
- 模块E: infoaudit 审核态是否并入 my-publish 的状态 Tab。
- 模块G: 实名结果轮询(旧 success 页 oauth_get 轮询 status 0/1/2/3)是否在新版承载。
- 模块H: 充值仅服务端确认后到账;防重复支付;type 0/1/2 区分。
- 模块I/J: credit/creditagreement/invitation/helpcenter 去向结论。

## 后端记录(不在本任务改动,建议另行修复)
- BE-SEC-01: `/user/token` 凭 mobile 即签发 JWT,无凭证校验。
- BE-SEC-02: `GET /**` permitAll,数据 GET 端点鉴权靠各 handler。
