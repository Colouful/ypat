<!--
 * @Author: shawn
 * @LastEditTime: 2020-03-08 09:01:45
 -->
<template>
  <view class="login-box fadeIn">
    <image
      class="logo"
      src="/static/images/login/logo.png"
    ></image>

    <button
      class="login-btn"
      open-type="getUserInfo"
      @getuserinfo="login"
      withCredentials="true"
    >
      <!-- #ifdef MP-WEIXIN -->
      授权微信用户信息
      <!-- #endif -->
      <!-- #ifdef MP-BAIDU -->
      授权百度用户信息
      <!-- #endif -->

    </button>
    <view class="bottom">
      登录代表您已同意
      <text
        class="active"
        @tap="navTo('/pages/login/agreement/index')"
      >爱去拍用户协议、隐私协议</text>
    </view>
    <!--加载框 start-->
    <tui-loading :visible="visible"></tui-loading>
    <!--加载框 end-->
  </view>
</template>

<script src="./index.js">

</script>

<style lang="scss" scoped>
@import "./index.scss";
</style>
