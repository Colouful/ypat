<!--
 * @Author: shawn
 * @LastEditTime: 2020-06-07 16:11:37
 -->
<template>
  <view class="container fadeIn">

    <view class="user-section">
      <view
        class="user-info-box"
        @tap="navTo('/pages/mine/userInfo/index')"
      >
        <view class="portrait-box">
          <image
            class="portrait"
            :src="userInfo.imgpath || '/static/images/mine/my_touxiang@2x.png'"
          ></image>
        </view>
        <view class="info-box">
          <view class="username">{{formatNum(userInfo.mobile) || '登录注册'}} <image
              v-if="userInfo.gender&&(userInfo.gender==='1'||userInfo.gender==='2')"
              mode="widthFix"
              class="sex tui-skeleton-rect"
              :src="(userInfo.gender==='2'&&'/static/images/mine/nv.png')||(userInfo&&userInfo.gender==='1'&&'/static/images/mine/nan.png')"
            ></image>
          </view>
          <view class="username">
            <!-- <text
              v-if="userInfo.realnameflag==='1'"
              class="tag tui-skeleton-rect"
            >已实名</text> -->
            <text
              v-if="userInfo.creditflag==='1'"
              class="tag tui-skeleton-rect"
            >信用保证</text>
          </view>
        </view>
        <tui-icon
          v-if="userInfo.mobile"
          class="icon-right"
          name="arrowright"
          :size="20"
          color="#fff"
        ></tui-icon>
      </view>
      <view class="tui-header-btm">
        <block 
        v-for="(item) in numList"
          :key="item.key">
          
        <view
        v-if="(item.key==='ppd'&&showRealName.realname==='1') || item.key!=='ppd'"
          class="tui-btm-item"
          @tap="navTo(`/pages/mine/${item.url}/index?type=${item.key}&name=${item.name}`)"
        >
          <view
            class="tui-btm-num"
            v-if="item.key==='rectimesCount'"
          >{{rectimesCount || 0}}</view>
          <view
            class="tui-btm-num"
            v-else
          >{{userInfo[item.key] || 0}}</view>
          <view class="tui-btm-text">{{item.name}}</view>
        </view>
        </block>
      </view>
    </view>

    <view class="cover-container">
      <view class="credit-box"
       v-if="showRealName.realname==='1'">
        <view class="title">信用</view>
        <view class="item-container">
          <view
           
            class="item-box"
            @tap="navTo('/pages/mine/realname/intro/index','realname')"
          >
            <view>
              <view class="title-item">
                实名认证
              </view>
              <view class="title-desc">
                实名更可靠
              </view>
            </view>
            <image
              class="item-img"
              src="/static/images/mine/credit_user.png"
            ></image>
          </view>
          <view
        
            class="item-box"
            @tap="navTo('/pages/mine/credit/index','credit')"
          >
            <view>
              <view class="title-item">
                信用担保
              </view>
              <view class="title-desc">
                信用约拍保证
              </view>
            </view>
            <image
              class="item-img"
              src="/static/images/mine/credit_dp.png"
            ></image>
          </view>
        </view>
      </view>
      <tui-list-view class="tui-list-view">
        <tui-list-cell
          v-if="role&&role===item.role || !item.role"
          padding="40rpx 40rpx"
          v-for="(item,index) in cellList"
          :key="index"
          @tap="navTo(item.url,'',item)"
          :arrow="true"
          :bottomLine="item.line"
        >
          <text class="tui-list-cell-name">{{item.title}}</text>
          <badge
            class="dot-msg"
            type="danger"
            size="small"
            dot
            v-if="item.dot&&msgCount>0"
          ></badge>
          <view
            v-if="item.desc"
            class="tui-right"
          >{{item.desc}}</view>
        </tui-list-cell>
      </tui-list-view>
    </view>
    <!--加载框 start-->
    <tui-loading :visible="visible"></tui-loading>
    <!--加载框 end-->
    <view class="tui-safearea-bottom"></view>
    <tab-bar :current="2"></tab-bar>
  </view>
</template>
<script src="./index.js">
</script>
<style lang='scss'>
@import "./index.scss";
</style>
