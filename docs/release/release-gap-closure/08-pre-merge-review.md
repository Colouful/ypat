# 合并前独立审查

## 基线

- main SHA：06e454465448af0e052d47a0420a8142fa1eaf65
- feature SHA：276c7be60a30d38446b91c0f228a923251d1b739
- 审查时间：2026-06-28 12:41:53 +0800
- 审查范围：`origin/main...HEAD` 全量 diff，重点覆盖 SecurityConfig、Token 刷新、公开接口白名单、反馈接口、消息跳转、保证金开关、实名认证、虚假交互、pnpm workspace 变更、敏感信息扫描和合并门禁。

## Findings

| ID | 严重度 | 文件 | 问题 | 处理 |
|---|---|---|---|---|
| PMR-001 | P1 | `backend/system-wap/src/main/java/com/ypat/config/WebSecurityConfig.java` | WAP 安全白名单仍匿名放行 `/manage/**`，该路径属于管理后台历史路径，不应作为移动端公开接口。 | 已删除 POST/GET 白名单中的 `/manage/**`，新增 `WebSecurityConfigSourceTest` 防回退。 |
| PMR-002 | P1 | `backend/system-wap/src/main/java/com/ypat/controller/UserController.java`、`MypatInfoController.java` | 资料修改、我的发布、我的申请、收藏入口仍可能信任前端传入的用户 ID。 | 已统一以后端 Token 里的当前用户为准，忽略客户端 `userid/id`。 |
| PMR-003 | P1 | `backend/system-wap/src/main/java/com/ypat/controller/BillController.java`、`RecordController.java`、`backend/system-domain/src/main/java/com/ypat/service/BillService.java` | 账单和流水存在按 ID 读取或直接新增的 WAP 暴露面；账单分页未绑定当前用户订单。 | 已关闭 WAP 侧 get/add，账单分页按当前用户订单 `out_trade_no` 过滤，流水分页保持当前用户过滤。 |
| PMR-004 | P1 | `backend/system-wap/src/main/java/com/ypat/controller/OauthController.java`、`YpatInfoController.java` | WAP 侧实名认证详情和审核/推荐管理动作存在普通用户越权风险。 | 已限制实名详情只能读取当前用户；WAP 侧管理审核入口显式拒绝，管理后台继续走 `system-web`。 |
| PMR-005 | P2 | `backend/system-wap/src/main/java/com/ypat/controller/FeedbackController.java` | 反馈限频依赖 Redis，Redis 异常时会导致反馈接口 500。 | 已改为 fail-open(失败放行)：Redis 正常限频，异常记录 warn 且继续提交；新增 Redis 异常测试。 |
| PMR-006 | P2 | `backend/dev/mysql/20260628_create_feedback.sql`、`docs/release/PRODUCTION_LAUNCH_TODO.md` | 反馈表脚本是手工 SQL，不会自动执行；原文档未包含完整执行和回滚说明。 | 已补充校验注释、回滚脚本和生产执行/备份/回滚待办。 |
| PMR-007 | P3 | `frontend/pnpm-workspace.yaml` | 新增 pnpm workspace 配置需要确认必要性。 | 保留。该文件只配置 pnpm 11 的 `allowBuilds`，未声明 packages，不引入多包 workspace，用于保证依赖构建脚本可控执行。 |
| PMR-008 | P0 | `backend/system-web/src/main/java/com/ypat/third/wxmess/WXConfig.java`、`backend/system-web/src/main/resources/dev/systemprop.yml`、`backend/system-web/src/main/resources/pro/systemprop.yml`、`backend/system-wap/src/main/resources/conf/sys_conf.properties`、`backend/system-wap/src/main/test/com/mock/MockTest.java` | 敏感信息扫描发现历史硬编码微信 AppSecret、支付 key、百度密钥和 mock access_token。 | 已改为环境变量读取或占位，源码只保留变量名；上线待办要求轮换曾入库密钥。 |

## 关键安全结论

- `/manage/**` 不再被 WAP 匿名放行。
- `/user/token` 仍必须依赖当前有效 Token，不接受裸 `mobile` 签发。
- 私有 GET 接口默认需要认证；本次补充了用户归属覆盖，减少客户端伪造 `userid` 的越权面。
- WAP 侧管理审核能力已拒绝，管理动作应由 `system-web` 管理后台 session 链路执行。
- 源码中的历史微信密钥和 access_token 已移除；如这些值曾用于真实环境，必须在平台侧轮换。

## 数据库迁移结论

反馈表采用手工运维 SQL；本轮没有自动执行生产数据库 migration。已新增回滚脚本，并在 `PRODUCTION_LAUNCH_TODO.md` 记录执行人、执行环境、执行时间、备份、校验和回滚要求。

## Redis 依赖结论

反馈接口限频依赖 Redis。修复后 Redis 正常时 60 秒限频；Redis 异常时记录 warn，但不阻断反馈提交；日志不记录反馈内容和联系方式。

## API 与路由结论

- 公开接口白名单保留登录、公开约拍、Banner、文章、地区、模板、参数、商品和支付通知。
- 管理后台 `/manage/**` 不属于 WAP 公开接口。
- `/bill/get`、`/bill/add`、`/record/get`、`/record/add` 在 WAP 侧拒绝，避免用户侧直接读取或写入财务记录。

## 测试结论

已新增并通过后端相关模块测试：`FeedbackControllerTest`、`WebSecurityConfigSourceTest`、`WapAuthorizationSourceTest`、`BillServiceAuthorizationSourceTest`。全量前端、后端测试结果见 `05-test-evidence.md` 和 `artifacts/test-results.txt`。

## 是否允许合并

待全量测试、PR 检查和最终 Head SHA 校验通过后允许合并；生产发布状态仍最高为 `CONDITIONALLY_READY`。
