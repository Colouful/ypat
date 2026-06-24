<!--
 * @Author: shawn
 * @LastEditTime: 2020-04-14 14:04:16
 -->
<template>
  <view>
    <tui-skeleton
      v-if="skeletonShowCategory"
      skeletonBgColor="#e9e9e9"
      backgroundColor="#ffffff"
      borderRadius="10rpx"
    ></tui-skeleton>
    <view>
      <view
        v-for="(item,index) in content"
        :key="index"
      >
        <home-item :item="item"></home-item>
      </view>
      <!-- 加载更多 start -->
      <tui-loadmore
        :visible="loading"
        :index="3"
        type="orange"
      ></tui-loadmore>
      <!-- 加载更多 end -->

      <!-- 暂无数据 start-->
      <!-- :style="[{animationDelay: .3 + 's'}]" -->

      <tui-tips
        class="animation-fade"
        v-if="content.length===0"
        :fixed="true"
        imgUrl="/static/images/common/nodata@3x.png"
      >{{statusText}}
        <button
          v-if="status==='nodata'"
          type="primary"
          @tap="navTo('/pages/home/publish/index',true)"
          class="success-btn"
        >去发布</button>
        <button
          v-if="status==='refresh'"
          type="primary"
          @tap="downRefresh(false)"
          class="success-btn"
        >刷新</button>
        <button
          v-if="status==='setting'"
          type="primary"
          @tap="setting"
          class="success-btn"
        >去设置</button>
      </tui-tips>

      <!-- 暂无数据 end-->

      <!--没有更多了 start-->
      <tui-nomore :visible="pullUpOn&&content.length>0"></tui-nomore>
      <!--没有更多了 end-->
    </view>
    <qrcode-alert
      :visiable="qrcodeAlertShow"
      @callback="qrcodeAlertShow=false"
      title="关注微信公众号发起约拍"
    ></qrcode-alert>
  </view>
</template>

<script src="./index.js">

</script>


<style lang="scss" scoped>
@import "./index.scss";
</style>
