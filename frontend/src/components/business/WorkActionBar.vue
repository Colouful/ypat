<template>
  <view class="work-action-bar">
    <view
      class="work-action-bar__btn"
      :class="{ 'work-action-bar__btn--active': work.isLiked }"
      @tap="onLike"
    >
      <view class="work-action-bar__icon-wrap">
        <KeepIcon name="star" :size="36" :color="work.isLiked ? '#23C268' : '#83888F'" />
      </view>
      <text class="work-action-bar__label">{{ work.isLiked ? '已赞' : '点赞' }}</text>
    </view>
    <view
      class="work-action-bar__btn"
      :class="{ 'work-action-bar__btn--active': work.isFavorited }"
      @tap="onFavorite"
    >
      <view class="work-action-bar__icon-wrap">
        <KeepIcon name="gem" :size="36" :color="work.isFavorited ? '#23C268' : '#83888F'" />
      </view>
      <text class="work-action-bar__label">{{ work.isFavorited ? '已收藏' : '收藏' }}</text>
    </view>
    <view class="work-action-bar__btn" @tap="onShare">
      <view class="work-action-bar__icon-wrap">
        <KeepIcon name="handshake" :size="36" color="#83888F" />
      </view>
      <text class="work-action-bar__label">分享</text>
    </view>
    <view v-if="!work.isOwner" class="work-action-bar__primary" @tap="onApply">
      <text>立即约拍</text>
    </view>
    <view v-else-if="mode === 'my'" class="work-action-bar__primary work-action-bar__primary--offline" @tap="onOffline">
      <text>下架作品</text>
    </view>
  </view>
</template>

<script setup lang="ts">
import KeepIcon from './KeepIcon.vue'
import type { WorkDetail } from '@/api/types/work'

const props = withDefaults(defineProps<{
  work: WorkDetail
  mode?: 'detail' | 'my'
}>(), {
  mode: 'detail',
})

const emit = defineEmits<{
  (e: 'like'): void
  (e: 'unlike'): void
  (e: 'favorite'): void
  (e: 'unfavorite'): void
  (e: 'share'): void
  (e: 'apply'): void
  (e: 'offline'): void
}>()

let lastClickTime = 0
function throttle(handler: () => void) {
  const now = Date.now()
  if (now - lastClickTime < 600) return
  lastClickTime = now
  handler()
}

function onLike() {
  throttle(() => {
    if (props.work.isLiked) emit('unlike')
    else emit('like')
  })
}
function onFavorite() {
  throttle(() => {
    if (props.work.isFavorited) emit('unfavorite')
    else emit('favorite')
  })
}
function onShare() { emit('share') }
function onApply() { emit('apply') }
function onOffline() { emit('offline') }
</script>

<style lang="scss" scoped>
.work-action-bar {
  position: fixed;
  left: 0;
  right: 0;
  bottom: 0;
  display: flex;
  align-items: center;
  background: $color-bg-card;
  padding: 16rpx 32rpx calc(16rpx + env(safe-area-inset-bottom));
  z-index: 200;
  &__btn {
    width: 96rpx;
    height: 88rpx;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    gap: 6rpx;
    border-radius: 24rpx;
    transition: background-color 0.2s ease;
    &--active {
      .work-action-bar__label {
        color: $color-primary;
        font-weight: 800;
      }
    }
  }
  &__icon-wrap {
    display: flex;
    align-items: center;
    justify-content: center;
    width: 42rpx;
    height: 42rpx;
  }
  &__label {
    color: $color-text-helper;
    font-size: 20rpx;
    font-weight: 700;
    line-height: 1;
    white-space: nowrap;
  }
  &__icon {
    font-size: 44rpx;
    color: $color-text-primary;
    line-height: 1;
    &--active { color: $color-primary; }
  }
  &__primary {
    flex: 1;
    margin-left: 16rpx;
    height: 80rpx;
    line-height: 80rpx;
    text-align: center;
    background: $color-primary;
    color: #FFFFFF;
    border-radius: 999rpx;
    font-size: 30rpx;
    font-weight: 600;
    box-shadow: 0 12rpx 24rpx rgba(35, 194, 104, 0.28);
    &--offline {
      background: $color-text-helper;
      box-shadow: none;
    }
  }
}
</style>
