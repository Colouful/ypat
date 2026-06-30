<template>
  <view class="page">
    <KeepPageNav title="会员订单" />

    <view v-if="orders.length === 0 && !loading" class="empty">
      <KeepState type="empty" title="暂无会员订单" description="开通会员后，订单会显示在这里" />
    </view>

    <view v-else class="list">
      <view v-for="o in orders" :key="o.id" class="row">
        <view class="row__head">
          <text class="row__name">{{ planLabel(o) }}</text>
          <text class="row__status" :class="`row__status--${o.status}`">{{ o.statusTxt || statusLabel(o.status) }}</text>
        </view>
        <view class="row__meta">
          <text class="row__amount">¥{{ formatYuan(o.priceFen) }} · {{ o.durationDays }} 天</text>
          <text class="row__time">{{ formatTime(o.paidAt || o.credate) }}</text>
        </view>
        <text class="row__order-no">订单号：{{ o.outTradeNo }}</text>
      </view>

      <view v-if="loading" class="footer">加载中…</view>
      <view v-else-if="!hasMore" class="footer">没有更多了</view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { onLoad, onPullDownRefresh, onReachBottom } from '@dcloudio/uni-app'
import KeepPageNav from '@/components/business/KeepPageNav.vue'
import KeepState from '@/components/business/KeepState.vue'
import * as memberApi from '@/api/modules/member'
import type { MemberOrder } from '@/api/types'

const orders = ref<MemberOrder[]>([])
const loading = ref(false)
const hasMore = ref(true)
const page = ref(0)
const size = 10

function formatYuan(fen: number): string {
  if (!fen || fen <= 0) return '0.00'
  return (fen / 100).toFixed(2)
}

function formatTime(value?: string): string {
  if (!value) return ''
  return value.replace('T', ' ').slice(0, 16)
}

function statusLabel(status: string): string {
  switch (status) {
    case '0': return '待支付'
    case '1': return '已支付'
    case '2': return '已取消'
    case '3': return '已退款'
    default: return status
  }
}

function planLabel(o: MemberOrder): string {
  // planCode 是后端枚举（暂未定义中文表），fallback 用 duration 描述
  return `会员套餐 ${o.planCode || ''}`.trim()
}

async function load(reset = false): Promise<void> {
  if (loading.value) return
  if (!reset && !hasMore.value) return
  loading.value = true
  if (reset) {
    page.value = 0
    hasMore.value = true
  }
  try {
    const result = await memberApi.getMemberOrders({ page: page.value, size })
    const content = result.data?.content ?? []
    orders.value = reset ? content : orders.value.concat(content)
    const total = result.data?.totalElements ?? 0
    hasMore.value = orders.value.length < total
    page.value += 1
  } catch {
    hasMore.value = false
  } finally {
    loading.value = false
  }
}

onLoad(() => {
  void load(true)
})

onPullDownRefresh(async () => {
  await load(true)
  uni.stopPullDownRefresh()
})

onReachBottom(() => {
  void load()
})
</script>

<style scoped lang="scss">
.page { min-height: 100vh; padding: 24rpx 28rpx calc(60rpx + env(safe-area-inset-bottom)); background: $color-bg-page; }
.empty { margin-top: 80rpx; }

.list { padding: 6rpx 0; }
.row { margin-bottom: 16rpx; padding: 28rpx; border-radius: $radius-keep-card; background: #fff; box-shadow: $shadow-keep-card; }
.row__head { display: flex; align-items: center; justify-content: space-between; }
.row__name { color: $color-text-primary; font-size: 30rpx; font-weight: 900; }
.row__status { padding: 4rpx 16rpx; border-radius: $radius-round; color: $color-text-secondary; background: $color-bg-chip; font-size: 22rpx; font-weight: 800; }
.row__status--1 { color: $color-primary-dark; background: $color-primary-light; }
.row__status--2, .row__status--3 { color: $color-accent-red; }
.row__meta { display: flex; justify-content: space-between; margin-top: 14rpx; }
.row__amount { color: $color-text-primary; font-size: 26rpx; font-weight: 800; }
.row__time { color: $color-text-helper; font-size: 22rpx; font-weight: 700; }
.row__order-no { display: block; margin-top: 12rpx; color: $color-text-helper; font-size: 22rpx; font-family: monospace; }

.footer { padding: 28rpx 0; color: $color-text-helper; font-size: 24rpx; text-align: center; }
</style>