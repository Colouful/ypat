# 作品约拍重复提交防护实施计划

> **For agentic workers（面向智能代理执行者）:** REQUIRED SUB-SKILL（必需子技能）: Use superpowers:subagent-driven-development（子代理驱动开发，推荐） or superpowers:executing-plans（计划执行） to implement this plan task-by-task（逐任务实施本计划）. Steps use checkbox（复选框） (`- [ ]`) syntax for tracking（跟踪）。

**Goal（目标）:** 让作品详情展示当前用户是否已约拍，并从详情入口和申请页提交时序两处阻止重复提交。

**Architecture（架构）:** 后端作品详情复用现有按作品统计约拍消息的仓储查询，返回 `isApplied`（是否已约拍）；前端详情类型和适配层标准化该字段，作品操作栏据此显示禁用状态；申请页在第一次异步请求前设置提交锁。后端快捷约拍接口原有重复校验保持不变，作为最终一致性保护。

**Tech Stack（技术栈）:** Java（后端语言）、Spring Boot（后端框架）、Spring Data JPA（数据访问框架）、Vue 3（前端框架）、uni-app（小程序框架）、TypeScript（类型脚本）、Vitest（前端单元测试）、JUnit（Java 单元测试框架）。

---

## 文件结构

- Modify（修改）: `backend/system-wap/src/test/java/com/ypat/controller/WorkQuickApplySourceTest.java`，增加作品详情约拍状态契约测试。
- Modify（修改）: `backend/system-domain/src/main/java/com/ypat/service/WorkService.java`，在作品详情响应中增加 `isApplied`（是否已约拍）。
- Modify（修改）: `frontend/src/api/types/work.ts`，扩展作品详情类型。
- Modify（修改）: `frontend/src/api/modules/work.ts`，标准化作品详情约拍状态。
- Modify（修改）: `frontend/src/components/business/__tests__/work-action-bar.test.ts`，覆盖未约拍和已约拍操作栏行为。
- Modify（修改）: `frontend/src/components/business/WorkActionBar.vue`，展示不可点击的“已约拍”状态。
- Modify（修改）: `frontend/src/pages-sub/work/detail.test.ts`，覆盖返回详情后的状态刷新。
- Modify（修改）: `frontend/src/pages-sub/work/detail.vue`，页面再次显示时刷新作品详情。
- Modify（修改）: `frontend/src/pages-sub/work/apply.test.ts`，增加提交锁时序回归测试。
- Modify（修改）: `frontend/src/pages-sub/work/apply.vue`，在余额刷新前设置提交锁。

### Task 1（任务 1）: 后端作品详情返回约拍状态

**Files（文件）:**
- Modify（修改）: `backend/system-wap/src/test/java/com/ypat/controller/WorkQuickApplySourceTest.java`
- Modify（修改）: `backend/system-domain/src/main/java/com/ypat/service/WorkService.java:286`

- [x] **Step 1（步骤 1）: 写失败测试**

在 `workDetailReturnsMiniappCompatibleStateFields`（作品详情返回小程序兼容状态字段测试）末尾增加：

```java
assertTrue(source.contains("messInfoRepository.countSendByWorkId(MessType.send.value, viewerId, workId)"));
assertTrue(source.contains("res.put(\"isApplied\", applied)"));
```

- [x] **Step 2（步骤 2）: 运行轻量断言确认失败**

Run（运行）:

```bash
node -e "const fs=require('fs');const s=fs.readFileSync('backend/system-domain/src/main/java/com/ypat/service/WorkService.java','utf8');if(!s.includes('res.put(\"isApplied\", applied)'))process.exit(1)"
```

Expected（预期）: 退出码为 1，因为响应尚未包含 `isApplied`（是否已约拍）。不运行 Maven（Java 构建工具），避免触发构建或依赖下载。

- [x] **Step 3（步骤 3）: 实现最小后端逻辑**

在 `WorkService.getDetail`（作品详情领域方法）的当前用户状态区域加入：

```java
boolean applied = false;
if (viewerId != null) {
    Long appliedCount = messInfoRepository.countSendByWorkId(MessType.send.value, viewerId, workId);
    applied = appliedCount != null && appliedCount > 0;
}
res.put("isApplied", applied);
```

保持 `likeflag`（点赞标记）、`colflag`（收藏标记）和 `isOwner`（是否作者）原有逻辑不变。

- [x] **Step 4（步骤 4）: 运行轻量断言确认通过**

Run（运行）:

```bash
node -e "const fs=require('fs');const s=fs.readFileSync('backend/system-domain/src/main/java/com/ypat/service/WorkService.java','utf8');if(!s.includes('messInfoRepository.countSendByWorkId(MessType.send.value, viewerId, workId)')||!s.includes('res.put(\"isApplied\", applied)'))process.exit(1)"
```

Expected（预期）: 无输出，退出码为 0。

- [x] **Step 5（步骤 5）: 检查差异格式**

Run（运行）:

```bash
git diff --check -- backend/system-domain/src/main/java/com/ypat/service/WorkService.java backend/system-wap/src/test/java/com/ypat/controller/WorkQuickApplySourceTest.java
```

Expected（预期）: 无输出，退出码为 0。

### Task 2（任务 2）: 作品操作栏展示已约拍状态

**Files（文件）:**
- Modify（修改）: `frontend/src/api/types/work.ts:60`
- Modify（修改）: `frontend/src/api/modules/work.ts:100`
- Modify（修改）: `frontend/src/components/business/__tests__/work-action-bar.test.ts`
- Modify（修改）: `frontend/src/components/business/WorkActionBar.vue:29`
- Modify（修改）: `frontend/src/pages-sub/work/detail.test.ts`
- Modify（修改）: `frontend/src/pages-sub/work/detail.vue:29`

- [x] **Step 1（步骤 1）: 写失败测试**

在 `createWork`（创建测试作品）默认值中增加：

```ts
isApplied: false,
```

在 `work-action-bar.test.ts`（作品操作栏测试）增加：

```ts
it('emits apply only when the work has not been applied to', async () => {
  const wrapper = mount(WorkActionBar, {
    props: { work: createWork() },
  })

  await wrapper.find('.work-action-bar__primary').trigger('tap')

  expect(wrapper.emitted('apply')).toHaveLength(1)
})

it('shows a disabled applied state without emitting apply', async () => {
  const wrapper = mount(WorkActionBar, {
    props: { work: createWork({ isApplied: true }) },
  })

  const primary = wrapper.find('.work-action-bar__primary')
  expect(primary.text()).toBe('已约拍')
  expect(primary.classes()).toContain('work-action-bar__primary--disabled')
  await primary.trigger('tap')
  expect(wrapper.emitted('apply')).toBeUndefined()
})
```

- [x] **Step 2（步骤 2）: 运行测试确认失败**

Run（运行）:

```bash
cd frontend && npx vitest run src/components/business/__tests__/work-action-bar.test.ts
```

Expected（预期）: 第二个新增测试失败，因为组件仍显示“立即约拍”并触发 `apply`（申请）事件。

- [x] **Step 3（步骤 3）: 扩展类型和适配**

在 `WorkDetail`（作品详情类型）中加入：

```ts
isApplied: boolean
```

在 `normalizeWorkDetail`（作品详情标准化函数）的返回对象中加入：

```ts
isApplied: isOn(raw.isApplied) || isOn(raw.applyflag) || isOn(raw.applied),
```

- [x] **Step 4（步骤 4）: 实现操作栏状态**

将主按钮区域调整为：

```vue
<view v-if="!work.isOwner && !work.isApplied" class="work-action-bar__primary" @tap="onApply">
  <text>立即约拍</text>
</view>
<view v-else-if="!work.isOwner" class="work-action-bar__primary work-action-bar__primary--disabled">
  <text>已约拍</text>
</view>
<view v-else-if="mode === 'my'" class="work-action-bar__primary work-action-bar__primary--offline" @tap="onOffline">
  <text>下架作品</text>
</view>
```

在样式中加入：

```scss
&--disabled {
  background: $color-text-helper;
  box-shadow: none;
}
```

- [x] **Step 5（步骤 5）: 运行相关测试确认通过**

Run（运行）:

```bash
cd frontend && npx vitest run src/components/business/__tests__/work-action-bar.test.ts
```

Expected（预期）: 5 个测试全部通过。

- [x] **Step 6（步骤 6）: 写返回详情刷新失败测试**

在 `detail.test.ts`（作品详情测试）中断言页面导入并注册 `onShow`（显示钩子），且仅在作品编号存在时调用 `load`（加载函数）。

- [x] **Step 7（步骤 7）: 运行详情测试确认失败**

Run（运行）:

```bash
cd frontend && npx vitest run src/pages-sub/work/detail.test.ts
```

Expected（预期）: 新增测试失败，因为页面当前只在 `onMounted`（挂载钩子）加载详情。

- [x] **Step 8（步骤 8）: 实现返回详情刷新**

在作品详情页导入 `onShow`（显示钩子），并增加：

```ts
onShow(() => {
  if (id.value) void load()
})
```

- [x] **Step 9（步骤 9）: 运行详情测试确认通过**

Run（运行）:

```bash
cd frontend && npx vitest run src/pages-sub/work/detail.test.ts
```

Expected（预期）: 3 个测试全部通过。

### Task 3（任务 3）: 申请页从点击开始锁定提交

**Files（文件）:**
- Modify（修改）: `frontend/src/pages-sub/work/apply.test.ts`
- Modify（修改）: `frontend/src/pages-sub/work/apply.vue:199`

- [x] **Step 1（步骤 1）: 写失败测试**

在 `apply.test.ts`（申请页测试）增加：

```ts
it('locks submission before refreshing the balance', () => {
  const submitStart = source.indexOf('async function submitApply()')
  const submitEnd = source.indexOf('function buildYpatApplyContent()', submitStart)
  const submitSource = source.slice(submitStart, submitEnd)

  expect(submitSource.indexOf('submitting.value = true')).toBeLessThan(
    submitSource.indexOf('await refreshPpdBalance()'),
  )
})
```

- [x] **Step 2（步骤 2）: 运行测试确认失败**

Run（运行）:

```bash
cd frontend && npx vitest run src/pages-sub/work/apply.test.ts
```

Expected（预期）: 新增测试失败，因为 `submitting.value = true` 当前位于余额刷新之后。

- [x] **Step 3（步骤 3）: 调整提交时序**

将 `submitApply`（提交申请函数）开头和余额判断调整为：

```ts
async function submitApply(): Promise<void> {
  if (submitDisabled.value || (!workId.value && !ypatId.value)) return
  submitting.value = true
  try {
    const latestPpd = await refreshPpdBalance()
    if (latestPpd < applyCost) {
      showRechargeGuide(latestPpd)
      return
    }
    await requestMessageSubscribe('apply')
```

保留函数末尾现有 `catch`（异常捕获）和 `finally`（最终处理），由 `finally`（最终处理）统一恢复 `submitting.value = false`。

- [x] **Step 4（步骤 4）: 运行申请页测试确认通过**

Run（运行）:

```bash
cd frontend && npx vitest run src/pages-sub/work/apply.test.ts
```

Expected（预期）: 4 个测试全部通过。

### Task 4（任务 4）: 针对性验证

**Files（文件）:**
- Verify（验证）: 上述所有修改文件

- [x] **Step 1（步骤 1）: 运行前端相关测试**

Run（运行）:

```bash
cd frontend && npx vitest run src/components/business/__tests__/work-action-bar.test.ts src/pages-sub/work/apply.test.ts src/pages-sub/work/detail.test.ts
```

Expected（预期）: 3 个测试文件、12 个测试全部通过。

- [x] **Step 2（步骤 2）: 运行后端源码契约断言**

Run（运行）:

```bash
node -e "const fs=require('fs');const service=fs.readFileSync('backend/system-domain/src/main/java/com/ypat/service/WorkService.java','utf8');const test=fs.readFileSync('backend/system-wap/src/test/java/com/ypat/controller/WorkQuickApplySourceTest.java','utf8');if(!service.includes('res.put(\"isApplied\", applied)')||!test.includes('res.put(\\\"isApplied\\\", applied)'))process.exit(1)"
```

Expected（预期）: 无输出，退出码为 0。

- [x] **Step 3（步骤 3）: 运行差异格式检查**

Run（运行）:

```bash
git diff --check -- backend/system-domain/src/main/java/com/ypat/service/WorkService.java backend/system-wap/src/test/java/com/ypat/controller/WorkQuickApplySourceTest.java frontend/src/api/types/work.ts frontend/src/api/modules/work.ts frontend/src/components/business/__tests__/work-action-bar.test.ts frontend/src/components/business/WorkActionBar.vue frontend/src/pages-sub/work/detail.test.ts frontend/src/pages-sub/work/detail.vue frontend/src/pages-sub/work/apply.test.ts frontend/src/pages-sub/work/apply.vue
```

Expected（预期）: 无输出，退出码为 0。

- [x] **Step 4（步骤 4）: 审查最终差异**

Run（运行）:

```bash
git diff -- backend/system-domain/src/main/java/com/ypat/service/WorkService.java backend/system-wap/src/test/java/com/ypat/controller/WorkQuickApplySourceTest.java frontend/src/api/types/work.ts frontend/src/api/modules/work.ts frontend/src/components/business/__tests__/work-action-bar.test.ts frontend/src/components/business/WorkActionBar.vue frontend/src/pages-sub/work/detail.test.ts frontend/src/pages-sub/work/detail.vue frontend/src/pages-sub/work/apply.test.ts frontend/src/pages-sub/work/apply.vue
```

Expected（预期）: 只包含 `isApplied`（是否已约拍）数据链路、操作栏禁用状态、提交锁时序及其测试，不包含需求外改动。

不执行依赖下载和完整构建，由用户完成小程序手动验收。
