<template>
  <view class="bills-page">
    <!-- 页面标题 -->
    <view class="bills-header">
      <text class="bills-header__title">账单明细</text>
    </view>

    <!-- 账单列表 -->
    <view v-if="billList.length > 0" class="bills-list">
      <view
        v-for="item in billList"
        :key="item.id"
        class="bill-item"
      >
        <view class="bill-item__left">
          <text class="bill-item__desc">{{ item.description }}</text>
          <text class="bill-item__time">{{ formatTime(item.credate) }}</text>
        </view>
        <view class="bill-item__right">
          <text
            class="bill-item__amount"
            :class="{
              'bill-item__amount--positive': item.amount > 0,
              'bill-item__amount--negative': item.amount < 0,
            }"
          >
            {{ item.amount > 0 ? '+' : '' }}{{ item.amount }}
          </text>
          <text class="bill-item__balance">余额: {{ item.balance }}</text>
        </view>
      </view>
    </view>

    <!-- 空状态 -->
    <view v-else-if="!loading" class="bills-empty">
      <text class="bills-empty__icon">📋</text>
      <text class="bills-empty__text">暂无账单记录</text>
    </view>

    <!-- 加载状态 -->
    <view v-if="loading" class="bills-loading">
      <text class="bills-loading__text">加载中...</text>
    </view>

    <!-- 底部提示 -->
    <view v-if="billList.length > 0 && !hasMore" class="bills-footer">
      <text class="bills-footer__text">没有更多了</text>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { onPullDownRefresh, onReachBottom } from '@dcloudio/uni-app'
import { useUserStore } from '@/stores/user'
import * as paymentApi from '@/api/modules/payment'

interface Bill {
  id: number
  type: number
  total_fee: number
  out_trade_no: string
  credate: string
}

const userStore = useUserStore()

const billList = ref<Bill[]>([])
const loading = ref(false)
const hasMore = ref(true)
const currentPage = ref(1)
const pageSize = 20

/** 格式化时间 */
function formatTime(timeStr: string): string {
  if (!timeStr) return ''
  const date = new Date(timeStr)
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  const hours = String(date.getHours()).padStart(2, '0')
  const minutes = String(date.getMinutes()).padStart(2, '0')
  return `${year}-${month}-${day} ${hours}:${minutes}`
}

/** 加载账单列表 */
async function loadBills(isRefresh = false) {
  if (loading.value) return
  if (!isRefresh && !hasMore.value) return

  loading.value = true

  if (isRefresh) {
    currentPage.value = 1
    hasMore.value = true
  }

  try {
    const userId = Number(userStore.userInfo?.id)
    if (!userId) {
      uni.showToast({ title: '请先登录', icon: 'none' })
      return
    }

    const res = await paymentApi.getBillList({
      page: currentPage.value,
      size: pageSize,
      userId,
    })

    if (res.data) {
      const { content, totalPages, number } = res.data
      if (isRefresh) {
        billList.value = content
      } else {
        billList.value = [...billList.value, ...content]
      }
      currentPage.value = number + 2
      hasMore.value = number + 1 < totalPages
    }
  } catch (error) {
    uni.showToast({ title: '加载失败，请重试', icon: 'none' })
  } finally {
    loading.value = false
  }
}

/** 下拉刷新 */
onPullDownRefresh(async () => {
  await loadBills(true)
  uni.stopPullDownRefresh()
})

/** 上拉加载更多 */
onReachBottom(() => {
  if (hasMore.value && !loading.value) {
    loadBills()
  }
})

onMounted(() => {
  loadBills()
})
</script>

<style lang="scss">
@import '@/styles/tokens.scss';

.bills-page {
  min-height: 100vh;
  background-color: $color-bg-page;
  padding-bottom: $safe-area-inset-bottom;
}

.bills-header {
  background-color: $color-bg-card;
  padding: $spacing-lg;
  border-bottom: 1rpx solid $color-border;

  &__title {
    font-size: $font-size-xl;
    font-weight: $font-weight-bold;
    color: $color-text-primary;
  }
}

.bills-list {
  padding: $spacing-md;
}

.bill-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background-color: $color-bg-card;
  padding: $spacing-lg;
  margin-bottom: $spacing-sm;
  border-radius: $radius-md;
  box-shadow: $shadow-sm;

  &__left {
    flex: 1;
    display: flex;
    flex-direction: column;
    margin-right: $spacing-md;
  }

  &__desc {
    font-size: $font-size-base;
    font-weight: $font-weight-medium;
    color: $color-text-primary;
    margin-bottom: $spacing-xs;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  &__time {
    font-size: $font-size-sm;
    color: $color-text-helper;
  }

  &__right {
    display: flex;
    flex-direction: column;
    align-items: flex-end;
  }

  &__amount {
    font-size: $font-size-lg;
    font-weight: $font-weight-bold;

    &--positive {
      color: $color-primary;
    }

    &--negative {
      color: $color-accent-red;
    }
  }

  &__balance {
    font-size: $font-size-xs;
    color: $color-text-helper;
    margin-top: 4rpx;
  }
}

.bills-empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding-top: 200rpx;

  &__icon {
    font-size: 100rpx;
    margin-bottom: $spacing-lg;
  }

  &__text {
    font-size: $font-size-base;
    color: $color-text-helper;
  }
}

.bills-loading {
  display: flex;
  justify-content: center;
  padding: $spacing-lg;

  &__text {
    font-size: $font-size-sm;
    color: $color-text-secondary;
  }
}

.bills-footer {
  display: flex;
  justify-content: center;
  padding: $spacing-lg;

  &__text {
    font-size: $font-size-sm;
    color: $color-text-helper;
  }
}
</style>
