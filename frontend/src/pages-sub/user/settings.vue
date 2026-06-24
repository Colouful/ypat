<template>
  <view class="settings-page">
    <view class="settings-section">
      <view class="section-title">账号</view>
      <view class="settings-card">
        <view class="settings-item" @tap="handlePhone">
          <text class="settings-item__label">手机号</text>
          <view class="settings-item__right">
            <text class="settings-item__value">{{ maskedPhone }}</text>
            <text class="settings-item__arrow">›</text>
          </view>
        </view>
        <view class="settings-item" @tap="goRealname">
          <text class="settings-item__label">实名认证</text>
          <view class="settings-item__right">
            <text class="settings-item__value" :class="{ 'settings-item__value--verified': isVerified }">
              {{ verifyStatus }}
            </text>
            <text class="settings-item__arrow">›</text>
          </view>
        </view>
      </view>
    </view>

    <view class="settings-section">
      <view class="section-title">通用</view>
      <view class="settings-card">
        <view class="settings-item" @tap="handleClearCache">
          <text class="settings-item__label">清除缓存</text>
          <view class="settings-item__right">
            <text class="settings-item__value">{{ cacheSize }}</text>
            <text class="settings-item__arrow">›</text>
          </view>
        </view>
        <view class="settings-item" @tap="goAbout">
          <text class="settings-item__label">关于我们</text>
          <view class="settings-item__right">
            <text class="settings-item__arrow">›</text>
          </view>
        </view>
        <view class="settings-item" @tap="goFeedback">
          <text class="settings-item__label">意见反馈</text>
          <view class="settings-item__right">
            <text class="settings-item__arrow">›</text>
          </view>
        </view>
      </view>
    </view>

    <view class="logout-section">
      <view class="logout-btn" @tap="handleLogout">
        <text class="logout-btn__text">退出登录</text>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()

const cacheSize = ref('0 KB')

const maskedPhone = computed(() => {
  const mobile = userStore.userInfo?.mobile || ''
  if (mobile.length >= 11) {
    return mobile.slice(0, 3) + '****' + mobile.slice(7)
  }
  return mobile || '未绑定'
})

const isVerified = computed(() => {
  return userStore.userInfo?.realnameflag === 1
})

const verifyStatus = computed(() => {
  return isVerified.value ? '已认证' : '未认证'
})

onShow(() => {
  getStorageSize()
})

function getStorageSize() {
  uni.getStorageInfo({
    success(res) {
      const sizeKB = res.currentSize
      if (sizeKB >= 1024) {
        cacheSize.value = (sizeKB / 1024).toFixed(2) + ' MB'
      } else {
        cacheSize.value = sizeKB + ' KB'
      }
    },
  })
}

function handlePhone() {
  uni.showToast({ title: '暂不支持修改手机号', icon: 'none' })
}

function goRealname() {
  uni.navigateTo({ url: '/pages-sub/user/realname' })
}

function goAbout() {
  uni.navigateTo({ url: '/pages-sub/user/about' })
}

function goFeedback() {
  uni.navigateTo({ url: '/pages-sub/user/feedback' })
}

function handleClearCache() {
  uni.showModal({
    title: '提示',
    content: '确定要清除缓存吗？',
    success(res) {
      if (res.confirm) {
        const token = uni.getStorageSync('ypat_token')
        const userInfo = uni.getStorageSync('ypat_user_info')
        uni.clearStorageSync()
        if (token) {
          uni.setStorageSync('ypat_token', token)
        }
        if (userInfo) {
          uni.setStorageSync('ypat_user_info', userInfo)
        }
        getStorageSize()
        uni.showToast({ title: '缓存已清除', icon: 'success' })
      }
    },
  })
}

function handleLogout() {
  uni.showModal({
    title: '提示',
    content: '确定要退出登录吗？',
    success(res) {
      if (res.confirm) {
        userStore.logout()
      }
    },
  })
}
</script>

<style lang="scss">
@import '@/styles/tokens.scss';

.settings-page {
  min-height: 100vh;
  background-color: $color-bg-page;
  padding: $spacing-lg;
  padding-bottom: calc(env(safe-area-inset-bottom) + 40rpx);
}

.settings-section {
  margin-bottom: $spacing-lg;
}

.section-title {
  font-size: $font-size-sm;
  color: $color-text-secondary;
  margin-bottom: $spacing-sm;
  padding-left: $spacing-xs;
}

.settings-card {
  background-color: $color-bg-card;
  border-radius: $radius-md;
  overflow: hidden;
  box-shadow: $shadow-sm;
}

.settings-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: $spacing-lg;
  border-bottom: 1rpx solid $color-border;

  &:last-child {
    border-bottom: none;
  }

  &:active {
    background-color: $color-bg-page;
  }

  &__label {
    font-size: $font-size-base;
    color: $color-text-primary;
    font-weight: $font-weight-regular;
  }

  &__right {
    display: flex;
    align-items: center;
    gap: $spacing-sm;
  }

  &__value {
    font-size: $font-size-sm;
    color: $color-text-secondary;

    &--verified {
      color: $color-primary;
    }
  }

  &__arrow {
    font-size: $font-size-lg;
    color: $color-text-helper;
  }
}

.logout-section {
  margin-top: $spacing-xxl;
}

.logout-btn {
  background-color: $color-bg-card;
  border-radius: $radius-md;
  padding: $spacing-lg;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: $shadow-sm;

  &:active {
    opacity: 0.8;
  }

  &__text {
    font-size: $font-size-base;
    color: $color-accent-red;
    font-weight: $font-weight-medium;
  }
}
</style>
