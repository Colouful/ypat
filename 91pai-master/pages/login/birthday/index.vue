<!--
 * @Author: shawn
 * @LastEditTime: 2020-04-15 14:06:29
 -->
<template>
  <view>
    <view class="container fadeIn">
      <view>
        <view class="title">
          您的出生日期
        </view>
        <view class=" text-center">
          <image
            class="birthday-img"
            src="/static/images/mine/birthday.png"
          ></image>
          <!-- <view
            @tap="show"
            class="birthday"
          >
            <view :class="['birthday-text',dateStr?'active':'']"> {{dateStr||'点击选择生日'}} </view>
          </view> -->
        </view>
        <view>
          <!-- 约拍时间 -->
          <w-picker
            mode="date"
            fields="day"
            showView
            endYear="2040"
            :value="defaultValTime"
            :current="false"
            @confirm="onConfirm"
            :disabledAfter="true"
            ref="time"
            themeColor="#1BB6B6"
          ></w-picker>
          <!-- 约拍时间 -->
        </view>
      </view>
    </view>
    <view @tap="submit">
      <uni-button
        :class="dateStr?'btn-primary-fixed active':'btn-primary-fixed disactive'"
        class="btn-primary-fixed"
        hover-class="btn-hover"
      >下一步</uni-button>
    </view>

  </view>
</template>

<script src="./index.js">

</script>

<style lang="scss">
@import "./index.scss";
</style>
