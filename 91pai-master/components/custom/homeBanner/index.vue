<template>
  <view v-if="visibleBanners.length" class="home-banner">
    <swiper
      class="home-banner-swiper"
      :autoplay="visibleBanners.length > 1"
      :circular="visibleBanners.length > 1"
      :interval="5000"
      :duration="300"
      @change="change"
    >
      <swiper-item v-for="(item,index) in visibleBanners" :key="item.id || index">
        <view class="banner-card" @tap="tapBanner(item)">
          <image class="banner-image" :src="item.imgpath" mode="aspectFill" />
          <view class="banner-mask">
            <view class="banner-title">{{item.title || '爱去拍精选'}}</view>
          </view>
        </view>
      </swiper-item>
    </swiper>
    <view v-if="visibleBanners.length > 1" class="banner-dots">
      <view
        v-for="(item,index) in visibleBanners"
        :key="'dot-' + (item.id || index)"
        :class="['dot', current === index ? 'active' : '']"
      ></view>
    </view>
  </view>
</template>

<script src="./index.js"></script>

<style lang="scss" scoped>
@import "./index.scss";
</style>
