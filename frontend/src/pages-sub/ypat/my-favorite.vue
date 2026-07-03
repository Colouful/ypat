<template>
  <view class="page">
    <KeepPageNav title="我的收藏" />
    <view v-if="loading && !items.length" class="state">加载中...</view>
    <view v-else-if="!items.length" class="state">暂无收藏</view>
    <view v-else>
      <view v-for="item in items" :key="item.id" class="card" @tap="openDetail(item.id)">
        <image class="cover" :src="item.pics?.[0] || '/static/default-cover.png'" mode="aspectFill" />
        <view class="body">
          <view class="header">
            <text class="name">{{ item.userQo?.nickname || '匿名用户' }}</text>
            <text class="time">{{ item.timeStr || formatTime(item.pubdate) }}</text>
          </view>
          <text class="desc">{{ item.describ }}</text>
          <view class="meta">
            <text>{{ item.city || '' }}</text>
            <text>{{ getChargeLabel(item.chargeway, item.chargeamt) }}</text>
          </view>
        </view>
      </view>
    </view>
    <view v-if="loadingMore" class="footer">加载中...</view>
    <view v-else-if="items.length && !hasMore" class="footer">没有更多了</view>
  </view>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { onPullDownRefresh, onReachBottom, onShow } from '@dcloudio/uni-app'
import { useUserStore } from '@/stores/user'
import * as ypatApi from '@/api/modules/ypat'
import { CHARGE_WAY_LABELS } from '@/constants/enums'
import type { YpatInfo } from '@/api/types'

const userStore = useUserStore()
const items = ref<YpatInfo[]>([])
const loading = ref(false)
const loadingMore = ref(false)
const hasMore = ref(true)
const page = ref(0)
const size = 10

async function load(refresh = false): Promise<void> {
  const userid = userStore.userInfo?.id
  if (!userid || loading.value || loadingMore.value || (!refresh && !hasMore.value)) return
  if (refresh) {
    page.value = 0
    hasMore.value = true
    loading.value = true
  } else {
    loadingMore.value = true
  }
  try {
    const result = await ypatApi.getMyFavoriteList({ userid, page: page.value, size })
    const content = result.data?.content || []
    items.value = refresh ? content : items.value.concat(content)
    const current = result.data?.number ?? page.value
    hasMore.value = current + 1 < (result.data?.totalPages || 0)
    page.value = current + 1
  } catch (error) {
    uni.showToast({ title: error instanceof Error ? error.message : '收藏加载失败', icon: 'none' })
  } finally {
    loading.value = false
    loadingMore.value = false
  }
}

function getChargeLabel(chargeway: string, chargeamt?: number): string {
  if (Number(chargeamt || 0) > 0) return `¥${chargeamt}`
  return CHARGE_WAY_LABELS[chargeway] || '费用协商'
}

function formatTime(value?: string): string {
  if (!value) return ''
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return value
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`
}

function openDetail(id: number): void { uni.navigateTo({ url: `/pages-sub/ypat/detail?id=${id}` }) }

onShow(() => load(true))
onPullDownRefresh(async () => { await load(true); uni.stopPullDownRefresh() })
onReachBottom(() => load())
</script>

<style scoped lang="scss">

.page { min-height: 100vh; padding: 28rpx; background: $color-bg-page; }
.state, .footer { padding: 120rpx 20rpx; color: $color-text-helper; text-align: center; }
.footer { padding: 28rpx; }
.card { display: flex; margin-bottom: 22rpx; padding: 22rpx; overflow: hidden; border-radius: $radius-keep-card; background: $color-bg-card; box-shadow: $shadow-keep-card; }
.cover { width: 204rpx; height: 220rpx; border-radius: 24rpx; background: $color-bg-chip; }
.body { flex: 1; min-width: 0; padding: 6rpx 0 6rpx 22rpx; }
.header, .meta { display: flex; justify-content: space-between; }
.name { color: $color-text-primary; font-weight: 900; }
.time, .meta { color: $color-text-helper; font-size: 23rpx; font-weight: 700; }
.desc { display: block; margin: 16rpx 0; color: $color-text-secondary; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
</style>
