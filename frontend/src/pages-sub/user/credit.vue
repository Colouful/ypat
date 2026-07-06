<template>
  <view class="page">
    <KeepPageNav title="信用担保" />

    <view class="hero" :class="{ 'hero--done': isGuaranteed }">
      <view class="hero__icon">
        <KeepIcon name="shield" :size="58" color="#fff" />
      </view>
      <text class="hero__title">{{ isGuaranteed ? '已开通信用担保' : '开通信用担保' }}</text>
      <text class="hero__desc">
        {{ isGuaranteed ? '你的主页会展示信用担保标识。' : '缴纳保证金后，主页将展示信用担保标识。' }}
      </text>
    </view>

    <view class="deposit-card">
      <view>
        <text class="deposit-card__label">保证金金额</text>
        <view class="deposit-card__price">
          <text class="deposit-card__unit">¥</text>
          <text class="deposit-card__amount">199</text>
        </view>
      </view>
      <view class="deposit-card__tag">可申请退还</view>
    </view>

    <view class="section">
      <text class="section__title">保证金说明</text>
      <view v-for="item in notes" :key="item" class="note-row">
        <view class="note-row__dot" />
        <text class="note-row__text">{{ item }}</text>
      </view>
    </view>

    <view class="agreement" @tap="accepted = !accepted">
      <view class="checkbox" :class="{ 'checkbox--checked': accepted }">
        <KeepIcon v-if="accepted" name="check" :size="24" color="#fff" />
      </view>
      <text>我已阅读并同意</text>
      <text class="agreement__link" @tap.stop="showAgreement">《保证金协议》</text>
    </view>

    <!-- #ifdef MP-WEIXIN -->
    <button
      class="pay-btn"
      :class="{ 'pay-btn--disabled': !canPay }"
      :disabled="!canPay"
      :loading="paying"
      @tap="pay"
    >
      {{ isGuaranteed ? '已完成信用担保' : paying ? '处理中...' : '立即缴纳保证金' }}
    </button>
    <!-- #endif -->

    <!-- #ifndef MP-WEIXIN -->
    <view class="unsupported">当前后端仅配置微信小程序支付，请在小程序内缴纳保证金。</view>
    <!-- #endif -->
  </view>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { onUnload } from '@dcloudio/uni-app'
import KeepIcon from '@/components/business/KeepIcon.vue'
import KeepPageNav from '@/components/business/KeepPageNav.vue'
import { useUserStore } from '@/stores/user'
import * as paymentApi from '@/api/modules/payment'
import type { OrderInfo } from '@/api/types'

const DEPOSIT_FEE_FEN = 19900

const userStore = useUserStore()
const accepted = ref(false)
const paying = ref(false)
let pollingCancelled = false

const isGuaranteed = computed(() => userStore.userInfo?.creditflag === '1')
const canPay = computed(() => accepted.value && !paying.value && !isGuaranteed.value)

const notes = [
  '保证金用于提升约拍双方的信用识别，不作为任何形式的交易款项。',
  '缴纳之日起满 3 个月后，可联系平台客服申请退还。',
  '提前退款将按旧版规则收取保证金金额 15% 作为平台管理费。',
  '保证金退款后，将不再展示信用担保标识。',
]

function isPaid(order?: OrderInfo): boolean {
  return order?.result_code === 'SUCCESS' || order?.status === '1'
}

function isFailed(order?: OrderInfo): boolean {
  return order?.result_code === 'FAIL' || Boolean(order?.err_code)
}

async function waitForServerConfirmation(outTradeNo: string, attempts = 10): Promise<'paid' | 'failed' | 'pending'> {
  for (let index = 0; index < attempts && !pollingCancelled; index += 1) {
    await new Promise((resolve) => setTimeout(resolve, 2000))
    try {
      const result = await paymentApi.getOrderStatus(outTradeNo)
      const order = result.data?.content?.[0]
      if (isPaid(order)) return 'paid'
      if (isFailed(order)) return 'failed'
    } catch {
      // 支付回调或订单服务可能短暂延迟，限定次数内继续确认。
    }
  }
  return 'pending'
}

function invokeWechatPayment(data: {
  timeStamp: string
  nonceStr: string
  package: string
  signType: string
  paySign: string
}): Promise<void> {
  return new Promise((resolve, reject) => {
    uni.requestPayment({
      provider: 'wxpay',
      orderInfo: {},
      timeStamp: data.timeStamp,
      nonceStr: data.nonceStr,
      package: data.package,
      signType: data.signType as 'MD5' | 'HMAC-SHA256',
      paySign: data.paySign,
      success: () => resolve(),
      fail: (error) => reject(new Error(error.errMsg || '支付失败')),
    })
  })
}

async function pay(): Promise<void> {
  if (isGuaranteed.value) {
    uni.showToast({ title: '已信用担保', icon: 'none' })
    return
  }
  if (!accepted.value) {
    uni.showToast({ title: '请先同意保证金协议', icon: 'none' })
    return
  }
  if (paying.value) return

  paying.value = true
  pollingCancelled = false

  try {
    const order = await paymentApi.createOrder({
      type: '2',
      productid: 0,
      total_fee: DEPOSIT_FEE_FEN,
    })

    if (!order.data?.package || !order.data.timeStamp || !order.data.nonceStr || !order.data.paySign || !order.data.out_trade_no) {
      throw new Error('支付参数不完整')
    }

    await invokeWechatPayment({
      timeStamp: order.data.timeStamp,
      nonceStr: order.data.nonceStr,
      package: order.data.package,
      signType: order.data.signType || 'HMAC-SHA256',
      paySign: order.data.paySign,
    })

    uni.showLoading({ title: '服务端确认中...' })
    const state = await waitForServerConfirmation(order.data.out_trade_no)
    uni.hideLoading()

    if (state === 'paid') {
      await userStore.updateUserInfo()
      uni.showToast({ title: '担保开通成功', icon: 'success' })
      setTimeout(() => uni.navigateBack(), 1200)
      return
    }

    if (state === 'failed') {
      throw new Error('服务端确认支付失败')
    }

    uni.showModal({
      title: '支付结果确认中',
      content: '服务端暂未确认信用担保状态，请稍后回到我的页面刷新查看。',
      showCancel: false,
    })
  } catch (error) {
    uni.hideLoading()
    const message = error instanceof Error ? error.message : '支付失败'
    if (message.toLowerCase().includes('cancel')) uni.showToast({ title: '已取消支付', icon: 'none' })
    else uni.showToast({ title: message, icon: 'none' })
  } finally {
    paying.value = false
  }
}

function showAgreement(): void {
  uni.showModal({
    title: '保证金协议',
    content: '保证金用于约拍诚信担保。平台将依据规则冻结、使用或处置保证金；退款、提前退款管理费及担保权益终止规则以平台公示为准。',
    showCancel: false,
  })
}

onUnload(() => {
  pollingCancelled = true
})
</script>

<style scoped lang="scss">
.page {
  min-height: 100vh;
  box-sizing: border-box;
  padding: 24rpx 28rpx calc(64rpx + env(safe-area-inset-bottom));
  background: $color-bg-page;
}

.hero {
  position: relative;
  overflow: hidden;
  padding: 44rpx 34rpx;
  border-radius: $radius-keep-card;
  color: #fff;
  background: linear-gradient(135deg, #1A1D1F, #34403A);
  box-shadow: $shadow-keep-card;
}

.hero--done {
  background: linear-gradient(135deg, $color-primary-dark, $color-primary);
}

.hero__icon {
  @include flex-center;
  width: 104rpx;
  height: 104rpx;
  border-radius: 32rpx;
  background: rgba(255, 255, 255, 0.16);
}

.hero__title {
  display: block;
  margin-top: 28rpx;
  font-size: 42rpx;
  font-weight: 900;
}

.hero__desc {
  display: block;
  margin-top: 12rpx;
  opacity: 0.78;
  font-size: 25rpx;
  font-weight: 700;
  line-height: 1.6;
}

.deposit-card {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 24rpx;
  padding: 34rpx 32rpx;
  border-radius: $radius-keep-card;
  background: #fff;
  box-shadow: $shadow-keep-card;
}

.deposit-card__label {
  display: block;
  color: $color-text-secondary;
  font-size: 25rpx;
  font-weight: 800;
}

.deposit-card__price {
  display: flex;
  align-items: baseline;
  margin-top: 8rpx;
  color: $color-text-primary;
}

.deposit-card__unit {
  font-size: 30rpx;
  font-weight: 900;
}

.deposit-card__amount {
  margin-left: 4rpx;
  font-size: 66rpx;
  font-weight: 900;
  line-height: 1;
}

.deposit-card__tag {
  flex: none;
  padding: 12rpx 22rpx;
  border-radius: $radius-round;
  color: $color-primary-dark;
  background: $color-primary-light;
  font-size: 24rpx;
  font-weight: 900;
}

.section {
  margin-top: 24rpx;
  padding: 32rpx;
  border-radius: $radius-keep-card;
  background: #fff;
  box-shadow: $shadow-keep-card;
}

.section__title {
  display: block;
  color: $color-text-primary;
  font-size: 31rpx;
  font-weight: 900;
}

.note-row {
  display: flex;
  gap: 16rpx;
  margin-top: 22rpx;
}

.note-row__dot {
  width: 12rpx;
  height: 12rpx;
  flex: none;
  margin-top: 13rpx;
  border-radius: 50%;
  background: $color-primary;
}

.note-row__text {
  color: $color-text-secondary;
  font-size: 25rpx;
  font-weight: 700;
  line-height: 1.55;
}

.agreement {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 8rpx;
  margin-top: 26rpx;
  color: $color-text-secondary;
  font-size: 24rpx;
  font-weight: 700;
}

.checkbox {
  @include flex-center;
  width: 34rpx;
  height: 34rpx;
  flex: none;
  margin-right: 6rpx;
  border: 2rpx solid $color-primary;
  border-radius: 50%;
  background: #fff;
}

.checkbox--checked {
  background: $color-primary;
}

.agreement__link {
  color: $color-primary-dark;
  font-weight: 900;
}

.pay-btn {
  margin-top: 34rpx;
  height: 92rpx;
  border-radius: 999rpx;
  color: #fff;
  background: $color-primary;
  font-size: 28rpx;
  font-weight: 900;
  line-height: 92rpx;
}

.pay-btn--disabled {
  opacity: 0.45;
}

.pay-btn::after {
  border: 0;
}

.unsupported {
  margin-top: 28rpx;
  padding: 30rpx;
  border-radius: $radius-keep-card;
  color: $color-text-secondary;
  background: #fff;
  box-shadow: $shadow-keep-card;
  font-size: 25rpx;
  font-weight: 700;
  line-height: 1.55;
  text-align: center;
}
</style>
