<!--
 * @Author: shawn
 * @LastEditTime : 2020-01-05 11:19:11
 -->
<template>
  <view class="img-box index-page fadeIn">
    <view class="invite_bg-box">
      <image
        mode="widthFix"
        class="invite_bg"
        src="/static/images/mine/invite_bg.png"
      ></image>
      <image
        mode="widthFix"
        class="Rectangle"
        src="/static/images/mine/Rectangle.png"
      ></image>
    </view>
    <view class="text-box">

      <view class="title-desc">
        <view class="title-box">
          <image
            class="logo"
            src="/static/images/mine/logo.png"
          ></image>
          爱去拍
        </view>
        爱去拍 — 个人与摄影师的专属约拍平台
      </view>
      <view
        class="btn-box"
        @tap="goHome"
      >
        <image
          mode="widthFix"
          class="btn_bg"
          src="/static/images/mine/btn_bg.png"
        ></image>
        <view class="btn_text">马上体验</view>
      </view>
    </view>
  </view>
</template>

<script src="./index.js">

</script>

<style lang="scss">
page {
  background-color: #2a2d2d;
}
.index-page {
  position: fixed;
  height: 100%;
  background-color: #2c2d2d;
}
@import "./index.scss";
</style>
