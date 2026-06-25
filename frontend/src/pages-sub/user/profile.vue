<template>
  <view class="page">
    <view class="navbar" :style="{ paddingTop: `${statusBarHeight}px` }">
      <view class="navbar__content">
        <text class="back" @tap="back">‹</text>
        <text class="nav-title">个人主页</text>
        <view class="placeholder" />
      </view>
    </view>

    <view v-if="loading" class="state">加载中...</view>
    <view v-else-if="errorMessage" class="state error">{{ errorMessage }}</view>
    <view v-else-if="profile" class="content" :style="{ paddingTop: `${navBarHeight}px` }">
      <view class="hero" />
      <view class="user-card">
        <image class="avatar" :src="profile.imgpath || profile.avatarurl || '/static/default-avatar.png'" mode="aspectFill" />
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
import type { UserInfo, YpatInfo } from '@/api/types'

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
@import '@/styles/tokens.scss';

.page { min-height: 100vh; background: $color-bg-page; }
.navbar { position: fixed; z-index: 10; top: 0; left: 0; right: 0; background: rgba(255,255,255,.95); }
.navbar__content { height: 44px; display: flex; align-items: center; justify-content: space-between; padding: 0 24rpx; }
.back { width: 60rpx; font-size: 54rpx; }
.nav-title { font-size: 31rpx; font-weight: 600; }
.placeholder { width: 60rpx; }
.state { padding: 260rpx 30rpx; color: #808997; text-align: center; }
.error { color: #b4232c; }
.hero { height: 280rpx; background: linear-gradient(135deg, $color-primary, #6dd8a0); }
.user-card { margin: -70rpx 24rpx 0; padding: 28rpx; border-radius: 32rpx; background: #fff; box-shadow: 0 6rpx 24rpx rgba(20, 24, 31, .04); text-align: center; }
.avatar { width: 130rpx; height: 130rpx; margin-top: -90rpx; border: 6rpx solid #fff; border-radius: 50%; }
.name-row { display: flex; justify-content: center; align-items: center; gap: 12rpx; margin-top: 14rpx; }
.name { font-size: 34rpx; font-weight: 600; }
.badge { color: #168849; font-size: 22rpx; }
.meta { display: block; margin-top: 10rpx; color: #7d8694; font-size: 25rpx; }
.stats { display: flex; justify-content: space-around; margin-top: 28rpx; }
.stats view { display: flex; flex-direction: column; gap: 8rpx; }
.stats text:first-child { font-size: 31rpx; font-weight: 600; }
.stats text:last-child { color: #8c95a2; font-size: 23rpx; }
.works { margin: 24rpx; padding: 28rpx; border-radius: 32rpx; background: #fff; box-shadow: 0 6rpx 24rpx rgba(20, 24, 31, .04); }
.section-title { font-size: 30rpx; font-weight: 600; }
.grid { display: grid; grid-template-columns: repeat(2, 1fr); gap: 18rpx; margin-top: 24rpx; }
.work { overflow: hidden; border-radius: 18rpx; background: $color-bg-page; }
.work image { width: 100%; height: 220rpx; }
.work text { display: block; padding: 14rpx; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.empty { padding: 80rpx 0; color: $color-text-helper; text-align: center; }
.more { margin-top: 24rpx; border-radius: 38rpx; }
</style>
