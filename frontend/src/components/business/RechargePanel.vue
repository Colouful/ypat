<template>
  <view class="recharge">
    <view class="balance-card">
      <text class="balance-card__label">当前拍拍豆</text>
      <text class="balance-card__value">{{ userStore.userInfo?.ppd || 0 }}</text>
    </view>

    <view v-if="loading" class="state">商品加载中...</view>
    <view v-else class="products">
      <view v-for="item in products" :key="item.id" class="product" :class="{ active: selectedId === item.id }" @tap="selectedId = item.id">
        <text class="product__amount">{{ item.currval }} 拍拍豆</text>
        <text class="product__price">¥{{ formatPrice(item.oldval) }}</text>
      </view>
    </view>

    <!-- #ifdef MP-WEIXIN -->
    <button class="pay" :disabled="!selected || paying" :loading="paying" @tap="pay">
      {{ paying ? '处理中...' : `立即支付 ¥${selected ? formatPrice(selected.oldval) : '0.00'}` }}
    </button>
    <!-- #endif -->

    <!-- #ifndef MP-WEIXIN -->
    <view class="unsupported">当前后端仅配置微信小程序支付，H5/App 暂不提供充值入口。</view>
    <!-- #endif -->
  </view>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { onLoad, onUnload } from '@dcloudio/uni-app'
import { useUserStore } from '@/stores/user'
import * as paymentApi from '@/api/modules/payment'
import type { OrderInfo, Product } from '@/api/types'

const userStore = useUserStore()
const loading = ref(true)
const paying = ref(false)
const products = ref<Product[]>([])
const selectedId = ref<number | null>(null)
let pollingCancelled = false

const selected = computed(() => products.value.find((item) => item.id === selectedId.value) || null)

function formatPrice(value: number): string {
  return (Number(value || 0) / 100).toFixed(2)
}

async function loadProducts(): Promise<void> {
  loading.value = true
  try {
    const result = await paymentApi.getProductList({ page: 0, size: 20, status: '1' })
    products.value = result.data?.content || []
    selectedId.value = products.value[0]?.id || null
  } catch (error) {
    uni.showToast({ title: error instanceof Error ? error.message : '商品加载失败', icon: 'none' })
  } finally {
    loading.value = false
  }
}

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
      // 支付回调或订单服务可能暂时不可用，继续在限定次数内查询。
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
  if (!selected.value || paying.value || !userStore.userInfo?.id) return

  paying.value = true
  pollingCancelled = false

  try {
    const order = await paymentApi.createOrder({
      type: '0',
      productid: selected.value.id,
      total_fee: selected.value.oldval,
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
      uni.showToast({ title: '充值成功', icon: 'success' })
      setTimeout(() => uni.navigateBack(), 1200)
      return
    }

    if (state === 'failed') {
      throw new Error('服务端确认支付失败')
    }

    uni.showModal({
      title: '支付结果确认中',
      content: '服务端暂未确认到账，请稍后在钱包页面刷新。系统不会在前端自行增加余额。',
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

onLoad(loadProducts)
onUnload(() => {
  pollingCancelled = true
})
</script>

<style scoped lang="scss">

.recharge { min-height: 100vh; box-sizing: border-box; padding: 28rpx; background: $color-bg-page; }
.balance-card { padding: 38rpx; border-radius: $radius-keep-card; color: #fff; background: linear-gradient(135deg, $color-primary, #64d995); }
.balance-card__label { display: block; font-size: 26rpx; opacity: .9; }
.balance-card__value { display: block; margin-top: 10rpx; font-size: 64rpx; font-weight: 700; }
.state, .unsupported { margin-top: 28rpx; padding: 32rpx; border-radius: $radius-keep-card; color: #747e8d; background: $color-bg-card; box-shadow: $shadow-keep-card; text-align: center; }
.products { display: grid; grid-template-columns: repeat(2, 1fr); gap: 20rpx; margin-top: 28rpx; }
.product { padding: 34rpx 20rpx; border: 2rpx solid transparent; border-radius: $radius-keep-card; background: $color-bg-card; box-shadow: $shadow-keep-card; text-align: center; }
.product.active { border-color: $color-primary; background: $color-primary-light; }
.product__amount { display: block; color: $color-text-primary; font-size: 30rpx; font-weight: 600; }
.product__price { display: block; margin-top: 12rpx; color: #f26a3d; font-size: 27rpx; }
.pay { margin-top: 40rpx; height: 92rpx; line-height: 92rpx; border-radius: 999rpx; color: #fff; background: $color-primary; }
.pay[disabled] { opacity: .45; }
.pay::after { border: 0; }
</style>
