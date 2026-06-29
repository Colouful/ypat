<template>
  <view class="keep-tabbar">
    <view class="keep-tabbar__shell" />
    <view class="keep-tabbar__notch" />
    <view class="keep-tabbar__items">
      <view
        v-for="item in items"
        :key="item.key"
        class="keep-tabbar__item"
        :class="{
          'keep-tabbar__item--active': active === item.key,
          'keep-tabbar__item--publish': item.key === 'publish',
        }"
        @tap="go(item)"
      >
        <view class="keep-tabbar__icon">
          <KeepIcon
            :name="item.icon"
            :size="item.key === 'publish' ? 58 : 48"
            color="#FFFFFF"
          />
        </view>
        <text class="keep-tabbar__label">{{ item.label }}</text>
        <view v-if="active === item.key && item.key !== 'publish'" class="keep-tabbar__dot" />
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import KeepIcon from './KeepIcon.vue'
import { goRootTab, openPublish, type RootTabUrl } from '@/utils/tab-navigation'

type TabKey = 'home' | 'publish' | 'mine'

type TabItem = {
  key: TabKey
  label: string
  icon: string
  url?: RootTabUrl
}

const props = withDefaults(defineProps<{
  active: TabKey
}>(), {})

const items: TabItem[] = [
  { key: 'home', label: '首页', icon: 'home', url: '/pages/home/index' },
  { key: 'publish', label: '发布', icon: 'plus-circle' },
  { key: 'mine', label: '我的', icon: 'user', url: '/pages/mine/index' },
]

function go(item: TabItem): void {
  if (item.key === 'publish') {
    openPublish()
    return
  }
  if (props.active === item.key || !item.url) return
  goRootTab(item.url)
}
</script>

<style scoped lang="scss">

.keep-tabbar {
  position: fixed;
  z-index: $z-index-navbar;
  right: 0;
  bottom: 0;
  left: 0;
  box-sizing: border-box;
  height: calc(156rpx + env(safe-area-inset-bottom));
  padding-bottom: env(safe-area-inset-bottom);
  background: transparent;
}

.keep-tabbar__shell {
  position: absolute;
  right: 0;
  bottom: 0;
  left: 0;
  height: calc(118rpx + env(safe-area-inset-bottom));
  border-radius: 46rpx 46rpx 0 0;
  background: #ff2d3d;
  box-shadow: 0 -14rpx 34rpx rgba(255, 45, 61, 0.18);
}

.keep-tabbar__notch {
  position: absolute;
  z-index: 1;
  top: 4rpx;
  left: 50%;
  width: 150rpx;
  height: 150rpx;
  border-radius: 50%;
  background: #fff;
  transform: translateX(-50%);
}

.keep-tabbar__items {
  position: relative;
  z-index: 2;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 102rpx;
  box-sizing: border-box;
  height: 156rpx;
  padding-top: 28rpx;
}

.keep-tabbar__item {
  position: relative;
  @include flex-column;
  align-items: center;
  justify-content: center;
  width: 118rpx;
  height: 92rpx;
  color: #fff;
  font-size: 0;
  font-weight: 800;
  line-height: 1.2;
}

.keep-tabbar__icon {
  @include flex-center;
  width: 54rpx;
  height: 54rpx;
}

.keep-tabbar__label {
  width: 0;
  height: 0;
  overflow: hidden;
  font-size: 0;
}

.keep-tabbar__item--active {
  color: #fff;
}

.keep-tabbar__dot {
  position: absolute;
  bottom: 0;
  left: 50%;
  width: 8rpx;
  height: 8rpx;
  border-radius: 50%;
  background: #fff;
  transform: translateX(-50%);
}

.keep-tabbar__item--publish {
  width: 132rpx;
  height: 132rpx;
  transform: translateY(-44rpx);
  color: #fff;
}

.keep-tabbar__item--publish .keep-tabbar__icon {
  width: 112rpx;
  height: 112rpx;
  border: 8rpx solid #fff;
  border-radius: 50%;
  background: #ff2d3d;
  box-shadow: 0 18rpx 36rpx rgba(255, 45, 61, 0.28);
}

.keep-tabbar__item--publish .keep-tabbar__label {
  display: none;
}
</style>
