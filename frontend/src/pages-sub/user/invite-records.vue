<template>
  <view class="page">
    <KeepPageNav title="邀请记录" />

    <view v-if="records.length === 0 && !loading" class="empty">
      <KeepState type="empty" title="还没有邀请记录" description="邀请好友加入爱去拍，成功注册后奖励会显示在这里" />
    </view>

    <view v-else class="list">
      <view v-for="item in records" :key="item.id" class="row">
        <image class="row__avatar" :src="item.inviteeImgpath || '/static/default-avatar.png'" mode="aspectFill" />
        <view class="row__body">
          <text class="row__name">{{ item.inviteeNickname || item.inviteeMobileMask || '新用户' }}</text>
          <text class="row__time">{{ formatTime(item.credate) }}</text>
        </view>
        <text class="row__reward">+{{ item.rewardPpd ?? 0 }} 拍拍豆</text>
      </view>

      <view v-if="loading" class="footer">加载中…</view>
      <view v-else-if="!hasMore" class="footer">没有更多了</view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { onLoad, onPullDownRefresh, onReachBottom } from '@dcloudio/uni-app'
import KeepPageNav from '@/components/business/KeepPageNav.vue'
import KeepState from '@/components/business/KeepState.vue'
import * as inviteApi from '@/api/modules/invite'
import type { InviteRecord } from '@/api/types'

const records = ref<InviteRecord[]>([])
const loading = ref(false)
const hasMore = ref(true)
const page = ref(0)
const size = 10

async function load(reset = false): Promise<void> {
  if (loading.value) return
  if (!reset && !hasMore.value) return
  loading.value = true
  if (reset) {
    page.value = 0
    hasMore.value = true
  }
  try {
    const result = await inviteApi.getInviteRecords({ page: page.value, size })
    const content = result.data?.content ?? []
    records.value = reset ? content : records.value.concat(content)
    const total = result.data?.totalElements ?? 0
    hasMore.value = records.value.length < total
    page.value += 1
  } catch {
    hasMore.value = false
  } finally {
    loading.value = false
  }
}

function formatTime(value?: string): string {
  if (!value) return ''
  // 后端 ISO 字符串 GMT+8，无需二次转换；截断到分钟即可。
  return value.replace('T', ' ').slice(0, 16)
}

onLoad(() => {
  void load(true)
})

onPullDownRefresh(async () => {
  await load(true)
  uni.stopPullDownRefresh()
})

onReachBottom(() => {
  void load()
})
</script>

<style scoped lang="scss">
.page { min-height: 100vh; padding: 24rpx 28rpx calc(60rpx + env(safe-area-inset-bottom)); background: $color-bg-page; }
.empty { margin-top: 80rpx; }

.list { padding: 6rpx 0; }
.row { display: flex; align-items: center; gap: 18rpx; padding: 22rpx; margin-bottom: 14rpx; border-radius: $radius-keep-card; background: #fff; box-shadow: $shadow-keep-card; }
.row__avatar { width: 84rpx; height: 84rpx; flex: none; border-radius: 50%; background: $color-bg-chip; }
.row__body { flex: 1; min-width: 0; }
.row__name { display: block; color: $color-text-primary; font-size: 28rpx; font-weight: 800; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.row__time { display: block; margin-top: 6rpx; color: $color-text-helper; font-size: 22rpx; font-weight: 700; }
.row__reward { flex: none; color: $color-accent-gold; font-size: 26rpx; font-weight: 900; }

.footer { padding: 28rpx 0; color: $color-text-helper; font-size: 24rpx; text-align: center; }
</style>
