<template>
  <view class="profile-page">
    <!-- 自定义导航栏 -->
    <view class="navbar" :style="{ paddingTop: statusBarHeight + 'px' }">
      <view class="navbar__content">
        <view class="navbar__back" @tap="handleBack">
          <text class="navbar__back-icon">&#xe60b;</text>
        </view>
        <text class="navbar__title">个人主页</text>
        <view class="navbar__placeholder" />
      </view>
    </view>

    <!-- 加载状态 -->
    <view v-if="loading" class="loading-state">
      <view class="loading-spinner" />
      <text class="loading-text">加载中...</text>
    </view>

    <!-- 错误状态 -->
    <view v-else-if="error" class="error-state">
      <text class="error-icon">!</text>
      <text class="error-text">{{ error }}</text>
      <view class="error-retry" @tap="loadUserData">
        <text class="error-retry-text">重新加载</text>
      </view>
    </view>

    <!-- 主内容 -->
    <view v-else class="profile-content" :style="{ paddingTop: navBarHeight + 'px' }">
      <!-- 封面区域 -->
      <view class="cover-section">
        <view class="cover-bg" />
      </view>

      <!-- 用户信息卡片 -->
      <view class="user-card">
        <!-- 头像 -->
        <view class="avatar-wrapper">
          <image
            class="avatar"
            :src="profileUser?.avatarurl || '/static/images/default-avatar.png'"
            mode="aspectFill"
          />
          <view v-if="profileUser?.realnameflag" class="verified-badge">
            <text class="verified-icon">&#xe612;</text>
          </view>
        </view>

        <!-- 昵称 -->
        <text class="user-nickname">{{ profileUser?.nickname || '未设置昵称' }}</text>

        <!-- 职业标签 -->
        <view class="user-meta">
          <text v-if="professLabel" class="profess-tag">{{ professLabel }}</text>
          <text v-if="cityText" class="city-text">{{ cityText }}</text>
        </view>

        <!-- 数据统计行 -->
        <view class="stats-row">
          <view class="stat-item">
            <text class="stat-value">{{ profileUser?.pubtimes || 0 }}</text>
            <text class="stat-label">发布</text>
          </view>
          <view class="stat-divider" />
          <view class="stat-item">
            <text class="stat-value">{{ profileUser?.rectimes || 0 }}</text>
            <text class="stat-label">报名</text>
          </view>
          <view class="stat-divider" />
          <view class="stat-item">
            <text class="stat-value">{{ profileUser?.coltimes || 0 }}</text>
            <text class="stat-label">收藏</text>
          </view>
        </view>
      </view>

      <!-- 作品网格 -->
      <view class="works-section">
        <view class="section-header">
          <text class="section-title">发布的约拍</text>
          <text class="section-count">共{{ totalWorks }}条</text>
        </view>

        <view v-if="worksList.length > 0" class="works-grid">
          <view
            v-for="item in worksList"
            :key="item.id"
            class="work-item"
            @tap="goToDetail(item.id)"
          >
            <image
              class="work-cover"
              :src="getFirstImage(item.pics)"
              mode="aspectFill"
            />
            <view class="work-info">
              <text class="work-title">{{ item.title }}</text>
              <text class="work-city">{{ item.city }}</text>
            </view>
          </view>
        </view>

        <view v-else class="empty-works">
          <text class="empty-text">暂无发布内容</text>
        </view>

        <!-- 加载更多 -->
        <view v-if="hasMore" class="load-more" @tap="loadMoreWorks">
          <text class="load-more-text">{{ loadingMore ? '加载中...' : '查看更多' }}</text>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { useAppStore } from '@/stores/app'
import { useUserStore, type UserInfo } from '@/stores/user'
import * as userApi from '@/api/modules/user'
import * as ypatApi from '@/api/modules/ypat'
import { PROFESS_LABELS } from '@/constants/enums'
import type { YpatInfo } from '@/api/types'

const appStore = useAppStore()
const userStore = useUserStore()

const statusBarHeight = computed(() => appStore.statusBarHeight)
const navBarHeight = computed(() => appStore.navBarHeight)

// 页面状态
const loading = ref(true)
const error = ref('')
const profileUser = ref<UserInfo | null>(null)
const worksList = ref<YpatInfo[]>([])
const totalWorks = ref(0)
const currentPage = ref(1)
const pageSize = 10
const hasMore = ref(false)
const loadingMore = ref(false)
const userId = ref<number>(0)

// 计算属性
const professLabel = computed(() => {
  if (!profileUser.value?.profess) return ''
  return PROFESS_LABELS[profileUser.value.profess] || ''
})

const cityText = computed(() => {
  if (!profileUser.value) return ''
  const { province, city } = profileUser.value
  if (province && city && province !== city) {
    return `${province} · ${city}`
  }
  return city || province || ''
})

// 页面加载
onLoad((query) => {
  const id = query?.id
  if (id) {
    userId.value = Number(id)
  } else if (userStore.userInfo) {
    userId.value = Number(userStore.userInfo.id)
  }
  if (userId.value) {
    loadUserData()
  } else {
    loading.value = false
    error.value = '用户信息不存在'
  }
})

// 加载用户数据
async function loadUserData() {
  loading.value = true
  error.value = ''
  try {
    const res = await userApi.getUserInfo(userId.value)
    if (res.success && res.data) {
      // 将 API 返回的 UserInfo 映射到本地 store 的 UserInfo 格式
      profileUser.value = {
        id: String(res.data.id),
        nickname: res.data.nickname,
        gender: res.data.gender,
        profess: res.data.profess || '',
        mobile: res.data.mobile,
        ppd: String(res.data.ppd || '0'),
        avatarurl: res.data.imgpath,
        realnameflag: res.data.realnameflag || 0,
        creditflag: res.data.creditflag || 0,
        pubtimes: res.data.pubtimes || 0,
        rectimes: res.data.rectimes || 0,
        coltimes: res.data.coltimes || 0,
        status: res.data.status,
        province: res.data.province,
        city: res.data.city,
        openid: res.data.openid,
      }
      await loadWorks()
    } else {
      error.value = res.message || '获取用户信息失败'
    }
  } catch (e: any) {
    error.value = e?.message || '网络异常，请稍后重试'
  } finally {
    loading.value = false
  }
}

// 加载作品列表
async function loadWorks() {
  try {
    const res = await ypatApi.getMyPublishList({
      userId: userId.value,
      page: currentPage.value,
      size: pageSize,
    })
    if (res.success && res.data) {
      worksList.value = res.data.content
      totalWorks.value = res.data.totalElements
      hasMore.value = currentPage.value < res.data.totalPages
    }
  } catch {
    // 作品加载失败不阻塞页面
  }
}

// 加载更多
async function loadMoreWorks() {
  if (loadingMore.value || !hasMore.value) return
  loadingMore.value = true
  currentPage.value++
  try {
    const res = await ypatApi.getMyPublishList({
      userId: userId.value,
      page: currentPage.value,
      size: pageSize,
    })
    if (res.success && res.data) {
      worksList.value.push(...res.data.content)
      hasMore.value = currentPage.value < res.data.totalPages
    }
  } catch {
    currentPage.value--
  } finally {
    loadingMore.value = false
  }
}

// 获取第一张图片
function getFirstImage(pics: string[]): string {
  if (!pics || pics.length === 0) return '/static/images/default-cover.png'
  return pics[0] || '/static/images/default-cover.png'
}

// 跳转详情
function goToDetail(id: number) {
  uni.navigateTo({ url: `/pages-sub/ypat/detail?id=${id}` })
}

// 返回
function handleBack() {
  uni.navigateBack({ delta: 1 })
}
</script>

<style lang="scss">
@import '@/styles/tokens.scss';

.profile-page {
  min-height: 100vh;
  background-color: $color-bg-page;
}

// 导航栏
.navbar {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: $z-index-navbar;
  background: transparent;

  &__content {
    display: flex;
    align-items: center;
    justify-content: space-between;
    height: 88rpx;
    padding: 0 $spacing-lg;
  }

  &__back {
    width: 64rpx;
    height: 64rpx;
    display: flex;
    align-items: center;
    justify-content: center;
    background: rgba(0, 0, 0, 0.3);
    border-radius: $radius-round;
  }

  &__back-icon {
    font-family: 'iconfont';
    font-size: $font-size-lg;
    color: #ffffff;
  }

  &__title {
    font-size: $font-size-lg;
    font-weight: $font-weight-semibold;
    color: #ffffff;
  }

  &__placeholder {
    width: 64rpx;
  }
}

// 加载状态
.loading-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding-top: 400rpx;
}

.loading-spinner {
  width: 64rpx;
  height: 64rpx;
  border: 4rpx solid $color-border;
  border-top-color: $color-primary;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

.loading-text {
  margin-top: $spacing-lg;
  font-size: $font-size-base;
  color: $color-text-secondary;
}

// 错误状态
.error-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding-top: 400rpx;
}

.error-icon {
  width: 80rpx;
  height: 80rpx;
  line-height: 80rpx;
  text-align: center;
  font-size: $font-size-xl;
  font-weight: $font-weight-bold;
  color: #ffffff;
  background: $color-accent-red;
  border-radius: 50%;
}

.error-text {
  margin-top: $spacing-lg;
  font-size: $font-size-base;
  color: $color-text-secondary;
}

.error-retry {
  margin-top: $spacing-xl;
  padding: $spacing-md $spacing-xxl;
  background: $color-primary;
  border-radius: $radius-round;
}

.error-retry-text {
  font-size: $font-size-base;
  color: #ffffff;
}

// 封面区域
.cover-section {
  position: relative;
  height: 300rpx;
}

.cover-bg {
  width: 100%;
  height: 100%;
  background: linear-gradient(135deg, $color-primary 0%, $color-primary-dark 50%, $color-accent-blue 100%);
}

// 用户卡片
.user-card {
  position: relative;
  margin: -60rpx $spacing-lg 0;
  padding: 80rpx $spacing-xl $spacing-xl;
  background: $color-bg-card;
  border-radius: $radius-lg;
  box-shadow: $shadow-md;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.avatar-wrapper {
  position: absolute;
  top: -60rpx;
  left: 50%;
  transform: translateX(-50%);
}

.avatar {
  width: 120rpx;
  height: 120rpx;
  border-radius: 50%;
  border: 6rpx solid #ffffff;
  box-shadow: $shadow-sm;
}

.verified-badge {
  position: absolute;
  right: -4rpx;
  bottom: -4rpx;
  width: 36rpx;
  height: 36rpx;
  background: $color-primary;
  border-radius: 50%;
  border: 4rpx solid #ffffff;
  display: flex;
  align-items: center;
  justify-content: center;
}

.verified-icon {
  font-family: 'iconfont';
  font-size: 20rpx;
  color: #ffffff;
}

.user-nickname {
  margin-top: $spacing-md;
  font-size: $font-size-xl;
  font-weight: $font-weight-semibold;
  color: $color-text-primary;
}

.user-meta {
  display: flex;
  align-items: center;
  gap: $spacing-sm;
  margin-top: $spacing-sm;
}

.profess-tag {
  padding: 4rpx 16rpx;
  font-size: $font-size-sm;
  color: $color-primary;
  background: $color-primary-light;
  border-radius: $radius-round;
}

.city-text {
  font-size: $font-size-sm;
  color: $color-text-secondary;
}

// 统计行
.stats-row {
  display: flex;
  align-items: center;
  width: 100%;
  margin-top: $spacing-xl;
  padding-top: $spacing-xl;
  border-top: 1rpx solid $color-divider;
}

.stat-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.stat-value {
  font-size: $font-size-xxl;
  font-weight: $font-weight-bold;
  color: $color-text-primary;
}

.stat-label {
  margin-top: 4rpx;
  font-size: $font-size-sm;
  color: $color-text-secondary;
}

.stat-divider {
  width: 1rpx;
  height: 48rpx;
  background: $color-divider;
}

// 作品区域
.works-section {
  margin: $spacing-lg;
  padding: $spacing-xl;
  background: $color-bg-card;
  border-radius: $radius-lg;
  box-shadow: $shadow-sm;
}

.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: $spacing-lg;
}

.section-title {
  font-size: $font-size-lg;
  font-weight: $font-weight-semibold;
  color: $color-text-primary;
}

.section-count {
  font-size: $font-size-sm;
  color: $color-text-helper;
}

.works-grid {
  display: flex;
  flex-wrap: wrap;
  gap: $spacing-md;
}

.work-item {
  width: calc(50% - 8rpx);
  border-radius: $radius-md;
  overflow: hidden;
  background: $color-bg-page;
}

.work-cover {
  width: 100%;
  height: 240rpx;
}

.work-info {
  padding: $spacing-sm $spacing-md;
}

.work-title {
  display: block;
  font-size: $font-size-base;
  font-weight: $font-weight-medium;
  color: $color-text-primary;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.work-city {
  display: block;
  margin-top: 4rpx;
  font-size: $font-size-xs;
  color: $color-text-helper;
}

// 空状态
.empty-works {
  padding: $spacing-xxl 0;
  text-align: center;
}

.empty-text {
  font-size: $font-size-base;
  color: $color-text-helper;
}

// 加载更多
.load-more {
  margin-top: $spacing-lg;
  padding: $spacing-md 0;
  text-align: center;
}

.load-more-text {
  font-size: $font-size-base;
  color: $color-primary;
}
</style>
