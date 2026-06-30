<template>
  <view class="page">
    <KeepPageNav title="好友邀请" />

    <view class="hero">
      <text class="hero__title">邀请好友 共拍同框</text>
      <text class="hero__subtitle">{{ ruleText || '加载邀请规则中...' }}</text>
    </view>

    <view v-if="!isLoggedIn" class="card card--login">
      <text class="card__line">登录后即可生成你的专属邀请码</text>
      <button class="card__cta" @tap="goLogin">去登录</button>
    </view>

    <view v-else class="card card--code">
      <text class="card__caption">我的邀请码</text>
      <view class="code-row">
        <text class="code-row__value">{{ summary?.inviteCode || '——' }}</text>
        <button class="code-row__copy" :disabled="!summary?.inviteCode" @tap="copyCode">复制</button>
      </view>
      <view class="stats">
        <view class="stat">
          <text class="stat__value">{{ summary?.totalInvited ?? '-' }}</text>
          <text class="stat__label">已邀请</text>
        </view>
        <view class="stat-divider" />
        <view class="stat">
          <text class="stat__value">{{ summary?.totalReward ?? '-' }}</text>
          <text class="stat__label">累计拍拍豆</text>
        </view>
      </view>
    </view>

    <view v-if="isLoggedIn" class="actions">
      <!-- #ifdef MP-WEIXIN -->
      <button class="actions__share" open-type="share">
        <KeepIcon name="users" :size="36" color="#fff" />
        <text>邀请微信好友</text>
      </button>
      <!-- #endif -->
      <!-- #ifdef H5 -->
      <button class="actions__share" :disabled="!summary?.inviteCode" @tap="copyShareLink">
        <KeepIcon name="users" :size="36" color="#fff" />
        <text>复制邀请链接</text>
      </button>
      <!-- #endif -->
      <view class="actions__secondary" @tap="goRecords">查看邀请记录</view>
    </view>

    <view class="rule">
      <text class="rule__title">活动规则</text>
      <view class="rule__row"><view class="rule__dot" /><text class="rule__text">好友通过你的邀请码注册成功</text></view>
      <view class="rule__row"><view class="rule__dot" /><text class="rule__text">由服务端校验关系并发放 {{ summary?.rewardPpd ?? 0 }} 拍拍豆</text></view>
      <view class="rule__row"><view class="rule__dot" /><text class="rule__text">同一新用户只能被绑定一次，自我邀请无效</text></view>
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
import type { InviteSummary } from '@/api/types'

const userStore = useUserStore()
const summary = ref<InviteSummary | null>(null)
const ruleText = ref('')

const isLoggedIn = computed(() => userStore.isLoggedIn)

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
    if (result.success && result.data) ruleText.value = result.data.ruleText
  } catch {
    ruleText.value = ''
  }
}

function copyCode(): void {
  const code = summary.value?.inviteCode
  if (!code) return
  uni.setClipboardData({ data: code, success: () => uni.showToast({ title: '邀请码已复制', icon: 'success' }) })
}

function copyShareLink(): void {
  const code = summary.value?.inviteCode
  if (!code) return
  const path = `/pages-sub/content/invite-landing?inviteCode=${code}`
  const link = `${location.origin}${location.pathname}#${path}`
  uni.setClipboardData({ data: link, success: () => uni.showToast({ title: '邀请链接已复制', icon: 'success' }) })
}

function goLogin(): void {
  uni.navigateTo({ url: '/pages/login/index?redirect=' + encodeURIComponent('/pages-sub/user/invite') })
}

function goRecords(): void {
  uni.navigateTo({ url: '/pages-sub/user/invite-records' })
}

onShareAppMessage(() => {
  const code = summary.value?.inviteCode
  const nickname = userStore.userInfo?.nickname || '好友'
  return {
    title: `${nickname} 邀请你一起来爱去拍，注册即送拍拍豆`,
    path: code
      ? `/pages-sub/content/invite-landing?inviteCode=${code}`
      : '/pages-sub/content/invite-landing',
    imageUrl: '/static/default-avatar.png',
  }
})

onShareTimeline(() => {
  const code = summary.value?.inviteCode
  return {
    title: '爱去拍：和同城摄影师 / 模特一起拍',
    query: code ? `inviteCode=${code}&source=timeline` : '',
  }
})

onLoad(() => {
  void loadRule()
  void loadSummary()
})
</script>

<style scoped lang="scss">
.page { min-height: 100vh; padding: 24rpx 28rpx calc(60rpx + env(safe-area-inset-bottom)); background: $color-bg-page; }

.hero {
  margin: 16rpx 0 28rpx;
  padding: 50rpx 30rpx;
  border-radius: $radius-keep-card;
  background: linear-gradient(135deg, $color-primary, $color-primary-dark);
  text-align: center;
}
.hero__title { display: block; color: #fff; font-size: 40rpx; font-weight: 900; }
.hero__subtitle { display: block; margin-top: 14rpx; color: rgba(255, 255, 255, 0.85); font-size: 26rpx; line-height: 1.6; }

.card { margin-bottom: 24rpx; padding: 30rpx; border-radius: $radius-keep-card; background: #fff; box-shadow: $shadow-keep-card; }
.card--login { text-align: center; }
.card__line { display: block; color: $color-text-secondary; font-size: 26rpx; }
.card__cta { margin-top: 22rpx; height: 80rpx; border-radius: 999rpx; color: #fff; background: $color-primary; font-size: 28rpx; font-weight: 800; line-height: 80rpx; }
.card__cta::after { border: 0; }

.card__caption { display: block; color: $color-text-secondary; font-size: 24rpx; font-weight: 700; }
.code-row { display: flex; align-items: center; gap: 18rpx; margin-top: 14rpx; }
.code-row__value { flex: 1; color: $color-text-primary; font-size: 48rpx; font-weight: 900; letter-spacing: 4rpx; }
.code-row__copy { width: 130rpx; height: 64rpx; border-radius: $radius-round; color: $color-primary-dark; background: $color-primary-light; font-size: 24rpx; font-weight: 800; line-height: 64rpx; }
.code-row__copy::after { border: 0; }
.code-row__copy[disabled] { color: $color-text-secondary; background: $color-bg-chip; }

.stats { display: flex; align-items: center; margin-top: 24rpx; padding-top: 24rpx; border-top: 1rpx solid $color-border; }
.stat { flex: 1; text-align: center; }
.stat__value { display: block; color: $color-text-primary; font-size: 38rpx; font-weight: 900; }
.stat__label { display: block; margin-top: 6rpx; color: $color-text-helper; font-size: 22rpx; font-weight: 700; }
.stat-divider { width: 1rpx; height: 60rpx; background: $color-border; }

.actions { margin-top: 4rpx; }
.actions__share { @include flex-center; gap: 14rpx; height: 96rpx; border-radius: 999rpx; color: #fff; background: $color-primary; font-size: 30rpx; font-weight: 900; line-height: 96rpx; }
.actions__share[disabled] { opacity: 0.55; }
.actions__share::after { border: 0; }
.actions__secondary { margin-top: 18rpx; padding: 22rpx 0; border-radius: 999rpx; color: $color-primary-dark; background: $color-primary-light; font-size: 28rpx; font-weight: 800; text-align: center; }

.rule { margin-top: 36rpx; padding: 30rpx; border-radius: $radius-keep-card; background: $color-bg-chip; }
.rule__title { display: block; color: $color-text-primary; font-size: 28rpx; font-weight: 900; }
.rule__row { display: flex; align-items: flex-start; gap: 16rpx; margin-top: 18rpx; }
.rule__dot { margin-top: 14rpx; width: 12rpx; height: 12rpx; border-radius: 50%; background: $color-primary; }
.rule__text { flex: 1; color: $color-text-secondary; font-size: 26rpx; line-height: 1.6; }
</style>
