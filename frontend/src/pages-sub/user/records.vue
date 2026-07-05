<template>
  <view class="page">
    <KeepPageNav title="收支记录" />
    <view v-if="items.length" class="list">
      <view v-for="item in items" :key="item.id" class="item">
        <view class="badge">{{ badge(item.type) }}</view>
        <view class="info">
          <text class="title">{{ item.typeTxt || label(item.type) }}</text>
          <text class="time">{{ formatTime(item.credate) }}</text>
        </view>
        <text class="amount">{{ signedAmount(item) }}</text>
      </view>
    </view>
    <view v-else-if="!loading" class="empty">暂无收支记录</view>
    <view v-if="loading" class="loading">加载中...</view>
    <view v-if="items.length && !hasMore" class="footer">没有更多了</view>
  </view>
</template>

<script setup lang="ts">
import KeepPageNav from '@/components/business/KeepPageNav.vue'
import { ref } from 'vue'
import { onLoad, onPullDownRefresh, onReachBottom } from '@dcloudio/uni-app'
import { useUserStore } from '@/stores/user'
import * as paymentApi from '@/api/modules/payment'
import { RECORD_TYPE_LABELS, RecordType } from '@/constants/enums'
import type { RecordInfo } from '@/api/types'

const userStore = useUserStore()
const items = ref<RecordInfo[]>([])
const loading = ref(false)
const hasMore = ref(true)
const page = ref(0)
const size = 20
const incomeTypes = new Set<string>([RecordType.TOPUP, RecordType.INVITE, RecordType.SYSTEM])

async function load(refresh = false): Promise<void> {
  if (loading.value || (!refresh && !hasMore.value)) return
  if (!userStore.userInfo?.id) {
    uni.showToast({ title: '请先登录', icon: 'none' })
    return
  }
  if (refresh) {
    page.value = 0
    hasMore.value = true
  }
  loading.value = true
  try {
    const result = await paymentApi.getRecordList({
      page: page.value,
      size,
      userid: userStore.userInfo.id,
    })
    const content = result.data?.content || []
    items.value = refresh ? content : [...items.value, ...content]
    const current = result.data?.number ?? page.value
    hasMore.value = current + 1 < (result.data?.totalPages || 0)
    page.value = current + 1
  } catch (error) {
    uni.showToast({ title: error instanceof Error ? error.message : '记录加载失败', icon: 'none' })
  } finally {
    loading.value = false
  }
}

function label(type: string): string {
  return RECORD_TYPE_LABELS[type] || '其他'
}

function badge(type: string): string {
  const map: Record<string, string> = {
    [RecordType.TOPUP]: '充',
    [RecordType.INVITE]: '邀',
    [RecordType.SYSTEM]: '赠',
    [RecordType.PUBLISH]: '发',
    [RecordType.APPLY]: '报',
    [RecordType.VIEW_CONTACT]: '查',
  }
  return map[type] || '其'
}

function signedAmount(item: RecordInfo): string {
  const prefix = incomeTypes.has(item.type) ? '+' : '-'
  return `${prefix}${Math.abs(Number(item.ppd || 0))} PPD`
}

function formatTime(value: string): string {
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return value
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`
}

onLoad(() => load(true))
onPullDownRefresh(async () => {
  await load(true)
  uni.stopPullDownRefresh()
})
onReachBottom(() => load())
</script>

<style scoped lang="scss">

.page { min-height: 100vh; padding: 24rpx; background: $color-bg-page; }
.item { display: flex; align-items: center; margin-bottom: 18rpx; padding: 26rpx; border-radius: $radius-keep-card; background: $color-bg-card; box-shadow: $shadow-keep-card; }
.badge { width: 72rpx; height: 72rpx; line-height: 72rpx; border-radius: 50%; color: $color-primary-dark; background: #C9F4D9; text-align: center; }
.info { flex: 1; margin-left: 20rpx; }
.title { display: block; color: $color-text-primary; font-size: 28rpx; font-weight: 600; }
.time { display: block; margin-top: 8rpx; color: $color-text-helper; font-size: 23rpx; }
.amount { color: #3d4654; font-size: 28rpx; font-weight: 600; }
.empty, .loading, .footer { padding: 180rpx 20rpx; color: $color-text-secondary; text-align: center; }
.footer { padding: 30rpx; }
</style>
