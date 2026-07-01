<template>
  <view class="discover-page">
    <view class="discover-scroll" :style="{ paddingTop: statusBarHeight + 24 + 'px' }">
      <view class="discover-top">
        <view>
          <text class="discover-title">发现</text>
          <text class="discover-subtitle">找灵感、看动态、管理约拍流程</text>
        </view>
        <view class="discover-action" @tap="goSearch">
          <KeepIcon name="search" :size="42" />
        </view>
      </view>

      <view class="hero-card">
        <view class="hero-card__main">
          <text class="hero-card__label">今日推荐</text>
          <text class="hero-card__title">复古港风、INS、胶片风都在这里</text>
          <text class="hero-card__desc">从首页筛选到详情申请，按同一套视觉节奏完成探索。</text>
        </view>
        <view class="hero-card__button" @tap="goSearch">
          <text>去搜索</text>
          <KeepIcon name="chevron-right" :size="28" />
        </view>
      </view>

      <view class="quick-card">
        <view v-for="item in quickItems" :key="item.label" class="quick-card__item" @tap="item.action">
          <view class="quick-card__icon">
            <KeepIcon :name="item.icon" :size="42" />
          </view>
          <text>{{ item.label }}</text>
        </view>
      </view>

      <view class="section-head">
        <text class="section-head__title">热门风格</text>
        <text class="section-head__more" @tap="goSearch">更多</text>
      </view>
      <scroll-view class="style-scroll" scroll-x>
        <view class="style-row">
          <view v-for="style in styles" :key="style" class="style-chip" @tap="searchStyle(style)">
            {{ style }}
          </view>
        </view>
      </scroll-view>

      <view class="section-head">
        <text class="section-head__title">流程入口</text>
        <text class="section-head__more" @tap="goMine">我的</text>
      </view>
      <view class="flow-list">
        <view v-for="item in flowItems" :key="item.title" class="flow-card" @tap="item.action">
          <view class="flow-card__icon">
            <KeepIcon :name="item.icon" :size="42" />
          </view>
          <view class="flow-card__body">
            <text class="flow-card__title">{{ item.title }}</text>
            <text class="flow-card__desc">{{ item.desc }}</text>
          </view>
          <KeepIcon name="chevron-right" :size="34" color="#B3B8BE" />
        </view>
      </view>
    </view>

    <KeepTabBar active="discover" />
  </view>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { useAppStore } from '@/stores/app'
import { useUserStore } from '@/stores/user'
import { PHOTO_STYLES } from '@/constants/enums'
import KeepIcon from '@/components/business/KeepIcon.vue'
import KeepTabBar from '@/components/business/KeepTabBar.vue'
import { goRootTab, openMessage, openPublish } from '@/utils/tab-navigation'

const appStore = useAppStore()
const userStore = useUserStore()
const statusBarHeight = computed(() => appStore.statusBarHeight)
const styles = PHOTO_STYLES.slice(0, 8)

const quickItems = [
  { label: '找同城', icon: 'map-pin', action: () => goSearch() },
  { label: '发布约拍', icon: 'plus-circle', action: () => openPublish() },
  { label: '我的申请', icon: 'check', action: () => checkLogin('/pages-sub/ypat/my-apply') },
  { label: '收藏夹', icon: 'star', action: () => checkLogin('/pages-sub/ypat/my-favorite') },
]

const flowItems = [
  { title: '我的发布', desc: '查看发布状态和收到的申请', icon: 'camera', action: () => checkLogin('/pages-sub/ypat/my-publish') },
  { title: '消息中心', desc: '处理收到的约拍与申请动态', icon: 'mail', action: () => openMessage() },
  { title: '实名认证', desc: '完善信用资料，提高合作可信度', icon: 'shield', action: () => checkLogin('/pages-sub/user/realname') },
  { title: '我的拍拍豆', desc: '充值、账单、收支记录统一管理', icon: 'wallet', action: () => checkLogin('/pages-sub/user/wallet') },
]

function goSearch(): void {
  uni.navigateTo({ url: '/pages-sub/ypat/search' })
}

function goMine(): void {
  goRootTab('/pages/mine/index')
}

function searchStyle(style: string): void {
  uni.navigateTo({ url: `/pages-sub/ypat/search?keyword=${encodeURIComponent(style)}` })
}

function checkLogin(url: string): void {
  if (!userStore.isLoggedIn) {
    uni.navigateTo({ url: '/pages/login/index' })
    return
  }
  uni.navigateTo({ url })
}

onShow(() => {
  if (userStore.isLoggedIn) userStore.refreshUnreadCount()
})
</script>

<style scoped lang="scss">

.discover-page {
  min-height: 100vh;
  background: $color-bg-page;
}

.discover-scroll {
  box-sizing: border-box;
  min-height: 100vh;
  padding-right: 32rpx;
  padding-bottom: calc(172rpx + env(safe-area-inset-bottom));
  padding-left: 32rpx;
}

.discover-top {
  @include flex-between;
}

.discover-title {
  display: block;
  color: $color-text-primary;
  font-size: 48rpx;
  font-weight: 900;
}

.discover-subtitle {
  display: block;
  margin-top: 8rpx;
  color: $color-text-secondary;
  font-size: 26rpx;
  font-weight: 700;
}

.discover-action {
  @include flex-center;
  width: 84rpx;
  height: 84rpx;
  border-radius: 50%;
  background: #fff;
  box-shadow: $shadow-keep-card;
}

.hero-card {
  @include flex-between;
  gap: 24rpx;
  margin-top: 34rpx;
  padding: 34rpx;
  border-radius: 36rpx;
  color: #fff;
  background: linear-gradient(118deg, #252A32, #3B3A50);
}

.hero-card__main {
  min-width: 0;
  flex: 1;
}

.hero-card__label,
.hero-card__title,
.hero-card__desc {
  display: block;
}

.hero-card__label {
  color: $color-primary-light;
  font-size: 24rpx;
  font-weight: 900;
}

.hero-card__title {
  margin-top: 14rpx;
  font-size: 36rpx;
  font-weight: 900;
  line-height: 1.3;
}

.hero-card__desc {
  margin-top: 14rpx;
  color: rgba(255, 255, 255, 0.72);
  font-size: 24rpx;
  font-weight: 700;
  line-height: 1.5;
}

.hero-card__button {
  display: flex;
  align-items: center;
  gap: 4rpx;
  flex: none;
  padding: 20rpx 26rpx;
  border-radius: $radius-round;
  color: $color-text-primary;
  background: #FFD56A;
  font-weight: 900;
}

.quick-card {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 18rpx;
  margin-top: 28rpx;
  padding: 26rpx 18rpx;
  border-radius: $radius-keep-card;
  background: #fff;
  box-shadow: $shadow-keep-card;
}

.quick-card__item {
  @include flex-column;
  align-items: center;
  gap: 12rpx;
  color: $color-text-secondary;
  font-size: 24rpx;
  font-weight: 900;
}

.quick-card__icon {
  @include flex-center;
  width: 78rpx;
  height: 78rpx;
  border-radius: 28rpx;
  color: $color-text-primary;
  background: $color-bg-chip;
}

.section-head {
  @include flex-between;
  margin-top: 42rpx;
  margin-bottom: 22rpx;
}

.section-head__title {
  color: $color-text-primary;
  font-size: 38rpx;
  font-weight: 900;
}

.section-head__more {
  color: $color-text-secondary;
  font-size: 26rpx;
  font-weight: 800;
}

.style-scroll {
  width: 100%;
  white-space: nowrap;
}

.style-row {
  display: inline-flex;
  gap: 18rpx;
  padding-right: 32rpx;
}

.style-chip {
  @include keep-chip(false);
  background: #fff;
}

.flow-list {
  display: flex;
  flex-direction: column;
  gap: 18rpx;
}

.flow-card {
  display: flex;
  align-items: center;
  gap: 22rpx;
  padding: 28rpx;
  border-radius: 30rpx;
  background: #fff;
  box-shadow: $shadow-keep-card;
}

.flow-card__icon {
  @include flex-center;
  width: 78rpx;
  height: 78rpx;
  border-radius: 26rpx;
  color: $color-primary-dark;
  background: $color-primary-light;
}

.flow-card__body {
  min-width: 0;
  flex: 1;
}

.flow-card__title,
.flow-card__desc {
  display: block;
}

.flow-card__title {
  color: $color-text-primary;
  font-size: 30rpx;
  font-weight: 900;
}

.flow-card__desc {
  margin-top: 8rpx;
  color: $color-text-secondary;
  font-size: 24rpx;
  font-weight: 700;
}
</style>
