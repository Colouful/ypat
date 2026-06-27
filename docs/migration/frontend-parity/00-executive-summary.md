# 00 执行摘要 (Executive Summary)

> 任务: 以旧版 `91pai-master` 业务行为为基准,将缺失业务逻辑迁移到新版 `frontend`(保留新视觉),形成可审计证据链。
> 基线 commit: `bd7d3f9` (= origin/main) · 任务分支: `claude/frontend-legacy-parity`
> 最近更新: 见各模块小节

## 项目事实(已核验)
- 旧版: uni-app **Vue2 + Vuex + vue-cli**;页面逻辑在 `index.js`(`<script src>`);HTTP `common/vmeitime-http`;无 401 拦截;token=`UNI_LOCAL_token`。
- 新版: uni-app **Vue3 + TS + Pinia + Vite**;`src/api/request.ts`(含 401 刷新/防重/超时);token=`ypat_token`。
- 后端: Spring Cloud,前端网关 `system-wap`;**全部 form 绑定,无 JSON body**;响应 `{code,msg,res}`,成功 200;Token 头;`GET /**` permitAll。
- 路由: 旧 26 / 新 26(6主包+20分包),含义不同(见 01)。

## 已建立的证据
- 01 路由映射矩阵 ✅ · 03 接口契约矩阵(后端核验)✅ · 04 登录缓存状态 ✅ · 06 差异登记 ✅
- artifacts: old-routes.json / new-routes.json ✅
- 子代理审计: 新前端 API/Store、旧前端 API/Store、后端 Controller 契约(均已归档于本目录证据)

## 关键结论(截至基线)
新版 API 层与后端契约**大体一致**(form 绑定正确、Token、响应包、401 刷新均正确),但存在明确缺陷:
1. **GAP-AUTH-01 (P0/P1)**: 微信登录被硬编码测试账号绕过。
2. **GAP-API-01 (P1)**: 我的发布/收藏/申请列表用 POST,后端 GET-only → 405。
3. **GAP-API-03 (P1)**: 生产 http 地址触发 HTTPS 强校验崩溃。
4. 多处"内联化"重构(报名/联系方式/审核态)需逐页验证业务规则未丢失。

## 模块进度
| 模块 | 范围 | 状态 |
|---|---|---|
| 基线 | 盘点+证据 | ✅ 完成 |
| A 登录/用户初始化 | login/complete-info/edit-info | 🔄 进行中 |
| B 首页/发现/搜索 | home/discover/search | ⏳ |
| C 详情/用户主页 | detail/profile | ⏳ |
| D 发布 | publish | ⏳ |
| E 我的约拍 | my-publish/apply/favorite | ⏳ |
| F 消息/联系方式 | message/message-detail | ⏳ |
| G 实名 | realname | ⏳ |
| H 钱包/充值/账单 | wallet/recharge/records/bills | ⏳ |
| I 设置/内容/法律 | settings/about/feedback/content | ⏳ |
| J 旧版遗留能力 | invitation/credit/help... | ⏳ |
| 验证/上线 | check + release readiness | ⏳ |

## Skill 使用
| Skill | 来源 | 版本 | 用途 | 已用 |
|---|---|---|---|---|
| cloudbase | skillhub (官方) | 2.24.2-beta.1 | 小程序/微信支付/云能力辅助分析 | 已加载(本会话可用) |
| 子代理(general-purpose) | 内置 | — | 旧/新/后端三方代码审计 | ✅ |

## 上线结论
**NOT_READY**(基线阶段;P0/P1 待清零)。
