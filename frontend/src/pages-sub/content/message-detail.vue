<template>
  <view class="page">
    <KeepPageNav title="消息详情" />
    <view v-if="loading" class="state">加载中...</view>
    <view v-else-if="error" class="state error">{{ error }}</view>
    <template v-else-if="message">
      <view class="card sender">
        <image class="avatar" :src="message.imgpath || '/static/tab/mine.png'" mode="aspectFill" />
        <view>
          <text class="name">{{ message.nickname || '匿名用户' }}</text>
          <text class="time">{{ message.timeStr || formatTime(message.credate) }}</text>
        </view>
      </view>

      <view class="card">
        <text class="content">{{ message.content || '暂无消息内容' }}</text>
      </view>

      <view v-if="message.ypatid" class="card link" @tap="goYpatDetail">
        <text>查看相关约拍</text>
        <KeepIcon name="chevron-right" :size="34" color="#B3B8BE" />
      </view>

      <view v-if="contactRevealed && contactItems.length" class="card">
        <text class="section-title">联系方式</text>
        <view v-for="item in contactItems" :key="item.label" class="contact-row">
          <text>{{ item.label }}</text>
          <text selectable>{{ item.value }}</text>
        </view>
      </view>

      <button
        v-if="!contactRevealed"
        class="action"
        :class="{ 'action--disabled': quoteLoading || revealing }"
        :loading="revealing"
        :disabled="quoteLoading || revealing"
        @tap="handleViewContact"
      >
        <text v-if="quoteLoading">费用加载中...</text>
        <text v-else-if="quoteFailed">费用加载失败，点击重试</text>
        <text v-else-if="viewContactQuote">查看联系方式（实扣 {{ viewContactCost }} 拍豆）</text>
      </button>
    </template>
  </view>
</template>

<script setup lang="ts">
import KeepPageNav from '@/components/business/KeepPageNav.vue'
import { computed, ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { useUserStore } from '@/stores/user'
import { useMemberStore } from '@/stores/member'
import * as messageApi from '@/api/modules/message'
import * as userApi from '@/api/modules/user'
import type { LinkWay, MessInfo } from '@/api/types'
import KeepIcon from '@/components/business/KeepIcon.vue'

const userStore = useUserStore()
const memberStore = useMemberStore()
const loading = ref(false)
const revealing = ref(false)
const error = ref('')
const message = ref<MessInfo | null>(null)
const contactRevealed = ref(false)
const contactInfo = ref<LinkWay | null>(null)
const messageId = ref(0)
const quoteLoading = ref(true)
const quoteFailed = ref(false)

const viewContactQuote = computed(() => memberStore.quotes.VIEW_CONTACT ?? null)
const viewContactCost = computed(() => viewContactQuote.value?.actualPpd ?? 0)

const contactItems = computed(() => {
  const info = contactInfo.value
  if (!info) return []
  return [
    { label: '手机号', value: info.mobile },
    { label: '微信', value: info.wx },
    { label: 'QQ', value: info.qq },
    { label: '微博', value: info.wb },
  ].filter((item): item is { label: string; value: string } => Boolean(item.value))
})

function formatTime(value?: string): string {
  if (!value) return ''
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return value
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`
}

async function loadDetail(): Promise<void> {
  if (!messageId.value || !userStore.userInfo?.id) {
    error.value = '请先登录'
    return
  }
  loading.value = true
  try {
    message.value = (await messageApi.getMessageDetail(messageId.value, userStore.userInfo.id)).data
    if (message.value?.linkwayflag === '1') {
      await revealContact()
    }
    void userStore.refreshUnreadCount()
  } catch (err) {
    error.value = err instanceof Error ? err.message : '消息加载失败'
  } finally {
    loading.value = false
  }
}

async function refreshContactQuote(): Promise<void> {
  quoteLoading.value = true
  quoteFailed.value = false
  try {
    const result = await memberStore.refreshBenefitQuote('VIEW_CONTACT')
    quoteFailed.value = !result
  } catch {
    quoteFailed.value = true
  } finally {
    quoteLoading.value = false
  }
}

function goYpatDetail(): void {
  if (message.value?.ypatid) uni.navigateTo({ url: `/pages-sub/ypat/detail?id=${message.value.ypatid}` })
}

function handleViewContact(): void {
  if (!userStore.userInfo?.id) {
    uni.showToast({ title: '请先登录', icon: 'none' })
    return
  }
  if (quoteFailed.value) {
    void refreshContactQuote()
    return
  }
  const quote = viewContactQuote.value
  if (!quote || quoteLoading.value) return
  const currentPpd = Number(userStore.userInfo.ppd || 0)
  if (currentPpd < viewContactCost.value) {
    uni.showModal({
      title: '余额不足',
      content: `查看联系方式本次实扣 ${viewContactCost.value} 拍豆，是否前往充值？`,
      confirmText: '去充值',
      success: ({ confirm }) => confirm && uni.navigateTo({ url: '/pages-sub/user/recharge' }),
    })
    return
  }
  uni.showModal({
    title: '确认查看',
    content: quote.discountPpd > 0
      ? `本次实扣 ${quote.actualPpd} 拍豆；原价 ${quote.originalPpd} 拍豆，${quote.levelName || '会员'}优惠 ${quote.discountPpd} 拍豆。`
      : `本次实扣 ${quote.actualPpd} 拍豆；原价 ${quote.originalPpd} 拍豆，暂无会员优惠。`,
    success: ({ confirm }) => confirm && revealContact(),
  })
}

async function revealContact(): Promise<void> {
  if (!message.value || !userStore.userInfo?.id || revealing.value) return
  revealing.value = true
  contactRevealed.value = false
  try {
    const result = await userApi.getLinkWay(message.value.sendperid, message.value.id)
    contactInfo.value = result.data
    contactRevealed.value = true
    await userStore.updateUserInfo()
    await userStore.refreshUnreadCount()
  } catch (err) {
    contactInfo.value = null
    contactRevealed.value = false
    uni.showToast({ title: err instanceof Error ? err.message : '联系方式获取失败', icon: 'none' })
  } finally {
    revealing.value = false
  }
}

onLoad((query) => {
  messageId.value = Number(query?.id || 0)
  void Promise.all([loadDetail(), refreshContactQuote()])
})
</script>

<style scoped lang="scss">

.page { min-height: 100vh; box-sizing: border-box; padding: 28rpx; background: $color-bg-page; }
.state { padding: 220rpx 30rpx; color: $color-text-secondary; text-align: center; }
.error { color: #b4232c; }
.card { margin-bottom: 20rpx; padding: 28rpx; border-radius: $radius-keep-card; background: $color-bg-card; box-shadow: $shadow-keep-card; }
.sender { display: flex; align-items: center; gap: 18rpx; }
.avatar { width: 84rpx; height: 84rpx; border-radius: 50%; }
.name, .time { display: block; }
.name { font-weight: 600; }
.time { margin-top: 8rpx; color: $color-text-helper; font-size: 23rpx; }
.content { color: $color-text-secondary; line-height: 1.7; }
.link, .contact-row { display: flex; justify-content: space-between; }
.section-title { display: block; margin-bottom: 20rpx; font-weight: 600; }
.contact-row { padding: 18rpx 0; border-top: 1rpx solid $color-border; }
.action { margin-top: 36rpx; color: #fff; background: $color-primary; border-radius: 999rpx; }
.action--disabled { color: #A7ADB4; background: #EEF2F1; }
.action::after { border: 0; }
</style>
