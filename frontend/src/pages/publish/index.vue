<template>
  <view class="publish-page">
    <view class="publish-nav" :style="{ paddingTop: statusBarHeight + 'px' }">
      <view class="publish-nav__back" @tap="goHome">
        <KeepIcon name="chevron-left" :size="42" />
      </view>
      <text class="publish-nav__title">发布约拍</text>
      <view class="publish-nav__placeholder" />
    </view>

    <view v-if="!userStore.isLoggedIn" class="publish-login">
      <KeepState
        type="login"
        title="登录后发布约拍"
        description="完成微信登录后即可发布拍摄需求并接收报名消息。"
        button-text="去登录"
        @action="goLogin"
      />
    </view>
    <YpatPublishForm v-else />
  </view>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { useAppStore } from '@/stores/app'
import { useUserStore } from '@/stores/user'
import KeepIcon from '@/components/business/KeepIcon.vue'
import KeepState from '@/components/business/KeepState.vue'
import YpatPublishForm from '@/components/business/YpatPublishForm.vue'

const userStore = useUserStore()
const appStore = useAppStore()
const statusBarHeight = computed(() => appStore.statusBarHeight)

function goLogin(): void {
  uni.navigateTo({ url: '/pages/login/index' })
}

function goHome(): void {
  uni.switchTab({ url: '/pages/home/index' })
}

onShow(() => {
  try { uni.hideTabBar({ animation: false }) } catch(e) {}
  if (userStore.isLoggedIn) userStore.updateUserInfo().catch(() => undefined)
})
</script>

<style scoped lang="scss">
@import '@/styles/tokens.scss';
@import '@/styles/mixins.scss';

.publish-page {
  min-height: 100vh;
  background: $color-bg-page;
}

.publish-nav {
  display: flex;
  align-items: center;
  height: 112rpx;
  padding-right: 36rpx;
  padding-left: 36rpx;
  background: $color-bg-page;
}

.publish-nav__back,
.publish-nav__placeholder {
  @include flex-center;
  width: 72rpx;
  height: 72rpx;
}

.publish-nav__back {
  border-radius: 50%;
  background: $color-bg-chip;
}

.publish-nav__title {
  flex: 1;
  color: $color-text-primary;
  font-size: 38rpx;
  font-weight: 800;
  text-align: center;
}

.publish-login {
  padding: 36rpx;
}
</style>
