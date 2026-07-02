<template>
  <view class="work-tab">
    <KeepPageNav title="作品" />
    <scroll-view class="work-tab__categories" scroll-x>
      <view class="work-tab__categories-row">
        <view v-for="cat in categories" :key="cat.value"
              :class="['work-tab__cat', { 'work-tab__cat--active': activeCategory === cat.value }]"
              @tap="onCategoryChange(cat.value)">
          <text :class="{ 'work-tab__cat-text--active': activeCategory === cat.value }">{{ cat.label }}</text>
        </view>
        <view class="work-tab__cat work-tab__cat--filter" @tap="filterVisible = true">
          <KeepIcon name="sliders" :size="20" :color="filterCount > 0 ? '#23C268' : '#83888F'" />
          <text :class="{ 'work-tab__cat-text--active': filterCount > 0 }">筛选</text>
          <text v-if="filterCount > 0" class="work-tab__filter-badge">{{ filterCount }}</text>
        </view>
      </view>
    </scroll-view>

    <view class="work-tab__list">
      <view class="work-tab__columns">
        <view class="work-tab__column">
          <view v-for="(item, i) in leftItems" :key="`l-${item.id || i}`" class="work-tab__col-item">
            <WorkCard :work="item" @tap="goDetail(item)" />
          </view>
        </view>
        <view class="work-tab__column">
          <view v-for="(item, i) in rightItems" :key="`r-${item.id || i}`" class="work-tab__col-item">
            <WorkCard :work="item" @tap="goDetail(item)" />
          </view>
        </view>
      </view>
      <WorkListSkeleton v-if="loading && items.length === 0" />
      <view v-else-if="loading" class="work-tab__loading">加载中...</view>
      <view v-else-if="!hasMore && items.length === 0" class="work-tab__empty">暂无作品</view>
      <view v-else-if="!hasMore" class="work-tab__nomore">— 已加载全部 —</view>
    </view>

    <WorkFilterPanel v-model:visible="filterVisible" v-model="filterValue" @confirm="onFilterConfirm" @reset="onFilterReset" />
    <KeepTabBar active="work" />
  </view>
</template>

<script setup lang="ts">
import { computed, onMounted, ref, shallowRef } from 'vue'
import KeepPageNav from '@/components/business/KeepPageNav.vue'
import KeepTabBar from '@/components/business/KeepTabBar.vue'
import WorkCard from '@/components/business/WorkCard.vue'
import WorkFilterPanel from '@/components/business/WorkFilterPanel.vue'
import WorkListSkeleton from '@/components/business/WorkListSkeleton.vue'
import { getList } from '@/api/modules/work'
import { useUserStore } from '@/stores/user'
import type { WorkListItem, WorkListResult } from '@/api/types/work'
import KeepIcon from '@/components/business/KeepIcon.vue'

const userStore = useUserStore()

const categories = [
  { value: '', label: '推荐' },
  { value: '同城', label: '同城' },
  { value: '模特', label: '模特' },
  { value: '摄影', label: '摄影师' },
  { value: '化妆', label: '化妆师' },
  { value: '修图', label: '修图师' },
]
const activeCategory = ref('')
const filterVisible = ref(false)
const filterValue = ref({ region: '', gender: '', profession: '' })

// 大数组用 shallowRef 避免深响应（不递归代理），
// 提升瀑布流卡片渲染性能
const items = shallowRef<WorkListItem[]>([])
const page = ref(1)
const pageSize = 20
const total = ref(0)
const loading = ref(false)
const hasMore = ref(true)

const filterCount = computed(() => {
  let n = 0
  if (filterValue.value.region) n++
  if (filterValue.value.gender) n++
  if (filterValue.value.profession) n++
  return n
})

// 简化分配：奇偶交替
const leftItems = computed(() => items.value.filter((_, i) => i % 2 === 0))
const rightItems = computed(() => items.value.filter((_, i) => i % 2 === 1))

async function load(reset = false) {
  if (loading.value) return
  loading.value = true
  try {
    const params: any = {
      page: reset ? 1 : page.value,
      size: pageSize,
    }
    if (activeCategory.value) params.category = activeCategory.value
    if (filterValue.value.gender) params.gender = filterValue.value.gender
    if (filterValue.value.profession) params.profession = filterValue.value.profession
    if (filterValue.value.region === 'current' && userStore.userInfo?.city) {
      params.city = userStore.userInfo.city
    }
    const res = await getList(params)
    const data: WorkListResult = (res && res.data) || { page: 1, size: 0, total: 0, items: [] }
    if (reset) {
      items.value = data.items
      page.value = 2
    } else {
      items.value = items.value.concat(data.items)
      page.value++
    }
    total.value = data.total
    hasMore.value = items.value.length < data.total
  } catch (e) {
    uni.showToast({ title: '加载失败', icon: 'none' })
  } finally {
    loading.value = false
  }
}

function onCategoryChange(cat: string) {
  if (activeCategory.value === cat) return
  activeCategory.value = cat
  load(true)
}
function onFilterConfirm() { load(true) }
function onFilterReset() {
  filterValue.value = { region: '', gender: '', profession: '' }
  load(true)
}
function goDetail(item: WorkListItem) {
  uni.navigateTo({ url: `/pages-sub/work/detail?id=${item.id}` })
}

onMounted(() => load(true))
</script>

<style lang="scss" scoped>
.work-tab {
  min-height: 100vh;
  background: #FFFFFF;
  padding-bottom: calc(148rpx + env(safe-area-inset-bottom));
  &__categories {
    width: 100%;
    white-space: nowrap;
    background: #FFFFFF;
    border-bottom: 2rpx solid $color-border;
  }
  &__categories-row {
    display: flex;
    align-items: center;
    min-width: 100%;
    padding: 16rpx 32rpx;
    gap: 16rpx;
  }
  &__cat {
    flex: none;
    padding: 8rpx 0;
    position: relative;
    &--active {
      font-weight: 600;
    }
    &--filter {
      margin-left: auto;
      display: flex;
      align-items: center;
      gap: 4rpx;
    }
  }
  &__cat-text--active {
    color: $color-primary;
    &::after {
      content: '';
      position: absolute;
      bottom: 0;
      left: 50%;
      transform: translateX(-50%);
      width: 32rpx;
      height: 4rpx;
      background: $color-primary;
      border-radius: 2rpx;
    }
  }
  &__filter-icon {
    font-size: 28rpx;
  }
  &__filter-badge {
    background: $color-primary;
    color: #FFFFFF;
    font-size: 20rpx;
    padding: 0 8rpx;
    border-radius: 999rpx;
    min-width: 28rpx;
    text-align: center;
  }
  &__list { padding: 16rpx 24rpx; }
  &__columns {
    display: flex;
    gap: 16rpx;
  }
  &__column {
    flex: 1;
    min-width: 0;
  }
  &__col-item {
    margin-bottom: 16rpx;
  }
  &__loading, &__empty, &__nomore {
    text-align: center;
    padding: 32rpx 0;
    color: $color-text-helper;
    font-size: 24rpx;
  }
}
</style>
