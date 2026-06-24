<!--
 * @Author: shawn
 * @LastEditTime : 2020-01-18 11:43:18
 -->
<template>
  <view class="container">

    <view class="fixed-box">
      <view class="tab-box">
        <view
          @tap="changeTab(1)"
          :class="['item', currentTab==='1'?'active':'']"
        >等待审核</view>
        <view
          @tap="changeTab(2)"
          :class="['item', currentTab==='2'?'active':'']"
        >审核通过</view>
        <view
          @tap="changeTab(3)"
          :class="['item', currentTab==='3'?'active':'']"
        >审核拒绝</view>
      </view>
    </view>
    <view class="home-container fadeIn">
      <info-list
        ref="mychild"
        @fatherMethod="fatherMethod"
        :type="currentTab+''"
      ></info-list>
    </view>
  </view>
</template>

<script src="./index.js">

</script>

<style lang="scss" scoped>
@import "./index.scss";
</style>
