# YPAT 待办事项

> 最近更新：2026-06-30
> 适用范围：YPAT uni-app + Spring Boot 项目

## ✅ 已完成（合并入 main）

- **PR #9**：切片 1 — 登录/完善信息/我的页面迁移修复
- **PR #10**：切片 2 — 邀请端到端（后端 + 前端）
- **PR #11**：切片 3 — 会员端到端（最小可用 A 范围）

## 🟡 当前会话未完成 / 待人工执行

### P0（生产上线前必须做）

- [ ] **执行 `docs/sql/pending/V_pending_member.sql`** 在生产 MySQL 上
  - 创建 `t_invite_relation` / `t_member_plan` / `t_user_member` / `t_member_order` 4 张表
  - 建立索引与唯一约束
  - 写入 3 档种子套餐
  - 详见 `docs/sql/pending/README.md`

- [ ] **真机验证（按 spec §16 明确未验证项）**
  - 微信小程序真机 `getPhoneNumber` / 邀请落地 / 会员支付全链路
  - H5 真机 60s 倒计时、邀请记录分页、登录 redirect
  - 跨端登录态同步
  - 由于本会话无真机环境，全部走代码评审 + 单测验证

### P1（产品体验小缺口）

- [ ] **前端"取消支付"按钮**：用户从支付页返回后，`/member/order/cancel` 接口已实现但前端 UI 未暴露。建议在 `pages-sub/user/member-orders.vue` 已支付订单以外的状态加"取消订单"按钮，触发 `cancelMemberOrder()` 并刷新列表
- [ ] **小程序 `admin-audit-soon.vue`**：仅占位页，待 `InfoAudit` 后端接口 + 审核 UI 一同上线

### P2（切片 B — 暂未做，待产品决策）

- [ ] **会员退款流程**：需要微信支付 v3 退款接口 + 回调 + 幂等，新增 `t_refund` 表
- [ ] **会员续费历史时间轴**：新增 `t_member_history` 增量表，前端时间轴 UI
- [ ] **运营后台**：会员套餐 CRUD、用户会员状态查询、邀请关系查询（目前依赖 SQL 手工操作）
- [ ] **数据库迁移自动化**：建议引入 Flyway，把 `docs/sql/pending/*.sql` 收编进 `db/migration/V*.sql`
- [ ] **微信支付 sandbox 联调**：会员支付 / 退款 / 关单 联调需要真实 sandbox 凭据

### P3（其他重构）

- [ ] `InviteService.findPage` 改批量查询（消除 N+1）
- [ ] `t_user_member` 加 `cancel_at` 字段（用户取消会员时记录，不删行）
- [ ] 前端 KeepMemberCard 组件抽出来（mine 页面"会员卡"入口）
- [ ] App OAuth 登录：spec §4.4 列出但无后端能力，本次占位

## 📝 决策记录

- **2026-06-30** 切片 B 不做：当前业务使用 `source_order_no` 字段已经够查"最近一次开通的订单"，历史时间轴属于产品升级而非必需；退款功能需要真实 sandbox 才能联调验证；产品侧暂未提出退款诉求
- **2026-06-30** 会员 H5 支付未做：uni-app 没有 H5 的 JSAPI 微信支付，H5 入口引导用户在小程序内开通
- **2026-06-30** 管理员判定前端用 openid 比对 `constants/admin.ts`：后端无 `role/isAdmin` 字段，硬编码 `Const.SYS_ADMIN` 同步；建议后续后端 `/user/get` 返回 `roles` 字段

## 🔗 相关文档

- `docs/migration/auth-mine-member-invite-audit.md` — 三切片审计
- `docs/migration/navigation-and-mine-migration.md` — 导航与我的页面迁移
- `docs/sql/pending/README.md` — SQL 执行说明