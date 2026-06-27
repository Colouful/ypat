<!--
 * @Author: shawn
 * @LastEditTime: 2020-05-07 22:54:33
 -->
<template>
  <view class="container">
    <view class="top-box">
      <view class="title">欢迎加入爱去拍</view>
      <view class="subtitle">向爱去拍介绍一下你自己</view>
    </view>
    <view class="warp-box">
      <view class="cell-list">
        <view class="title-box">
          <view class="cell-title">性别</view>
        </view>
        <view class="item-btn-box">
          <view
            @tap="selectGender('男')"
            class="item-btn"
            :class="{'active':resultInfo.gender==='男'}"
          >
            <image
              mode="aspectFit"
              class="sex tui-skeleton-rect"
              :src="resultInfo.gender==='男'?`/static/images/home/nan2.png`:`/static/images/home/nan.png`"
            />
            <text>男</text>
          </view>
          <view
            @tap="selectGender('女')"
            class="item-btn"
            :class="{'active':resultInfo.gender==='女'}"
          >
            <image
              mode="aspectFit"
              class="sex tui-skeleton-rect"
              :src="resultInfo.gender==='女'?`/static/images/home/nv2.png`:`/static/images/home/nv.png`"
            />
            <text>女</text>
          </view>
        </view>
      </view>
      <view
        class="cell-list-two"
        @tap="toggleTab({
			mode:'date',
          key: 'birthday',
          name: '生日',
			})"
        hover-class="cell-hover"
      >
        <view class="title-box">
          <view class="cell-title">生日</view>
          <view class="subtitle-box">
            <view v-if="resultInfo.birthday" class="desc active">{{resultInfo.birthday}}</view>
            <view v-else class="desc">请选择出生日期</view>
            <view class="arrow"></view>
          </view>
        </view>
      </view>
      <view
        class="cell-list-two"
        @tap="toggleTab({
			mode:'profess',
          key: 'profess',
          name: '职业',
			})"
        hover-class="cell-hover"
      >
        <view class="title-box">
          <view class="cell-title">职业</view>

          <view class="subtitle-box">
            <view v-if="resultInfo.profess" class="desc active">{{resultInfo.profess}}</view>
            <view v-else class="desc">请选择职业</view>
            <view class="arrow"></view>
          </view>
        </view>
      </view>
      <view
        class="cell-list-two"
        hover-class="cell-hover"
        @tap="toggleTab({
          mode:'region',
          key: 'area',
			})"
      >
        <view class="title-box">
          <view class="cell-title">城市</view>

          <view class="subtitle-box">
            <view v-if="resultInfo.area" class="desc active">{{resultInfo.area}}</view>
            <view v-else class="desc">请选择城市</view>
            <view class="arrow"></view>
          </view>
        </view>
      </view>
    </view>
    <view @tap="formSubmit">
      <uni-button
        :class="resultInfo.gender&&resultInfo.area&&resultInfo.profess&&resultInfo.birthday?'btn-primary-fixed active':'btn-primary-fixed disactive'"
        hover-class="btn-hover"
      >保存</uni-button>
    </view>
    <!--底部选择层-->
    <tui-bottom-popup :show="showShare" @close="hidePopup">
      <view class="popup-box">
        <view class="popup-header">
          <view class="cancle" @tap="clickPop('cancle')">取消</view>
          <view class="confirm" @tap="clickPop('confirm')">确定</view>
        </view>
        <view class="content-box">
          <view
            class="item"
            :key="index"
            @tap="selectItem(item)"
            v-for="(item,index) in professList"
            :class="{'active':selectProfess===item.label}"
          >{{item.label}}</view>
        </view>
      </view>
    </tui-bottom-popup>
    <!--底部选择层-->
    <w-picker
      mode="region"
      :value="defaultRegion"
      default-type="value"
      :hide-area="false"
      @confirm="onConfirm($event,'region')"
      @cancel="onCancel"
      ref="area"
    ></w-picker>
    <!-- 约拍时间 -->
    <w-picker
      mode="date"
      endYear="2040"
      :value="defaultValTime"
      :current="false"
      fields="day"
      @confirm="onConfirm"
      @cancel="onCancel"
      themeColor="#1BB6B6"
      disabled-after
      ref="birthday"
    ></w-picker>
    <!-- 约拍时间 -->
  </view>
</template>

<script src="./index.js">
</script>

<style lang="scss" scoped>
@import "./index.scss";
</style>
