# 最终上线评估

结论：`CONDITIONALLY_READY`

## 判定依据

- P0：0。
- P1：0。
- BE-SEC-01：FIXED。
- BE-SEC-02：FIXED。
- GAP-F-03：FIXED。
- GAP-I-01：FIXED。
- 前端 type-check/lint/test/H5 build/mp-weixin build/check：通过。
- 后端 `mvn test`：通过，19 tests passed。
- 合并前复审新增覆盖：`/manage/**` 白名单防回退、私有接口用户归属、账单归属过滤、反馈 Redis 故障降级。
- 正式 HTTPS 域名：未提供。
- 微信后台合法域名：未提供配置证据。
- `.env.production`：本轮按要求未修改，仍包含 HTTP IP。

## 为什么不是 READY

当前没有正式 HTTPS API 域名、正式 HTTPS 图片域名和微信小程序后台域名配置证据。构建产物扫描已检出 `82.156.14.216` 和 `http://`，不能直接发布正式生产。

## 为什么不是 NOT_READY

本轮 P0/P1、安全阻塞、反馈必失败入口、消息跳转核心错误均已关闭；合并前复审发现的 WAP 管理路径白名单和私有接口归属问题已修复；前后端自动化验证和构建均通过。剩余项集中在生产域名、证书、微信后台配置和真实环境人工联调。

## 发布门槛

升级为 READY 前必须完成：

- 更新 `frontend/.env.production` 为正式 HTTPS 域名。
- 重新执行 `pnpm install --frozen-lockfile`、`pnpm run check`、`pnpm run build:h5`、`pnpm run build:mp-weixin`。
- 重新扫描 dist，确认无 `82.156.14.216`、非预期 `http://`、`localhost`。
- 完成微信小程序合法域名配置。
- 完成真实登录、发布、报名、消息、实名、充值、支付结果确认和 Token 失效人工联调。
