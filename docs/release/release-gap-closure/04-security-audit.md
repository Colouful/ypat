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
- 修改后：删除 `GET /**`，建立明确公开 GET 白名单：登录验证码、公开约拍列表和详情、Banner、文章、地区、模板、参数、商品、支付通知和管理端路径；其余接口默认认证。
- 重点私有接口：用户资料、我的发布、我的申请、我的收藏、消息、消息详情、联系方式、钱包、收支记录、账单、实名认证、Token 刷新均不在匿名 GET 通配白名单。
- 测试：后端 `mvn test` 编译并执行安全相关单测；安全白名单变更通过静态审计记录在本文件。
- 状态：FIXED。

## 残余风险

- 本轮未接入真实运行环境，未执行带真实网关、真实 Redis、真实数据库的端到端越权测试。
- `mvn test` 日志中存在 logback(日志框架) 文件 appender 打开失败噪声，但测试退出码为 0，Surefire 汇总为 14 passed。
- 上线前仍需在预发环境用不同账号 Token 做接口级越权联调。
