<template>
  <view class="complain-page">
    <view class="section">
      <view class="section-title">请选择投诉原因</view>
      <view class="reason-grid">
        <view
          v-for="item in reasons"
          :key="item"
          :class="['reason-item', reason === item ? 'active' : '']"
          @tap="reason = item"
        >{{item}}</view>
      </view>
    </view>
    <view class="section">
      <textarea
        class="content-input"
        v-model="content"
        maxlength="300"
        placeholder="请输入您的投诉内容"
        placeholder-class="placeholder"
      />
    </view>
    <view class="section">
      <view class="section-title">证据截图</view>
      <view class="image-list">
        <view v-for="(image,index) in images" :key="image" class="image-wrap">
          <image class="evidence" :src="image" mode="aspectFill" @tap="preview(index)" />
          <view class="remove" @tap="remove(index)">×</view>
        </view>
        <view v-if="images.length < 3" class="add-image" @tap="chooseImage">+</view>
      </view>
    </view>
    <view class="black-btn" @tap="confirmBlack">拉黑TA</view>
    <view class="footer">
      <view class="submit-btn" @tap="submit">提交</view>
    </view>
  </view>
</template>

<script src="./index.js"></script>

<style lang="scss" scoped>
@import "./index.scss";
</style>
