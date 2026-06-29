<template>
  <view class="keep-tabbar">
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
          :size="item.key === 'publish' ? 54 : 44"
          :color="item.key === 'publish' ? '#FFFFFF' : active === item.key ? '#1A1D1F' : '#83888F'"
        />
      </view>
      <text class="keep-tabbar__label">{{ item.label }}</text>
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
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 78rpx;
  box-sizing: border-box;
  min-height: calc(132rpx + env(safe-area-inset-bottom));
  padding-top: 18rpx;
  padding-bottom: env(safe-area-inset-bottom);
  border-top: 1rpx solid $color-border;
  background: rgba(255, 255, 255, 0.96);
}

.keep-tabbar__item {
  position: relative;
  @include flex-column;
  align-items: center;
  justify-content: center;
  width: 132rpx;
  height: 104rpx;
  color: $color-text-helper;
  font-size: 24rpx;
  font-weight: 800;
  line-height: 1.2;
}

.keep-tabbar__icon {
  @include flex-center;
  width: 54rpx;
  height: 54rpx;
}

.keep-tabbar__label {
  margin-top: 6rpx;
}

.keep-tabbar__item--active {
  color: $color-text-primary;
}

.keep-tabbar__item--publish {
  transform: translateY(-24rpx);
  color: #fff;
}

.keep-tabbar__item--publish .keep-tabbar__icon {
  width: 96rpx;
  height: 96rpx;
  border: 8rpx solid #fff;
  border-radius: 50%;
  background: $color-text-primary;
  box-shadow: 0 14rpx 34rpx rgba(27, 30, 35, 0.18);
}

.keep-tabbar__item--publish .keep-tabbar__label {
  margin-top: 8rpx;
  color: $color-text-primary;
}
</style>
