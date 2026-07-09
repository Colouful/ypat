<template>
  <view class="page">
    <KeepPageNav title="好友邀请" />

    <view class="hero">
      <view class="hero__logo"><KeepIcon name="camera" :size="70" color="#fff" /></view>
      <text class="hero__brand">爱去拍</text>
      <text class="hero__title">{{ landingTitle }}</text>
      <view class="hero__tags">
        <text>摄影师</text>
        <text>模特</text>
        <text>同城约拍</text>
      </view>
    </view>

    <view v-if="inviteCode" class="invite-card">
      <text class="invite-card__label">好友的邀请码</text>
      <text class="invite-card__code">{{ inviteCode }}</text>
      <text class="invite-card__hint">登录注册后会自动为你保留这份邀请关系</text>
    </view>

    <view class="reward-card">
      <text class="reward-card__label">邀请奖励</text>
      <view class="reward-card__main">
        <text class="reward-card__number">{{ rewardPpd }}</text>
        <text class="reward-card__unit">{{ rewardUnit }}</text>
      </view>
      <text class="reward-card__desc">{{ ruleText }}</text>
    </view>

    <view class="benefit">
      <view class="benefit__row">
        <KeepIcon name="check" :size="28" color="#23C268" />
        <text>发现同城摄影师、模特、化妆师</text>
      </view>
      <view class="benefit__row">
        <KeepIcon name="check" :size="28" color="#23C268" />
        <text>发布约拍需求，找到更匹配的拍摄伙伴</text>
      </view>
      <view class="benefit__row">
        <KeepIcon name="check" :size="28" color="#23C268" />
        <text>通过好友邀请加入，注册链路自动绑定</text>
      </view>
    </view>

    <button class="cta" :disabled="!inviteEnabled" @tap="goLogin">{{ ctaText }}</button>
    <view class="agreement">
      <text>登录即代表同意</text>
      <text class="agreement__link" @tap="goAgreement">《用户协议》</text>
      <text>与</text>
      <text class="agreement__link" @tap="goPrivacy">《隐私政策》</text>
    </view>
  </view>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import KeepPageNav from '@/components/business/KeepPageNav.vue'
import KeepIcon from '@/components/business/KeepIcon.vue'
import * as inviteApi from '@/api/modules/invite'
import type { InviteRule } from '@/api/types'
import { captureInviteFromQuery } from '@/services/invite-context'

const inviteCode = ref('')
const inviteSource = ref('share')
const rule = ref<InviteRule | null>(null)

const inviteEnabled = computed(() => (rule.value?.enabled || '1') === '1')
const rewardPpd = computed(() => rule.value?.rewardPpd ?? 0)
const rewardUnit = computed(() => rule.value?.rewardUnit || '拍拍豆')
const ruleText = computed(() => rule.value?.ruleText || `好友通过你的邀请码注册后，自动到账 ${rewardPpd.value} 拍拍豆。`)
const landingTitle = computed(() => rule.value?.landingTitle || '我正在使用爱去拍，找摄影师、找模特特别方便，推荐你也来体验。')
const ctaText = computed(() => (inviteEnabled.value ? '马上体验' : '邀请活动暂未开启'))

async function loadRule(): Promise<void> {
  try {
    const result = await inviteApi.getInviteRule()
    if (result.success && result.data) rule.value = result.data
  } catch {
    rule.value = null
  }
}

function goLogin(): void {
  if (!inviteEnabled.value) {
    uni.showToast({ title: '邀请活动暂未开启', icon: 'none' })
    return
  }
  const params: string[] = []
  if (inviteCode.value) params.push(`inviteCode=${encodeURIComponent(inviteCode.value)}`)
  if (inviteSource.value) params.push(`source=${encodeURIComponent(inviteSource.value)}`)
  const query = params.length ? `?${params.join('&')}` : ''
  uni.navigateTo({ url: `/pages/login/index${query}` })
}

function goAgreement(): void { uni.navigateTo({ url: '/pages-sub/content/agreement' }) }
function goPrivacy(): void { uni.navigateTo({ url: '/pages-sub/content/privacy' }) }

onLoad((query) => {
  const code = query?.inviteCode ? String(query.inviteCode) : ''
  const recmobile = query?.recmobile ? String(query.recmobile) : ''
  const source = query?.source ? String(query.source) : 'share'
  inviteCode.value = code
  inviteSource.value = source
  captureInviteFromQuery({ inviteCode: code, recmobile, source })
  void loadRule()
})
</script>

<style scoped lang="scss">
.page {
  min-height: 100vh;
  padding: 24rpx 28rpx calc(60rpx + env(safe-area-inset-bottom));
  background: linear-gradient(180deg, #effff5 0%, #fff 420rpx, $color-bg-page 421rpx);
}

.hero {
  padding: 44rpx 18rpx 34rpx;
  text-align: center;
}

.hero__logo {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 132rpx;
  height: 132rpx;
  margin: 0 auto 22rpx;
  border-radius: 42rpx;
  background: $color-primary;
  box-shadow: $shadow-keep-button;
}

.hero__brand {
  display: block;
  color: $color-primary-dark;
  font-size: 28rpx;
  font-weight: 900;
}

.hero__title {
  display: block;
  margin: 20rpx auto 0;
  max-width: 620rpx;
  color: $color-text-primary;
  font-size: 38rpx;
  font-weight: 900;
  line-height: 1.45;
}

.hero__tags {
  display: flex;
  justify-content: center;
  gap: 14rpx;
  margin-top: 26rpx;

  text {
    padding: 8rpx 18rpx;
    border-radius: $radius-round;
    color: $color-primary-dark;
    background: $color-primary-soft;
    font-size: 22rpx;
    font-weight: 800;
  }
}

.invite-card,
.reward-card,
.benefit {
  margin-bottom: 22rpx;
  padding: 30rpx;
  border-radius: $radius-keep-card;
  background: #fff;
  box-shadow: $shadow-keep-card;
}

.invite-card {
  text-align: center;
}

.invite-card__label {
  display: block;
  color: $color-text-helper;
  font-size: 24rpx;
  font-weight: 800;
}

.invite-card__code {
  display: block;
  margin-top: 12rpx;
  color: $color-primary-dark;
  font-size: 50rpx;
  font-weight: 900;
  letter-spacing: 4rpx;
}

.invite-card__hint {
  display: block;
  margin-top: 12rpx;
  color: $color-text-secondary;
  font-size: 24rpx;
  line-height: 1.6;
}

.reward-card {
  background: linear-gradient(135deg, $color-primary-soft 0%, #fff 70%);
}

.reward-card__label {
  display: block;
  color: $color-primary-dark;
  font-size: 24rpx;
  font-weight: 900;
}

.reward-card__main {
  display: flex;
  align-items: baseline;
  gap: 12rpx;
  margin-top: 8rpx;
  color: $color-accent-gold;
}

.reward-card__number {
  font-size: 64rpx;
  font-weight: 900;
}

.reward-card__unit {
  font-size: 28rpx;
  font-weight: 900;
}

.reward-card__desc {
  display: block;
  margin-top: 8rpx;
  color: $color-text-secondary;
  font-size: 26rpx;
  line-height: 1.6;
}

.benefit__row {
  display: flex;
  align-items: flex-start;
  gap: 14rpx;
  color: $color-text-secondary;
  font-size: 26rpx;
  line-height: 1.6;
}

.benefit__row + .benefit__row {
  margin-top: 18rpx;
}

.cta {
  margin-top: 34rpx;
  width: 100%;
  height: 96rpx;
  border-radius: $radius-round;
  color: #fff;
  background: $color-primary;
  box-shadow: $shadow-keep-button;
  font-size: 30rpx;
  font-weight: 900;
  line-height: 96rpx;
}

.cta[disabled] {
  opacity: 0.55;
}

.cta::after {
  border: 0;
}

.agreement {
  margin-top: 24rpx;
  color: $color-text-secondary;
  font-size: 22rpx;
  text-align: center;
}

.agreement__link {
  color: $color-text-primary;
  font-weight: 800;
}
</style>
