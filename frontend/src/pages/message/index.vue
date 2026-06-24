<template>
  <view class="page">
    <view v-if="!userStore.isLoggedIn" class="state">
      <text>登录后查看消息</text>
      <button @tap="goLogin">去登录</button>
    </view>
    <template v-else>
      <view class="tabs">
        <view :class="['tab', { active: tab === 'received' }]" @tap="switchTab('received')">收到的</view>
        <view :class="['tab', { active: tab === 'sent' }]" @tap="switchTab('sent')">发出的</view>
      </view>
      <view v-if="loading && items.length === 0" class="state">加载中...</view>
      <view v-else-if="items.length === 0" class="state">暂无消息</view>
      <view v-else>
        <view v-for="item in items" :key="item.id" class="item" @tap="openDetail(item.id)">
          <image class="avatar" :src="item.userQo?.imgpath || '/static/tab/mine.png'" mode="aspectFill" />
          <view class="body">
            <text class="name">{{ item.userQo?.nickname || '用户' }}</text>
            <text class="content">{{ item.describ || '约拍动态' }}</text>
            <text class="meta">{{ item.city || '' }} {{ item.timeStr || item.pubdate || '' }}</text>
          </view>
        </view>
      </view>
    </template>
  </view>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { onPullDownRefresh, onReachBottom, onShow } from '@dcloudio/uni-app'
import { useUserStore } from '@/stores/user'
import * as ypatApi from '@/api/modules/ypat'
import type { YpatInfo } from '@/api/types'

const userStore = useUserStore()
const tab = ref<'received' | 'sent'>('received')
const loading = ref(false)
const items = ref<YpatInfo[]>([])
const page = ref(0)
const hasMore = ref(true)

async function load(refresh = false): Promise<void> {
  const userid = userStore.userInfo?.id
  if (!userid || loading.value || (!refresh && !hasMore.value)) return
  if (refresh) {
    page.value = 0
    hasMore.value = true
  }
  loading.value = true
  try {
    const params = { userid, page: page.value, size: 20 }
    const result = tab.value === 'received'
      ? await ypatApi.getMyReceivedList(params)
      : await ypatApi.getMySentList(params)
    const content = result.data?.content || []
    items.value = refresh ? content : items.value.concat(content)
    const current = result.data?.number ?? page.value
    hasMore.value = current + 1 < (result.data?.totalPages || 0)
    page.value = current + 1
  } catch (error) {
    uni.showToast({ title: error instanceof Error ? error.message : '消息加载失败', icon: 'none' })
  } finally {
    loading.value = false
  }
}

function switchTab(value: 'received' | 'sent'): void {
  tab.value = value
  items.value = []
  load(true)
}

function openDetail(id: number): void {
  uni.navigateTo({ url: `/pages-sub/ypat/detail?id=${id}` })
}

function goLogin(): void {
  uni.navigateTo({ url: '/pages/login/index' })
}

onShow(() => load(true))
onPullDownRefresh(async () => {
  await load(true)
  uni.stopPullDownRefresh()
})
onReachBottom(() => load())
</script>

<style scoped>
.page { min-height: 100vh; background: #f7f8fa; }
.tabs { display: flex; padding: 16rpx 24rpx; background: #fff; }
.tab { padding: 16rpx 28rpx; color: #7c8593; }
.active { color: #23c268; font-weight: 600; }
.state { padding: 220rpx 30rpx; color: #929aa7; text-align: center; }
.item { display: flex; padding: 24rpx 28rpx; margin-bottom: 2rpx; background: #fff; }
.avatar { width: 84rpx; height: 84rpx; border-radius: 50%; }
.body { flex: 1; margin-left: 20rpx; }
.name, .content, .meta { display: block; }
.name { font-weight: 600; }
.content { margin: 8rpx 0; color: #596270; }
.meta { color: #9aa2ae; font-size: 22rpx; }
</style>
