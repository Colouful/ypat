# 后端安全审计

## BE-SEC-01 Token 刷新

- 是否属实：是。
- 修改前：`/user/token` 接收 `mobile`，`UserService.getToken` 通过 `findByMobile(mobile)` 查用户并签发 JWT。
- 风险：攻击者可伪造手机号获取他人 Token，属于生产阻塞级认证漏洞。
- 修改后：`LoginController.token` 读取 `UserUtil.getUserId()`；`UserService.getToken(UserQo, authenticatedUserId)` 只按当前认证用户 ID 查询用户并续签，忽略请求里的 `mobile`。
- 前端契约：`request.ts` 刷新 Token 不再传 `mobile`，只携带当前 `Token` header。
- 测试：`UserServiceTokenRefreshTest` 覆盖忽略伪造 mobile、无认证用户拒绝、未知认证用户拒绝。
- 状态：FIXED。

## BE-SEC-02 GET 匿名访问

- 是否属实：是。
- 修改前：`WebSecurityConfig` 对 `HttpMethod.GET, "/**"` 执行 `permitAll`。
- 风险：用户资料、我的约拍、消息、账单、实名等私有 GET 接口可绕过认证过滤器，只依赖 handler 参数，容易形成越权入口。
- 修改后：删除 `GET /**`，建立明确公开 GET 白名单：登录验证码、公开约拍列表和详情、Banner、文章、地区、模板、参数、商品和支付通知；其余接口默认认证。
- 合并前复审：WAP 白名单中残留的 `/manage/**` 已删除。`/manage/**` 属于 `system-web` 管理后台历史路径，不是 WAP 公开接口。
- 重点私有接口：用户资料、我的发布、我的申请、我的收藏、消息、消息详情、联系方式、钱包、收支记录、账单、实名认证、Token 刷新均不在匿名 GET 通配白名单。
- 私有数据归属：资料修改、我的发布、我的申请、我的收藏、账单分页、流水分页、实名详情均以后端当前 Token 用户为准，不信任前端传入的 `userid/id`。
- 管理动作：WAP 侧 `/ypat/audit/list`、`/ypat/audit`、`/ypat/upRecom`、`/oauth/audit` 显式拒绝；管理后台继续使用 `system-web` session 鉴权链路。
- 测试：后端 `mvn test` 编译并执行安全相关单测；新增 `WebSecurityConfigSourceTest`、`WapAuthorizationSourceTest`、`BillServiceAuthorizationSourceTest`。
- 状态：FIXED。

## 反馈 Redis 限频

- 是否属实：是，初版反馈接口在 Redis 不可用时会导致提交失败。
- 修改后：Redis 正常时按 `feedback:add:<userid>` 做 60 秒限频；Redis 异常时记录 warn 并继续提交反馈。
- 日志约束：仅记录用户 ID 和异常类型，不记录反馈内容或联系方式。
- 测试：`FeedbackControllerTest.addAllowsSubmissionWhenRedisIsUnavailable`。
- 状态：FIXED。

## 硬编码密钥扫描

- 是否属实：是。合并前扫描发现 `system-web` 管理后台历史微信配置类包含硬编码 AppSecret 和支付 key，`MockTest` 包含历史 access_token。
- 修改后：`WXConfig` 改为读取 `YPAT_WEB_WX_APP_ID`、`YPAT_WEB_WX_APP_SECRET`、`YPAT_WEB_WX_MCH_ID`、`YPAT_WEB_WX_PAY_KEY` 环境变量；管理后台 dev/pro 百度 OCR 配置改为环境变量占位；WAP `sys_conf.properties` 中微信、支付和百度密钥改为环境变量占位；短信 mock 默认关闭；mock 文件改为读取测试环境变量。
- 残余要求：如历史值曾用于真实环境，必须在微信和支付平台轮换密钥；不得把真实值写入 Git、镜像明文层或日志。
- 测试：敏感信息扫描复核。
- 状态：FIXED_CODE，OPS_ROTATION_REQUIRED。

## 残余风险

- 本轮未接入真实运行环境，未执行带真实网关、真实 Redis、真实数据库的端到端越权测试。
- `mvn test` 日志中存在 logback(日志框架) 文件 appender 打开失败噪声，但测试退出码为 0，Surefire 汇总为 14 passed。
- 上线前仍需在预发环境用不同账号 Token 做接口级越权联调。
