# Errors

Command failures and integration errors.

---

## [ERR-20260712-001] repository-path-assumption

**Logged**: 2026-07-12T00:00:00+08:00
**Priority**: low
**Status**: resolved
**Area**: config

### Summary
Repository searches failed because commands included component and backend module paths that do not exist.

### Error
```text
rg received nonexistent WorkDetailView.vue and system-infrastructure paths.
```

### Context
- The failed operations were read-only repository searches.
- No project source files were modified by the failed commands.

### Suggested Fix
Use `rg --files` or list module directories before passing assumed paths to `rg`.

### Metadata
- Reproducible: yes
- Related Files: frontend/src/components/business/YpatDetailView.vue

### Resolution
- **Resolved**: 2026-07-12T00:00:00+08:00
- **Notes**: Subsequent searches were limited to paths confirmed to exist.

---
