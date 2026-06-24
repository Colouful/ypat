<template>
  <view class="my-favorite-page">
    <!-- 加载骨架屏 -->
    <view v-if="loading && list.length === 0" class="skeleton-list">
      <view v-for="i in 3" :key="i" class="skeleton-card">
        <view class="skeleton-card__cover" />
        <view class="skeleton-card__body">
          <view class="skeleton-line skeleton-line--header" />
          <view class="skeleton-line skeleton-line--content" />
          <view class="skeleton-line skeleton-line--footer" />
        </view>
      </view>
    </view>

    <!-- 空状态 -->
    <view v-else-if="!loading && list.length === 0" class="empty-state">
      <image class="empty-state__icon" src="/static/images/empty-favorite.png" mode="aspectFit" />
      <text class="empty-state__text">暂无收藏</text>
      <text class="empty-state__sub">收藏感兴趣的约拍，方便随时查看</text>
    </view>

    <!-- 列表 -->
    <view v-else class="favorite-list">
      <view
        v-for="item in list"
        :key="item.id"
        class="favorite-card"
        @tap="goDetail(item.id)"
      >
        <!-- 封面图 -->
        <image
          v-if="getFirstImage(item.images)"
          class="favorite-card__cover"
          :src="getFirstImage(item.images)"
          mode="aspectFill"
          lazy-load
        />

        <!-- 内容区 -->
        <view class="favorite-card__body">
          <!-- 用户信息 -->
          <view class="favorite-card__header">
            <image
              v-if="item.avatarUrl"
              class="favorite-card__avatar"
              :src="item.avatarUrl"
              mode="aspectFill"
            />
            <view v-else class="favorite-card__avatar favorite-card__avatar--default">
              <text class="favorite-card__avatar-text">{{ getAvatarText(item.nickName) }}</text>
            </view>
            <view class="favorite-card__user">
              <text class="favorite-card__nickname">{{ item.nickName || '匿名用户' }}</text>
              <text v-if="item.profess" class="favorite-card__profess">{{ getProfessLabel(item.profess) }}</text>
            </view>
            <text class="favorite-card__time">{{ formatTime(item.createTime) }}</text>
          </view>

          <!-- 描述 -->
          <text class="favorite-card__desc">{{ item.content }}</text>

          <!-- 底部标签和统计 -->
          <view class="favorite-card__footer">
            <view class="favorite-card__tags">
              <text v-if="item.city" class="favorite-card__tag">{{ item.city }}</text>
              <text v-if="item.price > 0" class="favorite-card__tag favorite-card__tag--price">
                {{ getChargeLabel(item.priceType) }}
              </text>
            </view>
            <view class="favorite-card__stats">
              <text class="favorite-card__stat">{{ item.readCount || 0 }}浏览</text>
              <text class="favorite-card__stat">{{ item.favoriteCount || 0 }}收藏</text>
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
import { PROFESS_LABELS, CHARGE_WAY_LABELS } from '@/constants/enums'
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

/** 获取头像首字 */
function getAvatarText(name?: string): string {
  if (!name) return '匿'
  return name.charAt(0)
}

/** 获取职业标签 */
function getProfessLabel(code: string): string {
  return PROFESS_LABELS[code] || ''
}

/** 获取收费方式标签 */
function getChargeLabel(priceType: number): string {
  return CHARGE_WAY_LABELS[String(priceType)] || '费用面议'
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
    const res = await ypatApi.getMyFavoriteList({
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
  uni.setNavigationBarTitle({ title: '我的收藏' })
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

.my-favorite-page {
  min-height: 100vh;
  background-color: $color-bg-page;
  padding-bottom: calc(#{$safe-area-inset-bottom} + 40rpx);
}

// 骨架屏
.skeleton-list {
  padding: $spacing-md;
}

.skeleton-card {
  background-color: $color-bg-card;
  border-radius: $radius-md;
  margin-bottom: $spacing-md;
  overflow: hidden;
  box-shadow: $shadow-sm;

  &__cover {
    width: 100%;
    height: 360rpx;
    background: linear-gradient(90deg, #f0f0f0 25%, #e0e0e0 50%, #f0f0f0 75%);
    background-size: 200% 100%;
    animation: shimmer 1.5s infinite;
  }

  &__body {
    padding: $spacing-md;
  }
}

.skeleton-line {
  height: 24rpx;
  background: linear-gradient(90deg, #f0f0f0 25%, #e0e0e0 50%, #f0f0f0 75%);
  background-size: 200% 100%;
  animation: shimmer 1.5s infinite;
  border-radius: 4rpx;
  margin-bottom: $spacing-sm;

  &--header {
    width: 50%;
    height: 28rpx;
  }

  &--content {
    width: 90%;
  }

  &--footer {
    width: 60%;
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

// 列表（类似首页卡片风格）
.favorite-list {
  padding: $spacing-sm $spacing-md;
}

.favorite-card {
  background-color: $color-bg-card;
  border-radius: $radius-md;
  margin-bottom: $spacing-md;
  overflow: hidden;
  box-shadow: $shadow-sm;
  transition: transform $duration-fast;

  &:active {
    transform: scale(0.98);
  }

  &__cover {
    width: 100%;
    height: 360rpx;
  }

  &__body {
    padding: $spacing-md;
  }

  &__header {
    display: flex;
    align-items: center;
    margin-bottom: $spacing-sm;
  }

  &__avatar {
    width: 56rpx;
    height: 56rpx;
    border-radius: 50%;
    margin-right: $spacing-sm;

    &--default {
      display: flex;
      align-items: center;
      justify-content: center;
      background-color: $color-primary-light;
    }
  }

  &__avatar-text {
    font-size: $font-size-sm;
    color: $color-primary;
    font-weight: $font-weight-medium;
  }

  &__user {
    flex: 1;
    display: flex;
    flex-direction: column;
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

    &--price {
      color: $color-primary;
      background-color: $color-primary-light;
    }
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
