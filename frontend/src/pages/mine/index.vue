<template>
  <view class="mine-page">
    <view class="mine-scroll" :style="{ paddingTop: statusBarHeight + 20 + 'px' }">
      <view class="mine-top">
        <view class="mine-top__icon" @tap="goSettings">
          <KeepIcon name="menu" :size="46" />
        </view>
        <view class="mine-top__right">
          <view class="mine-top__icon" @tap="showToast('扫一扫')">
            <KeepIcon name="grid" :size="44" />
          </view>
          <view class="mine-top__mail" @tap="goMessage">
            <KeepIcon name="mail" :size="48" />
            <text v-if="unreadCount > 0" class="mine-top__badge">{{ unreadCount > 9 ? '9' : unreadCount }}</text>
          </view>
        </view>
      </view>

      <view v-if="isLoggedIn" class="profile-head" @tap="goProfile">
        <image class="profile-head__avatar" :src="avatar" mode="aspectFill" />
        <view class="profile-head__main">
          <view class="profile-head__name-row">
            <text class="profile-head__name">{{ userInfo?.nickname || '未设置昵称' }}</text>
            <view v-if="isVerified" class="profile-head__check">
              <KeepIcon name="check" :size="26" color="#23C268" />
            </view>
          </view>
          <text class="profile-head__stats">
            关注 <text class="profile-head__strong">356</text>
            粉丝 <text class="profile-head__strong">2.4k</text>
            约拍 <text class="profile-head__strong">{{ userInfo?.pubtimes || userInfo?.rectimes || 0 }}</text>
          </text>
        </view>
        <KeepIcon name="chevron-right" :size="46" color="#B3B8BE" />
      </view>

      <view v-else class="login-panel">
        <KeepState
          type="login"
          title="点击登录"
          description="登录后查看约拍数据、收藏和钱包。"
          button-text="微信登录"
          @action="goLogin"
        />
      </view>

      <view v-if="isLoggedIn" class="identity-pills">
        <view class="identity-pill">{{ professLabel || '模特' }} <text>Lv.4</text></view>
        <view class="identity-pill">信用分 735 <text>优秀</text></view>
        <view class="identity-pill">
          <text class="identity-pill__plain">已实名</text>
          <KeepIcon name="check" :size="24" color="#1B1E23" :stroke-width="3" />
        </view>
      </view>

      <view class="member-card">
        <view>
          <view class="member-card__title">
            <KeepIcon name="gem" :size="34" color="#FFFFFF" />
            <text>爱去拍 · 信用会员</text>
          </view>
          <text class="member-card__desc">专属曝光 · 优先约拍 · 更多权益</text>
        </view>
        <view class="member-card__button" @tap="showToast('立即开通')">
          <text>立即开通</text>
          <KeepIcon name="chevron-right" :size="28" />
        </view>
      </view>

      <view class="quick-card">
        <view class="quick-card__item" @tap="goPublish">
          <KeepIcon name="camera" :size="52" />
          <text>我的发布</text>
        </view>
        <view class="quick-card__item" @tap="goApply">
          <KeepIcon name="check" :size="52" />
          <text>我的约拍</text>
        </view>
        <view class="quick-card__item" @tap="goFavorite">
          <KeepIcon name="star" :size="52" />
          <text>我的收藏</text>
        </view>
        <view class="quick-card__item" @tap="goWallet">
          <KeepIcon name="wallet" :size="52" />
          <text>钱包</text>
        </view>
      </view>

      <view class="data-tabs">
        <text class="data-tabs__item data-tabs__item--active">数据概览</text>
        <text class="data-tabs__item">约拍记录</text>
        <text class="data-tabs__item">我的作品</text>
      </view>

      <view class="data-card">
        <view class="data-card__top">
          <view>
            <view class="data-card__label">
              <KeepIcon name="chart" :size="34" color="#23C268" />
              <text>累计约拍</text>
            </view>
            <text class="data-card__num">{{ totalYpat }}<text>次</text></text>
          </view>
          <view class="data-card__coin" @tap="goWallet">
            <KeepIcon name="coins" :size="28" color="#FF9F1C" />
            <text>约拍币 {{ userInfo?.ppd || 0 }}</text>
            <KeepIcon name="chevron-right" :size="24" color="#FF9F1C" />
          </view>
        </view>
        <view class="mini-cards">
          <view class="mini-card">
            <view class="mini-card__label">
              <KeepIcon name="star" :size="34" color="#23C268" />
              <text>被收藏</text>
            </view>
            <text class="mini-card__value">{{ userInfo?.coltimes || 0 }}<text>次</text></text>
          </view>
          <view class="mini-card">
            <view class="mini-card__label">
              <KeepIcon name="image" :size="34" color="#23C268" />
              <text>作品</text>
            </view>
            <text class="mini-card__value">128<text>组</text></text>
          </view>
        </view>
      </view>
    </view>

    <KeepTabBar active="mine" :unread-count="unreadCount" />
  </view>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { useUserStore } from '@/stores/user'
import { useAppStore } from '@/stores/app'
import { PROFESS_LABELS } from '@/constants/enums'
import KeepIcon from '@/components/business/KeepIcon.vue'
import KeepState from '@/components/business/KeepState.vue'
import KeepTabBar from '@/components/business/KeepTabBar.vue'
import { goTab } from '@/utils/tab-navigation'

const userStore = useUserStore()
const appStore = useAppStore()

const statusBarHeight = computed(() => appStore.statusBarHeight)
const unreadCount = computed(() => userStore.unreadCount)
const isLoggedIn = computed(() => userStore.isLoggedIn)
const userInfo = computed(() => userStore.userInfo)
const avatar = computed(() => userInfo.value?.imgpath || userInfo.value?.avatarurl || '/static/default-avatar.png')
const isVerified = computed(() => userInfo.value?.realnameflag === '1' || userInfo.value?.status === '2')
const totalYpat = computed(() => (userInfo.value?.pubtimes || 0) + (userInfo.value?.rectimes || 0))
const professLabel = computed(() => {
  const code = userInfo.value?.profess
  return code ? PROFESS_LABELS[code] || '' : ''
})

onShow(() => {
  if (isLoggedIn.value) {
    userStore.updateUserInfo()
    userStore.refreshUnreadCount()
  }
})

function goLogin() { uni.navigateTo({ url: '/pages/login/index' }) }
function goMessage() { goTab('/pages/message/index') }
function goProfile() { uni.navigateTo({ url: '/pages-sub/user/profile' }) }
function goSettings() { uni.navigateTo({ url: '/pages-sub/user/settings' }) }
function goPublish() { checkLogin(() => uni.navigateTo({ url: '/pages-sub/ypat/my-publish' })) }
function goApply() { checkLogin(() => uni.navigateTo({ url: '/pages-sub/ypat/my-apply' })) }
function goFavorite() { checkLogin(() => uni.navigateTo({ url: '/pages-sub/ypat/my-favorite' })) }
function goWallet() { checkLogin(() => uni.navigateTo({ url: '/pages-sub/user/wallet' })) }
function showToast(title: string) { uni.showToast({ title, icon: 'none' }) }

function checkLogin(callback: () => void) {
  if (!isLoggedIn.value) {
    uni.navigateTo({ url: '/pages/login/index' })
    return
  }
  callback()
}
</script>

<style lang="scss">

.mine-page {
  min-height: 100vh;
  background: $color-bg-page;
}

.mine-scroll {
  padding: 0 32rpx calc(172rpx + env(safe-area-inset-bottom));
}

.mine-top {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.mine-top__right {
  display: flex;
  align-items: center;
  gap: 30rpx;
}

.mine-top__icon,
.mine-top__mail {
  position: relative;
  color: $color-text-primary;
}

.mine-top__badge {
  position: absolute;
  top: -10rpx;
  right: -16rpx;
  min-width: 34rpx;
  height: 34rpx;
  padding: 0 8rpx;
  border-radius: $radius-round;
  color: #fff;
  background: $color-accent-red;
  font-size: 20rpx;
  font-weight: 800;
  line-height: 34rpx;
  text-align: center;
}

.profile-head {
  display: flex;
  align-items: center;
  gap: 28rpx;
  margin-top: 40rpx;
}

.profile-head__avatar {
  width: 128rpx;
  height: 128rpx;
  flex: none;
  border-radius: 50%;
  background: $color-bg-chip;
}

.profile-head__main {
  min-width: 0;
  flex: 1;
}

.profile-head__name-row {
  display: flex;
  align-items: center;
  gap: 14rpx;
}

.profile-head__name {
  color: $color-text-primary;
  font-size: 48rpx;
  font-weight: 800;
}

.profile-head__check {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 42rpx;
  height: 42rpx;
  border: 4rpx solid $color-primary;
  border-radius: 50%;
}

.profile-head__stats {
  display: block;
  margin-top: 12rpx;
  color: $color-text-secondary;
  font-size: 28rpx;
  font-weight: 700;
}

.profile-head__strong {
  color: $color-text-primary;
  font-weight: 800;
}

.login-panel {
  margin-top: 36rpx;
  border-radius: $radius-keep-card;
  background: #fff;
}

.identity-pills {
  display: flex;
  flex-wrap: wrap;
  gap: 18rpx;
  margin-top: 36rpx;
}

.identity-pill {
  display: flex;
  align-items: center;
  gap: 10rpx;
  height: 60rpx;
  padding: 0 28rpx;
  border-radius: $radius-round;
  color: $color-text-primary;
  background: #fff;
  font-size: 28rpx;
  font-weight: 800;
  box-shadow: $shadow-keep-card;
}

.identity-pill text {
  color: $color-primary-dark;
  background: $color-primary-light;
  padding: 2rpx 12rpx;
  border-radius: $radius-round;
  font-size: 22rpx;
}

.identity-pill__plain {
  color: $color-text-primary !important;
  background: transparent !important;
  padding: 0 !important;
  font-size: 28rpx !important;
}

.member-card {
  display: flex;
  align-items: center;
  margin-top: 36rpx;
  padding: 34rpx;
  border-radius: 36rpx;
  color: #fff;
  background: linear-gradient(110deg, #2A2D3A, #3C3550);
}

.member-card__title,
.member-card__desc {
  display: block;
}

.member-card__title {
  display: flex;
  align-items: center;
  gap: 12rpx;
  font-size: 34rpx;
  font-weight: 800;
}

.member-card__desc {
  margin-top: 12rpx;
  opacity: 0.82;
  font-size: 26rpx;
  font-weight: 700;
}

.member-card__button {
  display: flex;
  align-items: center;
  gap: 4rpx;
  margin-left: auto;
  padding: 18rpx 30rpx;
  border-radius: $radius-round;
  color: #5A3A00;
  background: linear-gradient(135deg, #FFE08A, #F5B642);
  font-size: 28rpx;
  font-weight: 800;
}

.quick-card {
  display: flex;
  margin-top: 28rpx;
  padding: 32rpx 0;
  border-radius: $radius-keep-card;
  background: #fff;
  box-shadow: $shadow-keep-card;
}

.quick-card__item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12rpx;
  color: $color-text-primary;
  font-size: 26rpx;
  font-weight: 800;
}

.quick-card__item text {
  color: $color-text-secondary;
}

.data-tabs {
  display: flex;
  align-items: baseline;
  gap: 42rpx;
  margin-top: 44rpx;
  padding-left: 12rpx;
}

.data-tabs__item {
  position: relative;
  color: $color-text-helper;
  font-size: 32rpx;
  font-weight: 800;
}

.data-tabs__item--active {
  color: $color-text-primary;
  font-size: 42rpx;
}

.data-tabs__item--active::after {
  position: absolute;
  right: 0;
  bottom: -14rpx;
  left: 0;
  height: 8rpx;
  border-radius: 6rpx;
  background: $color-text-primary;
  content: '';
}

.data-card {
  margin-top: 34rpx;
  padding: 42rpx 36rpx;
  border-radius: $radius-keep-card;
  background: #fff;
  box-shadow: $shadow-keep-card;
}

.data-card__top {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
}

.data-card__label,
.mini-card__label {
  display: flex;
  align-items: center;
  gap: 12rpx;
  color: $color-primary-dark;
  font-size: 28rpx;
  font-weight: 800;
}

.data-card__num {
  display: block;
  margin-top: 14rpx;
  color: $color-text-primary;
  font-size: 86rpx;
  font-weight: 900;
  line-height: 1;
}

.data-card__num text,
.mini-card__value text {
  margin-left: 8rpx;
  color: $color-text-secondary;
  font-size: 28rpx;
  font-weight: 800;
}

.data-card__coin {
  display: flex;
  align-items: center;
  gap: 8rpx;
  padding: 18rpx 28rpx;
  border-radius: $radius-round;
  color: $color-accent-orange;
  background: $color-orange-soft;
  font-size: 28rpx;
  font-weight: 800;
}

.mini-cards {
  display: flex;
  gap: 22rpx;
  margin-top: 36rpx;
}

.mini-card {
  flex: 1;
  padding: 30rpx 28rpx;
  border-radius: 28rpx;
  background: $color-bg-page;
}

.mini-card__value {
  display: block;
  margin-top: 18rpx;
  color: $color-text-primary;
  font-size: 52rpx;
  font-weight: 900;
}

</style>
