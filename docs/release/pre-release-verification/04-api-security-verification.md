# API 安全验证

更新时间：2026-06-28 15:10 +0800

## 结论

当前 worktree 的 WAP 服务在本地端口 `18081` 验证通过：公开接口未被鉴权层错误拒绝，私有接口和 `/manage/` 匿名访问均返回业务码 `401`。Token(令牌) 刷新正向链路因缺少专用测试 Token，本轮仅通过自动化单元测试覆盖。

## 自动化测试覆盖

| 测试 | 覆盖点 | 结果 |
| --- | --- | --- |
| `WebSecurityConfigSourceTest` | 私有路径不在匿名白名单，`/manage/**` 不被粗放放行 | 通过 |
| `WapAuthorizationSourceTest` | WAP Controller 私有入口鉴权注解和调用链 | 通过 |
| `UserServiceTokenRefreshTest` | 无 Token、伪造用户、有效当前用户刷新 | 通过 |
| `FeedbackControllerTest` | 未登录、参数校验、Redis 异常 fail-open | 通过 |
| `SecretExternalizationSourceTest` | 历史密钥和 keystore 不回归 | 通过 |

后端总计：20 个测试，0 failure(失败)，0 error(错误)，0 skipped(跳过)。

## API 冒烟结果

执行命令：

```bash
scripts/release/api-security-smoke.sh http://localhost:18081
```

结果：退出码 0。

| 类别 | 端点 | 结果 |
| --- | --- | --- |
| 公开 | `/ypat/tc/list`、`/ypat/zx/list`、`/ypat/get`、`/banner/list`、`/article/list`、`/product/list` | HTTP 200，业务码 `1002`，说明未被鉴权拒绝；业务数据依赖 `system-restapi`，本轮未完整启动。 |
| 公开 | `/area/list`、`/tmplid/list`、`/param/list` | HTTP 200，业务码 `200`。 |
| 私有 | `/user/get`、`/my/ypat/send/list`、`/my/ypat/rec/list`、`/mess/get`、`/feedback/add`、`/oauth/get`、`/order/get`、`/order/findPage`、`/bill/findPage`、`/record/findPage`、`/user/token` | HTTP 200，业务码 `401`，匿名被拒绝。 |
| 管理 | `/manage/` | HTTP 200，业务码 `401`，匿名被拒绝。 |

完整输出：`docs/release/pre-release-verification/artifacts/endpoint-results.txt`。

## 越权验证状态

| 场景 | 自动化 | 本轮状态 |
| --- | --- | --- |
| A 读取 B 消息 | Controller/Service 静态测试覆盖资源归属关键路径 | `CODE_READY` |
| A 读取 B 账单 | `BillServiceAuthorizationSourceTest` 覆盖归属校验 | `CODE_READY` |
| A 读取 B 收支记录 | 静态测试覆盖私有端点鉴权 | `CODE_READY` |
| A 修改 B 资料 | 私有端点匿名拒绝；双用户真实联调未执行 | `MANUAL_REQUIRED` |
| A 用 B userid 绕过 | Token 刷新测试覆盖身份来自 Token，不接受裸 mobile | `CODE_READY` |
| 普通用户调用审核接口 | 管理端匿名不放行；角色权限需完整管理端预发环境复核 | `MANUAL_REQUIRED` |

## 非当前 worktree 服务说明

本机 `localhost:8081` 上存在一个非本 worktree 启动的旧本地服务，曾返回测试手机号相关数据。该服务不作为本轮验收依据。本轮实际验证的当前代码服务为 `localhost:18081`，私有接口匿名访问均被拒绝。

## 限制

- 未配置 `YPAT_TEST_TOKEN`，有效 Token 刷新正向冒烟未执行，依赖 `UserServiceTokenRefreshTest` 自动化测试证明。
- 未准备两个真实测试用户和完整业务库，双用户端到端越权验证需预发环境补跑。
- 真实微信登录和支付回调属于平台联调，标记 `MANUAL_REQUIRED`。
