<template>
  <view class="login-page">
    <KeepPageNav title="登录" />
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
      <view class="phone-card">
        <view class="phone-field">
          <KeepIcon name="phone" :size="34" color="#83888F" />
          <input
            v-model="mobile"
            class="phone-input"
            type="number"
            maxlength="11"
            placeholder="请输入手机号"
            placeholder-class="phone-placeholder"
          />
        </view>
        <view class="phone-field">
          <KeepIcon name="shield" :size="34" color="#83888F" />
          <input
            v-model="smsCode"
            class="phone-input"
            type="number"
            maxlength="6"
            placeholder="请输入验证码"
            placeholder-class="phone-placeholder"
          />
          <button class="code-button" :disabled="codeSending || countdown > 0" @tap="handleSendH5Code">
            {{ countdown > 0 ? `${countdown}s` : '获取验证码' }}
          </button>
        </view>
        <button class="wx-button phone-login-button" :disabled="submitting" @tap="handleH5PhoneLogin">
          <KeepIcon name="phone" :size="38" color="#FFFFFF" />
          <text>{{ submitting ? '登录中...' : '手机号登录' }}</text>
        </button>
        <text v-if="debugCode && isDevEnv" class="debug-code">开发验证码：{{ debugCode }}</text>
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
          <KeepIcon v-if="agreed" name="check" :size="22" color="#FFFFFF" :stroke-width="3" />
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
import { onLoad } from '@dcloudio/uni-app'
import { useUserStore } from '@/stores/user'
import KeepIcon from '@/components/business/KeepIcon.vue'
import { goRootTab, isRootTabUrl } from '@/utils/tab-navigation'
import { isPhone } from '@/utils/validate'
import { isProfileComplete } from '@/utils/profile'
import {
  captureInviteFromQuery,
  consumeInviteContext,
  getInviteContext,
} from '@/services/invite-context'
import type { UserInfo } from '@/api/types'

const isDevEnv = import.meta.env.DEV

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
const codeSending = ref(false)
const countdown = ref(0)
const mobile = ref('')
const smsCode = ref('')
const debugCode = ref('')
const redirectUrl = ref('')
let countdownTimer: ReturnType<typeof setInterval> | null = null

// 登录成功后的回跳：资料不完整(对齐旧版 getNextUrl)→ 引导完善资料；
// 否则保留原目标回跳(登录回跳)。完善资料后会继续走 redirect，因此
// 邀请关系绑定结果与目标页跳转互不阻塞。
function redirectAfterLogin(user: UserInfo): void {
  consumeInviteContext(user.mobile)
  uni.showToast({ title: '登录成功', icon: 'success' })

  if (!isProfileComplete(user)) {
    const url = redirectUrl.value
      ? `/pages-sub/user/complete-info?redirect=${encodeURIComponent(redirectUrl.value)}`
      : '/pages-sub/user/complete-info'
    setTimeout(() => uni.redirectTo({ url }), 600)
    return
  }

  if (redirectUrl.value) {
    const target = redirectUrl.value
    setTimeout(() => {
      if (isRootTabUrl(target)) goRootTab(target)
      else uni.redirectTo({ url: target })
    }, 600)
    return
  }

  const pages = getCurrentPages()
  if (pages.length > 1) uni.navigateBack()
  else goRootTab('/pages/home/index')
}

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

    const invite = getInviteContext()
    const user = await userStore.login({
      code: loginResult.code,
      encryptedData: detail.encryptedData,
      iv: detail.iv,
      channel: '0',
      recmobile: invite?.recmobile,
    })

    redirectAfterLogin(user)
  } catch (error) {
    const message = error instanceof Error ? error.message : '登录失败，请重试'
    uni.showToast({ title: message, icon: 'none' })
  } finally {
    submitting.value = false
  }
}

function ensureAgreement(): boolean {
  if (!agreed.value) {
    uni.showToast({ title: '请先同意用户协议和隐私政策', icon: 'none' })
    return false
  }
  return true
}

function validateMobileInput(): boolean {
  if (!isPhone(mobile.value)) {
    uni.showToast({ title: '请输入正确的手机号', icon: 'none' })
    return false
  }
  return true
}

function startCountdown(): void {
  countdown.value = 60
  if (countdownTimer) clearInterval(countdownTimer)
  countdownTimer = setInterval(() => {
    countdown.value -= 1
    if (countdown.value <= 0 && countdownTimer) {
      clearInterval(countdownTimer)
      countdownTimer = null
    }
  }, 1000)
}

async function handleSendH5Code(): Promise<void> {
  if (!ensureAgreement() || !validateMobileInput()) return
  codeSending.value = true
  debugCode.value = ''
  try {
    const code = await userStore.requestH5LoginCode(mobile.value.trim())
    if (code) debugCode.value = code
    startCountdown()
    uni.showToast({ title: '验证码已发送', icon: 'success' })
  } catch (error) {
    const message = error instanceof Error ? error.message : '验证码发送失败'
    uni.showToast({ title: message, icon: 'none' })
  } finally {
    codeSending.value = false
  }
}

async function handleH5PhoneLogin(): Promise<void> {
  if (!ensureAgreement() || !validateMobileInput()) return
  if (!/^\d{6}$/.test(smsCode.value.trim())) {
    uni.showToast({ title: '请输入 6 位验证码', icon: 'none' })
    return
  }

  submitting.value = true
  try {
    const invite = getInviteContext()
    const user = await userStore.loginByPhone({
      mobile: mobile.value.trim(),
      smsCode: smsCode.value.trim(),
      recmobile: invite?.recmobile,
    })
    redirectAfterLogin(user)
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

onLoad((query) => {
  redirectUrl.value = decodeURIComponent(String(query?.redirect || ''))
  captureInviteFromQuery({
    inviteCode: query?.inviteCode ? String(query.inviteCode) : null,
    recmobile: query?.recmobile ? String(query.recmobile) : null,
    source: query?.source ? String(query.source) : 'login',
  })
})
</script>

<style lang="scss">

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

.phone-card,
.unsupported-card {
  padding: 36rpx;
  border-radius: $radius-keep-card;
  background: $color-bg-page;
}

.unsupported-card__title {
  display: block;
  color: $color-text-primary;
  font-size: 32rpx;
  font-weight: 800;
  text-align: center;
}

.phone-field {
  @include flex-between;
  height: 100rpx;
  margin-bottom: 20rpx;
  padding: 0 28rpx;
  border-radius: $radius-round;
  background: #fff;
}

.phone-input {
  flex: 1;
  min-width: 0;
  height: 100%;
  margin-left: 16rpx;
  color: $color-text-primary;
  font-size: 30rpx;
  font-weight: 700;
}

.phone-placeholder {
  color: $color-text-helper;
  font-weight: 600;
}

.code-button {
  @include flex-center;
  width: 176rpx;
  height: 68rpx;
  margin-left: 18rpx;
  border-radius: $radius-round;
  color: $color-primary-dark;
  background: $color-primary-light;
  font-size: 24rpx;
  font-weight: 800;
  line-height: 68rpx;
}

.code-button[disabled] {
  color: $color-text-secondary;
  background: $color-bg-chip;
}

.phone-login-button {
  margin-top: 28rpx;
}

.debug-code {
  display: block;
  margin-top: 18rpx;
  color: $color-accent-orange;
  font-size: 24rpx;
  font-weight: 700;
  text-align: center;
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
