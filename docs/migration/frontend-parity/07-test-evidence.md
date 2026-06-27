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

## 待执行(后续模块)
- pnpm run build:h5 / build:mp-weixin: **尚未运行**。预期风险 GAP-API-03(生产 http 触发 env.ts HTTPS 强校验抛错)需先决策。dev 模式构建不受影响。
- pnpm run lint(全量)、pnpm run check(含构建): 后续模块完成后整体运行。
