<template>
  <view class="publish-form">
    <view class="publish-summary">
      <view>
        <text class="publish-summary__label">当前拍拍豆</text>
        <text class="publish-summary__value">{{ userStore.userInfo?.ppd || 0 }}</text>
      </view>
      <view class="publish-summary__cost">
        <KeepIcon name="coins" :size="30" color="#FF9F1C" />
        <text>发布消耗 3</text>
      </view>
    </view>

    <view class="target-picker">
      <view
        v-for="item in targets"
        :key="item.value"
        class="target-picker__item"
        :class="{ 'target-picker__item--active': model.target === item.value }"
        @tap="model.target = item.value"
      >
        <KeepIcon :name="item.icon" :size="56" />
        <text>{{ item.label }}</text>
      </view>
    </view>

    <view class="field-card">
      <text class="field-card__label">标题</text>
      <input v-model="title" class="field-card__input" maxlength="60" placeholder="一句话说明你想拍什么" />
    </view>

    <view class="field-card field-card--textarea">
      <text class="field-card__label">详细描述</text>
      <textarea v-model="model.describ" class="field-card__textarea" maxlength="200" placeholder="拍摄主题、风格、时间地点、出片要求..." />
      <text class="field-card__count">{{ model.describ.length }}/200</text>
    </view>

    <view class="field-card">
      <text class="field-card__label">合作方式</text>
      <view class="option-wrap">
        <view
          v-for="item in chargeWays"
          :key="item.value"
          class="option-pill"
          :class="{ 'option-pill--active': model.chargeway === item.value }"
          @tap="selectChargeWay(item.value)"
        >
          {{ item.label }}
        </view>
      </view>
      <view v-if="model.chargeway === '1' || model.chargeway === '2'" class="price-input-field">
        <input
          :value="model.chargeamt"
          class="price-input"
          type="number"
          maxlength="8"
          placeholder="请输入金额"
          @input="changeChargeAmount"
        />
        <view v-if="model.chargeamt" class="price-input__clear" @tap.stop="clearChargeAmount">
          <KeepIcon name="close" :size="22" color="#83888F" :stroke-width="3" />
        </view>
      </view>
    </view>

    <view class="field-card">
      <text class="field-card__label">拍摄风格（可多选）</text>
      <view class="option-wrap">
        <view
          v-for="style in styles"
          :key="style"
          class="option-pill"
          :class="{ 'option-pill--active': selectedStyles.includes(style) }"
          @tap="toggleStyle(style)"
        >
          {{ style }}
        </view>
      </view>
    </view>

    <view class="field-card">
      <text class="field-card__label">城市</text>
      <picker mode="region" :value="regionValue" @change="changeRegion">
        <view class="city-field" :class="{ 'city-field--placeholder': !regionText }">{{ regionText || '请选择城市' }}</view>
      </picker>
    </view>

    <view class="field-card">
      <text class="field-card__label">拍摄日期</text>
      <picker mode="date" :value="model.patdate" :start="minPatdate" @change="changePatdate">
        <view class="city-field" :class="{ 'city-field--placeholder': !model.patdate }">{{ model.patdate || '请选择拍摄日期' }}</view>
      </picker>
    </view>

    <view class="field-card">
      <text class="field-card__label">参考图 / 样片</text>
      <view class="upload-grid">
        <view v-for="(path, index) in localPaths" :key="path" class="upload-grid__item">
          <image :src="path" mode="aspectFill" />
          <view class="upload-grid__remove" @tap.stop="removeImage(index)">
            <KeepIcon name="close" :size="22" color="#FFFFFF" :stroke-width="3" />
          </view>
        </view>
        <view v-if="localPaths.length < 9" class="upload-grid__add" @tap="chooseImages">
          <KeepIcon name="plus-circle" :size="42" />
          <text>上传</text>
        </view>
      </view>
      <text v-if="processing" class="publish-tip">正在处理 {{ processedCount }}/{{ localPaths.length }}</text>
    </view>

    <text class="publish-tip">提交后将进入审核，审核通过后展示在广场。</text>
    <view class="publish-submit">
      <button class="publish-submit__button" :class="{ 'publish-submit__button--disabled': submitting || processing }" :disabled="submitting || processing" :loading="submitting" @tap="submit">
        {{ submitting ? '提交中...' : '发布约拍' }}
      </button>
    </view>
  </view>
</template>

<script setup lang="ts">
import { computed, reactive, ref } from 'vue'
import { useUserStore } from '@/stores/user'
import * as ypatApi from '@/api/modules/ypat'
import { FEATURE_FLAGS } from '@/config/features'
import { filePathToDataUrl } from '@/utils/file-base64'
import { isPublishProfileReady } from '@/utils/profile'
import { normalizePositiveIntegerInput, toPositiveIntegerAmount } from '@/utils/amount'
import { PHOTO_STYLES } from '@/constants/enums'
import KeepIcon from './KeepIcon.vue'
import type { UserInfo } from '@/api/types'

const userStore = useUserStore()
const today = formatLocalDate()
const minPatdate = formatLocalDate(1)
const submitting = ref(false)
const processing = ref(false)
const processedCount = ref(0)
const localPaths = ref<string[]>([])
const base64Cache = ref<string[]>([])
const title = ref('')
const selectedStyles = ref<string[]>(['INS', '胶片', '情绪'])

const targets = [
  { value: '0', label: '我要约摄影师', icon: 'camera' },
  { value: '1', label: '我要约模特', icon: 'user' },
]
const chargeWays = [
  { value: '0', label: '希望互免' },
  { value: '1', label: '我要收费' },
  { value: '2', label: '可付费' },
  { value: '3', label: '费用协商' },
]
const styles = PHOTO_STYLES.slice(0, 9)

const model = reactive({
  target: '0',
  describ: '',
  patdate: '',
  province: '',
  city: '',
  area: '',
  patarea: '',
  chargeway: '0',
  chargeamt: '',
  patstyle: '',
  realnameflag: '0',
  creditflag: '0',
})

const regionValue = computed(() => (model.province && model.city ? [model.province, model.city, model.area] : []))
const regionText = computed(() => [model.city.replace('市', ''), model.area].filter(Boolean).join(' · '))
const chargeAmountValid = computed(() => {
  if (model.chargeway !== '1' && model.chargeway !== '2') return true
  return toPositiveIntegerAmount(model.chargeamt) !== undefined
})
const chargeAmountValue = computed(() => (
  model.chargeway === '1' || model.chargeway === '2'
    ? toPositiveIntegerAmount(model.chargeamt)
    : 0
))
function formatLocalDate(offsetDays = 0): string {
  const date = new Date()
  date.setDate(date.getDate() + offsetDays)
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}

function changeRegion(event: { detail: { value: string[] } }): void {
  const [province = '', city = '', area = ''] = event.detail.value
  model.province = province
  model.city = city
  model.area = area
}

function selectChargeWay(value: string): void {
  model.chargeway = value
  if (value === '0' || value === '3') model.chargeamt = ''
}

function changeChargeAmount(event: Event): string {
  const detailValue = (event as Event & { detail?: { value?: string | number } }).detail?.value
  const targetValue = (event as Event & { target?: { value?: string | number } }).target?.value
  const normalized = normalizePositiveIntegerInput(detailValue ?? targetValue)
  model.chargeamt = normalized
  return normalized
}

function clearChargeAmount(): void {
  model.chargeamt = ''
}

function changePatdate(event: { detail: { value: string } }): void {
  const value = event.detail.value
  if (value <= today) {
    model.patdate = ''
    uni.showToast({ title: '请选择明天及以后的日期', icon: 'none' })
    return
  }
  model.patdate = value
}

function toggleStyle(style: string): void {
  selectedStyles.value = selectedStyles.value.includes(style)
    ? selectedStyles.value.filter((item) => item !== style)
    : selectedStyles.value.concat(style)
}

function chooseImages(): void {
  uni.chooseImage({
    count: 9 - localPaths.value.length,
    sizeType: ['compressed'],
    sourceType: ['album', 'camera'],
    success: (result) => {
      localPaths.value.push(...result.tempFilePaths)
      base64Cache.value.length = localPaths.value.length
    },
  })
}

function removeImage(index: number): void {
  localPaths.value.splice(index, 1)
  base64Cache.value.splice(index, 1)
}

async function convertImages(): Promise<string[]> {
  processing.value = true
  processedCount.value = 0
  try {
    for (let index = 0; index < localPaths.value.length; index += 1) {
      if (!base64Cache.value[index]) base64Cache.value[index] = await filePathToDataUrl(localPaths.value[index])
      processedCount.value += 1
    }
    return base64Cache.value.filter(Boolean)
  } finally {
    processing.value = false
  }
}

async function submit(): Promise<void> {
  if (submitting.value || processing.value) return
  if (!validateSubmit()) return
  if (!FEATURE_FLAGS.deposit && model.creditflag === '1') {
    model.creditflag = '0'
    uni.showToast({ title: '当前版本暂未开放保证金服务，已关闭保证金要求', icon: 'none' })
  }
  // 发布前置: 提交前刷新一次,避免从编辑资料返回后仍拿到旧 store。
  const cachedUser = userStore.userInfo
  const latestUser = await userStore.updateUserInfo()
  if (!isPublishProfileReady(mergeNonEmptyUser(latestUser, cachedUser))) {
    uni.showModal({
      title: '请先完善资料',
      content: '发布前需补全性别、昵称、头像和微信号，方便对方联系你。',
      confirmText: '去完善',
      success: ({ confirm }) => confirm && uni.navigateTo({ url: '/pages-sub/user/edit-info' }),
    })
    return
  }
  if ((userStore.userInfo?.ppd || 0) < 3) {
    uni.showModal({
      title: '拍拍豆不足',
      content: '是否前往充值？',
      confirmText: '去充值',
      success: ({ confirm }) => confirm && uni.navigateTo({ url: '/pages-sub/user/recharge' }),
    })
    return
  }

  submitting.value = true
  try {
    const pics = await convertImages()
    if (pics.length !== localPaths.value.length) throw new Error('部分图片处理失败')
    await ypatApi.submit({
      ...model,
      creditflag: FEATURE_FLAGS.deposit ? model.creditflag : '0',
      chargeamt: chargeAmountValue.value,
      describ: `${title.value.trim()}\n${model.describ.trim()}`,
      patstyle: selectedStyles.value.join(','),
      pics,
    })
    uni.showToast({ title: '发布成功，等待审核', icon: 'success' })
    reset()
    await userStore.updateUserInfo()
  } catch (error) {
    uni.showToast({ title: error instanceof Error ? error.message : '发布失败，请重试', icon: 'none' })
  } finally {
    submitting.value = false
  }
}

function mergeNonEmptyUser(
  latest: UserInfo | null | undefined,
  cached: UserInfo | null | undefined,
): UserInfo | null {
  if (!latest && !cached) return null
  const merged = { ...(latest || {}), ...(pickNonEmpty(cached) || {}) }
  return merged.id ? merged as UserInfo : null
}

function pickNonEmpty(user: UserInfo | null | undefined): Partial<UserInfo> {
  if (!user) return {}
  return Object.fromEntries(
    Object.entries(user).filter(([, value]) => value !== undefined && value !== null && value !== ''),
  ) as Partial<UserInfo>
}

function validateSubmit(): boolean {
  if (!title.value.trim()) {
    uni.showToast({ title: '请输入标题', icon: 'none' })
    return false
  }
  if (model.describ.trim().length < 6) {
    uni.showToast({ title: '拍摄需求至少 6 个字', icon: 'none' })
    return false
  }
  if (!model.patdate) {
    uni.showToast({ title: '请选择拍摄日期', icon: 'none' })
    return false
  }
  if (model.patdate <= today) {
    uni.showToast({ title: '请选择明天及以后的日期', icon: 'none' })
    return false
  }
  if (!model.province.trim() || !model.city.trim()) {
    uni.showToast({ title: '请选择城市', icon: 'none' })
    return false
  }
  if (!localPaths.value.length) {
    uni.showToast({ title: '请上传参考图', icon: 'none' })
    return false
  }
  if (!chargeAmountValid.value) {
    uni.showToast({ title: '请输入正整数金额', icon: 'none' })
    return false
  }
  return true
}

function reset(): void {
  Object.assign(model, {
    target: '0',
    describ: '',
    patdate: '',
    province: '',
    city: '',
    area: '',
    patarea: '',
    chargeway: '0',
    chargeamt: '',
    patstyle: '',
    realnameflag: '0',
    creditflag: '0',
  })
  title.value = ''
  selectedStyles.value = ['INS', '胶片', '情绪']
  localPaths.value = []
  base64Cache.value = []
  processedCount.value = 0
}
</script>

<style scoped lang="scss">

.publish-form {
  padding: 0 36rpx 36rpx;
  background: $color-bg-page;
}

.publish-summary {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 24rpx;
  padding: 26rpx 30rpx;
  border-radius: $radius-keep-field;
  background: $color-bg-card;
  box-shadow: $shadow-keep-card;
}

.publish-summary__label,
.publish-summary__value {
  display: block;
}

.publish-summary__label {
  color: $color-text-secondary;
  font-size: 24rpx;
  font-weight: 800;
}

.publish-summary__value {
  margin-top: 4rpx;
  color: $color-text-primary;
  font-size: 44rpx;
  font-weight: 900;
}

.publish-summary__cost {
  display: inline-flex;
  align-items: center;
  gap: 8rpx;
  padding: 14rpx 18rpx;
  border-radius: $radius-round;
  color: #9C7836;
  background: $color-gold-soft;
  font-size: 24rpx;
  font-weight: 900;
}

.target-picker {
  display: flex;
  gap: 20rpx;
  margin-bottom: 32rpx;
}

.target-picker__item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 152rpx;
  border: 4rpx solid $color-border;
  border-radius: $radius-keep-field;
  color: $color-text-primary;
  background: $color-bg-card;
  font-size: 32rpx;
  font-weight: 800;
}

.target-picker__item text {
  margin-top: 14rpx;
}

.target-picker__item--active {
  color: $color-primary-dark;
  border-color: $color-primary;
  background: #F0FCF5;
}

.field-card {
  margin-bottom: 26rpx;
  padding: 28rpx 32rpx;
  border-radius: $radius-keep-field;
  background: $color-bg-card;
  box-shadow: $shadow-keep-card;
}

.field-card--textarea {
  min-height: 248rpx;
}

.field-card__label {
  display: block;
  margin-bottom: 20rpx;
  color: $color-text-secondary;
  font-size: 28rpx;
  font-weight: 800;
}

.field-card__input,
.city-field {
  width: 100%;
  color: $color-text-primary;
  font-size: 32rpx;
  font-weight: 600;
}

.city-field.city-field--placeholder {
  color: $color-text-helper;
  font-weight: 500;
}

.field-card__textarea {
  width: 100%;
  height: 142rpx;
  color: $color-text-primary;
  font-size: 32rpx;
  line-height: 1.5;
}

.field-card__count {
  display: block;
  margin-top: 12rpx;
  color: $color-text-helper;
  font-size: 24rpx;
  text-align: right;
}

.option-wrap {
  display: flex;
  flex-wrap: wrap;
  gap: 20rpx;
}

.option-pill {
  @include keep-chip;
}

.option-pill--active {
  @include keep-chip(true);
}

.price-input-field {
  display: flex;
  align-items: center;
  height: 76rpx;
  margin-top: 22rpx;
  padding: 0 24rpx;
  border-radius: 24rpx;
  background: $color-bg-chip;
}

.price-input {
  flex: 1;
  min-width: 0;
  height: 76rpx;
}

.price-input__clear {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 44rpx;
  height: 44rpx;
  margin-left: 14rpx;
}

.upload-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16rpx;
}

.upload-grid__item,
.upload-grid__add {
  position: relative;
  height: 156rpx;
  overflow: hidden;
  border-radius: 24rpx;
  background: $color-bg-chip;
}

.upload-grid__item image {
  width: 100%;
  height: 100%;
}

.upload-grid__remove {
  position: absolute;
  display: flex;
  align-items: center;
  justify-content: center;
  top: 8rpx;
  right: 8rpx;
  width: 40rpx;
  height: 40rpx;
  border-radius: 50%;
  color: #fff;
  background: rgba(0, 0, 0, 0.5);
}

.upload-grid__add {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  border: 4rpx dashed $color-text-helper;
  color: $color-text-helper;
  font-size: 28rpx;
  font-weight: 700;
}

.upload-grid__add text {
  margin-top: 8rpx;
}

.publish-tip {
  display: block;
  margin: 18rpx 0;
  color: $color-text-secondary;
  font-size: 24rpx;
}

.publish-submit {
  margin-top: 26rpx;
  padding-bottom: 16rpx;
}

.publish-submit__button {
  @include keep-primary-button;
  width: 100%;
  color: #fff;
  background: $color-primary;
  background-color: $color-primary;
  line-height: 104rpx;
}

.publish-submit__button::after {
  border: 0;
}

// 组件 WXSS 禁止 [disabled] 属性选择器，改用 class 绑定；保留品牌主色，不用 opacity 淡化
.publish-submit__button--disabled {
  color: #fff;
  background: $color-primary;
  background-color: $color-primary;
}
</style>
