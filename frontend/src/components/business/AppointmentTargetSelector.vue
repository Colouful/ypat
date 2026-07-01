<template>
  <view class="target-selector">
    <view class="target-selector__grid">
      <view v-for="item in targets" :key="item.value"
            class="target-selector__card"
            @tap="emit('select', item.value)">
        <view class="target-selector__icon">
          <text class="target-selector__svg" :class="`target-selector__svg--${item.svgKey}`">●</text>
        </view>
        <text class="target-selector__label">{{ item.label }}</text>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { YpatTarget, type YpatTargetType } from '@/constants/enums'

const emit = defineEmits<{
  (e: 'select', value: YpatTargetType): void
}>()

interface TargetItem {
  value: YpatTargetType
  label: string
  svgKey: string
}

const targets: TargetItem[] = [
  { value: YpatTarget.PHOTOGRAPHER, label: '约摄影师', svgKey: 'camera' },
  { value: YpatTarget.VIDEOGRAPHER, label: '约摄像师', svgKey: 'video' },
  { value: YpatTarget.MERCHANT,     label: '约商家',   svgKey: 'shop' },
  { value: YpatTarget.MAKEUP,       label: '约化妆师', svgKey: 'makeup' },
  { value: YpatTarget.RETOUCHER,    label: '约修图师', svgKey: 'ps' },
  { value: YpatTarget.MODEL,        label: '约模特',   svgKey: 'dress' },
]
</script>

<style lang="scss" scoped>
.target-selector {
  padding: 24rpx 32rpx;
  background: #FFFFFF;
  min-height: 100vh;
  &__grid {
    display: grid;
    grid-template-columns: 1fr 1fr;
    gap: 24rpx;
  }
  &__card {
    position: relative;
    background: linear-gradient(135deg, #FFFAFA 0%, #FFF0F2 100%);
    border-radius: 16rpx;
    padding: 80rpx 24rpx 48rpx;
    display: flex;
    flex-direction: column;
    align-items: center;
    overflow: hidden;
  }
  &__icon {
    position: absolute;
    top: 24rpx;
    left: 24rpx;
    color: $color-primary;
    font-size: 56rpx;
    font-weight: 700;
  }
  &__svg {
    color: $color-primary;
    font-size: 56rpx;
    line-height: 1;
  }
  &__label {
    font-size: 32rpx;
    font-weight: 600;
    color: $color-text-primary;
  }
}
</style>
