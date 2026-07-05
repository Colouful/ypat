<template>
  <view class="page">
    <KeepPageNav title="会员中心" />

    <view class="hero" :class="{ 'hero--active': status?.active }">
      <text class="hero__label">{{ status?.active ? '会员生效中' : '开通会员' }}</text>
      <text class="hero__line">{{ status?.active ? `有效期至 ${formatDate(status?.expireAt)}` : '解锁更多权益 · 享专属服务' }}</text>
      <view v-if="!status?.active && plans.length === 0 && !loading" class="hero__hint">
        套餐暂未配置，敬请期待
      </view>
    </view>

    <view v-if="loading" class="loading">加载套餐中…</view>

    <view v-else class="plans">
      <view
        v-for="plan in plans"
        :key="plan.id"
        class="plan-card"
        :class="{ 'plan-card--recommended': isRecommended(plan), 'plan-card--selected': selectedPlan?.id === plan.id }"
        @tap="selectPlan(plan)"
      >
        <view v-if="isRecommended(plan)" class="plan-card__tag">推荐</view>
        <text class="plan-card__name">{{ plan.name }}</text>
        <text class="plan-card__duration">{{ plan.durationDays }} 天</text>
        <view class="plan-card__price">
          <text class="plan-card__price-yuan">¥{{ formatYuan(plan.priceFen) }}</text>
          <text v-if="plan.originPriceFen && plan.originPriceFen > plan.priceFen" class="plan-card__price-origin">
            ¥{{ formatYuan(plan.originPriceFen) }}
          </text>
        </view>
        <text v-if="plan.giftPpd && plan.giftPpd > 0" class="plan-card__gift">赠送 {{ plan.giftPpd }} 拍拍豆</text>
        <text v-if="plan.benefits" class="plan-card__benefit">{{ plan.benefits }}</text>
        <view class="plan-card__select">{{ selectedPlan?.id === plan.id ? '已选择' : '选择套餐' }}</view>
      </view>
    </view>

    <view class="agreement">
      支付即代表同意《会员服务协议》；会员开通后不支持退款。
    </view>

    <view class="orders-link" @tap="goOrders">查看历史订单</view>

    <view v-if="plans.length > 0" class="bottom-bar">
      <view class="bottom-bar__info">
        <text class="bottom-bar__name">{{ selectedPlan?.name || '请选择套餐' }}</text>
        <text class="bottom-bar__sub">{{ selectedPlan ? `${selectedPlan.durationDays} 天 · 赠送 ${selectedPlan.giftPpd || 0} 拍拍豆` : '开通会员享受更多权益' }}</text>
      </view>
      <view class="bottom-bar__button" :class="{ 'bottom-bar__button--disabled': !selectedPlan || submitting }" @tap="submitSelected">
        {{ submitting ? '处理中' : `立即${status?.active ? '续费' : '开通'}` }}
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import KeepPageNav from '@/components/business/KeepPageNav.vue'
import { useUserStore } from '@/stores/user'
import { useMemberStore } from '@/stores/member'
import * as memberApi from '@/api/modules/member'
import type { MemberOrderCreateResult, MemberPlan } from '@/api/types'

const userStore = useUserStore()
const memberStore = useMemberStore()
const loading = ref(false)
const submitting = ref(false)
const plans = ref<MemberPlan[]>([])
const selectedPlanId = ref<number | null>(null)

const status = computed(() => memberStore.status)
const selectedPlan = computed(() => plans.value.find((plan) => plan.id === selectedPlanId.value) || null)

function formatDate(value?: string): string {
  if (!value) return ''
  return value.replace('T', ' ').slice(0, 10)
}

function formatYuan(fen: number): string {
  if (!fen || fen <= 0) return '0.00'
  return (fen / 100).toFixed(2)
}

function isRecommended(plan: MemberPlan): boolean {
  return plan.recommended === '1'
}

async function loadPlans(): Promise<void> {
  loading.value = true
  try {
    const result = await memberApi.getMemberPlans()
    if (result.success && Array.isArray(result.data)) {
      plans.value = result.data
      selectedPlanId.value = plans.value.find((plan) => isRecommended(plan))?.id || plans.value[0]?.id || null
    } else {
      plans.value = []
      selectedPlanId.value = null
    }
  } catch {
    plans.value = []
    selectedPlanId.value = null
  } finally {
    loading.value = false
  }
}

async function loadStatus(): Promise<void> {
  if (!userStore.isLoggedIn) return
  await memberStore.refreshStatus()
}

function ensureLogin(): boolean {
  if (userStore.isLoggedIn) return true
  uni.navigateTo({ url: '/pages/login/index?redirect=' + encodeURIComponent('/pages-sub/user/member/index') })
  return false
}

function selectPlan(plan: MemberPlan): void {
  selectedPlanId.value = plan.id
}

async function submitSelected(): Promise<void> {
  if (!selectedPlan.value) {
    uni.showToast({ title: '请选择会员套餐', icon: 'none' })
    return
  }
  await createOrderAndPay(selectedPlan.value)
}

async function createOrderAndPay(plan: MemberPlan): Promise<void> {
  if (submitting.value) return
  if (!ensureLogin()) return
  submitting.value = true
  try {
    const result = await memberApi.createMemberOrder(plan.id)
    if (!result.success || !result.data) {
      uni.showToast({ title: result.message || '下单失败', icon: 'none' })
      return
    }
    await launchWxPay(result.data)
  } finally {
    submitting.value = false
  }
}

async function launchWxPay(payload: MemberOrderCreateResult): Promise<void> {
  return new Promise((resolve) => {
    // #ifdef MP-WEIXIN
    uni.requestPayment({
      provider: 'wxpay',
      timeStamp: payload.timeStamp,
      nonceStr: payload.nonceStr,
      package: payload.packageValue,
      signType: payload.signType,
      paySign: payload.paySign,
      orderInfo: '',
      success: () => {
        void onPaySuccess(payload.outTradeNo)
      },
      fail: (err: { errMsg?: string }) => {
        const cancelled = err?.errMsg?.includes('cancel')
        uni.showToast({ title: cancelled ? '已取消支付' : '支付失败', icon: 'none' })
        resolve()
      },
    } as UniApp.RequestPaymentOptions)
    // #endif
    // #ifdef H5
    // H5 没有原生 requestPayment，跳到中间页提示用户在微信内打开
    uni.showModal({
      title: '请在小程序内开通',
      content: 'H5 暂不支持微信支付，请在微信小程序内打开后开通。',
      showCancel: false,
      success: () => resolve(),
    })
    // #endif
    // #ifndef MP-WEIXIN || H5
    uni.showToast({ title: '当前平台暂不支持支付', icon: 'none' })
    resolve()
    // #endif
  })
}

async function onPaySuccess(outTradeNo: string): Promise<void> {
  uni.showLoading({ title: '正在确认支付结果' })
  const granted = await memberStore.pollUntilPaid(outTradeNo)
  uni.hideLoading()
  if (granted) {
    uni.showToast({ title: '开通成功', icon: 'success' })
  } else {
    uni.showModal({
      title: '支付结果确认中',
      content: '我们将在稍后确认订单并开通，会员中心会自动更新。',
      showCancel: false,
    })
  }
}

function goOrders(): void {
  uni.navigateTo({ url: '/pages-sub/user/member-orders' })
}

onLoad(async () => {
  await loadPlans()
  await loadStatus()
})
</script>

<style scoped lang="scss">
.page { min-height: 100vh; padding: 24rpx 28rpx calc(170rpx + env(safe-area-inset-bottom)); background: $color-bg-page; }

.hero { padding: 50rpx 30rpx; border-radius: $radius-keep-card; background: $color-bg-card; box-shadow: $shadow-keep-card; text-align: center; }
.hero--active { background: linear-gradient(135deg, $color-primary, $color-primary-dark); }
.hero--active .hero__label, .hero--active .hero__line { color: #fff; }
.hero__label { display: block; color: $color-text-primary; font-size: 38rpx; font-weight: 900; }
.hero__line { display: block; margin-top: 14rpx; color: $color-text-secondary; font-size: 26rpx; }
.hero__hint { display: block; margin-top: 18rpx; color: $color-text-helper; font-size: 24rpx; }

.loading { padding: 80rpx 0; color: $color-text-helper; font-size: 26rpx; text-align: center; }

.plans { display: flex; flex-direction: column; gap: 18rpx; margin-top: 28rpx; }
.plan-card { position: relative; padding: 36rpx 30rpx; border-radius: $radius-keep-card; background: #fff; box-shadow: $shadow-keep-card; }
.plan-card--recommended { border: 2rpx solid $color-primary; }
.plan-card--selected { box-shadow: 0 18rpx 48rpx rgba(23, 168, 87, 0.16); }
.plan-card__tag { position: absolute; top: -16rpx; right: 24rpx; padding: 4rpx 18rpx; border-radius: $radius-round; color: #fff; background: $color-primary; font-size: 22rpx; font-weight: 800; }
.plan-card__name { display: block; color: $color-text-primary; font-size: 32rpx; font-weight: 900; }
.plan-card__duration { display: block; margin-top: 8rpx; color: $color-text-secondary; font-size: 24rpx; font-weight: 700; }
.plan-card__price { margin-top: 18rpx; display: flex; align-items: baseline; gap: 16rpx; }
.plan-card__price-yuan { color: $color-accent-orange; font-size: 44rpx; font-weight: 900; }
.plan-card__price-origin { color: $color-text-helper; font-size: 24rpx; text-decoration: line-through; }
.plan-card__gift { display: block; margin-top: 12rpx; color: $color-primary-dark; font-size: 24rpx; font-weight: 800; }
.plan-card__benefit { display: block; margin-top: 16rpx; color: $color-text-secondary; font-size: 24rpx; line-height: 1.6; }
.plan-card__select { margin-top: 22rpx; height: 64rpx; border-radius: 999rpx; color: $color-primary-dark; background: rgba(23, 168, 87, 0.1); font-size: 24rpx; font-weight: 800; line-height: 64rpx; text-align: center; }

.agreement { margin-top: 32rpx; color: $color-text-helper; font-size: 22rpx; text-align: center; }
.orders-link { margin-top: 24rpx; padding: 22rpx 0; color: $color-primary-dark; font-size: 26rpx; font-weight: 800; text-align: center; }
.bottom-bar { position: fixed; right: 0; bottom: 0; left: 0; z-index: 10; display: flex; align-items: center; justify-content: space-between; gap: 24rpx; padding: 20rpx 28rpx calc(20rpx + env(safe-area-inset-bottom)); background: #fff; box-shadow: 0 -12rpx 36rpx rgba(15, 23, 42, 0.08); }
.bottom-bar__info { min-width: 0; flex: 1; }
.bottom-bar__name { display: block; overflow: hidden; color: $color-text-primary; font-size: 28rpx; font-weight: 900; text-overflow: ellipsis; white-space: nowrap; }
.bottom-bar__sub { display: block; margin-top: 6rpx; overflow: hidden; color: $color-text-secondary; font-size: 22rpx; text-overflow: ellipsis; white-space: nowrap; }
.bottom-bar__button { width: 250rpx; height: 76rpx; border-radius: 999rpx; color: #fff; background: $color-primary; font-size: 28rpx; font-weight: 900; line-height: 76rpx; text-align: center; }
.bottom-bar__button--disabled { opacity: 0.55; }
</style>
