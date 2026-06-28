# 测试证据

## 汇总

| 命令 | 执行目录 | 开始时间 | 结束时间 | 退出码 | 通过数量 | 失败数量 | 关键输出 | 修复内容 |
| -- | -- | -- | -- | --: | --: | --: | -- | -- |
| `pnpm install --frozen-lockfile` | `frontend` | 2026-06-28 12:05 | 2026-06-28 12:07 | 130 | 0 | 1 | 私有源 `nodejs.100credit.cn` 大量 `ECONNRESET`，手动终止 | 记录外部镜像阻塞 |
| `pnpm install --frozen-lockfile --registry=https://registry.npmjs.org` | `frontend` | 2026-06-28 12:07 | 2026-06-28 12:08 | 1 | 0 | 1 | 依赖下载完成，但 pnpm 10 拦截 build scripts | 执行 `pnpm approve-builds --all` |
| `pnpm approve-builds --all` | `frontend` | 2026-06-28 12:08 | 2026-06-28 12:08 | 0 | 4 | 0 | `@parcel/watcher`、`core-js`、`esbuild`、`vue-demi` postinstall 完成 | 生成最小 `pnpm-workspace.yaml` 审批 |
| `pnpm install --frozen-lockfile --registry=https://registry.npmjs.org` | `frontend` | 2026-06-28 12:08 | 2026-06-28 12:08 | 0 | - | 0 | frozen install 成功 | 前端依赖可复现 |
| `pnpm run type-check` | `frontend` | 2026-06-28 12:09 | 2026-06-28 12:09 | 0 | - | 0 | `vue-tsc --noEmit` 通过 | 修复 `my-apply.vue` 消息类型 |
| `pnpm run lint` | `frontend` | 2026-06-28 12:09 | 2026-06-28 12:09 | 0 | - | 0 | ESLint quiet 通过 | 无 |
| `pnpm run test` | `frontend` | 2026-06-28 12:09 | 2026-06-28 12:09 | 0 | 57 | 0 | 8 files passed, 57 tests passed | 覆盖消息导航和反馈 API |
| `pnpm run build:h5` | `frontend` | 2026-06-28 12:09 | 2026-06-28 12:09 | 0 | - | 0 | `DONE Build complete` | H5 构建通过 |
| `pnpm run build:mp-weixin` | `frontend` | 2026-06-28 12:09 | 2026-06-28 12:10 | 0 | - | 0 | `DONE Build complete` | 小程序构建通过 |
| `pnpm run check` | `frontend` | 2026-06-28 12:10 | 2026-06-28 12:10 | 0 | 57 | 0 | 串联 type-check/lint/test/build 全通过 | 前端综合验证 |
| `mvn test` | `backend` | 2026-06-28 12:12 | 2026-06-28 12:12 | 0 | 14 | 0 | `Tests run: 14, Failures: 0, Errors: 0, Skipped: 0` | 后端安全和反馈测试 |
| dist 地址扫描 | `frontend` | 2026-06-28 12:10 | 2026-06-28 12:10 | 0 | - | - | 检出 `82.156.14.216` 和 `http://` | 记录为上线前运维阻塞 |
| `pnpm install --frozen-lockfile --registry=https://registry.npmjs.org` | `frontend` | 2026-06-28 12:57:45 | 2026-06-28 12:57:45 | 0 | - | 0 | `Already up to date` | 合并前复审安装验证 |
| `pnpm run type-check` | `frontend` | 2026-06-28 12:58:16 | 2026-06-28 12:58:18 | 0 | - | 0 | `vue-tsc --noEmit` 通过 | 合并前复审类型验证 |
| `pnpm run lint` | `frontend` | 2026-06-28 12:58:18 | 2026-06-28 12:58:21 | 0 | - | 0 | ESLint quiet 通过 | 合并前复审 lint 验证 |
| `pnpm run test` | `frontend` | 2026-06-28 12:58:21 | 2026-06-28 12:58:23 | 0 | 57 | 0 | 8 files passed, 57 tests passed | 合并前复审单测 |
| `pnpm run build:h5` | `frontend` | 2026-06-28 12:58:36 | 2026-06-28 12:58:43 | 0 | - | 0 | `DONE Build complete` | 合并前 H5 构建 |
| `pnpm run build:mp-weixin` | `frontend` | 2026-06-28 12:58:43 | 2026-06-28 12:58:47 | 0 | - | 0 | `DONE Build complete` | 合并前微信小程序构建 |
| `pnpm run check` | `frontend` | 2026-06-28 12:58:57 | 2026-06-28 12:59:13 | 0 | 57 | 0 | 串联 type-check/lint/test/build 全通过 | 合并前前端综合验证 |
| dist 地址扫描 | `frontend` | 2026-06-28 12:59 | 2026-06-28 12:59 | 0 | - | - | 检出 `82.156.14.216` 和 `http://`，未发现业务 `localhost` | 继续记录为 OPS_BLOCKED |
| `mvn test` | `backend` | 2026-06-28 13:01:31 | 2026-06-28 13:01:34 | 0 | 19 | 0 | 领域 1 条 + WAP 18 条测试通过，Reactor 全 SUCCESS | 合并前后端全量验证 |
| `mvn test` | `backend` | 2026-06-28 13:10:17 | 2026-06-28 13:10:20 | 0 | 19 | 0 | 密钥外置配置变更后，领域 1 条 + WAP 18 条测试通过 | 最终后端复核 |
| `mvn test` | `backend` | 2026-06-28 13:12:45 | 2026-06-28 13:12:48 | 0 | 19 | 0 | 短信 mock 默认关闭后，后端全量测试通过 | 最终安全配置复核 |

## 证据文件

- `artifacts/frontend-pnpm-install.log`
- `artifacts/frontend-pnpm-install-npmjs.log`
- `artifacts/frontend-pnpm-approve-builds.log`
- `artifacts/frontend-pnpm-install-final.log`
- `artifacts/frontend-type-check.log`
- `artifacts/frontend-lint.log`
- `artifacts/frontend-test.log`
- `artifacts/frontend-build-h5.log`
- `artifacts/frontend-build-mp-weixin.log`
- `artifacts/frontend-check.log`
- `artifacts/frontend-dist-address-scan.txt`
- `artifacts/backend-mvn-test.log`

## 未执行项

- 真实微信登录、真实支付、真实生产数据库变更：NOT_EXECUTED。原因：需要生产凭证、真实支付链路或不可逆生产数据操作，本轮禁止执行。
- 预发环境跨账号越权联调：NOT_EXECUTED。原因：当前只在本地代码和单测环境验证，未提供预发服务地址和测试账号。
