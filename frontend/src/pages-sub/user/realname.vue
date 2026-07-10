<template>
  <view class="page">
    <KeepPageNav title="实名认证" />
    <view v-if="loading" class="state">正在查询认证状态...</view>

    <view v-else-if="authInfo?.status === '1'" class="state card">
      <text class="title">审核中</text>
      <text class="desc">资料已提交，请耐心等待审核。</text>
    </view>

    <view v-else-if="authInfo?.status === '2'" class="state card">
      <text class="title success">已认证</text>
      <text class="desc">{{ maskedName }} {{ maskedCode }}</text>
    </view>

    <view v-else class="card">
      <view v-if="status === '3'" class="warning">审核未通过，请核对资料后重新提交。本次重新提交无需再次支付。</view>

      <text class="label">身份证正面</text>
      <view class="picker" @tap="chooseImage('front')">
        <image v-if="frontPath" :src="frontPath" mode="aspectFill" />
        <text v-else>点击选择身份证正面</text>
      </view>

      <text class="label">身份证反面</text>
      <view class="picker" @tap="chooseImage('back')">
        <image v-if="backPath" :src="backPath" mode="aspectFill" />
        <text v-else>点击选择身份证反面</text>
      </view>

      <text class="label">手持身份证</text>
      <view class="picker" @tap="chooseImage('hand')">
        <image v-if="handPath" :src="handPath" mode="aspectFill" />
        <text v-else>点击选择手持身份证照片</text>
      </view>

      <text class="label">真实姓名</text>
      <input v-model="form.name" class="input" maxlength="30" placeholder="请输入真实姓名" />

      <text class="label">证件号码</text>
      <input v-model="form.certcode" class="input" maxlength="18" placeholder="请输入证件号码" />

      <button class="submit" :disabled="submitDisabled" :loading="busy" @tap="submit">
        {{ busy ? '处理中...' : (canSubmitWithoutPay ? '提交认证' : '支付并提交') }}
      </button>
      <text class="privacy">实名认证审核费 29 元；审核失败后重新提交无需再次支付。页面退出后会清理本地临时资料。</text>
    </view>
  </view>
</template>

<script setup lang="ts">
import KeepPageNav from '@/components/business/KeepPageNav.vue'
import { computed, reactive, ref } from 'vue'
import { onLoad, onUnload } from '@dcloudio/uni-app'
import * as oauthApi from '@/api/modules/oauth'
import * as paymentApi from '@/api/modules/payment'
import { useUserStore } from '@/stores/user'
import type { OauthInfo, OrderInfo } from '@/api/types'

const REALNAME_PHOTO_COUNT = oauthApi.REALNAME_PHOTO_COUNT
const PAYMENT_POLL_LIMIT = 20
const PAYMENT_POLL_INTERVAL_MS = 1500

const userStore = useUserStore()
const loading = ref(true)
const submitting = ref(false)
const paying = ref(false)
const authInfo = ref<OauthInfo | null>(null)
const frontPath = ref('')
const backPath = ref('')
const handPath = ref('')
const form = reactive({ name: '', certcode: '' })
let pollingCancelled = false

const status = computed(() => authInfo.value?.status || userStore.userInfo?.status || '0')
const canSubmitWithoutPay = computed(() => status.value === '3' || status.value === '4')
const needsPayment = computed(() => status.value === '0' || !status.value)
const busy = computed(() => submitting.value || paying.value)
const selectedPhotos = computed(() => [frontPath.value, backPath.value, handPath.value].filter(Boolean))
const formInvalid = computed(() => (
  !form.name.trim()
  || !/^\d{15}$|^\d{17}[\dXx]$/.test(form.certcode.trim())
  || selectedPhotos.value.length !== REALNAME_PHOTO_COUNT
))
const submitDisabled = computed(() => busy.value || formInvalid.value)

const maskedName = computed(() => {
  const value = authInfo.value?.name || ''
  return value.length > 1 ? `${value[0]}${'*'.repeat(value.length - 1)}` : value
})

const maskedCode = computed(() => {
  const value = authInfo.value?.certcode || ''
  return value.length > 8 ? `${value.slice(0, 3)}***********${value.slice(-4)}` : value
})

async function loadDetail(): Promise<void> {
  loading.value = true
  try {
    authInfo.value = (await oauthApi.getAuthDetail()).data || null
  } catch {
    authInfo.value = null
  } finally {
    loading.value = false
  }
}

function chooseImage(side: 'front' | 'back' | 'hand'): void {
  uni.chooseImage({
    count: 1,
    sizeType: ['compressed'],
    sourceType: ['album', 'camera'],
    success: async ({ tempFilePaths }) => {
      const path = tempFilePaths[0]
      if (!path) return
      if (side === 'front') {
        frontPath.value = path
        await recognizeFront(path)
      } else if (side === 'back') {
        backPath.value = path
      } else {
        handPath.value = path
      }
    },
  })
}

async function recognizeFront(path: string): Promise<void> {
  uni.showLoading({ title: '识别中...' })
  try {
    const result = await oauthApi.ocrIdCard(path)
    form.name = result.data?.name || ''
    form.certcode = result.data?.certcode || ''
  } catch {
    uni.showToast({ title: '识别失败，请手动填写', icon: 'none' })
  } finally {
    uni.hideLoading()
  }
}

async function submit(): Promise<void> {
  if (submitDisabled.value) return
  if (canSubmitWithoutPay.value) {
    await submitAfterPaymentConfirmed()
    return
  }
  if (needsPayment.value) {
    await confirmAndPay()
    return
  }
  uni.showToast({ title: '当前状态暂不可提交', icon: 'none' })
}

async function confirmAndPay(): Promise<void> {
  const confirmed = await confirmRealnamePayment()
  if (!confirmed) return

  paying.value = true
  pollingCancelled = false
  try {
    const order = await createRealnameOrder()
    await invokeWechatPayment({
      timeStamp: order.timeStamp,
      nonceStr: order.nonceStr,
      package: order.package,
      signType: order.signType || 'HMAC-SHA256',
      paySign: order.paySign,
    })

    uni.showLoading({ title: '服务端确认中...' })
    const paid = await waitForRealnamePayment(order.out_trade_no)
    uni.hideLoading()
    if (!paid) {
      uni.showToast({ title: '支付确认中，请稍后重试', icon: 'none' })
      return
    }

    await userStore.updateUserInfo()
    await loadDetail()
    await submitAfterPaymentConfirmed()
  } catch (error) {
    uni.hideLoading()
    const message = error instanceof Error ? error.message : '支付失败'
    if (message.toLowerCase().includes('cancel')) {
      uni.showToast({ title: '已取消支付', icon: 'none' })
    } else {
      uni.showToast({ title: message, icon: 'none' })
    }
  } finally {
    paying.value = false
  }
}

function confirmRealnamePayment(): Promise<boolean> {
  return new Promise((resolve) => {
    uni.showModal({
      title: '实名认证',
      content: `实名信息需人工审核，将收取 ${oauthApi.REALNAME_AUDIT_FEE_YUAN} 元审核费。审核失败后重新提交无需再次支付。`,
      confirmText: '去支付',
      cancelText: '取消',
      success: ({ confirm }) => resolve(Boolean(confirm)),
      fail: () => resolve(false),
    })
  })
}

async function createRealnameOrder() {
  const order = await paymentApi.createOrder({
    type: oauthApi.REALNAME_ORDER_TYPE,
    total_fee: oauthApi.REALNAME_AUDIT_FEE_YUAN,
  })
  if (!order.data?.package || !order.data.timeStamp || !order.data.nonceStr || !order.data.paySign || !order.data.out_trade_no) {
    throw new Error('支付参数不完整')
  }
  return order.data
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

async function waitForRealnamePayment(outTradeNo: string): Promise<boolean> {
  for (let index = 0; index < PAYMENT_POLL_LIMIT && !pollingCancelled; index += 1) {
    try {
      const result = await paymentApi.getOrderStatus(outTradeNo)
      const paid = (result.data?.content || []).some(isPaidRealnameOrder)
      if (paid) return true
    } catch {
      // 微信回调可能晚于前端支付完成，限定次数内继续确认。
    }
    await delay(PAYMENT_POLL_INTERVAL_MS)
  }
  return false
}

function isPaidRealnameOrder(order: OrderInfo): boolean {
  return order.type === oauthApi.REALNAME_ORDER_TYPE && (order.status === '1' || order.result_code === 'SUCCESS')
}

function delay(ms: number): Promise<void> {
  return new Promise((resolve) => setTimeout(resolve, ms))
}

async function submitAfterPaymentConfirmed(): Promise<void> {
  if (formInvalid.value) return
  submitting.value = true
  try {
    await oauthApi.submitAuth({
      name: form.name.trim(),
      certcode: form.certcode.trim().toUpperCase(),
      pics: [frontPath.value, backPath.value, handPath.value],
    })
    clearForm()
    await userStore.updateUserInfo()
    uni.showToast({ title: '提交成功', icon: 'success' })
    await loadDetail()
  } catch (error) {
    uni.showToast({ title: error instanceof Error ? error.message : '提交失败', icon: 'none' })
  } finally {
    submitting.value = false
  }
}

function clearForm(): void {
  form.name = ''
  form.certcode = ''
  frontPath.value = ''
  backPath.value = ''
  handPath.value = ''
}

onLoad(loadDetail)
onUnload(() => {
  pollingCancelled = true
  clearForm()
})
</script>

<style scoped lang="scss">

.page { min-height: 100vh; box-sizing: border-box; padding: 28rpx; background: $color-bg-page; }
.card { padding: 32rpx; border-radius: $radius-keep-card; background: $color-bg-card; box-shadow: $shadow-keep-card; }
.state { padding: 180rpx 30rpx; color: $color-text-secondary; text-align: center; }
.title { display: block; color: $color-text-primary; font-size: 36rpx; font-weight: 600; }
.success { color: $color-primary-dark; }
.desc, .privacy { display: block; margin-top: 18rpx; color: $color-text-secondary; font-size: 25rpx; text-align: center; }
.warning { margin-bottom: 24rpx; padding: 20rpx; border-radius: 16rpx; color: #b4232c; background: #FFF3DF; }
.label { display: block; margin: 28rpx 0 12rpx; font-weight: 600; }
.picker { height: 260rpx; display: flex; align-items: center; justify-content: center; overflow: hidden; border: 2rpx dashed #cfd5dd; border-radius: 20rpx; color: $color-text-secondary; background: $color-bg-chip; }
.picker image { width: 100%; height: 100%; }
.input { height: 88rpx; box-sizing: border-box; padding: 0 22rpx; border-radius: 16rpx; background: $color-bg-page; }
.submit { margin-top: 38rpx; height: 92rpx; line-height: 92rpx; border-radius: 999rpx; color: #fff; background: $color-primary; }
.submit[disabled] { opacity: .45; }
.submit::after { border: 0; }
</style>
