<template>
  <view class="publish-page">
    <view class="publish-nav" :style="{ paddingTop: statusBarHeight + 'px' }">
      <view class="publish-nav__placeholder" />
      <text class="publish-nav__title">发布约拍</text>
      <view class="publish-nav__placeholder" />
    </view>

    <view class="publish-hero">
      <text class="publish-hero__eyebrow">发布约拍</text>
      <text class="publish-hero__title">把拍摄需求说清楚，更快遇到合适伙伴</text>
      <text class="publish-hero__desc">补充风格、城市、日期和样片，系统会优先展示完整度更高的内容。</text>
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
    <KeepTabBar active="publish" :unread-count="userStore.unreadCount" />
  </view>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { useAppStore } from '@/stores/app'
import { useUserStore } from '@/stores/user'
import KeepState from '@/components/business/KeepState.vue'
import KeepTabBar from '@/components/business/KeepTabBar.vue'
import YpatPublishForm from '@/components/business/YpatPublishForm.vue'

const userStore = useUserStore()
const appStore = useAppStore()
const statusBarHeight = computed(() => appStore.statusBarHeight)

function goLogin(): void {
  uni.navigateTo({ url: '/pages/login/index' })
}

onShow(() => {
  if (userStore.isLoggedIn) userStore.updateUserInfo().catch(() => undefined)
})
</script>

<style scoped lang="scss">

.publish-page {
  box-sizing: border-box;
  min-height: 100vh;
  padding-bottom: calc(168rpx + env(safe-area-inset-bottom));
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

.publish-nav__placeholder {
  @include flex-center;
  width: 72rpx;
  height: 72rpx;
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

.publish-hero {
  margin: 12rpx 36rpx 28rpx;
  padding: 34rpx;
  border-radius: $radius-keep-card;
  color: #fff;
  background: linear-gradient(135deg, #1F242B, #2D3E35);
}

.publish-hero__eyebrow,
.publish-hero__title,
.publish-hero__desc {
  display: block;
}

.publish-hero__eyebrow {
  color: $color-primary-light;
  font-size: 24rpx;
  font-weight: 900;
}

.publish-hero__title {
  margin-top: 12rpx;
  font-size: 38rpx;
  font-weight: 900;
  line-height: 1.32;
}

.publish-hero__desc {
  margin-top: 14rpx;
  color: rgba(255, 255, 255, 0.72);
  font-size: 25rpx;
  font-weight: 700;
  line-height: 1.5;
}
</style>
