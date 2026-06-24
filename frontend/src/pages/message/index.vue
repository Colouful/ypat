<template>
  <view class="message-page">
    <view v-if="!isLoggedIn" class="login-prompt">
      <text class="login-prompt__text">登录后查看消息</text>
      <button class="login-prompt__btn" @tap="goLogin">
        <text class="login-prompt__btn-text">去登录</text>
      </button>
    </view>

    <view v-else class="message-content">
      <view class="message-tabs">
        <view class="message-tab" :class="{ 'message-tab--active': activeTab === 'received' }" @tap="activeTab = 'received'">
          <text class="message-tab__text">收到的</text>
          <view v-if="recUnread > 0" class="message-tab__badge">
            <text class="message-tab__badge-text">{{ recUnread }}</text>
          </view>
        </view>
        <view class="message-tab" :class="{ 'message-tab--active': activeTab === 'sent' }" @tap="activeTab = 'sent'">
          <text class="message-tab__text">发出的</text>
          <view v-if="sendUnread > 0" class="message-tab__badge">
            <text class="message-tab__badge-text">{{ sendUnread }}</text>
          </view>
        </view>
      </view>

      <view v-if="loading && list.length === 0" class="loading-state">
        <text class="loading-state__text">加载中...</text>
      </view>

      <view v-else-if="list.length === 0" class="empty-state">
        <text class="empty-state__icon">📭</text>
        <text class="empty-state__text">暂无消息</text>
      </view>

      <view v-else class="message-list">
        <view v-for="item in list" :key="item.id" class="message-item" @tap="goDetail(item)">
          <image class="message-item__avatar" :src="item.imgpath || '/static/tab/mine.png'" mode="aspectFill" />
          <view class="message-item__body">
            <view class="message-item__header">
              <text class="message-item__name">{{ item.nickname || '用户' }}</text>
              <text class="message-item__time">{{ item.timeStr }}</text>
            </view>
            <text class="message-item__content">{{ item.content || '发来一条约拍申请' }}</text>
          </view>
          <view v-if="item.messviewflag !== '1'" class="message-item__dot" />
        </view>
      </view>

      <view v-if="!hasMore && list.length > 0" class="load-end">
        <text class="load-end__text">没有更多了</text>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { onPullDownRefresh, onReachBottom, onShow } from '@dcloudio/uni-app'
import { useUserStore } from '@/stores/user'
import * as ypatApi from '@/api/modules/ypat'
import * as messageApi from '@/api/modules/message'
import type { MessInfo } from '@/api/types'

const userStore = useUserStore()

const isLoggedIn = computed(() => userStore.isLoggedIn)
const userId = computed(() => userStore.userInfo?.id)

const activeTab = ref<'received' | 'sent'>('received')
const loading = ref(false)
const list = ref<MessInfo[]>([])
const page = ref(0)
const hasMore = ref(true)
const recUnread = ref(0)
const sendUnread = ref(0)

async function loadList(refresh = false) {
  if (!userId.value) return
  if (loading.value) return
  if (refresh) {
    page.value = 0
    hasMore.value = true
  }
  if (!hasMore.value) return

  loading.value = true
  try {
    const params = {
      page: page.value,
      size: 20,
      ...(activeTab.value === 'received' ? { recperid: userId.value } : { sendperid: userId.value }),
    }
    const res = activeTab.value === 'received'
      ? await ypatApi.getMyReceivedList(params)
      : await ypatApi.getMySentList(params)

    if (res.success) {
      const content = res.data.content || []
      if (refresh) {
        list.value = content
      } else {
        list.value.push(...content)
      }
      hasMore.value = content.length >= 20
      page.value++
    }
  } catch (_) {
  } finally {
    loading.value = false
  }
}

async function loadUnreadCounts() {
  if (!userId.value) return
  try {
    const [recRes, sendRes] = await Promise.all([
      messageApi.getRecUnreadCount('', userId.value),
      messageApi.getSendUnreadCount('', userId.value),
    ])
    if (recRes.success) recUnread.value = recRes.data || 0
    if (sendRes.success) sendUnread.value = sendRes.data || 0
  } catch (_) {}
}

function goDetail(item: MessInfo) {
  uni.navigateTo({ url: `/pages-sub/content/message-detail?id=${item.id}` })
}

function goLogin() {
  uni.navigateTo({ url: '/pages/login/index' })
}

watch(activeTab, () => {
  list.value = []
  loadList(true)
})

onShow(() => {
  if (isLoggedIn.value) {
    loadList(true)
    loadUnreadCounts()
  }
})

onPullDownRefresh(() => {
  loadList(true).finally(() => uni.stopPullDownRefresh())
})

onReachBottom(() => {
  loadList()
})
</script>

<style lang="scss">
@import '@/styles/tokens.scss';

.message-page {
  min-height: 100vh;
  background-color: $color-bg-page;
  padding-bottom: calc(env(safe-area-inset-bottom) + 100rpx);
}

.login-prompt {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding-top: 300rpx;

  &__text {
    font-size: $font-size-lg;
    color: $color-text-secondary;
    margin-bottom: $spacing-lg;
  }

  &__btn {
    background-color: $color-primary;
    border-radius: $radius-round;
    padding: $spacing-sm $spacing-xxl;
    border: none;

    &::after { border: none; }
  }

  &__btn-text {
    color: #fff;
    font-size: $font-size-base;
  }
}

.message-tabs {
  display: flex;
  background-color: #fff;
  padding: $spacing-sm $spacing-md;
  border-bottom: 1rpx solid $color-border;
}

.message-tab {
  position: relative;
  padding: $spacing-sm $spacing-lg;

  &--active &__text {
    color: $color-primary;
    font-weight: $font-weight-semibold;
  }

  &__text {
    font-size: $font-size-base;
    color: $color-text-secondary;
  }

  &__badge {
    position: absolute;
    top: 4rpx;
    right: 4rpx;
    min-width: 32rpx;
    height: 32rpx;
    background-color: $color-accent-red;
    border-radius: $radius-round;
    display: flex;
    align-items: center;
    justify-content: center;
    padding: 0 6rpx;
  }

  &__badge-text {
    font-size: 20rpx;
    color: #fff;
  }
}

.message-list {
  padding: $spacing-sm 0;
}

.message-item {
  display: flex;
  align-items: center;
  padding: $spacing-md $spacing-lg;
  background-color: #fff;
  margin-bottom: 2rpx;

  &__avatar {
    width: 88rpx;
    height: 88rpx;
    border-radius: 50%;
    margin-right: $spacing-md;
    flex-shrink: 0;
  }

  &__body {
    flex: 1;
    overflow: hidden;
  }

  &__header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-bottom: $spacing-xs;
  }

  &__name {
    font-size: $font-size-base;
    font-weight: $font-weight-medium;
    color: $color-text-primary;
  }

  &__time {
    font-size: $font-size-xs;
    color: $color-text-helper;
  }

  &__content {
    font-size: $font-size-sm;
    color: $color-text-secondary;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  &__dot {
    width: 16rpx;
    height: 16rpx;
    border-radius: 50%;
    background-color: $color-accent-red;
    margin-left: $spacing-sm;
    flex-shrink: 0;
  }
}

.loading-state, .empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding-top: 200rpx;

  &__icon {
    font-size: 80rpx;
    margin-bottom: $spacing-md;
  }

  &__text {
    font-size: $font-size-base;
    color: $color-text-secondary;
  }
}

.load-end {
  display: flex;
  justify-content: center;
  padding: $spacing-lg;

  &__text {
    font-size: $font-size-sm;
    color: $color-text-helper;
  }
}
</style>
