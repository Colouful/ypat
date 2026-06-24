<!--
 * @Author: shawn
 * @LastEditTime: 2020-04-14 14:34:32
 -->
<template>
  <tui-modal
    :show="visiable"
    @cancel="close"
    :button="[]"
    closeIcon
  >
    <view>
      <view class="title">
        {{title}}
      </view>
      <!-- #ifdef H5 -->
      <image
        @tap="saveImg('/static/images/common/gzh.png')"
        class="img"
        src="/static/images/common/gzh.png"
        mode="widthFix"
      />
      <!-- #endif -->
      <!-- #ifdef MP -->
      <image
        @tap="saveImg('/static/images/common/miniCode.png')"
        class="img"
        src="/static/images/common/miniCode.png"
        mode="widthFix"
      />
      <!-- #endif -->

      <view class="subtitle">点击图片保存到相册</view>
    </view>
  </tui-modal>
</template>

<script src="./index.js">

</script>


<style lang="scss" scoped>
@import "./index.scss";
</style>
