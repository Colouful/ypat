<template>
  <view class="region-picker-page">
    <KeepPageNav title="选择地区" />
    <view class="region-picker-page__list">
      <view v-for="opt in options" :key="opt.label"
            :class="['region-picker-page__item', { 'region-picker-page__item--active': selected === opt.label }]"
            @tap="onSelect(opt)">
        <text>{{ opt.label }}</text>
        <KeepIcon v-if="selected === opt.label" name="check" :size="20" color="#23C268" />
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import KeepPageNav from '@/components/business/KeepPageNav.vue'
import KeepIcon from '@/components/business/KeepIcon.vue'

defineProps<{
  selected?: string
}>()

const emit = defineEmits<{
  (e: 'select', value: { province: string; city: string; area: string; label: string }): void
}>()

const options = [
  { label: '北京市 / 北京市 / 东城区', province: '北京市', city: '北京市', area: '东城区' },
  { label: '上海市 / 上海市 / 黄浦区', province: '上海市', city: '上海市', area: '黄浦区' },
  { label: '广州市 / 广东省 / 天河区', province: '广东省', city: '广州市', area: '天河区' },
  { label: '深圳市 / 广东省 / 南山区', province: '广东省', city: '深圳市', area: '南山区' },
  { label: '杭州市 / 浙江省 / 西湖区', province: '浙江省', city: '杭州市', area: '西湖区' },
  { label: '成都市 / 四川省 / 锦江区', province: '四川省', city: '成都市', area: '锦江区' },
]

function onSelect(opt: any) {
  emit('select', opt)
}
</script>

<style lang="scss" scoped>
.region-picker-page {
  min-height: 100vh;
  background: $color-bg-page;
  &__list {
    background: $color-bg-card;
    margin-top: 16rpx;
  }
  &__item {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 24rpx 32rpx;
    border-bottom: 2rpx solid $color-border;
    font-size: 28rpx;
    color: $color-text-primary;
    &--active { color: $color-primary-dark; }
  }
}
</style>
