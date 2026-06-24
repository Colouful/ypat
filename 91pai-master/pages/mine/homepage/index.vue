<!--
 * @Author: shawn
 * @LastEditTime: 2020-04-23 13:47:49
 -->
<template>
  <view class="container">

    <view class="fixed-box">
      <view class="bg-box">
        <image
          class="logo"
          src="/static/images/mine/homepage.png"
          mode="widthFix"
        ></image>
        <view class="box">
          <view class="header-box">
            <image
              class="header"
              @tap.stop="previewImage"
              mode="aspectFill"
              :src="userInfo.imgpath || '/static/images/mine/my_touxiang@2x.png'"
            ></image>
            <view class="tag-box">
              <text class="tag tui-skeleton-rect">{{userInfo.realnameflag==='1'?'已实名':'未实名'}}</text>
              <text class="tag tui-skeleton-rect">{{userInfo.realnameflag==='1'?'信用保证':'未担保'}}</text>
            </view>
          </view>
          <view class="title">
            {{userInfo.nickname}}
          </view>
          <view
            class="location"
            v-if="userInfo.province"
          >
            <tui-icon
              name="position"
              :size="14"
              color="#B4B9B9"
            ></tui-icon>
            {{userInfo.province}}
          </view>
          <view class="yptime">
            已收到约拍<text class="homepage-num">{{userInfo.rectimes}}</text>次
          </view>
        </view>
      </view>
      <view class="tab-box">
        <view
          @tap="changeTab(0)"
          :class="['item', currentTab==='0'?'active':'']"
        >发布的约拍</view>
        <view
          @tap="changeTab(1)"
          :class="['item', currentTab==='1'?'active':'']"
        >申请的约拍</view>
      </view>
    </view>
    <view class="home-container fadeIn">
      <yp-list
        :userid="userId"
        ref="mychild"
        @fatherMethod="fatherMethod"
        :type="currentTab+''"
      ></yp-list>
    </view>
  </view>
</template>

<script src="./index.js">

</script>

<style lang="scss" scoped>
@import "./index.scss";
</style>
