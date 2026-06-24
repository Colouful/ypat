<!--
 * @Author: shawn
 * @LastEditTime: 2020-04-13 21:32:48
 -->
<template>
  <view>
    <qrcode-alert
      :visiable="qrcodeAlertShow"
      @callback="qrcodeAlertShow=false"
      title="关注微信公众号发起约拍"
    ></qrcode-alert>

    <tui-tabbar
      selectedColor="#2C2D2D"
      :tabBar="tabBar"
      :hump="true"
      :current="current"
      @click="tabbarSwitch"
    ></tui-tabbar>
  </view>
</template>

<script src="./index.js">

</script>

<style lang="scss" scoped>
@import "./index.scss";
</style>
