<!--
 * @Author: shawn
 * @LastEditTime: 2020-03-26 08:08:31
 -->
<template>
  <view class="fadeIn">
    <view
      class="zx-tab"
      v-if="!type"
    >
      <view
        @tap="changeTab(0)"
        :class="['tab-item', currentTab==='0'?'active':'']"
      >
        <badge
          class="dot-msg-tab"
          type="danger"
          size="small"
          dot
          v-if="count_rec&&count_rec>0"
        ></badge>
        收到的约拍
      </view>
      <view
        @tap="changeTab(1)"
        :class="['tab-item', currentTab==='1'?'active':'']"
      >
        <badge
          v-if="count_send&&count_send>0"
          class="dot-msg-tab"
          type="danger"
          size="small"
          dot
        ></badge>
        申请的约拍
      </view>
    </view>
    <view :class="{'home-container':!type}">
      <message-list
        ref="mychild0"
        @fatherMethod="fatherMethod"
      ></message-list>

    </view>
    <view class="tui-safearea-bottom"></view>
  </view>
</template>

<script src="./index.js">

</script>

<style lang="scss" scoped>
@import "./index.scss";
</style>
