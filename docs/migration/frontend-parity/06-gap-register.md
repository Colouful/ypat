# 06 差异登记册 (Gap Register)

> 分级: P0 资金/越权/安全 | P1 核心流程中断 | P2 状态/边界/降级 | P3 文案/轻交互
> 状态: OPEN | IMPLEMENTING | FIXED | ACCEPTED | ACCEPTED_WITH_ADR | ACCEPTED_FOR_CURRENT_RELEASE | OPS_BLOCKED | BACKEND_BLOCKED | PRODUCT_DECISION | NOT_APPLICABLE | RETIRED

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
| GAP-API-02 | P2 | B | /user/findByCityAndProfess wap 不存在 | 前端无调用;本轮删除前端死 API 导出;后端内部 /service 接口仍供系统内调用 | 无运行入口 | NOT_APPLICABLE |
| GAP-API-03 | P1 | 平台 | 生产 HTTPS 强校验 vs http 生产地址 | env.ts 生产强制 HTTPS;.env.production 仍为 http://82.156.14.216:8088 | 本轮不改 .env.production;上线前按 docs/release/PRODUCTION_LAUNCH_TODO.md 配置正式 HTTPS 域名 | OPS_BLOCKED |
| GAP-AUTH-02 | P2 | A | 新旧 storage key 不一致,升级丢登录态 | UNI_LOCAL_token vs ypat_token | 旧 Token 来自旧认证链路,强制重新登录更安全 | ACCEPTED |
| GAP-STATE-01 | P3 | B | "同城"tab getLocation 拿到坐标后写死"全国"(无逆地理编码) | home/index.vue:344 | 降级: 旧版本无 GPS,此为新功能;当前优雅降级为全国。逆地理编码需地图SDK,留作增强 | ACCEPTED |
| GAP-B-01 | P2 | B | 搜索风格标签把风格当 city 传,永远无结果 | search.vue:64 + discover searchStyle/home quickItem 传 keyword | 命中 PHOTO_STYLE → patstyle,否则 city | **FIXED** |
| GAP-B-02 | P3 | B | 资料完善门禁旧版首页 onShow 也触发,新版仅登录 | 旧 home index.js:152 getNextUrl(res,'home') | 登录门禁覆盖主路径,发布前置兜底;记录接受 | ACCEPTED |
| GAP-FAV-01 | P3 | E | 旧版收藏只增无取消,新版若有"取消收藏"将无对应后端端点 | 后端仅 /my/ypat/sc/add(只增),无删除;新版没有取消收藏按钮 | 不提供假取消 | NOT_APPLICABLE |
| GAP-NAV-01 | P3 | B | discover 页不在原生 tabBar | KeepTabBar、首页和消息空态均可进入 /pages/discover/index | 自定义 TabBar 入口已确认 | FIXED |

| GAP-C-01 | P2 | C | 报名理由硬编码,丢失用户自填(旧 ≥6字) | YpatDetailView apply() | editable modal 收集理由 + ≥6字校验 | **FIXED** |
| GAP-C-02 | P3 | C | 详情 share() 仅 toast | 本轮新增页面级 onShareAppMessage;H5 降级复制真实链接 | 真实分享/复制,不再假成功 | FIXED |
| GAP-C-03 | P3 | C | 详情"私信"stub、"关注"无后端 | 本轮移除假关注;私信改为真实主页入口 | 无后端能力时不展示假操作 | FIXED |

| GAP-F-01 | **P0** | F | 联系方式解锁费用显示/预检 1 豆,后端实扣 3 | message-detail handleViewContact | VIEW_CONTACT_PPD=3,阈值+文案改 3 | **FIXED** |
| GAP-F-02 | **P1** | F | message-detail 死页,联系方式解锁不可达 | 全仓无跳转 | 消息中心 received→message-detail?id | **FIXED** |
| GAP-F-03 | P1 | F | rec/send 列表项类型 YpatInfo 实为 MessInfo;sent 误用消息 id 跳详情 | MypatInfoController 返回 MessInfoQo;MessInfoQo.id=消息ID,ypatid=约拍ID | 类型改为 MessInfo;sent 仅用 ypatid,缺失补查 /mess/get | FIXED |

| GAP-G-01 | P1 | G | 实名仅2张证件照(缺手持),旧需3张且有29元审核费 | 后端和管理后台兼容两照;已创建 ADR-REALNAME-PHOTO-AND-FEE-POLICY | 当前采用两照免费认证;恢复三照/收费需重新评审 | ACCEPTED_WITH_ADR |
| GAP-IMG-01 | **P1** | D/G/A | 图片 base64 缺 dataURL 前缀(publish/realname 裸 base64),后端按 `,` 切分取图(MockTest 用 `data:image;base64,` 前缀) | file-base64 去头 | 新增 filePathToDataUrl/ensureImageDataUrl 统一补前缀;publish/oauth/edit-info 全部带前缀;加单测 | **FIXED** |

| GAP-I-01 | P1 | I | 反馈 /feedback/add 后端不存在 | 本轮新增后端端点、数据库 migration、前端 typed client 和测试 | 反馈提交不再必然失败 | FIXED |
| GAP-I-02 | P3 | I | 不支持改手机号 | settings handlePhone | 与旧一致,记录 | ACCEPTED |

| GAP-J-01 | P1 | J | 保证金充值流程缺失,1011 仅 toast 无支付入口 | 本轮不恢复真实199元保证金支付;新增 FEATURE_FLAGS.deposit=false | 当前版本关闭保证金服务,1011 明确提示暂未开放 | ACCEPTED_FOR_CURRENT_RELEASE |
| GAP-J-02 | P3 | J | 邀请体系当前版本下线 | 已删除 MINE.INVITE 悬空路由常量;历史账单 RecordType.INVITE 仅用于展示旧记录 | 当前版本下线邀请体系 | RETIRED |

## 人工联调与增强清单
- 报名(orderShe)和联系方式(linkway)主流程已迁移;上线前仍需用真实账号联调实名、余额、保证金、约拍理由、重复报名等服务端门禁。
- 发布表单字段、日期、图片和拍拍豆预检已迁移;草稿恢复属于增强项。
- 实名结果状态 0/1/2/3 需在预发环境用后台审核联调确认。
- 充值必须继续以服务端支付结果确认为准;真实微信支付本轮未执行。
- credit/creditagreement 当前版本按 GAP-J-01 关闭;invitation 当前版本 RETIRED。

## 后端安全记录
- BE-SEC-01: `/user/token` 凭 mobile 即签发 JWT,无凭证校验。状态: FIXED。本轮改为有效当前 Token 续签,前端刷新不再传 mobile。
- BE-SEC-02: `GET /**` permitAll,数据 GET 端点鉴权靠各 handler。状态: FIXED。本轮改为明确公开 GET 白名单,其余默认认证。
