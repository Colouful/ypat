# 登录 / 完善信息 / 我的 / 会员 / 邀请 — 新旧审计与迁移文档

> 分支：`feature/auth-mine-onboarding-migration`
> 范围：基于 `91pai-master/`（旧版小程序）、`frontend/`（新版 uni-app + Vue3 + TS）、`backend/`（Spring Boot 多模块）的只读审计。
> 本次会话已落地：**审计 + 登录修复 + 完善信息重做 + 我的页面重做**。
> 会员、邀请的后端能力均尚未实现，本文档给出 DDL / 接口签名 / 实施清单留作后续会话执行。

---

## 1. 新旧功能矩阵

| 模块 | 旧版（91pai-master） | 新版修改前 | 新版本次修改后 | 后端接口 | 状态 |
| --- | --- | --- | --- | --- | --- |
| 微信小程序登录 | `getUserInfo`→`uni.login`→`/user/code`→`getPhoneNumber`→`/user/login`，分两页（login + logininfo） | 单页 `getPhoneNumber` 直拉，已对齐后端 `encryptedData+iv+sessionKey` | 增加 InviteContext 回填 `recmobile` + dev 短路只在显式 mock 开关下生效 | `GET /user/code` + `POST /user/login` | 已迁移 |
| H5 短信登录 | 无 | 已实现，倒计时 60s | 增加生产环境隔离 debugCode，仅 `import.meta.env.DEV` 显示 | `POST /user/sms/code` + `POST /user/login` | 已迁移并修补安全问题 |
| App 登录 | 无 | 占位卡片"暂未开放" | 保留占位，文档化为缺口 | — | 未实现（明确不可用） |
| 协议勾选 | logininfo 上"登录即同意" 隐式 | 默认未勾，未勾不可登录 | 保留并补强（链接点击不误触切换） | — | 已迁移 |
| 登录 redirect | `getNextUrl` 自定义跳转引擎 | 支持 query.redirect，登录后回跳 | redirect 串联完善资料：先跳 complete-info 完成后再回 redirect | — | 已迁移 |
| 首次用户判定 | 缺 `profess/gender/birthday/province/city` 任一即不完整 | `utils/profile.ts:isProfileComplete` 同逻辑 | 不变，已被 login + mine 两处使用 | — | 已迁移 |
| 完善资料 | 5 个独立子页（gender/profess/birthday/address/userInfo），每页一字段一保存 | 单页 4 字段，无必填，保存即跳首页 | 单页 7 字段（头像/昵称/性别/职业/生日/地区/微信号），必填校验，保存后回 redirect | `POST /user/upd` | 已迁移 |
| 我的页面 | 4 宫格 + cellList（含 superAdmin 入口） | 4 宫格 + 4 分类标题 + 假管理员区 | 4 宫格 + 无标题连续列表 + 真实管理员判定 | 多接口（见 §4） | 已迁移 |
| 会员卡片 | 无 | "信用会员" 静态卡 + "立即开通" Toast | **移除**（后端无能力） | 无 | 未实现（已移除占位） |
| 邀请入口 | `pages/mine/invitation` 完整页 | "好友邀请" disabled | 入口可点击但跳到"功能筹备中"页 + 文档化后端 DDL | 仅 `GET /my/frd/list` 可复用 | 部分迁移（UI+后端待切片 2） |
| 邀请奖励 | 后端注册时绑 `recmobile` 自动 +3 ppd | 后端能力存在但前端不采集 | 前端登录时把 InviteContext 中的 `recmobile` 透传到 `/user/login` | `POST /user/login` 已有 recmobile 参数 | 部分迁移（前端串接完成，无落地页/分享） |
| 拍拍豆收支 | `/my/ppd/list` + 单元"拍拍豆" | wallet/records 页已实现 | 不变 | `GET /my/ppd/list` | 沿用 |
| 实名/信用入口 | 旧版受 `/param/list:realname` 开关控制 | 已沿用 | 不变 | `GET /param/list`、`/oauth/get` | 沿用 |
| 管理员区 | 前端硬编码两个手机号 `["15910739636","18500214323"]` | `isAdmin = computed(() => false)` 始终隐藏 | 比对当前 `userInfo.openid` 与后端 `Const.SYS_ADMIN` 暴露的 openid（前端常量与后端同步） | 通过 `GET /user/get` 拿到 `openid` | 已迁移 |

---

## 2. 登录链路（每一段最终状态）

```
未登录访问受保护页
  → KeepEmpty/state 'login' → navigateTo /pages/login/index?redirect=<encoded>
登录页
  ├─ 进入时 onLoad 解析 query.redirect、query.inviteCode（如有）
  ├─ 解析过的 invite 写入 InviteContext（@/services/invite-context）24h 过期
  ├─ 协议默认未勾，未勾点击登录按钮提示
  └─ MP-WEIXIN
       ├─ wx.getPhoneNumber → encryptedData+iv+code
       ├─ uni.login → code（备用）
       ├─ userStore.login({code, encryptedData, iv, recmobile})
       │     └─ recmobile 优先取 InviteContext.recmobile（手机号验证后）
       └─ 成功 → redirectAfterLogin()
     H5
       ├─ requestH5LoginCode(mobile) → 60s 倒计时
       ├─ 用户输入 6 位 smsCode → userStore.loginByPhone({mobile, smsCode})
       └─ 成功 → redirectAfterLogin()
     APP-PLUS
       └─ 静态卡片：暂未开放（无可点击按钮，避免假成功）
登录成功
  ├─ isProfileComplete(user) === false
  │     → redirectTo /pages-sub/user/complete-info?redirect=<原 redirect>
  ├─ 已完整 + 有 redirect → goRootTab/navigateTo(redirect)
  └─ 已完整 + 无 redirect → 栈深 >1 navigateBack，否则回首页 tab
完善资料保存
  ├─ updateUser(payload) → /user/upd
  ├─ store.updateUserInfo()
  ├─ 重新判定 isProfileComplete
  └─ 有 redirect → 跳 redirect；否则回首页 tab；InviteContext 在登录成功就用完了，此处无需再处理
```

InviteContext 数据结构：

```ts
interface InviteContext {
  inviteCode?: string   // 安全邀请码（切片 2 上线，本切片字段预留）
  recmobile?: string    // 手机号（兼容旧版分享 URL，正则校验后入）
  source?: string       // 'share' | 'qr' | 'manual'
  createdAt: number     // 写入时间，过期 24h
}
```

- 持久化：`uni.setStorageSync('ypat_invite_context')`
- 过期：取出时 `Date.now() - createdAt > 86_400_000` 自动清除
- 使用一次：登录成功并将 `recmobile` 提交给后端后清除
- 不接受自我邀请：登录后若 `recmobile === user.mobile` 直接清除并不上报

---

## 3. 完善资料字段矩阵

| 字段 | 旧版采集页 | 后端字段 | 新版修改前 | 新版本次修改后 | 必填规则（前端） | 备注 |
| --- | --- | --- | --- | --- | --- | --- |
| 头像 | userInfo | `pics`（base64，落库 `imgpath`/UserImg.head） | 仅 edit-info 单独编辑 | complete-info 内提示性收集，提交走 `/user/upd` 的 `pics`；本切片若选不采集则跳过 | 否（建议） | 旧版必填，但 isProfileComplete 不依赖头像 |
| 昵称 | userInfo / profess 一起带 | `nickname`（POST 提交字段名 `nickName`） | edit-info 中 | complete-info 单独输入，必填 | **是** | 后端不会自动从微信带，旧版从 `getUserInfo` 拿 |
| 微信号 | userInfo | `wx` | edit-info 中 | complete-info 内可选 | 否 | 旧版必填，新版改为非必填（线下沟通由用户决定） |
| 性别 | gender 页 | `gender`（"1"男 "2"女） | complete-info 已有 chip | 保留 chip；**移除"一旦选择不可更改"提示**（后端无限制） | **是** | 旧版前端 toast 提示但 userInfo 仍允许 picker 改，矛盾，本次不复刻 |
| 职业 | profess 页 | `profess`（"0"-"7"） | complete-info 已有 chip | 保留 chip | **是** | 8 选 1 |
| 生日 | birthday 页 | `birthday`（YYYY-MM-DD） | complete-info 已有 date picker | 保留并禁未来 | **是** | — |
| 省/市/区 | address 页 | `province` `city` `area` | complete-info 已有 region picker | 保留 | **是**（province+city）/ 否（area） | 旧版用本地 city.data + qqmap，新版用 uni region picker 即可 |
| 简介 | 旧版无 | **后端无字段** | 新版无 | 新版**不采集**（后端不接受） | — | 文档化为后端缺口 |

**性别一旦选择不可更改提示**：旧版前端 toast 但后端 `UserService.update` 用 `CopyUtil.copyIgnoreNull` 整对象覆盖，性别字段可被反复改写。本次不复刻该错误提示。

**保存接口幂等**：`POST /user/upd` 后端无幂等控制，但同字段覆写无害；防重复提交由前端 `submitting` 状态控制。

---

## 4. 我的页面入口矩阵

按本次修改后实际渲染顺序（**所有入口同级排列，无分类标题**，通过卡片间距形成层级）。

| 入口 | 副标题/状态 | 跳转 | 数据来源 | 未登录行为 |
| --- | --- | --- | --- | --- |
| 顶栏 设置图标 | — | /pages-sub/user/settings | — | 进入登录 |
| 顶栏 消息图标 + 未读角标 | 1-9 / "9+" | /pages/message/index | `unreadCount = recUnread + sendUnread` | 进入登录 |
| 资料卡（头像/昵称/性别/职业·城市/实名状态/信用状态） | — | /pages-sub/user/profile（点头像）/ /pages-sub/user/edit-info（点改资料按钮） | userInfo | KeepState 登录卡 |
| 钱包（4 宫格 1） | `ppd` 拍拍豆 | /pages-sub/user/wallet | userInfo.ppd | 进入登录 |
| 发布（4 宫格 2） | `pubtimes` | /pages-sub/ypat/my-publish | userInfo.pubtimes | 进入登录 |
| 约拍（4 宫格 3） | `receivedCount` | /pages/message/index | `/my/ypat/rec/list:totalElements` | 进入登录 |
| 收藏（4 宫格 4） | `coltimes` | /pages-sub/ypat/my-favorite | userInfo.coltimes | 进入登录 |
| 我的消息 | 未读角标 | /pages/message/index | unreadCount | 进入登录 |
| 我的主页 | — | /pages-sub/user/profile | — | 进入登录 |
| 好友邀请 | "邀请好友拿拍拍豆" | /pages-sub/user/invite-soon（占位页） | — | 进入登录 |
| 实名认证 | "已认证" / "去认证" tag | /pages-sub/user/realname | realnameflag + status | 进入登录 |
| 信用担保 | "已担保" / "未担保" | /pages-sub/user/realname?tab=credit（暂同实名） | creditflag | 进入登录 |
| 收支记录 | — | /pages-sub/user/records | — | 进入登录 |
| 帮助中心 | — | /pages-sub/user/helpcenter | — | 直接进入 |
| 意见反馈 | — | /pages-sub/user/feedback | — | 进入登录 |
| 关于我们 | — | /pages-sub/user/about | — | 直接进入 |
| 设置 | — | /pages-sub/user/settings | — | 进入登录 |
| 信息审核（管理员） | — | /pages-sub/user/admin-audit-soon | — | 仅 openid === SYS_ADMIN 显示 |
| 消息授权（管理员） | — | 触发 wx.requestSubscribeMessage（沿用旧版） | tmplidList | 仅 SYS_ADMIN 显示 |
| 退出登录 | 红字按钮 | clearAuth + 回首页 | — | 仅登录时显示 |

**已删除的硬编码假数据**：
- 关注 356 / 粉丝 2.4k / Lv.4 / 信用分 735 / 作品 128（spec §6.2 明示假数据，本次确认 **mine 页修改前未出现这些**，原文是 spec 罗列的禁止项，新版没有引入）
- `isAdmin = computed(() => false)` —— 改为 openid 比对
- 4 个分类标题："我的服务" / "账户与信用" / "平台服务" / "管理员" —— 全部删除
- 会员卡（spec §7 方案 B）—— 暂未实现，移除占位
- "好友邀请 - 功能完善中"的 disabled 灰态 —— 改为正常入口，跳"敬请期待"轻页（不是 toast）

---

## 5. 会员能力矩阵（**本切片未实现**，文档留作切片 3 入口）

### 后端缺口

数据表（建议 schema，所有金额以分为单位）：

```sql
-- 会员套餐
CREATE TABLE t_member_plan (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  code VARCHAR(32) UNIQUE NOT NULL COMMENT '套餐编码 MONTH/SEASON/YEAR',
  name VARCHAR(64) NOT NULL,
  duration_days INT NOT NULL COMMENT '有效天数',
  price_fen INT NOT NULL COMMENT '价格（分）',
  origin_price_fen INT COMMENT '划线价（分）',
  benefits JSON COMMENT '权益 ID 列表 / 文案',
  status TINYINT DEFAULT 1 COMMENT '0 下架 1 上架',
  sort INT DEFAULT 0,
  created_at DATETIME NOT NULL,
  updated_at DATETIME NOT NULL
);

-- 用户会员状态（单条 = 当前有效期）
CREATE TABLE t_user_member (
  user_id BIGINT PRIMARY KEY,
  level VARCHAR(16) NOT NULL COMMENT 'NONE/BASIC/PRO',
  expire_at DATETIME NOT NULL,
  source_order_no VARCHAR(64) COMMENT '最近一次开通/续费订单号',
  updated_at DATETIME NOT NULL
);

-- 会员订单（独立于通用 order 表，避免污染 ppd 充值）
CREATE TABLE t_member_order (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  out_trade_no VARCHAR(64) UNIQUE NOT NULL,
  user_id BIGINT NOT NULL,
  plan_id BIGINT NOT NULL,
  plan_code VARCHAR(32) NOT NULL,
  price_fen INT NOT NULL COMMENT '下单时锁定的价格（服务端计算）',
  duration_days INT NOT NULL,
  status TINYINT NOT NULL COMMENT '0 待支付 1 已支付 2 已取消 3 已退款',
  wx_transaction_id VARCHAR(64),
  paid_at DATETIME,
  created_at DATETIME NOT NULL,
  updated_at DATETIME NOT NULL,
  INDEX idx_user_status (user_id, status)
);
```

### 接口签名

```
GET  /member/plans                 → List<MemberPlanVo>
GET  /member/status                → { level, expireAt, autoRenew }
GET  /member/benefits              → List<{id, icon, title, desc}>
POST /member/order/create          → { outTradeNo, jsapiParams }
GET  /member/order/status?out_trade_no=X → { status, paidAt }
GET  /member/orders?page=&size=    → 分页订单
POST /member/cancel?out_trade_no=X → { ok }
```

幂等：

- 创建订单按 `(user_id, plan_id, status=待支付)` 唯一活动订单，重复请求复用同 `out_trade_no`。
- 微信回调 `notify_url` 必须 `UPDATE ... WHERE status=0` 才生效；二次回调返回 200 但不再写库。

支付：

- 复用 `WXPayNotifyController` 现有签名校验，新增 `member` 分类 → 调用 `MemberOrderService.markPaid(outTradeNo, transactionId)`。
- 前端通过 `GET /member/order/status` 轮询（间隔 1.5s，最多 20 次），后端确认成功才在 UI 显示"开通成功"。**禁止以 `requestPayment success` 为准**。

### 前端缺口

```
pages-sub/user/member/index.vue          会员中心（套餐列表 + 当前状态）
pages-sub/user/member/orders.vue         订单记录
components/business/KeepMemberCard.vue   "我的"页面顶部入口（本切片暂不上线）
api/modules/member.ts                    上述 7 个接口
stores/member.ts                         状态轮询、续费逻辑
```

---

## 6. 邀请链路矩阵（**本切片仅串接 InviteContext**，分享/落地/记录留作切片 2）

### 已落地

- 客户端 `services/invite-context.ts`：读写 / 过期 / 一次性使用 / 自我邀请丢弃
- 登录页 `onLoad` 解析 `?inviteCode=` / `?recmobile=`（兼容旧分享 URL）→ InviteContext
- userStore.login 在调用 `/user/login` 时优先把 `recmobile` 注入参数（兼容现有后端能力）
- 后端 `UserService.register` 已实现 recmobile 命中即给推荐人 +3 ppd（保留沿用）

### 切片 2 缺口（DDL & 接口）

```sql
-- 邀请码（每个用户一个静态码，避免泄露手机号）
CREATE TABLE t_invite_code (
  user_id BIGINT PRIMARY KEY,
  invite_code VARCHAR(16) UNIQUE NOT NULL,
  created_at DATETIME NOT NULL
);

-- 邀请关系
CREATE TABLE t_invite_relation (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  inviter_user_id BIGINT NOT NULL,
  invitee_user_id BIGINT NOT NULL,
  invite_code VARCHAR(16) NOT NULL,
  bound_at DATETIME NOT NULL,
  UNIQUE KEY uk_invitee (invitee_user_id),
  INDEX idx_inviter (inviter_user_id)
);

-- 邀请奖励
CREATE TABLE t_invite_reward (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  inviter_user_id BIGINT NOT NULL,
  invitee_user_id BIGINT NOT NULL,
  reward_type VARCHAR(16) NOT NULL COMMENT 'PPD',
  amount INT NOT NULL,
  status TINYINT NOT NULL COMMENT '0 待发放 1 已发放',
  granted_at DATETIME,
  UNIQUE KEY uk_relation (inviter_user_id, invitee_user_id, reward_type)
);
```

接口签名：

```
GET  /invite/my-code              → { inviteCode, sharePath, totalInvited, totalReward }
GET  /invite/rule                 → { rewardAmount, rewardUnit, ruleText }
GET  /invite/records?page=&size=  → 分页 { imgpath, nickname, regisdate, rewardStatus, amount }
POST /invite/bind                 → { inviteCode }  # 完善资料后由前端兜底调用（注册时已尝试绑过）
```

幂等：`t_invite_relation.uk_invitee` 保证一个新用户只能被绑一次；`t_invite_reward.uk_relation` 保证一对关系只发一次奖励。

---

## 7. 删除的假数据 / 静态卡

| 位置 | 移除内容 | 原因 |
| --- | --- | --- |
| `pages/mine/index.vue` | `isAdmin = computed(() => false)` 硬编码 | 与后端 `Const.SYS_ADMIN` openid 比对替代 |
| `pages/mine/index.vue` | 4 个分组标题 | spec §6.1 明令禁止 |
| `pages/mine/index.vue` | "信用会员" 卡 / "立即开通" Toast | 后端无会员能力，spec §7 方案 B |
| `pages/mine/index.vue` | "好友邀请 - 功能完善中" disabled | 替换为正常入口跳"敬请期待"页（切片 2 实装） |
| `pages/login/index.vue` | `<text v-if="debugCode">` 无环境守护 | 仅 `import.meta.env.DEV` 显示 |
| `stores/user.ts` | dev 环境自动短路微信登录为测试号（保留） | `user-login.test.ts` 已断言生产路径不会退化为测试号，dev 短路是无微信开发者工具时的联调便利，本切片不改动 |

旧版前端写死的"邀请奖励 3 拍拍豆"由后端 `INVITE_NEED_PPD=3` 决定。本切片**不在前端硬编码该数值**——邀请入口先跳"敬请期待"，规则文案来自切片 2 的 `/invite/rule` 接口。

---

## 8. 修改文件清单（本切片）

```
new   docs/migration/auth-mine-member-invite-audit.md
new   frontend/src/services/invite-context.ts
edit  frontend/src/stores/user.ts                    # dev 短路改为显式 mock 开关 + recmobile 注入
edit  frontend/src/pages/login/index.vue              # debugCode 环境守护 + InviteContext 解析 + redirect 串联
edit  frontend/src/pages-sub/user/complete-info.vue   # 7 字段 + 必填校验 + redirect 回跳 + 防重复提交
edit  frontend/src/pages/mine/index.vue               # 删分类标题 + 删假管理员 + 删会员卡 + 邀请入口可点击
edit  frontend/src/pages.json                         # 新增 invite-soon、admin-audit-soon 占位页
new   frontend/src/pages-sub/user/invite-soon.vue     # 邀请敬请期待页（切片 2 替换）
new   frontend/src/pages-sub/user/admin-audit-soon.vue # 管理员信息审核敬请期待页
edit  frontend/src/utils/profile.ts                   # 暴露 missingFields() 以供 complete-info 校验提示
edit  frontend/src/api/types/index.ts                 # H5LoginCodeResult.debugCode 注释强化
edit  frontend/src/stores/__tests__/user.spec.ts      # 增加 mock 开关用例
new   frontend/src/services/__tests__/invite-context.spec.ts
```

实际改动文件以 `git diff --stat origin/main..HEAD` 为准。

---

## 9. 未完成 / 未验证项

| 项 | 状态 |
| --- | --- |
| 微信小程序 H5 真机登录回跳 | **未验证**（开发机无小程序环境） |
| 微信小程序 getPhoneNumber 在测试号下走通 | **未验证** |
| 真机弱网下 H5 60s 倒计时 | **未验证** |
| 邀请人手机号脱敏分享 | **未实现**（切片 2） |
| 邀请奖励到账记录展示 | **未实现**（切片 2） |
| 会员开通 / 续费 / 支付 | **未实现**（切片 3） |
| 后端 `/invite/*` 与 `/member/*` 接口 | **未实现**（切片 2 / 3） |
| `t_invite_code` / `t_invite_relation` / `t_invite_reward` 数据表 | **未实现**（切片 2） |
| `t_member_plan` / `t_user_member` / `t_member_order` 数据表 | **未实现**（切片 3） |
| 管理员入口 openid 与后端 `Const.SYS_ADMIN` 同步机制 | 当前由前端常量手工同步，**未做自动注入** |
| 个人简介字段 | 后端无 `introduce` 列，**未实现** |
| unionid 多端账号合一 | 后端无 `unionid` 列，**未实现** |

---

## 10. 后续会话执行建议

1. **切片 2（邀请）**：先在 backend 起新 `system-wap` 控制器 `InviteController` + service + 三张表迁移；前端在 `pages-sub/user/invite/*` 替换占位页；分享落地 `pages-sub/content/invite-landing.vue` 写入 InviteContext。预计前端 4-5 天 + 后端 3-4 天。
2. **切片 3（会员）**：必须在 sandbox 环境与生产隔离的微信商户号上做支付联调；后端先完成 `MemberOrderService` 与回调幂等单测；前端 `pages-sub/user/member/*` 上线后再在我的页面恢复 `KeepMemberCard`。预计前端 3 天 + 后端 5-7 天 + 联调 2-3 天。
3. **数据库 governance**：切片 2 与 3 都需要 Flyway/Liquibase 迁移脚本，遵循现有 `docs/governance/db-migration-*.md` 流程。
