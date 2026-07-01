<template>
  <view class="keep-tabbar">
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
            :size="item.key === 'publish' ? 54 : 46"
            :color="active === item.key ? activeColor : inactiveColor"
          />
        </view>
        <text class="keep-tabbar__label">{{ item.label }}</text>
        <view v-if="item.dot" class="keep-tabbar__dot" />
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import KeepIcon from './KeepIcon.vue'
import { goRootTab, openPublish, type RootTabUrl } from '@/utils/tab-navigation'

type TabKey = 'home' | 'discover' | 'publish' | 'message' | 'mine'

type TabItem = {
  key: 'home' | 'publish' | 'mine'
  label: string
  icon: string
  url?: RootTabUrl
  dot?: boolean
}

const props = withDefaults(defineProps<{
  active: TabKey
}>(), {})

const items: TabItem[] = [
  { key: 'home', label: '首页', icon: 'home', url: '/pages/home/index' },
  { key: 'publish', label: '发布', icon: 'plus-circle' },
  { key: 'mine', label: '我的', icon: 'user', url: '/pages/mine/index' },
]

const activeColor = '#1A1D1F'
const inactiveColor = '#B3B8BE'

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
  height: calc(148rpx + env(safe-area-inset-bottom));
  border-top: 1rpx solid $color-border;
  background: rgba(255, 255, 255, 0.98);
  box-shadow: 0 -10rpx 28rpx rgba(20, 24, 31, 0.06);
  backdrop-filter: blur(24rpx);
}

.keep-tabbar__items {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  box-sizing: border-box;
  height: 100%;
  padding-top: 10rpx;
  padding-bottom: calc(12rpx + env(safe-area-inset-bottom));
}

.keep-tabbar__item {
  position: relative;
  @include flex-column;
  align-items: center;
  justify-content: center;
  flex: 1;
  min-width: 0;
  height: 112rpx;
  color: $color-text-helper;
  font-size: 22rpx;
  font-weight: 700;
  line-height: 1.2;
  transition: color 0.18s ease;
}

.keep-tabbar__icon {
  @include flex-center;
  width: 54rpx;
  height: 54rpx;
  transition: transform 0.18s ease;
}

.keep-tabbar__label {
  display: block;
  margin-top: 6rpx;
  color: currentColor;
  font-size: 22rpx;
  font-weight: 700;
  line-height: 1;
}

.keep-tabbar__item--active {
  color: $color-text-primary;
  font-weight: 900;
}

.keep-tabbar__item--active .keep-tabbar__icon {
  transform: translateY(-2rpx);
}

.keep-tabbar__item--active .keep-tabbar__label {
  font-weight: 900;
}

.keep-tabbar__dot {
  position: absolute;
  top: 12rpx;
  right: 50%;
  width: 10rpx;
  height: 10rpx;
  border-radius: 50%;
  background: $color-accent-red;
  transform: translateX(24rpx);
}

.keep-tabbar__item--publish .keep-tabbar__icon {
  width: 60rpx;
  height: 60rpx;
}
</style>
