<template>
  <view class="publish-page">
    <KeepPageNav title="发布" />
    <view v-if="!userStore.isLoggedIn" class="publish-login">
      <KeepState
        type="login"
        title="登录后发布"
        description="完成微信登录后即可发布约拍需求或作品。"
        button-text="去登录"
        @action="goLogin"
      />
    </view>
    <PublishEntry v-else @appointment="goAppointment" @work="goWork" />
    <KeepTabBar active="publish" />
  </view>
</template>

<script setup lang="ts">
import KeepPageNav from '@/components/business/KeepPageNav.vue'
import KeepState from '@/components/business/KeepState.vue'
import KeepTabBar from '@/components/business/KeepTabBar.vue'
import PublishEntry from '@/components/business/PublishEntry.vue'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()

function goLogin(): void {
  uni.navigateTo({ url: `/pages/login/index?redirect=${encodeURIComponent('/pages/publish/index')}` })
}

function goAppointment() {
  uni.navigateTo({ url: '/pages-sub/publish/target-select' })
}
function goWork() {
  uni.navigateTo({ url: '/pages-sub/work/publish' })
}
</script>

<style scoped lang="scss">
.publish-page {
  box-sizing: border-box;
  min-height: 100vh;
  padding-bottom: calc(148rpx + env(safe-area-inset-bottom));
  background: $color-bg-page;
}
.publish-login {
  padding: 36rpx;
}
</style>
