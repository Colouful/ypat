# 基线与执行计划

## Git 基线

- 原工作区：`/Users/lizhenwei/workspace/vueworkspace/ypat-workspace`
- 原工作区分支：`dev2`
- 原工作区未提交文件：`.omx/state/session.json`
- 处理方式：未删除、未覆盖、未 stash，改用独立 worktree。
- 独立 worktree：`/Users/lizhenwei/workspace/vueworkspace/ypat-workspace-worktrees/release-gap-closure`
- 任务分支：`codex/release-gap-closure`
- 基线 commit：`06e454465448af0e052d47a0420a8142fa1eaf65`

## 最新 main 迁移证据确认

- `docs/migration/frontend-parity/` 存在，包含 00-08 主证据文档。
- `08-release-readiness.md` 存在 release readiness(发布就绪) 结论。
- 最新 main 已包含前端 parity(迁移对齐) 证据，但本轮发现部分状态已过期，已同步更新。

## 执行顺序与结果

| 顺序 | 项目 | 结果 |
| --: | -- | -- |
| 1 | 基线和文档 | 已创建 `docs/release/` 证据目录和生产上线待办 |
| 2 | GAP-F-03 | 已修复并测试 |
| 3 | GAP-I-01 | 已补后端、前端 API 和测试 |
| 4 | GAP-J-01 | 已用统一功能开关关闭保证金服务 |
| 5 | GAP-G-01 | 已核验并创建 ADR |
| 6 | 后端安全 | BE-SEC-01、BE-SEC-02 已修复并测试 |
| 7 | 虚假交互 | 已清理假关注、假私信、假分享 |
| 8 | Gap 状态清理 | 已更新迁移证据文档 |
| 9 | 全量测试 | 前端/后端验证通过 |
| 10 | 修改记录 | 已记录 CHANGE-001 到 CHANGE-007 |
| 11 | 最终上线评估 | `CONDITIONALLY_READY` |
