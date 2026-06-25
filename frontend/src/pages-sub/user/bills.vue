<template>
  <view class="page">
    <view v-if="items.length" class="list">
      <view v-for="item in items" :key="item.id" class="item">
        <view><text class="item__title">{{ item.typeTxt || orderTypeText(item.type) }}</text><text class="item__time">{{ formatTime(item.credate) }}</text><text class="item__number">订单号：{{ item.out_trade_no }}</text></view>
        <view class="item__right"><text class="item__amount">¥{{ formatPrice(item.total_fee) }}</text><text class="item__status">{{ statusText(item) }}</text></view>
      </view>
    </view>
    <view v-else-if="!loading" class="empty">暂无账单记录</view>
    <view v-if="loading" class="loading">加载中...</view>
    <view v-if="items.length && !hasMore" class="footer">没有更多了</view>
  </view>
</template>
<script setup lang="ts">
import { ref } from 'vue'
import { onLoad, onPullDownRefresh, onReachBottom } from '@dcloudio/uni-app'
import { useUserStore } from '@/stores/user'
import * as paymentApi from '@/api/modules/payment'
import type { Bill } from '@/api/types'
const userStore = useUserStore()
const items = ref<Bill[]>([])
const loading = ref(false)
const hasMore = ref(true)
const page = ref(0)
const size = 20
async function load(refresh = false): Promise<void> {
  if (loading.value || (!refresh && !hasMore.value)) return
  if (!userStore.userInfo?.id) { uni.showToast({ title: '请先登录', icon: 'none' }); return }
  if (refresh) { page.value = 0; hasMore.value = true }
  loading.value = true
  try {
    const result = await paymentApi.getBillList({ page: page.value, size, userid: userStore.userInfo.id })
    const content = result.data?.content || []
    items.value = refresh ? content : [...items.value, ...content]
    const current = result.data?.number ?? page.value
    hasMore.value = current + 1 < (result.data?.totalPages || 0)
    page.value = current + 1
  } catch (error) { uni.showToast({ title: error instanceof Error ? error.message : '账单加载失败', icon: 'none' }) }
  finally { loading.value = false }
}
function formatPrice(value: number): string { return (Number(value || 0) / 100).toFixed(2) }
function formatTime(value?: string): string {
  if (!value) return ''
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return value
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`
}
function orderTypeText(type: string): string { return ({ '0': '拍拍豆充值', '1': '实名认证', '2': '保证金' } as Record<string, string>)[type] || '订单' }
function statusText(item: Bill): string {
  if (item.result_code === 'SUCCESS' || item.status === '1') return '支付成功'
  if (item.result_code === 'FAIL' || item.err_code) return '支付失败'
  return '待支付'
}
onLoad(() => load(true))
onPullDownRefresh(async () => { await load(true); uni.stopPullDownRefresh() })
onReachBottom(() => load())
</script>
<style scoped lang="scss">
.page { min-height: 100vh; padding: 24rpx; background: #f7f8fa; }
.item { display: flex; justify-content: space-between; margin-bottom: 18rpx; padding: 28rpx; border-radius: 22rpx; background: #fff; }
.item__title { display: block; color: #1d2433; font-size: 29rpx; font-weight: 600; }
.item__time, .item__number, .item__status { display: block; margin-top: 10rpx; color: #8b94a3; font-size: 23rpx; }
.item__right { text-align: right; }
.item__amount { color: #f26a3d; font-size: 30rpx; font-weight: 600; }
.empty, .loading, .footer { padding: 180rpx 20rpx; color: #8b94a3; text-align: center; }
.footer { padding: 30rpx; }
</style>
