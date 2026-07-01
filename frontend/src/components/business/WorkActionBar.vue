<template>
  <view class="work-action-bar">
    <view class="work-action-bar__btn" @tap="onLike">
      <text class="work-action-bar__icon" :class="{ 'work-action-bar__icon--active': work.isLiked }">👍</text>
    </view>
    <view class="work-action-bar__btn" @tap="onFavorite">
      <text class="work-action-bar__icon">☆</text>
    </view>
    <view class="work-action-bar__btn" @tap="onShare">
      <text class="work-action-bar__icon">↻</text>
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
    height: 80rpx;
    display: flex;
    align-items: center;
    justify-content: center;
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
    background: linear-gradient(90deg, #C9F4D9 0%, #17A857 100%);
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
