<!--
 * @Author: shawn
 * @LastEditTime: 2020-04-10 18:24:53
 -->
<template>
  <view class="head-box tui-skeleton-rect" @tap="navTo(`/pages/mine/homepage/index?userId=${item.userQo.id}`)">
    <view class="head-item">
      <image
        mode="aspectFill"
        v-if="item.userQo&&item.userQo.imgpath"
        class="head tui-skeleton-rect"
        :src="item.userQo&&item.userQo.imgpath"
      ></image>
      <view class="name-box">
        <view class="name tui-skeleton-rect">{{item.userQo&&item.userQo.nickname}}
          <image
            v-if="item.userQo&&item.userQo.gender&&(item.userQo.gender==='1'||item.userQo.gender==='2')"
            mode="widthFix"
            class="sex tui-skeleton-rect"
            :src="(item.userQo&&item.userQo.gender==='2'&&'/static/images/home/female.png')||(item.userQo&&item.userQo.gender==='1'&&'/static/images/home/male.png')"
          ></image>

        </view>
        <view><text class="profession tui-skeleton-rect">{{profess(item.userQo&&item.userQo.profess)}}</text>
          <text
            v-if="item.userQo&&item.userQo.realnameflag==='1'"
            class="tag tui-skeleton-rect"
          >已实名</text>
          <text
            v-if="item.userQo&&item.userQo.creditflag==='1'"
            class="tag tui-skeleton-rect"
          >信用保证</text>
        </view>
      </view>
    </view>
    <view class="time tui-skeleton-rect">{{item.timeStr}}</view>
  </view>
</template>

<script src="./index.js">

</script>


<style lang="scss" scoped>
@import "./index.scss";
</style>
