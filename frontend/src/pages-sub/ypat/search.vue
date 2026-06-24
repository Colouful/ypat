<template>
  <view class="search-page">
    <!-- 搜索栏 -->
    <view class="search-bar" :style="{ paddingTop: statusBarHeight + 'px' }">
      <view class="search-bar__content">
        <view class="search-bar__input-wrap">
          <text class="search-bar__icon">🔍</text>
          <input
            class="search-bar__input"
            v-model="keyword"
            type="text"
            placeholder="搜索约拍"
            placeholder-class="search-bar__placeholder"
            confirm-type="search"
            :focus="autoFocus"
            @confirm="doSearch"
          />
          <view v-if="keyword" class="search-bar__clear" @tap="clearKeyword">
            <text class="search-bar__clear-icon">✕</text>
          </view>
        </view>
        <view class="search-bar__cancel" @tap="handleCancel">
          <text class="search-bar__cancel-text">取消</text>
        </view>
      </view>
    </view>

    <view class="search-body" :style="{ paddingTop: searchBarHeight + 'px' }">
      <!-- 搜索结果列表 -->
      <view v-if="hasSearched" class="result-section">
        <!-- 加载中 -->
        <view v-if="loading && resultList.length === 0" class="skeleton-list">
          <view v-for="i in 3" :key="i" class="skeleton-card">
            <view class="skeleton-image" />
            <view class="skeleton-info">
              <view class="skeleton-line skeleton-line--title" />
              <view class="skeleton-line skeleton-line--desc" />
              <view class="skeleton-line skeleton-line--short" />
            </view>
          </view>
        </view>

        <!-- 空结果 -->
        <view v-else-if="resultList.length === 0 && !loading" class="empty-state">
          <text class="empty-state__text">未找到相关约拍</text>
          <text class="empty-state__sub">换个关键词试试</text>
        </view>

        <!-- 结果列表 -->
        <view v-else class="ypat-list">
          <view v-for="item in resultList" :key="item.id" class="ypat-card" @tap="goDetail(item.id)">
            <image v-if="getFirstImage(item.pics)" class="ypat-card__image" :src="getFirstImage(item.pics)" mode="aspectFill" lazy-load />
            <view class="ypat-card__content">
              <view class="ypat-card__header">
                <image class="ypat-card__avatar" :src="item.userQo?.imgpath || '/static/default-avatar.png'" mode="aspectFill" />
                <view class="ypat-card__user">
                  <text class="ypat-card__nickname">{{ item.userQo?.nickname || '匿名用户' }}</text>
                  <text class="ypat-card__profess">{{ getProfessLabel(item.userQo?.profess) }}</text>
                </view>
                <text class="ypat-card__time">{{ item.timeStr || formatDate(item.pubdate) }}</text>
              </view>
              <text class="ypat-card__title">{{ item.title }}</text>
              <text class="ypat-card__desc">{{ item.describ }}</text>
              <view class="ypat-card__footer">
                <view class="ypat-card__tags">
                  <text v-if="item.city" class="ypat-card__tag">{{ item.city }}</text>
                  <text class="ypat-card__tag ypat-card__tag--charge">{{ item.chargewayTxt || getChargeWayLabel(item.chargeway) }}</text>
                </view>
                <view class="ypat-card__stats">
                  <text class="ypat-card__stat">{{ item.readtimes || 0 }}阅读</text>
                </view>
              </view>
            </view>
          </view>
        </view>

        <!-- 加载更多 -->
        <view v-if="!hasMore && resultList.length > 0" class="load-end">
          <text class="load-end__text">没有更多了</text>
        </view>
      </view>

      <!-- 搜索历史和热门标签 -->
      <view v-else class="suggest-section">
        <!-- 搜索历史 -->
        <view v-if="historyList.length > 0" class="history-section">
          <view class="section-header">
            <text class="section-header__title">搜索历史</text>
            <view class="section-header__clear" @tap="clearHistory">
              <text class="section-header__clear-text">清空</text>
            </view>
          </view>
          <view class="history-tags">
            <view v-for="(item, index) in historyList" :key="index" class="history-tag" @tap="tapHistoryTag(item)">
              <text class="history-tag__text">{{ item }}</text>
            </view>
          </view>
        </view>

        <!-- 热门标签 -->
        <view class="hot-section">
          <view class="section-header">
            <text class="section-header__title">热门风格</text>
          </view>
          <view class="hot-tags">
            <view v-for="(style, index) in photoStyles" :key="index" class="hot-tag" @tap="tapHotTag(style)">
              <text class="hot-tag__text">{{ style }}</text>
            </view>
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { onReachBottom } from '@dcloudio/uni-app'
import { useAppStore } from '@/stores/app'
import * as ypatApi from '@/api/modules/ypat'
import { PROFESS_LABELS, CHARGE_WAY_LABELS, PHOTO_STYLES } from '@/constants/enums'
import type { YpatInfo } from '@/api/types'

const HISTORY_STORAGE_KEY = 'ypat_search_history'
const MAX_HISTORY_COUNT = 20

const appStore = useAppStore()

const statusBarHeight = computed(() => appStore.statusBarHeight)
const searchBarHeight = computed(() => appStore.navBarHeight)

const keyword = ref('')
const autoFocus = ref(true)
const loading = ref(false)
const hasSearched = ref(false)
const resultList = ref<YpatInfo[]>([])
const page = ref(0)
const hasMore = ref(true)
const historyList = ref<string[]>([])
const photoStyles = PHOTO_STYLES

function getProfessLabel(code?: string): string {
  if (!code) return ''
  return PROFESS_LABELS[code] || ''
}

function getChargeWayLabel(type?: number): string {
  if (type === undefined || type === null) return ''
  return CHARGE_WAY_LABELS[String(type)] || ''
}

function getFirstImage(pics?: string[]): string {
  if (!pics || pics.length === 0) return ''
  return pics[0]?.trim() || ''
}

function formatDate(dateStr?: string): string {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  const month = date.getMonth() + 1
  const day = date.getDate()
  return `${month}月${day}日`
}

function loadHistory() {
  try {
    const stored = uni.getStorageSync(HISTORY_STORAGE_KEY)
    if (stored) {
      historyList.value = Array.isArray(stored) ? stored : JSON.parse(stored)
    }
  } catch {
    historyList.value = []
  }
}

function saveHistory(word: string) {
  const trimmed = word.trim()
  if (!trimmed) return
  const list = historyList.value.filter((item) => item !== trimmed)
  list.unshift(trimmed)
  if (list.length > MAX_HISTORY_COUNT) {
    list.splice(MAX_HISTORY_COUNT)
  }
  historyList.value = list
  uni.setStorageSync(HISTORY_STORAGE_KEY, list)
}

function clearHistory() {
  uni.showModal({
    title: '提示',
    content: '确定要清空搜索历史吗？',
    confirmColor: '#23C268',
    success: (res) => {
      if (res.confirm) {
        historyList.value = []
        uni.removeStorageSync(HISTORY_STORAGE_KEY)
      }
    },
  })
}

function clearKeyword() {
  keyword.value = ''
  hasSearched.value = false
  resultList.value = []
  page.value = 0
  hasMore.value = true
  autoFocus.value = true
}

function handleCancel() {
  uni.navigateBack()
}

function goDetail(id: number) {
  uni.navigateTo({ url: `/pages-sub/ypat/detail?id=${id}` })
}

function tapHistoryTag(word: string) {
  keyword.value = word
  doSearch()
}

function tapHotTag(style: string) {
  keyword.value = style
  doSearch()
}

async function doSearch() {
  const trimmed = keyword.value.trim()
  if (!trimmed) {
    uni.showToast({ title: '请输入搜索关键词', icon: 'none' })
    return
  }

  saveHistory(trimmed)
  hasSearched.value = true
  page.value = 0
  hasMore.value = true
  resultList.value = []
  await loadResults()
}

async function loadResults() {
  if (loading.value) return
  if (!hasMore.value) return

  const trimmed = keyword.value.trim()
  if (!trimmed) return

  loading.value = true
  try {
    const params = {
      page: page.value,
      size: 10,
      city: trimmed,
    }
    const res = await ypatApi.getRecommendList(params)
    if (res.success) {
      const content = res.data.content || []
      if (page.value === 0) {
        resultList.value = content
      } else {
        resultList.value.push(...content)
      }
      hasMore.value = content.length >= 10
      page.value++
    } else {
      uni.showToast({ title: res.message || '搜索失败', icon: 'none' })
    }
  } catch {
    uni.showToast({ title: '网络异常，请重试', icon: 'none' })
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadHistory()
})

onReachBottom(() => {
  if (hasSearched.value) {
    loadResults()
  }
})
</script>

<style lang="scss">
@import '@/styles/tokens.scss';

.search-page {
  min-height: 100vh;
  background-color: $color-bg-page;
}

// 搜索栏
.search-bar {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: $z-index-navbar;
  background-color: #fff;
  box-shadow: $shadow-sm;

  &__content {
    display: flex;
    align-items: center;
    height: 88rpx;
    padding: 0 $spacing-md;
  }

  &__input-wrap {
    flex: 1;
    display: flex;
    align-items: center;
    height: 64rpx;
    background-color: $color-bg-page;
    border-radius: $radius-round;
    padding: 0 $spacing-md;
  }

  &__icon {
    font-size: $font-size-sm;
    margin-right: $spacing-xs;
  }

  &__input {
    flex: 1;
    font-size: $font-size-base;
    color: $color-text-primary;
    height: 64rpx;
  }

  &__placeholder {
    font-size: $font-size-base;
    color: $color-text-helper;
  }

  &__clear {
    width: 40rpx;
    height: 40rpx;
    display: flex;
    align-items: center;
    justify-content: center;
    background-color: $color-text-helper;
    border-radius: 50%;
    margin-left: $spacing-xs;
  }

  &__clear-icon {
    font-size: 20rpx;
    color: #fff;
  }

  &__cancel {
    padding: 0 $spacing-md;
  }

  &__cancel-text {
    font-size: $font-size-base;
    color: $color-text-secondary;
  }
}

// 搜索主体
.search-body {
  padding: $spacing-md;
}

// 建议区域
.suggest-section {
  padding-top: $spacing-sm;
}

// 搜索历史
.history-section {
  margin-bottom: $spacing-xl;
}

.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: $spacing-md;

  &__title {
    font-size: $font-size-lg;
    font-weight: $font-weight-semibold;
    color: $color-text-primary;
  }

  &__clear {
    padding: $spacing-xs $spacing-sm;
  }

  &__clear-text {
    font-size: $font-size-sm;
    color: $color-text-helper;
  }
}

.history-tags {
  display: flex;
  flex-wrap: wrap;
  gap: $spacing-sm;
}

.history-tag {
  background-color: $color-bg-card;
  border-radius: $radius-round;
  padding: $spacing-sm $spacing-lg;
  box-shadow: $shadow-sm;

  &__text {
    font-size: $font-size-sm;
    color: $color-text-primary;
  }
}

// 热门标签
.hot-section {
  margin-bottom: $spacing-xl;
}

.hot-tags {
  display: flex;
  flex-wrap: wrap;
  gap: $spacing-sm;
}

.hot-tag {
  background-color: $color-primary-light;
  border-radius: $radius-round;
  padding: $spacing-sm $spacing-lg;

  &__text {
    font-size: $font-size-sm;
    color: $color-primary;
  }
}

// 结果区域
.result-section {
  padding-top: $spacing-sm;
}

// 约拍卡片列表
.ypat-list {
  padding: 0;
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

  &__title {
    font-size: $font-size-lg;
    font-weight: $font-weight-medium;
    color: $color-text-primary;
    margin-bottom: $spacing-xs;
    display: -webkit-box;
    -webkit-box-orient: vertical;
    -webkit-line-clamp: 1;
    overflow: hidden;
  }

  &__desc {
    font-size: $font-size-base;
    color: $color-text-secondary;
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

// 骨架屏
.skeleton-list {
  padding: 0;
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

// 空状态
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

// 加载结束
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
