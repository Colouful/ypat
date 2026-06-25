<template>
  <view class="page">
    <view v-if="loading && !items.length" class="state">加载中...</view>
    <view v-else-if="!items.length" class="state">暂无报名记录</view>
    <view v-else>
      <view v-for="item in items" :key="item.id" class="card" @tap="openDetail(item.id)">
        <image class="cover" :src="item.pics?.[0] || '/static/default-cover.png'" mode="aspectFill" />
        <view class="body">
          <text class="title">{{ item.targetTxt || '约拍报名' }}</text>
          <text class="desc">{{ item.describ }}</text>
          <text class="meta">{{ item.city || '' }} {{ item.timeStr || formatTime(item.pubdate) }}</text>
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
    const result = await ypatApi.getMySentList({ userid, page: page.value, size })
    const content = result.data?.content || []
    items.value = refresh ? content : items.value.concat(content)
    const current = result.data?.number ?? page.value
    hasMore.value = current + 1 < (result.data?.totalPages || 0)
    page.value = current + 1
  } catch (error) {
    uni.showToast({ title: error instanceof Error ? error.message : '列表加载失败', icon: 'none' })
  } finally {
    loading.value = false
    loadingMore.value = false
  }
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
.page { min-height: 100vh; padding: 24rpx; background: #f7f8fa; }
.state, .footer { padding: 160rpx 20rpx; color: #929aa7; text-align: center; }
.footer { padding: 28rpx; }
.card { display: flex; margin-bottom: 18rpx; overflow: hidden; border-radius: 22rpx; background: #fff; }
.cover { width: 210rpx; height: 190rpx; background: #edf0f4; }
.body { flex: 1; min-width: 0; padding: 22rpx; }
.title, .desc, .meta { display: block; }
.title { color: #1d2433; font-size: 29rpx; font-weight: 600; }
.desc { margin: 12rpx 0; color: #596270; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.meta { color: #929aa7; font-size: 23rpx; }
</style>
