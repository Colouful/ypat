<template>
  <view class="work-waterfall">
    <view class="work-waterfall__column" :style="{ width: columnWidth }">
      <view v-for="(item, i) in leftItems" :key="`l-${item.id || i}`" class="work-waterfall__item">
        <slot :item="item" :column="'left'" :index="i" />
      </view>
    </view>
    <view class="work-waterfall__column" :style="{ width: columnWidth, marginLeft: gap }">
      <view v-for="(item, i) in rightItems" :key="`r-${item.id || i}`" class="work-waterfall__item">
        <slot :item="item" :column="'right'" :index="i" />
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'

const props = withDefaults(defineProps<{
  items: any[]
  ratioThreshold?: number  // 大于 1 时倾向左列；小于 1 时倾向右列
}>(), {
  ratioThreshold: 1,
})

const columnWidth = ref('calc(50% - 8rpx)')
const gap = ref('16rpx')

// 简化分配：按奇偶交替
const leftItems = computed(() => props.items.filter((_, i) => i % 2 === 0))
const rightItems = computed(() => props.items.filter((_, i) => i % 2 === 1))
</script>

<style lang="scss" scoped>
.work-waterfall {
  display: flex;
  width: 100%;
  &__column {
    flex: 0 0 50%;
  }
  &__item {
    width: 100%;
  }
}
</style>
