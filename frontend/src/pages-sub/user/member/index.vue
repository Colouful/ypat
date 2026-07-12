<template>
  <view class="page">
    <KeepPageNav title="会员中心" />

    <view
      class="hero"
      :class="{ 'hero--active': status?.active }"
    >
      <view class="hero__top">
        <view class="hero__badge">
          VIP
        </view>
        <text class="hero__state">
          {{ status?.active ? '会员生效中' : '会员未开通' }}
        </text>
      </view>
      <text class="hero__label">
        爱去拍 · 信用会员
      </text>
      <text class="hero__line">
        {{ status?.active ? `有效期至 ${formatDate(status?.expireAt)}` : '专属曝光 · 优先约拍 · 发布更省' }}
      </text>
      <view class="hero__features">
        <text>优先曝光</text>
        <text>专属标识</text>
        <text>约拍优惠</text>
      </view>
      <view
        v-if="!status?.active && plans.length === 0 && !loading"
        class="hero__hint"
      >
        套餐暂未配置，敬请期待
      </view>
    </view>

    <view
      v-if="loading"
      class="loading"
    >
      加载套餐中…
    </view>

    <template v-else>
      <view class="section section--plans">
        <view class="section__head">
          <text class="section__title">
            选择套餐
          </text>
          <text class="section__sub">
            权益随套餐一起开通
          </text>
        </view>
        <view class="plans">
          <view
            v-for="plan in plans"
            :key="plan.id"
            class="plan-card"
            :class="{ 'plan-card--recommended': isRecommended(plan), 'plan-card--selected': selectedPlan?.id === plan.id }"
            @tap="selectPlan(plan)"
          >
            <text
              v-if="isRecommended(plan)"
              class="plan-card__tag"
            >
              推荐
            </text>
            <view class="plan-card__main">
              <view class="plan-card__info">
                <text class="plan-card__name">
                  {{ plan.name }}
                </text>
                <text class="plan-card__duration">
                  {{ plan.durationDays }} 天有效期
                </text>
                <view class="plan-card__chips">
                  <text
                    v-if="plan.giftPpd && plan.giftPpd > 0"
                    class="plan-card__chip"
                  >
                    赠 {{ plan.giftPpd }} 拍拍豆
                  </text>
                  <text
                    v-if="plan.levelCode"
                    class="plan-card__chip"
                  >
                    {{ plan.levelCode }}
                  </text>
                </view>
              </view>
              <view class="plan-card__price">
                <text class="plan-card__price-yuan">
                  ¥{{ formatYuan(plan.priceFen) }}
                </text>
                <text
                  v-if="plan.originPriceFen && plan.originPriceFen > plan.priceFen"
                  class="plan-card__price-origin"
                >
                  ¥{{ formatYuan(plan.originPriceFen) }}
                </text>
              </view>
            </view>
            <view
              class="plan-card__check"
              :class="{ 'plan-card__check--selected': selectedPlan?.id === plan.id }"
            >
              <text>{{ selectedPlan?.id === plan.id ? '✓' : '' }}</text>
            </view>
          </view>
        </view>
      </view>

      <view class="section section--benefits">
        <view class="section__head">
          <text class="section__title">
            会员权益
          </text>
          <text class="section__sub">
            开通后立即生效
          </text>
        </view>
        <view class="benefit-grid">
          <view
            v-for="item in benefitItems"
            :key="item.title"
            class="benefit-item"
          >
            <view class="benefit-item__icon">
              {{ item.icon }}
            </view>
            <view class="benefit-item__content">
              <text class="benefit-item__title">
                {{ item.title }}
              </text>
              <text class="benefit-item__desc">
                {{ item.desc }}
              </text>
            </view>
          </view>
        </view>
      </view>
    </template>

    <view class="agreement">
      支付即代表同意《会员服务协议》；会员开通后不支持退款。
    </view>

    <view
      class="orders-link"
      @tap="goOrders"
    >
      查看历史订单
    </view>

    <view
      v-if="plans.length > 0"
      class="bottom-bar"
    >
      <view class="bottom-bar__info">
        <text class="bottom-bar__name">
          {{ selectedPlan?.name || '请选择套餐' }}
        </text>
        <text class="bottom-bar__sub">
          {{ selectedPlan ? `${selectedPlan.durationDays} 天 · 赠送 ${selectedPlan.giftPpd || 0} 拍拍豆` : '开通会员享受更多权益' }}
        </text>
      </view>
      <view
        class="bottom-bar__button"
        :class="{ 'bottom-bar__button--disabled': !selectedPlan || submitting }"
        @tap="submitSelected"
      >
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
import { getPaymentChannel, redirectToH5Pay, toMiniappPayParams } from '@/services/payment-channel'
import type { MemberPlan, PaymentCreateResult } from '@/api/types'

const userStore = useUserStore()
const memberStore = useMemberStore()
const loading = ref(false)
const submitting = ref(false)
const plans = ref<MemberPlan[]>([])
const selectedPlanId = ref<number | null>(null)

const status = computed(() => memberStore.status)
const selectedPlan = computed(() => plans.value.find((plan) => plan.id === selectedPlanId.value) || null)
const benefitItems = computed(() => {
  const gift = selectedPlan.value?.giftPpd || 0
  return [
    { icon: '曝', title: '同城优先曝光', desc: '作品与约拍信息获得更靠前展示机会' },
    { icon: '标', title: '专属会员标识', desc: '主页与沟通场景展示会员身份，增强可信度' },
    { icon: '惠', title: '发布约拍优惠', desc: '发布撮合类需求可享会员专属拍拍豆优惠' },
    { icon: '豆', title: '开通赠送拍拍豆', desc: gift > 0 ? `当前套餐赠送 ${gift} 拍拍豆` : '部分套餐含额外拍拍豆奖励' },
  ]
})

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
    const result = await memberApi.createMemberOrder(plan.id, getPaymentChannel())
    if (!result.success || !result.data) {
      uni.showToast({ title: result.message || '下单失败', icon: 'none' })
      return
    }
    await launchWxPay(result.data)
  } finally {
    submitting.value = false
  }
}

async function launchWxPay(payload: PaymentCreateResult): Promise<void> {
  return new Promise((resolve) => {
    if (payload.channel === 'H5') {
      try {
        redirectToH5Pay(payload.h5Url)
      } catch (error) {
        uni.showToast({ title: error instanceof Error ? error.message : '支付链接缺失', icon: 'none' })
      }
      resolve()
      return
    }
    // #ifdef MP-WEIXIN
    const payParams = toMiniappPayParams(payload)
    uni.requestPayment({
      provider: 'wxpay',
      timeStamp: payParams.timeStamp,
      nonceStr: payParams.nonceStr,
      package: payParams.package,
      signType: payParams.signType as never,
      paySign: payParams.paySign,
      orderInfo: '',
      success: () => {
        void onPaySuccess(payload.outTradeNo).finally(resolve)
      },
      fail: (err: { errMsg?: string }) => {
        const cancelled = err?.errMsg?.includes('cancel')
        uni.showToast({ title: cancelled ? '已取消支付' : '支付失败', icon: 'none' })
        resolve()
      },
    } as UniApp.RequestPaymentOptions)
    // #endif
    // #ifdef H5
    uni.showToast({ title: '正在跳转支付', icon: 'none' })
    resolve()
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
.page { min-height: 100vh; padding: 24rpx 28rpx calc(180rpx + env(safe-area-inset-bottom)); background: linear-gradient(180deg, #F5F7F9 0%, $color-bg-page 360rpx); }

.hero { position: relative; overflow: hidden; padding: 26rpx 28rpx 28rpx; border-radius: $radius-keep-card; color: #fff; background: linear-gradient(120deg, #252833 0%, #3C344B 58%, #315B4D 100%); box-shadow: 0 18rpx 42rpx rgba(43, 35, 30, 0.16); }
.hero::before { content: ''; position: absolute; top: -92rpx; right: -64rpx; width: 210rpx; height: 210rpx; border-radius: 50%; background: rgba(255, 218, 130, 0.2); }
.hero::after { content: ''; position: absolute; right: 28rpx; bottom: 24rpx; width: 96rpx; height: 96rpx; border: 2rpx solid rgba(255, 224, 138, 0.24); border-radius: 50%; }
.hero--active { background: linear-gradient(120deg, #18291F 0%, $color-primary-dark 56%, #B28333 100%); }
.hero__top { position: relative; z-index: 1; display: flex; align-items: center; justify-content: space-between; }
.hero__badge { padding: 6rpx 16rpx; border-radius: $radius-round; color: #5A3A00; background: linear-gradient(135deg, #FFE9A6, #F5B642); font-size: 21rpx; font-weight: 900; letter-spacing: 0; }
.hero__state { color: rgba(255, 255, 255, 0.78); font-size: 22rpx; font-weight: 700; }
.hero__label { position: relative; z-index: 1; display: block; margin-top: 26rpx; color: #fff; font-size: 38rpx; font-weight: 900; }
.hero__line { position: relative; z-index: 1; display: block; margin-top: 10rpx; color: rgba(255, 255, 255, 0.76); font-size: 23rpx; font-weight: 700; }
.hero__features { position: relative; z-index: 1; display: flex; flex-wrap: wrap; gap: 10rpx; margin-top: 18rpx; }
.hero__features text { padding: 8rpx 15rpx; border: 1rpx solid rgba(255, 232, 174, 0.28); border-radius: $radius-round; color: #FFE8AE; background: rgba(255, 255, 255, 0.08); font-size: 20rpx; font-weight: 800; }
.hero__hint { position: relative; z-index: 1; display: block; margin-top: 16rpx; color: rgba(255, 255, 255, 0.74); font-size: 22rpx; }

.loading { padding: 80rpx 0; color: $color-text-helper; font-size: 26rpx; text-align: center; }

.section { margin-top: 22rpx; }
.section--benefits { padding: 24rpx; border-radius: $radius-keep-card; background: #fff; box-shadow: $shadow-keep-card; }
.section__head { display: flex; align-items: flex-end; justify-content: space-between; gap: 18rpx; margin-bottom: 16rpx; }
.section__title { color: $color-text-primary; font-size: 30rpx; font-weight: 900; }
.section__sub { flex: none; color: $color-text-helper; font-size: 21rpx; font-weight: 700; }

.benefit-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 14rpx; }
.benefit-item { min-width: 0; padding: 18rpx 16rpx; border-radius: $radius-keep-card; background: #F7FAF8; }
.benefit-item__icon { display: flex; align-items: center; justify-content: center; width: 48rpx; height: 48rpx; border-radius: 14rpx; color: #6A4300; background: linear-gradient(135deg, #FFE8A8, #F4BD51); font-size: 22rpx; font-weight: 900; }
.benefit-item__title { display: block; margin-top: 12rpx; color: $color-text-primary; font-size: 24rpx; font-weight: 900; }
.benefit-item__desc { display: block; margin-top: 6rpx; color: $color-text-secondary; font-size: 20rpx; line-height: 1.45; }

.plans { display: grid; grid-template-columns: repeat(2, minmax(0, 1fr)); gap: 14rpx; }
.plan-card { position: relative; min-width: 0; min-height: 196rpx; overflow: hidden; padding: 22rpx 20rpx 48rpx; border: 2rpx solid transparent; border-radius: $radius-keep-card; background: #fff; box-shadow: $shadow-keep-card; }
.plan-card:last-child:nth-child(odd) { grid-column: 1 / -1; min-height: 150rpx; }
.plan-card--recommended { border-color: rgba(245, 182, 66, 0.58); }
.plan-card--selected { border-color: $color-primary; background: #F9FCFA; box-shadow: 0 14rpx 34rpx rgba(23, 168, 87, 0.14); }
.plan-card__main { display: block; }
.plan-card:last-child:nth-child(odd) .plan-card__main { display: grid; grid-template-columns: minmax(0, 1fr) auto; align-items: center; gap: 24rpx; }
.plan-card__info { min-width: 0; }
.plan-card__name { display: block; padding-right: 62rpx; color: $color-text-primary; font-size: 27rpx; font-weight: 900; line-height: 1.25; }
.plan-card__tag { position: absolute; top: 14rpx; right: 14rpx; z-index: 1; padding: 3rpx 10rpx; border-radius: $radius-round; color: #6A4300; background: #FFE8A8; font-size: 18rpx; font-weight: 900; }
.plan-card__duration { display: block; margin-top: 8rpx; color: $color-text-secondary; font-size: 21rpx; font-weight: 700; }
.plan-card__chips { display: flex; flex-wrap: wrap; gap: 8rpx; margin-top: 12rpx; }
.plan-card__chip { padding: 6rpx 10rpx; border-radius: $radius-round; color: $color-primary-dark; background: rgba(23, 168, 87, 0.1); font-size: 18rpx; font-weight: 800; }
.plan-card__price { display: flex; flex-direction: column; align-items: flex-start; margin-top: 14rpx; }
.plan-card:last-child:nth-child(odd) .plan-card__price { align-items: flex-end; margin-top: 0; padding-right: 4rpx; }
.plan-card__price-yuan { color: #C66F1A; font-size: 34rpx; font-weight: 900; }
.plan-card__price-origin { margin-top: 3rpx; color: $color-text-helper; font-size: 19rpx; text-decoration: line-through; }
.plan-card__check { position: absolute; right: 16rpx; bottom: 14rpx; display: flex; align-items: center; justify-content: center; width: 30rpx; height: 30rpx; border: 2rpx solid #C8D1CB; border-radius: 50%; color: transparent; background: #fff; font-size: 18rpx; font-weight: 900; }
.plan-card__check--selected { border-color: $color-primary; color: #fff; background: $color-primary; }

.agreement { margin-top: 32rpx; color: $color-text-helper; font-size: 22rpx; text-align: center; }
.orders-link { margin-top: 24rpx; padding: 22rpx 0; color: $color-primary-dark; font-size: 26rpx; font-weight: 800; text-align: center; }
.bottom-bar { position: fixed; right: 0; bottom: 0; left: 0; z-index: 10; display: flex; align-items: center; justify-content: space-between; gap: 24rpx; padding: 20rpx 28rpx calc(20rpx + env(safe-area-inset-bottom)); background: #fff; box-shadow: 0 -12rpx 36rpx rgba(15, 23, 42, 0.08); }
.bottom-bar__info { min-width: 0; flex: 1; }
.bottom-bar__name { display: block; overflow: hidden; color: $color-text-primary; font-size: 28rpx; font-weight: 900; text-overflow: ellipsis; white-space: nowrap; }
.bottom-bar__sub { display: block; margin-top: 6rpx; overflow: hidden; color: $color-text-secondary; font-size: 22rpx; text-overflow: ellipsis; white-space: nowrap; }
.bottom-bar__button { width: 250rpx; height: 76rpx; border-radius: 999rpx; color: #fff; background: $color-primary; font-size: 28rpx; font-weight: 900; line-height: 76rpx; text-align: center; }
.bottom-bar__button--disabled { opacity: 0.55; }
</style>
