<template>
  <view class="tag-picker-page">
    <KeepPageNav title="选择标签" />
    <view class="tag-picker-page__grid">
      <view v-for="t in tags" :key="t.id"
            :class="['tag-picker-page__chip', { 'tag-picker-page__chip--active': selectedIds.includes(t.id) }]"
            @tap="toggle(t.id)">
        <text>{{ t.name }}</text>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'
import KeepPageNav from '@/components/business/KeepPageNav.vue'
import { getWorkTags } from '@/api/modules/dict'
import type { WorkTag } from '@/api/types/work'

const props = withDefaults(defineProps<{
  selectedIds: number[]
  maxSelect?: number
}>(), {
  maxSelect: 5,
})

const emit = defineEmits<{
  (e: 'update:selectedIds', v: number[]): void
  (e: 'confirm', v: number[]): void
}>()

const tags = ref<WorkTag[]>([])

watch(() => props.selectedIds, () => {}, { deep: true })

import { onMounted } from 'vue'
onMounted(async () => {
  try {
    const res = await getWorkTags()
    const data = (res && res.data) || []
    tags.value = data || []
  } catch {
    tags.value = []
  }
})

function toggle(id: number) {
  const isSel = props.selectedIds.includes(id)
  let next: number[]
  if (isSel) {
    next = props.selectedIds.filter((x) => x !== id)
  } else {
    if (props.selectedIds.length >= props.maxSelect) {
      uni.showToast({ title: `最多选择 ${props.maxSelect} 个`, icon: 'none' })
      return
    }
    next = [...props.selectedIds, id]
  }
  emit('update:selectedIds', next)
  emit('confirm', next)
}
</script>

<style lang="scss" scoped>
.tag-picker-page {
  min-height: 100vh;
  background: $color-bg-page;
  padding: 16rpx 24rpx;
  &__grid {
    display: flex;
    flex-wrap: wrap;
    gap: 16rpx;
  }
  &__chip {
    padding: 12rpx 28rpx;
    background: $color-bg-card;
    border-radius: 999rpx;
    font-size: 26rpx;
    color: $color-text-primary;
    &--active {
      background: $color-primary-light;
      color: $color-primary-dark;
      font-weight: 500;
    }
  }
}
</style>
