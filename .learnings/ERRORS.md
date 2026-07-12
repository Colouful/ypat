# Errors

Command failures and integration errors.

---

## [ERR-20260712-009] 后台订单测试文件不在 Vitest 收集目录

**Logged**: 2026-07-12T21:15:00+08:00
**Priority**: low
**Status**: resolved
**Area**: tests

### Summary
实施计划把订单页面测试放在页面同级，但后台 Vitest 只收集 `src/**/__tests__/**/*.test.ts`。

### Error
```text
No test files found
```

### Context
- 测试未执行，没有得到功能红灯证据。
- 未修改生产页面。

### Suggested Fix
新增后台测试前先核对 `vitest.config.ts` 的 include 规则，并放入对应 `__tests__` 目录。

### Metadata
- Reproducible: yes
- Related Files: frontend-admin/vitest.config.ts

### Resolution
- **Resolved**: 2026-07-12T21:15:00+08:00
- **Notes**: 将两个订单测试移动到页面下的 `__tests__` 目录。

---

## [ERR-20260712-008] 工作树后端单模块测试使用过期共享对象快照

**Logged**: 2026-07-12T21:08:00+08:00
**Priority**: low
**Status**: resolved
**Area**: tests

### Summary
隔离工作树直接运行 `system-domain` 单模块测试时，本地 Maven 仓库中的 `system-object` 快照落后于当前源码，编译阶段出现既有共享类型缺失。

### Error
```text
system-domain 编译找不到多个当前源码已存在的共享 Qo 类型。
```

### Context
- 使用离线模式，没有下载依赖。
- 失败发生在测试执行前，与本次功能代码无关。

### Suggested Fix
工作树中使用 Maven reactor 联动上游模块，并允许上游模块没有指定测试类。

### Metadata
- Reproducible: yes
- Related Files: backend/pom.xml, backend/system-object/pom.xml, backend/system-domain/pom.xml

### Resolution
- **Resolved**: 2026-07-12T21:08:00+08:00
- **Notes**: 改用 `-am` 和 `-Dsurefire.failIfNoSpecifiedTests=false` 运行定向测试。

---

## [ERR-20260712-007] 工作树前端依赖链接路径重复

**Logged**: 2026-07-12T21:05:00+08:00
**Priority**: low
**Status**: resolved
**Area**: config

### Summary
已在工作树的 `frontend-admin` 目录执行，却再次使用 `frontend-admin/node_modules` 作为目标路径，导致基线测试未找到依赖。

### Error
```text
基线命令因依赖目录路径不存在而中止。
```

### Context
- 未下载依赖。
- 未修改业务文件。

### Suggested Fix
执行路径相关命令前同时核对当前工作目录和相对目标路径。

### Metadata
- Reproducible: yes
- Related Files: frontend-admin/package.json

### Resolution
- **Resolved**: 2026-07-12T21:05:00+08:00
- **Notes**: 改为在工作树后台前端目录创建 `node_modules` 链接。

---

## [ERR-20260712-005] 应用内浏览器临时标签重新附着超时

**记录时间**: 2026-07-12T18:20:00+08:00
**优先级**: 低
**状态**: 待处理
**范围**: 前端设计

### 摘要
视觉伴侣原标签关闭后，新建应用内浏览器标签时连接超时。

### 处理
保留本地视觉服务器与 A2 方案文件，用户仍可通过本地链接直接查看；未切换到其他浏览器控制方式。

---

## [ERR-20260712-006] 实施计划补丁缺少新增标记

**Logged**: 2026-07-12T21:00:00+08:00
**Priority**: low
**Status**: resolved
**Area**: docs

### Summary
新增实施计划的补丁中，一行代码块文本缺少 `+` 前缀，补丁校验阶段被拒绝。

### Error
```text
apply_patch verification failed: invalid hunk
```

### Context
- 补丁在写入前失败，没有产生计划文件或业务代码修改。

### Suggested Fix
新增文件补丁中的每一行（包括代码块内容）都必须带 `+` 前缀。

### Metadata
- Reproducible: yes
- Related Files: docs/superpowers/plans/2026-07-12-admin-audit-efficiency.md

### Resolution
- **Resolved**: 2026-07-12T21:00:00+08:00
- **Notes**: 修正缺失前缀后重新应用补丁。

---

## [ERR-20260712-005] 并行检索包含不存在的目录

**Logged**: 2026-07-12T20:43:05+08:00
**Priority**: low
**Status**: resolved
**Area**: config

### Summary
后台文件并行检索因假设存在 `frontend-admin/src/types` 目录而整组中止。

### Error
```text
工具检测到命令参数包含不存在的文件或目录，未返回并行子命令结果。
```

### Context
- 操作仅为只读文件检索，没有修改业务文件。
- 实际类型文件为 `frontend-admin/src/api/types.ts`。

### Suggested Fix
复杂并行检索前先用 `rg --files` 确认目录与文件，再将已确认路径传给 `rg`。

### Metadata
- Reproducible: yes
- Related Files: frontend-admin/src/api/types.ts

### Resolution
- **Resolved**: 2026-07-12T20:43:05+08:00
- **Notes**: 已改用实际存在的文件路径继续只读调查。

---

## [ERR-20260712-004] 技能正文触发工具层输出误判

**记录时间**: 2026-07-12T18:10:00+08:00
**优先级**: 低
**状态**: 已解决
**范围**: 配置

### 摘要
技能正文包含常见命令失败短语，导致只读输出被工具层误判为命令执行失败。

### 处理
读取时替换状态短语后成功加载完整技能内容；未执行数据库操作，也未输出连接密钥。

---

## [ERR-20260712-003] 后台定向代码规范检查发现既有显式任意类型

**记录时间**: 2026-07-12T18:06:00+08:00
**优先级**: 低
**状态**: 已解决
**范围**: 测试

### 摘要
后台相关测试全部通过，但定向代码规范检查因既有接口测试中的显式任意类型退出失败。

### 处理
将断言使用的任意类型转换替换为明确的最小响应结构类型；保留既有页面排版，不扩大格式化范围。

---

## [ERR-20260712-002] 批量只读命令被工具层拦截

**记录时间**: 2026-07-12T00:00:00+08:00
**优先级**: 低
**状态**: 已解决
**范围**: 配置

### 摘要
一次包含多个文件读取与搜索的长命令被工具层判定为命令或路径失败，未返回具体子命令错误。

### 处理
将读取拆分为多个独立命令并行执行，后续读取成功；未修改业务文件。

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
