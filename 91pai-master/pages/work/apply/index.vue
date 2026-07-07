<template>
  <view class="apply-page">
    <view class="safe-bar">安全防骗提醒：切勿独自与陌生人在室内约拍，切勿私下发送私密照片。</view>
    <view class="card target-card">
      <view class="card-title">约拍对象</view>
      <view class="target-user">
        <image class="target-avatar" :src="authorAvatar" mode="aspectFill" />
        <view class="target-main">
          <view class="target-name">{{authorName}}</view>
          <view class="target-meta">{{authorMeta}}</view>
        </view>
      </view>
    </view>
    <view class="card">
      <view class="card-title">约拍理由</view>
      <textarea class="reason-input" v-model="reason" maxlength="120" placeholder="请输入约拍理由～" />
    </view>
    <view class="card">
      <view class="card-title">我的联系方式</view>
      <view class="contact-row">
        <view class="contact-label">手机号：{{userInfo.mobile || '未填写'}}</view>
        <view v-if="!userInfo.mobile" class="edit-contact" @tap="goUserInfo">去完善</view>
      </view>
      <view class="contact-row">微信号：<input class="wx-input" v-model="wx" placeholder="请输入微信号" /></view>
    </view>
    <view class="tips">
      温馨提示：平台禁止一切色情私房约拍；切勿轻易相信未见面先交纳费用或定金的合作。
    </view>
    <view class="footer">
      <view class="cost">
        <view class="cost-num">5 麻豆</view>
        <view class="cost-left">提交约拍申请</view>
      </view>
      <view class="submit-btn" @tap="submit">确认提交</view>
    </view>
  </view>
</template>

<script src="./index.js"></script>

<style lang="scss" scoped>
@import "./index.scss";
</style>
