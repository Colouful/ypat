# 约拍申请与消息展示链路修复实施计划

> **For agentic workers（面向智能代理执行者）:** REQUIRED SUB-SKILL（必需子技能）: Use superpowers:subagent-driven-development（子代理驱动开发，推荐） or superpowers:executing-plans（计划执行） to implement this plan task-by-task（逐任务实施本计划）. Steps use checkbox（复选框） (`- [ ]`) syntax for tracking（跟踪）。

**Goal（目标）:** 让“我的申请”和消息页“申请的约拍”使用正确的申请列表接口，并兼容旧后端分页结构，使约拍提交后立即可见。

**Architecture（架构）:** 在约拍 API（应用程序接口）模块增加 `/my/ypat/app/list`（我的申请列表接口）封装，并在分页适配器统一转换 `pages/totals`（页数/总数）。两个页面分别使用 `YpatInfo`（约拍详情）渲染申请卡片；消息页“收到的约拍”继续使用 `MessInfo`（消息详情），不修改后端落库、扣豆或反馈消息逻辑。

**Tech Stack（技术栈）:** Vue 3（前端框架）、uni-app（小程序框架）、TypeScript（类型脚本）、Vitest（前端单元测试）、ESLint（代码规范检查）。

---

## 文件结构

- Modify（修改）: `frontend/src/api/adapters/index.ts`，兼容旧版 `content/pages/totals`（内容/页数/总数）分页结构和调用方页码回退值。
- Modify（修改）: `frontend/src/api/__tests__/adapters.test.ts`，覆盖旧分页转换。
- Modify（修改）: `frontend/src/api/modules/ypat.ts`，增加 `getMyApplicationList`（获取我的申请列表）。
- Modify（修改）: `frontend/src/api/__tests__/api-contracts.test.ts`，覆盖申请接口路径、请求方法和标准化结果。
- Create（创建）: `frontend/src/pages-sub/ypat/my-apply.test.ts`，记录“我的申请”页面数据源和导航契约。
- Modify（修改）: `frontend/src/pages-sub/ypat/my-apply.vue`，改用约拍申请详情模型。
- Create（创建）: `frontend/src/pages/message/index.test.ts`，记录消息页两类列表的数据源契约。
- Modify（修改）: `frontend/src/pages/message/index.vue`，分开渲染收到消息和已提交申请。

## Task 1（任务 1）: 申请列表接口与旧分页适配

**Files（文件）:**
- Modify（修改）: `frontend/src/api/__tests__/adapters.test.ts`
- Modify（修改）: `frontend/src/api/__tests__/api-contracts.test.ts`
- Modify（修改）: `frontend/src/api/adapters/index.ts`
- Modify（修改）: `frontend/src/api/modules/ypat.ts`

- [x] **Step 1（步骤 1）: 写旧分页失败测试**

在 `adapters.test.ts`（适配器测试）增加：

```ts
describe('normalizePageResult', () => {
  it('normalizes legacy content pages and totals with caller pagination fallbacks', async () => {
    const { normalizePageResult } = await loadAdapters()

    expect(normalizePageResult(
      { content: [{ id: 5 }], pages: 3, totals: 21 },
      { number: 1, size: 10 },
    )).toEqual({
      content: [{ id: 5 }],
      totalElements: 21,
      totalPages: 3,
      number: 1,
      size: 10,
    })
  })
})
```

- [x] **Step 2（步骤 2）: 写申请接口失败测试**

在 `api-contracts.test.ts`（接口契约测试）增加：

```ts
it('loads my applications from the legacy app endpoint and normalizes pagination', async () => {
  const p = { userid: 7, page: 1, size: 10 }
  requestMocks.get.mockResolvedValueOnce({
    success: true,
    data: { content: [{ id: 5 }], pages: 2, totals: 11 },
    code: '200',
    message: '',
  })

  const result = await ypatApi.getMyApplicationList(p)

  expect(requestMocks.get).toHaveBeenCalledWith('/my/ypat/app/list', { ...p })
  expect(result.data).toEqual({
    content: [{ id: 5 }],
    totalElements: 11,
    totalPages: 2,
    number: 1,
    size: 10,
  })
})
```

- [x] **Step 3（步骤 3）: 运行测试确认按预期失败**

Run（运行）:

```bash
cd frontend && /Users/lizhenwei/workspace/vueworkspace/ypat-workspace/frontend/node_modules/.bin/vitest run src/api/__tests__/adapters.test.ts src/api/__tests__/api-contracts.test.ts
```

Expected（预期）: `normalizePageResult`（分页结果标准化方法）不接受第二个参数，且 `getMyApplicationList`（获取我的申请列表）不存在。

- [x] **Step 4（步骤 4）: 实现旧分页适配**

将 `normalizePageResult`（分页结果标准化方法）签名调整为：

```ts
interface PageFallback {
  number?: number
  size?: number
}

export function normalizePageResult<T>(data: any, fallback: PageFallback = {}): PageResult<T>
```

在 `content`（内容列表）分支使用空值合并，保留合法的零值：

```ts
const content = data.content || []
const totalElements = Number(data.totalElements ?? data.totals ?? content.length)
const size = Number(data.size ?? fallback.size ?? 10)
return {
  content,
  totalElements,
  totalPages: Number(data.totalPages ?? data.pages ?? (size > 0 ? Math.ceil(totalElements / size) : 0)),
  number: Number(data.number ?? fallback.number ?? 0),
  size,
}
```

空数据和其他分页分支也使用 `fallback.number`（回退页码）与 `fallback.size`（回退每页条数），但不改变已有标准响应结果。

- [x] **Step 5（步骤 5）: 实现我的申请接口**

在 `ypat.ts`（约拍接口模块）导入 `normalizePageResult`（分页结果标准化方法），增加：

```ts
export async function getMyApplicationList(
  params: YpatMyListParams,
): Promise<ApiResult<PageResult<YpatInfo>>> {
  const result = await get<unknown>('/my/ypat/app/list', { ...params })
  return {
    ...result,
    data: normalizePageResult<YpatInfo>(result.data, {
      number: params.page,
      size: params.size,
    }),
  }
}
```

- [x] **Step 6（步骤 6）: 运行接口与适配器测试确认通过**

Run（运行）:

```bash
cd frontend && /Users/lizhenwei/workspace/vueworkspace/ypat-workspace/frontend/node_modules/.bin/vitest run src/api/__tests__/adapters.test.ts src/api/__tests__/api-contracts.test.ts
```

Expected（预期）: 两个测试文件全部通过。

- [x] **Step 7（步骤 7）: 提交接口层修复**

```bash
git add frontend/src/api/adapters/index.ts frontend/src/api/__tests__/adapters.test.ts frontend/src/api/modules/ypat.ts frontend/src/api/__tests__/api-contracts.test.ts
git commit -m "fix: restore ypat application list api"
```

## Task 2（任务 2）: 修复我的申请页面

**Files（文件）:**
- Create（创建）: `frontend/src/pages-sub/ypat/my-apply.test.ts`
- Modify（修改）: `frontend/src/pages-sub/ypat/my-apply.vue`

- [x] **Step 1（步骤 1）: 写页面失败契约测试**

创建 `my-apply.test.ts`（我的申请页面测试）：

```ts
import { readFileSync } from 'node:fs'
import { fileURLToPath, URL } from 'node:url'
import { describe, expect, it } from 'vitest'

describe('my ypat application page contract', () => {
  const file = fileURLToPath(new URL('./my-apply.vue', import.meta.url))
  const source = readFileSync(file, 'utf8')

  it('loads submitted applications instead of viewed feedback messages', () => {
    expect(source).toContain('ypatApi.getMyApplicationList')
    expect(source).not.toContain('ypatApi.getMySentList')
    expect(source).toContain('const items = ref<YpatInfo[]>([])')
  })

  it('renders ypat details and navigates with the ypat id', () => {
    expect(source).toContain("item.pics?.[0] || '/static/default-cover.png'")
    expect(source).toContain("item.userQo?.nickname || '匿名用户'")
    expect(source).toContain('item.describ')
    expect(source).toContain('`/pages-sub/ypat/detail?id=${item.id}`')
  })
})
```

- [x] **Step 2（步骤 2）: 运行页面测试确认按预期失败**

Run（运行）:

```bash
cd frontend && /Users/lizhenwei/workspace/vueworkspace/ypat-workspace/frontend/node_modules/.bin/vitest run src/pages-sub/ypat/my-apply.test.ts
```

Expected（预期）: 页面仍调用 `getMySentList`（获取反馈消息列表）并使用 `MessInfo`（消息详情）。

- [x] **Step 3（步骤 3）: 切换数据源和页面模型**

在 `my-apply.vue`（我的申请页面）中：

```ts
import type { YpatInfo } from '@/api/types'

const items = ref<YpatInfo[]>([])

const result = await ypatApi.getMyApplicationList({ userid, page: page.value, size })

function openDetail(item: YpatInfo): void {
  uni.navigateTo({ url: `/pages-sub/ypat/detail?id=${item.id}` })
}
```

删除 `messageApi`（消息接口模块）和 `resolveMessageNavigation`（消息导航解析方法）依赖。模板字段调整为：

```vue
<image class="cover" :src="item.pics?.[0] || '/static/default-cover.png'" mode="aspectFill" />
<text class="title">{{ item.targetTxt || '约拍申请' }}</text>
<text class="publisher">{{ item.userQo?.nickname || '匿名用户' }}</text>
<text class="desc">{{ item.describ || '已提交约拍申请' }}</text>
<text class="meta">{{ [item.city, item.area].filter(Boolean).join(' · ') || '地点待沟通' }} {{ item.timeStr || formatTime(item.pubdate) }}</text>
```

为 `.publisher`（发布者文本）补充与现有卡片一致的紧凑文字样式，不改动页面布局结构。

- [x] **Step 4（步骤 4）: 运行我的申请页面测试确认通过**

Run（运行）:

```bash
cd frontend && /Users/lizhenwei/workspace/vueworkspace/ypat-workspace/frontend/node_modules/.bin/vitest run src/pages-sub/ypat/my-apply.test.ts src/api/__tests__/api-contracts.test.ts
```

Expected（预期）: 两个测试文件全部通过。

- [x] **Step 5（步骤 5）: 提交我的申请页面修复**

```bash
git add frontend/src/pages-sub/ypat/my-apply.vue frontend/src/pages-sub/ypat/my-apply.test.ts
git commit -m "fix: show submitted ypat applications"
```

## Task 3（任务 3）: 修复消息页申请标签

**Files（文件）:**
- Create（创建）: `frontend/src/pages/message/index.test.ts`
- Modify（修改）: `frontend/src/pages/message/index.vue`

- [ ] **Step 1（步骤 1）: 写消息页失败契约测试**

创建 `index.test.ts`（消息页面测试）：

```ts
import { readFileSync } from 'node:fs'
import { fileURLToPath, URL } from 'node:url'
import { describe, expect, it } from 'vitest'

describe('message ypat list contract', () => {
  const file = fileURLToPath(new URL('./index.vue', import.meta.url))
  const source = readFileSync(file, 'utf8')

  it('keeps received messages and loads submitted applications for the sent tab', () => {
    expect(source).toContain('ypatApi.getMyReceivedList')
    expect(source).toContain('ypatApi.getMyApplicationList')
    expect(source).not.toContain('ypatApi.getMySentList')
    expect(source).toContain('const receivedItems = ref<MessInfo[]>([])')
    expect(source).toContain('const applicationItems = ref<YpatInfo[]>([])')
  })

  it('shows application totals instead of viewed-feedback unread counts', () => {
    expect(source).toContain('申请总数')
    expect(source).toContain('applicationCount')
    expect(source).not.toContain('getSendUnreadCount')
  })

  it('opens submitted applications by ypat id', () => {
    expect(source).toContain('function openApplicationDetail(item: YpatInfo)')
    expect(source).toContain('`/pages-sub/ypat/detail?id=${item.id}`')
  })
})
```

- [ ] **Step 2（步骤 2）: 运行消息页测试确认按预期失败**

Run（运行）:

```bash
cd frontend && /Users/lizhenwei/workspace/vueworkspace/ypat-workspace/frontend/node_modules/.bin/vitest run src/pages/message/index.test.ts
```

Expected（预期）: 消息页仍用 `getMySentList`（获取反馈消息列表）和单一 `MessInfo`（消息详情）数组。

- [ ] **Step 3（步骤 3）: 分离收到消息和申请列表状态**

将消息页状态调整为：

```ts
import type { MessInfo, PageResult, YpatInfo } from '@/api/types'

const receivedItems = ref<MessInfo[]>([])
const applicationItems = ref<YpatInfo[]>([])
const activeItemCount = computed(() => (
  tab.value === 'received' ? receivedItems.value.length : applicationItems.value.length
))
const applicationCount = ref(0)
```

`load`（加载列表）根据当前标签分别调用：

```ts
if (tab.value === 'received') {
  const result = await ypatApi.getMyReceivedList(params)
  const content = result.data?.content || []
  receivedItems.value = refresh ? content : receivedItems.value.concat(content)
  updatePagination(result.data)
} else {
  const result = await ypatApi.getMyApplicationList(params)
  const content = result.data?.content || []
  applicationItems.value = refresh ? content : applicationItems.value.concat(content)
  applicationCount.value = result.data?.totalElements || 0
  updatePagination(result.data)
}
```

增加本地 `updatePagination`（更新分页）方法接收 `PageResult<unknown>`（通用分页结果），统一更新 `page`（页码）和 `hasMore`（是否有更多），避免两个分支重复分页计算。

```ts
function updatePagination(result?: PageResult<unknown>): void {
  const current = result?.number ?? page.value
  hasMore.value = current + 1 < (result?.totalPages || 0)
  page.value = current + 1
}
```

- [ ] **Step 4（步骤 4）: 调整统计和刷新逻辑**

将 `refreshUnreadBreakdown`（刷新未读统计）改名为 `refreshMessageStats`（刷新消息统计），并请求：

```ts
const [received, applications] = await Promise.all([
  messageApi.getRecUnreadCount('1', userid),
  ypatApi.getMyApplicationList({ userid, page: 0, size: 1 }),
])
receivedUnread.value = Number(received.data || 0)
applicationCount.value = Number(applications.data?.totalElements || 0)
```

未登录或请求失败时将两个统计值归零。删除 `getSendUnreadCount`（获取反馈未读数）调用。切换标签时只清空目标标签对应数组并重新加载第一页。

```ts
function switchTab(value: 'received' | 'sent'): void {
  tab.value = value
  if (value === 'received') receivedItems.value = []
  else applicationItems.value = []
  void load(true)
}
```

- [ ] **Step 5（步骤 5）: 分开渲染两类卡片**

模板按 `tab`（当前标签）分支：

```vue
<view v-if="tab === 'received'">
  <view v-for="item in receivedItems" :key="item.id" class="message-card" @tap="openReceivedDetail(item)">
    <image class="message-card__avatar" :src="item.imgpath || '/static/default-avatar.png'" mode="aspectFill" />
    <view class="message-card__body">
      <view class="message-card__head">
        <text class="message-card__name">{{ item.nickname || '用户' }}</text>
        <text class="message-card__tag">收到</text>
      </view>
      <text class="message-card__content">{{ item.content || '约拍动态' }}</text>
      <view class="message-card__meta">
        <text>{{ item.city || '同城' }}</text>
        <text>{{ item.timeStr || item.credate || '刚刚' }}</text>
      </view>
    </view>
    <KeepIcon name="chevron-right" :size="34" color="#B3B8BE" />
  </view>
</view>
<view v-else>
  <view v-for="item in applicationItems" :key="item.id" class="message-card" @tap="openApplicationDetail(item)">
    <image class="message-card__avatar" :src="item.userQo?.imgpath || '/static/default-avatar.png'" mode="aspectFill" />
    <view class="message-card__body">
      <view class="message-card__head">
        <text class="message-card__name">{{ item.userQo?.nickname || '匿名用户' }}</text>
        <text class="message-card__tag message-card__tag--sent">申请</text>
      </view>
      <text class="message-card__content">{{ item.describ || '已提交约拍申请' }}</text>
      <view class="message-card__meta">
        <text>{{ [item.city, item.area].filter(Boolean).join(' · ') || '地点待沟通' }}</text>
        <text>{{ item.timeStr || item.pubdate || '刚刚' }}</text>
      </view>
    </view>
    <KeepIcon name="chevron-right" :size="34" color="#B3B8BE" />
  </view>
</view>
```

状态卡和“没有更多”判断使用 `activeItemCount`（当前列表条数）。申请统计卡标题改为“申请总数”，数字和标签数字使用 `applicationCount`（申请总数）。

收到消息点击仍复用 `resolveMessageNavigation`（消息导航解析方法）；申请点击直接执行：

```ts
function openApplicationDetail(item: YpatInfo): void {
  uni.navigateTo({ url: `/pages-sub/ypat/detail?id=${item.id}` })
}
```

- [ ] **Step 6（步骤 6）: 运行消息页测试确认通过**

Run（运行）:

```bash
cd frontend && /Users/lizhenwei/workspace/vueworkspace/ypat-workspace/frontend/node_modules/.bin/vitest run src/pages/message/index.test.ts src/utils/__tests__/message-navigation.test.ts src/api/__tests__/api-contracts.test.ts
```

Expected（预期）: 三个测试文件全部通过。

- [ ] **Step 7（步骤 7）: 提交消息页修复**

```bash
git add frontend/src/pages/message/index.vue frontend/src/pages/message/index.test.ts
git commit -m "fix: show applications in message tab"
```

## Task 4（任务 4）: 针对性验证

**Files（文件）:**
- Verify（验证）: 本计划所有修改文件

- [ ] **Step 1（步骤 1）: 运行约拍与消息相关测试**

Run（运行）:

```bash
cd frontend && /Users/lizhenwei/workspace/vueworkspace/ypat-workspace/frontend/node_modules/.bin/vitest run src/api/__tests__/adapters.test.ts src/api/__tests__/api-contracts.test.ts src/pages-sub/ypat/my-apply.test.ts src/pages/message/index.test.ts src/pages-sub/work/apply.test.ts src/pages-sub/ypat/detail.test.ts src/components/business/__tests__/ypat-detail-view.test.ts src/utils/__tests__/message-navigation.test.ts
```

Expected（预期）: 所有相关测试通过。

- [ ] **Step 2（步骤 2）: 运行修改文件代码规范检查**

Run（运行）:

```bash
cd frontend && /Users/lizhenwei/workspace/vueworkspace/ypat-workspace/frontend/node_modules/.bin/eslint src/api/adapters/index.ts src/api/__tests__/adapters.test.ts src/api/modules/ypat.ts src/api/__tests__/api-contracts.test.ts src/pages-sub/ypat/my-apply.vue src/pages-sub/ypat/my-apply.test.ts src/pages/message/index.vue src/pages/message/index.test.ts --quiet
```

Expected（预期）: 无输出，退出码为 0。

- [ ] **Step 3（步骤 3）: 运行差异格式检查**

Run（运行）:

```bash
git diff --check
```

Expected（预期）: 无输出，退出码为 0。

- [ ] **Step 4（步骤 4）: 复核最终范围**

确认最终差异只包含申请列表接口、旧分页适配、两个页面的数据源与渲染、相关测试及计划状态；不包含后端、数据库、作品快捷约拍或页面整体视觉重构。

按用户要求不下载依赖、不执行完整构建，由用户在微信开发者工具完成手动验收。
