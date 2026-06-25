<template>
  <view class="login-page">
    <view class="login-header">
      <image class="login-logo" src="/static/tab/home-active.png" mode="aspectFit" />
      <text class="login-title">爱去拍</text>
      <text class="login-subtitle">摄影约拍撮合平台</text>
    </view>

    <view class="login-body">
      <!-- #ifdef MP-WEIXIN -->
      <button
        class="login-btn login-btn--primary"
        open-type="getPhoneNumber"
        :disabled="submitting"
        @getphonenumber="handleWechatPhoneAuthorization"
      >
        <text class="login-btn__text">{{ submitting ? '登录中...' : '微信手机号授权登录' }}</text>
      </button>
      <text class="login-tip">登录需要微信手机号授权，用于匹配已有账号并保障交易安全。</text>
      <!-- #endif -->

      <!-- #ifdef H5 -->
      <view class="unsupported-card">
        <text class="unsupported-card__title">H5 登录暂未开放</text>
        <text class="unsupported-card__desc">当前后端只支持微信小程序加密手机号登录，避免使用无效的手机号直登流程。</text>
      </view>
      <!-- #endif -->

      <!-- #ifdef APP-PLUS -->
      <view class="unsupported-card">
        <text class="unsupported-card__title">App 微信登录暂未开放</text>
        <text class="unsupported-card__desc">App 授权数据与当前小程序解密接口不兼容，需后端增加 App OAuth 登录能力后启用。</text>
      </view>
      <!-- #endif -->
    </view>

    <view class="login-footer">
      <view class="login-agreement">
        <view
          class="login-agreement__check"
          :class="{ 'login-agreement__check--active': agreed }"
          @tap="agreed = !agreed"
        >
          <text v-if="agreed" class="login-agreement__checkmark">✓</text>
        </view>
        <text class="login-agreement__text">我已阅读并同意</text>
        <text class="login-agreement__link" @tap="goAgreement">《用户协议》</text>
        <text class="login-agreement__text">和</text>
        <text class="login-agreement__link" @tap="goPrivacy">《隐私政策》</text>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useUserStore } from '@/stores/user'

interface PhoneAuthorizationEvent {
  detail?: {
    errMsg?: string
    encryptedData?: string
    iv?: string
  }
}

const userStore = useUserStore()
const agreed = ref(false)
const submitting = ref(false)

async function handleWechatPhoneAuthorization(event: PhoneAuthorizationEvent): Promise<void> {
  if (!agreed.value) {
    uni.showToast({ title: '请先同意用户协议和隐私政策', icon: 'none' })
    return
  }

  const detail = event.detail
  if (!detail?.errMsg?.includes('ok')) {
    uni.showToast({ title: '你已取消手机号授权', icon: 'none' })
    return
  }

  if (!detail.encryptedData || !detail.iv) {
    uni.showModal({
      title: '当前微信授权方式不兼容',
      content: '现有后端需要 encryptedData 和 iv 解密手机号，请升级后端到微信手机号 code 换取接口后再重试。',
      showCancel: false,
    })
    return
  }

  submitting.value = true
  try {
    const loginResult = await new Promise<UniApp.LoginRes>((resolve, reject) => {
      uni.login({ provider: 'weixin', success: resolve, fail: reject })
    })

    await userStore.login({
      code: loginResult.code,
      encryptedData: detail.encryptedData,
      iv: detail.iv,
      channel: '0',
    })

    uni.showToast({ title: '登录成功', icon: 'success' })
    const pages = getCurrentPages()
    if (pages.length > 1) uni.navigateBack()
    else uni.switchTab({ url: '/pages/home/index' })
  } catch (error) {
    const message = error instanceof Error ? error.message : '登录失败，请重试'
    uni.showToast({ title: message, icon: 'none' })
  } finally {
    submitting.value = false
  }
}

function goAgreement(): void {
  uni.navigateTo({ url: '/pages-sub/content/agreement' })
}

function goPrivacy(): void {
  uni.navigateTo({ url: '/pages-sub/content/privacy' })
}
</script>

<style lang="scss">
@import '@/styles/tokens.scss';

.login-page {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  padding: 0 $spacing-xl;
  background: #fff;
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
}

.login-subtitle,
.login-tip,
.unsupported-card__desc {
  margin-top: $spacing-sm;
  font-size: $font-size-sm;
  line-height: 1.7;
  color: $color-text-secondary;
  text-align: center;
}

.login-body {
  flex: 1;
}

.login-btn {
  width: 100%;
  height: 96rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 0;
  border-radius: $radius-xl;
  background: $color-primary;

  &::after { border: 0; }
  &[disabled] { opacity: .65; }

  &__text {
    color: #fff;
    font-size: $font-size-lg;
    font-weight: $font-weight-medium;
  }
}

.unsupported-card {
  padding: $spacing-xl;
  border-radius: $radius-lg;
  background: $color-bg-page;
  text-align: center;

  &__title {
    display: block;
    font-size: $font-size-lg;
    font-weight: $font-weight-semibold;
    color: $color-text-primary;
  }
}

.login-footer {
  padding: $spacing-xl 0 calc(env(safe-area-inset-bottom) + 40rpx);
}

.login-agreement {
  display: flex;
  align-items: center;
  justify-content: center;
  flex-wrap: wrap;

  &__check {
    width: 36rpx;
    height: 36rpx;
    margin-right: $spacing-xs;
    display: flex;
    align-items: center;
    justify-content: center;
    border: 2rpx solid $color-border;
    border-radius: 50%;

    &--active {
      background: $color-primary;
      border-color: $color-primary;
    }
  }

  &__checkmark { color: #fff; font-size: 20rpx; }
  &__text { color: $color-text-secondary; font-size: $font-size-xs; }
  &__link { color: $color-primary; font-size: $font-size-xs; }
}
</style>
