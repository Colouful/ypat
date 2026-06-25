<template>
  <view class="page">
    <view class="navbar" :style="{ paddingTop: `${statusBarHeight}px` }">
      <view class="navbar__content">
        <text class="navbar__back" @tap="back">‹</text>
        <text class="navbar__title">约拍详情</text>
        <view class="navbar__placeholder" />
      </view>
    </view>
    <YpatDetailView :id="ypatId" :style="{ paddingTop: `${navHeight}px` }" />
  </view>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { useAppStore } from '@/stores/app'
import YpatDetailView from '@/components/business/YpatDetailView.vue'

const appStore = useAppStore()
const ypatId = ref(0)
const statusBarHeight = appStore.statusBarHeight
const navHeight = appStore.navBarHeight

function back(): void {
  if (getCurrentPages().length > 1) uni.navigateBack()
  else uni.switchTab({ url: '/pages/home/index' })
}

onLoad((query) => {
  ypatId.value = Number(query?.id || 0)
})
</script>

<style scoped lang="scss">
.page { min-height: 100vh; background: #f7f8fa; }
.navbar { position: fixed; z-index: 10; top: 0; left: 0; right: 0; background: rgba(255,255,255,.96); }
.navbar__content { height: 44px; display: flex; align-items: center; justify-content: space-between; padding: 0 24rpx; }
.navbar__back { width: 64rpx; color: #1d2433; font-size: 56rpx; line-height: 1; }
.navbar__title { color: #1d2433; font-size: 31rpx; font-weight: 600; }
.navbar__placeholder { width: 64rpx; }
</style>
