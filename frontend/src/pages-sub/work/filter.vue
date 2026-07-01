<template>
  <view class="work-filter-page">
    <KeepPageNav title="作品筛选" />
    <WorkFilterPanel v-model:visible="visible" v-model="filterValue" @confirm="onConfirm" @reset="onReset" />
    <view class="work-filter-page__content">
      <text>筛选内容已通过侧边栏打开</text>
    </view>
  </view>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import KeepPageNav from '@/components/business/KeepPageNav.vue'
import WorkFilterPanel from '@/components/business/WorkFilterPanel.vue'

const visible = ref(false)
const filterValue = ref({ region: '', gender: '', profession: '' })

onMounted(() => {
  visible.value = true
})

function onConfirm() {
  uni.navigateBack()
}
function onReset() {
  filterValue.value = { region: '', gender: '', profession: '' }
}
</script>

<style lang="scss" scoped>
.work-filter-page {
  min-height: 100vh;
  background: $color-bg-page;
  &__content {
    padding: 32rpx;
    color: $color-text-helper;
  }
}
</style>
