<template>
  <view class="work-publish-page">
    <KeepPageNav title="发布作品" />
    <WorkPublishForm @submitted="onSubmitted" @error="onError" />
  </view>
</template>

<script setup lang="ts">
import KeepPageNav from '@/components/business/KeepPageNav.vue'
import WorkPublishForm from '@/components/business/WorkPublishForm.vue'

function onSubmitted() {
  setTimeout(() => uni.navigateBack(), 1500)
}
function onError(_msg: string) {
  // toast 已由组件内显示
}
</script>

<style lang="scss" scoped>
.work-publish-page {
  min-height: 100vh;
  background: $color-bg-page;
}
</style>
