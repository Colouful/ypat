<!--
 * @Author: shawn
 * @LastEditTime: 2019-10-26 14:52:13
 -->
<template>
  <view
    class="tui-toast"
    :class="[visible?'tui-toast-show':'',content?'tui-toast-padding':'',icon?'':'tui-unicon-padding']"
    :style="{width:getWidth(icon,content)}"
  >
    <image
      :src="imgUrl"
      class="tui-toast-img"
      v-if="icon"
    ></image>
    <view
      class="tui-toast-text"
      :class="[icon?'':'tui-unicon']"
    >{{title}}</view>
    <view
      class="tui-toast-text tui-content-ptop"
      v-if="content && icon"
    >{{content}}</view>
  </view>
</template>
<script src="./index.js">

</script>

<style lang="scss" scoped>
@import "./index.scss";
</style>