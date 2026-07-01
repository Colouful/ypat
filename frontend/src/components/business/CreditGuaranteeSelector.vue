<template>
  <view class="credit-selector">
    <view v-for="opt in options" :key="opt.value"
          :class="['credit-selector__item', { 'credit-selector__item--active': modelValue === opt.value }]"
          @tap="select(opt.value)">
      <text class="credit-selector__text">{{ opt.label }}</text>
    </view>
  </view>
</template>

<script setup lang="ts">
defineProps<{
  modelValue: '0' | '1'
}>()

const emit = defineEmits<{
  (e: 'update:modelValue', v: '0' | '1'): void
}>()

const options: Array<{ value: '0' | '1'; label: string }> = [
  { value: '0', label: '不要求对方存入保证金' },
  { value: '1', label: '要求对方存入保证金' },
]

function select(v: '0' | '1') {
  emit('update:modelValue', v)
}
</script>

<style lang="scss" scoped>
.credit-selector {
  background: $color-bg-card;
  border-radius: 32rpx;
  overflow: hidden;
  &__item {
    display: flex;
    align-items: center;
    height: 96rpx;
    padding: 0 32rpx;
    border-bottom: 2rpx solid $color-border;
    &:last-child { border-bottom: none; }
    &--active {
      background: $color-primary-light;
      .credit-selector__text {
        color: $color-primary-dark;
        font-weight: 500;
      }
    }
  }
  &__text {
    font-size: 28rpx;
    color: $color-text-primary;
  }
}
</style>
