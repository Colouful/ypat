<template>
  <view class="records-page">
    <!-- 页面标题 -->
    <view class="page-header">
      <text class="page-title">消费记录</text>
    </view>

    <!-- 加载状态 -->
    <view v-if="loading && recordList.length === 0" class="loading-wrapper">
      <view class="loading-spinner" />
      <text class="loading-text">加载中...</text>
    </view>

    <!-- 空状态 -->
    <view v-else-if="!loading && recordList.length === 0" class="empty-wrapper">
      <view class="empty-icon">&#128203;</view>
      <text class="empty-text">暂无消费记录</text>
    </view>

    <!-- 记录列表 -->
    <view v-else class="record-list">
      <view
        v-for="record in recordList"
        :key="record.id"
        class="record-item"
      >
        <view class="record-badge" :class="getBadgeClass(record.type)">
          <text class="record-badge-text">{{ getBadgeIcon(record.type) }}</text>
        </view>
        <view class="record-info">
          <text class="record-desc">{{ record.content || getTypeLabel(record.type) }}</text>
          <text class="record-date">{{ formatDate(record.createTime) }}</text>
        </view>
        <view class="record-amount" :class="isIncome(record.type) ? 'amount-income' : 'amount-expense'">
          <text>{{ isIncome(record.type) ? '+' : '-' }}{{ getAmountDisplay(record) }}</text>
        </view>
      </view>

      <!-- 加载更多 -->
      <view v-if="loadingMore" class="load-more">
        <text class="load-more-text">加载中...</text>
      </view>
      <view v-else-if="noMore" class="load-more">
        <text class="load-more-text">没有更多了</text>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { onLoad, onPullDownRefresh, onReachBottom } from '@dcloudio/uni-app'
import { useUserStore } from '@/stores/user'
import * as paymentApi from '@/api/modules/payment'
import { RECORD_TYPE_LABELS, RecordType } from '@/constants/enums'

interface RecordInfo {
  id: number
  userId: number
  type: number
  targetId: number
  targetType: number
  content: string
  createTime: string
}

const userStore = useUserStore()

const loading = ref(false)
const loadingMore = ref(false)
const noMore = ref(false)
const recordList = ref<RecordInfo[]>([])
const currentPage = ref(1)
const pageSize = 15

/** 收入类型：充值、邀请奖励、系统赠送 */
const incomeTypes = [RecordType.TOPUP, RecordType.INVITE, RecordType.SYSTEM]

/** 判断是否为收入 */
function isIncome(type: number): boolean {
  return incomeTypes.includes(String(type))
}

/** 获取类型标签 */
function getTypeLabel(type: number): string {
  return RECORD_TYPE_LABELS[String(type)] || '其他'
}

/** 获取类型徽章图标文字 */
function getBadgeIcon(type: number): string {
  const typeStr = String(type)
  const iconMap: Record<string, string> = {
    [RecordType.TOPUP]: '充',
    [RecordType.INVITE]: '邀',
    [RecordType.SYSTEM]: '赠',
    [RecordType.PUBLISH]: '发',
    [RecordType.APPLY]: '报',
    [RecordType.VIEW_CONTACT]: '查',
  }
  return iconMap[typeStr] || '其'
}

/** 获取徽章样式类 */
function getBadgeClass(type: number): string {
  const typeStr = String(type)
  if (incomeTypes.includes(typeStr)) {
    return 'badge-income'
  }
  return 'badge-expense'
}

/** 获取金额显示 */
function getAmountDisplay(record: RecordInfo): string {
  // content 中如果含有数字则提取，否则显示类型
  const match = record.content?.match(/(\d+)/)
  return match ? match[1] + ' PPD' : '- PPD'
}

/** 格式化日期 */
function formatDate(dateStr: string): string {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  const hour = String(date.getHours()).padStart(2, '0')
  const minute = String(date.getMinutes()).padStart(2, '0')
  return `${year}-${month}-${day} ${hour}:${minute}`
}

/** 获取记录列表 */
async function fetchRecords(isRefresh = false) {
  if (!userStore.userInfo?.id) {
    uni.showToast({ title: '请先登录', icon: 'none' })
    return
  }

  if (isRefresh) {
    currentPage.value = 1
    noMore.value = false
  }

  if (currentPage.value === 1) {
    loading.value = true
  } else {
    loadingMore.value = true
  }

  try {
    const res = await paymentApi.getRecordList({
      page: currentPage.value,
      size: pageSize,
      userId: Number(userStore.userInfo.id),
    })

    if (res.data) {
      const list = res.data.content || []
      if (isRefresh) {
        recordList.value = list
      } else {
        recordList.value = [...recordList.value, ...list]
      }

      // 判断是否还有更多
      if (list.length < pageSize || recordList.value.length >= res.data.totalElements) {
        noMore.value = true
      }
    }
  } catch (e) {
    uni.showToast({ title: '获取记录失败', icon: 'none' })
  } finally {
    loading.value = false
    loadingMore.value = false
  }
}

onLoad(() => {
  fetchRecords(true)
})

onPullDownRefresh(async () => {
  await fetchRecords(true)
  uni.stopPullDownRefresh()
})

onReachBottom(() => {
  if (!noMore.value && !loadingMore.value) {
    currentPage.value++
    fetchRecords(false)
  }
})
</script>

<style lang="scss">
@import '@/styles/tokens.scss';

.records-page {
  min-height: 100vh;
  background-color: $color-bg-page;
  padding: $spacing-xl;
}

.page-header {
  margin-bottom: $spacing-xl;

  .page-title {
    display: block;
    font-size: $font-size-xxl;
    font-weight: $font-weight-bold;
    color: $color-text-primary;
  }
}

.loading-wrapper {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 120rpx 0;

  .loading-spinner {
    width: 48rpx;
    height: 48rpx;
    border: 4rpx solid $color-border;
    border-top-color: $color-primary;
    border-radius: 50%;
    animation: spin 0.8s linear infinite;
  }

  .loading-text {
    margin-top: $spacing-md;
    font-size: $font-size-sm;
    color: $color-text-helper;
  }
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

.empty-wrapper {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 200rpx 0;

  .empty-icon {
    font-size: 96rpx;
    margin-bottom: $spacing-lg;
    opacity: 0.4;
  }

  .empty-text {
    font-size: $font-size-base;
    color: $color-text-helper;
  }
}

.record-list {
  display: flex;
  flex-direction: column;
  gap: $spacing-md;
}

.record-item {
  display: flex;
  align-items: center;
  background-color: $color-bg-card;
  border-radius: $radius-lg;
  padding: $spacing-lg;
  box-shadow: $shadow-sm;
}

.record-badge {
  width: 72rpx;
  height: 72rpx;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  margin-right: $spacing-lg;

  &.badge-income {
    background-color: rgba(35, 194, 104, 0.12);
  }

  &.badge-expense {
    background-color: rgba(255, 77, 79, 0.12);
  }

  .record-badge-text {
    font-size: $font-size-sm;
    font-weight: $font-weight-semibold;

    .badge-income & {
      color: $color-primary;
    }

    .badge-expense & {
      color: $color-accent-red;
    }
  }
}

.record-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;

  .record-desc {
    font-size: $font-size-base;
    font-weight: $font-weight-medium;
    color: $color-text-primary;
    margin-bottom: $spacing-xs;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  .record-date {
    font-size: $font-size-sm;
    color: $color-text-helper;
  }
}

.record-amount {
  flex-shrink: 0;
  margin-left: $spacing-md;
  font-size: $font-size-md;
  font-weight: $font-weight-semibold;

  &.amount-income {
    color: $color-primary;
  }

  &.amount-expense {
    color: $color-accent-red;
  }
}

.load-more {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: $spacing-xl 0;

  .load-more-text {
    font-size: $font-size-sm;
    color: $color-text-helper;
  }
}
</style>
