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

## 历史数据回填决策

> 详见 `docs/sql/pending/V_pending_member.sql` 第 8 节（脚本模板）、第 9 节（只读统计）、第 10 节（决策记录模板）。
> 当前状态：**DBA 不应执行任何历史回填脚本**。本次三切片合并入 main 仅交付"建表 + 索引 + 套餐种子"。

### 老用户会员回填（脚本 A）

- [ ] 是否执行脚本 A：老实名认证用户补会员
- 当前决定：**暂不执行，默认永久关闭**
- 原因：目前未确认平台历史上存在"实名认证赠会员"的运营承诺
- 启用条件（必须全部满足）：
  - 运营书面确认补会员活动方案
  - 明确目标用户范围（白名单 / 活动 ID / 注册时间段）
  - 明确会员等级与有效期
  - staging 执行并抽样核对通过
  - 执行前生成完整用户清单并审批
- 禁止直接对全部 `realnameflag = '1'` 用户执行
- 风险提示：执行后大量老用户突然获得会员 → 会员人数 / 到期提醒 / 运营统计失真 → 用户认为实名认证都应赠送 → 后续取消赠送易引发投诉

### 历史邀请关系回填（脚本 B）

- [ ] 是否执行脚本 B：旧 `recmobile` 邀请关系迁移
- 当前决定：**暂缓，上线前根据历史数据统计决定**
- 必须先在 staging 执行只读统计（V_pending_member.sql 第 9 节 5 条 SQL），取得：
  - 历史实名认证用户数量
  - 存在旧邀请手机号的用户数量
  - 能匹配到真实邀请人的关系数量
  - 自我邀请数量（必须为 0 否则数据脏）
  - 无法匹配邀请人的历史数据数量
- 同时确认：旧邀请奖励（拍拍豆 +3）是否已发放 + 是否存在对应流水（`t_record type=FRI`）
- 决策矩阵：
  - 旧奖励已发放 → 可执行（reward_ppd=3 仅是标记）
  - 旧奖励未发放 → 三选一：
    - (a) 不迁移历史邀请（推荐默认）
    - (b) 仅迁移关系，reward_ppd=0
    - (c) 迁移关系并补发奖励（需审批 + 财务确认）
- 禁止直接执行默认模板，需结合统计结果调整
- 所有历史脚本必须先在 staging 执行并抽样核对，禁止直接在生产运行

### 推荐实施顺序

```text
当前阶段（DBA 可立即执行）：
  ✓ 1-7 节：建表 / 索引 / 唯一约束 / 种子套餐（3 档占位价）
  ✗ 8.1 脚本 A：不执行
  ✗ 8.2 脚本 B：不执行

上线前（业务方 + DBA 评审）：
  1. 在 staging 跑第 9 节 5 条只读统计
  2. 根据统计结果决定是否启用脚本 A / 脚本 B
  3. 若启用，按第 10 节决策记录模板走审批
  4. 在 staging 抽样核对后再上生产
```

### DBA 当前最小动作

| 步骤 | 文件 / 命令 | 影响 |
| --- | --- | --- |
| ✅ 执行建表 | `V_pending_member.sql` 1-4 节 | 4 张空表 |
| ✅ 执行索引 | 同上 1-4 节 | 主键 + 唯一约束 + 普通索引 |
| ✅ 执行种子套餐 | 第 5 节 | 3 档占位套餐 |
| ❌ 执行脚本 A | 第 8.1 节 | 默认注释，未授权不执行 |
| ❌ 执行脚本 B | 第 8.2 节 | 默认注释，未授权不执行 |

---

## 🔗 相关文档

- `docs/migration/auth-mine-member-invite-audit.md` — 三切片审计
- `docs/migration/navigation-and-mine-migration.md` — 导航与我的页面迁移
- `docs/sql/pending/README.md` — SQL 执行说明
- `docs/sql/pending/V_pending_member.sql` — 待执行 SQL