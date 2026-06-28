# 预发验证执行摘要

更新时间：2026-06-28 15:10 +0800

## 基线

- 任务分支：`codex/pre-release-verification`
- 基线 main：`b3a5e433921d3f4d766caf398f1c4189506bd473`
- 目标：建立预发验证、CI、上线前自动检查和生产上线准备证据。

## 当前结论

`CONDITIONALLY_READY`。

代码层面的预发准备已经完成：新增 CI(持续集成) 工作流、生产 preflight(上线前检查) 脚本、API(应用程序接口) 鉴权冒烟脚本、预发配置示例、密钥外置改造、反馈表 migration(数据库迁移) 验证证据和预发验收文档。

本轮未部署正式生产，也未连接生产数据库、未发起真实微信支付、未使用真实用户隐私数据。由于正式 HTTPS(超文本传输安全协议) 域名、微信合法域名、正式第三方密钥、预发完整依赖和真实支付/实名审核人工联调仍未完成，不能标记为 `PRE_RELEASE_READY` 或生产可发布。

## 已完成

- 从最新远端 `main` 创建独立 worktree(工作树) 和任务分支。
- 建立 `docs/release/pre-release-verification/` 证据目录。
- 扫描并外置后端历史硬编码密钥、密码和 keystore(密钥库) 文件。
- 新增 `frontend/.env.staging.example` 和 `backend/.env.staging.example`。
- 在本地 Docker MySQL(MySQL 数据库) 中验证反馈表建表、索引、幂等和回滚脚本。
- 使用 Docker Compose(Docker 编排) 验证本地 MySQL 和 Redis(Redis 缓存) 依赖健康。
- 启动当前 worktree 的 WAP(Wireless Application Protocol，移动端后端) 服务并执行匿名/私有 API 安全冒烟。
- 执行前端 type-check(类型检查)、lint(代码规范检查)、test(测试)、H5 构建、小程序构建和 check(综合检查)。
- 执行后端 Maven(Maven 构建工具) 测试，20 个测试通过。
- 新增 GitHub Actions(GitHub 自动化流水线) CI。
- 新增 `scripts/release/preflight-check.sh` 和 `scripts/release/api-security-smoke.sh`。

## 关键阻塞

- `frontend/.env.production` 仍是 HTTP IP，构建产物包含 `82.156.14.216`，生产发布必须阻断。
- 当前测试环境未完整启动 `system-restapi`、注册中心和真实业务数据库，部分公开接口只能验证“未被鉴权错误拦截”，不能证明业务数据流完整。
- 真实微信登录、真实支付、实名审核后台联调、微信小程序后台合法域名配置均为人工验证项。
- 反馈表 migration 已在本地可销毁库验证，生产仍需 DBA 或运维在目标环境手工执行。
