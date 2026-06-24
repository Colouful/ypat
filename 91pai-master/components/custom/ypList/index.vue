<!--
 * @Author: shawn
 * @LastEditTime : 2020-01-08 00:59:07
 -->
<template>
  <view>
    <block
      v-for="(item,index) in content"
      :key="index"
    >
      <home-item
        :item="item"
        :type="type==='0'&&!userid?'homePage':''"
      ></home-item>
    </block>
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
    <tui-nomore :visible="pullUpOn&&content.length>0"></tui-nomore>
    <!--没有更多了 end-->
  </view>
</template>

<script src="./index.js">

</script>


<style lang="scss" scoped>
@import "./index.scss";
</style>
