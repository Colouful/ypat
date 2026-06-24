<!--
 * @Author: shawn
 * @LastEditTime: 2020-03-26 11:59:20
 -->
<!--
 * @Author: shawn
 * @LastEditTime : 2019-12-18 22:28:40
 -->
<template>
  <view class="page-container fadeIn">
    <view class="cell-item">
      <list-cell :last="true">
        <view
          class="tui-msg-box"
          @tap="navTo(`/pages/mine/homepage/index?userId=${messDesc.sendperid}`)"
        >
          <view class="msg-pic-box">
            <image
              :src="messDesc.imgpath"
              class="tui-msg-pic"
              mode="widthFill"
            ></image>
          </view>
          <view class="tui-msg-item">
            <view class="tui-msg-name">
              {{messDesc.nickname || 'xxx'}}
              <view class="location">
                <tui-icon
                  name="position"
                  :size="14"
                  color="#B4B9B9"
                ></tui-icon>
                {{messDesc.city  || 'xxx'}}
              </view>
            </view>
            <view :class="['tui-msg-content']">
              <text class="time"> {{messDesc.timeStr  || 'xxx'}}</text>
              <text v-if="type==='1'">您向<text class="nickname">{{messDesc.nickname}}</text>发起了约拍请求</text>
              <text v-else>向您发起了约拍请求</text>

            </view>
          </view>

        </view>

      </list-cell>
    </view>
    <!-- 回复 -->
    <view class="user-info-box">
      <view class="subtitle">{{messDesc.content}}</view>
    </view>

    <view class="contact-box">
      <view class="contact-title">对方联系方式</view>
      <view class="cell-box">
        <view class="cell-item">
          <view class="cell-label">
            手机号
            <text class="cell-value">{{info.mobile || '*** **** ****'}}</text>
          </view>
          <view
            v-if="info.mobile"
            class="action"
            @tap="clipboard(info.mobile)"
          >复制</view>
        </view>
        <view class="cell-item">
          <view class="cell-label">
            微信号
            <text class="cell-value">{{info.wx || '**********'}}</text>
          </view>
          <view
            v-if="info.wx"
            class="action"
            @tap="clipboard(info.wx)"
          >复制</view>
        </view>

      </view>
    </view>
    <view @tap="goSubmit">
      <uni-button
        :class="linkwayflag==='1' || info.mobile?'btn-primary-fixed disactive':'btn-primary-fixed'"
        hover-class="btn-hover"
      >查看联系方式</uni-button>
    </view>
    <confirm-alert
      @sure="sure"
      @cancle="cancle"
      :visiable="visiable"
      :button="button"
    >
      <view v-if="pathName==='sure'">
        查看Ta的联系方式将会使用<text class="activeText">3</text>个拍拍豆，是否确定使用拍拍豆？
      </view>
      <view v-if="pathName==='/pages/mine/ppd/index'">
        查看她的联系方式将会使用<text class="activeText">3</text>个拍拍豆，您当前剩余拍拍豆暂不充足，赶紧前去获取吧
      </view>

    </confirm-alert>
  </view>
</template>

<script src="./index.js">

</script>

<style lang="scss" scoped>
@import "./index.scss";
</style>
