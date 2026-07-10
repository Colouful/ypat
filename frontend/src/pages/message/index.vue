<template>
  <view class="message-page">
    <view class="message-scroll" :style="{ paddingTop: statusBarHeight + 24 + 'px' }">
      <view class="message-top">
        <view>
          <text class="message-title">消息</text>
          <text class="message-subtitle">约拍申请与合作动态</text>
        </view>
        <view class="message-top__icon" @tap="goHome">
          <KeepIcon name="home" :size="42" />
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
        <view class="notify-card">
          <view class="notify-card__icon">
            <KeepIcon name="bell" :size="36" color="#23C268" />
          </view>
          <view class="notify-card__body">
            <text class="notify-card__title">消息提醒</text>
            <text class="notify-card__desc">开启后可及时收到约拍申请、审核结果等微信通知。</text>
          </view>
          <button class="notify-card__btn" hover-class="none" @tap="enableSubscribe">开启</button>
        </view>

        <view class="message-stats">
          <view class="message-stats__item message-stats__item--received">
            <text class="message-stats__num">{{ receivedUnread }}</text>
            <text class="message-stats__label">收到未读</text>
          </view>
          <view class="message-stats__item message-stats__item--sent">
            <text class="message-stats__num">{{ sentUnread }}</text>
            <text class="message-stats__label">申请未读</text>
          </view>
        </view>

        <view class="tabs">
          <view :class="['tab', { active: tab === 'received' }]" @tap="switchTab('received')">收到的约拍<text v-if="receivedUnread" class="tab__badge">{{ receivedUnread > 99 ? '99+' : receivedUnread }}</text></view>
          <view :class="['tab', { active: tab === 'sent' }]" @tap="switchTab('sent')">申请的约拍<text v-if="sentUnread" class="tab__badge tab__badge--sent">{{ sentUnread > 99 ? '99+' : sentUnread }}</text></view>
        </view>

        <KeepState v-if="loading && items.length === 0" type="loading" title="加载中..." />
        <KeepState v-else-if="items.length === 0" type="empty" title="暂无消息" description="去首页看看新的约拍机会。" button-text="去广场" @action="goHome" />
        <view v-else>
          <view v-for="item in items" :key="item.id" class="message-card" @tap="openDetail(item)">
            <image class="message-card__avatar" :src="item.imgpath || '/static/default-avatar.png'" mode="aspectFill" />
            <view class="message-card__body">
              <view class="message-card__head">
                <text class="message-card__name">{{ item.nickname || '用户' }}</text>
                <text :class="['message-card__tag', tab === 'sent' ? 'message-card__tag--sent' : '']">{{ tab === 'received' ? '收到' : '申请' }}</text>
              </view>
              <text class="message-card__content">{{ item.content || '约拍动态' }}</text>
              <view class="message-card__meta">
                <text>{{ item.city || '同城' }}</text>
                <text>{{ item.timeStr || item.credate || '刚刚' }}</text>
              </view>
            </view>
            <KeepIcon name="chevron-right" :size="34" color="#B3B8BE" />
          </view>
        </view>
        <view v-if="items.length && !hasMore" class="load-end">没有更多了</view>
      </template>
    </view>

    <KeepTabBar active="message" />
  </view>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { onPullDownRefresh, onReachBottom, onShow } from '@dcloudio/uni-app'
import { useAppStore } from '@/stores/app'
import { useUserStore } from '@/stores/user'
import * as ypatApi from '@/api/modules/ypat'
import * as messageApi from '@/api/modules/message'
import KeepIcon from '@/components/business/KeepIcon.vue'
import KeepState from '@/components/business/KeepState.vue'
import KeepTabBar from '@/components/business/KeepTabBar.vue'
import { goRootTab } from '@/utils/tab-navigation'
import type { MessInfo } from '@/api/types'
import { resolveMessageNavigation } from '@/utils/message-navigation'
import {
  getSubscribeMessageToastTitle,
  preloadMessageSubscribeTemplates,
  requestMessageSubscribe,
} from '@/utils/subscribe-message'

const appStore = useAppStore()
const userStore = useUserStore()
const statusBarHeight = computed(() => appStore.statusBarHeight)
const tab = ref<'received' | 'sent'>('received')
const loading = ref(false)
const items = ref<MessInfo[]>([])
const page = ref(0)
const hasMore = ref(true)
const receivedUnread = ref(0)
const sentUnread = ref(0)

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

async function refreshUnreadBreakdown(): Promise<void> {
  const userid = userStore.userInfo?.id
  if (!userid) {
    receivedUnread.value = 0
    sentUnread.value = 0
    return
  }
  try {
    const [received, sent] = await Promise.all([
      messageApi.getRecUnreadCount('1', userid),
      messageApi.getSendUnreadCount('4', userid),
    ])
    receivedUnread.value = Number(received.data || 0)
    sentUnread.value = Number(sent.data || 0)
  } catch {
    receivedUnread.value = 0
    sentUnread.value = 0
  }
}

function switchTab(value: 'received' | 'sent'): void {
  tab.value = value
  items.value = []
  load(true)
}

async function openDetail(item: MessInfo): Promise<void> {
  const userid = userStore.userInfo?.id
  if (!userid) return
  const result = await resolveMessageNavigation({
    tab: tab.value,
    item,
    getMessageDetail: async (messageId) => (await messageApi.getMessageDetail(messageId, userid)).data,
  })
  if (result.type === 'navigate') {
    uni.navigateTo({ url: result.url })
  } else {
    uni.showToast({ title: result.message, icon: 'none' })
  }
}

async function enableSubscribe(): Promise<void> {
  const result = await requestMessageSubscribe('message')
  uni.showToast({
    title: getSubscribeMessageToastTitle(result),
    icon: 'none',
  })
}

function goHome(): void {
  goRootTab('/pages/home/index')
}

function goLogin(): void {
  uni.navigateTo({ url: '/pages/login/index' })
}

onShow(() => {
  if (userStore.isLoggedIn) {
    void preloadMessageSubscribeTemplates()
    userStore.refreshUnreadCount()
    refreshUnreadBreakdown()
    load(true)
  }
})
onPullDownRefresh(async () => {
  await Promise.all([load(true), userStore.refreshUnreadCount(), refreshUnreadBreakdown()])
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

.notify-card {
  display: flex;
  align-items: center;
  gap: 18rpx;
  margin-bottom: 20rpx;
  padding: 24rpx;
  border: 2rpx solid rgba(35, 194, 104, 0.16);
  border-radius: $radius-keep-card;
  background: #fff;
  box-shadow: $shadow-keep-card;
}

.notify-card__icon {
  @include flex-center;
  width: 72rpx;
  height: 72rpx;
  flex: none;
  border-radius: 24rpx;
  background: $color-primary-soft;
}

.notify-card__body {
  min-width: 0;
  flex: 1;
}

.notify-card__title,
.notify-card__desc {
  display: block;
}

.notify-card__title {
  color: $color-text-primary;
  font-size: 29rpx;
  font-weight: 900;
}

.notify-card__desc {
  margin-top: 6rpx;
  color: $color-text-secondary;
  font-size: 23rpx;
  font-weight: 700;
  line-height: 1.45;
}

.notify-card__btn {
  width: 112rpx;
  height: 58rpx;
  flex: none;
  border-radius: $radius-round;
  color: #fff;
  background: $color-primary;
  font-size: 24rpx;
  font-weight: 900;
  line-height: 58rpx;
}

.notify-card__btn::after {
  border: 0;
}

.message-stats {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 18rpx;
  margin-bottom: 22rpx;
}

.message-stats__item {
  padding: 22rpx 24rpx;
  border-radius: 24rpx;
  background: #fff;
  box-shadow: $shadow-keep-card;
}

.message-stats__item--received {
  border-left: 8rpx solid $color-primary;
}

.message-stats__item--sent {
  border-left: 8rpx solid #FF9F1C;
}

.message-stats__num,
.message-stats__label {
  display: block;
}

.message-stats__num {
  color: $color-text-primary;
  font-size: 38rpx;
  font-weight: 900;
  line-height: 1.15;
}

.message-stats__label {
  margin-top: 6rpx;
  color: $color-text-secondary;
  font-size: 23rpx;
  font-weight: 800;
}

.tabs {
  display: flex;
  gap: 18rpx;
  margin-bottom: 26rpx;
}

.tab {
  position: relative;
  @include keep-chip(false);
  background: #fff;
}

.tab.active {
  @include keep-chip(true);
}

.tab__badge {
  min-width: 28rpx;
  margin-left: 8rpx;
  padding: 0 8rpx;
  border-radius: $radius-round;
  color: #fff;
  background: $color-primary;
  font-size: 20rpx;
  font-weight: 900;
  line-height: 28rpx;
}

.tab__badge--sent {
  background: #FF9F1C;
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

.message-card__tag--sent {
  color: #B75A00;
  background: rgba(255, 159, 28, 0.16);
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
