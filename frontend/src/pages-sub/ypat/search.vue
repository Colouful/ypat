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
.page { min-height: 100vh; background: #f7f8fa; }
.search-bar { position: fixed; z-index: 10; top: 0; left: 0; right: 0; display: flex; align-items: center; gap: 18rpx; padding-left: 24rpx; padding-right: 24rpx; padding-bottom: 14rpx; background: #fff; }
.input { flex: 1; height: 68rpx; padding: 0 24rpx; border-radius: 34rpx; background: #f1f3f5; }
.cancel { color: #596270; }
.content { padding-left: 24rpx; padding-right: 24rpx; padding-bottom: 50rpx; }
.section-title { font-size: 30rpx; font-weight: 600; }
.tags { display: flex; flex-wrap: wrap; gap: 16rpx; margin-top: 24rpx; }
.tag { padding: 14rpx 24rpx; border-radius: 28rpx; background: #fff; }
.state, .footer { padding: 180rpx 20rpx; color: #929aa7; text-align: center; }
.footer { padding: 28rpx; }
.card { display: flex; margin-bottom: 18rpx; overflow: hidden; border-radius: 22rpx; background: #fff; }
.cover { width: 210rpx; height: 190rpx; }
.body { flex: 1; min-width: 0; padding: 22rpx; }
.name, .desc { display: block; }
.name { font-weight: 600; }
.desc { margin: 14rpx 0; color: #596270; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.meta { display: flex; justify-content: space-between; color: #929aa7; font-size: 23rpx; }
</style>
