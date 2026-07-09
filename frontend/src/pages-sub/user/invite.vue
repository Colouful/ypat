<template>
  <view class="page">
    <KeepPageNav title="我的邀请" />

    <view class="hero">
      <view class="hero__badge">邀请有礼</view>
      <text class="hero__title">邀好友一起爱去拍</text>
      <text class="hero__subtitle">{{ landingTitle }}</text>
      <view class="hero__reward">
        <text class="hero__reward-label">每成功邀请 1 位好友</text>
        <view class="hero__reward-main">
          <text class="hero__reward-number">{{ rewardPpd }}</text>
          <text class="hero__reward-unit">{{ rewardUnit }}</text>
        </view>
      </view>
    </view>

    <view v-if="!inviteEnabled" class="notice">
      <KeepIcon name="help-circle" :size="28" color="#17A857" />
      <text>邀请活动暂未开启，配置启用后即可继续邀请好友。</text>
    </view>

    <view v-if="!isLoggedIn" class="panel panel--login">
      <text class="panel__title">登录后生成专属邀请码</text>
      <text class="panel__desc">分享给微信好友，好友通过你的邀请加入后，奖励会自动到账。</text>
      <button class="primary-btn" @tap="goLogin">去登录</button>
    </view>

    <view v-else class="panel panel--invite">
      <view class="code-card">
        <text class="code-card__label">我的邀请码</text>
        <view class="code-card__row">
          <text class="code-card__value">{{ summary?.inviteCode || '——' }}</text>
          <button class="code-card__copy" :disabled="!summary?.inviteCode" @tap="copyCode">复制</button>
        </view>
      </view>

      <view class="stats">
        <view class="stat">
          <text class="stat__value">{{ summary?.totalInvited ?? 0 }}</text>
          <text class="stat__label">成功邀请</text>
        </view>
        <view class="stat-divider" />
        <view class="stat">
          <text class="stat__value">{{ summary?.totalReward ?? 0 }}</text>
          <text class="stat__label">累计奖励</text>
        </view>
      </view>

      <view class="actions">
        <!-- #ifdef MP-WEIXIN -->
        <button class="primary-btn" open-type="share" :disabled="!canInvite">
          <KeepIcon name="users" :size="36" color="#fff" />
          <text>邀请微信好友</text>
        </button>
        <!-- #endif -->
        <!-- #ifdef H5 -->
        <button class="primary-btn" :disabled="!canInvite" @tap="copyShareLink">
          <KeepIcon name="copy" :size="36" color="#fff" />
          <text>复制邀请链接</text>
        </button>
        <!-- #endif -->
        <!-- #ifdef MP-WEIXIN -->
        <view class="ghost-btn" :class="{ 'ghost-btn--disabled': !canInvite }" @tap="copyShareLink">
          <KeepIcon name="copy" :size="30" color="#17A857" />
          <text>复制邀请链接</text>
        </view>
        <!-- #endif -->
        <view class="ghost-btn" @tap="goRecords">
          <KeepIcon name="menu" :size="30" color="#17A857" />
          <text>查看邀请记录</text>
        </view>
      </view>
    </view>

    <view class="rule">
      <text class="rule__title">活动规则</text>
      <view class="rule__item">
        <view class="rule__index">1</view>
        <text class="rule__text">好友点击你的分享卡片，进入邀请落地页并完成登录注册。</text>
      </view>
      <view class="rule__item">
        <view class="rule__index">2</view>
        <text class="rule__text">{{ ruleText }}</text>
      </view>
      <view class="rule__item">
        <view class="rule__index">3</view>
        <text class="rule__text">同一新用户只能绑定一次邀请关系，自我邀请无效。</text>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { onLoad, onShareAppMessage, onShareTimeline } from '@dcloudio/uni-app'
import KeepPageNav from '@/components/business/KeepPageNav.vue'
import KeepIcon from '@/components/business/KeepIcon.vue'
import { useUserStore } from '@/stores/user'
import * as inviteApi from '@/api/modules/invite'
import type { InviteRule, InviteSummary } from '@/api/types'
import { buildInviteLandingPath } from '@/services/invite-context'

const userStore = useUserStore()
const summary = ref<InviteSummary | null>(null)
const rule = ref<InviteRule | null>(null)

const isLoggedIn = computed(() => userStore.isLoggedIn)
const inviteEnabled = computed(() => (summary.value?.enabled || rule.value?.enabled || '1') === '1')
const rewardPpd = computed(() => summary.value?.rewardPpd ?? rule.value?.rewardPpd ?? 0)
const rewardUnit = computed(() => rule.value?.rewardUnit || '拍拍豆')
const ruleText = computed(() => summary.value?.ruleText || rule.value?.ruleText || `好友通过你的邀请码注册后，自动到账 ${rewardPpd.value} 拍拍豆。`)
const shareTitle = computed(() => summary.value?.shareTitle || rule.value?.shareTitle || '好友邀请你加入爱去拍，找摄影师、找模特更方便')
const landingTitle = computed(() => summary.value?.landingTitle || rule.value?.landingTitle || '我正在使用爱去拍，找摄影师、找模特特别方便，推荐你也来体验。')
const canInvite = computed(() => inviteEnabled.value && Boolean(summary.value?.inviteCode))

async function loadSummary(): Promise<void> {
  if (!isLoggedIn.value) return
  try {
    const result = await inviteApi.getMyInviteInfo()
    if (result.success && result.data) summary.value = result.data
  } catch {
    summary.value = null
  }
}

async function loadRule(): Promise<void> {
  try {
    const result = await inviteApi.getInviteRule()
    if (result.success && result.data) rule.value = result.data
  } catch {
    rule.value = null
  }
}

function copyCode(): void {
  const code = summary.value?.inviteCode
  if (!code) return
  uni.setClipboardData({ data: code, success: () => uni.showToast({ title: '邀请码已复制', icon: 'success' }) })
}

function copyShareLink(): void {
  if (!inviteEnabled.value) {
    uni.showToast({ title: '邀请活动暂未开启', icon: 'none' })
    return
  }
  const code = summary.value?.inviteCode
  if (!code) return
  const path = buildInviteLandingPath(code, 'share')
  // #ifdef H5
  const link = `${location.origin}${location.pathname}#${path}`
  uni.setClipboardData({ data: link, success: () => uni.showToast({ title: '邀请链接已复制', icon: 'success' }) })
  // #endif
  // #ifndef H5
  uni.setClipboardData({ data: path, success: () => uni.showToast({ title: '邀请路径已复制', icon: 'success' }) })
  // #endif
}

function goLogin(): void {
  uni.navigateTo({ url: '/pages/login/index?redirect=' + encodeURIComponent('/pages-sub/user/invite') })
}

function goRecords(): void {
  uni.navigateTo({ url: '/pages-sub/user/invite-records' })
}

onShareAppMessage(() => {
  const code = canInvite.value ? summary.value?.inviteCode : ''
  return {
    title: shareTitle.value,
    path: buildInviteLandingPath(code, 'share'),
    imageUrl: '/static/default-cover.png',
  }
})

onShareTimeline(() => {
  const code = canInvite.value ? summary.value?.inviteCode : ''
  const query = code
    ? `inviteCode=${encodeURIComponent(code)}&source=timeline`
    : 'source=timeline'
  return {
    title: shareTitle.value,
    query,
    imageUrl: '/static/default-cover.png',
  }
})

onLoad(() => {
  void loadRule()
  void loadSummary()
})
</script>

<style scoped lang="scss">
.page {
  min-height: 100vh;
  padding: 24rpx 28rpx calc(60rpx + env(safe-area-inset-bottom));
  background: linear-gradient(180deg, #eafff2 0%, $color-bg-page 480rpx);
}

.hero {
  position: relative;
  overflow: hidden;
  margin: 16rpx 0 24rpx;
  padding: 42rpx 32rpx 34rpx;
  border-radius: 40rpx;
  color: #fff;
  background: linear-gradient(135deg, $color-primary 0%, $color-primary-dark 62%, #0e8e66 100%);
  box-shadow: $shadow-keep-button;
}

.hero::after {
  position: absolute;
  right: -110rpx;
  top: -120rpx;
  width: 300rpx;
  height: 300rpx;
  border: 36rpx solid rgba(255, 255, 255, 0.18);
  border-radius: 50%;
  content: '';
}

.hero__badge {
  position: relative;
  z-index: 1;
  display: inline-flex;
  padding: 8rpx 18rpx;
  border-radius: $radius-round;
  color: $color-primary-dark;
  background: rgba(255, 255, 255, 0.88);
  font-size: 22rpx;
  font-weight: 800;
}

.hero__title {
  position: relative;
  z-index: 1;
  display: block;
  margin-top: 28rpx;
  font-size: 48rpx;
  font-weight: 900;
  line-height: 1.18;
}

.hero__subtitle {
  position: relative;
  z-index: 1;
  display: block;
  margin-top: 16rpx;
  max-width: 560rpx;
  color: rgba(255, 255, 255, 0.86);
  font-size: 26rpx;
  line-height: 1.6;
}

.hero__reward {
  position: relative;
  z-index: 1;
  margin-top: 34rpx;
  padding: 24rpx;
  border: 1rpx solid rgba(255, 255, 255, 0.28);
  border-radius: 28rpx;
  background: rgba(255, 255, 255, 0.16);
}

.hero__reward-label {
  display: block;
  color: rgba(255, 255, 255, 0.82);
  font-size: 24rpx;
  font-weight: 700;
}

.hero__reward-main {
  display: flex;
  align-items: baseline;
  gap: 12rpx;
  margin-top: 6rpx;
}

.hero__reward-number {
  font-size: 64rpx;
  font-weight: 900;
}

.hero__reward-unit {
  font-size: 28rpx;
  font-weight: 800;
}

.notice {
  display: flex;
  align-items: center;
  gap: 12rpx;
  margin-bottom: 20rpx;
  padding: 20rpx 24rpx;
  border-radius: 24rpx;
  color: $color-primary-dark;
  background: $color-primary-soft;
  font-size: 24rpx;
  line-height: 1.5;
}

.panel {
  padding: 30rpx;
  border-radius: $radius-keep-card;
  background: #fff;
  box-shadow: $shadow-keep-card;
}

.panel--login {
  text-align: center;
}

.panel__title {
  display: block;
  color: $color-text-primary;
  font-size: 32rpx;
  font-weight: 900;
}

.panel__desc {
  display: block;
  margin: 14rpx 0 28rpx;
  color: $color-text-secondary;
  font-size: 26rpx;
  line-height: 1.6;
}

.code-card {
  padding: 26rpx;
  border-radius: 28rpx;
  background: $color-primary-soft;
}

.code-card__label {
  display: block;
  color: $color-primary-dark;
  font-size: 24rpx;
  font-weight: 800;
}

.code-card__row {
  display: flex;
  align-items: center;
  gap: 18rpx;
  margin-top: 12rpx;
}

.code-card__value {
  flex: 1;
  min-width: 0;
  color: $color-text-primary;
  font-size: 46rpx;
  font-weight: 900;
  letter-spacing: 2rpx;
  word-break: break-all;
}

.code-card__copy {
  width: 128rpx;
  height: 64rpx;
  border-radius: $radius-round;
  color: #fff;
  background: $color-primary;
  font-size: 24rpx;
  font-weight: 800;
  line-height: 64rpx;
}

.code-card__copy::after,
.primary-btn::after {
  border: 0;
}

.stats {
  display: flex;
  align-items: center;
  margin: 28rpx 0;
}

.stat {
  flex: 1;
  text-align: center;
}

.stat__value {
  display: block;
  color: $color-text-primary;
  font-size: 42rpx;
  font-weight: 900;
}

.stat__label {
  display: block;
  margin-top: 6rpx;
  color: $color-text-helper;
  font-size: 22rpx;
  font-weight: 700;
}

.stat-divider {
  width: 1rpx;
  height: 64rpx;
  background: $color-border;
}

.actions {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 18rpx;
}

.primary-btn {
  grid-column: span 2;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12rpx;
  width: 100%;
  height: 92rpx;
  border-radius: $radius-round;
  color: #fff;
  background: $color-primary;
  font-size: 30rpx;
  font-weight: 900;
  line-height: 92rpx;
}

.primary-btn[disabled] {
  opacity: 0.5;
}

.ghost-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8rpx;
  height: 76rpx;
  border-radius: $radius-round;
  color: $color-primary-dark;
  background: $color-primary-soft;
  font-size: 25rpx;
  font-weight: 800;
}

.ghost-btn--disabled {
  opacity: 0.55;
}

.rule {
  margin-top: 24rpx;
  padding: 30rpx;
  border-radius: $radius-keep-card;
  background: #fff;
  box-shadow: $shadow-keep-card;
}

.rule__title {
  display: block;
  color: $color-text-primary;
  font-size: 30rpx;
  font-weight: 900;
}

.rule__item {
  display: flex;
  align-items: flex-start;
  gap: 18rpx;
  margin-top: 22rpx;
}

.rule__index {
  display: flex;
  align-items: center;
  justify-content: center;
  flex: none;
  width: 36rpx;
  height: 36rpx;
  border-radius: 50%;
  color: #fff;
  background: $color-primary;
  font-size: 22rpx;
  font-weight: 900;
}

.rule__text {
  flex: 1;
  color: $color-text-secondary;
  font-size: 26rpx;
  line-height: 1.6;
}
</style>
