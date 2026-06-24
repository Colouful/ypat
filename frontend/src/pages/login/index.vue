<template>
  <view class="login-page">
    <view class="login-header">
      <image class="login-logo" src="/static/tab/home-active.png" mode="aspectFit" />
      <text class="login-title">爱去拍</text>
      <text class="login-subtitle">摄影约拍撮合平台</text>
    </view>

    <view class="login-body">
      <!-- #ifdef MP-WEIXIN -->
      <button class="login-btn login-btn--primary" @tap="handleWxLogin">
        <text class="login-btn__text">微信授权登录</text>
      </button>
      <!-- #endif -->

      <!-- #ifdef H5 -->
      <view class="login-form">
        <view class="login-form__item">
          <input
            v-model="phone"
            class="login-form__input"
            type="number"
            maxlength="11"
            placeholder="请输入手机号"
            placeholder-class="login-form__placeholder"
          />
        </view>
        <button class="login-btn login-btn--primary" :disabled="!isPhoneValid || submitting" @tap="handlePhoneLogin">
          <text class="login-btn__text">{{ submitting ? '登录中...' : '登录' }}</text>
        </button>
      </view>
      <!-- #endif -->

      <!-- #ifdef APP-PLUS -->
      <button class="login-btn login-btn--primary" @tap="handleWxLogin">
        <text class="login-btn__text">微信登录</text>
      </button>
      <!-- #endif -->
    </view>

    <view class="login-footer">
      <view class="login-agreement">
        <view class="login-agreement__check" :class="{ 'login-agreement__check--active': agreed }" @tap="agreed = !agreed">
          <text v-if="agreed" class="login-agreement__checkmark">✓</text>
        </view>
        <text class="login-agreement__text">
          我已阅读并同意
        </text>
        <text class="login-agreement__link" @tap="goAgreement">《用户协议》</text>
        <text class="login-agreement__text">和</text>
        <text class="login-agreement__link" @tap="goPrivacy">《隐私政策》</text>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()

const phone = ref('')
const agreed = ref(false)
const submitting = ref(false)

const isPhoneValid = computed(() => /^1[3-9]\d{9}$/.test(phone.value))

async function handleWxLogin() {
  if (!agreed.value) {
    uni.showToast({ title: '请先同意用户协议', icon: 'none' })
    return
  }

  try {
    submitting.value = true
    // #ifdef MP-WEIXIN
    const loginRes = await new Promise<UniApp.LoginRes>((resolve, reject) => {
      uni.login({ provider: 'weixin', success: resolve, fail: reject })
    })

    await userStore.login({ code: loginRes.code, channel: '0' })
    handleLoginSuccess()
    // #endif
  } catch (err: any) {
    uni.showToast({ title: err?.message || '登录失败，请重试', icon: 'none' })
  } finally {
    submitting.value = false
  }
}

async function handlePhoneLogin() {
  if (!agreed.value) {
    uni.showToast({ title: '请先同意用户协议', icon: 'none' })
    return
  }
  if (!isPhoneValid.value) {
    uni.showToast({ title: '请输入正确的手机号', icon: 'none' })
    return
  }

  try {
    submitting.value = true
    await userStore.login({ mobile: phone.value, channel: '2' })
    handleLoginSuccess()
  } catch (err: any) {
    uni.showToast({ title: err?.message || '登录失败，请重试', icon: 'none' })
  } finally {
    submitting.value = false
  }
}

function handleLoginSuccess() {
  const pages = getCurrentPages()
  if (pages.length > 1) {
    uni.navigateBack()
  } else {
    uni.switchTab({ url: '/pages/home/index' })
  }
}

function goAgreement() {
  uni.navigateTo({ url: '/pages-sub/content/agreement' })
}

function goPrivacy() {
  uni.navigateTo({ url: '/pages-sub/content/privacy' })
}
</script>

<style lang="scss">
@import '@/styles/tokens.scss';

.login-page {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background-color: #fff;
  padding: 0 $spacing-xl;
}

.login-header {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding-top: 200rpx;
  margin-bottom: 100rpx;
}

.login-logo {
  width: 160rpx;
  height: 160rpx;
  margin-bottom: $spacing-lg;
}

.login-title {
  font-size: 56rpx;
  font-weight: $font-weight-bold;
  color: $color-text-primary;
  margin-bottom: $spacing-sm;
}

.login-subtitle {
  font-size: $font-size-base;
  color: $color-text-secondary;
}

.login-body {
  flex: 1;
}

.login-form {
  margin-bottom: $spacing-xl;

  &__item {
    border-bottom: 2rpx solid $color-border;
    padding: $spacing-md 0;
    margin-bottom: $spacing-lg;
  }

  &__input {
    font-size: $font-size-lg;
    color: $color-text-primary;
  }

  &__placeholder {
    color: $color-text-helper;
  }
}

.login-btn {
  width: 100%;
  height: 96rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: $radius-xl;
  border: none;

  &--primary {
    background-color: $color-primary;
  }

  &[disabled] {
    background-color: $color-primary-light;
  }

  &__text {
    font-size: $font-size-lg;
    font-weight: $font-weight-medium;
    color: #fff;
  }

  &::after {
    border: none;
  }
}

.login-footer {
  padding: $spacing-xl 0;
  padding-bottom: calc(env(safe-area-inset-bottom) + 40rpx);
}

.login-agreement {
  display: flex;
  align-items: center;
  justify-content: center;
  flex-wrap: wrap;

  &__check {
    width: 36rpx;
    height: 36rpx;
    border: 2rpx solid $color-border;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    margin-right: $spacing-xs;

    &--active {
      background-color: $color-primary;
      border-color: $color-primary;
    }
  }

  &__checkmark {
    font-size: 20rpx;
    color: #fff;
  }

  &__text {
    font-size: $font-size-xs;
    color: $color-text-secondary;
  }

  &__link {
    font-size: $font-size-xs;
    color: $color-primary;
  }
}
</style>
