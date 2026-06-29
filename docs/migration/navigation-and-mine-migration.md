# 导航栏与我的页面迁移审计

## 1. 导航对照

| 项目 | 旧版 | 新版修改前 | 新版修改后 |
| -- | -- | -- | -- |
| 首页 | 有，原生 Tab | 广场 | 首页 |
| 发布 | 自定义 TabBar 中间凸起按钮，`navigateTo` | 普通 Tab，`reLaunch` | 中间凸起按钮，登录后 `navigateTo` |
| 我的 | 有，原生 Tab | 有 | 有 |
| 发现 | 无独立 Tab | 有 | 从底部移除，首页保留“灵感发现”入口 |
| 消息 | 我的页面入口 | 独立 Tab | 移入我的页面顶部、我的服务列表、首页未读入口 |

## 2. 我的页面功能矩阵

| 功能 | 旧版 | 新版修改前 | 新版修改后 | 接口 | 状态 |
| -- | -- | -- | -- | -- | -- |
| 头像 | 有 | 有 | 保留，来自用户资料 | `/user/get` | 已对齐 |
| 昵称/手机号 | 手机号脱敏为主 | 昵称或固定文案 | 昵称优先，缺失时手机号脱敏 | `/user/get` | 已对齐 |
| 性别 | 有 | 无 | 展示真实性别标签 | `/user/get` | 已对齐 |
| 职业身份 | 有 | 有，但带假等级 | 仅展示真实职业身份 | `/user/get` | 已对齐 |
| 实名状态 | 旧版按 `status/realnameflag` 判断 | 静态“已实名” | 未认证/审核中/已认证/认证失败 | `/user/get` | 已对齐 |
| 信用担保状态 | 旧版按 `creditflag` 判断 | 静态信用分 | 未信用担保/已信用担保 | `/user/get` | 状态已对齐，缴纳入口缺口 |
| 我的钱包 | 拍拍豆 | 钱包入口 | 展示 `ppd` 并跳转钱包 | `/user/get` | 已对齐 |
| 我的发布 | 发布的约拍 | 有入口 | 展示 `pubtimes` 并跳转我的发布 | `/my/ypat/pub/list` | 已对齐 |
| 我的约拍 | 收到的约拍 | 有入口 | 展示收到约拍列表总数 | `/my/ypat/rec/list` | 已对齐 |
| 我的收藏 | 收藏的约拍 | 有入口 | 展示 `coltimes` 并跳转收藏 | `/my/ypat/sc/list` | 已对齐 |
| 我的消息 | 列表入口带红点 | 底部 Tab | 顶部按钮和服务列表，保留未读数 | `/my/ypat/*/unread/count` | 已对齐 |
| 我的主页 | 有 | 缺入口 | 跳转当前用户个人主页 | `/user/get`、`/my/ypat/pub/list` | 已对齐 |
| 好友邀请 | 有，赚拍拍豆 | 无 | 展示为功能完善中 | 缺新版奖励接口/页面 | 缺口 |
| 实名认证 | 有状态跳转 | 有页面但入口弱 | 服务列表展示状态并跳转 | `/oauth/get`、`/oauth/add` | 已对齐 |
| 信用担保 | 有 | 无真实入口 | 展示状态，不伪造缴纳流程 | 缺新版页面/支付流程 | 缺口 |
| 收支记录 | 有 | 子页面存在 | 服务列表跳转收支记录 | `/record/findPage` | 已对齐 |
| 帮助中心 | 静态 FAQ | 无入口 | 新增新版帮助中心页 | 静态旧版 FAQ | 已迁移 |
| 关于我们 | 有 | 子页面存在 | 服务列表跳转 | 本地页面 | 已对齐 |
| 设置 | 有 | 子页面存在 | 服务列表跳转 | 本地页面 | 已对齐 |
| 意见反馈 | 旧版无主入口 | 子页面存在 | 服务列表跳转 | `/feedback/add` | 已对齐 |
| 消息授权 | 管理员专属 | 无 | 不向普通用户展示 | 新版缺真实角色/模板授权 | 缺口 |
| 信息审核 | 管理员专属 | 无 | 不向普通用户展示 | 新版缺权限字段/页面 | 缺口 |

## 3. 删除的虚假内容

- 删除固定关注数 `356`。
- 删除固定粉丝数 `2.4k`。
- 删除固定等级 `Lv.4`。
- 删除固定信用分 `735` 和“优秀”评价。
- 删除固定作品数 `128 组`。
- 删除“信用会员”“立即开通”“专属曝光”“优先约拍”等无后端支撑会员展示。
- 删除“累计约拍/作品”等无法由当前接口真实证明的展示模块。

## 4. 后端缺口

- 缺少新版好友邀请页面、邀请规则、二维码/分享能力和奖励接口对接。
- 缺少新版信用担保缴纳页面、重复缴纳校验和支付闭环。
- 缺少新版管理员角色字段或权限接口；旧版仅按手机号写本地 `role`。
- 缺少新版消息授权页面及订阅模板授权管理流程。
- 缺少新版信息审核页面和直接路由权限保护。
- 首页同城没有经纬度反查城市接口；当前优先使用用户资料里的真实城市，未设置时降级为全国。

## 5. 修改文件清单

| 文件 | 修改内容 |
| -- | -- |
| `frontend/src/utils/tab-navigation.ts` | 根 Tab 收窄为首页/我的，新增 `openPublish`、`openMessage`、`goRootTab` |
| `frontend/src/utils/__tests__/tab-navigation.test.ts` | 增加导航行为单元测试 |
| `frontend/src/components/business/KeepTabBar.vue` | 底部导航改为首页/发布/我的三项，中间发布按钮突出 |
| `frontend/src/components/business/KeepIcon.vue` | 增加 `users`、`help-circle` 图标 |
| `frontend/src/pages/home/index.vue` | 首页承接发现入口，新增消息未读入口，修正同城城市参数 |
| `frontend/src/pages/discover/index.vue` | 从底部发现 Tab 改为保留页面，修正发布/消息跳转 |
| `frontend/src/pages/message/index.vue` | 从底部消息 Tab 改为保留页面，修正根页面跳转 |
| `frontend/src/pages/publish/index.vue` | 发布页登录跳转携带回跳 |
| `frontend/src/pages/login/index.vue` | 支持登录后 `redirect` 回跳 |
| `frontend/src/pages/mine/index.vue` | 重建“我的”真实业务模块和服务分组 |
| `frontend/src/pages-sub/user/helpcenter.vue` | 新增帮助中心 FAQ 页面 |
| `frontend/src/pages-sub/user/complete-info.vue` | 根页面跳转改为 `goRootTab` |
| `frontend/src/components/business/YpatDetailView.vue` | 空栈返回首页改为 `goRootTab` |
| `frontend/src/composables/useNavigation.ts` | 根页面、发布、消息导航职责拆分 |
| `frontend/src/stores/user.ts` | 退出登录后返回首页使用 `goRootTab` |
| `frontend/src/pages.json` | 增加帮助中心页面路由 |

## 6. 验证结果

| 项目 | 结果 |
| -- | -- |
| npm ci | 通过；存在依赖包废弃警告，未阻断安装 |
| type-check | 通过，`vue-tsc --noEmit` |
| lint | 通过，`eslint src --ext .ts,.vue --quiet` |
| test | 通过，12 个测试文件、81 个测试 |
| H5 build | 通过，`uni build` |
| 微信小程序 build | 通过，`uni build -p mp-weixin` |
