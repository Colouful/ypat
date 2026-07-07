<template>
  <view class="realname-intro-page">
    <view class="hero">
      <view class="cert-card">
        <view class="cert-icon">认证</view>
      </view>
      <view class="hero-title">开启身份认证</view>
      <view class="hero-subtitle">安全畅想更多服务</view>
    </view>

    <view class="benefit-grid">
      <view class="benefit-item" v-for="item in benefits" :key="item.title">
        <view class="benefit-icon">{{item.icon}}</view>
        <view class="benefit-title">{{item.title}}</view>
        <view class="benefit-desc">{{item.desc}}</view>
      </view>
    </view>

    <view class="bottom-safe"></view>
    <view class="footer">
      <button class="start-btn" @tap="start">开始实名</button>
    </view>
  </view>
</template>

<script src="./index.js"></script>

<style lang="scss" scoped>
@import "./index.scss";
</style>
