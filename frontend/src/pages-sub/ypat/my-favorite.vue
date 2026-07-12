<template>
  <view class="favorite-page">
    <KeepPageNav title="我的收藏" />
    <view class="favorite-tabs">
      <view
        v-for="tab in tabs"
        :key="tab.value"
        :class="['favorite-tabs__item', { 'favorite-tabs__item--active': activeTab === tab.value }]"
        @tap="switchTab(tab.value)"
      >
        <text>{{ tab.label }}</text>
      </view>
    </view>

    <view class="favorite-content">
      <view
        v-if="currentLoading && !currentItemCount"
        class="favorite-state"
      >
        加载中...
      </view>
      <view
        v-else-if="!currentItemCount"
        class="favorite-state"
      >
        {{ activeTab === 'ypat' ? '暂无收藏的约拍' : '暂无收藏的作品' }}
      </view>

      <template v-else-if="activeTab === 'ypat'">
        <view
          v-for="item in ypatState.items"
          :key="item.id"
          class="favorite-card"
          @tap="openYpat(item.id)"
        >
          <image
            class="favorite-card__cover"
            :src="item.pics?.[0] || '/static/default-cover.png'"
            mode="aspectFill"
          />
          <view class="favorite-card__body">
            <view class="favorite-card__header">
              <text class="favorite-card__name">
                {{ item.userQo?.nickname || '匿名用户' }}
              </text>
              <text class="favorite-card__time">
                {{ item.timeStr || formatTime(item.pubdate) }}
              </text>
            </view>
            <text class="favorite-card__desc">
              {{ item.describ || '约拍信息' }}
            </text>
            <view class="favorite-card__meta">
              <text>{{ item.city || '地点待协商' }}</text>
              <text>{{ getChargeLabel(item.chargeway, item.chargeamt) }}</text>
            </view>
          </view>
        </view>
      </template>

      <template v-else>
        <view
          v-for="item in workState.items"
          :key="item.id"
          class="favorite-card"
          @tap="openWork(item.id)"
        >
          <view class="favorite-card__cover-wrap">
            <image
              class="favorite-card__cover"
              :src="item.coverUrl || '/static/default-cover.png'"
              mode="aspectFill"
            />
            <text
              v-if="item.isVideo === '1'"
              class="favorite-card__video"
            >
              视频
            </text>
          </view>
          <view class="favorite-card__body">
            <view
              v-if="item.tags?.length"
              class="favorite-card__tags"
            >
              <text
                v-for="tag in item.tags.slice(0, 2)"
                :key="tag"
                class="favorite-card__tag"
              >
                {{ tag }}
              </text>
            </view>
            <text class="favorite-card__desc">
              {{ item.description || '摄影作品' }}
            </text>
            <text class="favorite-card__author">
              {{ item.nickname || '匿名用户' }} · {{ getProfessionLabel(item.profession) }}
            </text>
            <view class="favorite-card__meta">
              <text>{{ item.city || '地区未填写' }}</text>
              <text class="favorite-card__count">
                <KeepIcon
                  name="star"
                  :size="22"
                  color="#23C268"
                />
                {{ item.favoriteCount || 0 }}
              </text>
            </view>
          </view>
        </view>
      </template>

      <view
        v-if="currentLoadingMore"
        class="favorite-footer"
      >
        加载中...
      </view>
      <view
        v-else-if="currentItemCount && !currentHasMore"
        class="favorite-footer"
      >
        没有更多了
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { computed, reactive, ref } from 'vue'
import { onPullDownRefresh, onReachBottom, onShow } from '@dcloudio/uni-app'
import KeepIcon from '@/components/business/KeepIcon.vue'
import KeepPageNav from '@/components/business/KeepPageNav.vue'
import { getFavoriteWorks } from '@/api/modules/work'
import * as ypatApi from '@/api/modules/ypat'
import { CHARGE_WAY_LABELS, getProfessLabel } from '@/constants/enums'
import { useUserStore } from '@/stores/user'
import type { YpatInfo } from '@/api/types'
import type { WorkListItem } from '@/api/types/work'

type FavoriteTab = 'ypat' | 'work'

interface ListState<T> {
  items: T[]
  page: number
  loading: boolean
  loadingMore: boolean
  hasMore: boolean
  loaded: boolean
}

function createListState<T>(page: number): ListState<T> {
  return reactive({
    items: [],
    page,
    loading: false,
    loadingMore: false,
    hasMore: true,
    loaded: false,
  }) as ListState<T>
}

const size = 10
const userStore = useUserStore()
const activeTab = ref<FavoriteTab>('ypat')
const tabs: Array<{ label: string; value: FavoriteTab }> = [
  { label: '约拍', value: 'ypat' },
  { label: '作品', value: 'work' },
]
const ypatState = createListState<YpatInfo>(0)
const workState = createListState<WorkListItem>(1)
const currentLoading = computed(() => activeTab.value === 'ypat' ? ypatState.loading : workState.loading)
const currentLoadingMore = computed(() => activeTab.value === 'ypat' ? ypatState.loadingMore : workState.loadingMore)
const currentHasMore = computed(() => activeTab.value === 'ypat' ? ypatState.hasMore : workState.hasMore)
const currentItemCount = computed(() => activeTab.value === 'ypat' ? ypatState.items.length : workState.items.length)

async function loadYpat(refresh = false): Promise<void> {
  const userid = userStore.userInfo?.id
  if (!userid || ypatState.loading || ypatState.loadingMore || (!refresh && !ypatState.hasMore)) return
  if (refresh) {
    ypatState.page = 0
    ypatState.hasMore = true
    ypatState.loading = true
  } else {
    ypatState.loadingMore = true
  }
  try {
    const result = await ypatApi.getMyFavoriteList({ userid, page: ypatState.page, size })
    const content = result.data?.content || []
    ypatState.items = refresh ? content : ypatState.items.concat(content)
    const current = result.data?.number ?? ypatState.page
    ypatState.hasMore = current + 1 < (result.data?.totalPages || 0)
    ypatState.page = current + 1
    ypatState.loaded = true
  } catch (error) {
    uni.showToast({ title: error instanceof Error ? error.message : '约拍收藏加载失败', icon: 'none' })
  } finally {
    ypatState.loading = false
    ypatState.loadingMore = false
  }
}

async function loadWork(refresh = false): Promise<void> {
  if (workState.loading || workState.loadingMore || (!refresh && !workState.hasMore)) return
  if (refresh) {
    workState.page = 1
    workState.hasMore = true
    workState.loading = true
  } else {
    workState.loadingMore = true
  }
  try {
    const result = await getFavoriteWorks({ page: workState.page, size })
    const content = result.data?.items || []
    workState.items = refresh ? content : workState.items.concat(content)
    const current = result.data?.page ?? workState.page
    workState.hasMore = result.data?.hasMore ?? workState.items.length < (result.data?.total || 0)
    workState.page = current + 1
    workState.loaded = true
  } catch (error) {
    uni.showToast({ title: error instanceof Error ? error.message : '作品收藏加载失败', icon: 'none' })
  } finally {
    workState.loading = false
    workState.loadingMore = false
  }
}

function loadCurrent(refresh = false): Promise<void> {
  return activeTab.value === 'ypat' ? loadYpat(refresh) : loadWork(refresh)
}

function switchTab(tab: FavoriteTab): void {
  if (activeTab.value === tab) return
  activeTab.value = tab
  const loaded = tab === 'ypat' ? ypatState.loaded : workState.loaded
  if (!loaded) void loadCurrent(true)
}

function getChargeLabel(chargeway: string, chargeamt?: number): string {
  if (Number(chargeamt || 0) > 0) return `¥${chargeamt}`
  return CHARGE_WAY_LABELS[chargeway] || '费用协商'
}

function getProfessionLabel(value?: string): string {
  return value ? getProfessLabel(value) : '摄影爱好者'
}

function formatTime(value?: string): string {
  if (!value) return ''
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return value
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`
}

function openYpat(id: number): void {
  uni.navigateTo({ url: `/pages-sub/ypat/detail?id=${id}` })
}

function openWork(id: number): void {
  uni.navigateTo({ url: `/pages-sub/work/detail?id=${id}` })
}

onShow(() => { void loadCurrent(true) })
onPullDownRefresh(async () => {
  await loadCurrent(true)
  uni.stopPullDownRefresh()
})
onReachBottom(() => { void loadCurrent() })
</script>

<style scoped lang="scss">
.favorite-page {
  min-height: 100vh;
  background: $color-bg-page;
}

.favorite-tabs {
  display: flex;
  height: 92rpx;
  padding: 0 28rpx;
  border-bottom: 1rpx solid $color-border;
  background: $color-bg-card;
}

.favorite-tabs__item {
  position: relative;
  @include flex-center;
  flex: 1;
  color: $color-text-helper;
  font-size: 28rpx;
  font-weight: 700;
}

.favorite-tabs__item--active {
  color: $color-primary-dark;
  font-weight: 900;
}

.favorite-tabs__item--active::after {
  position: absolute;
  right: 30%;
  bottom: 0;
  left: 30%;
  height: 6rpx;
  border-radius: 6rpx 6rpx 0 0;
  background: $color-primary;
  content: '';
}

.favorite-content {
  padding: 24rpx 28rpx 40rpx;
}

.favorite-state {
  padding: 140rpx 20rpx;
  color: $color-text-helper;
  text-align: center;
}

.favorite-card {
  display: flex;
  gap: 22rpx;
  margin-bottom: 20rpx;
  padding: 20rpx;
  overflow: hidden;
  border: 1rpx solid $color-border;
  border-radius: $radius-keep-card;
  background: $color-bg-card;
  box-shadow: 0 8rpx 24rpx rgba(31, 55, 40, 0.06);
}

.favorite-card__cover-wrap {
  position: relative;
  width: 196rpx;
  height: 216rpx;
  flex: none;
}

.favorite-card__cover {
  width: 196rpx;
  height: 216rpx;
  flex: none;
  border-radius: 18rpx;
  background: $color-bg-chip;
}

.favorite-card__video {
  position: absolute;
  top: 10rpx;
  right: 10rpx;
  padding: 4rpx 12rpx;
  border-radius: 10rpx;
  color: #fff;
  background: rgba(0, 0, 0, 0.55);
  font-size: 20rpx;
}

.favorite-card__body {
  min-width: 0;
  flex: 1;
  padding: 4rpx 0;
}

.favorite-card__header,
.favorite-card__meta {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12rpx;
}

.favorite-card__name {
  min-width: 0;
  overflow: hidden;
  color: $color-text-primary;
  font-weight: 900;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.favorite-card__time {
  flex: none;
  color: $color-text-helper;
  font-size: 22rpx;
  font-weight: 700;
}

.favorite-card__tags {
  display: flex;
  gap: 8rpx;
  overflow: hidden;
}

.favorite-card__tag {
  padding: 4rpx 12rpx;
  border-radius: 8rpx;
  color: $color-primary-dark;
  background: $color-primary-light;
  font-size: 20rpx;
  font-weight: 800;
  white-space: nowrap;
}

.favorite-card__desc {
  display: -webkit-box;
  margin: 16rpx 0;
  overflow: hidden;
  color: $color-text-secondary;
  font-size: 27rpx;
  font-weight: 800;
  line-height: 1.45;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
}

.favorite-card__author {
  display: block;
  overflow: hidden;
  color: $color-text-helper;
  font-size: 22rpx;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.favorite-card__meta {
  margin-top: 16rpx;
  color: $color-text-helper;
  font-size: 22rpx;
  font-weight: 700;
}

.favorite-card__count {
  display: inline-flex;
  align-items: center;
  gap: 6rpx;
  color: $color-primary-dark;
}

.favorite-footer {
  padding: 28rpx;
  color: $color-text-helper;
  text-align: center;
}
</style>
