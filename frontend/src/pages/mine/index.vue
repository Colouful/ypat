<template>
  <view class="mine-page">
    <view class="mine-header" :style="{ paddingTop: statusBarHeight + 'px' }">
      <view v-if="isLoggedIn" class="user-info">
        <image class="user-info__avatar" :src="userInfo?.imgpath || '/static/tab/mine.png'" mode="aspectFill" @tap="goProfile" />
        <view class="user-info__detail">
          <text class="user-info__name">{{ userInfo?.nickname || '未设置昵称' }}</text>
          <view class="user-info__tags">
            <text v-if="userInfo?.profess" class="user-info__tag">{{ professLabel }}</text>
            <text v-if="userInfo?.city" class="user-info__tag">{{ userInfo.city }}</text>
            <text v-if="isVerified" class="user-info__tag user-info__tag--verified">已实名</text>
          </view>
        </view>
        <view class="user-info__settings" @tap="goSettings">
          <text class="user-info__settings-icon">⚙</text>
        </view>
      </view>
      <view v-else class="user-login-prompt" @tap="goLogin">
        <image class="user-login-prompt__avatar" src="/static/tab/mine.png" mode="aspectFill" />
        <text class="user-login-prompt__text">点击登录</text>
      </view>
    </view>

    <view v-if="isLoggedIn" class="stats-section">
      <view class="stats-item" @tap="goPublish">
        <text class="stats-item__count">{{ userInfo?.pubtimes || 0 }}</text>
        <text class="stats-item__label">发布</text>
      </view>
      <view class="stats-item" @tap="goApply">
        <text class="stats-item__count">{{ userInfo?.rectimes || 0 }}</text>
        <text class="stats-item__label">申请</text>
      </view>
      <view class="stats-item" @tap="goFavorite">
        <text class="stats-item__count">{{ userInfo?.coltimes || 0 }}</text>
        <text class="stats-item__label">收藏</text>
      </view>
      <view class="stats-item" @tap="goWallet">
        <text class="stats-item__count stats-item__count--ppd">{{ userInfo?.ppd || 0 }}</text>
        <text class="stats-item__label">拍拍豆</text>
      </view>
    </view>

    <view class="menu-section">
      <view class="menu-group">
        <view class="menu-item" @tap="goPublish">
          <text class="menu-item__icon">📷</text>
          <text class="menu-item__text">我的发布</text>
          <text class="menu-item__arrow">›</text>
        </view>
        <view class="menu-item" @tap="goApply">
          <text class="menu-item__icon">📝</text>
          <text class="menu-item__text">我的申请</text>
          <text class="menu-item__arrow">›</text>
        </view>
        <view class="menu-item" @tap="goFavorite">
          <text class="menu-item__icon">❤</text>
          <text class="menu-item__text">我的收藏</text>
          <text class="menu-item__arrow">›</text>
        </view>
      </view>

      <view class="menu-group">
        <view class="menu-item" @tap="goWallet">
          <text class="menu-item__icon">💰</text>
          <text class="menu-item__text">我的拍拍豆</text>
          <text class="menu-item__arrow">›</text>
        </view>
        <view class="menu-item" @tap="goRealname">
          <text class="menu-item__icon">🪪</text>
          <text class="menu-item__text">实名认证</text>
          <text v-if="isVerified" class="menu-item__badge menu-item__badge--success">已认证</text>
          <text v-else class="menu-item__badge">未认证</text>
          <text class="menu-item__arrow">›</text>
        </view>
      </view>

      <view class="menu-group">
        <view class="menu-item" @tap="goEditInfo">
          <text class="menu-item__icon">👤</text>
          <text class="menu-item__text">编辑资料</text>
          <text class="menu-item__arrow">›</text>
        </view>
        <view class="menu-item" @tap="goFeedback">
          <text class="menu-item__icon">💬</text>
          <text class="menu-item__text">意见反馈</text>
          <text class="menu-item__arrow">›</text>
        </view>
        <view class="menu-item" @tap="goAbout">
          <text class="menu-item__icon">ℹ</text>
          <text class="menu-item__text">关于我们</text>
          <text class="menu-item__arrow">›</text>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { useUserStore } from '@/stores/user'
import { useAppStore } from '@/stores/app'
import { PROFESS_LABELS } from '@/constants/enums'

const userStore = useUserStore()
const appStore = useAppStore()

const statusBarHeight = computed(() => appStore.statusBarHeight)
const isLoggedIn = computed(() => userStore.isLoggedIn)
const userInfo = computed(() => userStore.userInfo)
const isVerified = computed(() => userInfo.value?.realnameflag === '1' || userInfo.value?.status === '2')
const professLabel = computed(() => {
  const code = userInfo.value?.profess
  return code ? PROFESS_LABELS[code] || '' : ''
})

onShow(() => {
  if (isLoggedIn.value) {
    userStore.updateUserInfo()
  }
})

function goLogin() { uni.navigateTo({ url: '/pages/login/index' }) }
function goProfile() { uni.navigateTo({ url: '/pages-sub/user/profile' }) }
function goSettings() { uni.navigateTo({ url: '/pages-sub/user/settings' }) }
function goPublish() { checkLogin(() => uni.navigateTo({ url: '/pages-sub/ypat/my-publish' })) }
function goApply() { checkLogin(() => uni.navigateTo({ url: '/pages-sub/ypat/my-apply' })) }
function goFavorite() { checkLogin(() => uni.navigateTo({ url: '/pages-sub/ypat/my-favorite' })) }
function goWallet() { checkLogin(() => uni.navigateTo({ url: '/pages-sub/user/wallet' })) }
function goRealname() { checkLogin(() => uni.navigateTo({ url: '/pages-sub/user/realname' })) }
function goEditInfo() { checkLogin(() => uni.navigateTo({ url: '/pages-sub/user/edit-info' })) }
function goFeedback() { checkLogin(() => uni.navigateTo({ url: '/pages-sub/user/feedback' })) }
function goAbout() { uni.navigateTo({ url: '/pages-sub/user/about' }) }

function checkLogin(callback: () => void) {
  if (!isLoggedIn.value) {
    uni.navigateTo({ url: '/pages/login/index' })
    return
  }
  callback()
}
</script>

<style lang="scss">
@import '@/styles/tokens.scss';

.mine-page {
  min-height: 100vh;
  background-color: $color-bg-page;
  padding-bottom: calc(env(safe-area-inset-bottom) + 100rpx);
}

.mine-header {
  background: linear-gradient(135deg, $color-primary, $color-primary-dark);
  padding: $spacing-xl $spacing-lg;
  padding-bottom: $spacing-xxl;
}

.user-info {
  display: flex;
  align-items: center;

  &__avatar {
    width: 120rpx;
    height: 120rpx;
    border-radius: 50%;
    border: 4rpx solid rgba(255, 255, 255, 0.3);
    margin-right: $spacing-md;
  }

  &__detail {
    flex: 1;
  }

  &__name {
    font-size: $font-size-xl;
    font-weight: $font-weight-bold;
    color: #fff;
    margin-bottom: $spacing-xs;
  }

  &__tags {
    display: flex;
    gap: $spacing-xs;
  }

  &__tag {
    font-size: $font-size-xs;
    color: rgba(255, 255, 255, 0.8);
    background-color: rgba(255, 255, 255, 0.15);
    padding: 4rpx 12rpx;
    border-radius: $radius-sm;

    &--verified {
      background-color: rgba(255, 255, 255, 0.25);
    }
  }

  &__settings {
    padding: $spacing-sm;
  }

  &__settings-icon {
    font-size: $font-size-xl;
    color: #fff;
  }
}

.user-login-prompt {
  display: flex;
  align-items: center;
  padding: $spacing-lg 0;

  &__avatar {
    width: 120rpx;
    height: 120rpx;
    border-radius: 50%;
    border: 4rpx solid rgba(255, 255, 255, 0.3);
    margin-right: $spacing-md;
  }

  &__text {
    font-size: $font-size-xl;
    font-weight: $font-weight-medium;
    color: #fff;
  }
}

.stats-section {
  display: flex;
  background-color: #fff;
  margin: -30rpx $spacing-md 0;
  border-radius: $radius-md;
  padding: $spacing-lg 0;
  box-shadow: $shadow-sm;
  position: relative;
  z-index: 1;
}

.stats-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;

  &__count {
    font-size: $font-size-xxl;
    font-weight: $font-weight-bold;
    color: $color-text-primary;
    margin-bottom: 4rpx;

    &--ppd {
      color: $color-accent-orange;
    }
  }

  &__label {
    font-size: $font-size-xs;
    color: $color-text-secondary;
  }
}

.menu-section {
  padding: $spacing-md;
}

.menu-group {
  background-color: #fff;
  border-radius: $radius-md;
  margin-bottom: $spacing-md;
  overflow: hidden;
}

.menu-item {
  display: flex;
  align-items: center;
  padding: $spacing-md $spacing-lg;
  border-bottom: 1rpx solid $color-border;

  &:last-child {
    border-bottom: none;
  }

  &__icon {
    font-size: $font-size-lg;
    margin-right: $spacing-md;
    width: 48rpx;
    text-align: center;
  }

  &__text {
    flex: 1;
    font-size: $font-size-base;
    color: $color-text-primary;
  }

  &__badge {
    font-size: $font-size-xs;
    color: $color-text-helper;
    margin-right: $spacing-sm;

    &--success {
      color: $color-primary;
    }
  }

  &__arrow {
    font-size: $font-size-lg;
    color: $color-text-helper;
  }
}
</style>
