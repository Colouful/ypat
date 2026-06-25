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

<style scoped lang="scss">
@import '@/styles/tokens.scss';

.page { min-height: 100vh; padding: 24rpx; background: $color-bg-page; }
.tabs { display: flex; gap: 18rpx; padding: 10rpx 0 26rpx; }
.tab { padding: 16rpx 30rpx; border-radius: 999rpx; color: $color-text-secondary; background: $color-bg-chip; font-weight: 800; }
.active { color: $color-primary-dark; border: 3rpx solid $color-primary; background: $color-primary-light; }
.state { margin-top: 28rpx; padding: 96rpx 30rpx; border-radius: 32rpx; color: $color-text-helper; background: #fff; text-align: center; }
.state button { margin-top: 24rpx; padding: 0 36rpx; border-radius: 999rpx; color: #fff; background: $color-primary; }
.item { display: flex; padding: 28rpx; margin-bottom: 22rpx; border-radius: 32rpx; background: #fff; box-shadow: 0 6rpx 24rpx rgba(20, 24, 31, .04); }
.avatar { width: 92rpx; height: 92rpx; border-radius: 50%; background: $color-bg-chip; }
.body { flex: 1; min-width: 0; margin-left: 22rpx; }
.name, .content, .meta { display: block; }
.name { color: $color-text-primary; font-size: 30rpx; font-weight: 800; }
.content { margin: 10rpx 0; color: #596270; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.meta { color: $color-text-helper; font-size: 23rpx; font-weight: 700; }
</style>
