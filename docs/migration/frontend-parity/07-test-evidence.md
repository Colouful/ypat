# 07 测试证据 (Test Evidence)

> 命令在 `frontend/` 下执行。退出码为空表示 0(成功)。

## 基线状态(修复前, commit bd7d3f9)
| 命令 | 退出码 | 结果 |
|---|---:|---|
| pnpm run type-check | 2 | ❌ 2 错误: pages/home/index.vue 使用不存在的 UniApp.GetSettingSuccess / OpenSettingSuccess |
| pnpm run test | 1 | ❌ 1/31 失败: request.test.ts "preserves empty string" — res:'' 被强制转 null |

> 结论: **基线 type-check 与 test 均为 RED**,在任何业务迁移前先恢复绿色基线(否则无法验证后续改动)。

## 修复后
| 命令 | 退出码 | 结果 | 修复 |
|---|---:|---|---|
| pnpm run type-check | 0 | ✅ 通过 | GetSettingSuccess→GetSettingSuccessResult ×2 (home/index.vue) |
| pnpm run test | 0 | ✅ 33/33 通过 | extractDataField 保留 falsy 值且仍兼容二次 JSON 解析;新增 2 条 ypat 列表/写操作契约回归测试 |
| pnpm exec eslint src/api | 0 | ✅ 通过 | — |

## 已修复缺陷明细
1. **GAP-API-01 (P1)** — `frontend/src/api/modules/ypat.ts`: `getMyPublishList/getMyFavoriteList/getMyReceivedList/getMySentList` 由 POST 改 GET(后端 `@GetMapping`)。回归测试: `api-contracts.test.ts` "my-ypat list endpoints use GET"。
2. **TEST-FIX-01** — `frontend/src/api/request.ts`: `mapBackendResponse` 的 res/result 提取改用 `extractDataField`,保留 0/false/''/null,仅对非空字符串二次 JSON.parse;`parseResponsePayload` 顶层空响应仍返回 null。两条原本互斥的测试现同时通过。
3. **TC-FIX-01** — `frontend/src/pages/home/index.vue`: 修正 uni getSetting/openSetting 成功结果类型名,恢复 type-check 绿色。

## 最终自动化验证(全模块完成后)
| 命令 | 退出码 | 结果 |
|---|---:|---|
| pnpm run type-check | 0 | ✅ 通过 |
| pnpm run test | 0 | ✅ 50/50 通过(新增 profile/file-base64/user-login/ypat 契约 用例) |
| pnpm run lint(--quiet 仅错误) | 0 | ✅ 0 error(存量模板格式 warning 不计入) |
| pnpm run build:h5 | 0 | ✅ Build complete |
| pnpm run build:mp-weixin | 0 | ✅ Build complete(dist/build/mp-weixin) |
| git diff --check | 0 | ✅ 无空白错误 |

## GAP-API-03 构建/运行结论(重要)
- 两个 build **均编译通过**。但生产构建把 `http://82.156.14.216:8088` 与 HTTPS 守卫错误一并打包(dist 中可见 `生产环境接口地址必须使用` 字符串)。
- env.ts 守卫在**运行时**执行: 生产环境(VITE_APP_ENV=production)+ http 地址 → `getEnvConfig()` 抛错 → **应用加载即崩溃**(H5 与小程序)。
- **用户决策: 保持守卫,改用 HTTPS 域名**。→ 前端不改 env.ts(安全控制正确);**上线前置**: 运维为后端配置 HTTPS 域名并将 .env.production 改为 https://域名。前端无法独立完成 → 列为部署阻塞项(见 08)。
- 不修改 env.ts、不放宽校验、不伪造。

## 2026-06-28 release-gap-closure 验证

| 命令 | 结果 | 关键输出 |
| -- | -- | -- |
| `pnpm install --frozen-lockfile --registry=https://registry.npmjs.org` | PASS | frozen install 通过;私有源 `nodejs.100credit.cn` 曾出现 `ECONNRESET` |
| `pnpm run type-check` | PASS | `vue-tsc --noEmit` 退出码 0 |
| `pnpm run lint` | PASS | ESLint quiet 退出码 0 |
| `pnpm run test` | PASS | 8 files passed, 57 tests passed |
| `pnpm run build:h5` | PASS | `DONE Build complete` |
| `pnpm run build:mp-weixin` | PASS | `DONE Build complete` |
| `pnpm run check` | PASS | 串联验证通过 |
| `mvn test` | PASS | 14 tests passed, 0 failures, 0 skipped |
| dist 地址扫描 | OPS_BLOCKED | 构建产物仍包含 `82.156.14.216` 和 `http://`,原因是本轮按要求未修改 `.env.production` |

证据文件见 `docs/release/release-gap-closure/artifacts/`。
