<template>
  <view class="page">
    <KeepPageNav title="爱去拍" />

    <view class="hero">
      <view class="hero__logo"><KeepIcon name="camera" :size="80" color="#fff" /></view>
      <text class="hero__brand">爱去拍</text>
      <text class="hero__slogan">遇见同频的拍摄伙伴</text>
    </view>

    <view v-if="inviteCode" class="invite-card">
      <text class="invite-card__label">邀请码</text>
      <text class="invite-card__code">{{ inviteCode }}</text>
      <text class="invite-card__hint">已为你准备好邀请码，注册即可绑定关系并领取奖励</text>
    </view>

    <view class="benefit">
      <text class="benefit__title">注册即可享</text>
      <view class="benefit__row"><view class="benefit__dot" /><text class="benefit__text">新用户注册赠送 10 拍拍豆</text></view>
      <view class="benefit__row"><view class="benefit__dot" /><text class="benefit__text">{{ ruleText || '邀请规则加载中...' }}</text></view>
      <view class="benefit__row"><view class="benefit__dot" /><text class="benefit__text">同城摄影师、模特、化妆师精准匹配</text></view>
    </view>

    <button class="cta" @tap="goLogin">登录并加入</button>
    <view class="agreement">
      <text>登录即代表同意</text>
      <text class="agreement__link" @tap="goAgreement">《用户协议》</text>
      <text>与</text>
      <text class="agreement__link" @tap="goPrivacy">《隐私政策》</text>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import KeepPageNav from '@/components/business/KeepPageNav.vue'
import KeepIcon from '@/components/business/KeepIcon.vue'
import * as inviteApi from '@/api/modules/invite'
import { captureInviteFromQuery } from '@/services/invite-context'

const inviteCode = ref('')
const ruleText = ref('')

async function loadRule(): Promise<void> {
  try {
    const result = await inviteApi.getInviteRule()
    if (result.success && result.data) ruleText.value = result.data.ruleText
  } catch {
    ruleText.value = ''
  }
}

function goLogin(): void {
  uni.navigateTo({ url: '/pages/login/index' })
}

function goAgreement(): void { uni.navigateTo({ url: '/pages-sub/content/agreement' }) }
function goPrivacy(): void { uni.navigateTo({ url: '/pages-sub/content/privacy' }) }

onLoad((query) => {
  const code = query?.inviteCode ? String(query.inviteCode) : ''
  const recmobile = query?.recmobile ? String(query.recmobile) : ''
  if (code) inviteCode.value = code
  captureInviteFromQuery({
    inviteCode: code,
    recmobile,
    source: query?.source ? String(query.source) : 'share',
  })
  void loadRule()
})
</script>

<style scoped lang="scss">
.page { min-height: 100vh; padding: 24rpx 28rpx calc(60rpx + env(safe-area-inset-bottom)); background: $color-bg-page; }

.hero { padding: 60rpx 0 36rpx; text-align: center; }
.hero__logo { @include flex-center; width: 160rpx; height: 160rpx; margin: 0 auto 24rpx; border-radius: 52rpx; background: $color-primary; box-shadow: $shadow-keep-button; }
.hero__brand { display: block; color: $color-text-primary; font-size: 52rpx; font-weight: 900; letter-spacing: 4rpx; }
.hero__slogan { display: block; margin-top: 14rpx; color: $color-text-secondary; font-size: 28rpx; }

.invite-card { margin: 30rpx 0; padding: 36rpx; border-radius: $radius-keep-card; background: #fff; box-shadow: $shadow-keep-card; text-align: center; }
.invite-card__label { display: block; color: $color-text-secondary; font-size: 24rpx; font-weight: 700; }
.invite-card__code { display: block; margin-top: 16rpx; color: $color-primary-dark; font-size: 52rpx; font-weight: 900; letter-spacing: 6rpx; }
.invite-card__hint { display: block; margin-top: 16rpx; color: $color-text-helper; font-size: 24rpx; line-height: 1.6; }

.benefit { padding: 30rpx; border-radius: $radius-keep-card; background: $color-bg-chip; }
.benefit__title { display: block; color: $color-text-primary; font-size: 30rpx; font-weight: 900; }
.benefit__row { display: flex; align-items: flex-start; gap: 16rpx; margin-top: 18rpx; }
.benefit__dot { margin-top: 14rpx; width: 12rpx; height: 12rpx; border-radius: 50%; background: $color-primary; }
.benefit__text { flex: 1; color: $color-text-secondary; font-size: 26rpx; line-height: 1.6; }

.cta { margin-top: 40rpx; height: 96rpx; border-radius: 999rpx; color: #fff; background: $color-primary; font-size: 30rpx; font-weight: 900; line-height: 96rpx; }
.cta::after { border: 0; }

.agreement { margin-top: 24rpx; color: $color-text-secondary; font-size: 22rpx; text-align: center; }
.agreement__link { color: $color-text-primary; font-weight: 800; }
</style>
