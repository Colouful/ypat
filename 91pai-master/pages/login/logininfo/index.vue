<!--
 * @Author: shawn
 * @LastEditTime: 2020-03-08 08:59:45
 -->
<template>
  <view class="login-box fadeIn">
    <image
      class="logo"
      src="/static/images/login/logo.png"
    ></image>
    <!-- #ifdef MP-WEIXIN -->
    <view class="desc">请完成微信授权以继续使用</view>
    <!-- #endif -->
    <!-- #ifdef MP-BAIDU -->
    <view class="desc">请完成百度授权以继续使用</view>
    <!-- #endif -->
    <button
      class="login-btn"
      open-type="getPhoneNumber"
      @getphonenumber="getPhoneNumber"
      withCredentials="true"
    >
      <!-- #ifdef MP-WEIXIN -->
      微信用户一键登录
      <!-- #endif -->
      <!-- #ifdef MP-BAIDU -->
      百度用户一键登录
      <!-- #endif -->
    </button>
  </view>
</template>

<script src="./index.js">

</script>

<style lang="scss" scoped>
@import "./index.scss";
</style>
