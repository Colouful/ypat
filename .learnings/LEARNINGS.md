# Learnings

Corrections, insights, and knowledge gaps captured during development.

**Categories**: correction | insight | knowledge_gap | best_practice

---

## [LRN-20260712-001] best_practice

**记录时间**: 2026-07-12T18:12:00+08:00
**优先级**: 中
**状态**: 已处理
**范围**: 数据库迁移

### 摘要
幂等字段迁移除检查字段是否存在外，还需要校验空值、可空性和默认值定义。

### 处理
实名认证费用迁移会保留已有非空配置，仅补齐空值并将字段规范为非空、默认 1。

---
