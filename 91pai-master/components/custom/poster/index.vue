<!--
 * @Author: shawn
 * @LastEditTime: 2020-05-19 21:58:52
 -->
<template>
  <view class="content">
    <button @tap="shareFc">1</button>
    <view class="flex_row_c_c modalView" :class="qrShow?'show':''">
      <view class="flex_column">
        <view class="backgroundColor-white">
          <view class="posterImage-box">
            <view class="close-img-box" @tap="hideQr()">
              <image src="/static/images/poster/close-img.png" mode="widthFix" class="close-img" />
            </view>
            <view v-if="true">
              <view class="posterImageCopy-box">
                <image :src="curImg" mode="widthFix" class="posterImageCopy" />
              </view>
              <view class="bottom-view">
                <view class="name-box">
                  <view>
                    <view class="name">{{`${content.userQo.nickname}/${content.city}`}}</view>
                    <view class="title">{{`${content.targetTxt} · ${content.chargewayTxt}`}}</view>
                  </view>
                  <image :src="qrCodeUrl" mode="aspectFit" class="qr-img" />
                </view>
                <view class="logo-img-box">
                  <image src="/static/images/poster/logo-img.png" mode="widthFix" class="logo-img" />
                </view>
              </view>
            </view>
            <image v-else :src="poster.finalPath || ''" mode="widthFix" class="posterImage" />
          </view>
        </view>
        <view class="flex_row marginTop2vh">
          <button
            type="primary"
            class="nextBtn"
            hover-class="btn-hover"
            @tap.prevent.stop="nextImg()"
          >切换图片</button>
          <button
            type="primary"
            class="saveBtn"
            hover-class="btn-hover"
            @tap.prevent.stop="saveImage()"
          >保存海报</button>
        </view>
      </view>
    </view>
    <view class="hideCanvasView">
      <canvas
        class="hideCanvas"
        canvas-id="default_PosterCanvasId"
        :style="{width: (poster.width||10) + 'px', height: (poster.height||10) + 'px'}"
      ></canvas>
    </view>
  </view>
</template>

<script src="./index.js">
</script>


<style lang="scss" scoped>
@import "./index.scss";
</style>
