<template>
  <view v-if="visible" class="splash-overlay">
    <view class="skip" @tap="close">跳过 {{seconds}}s</view>
    <view class="brand">
      <image class="logo" src="/static/images/login/logo.png" mode="widthFix" />
      <view class="title">爱去拍</view>
      <view class="subtitle">安全约拍，发现更好的创作伙伴</view>
    </view>
    <view class="bottom-text">{{seconds}}s 后进入</view>
  </view>
</template>

<script src="./index.js"></script>

<style lang="scss" scoped>
@import "./index.scss";
</style>
