<template>
  <view class="message-detail-page">
    <view v-if="loading" class="loading-state">
      <view class="loading-spinner" />
      <text class="loading-text">加载中...</text>
    </view>

    <view v-else-if="error" class="error-state">
      <text class="error-state__text">加载失败</text>
      <text class="error-state__sub">{{ error }}</text>
      <view class="error-state__btn" @tap="loadDetail">
        <text class="error-state__btn-text">重新加载</text>
      </view>
    </view>

    <view v-else-if="message" class="message-content">
      <!-- Sender info -->
      <view class="sender-section">
        <image
          class="sender-section__avatar"
          :src="message.imgpath || '/static/images/default-avatar.png'"
          mode="aspectFill"
        />
        <view class="sender-section__info">
          <text class="sender-section__name">{{ message.nickname || '匿名用户' }}</text>
          <text class="sender-section__time">{{ message.timeStr || formatTime(message.credate) }}</text>
        </view>
      </view>

      <!-- Message content -->
      <view class="message-body">
        <text class="message-body__text">{{ message.content }}</text>
      </view>

      <!-- Related ypat -->
      <view v-if="message.ypatid" class="related-ypat" @tap="goYpatDetail">
        <view class="related-ypat__header">
          <text class="related-ypat__label">相关约拍</text>
        </view>
        <view class="related-ypat__card">
          <text class="related-ypat__title">相关约拍 #{{ message.ypatid }}</text>
          <view class="related-ypat__arrow">
            <text class="related-ypat__arrow-icon">></text>
          </view>
        </view>
      </view>

      <!-- Contact info revealed -->
      <view v-if="contactRevealed && contactInfo" class="contact-section">
        <view class="contact-section__header">
          <text class="contact-section__title">联系方式</text>
        </view>
        <view class="contact-section__body">
          <view class="contact-item">
            <text class="contact-item__label">{{ contactInfo.label }}</text>
            <text class="contact-item__value" selectable>{{ contactInfo.value }}</text>
          </view>
          <view class="contact-item__tip">
            <text class="contact-item__tip-text">长按可复制联系方式</text>
          </view>
        </view>
      </view>
    </view>

    <!-- Bottom action -->
    <view v-if="message && !contactRevealed && !loading" class="bottom-bar">
      <view class="bottom-bar__content">
        <view class="bottom-bar__btn" @tap="handleViewContact">
          <text class="bottom-bar__btn-text">查看联系方式</text>
          <text class="bottom-bar__btn-cost">消耗1PPD</text>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { useUserStore } from '@/stores/user'
import * as messageApi from '@/api/modules/message'
import * as userApi from '@/api/modules/user'
import type { MessInfo, LinkWay } from '@/api/types'

const userStore = useUserStore()

const loading = ref(false)
const error = ref('')
const message = ref<MessInfo | null>(null)
const contactRevealed = ref(false)
const contactInfo = ref<LinkWay | null>(null)
const messageId = ref(0)

function formatTime(time: string): string {
  if (!time) return ''
  const date = new Date(time)
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  const hour = String(date.getHours()).padStart(2, '0')
  const minute = String(date.getMinutes()).padStart(2, '0')
  return `${year}-${month}-${day} ${hour}:${minute}`
}

async function loadDetail() {
  if (!messageId.value) return
  loading.value = true
  error.value = ''
  try {
    const userId = userStore.userInfo?.id
    if (!userId) {
      error.value = '请先登录'
      return
    }
    const res = await messageApi.getMessageDetail(messageId.value, Number(userId))
    if (res.success) {
      message.value = res.data
    } else {
      error.value = res.message || '获取消息详情失败'
    }
  } catch {
    error.value = '网络异常，请稍后重试'
  } finally {
    loading.value = false
  }
}

function goYpatDetail() {
  if (message.value?.ypatid) {
    uni.navigateTo({
      url: `/pages-sub/ypat/detail?id=${message.value.ypatid}`,
    })
  }
}

function handleViewContact() {
  if (!userStore.isLoggedIn) {
    uni.showToast({ title: '请先登录', icon: 'none' })
    return
  }

  const ppd = Number(userStore.userInfo?.ppd || 0)
  if (ppd < 1) {
    uni.showModal({
      title: '余额不足',
      content: '您的PPD余额不足，是否前往充值？',
      confirmText: '去充值',
      confirmColor: '#23C268',
      success: (res) => {
        if (res.confirm) {
          uni.navigateTo({ url: '/pages-sub/user/recharge' })
        }
      },
    })
    return
  }

  uni.showModal({
    title: '提示',
    content: '查看联系方式将消耗1PPD，是否继续？',
    confirmText: '确认查看',
    confirmColor: '#23C268',
    success: (res) => {
      if (res.confirm) {
        revealContact()
      }
    },
  })
}

async function revealContact() {
  if (!message.value || !userStore.userInfo) return

  uni.showLoading({ title: '加载中...' })
  try {
    const res = await userApi.getLinkWay(
      message.value.sendperid,
      Number(userStore.userInfo.id),
      message.value.id
    )
    if (res.success) {
      contactInfo.value = res.data
      contactRevealed.value = true
      const currentPpd = Number(userStore.userInfo.ppd || 0)
      userStore.updateUserInfo({ ppd: String(currentPpd - 1) } as any)
      uni.showToast({ title: '获取成功', icon: 'success' })
    } else {
      uni.showToast({ title: res.message || '获取失败', icon: 'none' })
    }
  } catch {
    uni.showToast({ title: '网络异常，请稍后重试', icon: 'none' })
  } finally {
    uni.hideLoading()
  }
}

onLoad((query) => {
  const id = Number(query?.id)
  if (id) {
    messageId.value = id
    loadDetail()
  } else {
    error.value = '消息ID无效'
  }
})
</script>

<style lang="scss">
@import '@/styles/tokens.scss';

.message-detail-page {
  min-height: 100vh;
  background-color: $color-bg-page;
  padding-bottom: calc(140rpx + env(safe-area-inset-bottom));
}

.loading-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 200rpx 0;
}

.loading-spinner {
  width: 64rpx;
  height: 64rpx;
  border: 4rpx solid $color-border;
  border-top-color: $color-primary;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
  margin-bottom: $spacing-md;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

.loading-text {
  font-size: $font-size-sm;
  color: $color-text-helper;
}

.error-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 200rpx $spacing-xl;

  &__text {
    font-size: $font-size-lg;
    color: $color-text-secondary;
    margin-bottom: $spacing-sm;
  }

  &__sub {
    font-size: $font-size-sm;
    color: $color-text-helper;
    margin-bottom: $spacing-xl;
  }

  &__btn {
    padding: $spacing-sm $spacing-xl;
    background-color: $color-primary;
    border-radius: $radius-round;
  }

  &__btn-text {
    font-size: $font-size-base;
    color: #fff;
  }
}

.message-content {
  padding: $spacing-md;
}

.sender-section {
  display: flex;
  align-items: center;
  background-color: $color-bg-card;
  border-radius: $radius-md;
  padding: $spacing-lg;
  margin-bottom: $spacing-md;

  &__avatar {
    width: 96rpx;
    height: 96rpx;
    border-radius: 50%;
    margin-right: $spacing-md;
  }

  &__info {
    flex: 1;
  }

  &__name {
    display: block;
    font-size: $font-size-lg;
    font-weight: $font-weight-medium;
    color: $color-text-primary;
    margin-bottom: $spacing-xs;
  }

  &__time {
    display: block;
    font-size: $font-size-sm;
    color: $color-text-helper;
  }
}

.message-body {
  background-color: $color-bg-card;
  border-radius: $radius-md;
  padding: $spacing-lg;
  margin-bottom: $spacing-md;

  &__text {
    font-size: $font-size-base;
    color: $color-text-primary;
    line-height: 1.8;
    white-space: pre-wrap;
    word-break: break-all;
  }
}

.related-ypat {
  background-color: $color-bg-card;
  border-radius: $radius-md;
  padding: $spacing-lg;
  margin-bottom: $spacing-md;

  &__header {
    margin-bottom: $spacing-sm;
  }

  &__label {
    font-size: $font-size-sm;
    color: $color-text-helper;
  }

  &__card {
    display: flex;
    align-items: center;
    justify-content: space-between;
    background-color: $color-bg-page;
    border-radius: $radius-sm;
    padding: $spacing-md;
  }

  &__title {
    flex: 1;
    font-size: $font-size-base;
    color: $color-primary;
    font-weight: $font-weight-medium;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  &__arrow {
    margin-left: $spacing-sm;
  }

  &__arrow-icon {
    font-size: $font-size-sm;
    color: $color-text-helper;
  }
}

.contact-section {
  background-color: $color-bg-card;
  border-radius: $radius-md;
  padding: $spacing-lg;
  margin-bottom: $spacing-md;
  border: 2rpx solid $color-primary-light;

  &__header {
    margin-bottom: $spacing-md;
  }

  &__title {
    font-size: $font-size-md;
    font-weight: $font-weight-semibold;
    color: $color-primary;
  }

  &__body {
    display: flex;
    flex-direction: column;
    gap: $spacing-sm;
  }
}

.contact-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: $spacing-sm 0;

  &__label {
    font-size: $font-size-base;
    color: $color-text-secondary;
  }

  &__value {
    font-size: $font-size-base;
    font-weight: $font-weight-medium;
    color: $color-text-primary;
  }

  &__tip {
    padding-top: $spacing-sm;
    border-top: 1rpx solid $color-divider;
  }

  &__tip-text {
    font-size: $font-size-xs;
    color: $color-text-helper;
  }
}

.bottom-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  background-color: $color-bg-card;
  box-shadow: 0 -2rpx 12rpx rgba(0, 0, 0, 0.06);
  padding: $spacing-md $spacing-xl;
  padding-bottom: calc($spacing-md + env(safe-area-inset-bottom));
  z-index: $z-index-navbar;

  &__content {
    display: flex;
    align-items: center;
  }

  &__btn {
    flex: 1;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    height: 96rpx;
    background-color: $color-primary;
    border-radius: $radius-lg;

    &:active {
      opacity: 0.85;
    }
  }

  &__btn-text {
    font-size: $font-size-lg;
    font-weight: $font-weight-semibold;
    color: #fff;
  }

  &__btn-cost {
    font-size: $font-size-xs;
    color: rgba(255, 255, 255, 0.8);
    margin-top: 4rpx;
  }
}
</style>
