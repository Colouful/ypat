<!--
 * @Author: shawn
 * @LastEditTime : 2019-12-21 09:02:42
 -->
<template>
  <view class="container fadeIn">
    <view class="top-box">
      <image
        class="portrait"
        src="/static/images/mine/logo-mine.png"
      ></image>
      <view>爱去拍-个人与摄影师的约拍平台</view>
    </view>
    <tui-list-view class="tui-list-view">
      <tui-list-cell
        @tap="makePhoneCall(item)"
        class="tui-list"
        v-for="(item,index) in cellList"
        :key="index"
        :arrow="false"
      >
        <text class="tui-list-cell-name">{{item.title}}</text>
        <view class="tui-right">{{item.desc}}</view>
      </tui-list-cell>
    </tui-list-view>
    <view v-if="token" class="logout" @tap="openActionSheet">退出登录</view>
    <tui-actionsheet
      :show="showActionSheet"
      :tips="tips"
      :item-list="itemList"
      :mask-closable="maskClosable"
      :color="color"
      :size="size"
      :is-cancel="isCancel"
      @click="itemClick"
      @cancel="closeActionSheet"
    ></tui-actionsheet>
  </view>
</template>

<script src="./index.js">

</script>

<style lang="scss" scoped>
@import "./index.scss";
</style>
