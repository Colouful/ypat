<template>
  <view class="detail-page">
    <KeepState v-if="loading" type="loading" title="加载中..." />
    <KeepState v-else-if="errorMessage" type="error" :title="errorMessage" button-text="重新加载" @action="load" />
    <template v-else-if="detail">
      <view class="detail-topbar">
        <view class="detail-icon-button" @tap="back">
          <KeepIcon name="chevron-left" :size="42" />
        </view>
        <view class="detail-icon-button" @tap="share">
          <KeepIcon name="compass" :size="38" />
        </view>
      </view>

      <view class="hero-wrap">
        <swiper class="hero-swiper" circular :indicator-dots="images.length > 1">
          <swiper-item v-for="(image, index) in images" :key="image">
            <image :src="image" mode="aspectFill" @tap="preview(index)" />
          </swiper-item>
        </swiper>
        <view class="hero-count">1 / {{ images.length || 1 }}</view>
      </view>

      <view class="detail-sheet">
        <view class="detail-tags">
          <text class="detail-tag detail-tag--green">{{ detail.targetTxt || targetLabel }}</text>
          <text class="detail-tag detail-tag--green">{{ detail.chargewayTxt || detail.chargeway }}</text>
          <text class="detail-tag">📍 {{ cityText }}</text>
        </view>

        <text class="detail-title">{{ detailTitle }}</text>
        <view class="detail-meta">
          <text>👁 {{ detail.readtimes || 0 }}</text>
          <text>🤝 约拍 {{ detail.pattimes || 0 }}</text>
          <text>★ 收藏 {{ detail.coltimes || 0 }}</text>
        </view>
        <text class="detail-body">{{ detail.describ }}</text>

        <view v-if="styleTags.length" class="style-tags">
          <text v-for="style in styleTags" :key="style" class="style-tag">{{ style }}</text>
        </view>

        <view class="author-card" @tap="goProfile">
          <image class="author-card__avatar" :src="detail.userQo?.imgpath || '/static/default-avatar.png'" mode="aspectFill" />
          <view class="author-card__info">
            <text class="author-card__name">{{ detail.userQo?.nickname || '匿名用户' }}</text>
            <text class="author-card__desc">{{ authorDesc }}</text>
          </view>
          <view class="author-card__follow" @tap.stop="followed = !followed">{{ followed ? '已关注' : '+ 关注' }}</view>
        </view>

        <text class="section-title">TA 的作品</text>
        <view class="portfolio-grid">
          <image v-for="image in portfolioImages" :key="image" :src="image" mode="aspectFill" />
        </view>
      </view>

      <view class="detail-actions">
        <view class="detail-actions__mini" @tap="favorite">
          <KeepIcon name="star" :size="42" />
          <text>{{ favorited ? '已收藏' : '收藏' }}</text>
        </view>
        <view class="detail-actions__mini" @tap="showToast('打开私信')">
          <KeepIcon name="mail" :size="42" />
          <text>私信</text>
        </view>
        <button class="detail-actions__primary" :disabled="actionLoading" :loading="actionLoading" @tap="apply">立即约拍</button>
      </view>
    </template>
  </view>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { useUserStore } from '@/stores/user'
import * as ypatApi from '@/api/modules/ypat'
import { put } from '@/api/request'
import { TARGET_LABELS } from '@/constants/enums'
import KeepIcon from './KeepIcon.vue'
import KeepState from './KeepState.vue'
import type { YpatInfo } from '@/api/types'

const props = defineProps<{ id: number }>()
const userStore = useUserStore()
const detail = ref<YpatInfo | null>(null)
const loading = ref(false)
const actionLoading = ref(false)
const errorMessage = ref('')
const favorited = ref(false)
const followed = ref(false)
const images = computed(() => detail.value?.pics?.filter(Boolean) || ['/static/default-cover.png'])
const portfolioImages = computed(() => images.value.slice(0, 6).concat(Array(Math.max(0, 6 - images.value.length)).fill('/static/default-cover.png')))
const detailTitle = computed(() => detail.value?.describ?.split('\n')[0] || detail.value?.targetTxt || '约拍详情')
const cityText = computed(() => [detail.value?.city, detail.value?.area].filter(Boolean).join('·') || '同城')
const styleTags = computed(() => (detail.value?.patstyleTxt || detail.value?.patstyle || '').split(/[,，\s]+/).filter(Boolean).slice(0, 6))
const targetLabel = computed(() => detail.value ? TARGET_LABELS[detail.value.target] || '约拍' : '约拍')
const authorDesc = computed(() => {
  const realname = detail.value?.userQo?.realnameflag === '1' ? '已实名' : '未实名'
  const credit = detail.value?.creditflag === '1' ? '信用担保' : '信用良好'
  return `摄影爱好者 · ${realname} · ${credit}`
})

async function load(): Promise<void> {
  if (!props.id) {
    errorMessage.value = '缺少约拍 ID'
    return
  }
  loading.value = true
  errorMessage.value = ''
  try {
    const result = await ypatApi.getDetail(props.id)
    detail.value = result.data
    favorited.value = result.data?.colflag === '1'
    put('/ypat/yd/add', { ypatid: props.id }, { showError: false }).catch(() => undefined)
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : '获取详情失败'
  } finally {
    loading.value = false
  }
}

function preview(index: number): void {
  uni.previewImage({ current: index, urls: images.value })
}

function back(): void {
  if (getCurrentPages().length > 1) uni.navigateBack()
  else uni.switchTab({ url: '/pages/home/index' })
}

function share(): void {
  showToast('已分享')
}

function goProfile(): void {
  if (detail.value?.userid) uni.navigateTo({ url: `/pages-sub/user/profile?id=${detail.value.userid}` })
}

function showToast(title: string): void {
  uni.showToast({ title, icon: 'none' })
}

function requireLogin(): boolean {
  if (userStore.isLoggedIn) return true
  uni.navigateTo({ url: '/pages/login/index' })
  return false
}

async function favorite(): Promise<void> {
  if (!requireLogin() || !detail.value || favorited.value || actionLoading.value) return
  actionLoading.value = true
  try {
    await ypatApi.addFavorite(userStore.userInfo!.id, detail.value.id)
    favorited.value = true
    uni.showToast({ title: '收藏成功', icon: 'success' })
  } catch (error) {
    uni.showToast({ title: error instanceof Error ? error.message : '收藏失败', icon: 'none' })
  } finally {
    actionLoading.value = false
  }
}

async function apply(): Promise<void> {
  if (!requireLogin() || !detail.value || actionLoading.value) return
  if (detail.value.userid === userStore.userInfo!.id) {
    uni.showToast({ title: '不能报名自己发布的约拍', icon: 'none' })
    return
  }
  actionLoading.value = true
  try {
    await ypatApi.applyYpat({
      sendperid: userStore.userInfo!.id,
      recperid: detail.value.userid,
      ypatid: detail.value.id,
      content: '我想报名参加您的约拍',
    })
    uni.showToast({ title: '报名成功', icon: 'success' })
  } catch (error) {
    uni.showToast({ title: error instanceof Error ? error.message : '报名失败', icon: 'none' })
  } finally {
    actionLoading.value = false
  }
}

watch(() => props.id, load, { immediate: true })
</script>

<style scoped lang="scss">
@import '@/styles/tokens.scss';
@import '@/styles/mixins.scss';

.detail-page {
  min-height: 100vh;
  padding-bottom: 168rpx;
  background: $color-bg-page;
}

.detail-topbar {
  position: fixed;
  top: calc(24rpx + env(safe-area-inset-top));
  right: 28rpx;
  left: 28rpx;
  z-index: 20;
  display: flex;
  justify-content: space-between;
}

.detail-icon-button {
  @include flex-center;
  width: 72rpx;
  height: 72rpx;
  border-radius: 50%;
  color: #fff;
  background: rgba(0, 0, 0, 0.34);
}

.hero-wrap {
  position: relative;
}

.hero-swiper {
  height: 720rpx;
  background: $color-bg-chip;
}

.hero-swiper image {
  width: 100%;
  height: 100%;
}

.hero-count {
  position: absolute;
  right: 28rpx;
  bottom: 46rpx;
  padding: 8rpx 22rpx;
  border-radius: $radius-round;
  color: #fff;
  background: rgba(0, 0, 0, 0.42);
  font-size: 24rpx;
  font-weight: 700;
}

.detail-sheet {
  position: relative;
  z-index: 2;
  margin-top: -44rpx;
  padding: 40rpx 36rpx 36rpx;
  border-radius: 44rpx 44rpx 0 0;
  background: #fff;
}

.detail-tags,
.style-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 14rpx;
}

.detail-tag,
.style-tag {
  padding: 10rpx 22rpx;
  border-radius: $radius-round;
  color: $color-text-secondary;
  background: $color-bg-chip;
  font-size: 24rpx;
  font-weight: 800;
}

.detail-tag--green {
  color: $color-primary-dark;
  background: $color-primary-light;
}

.detail-title {
  display: block;
  margin-top: 28rpx;
  color: $color-text-primary;
  font-size: 42rpx;
  font-weight: 900;
  line-height: 1.35;
}

.detail-meta {
  display: flex;
  gap: 32rpx;
  margin-top: 28rpx;
  padding: 26rpx 0;
  border-top: 1rpx solid $color-border;
  border-bottom: 1rpx solid $color-border;
  color: $color-text-secondary;
  font-size: 26rpx;
  font-weight: 700;
}

.detail-body {
  display: block;
  margin-top: 28rpx;
  color: #3A3D3F;
  font-size: 30rpx;
  line-height: 1.75;
}

.style-tags {
  margin-top: 28rpx;
}

.author-card {
  display: flex;
  align-items: center;
  gap: 24rpx;
  margin-top: 36rpx;
  padding: 28rpx;
  border: 1rpx solid $color-border;
  border-radius: 32rpx;
}

.author-card__avatar {
  width: 100rpx;
  height: 100rpx;
  flex: none;
  border-radius: 50%;
  background: $color-bg-chip;
}

.author-card__info {
  min-width: 0;
  flex: 1;
}

.author-card__name,
.author-card__desc {
  display: block;
}

.author-card__name {
  color: $color-text-primary;
  font-size: 32rpx;
  font-weight: 800;
}

.author-card__desc {
  margin-top: 8rpx;
  color: $color-text-secondary;
  font-size: 24rpx;
}

.author-card__follow {
  padding: 16rpx 30rpx;
  border-radius: $radius-round;
  color: $color-primary-dark;
  background: $color-primary-light;
  font-size: 26rpx;
  font-weight: 800;
}

.section-title {
  display: block;
  margin-top: 40rpx;
  color: $color-text-primary;
  font-size: 34rpx;
  font-weight: 900;
}

.portfolio-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 10rpx;
  margin-top: 24rpx;
}

.portfolio-grid image {
  width: 100%;
  height: 212rpx;
  border-radius: 20rpx;
  background: $color-bg-chip;
}

.detail-actions {
  position: fixed;
  right: 0;
  bottom: 0;
  left: 0;
  z-index: 30;
  display: flex;
  align-items: center;
  gap: 28rpx;
  padding: 24rpx 36rpx calc(28rpx + env(safe-area-inset-bottom));
  border-top: 1rpx solid $color-border;
  background: #fff;
}

.detail-actions__mini {
  display: flex;
  flex-direction: column;
  align-items: center;
  color: $color-text-secondary;
  font-size: 22rpx;
  font-weight: 700;
}

.detail-actions__primary {
  flex: 1;
  height: 100rpx;
  border-radius: $radius-round;
  color: #fff;
  background: $color-primary;
  box-shadow: $shadow-keep-button;
  font-size: 32rpx;
  font-weight: 900;
  line-height: 100rpx;
}
</style>
