<template>
  <view class="page">
    <KeepPageNav title="信息审核" />
    <view class="card">
      <text class="card__title">管理员入口</text>
      <text class="card__desc">
        旧版信息审核功能依赖 `/ypat/audit/list` 与 `/ypat/audit`，将随管理后台模块在切片 2 之后接入。
      </text>
    </view>
    <button class="back" @tap="goBack">返回我的</button>
  </view>
</template>

<script setup lang="ts">
import KeepPageNav from '@/components/business/KeepPageNav.vue'

function goBack(): void {
  const pages = getCurrentPages()
  if (pages.length > 1) uni.navigateBack()
  else uni.switchTab({ url: '/pages/mine/index' })
}
</script>

<style scoped lang="scss">
.page {
  min-height: 100vh;
  padding: 28rpx 28rpx calc(60rpx + env(safe-area-inset-bottom));
  background: $color-bg-page;
}

.card {
  margin: 36rpx 0;
  padding: 36rpx;
  border-radius: $radius-keep-card;
  background: #fff;
  box-shadow: $shadow-keep-card;
}

.card__title {
  display: block;
  color: $color-text-primary;
  font-size: 32rpx;
  font-weight: 900;
}

.card__desc {
  display: block;
  margin-top: 18rpx;
  color: $color-text-secondary;
  font-size: 26rpx;
  line-height: 1.7;
}

.back {
  margin-top: 28rpx;
  height: 92rpx;
  border-radius: 999rpx;
  color: #fff;
  background: $color-text-primary;
  font-size: 30rpx;
  font-weight: 800;
  line-height: 92rpx;
}

.back::after {
  border: 0;
}
</style>
