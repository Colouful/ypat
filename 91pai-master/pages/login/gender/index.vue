<!--
 * @Author: shawn
 * @LastEditTime: 2020-03-26 11:58:58
 -->
<template>
  <view>
    <view class="container fadeIn">
      <view class="title">
        选择您的性别
        <view class="info">
          性别一旦选择不可更改
        </view>
      </view>
      <view class="item-box">
        <view
          class="item-man"
          @tap="selectMan('1')"
        >
          <image
            class="tx"
            :src="`/static/images/mine/${manType==='1'?'man_ac':'man'}.png`"
          ></image>
          <view :class="[manType==='1'?'active':'']">男生</view>
        </view>
        <view
          class="item-man"
          @tap="selectMan('2')"
        >
          <image
            class="tx"
            :src="`/static/images/mine/${manType==='2'?'woman_ac':'woman'}.png`"
          ></image>
          <view :class="[manType==='2'?'active':'']">女生</view>
        </view>
      </view>

    </view>
    <view @tap="saveUserInfo">
      <uni-button
        :class="manType?'btn-primary-fixed active':'btn-primary-fixed disactive'"
        hover-class="btn-hover"
        formType="submit"
        type="primary"
      >下一步</uni-button>
    </view>
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
