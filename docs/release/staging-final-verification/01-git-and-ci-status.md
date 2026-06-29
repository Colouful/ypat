# Git & CI 状态

## 远端

- `origin/main` HEAD: `f7df2e2ba8477904fc045a4e07a625ece7e7ae38`
- 最近 10 提交:

  ```
  f7df2e2 Merge pull request #7 from Colouful/hotfix/ci-staging-verification
  a5f83fc fix(ci): require URL scheme in production artifact staging-alias scan
  4b19197 fix(test): re-import env module per case so envConfig sees mutated env
  211b1ba fix(ci): turn off no-extra-semi for defensive leading-semicolon idiom
  b9e48… fix(ci): unblock compose/frontend-env/security/shellcheck jobs
  c71580a fix(ci): inline literal CI URLs at job-level env in frontend-production
  fc61aa1 docs(evidence): add governance audit trail
  581a669 docs(governance): add environment isolation, secret mgmt, db migration, incident, backup, prod guides
  376b7a8 ci: validate three-environment profiles with fail-closed production gates
  c7784a5 fix(deploy): immutable staging releases and require explicit confirmation for production
  ```

## PR #7

| 字段 | 值 |
| --- | --- |
| state | MERGED |
| mergedAt | 2026-06-29T09:34:50Z |
| headRefOid | a5f83fc1afb234acd3b2a4a719952d35b822cc0d |
| mergeCommit | f7df2e2ba8477904fc045a4e07a625ece7e7ae38 |
| URL | https://github.com/Colouful/ypat/pull/7 |

## CI Run 28362618060

- status: completed
- conclusion: **success**
- headSha: f7df2e2 (与 origin/main 一致)
- event: push
- 10 个 Job 全绿:

  | Job | conclusion |
  | --- | --- |
  | security / secret-scan | success |
  | frontend / development | success |
  | frontend / staging | success |
  | frontend / production | success |
  | backend / unit-tests | success |
  | backend / build-dev | success |
  | backend / build-pre | success |
  | backend / build-pro | success |
  | compose / config | success |
  | shell / syntax-check | success |

## 结论

```
CI_VERIFIED
VERIFIED_MAIN_SHA = f7df2e2ba8477904fc045a4e07a625ece7e7ae38
```
