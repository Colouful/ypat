<template>
  <view class="apply-page">
    <KeepPageNav title="立即约拍" />

    <view class="safe-card" @tap="showSafetyModal">
      <view class="safe-card__icon">
        <KeepIcon name="shield" :size="38" color="#17A857" />
      </view>
      <view class="safe-card__body">
        <text class="safe-card__title">安全防骗提醒</text>
        <text class="safe-card__desc">平台不会要求私下转账，见面前请确认身份与拍摄方案。</text>
      </view>
      <KeepIcon name="chevron-right" :size="30" color="#83888F" />
    </view>

    <view v-if="work" class="author-card">
      <image class="author-card__avatar" :src="authorAvatar" mode="aspectFill" />
      <view class="author-card__body">
        <text class="author-card__name">{{ work.user?.nickname || '匿名用户' }}</text>
        <text class="author-card__meta">{{ authorMeta }}</text>
      </view>
    </view>

    <view class="form-card">
      <text class="field-title">约拍理由</text>
      <textarea
        class="textarea"
        maxlength="200"
        :value="reason"
        placeholder="简单说说想合作的风格、时间或拍摄想法"
        placeholder-class="textarea__placeholder"
        @input="onReasonInput"
      />
      <text class="counter">{{ reason.length }}/200</text>

      <text class="field-title field-title--space">我的联系方式</text>
      <input v-model="mobile" class="input" maxlength="11" type="number" placeholder="手机号" />
      <input v-model="wx" class="input" maxlength="40" placeholder="微信号" />
    </view>

    <view class="tips-card">
      <text class="tips-card__title">温馨提示</text>
      <text class="tips-card__text">提交后会消耗拍拍豆，作者可通过联系方式与你沟通。若对方要求绕开平台付款，请谨慎处理。</text>
    </view>

    <view class="bottom-bar">
      <view class="bottom-bar__cost">
        <text class="bottom-bar__label">本次消耗</text>
        <text class="bottom-bar__value">1 拍拍豆</text>
      </view>
      <button
        class="bottom-bar__submit"
        :class="{ 'bottom-bar__submit--disabled': submitDisabled }"
        :loading="submitting"
        hover-class="none"
        @tap="submitApply"
      >
        确认提交
      </button>
    </view>
  </view>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import KeepIcon from '@/components/business/KeepIcon.vue'
import KeepPageNav from '@/components/business/KeepPageNav.vue'
import { getDetail, quickApply } from '@/api/modules/work'
import { normalizeImageUrl } from '@/api/adapters'
import { getProfessLabel } from '@/constants/enums'
import { useUserStore } from '@/stores/user'
import type { WorkDetail } from '@/api/types/work'

const SAFETY_KEY = 'ypat_work_apply_safety_until'

const userStore = useUserStore()
const workId = ref(0)
const work = ref<WorkDetail | null>(null)
const reason = ref('')
const mobile = ref('')
const wx = ref('')
const submitting = ref(false)

const authorAvatar = computed(() => normalizeImageUrl(work.value?.user?.avatar) || '/static/default-avatar.png')
const authorMeta = computed(() => {
  const user = work.value?.user
  return [getProfessLabel(user?.profession || ''), user?.city].filter(Boolean).join(' · ') || '作品作者'
})
const submitDisabled = computed(() => (
  submitting.value
  || reason.value.trim().length < 8
  || (!mobile.value.trim() && !wx.value.trim())
))

async function loadWork(): Promise<void> {
  if (!workId.value) return
  try {
    work.value = (await getDetail(workId.value)).data || null
  } catch {
    work.value = null
  }
}

function shouldShowSafety(): boolean {
  const until = Number(uni.getStorageSync(SAFETY_KEY) || 0)
  return Date.now() > until
}

function showSafetyModal(): void {
  uni.showModal({
    title: '安全防骗提醒',
    content: '不要提前私下转账，不要点击陌生链接。建议先确认对方实名信息、拍摄地点、交付内容和费用规则。',
    confirmText: '近期不再提醒',
    cancelText: '知道了',
    success: ({ confirm }) => {
      if (confirm) uni.setStorageSync(SAFETY_KEY, Date.now() + 7 * 24 * 60 * 60 * 1000)
    },
  })
}

function onReasonInput(event: Event): void {
  const detail = (event as unknown as { detail?: { value?: string } }).detail
  const target = event.target as HTMLTextAreaElement | null
  reason.value = String(detail?.value ?? target?.value ?? '')
}

async function submitApply(): Promise<void> {
  if (submitDisabled.value || !workId.value) return
  submitting.value = true
  try {
    await quickApply({
      workId: workId.value,
      reason: reason.value.trim(),
      mobile: mobile.value.trim(),
      wx: wx.value.trim(),
    })
    uni.showToast({ title: '约拍申请已提交', icon: 'success' })
    setTimeout(() => uni.navigateBack(), 800)
  } catch (error) {
    uni.showToast({ title: error instanceof Error ? error.message : '提交失败', icon: 'none' })
  } finally {
    submitting.value = false
  }
}

onLoad((options) => {
  workId.value = Number(options?.workId || options?.id || 0)
  mobile.value = userStore.userInfo?.mobile || ''
  wx.value = userStore.userInfo?.wx || ''
  void loadWork()
  if (shouldShowSafety()) setTimeout(showSafetyModal, 220)
})
</script>

<style scoped lang="scss">
.apply-page {
  min-height: 100vh;
  padding: 28rpx 28rpx calc(148rpx + env(safe-area-inset-bottom));
  background: $color-bg-page;
}

.safe-card,
.author-card,
.form-card,
.tips-card {
  border-radius: 30rpx;
  background: #FFFFFF;
  box-shadow: $shadow-keep-card;
}

.safe-card {
  display: flex;
  align-items: center;
  gap: 18rpx;
  padding: 24rpx;
  border: 2rpx solid rgba(35, 194, 104, 0.18);
}

.safe-card__icon {
  @include flex-center;
  width: 72rpx;
  height: 72rpx;
  flex: none;
  border-radius: 24rpx;
  background: $color-primary-soft;
}

.safe-card__body {
  min-width: 0;
  flex: 1;
}

.safe-card__title,
.author-card__name,
.field-title,
.tips-card__title {
  display: block;
  color: $color-text-primary;
  font-weight: 900;
}

.safe-card__title {
  font-size: 30rpx;
}

.safe-card__desc {
  display: block;
  margin-top: 8rpx;
  color: $color-text-secondary;
  font-size: 24rpx;
  line-height: 1.45;
}

.author-card {
  display: flex;
  align-items: center;
  gap: 18rpx;
  margin-top: 24rpx;
  padding: 24rpx;
}

.author-card__avatar {
  width: 92rpx;
  height: 92rpx;
  flex: none;
  border-radius: 50%;
  background: $color-bg-chip;
}

.author-card__body {
  min-width: 0;
  flex: 1;
}

.author-card__name {
  overflow: hidden;
  font-size: 32rpx;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.author-card__meta {
  display: block;
  margin-top: 8rpx;
  color: $color-text-secondary;
  font-size: 24rpx;
  font-weight: 700;
}

.form-card {
  margin-top: 24rpx;
  padding: 28rpx;
}

.field-title {
  font-size: 30rpx;
}

.field-title--space {
  margin-top: 28rpx;
}

.textarea {
  box-sizing: border-box;
  width: 100%;
  height: 220rpx;
  margin-top: 18rpx;
  padding: 22rpx;
  border-radius: 22rpx;
  background: $color-bg-page;
  color: $color-text-primary;
  font-size: 27rpx;
  line-height: 1.6;
}

.counter {
  display: block;
  margin-top: 10rpx;
  color: $color-text-helper;
  font-size: 23rpx;
  font-weight: 700;
  text-align: right;
}

.input {
  box-sizing: border-box;
  width: 100%;
  height: 92rpx;
  margin-top: 16rpx;
  padding: 0 24rpx;
  border-radius: 22rpx;
  background: $color-bg-page;
  color: $color-text-primary;
  font-size: 28rpx;
  font-weight: 700;
}

.tips-card {
  margin-top: 24rpx;
  padding: 26rpx;
}

.tips-card__title {
  font-size: 28rpx;
}

.tips-card__text {
  display: block;
  margin-top: 12rpx;
  color: $color-text-secondary;
  font-size: 24rpx;
  line-height: 1.65;
}

.bottom-bar {
  position: fixed;
  right: 0;
  bottom: 0;
  left: 0;
  z-index: 100;
  display: flex;
  align-items: center;
  gap: 24rpx;
  padding: 18rpx 28rpx calc(18rpx + env(safe-area-inset-bottom));
  background: rgba(255, 255, 255, 0.96);
  box-shadow: 0 -10rpx 30rpx rgba(20, 24, 31, 0.08);
}

.bottom-bar__cost {
  min-width: 190rpx;
}

.bottom-bar__label {
  display: block;
  color: $color-text-helper;
  font-size: 22rpx;
  font-weight: 800;
}

.bottom-bar__value {
  display: block;
  margin-top: 4rpx;
  color: $color-accent-orange;
  font-size: 30rpx;
  font-weight: 900;
}

.bottom-bar__submit {
  @include flex-center;
  box-sizing: border-box;
  flex: 1;
  min-width: 0;
  height: 88rpx;
  margin: 0;
  padding: 0 28rpx;
  border: 0;
  border-radius: $radius-round;
  color: #FFFFFF;
  background: $color-primary;
  font-size: 30rpx;
  font-weight: 900;
  line-height: 1;
  box-shadow: $shadow-keep-button;
  white-space: nowrap;
}

.bottom-bar__submit--disabled {
  color: #A7ADB4;
  background: #EEF2F1;
  box-shadow: none;
}

.bottom-bar__submit::after {
  border: 0;
}
</style>
