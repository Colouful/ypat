<!--
 * @Author: shawn
 * @LastEditTime: 2020-04-10 12:37:56
 -->
<template>
  <view class="container fadeIn">
    <image
      class="black-bg"
      mode="aspectFill"
      src="/static/images/mine/credit_bg@2x.png"
    ></image>
    <view class="white-box">
      <image
        class="box-img"
        mode="widthFix"
        src="/static/images/home/invitation_box.png"
      ></image>
      <view class="box">
        <view class="top">邀请好友成功注册即可
          <view> 获得<text class="num">3</text>个拍拍豆，快去邀请吧！</view>
        </view>
        <view class="title">
          <image
            mode="widthFix"
            class="title-img"
            src="/static/images/home/invitation_title.png"
          ></image>
          <text class="text">拍拍豆</text>
        </view>
        <view class="list-box">
          <view class="list-item">
            <image
              class="list-item-img"
              mode="widthFix"
              src="/static/images/home/step1.png"
            ></image>
            可用于信息发布
          </view>
          <view class="list-item">
            <image
              class="list-item-img"
              mode="widthFix"
              src="/static/images/home/step2.png"
            ></image>
            可用于约拍摄影师/模特
          </view>
          <view class="list-item">
            <image
              class="list-item-img"
              mode="widthFix"
              src="/static/images/home/step3.png"
            ></image>
            可用于查看摄影师/模特联系方式
          </view>
        </view>
      </view>
    </view>
    <button open-type="share" class="sharebtn">
      <view class="btn-box">
        <image
          class="btn-bg"
          mode="widthFix"
          src="/static/images/home/invitation_btn_bg.png"
        ></image>
        <text class="btn-text">立即邀请</text>
      </view>
    </button>
    <view class="invitation-list">
      <view class="list-title">我的邀请{{totalElements || "0"}}个<text class="total">共计收入<text class="ppd">{{3*totalElements || '0'}}</text>拍拍豆</text></view>
      <view
        class="list"
        v-for="(item,index) in invitation"
        :key="index"
      >
        <view>
          <image
            class="list-img"
            mode="aspectFill"
            :src="item.imgpath"
          ></image>
          {{item.nickname}}<text class="active">+3</text>
        </view>
        <view class="time">
          {{item.regisdate}}
        </view>
      </view>

    </view>
    <!-- 加载更多 start -->
    <tui-loadmore
      :visible="loading"
      :index="3"
      type="orange"
    ></tui-loadmore>
    <!-- 加载更多 end -->

    <!--没有更多了 start-->
    <tui-nomore :visible="pullUpOn&&invitation.length>0"></tui-nomore>
    <!--没有更多了 end-->
  </view>
</template>

<script src="./index.js">

</script>

<style lang="scss" scoped>
@import "./index.scss";
</style>
