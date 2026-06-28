# 代码修改记录

状态仅使用：`PLANNED`、`IMPLEMENTING`、`IMPLEMENTED`、`VERIFIED`、`BLOCKED`、`ROLLED_BACK`。

## CHANGE-001：修复消息列表实体和申请跳转语义

- 时间：2026-06-28
- 所属 Gap：GAP-F-03
- 严重度：P1
- 修改前行为：`/my/ypat/send/list` 返回消息实体时，前端把 `item.id` 当约拍 ID 跳详情，可能打开错误约拍或不存在详情。
- 修改后行为：received(收到) 用消息 ID 跳 `message-detail`；sent(申请) 只用 `ypatid` 跳约拍详情；缺失时补查 `/mess/get`，仍缺失则提示“关联约拍不存在或已下架”。
- 修改原因：后端 `MessInfoQo.id` 是消息记录 ID，`MessInfoQo.ypatid` 才是约拍 ID。
- 旧版证据：旧版消息和详情链路使用消息记录承载约拍申请，联系方式解锁以消息 ID 为入口。
- 新版证据：`frontend/src/pages/message/index.vue`、`frontend/src/pages-sub/ypat/my-apply.vue` 改为 `MessInfo[]`。
- 后端证据：`MypatInfoController.myRecList/mySendList` 使用 `MessInfoQo`；`MessInfoService.findById/findPage` 设置 `ypatid`。
- 修改文件：`frontend/src/api/modules/ypat.ts`、`frontend/src/pages/message/index.vue`、`frontend/src/pages-sub/ypat/my-apply.vue`、`frontend/src/utils/message-navigation.ts`、`frontend/src/utils/__tests__/message-navigation.test.ts`。
- 关键函数：`resolveMessageNavigation`、`openDetail`。
- 接口变化：前端类型契约从 `PageResult<YpatInfo>` 改为 `PageResult<MessInfo>`。
- 路由变化：sent tab 跳转参数从消息 ID 改为约拍 ID。
- 数据库变化：无。
- 兼容性影响：如果后端历史消息缺少 `ypatid`，不再错误跳转，会提示下架或不存在。
- 风险：旧消息缺少关联约拍时用户无法进入详情，但这是正确降级。
- 测试：前端 `message-navigation.test.ts` 5 条；`pnpm run test`。
- 测试结果：57 passed。
- Commit：cc58b5d。
- 状态：VERIFIED

## CHANGE-002：新增持久化意见反馈端点

- 时间：2026-06-28
- 所属 Gap：GAP-I-01
- 严重度：P1
- 修改前行为：前端调用 `POST /feedback/add`，后端无端点，上线后提交必失败。
- 修改后行为：`POST /feedback/add` 可提交反馈，userid 从 Token 获取，内容和联系方式校验后持久化到 `t_feedback`。
- 修改原因：不能保留一个必然失败的线上提交入口。
- 旧版证据：旧版无直接 parity(迁移对齐) 页面，该入口是新版功能。
- 新版证据：`frontend/src/pages-sub/user/feedback.vue` 改用 `feedbackApi.addFeedback`，包含提交中、防重复、未登录和错误处理。
- 后端证据：新增 WAP Controller、Feign client、REST API Controller、domain service、repository、entity、Qo。
- 修改文件：`backend/system-wap/src/main/java/com/ypat/controller/FeedbackController.java`、`backend/system-wap/src/main/java/com/ypat/service/FeedbackServiceClient.java`、`backend/system-restapi/src/main/java/com/ypat/controller/FeedbackController.java`、`backend/system-domain/src/main/java/com/ypat/entity/Feedback.java`、`backend/system-domain/src/main/java/com/ypat/repository/FeedbackRepository.java`、`backend/system-domain/src/main/java/com/ypat/service/FeedbackService.java`、`backend/system-object/src/main/java/com/ypat/FeedbackQo.java`、`backend/dev/mysql/20260628_create_feedback.sql`、`frontend/src/api/modules/feedback.ts`、`frontend/src/api/types/index.ts`、`frontend/src/pages-sub/user/feedback.vue`。
- 关键函数：`FeedbackController.add`、`FeedbackService.add`、`addFeedback`。
- 接口变化：新增 `POST /feedback/add`，`Content-Type: application/x-www-form-urlencoded`。
- 路由变化：无。
- 数据库变化：新增 `t_feedback` migration(数据库迁移脚本)。
- 兼容性影响：需要上线前执行数据库 migration。
- 风险：依赖 Redis 做简单限频；预发需确认 Redis 可用。
- 测试：`FeedbackControllerTest` 5 条；前端 API contract 测试。
- 测试结果：后端 14 passed，前端 57 passed。
- Commit：cc58b5d。
- 状态：VERIFIED

## CHANGE-003：通过统一功能开关关闭保证金服务

- 时间：2026-06-28
- 所属 Gap：GAP-J-01
- 严重度：P1
- 修改前行为：报名遇到后端 `1011` 时只有普通 Toast；发布表单存在历史草稿 `creditflag=1` 风险。
- 修改后行为：`FEATURE_FLAGS.deposit=false` 统一关闭保证金；发布提交强制 `creditflag=0`；后端 `1011` 展示明确不可报名说明。
- 修改原因：本轮不恢复真实 199 元保证金支付，不能展示虚假支付或错误绕过后端要求。
- 旧版证据：`91pai-master/pages/mine/credit/`、`creditagreement/` 存在保证金协议和充值链路。
- 新版证据：`frontend/src/config/features.ts`、`YpatPublishForm.vue`、`request.ts`。
- 后端证据：后端仍可能返回 `ResponseCode.FAIL_NOCRED(1011)`，前端不绕过。
- 修改文件：`frontend/src/config/features.ts`、`frontend/src/components/business/YpatPublishForm.vue`、`frontend/src/api/request.ts`。
- 关键函数：`submit`、`showBusinessGuide`。
- 接口变化：无。
- 路由变化：不新增保证金支付路由。
- 数据库变化：无。
- 兼容性影响：要求保证金的存量约拍当前版本无法报名，需运营只读检查。
- 风险：存量 `creditflag=1` 约拍会触发不可报名提示。
- 测试：`pnpm run check`。
- 测试结果：通过。
- Commit：cc58b5d。
- 状态：VERIFIED

## CHANGE-004：确认实名认证两照免费策略并修复图片提交兼容

- 时间：2026-06-28
- 所属 Gap：GAP-G-01
- 严重度：P1
- 修改前行为：新版提交两照且 dataURL 图片可能被后端旧逻辑跳过；三照/收费差异未有正式决策记录。
- 修改后行为：后端 `/oauth/add` 同时接受 dataURL 和裸 Base64；两照免费策略通过 ADR 固化。
- 修改原因：后端和管理后台兼容两照，减少手持身份证采集更符合隐私最小化。
- 旧版证据：`91pai-master/pages/mine/realname/` 三照；旧订单 `type=1` 属于审核费历史链路。
- 新版证据：`frontend/src/pages-sub/user/realname.vue` 两照免费提交。
- 后端证据：`OauthQo.pics` 列表；`UserService.oauth` 不强制三张；管理后台遍历 `user.pics`。
- 修改文件：`backend/system-wap/src/main/java/com/ypat/controller/OauthController.java`、`docs/decisions/ADR-REALNAME-PHOTO-AND-FEE-POLICY.md`。
- 关键函数：`OauthController.add`。
- 接口变化：`/oauth/add` 兼容 dataURL。
- 路由变化：无。
- 数据库变化：无。
- 兼容性影响：不恢复手持照和 29 元收费。
- 风险：如果未来后台改为强依赖第三张图片，必须同步恢复前端和后端校验。
- 测试：`mvn test`、`pnpm run check`。
- 测试结果：后端 14 passed，前端 check 通过。
- Commit：cc58b5d。
- 状态：VERIFIED

## CHANGE-005：后端 Token 刷新与 GET 鉴权安全加固

- 时间：2026-06-28
- 所属 Gap：BE-SEC-01、BE-SEC-02
- 严重度：P0
- 修改前行为：`/user/token` 可仅凭 `mobile` 签发 Token；`GET /**` 匿名放行。
- 修改后行为：Token 刷新必须有当前有效 Token；GET 仅白名单匿名，其余默认认证。
- 修改原因：关闭认证绕过和私有数据匿名读取风险。
- 旧版证据：不适用。
- 新版证据：`frontend/src/api/request.ts` 刷新不传 mobile。
- 后端证据：`LoginController.token` 使用 `UserUtil.getUserId()`；`WebSecurityConfig` 删除 `GET /**`。
- 修改文件：`backend/system-wap/src/main/java/com/ypat/controller/LoginController.java`、`backend/system-wap/src/main/java/com/ypat/service/UserService.java`、`backend/system-wap/src/main/java/com/ypat/config/WebSecurityConfig.java`、`backend/system-wap/src/test/java/com/ypat/service/UserServiceTokenRefreshTest.java`、`frontend/src/api/request.ts`。
- 关键函数：`token`、`getToken`、`configure(HttpSecurity)`、`refreshToken`。
- 接口变化：`GET /user/token` 不再接受裸 `mobile` 刷新。
- 路由变化：GET 匿名访问改为白名单。
- 数据库变化：无。
- 兼容性影响：旧 Token 或无 Token 刷新会失败并跳登录；这是安全预期。
- 风险：若存在未登记的公开 GET 页面，可能需要加入白名单。
- 测试：`UserServiceTokenRefreshTest` 3 条；`mvn test`。
- 测试结果：14 passed。
- Commit：cc58b5d。
- 状态：VERIFIED

## CHANGE-006：清理详情页虚假关注、私信、分享交互

- 时间：2026-06-28
- 所属 Gap：虚假交互、GAP-C-02、GAP-C-03
- 严重度：P1
- 修改前行为：关注只改本地变量，私信只提示“打开私信”，分享只提示“已分享”。
- 修改后行为：无后端关注则不展示假关注；私信改为真实主页入口；小程序分享使用 `onShareAppMessage`，H5/通用端复制真实详情路径。
- 修改原因：禁止让用户误以为未落库的操作真实成功。
- 旧版证据：旧版详情页有真实小程序分享能力。
- 新版证据：`YpatDetailView.vue` 和 `pages-sub/ypat/detail.vue`。
- 后端证据：未发现关注或即时聊天后端能力。
- 修改文件：`frontend/src/components/business/YpatDetailView.vue`、`frontend/src/components/business/KeepIcon.vue`、`frontend/src/pages-sub/ypat/detail.vue`。
- 关键函数：`copyShareLink`、`goProfile`、`onShareAppMessage`。
- 接口变化：无。
- 路由变化：私信按钮改为主页导航。
- 数据库变化：无。
- 兼容性影响：用户不再看到不可用的关注/私信成功反馈。
- 风险：H5 分享能力降级为复制路径。
- 测试：`pnpm run check`。
- 测试结果：通过。
- Commit：cc58b5d。
- 状态：VERIFIED

## CHANGE-007：清理其余 Gap 状态和邀请悬空路由

- 时间：2026-06-28
- 所属 Gap：GAP-API-02、GAP-API-03、GAP-AUTH-02、GAP-FAV-01、GAP-NAV-01、GAP-J-02
- 严重度：P2/P3
- 修改前行为：迁移证据中仍有 OPEN 状态；前端保留不存在邀请页常量。
- 修改后行为：死 API 前端引用删除；生产 HTTP IP 标为 OPS_BLOCKED；旧 Token 强制重登标为 ACCEPTED；收藏取消标为 NOT_APPLICABLE；发现页入口确认；邀请体系标为 RETIRED。
- 修改原因：不能让已处理或已决策问题继续 OPEN。
- 旧版证据：旧版邀请体系存在但新版无入口。
- 新版证据：`KeepTabBar`、首页、消息空态均可进入发现页；无取消收藏按钮。
- 后端证据：后端仅有收藏新增，无取消收藏端点。
- 修改文件：`frontend/src/api/modules/user.ts`、`frontend/src/constants/pages.ts`、`docs/migration/frontend-parity/*`、`docs/release/*`。
- 关键函数：无。
- 接口变化：删除前端死 API `findByCityAndProfess` 导出。
- 路由变化：删除 `MINE.INVITE` 悬空常量。
- 数据库变化：无。
- 兼容性影响：历史邀请奖励账单类型仍保留展示，不代表邀请体系在线。
- 风险：若产品恢复邀请体系，需要重新增加路由和后端能力。
- 测试：`pnpm run check`。
- 测试结果：通过。
- Commit：cc58b5d。
- 状态：VERIFIED

## 修改文件汇总

最终文件清单见 `artifacts/changed-files.txt`。本轮主要修改类型：

| 文件 | 修改类型 | 修改原因 | 对应 Gap | 风险 |
| -- | ---- | ---- | ------ | -- |
| 前端消息/我的申请 | 修改 | 消息 ID 与约拍 ID 分离 | GAP-F-03 | 旧消息无 ypatid 时无法跳详情 |
| 前端反馈 | 新增/修改 | 避免反馈入口必失败 | GAP-I-01 | 依赖后端 migration |
| 后端反馈 | 新增 | 持久化反馈与校验 | GAP-I-01 | 依赖 Redis 和数据库 |
| 前端保证金开关 | 新增/修改 | 当前版本关闭保证金 | GAP-J-01 | 存量保证金约拍不可报名 |
| 后端认证安全 | 修改/测试 | 关闭 Token 和 GET 匿名漏洞 | BE-SEC-01/02 | 公开 GET 需维护白名单 |
| 实名 ADR/后端图片 | 新增/修改 | 两照免费策略与 dataURL 兼容 | GAP-G-01 | 后台策略变更需重新评审 |
| 迁移和发布文档 | 修改/新增 | 同步证据和上线结论 | 全部 | 需要随 commit SHA 更新 |
