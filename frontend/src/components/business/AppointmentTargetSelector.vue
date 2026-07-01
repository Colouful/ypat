<template>
  <view class="target-selector">
    <view class="target-selector__grid">
      <view v-for="item in targets" :key="item.value"
            class="target-selector__card"
            @tap="emit('select', item.value)">
        <view class="target-selector__icon">
          <KeepIcon :name="item.icon" :size="56" :color="primaryColor" />
        </view>
        <text class="target-selector__label">{{ item.label }}</text>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import KeepIcon from './KeepIcon.vue'
import { YpatTarget, type YpatTargetType } from '@/constants/enums'

const primaryColor = '#23C268'

const emit = defineEmits<{
  (e: 'select', value: YpatTargetType): void
}>()

interface TargetItem {
  value: YpatTargetType
  label: string
  icon: string
}

const targets: TargetItem[] = [
  { value: YpatTarget.PHOTOGRAPHER, label: '约摄影师', icon: 'camera' },
  { value: YpatTarget.VIDEOGRAPHER, label: '约摄像师', icon: 'video' },
  { value: YpatTarget.MERCHANT,     label: '约商家',   icon: 'gem' },
  { value: YpatTarget.MAKEUP,       label: '约化妆师', icon: 'sparkles' },
  { value: YpatTarget.RETOUCHER,    label: '约修图师', icon: 'edit' },
  { value: YpatTarget.MODEL,        label: '约模特',   icon: 'star' },
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
    gap: 24rpx;
  }
  &__card {
    position: relative;
    background: $color-bg-card;
    border-radius: 16rpx;
    padding: 80rpx 24rpx 48rpx;
    display: flex;
    flex-direction: column;
    align-items: center;
    overflow: hidden;
    box-shadow: $shadow-keep-card;
  }
  &__icon {
    position: absolute;
    top: 24rpx;
    left: 24rpx;
  }
  &__label {
    font-size: 32rpx;
    font-weight: 600;
    color: $color-text-primary;
  }
}
</style>
