<template>
  <view class="keep-page-nav">
    <view class="keep-page-nav__bar" :style="{ paddingTop: `${statusBarHeight}px` }">
      <view class="keep-page-nav__content">
        <view class="keep-page-nav__back" @tap="goBackOrHome">
          <KeepIcon name="chevron-left" :size="42" color="#1A1D1F" />
        </view>
        <text class="keep-page-nav__title">{{ title }}</text>
        <view class="keep-page-nav__placeholder" />
      </view>
    </view>
    <view class="keep-page-nav__spacer" :style="{ height: `${navBarHeight}px` }" />
  </view>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useAppStore } from '@/stores/app'
import { goBackOrHome } from '@/utils/tab-navigation'
import KeepIcon from './KeepIcon.vue'

defineProps<{
  title: string
}>()

const appStore = useAppStore()
const statusBarHeight = computed(() => appStore.statusBarHeight)
const navBarHeight = computed(() => Math.max(appStore.navBarHeight, appStore.statusBarHeight + 48))
</script>

<style scoped lang="scss">
.keep-page-nav__bar {
  position: fixed;
  z-index: $z-index-navbar;
  top: 0;
  right: 0;
  left: 0;
  background: rgba(245, 246, 248, 0.96);
}

.keep-page-nav__content {
  display: flex;
  align-items: center;
  height: 96rpx;
  padding: 0 28rpx;
}

.keep-page-nav__back,
.keep-page-nav__placeholder {
  @include flex-center;
  width: 72rpx;
  height: 72rpx;
  flex: none;
}

.keep-page-nav__back {
  border-radius: 50%;
  background: $color-bg-card;
  box-shadow: $shadow-keep-card;
}

.keep-page-nav__title {
  flex: 1;
  color: $color-text-primary;
  font-size: 34rpx;
  font-weight: 900;
  text-align: center;
}
</style>
