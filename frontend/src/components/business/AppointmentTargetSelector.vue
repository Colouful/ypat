<template>
  <view class="target-selector">
    <view class="target-selector__grid">
      <view
        v-for="item in targets"
        :key="item.value"
        class="target-selector__card"
        :class="['target-selector__card--' + item.theme]"
        @tap="emit('select', item.value)"
        hover-class="target-selector__card--hover"
        :hover-stay-time="50"
      >
        <view class="target-selector__icon">
          <KeepIcon :name="item.icon" :size="48" :color="item.color" :strokeWidth="1.6" />
        </view>
        <text class="target-selector__label">{{ item.label }}</text>
        <text class="target-selector__sub">{{ item.sub }}</text>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import KeepIcon from './KeepIcon.vue'
import { YpatTarget, type YpatTargetType } from '@/constants/enums'

const emit = defineEmits<{
  (e: 'select', value: YpatTargetType): void
}>()

interface TargetItem {
  value: YpatTargetType
  label: string
  sub: string
  icon: string
  color: string
  theme: 'green' | 'orange' | 'purple' | 'teal' | 'gold' | 'blue'
}

const targets: TargetItem[] = [
  { value: YpatTarget.PHOTOGRAPHER, label: '约摄影师', sub: 'Camera', icon: 'camera', color: '#23C268', theme: 'green' },
  { value: YpatTarget.VIDEOGRAPHER, label: '约摄像师', sub: 'Video',    icon: 'video',  color: '#3498DB', theme: 'blue' },
  { value: YpatTarget.MERCHANT,     label: '约商家',   sub: 'Shop',     icon: 'gem',    color: '#E8A23B', theme: 'gold' },
  { value: YpatTarget.MAKEUP,       label: '约化妆师', sub: 'Makeup',   icon: 'sparkles', color: '#FF4D8B', theme: 'purple' },
  { value: YpatTarget.RETOUCHER,    label: '约修图师', sub: 'Retouch',  icon: 'edit',   color: '#9B59B6', theme: 'purple' },
  { value: YpatTarget.MODEL,        label: '约模特',   sub: 'Model',    icon: 'star',   color: '#10B5A8', theme: 'teal' },
]
</script>

<style lang="scss" scoped>
.target-selector {
  padding: 24rpx 32rpx;
  background: $color-bg-page;
  min-height: 100vh;

  &__grid {
    display: grid;
    grid-template-columns: 1fr 1fr;
    gap: 20rpx;
  }

  &__card {
    position: relative;
    background: $color-bg-card;
    border-radius: 24rpx;
    padding: 36rpx 24rpx 28rpx;
    display: flex;
    flex-direction: column;
    align-items: center;
    overflow: hidden;
    box-shadow: $shadow-keep-card;
    transition: transform 0.18s ease, box-shadow 0.18s ease;
  }

  &__card--hover {
    transform: translateY(-4rpx);
    box-shadow: 0 16rpx 32rpx rgba(20, 24, 31, 0.08);
  }

  // 主题色 - 圆形图标背景
  &__card--green &__icon {
    background: rgba(35, 194, 104, 0.12);
  }
  &__card--blue &__icon {
    background: rgba(52, 152, 219, 0.12);
  }
  &__card--gold &__icon {
    background: rgba(232, 162, 59, 0.14);
  }
  &__card--purple &__icon {
    background: rgba(155, 89, 182, 0.12);
  }
  &__card--teal &__icon {
    background: rgba(16, 181, 168, 0.12);
  }
  &__card--orange &__icon {
    background: rgba(255, 159, 28, 0.14);
  }

  &__icon {
    width: 96rpx;
    height: 96rpx;
    border-radius: 32rpx;
    display: flex;
    align-items: center;
    justify-content: center;
    margin-bottom: 24rpx;
  }

  &__label {
    font-size: 32rpx;
    font-weight: 600;
    color: $color-text-primary;
    line-height: 1.3;
  }

  &__sub {
    font-size: 22rpx;
    color: $color-text-helper;
    margin-top: 6rpx;
    letter-spacing: 1rpx;
  }
}
</style>
