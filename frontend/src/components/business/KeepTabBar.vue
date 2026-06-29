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
  height: 140rpx;
  background: transparent;
}

.keep-tabbar__shell {
  position: absolute;
  right: 20rpx;
  bottom: 8rpx;
  left: 20rpx;
  height: 108rpx;
  border-radius: 54rpx;
  background: $color-primary;
  box-shadow: 0 20rpx 42rpx rgba(20, 24, 31, 0.22);
}

.keep-tabbar__notch {
  position: absolute;
  z-index: 1;
  top: -4rpx;
  left: 50%;
  width: 146rpx;
  height: 146rpx;
  border-radius: 50%;
  background: #fff;
  box-shadow: 0 8rpx 26rpx rgba(35, 194, 104, 0.18);
  transform: translateX(-50%);
}

.keep-tabbar__items {
  position: relative;
  z-index: 2;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 116rpx;
  box-sizing: border-box;
  height: 140rpx;
  padding-top: 38rpx;
}

.keep-tabbar__item {
  position: relative;
  @include flex-column;
  align-items: center;
  justify-content: center;
  width: 118rpx;
  height: 86rpx;
  color: #fff;
  font-size: 22rpx;
  font-weight: 800;
  line-height: 1.2;
}

.keep-tabbar__icon {
  @include flex-center;
  width: 54rpx;
  height: 54rpx;
}

.keep-tabbar__label {
  display: block;
  margin-top: 6rpx;
  color: #fff;
  font-size: 22rpx;
  font-weight: 800;
  line-height: 1;
}

.keep-tabbar__item--active {
  color: #fff;
}

.keep-tabbar__dot {
  position: absolute;
  bottom: -8rpx;
  left: 50%;
  width: 8rpx;
  height: 8rpx;
  border-radius: 50%;
  background: #fff;
  transform: translateX(-50%);
}

.keep-tabbar__item--publish {
  width: 126rpx;
  height: 126rpx;
  transform: translateY(-48rpx);
  color: #fff;
}

.keep-tabbar__item--publish .keep-tabbar__icon {
  width: 100rpx;
  height: 100rpx;
  border: 10rpx solid #fff;
  border-radius: 50%;
  background: $color-primary;
  box-shadow: 0 14rpx 28rpx rgba(35, 194, 104, 0.24);
}

.keep-tabbar__item--publish .keep-tabbar__label {
  display: none;
}
</style>
