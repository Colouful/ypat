<template>
  <view class="wallet-page">
    <!-- 余额卡片 -->
    <view class="balance-card">
      <view class="balance-card__header">
        <text class="balance-card__label">PPD余额</text>
      </view>
      <view class="balance-card__amount">
        <text class="balance-card__number">{{ userStore.userInfo?.ppd || '0' }}</text>
      </view>
      <button class="btn-recharge" @tap="goRecharge">
        立即充值
      </button>
    </view>

    <!-- 快捷入口 -->
    <view class="quick-links">
      <view class="quick-links__item" @tap="goRecords">
        <view class="quick-links__icon quick-links__icon--record">
          <text class="iconfont icon-list"></text>
        </view>
        <text class="quick-links__text">消费记录</text>
        <text class="iconfont icon-arrow-right quick-links__arrow"></text>
      </view>
      <view class="quick-links__divider"></view>
      <view class="quick-links__item" @tap="goBills">
        <view class="quick-links__icon quick-links__icon--bill">
          <text class="iconfont icon-bill"></text>
        </view>
        <text class="quick-links__text">账单明细</text>
        <text class="iconfont icon-arrow-right quick-links__arrow"></text>
      </view>
    </view>

    <!-- 最近记录 -->
    <view class="recent-section">
      <view class="recent-section__header">
        <text class="recent-section__title">最近记录</text>
      </view>

      <!-- 加载状态 -->
      <view v-if="loading" class="recent-loading">
        <view class="loading-spinner"></view>
        <text class="loading-text">加载中...</text>
      </view>

      <!-- 空状态 -->
      <view v-else-if="records.length === 0" class="recent-empty">
        <text class="recent-empty__text">暂无消费记录</text>
      </view>

      <!-- 记录列表 -->
      <view v-else class="record-list">
        <view
          v-for="item in records"
          :key="item.id"
          class="record-item"
        >
          <view class="record-item__icon" :class="getRecordIconClass(item.type)">
            <text class="iconfont" :class="getRecordIcon(item.type)"></text>
          </view>
          <view class="record-item__info">
            <text class="record-item__label">{{ getRecordTypeLabel(item.type) }}</text>
            <text class="record-item__desc">{{ item.typeTxt || '' }}</text>
          </view>
          <view class="record-item__right">
            <text class="record-item__date">{{ formatDate(item.credate) }}</text>
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { useUserStore } from '@/stores/user'
import * as paymentApi from '@/api/modules/payment'
import { RecordType, RECORD_TYPE_LABELS } from '@/constants/enums'
import type { RecordInfo } from '@/api/types'

const userStore = useUserStore()

const loading = ref(true)
const records = ref<RecordInfo[]>([])

onLoad(async () => {
  await fetchRecentRecords()
})

async function fetchRecentRecords() {
  loading.value = true
  try {
    const userId = Number(userStore.userInfo?.id)
    if (!userId) {
      loading.value = false
      return
    }

    const res = await paymentApi.getRecordList({
      page: 1,
      size: 5,
      userId,
    })

    if (res.success && res.data) {
      records.value = res.data.content || []
    }
  } catch {
    uni.showToast({ title: '加载失败', icon: 'none' })
  } finally {
    loading.value = false
  }
}

function getRecordTypeLabel(type: number): string {
  return RECORD_TYPE_LABELS[String(type)] || '其他'
}

function getRecordIcon(type: number): string {
  const iconMap: Record<number, string> = {
    0: 'icon-coin',
    1: 'icon-gift',
    2: 'icon-system',
    3: 'icon-publish',
    4: 'icon-apply',
    5: 'icon-eye',
  }
  return iconMap[type] || 'icon-coin'
}

function getRecordIconClass(type: number): string {
  const classMap: Record<number, string> = {
    0: 'record-item__icon--topup',
    1: 'record-item__icon--invite',
    2: 'record-item__icon--system',
    3: 'record-item__icon--publish',
    4: 'record-item__icon--apply',
    5: 'record-item__icon--view',
  }
  return classMap[type] || 'record-item__icon--topup'
}

function formatDate(dateStr: string): string {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  const hours = String(date.getHours()).padStart(2, '0')
  const minutes = String(date.getMinutes()).padStart(2, '0')
  return `${month}-${day} ${hours}:${minutes}`
}

function goRecharge() {
  uni.navigateTo({ url: '/pages-sub/user/recharge' })
}

function goRecords() {
  uni.navigateTo({ url: '/pages-sub/user/records' })
}

function goBills() {
  uni.navigateTo({ url: '/pages-sub/user/bills' })
}
</script>

<style lang="scss">
@import '@/styles/tokens.scss';

.wallet-page {
  min-height: 100vh;
  background-color: $color-bg-page;
  padding: $spacing-xl;
  padding-bottom: calc(#{$spacing-xxl} + env(safe-area-inset-bottom));
}

.balance-card {
  background-color: $color-bg-card;
  border-radius: $radius-lg;
  padding: $spacing-xxl $spacing-xl;
  box-shadow: $shadow-md;
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-bottom: $spacing-xl;

  &__header {
    margin-bottom: $spacing-md;
  }

  &__label {
    font-size: $font-size-base;
    color: $color-text-secondary;
  }

  &__amount {
    margin-bottom: $spacing-xl;
  }

  &__number {
    font-size: 80rpx;
    font-weight: $font-weight-bold;
    color: $color-accent-orange;
    line-height: 1.2;
  }
}

.btn-recharge {
  width: 100%;
  height: 88rpx;
  line-height: 88rpx;
  background-color: $color-primary;
  color: #fff;
  font-size: $font-size-lg;
  font-weight: $font-weight-medium;
  border-radius: $radius-xl;
  border: none;
  text-align: center;

  &::after {
    border: none;
  }
}

.quick-links {
  background-color: $color-bg-card;
  border-radius: $radius-lg;
  padding: 0 $spacing-xl;
  box-shadow: $shadow-sm;
  margin-bottom: $spacing-xl;

  &__item {
    display: flex;
    align-items: center;
    height: 104rpx;
  }

  &__divider {
    height: 1rpx;
    background-color: $color-divider;
  }

  &__icon {
    width: 56rpx;
    height: 56rpx;
    border-radius: $radius-md;
    display: flex;
    align-items: center;
    justify-content: center;
    margin-right: $spacing-lg;

    .iconfont {
      font-size: 28rpx;
      color: #fff;
    }

    &--record {
      background-color: $color-accent-orange;
    }

    &--bill {
      background-color: $color-accent-blue;
    }
  }

  &__text {
    flex: 1;
    font-size: $font-size-base;
    color: $color-text-primary;
    font-weight: $font-weight-medium;
  }

  &__arrow {
    font-size: $font-size-sm;
    color: $color-text-helper;
  }
}

.recent-section {
  background-color: $color-bg-card;
  border-radius: $radius-lg;
  padding: $spacing-xl;
  box-shadow: $shadow-sm;

  &__header {
    margin-bottom: $spacing-lg;
  }

  &__title {
    font-size: $font-size-lg;
    font-weight: $font-weight-semibold;
    color: $color-text-primary;
  }
}

.recent-loading {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: $spacing-xxl 0;

  .loading-spinner {
    width: 48rpx;
    height: 48rpx;
    border: 4rpx solid $color-border;
    border-top-color: $color-primary;
    border-radius: 50%;
    animation: spin 0.8s linear infinite;
  }

  .loading-text {
    margin-top: $spacing-md;
    font-size: $font-size-sm;
    color: $color-text-secondary;
  }
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

.recent-empty {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: $spacing-xxl 0;

  &__text {
    font-size: $font-size-base;
    color: $color-text-helper;
  }
}

.record-list {
  display: flex;
  flex-direction: column;
}

.record-item {
  display: flex;
  align-items: center;
  padding: $spacing-lg 0;

  &:not(:last-child) {
    border-bottom: 1rpx solid $color-divider;
  }

  &__icon {
    width: 56rpx;
    height: 56rpx;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    margin-right: $spacing-lg;
    flex-shrink: 0;

    .iconfont {
      font-size: 24rpx;
      color: #fff;
    }

    &--topup {
      background-color: $color-primary;
    }

    &--invite {
      background-color: $color-accent-purple;
    }

    &--system {
      background-color: $color-accent-blue;
    }

    &--publish {
      background-color: $color-accent-orange;
    }

    &--apply {
      background-color: $color-primary-dark;
    }

    &--view {
      background-color: $color-text-secondary;
    }
  }

  &__info {
    flex: 1;
    display: flex;
    flex-direction: column;
    overflow: hidden;
  }

  &__label {
    font-size: $font-size-base;
    color: $color-text-primary;
    font-weight: $font-weight-medium;
    margin-bottom: 4rpx;
  }

  &__desc {
    font-size: $font-size-sm;
    color: $color-text-secondary;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  &__right {
    flex-shrink: 0;
    margin-left: $spacing-md;
  }

  &__date {
    font-size: $font-size-xs;
    color: $color-text-helper;
  }
}
</style>
