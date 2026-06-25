<template>
  <view class="page">
    <view class="search-bar" :style="{ paddingTop: `${statusBarHeight}px` }">
      <input v-model="keyword" class="input" placeholder="搜索城市或拍摄风格" confirm-type="search" @confirm="search(true)" />
      <text class="cancel" @tap="back">取消</text>
    </view>

    <view class="content" :style="{ paddingTop: `${navHeight + 12}px` }">
      <view v-if="!searched" class="suggestions">
        <text class="section-title">热门风格</text>
        <view class="tags">
          <view v-for="style in photoStyles" :key="style" class="tag" @tap="useTag(style)">{{ style }}</view>
        </view>
      </view>

      <view v-else-if="loading && !items.length" class="state">搜索中...</view>
      <view v-else-if="!items.length" class="state">未找到相关约拍</view>
      <view v-else>
        <view v-for="item in items" :key="item.id" class="card" @tap="openDetail(item.id)">
          <image class="cover" :src="item.pics?.[0] || '/static/default-cover.png'" mode="aspectFill" />
          <view class="body">
            <text class="name">{{ item.userQo?.nickname || '匿名用户' }}</text>
            <text class="desc">{{ item.describ }}</text>
            <view class="meta"><text>{{ item.city }}</text><text>{{ chargeLabel(item.chargeway) }}</text></view>
          </view>
        </view>
      </view>
      <view v-if="loading && items.length" class="footer">加载中...</view>
      <view v-else-if="items.length && !hasMore" class="footer">没有更多了</view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { onReachBottom } from '@dcloudio/uni-app'
import { useAppStore } from '@/stores/app'
import * as ypatApi from '@/api/modules/ypat'
import { CHARGE_WAY_LABELS, PHOTO_STYLES } from '@/constants/enums'
import type { YpatInfo } from '@/api/types'

const appStore = useAppStore()
const statusBarHeight = computed(() => appStore.statusBarHeight)
const navHeight = computed(() => appStore.navBarHeight)
const keyword = ref('')
const searched = ref(false)
const loading = ref(false)
const items = ref<YpatInfo[]>([])
const page = ref(0)
const hasMore = ref(true)
const photoStyles = PHOTO_STYLES

async function search(refresh = false): Promise<void> {
  const value = keyword.value.trim()
  if (!value || loading.value || (!refresh && !hasMore.value)) return
  if (refresh) {
    searched.value = true
    page.value = 0
    hasMore.value = true
    items.value = []
  }
  loading.value = true
  try {
    const result = await ypatApi.getRecommendList({ page: page.value, size: 10, city: value })
    const content = result.data?.content || []
    items.value = refresh ? content : items.value.concat(content)
    const current = result.data?.number ?? page.value
    hasMore.value = current + 1 < (result.data?.totalPages || 0)
    page.value = current + 1
  } catch (error) {
    uni.showToast({ title: error instanceof Error ? error.message : '搜索失败', icon: 'none' })
  } finally {
    loading.value = false
  }
}

function useTag(value: string): void { keyword.value = value; search(true) }
function chargeLabel(value: string): string { return CHARGE_WAY_LABELS[value] || '费用面议' }
function openDetail(id: number): void { uni.navigateTo({ url: `/pages-sub/ypat/detail?id=${id}` }) }
function back(): void { uni.navigateBack() }

onReachBottom(() => search(false))
</script>

<style scoped lang="scss">
@import '@/styles/tokens.scss';

.page { min-height: 100vh; background: $color-bg-page; }
.search-bar { position: fixed; z-index: 10; top: 0; left: 0; right: 0; display: flex; align-items: center; gap: 18rpx; padding-left: 28rpx; padding-right: 28rpx; padding-bottom: 18rpx; background: $color-bg-page; }
.input { flex: 1; height: 76rpx; padding: 0 30rpx; border-radius: 38rpx; background: $color-bg-chip; font-weight: 700; }
.cancel { color: $color-text-primary; font-weight: 800; }
.content { padding-left: 28rpx; padding-right: 28rpx; padding-bottom: 50rpx; }
.section-title { font-size: 34rpx; font-weight: 900; }
.tags { display: flex; flex-wrap: wrap; gap: 18rpx; margin-top: 26rpx; }
.tag { padding: 14rpx 28rpx; border-radius: 999rpx; color: $color-text-secondary; background: #fff; font-weight: 800; }
.state, .footer { padding: 120rpx 20rpx; color: $color-text-helper; text-align: center; }
.footer { padding: 28rpx; }
.card { display: flex; margin-bottom: 22rpx; padding: 22rpx; overflow: hidden; border-radius: 32rpx; background: #fff; box-shadow: 0 6rpx 24rpx rgba(20, 24, 31, .04); }
.cover { width: 204rpx; height: 220rpx; border-radius: 24rpx; background: $color-bg-chip; }
.body { flex: 1; min-width: 0; padding: 6rpx 0 6rpx 22rpx; }
.name, .desc { display: block; }
.name { color: $color-text-primary; font-size: 30rpx; font-weight: 800; }
.desc { margin: 16rpx 0; color: #596270; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.meta { display: flex; justify-content: space-between; color: $color-text-helper; font-size: 23rpx; font-weight: 700; }
</style>
