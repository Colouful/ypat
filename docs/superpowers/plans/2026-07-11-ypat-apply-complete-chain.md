# 普通约拍申请完整链路修复实施计划

> **For agentic workers（面向智能代理执行者）:** REQUIRED SUB-SKILL（必需子技能）: Use superpowers:subagent-driven-development（子代理驱动开发，推荐） or superpowers:executing-plans（计划执行） to implement this plan task-by-task（逐任务实施本计划）. Steps use checkbox（复选框） (`- [ ]`) syntax for tracking（跟踪）。

**Goal（目标）:** 修复普通约拍发布者编号缺失导致无法提交的问题，并在详情、申请页和后端事务三层阻止重复申请。

**Architecture（架构）:** `/ypat/get`（约拍详情接口）显式返回发布者编号和 `msgflag`（已申请标记）；客户端提交只携带约拍编号和内容，服务端从 Token（登录令牌）和约拍记录确定双方身份。普通约拍与作品快捷约拍均在重复检查和扣豆前锁定申请用户，前端详情及申请页消费最新申请状态。

**Tech Stack（技术栈）:** Java（后端语言）、Spring Boot（后端框架）、Spring Data JPA（数据访问框架）、Vue 3（前端框架）、uni-app（小程序框架）、TypeScript（类型脚本）、Vitest（前端单元测试）、JUnit（Java 单元测试框架）。

---

## 文件结构

- Modify（修改）: `backend/system-object/src/main/java/com/ypat/YpatInfoQo.java`，补齐 `msgflag`（已申请标记）访问方法。
- Modify（修改）: `backend/system-domain/src/main/java/com/ypat/service/YpatInfoService.java`，返回发布者编号和当前用户申请状态。
- Modify（修改）: `backend/system-domain/src/main/java/com/ypat/service/MessInfoService.java`，增加普通约拍数据校验和悲观锁。
- Modify（修改）: `backend/system-domain/src/main/java/com/ypat/service/WorkService.java`，为作品快捷约拍增加悲观锁。
- Create（创建）: `backend/system-wap/src/test/java/com/ypat/controller/YpatApplyFlowSourceTest.java`，记录后端约拍链路源码契约。
- Modify（修改）: `frontend/src/api/types/index.ts`，调整约拍详情和申请参数类型。
- Modify（修改）: `frontend/src/components/business/YpatDetailView.vue`，展示已约拍状态并公开刷新方法。
- Modify（修改）: `frontend/src/components/business/__tests__/ypat-detail-view.test.ts`，覆盖已约拍和发布者编号兼容行为。
- Modify（修改）: `frontend/src/pages-sub/ypat/detail.vue`，返回页面时刷新详情。
- Create（创建）: `frontend/src/pages-sub/ypat/detail.test.ts`，记录详情返回刷新契约。
- Modify（修改）: `frontend/src/pages-sub/work/apply.vue`，提交前检查最新目标状态并移除客户端身份参数。
- Modify（修改）: `frontend/src/pages-sub/work/apply.test.ts`，覆盖普通约拍和作品约拍的直接链接防重复。

### Task 1（任务 1）: 约拍详情返回发布者和申请状态

**Files（文件）:**
- Create（创建）: `backend/system-wap/src/test/java/com/ypat/controller/YpatApplyFlowSourceTest.java`
- Modify（修改）: `backend/system-object/src/main/java/com/ypat/YpatInfoQo.java`
- Modify（修改）: `backend/system-domain/src/main/java/com/ypat/service/YpatInfoService.java`

- [ ] **Step 1（步骤 1）: 写后端失败契约测试**

创建 `YpatApplyFlowSourceTest.java`，读取 `YpatInfoQo.java` 和 `YpatInfoService.java`，断言包含：

```java
assertTrue(qoSource.contains("public String getMsgflag()"));
assertTrue(qoSource.contains("public void setMsgflag(String msgflag)"));
assertTrue(serviceSource.contains("ypatInfoQo.setUserid(user.getId())"));
assertTrue(serviceSource.contains("messInfoRepository.countSend(MessType.send.value, userid, ypatInfo.getId())"));
assertTrue(serviceSource.contains("ypatInfoQo.setMsgflag(hasSent != null && hasSent > 0 ? YesNo.yes.value : YesNo.no.value)"));
```

测试使用现有 `readSource`（读取源码）模式，不依赖运行数据库。

- [ ] **Step 2（步骤 2）: 运行轻量断言确认失败**

Run（运行）:

```bash
node -e "const fs=require('fs');const q=fs.readFileSync('backend/system-object/src/main/java/com/ypat/YpatInfoQo.java','utf8');const s=fs.readFileSync('backend/system-domain/src/main/java/com/ypat/service/YpatInfoService.java','utf8');if(!q.includes('public String getMsgflag()')||!s.includes('ypatInfoQo.setUserid(user.getId())'))process.exit(1)"
```

Expected（预期）: 退出码为 1，因为详情契约尚未实现。

- [ ] **Step 3（步骤 3）: 实现传输对象和详情状态**

在 `YpatInfoQo`（约拍详情传输对象）增加：

```java
public String getMsgflag() {
    return msgflag;
}

public void setMsgflag(String msgflag) {
    this.msgflag = msgflag;
}
```

在 `YpatInfoService`（约拍详情领域服务）注入 `MessInfoRepository`（消息仓储），导入 `MessType`（消息类型），并在 `findById`（按编号查询详情）中实现：

```java
ypatInfoQo.setMsgflag(YesNo.no.value);
if(user != null){
    ypatInfoQo.setUserid(user.getId());
    // 保留现有 userQo 组装逻辑
}
if(userid != null) {
    int favoriteCount = userYpatRepository.countByUseridAndYpatid(userid, ypatInfo.getId());
    ypatInfoQo.setColflag(favoriteCount > 0 ? YesNo.yes.value : YesNo.no.value);
    Long hasSent = messInfoRepository.countSend(MessType.send.value, userid, ypatInfo.getId());
    ypatInfoQo.setMsgflag(hasSent != null && hasSent > 0 ? YesNo.yes.value : YesNo.no.value);
}
```

- [ ] **Step 4（步骤 4）: 运行轻量断言确认通过**

Run（运行）:

```bash
node -e "const fs=require('fs');const q=fs.readFileSync('backend/system-object/src/main/java/com/ypat/YpatInfoQo.java','utf8');const s=fs.readFileSync('backend/system-domain/src/main/java/com/ypat/service/YpatInfoService.java','utf8');if(!q.includes('public String getMsgflag()')||!q.includes('public void setMsgflag(String msgflag)')||!s.includes('ypatInfoQo.setUserid(user.getId())')||!s.includes('messInfoRepository.countSend(MessType.send.value, userid, ypatInfo.getId())'))process.exit(1)"
```

Expected（预期）: 无输出，退出码为 0。

### Task 2（任务 2）: 后端并发和身份保护

**Files（文件）:**
- Modify（修改）: `backend/system-wap/src/test/java/com/ypat/controller/YpatApplyFlowSourceTest.java`
- Modify（修改）: `backend/system-domain/src/main/java/com/ypat/service/MessInfoService.java`
- Modify（修改）: `backend/system-domain/src/main/java/com/ypat/service/WorkService.java`

- [ ] **Step 1（步骤 1）: 扩展失败契约测试**

在 `YpatApplyFlowSourceTest`（约拍申请链路源码测试）增加：

```java
assertTrue(messageServiceSource.contains("userRepository.findByIdForUpdate(userid)"));
assertTrue(messageServiceSource.contains("if (ypatInfo == null)"));
assertTrue(messageServiceSource.contains("if (userid.equals(recper.getId()))"));
assertTrue(workServiceSource.contains("userRepository.findByIdForUpdate(viewerId)"));
```

- [ ] **Step 2（步骤 2）: 运行轻量断言确认失败**

Run（运行）:

```bash
node -e "const fs=require('fs');const m=fs.readFileSync('backend/system-domain/src/main/java/com/ypat/service/MessInfoService.java','utf8');const w=fs.readFileSync('backend/system-domain/src/main/java/com/ypat/service/WorkService.java','utf8');if(!m.includes('userRepository.findByIdForUpdate(userid)')||!w.includes('userRepository.findByIdForUpdate(viewerId)'))process.exit(1)"
```

Expected（预期）: 退出码为 1，因为两个申请分支仍使用无锁查询。

- [ ] **Step 3（步骤 3）: 实现普通约拍保护**

将申请用户查询改为：

```java
User user = userRepository.findByIdForUpdate(userid);
if(user == null){
    throw new SysException(ResponseCode.FAIL_AUTH);
}
YpatInfo ypatInfo = ypatInfoRepository.findById(ypatid);
if(ypatInfo == null){
    throw new SysException(ResponseCode.FAIL_NOT);
}
User recper = ypatInfo.getUser();
if(recper == null){
    throw new SysException(ResponseCode.FAIL_NOT);
}
if(userid.equals(recper.getId())){
    throw new SysException(ResponseCode.FAIL_VAL, "不能给自己约拍");
}
```

现有重复查询必须位于上述写锁之后。

- [ ] **Step 4（步骤 4）: 实现作品快捷约拍保护**

在 `WorkService.quickApply`（作品快捷约拍领域方法）中把：

```java
User viewer = userRepository.findById(viewerId);
```

替换为：

```java
User viewer = userRepository.findByIdForUpdate(viewerId);
```

- [ ] **Step 5（步骤 5）: 运行后端轻量契约确认通过**

Run（运行）:

```bash
node -e "const fs=require('fs');const m=fs.readFileSync('backend/system-domain/src/main/java/com/ypat/service/MessInfoService.java','utf8');const w=fs.readFileSync('backend/system-domain/src/main/java/com/ypat/service/WorkService.java','utf8');if(!m.includes('userRepository.findByIdForUpdate(userid)')||!m.includes('if(userid.equals(recper.getId()))')||!w.includes('userRepository.findByIdForUpdate(viewerId)'))process.exit(1)"
```

Expected（预期）: 无输出，退出码为 0。

### Task 3（任务 3）: 约拍详情展示并刷新已申请状态

**Files（文件）:**
- Modify（修改）: `frontend/src/api/types/index.ts`
- Modify（修改）: `frontend/src/components/business/__tests__/ypat-detail-view.test.ts`
- Modify（修改）: `frontend/src/components/business/YpatDetailView.vue`
- Create（创建）: `frontend/src/pages-sub/ypat/detail.test.ts`
- Modify（修改）: `frontend/src/pages-sub/ypat/detail.vue`

- [ ] **Step 1（步骤 1）: 写组件失败测试**

在约拍详情组件测试增加：

```ts
it('已提交约拍时展示禁用状态且不再跳转', async () => {
  const wrapper = await mountWithDetail(createDetail({ msgflag: '1' }))
  const button = wrapper.find('.detail-actions__primary')

  expect(button.text()).toBe('已约拍')
  expect(button.attributes('disabled')).toBeDefined()
  await button.trigger('tap')
  expect(navigateTo).not.toHaveBeenCalled()
})

it('顶层发布者编号缺失时使用 userQo.id', async () => {
  const wrapper = await mountWithDetail(createDetail({ userid: undefined }))
  await wrapper.find('.author-card').trigger('tap')
  expect(navigateTo).toHaveBeenCalledWith({ url: '/pages-sub/user/profile?id=201' })
})
```

- [ ] **Step 2（步骤 2）: 写页面刷新失败测试**

创建 `detail.test.ts`（约拍详情页面测试），断言页面包含组件引用、`onShow`（显示钩子）以及：

```ts
detailView.value?.load()
```

- [ ] **Step 3（步骤 3）: 运行测试确认失败**

Run（运行）:

```bash
cd frontend && npx vitest run src/components/business/__tests__/ypat-detail-view.test.ts src/pages-sub/ypat/detail.test.ts
```

Expected（预期）: 已申请按钮、发布者兜底或页面刷新断言失败。

- [ ] **Step 4（步骤 4）: 实现详情组件状态**

`YpatInfo`（约拍详情类型）调整为：

```ts
userid?: number
msgflag?: string
```

详情组件增加：

```ts
const publisherId = computed(() => Number(detail.value?.userid || detail.value?.userQo?.id || 0))
const applied = computed(() => detail.value?.msgflag === '1')
```

主按钮使用 `:disabled="actionLoading || applied"`，文本使用 `{{ applied ? '已约拍' : '立即约拍' }}`。`goProfile`（进入主页）和本人约拍判断使用 `publisherId`（发布者编号）；`apply`（申请动作）在 `applied`（已申请）时直接返回。组件末尾增加：

```ts
defineExpose({ load })
```

- [ ] **Step 5（步骤 5）: 实现详情页返回刷新**

页面模板和脚本增加：

```vue
<YpatDetailView ref="detailView" :id="ypatId" @share-meta="updateShareMeta" />
```

```ts
const detailView = ref<InstanceType<typeof YpatDetailView> | null>(null)

onShow(() => {
  if (ypatId.value) detailView.value?.load()
})
```

- [ ] **Step 6（步骤 6）: 运行详情测试确认通过**

Run（运行）:

```bash
cd frontend && npx vitest run src/components/business/__tests__/ypat-detail-view.test.ts src/pages-sub/ypat/detail.test.ts
```

Expected（预期）: 两个测试文件全部通过。

### Task 4（任务 4）: 申请页提交前检查最新状态

**Files（文件）:**
- Modify（修改）: `frontend/src/api/types/index.ts`
- Modify（修改）: `frontend/src/pages-sub/work/apply.test.ts`
- Modify（修改）: `frontend/src/pages-sub/work/apply.vue`

- [ ] **Step 1（步骤 1）: 写申请页失败测试**

增加源码契约断言：

```ts
expect(source).toContain("const isAlreadyApplied = computed(() => ypatId.value ? ypat.value?.msgflag === '1' : work.value?.isApplied === true)")
expect(source).toContain("isAlreadyApplied.value ? '已约拍' : '确认提交'")
expect(source).toContain('const latestTarget = ypatId.value ? await loadYpat() : await loadWork()')
expect(source.indexOf('const latestTarget =')).toBeLessThan(source.indexOf('await refreshPpdBalance()'))
expect(source).toContain("uni.showToast({ title: '你已提交过该约拍', icon: 'none' })")
expect(source).not.toContain('sendperid: currentUserId')
expect(source).not.toContain('recperid: publisherId')
```

- [ ] **Step 2（步骤 2）: 运行申请页测试确认失败**

Run（运行）:

```bash
cd frontend && npx vitest run src/pages-sub/work/apply.test.ts
```

Expected（预期）: 新增状态检查和精简请求参数断言失败。

- [ ] **Step 3（步骤 3）: 精简申请参数类型**

将 `YpatApplyParams`（约拍申请参数）改为：

```ts
export interface YpatApplyParams {
  ypatid: number
  content?: string
}
```

- [ ] **Step 4（步骤 4）: 实现申请页目标状态检查**

让 `loadWork`（加载作品）和 `loadYpat`（加载约拍）返回加载结果；增加 `isAlreadyApplied`（是否已申请）计算值，并把它加入按钮禁用条件与按钮文本。

在提交锁之后、余额刷新之前执行：

```ts
const latestTarget = ypatId.value ? await loadYpat() : await loadWork()
if (!latestTarget) {
  uni.showToast({ title: '约拍信息加载失败', icon: 'none' })
  return
}
if (isAlreadyApplied.value) {
  uni.showToast({ title: '你已提交过该约拍', icon: 'none' })
  return
}
```

普通约拍提交改为：

```ts
await ypatApi.applyYpat({
  ypatid: ypatId.value,
  content: buildYpatApplyContent(),
})
```

- [ ] **Step 5（步骤 5）: 运行申请页测试确认通过**

Run（运行）:

```bash
cd frontend && npx vitest run src/pages-sub/work/apply.test.ts
```

Expected（预期）: 申请页相关测试全部通过。

### Task 5（任务 5）: 针对性验证

**Files（文件）:**
- Verify（验证）: 上述所有修改文件

- [ ] **Step 1（步骤 1）: 运行相关前端测试**

Run（运行）:

```bash
cd frontend && npx vitest run src/components/business/__tests__/ypat-detail-view.test.ts src/pages-sub/ypat/detail.test.ts src/pages-sub/work/apply.test.ts src/components/business/__tests__/work-action-bar.test.ts src/pages-sub/work/detail.test.ts
```

Expected（预期）: 所有相关测试通过。

- [ ] **Step 2（步骤 2）: 运行前端代码规范检查**

Run（运行）:

```bash
cd frontend && npx eslint src/api/types/index.ts src/components/business/YpatDetailView.vue src/components/business/__tests__/ypat-detail-view.test.ts src/pages-sub/ypat/detail.vue src/pages-sub/ypat/detail.test.ts src/pages-sub/work/apply.vue src/pages-sub/work/apply.test.ts --quiet
```

Expected（预期）: 无输出，退出码为 0。

- [ ] **Step 3（步骤 3）: 运行后端源码契约断言**

Run（运行）:

```bash
node -e "const fs=require('fs');const q=fs.readFileSync('backend/system-object/src/main/java/com/ypat/YpatInfoQo.java','utf8');const y=fs.readFileSync('backend/system-domain/src/main/java/com/ypat/service/YpatInfoService.java','utf8');const m=fs.readFileSync('backend/system-domain/src/main/java/com/ypat/service/MessInfoService.java','utf8');const w=fs.readFileSync('backend/system-domain/src/main/java/com/ypat/service/WorkService.java','utf8');if(!q.includes('getMsgflag')||!y.includes('messInfoRepository.countSend')||!m.includes('findByIdForUpdate(userid)')||!w.includes('findByIdForUpdate(viewerId)'))process.exit(1)"
```

Expected（预期）: 无输出，退出码为 0。

- [ ] **Step 4（步骤 4）: 运行本次差异格式检查**

Run（运行）:

```bash
git diff --check -- backend/system-object/src/main/java/com/ypat/YpatInfoQo.java backend/system-domain/src/main/java/com/ypat/service/YpatInfoService.java backend/system-domain/src/main/java/com/ypat/service/MessInfoService.java backend/system-domain/src/main/java/com/ypat/service/WorkService.java backend/system-wap/src/test/java/com/ypat/controller/YpatApplyFlowSourceTest.java frontend/src/api/types/index.ts frontend/src/components/business/YpatDetailView.vue frontend/src/components/business/__tests__/ypat-detail-view.test.ts frontend/src/pages-sub/ypat/detail.vue frontend/src/pages-sub/ypat/detail.test.ts frontend/src/pages-sub/work/apply.vue frontend/src/pages-sub/work/apply.test.ts
```

Expected（预期）: 无输出，退出码为 0。

- [ ] **Step 5（步骤 5）: 审查最终差异**

确认最终差异只包含发布者编号、申请状态、客户端身份参数移除、详情刷新、悲观锁和相关测试，不包含需求外改动。

不下载依赖、不执行完整构建，由用户完成微信开发者工具手动验收。
