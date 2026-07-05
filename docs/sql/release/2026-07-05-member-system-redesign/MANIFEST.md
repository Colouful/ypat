# 会员系统重设计 SQL 发布包

## 发布标识

- 发布包：`member-system-redesign`
- 日期：`2026-07-05`
- 适用数据库：MySQL 8.0+
- 执行环境：先测试库，验证通过后再生产库
- 原始来源：`docs/sql/pending`
- 上线检查：`docs/release/member-system-redesign-checklist.md`

## 执行顺序

| 顺序 | 文件 | 来源 | 用途 | sha256 |
| --- | --- | --- | --- | --- |
| 1 | `001_V_pending_member.sql` | `docs/sql/pending/V_pending_member.sql` | 创建会员/邀请基础表和会员套餐种子 | `f8189729b1d4de4bc8bb0da57266bc6caac11b4228d76c6b6a9c02c3433f88dc` |
| 2 | `002_V_member_system_redesign.sql` | `docs/sql/pending/V_member_system_redesign.sql` | 收敛会员套餐/订单字段，创建权益规则和操作日志表 | `0c5fc8abf98dde6ed8c00c287a96489a46ef8da44a454e4360ae4281ba497bf0` |

## 执行命令模板

```bash
mysql -u<user> -p<pwd> <database> < docs/sql/release/2026-07-05-member-system-redesign/001_V_pending_member.sql
mysql -u<user> -p<pwd> <database> < docs/sql/release/2026-07-05-member-system-redesign/002_V_member_system_redesign.sql
```

## 校验命令

```bash
shasum -a 256 docs/sql/release/2026-07-05-member-system-redesign/*.sql
```

期望输出：

```text
f8189729b1d4de4bc8bb0da57266bc6caac11b4228d76c6b6a9c02c3433f88dc  docs/sql/release/2026-07-05-member-system-redesign/001_V_pending_member.sql
0c5fc8abf98dde6ed8c00c287a96489a46ef8da44a454e4360ae4281ba497bf0  docs/sql/release/2026-07-05-member-system-redesign/002_V_member_system_redesign.sql
```

## 注意事项

- 必须按文件名前缀顺序执行。
- 生产执行前必须完成数据库备份。
- 两个脚本均设计为可重复执行，但生产仍需保留执行日志和校验输出。
- 若测试库执行任一脚本报错，停止后续执行并保留完整错误日志。
