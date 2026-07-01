<template>
  <view class="publish-entry">
    <text class="publish-entry__title">您可以发布</text>
    <view class="publish-entry__card" @tap="emit('appointment')">
      <view class="publish-entry__icon publish-entry__icon--camera">
        <KeepIcon name="camera" :size="56" color="#FFFFFF" />
      </view>
      <view class="publish-entry__info">
        <text class="publish-entry__card-title">发布约拍</text>
        <text class="publish-entry__card-desc">您可以发布约拍需求。约拍摄影师、模特、商家、化妆师、修图师</text>
      </view>
    </view>
    <view class="publish-entry__card" @tap="emit('work')">
      <view class="publish-entry__icon publish-entry__icon--image">
        <KeepIcon name="image" :size="56" color="#FFFFFF" />
      </view>
      <view class="publish-entry__info">
        <text class="publish-entry__card-title">发布作品</text>
        <text class="publish-entry__card-desc">您可以上传自己的作品或照片，来提高自己的约拍吸引力</text>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import KeepIcon from './KeepIcon.vue'

const emit = defineEmits<{
  (e: 'appointment'): void
  (e: 'work'): void
}>()
</script>

<style lang="scss" scoped>
.publish-entry {
  padding: 48rpx 32rpx 32rpx;
  background: $color-bg-page;
  min-height: 100vh;
  &__title {
    display: block;
    font-size: 64rpx;
    font-weight: 800;
    color: $color-text-primary;
    margin-bottom: 64rpx;
    letter-spacing: 2rpx;
  }
  &__card {
    display: flex;
    align-items: flex-start;
    margin-bottom: 48rpx;
  }
  &__icon {
    width: 96rpx;
    height: 96rpx;
    border-radius: 24rpx;
    display: flex;
    align-items: center;
    justify-content: center;
    flex-shrink: 0;
    margin-right: 24rpx;
    &--camera { background: linear-gradient(135deg, $color-primary 0%, $color-primary-dark 100%); }
    &--image { background: linear-gradient(135deg, $color-primary-light 0%, $color-primary 100%); }
  }
  &__info {
    flex: 1;
    padding-top: 8rpx;
  }
  &__card-title {
    display: block;
    font-size: 36rpx;
    font-weight: 600;
    color: $color-text-primary;
    margin-bottom: 12rpx;
  }
  &__card-desc {
    display: block;
    font-size: 26rpx;
    color: $color-text-secondary;
    line-height: 1.6;
  }
}
</style>
