# GitHub 主分支保护规则

**本文件记录 YPAT 仓库主分支（`main`）的强制保护规则。**
**GitHub 上的实际配置必须由仓库管理员手动完成。**
AI 不得自动修改 GitHub 仓库管理设置，避免错误锁定仓库。

最后更新：2026-06-29

---

## 1. 必须配置的保护规则

### 1.1 分支保护（Branch Protection）

| 规则 | 必需 | 说明 |
|------|------|------|
| 禁止直接 push `main` | ✅ | 必须通过 PR |
| 禁止 force push | ✅ | 保护历史不被重写 |
| 禁止删除分支 | ✅ | 防止误操作 |
| 必须 PR 才能合并 | ✅ | — |
| 至少 1 个审核 | ✅ | 强制 Code Review |
| 必须最新 | ✅ | merge 前 rebase / 同步 |
| CI Required Checks | ✅ | 详见第 2 节 |
| 禁止 Squash | ❌ | 允许（保留 merge commit） |
| 必须 Conventional Commits | ❌ | 软约束，靠 PR review 兜底 |

### 1.2 GitHub Rulesets（推荐，比 Branch Protection 更强）

```yaml
target: branch
ref_name: refs/heads/main
enforcement: active
rules:
  - type: non_fast_forward
    enforce: true
  - type: deletion
    enforce: true
  - type: pull_request
    required_approving_review_count: 1
    require_code_owner_review: true
    dismiss_stale_reviews_on_push: true
    required_review_thread_resolution: true
  - type: required_status_checks
    required_checks:
      - frontend / type-check
      - frontend / lint
      - frontend / test
      - backend / build-dev
      - backend / build-pre
      - backend / build-pro
      - compose / config-dev
      - compose / config-staging
      - compose / config-production
      - compose / config-fastdfs-staging
      - security / secret-scan
  - type: restrict_pushes
    push_allowances: []
```

> AI 不能自动 apply ruleset，必须由仓库 owner 在 https://github.com/Colouful/ypat/settings/rules 设置。

## 2. CI Required Checks

详见 [`../../.github/workflows/ci.yml`](../../.github/workflows/ci.yml)（本轮治理任务创建）。

### 2.1 必须通过的 Job

| Job | 触发条件 | 阻断合并 |
|-----|----------|----------|
| `frontend / type-check` | 所有 PR | ✅ |
| `frontend / lint` | 所有 PR | ✅ |
| `frontend / test` | 所有 PR | ✅ |
| `backend / build-dev` | 所有 PR | ✅ |
| `backend / build-pre` | 所有 PR | ✅ |
| `backend / build-pro` | 所有 PR | ✅ |
| `compose / config-dev` | 所有 PR | ✅ |
| `compose / config-staging` | 所有 PR | ✅ |
| `compose / config-production` | 所有 PR | ✅ |
| `compose / config-fastdfs-staging` | 所有 PR | ✅ |
| `security / secret-scan` | 所有 PR | ✅ |
| `shell / syntax-check` | 所有 PR | ✅ |

## 3. CODEOWNERS

本仓库创建了 [`../../.github/CODEOWNERS`](../../.github/CODEOWNERS)：

```
# 默认 owner
*                          @lizhenwei

# 数据库 schema 改动需后端 owner 强制 review
backend/**/sql/**           @lizhenwei
backend/**/migration/**    @lizhenwei

# 部署相关改动
docker-compose*.yml        @lizhenwei
scripts/deploy/            @lizhenwei
.github/workflows/         @lizhenwei

# 文档
docs/                      @lizhenwei
DEPLOY_ENVS.md             @lizhenwei
```

> 实际部署到 GitHub 后，GitHub 会自动要求 CODEOWNER review。

## 4. 紧急修复流程

紧急安全修复可绕过 PR 流程：

1. 在 hotfix 分支（如 `hotfix/CVE-2026-xxx`）提交修复
2. 至少 1 个 owner 现场审核
3. 合并后立即通知所有 owner
4. 24 小时内补 PR description / ChangeLog

## 5. AI 行为约束

| 行为 | 允许 | 说明 |
|------|------|------|
| AI 创建 PR | ✅ | 通过 gh CLI |
| AI 合并 PR | ⚠️ | 仅在 owner 显式批准时 |
| AI 推送 `main` | ❌ | 必须通过 PR |
| AI force-push | ❌ | 严禁 |
| AI 修改 GitHub Rulesets | ❌ | 必须 owner 手动 |
| AI 修改 Branch Protection | ❌ | 必须 owner 手动 |

## 6. 自检清单

仓库管理员每月核查：

- [ ] main 分支无法直接 push
- [ ] PR 至少 1 个 approval
- [ ] CI Required Checks 全部启用
- [ ] CODEOWNERS 文件最新
- [ ] Rulesets 与本文件一致

---

**人工配置入口**：
- Branch Protection: https://github.com/Colouful/ypat/settings/branches
- Rulesets: https://github.com/Colouful/ypat/settings/rules
- CODEOWNERS: https://github.com/Colouful/ypat/blob/main/.github/CODEOWNERS