<!--
 * @Author: shawn
 * @LastEditTime: 2020-04-10 09:40:12
 -->
<template name="cropper">
  <view>
    <image
      :src="imgSrc.imgSrc"
      class="my-avatar"
      mode="aspectFill"
      :style="imgStyle"
    ></image>
    <!-- 上传图片 -->
    <canvas
      canvas-id="avatar-canvas"
      id="avatar-canvas"
      class="my-canvas"
      :style="{top: styleTop, height: cvsStyleHeight}"
      disable-scroll="false"
    ></canvas>
    <!-- 截取边框 -->
    <canvas
      canvas-id="oper-canvas"
      id="oper-canvas"
      class="oper-canvas"
      :style="{top: styleTop, height: cvsStyleHeight}"
      disable-scroll="false"
      @touchstart="fStart"
      @touchmove="fMove"
      @touchend="fEnd"
    ></canvas>
    <view
      class="oper-wrapper"
      :style="{display: styleDisplay}"
    >
      <view
        class="btn-wrapper"
        v-if="showOper"
      >
        <view
          class="btn-action"
          hover-class="opacity"
          @tap="fClose"
        >取消</view>
        <view
          class="btn-action"
          hover-class="opacity"
          @tap="fUpload"
        >选取</view>
      </view>
    </view>
  </view>
</template>

<script src="./index.js">

</script>

<style lang="scss" scoped>
@import "./index.scss";
</style>

