# 我的收藏双标签页 Implementation Plan（实施计划）

> **For agentic workers（给代理执行者）:** REQUIRED SUB-SKILL（必需子技能）: Use superpowers:subagent-driven-development（子代理驱动开发，推荐）or superpowers:executing-plans（计划执行）to implement this plan task-by-task。Steps use checkbox（复选框）`- [ ]` syntax for tracking（进度跟踪）。本次用户已明确指定由 brainstorming（需求梳理）在当前会话连续执行，不派发子代理。

**Goal（目标）:** 为“我的收藏”页面增加“约拍 / 作品”两个主题色 tab（标签页），并补齐当前用户收藏作品的分页查询链路。

**Architecture（架构）:** 约拍收藏继续使用现有 `/my/ypat/sc/list` 接口；作品收藏新增 `/work/favorites` 分页接口，通过 WAP（网页服务）、Feign（声明式服务调用）、REST API（接口服务）到 `WorkService`（作品领域服务）。页面按 tab（标签页）分别维护分页状态，默认加载约拍，作品首次切换时按需加载。

**Tech Stack（技术栈）:** Vue 3（渐进式前端框架）、uni-app（跨端应用框架）、TypeScript（类型化脚本语言）、SCSS（样式预处理器）、Spring Boot（Java 应用框架）、Spring Data JPA（数据访问框架）、Feign（声明式服务调用）。

**Verification Constraint（验证约束）:** 不下载依赖、不执行构建或自动化测试；只运行 ESLint（代码规范检查）、`git diff --check`（差异格式检查）、接口路径静态核对，并由用户手工联调。

---

## 文件结构

- `backend/system-domain/src/main/java/com/ypat/service/WorkService.java`：查询收藏关系并组装作品列表项。
- `backend/system-restapi/src/main/java/com/ypat/controller/WorkController.java`：暴露内部作品收藏分页接口。
- `backend/system-wap/src/main/java/com/ypat/service/WorkServiceClient.java`：声明 Feign（声明式服务调用）方法。
- `backend/system-wap/src/main/java/com/ypat/controller/WorkController.java`：校验登录态并暴露小程序接口。
- `frontend/src/api/modules/work.ts`：提供类型安全的作品收藏列表请求函数。
- `frontend/src/pages-sub/ypat/my-favorite.vue`：双 tab（标签页）、独立分页状态、卡片展示和主题样式。

### Task 1: 补齐作品收藏分页后端链路

**Files（文件）:**
- Modify（修改）: `backend/system-domain/src/main/java/com/ypat/service/WorkService.java`
- Modify（修改）: `backend/system-restapi/src/main/java/com/ypat/controller/WorkController.java`
- Modify（修改）: `backend/system-wap/src/main/java/com/ypat/service/WorkServiceClient.java`
- Modify（修改）: `backend/system-wap/src/main/java/com/ypat/controller/WorkController.java`

- [ ] **Step 1: 在领域服务增加收藏作品分页方法**

在 `WorkService.myWorks`（我的作品方法）之前加入：

```java
/**
 * 当前用户收藏的作品，按收藏时间倒序
 */
public Map<String, Object> favoriteWorks(Long userId, Integer page, Integer size) {
    if (userId == null) throw new SysException(ResponseCode.FAIL_AUTH);
    int p = page == null || page < 1 ? 1 : page;
    int s = size == null || size < 1 ? 10 : Math.min(size, 20);
    Page<WorkFavorite> result = workFavoriteRepository.findByUserIdOrderByCreatedAtDesc(
        userId, new PageRequest(p - 1, s));

    List<WorkListItem> items = new ArrayList<>();
    for (WorkFavorite favorite : result.getContent()) {
        Work work = workRepository.findByIdAndStatusAndDeletedFlag(
            favorite.getWorkId(), WorkStatus.shtg.value, 0);
        if (work == null) continue;

        WorkListItem item = new WorkListItem();
        item.setId(work.getId());
        item.setDescription(work.getDescription());
        item.setMediaType(work.getMediaType());
        item.setIsVideo("2".equals(work.getMediaType()) ? "1" : "0");
        item.setReadCount(work.getReadCount());
        item.setLikeCount(work.getLikeCount());
        item.setFavoriteCount(work.getFavoriteCount());
        item.setPublishTime(work.getPublishTime());

        List<WorkMedia> medias = workMediaRepository
            .findByWorkIdAndDeletedAtIsNullOrderBySortNoAsc(work.getId());
        if (!medias.isEmpty()) item.setCoverUrl(medias.get(0).getUrl());

        User user = userRepository.findById(work.getUserid());
        if (user != null) {
            item.setUserId(user.getId());
            item.setNickname(user.getNickname());
            item.setAvatar(user.getAvatarurl());
            item.setGender(user.getGender());
            item.setProfession(user.getProfess());
            item.setCity(user.getCity());
            item.setArea(user.getArea());
        }
        item.setTags(loadWorkTagNames(work.getId(), Collections.emptyList()));
        items.add(item);
    }

    Map<String, Object> response = new HashMap<>();
    response.put("page", p);
    response.put("size", s);
    response.put("total", result.getTotalElements());
    response.put("hasMore", result.hasNext());
    response.put("items", items);
    return response;
}
```

- [ ] **Step 2: 在 REST API（接口服务）增加内部端点**

在 `backend/system-restapi/.../WorkController.java` 的 `my`（我的作品）方法后加入：

```java
@GetMapping("/favorites")
public ResponseApiBody favorites(@RequestParam("userId") String userId,
                                 @RequestParam("page") Integer page,
                                 @RequestParam("size") Integer size) {
    return ResponseApiBody.success(
        workService.favoriteWorks(Long.parseLong(userId), page, size));
}
```

- [ ] **Step 3: 在 Feign（声明式服务调用）客户端增加同路径声明**

在 `WorkServiceClient.my`（我的作品调用）后加入：

```java
@GetMapping("/service/work/favorites")
String favorites(@RequestParam("userId") String userId,
                 @RequestParam("page") Integer page,
                 @RequestParam("size") Integer size);
```

- [ ] **Step 4: 在 WAP（网页服务）增加登录用户入口**

在 `backend/system-wap/.../WorkController.java` 的 `my`（我的作品）方法后加入：

```java
@GetMapping("/work/favorites")
public String favorites(@RequestParam(value = "page", defaultValue = "1") Integer page,
                        @RequestParam(value = "size", defaultValue = "10") Integer size) {
    String userId = UserUtil.getUserId();
    if (userId == null) throw new SysException(ResponseCode.FAIL_AUTH);
    return workServiceClient.favorites(userId, page, size);
}
```

- [ ] **Step 5: 静态核对四层方法与路径**

Run（运行）:

```bash
rg -n 'work/favorites|service/work/favorites|favoriteWorks' \
  backend/system-wap/src/main/java/com/ypat/controller/WorkController.java \
  backend/system-wap/src/main/java/com/ypat/service/WorkServiceClient.java \
  backend/system-restapi/src/main/java/com/ypat/controller/WorkController.java \
  backend/system-domain/src/main/java/com/ypat/service/WorkService.java
```

Expected（预期）: WAP（网页服务）路径、Feign（声明式服务调用）路径、REST API（接口服务）路径和领域方法各出现一次，参数均为 `userId / page / size`。

- [ ] **Step 6: 提交后端改动**

```bash
git add \
  backend/system-domain/src/main/java/com/ypat/service/WorkService.java \
  backend/system-restapi/src/main/java/com/ypat/controller/WorkController.java \
  backend/system-wap/src/main/java/com/ypat/service/WorkServiceClient.java \
  backend/system-wap/src/main/java/com/ypat/controller/WorkController.java
git commit -m "feat: 增加作品收藏分页接口" \
  -m "补齐当前登录用户收藏作品从网页入口到领域服务的分页查询链路，并保持收藏时间倒序。" \
  -m "Constraint: 仅返回审核通过且未删除的作品。" \
  -m "Tested: 已静态核对四层接口路径和参数。" \
  -m "Co-authored-by: OmX <omx@oh-my-codex.dev>"
```

### Task 2: 增加前端作品收藏 API（接口）

**Files（文件）:**
- Modify（修改）: `frontend/src/api/modules/work.ts`
- Modify（修改）: `frontend/src/api/types/work.ts`

- [ ] **Step 1: 为作品列表结果增加分页边界字段**

在 `WorkListResult`（作品列表结果）中加入：

```ts
export interface WorkListResult {
  page: number
  size: number
  total: number
  hasMore?: boolean
  items: WorkListItem[]
}
```

- [ ] **Step 2: 增加列表请求函数**

在 `getMyWorks`（获取我的作品）后加入：

```ts
/** 收藏的作品 */
export function getFavoriteWorks(params: { page?: number; size?: number }): Promise<ApiResult<WorkListResult>> {
  return get<WorkListResult>('/work/favorites', params)
}
```

- [ ] **Step 3: 静态检查函数签名与请求路径**

Run（运行）:

```bash
rg -n 'getFavoriteWorks|/work/favorites|hasMore' frontend/src/api/modules/work.ts frontend/src/api/types/work.ts
```

Expected（预期）: 导出函数返回 `Promise<ApiResult<WorkListResult>>`（异步接口结果），请求路径为 `/work/favorites`。

### Task 3: 实现双 tab（标签页）收藏页面

**Files（文件）:**
- Modify（修改）: `frontend/src/pages-sub/ypat/my-favorite.vue`

- [ ] **Step 1: 替换页面模板**

模板结构使用：

```vue
<template>
  <view class="favorite-page">
    <KeepPageNav title="我的收藏" />
    <view class="favorite-tabs">
      <view
        v-for="tab in tabs"
        :key="tab.value"
        :class="['favorite-tabs__item', { 'favorite-tabs__item--active': activeTab === tab.value }]"
        @tap="switchTab(tab.value)"
      >
        <text>{{ tab.label }}</text>
      </view>
    </view>

    <view class="favorite-content">
      <view v-if="currentLoading && !currentItemCount" class="favorite-state">加载中...</view>
      <view v-else-if="!currentItemCount" class="favorite-state">
        {{ activeTab === 'ypat' ? '暂无收藏的约拍' : '暂无收藏的作品' }}
      </view>

      <template v-else-if="activeTab === 'ypat'">
        <view v-for="item in ypatState.items" :key="item.id" class="favorite-card" @tap="openYpat(item.id)">
          <image class="favorite-card__cover" :src="item.pics?.[0] || '/static/default-cover.png'" mode="aspectFill" />
          <view class="favorite-card__body">
            <view class="favorite-card__header">
              <text class="favorite-card__name">{{ item.userQo?.nickname || '匿名用户' }}</text>
              <text class="favorite-card__time">{{ item.timeStr || formatTime(item.pubdate) }}</text>
            </view>
            <text class="favorite-card__desc">{{ item.describ || '约拍信息' }}</text>
            <view class="favorite-card__meta">
              <text>{{ item.city || '地点待协商' }}</text>
              <text>{{ getChargeLabel(item.chargeway, item.chargeamt) }}</text>
            </view>
          </view>
        </view>
      </template>

      <template v-else>
        <view v-for="item in workState.items" :key="item.id" class="favorite-card" @tap="openWork(item.id)">
          <view class="favorite-card__cover-wrap">
            <image class="favorite-card__cover" :src="item.coverUrl || '/static/default-cover.png'" mode="aspectFill" />
            <text v-if="item.isVideo === '1'" class="favorite-card__video">视频</text>
          </view>
          <view class="favorite-card__body">
            <view v-if="item.tags?.length" class="favorite-card__tags">
              <text v-for="tag in item.tags.slice(0, 2)" :key="tag" class="favorite-card__tag">{{ tag }}</text>
            </view>
            <text class="favorite-card__desc">{{ item.description || '摄影作品' }}</text>
            <text class="favorite-card__author">
              {{ item.nickname || '匿名用户' }} · {{ getProfessionLabel(item.profession) }}
            </text>
            <view class="favorite-card__meta">
              <text>{{ item.city || '地区未填写' }}</text>
              <text class="favorite-card__count">
                <KeepIcon name="star" :size="22" color="#23C268" />
                {{ item.favoriteCount || 0 }}
              </text>
            </view>
          </view>
        </view>
      </template>

      <view v-if="currentLoadingMore" class="favorite-footer">加载中...</view>
      <view v-else-if="currentItemCount && !currentHasMore" class="favorite-footer">没有更多了</view>
    </view>
  </view>
</template>
```

- [ ] **Step 2: 替换页面脚本**

```ts
<script setup lang="ts">
import { computed, reactive, ref } from 'vue'
import { onPullDownRefresh, onReachBottom, onShow } from '@dcloudio/uni-app'
import KeepIcon from '@/components/business/KeepIcon.vue'
import KeepPageNav from '@/components/business/KeepPageNav.vue'
import { getFavoriteWorks } from '@/api/modules/work'
import * as ypatApi from '@/api/modules/ypat'
import { CHARGE_WAY_LABELS, getProfessLabel } from '@/constants/enums'
import { useUserStore } from '@/stores/user'
import type { YpatInfo } from '@/api/types'
import type { WorkListItem } from '@/api/types/work'

type FavoriteTab = 'ypat' | 'work'
interface ListState<T> {
  items: T[]
  page: number
  loading: boolean
  loadingMore: boolean
  hasMore: boolean
  loaded: boolean
}

function createListState<T>(page: number): ListState<T> {
  return reactive({ items: [], page, loading: false, loadingMore: false, hasMore: true, loaded: false }) as ListState<T>
}

const size = 10
const userStore = useUserStore()
const activeTab = ref<FavoriteTab>('ypat')
const tabs: Array<{ label: string; value: FavoriteTab }> = [
  { label: '约拍', value: 'ypat' },
  { label: '作品', value: 'work' },
]
const ypatState = createListState<YpatInfo>(0)
const workState = createListState<WorkListItem>(1)
const currentLoading = computed(() => activeTab.value === 'ypat' ? ypatState.loading : workState.loading)
const currentLoadingMore = computed(() => activeTab.value === 'ypat' ? ypatState.loadingMore : workState.loadingMore)
const currentHasMore = computed(() => activeTab.value === 'ypat' ? ypatState.hasMore : workState.hasMore)
const currentItemCount = computed(() => activeTab.value === 'ypat' ? ypatState.items.length : workState.items.length)

async function loadYpat(refresh = false): Promise<void> {
  const userid = userStore.userInfo?.id
  if (!userid || ypatState.loading || ypatState.loadingMore || (!refresh && !ypatState.hasMore)) return
  if (refresh) {
    ypatState.page = 0
    ypatState.hasMore = true
    ypatState.loading = true
  } else {
    ypatState.loadingMore = true
  }
  try {
    const result = await ypatApi.getMyFavoriteList({ userid, page: ypatState.page, size })
    const content = result.data?.content || []
    ypatState.items = refresh ? content : ypatState.items.concat(content)
    const current = result.data?.number ?? ypatState.page
    ypatState.hasMore = current + 1 < (result.data?.totalPages || 0)
    ypatState.page = current + 1
    ypatState.loaded = true
  } catch (error) {
    uni.showToast({ title: error instanceof Error ? error.message : '约拍收藏加载失败', icon: 'none' })
  } finally {
    ypatState.loading = false
    ypatState.loadingMore = false
  }
}

async function loadWork(refresh = false): Promise<void> {
  if (workState.loading || workState.loadingMore || (!refresh && !workState.hasMore)) return
  if (refresh) {
    workState.page = 1
    workState.hasMore = true
    workState.loading = true
  } else {
    workState.loadingMore = true
  }
  try {
    const result = await getFavoriteWorks({ page: workState.page, size })
    const content = result.data?.items || []
    workState.items = refresh ? content : workState.items.concat(content)
    const current = result.data?.page ?? workState.page
    workState.hasMore = result.data?.hasMore ?? workState.items.length < (result.data?.total || 0)
    workState.page = current + 1
    workState.loaded = true
  } catch (error) {
    uni.showToast({ title: error instanceof Error ? error.message : '作品收藏加载失败', icon: 'none' })
  } finally {
    workState.loading = false
    workState.loadingMore = false
  }
}

function loadCurrent(refresh = false): Promise<void> {
  return activeTab.value === 'ypat' ? loadYpat(refresh) : loadWork(refresh)
}

function switchTab(tab: FavoriteTab): void {
  if (activeTab.value === tab) return
  activeTab.value = tab
  const loaded = tab === 'ypat' ? ypatState.loaded : workState.loaded
  if (!loaded) void loadCurrent(true)
}

function getChargeLabel(chargeway: string, chargeamt?: number): string {
  if (Number(chargeamt || 0) > 0) return `¥${chargeamt}`
  return CHARGE_WAY_LABELS[chargeway] || '费用协商'
}
function getProfessionLabel(value?: string): string { return value ? getProfessLabel(value) : '摄影爱好者' }
function formatTime(value?: string): string {
  if (!value) return ''
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return value
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`
}
function openYpat(id: number): void { uni.navigateTo({ url: `/pages-sub/ypat/detail?id=${id}` }) }
function openWork(id: number): void { uni.navigateTo({ url: `/pages-sub/work/detail?id=${id}` }) }

onShow(() => { void loadCurrent(true) })
onPullDownRefresh(async () => { await loadCurrent(true); uni.stopPullDownRefresh() })
onReachBottom(() => { void loadCurrent() })
</script>
```

- [ ] **Step 3: 使用主题变量实现 B 方案样式**

```scss
<style scoped lang="scss">
.favorite-page { min-height: 100vh; background: $color-bg-page; }
.favorite-tabs {
  display: flex;
  height: 92rpx;
  padding: 0 28rpx;
  border-bottom: 1rpx solid $color-border;
  background: $color-bg-card;
}
.favorite-tabs__item {
  position: relative;
  @include flex-center;
  flex: 1;
  color: $color-text-helper;
  font-size: 28rpx;
  font-weight: 700;
}
.favorite-tabs__item--active { color: $color-primary-dark; font-weight: 900; }
.favorite-tabs__item--active::after {
  position: absolute;
  right: 30%;
  bottom: 0;
  left: 30%;
  height: 6rpx;
  border-radius: 6rpx 6rpx 0 0;
  background: $color-primary;
  content: '';
}
.favorite-content { padding: 24rpx 28rpx 40rpx; }
.favorite-state { padding: 140rpx 20rpx; color: $color-text-helper; text-align: center; }
.favorite-card {
  display: flex;
  gap: 22rpx;
  margin-bottom: 20rpx;
  padding: 20rpx;
  overflow: hidden;
  border: 1rpx solid $color-border;
  border-radius: $radius-keep-card;
  background: $color-bg-card;
  box-shadow: 0 8rpx 24rpx rgba(31, 55, 40, 0.06);
}
.favorite-card__cover-wrap { position: relative; width: 196rpx; height: 216rpx; flex: none; }
.favorite-card__cover { width: 196rpx; height: 216rpx; flex: none; border-radius: 18rpx; background: $color-bg-chip; }
.favorite-card__video { position: absolute; top: 10rpx; right: 10rpx; padding: 4rpx 12rpx; border-radius: 10rpx; color: #fff; background: rgba(0, 0, 0, 0.55); font-size: 20rpx; }
.favorite-card__body { min-width: 0; flex: 1; padding: 4rpx 0; }
.favorite-card__header, .favorite-card__meta { display: flex; align-items: center; justify-content: space-between; gap: 12rpx; }
.favorite-card__name { min-width: 0; overflow: hidden; color: $color-text-primary; font-weight: 900; text-overflow: ellipsis; white-space: nowrap; }
.favorite-card__time { flex: none; color: $color-text-helper; font-size: 22rpx; font-weight: 700; }
.favorite-card__tags { display: flex; gap: 8rpx; overflow: hidden; }
.favorite-card__tag { padding: 4rpx 12rpx; border-radius: 8rpx; color: $color-primary-dark; background: $color-primary-light; font-size: 20rpx; font-weight: 800; white-space: nowrap; }
.favorite-card__desc { display: -webkit-box; margin: 16rpx 0; overflow: hidden; color: $color-text-secondary; font-size: 27rpx; font-weight: 800; line-height: 1.45; -webkit-box-orient: vertical; -webkit-line-clamp: 2; }
.favorite-card__author { display: block; overflow: hidden; color: $color-text-helper; font-size: 22rpx; text-overflow: ellipsis; white-space: nowrap; }
.favorite-card__meta { margin-top: 16rpx; color: $color-text-helper; font-size: 22rpx; font-weight: 700; }
.favorite-card__count { display: inline-flex; align-items: center; gap: 6rpx; color: $color-primary-dark; }
.favorite-footer { padding: 28rpx; color: $color-text-helper; text-align: center; }
</style>
```

- [ ] **Step 4: 检查页面静态规范**

Run（运行）:

```bash
frontend/node_modules/.bin/eslint \
  frontend/src/api/modules/work.ts \
  frontend/src/api/types/work.ts \
  frontend/src/pages-sub/ypat/my-favorite.vue \
  --quiet
```

Expected（预期）: 退出码为 0，无 ESLint（代码规范检查）错误。

- [ ] **Step 5: 提交前端改动**

```bash
git add frontend/src/api/modules/work.ts frontend/src/api/types/work.ts frontend/src/pages-sub/ypat/my-favorite.vue
git commit -m "feat: 分类展示约拍与作品收藏" \
  -m "在我的收藏页增加主题色双标签与独立分页状态，并按需加载当前用户收藏的作品。" \
  -m "Constraint: 默认展示约拍，作品首次切换时加载。" \
  -m "Tested: ESLint 与差异格式检查通过。" \
  -m "Co-authored-by: OmX <omx@oh-my-codex.dev>"
```

### Task 4: 最终静态验证与手工验收说明

**Files（文件）:**
- Verify（验证）: Task 1 至 Task 3 的全部修改文件。

- [ ] **Step 1: 执行差异格式检查**

```bash
git -c core.whitespace=cr-at-eol diff --check HEAD~2..HEAD
```

Expected（预期）: 退出码为 0，无多余空白或冲突标记。

- [ ] **Step 2: 静态核对接口链路**

```bash
rg -n 'getFavoriteWorks|/work/favorites|/service/work/favorites|favoriteWorks' \
  frontend/src/api/modules/work.ts \
  frontend/src/api/types/work.ts \
  frontend/src/pages-sub/ypat/my-favorite.vue \
  backend/system-wap/src/main/java/com/ypat/controller/WorkController.java \
  backend/system-wap/src/main/java/com/ypat/service/WorkServiceClient.java \
  backend/system-restapi/src/main/java/com/ypat/controller/WorkController.java \
  backend/system-domain/src/main/java/com/ypat/service/WorkService.java
```

Expected（预期）: 前端、WAP（网页服务）、Feign（声明式服务调用）、REST API（接口服务）和领域服务路径连续完整。

- [ ] **Step 3: 提供手工验收清单**

手工检查以下场景：

1. 首次进入默认选中“约拍”，并显示现有约拍收藏。
2. 切换“作品”后只发起一次首次请求，并按收藏时间倒序展示作品。
3. 两类 tab（标签页）分别支持下拉刷新与触底分页。
4. 两类空列表分别显示准确文案。
5. 点击约拍进入 `/pages-sub/ypat/detail`，点击作品进入 `/pages-sub/work/detail`。
6. 从详情取消收藏返回后，当前 tab（标签页）自动刷新并移除对应卡片。
7. 接口失败时显示对应类型错误提示，已有列表不被清空。
8. tab（标签页）激活文字、指示线、作品标签和收藏量均使用主题色。

不运行依赖安装、前端构建、后端构建或自动化测试。
