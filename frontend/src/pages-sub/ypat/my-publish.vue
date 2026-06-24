<template>
  <view class="my-publish-page">
    <!-- 加载骨架屏 -->
    <view v-if="loading && list.length === 0" class="skeleton-list">
      <view v-for="i in 4" :key="i" class="skeleton-card">
        <view class="skeleton-card__image" />
        <view class="skeleton-card__info">
          <view class="skeleton-line skeleton-line--title" />
          <view class="skeleton-line skeleton-line--desc" />
          <view class="skeleton-line skeleton-line--short" />
        </view>
      </view>
    </view>

    <!-- 空状态 -->
    <view v-else-if="!loading && list.length === 0" class="empty-state">
      <image class="empty-state__icon" src="/static/images/empty-publish.png" mode="aspectFit" />
      <text class="empty-state__text">暂无发布记录</text>
      <text class="empty-state__sub">发布约拍信息，让更多人看到你</text>
    </view>

    <!-- 列表 -->
    <view v-else class="publish-list">
      <view
        v-for="item in list"
        :key="item.id"
        class="publish-card"
        @tap="goDetail(item.id)"
      >
        <image
          v-if="getFirstImage(item.images)"
          class="publish-card__image"
          :src="getFirstImage(item.images)"
          mode="aspectFill"
          lazy-load
        />
        <view v-else class="publish-card__image publish-card__image--empty">
          <text class="publish-card__image-placeholder">暂无图片</text>
        </view>

        <view class="publish-card__content">
          <view class="publish-card__header">
            <text class="publish-card__title">{{ item.title }}</text>
            <view class="publish-card__badge" :class="getStatusClass(item.status)">
              <text class="publish-card__badge-text">{{ getStatusLabel(item.status) }}</text>
            </view>
          </view>

          <text class="publish-card__desc">{{ item.content }}</text>

          <view class="publish-card__footer">
            <view class="publish-card__meta">
              <text v-if="item.city" class="publish-card__city">{{ item.city }}</text>
              <text class="publish-card__time">{{ formatTime(item.createTime) }}</text>
            </view>
            <view class="publish-card__stats">
              <text class="publish-card__stat">{{ item.readCount || 0 }}浏览</text>
              <text class="publish-card__stat">{{ item.favoriteCount || 0 }}收藏</text>
              <text class="publish-card__stat">{{ item.applyCount || 0 }}报名</text>
            </view>
          </view>
        </view>
      </view>
    </view>

    <!-- 加载更多 -->
    <view v-if="loadingMore" class="load-more">
      <text class="load-more__text">加载中...</text>
    </view>

    <!-- 没有更多 -->
    <view v-if="!hasMore && list.length > 0" class="load-end">
      <text class="load-end__text">没有更多了</text>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { onPullDownRefresh, onReachBottom } from '@dcloudio/uni-app'
import { useUserStore } from '@/stores/user'
import * as ypatApi from '@/api/modules/ypat'
import { YpatStatus, YPAT_STATUS_LABELS } from '@/constants/enums'
import type { YpatInfo } from '@/api/types'

const userStore = useUserStore()

const list = ref<YpatInfo[]>([])
const loading = ref(false)
const loadingMore = ref(false)
const hasMore = ref(true)
const page = ref(0)
const pageSize = 10

/** 获取第一张图片 */
function getFirstImage(images: string): string {
  if (!images) return ''
  const arr = images.split(',')
  return arr[0] || ''
}

/** 获取状态标签 */
function getStatusLabel(status: number): string {
  const statusStr = String(status)
  // 映射数字到枚举字符串
  const statusMap: Record<string, string> = {
    [YpatStatus.SUBMITTED]: '审核中',
    [YpatStatus.APPROVED]: '已通过',
    [YpatStatus.REJECTED]: '未通过',
    [YpatStatus.DRAFT]: '草稿',
  }
  return statusMap[statusStr] || YPAT_STATUS_LABELS[statusStr] || '未知'
}

/** 获取状态样式类 */
function getStatusClass(status: number): string {
  const statusStr = String(status)
  if (statusStr === YpatStatus.SUBMITTED) return 'publish-card__badge--orange'
  if (statusStr === YpatStatus.APPROVED) return 'publish-card__badge--green'
  if (statusStr === YpatStatus.REJECTED) return 'publish-card__badge--red'
  return 'publish-card__badge--gray'
}

/** 格式化时间 */
function formatTime(time: string): string {
  if (!time) return ''
  const date = new Date(time)
  const now = new Date()
  const diff = now.getTime() - date.getTime()
  const minutes = Math.floor(diff / 60000)
  const hours = Math.floor(diff / 3600000)
  const days = Math.floor(diff / 86400000)

  if (minutes < 1) return '刚刚'
  if (minutes < 60) return `${minutes}分钟前`
  if (hours < 24) return `${hours}小时前`
  if (days < 30) return `${days}天前`

  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  return `${date.getFullYear()}-${month}-${day}`
}

/** 加载列表 */
async function loadList(refresh = false) {
  if (!userStore.userInfo) {
    uni.showToast({ title: '请先登录', icon: 'none' })
    return
  }

  if (refresh) {
    page.value = 0
    hasMore.value = true
    loading.value = true
  } else {
    if (!hasMore.value) return
    loadingMore.value = true
  }

  try {
    const res = await ypatApi.getMyPublishList({
      page: page.value,
      size: pageSize,
      userId: Number(userStore.userInfo.id),
    })

    if (res.success) {
      const content = res.data.content || []
      if (refresh) {
        list.value = content
      } else {
        list.value.push(...content)
      }
      hasMore.value = content.length >= pageSize
      page.value++
    } else {
      uni.showToast({ title: res.message || '加载失败', icon: 'none' })
    }
  } catch (err) {
    uni.showToast({ title: '网络异常，请稍后重试', icon: 'none' })
  } finally {
    loading.value = false
    loadingMore.value = false
  }
}

/** 跳转详情 */
function goDetail(id: number) {
  uni.navigateTo({ url: `/pages-sub/ypat/detail?id=${id}` })
}

onMounted(() => {
  uni.setNavigationBarTitle({ title: '我的发布' })
  loadList(true)
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

.my-publish-page {
  min-height: 100vh;
  background-color: $color-bg-page;
  padding-bottom: calc(#{$safe-area-inset-bottom} + 40rpx);
}

// 骨架屏
.skeleton-list {
  padding: $spacing-md;
}

.skeleton-card {
  display: flex;
  background-color: $color-bg-card;
  border-radius: $radius-md;
  margin-bottom: $spacing-md;
  overflow: hidden;
  box-shadow: $shadow-sm;

  &__image {
    width: 200rpx;
    height: 200rpx;
    flex-shrink: 0;
    background: linear-gradient(90deg, #f0f0f0 25%, #e0e0e0 50%, #f0f0f0 75%);
    background-size: 200% 100%;
    animation: shimmer 1.5s infinite;
  }

  &__info {
    flex: 1;
    padding: $spacing-md;
    display: flex;
    flex-direction: column;
    justify-content: center;
  }
}

.skeleton-line {
  height: 24rpx;
  background: linear-gradient(90deg, #f0f0f0 25%, #e0e0e0 50%, #f0f0f0 75%);
  background-size: 200% 100%;
  animation: shimmer 1.5s infinite;
  border-radius: 4rpx;
  margin-bottom: $spacing-sm;

  &--title {
    width: 60%;
    height: 28rpx;
  }

  &--desc {
    width: 85%;
  }

  &--short {
    width: 40%;
    margin-bottom: 0;
  }
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
  padding: 240rpx $spacing-xl 0;

  &__icon {
    width: 240rpx;
    height: 240rpx;
    margin-bottom: $spacing-xl;
    opacity: 0.6;
  }

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

// 列表
.publish-list {
  padding: $spacing-md;
}

.publish-card {
  display: flex;
  background-color: $color-bg-card;
  border-radius: $radius-md;
  margin-bottom: $spacing-md;
  overflow: hidden;
  box-shadow: $shadow-sm;
  transition: transform $duration-fast;

  &:active {
    transform: scale(0.98);
  }

  &__image {
    width: 200rpx;
    height: 200rpx;
    flex-shrink: 0;

    &--empty {
      display: flex;
      align-items: center;
      justify-content: center;
      background-color: $color-bg-page;
    }
  }

  &__image-placeholder {
    font-size: $font-size-xs;
    color: $color-text-helper;
  }

  &__content {
    flex: 1;
    padding: $spacing-md;
    display: flex;
    flex-direction: column;
    justify-content: space-between;
    min-width: 0;
  }

  &__header {
    display: flex;
    align-items: flex-start;
    justify-content: space-between;
    margin-bottom: $spacing-xs;
  }

  &__title {
    flex: 1;
    font-size: $font-size-base;
    font-weight: $font-weight-medium;
    color: $color-text-primary;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
    margin-right: $spacing-sm;
  }

  &__badge {
    flex-shrink: 0;
    padding: 4rpx 16rpx;
    border-radius: $radius-sm;

    &--orange {
      background-color: rgba($color-accent-orange, 0.12);
    }

    &--green {
      background-color: rgba($color-primary, 0.12);
    }

    &--red {
      background-color: rgba($color-accent-red, 0.12);
    }

    &--gray {
      background-color: $color-bg-page;
    }
  }

  &__badge-text {
    font-size: $font-size-xs;
    font-weight: $font-weight-medium;

    .publish-card__badge--orange & {
      color: $color-accent-orange;
    }

    .publish-card__badge--green & {
      color: $color-primary;
    }

    .publish-card__badge--red & {
      color: $color-accent-red;
    }

    .publish-card__badge--gray & {
      color: $color-text-secondary;
    }
  }

  &__desc {
    font-size: $font-size-sm;
    color: $color-text-secondary;
    line-height: 1.5;
    display: -webkit-box;
    -webkit-box-orient: vertical;
    -webkit-line-clamp: 2;
    overflow: hidden;
    margin-bottom: $spacing-xs;
  }

  &__footer {
    display: flex;
    align-items: center;
    justify-content: space-between;
  }

  &__meta {
    display: flex;
    align-items: center;
    gap: $spacing-sm;
  }

  &__city {
    font-size: $font-size-xs;
    color: $color-primary;
    background-color: $color-primary-light;
    padding: 2rpx 10rpx;
    border-radius: $radius-sm;
  }

  &__time {
    font-size: $font-size-xs;
    color: $color-text-helper;
  }

  &__stats {
    display: flex;
    align-items: center;
    gap: $spacing-sm;
  }

  &__stat {
    font-size: $font-size-xs;
    color: $color-text-helper;
  }
}

// 加载更多
.load-more {
  display: flex;
  justify-content: center;
  padding: $spacing-lg;

  &__text {
    font-size: $font-size-sm;
    color: $color-text-helper;
  }
}

// 没有更多
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
