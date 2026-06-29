# Branch Protection 配置 — MANUAL_REQUIRED

## 现状

| 项 | 值 |
| --- | --- |
| `gh api repos/Colouful/ypat/rulesets` | `[]` (无 ruleset) |
| `gh api repos/Colouful/ypat/branches/main/protection` | HTTP 404 (无保护) |
| Collaborators | 1 (Colouful, admin) |
| CODEOWNERS | 存在于 `.github/CODEOWNERS`,全路径归 `@lizhenwei` |

## 状态

```
BRANCH_PROTECTION = MANUAL_REQUIRED
```

## 原因

仓库为 **solo-dev**(仅 1 个 collaborator)。"至少 1 Approval + Dismiss stale + Code Owner Review" 在 solo-dev 情境下会形成事实锁死:同一人提的 PR 无法审批自身,任何合并都必须 admin bypass,这与 "要求 Code Owner Review" 的初衷冲突。

安全边界明确:**只有具备 Owner/Admin 权限并确认不会锁死仓库时才创建。** 当前条件未满足。

## 推荐流程

二选一:

### 选项 A — 维持 solo-dev,放宽审批要求

```
required_approving_review_count = 0
require_code_owner_review = false
其他规则保留
```

仍能拦截:
- force push
- 直接删除 main
- 直接 push main(必须 PR)
- 未通过 CI 的合并

不能拦截:
- 单人 self-merge(无第二人复核)

### 选项 B — 增加 collaborator 后启用严格规则 (用户原方案)

先邀请第二个 collaborator(或 GitHub bot 账号),再启用:

```
required_approving_review_count = 1
require_code_owner_review = true
dismiss_stale_reviews_on_push = true
require_last_push_approval = true
required_status_checks (10 个):
  - security / secret-scan
  - frontend / development
  - frontend / staging
  - frontend / production
  - backend / unit-tests
  - backend / build-dev
  - backend / build-pre
  - backend / build-pro
  - compose / config
  - shell / syntax-check
strict (require branch up-to-date) = true
allow_force_pushes = false
allow_deletions = false
required_conversation_resolution = true
```

## UI 配置步骤 (选项 B)

1. `https://github.com/Colouful/ypat/settings/rules/new?target=branch`
2. **Ruleset Name**: `main-protection`
3. **Enforcement status**: Active
4. **Bypass list**: (留空,或仅在选项 B 下加入 Repository admin 用于紧急 hotfix)
5. **Target branches**:
   - Add target → Include by pattern → `main`
6. **Branch rules** 全部勾选:
   - [x] Restrict creations
   - [x] Restrict updates
   - [x] Restrict deletions
   - [x] Require linear history (可选)
   - [x] Require a pull request before merging
     - Required approvals: `1`
     - [x] Dismiss stale pull request approvals when new commits are pushed
     - [x] Require review from Code Owners
     - [x] Require approval of the most recent reviewable push
     - [x] Require conversation resolution before merging
   - [x] Require status checks to pass
     - [x] Require branches to be up to date before merging
     - Add checks (匹配名称必须与 CI Job 名完全相同):
       - `security / secret-scan`
       - `frontend / development`
       - `frontend / staging`
       - `frontend / production`
       - `backend / unit-tests`
       - `backend / build-dev`
       - `backend / build-pre`
       - `backend / build-pro`
       - `compose / config`
       - `shell / syntax-check`
   - [x] Block force pushes
7. Save

## API 配置步骤 (选项 A,推荐当前阶段)

```bash
env -u GITHUB_TOKEN gh api -X POST repos/Colouful/ypat/rulesets \
  -f name='main-protection' \
  -f target='branch' \
  -f enforcement='active' \
  -F 'conditions[ref_name][include][]=refs/heads/main' \
  -F 'rules[][type]=deletion' \
  -F 'rules[][type]=non_fast_forward' \
  -F 'rules[][type]=required_status_checks' \
  -F 'rules[3][parameters][strict_required_status_checks_policy]=true' \
  -F 'rules[3][parameters][required_status_checks][][context]=security / secret-scan' \
  -F 'rules[3][parameters][required_status_checks][][context]=frontend / development' \
  -F 'rules[3][parameters][required_status_checks][][context]=frontend / staging' \
  -F 'rules[3][parameters][required_status_checks][][context]=frontend / production' \
  -F 'rules[3][parameters][required_status_checks][][context]=backend / unit-tests' \
  -F 'rules[3][parameters][required_status_checks][][context]=backend / build-dev' \
  -F 'rules[3][parameters][required_status_checks][][context]=backend / build-pre' \
  -F 'rules[3][parameters][required_status_checks][][context]=backend / build-pro' \
  -F 'rules[3][parameters][required_status_checks][][context]=compose / config' \
  -F 'rules[3][parameters][required_status_checks][][context]=shell / syntax-check' \
  -F 'rules[][type]=pull_request' \
  -F 'rules[4][parameters][required_approving_review_count]=0' \
  -F 'rules[4][parameters][required_review_thread_resolution]=true'
```

(实际 array index 由 gh CLI 自动管理,如需手动构造可改用 JSON body via `--input -`。)

## 验证

启用后:
```bash
env -u GITHUB_TOKEN gh api repos/Colouful/ypat/rulesets
```

应返回 1 个 ruleset,enforcement=active。
