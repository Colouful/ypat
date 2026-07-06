<template>
  <view class="page">
    <view class="navbar" :style="{ paddingTop: `${statusBarHeight}px` }">
      <view class="navbar__content">
        <view class="back" @tap="back">
          <KeepIcon name="chevron-left" :size="42" color="#1B1E23" />
        </view>
        <text class="nav-title">个人主页</text>
        <view class="placeholder" />
      </view>
    </view>

    <view v-if="loading" class="state">加载中...</view>
    <view v-else-if="errorMessage" class="state error">{{ errorMessage }}</view>
    <view v-else-if="profile" class="content" :style="{ paddingTop: `${navBarHeight}px` }">
      <view class="hero">
        <!--
        <view class="hero__copy">
          <text class="hero__eyebrow">约拍主页</text>
          <text class="hero__title">{{ profile.nickname || '未设置昵称' }}</text>
          <text class="hero__desc">{{ profile.profess || '摄影爱好者' }} · {{ profile.city || '未设置城市' }}</text>
        </view>
        -->
      </view>

      <view class="user-card">
        <view class="avatar-wrap">
          <image class="avatar" :src="avatarSrc" mode="aspectFill" />
        </view>
        <view class="name-row">
          <text class="name">{{ profile.nickname || '未设置昵称' }}</text>
          <view v-if="profile.realnameflag === '1'" class="badge">
            <KeepIcon name="shield" :size="22" color="#17A857" />
            <text>已认证</text>
          </view>
        </view>
        <view class="meta-row">
          <view class="meta-chip">
            <KeepIcon name="camera" :size="22" color="#83888F" />
            <text>{{ profile.profess || '摄影爱好者' }}</text>
          </view>
          <view class="meta-chip">
            <KeepIcon name="map-pin" :size="22" color="#83888F" />
            <text>{{ profile.city || '未设置城市' }}</text>
          </view>
        </view>
        <view class="stats">
          <view class="stat-item">
            <text class="stat-value">{{ profile.pubtimes || 0 }}</text>
            <text class="stat-label">发布</text>
          </view>
          <view class="stat-item">
            <text class="stat-value">{{ profile.rectimes || 0 }}</text>
            <text class="stat-label">报名</text>
          </view>
          <view class="stat-item">
            <text class="stat-value">{{ profile.coltimes || 0 }}</text>
            <text class="stat-label">收藏</text>
          </view>
        </view>
        <view v-if="isOwnProfile" class="profile-actions">
          <view class="profile-action profile-action--primary" @tap="goEdit">编辑资料</view>
          <view class="profile-action" @tap="goSettings">设置</view>
        </view>
      </view>

      <view class="works">
        <view class="works__header">
          <view>
            <text class="section-title">发布的约拍</text>
            <text class="section-subtitle">共 {{ items.length }} 条内容</text>
          </view>
          <view v-if="items.length" class="works__mark">
            <KeepIcon name="image" :size="26" color="#17A857" />
          </view>
        </view>
        <view v-if="items.length" class="grid">
          <view v-for="item in items" :key="item.id" class="work" @tap="openDetail(item.id)">
            <image class="work__image" :src="normalizeImageUrl(item.pics?.[0]) || '/static/default-cover.png'" mode="aspectFill" />
            <view class="work__body">
              <text class="work__title">{{ item.targetTxt || item.describ || '未命名约拍' }}</text>
            </view>
          </view>
        </view>
        <view v-else class="empty">
          <view class="empty__icon">
            <KeepIcon name="image" :size="46" color="#B3B8BE" />
          </view>
          <text class="empty__title">暂无发布内容</text>
          <text class="empty__desc">TA 还没有公开的约拍作品</text>
        </view>
        <button v-if="hasMore" class="more" :loading="loadingMore" @tap="loadMore">查看更多</button>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { useAppStore } from '@/stores/app'
import { useUserStore } from '@/stores/user'
import * as userApi from '@/api/modules/user'
import * as ypatApi from '@/api/modules/ypat'
import { normalizeImageUrl } from '@/api/adapters'
import type { UserInfo, YpatInfo } from '@/api/types'
import KeepIcon from '@/components/business/KeepIcon.vue'

const appStore = useAppStore()
const userStore = useUserStore()
const statusBarHeight = computed(() => appStore.statusBarHeight)
const navBarHeight = computed(() => appStore.navBarHeight)
const profile = ref<UserInfo | null>(null)
const items = ref<YpatInfo[]>([])
const loading = ref(true)
const loadingMore = ref(false)
const errorMessage = ref('')
const userId = ref(0)
const page = ref(0)
const hasMore = ref(false)
const size = 10
const isOwnProfile = computed(() => Boolean(userStore.userInfo?.id && userStore.userInfo.id === userId.value))
const avatarSrc = computed(() => normalizeImageUrl(profile.value?.imgpath || profile.value?.avatarurl) || '/static/default-avatar.png')
const heroImageSrc = computed(() => normalizeImageUrl(items.value[0]?.pics?.[0]) || avatarSrc.value || '/static/default-cover.png')

async function loadProfile(): Promise<void> {
  loading.value = true
  try {
    profile.value = (await userApi.getUserInfo(userId.value)).data
    await loadWorks(true)
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : '用户信息加载失败'
  } finally {
    loading.value = false
  }
}

async function loadWorks(refresh = false): Promise<void> {
  if (refresh) page.value = 0
  const result = await ypatApi.getMyPublishList({ userid: userId.value, page: page.value, size })
  const content = result.data?.content || []
  items.value = refresh ? content : [...items.value, ...content]
  const current = result.data?.number ?? page.value
  hasMore.value = current + 1 < (result.data?.totalPages || 0)
  page.value = current + 1
}

async function loadMore(): Promise<void> {
  if (loadingMore.value || !hasMore.value) return
  loadingMore.value = true
  try {
    await loadWorks()
  } finally {
    loadingMore.value = false
  }
}

function openDetail(id: number): void {
  uni.navigateTo({ url: `/pages-sub/ypat/detail?id=${id}` })
}

function back(): void {
  uni.navigateBack()
}

function goEdit(): void {
  uni.navigateTo({ url: '/pages-sub/user/edit-info' })
}

function goSettings(): void {
  uni.navigateTo({ url: '/pages-sub/user/settings' })
}

onLoad((query) => {
  userId.value = Number(query?.id || userStore.userInfo?.id || 0)
  if (!userId.value) {
    loading.value = false
    errorMessage.value = '用户不存在'
    return
  }
  loadProfile()
})
</script>

<style scoped lang="scss">
.page {
  min-height: 100vh;
  background: $color-bg-page;
}

.navbar {
  position: fixed;
  z-index: 10;
  top: 0;
  right: 0;
  left: 0;
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(18rpx);
  box-shadow: 0 8rpx 24rpx rgba(20, 24, 31, 0.04);
}

.navbar__content {
  height: 44px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24rpx;
}

.back,
.placeholder {
  width: 60rpx;
  display: flex;
  align-items: center;
}

.nav-title {
  color: $color-text-primary;
  font-size: 31rpx;
  font-weight: 800;
}

.state {
  padding: 260rpx 30rpx;
  color: $color-text-secondary;
  text-align: center;
}

.error {
  color: #b4232c;
}

.content {
  padding-bottom: calc(56rpx + env(safe-area-inset-bottom));
}

.hero {
  position: relative;
  min-height: 330rpx;
  padding: 42rpx 32rpx 96rpx;
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: linear-gradient(135deg, #1f252c 0%, #20412f 48%, #23c268 100%);
  box-sizing: border-box;
}

.hero::after {
  content: "";
  position: absolute;
  right: -80rpx;
  bottom: -130rpx;
  width: 300rpx;
  height: 300rpx;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.12);
}

.hero__copy {
  position: relative;
  z-index: 1;
  min-width: 0;
  flex: 1;
  padding-right: 28rpx;
}

.hero__eyebrow,
.hero__title,
.hero__desc {
  display: block;
}

.hero__eyebrow {
  color: rgba(255, 255, 255, 0.72);
  font-size: 22rpx;
  font-weight: 800;
}

.hero__title {
  margin-top: 16rpx;
  color: #fff;
  font-size: 44rpx;
  font-weight: 900;
  line-height: 1.18;
}

.hero__desc {
  margin-top: 14rpx;
  color: rgba(255, 255, 255, 0.78);
  font-size: 24rpx;
  font-weight: 700;
  line-height: 1.4;
}

.hero__cover {
  position: relative;
  z-index: 1;
  width: 166rpx;
  height: 210rpx;
  flex: none;
  padding: 8rpx;
  border-radius: 34rpx;
  background: rgba(255, 255, 255, 0.2);
  box-shadow: 0 20rpx 42rpx rgba(0, 0, 0, 0.22);
  box-sizing: border-box;
}

.hero__cover-image {
  width: 100%;
  height: 100%;
  border-radius: 28rpx;
  background: rgba(255, 255, 255, 0.22);
}

.user-card {
  position: relative;
  z-index: 1;
  margin: -76rpx 24rpx 0;
  padding: 0 30rpx 30rpx;
  border-radius: 36rpx;
  background: $color-bg-card;
  box-shadow: 0 18rpx 48rpx rgba(20, 24, 31, 0.08);
  text-align: center;
}

.avatar-wrap {
  width: 142rpx;
  height: 142rpx;
  margin: 0 auto;
  transform: translateY(-58rpx);
  padding: 7rpx;
  border-radius: 50%;
  background: #fff;
  box-shadow: 0 14rpx 28rpx rgba(20, 24, 31, 0.12);
  box-sizing: border-box;
}

.avatar {
  width: 100%;
  height: 100%;
  border-radius: 50%;
  background: $color-bg-chip;
}

.name-row {
  margin-top: -40rpx;
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 12rpx;
}

.name {
  max-width: 430rpx;
  color: $color-text-primary;
  font-size: 36rpx;
  font-weight: 900;
  line-height: 1.25;
}

.badge {
  height: 38rpx;
  padding: 0 12rpx;
  border-radius: 999rpx;
  display: inline-flex;
  align-items: center;
  gap: 4rpx;
  color: #168849;
  background: $color-primary-soft;
  font-size: 21rpx;
  font-weight: 800;
  line-height: 38rpx;
}

.meta-row {
  margin-top: 18rpx;
  display: flex;
  justify-content: center;
  flex-wrap: wrap;
  gap: 12rpx;
}

.meta-chip {
  min-height: 44rpx;
  padding: 0 16rpx;
  border-radius: 999rpx;
  display: inline-flex;
  align-items: center;
  gap: 6rpx;
  color: $color-text-secondary;
  background: $color-bg-chip;
  font-size: 23rpx;
  font-weight: 700;
  line-height: 44rpx;
}

.stats {
  margin-top: 28rpx;
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 14rpx;
}

.stat-item {
  min-height: 112rpx;
  border-radius: 24rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background: linear-gradient(180deg, #f8faf9 0%, #f1f5f2 100%);
}

.stat-value {
  color: $color-text-primary;
  font-size: 34rpx;
  font-weight: 900;
  line-height: 1.1;
}

.stat-label {
  margin-top: 8rpx;
  color: $color-text-helper;
  font-size: 22rpx;
  font-weight: 800;
}

.profile-actions {
  display: flex;
  gap: 18rpx;
  margin-top: 28rpx;
}

.profile-action {
  flex: 1;
  height: 76rpx;
  border-radius: 999rpx;
  color: $color-text-secondary;
  background: $color-bg-chip;
  font-size: 26rpx;
  font-weight: 900;
  line-height: 76rpx;
}

.profile-action--primary {
  color: #fff;
  background: linear-gradient(135deg, $color-primary 0%, $color-primary-dark 100%);
  box-shadow: $shadow-keep-button;
}

.works {
  margin: 24rpx;
  padding: 28rpx;
  border-radius: $radius-keep-card;
  background: $color-bg-card;
  box-shadow: $shadow-keep-card;
}

.works__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 20rpx;
}

.section-title,
.section-subtitle {
  display: block;
}

.section-title {
  color: $color-text-primary;
  font-size: 31rpx;
  font-weight: 900;
}

.section-subtitle {
  margin-top: 8rpx;
  color: $color-text-helper;
  font-size: 22rpx;
  font-weight: 700;
}

.works__mark {
  width: 56rpx;
  height: 56rpx;
  border-radius: 18rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  background: $color-primary-soft;
}

.grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 18rpx;
  margin-top: 24rpx;
}

.work {
  overflow: hidden;
  border-radius: 24rpx;
  background: #fff;
  box-shadow: 0 8rpx 22rpx rgba(20, 24, 31, 0.06);
}

.work__image {
  width: 100%;
  height: 228rpx;
  display: block;
  background: $color-bg-chip;
}

.work__body {
  min-height: 78rpx;
  padding: 14rpx 16rpx;
  display: flex;
  align-items: center;
  box-sizing: border-box;
}

.work__title {
  width: 100%;
  color: $color-text-primary;
  font-size: 24rpx;
  font-weight: 800;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.empty {
  padding: 74rpx 0 70rpx;
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
}

.empty__icon {
  width: 96rpx;
  height: 96rpx;
  border-radius: 32rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  background: $color-bg-chip;
}

.empty__title {
  margin-top: 22rpx;
  color: $color-text-primary;
  font-size: 28rpx;
  font-weight: 900;
}

.empty__desc {
  margin-top: 8rpx;
  color: $color-text-helper;
  font-size: 23rpx;
  font-weight: 700;
}

.more {
  margin-top: 24rpx;
  border-radius: 999rpx;
  color: #fff;
  background: $color-primary;
}

.more::after {
  border: 0;
}
</style>
