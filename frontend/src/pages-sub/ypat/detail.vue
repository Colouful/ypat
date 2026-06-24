<template>
  <view class="detail-page">
    <!-- 自定义导航栏 -->
    <view class="navbar" :style="{ paddingTop: statusBarHeight + 'px' }">
      <view class="navbar__content">
        <view class="navbar__back" @tap="handleBack">
          <text class="navbar__back-icon">‹</text>
        </view>
        <text class="navbar__title">约拍详情</text>
        <view class="navbar__share" @tap="handleShare">
          <text class="navbar__share-icon">⊕</text>
        </view>
      </view>
    </view>

    <!-- 加载骨架屏 -->
    <view v-if="loading" class="skeleton" :style="{ paddingTop: navHeight + 'px' }">
      <view class="skeleton__swiper" />
      <view class="skeleton__user">
        <view class="skeleton__avatar" />
        <view class="skeleton__lines">
          <view class="skeleton__line skeleton__line--name" />
          <view class="skeleton__line skeleton__line--sub" />
        </view>
      </view>
      <view class="skeleton__content">
        <view class="skeleton__line skeleton__line--full" />
        <view class="skeleton__line skeleton__line--full" />
        <view class="skeleton__line skeleton__line--half" />
      </view>
    </view>

    <!-- 错误状态 -->
    <view v-else-if="error" class="error-state" :style="{ paddingTop: navHeight + 'px' }">
      <text class="error-state__text">加载失败</text>
      <text class="error-state__sub">{{ errorMsg }}</text>
      <view class="error-state__btn" @tap="loadDetail">
        <text class="error-state__btn-text">重新加载</text>
      </view>
    </view>

    <!-- 详情内容 -->
    <view v-else-if="detail" class="detail-content" :style="{ paddingTop: navHeight + 'px' }">
      <!-- 图片轮播 -->
      <view class="swiper-section">
        <swiper
          class="image-swiper"
          circular
          :indicator-dots="imageList.length > 1"
          indicator-color="rgba(255,255,255,0.4)"
          indicator-active-color="#FFFFFF"
          @change="onSwiperChange"
        >
          <swiper-item v-for="(img, index) in imageList" :key="index">
            <image class="swiper-image" :src="img" mode="aspectFill" @tap="previewImage(index)" />
          </swiper-item>
        </swiper>
        <view v-if="imageList.length > 1" class="swiper-counter">
          <text class="swiper-counter__text">{{ currentSwiperIndex + 1 }}/{{ imageList.length }}</text>
        </view>
      </view>

      <!-- 发布者信息 -->
      <view class="publisher-section">
        <image class="publisher-section__avatar" :src="detail.userQo?.imgpath || '/static/default-avatar.png'" mode="aspectFill" />
        <view class="publisher-section__info">
          <view class="publisher-section__name-row">
            <text class="publisher-section__nickname">{{ detail.userQo?.nickname || '匿名用户' }}</text>
            <view v-if="detail.userQo?.realnameflag" class="publisher-section__badge">
              <text class="publisher-section__badge-text">已认证</text>
            </view>
          </view>
          <text class="publisher-section__profess">{{ getProfessLabel(detail.userQo?.profess) }}</text>
        </view>
      </view>

      <!-- 标题 -->
      <view class="title-section">
        <text class="title-section__text">{{ detail.title }}</text>
      </view>

      <!-- 描述 -->
      <view class="desc-section">
        <text class="desc-section__text">{{ detail.describ }}</text>
      </view>

      <!-- 信息列表 -->
      <view class="info-section">
        <view class="info-item">
          <text class="info-item__label">发布时间</text>
          <text class="info-item__value">{{ detail.timeStr || formatDate(detail.pubdate) }}</text>
        </view>
        <view class="info-item">
          <text class="info-item__label">拍摄地点</text>
          <text class="info-item__value">{{ detail.city }}{{ detail.area ? ' ' + detail.area : '' }}</text>
        </view>
        <view class="info-item">
          <text class="info-item__label">收费方式</text>
          <text class="info-item__value">{{ detail.chargewayTxt || getChargeWayLabel(detail.chargeway) }}</text>
        </view>
        <view v-if="detail.chargeamt > 0" class="info-item">
          <text class="info-item__label">参考价格</text>
          <text class="info-item__value info-item__value--price">¥{{ detail.chargeamt }}</text>
        </view>
        <view class="info-item">
          <text class="info-item__label">浏览量</text>
          <text class="info-item__value">{{ detail.readtimes || 0 }}</text>
        </view>
      </view>

      <!-- 底部占位 -->
      <view class="bottom-placeholder" />
    </view>

    <!-- 固定底部操作栏 -->
    <view v-if="detail && !loading && !error" class="action-bar">
      <view class="action-bar__content">
        <view class="action-bar__favorite" @tap="handleFavorite">
          <text class="action-bar__favorite-icon" :class="{ 'action-bar__favorite-icon--active': isFavorited }">{{ isFavorited ? '★' : '☆' }}</text>
          <text class="action-bar__favorite-text" :class="{ 'action-bar__favorite-text--active': isFavorited }">{{ isFavorited ? '已收藏' : '收藏' }}</text>
        </view>
        <view class="action-bar__apply" @tap="handleApply">
          <text class="action-bar__apply-text">我要报名</text>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { useUserStore } from '@/stores/user'
import { useAppStore } from '@/stores/app'
import * as ypatApi from '@/api/modules/ypat'
import { PROFESS_LABELS, CHARGE_WAY_LABELS } from '@/constants/enums'
import type { YpatInfo } from '@/api/types'

const userStore = useUserStore()
const appStore = useAppStore()

const statusBarHeight = computed(() => appStore.statusBarHeight)
const navHeight = computed(() => appStore.navBarHeight)

const loading = ref(true)
const error = ref(false)
const errorMsg = ref('')
const detail = ref<YpatInfo | null>(null)
const isFavorited = ref(false)
const currentSwiperIndex = ref(0)
const ypatId = ref<number>(0)

const imageList = computed<string[]>(() => {
  if (!detail.value || !detail.value.pics) return []
  return detail.value.pics.filter((img) => img.trim() !== '')
})

function getProfessLabel(code?: string): string {
  if (!code) return ''
  return PROFESS_LABELS[code] || ''
}

function getChargeWayLabel(type?: string): string {
  if (type === undefined || type === null) return ''
  return CHARGE_WAY_LABELS[type] || ''
}

function formatDate(dateStr?: string): string {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}

function onSwiperChange(e: any) {
  currentSwiperIndex.value = e.detail.current
}

function previewImage(index: number) {
  uni.previewImage({
    current: index,
    urls: imageList.value,
  })
}

function handleBack() {
  const pages = getCurrentPages()
  if (pages.length > 1) {
    uni.navigateBack()
  } else {
    uni.switchTab({ url: '/pages/home/index' })
  }
}

function handleShare() {
  uni.showShareMenu({
    withShareTicket: true,
    menus: ['shareAppMessage', 'shareTimeline'],
  })
}

async function loadDetail() {
  if (!ypatId.value) return
  loading.value = true
  error.value = false
  errorMsg.value = ''

  try {
    const userId = userStore.userInfo?.id ? Number(userStore.userInfo.id) : undefined
    const res = await ypatApi.getDetail(ypatId.value, userId)
    if (res.success && res.data) {
      detail.value = res.data
      ypatApi.addReadCount(ypatId.value)
    } else {
      error.value = true
      errorMsg.value = res.message || '获取详情失败'
    }
  } catch (e: any) {
    error.value = true
    errorMsg.value = e?.message || '网络请求失败'
  } finally {
    loading.value = false
  }
}

async function handleFavorite() {
  if (!userStore.isLoggedIn) {
    uni.navigateTo({ url: '/pages/login/index' })
    return
  }
  if (!detail.value) return

  try {
    const userId = Number(userStore.userInfo!.id)
    const res = await ypatApi.addFavorite(userId, detail.value.id)
    if (res.success) {
      isFavorited.value = !isFavorited.value
      uni.showToast({
        title: isFavorited.value ? '收藏成功' : '已取消收藏',
        icon: 'none',
      })
    } else {
      uni.showToast({ title: res.message || '操作失败', icon: 'none' })
    }
  } catch {
    uni.showToast({ title: '网络异常，请重试', icon: 'none' })
  }
}

async function handleApply() {
  if (!userStore.isLoggedIn) {
    uni.navigateTo({ url: '/pages/login/index' })
    return
  }
  if (!detail.value) return

  const userId = Number(userStore.userInfo!.id)

  if (detail.value.userid === userId) {
    uni.showToast({ title: '不能报名自己发布的约拍', icon: 'none' })
    return
  }

  uni.showModal({
    title: '确认报名',
    content: '确定要报名该约拍吗？',
    confirmColor: '#23C268',
    success: async (modalRes) => {
      if (modalRes.confirm) {
        try {
          const res = await ypatApi.applyYpat({
            sendperid: userId,
            recperid: detail.value!.userid,
            ypatid: detail.value!.id,
            content: '我想报名参加您的约拍',
          })
          if (res.success) {
            uni.showToast({ title: '报名成功', icon: 'success' })
          } else {
            uni.showToast({ title: res.message || '报名失败', icon: 'none' })
          }
        } catch {
          uni.showToast({ title: '网络异常，请重试', icon: 'none' })
        }
      }
    },
  })
}

onLoad((query) => {
  if (query?.id) {
    ypatId.value = Number(query.id)
    loadDetail()
  } else {
    error.value = true
    errorMsg.value = '缺少约拍ID'
    loading.value = false
  }
})
</script>

<style lang="scss">
@import '@/styles/tokens.scss';

.detail-page {
  min-height: 100vh;
  background-color: $color-bg-page;
}

.navbar {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: $z-index-navbar;
  background-color: #fff;
  box-shadow: $shadow-sm;

  &__content {
    display: flex;
    align-items: center;
    justify-content: space-between;
    height: 88rpx;
    padding: 0 $spacing-md;
  }

  &__back {
    width: 64rpx;
    height: 64rpx;
    display: flex;
    align-items: center;
    justify-content: center;
  }

  &__back-icon {
    font-size: 48rpx;
    color: $color-text-primary;
    font-weight: $font-weight-bold;
  }

  &__title {
    font-size: $font-size-lg;
    font-weight: $font-weight-semibold;
    color: $color-text-primary;
  }

  &__share {
    width: 64rpx;
    height: 64rpx;
    display: flex;
    align-items: center;
    justify-content: center;
  }

  &__share-icon {
    font-size: 40rpx;
    color: $color-text-primary;
  }
}

// 骨架屏
.skeleton {
  padding: 0;

  &__swiper {
    width: 100%;
    height: 600rpx;
    background: linear-gradient(90deg, #f0f0f0 25%, #e0e0e0 50%, #f0f0f0 75%);
    background-size: 200% 100%;
    animation: shimmer 1.5s infinite;
  }

  &__user {
    display: flex;
    align-items: center;
    padding: $spacing-lg $spacing-md;
  }

  &__avatar {
    width: 80rpx;
    height: 80rpx;
    border-radius: 50%;
    background: linear-gradient(90deg, #f0f0f0 25%, #e0e0e0 50%, #f0f0f0 75%);
    background-size: 200% 100%;
    animation: shimmer 1.5s infinite;
    margin-right: $spacing-md;
  }

  &__lines {
    flex: 1;
  }

  &__line {
    height: 28rpx;
    background: linear-gradient(90deg, #f0f0f0 25%, #e0e0e0 50%, #f0f0f0 75%);
    background-size: 200% 100%;
    animation: shimmer 1.5s infinite;
    border-radius: 4rpx;
    margin-bottom: $spacing-sm;

    &--name { width: 40%; }
    &--sub { width: 25%; }
    &--full { width: 100%; }
    &--half { width: 60%; }
  }

  &__content {
    padding: 0 $spacing-md;
  }
}

@keyframes shimmer {
  0% { background-position: -200% 0; }
  100% { background-position: 200% 0; }
}

// 错误状态
.error-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 200rpx $spacing-xl;

  &__text {
    font-size: $font-size-lg;
    font-weight: $font-weight-medium;
    color: $color-text-primary;
    margin-bottom: $spacing-sm;
  }

  &__sub {
    font-size: $font-size-sm;
    color: $color-text-helper;
    margin-bottom: $spacing-xl;
  }

  &__btn {
    padding: $spacing-sm $spacing-xl;
    background-color: $color-primary;
    border-radius: $radius-round;
  }

  &__btn-text {
    font-size: $font-size-base;
    color: #fff;
  }
}

// 图片轮播
.swiper-section {
  position: relative;
}

.image-swiper {
  width: 100%;
  height: 600rpx;
}

.swiper-image {
  width: 100%;
  height: 100%;
}

.swiper-counter {
  position: absolute;
  right: $spacing-md;
  bottom: $spacing-md;
  background-color: rgba(0, 0, 0, 0.5);
  border-radius: $radius-round;
  padding: 4rpx 16rpx;

  &__text {
    font-size: $font-size-xs;
    color: #fff;
  }
}

// 发布者信息
.publisher-section {
  display: flex;
  align-items: center;
  padding: $spacing-lg $spacing-md;
  background-color: $color-bg-card;

  &__avatar {
    width: 80rpx;
    height: 80rpx;
    border-radius: 50%;
    margin-right: $spacing-md;
  }

  &__info {
    flex: 1;
  }

  &__name-row {
    display: flex;
    align-items: center;
    margin-bottom: 4rpx;
  }

  &__nickname {
    font-size: $font-size-lg;
    font-weight: $font-weight-medium;
    color: $color-text-primary;
    margin-right: $spacing-sm;
  }

  &__badge {
    background-color: $color-primary-light;
    border-radius: $radius-sm;
    padding: 2rpx 10rpx;
  }

  &__badge-text {
    font-size: $font-size-xs;
    color: $color-primary;
  }

  &__profess {
    font-size: $font-size-sm;
    color: $color-text-secondary;
  }
}

// 标题
.title-section {
  padding: $spacing-lg $spacing-md 0;
  background-color: $color-bg-card;

  &__text {
    font-size: $font-size-xl;
    font-weight: $font-weight-semibold;
    color: $color-text-primary;
    line-height: 1.5;
  }
}

// 描述
.desc-section {
  padding: $spacing-md;
  background-color: $color-bg-card;
  margin-bottom: $spacing-md;

  &__text {
    font-size: $font-size-base;
    color: $color-text-primary;
    line-height: 1.8;
    white-space: pre-wrap;
    word-break: break-all;
  }
}

// 信息列表
.info-section {
  background-color: $color-bg-card;
  padding: $spacing-md;
}

.info-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: $spacing-md 0;
  border-bottom: 1rpx solid $color-divider;

  &:last-child {
    border-bottom: none;
  }

  &__label {
    font-size: $font-size-base;
    color: $color-text-secondary;
  }

  &__value {
    font-size: $font-size-base;
    color: $color-text-primary;
    font-weight: $font-weight-medium;

    &--price {
      color: $color-accent-orange;
      font-weight: $font-weight-semibold;
    }
  }
}

// 底部占位
.bottom-placeholder {
  height: 160rpx;
}

// 固定底部操作栏
.action-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  z-index: $z-index-navbar;
  background-color: #fff;
  box-shadow: 0 -2rpx 12rpx rgba(0, 0, 0, 0.06);
  padding-bottom: env(safe-area-inset-bottom);

  &__content {
    display: flex;
    align-items: center;
    padding: $spacing-md $spacing-lg;
  }

  &__favorite {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    width: 120rpx;
    margin-right: $spacing-lg;
  }

  &__favorite-icon {
    font-size: 44rpx;
    color: $color-text-helper;

    &--active {
      color: $color-accent-orange;
    }
  }

  &__favorite-text {
    font-size: $font-size-xs;
    color: $color-text-secondary;
    margin-top: 4rpx;

    &--active {
      color: $color-accent-orange;
    }
  }

  &__apply {
    flex: 1;
    height: 88rpx;
    display: flex;
    align-items: center;
    justify-content: center;
    background-color: $color-primary;
    border-radius: $radius-round;
  }

  &__apply-text {
    font-size: $font-size-lg;
    font-weight: $font-weight-semibold;
    color: #fff;
  }
}
</style>
