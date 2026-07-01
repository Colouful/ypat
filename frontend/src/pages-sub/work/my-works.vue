<template>
  <view class="my-works-page">
    <KeepPageNav title="我的作品" />
    <view class="my-works-page__list">
      <WorkWaterfall :items="items">
        <template #default="{ item }">
          <WorkCard :work="item" @tap="goDetail(item)" />
        </template>
      </WorkWaterfall>
      <view v-if="!loading && items.length === 0" class="my-works-page__empty">暂无作品</view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import KeepPageNav from '@/components/business/KeepPageNav.vue'
import WorkCard from '@/components/business/WorkCard.vue'
import WorkWaterfall from '@/components/business/WorkWaterfall.vue'
import { getMyWorks } from '@/api/modules/work'
import type { WorkListItem } from '@/api/types/work'

const items = ref<WorkListItem[]>([])
const loading = ref(true)

onMounted(async () => {
  try {
    const res = await getMyWorks({ page: 1, size: 50 })
    const data = (res && res.data) || { items: [] }
    items.value = data.items || []
  } catch (e) {
    items.value = []
  } finally {
    loading.value = false
  }
})

function goDetail(item: WorkListItem) {
  uni.navigateTo({ url: `/pages-sub/work/detail?id=${item.id}` })
}
</script>

<style lang="scss" scoped>
.my-works-page {
  min-height: 100vh;
  background: $color-bg-page;
  &__list { padding: 16rpx 24rpx; }
  &__empty {
    text-align: center;
    padding: 96rpx 0;
    color: $color-text-helper;
    font-size: 26rpx;
  }
}
</style>
