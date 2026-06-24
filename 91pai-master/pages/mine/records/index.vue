<!--
 * @Author: shawn
 * @LastEditTime : 2020-01-05 08:26:24
 -->
<template>
  <view class="container fadeIn">
    <tui-list-view class="tui-list-view">
      <tui-list-cell
        padding="30rpx 40rpx"
        v-for="(item,index) in content"
        :key="index"
      >
        <view class="jilu">
          <view>
            <view class="tui-list-cell-name">{{item.typeTxt}}</view>
            <view class="tui-desc">{{item.credate}}</view>
          </view>
          <view :class="Number(item.type)>=3?'add':'subtraction'">{{Number(item.type)>=3?'':'+'}}{{item.ppd}}</view>
        </view>
      </tui-list-cell>
    </tui-list-view>
    <!-- 加载更多 start -->
    <tui-loadmore
      :visible="loading"
      :index="3"
      type="orange"
    ></tui-loadmore>
    <!-- 加载更多 end -->

    <!-- 暂无数据 start-->
    <tui-tips
      v-if="content.length===0"
      :fixed="true"
      imgUrl="/static/images/common/nodata@3x.png"
    >暂无数据</tui-tips>
    <!-- 暂无数据 end-->

    <!--没有更多了 start-->
    <tui-nomore :visible="pullUpOn && content.length>0"></tui-nomore>
    <!--没有更多了 end-->
  </view>
</template>

<script src="./index.js">

</script>

<style lang="scss" scoped>
@import "./index.scss";
</style>
