# 04 登录 / 缓存 / 状态 矩阵 (Auth / Storage / State)

## A. Storage Key 对比

| 用途 | 旧版 key | 新版 key | 兼容? |
|---|---|---|---|
| Token | `UNI_LOCAL_token` | `ypat_token` | ❌ key 不同 |
| 用户资料 | `UNI_LOCAL_userInfo` | `ypat_user_info` | ❌ key 不同 |
| 定位 | `UNI_LOCAL_location` ("lng,lat") | (无独立key,仅 draftForm.location) | ❌ 缺失 |
| openid | `UNI_LOCAL_openid` / `UNI_LOCAL_openidInfo` | (无独立 key,登录响应由后端维护) | ACCEPTED |
| 邀请人手机 | `UNI_LOCAL_recmobile` | (无) | 模块J |
| 发布草稿 | `UNI_LOCAL_publishData(+Address)` | pinia persist key `ypat`(draftForm) | 行为等价 |
| 消息缓存 | `UNI_LOCAL_itemMsg` | (无) | ⚠️ |
| role | `UNI_LOCAL_role` | (无) | 旧版 adminList 驱动,新版无管理员UI |
| 地区缓存 | `UNI_LOCAL_areaData` | (无) | 可按需 |
| channel | `UNI_LOCAL_channel` | (无,login 传 channel) | ⚠️ |

## B. Storage 兼容迁移决策

**关键问题: 新旧是否为同一微信小程序(同 AppID)?**
- 若**同 AppID**: 老用户升级后 storage 仍在,但 key 从 `UNI_LOCAL_token` → `ypat_token`,会被视为未登录 → 强制重新登录(体验降级,但**安全**)。
- 若**新 AppID**(全新小程序): 无历史 storage,无需迁移。

→ 待 `manifest.json` AppID 核对(模块A)。**结论**: 即使同 AppID,token 体系/JWT 可能已变,强制重新登录是可接受且更安全的策略;**不建议**复用旧 `UNI_LOCAL_token`。本轮已修复新后端 `/user/token` 裸 mobile 重签风险,刷新必须基于当前有效 Token。

## C. 状态管理对比

| 维度 | 旧版 Vuex | 新版 Pinia |
|---|---|---|
| 模块 | userInfo(单模块) | user / ypat / app / message |
| token 位置 | 仅 storage(不在 store) | store.user.token + storage |
| 用户资料 | Vuex userInfo + storage | store.user.userInfo + storage |
| 持久化 | 手动 setStorageSync | user: 手动(auth-storage);ypat: pinia-plugin-persistedstate(仅 draftForm) |
| 退出清理 | clearStorage 保留 channel/location/areaData | logout→clearAuth(清 ypat_token/ypat_user_info)+resetMemoryState |

## D. 重点检查结论

- **退出登录**: 新版 `logout` 调 `clearAuth` + `resetMemoryState` + switchTab home ✅。需确认 `clearAuth` 是否清干净所有用户态(unreadCount 等)。
- **登录失效死循环**: 新版 `refreshToken` 单飞(refreshPromise)+ `redirectToLogin` 单飞(redirectingToLogin),且 `/user/token` 不参与刷新拦截;本轮刷新请求不再传 mobile,只带当前 Token header → 避免死循环且避免伪造手机号续签 ✅。
- **多账号串数据**: 需确认切换账号时 message/ypat store 重置(模块F/E)。
- **城市/定位缓存缺失**: 定位失败时优雅降级为全国;旧版无 GPS,当前作为增强项接受。
- **WeChat 登录**: GAP-AUTH-01 已修复,生产不再使用硬编码测试账号。
