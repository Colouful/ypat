<template>
  <view class="work-skeleton">
    <view v-for="i in count" :key="i" class="work-skeleton__item">
      <view class="work-skeleton__cover skeleton-pulse" />
      <view class="work-skeleton__line skeleton-pulse" :style="{ width: '80%' }" />
      <view class="work-skeleton__line work-skeleton__line--short skeleton-pulse" />
      <view class="work-skeleton__user">
        <view class="work-skeleton__avatar skeleton-pulse" />
        <view class="work-skeleton__line work-skeleton__line--tiny skeleton-pulse" />
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
withDefaults(defineProps<{
  count?: number
}>(), { count: 4 })
</script>

<style lang="scss" scoped>
.work-skeleton {
  padding: 16rpx 24rpx;
  &__item {
    background: $color-bg-card;
    border-radius: 16rpx;
    padding: 16rpx;
    margin-bottom: 16rpx;
  }
  &__cover {
    width: 100%;
    aspect-ratio: 1 / 1;
    border-radius: 12rpx;
    background: $color-bg-page;
  }
  &__line {
    height: 28rpx;
    border-radius: 8rpx;
    background: $color-bg-page;
    margin: 16rpx 0 8rpx 0;
    &--short { width: 50%; height: 24rpx; }
    &--tiny { width: 40%; height: 20rpx; }
  }
  &__user {
    display: flex;
    align-items: center;
    gap: 12rpx;
    margin-top: 16rpx;
  }
  &__avatar {
    width: 40rpx;
    height: 40rpx;
    border-radius: 20rpx;
    background: $color-bg-page;
  }
}

.skeleton-pulse {
  animation: pulse 1.4s ease-in-out infinite;
}

@keyframes pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.4; }
}
</style>
