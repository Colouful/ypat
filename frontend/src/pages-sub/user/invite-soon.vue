<template>
  <view class="page">
    <KeepPageNav title="好友邀请" />
    <view class="hero">
      <view class="hero__icon">
        <KeepIcon name="users" :size="80" color="#fff" />
      </view>
      <text class="hero__title">邀请好友 共拍同框</text>
      <text class="hero__subtitle">奖励规则与邀请记录正在迁移，敬请期待</text>
    </view>

    <view class="card">
      <text class="card__title">即将上线</text>
      <view class="list">
        <view class="list__row">
          <view class="list__dot" />
          <text class="list__text">专属邀请码（替代手机号分享，避免泄露）</text>
        </view>
        <view class="list__row">
          <view class="list__dot" />
          <text class="list__text">好友注册即得拍拍豆奖励，规则由服务端配置</text>
        </view>
        <view class="list__row">
          <view class="list__dot" />
          <text class="list__text">分享落地页、邀请记录、奖励到账明细</text>
        </view>
      </view>
    </view>

    <view class="card card--note">
      <text class="card__note">注：旧版的"邀请 1 人 +3 拍拍豆"逻辑由后端 `UserService.register` 在 `recmobile` 命中时执行；新版前端已在登录入口承接邀请参数，待邀请码体系上线后将提供完整的邀请记录入口。</text>
    </view>

    <button class="back" @tap="goBack">返回我的</button>
  </view>
</template>

<script setup lang="ts">
import KeepPageNav from '@/components/business/KeepPageNav.vue'
import KeepIcon from '@/components/business/KeepIcon.vue'

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

.hero {
  margin: 36rpx 0 40rpx;
  padding: 60rpx 28rpx;
  border-radius: $radius-keep-card;
  background: linear-gradient(135deg, $color-primary, $color-primary-dark);
  text-align: center;
}

.hero__icon {
  @include flex-center;
  width: 144rpx;
  height: 144rpx;
  margin: 0 auto 28rpx;
  border-radius: 48rpx;
  background: rgba(255, 255, 255, 0.18);
}

.hero__title {
  display: block;
  color: #fff;
  font-size: 42rpx;
  font-weight: 900;
}

.hero__subtitle {
  display: block;
  margin-top: 14rpx;
  color: rgba(255, 255, 255, 0.85);
  font-size: 26rpx;
}

.card {
  margin-top: 24rpx;
  padding: 30rpx;
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

.list {
  margin-top: 22rpx;
}

.list__row {
  display: flex;
  align-items: flex-start;
  gap: 16rpx;
  padding: 14rpx 0;
}

.list__dot {
  margin-top: 14rpx;
  width: 12rpx;
  height: 12rpx;
  border-radius: 50%;
  background: $color-primary;
}

.list__text {
  flex: 1;
  color: $color-text-secondary;
  font-size: 26rpx;
  line-height: 1.6;
}

.card--note {
  background: $color-bg-chip;
}

.card__note {
  display: block;
  color: $color-text-helper;
  font-size: 24rpx;
  line-height: 1.7;
}

.back {
  margin-top: 40rpx;
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
