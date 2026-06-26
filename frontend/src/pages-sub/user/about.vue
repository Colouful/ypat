<template>
  <view class="about-page">
    <view class="about-header">
      <view class="about-logo">
        <image class="about-logo__image" src="/static/logo.png" mode="aspectFit" />
      </view>
      <text class="about-name">爱去拍</text>
      <text class="about-version">v1.0.0</text>
    </view>

    <view class="about-links">
      <view class="link-item" @tap="goAgreement">
        <text class="link-item__label">用户协议</text>
        <KeepIcon name="chevron-right" :size="34" color="#B3B8BE" />
      </view>
      <view class="link-item" @tap="goPrivacy">
        <text class="link-item__label">隐私政策</text>
        <KeepIcon name="chevron-right" :size="34" color="#B3B8BE" />
      </view>
    </view>

    <view class="about-footer">
      <text class="about-footer__text">© 2024 爱去拍 All Rights Reserved</text>
    </view>
  </view>
</template>

<script setup lang="ts">
import KeepIcon from '@/components/business/KeepIcon.vue'

function goAgreement() {
  uni.navigateTo({ url: '/pages-sub/content/agreement' })
}

function goPrivacy() {
  uni.navigateTo({ url: '/pages-sub/content/privacy' })
}
</script>

<style lang="scss">
@import '@/styles/tokens.scss';

.about-page {
  min-height: 100vh;
  background-color: $color-bg-page;
  display: flex;
  flex-direction: column;
  align-items: center;
  padding-bottom: calc(env(safe-area-inset-bottom) + 40rpx);
}

.about-header {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding-top: 120rpx;
  padding-bottom: 80rpx;
}

.about-logo {
  width: 160rpx;
  height: 160rpx;
  border-radius: $radius-xl;
  overflow: hidden;
  margin-bottom: $spacing-lg;
  box-shadow: $shadow-md;
  background-color: $color-bg-card;
  display: flex;
  align-items: center;
  justify-content: center;

  &__image {
    width: 160rpx;
    height: 160rpx;
  }
}

.about-name {
  font-size: $font-size-xxl;
  font-weight: $font-weight-bold;
  color: $color-text-primary;
  margin-bottom: $spacing-xs;
}

.about-version {
  font-size: $font-size-sm;
  color: $color-text-secondary;
}

.about-links {
  width: 100%;
  padding: 0 $spacing-lg;
  margin-top: $spacing-xl;

  background-color: $color-bg-card;
  margin-left: $spacing-lg;
  margin-right: $spacing-lg;
  width: calc(100% - #{$spacing-lg} * 2);
  border-radius: $radius-md;
  overflow: hidden;
  box-shadow: $shadow-sm;
}

.link-item {
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
  }

  &__arrow {
    font-size: $font-size-lg;
    color: $color-text-helper;
  }
}

.about-footer {
  margin-top: auto;
  padding: $spacing-xxl $spacing-lg;

  &__text {
    font-size: $font-size-xs;
    color: $color-text-helper;
  }
}
</style>
