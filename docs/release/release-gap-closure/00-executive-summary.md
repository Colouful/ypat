# YPAT 上线缺口收口与安全加固执行摘要

任务名称：YPAT 上线缺口收口与安全加固

## 基线

- Git 根目录：`/Users/lizhenwei/workspace/vueworkspace/ypat-workspace-worktrees/release-gap-closure`
- 基线 main commit：`06e454465448af0e052d47a0420a8142fa1eaf65`
- 任务分支：`codex/release-gap-closure`
- 是否直接修改 main：否
- 是否 push：否
- 是否 merge：否

## 已完成修改

- GAP-F-03：将消息中心和我的申请列表按 `MessInfo` 处理，received(收到) 用消息 ID 打开消息详情，sent(申请) 只用 `ypatid` 打开约拍详情，缺失时补查 `/mess/get`，仍缺失则停止跳转。
- GAP-I-01：采用优先方案，新增持久化反馈后端端点、数据库 migration(数据库迁移脚本)、类型化前端 API 和提交状态处理。
- GAP-J-01：通过统一 `FEATURE_FLAGS.deposit=false` 关闭保证金服务；发布约拍强制 `creditflag=0`，报名遇到 `1011` 只提示当前版本暂未开放。
- GAP-G-01：确认后端和管理后台兼容两照；保留两照免费策略，修复 `/oauth/add` 对 dataURL 图片的兼容，并创建 ADR。
- BE-SEC-01：`/user/token` 不再接受仅凭 `mobile` 刷新 Token，改为基于当前有效 Token 的用户 ID 续签。
- BE-SEC-02：移除 `GET /** permitAll`，改为明确公开 GET 白名单，其余接口默认认证。
- 虚假交互：详情页移除假关注、假私信、假分享；分享在小程序走 `onShareAppMessage`，H5/通用端复制真实详情路径。
- GAP-J-02：删除不存在邀请页的悬空路由常量；历史账单邀请奖励类型仅保留用于展示旧记录。

## 测试结果

- 后端：`mvn test` 通过，14 tests passed。
- 前端：`pnpm run type-check`、`pnpm run lint`、`pnpm run test`、`pnpm run build:h5`、`pnpm run build:mp-weixin`、`pnpm run check` 均通过。
- 构建产物扫描：当前 `frontend/.env.production` 仍为 HTTP IP，dist 中能检出 `82.156.14.216` 和 `http://`，按生产发布规则禁止直接发布。

## 结论

最终上线结论：`CONDITIONALLY_READY`

代码层 P0/P1 和本轮安全问题已关闭，自动化验证通过。正式上线仍依赖运维提供 HTTPS API 域名、HTTPS 图片域名、TLS 证书、微信小程序后台合法域名，并更新 `frontend/.env.production` 后重新构建和扫描。
