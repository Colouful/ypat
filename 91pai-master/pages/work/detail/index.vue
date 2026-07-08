<template>
  <view class="work-detail-page">
    <tui-loading :visible="loading"></tui-loading>
    <view v-if="content.id" class="content">
      <view class="media-list">
        <image
          v-for="(url,index) in mediaUrls"
          :key="url + index"
          class="media-image"
          :src="url"
          mode="widthFix"
          :data-index="index"
          @tap="previewImage"
        />
      </view>
      <view class="author-card">
        <image class="avatar" :src="authorAvatar" mode="aspectFill" />
        <view class="author-main">
          <view class="nickname">{{authorName}}</view>
          <view class="meta">{{authorMeta}}</view>
        </view>
        <view class="complain" @tap="goComplain">投诉</view>
      </view>
      <view class="description">{{content.description || '这个作品暂时没有描述'}}</view>
      <view class="bottom-space"></view>
    </view>
    <view v-else-if="!loading" class="empty">
      <view class="empty-title">作品不存在或已下架</view>
      <view class="empty-btn" @tap="loadDetail">重新加载</view>
    </view>
    <view v-if="content.id && !isSelf" class="bottom-bar">
      <view class="icon-action" @tap="toggleLike">
        <tui-icon :name="liked ? 'agree-fill' : 'agree'" :size="28" :color="liked ? '#ff5361' : '#333'"></tui-icon>
      </view>
      <view class="icon-action" @tap="toggleFavorite">
        <tui-icon :name="favorited ? 'star-fill' : 'star'" :size="28" :color="favorited ? '#ff5361' : '#333'"></tui-icon>
      </view>
      <button class="share-action" open-type="share">
        <tui-icon name="share" :size="28" color="#333"></tui-icon>
      </button>
      <view class="apply-btn" @tap="goApply">立即约拍</view>
    </view>
  </view>
</template>

<script src="./index.js"></script>

<style lang="scss" scoped>
@import "./index.scss";
</style>
