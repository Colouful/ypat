<!--
 * @Author: shawn
 * @LastEditTime: 2020-03-29 16:58:10
 -->
<template>
  <view class="container fadeIn">
    <image
      class="black-bg"
      mode="aspectFill"
      src="/static/images/mine/credit_bg@2x.png"
    ></image>
    <view class="white-box">
      <grid-four></grid-four>
      <view
        @tap="order_create"
        :class="[ischeck?'credit-btn active':'credit-btn']"
      >
        <image
          class="btn_bg"
          mode="widthFix"
          src="/static/images/mine/black_btn.png"
        >
        </image>
        保证金金额：<text class="money">199</text><text class="dw">元</text>
      </view>
      <view class="checkbox-box">
        <tui-icon
          @tap="ischeck=!ischeck"
          :name="ischeck?'circle-fill':'circle'"
          :size="20"
          class="check-item"
          :color="ischeck?'#FFCFA0':'#FFCFA0'"
        ></tui-icon>
        我已阅读并同意<text
          class="active"
          @tap="navTo('/pages/mine/creditagreement/index')"
        >《保证金协议》</text>
      </view>
    </view>
    <view class="white-box">
      <view class="title-list">保证金说明</view>
      <view class="cell-item">1.保证金缴纳之日起，3个月方可通过联系平台微信客服进行申请提现（举例说明：缴纳日为1月1日，则用户4月1日方可正常提取）。</view>
      <view class="cell-item">2.提前退款，将收取保证金金额15%作为平台管理费。</view>
      <view class="cell-item">3.退款到账卡：根据用户要求，可原支付通道方式返回或打款至用户指定账号。</view>
      <view class="cell-item">4.保证金已经退款，将不再享受平台任何保证金权利。</view>
      <view class="cell-item">5.其他详见<text
          class="active"
          @tap="navTo('/pages/mine/creditagreement/index')"
        >《保证金协议》</text>。</view>

    </view>
    <qrcode-alert
      :visiable="qrcodeAlertShow"
      @callback="qrcodeAlertShow=false"
      title="请到微信小程序缴纳保证金"
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
