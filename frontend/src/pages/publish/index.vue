<template>
  <view class="page">
    <view v-if="!userStore.isLoggedIn" class="login-card">
      <text class="login-card__title">登录后发布约拍</text>
      <text class="login-card__desc">完成登录后即可发布拍摄需求并接收报名消息。</text>
      <button class="login-card__button" @tap="goLogin">去登录</button>
    </view>
    <YpatPublishForm v-else />
  </view>
</template>

<script setup lang="ts">
import { onShow } from '@dcloudio/uni-app'
import { useUserStore } from '@/stores/user'
import YpatPublishForm from '@/components/business/YpatPublishForm.vue'

const userStore = useUserStore()

function goLogin(): void {
  uni.navigateTo({ url: '/pages/login/index' })
}

onShow(() => {
  if (userStore.isLoggedIn) userStore.updateUserInfo().catch(() => undefined)
})
</script>

<style scoped lang="scss">
.page { min-height: 100vh; background: #f7f8fa; }
.login-card { margin: 40rpx 28rpx; padding: 48rpx 32rpx; border-radius: 28rpx; background: #fff; text-align: center; }
.login-card__title { display: block; color: #1d2433; font-size: 34rpx; font-weight: 600; }
.login-card__desc { display: block; margin: 18rpx 0 30rpx; color: #7c8593; font-size: 26rpx; }
.login-card__button { color: #fff; background: #23c268; border-radius: 44rpx; }
.login-card__button::after { border: 0; }
</style>
