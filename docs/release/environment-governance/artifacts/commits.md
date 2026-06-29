# Commits - 任务所有原子提交

```
581a669 docs(governance): add environment isolation, secret mgmt, db migration, incident, backup, prod guides
376b7a8 ci: validate three-environment profiles with fail-closed production gates
c7784a5 fix(deploy): immutable staging releases and require explicit confirmation for production
07c3288 fix(compose): split staging/production compose namespaces and add fail-closed prod template
88f914a fix(compose): repair development compose — bind 127.0.0.1, redis auth, dedupe volumes
c2397d6 feat(config): add Spring Boot 1.5 EnvironmentPostProcessor for fail-closed env validation
e147234 fix(config): add backend env templates and harden web pre/pro fdfs_path
725afce docs(governance): add environment variables catalog, Java upgrade ADR, branch protection, CODEOWNERS
aa2511f fix(config): split FastDFS and backend env by environment profile
d5eb121 fix(env): remove production staging alias and add fail-closed frontend env validator
b4af32a fix(build): exclude pre/* from non-pre Maven profiles to prevent resource leakage
```

基线: f095578 feat: 修改协议 (origin/main HEAD before task)
HEAD: (待 PR 合并后填)