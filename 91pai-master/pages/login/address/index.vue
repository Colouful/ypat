<!--
 * @Author: shawn
 * @LastEditTime: 2020-05-07 23:31:34
 -->
<template>
  <view>
    <view class="container fadeIn">
      <view>
        <view class="title">
          您的所在城市
        </view>
        <view class=" text-center">
          <image
            class="birthday-img"
            src="/static/images/mine/birthday.png"
          ></image>
          <!-- <view
            @tap="show"
            class="birthday"
          >
            <view :class="['birthday-text',dateStr?'active':'']"> {{dateStr||'点击选择生日'}} </view>
          </view> -->
        </view>
        <view>
          <!-- 约拍时间 -->
          <w-picker
          showView
            mode="region"
            :value="defaultRegion"
            default-type="value"
            :hide-area="false"
            @confirm="onConfirm($event,'region')"
            @cancel="onCancel"
            ref="area"
          ></w-picker>
          <!-- 约拍时间 -->
        </view>
      </view>
    </view>
    <view @tap="submit">
      <uni-button
        :class="province&&city?'btn-primary-fixed active':'btn-primary-fixed disactive'"
        class="btn-primary-fixed"
        hover-class="btn-hover"
      >下一步</uni-button>
    </view>

  </view>
</template>

<script src="./index.js">

</script>

<style lang="scss">
@import "./index.scss";
</style>
