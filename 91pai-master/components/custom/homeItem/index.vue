<!--
 * @Author: shawn
 * @LastEditTime : 2020-01-04 22:32:44
 -->
<template>
  <view class="box">
    <view
      class="title-box"
      @tap="navTo(`/pages/home/desc/index?id=${item.id}`)"
    >
      <view class="title-rect">
        <view class="title  tui-skeleton-rect">{{target(item.target) || ''}}·{{chargeway(item.chargeway) || ''}}
          <text
            class="sh-status"
            v-if="item.statusTxt&&type==='homePage'"
          >{{item.statusTxt}}</text>
        </view>
        <view
          class="adress  tui-skeleton-rect"
          v-if="item.city"
        >
          <tui-icon
            name="position"
            :size="14"
            color="#B4B9B9"
          ></tui-icon> {{item.city || '其他'}}
        </view>
      </view>
      <view class="subtitle tui-skeleton-rect">{{item.describ || ''}}</view>
    </view>
    <view class="img-box">
      <view class="img-constainer">
        <!-- 图片1 -->
        <view class="tui-flex">
          <block
            v-if="item.pics&&item.pics.length===1"
            v-for="(item2,index) in item.pics"
            :key="index"
          >
            <image
              @click="previewImage(index)"
              v-if="item.pics&&item.pics.length===1"
              mode="aspectFill"
              :class="['image-box tui-skeleton-rect one']"
              :src="item2"
            ></image>

          </block>
        </view>
        <!-- 图片12 -->
        <!-- 图片12 -->
        <view class="tui-flex">
          <block
            v-if="item.pics&&item.pics.length===2"
            v-for="(item2,index) in item.pics"
            :key="index"
          >
            <image
              @click="previewImage(index)"
              v-if="item.pics&&item.pics.length===2"
              mode="aspectFill"
              :class="['image-box2 tui-skeleton-rect two']"
              :src="item2"
            ></image>

          </block>
        </view>
        <!-- 图片12 -->
        <!-- 图片3 -->
        <view
          class="tui-flex"
          v-if="item.pics&&item.pics.length===3"
        >
          <view class="tui-col-8  three-l">
            <image
              @click="previewImage(0)"
              class="image-box3 overhidden"
              mode="aspectFill"
              :src="item.pics&&item.pics[0]"
            ></image>
          </view>
          <view class="tui-col-4">
            <image
              @click="previewImage(1)"
              mode="aspectFill"
              class="image-box3 three-r"
              :src="item.pics&&item.pics[1]"
            ></image>
            <image
              @click="previewImage(2)"
              mode="aspectFill"
              class="image-box3 three-r"
              :src="item.pics&&item.pics[2]"
            ></image>
          </view>
        </view>
        <!-- 图片3 -->

        <!-- 图片4 -->
        <view
          class="tui-flex"
          v-if="item.pics&&item.pics.length===4"
        >
          <view class="tui-col-6 four-l-r">
            <image
              @click="previewImage(0)"
              mode="aspectFill"
              class="image-box4 four-l"
              :src="item.pics&&item.pics[0]"
            ></image>
            <image
              @click="previewImage(1)"
              mode="aspectFill"
              class="image-box4 four-l"
              :src="item.pics&&item.pics[1]"
            ></image>
          </view>
          <view class="tui-col-6">
            <image
              @click="previewImage(2)"
              mode="aspectFill"
              class="image-box4 four-r"
              :src="item.pics&&item.pics[2]"
            ></image>
            <image
              @click="previewImage(3)"
              mode="aspectFill"
              class="image-box4 four-r"
              :src="item.pics&&item.pics[3]"
            ></image>
          </view>
        </view>
        <!-- 图片4 -->

        <!-- 图片5 -->
        <view
          class="tui-flex"
          v-if="item.pics&&item.pics.length===5"
        >
          <image
            @click="previewImage(0)"
            class="image-box5 two"
            mode="aspectFill"
            :src="item.pics&&item.pics[0]"
          ></image>
          <image
            @click="previewImage(1)"
            class="image-box5 two"
            mode="aspectFill"
            :src="item.pics&&item.pics[1]"
          ></image>

        </view>
        <view
          class="tui-flex"
          v-if="item.pics&&item.pics.length===5"
        >
          <view class="tui-col-4 three">
            <image
              @click="previewImage(2)"
              class="image-box110 five-l"
              mode="aspectFill"
              :src="item.pics&&item.pics[2]"
            ></image>
          </view>
          <view class="tui-col-4 three">
            <image
              @click="previewImage(3)"
              class="image-box110"
              mode="aspectFill"
              :src="item.pics&&item.pics[3]"
            ></image>
          </view>
          <view class="tui-col-4 three">
            <image
              @click="previewImage(4)"
              class="image-box110"
              mode="aspectFill"
              :src="item.pics&&item.pics[4]"
            ></image>
          </view>

        </view>
        <!-- 图片5 -->

        <!-- 图片6 -->
        <view
          class="tui-flex"
          v-if="item.pics&&item.pics.length>=6"
        >
          <view class="tui-col-8  three-l">
            <image
              @click="previewImage(0)"
              class="image-box6 overhidden"
              mode="aspectFill"
              :src="item.pics&&item.pics[0]"
            ></image>
          </view>
          <view class="tui-col-4">
            <image
              @click="previewImage(1)"
              mode="aspectFill"
              class="image-box6 three-r"
              :src="item.pics&&item.pics[1]"
            ></image>
            <image
              @click="previewImage(2)"
              mode="aspectFill"
              class="image-box6 three-r"
              :src="item.pics&&item.pics[2]"
            ></image>
          </view>
        </view>
        <view
          class="tui-flex"
          v-if="item.pics&&item.pics.length>=6"
        >
          <view class="tui-col-4 three">
            <image
              @click="previewImage(3)"
              class="image-box110 five-l"
              mode="aspectFill"
              :src="item.pics&&item.pics[3]"
            ></image>
          </view>
          <view class="tui-col-4 three">
            <image
              @click="previewImage(4)"
              class="image-box110"
              mode="aspectFill"
              :src="item.pics&&item.pics[4]"
            ></image>
          </view>
          <view class="tui-col-4 three">
            <image
              @click="previewImage(5)"
              class="image-box110"
              mode="aspectFill"
              :src="item.pics&&item.pics[5]"
            ></image>
          </view>

        </view>
        <!-- 图片6 -->

      </view>
      <view
        class="img-bottom"
        v-if="
      (item.pattimes&&item.pattimes>0)||(item.readtimes&&item.readtimes>0)||(item.coltimes&&item.coltimes>0)"
      >
        <view class="tag-box">
          <view
            class="item"
            v-if="item.readtimes&&item.readtimes>0"
          >
            <image
              mode="aspectFill"
              class="look"
              src="/static/images/home/look.png"
            ></image>
            <text class="small-title">查看 {{item.readtimes}}</text>
          </view>
          <view
            class="item"
            v-if="item.pattimes&&item.pattimes>0"
          >
            <image
              mode="aspectFill"
              class="look"
              src="/static/images/home/get.png"
            ></image>
            <text class="small-title">收到约拍 {{item.pattimes}}</text>
          </view>
          <view
            class="item"
            v-if="item.coltimes&&item.coltimes>0"
          >
            <image
              mode="aspectFill"
              class="look"
              src="/static/images/home/save.png"
            ></image>
            <text class="small-title">收藏 {{item.coltimes}}</text>
          </view>
        </view>
      </view>
    </view>
    <header-box :item="item"></header-box>
  </view>
</template>

<script src="./index.js">

</script>


<style lang="scss" scoped>
@import "./index.scss";
</style>
