<template>
  <view class="login-page">
    <view class="login-brand">
      <view class="login-logo">
        <KeepIcon name="camera" :size="86" color="#FFFFFF" />
      </view>
      <text class="login-title">爱去拍</text>
      <text class="login-subtitle">遇见同频的拍摄伙伴</text>
    </view>

    <view class="login-body">
      <!-- #ifdef MP-WEIXIN -->
      <button
        class="wx-button"
        open-type="getPhoneNumber"
        :disabled="submitting"
        @getphonenumber="handleWechatPhoneAuthorization"
      >
        <KeepIcon name="phone" :size="38" color="#FFFFFF" />
        <text>{{ submitting ? '登录中...' : '微信一键登录' }}</text>
      </button>
      <text class="login-tip">登录需要微信手机号授权，用于匹配已有账号并保障交易安全。</text>
      <!-- #endif -->

      <!-- #ifdef H5 -->
      <view class="unsupported-card">
        <text class="unsupported-card__title">H5 登录暂未开放</text>
        <text class="unsupported-card__desc">当前后端只支持微信小程序加密手机号登录，请在微信小程序中使用登录能力。</text>
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
        <text class="login-agreement__text">登录即代表同意</text>
        <text class="login-agreement__link" @tap="goAgreement">《用户协议》</text>
        <text class="login-agreement__text">与</text>
        <text class="login-agreement__link" @tap="goPrivacy">《隐私政策》</text>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useUserStore } from '@/stores/user'
import KeepIcon from '@/components/business/KeepIcon.vue'

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
@import '@/styles/mixins.scss';

.login-page {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  padding: 0 60rpx;
  background: #fff;
}

.login-brand {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding-top: 240rpx;
}

.login-logo {
  @include flex-center;
  width: 168rpx;
  height: 168rpx;
  margin-bottom: 44rpx;
  border-radius: 52rpx;
  background: $color-primary;
  box-shadow: $shadow-keep-button;
}

.login-title {
  color: $color-text-primary;
  font-size: 56rpx;
  font-weight: 900;
  letter-spacing: 6rpx;
}

.login-subtitle,
.login-tip,
.unsupported-card__desc {
  margin-top: 18rpx;
  color: $color-text-secondary;
  font-size: 28rpx;
  line-height: 1.7;
  text-align: center;
}

.login-body {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: flex-end;
  padding-bottom: 48rpx;
}

.wx-button {
  @include keep-primary-button;
  width: 100%;
  gap: 14rpx;
  line-height: 104rpx;
}

.wx-button[disabled] {
  opacity: 0.65;
}

.unsupported-card {
  padding: 48rpx 36rpx;
  border-radius: $radius-keep-card;
  background: $color-bg-page;
  text-align: center;
}

.unsupported-card__title {
  display: block;
  color: $color-text-primary;
  font-size: 32rpx;
  font-weight: 800;
}

.login-footer {
  padding: 24rpx 0 calc(54rpx + env(safe-area-inset-bottom));
}

.login-agreement {
  display: flex;
  align-items: center;
  justify-content: center;
  flex-wrap: wrap;
}

.login-agreement__check {
  @include flex-center;
  width: 36rpx;
  height: 36rpx;
  margin-right: 10rpx;
  border: 2rpx solid $color-border;
  border-radius: 50%;
}

.login-agreement__check--active {
  border-color: $color-primary;
  background: $color-primary;
}

.login-agreement__checkmark {
  color: #fff;
  font-size: 20rpx;
}

.login-agreement__text,
.login-agreement__link {
  font-size: 24rpx;
}

.login-agreement__text {
  color: $color-text-secondary;
}

.login-agreement__link {
  color: $color-text-primary;
  font-weight: 800;
}
</style>
