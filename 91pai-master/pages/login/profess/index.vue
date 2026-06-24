<!--
 * @Author: shawn
 * @LastEditTime: 2020-03-26 11:58:45
 -->
<template>
  <view>
    <view class="container fadeIn">
      <view class="title">
        选择您的职业信息
      </view>
      <view class="tui-flex tui-align-between item-box">
        <view
          @tap="selectItem(item,index)"
          v-for="(item,index) in itemList"
          :key="index"
          :class="['item tui-col-6 mr',item===activeItem?'active':'']"
        >{{item}} </view>
      </view>

    </view>
    <view @tap="submit">
      <uni-button
        :class="activeItem?'btn-primary-fixed active':'btn-primary-fixed disactive'"
        hover-class="btn-hover"
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
