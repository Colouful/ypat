# YPAT 企业级发布验收报告

## 1. 文档元数据

| 项目 | 值 |
|---|---|
| GitHub 仓库 | https://github.com/Colouful/ypat |
| Pull Request | https://github.com/Colouful/ypat/pull/1 |
| 基准分支 | `main` |
| 修复分支 | `codex/fix-p0-runtime-contracts` |
| 文档生成前代码 SHA | `900377ebb4f26c335cee66b1336b937b5d0f167c` |
| 验收日期 | 2026-06-25 |
| 发布目标 | 微信小程序；H5/App 不在本次生产发布范围 |

> 本报告区分“源码静态复核”“外部 AI 提供的命令结果”和“本环境独立执行结果”。没有获得真实命令输出或真机证据的项目一律标记为未验证。

## 2. 本轮已集成的关键修改

| 文件/模块 | 修改内容 | 对应问题 | 静态复核 |
|---|---|---|---|
| `frontend/src/api/request.ts` | 兼容 `{code,msg,res}` 与 `{code,message,result}`，保留 `0/false/空字符串/null` | 全局响应契约 | 已复核 |
| `frontend/src/api/modules/user.ts` | `wxLogin` 使用真实蛇形字段；`getLinkWay` 改为 `userid,messid` | 登录、联系方式契约 | 已复核 |
| `frontend/src/api/modules/content.ts` | 地区和参数接口改为对象响应 | 内容契约 | 已复核 |
| `frontend/src/api/types/area-types.ts` | 新增真实地区、参数类型 | TypeScript 缺失类型 | 已复核 |
| `frontend/src/pages-sub/content/message-detail.vue` | 未登录保护、两参数调用、失败不暴露联系方式 | 联系方式扣费流程 | 已复核 |
| `backend/system-wap/.../OrderController.java` | 创建订单回传 `out_trade_no`；新增 `/order/status` | 支付确认缺失 | 已复核 |
| `backend/system-domain/.../OrderService.java` | 分页查询增加 `userid/out_trade_no/type/status` 条件 | 订单归属与串单 | 已复核 |
| `frontend/src/api/modules/payment.ts` | 新增服务端订单状态查询 | 支付确认 | 已复核 |
| `frontend/src/components/business/RechargePanel.vue` | 服务端轮询确认，页面卸载停止，不在前端增加余额 | 假支付成功 | 已复核 |
| `frontend/src/pages-sub/user/bills.vue` | 修正 `0=未支付、1=已支付` 的状态语义 | 状态误判 | 已复核 |
| `frontend/src/config/env.ts` | 生产 API/图片地址强制 HTTPS | 生产环境安全 | 已复核 |
| `frontend/src/static/default-avatar.png` | 新增 200×200 PNG | 默认头像 404 | 已复核存在 |
| `frontend/src/static/default-cover.png` | 新增 750×500 PNG | 默认封面 404 | 已复核存在 |
| `frontend/vitest.config.ts`、`frontend/test/setup.ts` | 新增纯 TypeScript 测试环境 | 测试无法执行 | 已复核配置 |
| `frontend/src/api/__tests__/*` | 响应映射、API 参数契约测试 | 核心契约回归 | 已添加 |
| `frontend/src/services/__tests__/auth-storage.test.ts` | Token、用户存储、清理回调测试 | 登录状态回归 | 已添加 |
| `frontend/package.json` | 移除 `--passWithNoTests` | 测试假通过 | 已复核 |
| `frontend/src/composables/useForm.ts` | 修复多余分号 ESLint 错误 | 质量门禁 | 已修复 |
| `frontend/src/pages-sub/user/realname.vue` | 清除不规则全角空格 | 质量门禁 | 已修复 |

## 3. 修复矩阵

| 编号 | 问题 | 状态 | 说明 |
|---|---|---|---|
| P0-01 | 后端 `msg/res` 与前端 `message/result` 不一致 | 已修复 | 同时保留旧结构兼容 |
| P0-02 | `AreaInfo/ParamInfo` 缺失且响应形状错误 | 已修复 | 独立契约类型文件 |
| P0-03 | `getLinkWay` 参数契约错误 | 已修复 | 后端真实接口仅接收两个参数 |
| P0-04 | TypeScript 原有 3 个阻断错误 | 待重新验证 | 源码层已处理，未在当前集成 SHA 上独立运行 |
| P1-01 | 默认头像、封面缺失 | 已修复 | 两个真实 PNG 已进入分支 |
| P1-02 | `wxLogin` 字段命名不一致 | 已修复 | 使用 `WxSessionResult` |
| P1-03 | Vitest 无法运行/无测试假通过 | 已修复待执行 | 新增纯 TS 配置和测试，脚本不允许无测试通过 |
| P1-04 | 创建订单不返回订单号 | 已修复 | 返回 `out_trade_no` |
| P1-05 | 前端仅轮询余额确认支付 | 已修复待联调 | 改为按当前用户、交易号查询订单状态 |
| SEC-01 | `/order/status` 订单归属 | 已修复 | 服务层真实过滤 `userid + out_trade_no` |
| SEC-02 | 旧 `/order/get` 归属校验 | 未修复 | 仍可能按任意 id 查询，发布前必须收紧 |
| SEC-03 | 旧 `/order/findPage` 用户范围 | 未修复 | WAP 接口仍接受客户端筛选条件，发布前必须强制当前用户 |
| SEC-04 | 前端篡改 `total_fee` | 未修复 | 拍拍豆金额仍需在后端从商品表重新计算 |
| LINT-01 | 首页两个空 catch | 未修复 | `frontend/src/pages/home/index.vue` |
| LINT-02 | 首页残留 `as any` | 未修复 | 应直接使用 `BannerListParams` |

## 4. CLI 验证状态

当前运行环境无法通过网络克隆私有仓库，因此以下命令尚未在本次最终集成 SHA 上独立执行，不能标记为通过：

| 检查项 | 命令 | 状态 |
|---|---|---|
| 干净安装 | `npm ci` | 未验证 |
| TypeScript | `npm run type-check` | 未验证 |
| ESLint | `npm run lint` | 已知不通过：首页仍有两个空 catch |
| 单元测试 | `npm run test` | 未验证 |
| H5 构建 | `npm run build:h5` | 未验证 |
| 微信小程序构建 | `npm run build:mp-weixin` | 未验证 |
| 后端编译 | `mvn -pl system-wap,system-domain -am test -DskipTests` | 未验证 |

外部 AI 曾在其临时克隆和临时提交上报告 `type-check/test/build` 通过，但该结果不是当前远端最终 SHA 的独立证据，不能替代重新验证。

## 5. 单元测试

已新增：

- 响应结构映射测试；
- API 参数契约测试；
- Token 和用户信息存储测试。

当前未获得最终集成 SHA 的真实测试运行输出，因此测试数量、通过数和失败数标记为未验证。

## 6. 支付专项验收

| 检查项 | 状态 | 说明 |
|---|---|---|
| 创建订单返回 `out_trade_no` | 已实现 | 后端返回给小程序 |
| 状态接口按 Token 用户过滤 | 已实现 | `/order/status` 不接收前端 userid |
| 服务层按 userid 和交易号过滤 | 已实现 | 防串单 |
| 前端服务端确认 | 已实现 | 最多 10 次、每次 2 秒 |
| 页面卸载停止轮询 | 已实现 | `onUnload` 取消 |
| 前端不自行增加余额 | 已实现 | 成功后重新获取用户信息 |
| 微信支付取消 | 已处理 | 显示取消提示 |
| 旧订单查询接口归属校验 | 未通过 | `/order/get`、`/order/findPage` 仍需加固 |
| 支付金额服务端计算 | 未通过 | 需从商品表读取价格，禁止信任 `total_fee` |
| 微信支付回调更新订单状态 | 未验证 | 未取得真实回调和数据库证据 |
| 真机支付 | 未验证 | 需要微信开发者工具/真机和商户环境 |

## 7. 登录专项验收

| 检查项 | 状态 |
|---|---|
| `uni.login` 与 code 换 session | 源码已对齐，未真机验证 |
| `openid/session_key/unionid` 字段 | 已修复 |
| Token 保存与清理 | 已实现并新增测试 |
| 并发 Token 刷新 | 源码存在单 Promise 保护，未压力验证 |
| H5 登录 | 不在本次发布范围 |
| App 登录 | 不在本次发布范围 |
| 微信小程序真机登录 | 未验证 |

## 8. 环境与安全验收

| 检查项 | 状态 |
|---|---|
| 生产 API 强制 HTTPS | 已实现 |
| 生产图片地址强制 HTTPS | 已实现 |
| 开发环境不默认连接生产 | 已实现，默认 localhost |
| 微信 AppID | 未验证真实有效性 |
| 商户配置 | 未验证 |
| npm 生产依赖漏洞可达性 | 未完成 |
| 后端 Spring Boot 1.5.9 / Spring Cloud Edgware 风险 | 高风险，需单独升级计划 |
| 敏感支付签名日志 | 仍需复核旧代码日志输出 |

## 9. 发布范围

| 平台 | 结论 |
|---|---|
| 微信小程序 | 未验证，禁止直接发布 |
| H5 | 不在本次发布范围 |
| App | 不在本次发布范围 |

## 10. 合并前硬门禁

必须在同一个最终 commit 上执行并留存完整日志：

```bash
cd frontend
rm -rf node_modules
npm ci
npm run type-check
npm run lint
npm run test
npm run build:h5
npm run build:mp-weixin

cd ../backend
mvn -pl system-wap,system-domain -am test -DskipTests
```

并完成：

1. 修复首页两个空 catch 和 `as any`；
2. 加固旧 `/order/get`、`/order/findPage`；
3. 服务端从商品数据计算订单金额；
4. 验证支付回调能把订单从未支付更新为已支付；
5. 微信小程序真机登录；
6. 微信小程序真实或沙箱支付；
7. 核对生产 HTTPS 域名、AppID、商户号；
8. 对生产依赖 Critical/High 漏洞完成可达性分析。

## 11. 最终结论

**不通过，禁止发布，禁止合并到 `main`。**

当前修复分支已经消除了全局响应契约、联系方式参数、默认资源和前端假支付成功等重要问题，并补充了服务端订单状态查询；但旧订单接口、服务端金额可信源、最终 CLI 日志和真机支付仍不满足企业级生产发布门禁。
