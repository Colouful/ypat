<template>
  <view class="message-page">
    <view class="message-scroll" :style="{ paddingTop: statusBarHeight + 24 + 'px' }">
      <view class="message-top">
        <view>
          <text class="message-title">消息</text>
          <text class="message-subtitle">约拍申请与合作动态</text>
        </view>
        <view class="message-top__icon" @tap="goDiscover">
          <KeepIcon name="compass" :size="42" />
        </view>
      </view>

      <view v-if="!userStore.isLoggedIn" class="state-card">
        <KeepState
          type="login"
          title="登录后查看消息"
          description="收到的约拍、申请记录和系统通知都会集中在这里。"
          button-text="去登录"
          @action="goLogin"
        />
      </view>

      <template v-else>
        <view class="tabs">
          <view :class="['tab', { active: tab === 'received' }]" @tap="switchTab('received')">收到的约拍</view>
          <view :class="['tab', { active: tab === 'sent' }]" @tap="switchTab('sent')">申请的约拍</view>
        </view>

        <KeepState v-if="loading && items.length === 0" type="loading" title="加载中..." />
        <KeepState v-else-if="items.length === 0" type="empty" title="暂无消息" description="去首页看看新的约拍机会。" button-text="去广场" @action="goHome" />
      <view v-else>
          <view v-for="item in items" :key="item.id" class="message-card" @tap="openDetail(item.id)">
            <image class="message-card__avatar" :src="item.userQo?.imgpath || item.userQo?.avatarurl || '/static/default-avatar.png'" mode="aspectFill" />
            <view class="message-card__body">
              <view class="message-card__head">
                <text class="message-card__name">{{ item.userQo?.nickname || '用户' }}</text>
                <text class="message-card__tag">{{ tab === 'received' ? '收到' : '申请' }}</text>
              </view>
              <text class="message-card__content">{{ item.describ || '约拍动态' }}</text>
              <view class="message-card__meta">
                <text>{{ item.city || '同城' }}</text>
                <text>{{ item.timeStr || item.pubdate || '刚刚' }}</text>
              </view>
            </view>
            <KeepIcon name="chevron-right" :size="34" color="#B3B8BE" />
          </view>
        </view>
        <view v-if="items.length && !hasMore" class="load-end">没有更多了</view>
      </template>
    </view>

    <KeepTabBar active="message" :unread-count="userStore.unreadCount" />
  </view>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { onPullDownRefresh, onReachBottom, onShow } from '@dcloudio/uni-app'
import { useAppStore } from '@/stores/app'
import { useUserStore } from '@/stores/user'
import * as ypatApi from '@/api/modules/ypat'
import KeepIcon from '@/components/business/KeepIcon.vue'
import KeepState from '@/components/business/KeepState.vue'
import KeepTabBar from '@/components/business/KeepTabBar.vue'
import type { YpatInfo } from '@/api/types'

const appStore = useAppStore()
const userStore = useUserStore()
const statusBarHeight = computed(() => appStore.statusBarHeight)
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

function goHome(): void {
  uni.switchTab({ url: '/pages/home/index' })
}

function goDiscover(): void {
  uni.navigateTo({ url: '/pages/discover/index' })
}

function goLogin(): void {
  uni.navigateTo({ url: '/pages/login/index' })
}

onShow(() => {
  try { uni.hideTabBar({ animation: false }) } catch(e) {}
  if (userStore.isLoggedIn) {
    userStore.refreshUnreadCount()
    load(true)
  }
})
onPullDownRefresh(async () => {
  await load(true)
  uni.stopPullDownRefresh()
})
onReachBottom(() => load())
</script>

<style scoped lang="scss">

.message-page {
  min-height: 100vh;
  background: $color-bg-page;
}

.message-scroll {
  box-sizing: border-box;
  min-height: 100vh;
  padding-right: 32rpx;
  padding-bottom: calc(172rpx + env(safe-area-inset-bottom));
  padding-left: 32rpx;
}

.message-top {
  @include flex-between;
  margin-bottom: 30rpx;
}

.message-title,
.message-subtitle {
  display: block;
}

.message-title {
  color: $color-text-primary;
  font-size: 48rpx;
  font-weight: 900;
}

.message-subtitle {
  margin-top: 8rpx;
  color: $color-text-secondary;
  font-size: 26rpx;
  font-weight: 700;
}

.message-top__icon {
  @include flex-center;
  width: 84rpx;
  height: 84rpx;
  border-radius: 50%;
  background: #fff;
  box-shadow: $shadow-keep-card;
}

.state-card {
  border-radius: $radius-keep-card;
  background: #fff;
  box-shadow: $shadow-keep-card;
}

.tabs {
  display: flex;
  gap: 18rpx;
  margin-bottom: 26rpx;
}

.tab {
  @include keep-chip(false);
  background: #fff;
}

.tab.active {
  @include keep-chip(true);
}

.message-card {
  display: flex;
  align-items: center;
  gap: 22rpx;
  margin-bottom: 22rpx;
  padding: 26rpx;
  border-radius: $radius-keep-card;
  background: #fff;
  box-shadow: $shadow-keep-card;
}

.message-card__avatar {
  width: 96rpx;
  height: 96rpx;
  flex: none;
  border-radius: 50%;
  background: $color-bg-chip;
}

.message-card__body {
  min-width: 0;
  flex: 1;
}

.message-card__head,
.message-card__meta {
  @include flex-between;
  gap: 18rpx;
}

.message-card__name {
  color: $color-text-primary;
  font-size: 30rpx;
  font-weight: 900;
}

.message-card__tag {
  flex: none;
  padding: 4rpx 14rpx;
  border-radius: $radius-round;
  color: $color-primary-dark;
  background: $color-primary-light;
  font-size: 22rpx;
  font-weight: 900;
}

.message-card__content {
  @include ellipsis(1);
  display: block;
  margin: 12rpx 0;
  color: $color-text-secondary;
  font-size: 26rpx;
  font-weight: 700;
}

.message-card__meta {
  color: $color-text-helper;
  font-size: 23rpx;
  font-weight: 800;
}

.load-end {
  padding: 28rpx;
  color: $color-text-helper;
  text-align: center;
}
</style>
