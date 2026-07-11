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

    <view v-if="applyTarget" class="author-card">
      <image class="author-card__avatar" :src="authorAvatar" mode="aspectFill" />
      <view class="author-card__body">
        <text class="author-card__name">{{ applyTarget.name }}</text>
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
      <text class="tips-card__text">提交后会消耗拍豆，作者可通过联系方式与你沟通。若对方要求绕开平台付款，请谨慎处理。</text>
    </view>

    <view class="bottom-bar">
      <view class="bottom-bar__cost" :class="{ 'bottom-bar__cost--warning': isBalanceInsufficient }">
        <view class="cost-metric">
          <text class="bottom-bar__label">剩余拍豆</text>
          <view class="cost-metric__line">
            <text class="bottom-bar__value">{{ currentPpd }}</text>
            <text class="bottom-bar__unit">拍豆</text>
          </view>
        </view>
        <view class="bottom-bar__divider" />
        <view class="cost-metric">
          <text class="bottom-bar__label">本次消耗</text>
          <view class="cost-metric__line">
            <text class="bottom-bar__value bottom-bar__value--cost">{{ applyCost }}</text>
            <text class="bottom-bar__unit">拍豆</text>
          </view>
        </view>
      </view>
      <button
        class="bottom-bar__submit"
        :class="{
          'bottom-bar__submit--disabled': submitDisabled,
          'bottom-bar__submit--warning': isBalanceInsufficient && !submitDisabled,
        }"
        :loading="submitting"
        hover-class="none"
        @tap="submitApply"
      >
        {{ isAlreadyApplied ? '已约拍' : '确认提交' }}
      </button>
    </view>
  </view>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { onLoad, onShow } from '@dcloudio/uni-app'
import KeepIcon from '@/components/business/KeepIcon.vue'
import KeepPageNav from '@/components/business/KeepPageNav.vue'
import { getDetail, quickApply } from '@/api/modules/work'
import * as ypatApi from '@/api/modules/ypat'
import { normalizeImageUrl } from '@/api/adapters'
import { APPLY_PPD, getProfessLabel, TARGET_LABELS } from '@/constants/enums'
import { useUserStore } from '@/stores/user'
import { preloadMessageSubscribeTemplates, requestMessageSubscribe } from '@/utils/subscribe-message'
import type { YpatInfo } from '@/api/types'
import type { WorkDetail } from '@/api/types/work'

const SAFETY_KEY = 'ypat_work_apply_safety_until'

const userStore = useUserStore()
const workId = ref(0)
const ypatId = ref(0)
const work = ref<WorkDetail | null>(null)
const ypat = ref<YpatInfo | null>(null)
const reason = ref('')
const mobile = ref('')
const wx = ref('')
const submitting = ref(false)

const applyCost = APPLY_PPD
const currentPpd = computed(() => Number(userStore.userInfo?.ppd || 0))
const isBalanceInsufficient = computed(() => currentPpd.value < applyCost)
const isAlreadyApplied = computed(() => ypatId.value ? ypat.value?.msgflag === '1' : work.value?.isApplied === true)
const applyTarget = computed(() => {
  if (ypat.value) {
    return {
      name: ypat.value.userQo?.nickname || '匿名用户',
      avatar: ypat.value.userQo?.imgpath || ypat.value.userQo?.avatarurl,
    }
  }
  if (work.value) {
    return {
      name: work.value.user?.nickname || '匿名用户',
      avatar: work.value.user?.avatar,
    }
  }
  return null
})
const authorAvatar = computed(() => normalizeImageUrl(applyTarget.value?.avatar) || '/static/default-avatar.png')
const authorMeta = computed(() => {
  if (ypat.value) {
    const target = TARGET_LABELS[ypat.value.target] || ypat.value.targetTxt
    return [target, ypat.value.city].filter(Boolean).join(' · ') || '约拍发布者'
  }
  const user = work.value?.user
  return [getProfessLabel(user?.profession || ''), user?.city].filter(Boolean).join(' · ') || '作品作者'
})
const submitDisabled = computed(() => (
  submitting.value
  || isAlreadyApplied.value
  || reason.value.trim().length < 8
  || (!mobile.value.trim() && !wx.value.trim())
))

async function loadWork(): Promise<WorkDetail | null> {
  if (!workId.value) return null
  try {
    work.value = (await getDetail(workId.value)).data || null
  } catch {
    work.value = null
  }
  return work.value
}

async function loadYpat(): Promise<YpatInfo | null> {
  if (!ypatId.value) return null
  try {
    ypat.value = (await ypatApi.getDetail(ypatId.value, userStore.userInfo?.id)).data || null
  } catch {
    ypat.value = null
  }
  return ypat.value
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

async function refreshPpdBalance(): Promise<number> {
  try {
    const latestUser = await userStore.updateUserInfo()
    return Number(latestUser?.ppd ?? userStore.userInfo?.ppd ?? 0)
  } catch {
    return currentPpd.value
  }
}

function showRechargeGuide(balance = currentPpd.value): void {
  uni.showModal({
    title: '拍豆余额不足',
    content: `当前剩余 ${balance} 拍豆，本次提交需要 ${applyCost} 拍豆。充值后再回来提交约拍申请吧。`,
    confirmText: '去充值',
    cancelText: '稍后再说',
    success: ({ confirm }) => confirm && uni.navigateTo({ url: '/pages-sub/user/recharge' }),
  })
}

async function submitApply(): Promise<void> {
  if (submitDisabled.value || (!workId.value && !ypatId.value)) return
  submitting.value = true
  try {
    const latestTarget = ypatId.value ? await loadYpat() : await loadWork()
    if (!latestTarget) {
      uni.showToast({ title: '约拍信息加载失败', icon: 'none' })
      return
    }
    if (isAlreadyApplied.value) {
      uni.showToast({ title: '你已提交过该约拍', icon: 'none' })
      return
    }
    const latestPpd = await refreshPpdBalance()
    if (latestPpd < applyCost) {
      showRechargeGuide(latestPpd)
      return
    }
    await requestMessageSubscribe('apply')
    if (ypatId.value) {
      await ypatApi.applyYpat({
        ypatid: ypatId.value,
        content: buildYpatApplyContent(),
      })
    } else {
      await quickApply({
        workId: workId.value,
        reason: reason.value.trim(),
        mobile: mobile.value.trim(),
        wx: wx.value.trim(),
      })
    }
    uni.showToast({ title: '约拍申请已提交', icon: 'success' })
    void userStore.updateUserInfo()
    void userStore.refreshUnreadCount()
    setTimeout(() => uni.navigateBack(), 800)
  } catch (error) {
    uni.showToast({ title: error instanceof Error ? error.message : '提交失败', icon: 'none' })
  } finally {
    submitting.value = false
  }
}

function buildYpatApplyContent(): string {
  const contacts = [
    mobile.value.trim() ? `手机号：${mobile.value.trim()}` : '',
    wx.value.trim() ? `微信：${wx.value.trim()}` : '',
  ].filter(Boolean)
  return [reason.value.trim(), ...contacts].join('\n')
}

onLoad((options) => {
  ypatId.value = Number(options?.ypatId || 0)
  workId.value = ypatId.value ? 0 : Number(options?.workId || options?.id || 0)
  mobile.value = userStore.userInfo?.mobile || ''
  wx.value = userStore.userInfo?.wx || ''
  if (ypatId.value) void loadYpat()
  else void loadWork()
  void refreshPpdBalance()
  void preloadMessageSubscribeTemplates()
  if (shouldShowSafety()) setTimeout(showSafetyModal, 220)
})

onShow(() => {
  void refreshPpdBalance()
})
</script>

<style scoped lang="scss">
.apply-page {
  min-height: 100vh;
  padding: 28rpx 28rpx calc(190rpx + env(safe-area-inset-bottom));
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
  gap: 18rpx;
  padding: 18rpx 28rpx calc(18rpx + env(safe-area-inset-bottom));
  background: rgba(255, 255, 255, 0.96);
  box-shadow: 0 -10rpx 30rpx rgba(20, 24, 31, 0.08);
}

.bottom-bar__cost {
  display: flex;
  align-items: center;
  flex: 1;
  min-width: 0;
  padding: 16rpx 18rpx;
  border: 2rpx solid rgba(35, 194, 104, 0.14);
  border-radius: 26rpx;
  background: linear-gradient(135deg, rgba(35, 194, 104, 0.08), rgba(255, 255, 255, 0.96));
}

.bottom-bar__cost--warning {
  border-color: rgba(255, 143, 31, 0.28);
  background: linear-gradient(135deg, rgba(255, 143, 31, 0.11), rgba(255, 255, 255, 0.96));
}

.cost-metric {
  min-width: 0;
  flex: 1;
}

.cost-metric__line {
  display: flex;
  align-items: baseline;
  gap: 6rpx;
  margin-top: 4rpx;
  white-space: nowrap;
}

.bottom-bar__divider {
  width: 2rpx;
  height: 54rpx;
  margin: 0 16rpx;
  background: rgba(131, 136, 143, 0.16);
}

.bottom-bar__label {
  display: block;
  color: $color-text-helper;
  font-size: 22rpx;
  font-weight: 800;
}

.bottom-bar__value {
  display: block;
  max-width: 112rpx;
  overflow: hidden;
  color: $color-accent-orange;
  font-size: 30rpx;
  font-weight: 900;
  line-height: 1.1;
  text-overflow: ellipsis;
}

.bottom-bar__value--cost {
  color: $color-text-primary;
}

.bottom-bar__unit {
  color: $color-text-secondary;
  font-size: 20rpx;
  font-weight: 800;
}

.bottom-bar__submit {
  @include flex-center;
  box-sizing: border-box;
  width: 216rpx;
  flex: none;
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

.bottom-bar__submit--warning {
  background: linear-gradient(135deg, $color-accent-orange, #FFB15C);
}

.bottom-bar__submit::after {
  border: 0;
}
</style>
