# API 与导航证据

## GAP-F-03

- 后端证据：`system-wap/MypatInfoController.myRecList/mySendList` 使用 `MessInfoQo`；`system-domain/MessInfoService.findById/findPage` 设置 `ypatid = messInfo.getYpatInfo().getId()`。
- 接口语义：`item.id` 是消息记录 ID，`item.ypatid` 是约拍 ID。
- 前端修复：`getMyReceivedList()` 和 `getMySentList()` 返回 `PageResult<MessInfo>`。
- received(收到) 导航：`/pages-sub/content/message-detail?id=<消息ID>`。
- sent(申请) 导航：`/pages-sub/ypat/detail?id=<约拍ID>`。
- 缺失 `ypatid`：补查 `/mess/get?id=<消息ID>`；仍缺失提示“关联约拍不存在或已下架”，不回退到消息 ID。
- 测试：`frontend/src/utils/__tests__/message-navigation.test.ts` 覆盖 5 个导航分支。

## GAP-I-01

- 原问题：前端提交 `POST /feedback/add`，后端无端点，上线后必然失败。
- 后端新增：`system-wap/FeedbackController`、`FeedbackServiceClient`、`system-restapi/FeedbackController`、`system-domain/FeedbackService`、`FeedbackRepository`、`Feedback`、`FeedbackQo`。
- 参数契约：`content` 必填，trim 后 10-500 字；`contact` 选填，最大 100 字。
- 安全处理：userid 从 Token 上下文获取，不信任前端 userid；内容和联系方式替换 `<`、`>`；联系方式不写普通日志；Redis 60 秒简单限频。
- 数据库：`backend/dev/mysql/20260628_create_feedback.sql` 新建 `t_feedback`。
- 前端新增：`frontend/src/api/modules/feedback.ts` 和 `FeedbackAddParams`。
- 测试：`FeedbackControllerTest` 5 条；前端 API contract(接口契约) 测试覆盖 `/feedback/add`。

## GAP-J-01

- 统一开关：`frontend/src/config/features.ts`，`deposit=false`。
- 发布约拍：关闭时不展示保证金选项；提交时强制 `creditflag=0`；历史草稿若带 `creditflag=1`，提交前重置并提示。
- 报名：后端返回 `1011` 时展示“该约拍要求信用保证金 / 当前版本暂未开放保证金服务，暂时无法报名该约拍。”，仅提供“知道了”。
- 上线数据检查：见 `docs/release/PRODUCTION_LAUNCH_TODO.md` 中只读 SQL。

## GAP-G-01

- 旧版证据：`91pai-master/pages/mine/realname/` 包含正面、反面、手持身份证三照和审核费链路。
- 新版策略：两照免费认证。
- 后端兼容：`OauthQo.pics` 是列表；`UserService.oauth` 不强制三张；本轮修复 `OauthController.add` 兼容 dataURL。
- 管理后台兼容：`system-web/templates/manage/user/detail.html` 遍历 `user.pics`，不访问 `pics[2]`。
- 决策文档：`docs/decisions/ADR-REALNAME-PHOTO-AND-FEE-POLICY.md`。

## 虚假交互

- 关注：无后端关注功能，详情页不再提供本地假关注。
- 私信：无即时聊天，详情页不再提示“打开私信”，改为真实用户主页入口。
- 分享：小程序使用页面级 `onShareAppMessage`；H5/通用端复制真实详情路径，不再提示虚假“已分享”。
