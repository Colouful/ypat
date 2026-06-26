<template>
  <view class="home-page">
    <view class="home-scroll" :style="{ paddingTop: statusBarHeight + 24 + 'px' }">
      <view class="home-top">
        <view class="home-search" @tap="goSearch">
          <KeepIcon name="search" :size="46" color="#B3B8BE" />
          <text class="home-search__placeholder">搜索摄影师 / 风格 / 城市</text>
        </view>
        <view class="home-ai" @tap="goDiscover">
          <KeepIcon name="sparkles" :size="34" color="#1B1E23" />
        </view>
      </view>

      <view class="quick-grid">
        <view v-for="item in quickItems" :key="item.label" class="quick-grid__item" @tap="handleQuickItem(item.value)">
          <view class="quick-grid__icon">
            <KeepIcon :name="item.icon" :size="50" />
          </view>
          <text class="quick-grid__label">{{ item.label }}</text>
        </view>
      </view>

      <view class="home-tabs">
        <view
          v-for="tabItem in tabs"
          :key="tabItem.key"
          class="home-tabs__item"
          :class="{ 'home-tabs__item--active': activeTab === tabItem.key }"
          @tap="switchTab(tabItem.key)"
        >
          <text>{{ tabItem.label }}</text>
        </view>
        <view class="home-tabs__filter" @tap="filterVisible = true">
          <text>筛选</text>
          <KeepIcon name="sliders" :size="24" />
        </view>
      </view>

      <scroll-view class="chip-scroll" scroll-x>
        <view class="chip-row">
          <view
            v-for="chip in quickChips"
            :key="chip.value"
            class="chip-row__item"
            :class="{ 'chip-row__item--active': activeChip === chip.value }"
            @tap="pickChip(chip.value)"
          >
            {{ chip.label }}
          </view>
        </view>
      </scroll-view>

      <view v-if="loading && list.length === 0" class="home-loading">
        <view v-for="index in 3" :key="index" class="home-skeleton" />
      </view>

      <KeepState
        v-else-if="list.length === 0 && !loading"
        type="empty"
        title="暂无约拍信息"
        description="换个筛选条件，或者下拉刷新试试"
      />

      <view v-else class="home-list">
        <KeepYpatCard
          v-for="item in cardItems"
          :key="item.id"
          :item="item"
          @tap="goDetail(item.id || 0)"
        />
      </view>

      <view v-if="!hasMore && list.length > 0" class="load-end">
        <text class="load-end__text">没有更多了</text>
      </view>
    </view>

    <KeepTabBar active="home" :unread-count="unreadCount" />

    <KeepFilterSheet
      v-model:visible="filterVisible"
      v-model="filterValue"
      :groups="filterGroups"
      :count="86"
      @reset="resetFilter"
      @confirm="applyFilter"
    />
  </view>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { onPullDownRefresh, onReachBottom, onShow } from '@dcloudio/uni-app'
import { useUserStore } from '@/stores/user'
import { useAppStore } from '@/stores/app'
import * as ypatApi from '@/api/modules/ypat'
import { CHARGE_WAY_LABELS, PHOTO_STYLES, TARGET_LABELS } from '@/constants/enums'
import KeepFilterSheet, { type KeepFilterGroup } from '@/components/business/KeepFilterSheet.vue'
import KeepIcon from '@/components/business/KeepIcon.vue'
import KeepState from '@/components/business/KeepState.vue'
import KeepTabBar from '@/components/business/KeepTabBar.vue'
import KeepYpatCard, { type KeepYpatCardItem } from '@/components/business/KeepYpatCard.vue'
import type { YpatInfo } from '@/api/types/index'

type TabKey = 'recommend' | 'nearby' | 'latest'
type FilterValue = Record<string, string[]>

const userStore = useUserStore()
const appStore = useAppStore()

const statusBarHeight = computed(() => appStore.statusBarHeight)
const unreadCount = computed(() => userStore.unreadCount)

const currentCity = ref('')
const activeTab = ref<TabKey>('latest')
const activeChip = ref('all')
const loading = ref(false)
const list = ref<YpatInfo[]>([])
const page = ref(0)
const hasMore = ref(true)
const filterVisible = ref(false)
const filterValue = ref<FilterValue>({
  target: ['all'],
  chargeway: ['0'],
  style: ['INS'],
})

const tabs: Array<{ key: TabKey; label: string }> = [
  { key: 'recommend', label: '推荐' },
  { key: 'nearby', label: '同城' },
  { key: 'latest', label: '最新' },
]

const quickItems = [
  { label: '约摄影师', icon: 'camera', value: 'photographer' },
  { label: '约模特', icon: 'user', value: 'model' },
  { label: '妆造师', icon: 'edit', value: '妆造师' },
  { label: '修图师', icon: 'image', value: '修图师' },
  { label: '找同城', icon: 'map-pin', value: 'nearby' },
]

const quickChips = [
  { label: '全部', value: 'all' },
  { label: '希望互免', value: 'free' },
  { label: '可付费', value: 'pay' },
  { label: '约摄影师', value: 'photographer' },
  { label: '约模特', value: 'model' },
  { label: 'INS', value: 'INS' },
  { label: '胶片', value: '胶片' },
  { label: '情绪', value: '情绪' },
]

const filterGroups: KeepFilterGroup[] = [
  {
    key: 'target',
    title: '我想找',
    multiple: false,
    options: [
      { label: '全部', value: 'all' },
      { label: '约摄影师', value: '0' },
      { label: '约模特', value: '1' },
    ],
  },
  {
    key: 'chargeway',
    title: '合作方式',
    multiple: true,
    options: [
      { label: '希望互免', value: '0' },
      { label: '我要收费', value: '1' },
      { label: '可付费', value: '2' },
      { label: '费用协商', value: '3' },
    ],
  },
  {
    key: 'style',
    title: '风格偏好',
    multiple: true,
    options: PHOTO_STYLES.slice(0, 6).map((style) => ({ label: style, value: style })),
  },
]

const cardItems = computed<KeepYpatCardItem[]>(() => list.value.map((item) => ({
  id: item.id,
  title: item.describ || item.targetTxt || '约拍需求',
  targetLabel: item.targetTxt || TARGET_LABELS[item.target] || '约拍',
  chargeLabel: item.chargewayTxt || CHARGE_WAY_LABELS[item.chargeway] || '费用协商',
  city: [item.city, item.area].filter(Boolean).join('·') || '同城',
  name: item.userQo?.nickname || '匿名用户',
  image: item.pics?.[0] || '/static/default-cover.png',
  avatar: item.userQo?.imgpath || item.userQo?.avatarurl || '/static/default-avatar.png',
  time: item.timeStr || item.pubdate || '刚刚',
  applyCount: item.pattimes || item.readtimes || 0,
  realname: item.realnameflag === '1' || item.userQo?.realnameflag === '1',
  credit: item.creditflag === '1' || item.userQo?.creditflag === '1',
})))

function buildParams() {
  const target = filterValue.value.target?.find((value) => value !== 'all')
  const chargeway = filterValue.value.chargeway?.[0]
  const style = filterValue.value.style?.[0]
  return {
    page: page.value,
    size: 10,
    status: 'shtg',
    ...(activeTab.value === 'recommend' ? { recomflag: '1' } : {}),
    ...(activeTab.value === 'nearby' && currentCity.value && currentCity.value !== '全国' ? { city: currentCity.value } : {}),
    ...(target ? { target } : {}),
    ...(chargeway ? { chargeway } : {}),
    ...(style ? { patstyle: style } : {}),
  }
}

async function loadList(refresh = false) {
  if (loading.value) return
  if (refresh) {
    page.value = 0
    hasMore.value = true
  }
  if (!hasMore.value) return

  loading.value = true
  try {
    const params = buildParams()
    const res = activeTab.value === 'latest'
      ? await ypatApi.getLatestList(params)
      : await ypatApi.getRecommendList(params)

    if (res.success) {
      const content = res.data.content || []
      list.value = refresh ? content : list.value.concat(content)
      hasMore.value = content.length >= 10
      page.value++
    }
  } catch (error) {
    uni.showToast({ title: error instanceof Error ? error.message : '约拍加载失败', icon: 'none' })
  } finally {
    loading.value = false
  }
}

function switchTab(key: TabKey) {
  if (activeTab.value === key) return
  activeTab.value = key
  list.value = []
  loadList(true)
}

function pickChip(value: string) {
  activeChip.value = value
  if (value === 'all') filterValue.value = { ...filterValue.value, target: ['all'], style: [], chargeway: [] }
  if (value === 'free') filterValue.value = { ...filterValue.value, chargeway: ['0'] }
  if (value === 'pay') filterValue.value = { ...filterValue.value, chargeway: ['2'] }
  if (value === 'photographer') filterValue.value = { ...filterValue.value, target: ['0'] }
  if (value === 'model') filterValue.value = { ...filterValue.value, target: ['1'] }
  if (PHOTO_STYLES.includes(value)) filterValue.value = { ...filterValue.value, style: [value] }
  loadList(true)
}

function handleQuickItem(value: string) {
  if (value === 'photographer' || value === 'model') {
    pickChip(value)
    return
  }
  if (value === 'nearby') {
    activeTab.value = 'nearby'
    activeChip.value = 'all'
    filterValue.value = { ...filterValue.value, target: ['all'], style: [], chargeway: [] }
    loadList(true)
    return
  }
  uni.navigateTo({ url: `/pages-sub/ypat/search?keyword=${encodeURIComponent(value)}` })
}

function applyFilter(value: FilterValue) {
  filterValue.value = value
  loadList(true)
}

function resetFilter() {
  filterValue.value = { target: ['all'], chargeway: [], style: [] }
  activeChip.value = 'all'
  loadList(true)
}

function goDetail(id: number) {
  if (!id) return
  uni.navigateTo({ url: `/pages-sub/ypat/detail?id=${id}` })
}

function goSearch() {
  uni.navigateTo({ url: '/pages-sub/ypat/search' })
}

function goDiscover() {
  uni.navigateTo({ url: '/pages/discover/index' })
}

async function getLocation() {
  try {
    await new Promise<UniApp.GetLocationSuccess>((resolve, reject) => {
      uni.getLocation({
        type: 'gcj02',
        success: resolve,
        fail: reject,
      })
    })
    currentCity.value = '全国'
  } catch (_) {
    currentCity.value = '全国'
  }
}

onMounted(() => {
  loadList(true)
  getLocation()
})

onShow(() => {
  try { uni.hideTabBar({ animation: false }) } catch(e) {}
  if (userStore.isLoggedIn) {
    userStore.refreshUnreadCount()
  }
})

onPullDownRefresh(() => {
  loadList(true).finally(() => {
    uni.stopPullDownRefresh()
  })
})

onReachBottom(() => {
  loadList()
})
</script>

<style lang="scss">
@import '@/styles/tokens.scss';
@import '@/styles/mixins.scss';

.home-page {
  min-height: 100vh;
  background: $color-bg-page;
}

.home-scroll {
  min-height: 100vh;
  padding-bottom: calc(168rpx + env(safe-area-inset-bottom));
}

.home-top {
  display: flex;
  align-items: center;
  gap: 22rpx;
  padding: 0 36rpx;
}

.home-search {
  display: flex;
  align-items: center;
  flex: 1;
  height: 84rpx;
  padding: 0 28rpx;
  border-radius: 42rpx;
  background: $color-bg-chip;
}

.home-search__placeholder {
  margin-left: 16rpx;
  color: $color-text-helper;
  font-size: 28rpx;
  font-weight: 700;
}

.home-ai {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 68rpx;
  height: 68rpx;
  border-radius: 50%;
  background: conic-gradient(from 90deg, #7C5CFF, #23C268, #FF9F1C, #7C5CFF);
}

.home-ai__spark {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 52rpx;
  height: 52rpx;
  border-radius: 50%;
  color: $color-text-primary;
  background: #fff;
  font-size: 30rpx;
  font-weight: 800;
}

.quick-grid {
  display: flex;
  justify-content: space-between;
  padding: 36rpx 32rpx 20rpx;
}

.quick-grid__item {
  flex: 1;
  text-align: center;
}

.quick-grid__icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 104rpx;
  height: 104rpx;
  margin: 0 auto 14rpx;
  border-radius: 32rpx;
  color: $color-text-primary;
  background: #fff;
  box-shadow: $shadow-keep-card;
}

.quick-grid__label {
  color: $color-text-secondary;
  font-size: 24rpx;
  font-weight: 800;
}

.home-tabs {
  position: sticky;
  top: 0;
  z-index: 10;
  display: flex;
  align-items: center;
  gap: 44rpx;
  padding: 32rpx 40rpx 8rpx;
  background: $color-bg-page;
}

.home-tabs__item {
  position: relative;
  padding-bottom: 18rpx;
  color: $color-text-helper;
  font-size: 34rpx;
  font-weight: 800;
}

.home-tabs__item--active {
  color: $color-text-primary;
  font-size: 42rpx;
}

.home-tabs__item--active::after {
  position: absolute;
  right: 0;
  bottom: 4rpx;
  left: 0;
  height: 14rpx;
  border-radius: 8rpx;
  background: $color-primary-light;
  content: '';
}

.home-tabs__item text {
  position: relative;
  z-index: 1;
}

.home-tabs__filter {
  display: flex;
  align-items: center;
  gap: 6rpx;
  margin-left: auto;
  color: $color-text-secondary;
  font-size: 26rpx;
  font-weight: 800;
}

.chip-scroll {
  width: 100%;
  white-space: nowrap;
}

.chip-row {
  display: flex;
  gap: 16rpx;
  padding: 20rpx 32rpx 24rpx;
}

.chip-row__item {
  @include keep-chip;
  flex: none;
}

.chip-row__item--active {
  @include keep-chip(true);
}

.home-list {
  padding: 0 32rpx;
}

.home-loading {
  padding: 0 32rpx;
}

.home-skeleton {
  height: 308rpx;
  margin-bottom: 22rpx;
  border-radius: $radius-keep-card;
  background: linear-gradient(90deg, #f0f1f3 25%, #e7e9ec 50%, #f0f1f3 75%);
  background-size: 200% 100%;
  animation: homeShimmer 1.4s infinite;
}

@keyframes homeShimmer {
  0% { background-position: -200% 0; }
  100% { background-position: 200% 0; }
}

.load-end {
  display: flex;
  justify-content: center;
  padding: 28rpx 0 10rpx;
}

.load-end__text {
  color: $color-text-helper;
  font-size: 24rpx;
  font-weight: 700;
}

</style>
