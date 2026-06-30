# YPAT 待办事项

> 最近更新：2026-06-30
> 关联 PR：#9（切片 1 登录/完善信息/我的页面）、#10（切片 2 邀请）、#11（切片 3 会员）
> 关联文档：`docs/sql/pending/`、`docs/migration/auth-mine-member-invite-audit.md`

---

## ✅ 已完成（合并入 main）

- PR #9 — 切片 1：登录 / 完善信息 / 我的页面迁移
- PR #10 — 切片 2：邀请端到端（后端 + 前端）
- PR #11 — 切片 3：会员端到端（最小可用）

---

## P0 — 阻塞生产上线

- [ ] **测试库执行 `docs/sql/pending/V_pending_member.sql`**
  - 2026-06-30 ✅ 已完成（`codex-ypat-keep-ui-redesign-mysql-1` 容器内）
  - 4 张表全部创建，索引 / 唯一约束建好，3 档种子套餐写入

- [ ] **SQL 结构与 Entity 逐字段核对**
  - 2026-06-30 ✅ 已完成（`MemberPlan` / `UserMember` / `MemberOrder` / `InviteRelation` 4 个 Java Entity 与 SQL DDL 字段级一致）
  - 含字段注释、主键、唯一约束、普通索引、字符集 `utf8mb4` / `utf8mb4_unicode_ci`、默认值

- [ ] **微信真机登录**
  - 状态：**未验证**
  - 缺口：开发机无微信开发者工具 + 真机；`getPhoneNumber` + `encryptedData/iv` 链路未真机打通

- [ ] **邀请完整链路**
  - 状态：**未验证**
  - 缺口：邀请主页 / 分享落地页 / 邀请记录分页，未在真机点开分享链接 → 注册 → 看到绑定关系

- [ ] **会员真实支付**
  - 状态：**未验证**
  - 缺口：`uni.requestPayment` 调起、`/wxpay/notify` 真实回调，本会话走本地跳过真机

- [ ] **支付回调**
  - 状态：**未验证**
  - 缺口：与会员真实支付同

- [ ] **重复回调幂等**
  - 状态：**单测覆盖**
  - 缺口：`MemberOrderRepository.markPaidIfPending` 条件 UPDATE 保证幂等；缺真机并发触发验证

- [ ] **跨账号订单越权验证**
  - 状态：**代码层覆盖**
  - 缺口：`MemberController.orderStatus` / `cancel` 校验 userId；缺渗透测试

---

## P1 — 产品体验与功能补齐

- [ ] **取消支付前端入口**
  - 后端 `MemberService.cancelOrder()` 已实现
  - 前端 `pages-sub/user/member-orders.vue` 需补"取消订单"按钮（半日工时）

- [ ] **`admin-audit-soon.vue` 真实后端实现**
  - 当前为占位页（PR #10 引入）
  - 依赖：`/ypat/audit/list` + `/ypat/audit`（已有）+ 审核 UI

- [ ] **会员套餐运营后台最小能力**
  - 现状：套餐只能 SQL 手工增删
  - 缺口：管理后台 CRUD（管理员 web 端）

---

## P2 — 切片 B（评估后暂不做）

- [ ] **退款**
  - 评估：需要微信支付 v3 退款接口 + 回调 + 幂等；新增 `t_refund` 表
  - 阻塞：无 sandbox 凭据；产品侧暂未提出诉求

- [ ] **续费历史时间轴**
  - 评估：当前 `t_user_member.source_order_no` 已存最近订单号，可满足"最近一次开通"查询；时间轴属于产品升级
  - 缺口：新增 `t_member_history` 增量表 + 前端时间轴 UI

- [ ] **Flyway / Liquibase**
  - 评估：项目无迁移工具，依赖 `ddl-auto: update`
  - 缺口：把 `docs/sql/pending/*.sql` 收编进 `db/migration/V*.sql`

- [ ] **微信支付 sandbox 联调**
  - 评估：会员支付 / 退款 / 关单 联调需要真实 sandbox 凭据
  - 阻塞：无 sandbox 凭据（用户确认本地跳过）

---

## P3 — 其他重构

- [ ] **InviteService N+1**
  - `InviteService.findPage` 每条记录调 `userRepository.findOne(id)` 拿昵称/手机
  - 改为批量 `IN` 查询或 LEFT JOIN

- [ ] **`t_user_member` 增加 `cancel_at` 字段**
  - 用户主动取消会员时记录，不删行（取消语义）

- [ ] **KeepMemberCard 组件抽取**
  - mine 页面"会员卡"入口内联实现，建议抽到 `components/business/KeepMemberCard.vue`

- [ ] **App OAuth 登录**
  - spec §4.4 列出但无后端能力；当前占位卡片"暂未开放"

---

## 📝 决策记录

- **2026-06-30** 切片 B 不做：当前业务使用 `source_order_no` 已经够查"最近一次开通的订单"；历史时间轴属于产品升级而非必需；退款功能需要真实 sandbox 才能联调验证
- **2026-06-30** 会员 H5 支付未做：uni-app 没有 H5 的 JSAPI 微信支付，H5 入口引导用户在小程序内开通
- **2026-06-30** 管理员判定用 openid 比对 `constants/admin.ts`：后端无 `role/isAdmin` 字段，硬编码 `Const.SYS_ADMIN` 同步；建议后续后端 `/user/get` 返回 `roles` 字段
- **2026-06-30** 种子套餐价格仅为开发占位：上线前需运营确认正式价格

---

## 🔗 相关文档

- `docs/migration/auth-mine-member-invite-audit.md` — 三切片审计
- `docs/migration/navigation-and-mine-migration.md` — 导航与我的页面迁移
- `docs/sql/pending/README.md` — SQL 执行说明
- `docs/sql/pending/V_pending_member.sql` — 待执行 SQL