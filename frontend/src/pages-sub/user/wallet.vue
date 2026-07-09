<template>
  <view class="page">
    <view class="hero">
      <KeepPageNav title="我的拍豆" />
      <view class="hero__content">
        <view class="balance">
          <text class="balance__num">{{ userStore.userInfo?.ppd || 0 }}</text>
          <text class="balance__unit">拍豆</text>
        </view>
        <button class="recharge-btn" @tap="openRechargeModal">充值</button>
      </view>
    </view>

    <view class="panel">
      <view class="tabs">
        <view
          v-for="tab in tabs"
          :key="tab.key"
          :class="['tab', { 'tab--active': activeTab === tab.key }]"
          @tap="activeTab = tab.key"
        >
          {{ tab.label }}
        </view>
      </view>

      <view v-if="activeTab === 'earn'" class="tab-panel">
        <view v-for="group in earnGroups" :key="group.title" class="task-group">
          <view class="section-title">{{ group.title }}</view>
          <view v-for="item in group.items" :key="item.title" class="task-row">
            <view class="task-row__main">
              <view class="task-row__title-line">
                <text class="task-row__title">{{ item.title }}</text>
                <text class="task-row__reward">+{{ item.reward }}</text>
              </view>
              <text class="task-row__desc">{{ item.desc }}</text>
            </view>
            <button
              :class="['task-row__action', { 'task-row__action--done': item.done }]"
              :disabled="item.done"
              @tap="item.action"
            >
              {{ item.done ? item.doneText : item.actionText }}
            </button>
          </view>
        </view>
      </view>

      <view v-else-if="activeTab === 'usage'" class="tab-panel">
        <view class="section-title">拍豆用途</view>
        <view class="usage-head">
          <text>用途详情</text>
          <text>非会员</text>
          <text>会员</text>
        </view>
        <view v-for="item in usageRows" :key="item.title" class="usage-row">
          <text>{{ item.title }}</text>
          <text>-{{ item.normal }}</text>
          <text>-{{ item.member }}</text>
        </view>
      </view>

      <view v-else class="tab-panel">
        <view v-if="recordsLoading" class="state">加载中...</view>
        <view v-else-if="records.length === 0" class="state">暂无拍豆记录</view>
        <view v-else>
          <view v-for="item in records" :key="item.id" class="record-row">
            <view>
              <text class="record-row__title">{{ getRecordTypeLabel(item.type) }}</text>
              <text class="record-row__time">{{ formatDate(item.credate) }}</text>
            </view>
            <text :class="['record-row__amount', { 'record-row__amount--out': !isIncome(item.type) }]">
              {{ signedAmount(item) }}
            </text>
          </view>
          <view class="record-more" @tap="goRecords">查看全部记录</view>
        </view>
      </view>
    </view>

    <view v-if="showRechargeModal" class="recharge-modal" @tap="closeRechargeModal">
      <view class="recharge-modal__panel" @tap.stop>
        <view class="recharge-modal__head">
          <text class="recharge-modal__title">拍豆充值</text>
          <text class="recharge-modal__close" @tap="closeRechargeModal">×</text>
        </view>
        <view v-if="productsLoading" class="recharge-empty">套餐加载中...</view>
        <view v-else-if="!hasRechargeProducts" class="recharge-empty">
          <text>暂无可用充值套餐</text>
          <button class="recharge-empty__retry" @tap.stop="loadProducts">重新加载</button>
        </view>
        <view v-else class="product-grid">
          <view
            v-for="item in recommendedProducts"
            :key="item.id"
            :class="[
              'product-card',
              {
                'product-card--active': selectedId === item.id,
                'product-card--recommended': isRecommendedProduct(item),
              },
            ]"
            @tap="selectedId = item.id"
          >
            <text v-if="isRecommendedProduct(item)" class="product-card__badge">优先推荐</text>
            <text class="product-card__label">充值金额</text>
            <text class="product-card__price">¥{{ formatPrice(item.oldval) }}</text>
            <text class="product-card__label product-card__label--ppd">获得拍豆数</text>
            <text class="product-card__amount">{{ item.currval }} 拍豆</text>
          </view>
        </view>

        <!-- #ifdef MP-WEIXIN -->
        <button v-if="hasRechargeProducts" class="pay-btn" :disabled="!selected || paying" :loading="paying" @tap="pay">
          {{ paying ? '处理中...' : `立即充值 ¥${selected ? formatPrice(selected.oldval) : '0.00'}` }}
        </button>
        <!-- #endif -->

        <!-- #ifndef MP-WEIXIN -->
        <view v-if="hasRechargeProducts" class="unsupported">当前仅支持微信小程序支付</view>
        <!-- #endif -->
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import KeepPageNav from '@/components/business/KeepPageNav.vue'
import { computed, ref } from 'vue'
import { onLoad, onShow, onUnload } from '@dcloudio/uni-app'
import { useUserStore } from '@/stores/user'
import * as paymentApi from '@/api/modules/payment'
import * as checkinApi from '@/api/modules/checkin'
import { APPLY_PPD, PUBLISH_PPD, RECORD_TYPE_LABELS, RecordType, VIEW_CONTACT_PPD } from '@/constants/enums'
import type { CheckinToday, OrderInfo, Product, RecordInfo } from '@/api/types'

type TabKey = 'earn' | 'usage' | 'records'

const userStore = useUserStore()
const activeTab = ref<TabKey>('earn')
const recordsLoading = ref(true)
const productsLoading = ref(false)
const paying = ref(false)
const showRechargeModal = ref(false)
const walletLoaded = ref(false)
const checkinToday = ref<CheckinToday | null>(null)
const checkinSubmitting = ref(false)
const records = ref<RecordInfo[]>([])
const products = ref<Product[]>([])
const selectedId = ref<number | null>(null)
const incomeTypes = new Set<string>([RecordType.TOPUP, RecordType.INVITE, RecordType.SYSTEM, RecordType.CHECKIN])
let pollingCancelled = false
let walletRefreshPromise: Promise<void> | null = null
let productLoadPromise: Promise<void> | null = null

const tabs: Array<{ key: TabKey; label: string }> = [
  { key: 'earn', label: '获得拍豆' },
  { key: 'usage', label: '拍豆用途' },
  { key: 'records', label: '拍豆记录' },
]

const recommendedProducts = computed(() => sortProductsByRecommendation(products.value))
const selected = computed(() => products.value.find((item) => item.id === selectedId.value) || null)
const userInfo = computed(() => userStore.userInfo)
const hasRechargeProducts = computed(() => recommendedProducts.value.length > 0)

const earnGroups = computed(() => [
  {
    title: '日常任务',
    items: [
      {
        title: '每日签到',
        reward: checkinToday.value?.rewardPpd || 1,
        desc: '每日签到获得拍豆',
        actionText: '去签到',
        doneText: '已签到',
        done: Boolean(checkinToday.value?.checkedIn),
        action: openCheckinConfirm,
      },
      {
        title: '邀请好友',
        reward: 3,
        desc: '每邀请一个好友，获得3拍豆',
        actionText: '去邀请',
        doneText: '已完成',
        done: false,
        action: () => uni.navigateTo({ url: '/pages-sub/user/invite' }),
      },
    ],
  },
  {
    title: '奖励任务',
    items: [
      {
        title: '发布约拍',
        reward: 5,
        desc: '发布约拍信息，获得更多合作机会',
        actionText: '去发布',
        doneText: '已完成',
        done: false,
        action: () => uni.switchTab({ url: '/pages/publish/index' }),
      },
      {
        title: '完成实名认证',
        reward: 5,
        desc: '完成实名认证可获得拍豆',
        actionText: '去实名',
        doneText: '已完成',
        done: userInfo.value?.realnameflag === '1' || userInfo.value?.status === '2',
        action: () => uni.navigateTo({ url: '/pages-sub/user/realname-intro' }),
      },
    ],
  },
  {
    title: '新手任务',
    items: [
      {
        title: '完善资料',
        reward: 2,
        desc: '完善个人资料，可获得2拍豆',
        actionText: '去完善',
        doneText: '已完成',
        done: Boolean(userInfo.value?.nickname && userInfo.value?.profess),
        action: () => uni.navigateTo({ url: '/pages-sub/user/complete-info' }),
      },
      {
        title: '绑定联系方式',
        reward: 5,
        desc: '绑定联系方式，可获得5拍豆',
        actionText: '去绑定',
        doneText: '已完成',
        done: Boolean(userInfo.value?.mobile || userInfo.value?.wx),
        action: () => uni.navigateTo({ url: '/pages-sub/user/edit-info' }),
      },
    ],
  },
])

const usageRows = [
  { title: '发布约拍信息', normal: PUBLISH_PPD, member: PUBLISH_PPD },
  { title: '刷新约拍信息（实名专享）', normal: PUBLISH_PPD, member: PUBLISH_PPD },
  { title: '提交约拍请求', normal: APPLY_PPD, member: Math.max(1, APPLY_PPD - 1) },
  { title: '查看约拍请求联系方式', normal: VIEW_CONTACT_PPD, member: VIEW_CONTACT_PPD },
]

function formatPrice(value: number): string {
  return (Number(value || 0) / 100).toFixed(2)
}

function isRecommendedProduct(item: Product): boolean {
  return item.recommended === '1'
}

function sortProductsByRecommendation(list: Product[]): Product[] {
  return list
    .map((item, index) => ({ item, index }))
    .sort((left, right) => {
      const recommendDiff = Number(isRecommendedProduct(right.item)) - Number(isRecommendedProduct(left.item))
      if (recommendDiff !== 0) return recommendDiff
      return left.index - right.index
    })
    .map(({ item }) => item)
}

async function fetchRecentRecords(): Promise<void> {
  recordsLoading.value = true
  try {
    const userid = userStore.userInfo?.id
    if (!userid) {
      records.value = []
      return
    }
    const result = await paymentApi.getRecordList({ page: 0, size: 10, userid })
    records.value = result.data?.content || []
  } catch (error) {
    uni.showToast({ title: error instanceof Error ? error.message : '记录加载失败', icon: 'none' })
  } finally {
    recordsLoading.value = false
  }
}

function refreshWalletData(includeProducts = false): Promise<void> {
  if (walletRefreshPromise) return walletRefreshPromise

  walletRefreshPromise = (async () => {
    if (includeProducts && products.value.length === 0) await loadProducts()
    if (walletLoaded.value || !includeProducts) await userStore.updateUserInfo()
    await fetchRecentRecords()
    walletLoaded.value = true
  })().finally(() => {
    walletRefreshPromise = null
  })

  return walletRefreshPromise
}

async function loadProducts(): Promise<void> {
  if (productLoadPromise) return productLoadPromise

  productLoadPromise = (async () => {
    productsLoading.value = true
    try {
      let result = await paymentApi.getProductList({ page: 0, size: 20, status: '0' })
      let content = result.data?.content || []
      if (content.length === 0) {
        result = await paymentApi.getProductList({ page: 0, size: 20, status: '1' })
        content = result.data?.content || []
      }
      const sortedProducts = sortProductsByRecommendation(content)
      products.value = sortedProducts
      selectedId.value = sortedProducts[0]?.id || null
    } catch (error) {
      uni.showToast({ title: error instanceof Error ? error.message : '套餐加载失败', icon: 'none' })
    } finally {
      productsLoading.value = false
      productLoadPromise = null
    }
  })()

  return productLoadPromise
}

function openRechargeModal(): void {
  showRechargeModal.value = true
  if (products.value.length === 0) void loadProducts()
}

function closeRechargeModal(): void {
  if (paying.value) return
  showRechargeModal.value = false
}

async function loadCheckinToday(): Promise<void> {
  if (!userStore.userInfo?.id) {
    checkinToday.value = null
    return
  }
  try {
    const result = await checkinApi.getCheckinToday()
    checkinToday.value = result.data || null
  } catch {
    checkinToday.value = null
  }
}

function openCheckinConfirm(): void {
  if (!userStore.userInfo?.id) {
    uni.showToast({ title: '请先登录', icon: 'none' })
    return
  }
  if (checkinSubmitting.value) return
  if (!checkinToday.value?.enabled) {
    uni.showToast({ title: '签到活动暂未开启', icon: 'none' })
    return
  }
  if (checkinToday.value.checkedIn) {
    uni.showToast({ title: '今日已签到', icon: 'none' })
    return
  }
  uni.showModal({
    title: checkinToday.value.confirmTitle || '每日签到',
    content: checkinToday.value.confirmContent || `签到成功可获得 ${checkinToday.value.rewardPpd || 1} 拍豆`,
    confirmText: '签到',
    success: (res) => {
      if (res.confirm) void submitCheckin()
    },
  })
}

async function submitCheckin(): Promise<void> {
  if (checkinSubmitting.value) return
  checkinSubmitting.value = true
  try {
    const result = await checkinApi.doCheckin()
    const data = result.data
    if (!data?.checkedIn) {
      uni.showToast({ title: data?.message || '签到失败，请稍后重试', icon: 'none' })
      await loadCheckinToday()
      return
    }
    checkinToday.value = {
      ...(checkinToday.value || {
        enabled: true,
        rewardPpd: data.rewardPpd || 1,
        confirmTitle: '每日签到',
        confirmContent: '签到成功可获得 1 拍豆',
        checkinDate: '',
      }),
      checkedIn: true,
      rewardPpd: data.rewardPpd || checkinToday.value?.rewardPpd || 1,
    }
    await refreshWalletData()
    const reward = Number(data.rewardPpd || 0)
    uni.showToast({ title: reward > 0 ? `签到成功，获得 ${reward} 拍豆` : '今日已签到', icon: 'none' })
  } catch {
    uni.showToast({ title: '签到失败，请稍后重试', icon: 'none' })
  } finally {
    checkinSubmitting.value = false
  }
}

function isIncome(type: string): boolean {
  return incomeTypes.has(type)
}

function getRecordTypeLabel(type: string): string {
  return RECORD_TYPE_LABELS[type] || '其他'
}

function signedAmount(item: RecordInfo): string {
  const prefix = isIncome(item.type) ? '+' : '-'
  return `${prefix}${Math.abs(Number(item.ppd || 0))}`
}

function formatDate(value: string): string {
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return value
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}:${String(date.getSeconds()).padStart(2, '0')}`
}

function goRecords(): void {
  uni.navigateTo({ url: '/pages-sub/user/records' })
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
      // 支付回调可能晚于前端支付完成，限定次数内继续查询。
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
      await fetchRecentRecords()
      showRechargeModal.value = false
      uni.showToast({ title: '充值成功', icon: 'success' })
      return
    }

    if (state === 'failed') throw new Error('服务端确认支付失败')

    uni.showModal({
      title: '支付结果确认中',
      content: '服务端暂未确认到账，请稍后刷新钱包。',
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

onLoad(() => {
  void refreshWalletData(true)
  void loadCheckinToday()
})
onShow(() => {
  if (walletLoaded.value) void refreshWalletData()
  void loadCheckinToday()
})
onUnload(() => {
  pollingCancelled = true
})
</script>

<style scoped lang="scss">
.page {
  min-height: 100vh;
  background: $color-bg-page;
}

.hero {
  min-height: 386rpx;
  color: #fff;
  background: linear-gradient(135deg, $color-primary 0%, $color-primary-dark 100%);
}

.hero :deep(.keep-page-nav__bar) {
  background: transparent;
}

.hero :deep(.keep-page-nav__back) {
  background: transparent;
  box-shadow: none;
}

.hero :deep(.keep-page-nav__title) {
  color: #fff;
  font-weight: 500;
}

.hero__content {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 54rpx 32rpx 82rpx;
}

.balance {
  display: flex;
  align-items: baseline;
  gap: 14rpx;
}

.balance__num {
  color: #fff;
  font-size: 92rpx;
  font-weight: 800;
  line-height: 1;
}

.balance__unit {
  color: #fff;
  font-size: 34rpx;
  font-weight: 700;
}

.recharge-btn {
  width: 176rpx;
  height: 76rpx;
  border-radius: 999rpx;
  color: $color-primary-dark;
  background: #fff;
  font-size: 28rpx;
  font-weight: 700;
  line-height: 76rpx;
}

.recharge-btn::after,
.task-row__action::after,
.pay-btn::after {
  border: 0;
}

.panel {
  overflow: hidden;
  margin-top: -46rpx;
  border-radius: 44rpx 44rpx 0 0;
  background: #fff;
}

.tabs {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  height: 86rpx;
  border-bottom: 1rpx solid #f1f1f1;
}

.tab {
  position: relative;
  color: #666;
  font-size: 29rpx;
  line-height: 86rpx;
  text-align: center;
}

.tab--active {
  color: #222;
  font-weight: 700;
}

.tab--active::after {
  position: absolute;
  bottom: 0;
  left: 50%;
  width: 60rpx;
  height: 6rpx;
  border-radius: 999rpx;
  background: $color-primary;
  content: '';
  transform: translateX(-50%);
}

.tab-panel {
  min-height: calc(100vh - 426rpx);
  background: #fff;
}

.task-group {
  border-top: 12rpx solid #f6f6f6;
}

.section-title {
  position: relative;
  padding: 24rpx 28rpx 20rpx 46rpx;
  color: #333;
  font-size: 29rpx;
  font-weight: 700;
}

.section-title::before {
  position: absolute;
  top: 28rpx;
  left: 28rpx;
  width: 6rpx;
  height: 28rpx;
  border-radius: 999rpx;
  background: $color-primary;
  content: '';
}

.task-row {
  display: flex;
  align-items: center;
  gap: 20rpx;
  padding: 22rpx 28rpx;
  border-top: 1rpx solid #f1f1f1;
}

.task-row__main {
  flex: 1;
  min-width: 0;
}

.task-row__title-line {
  display: flex;
  align-items: center;
  gap: 18rpx;
}

.task-row__title {
  color: #333;
  font-size: 29rpx;
  font-weight: 600;
}

.task-row__reward {
  color: $color-primary-dark;
  font-size: 28rpx;
  font-weight: 700;
}

.task-row__desc {
  display: block;
  margin-top: 8rpx;
  overflow: hidden;
  color: #999;
  font-size: 24rpx;
  line-height: 1.35;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.task-row__action {
  flex: none;
  width: 126rpx;
  height: 60rpx;
  border-radius: 999rpx;
  color: #fff;
  background: $color-primary;
  font-size: 25rpx;
  font-weight: 700;
  line-height: 60rpx;
}

.task-row__action--done {
  color: #aaa;
  background: #f3f3f3;
}

.usage-head,
.usage-row {
  display: grid;
  grid-template-columns: 1fr 120rpx 100rpx;
  align-items: center;
  padding: 22rpx 28rpx;
  border-top: 1rpx solid #f1f1f1;
  color: #333;
  font-size: 28rpx;
}

.usage-head {
  color: #555;
  font-weight: 700;
}

.usage-head text:nth-child(n + 2),
.usage-row text:nth-child(n + 2) {
  color: $color-primary-dark;
  text-align: right;
}

.record-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 26rpx 28rpx;
  border-bottom: 1rpx solid #f1f1f1;
}

.record-row__title,
.record-row__time {
  display: block;
}

.record-row__title {
  color: #333;
  font-size: 29rpx;
  font-weight: 700;
}

.record-row__time {
  margin-top: 8rpx;
  color: #999;
  font-size: 24rpx;
}

.record-row__amount {
  color: $color-primary-dark;
  font-size: 29rpx;
  font-weight: 700;
}

.record-row__amount--out {
  color: #555;
}

.record-more {
  padding: 30rpx 0;
  color: $color-primary-dark;
  font-size: 26rpx;
  font-weight: 700;
  text-align: center;
}

.state {
  padding: 88rpx 20rpx;
  color: #999;
  font-size: 26rpx;
  text-align: center;
}

.recharge-modal {
  position: fixed;
  z-index: 1000;
  top: 0;
  right: 0;
  bottom: 0;
  left: 0;
  display: flex;
  align-items: flex-end;
  background: rgba(0, 0, 0, 0.42);
}

.recharge-modal__panel {
  width: 100%;
  min-height: 640rpx;
  max-height: 88vh;
  padding: 30rpx 28rpx calc(44rpx + env(safe-area-inset-bottom));
  border-radius: 34rpx 34rpx 0 0;
  background: #fff;
  box-sizing: border-box;
}

.recharge-modal__head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 24rpx;
}

.recharge-modal__title {
  color: #222;
  font-size: 32rpx;
  font-weight: 800;
}

.recharge-modal__close {
  width: 56rpx;
  height: 56rpx;
  color: #777;
  font-size: 44rpx;
  line-height: 50rpx;
  text-align: center;
}

.product-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 18rpx;
}

.recharge-empty {
  display: flex;
  min-height: 340rpx;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #999;
  font-size: 28rpx;
  text-align: center;
}

.recharge-empty__retry {
  width: 176rpx;
  height: 60rpx;
  margin-top: 28rpx;
  border-radius: 999rpx;
  color: #fff;
  background: $color-primary;
  font-size: 25rpx;
  font-weight: 700;
  line-height: 60rpx;
}

.recharge-empty__retry::after {
  border: 0;
}

.product-card {
  position: relative;
  min-height: 184rpx;
  padding: 30rpx 18rpx 24rpx;
  border: 2rpx solid #f0f0f0;
  border-radius: 8rpx;
  background: #fafafa;
  box-sizing: border-box;
  text-align: center;
}

.product-card--active {
  border-color: $color-primary;
  background: $color-primary-light;
}

.product-card--recommended {
  border-color: $color-primary-dark;
}

.product-card__badge {
  position: absolute;
  top: 0;
  right: 0;
  min-width: 112rpx;
  height: 38rpx;
  padding: 0 12rpx;
  border-radius: 0 6rpx 0 8rpx;
  color: #fff;
  background: $color-primary;
  font-size: 21rpx;
  font-weight: 700;
  line-height: 38rpx;
  box-sizing: border-box;
}

.product-card__label {
  display: block;
  color: #888;
  font-size: 23rpx;
  line-height: 1.2;
}

.product-card__label--ppd {
  margin-top: 18rpx;
}

.product-card__amount,
.product-card__price {
  display: block;
  color: #222;
  font-size: 31rpx;
  font-weight: 800;
  line-height: 1.25;
}

.product-card__price {
  margin-top: 8rpx;
  color: $color-primary-dark;
}

.pay-btn {
  margin-top: 30rpx;
  height: 86rpx;
  border-radius: 999rpx;
  color: #fff;
  background: $color-primary;
  font-size: 29rpx;
  font-weight: 800;
  line-height: 86rpx;
}

.pay-btn[disabled] {
  opacity: .45;
}

.unsupported {
  margin-top: 24rpx;
  padding: 24rpx;
  border-radius: 8rpx;
  color: #999;
  background: #f6f6f6;
  font-size: 25rpx;
  text-align: center;
}
</style>
