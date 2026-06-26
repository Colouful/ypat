<template>
  <view class="page">
    <view class="balance-card">
      <text class="label">拍拍豆余额</text>
      <text class="amount">{{ userStore.userInfo?.ppd || 0 }}</text>
      <button class="recharge" @tap="goRecharge">立即充值</button>
    </view>

    <view class="links">
      <view @tap="goRecords">消费记录</view>
      <view @tap="goBills">账单明细</view>
    </view>

    <view class="recent">
      <text class="section-title">最近记录</text>
      <view v-if="loading" class="state">加载中...</view>
      <view v-else-if="records.length === 0" class="state">暂无记录</view>
      <view v-else>
        <view v-for="item in records" :key="item.id" class="record-item">
          <view>
            <text class="record-title">{{ getRecordTypeLabel(item.type) }}</text>
            <text class="record-time">{{ formatDate(item.credate) }}</text>
          </view>
          <text class="record-value">{{ signedAmount(item) }}</text>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { useUserStore } from '@/stores/user'
import * as paymentApi from '@/api/modules/payment'
import { RECORD_TYPE_LABELS, RecordType } from '@/constants/enums'
import type { RecordInfo } from '@/api/types'

const userStore = useUserStore()
const loading = ref(true)
const records = ref<RecordInfo[]>([])
const incomeTypes = new Set<string>([RecordType.TOPUP, RecordType.INVITE, RecordType.SYSTEM])

async function fetchRecentRecords(): Promise<void> {
  loading.value = true
  try {
    const userid = userStore.userInfo?.id
    if (!userid) return
    const result = await paymentApi.getRecordList({ page: 0, size: 5, userid })
    records.value = result.data?.content || []
  } catch (error) {
    uni.showToast({ title: error instanceof Error ? error.message : '记录加载失败', icon: 'none' })
  } finally {
    loading.value = false
  }
}

function getRecordTypeLabel(type: string): string {
  return RECORD_TYPE_LABELS[type] || '其他'
}

function signedAmount(item: RecordInfo): string {
  const prefix = incomeTypes.has(item.type) ? '+' : '-'
  return `${prefix}${Math.abs(Number(item.ppd || 0))} PPD`
}

function formatDate(value: string): string {
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return value
  return `${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`
}

function goRecharge(): void { uni.navigateTo({ url: '/pages-sub/user/recharge' }) }
function goRecords(): void { uni.navigateTo({ url: '/pages-sub/user/records' }) }
function goBills(): void { uni.navigateTo({ url: '/pages-sub/user/bills' }) }

onLoad(fetchRecentRecords)
</script>

<style scoped lang="scss">
@import '@/styles/tokens.scss';

.page { min-height: 100vh; padding: 28rpx; background: $color-bg-page; }
.balance-card { padding: 36rpx; border-radius: $radius-keep-card; color: #fff; background: linear-gradient(135deg, $color-primary, #69d89e); }
.label, .amount { display: block; }
.amount { margin: 12rpx 0 28rpx; font-size: 64rpx; font-weight: 700; }
.recharge { color: $color-primary-dark; background: $color-bg-card; box-shadow: $shadow-keep-card; border-radius: 40rpx; }
.recharge::after { border: 0; }
.links { display: grid; grid-template-columns: repeat(2, 1fr); gap: 18rpx; margin: 24rpx 0; }
.links view { padding: 28rpx; border-radius: $radius-keep-card; background: $color-bg-card; box-shadow: $shadow-keep-card; text-align: center; }
.recent { padding: 28rpx; border-radius: $radius-keep-card; background: $color-bg-card; box-shadow: $shadow-keep-card; }
.section-title { font-size: 30rpx; font-weight: 600; }
.state { padding: 80rpx 0; color: $color-text-helper; text-align: center; }
.record-item { display: flex; justify-content: space-between; padding: 22rpx 0; border-top: 1rpx solid $color-border; }
.record-title, .record-time { display: block; }
.record-time { margin-top: 8rpx; color: $color-text-helper; font-size: 23rpx; }
.record-value { font-weight: 600; }
</style>
