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
      <view class="hero" />
      <view class="user-card">
        <image class="avatar" :src="avatarSrc" mode="aspectFill" />
        <view class="name-row">
          <text class="name">{{ profile.nickname || '未设置昵称' }}</text>
          <text v-if="profile.realnameflag === '1'" class="badge">已认证</text>
        </view>
        <text class="meta">{{ profile.profess || '摄影爱好者' }} · {{ profile.city || '未设置城市' }}</text>
        <view class="stats">
          <view><text>{{ profile.pubtimes || 0 }}</text><text>发布</text></view>
          <view><text>{{ profile.rectimes || 0 }}</text><text>报名</text></view>
          <view><text>{{ profile.coltimes || 0 }}</text><text>收藏</text></view>
        </view>
        <view v-if="isOwnProfile" class="profile-actions">
          <view class="profile-action profile-action--primary" @tap="goEdit">编辑资料</view>
          <view class="profile-action" @tap="goSettings">设置</view>
        </view>
      </view>

      <view class="works">
        <text class="section-title">发布的约拍</text>
        <view v-if="items.length" class="grid">
          <view v-for="item in items" :key="item.id" class="work" @tap="openDetail(item.id)">
            <image :src="item.pics?.[0] || '/static/default-cover.png'" mode="aspectFill" />
            <text>{{ item.targetTxt || item.describ }}</text>
          </view>
        </view>
        <view v-else class="empty">暂无发布内容</view>
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

.page { min-height: 100vh; background: $color-bg-page; }
.navbar { position: fixed; z-index: 10; top: 0; left: 0; right: 0; background: rgba(255,255,255,.95); backdrop-filter: blur(16rpx); }
.navbar__content { height: 44px; display: flex; align-items: center; justify-content: space-between; padding: 0 24rpx; }
.back { width: 60rpx; display: flex; align-items: center; }
.nav-title { font-size: 31rpx; font-weight: 600; }
.placeholder { width: 60rpx; }
.state { padding: 260rpx 30rpx; color: $color-text-secondary; text-align: center; }
.error { color: #b4232c; }
.hero { height: 280rpx; background: linear-gradient(135deg, #252A32, #23C268); }
.user-card { margin: -70rpx 24rpx 0; padding: 30rpx; border-radius: 36rpx; background: $color-bg-card; box-shadow: $shadow-keep-card; text-align: center; }
.avatar { width: 130rpx; height: 130rpx; margin-top: -90rpx; border: 6rpx solid #fff; border-radius: 50%; }
.name-row { display: flex; justify-content: center; align-items: center; gap: 12rpx; margin-top: 14rpx; }
.name { font-size: 34rpx; font-weight: 600; }
.badge { color: #168849; font-size: 22rpx; }
.meta { display: block; margin-top: 10rpx; color: $color-text-secondary; font-size: 25rpx; }
.stats { display: flex; justify-content: space-around; margin-top: 28rpx; }
.stats view { display: flex; flex-direction: column; gap: 8rpx; }
.stats text:first-child { font-size: 31rpx; font-weight: 600; }
.stats text:last-child { color: $color-text-helper; font-size: 23rpx; }
.profile-actions { display: flex; gap: 18rpx; margin-top: 28rpx; }
.profile-action { flex: 1; height: 72rpx; border-radius: 999rpx; color: $color-text-secondary; background: $color-bg-chip; font-size: 26rpx; font-weight: 900; line-height: 72rpx; }
.profile-action--primary { color: #fff; background: $color-primary; }
.works { margin: 24rpx; padding: 28rpx; border-radius: $radius-keep-card; background: $color-bg-card; box-shadow: $shadow-keep-card; }
.section-title { font-size: 30rpx; font-weight: 600; }
.grid { display: grid; grid-template-columns: repeat(2, 1fr); gap: 18rpx; margin-top: 24rpx; }
.work { overflow: hidden; border-radius: 24rpx; background: $color-bg-page; }
.work image { width: 100%; height: 220rpx; }
.work text { display: block; padding: 14rpx; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.empty { padding: 80rpx 0; color: $color-text-helper; text-align: center; }
.more { margin-top: 24rpx; border-radius: 999rpx; color: #fff; background: $color-primary; }
.more::after { border: 0; }
</style>
