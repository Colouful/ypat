<!--
 * @Author: shawn
 * @LastEditTime: 2020-04-10 09:38:03
 -->
<template>
  <view class="container fadeIn">

    <view class="bg-box">
      <image
        class="logo"
        src="/static/images/mine/credit_bg@2x.png"
        mode="widthFix"
      ></image>
      <view class="bg-item-text">
        <view>
          <image
            class="database"
            src="/static/images/mine/database.png"
            mode="widthFix"
          ></image>
          {{userInfo.ppd || 0}}
        </view>
      </view>
      <view
        class="szjl"
        @click="navTo('/pages/mine/records/index')"
      >收支记录</view>
    </view>
    <view class="box-container">
      <view>
        <text class="title">拍拍豆</text>
        <text class="subtitle">是我们平台的特殊服务：可用来发布信息、进行约拍</text>
      </view>
      <view
        class="box-item"
        v-if="rechargeList&&rechargeList.length>0"
      >
        <view class="title">充值拍拍豆</view>
        <view class="item-box">
          <view
            @tap="selectItem(item)"
            :class="['item',activeItem===item.currval?'active':'']"
            v-for="(item,index) in rechargeList"
            :key="index"
          >
            <view
              v-if="activeItem===item.currval"
              class="tag"
            >已选</view>
            <view class="desc">{{item.name}}</view>
            <view class="money">{{item.currval}}<text class="dw">元</text></view>
          </view>
        </view>
      </view>
    </view>
    <view class="invite-box">
      <view class="btn_box">

        <view
          class="credit-btn"
          @tap="order_create"
        >
          <image
            :class="activeItem?'btn_bg':'btn_bg dis'"
            src="/static/images/mine/black_btn.png"
          ></image>
          立即购买{{activeItem?activeItem+'元':''}}
        </view>
        <button
          open-type="share"
          class="credit-btn line"
          hover-class="opacity"
        >
          邀请好友获取拍拍豆
        </button>
        <view class="invite-desc">每成功邀请一个好友即可获得3个拍拍豆</view>
      </view>

    </view>
    <qrcode-alert
      :visiable="qrcodeAlertShow"
      @callback="qrcodeAlertShow=false"
      title="请到微信小程序充值拍拍豆"
    ></qrcode-alert>

    <!--加载框 start-->
    <tui-loading :visible="visible"></tui-loading>
    <!--加载框 end-->
  </view>
</template>

<script src="./index.js">

</script>

<style lang="scss" scoped>
@import "./index.scss";
</style>
