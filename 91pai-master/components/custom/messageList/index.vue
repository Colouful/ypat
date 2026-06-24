<!--
 * @Author: shawn
 * @LastEditTime : 2019-12-21 11:04:49
 -->
<!--
 * @Author: shawn
 * @LastEditTime : 2019-12-18 22:08:53
 -->
<!--
 * @Author: shawn
 * @LastEditTime : 2019-12-18 22:06:16
 -->
<template>
  <view>
    <block
      v-for="(item,index) in content"
      :key="index"
    >
      <view
        class="cell-item"
        @tap="navTo('/pages/home/linkway/index',item,index)"
      >
   
          <view class="tui-msg-box">
            <view class="msg-pic-box">
              <image
                :src="item.imgpath"
                class="tui-msg-pic"
                mode="widthFill"
              ></image>
              <badge
                v-if="item.messviewflag==='0'"
                size="small"
                class="msg-dot-list"
                type="danger"
                :dot="true"
              ></badge>
            </view>
            <view class="tui-msg-item">
              <view class="tui-msg-name">
                {{item.nickname || userInfo.nickname}}
                <view class="location">
                  <tui-icon
                    name="position"
                    :size="14"
                    color="#B4B9B9"
                  ></tui-icon>
                  {{item.city}}
                </view>
              </view>
              <view :class="['tui-msg-content']">
                <text class="time"> {{item.timeStr}}</text>
                <text v-if="type==='0'">向您发起了约拍请求</text>
                <text v-else>您的约拍请求已被<text class="nickname">{{item.nickname || ''}}</text>查看</text>
              </view>
            </view>

          </view>
          <view :class="['look-tel',item.linkwayflag==='0'?'active':'']">
            {{item.linkwayflag==='0'?'未查看联系方式':'已查看联系方式'}}
          </view>

      </view>
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
