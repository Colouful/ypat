# YPAT 会员 / 邀请模块 — 数据库 SQL 待执行清单

> **关联 PR**：#9（登录/完善信息/我的页面）、#10（邀请端到端）、#11（会员端到端）
> **关联文件**：`V_pending_member.sql`、`V_member_system_redesign.sql`
> **数据库**：MySQL 8.0+
> **字符集**：`utf8mb4` / `utf8mb4_unicode_ci`

---

## 文件清单

| 文件 | 影响表 | 是否可重复执行 | 默认启用回填 |
| --- | --- | --- | --- |
| `V_pending_member.sql` | `t_member_plan` / `t_user_member` / `t_member_order` / `t_invite_relation` | ✅ 是（`IF NOT EXISTS` + `ON DUPLICATE KEY UPDATE`） | ❌ 默认全部注释 |
| `V_member_system_redesign.sql` | `t_member_plan` / `t_member_order` / `t_member_benefit_rule` / `t_member_operation_log` | ✅ 是（受保护 DDL + `ON DUPLICATE KEY UPDATE`） | 不涉及 |

---

## 价格单位约定（必读）

- 所有价格统一使用**「分」**（`price_fen` / `origin_price_fen`）
- 前端展示时**除以 100** 转成元
- 微信支付提交时**不得再次乘以 100**
- 与后端 `MemberPlan.priceFen` / `MemberOrder.priceFen` 完全一致

种子价格（**上线前需运营确认正式价格**）：

| code | name | duration_days | price_fen | 折合元 |
| --- | --- | --- | --- | --- |
| MONTH | 包月会员 | 30 | 1980 | ¥19.80 |
| SEASON | 包季会员 | 90 | 5880 | ¥58.80 |
| YEAR | 包年会员 | 365 | 19800 | ¥198.00 |

---

## 执行顺序

1. **测试库执行 `V_pending_member.sql` → 验证通过**
2. **测试库执行 `V_member_system_redesign.sql` → 验证通过**
3. **生产库执行前备份**
4. **生产库按同样顺序执行 SQL**
5. **生产库执行验证 SQL**
6. **业务联调（小程序 / H5 调通 GET /member/plans 等接口）**

> ⚠️ 步骤顺序不允许跳过。测试库失败或警告未确认前不要进生产。

---

## 步骤 1：测试库执行

### 前置条件

- 测试库 MySQL 8.0+ 已就绪
- 已确认现有 `t_user` / `t_order` 数据无 `id` 为空的脏行
- 当前 schema 中**不存在** `t_member_plan` / `t_user_member` / `t_member_order` / `t_invite_relation`

### 执行命令

```bash
# 把脚本拷到 MySQL 容器或本地
docker cp docs/sql/pending/V_pending_member.sql <mysql-container>:/tmp/
docker cp docs/sql/pending/V_member_system_redesign.sql <mysql-container>:/tmp/

# 进入容器或本地 mysql 客户端
docker exec -i <mysql-container> mysql -uroot -p<pwd> ypat_test -e "SOURCE /tmp/V_pending_member.sql;"
docker exec -i <mysql-container> mysql -uroot -p<pwd> ypat_test -e "SOURCE /tmp/V_member_system_redesign.sql;"
```

### 执行后验证

```sql
-- 表结构
SHOW TABLES LIKE 't_member_plan';
SHOW TABLES LIKE 't_user_member';
SHOW TABLES LIKE 't_member_order';
SHOW TABLES LIKE 't_invite_relation';
SHOW TABLES LIKE 't_member_benefit_rule';
SHOW TABLES LIKE 't_member_operation_log';

-- 索引与唯一约束
SHOW INDEX FROM `t_member_order`     WHERE Key_name IN ('uk_out_trade_no','idx_user_status');
SHOW INDEX FROM `t_invite_relation`  WHERE Key_name = 'uk_invitee_userid';
SHOW INDEX FROM `t_member_benefit_rule` WHERE Key_name = 'uk_level_scene_type';
SHOW INDEX FROM `t_member_operation_log` WHERE Key_name IN ('idx_user_created_at','idx_operator_created_at');

-- 种子套餐
SELECT code, name, duration_days, price_fen, status
FROM `t_member_plan` ORDER BY sort_no;

-- 会员重设计新增列与权益种子
SHOW COLUMNS FROM `t_member_plan` WHERE Field IN ('gift_ppd','level_code','recommended');
SHOW COLUMNS FROM `t_member_order` WHERE Field IN ('plan_name_snapshot','level_code_snapshot','origin_price_fen','gift_ppd');
SELECT level_code, scene, benefit_type, discount_ppd, effective, status
FROM `t_member_benefit_rule`
WHERE level_code = 'BASIC' AND scene = 'SUBMIT_YPAT';

-- 期望输出：
-- +-------+----------+---------------+-----------+--------+
-- | code  | name     | duration_days | price_fen | status |
-- +-------+----------+---------------+-----------+--------+
-- | MONTH | 包月会员 |            30 |      1980 | 1      |
-- | SEASON| 包季会员 |            90 |      5880 | 1      |
-- | YEAR  | 包年会员 |           365 |     19800 | 1      |
-- +-------+----------+---------------+-----------+--------+

-- 二次执行幂等检查：再次 SOURCE 应无任何报错
SOURCE /tmp/V_pending_member.sql;
SOURCE /tmp/V_member_system_redesign.sql;
```

---

## 步骤 2：生产库执行

### 备份（**必须**）

```bash
# 全量逻辑备份
mysqldump -uroot -p<pwd> ypat_prod | gzip > ypat_prod_$(date +%Y%m%d_%H%M%S).sql.gz

# 验证备份大小正常（不为 0 字节）
ls -lh ypat_prod_*.sql.gz
```

### 业务侧准备

- **暂停下单入口**（订单创建会写入 `t_member_order`，但 ddl-auto 会自动建表；如果生产已经按 ddl-auto 建过表，则本次脚本仅做核对与种子数据写入，下单可以不停）
- 通知业务方：会员 / 邀请能力将进入数据库就绪状态

### 执行命令

```bash
# 推荐：分两步，先结构后种子
docker cp docs/sql/pending/V_pending_member.sql <prod-mysql>:/tmp/
docker cp docs/sql/pending/V_member_system_redesign.sql <prod-mysql>:/tmp/
docker exec -i <prod-mysql> mysql -uroot -p<pwd> ypat_prod -e "SOURCE /tmp/V_pending_member.sql;"
docker exec -i <prod-mysql> mysql -uroot -p<pwd> ypat_prod -e "SOURCE /tmp/V_member_system_redesign.sql;"
```

---

## 步骤 3：执行后验证

```sql
-- 1) 表存在性
SHOW TABLES LIKE 't_member_plan';     -- 期望 1 行
SHOW TABLES LIKE 't_user_member';     -- 期望 1 行
SHOW TABLES LIKE 't_member_order';    -- 期望 1 行
SHOW TABLES LIKE 't_invite_relation'; -- 期望 1 行
SHOW TABLES LIKE 't_member_benefit_rule'; -- 期望 1 行
SHOW TABLES LIKE 't_member_operation_log'; -- 期望 1 行

-- 2) 唯一约束
SHOW INDEX FROM `t_member_order`    WHERE Key_name = 'uk_out_trade_no';
SHOW INDEX FROM `t_invite_relation` WHERE Key_name = 'uk_invitee_userid';

-- 3) 索引
SHOW INDEX FROM `t_member_order`    WHERE Key_name = 'idx_user_status';
SHOW INDEX FROM `t_invite_relation` WHERE Key_name = 'idx_inviter_userid';
SHOW INDEX FROM `t_member_benefit_rule` WHERE Key_name = 'uk_level_scene_type';
SHOW INDEX FROM `t_member_operation_log` WHERE Key_name IN ('idx_user_created_at','idx_operator_created_at');

-- 4) 种子数据行数
SELECT COUNT(*) AS plan_count FROM `t_member_plan`;
-- 期望：3
SELECT COUNT(*) AS submit_discount_rule_count
FROM `t_member_benefit_rule`
WHERE level_code = 'BASIC' AND scene = 'SUBMIT_YPAT' AND benefit_type = 'PPD_DISCOUNT';
-- 期望：1

-- 5) 应用侧 smoke test（需保证 restapi 服务已重启使 ddl-auto 跑过一次）
curl -sS 'https://<api-host>/service/member/plans' -H 'Authorization: Bearer <token>' | jq .
-- 期望返回包含 MONTH/SEASON/YEAR 三档
```

---

## 失败处理 / 回滚

### 失败处理流程

1. **执行中报错**：立刻停止后续 SQL，把错误日志发给开发
2. **结构已建但种子失败**：先 DROP 新建表再重试；新表无业务数据，无副作用
3. **业务数据写入后发现问题**：
   - `t_member_order` 在生产若有真实订单，**禁止直接 DROP**
   - 备份数据 → 修复脚本 → 重放 → 验证

### 回滚方式（人工执行，不写在脚本里）

```sql
-- 完全回滚（仅当 4 张表都没有业务数据时使用）
DROP TABLE IF EXISTS `t_member_order`;
DROP TABLE IF EXISTS `t_user_member`;
DROP TABLE IF EXISTS `t_member_plan`;
DROP TABLE IF EXISTS `t_invite_relation`;
DROP TABLE IF EXISTS `t_member_operation_log`;
DROP TABLE IF EXISTS `t_member_benefit_rule`;
```

### 回填模板

脚本第 8 段包含：

- 旧用户回填 `t_user_member`（基于 `realnameflag='1'`）
- 旧邀请关系回填（基于 `recmobile` 字段匹配）

**默认全部注释**，需要业务方书面授权后才启用，避免脚本自动给生产用户开通会员。

---

## 影响表汇总

| 表 | 行数（生产）影响 | 说明 |
| --- | --- | --- |
| `t_member_plan` | 仅 INSERT 3 条种子 | 可重复执行，`ON DUPLICATE KEY UPDATE` 只刷时间戳 |
| `t_user_member` | 仅创建空表 | 真实数据由业务产生 |
| `t_member_order` | 仅创建空表 | 真实订单由用户下单产生 |
| `t_invite_relation` | 仅创建空表 | 真实关系由注册流程产生 |

**对现有数据无侵入**：本脚本只 CREATE + INSERT 种子，**不 UPDATE / DELETE**任何已存在表的数据。

---

## 关联文档

- `docs/migration/auth-mine-member-invite-audit.md` — 三切片审计
- `docs/TODO.md` — 后续任务清单（P0/P1/P2/P3）
