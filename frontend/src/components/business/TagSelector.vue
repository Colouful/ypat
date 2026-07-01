<template>
  <view class="tag-selector">
    <view v-for="tag in tags" :key="tag.id"
          :class="['tag-selector__item', { 'tag-selector__item--active': selectedIds.includes(tag.id) }]"
          @tap="toggle(tag)">
      <text class="tag-selector__text">{{ tag.name }}</text>
    </view>
    <text v-if="tags.length === 0" class="tag-selector__empty">暂无标签</text>
  </view>
</template>

<script setup lang="ts">
import type { WorkTag } from '@/api/types/work'

const props = defineProps<{
  tags: WorkTag[]
  selectedIds: number[]
  maxSelect?: number
}>()

const emit = defineEmits<{
  (e: 'update:selectedIds', value: number[]): void
  (e: 'change', value: number[]): void
}>()

const max = props.maxSelect || 5

function toggle(tag: WorkTag) {
  if (!tag || tag.id === undefined) return
  const isSelected = props.selectedIds.includes(tag.id)
  let next: number[]
  if (isSelected) {
    next = props.selectedIds.filter((id) => id !== tag.id)
  } else {
    if (props.selectedIds.length >= max) {
      uni.showToast({ title: `最多选择 ${max} 个标签`, icon: 'none' })
      return
    }
    next = [...props.selectedIds, tag.id]
  }
  emit('update:selectedIds', next)
  emit('change', next)
}
</script>

<style lang="scss" scoped>
.tag-selector {
  display: flex;
  flex-wrap: wrap;
  gap: 16rpx;
  padding: 24rpx 0;
  &__item {
    padding: 12rpx 28rpx;
    background: $color-bg-chip;
    border-radius: 999rpx;
    font-size: 26rpx;
    color: $color-text-primary;
    transition: all 0.2s;
    &--active {
      background: $color-primary-light;
      color: $color-primary-dark;
      font-weight: 500;
    }
  }
  &__empty {
    color: $color-text-helper;
    font-size: 26rpx;
  }
}
</style>
