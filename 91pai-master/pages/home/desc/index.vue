<!--
 * @Author: shawn
 * @LastEditTime: 2020-05-19 22:38:09
 -->
<template>
  <view>
    <tui-skeleton v-if="skeletonShow" backgroundColor="#fafafa" borderRadius="10rpx"></tui-skeleton>
    <qrcode-alert :visiable="qrcodeAlertShow" @callback="qrcodeAlertShow=false" title="关注微信公众号发起约拍"></qrcode-alert>
    <view class="container fadeIn">
      <view class="tab-box">
        <view class="item">
          <image
            src="/static/images/home/man.png"
            class="tab-icon-man tui-skeleton-rect"
            mode="widthFix"
          />
          <view class="tab-text tui-skeleton-rect">{{target(content.target) || 'xxx'}}</view>
        </view>
        <view class="item">
          <image
            src="/static/images/home/price.png"
            class="tab-icon-price tui-skeleton-rect"
            mode="widthFix"
          />
          <view class="tab-text tui-skeleton-rect">{{getFee(content.chargeway) || 'xxx'}}</view>
        </view>
        <view class="item">
          <image
            src="/static/images/home/location.png"
            class="tab-icon-location tui-skeleton-rect"
            mode="widthFix"
          />
          <view class="tab-text tui-skeleton-rect">{{content.city || 'xxx'}}</view>
        </view>
      </view>
      <view class="user-info-box">
        <header-box :item="content"></header-box>
        <view class="subtitle tui-skeleton-rect">{{content.describ || ''}}</view>
      </view>
      <view class="user-info-desc">
        <view
          class="item-text tui-skeleton-rect"
          v-if="listType&&listType==='infoList'"
        >是否推荐：{{content.recmobile==='1'?'是':'否'}}</view>
        <view
          class="item-text tui-skeleton-rect"
          v-if="content.patdate"
        >约拍时间：{{content.patdate || ''}}</view>
        <view
          class="item-text tui-skeleton-rect"
          v-if="content.patarea"
        >约拍具体地点：{{content.patarea || ''}}</view>
        <view
          class="item-text tui-skeleton-rect"
          v-if="showRealName.realname==='1'"
        >是否要求对方实名：{{realnameflag(content.realnameflag) || '不需要'}}实名</view>
        <view
          class="item-text tui-skeleton-rect"
        >是否要求对方信用担保：{{creditflag(content.creditflag) || '不需要'}}担保</view>
        <view class="item-text tui-skeleton-rect" v-if="content.chargeamt">
          <text v-if="content.chargeway==='2'">可付费金额：</text>
          <text v-else>收费金额：</text>
          {{content.chargeamt || ''}}元
        </view>
        <view class="item-text tui-skeleton-rect" v-if="content.patstyle">
          <!-- TODO -->
          拍摄风格：
          <view class="style">
            <view class="item" :key="x" v-for="x in patstyleFunc(content.patstyle)">{{patstyle(x)}}</view>
          </view>
        </view>
      </view>
      <view class="user-info-img tui-skeleton-rect" v-if="content.pics&&content.pics.length>0">
        <!--banner-->
        <view class="tui-banner-swiper tui-skeleton-rect">
          <!-- <swiper
          :autoplay="true"
          :interval="8000"
          :duration="150"
          :circular="true"
          :current="current"
          :style="{height:scrollH + 'px'}"
          @change="bannerChange"
          >-->
          <block v-for="(item,index) in content.pics" :key="index">
            <!-- <swiper-item
              :data-index="index"
              @tap.stop="previewImage"
            >-->
            <image
              :data-index="index"
              @tap.stop="previewImage"
              :src="item"
              mode="widthFix"
              class="tui-slide-image tui-skeleton-rect"
              :style="{height:scrollH+'px'}"
            />
            <!-- </swiper-item> -->
          </block>
          <!-- </swiper> -->
          <!-- <tui-tag
          type="translucent"
          shape="circleLeft"
          size="small"
          >{{bannerIndex+1}}/{{content.pics.length}}</tui-tag>-->
        </view>
        <!-- <view class="tui-slide-image-box">
        <block
          v-for="(item,index) in content.pics"
          :key="index"
        >
          <image
            @click="changeItem(index)"
            :src="item"
            mode="aspectFill"
            :class="['tui-slide-image-item',index===bannerIndex?'active':'']"
          />
        </block>
        </view>-->
        <!--banner-->
        <!--没有更多了 start-->
        <tui-nomore :visible="true"></tui-nomore>
        <!--没有更多了 end-->
      </view>
      <view class="user-info-num" v-if="recContent.length>0">
        <view class="text tui-skeleton-rect">
          收到约拍
          <text class="active">{{recContent.length}}</text>个
        </view>
        <view class="tui-slide-image-box img">
          <block v-for="(item,index) in recContent" :key="index">
            <image
              @tap="goLinkway(item)"
              :src="item.imgpath"
              mode="aspectFill"
              :class="['tui-slide-image-item2']"
            />
          </block>
        </view>
        <!--banner-->
      </view>
      <view class="tui-safearea-bottom"></view>
      <view class="button-fixed" v-if="listType&&listType==='infoList'">
        <view class="button-box">
          <view
            v-if="(content.status==='2'&&content.recomflag==='1')"
            class="btn-box tui-skeleton-rect"
            @tap="confirmFun2('no_recmobile')"
          >取消推荐</view>
          <view
            v-if="content.status==='2'&&(content.recomflag==='0' || !content.recomflag)"
            class="btn-box tui-skeleton-rect"
            @tap="confirmFun2('yes_recmobile')"
          >上推荐</view>
          <view
            class="btn-box tui-skeleton-rect"
            @tap="confirmFun('no')"
            v-if="content.status==='1' || content.status==='2'"
          >不通过</view>
          <view
            class="btn-box tui-skeleton-rect"
            v-if="content.status==='1' || content.status==='3'"
            @tap="confirmFun('yes-yes')"
          >通过并推荐</view>
          <view
            v-if="content.status==='1' || content.status==='3'"
            class="btn-box tui-skeleton-rect"
            @tap="confirmFun('yes-no')"
          >通过不推荐</view>
        </view>
      </view>

      <view class="button-fixed" v-else-if="!content.userQo||userInfo.id!==content.userQo.id">
        <view class="button-box">
          <view class="icon-box" @tap="save">
            <tui-icon
              :name="content.colflag==='1'?'star-fill':'star'"
              :size="18"
              :color="content.colflag==='1'?'#21C2C2':'#646666'"
            ></tui-icon>
            <view
              :class="['text tui-skeleton-rect',content.colflag==='1'?'save tui-skeleton-rect':'']"
            >收藏</view>
          </view>
          <!-- #ifdef MP-WEIXIN -->
          <view class="icon-box" @tap="posterFun">
            <image class="shareimg" mode="aspectFill" src="/static/images/poster/shareimg.png" />
            <view :class="['text tui-skeleton-rect']">生成海报</view>
          </view>
          <!-- #endif -->
          <view class="btn-box-active tui-skeleton-rect" @tap="orderShe">约拍{{ta}}</view>
        </view>
      </view>
      <confirm-alert @sure="sure" @cancle="cancle" :visiable="visiable" :button="button">
        <view v-if="pathName==='/pages/mine/realname/index'">当前约拍对方要求您实名认证方可发起约拍，是否前去实名？</view>
        <view v-if="pathName==='/pages/mine/credit/index'">当前约拍对方要求您缴纳保证金方可发起约拍，是否缴纳？</view>
      </confirm-alert>
    </view>
    <!-- #ifdef MP-WEIXIN -->
    <poster ref="poster" :mobile="userInfo.mobile" :content="content"></poster>
    <!-- #endif -->
  </view>
</template>

<script src="./index.js">
</script>

<style lang="scss" scoped>
@import "./index.scss";
</style>
