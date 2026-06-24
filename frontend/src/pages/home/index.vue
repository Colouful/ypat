<template>
  <view class="home-page">
    <view class="navbar" :style="{ paddingTop: statusBarHeight + 'px' }">
      <view class="navbar__content">
        <view class="navbar__city" @tap="handleCityTap">
          <text class="navbar__city-text">{{ currentCity || '定位中' }}</text>
          <text class="navbar__city-arrow">▼</text>
        </view>
        <view class="navbar__search" @tap="goSearch">
          <text class="navbar__search-icon">🔍</text>
          <text class="navbar__search-placeholder">搜索约拍</text>
        </view>
        <view class="navbar__message" @tap="goMessage">
          <text class="navbar__message-icon">✉</text>
          <view v-if="unreadCount > 0" class="navbar__badge">
            <text class="navbar__badge-text">{{ unreadCount > 99 ? '99+' : unreadCount }}</text>
          </view>
        </view>
      </view>
    </view>

    <view class="home-content" :style="{ paddingTop: navHeight + 'px' }">
      <view v-if="bannerList.length > 0" class="banner-section">
        <swiper class="banner-swiper" circular autoplay :interval="4000" indicator-dots indicator-color="rgba(255,255,255,0.4)" indicator-active-color="#23C268">
          <swiper-item v-for="banner in bannerList" :key="banner.id">
            <image class="banner-image" :src="banner.imgpath" mode="aspectFill" />
          </swiper-item>
        </swiper>
      </view>

      <view class="tabs-section">
        <view v-for="tab in tabs" :key="tab.key" class="tab-item" :class="{ 'tab-item--active': activeTab === tab.key }" @tap="switchTab(tab.key)">
          <text class="tab-item__text">{{ tab.label }}</text>
          <view v-if="activeTab === tab.key" class="tab-item__indicator" />
        </view>
      </view>

      <view v-if="loading && list.length === 0" class="skeleton-list">
        <view v-for="i in 3" :key="i" class="skeleton-card">
          <view class="skeleton-image" />
          <view class="skeleton-info">
            <view class="skeleton-line skeleton-line--title" />
            <view class="skeleton-line skeleton-line--desc" />
            <view class="skeleton-line skeleton-line--short" />
          </view>
        </view>
      </view>

      <view v-else-if="list.length === 0 && !loading" class="empty-state">
        <text class="empty-state__text">暂无约拍信息</text>
        <text class="empty-state__sub">下拉刷新试试</text>
      </view>

      <view v-else class="ypat-list">
        <view v-for="item in list" :key="item.id" class="ypat-card" @tap="goDetail(item.id)">
          <image v-if="item.pics && item.pics.length > 0" class="ypat-card__image" :src="item.pics[0]" mode="aspectFill" lazy-load />
          <view class="ypat-card__content">
            <view class="ypat-card__header">
              <image v-if="item.userQo?.imgpath" class="ypat-card__avatar" :src="item.userQo.imgpath" mode="aspectFill" />
              <view class="ypat-card__user">
                <text class="ypat-card__nickname">{{ item.userQo?.nickname || '匿名用户' }}</text>
                <text class="ypat-card__profess">{{ getProfessLabel(item.userQo?.profess) }}</text>
              </view>
              <text class="ypat-card__time">{{ item.timeStr }}</text>
            </view>
            <text class="ypat-card__desc">{{ item.describ }}</text>
            <view class="ypat-card__footer">
              <view class="ypat-card__tags">
                <text v-if="item.city" class="ypat-card__tag">{{ item.city }}</text>
                <text v-if="item.chargewayTxt" class="ypat-card__tag ypat-card__tag--charge">{{ item.chargewayTxt }}</text>
                <text v-if="item.patstyleTxt" class="ypat-card__tag">{{ item.patstyleTxt }}</text>
              </view>
              <view class="ypat-card__stats">
                <text class="ypat-card__stat">{{ item.readtimes || 0 }}阅读</text>
              </view>
            </view>
          </view>
        </view>
      </view>

      <view v-if="!hasMore && list.length > 0" class="load-end">
        <text class="load-end__text">没有更多了</text>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { onPullDownRefresh, onReachBottom, onShow } from '@dcloudio/uni-app'
import { useUserStore } from '@/stores/user'
import { useAppStore } from '@/stores/app'
import * as ypatApi from '@/api/modules/ypat'
import * as contentApi from '@/api/modules/content'
import { PROFESS_LABELS } from '@/constants/enums'
import type { YpatInfo, Banner } from '@/api/types/index'

const userStore = useUserStore()
const appStore = useAppStore()

const statusBarHeight = computed(() => appStore.statusBarHeight)
const navHeight = computed(() => appStore.statusBarHeight + 44)
const unreadCount = computed(() => userStore.unreadCount)

const currentCity = ref('')
const activeTab = ref('recommend')
const loading = ref(false)
const list = ref<YpatInfo[]>([])
const bannerList = ref<Banner[]>([])
const page = ref(0)
const hasMore = ref(true)

const tabs = [
  { key: 'recommend', label: '推荐' },
  { key: 'latest', label: '最新' },
  { key: 'nearby', label: '同城' },
]

function getProfessLabel(code?: string): string {
  if (!code) return ''
  return PROFESS_LABELS[code] || ''
}

async function loadBanners() {
  try {
    const res = await contentApi.getBannerList({ page: 0, size: 5, status: '1' } as any)
    if (res.success && res.data) {
      bannerList.value = res.data.content || []
    }
  } catch (_) {}
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
    const params = {
      page: page.value,
      size: 10,
      status: 'shtg',
      ...(activeTab.value === 'recommend' ? { recomflag: '1' } : {}),
      ...(activeTab.value === 'nearby' && currentCity.value ? { city: currentCity.value } : {}),
    }
    const res = activeTab.value === 'recommend'
      ? await ypatApi.getRecommendList(params)
      : await ypatApi.getLatestList(params)

    if (res.success) {
      const content = res.data.content || []
      if (refresh) {
        list.value = content
      } else {
        list.value.push(...content)
      }
      hasMore.value = content.length >= 10
      page.value++
    }
  } catch (_) {
  } finally {
    loading.value = false
  }
}

function switchTab(key: string) {
  if (activeTab.value === key) return
  activeTab.value = key
  list.value = []
  loadList(true)
}

function goDetail(id: number) {
  uni.navigateTo({ url: `/pages-sub/ypat/detail?id=${id}` })
}

function goSearch() {
  uni.navigateTo({ url: '/pages-sub/ypat/search' })
}

function goMessage() {
  uni.switchTab({ url: '/pages/message/index' })
}

function handleCityTap() {
  getLocation()
}

async function getLocation() {
  try {
    const res = await new Promise<UniApp.GetLocationSuccess>((resolve, reject) => {
      uni.getLocation({
        type: 'gcj02',
        success: resolve,
        fail: reject,
      })
    })
    currentCity.value = '定位中'
    // In real app, would reverse-geocode to city name
    currentCity.value = '全国'
  } catch (_) {
    currentCity.value = '全国'
  }
}

onMounted(() => {
  loadBanners()
  loadList(true)
  getLocation()
})

onShow(() => {
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

.home-page {
  min-height: 100vh;
  background-color: $color-bg-page;
}

.navbar {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: $z-index-navbar;
  background-color: #fff;

  &__content {
    display: flex;
    align-items: center;
    height: 88rpx;
    padding: 0 $spacing-md;
  }

  &__city {
    display: flex;
    align-items: center;
    margin-right: $spacing-sm;
  }

  &__city-text {
    font-size: $font-size-base;
    font-weight: $font-weight-medium;
    color: $color-text-primary;
  }

  &__city-arrow {
    font-size: $font-size-xs;
    color: $color-text-secondary;
    margin-left: 4rpx;
  }

  &__search {
    flex: 1;
    display: flex;
    align-items: center;
    height: 64rpx;
    background-color: $color-bg-page;
    border-radius: $radius-round;
    padding: 0 $spacing-md;
  }

  &__search-icon {
    font-size: $font-size-sm;
    margin-right: $spacing-xs;
  }

  &__search-placeholder {
    font-size: $font-size-sm;
    color: $color-text-helper;
  }

  &__message {
    position: relative;
    margin-left: $spacing-md;
    padding: $spacing-xs;
  }

  &__message-icon {
    font-size: $font-size-xl;
  }

  &__badge {
    position: absolute;
    top: 0;
    right: 0;
    min-width: 32rpx;
    height: 32rpx;
    background-color: $color-accent-red;
    border-radius: $radius-round;
    display: flex;
    align-items: center;
    justify-content: center;
    padding: 0 8rpx;
  }

  &__badge-text {
    font-size: 20rpx;
    color: #fff;
    line-height: 1;
  }
}

.home-content {
  padding-bottom: calc(env(safe-area-inset-bottom) + 100rpx);
}

.banner-section {
  padding: $spacing-md;
}

.banner-swiper {
  height: 300rpx;
  border-radius: $radius-md;
  overflow: hidden;
}

.banner-image {
  width: 100%;
  height: 100%;
}

.tabs-section {
  display: flex;
  align-items: center;
  padding: $spacing-sm $spacing-md;
  background-color: #fff;
}

.tab-item {
  position: relative;
  padding: $spacing-sm $spacing-md;

  &__text {
    font-size: $font-size-base;
    color: $color-text-secondary;
  }

  &--active &__text {
    color: $color-primary;
    font-weight: $font-weight-semibold;
  }

  &__indicator {
    position: absolute;
    bottom: 0;
    left: 50%;
    transform: translateX(-50%);
    width: 40rpx;
    height: 6rpx;
    background-color: $color-primary;
    border-radius: 3rpx;
  }
}

.ypat-list {
  padding: $spacing-sm $spacing-md;
}

.ypat-card {
  background-color: #fff;
  border-radius: $radius-md;
  margin-bottom: $spacing-md;
  overflow: hidden;
  box-shadow: $shadow-sm;

  &__image {
    width: 100%;
    height: 400rpx;
  }

  &__content {
    padding: $spacing-md;
  }

  &__header {
    display: flex;
    align-items: center;
    margin-bottom: $spacing-sm;
  }

  &__avatar {
    width: 64rpx;
    height: 64rpx;
    border-radius: 50%;
    margin-right: $spacing-sm;
  }

  &__user {
    flex: 1;
  }

  &__nickname {
    font-size: $font-size-base;
    font-weight: $font-weight-medium;
    color: $color-text-primary;
  }

  &__profess {
    font-size: $font-size-xs;
    color: $color-text-secondary;
    margin-top: 4rpx;
  }

  &__time {
    font-size: $font-size-xs;
    color: $color-text-helper;
  }

  &__desc {
    font-size: $font-size-base;
    color: $color-text-primary;
    line-height: 1.6;
    display: -webkit-box;
    -webkit-box-orient: vertical;
    -webkit-line-clamp: 2;
    overflow: hidden;
    margin-bottom: $spacing-sm;
  }

  &__footer {
    display: flex;
    align-items: center;
    justify-content: space-between;
  }

  &__tags {
    display: flex;
    flex-wrap: wrap;
    gap: $spacing-xs;
  }

  &__tag {
    font-size: $font-size-xs;
    color: $color-text-secondary;
    background-color: $color-bg-page;
    padding: 4rpx 12rpx;
    border-radius: $radius-sm;

    &--charge {
      color: $color-primary;
      background-color: $color-primary-light;
    }
  }

  &__stats {
    display: flex;
    align-items: center;
  }

  &__stat {
    font-size: $font-size-xs;
    color: $color-text-helper;
  }
}

.skeleton-list {
  padding: $spacing-md;
}

.skeleton-card {
  background-color: #fff;
  border-radius: $radius-md;
  margin-bottom: $spacing-md;
  overflow: hidden;
}

.skeleton-image {
  width: 100%;
  height: 400rpx;
  background: linear-gradient(90deg, #f0f0f0 25%, #e0e0e0 50%, #f0f0f0 75%);
  background-size: 200% 100%;
  animation: shimmer 1.5s infinite;
}

.skeleton-info {
  padding: $spacing-md;
}

.skeleton-line {
  height: 28rpx;
  background: linear-gradient(90deg, #f0f0f0 25%, #e0e0e0 50%, #f0f0f0 75%);
  background-size: 200% 100%;
  animation: shimmer 1.5s infinite;
  border-radius: 4rpx;
  margin-bottom: $spacing-sm;

  &--title { width: 60%; }
  &--desc { width: 90%; }
  &--short { width: 40%; }
}

@keyframes shimmer {
  0% { background-position: -200% 0; }
  100% { background-position: 200% 0; }
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 200rpx $spacing-xl;

  &__text {
    font-size: $font-size-lg;
    color: $color-text-secondary;
    margin-bottom: $spacing-sm;
  }

  &__sub {
    font-size: $font-size-sm;
    color: $color-text-helper;
  }
}

.load-end {
  display: flex;
  justify-content: center;
  padding: $spacing-lg;

  &__text {
    font-size: $font-size-sm;
    color: $color-text-helper;
  }
}
</style>
