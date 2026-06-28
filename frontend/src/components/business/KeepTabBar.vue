<template>
  <view class="keep-tabbar">
    <view
      v-for="item in items"
      :key="item.key"
      class="keep-tabbar__item"
      :class="{ 'keep-tabbar__item--active': active === item.key }"
      @tap="go(item)"
    >
      <KeepIcon :name="item.icon" :size="item.key === 'publish' ? 46 : 44" />
      <text class="keep-tabbar__label">{{ item.label }}</text>
      <view v-if="item.key === 'discover' && showDiscoverDot" class="keep-tabbar__dot" />
      <view v-if="item.key === 'message' && unreadCount > 0" class="keep-tabbar__badge">
        {{ unreadCount > 9 ? '9+' : unreadCount }}
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import KeepIcon from './KeepIcon.vue'

type TabKey = 'home' | 'discover' | 'publish' | 'message' | 'mine'

type TabItem = {
  key: TabKey
  label: string
  icon: string
  url: string
  tab?: boolean
}

const props = withDefaults(defineProps<{
  active: TabKey
  unreadCount?: number
  showDiscoverDot?: boolean
}>(), {
  unreadCount: 0,
  showDiscoverDot: true,
})

const items: TabItem[] = [
  { key: 'home', label: '广场', icon: 'home', url: '/pages/home/index', tab: true },
  { key: 'discover', label: '发现', icon: 'compass', url: '/pages/discover/index' },
  { key: 'publish', label: '发布', icon: 'plus-circle', url: '/pages/publish/index', tab: true },
  { key: 'message', label: '消息', icon: 'mail', url: '/pages/message/index', tab: true },
  { key: 'mine', label: '我的', icon: 'user', url: '/pages/mine/index', tab: true },
]

function go(item: TabItem): void {
  if (props.active === item.key) return
  if (item.tab) {
    uni.switchTab({ url: item.url })
    return
  }
  uni.navigateTo({ url: item.url })
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
  align-items: flex-start;
  justify-content: space-around;
  height: 112rpx;
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
  min-width: 112rpx;
  color: $color-text-helper;
  font-size: 24rpx;
  font-weight: 800;
  line-height: 1.2;
}

.keep-tabbar__label {
  margin-top: 6rpx;
}

.keep-tabbar__item--active {
  color: $color-text-primary;
}

.keep-tabbar__dot {
  position: absolute;
  top: 2rpx;
  right: 26rpx;
  width: 18rpx;
  height: 18rpx;
  border: 4rpx solid #fff;
  border-radius: 50%;
  background: $color-accent-red;
}

.keep-tabbar__badge {
  position: absolute;
  top: -8rpx;
  right: 14rpx;
  min-width: 34rpx;
  height: 34rpx;
  padding: 0 8rpx;
  border: 4rpx solid #fff;
  border-radius: $radius-round;
  color: #fff;
  background: $color-accent-red;
  font-size: 20rpx;
  font-weight: 900;
  line-height: 34rpx;
  text-align: center;
}
</style>
