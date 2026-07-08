<template>
  <view class="realname-intro-page">
    <KeepPageNav title="实名认证说明" />

    <view class="hero">
      <view class="hero__copy">
        <text class="hero__eyebrow">身份认证</text>
        <text class="hero__title">实名后，约拍更安心</text>
        <text class="hero__desc">平台会保护你的身份资料，认证结果只用于提升交易可信度和服务权限。</text>
      </view>
      <view class="hero__badge">
        <KeepIcon name="shield" :size="56" color="#17A857" />
      </view>
    </view>

    <view class="benefit-card">
      <view v-for="item in benefits" :key="item.title" class="benefit-card__item">
        <view class="benefit-card__icon">
          <KeepIcon :name="item.icon" :size="34" color="#17A857" />
        </view>
        <view class="benefit-card__body">
          <text class="benefit-card__title">{{ item.title }}</text>
          <text class="benefit-card__desc">{{ item.desc }}</text>
        </view>
      </view>
    </view>

    <view class="privacy-card">
      <text class="privacy-card__title">资料如何使用</text>
      <text class="privacy-card__desc">身份证照片和证件信息仅用于平台审核与风控，不会在个人主页公开展示。</text>
    </view>

    <button class="start-button" @tap="startRealname">
      {{ verified ? '查看认证状态' : '开始实名' }}
    </button>
  </view>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import KeepIcon from '@/components/business/KeepIcon.vue'
import KeepPageNav from '@/components/business/KeepPageNav.vue'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()
const redirect = ref('')
const verified = computed(() => userStore.userInfo?.realnameflag === '1' || userStore.userInfo?.status === '2')

const benefits = [
  { title: '可约拍优质模特', desc: '高信任合作更容易被作者接单。', icon: 'camera' },
  { title: '可使用极速联系', desc: '关键联系方式与服务权限逐步开放。', icon: 'mail' },
  { title: '可刷新约拍动态', desc: '提升曝光机会，让更多同城用户看到你。', icon: 'chart' },
  { title: '实名认证醒目标识', desc: '主页展示认证状态，提高约拍成功率。', icon: 'check' },
]

function startRealname(): void {
  const query = redirect.value ? `?redirect=${encodeURIComponent(redirect.value)}` : ''
  uni.navigateTo({ url: `/pages-sub/user/realname${query}` })
}

onLoad((options) => {
  redirect.value = typeof options?.redirect === 'string' ? options.redirect : ''
})
</script>

<style scoped lang="scss">
.realname-intro-page {
  min-height: 100vh;
  padding: 28rpx 28rpx 48rpx;
  background: $color-bg-page;
}

.hero {
  position: relative;
  display: flex;
  min-height: 260rpx;
  overflow: hidden;
  padding: 34rpx;
  border-radius: 34rpx;
  background: linear-gradient(135deg, #FFFFFF 0%, #E8FAF1 54%, #DDF3FF 100%);
  box-shadow: $shadow-keep-card;
}

.hero__copy {
  position: relative;
  z-index: 1;
  max-width: 470rpx;
}

.hero__eyebrow {
  display: inline-flex;
  padding: 8rpx 18rpx;
  border-radius: $radius-round;
  color: $color-primary-dark;
  background: rgba(35, 194, 104, 0.14);
  font-size: 22rpx;
  font-weight: 900;
}

.hero__title {
  display: block;
  margin-top: 24rpx;
  color: $color-text-primary;
  font-size: 48rpx;
  font-weight: 900;
}

.hero__desc {
  display: block;
  margin-top: 16rpx;
  color: $color-text-secondary;
  font-size: 26rpx;
  font-weight: 700;
  line-height: 1.55;
}

.hero__badge {
  position: absolute;
  right: 34rpx;
  bottom: 34rpx;
  @include flex-center;
  width: 118rpx;
  height: 118rpx;
  border-radius: 36rpx;
  background: #FFFFFF;
  box-shadow: 0 20rpx 44rpx rgba(23, 168, 87, 0.16);
}

.benefit-card,
.privacy-card {
  margin-top: 24rpx;
  border-radius: 30rpx;
  background: #FFFFFF;
  box-shadow: $shadow-keep-card;
}

.benefit-card {
  padding: 8rpx 28rpx;
}

.benefit-card__item {
  display: flex;
  gap: 18rpx;
  padding: 24rpx 0;
  border-bottom: 1rpx solid $color-border;
}

.benefit-card__item:last-child {
  border-bottom: 0;
}

.benefit-card__icon {
  @include flex-center;
  width: 72rpx;
  height: 72rpx;
  flex: none;
  border-radius: 24rpx;
  background: $color-primary-soft;
}

.benefit-card__body {
  min-width: 0;
  flex: 1;
}

.benefit-card__title {
  display: block;
  color: $color-text-primary;
  font-size: 30rpx;
  font-weight: 900;
}

.benefit-card__desc,
.privacy-card__desc {
  display: block;
  margin-top: 8rpx;
  color: $color-text-secondary;
  font-size: 24rpx;
  line-height: 1.55;
}

.privacy-card {
  padding: 28rpx;
}

.privacy-card__title {
  color: $color-text-primary;
  font-size: 30rpx;
  font-weight: 900;
}

.start-button {
  height: 94rpx;
  margin-top: 36rpx;
  border-radius: $radius-round;
  color: #FFFFFF;
  background: $color-primary;
  font-size: 31rpx;
  font-weight: 900;
  line-height: 94rpx;
  box-shadow: $shadow-keep-button;
}

.start-button::after {
  border: 0;
}
</style>
