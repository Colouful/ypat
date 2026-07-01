<template>
  <view class="publish-entry-page">
    <KeepPageNav title="发布" />
    <PublishEntry @appointment="goAppointment" @work="goWork" />
  </view>
</template>

<script setup lang="ts">
import KeepPageNav from '@/components/business/KeepPageNav.vue'
import PublishEntry from '@/components/business/PublishEntry.vue'

function goAppointment() {
  uni.navigateTo({ url: '/pages-sub/publish/target-select' })
}
function goWork() {
  uni.navigateTo({ url: '/pages-sub/work/publish' })
}
</script>

<style lang="scss" scoped>
.publish-entry-page {
  min-height: 100vh;
  background: $color-bg-page;
  padding-top: 32rpx;
}
</style>
