<!--
 * @Author: shawn
 * @LastEditTime: 2020-05-15 13:34:10
 -->
<template>
  <view class="container">
    <view class="tab-box">
      <view class="tab-container">
        <view
          @tap="changeTab(1)"
          :class="['item', currentTab==='1'?'active':'']"
        >推荐</view>
        <view
          @tap="changeTab(0)"
          :class="['item', currentTab==='0'?'active':'']"
        >同城</view>
        <view
          @tap="changeTab(2)"
          :class="['item', currentTab==='2'?'active':'']"
        >最新</view>
      </view>
      <view
        class="recommended"
        @click="rDrawer"
      >筛选</view>
    </view>

    <view class="content-box">
      <view class="tip-box">
        <view
          v-if="msgCount&&msgCount>0"
          @tap="navTo('/pages/mine/message/index')"
          class="tips"
        >收到消息{{msgCount}}条<tui-icon
            name="arrowright"
            :size="18"
            color="#fff"
          ></tui-icon>
        </view>
      </view>
      <view class="home-container fadeIn">
        <home-list
          ref="mychild"
          @fatherMethod="fatherMethod"
        ></home-list>
      </view>
    </view>
    <!--右抽屉-->
    <tui-drawer
      mode="right"
      :visible="rightDrawer"
      @close="closeDrawer"
    >
      <view class="d-container">
        <view class="item-select-box">
          <view
            class="item-select"
            v-for="(item,index) in selectList"
            :key="index"
          >
            <view class="title-item">{{item.title}}</view>
            <view class="btn-item-box">
              <view
                @tap="checkItem(item2,index,index2)"
                :class="[item2.check?'btn-item  active':'btn-item']"
                v-for="(item2,index2) in item.list"
                :key="index2"
              >{{item2.title}}</view>

            </view>
          </view>

          <view class="btn-container">
            <view
              class="btn-item reset"
              @tap="reset"
            >重置</view>
            <view
              class="btn-item sure"
              @tap="sure"
            >确定</view>
          </view>
        </view>

      </view>
    </tui-drawer>
    <view class="tui-safearea-bottom"></view>
    <tab-bar></tab-bar>
  </view>
</template>
<script src="./index.js">

</script>

<style lang="scss" scoped>
@import "./index.scss";
</style>
