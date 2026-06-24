<!--
 * @Author: shawn
 * @LastEditTime: 2020-06-07 16:16:23
 -->
<template>
  <view class="box fadeIn">
    <!-- <image
      class="logo"
      src="/static/images/home/success.png"
      mode="aspectFill"
    ></image> -->
    <view class="logo-box">
      <image
        :class="['logo',showObj.type?showObj.type:'success']"
        :src="'/static/images/home/'+(showObj.type||'success')+'.png'"
        mode="aspectFix"
      ></image>
    </view>
    <view class="title">{{showObj.title}}</view>
    <view class="subtitle">{{showObj.desc}}</view>
    <view
      class="realname"
      v-if="userInfo&&userInfo.status&&userInfo.status !== '2'&&showRealName.realname === '1'"
    >
      <view class="realname-box">
        <view class="left-box">
          <view class="left-top">
            <image
              src="/static/images/mine/realname.png"
              mode="widthFix"
              class="realname-icon"
            ></image>
            <text class="text-realname">实名认证</text>
            <text>(更高约拍成功率)</text>
          </view>
          <view class="text-bottom">为保证服务真实性和约拍安全，前去实名认证</view>
        </view>
        <view
          class="btn"
          @tap="navTo('/pages/mine/realname/index','redirectTo')"
        >
          去实名
          <tui-icon
            name="arrowright"
            :size="14"
            color="#2C2D2D"
          ></tui-icon>
        </view>
      </view>
      <view>

      </view>

    </view>
    <button
      v-if="showObj.goUrl"
      type="primary"
      class="success-btn"
      @click="goHome"
    >{{showObj.btnText}}</button>
    <view v-else>
      <button
        v-if="!userInfo || !userInfo.status"
        type="primary"
        class="success-btn"
        @click="goHome"
      >回到首页</button>

      <button
        @click="goHome"
        v-if="userInfo&&userInfo.status&&userInfo.status !== '2'"
        class="btn-home"
        hover-class="tui-outline-hover"
      >回到首页</button>
    </view>

  </view>
</template>

<script src="./index.js">

</script>

<style lang="scss" scoped>
@import "./index.scss";
</style>
