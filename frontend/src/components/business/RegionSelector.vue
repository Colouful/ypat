<template>
  <view class="region-selector">
    <view v-if="config && config.allowNationwide" class="region-selector__row">
      <text class="region-selector__label">是否全国</text>
      <switch :checked="isNationwide" @change="onNationwideChange" color="#23C268" />
    </view>
    <view v-if="!isNationwide" class="region-selector__row" @tap="openPicker">
      <text class="region-selector__label">面向地区</text>
      <text class="region-selector__value">{{ displayRegion || '请选择省/市/区' }}</text>
      <KeepIcon name="chevron-right" :size="20" color="#B3B8BE" />
    </view>
  </view>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import KeepIcon from './KeepIcon.vue'

interface RegionConfig {
  allowNationwide?: boolean
}

const props = defineProps<{
  modelValue: { province: string; city: string; area: string } | null
  isNationwide: boolean
  config?: RegionConfig
}>()

const emit = defineEmits<{
  (e: 'update:modelValue', v: { province: string; city: string; area: string } | null): void
  (e: 'update:isNationwide', v: boolean): void
}>()

const displayRegion = computed(() => {
  if (!props.modelValue) return ''
  const { province, city, area } = props.modelValue
  return [province, city, area].filter(Boolean).join(' / ')
})

const isNationwide = computed(() => props.isNationwide)

function onNationwideChange(e: any) {
  const val = Boolean(e.detail.value)
  emit('update:isNationwide', val)
  if (val) emit('update:modelValue', null)
}

function openPicker() {
  // 简化：使用 uni picker
  uni.showActionSheet({
    itemList: ['北京市 / 北京市 / 东城区', '上海市 / 上海市 / 黄浦区', '广州市 / 广东省 / 天河区', '深圳市 / 广东省 / 南山区'],
    success: (res) => {
      const map: Record<number, { province: string; city: string; area: string }> = {
        0: { province: '北京市', city: '北京市', area: '东城区' },
        1: { province: '上海市', city: '上海市', area: '黄浦区' },
        2: { province: '广东省', city: '广州市', area: '天河区' },
        3: { province: '广东省', city: '深圳市', area: '南山区' },
      }
      const v = map[res.tapIndex]
      if (v) emit('update:modelValue', v)
    },
  })
}
</script>

<style lang="scss" scoped>
.region-selector {
  background: $color-bg-card;
  border-radius: 32rpx;
  padding: 8rpx 32rpx;
  &__row {
    display: flex;
    align-items: center;
    height: 96rpx;
    border-bottom: 2rpx solid $color-border;
    &:last-child { border-bottom: none; }
  }
  &__label {
    flex: 0 0 160rpx;
    font-size: 28rpx;
    color: $color-text-primary;
  }
  &__value {
    flex: 1;
    font-size: 28rpx;
    color: $color-text-primary;
  }
}
</style>
