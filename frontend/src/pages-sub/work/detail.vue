<template>
  <view class="work-detail-page">
    <KeepPageNav title="作品详情" />
    <view v-if="loading" class="work-detail-page__loading">
      <view class="work-detail-page__skel-user">
        <view class="work-detail-page__skel-avatar skeleton-pulse" />
        <view class="work-detail-page__skel-name skeleton-pulse" />
      </view>
      <view class="work-detail-page__skel-block skeleton-pulse" />
      <view class="work-detail-page__skel-block work-detail-page__skel-block--short skeleton-pulse" />
      <view class="work-detail-page__skel-action skeleton-pulse" />
    </view>
    <view v-else-if="work">
      <WorkDetailContent :work="work" @complain="onComplain" />
      <WorkActionBar :work="work"
                     @like="onLike"
                     @unlike="onUnlike"
                     @favorite="onFavorite"
                     @unfavorite="onUnfavorite"
                     @share="onShare"
                     @apply="onApply" />
    </view>
    <view v-else class="work-detail-page__empty">作品不存在或已下架</view>
  </view>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { onShareAppMessage } from '@dcloudio/uni-app'
import KeepPageNav from '@/components/business/KeepPageNav.vue'
import WorkDetailContent from '@/components/business/WorkDetailContent.vue'
import WorkActionBar from '@/components/business/WorkActionBar.vue'
import { getDetail, like, unlike, favorite, unfavorite } from '@/api/modules/work'
import type { WorkDetail } from '@/api/types/work'

const id = ref<number>(0)
const work = ref<WorkDetail | null>(null)
const loading = ref(true)

onMounted(() => {
  const pages = getCurrentPages()
  const page = pages[pages.length - 1] as any
  const opts = (page && page.options) || {}
  const idStr = opts.id
  if (!idStr) {
    loading.value = false
    return
  }
  id.value = Number(idStr)
  load()
})

async function load() {
  loading.value = true
  try {
    const res = await getDetail(id.value)
    work.value = (res && res.data) || null
  } catch (e) {
    work.value = null
  } finally {
    loading.value = false
  }
}

async function onLike() {
  try {
    await like(id.value)
    if (work.value) {
      work.value = { ...work.value, isLiked: true, likeCount: (work.value.likeCount || 0) + 1 }
    }
  } catch (e: any) {
    uni.showToast({ title: e?.message || '操作失败', icon: 'none' })
  }
}
async function onUnlike() {
  try {
    await unlike(id.value)
    if (work.value) {
      work.value = { ...work.value, isLiked: false, likeCount: Math.max(0, (work.value.likeCount || 0) - 1) }
    }
  } catch (e: any) {
    uni.showToast({ title: e?.message || '操作失败', icon: 'none' })
  }
}
async function onFavorite() {
  try {
    await favorite(id.value)
    if (work.value) {
      work.value = { ...work.value, isFavorited: true, favoriteCount: (work.value.favoriteCount || 0) + 1 }
    }
  } catch (e: any) {
    uni.showToast({ title: e?.message || '操作失败', icon: 'none' })
  }
}
async function onUnfavorite() {
  try {
    await unfavorite(id.value)
    if (work.value) {
      work.value = { ...work.value, isFavorited: false, favoriteCount: Math.max(0, (work.value.favoriteCount || 0) - 1) }
    }
  } catch (e: any) {
    uni.showToast({ title: e?.message || '操作失败', icon: 'none' })
  }
}

function onShare() {
  uni.showToast({ title: '点击右上角分享', icon: 'none' })
}

function onApply() {
  if (!id.value) return
  uni.navigateTo({ url: `/pages-sub/work/apply?workId=${id.value}` })
}

function onComplain() {
  if (!id.value) return
  uni.navigateTo({ url: `/pages-sub/work/complain?workId=${id.value}` })
}

onShareAppMessage(() => ({
  title: work.value?.description || '精彩作品',
  path: `/pages-sub/work/detail?id=${id.value}`,
}))
</script>

<style lang="scss" scoped>
.work-detail-page {
  min-height: 100vh;
  background: $color-bg-page;
  &__loading, &__empty {
    text-align: center;
    padding: 96rpx 0;
    color: $color-text-helper;
    font-size: 26rpx;
  }
  &__skel-user {
    display: flex;
    align-items: center;
    gap: 16rpx;
    padding: 24rpx 32rpx;
  }
  &__skel-avatar {
    width: 80rpx;
    height: 80rpx;
    border-radius: 40rpx;
    background: $color-bg-page;
  }
  &__skel-name {
    flex: 1;
    height: 32rpx;
    border-radius: 8rpx;
    background: $color-bg-page;
  }
  &__skel-block {
    height: 400rpx;
    margin: 0 32rpx 16rpx;
    border-radius: 16rpx;
    background: $color-bg-page;
    &--short { height: 120rpx; }
  }
  &__skel-action {
    height: 80rpx;
    margin: 32rpx 32rpx 0;
    border-radius: 999rpx;
    background: $color-bg-page;
  }
}

.skeleton-pulse {
  animation: pulse 1.4s ease-in-out infinite;
}
@keyframes pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.4; }
}
</style>
