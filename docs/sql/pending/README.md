# YPAT 数据库 SQL 待执行清单

本目录存放尚未在生产 MySQL 上执行的 SQL 脚本。当前仓库无 Flyway / Liquibase，新表由 Hibernate `ddl-auto: update` 自动创建；这些脚本由 DB 管理员手动执行，用于：

- 校验生产库表结构与代码一致
- 补齐 ddl-auto 不会建的索引 / 唯一约束
- 写入种子数据（套餐）
- 历史用户回填

## 文件清单

| 文件 | 关联 PR | 说明 | 状态 |
| --- | --- | --- | --- |
| `V_pending_member.sql` | #10 / #11 | 邀请关系 + 会员 4 张表 + 种子套餐 + 历史回填模板 | 待执行 |

## 执行方式

```bash
# 在生产 MySQL 上：
mysql -u<user> -p<pwd> ypat_prod < docs/sql/pending/V_pending_member.sql

# 验证：
mysql -u<user> -p<pwd> ypat_prod -e "SHOW TABLES LIKE 't_invite%'; SHOW TABLES LIKE 't_member%'; SHOW TABLES LIKE 't_user_member';"
```

## 执行前检查

1. 确认 `t_invite_relation` / `t_member_plan` / `t_user_member` / `t_member_order` 不存在（`IF NOT EXISTS` 兜底）
2. 确认现有 `t_user` 数据无 `id` 为空的脏数据
3. 种子套餐的 `code` / `name` / `price_fen` 须与产品确认后再启用（脚本默认启用 3 档占位）

## 执行后验证

- [ ] 4 张表都存在
- [ ] `t_invite_relation.uk_invitee_userid` 唯一约束已建
- [ ] `t_member_order.uk_out_trade_no` 唯一约束已建
- [ ] `t_member_plan` 至少有 3 条种子数据
- [ ] 前端 `GET /member/plans` 能正常返回