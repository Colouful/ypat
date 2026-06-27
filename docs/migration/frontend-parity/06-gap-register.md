# 06 差异登记册 (Gap Register)

> 分级: P0 资金/越权/安全 | P1 核心流程中断 | P2 状态/边界/降级 | P3 文案/轻交互
> 状态: OPEN 待处理 | FIXING 处理中 | FIXED 已修复并验证 | ACCEPTED 接受 | BACKEND 后端问题(不在本任务改动)

| ID | 级别 | 模块 | 标题 | 证据 | 处理方案 | 状态 |
|---|---|---|---|---|---|---|
| GAP-AUTH-01 | P0/P1 | A | 微信登录被硬编码测试账号绕过 | frontend/src/stores/user.ts:70-72 `login()` 恒调 `loginByPhoneInternal('18888888888','888888')` | login() 生产透传真实 code/encryptedData/iv 给 /user/login;测试账号仅 development;加 store 回归测试 | **FIXED** |
| GAP-AUTH-03 | P1 | A | 首登资料完善门禁缺失,complete-info 页无人跳转(死页) | 全仓 grep 仅 pages.json 出现 `complete-info`;旧版 getNextUrl 强制不完整资料→introduce | 新增 utils/profile.isProfileComplete(对齐 getNextUrl: profess/gender(1,2)/birthday/province/city);登录成功后不完整→redirectTo complete-info;加单测 | **FIXED** |
| GAP-A-EDIT-01 | P2 | A/D | edit-info 缺少微信号(wx)字段,但发布/报名前置(criterion#2)需要 wx | edit-info.vue 表单仅 nickname/gender/profess/birthday/region/avatar | edit-info 新增微信号输入(表单/init/save/store) | **FIXED** |
| GAP-D-01 | P2 | D | 描述 ≥6 字未校验 | YpatPublishForm canSubmit | submit 增加 describ≥6 校验 | **FIXED** |
| GAP-D-02 | P2 | D | 拍摄日期写死 today,无选择UI、不校验过期 | YpatPublishForm model.patdate | 新增 date picker(:start=today)+不过期校验 | **FIXED** |
| GAP-D-03 | P2 | D | 发布前置(gender/wx/mobile/nickname/imgpath)未校验 | YpatPublishForm submit | isPublishProfileReady + modal 引导 edit-info | **FIXED** |
| GAP-D-04 | P3 | D | 无 requestSubscribeMessage(审核通知) | — | mp-weixin 通知,记录留待补 | ACCEPTED |
| GAP-D-05 | P3 | D | 无草稿保存/恢复(旧 publishData) | ypat store draftForm 未接入表单 | 记录,留待增强 | ACCEPTED |
| GAP-API-01 | P1 | E | 我的列表用 POST,后端 GET-only → 405 | request POST vs MypatInfoController.java:48/90/97/112 `@GetMapping` | getMyPublishList/getMyFavoriteList/getMyReceivedList/getMySentList 改 GET+query;加回归测试 | **FIXED** |
| TEST-FIX-01 | P2 | A | mapBackendResponse 将 res:'' 强制转 null(基线测试 RED) | request.test.ts:26 | extractDataField 保留 falsy,顶层空响应仍 null | **FIXED** |
| TC-FIX-01 | P2 | B | type-check RED: GetSettingSuccess 类型名不存在 | home/index.vue:307/316 | 改 GetSettingSuccessResult | **FIXED** |
| GAP-API-02 | P2 | B | /user/findByCityAndProfess wap 不存在 | UserController 无该端点;/service 内部唯一调用点 YpatInfoController:175 已注释 | 确认前端调用点,移除或改用 /ypat 列表 | OPEN |
| GAP-API-03 | P1 | 平台 | 生产 HTTPS 强校验 vs http 生产地址 | env.ts:24-26 throw;.env.production http://82.156.14.216:8088 | 决策:生产应上 HTTPS;短期 IP 预发可放宽为 warn(待用户确认),不静默删校验 | OPEN |
| GAP-AUTH-02 | P2 | A | 新旧 storage key 不一致,升级丢登录态 | UNI_LOCAL_token vs ypat_token | 暂定强制重登(更安全);如需无感再做幂等迁移 | OPEN |
| GAP-STATE-01 | P3 | B | "同城"tab getLocation 拿到坐标后写死"全国"(无逆地理编码) | home/index.vue:344 | 降级: 旧版本无 GPS,此为新功能;当前优雅降级为全国。逆地理编码需地图SDK,留作增强 | ACCEPTED |
| GAP-B-01 | P2 | B | 搜索风格标签把风格当 city 传,永远无结果 | search.vue:64 + discover searchStyle/home quickItem 传 keyword | 命中 PHOTO_STYLE → patstyle,否则 city | **FIXED** |
| GAP-B-02 | P3 | B | 资料完善门禁旧版首页 onShow 也触发,新版仅登录 | 旧 home index.js:152 getNextUrl(res,'home') | 登录门禁覆盖主路径,发布前置兜底;记录接受 | ACCEPTED |
| GAP-FAV-01 | ? | E | 旧版收藏只增无取消,新版若有"取消收藏"将无对应后端端点 | 后端仅 /my/ypat/sc/add(只增),无删除 | 审计 my-favorite 页;若有取消需后端支持→暂禁用或确认 | OPEN |
| GAP-NAV-01 | ? | B | discover 页不在 tabBar,入口未知 | new pages.json tabBar 无 discover | 审计 home/discover 跳转入口 | OPEN |

| GAP-C-01 | P2 | C | 报名理由硬编码,丢失用户自填(旧 ≥6字) | YpatDetailView apply() | editable modal 收集理由 + ≥6字校验 | **FIXED** |
| GAP-C-02 | P3 | C | 详情 share() 仅 toast | YpatDetailView:131 | 真实分享需页面级 onShareAppMessage,记录 | ACCEPTED |
| GAP-C-03 | P3 | C | 详情"私信"stub、"关注"无后端 | YpatDetailView 模板 | 旧版无 viewer 联系入口;关注为新增无后端 | ACCEPTED |

| GAP-F-01 | **P0** | F | 联系方式解锁费用显示/预检 1 豆,后端实扣 3 | message-detail handleViewContact | VIEW_CONTACT_PPD=3,阈值+文案改 3 | **FIXED** |
| GAP-F-02 | **P1** | F | message-detail 死页,联系方式解锁不可达 | 全仓无跳转 | 消息中心 received→message-detail?id | **FIXED** |
| GAP-F-03 | P2 | F | rec/send 列表项类型 YpatInfo 实为 MessInfo;sent 跳详情 id 待确认 | message/index items 类型 | 需后端响应实体确认;received 不受影响 | OPEN |

| GAP-G-01 | P2 | G | 实名仅2张证件照(缺手持),旧需3张 | realname.vue front/back | 后端 pics 为 List;产品决策,记录 | OPEN |
| GAP-IMG-01 | P1(待验证) | D/G/A | base64 格式不一致(publish/realname 裸,edit-info 带前缀;旧带前缀) | file-base64 去头 vs edit-info 加前缀 | 需后端确认接受格式后统一,不盲改 | OPEN |

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
