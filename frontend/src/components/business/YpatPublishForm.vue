<template>
  <view class="publish-form">
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
      <textarea v-model="model.describ" class="field-card__textarea" maxlength="500" placeholder="拍摄主题、风格、时间地点、出片要求..." />
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
      <input
        v-if="model.chargeway === '1' || model.chargeway === '2'"
        v-model.number="model.chargeamt"
        class="price-input"
        type="digit"
        placeholder="请输入参考金额"
      />
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
        <view class="city-field">{{ regionText || '请选择城市' }}</view>
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

    <text class="publish-tip">发布需要 3 个拍拍豆，当前余额 {{ userStore.userInfo?.ppd || 0 }}</text>
    <view class="publish-submit">
      <button class="publish-submit__button" :disabled="!canSubmit || submitting || processing" :loading="submitting" @tap="submit">
        {{ submitting ? '提交中...' : '发布约拍' }}
      </button>
    </view>
  </view>
</template>

<script setup lang="ts">
import { computed, reactive, ref } from 'vue'
import { useUserStore } from '@/stores/user'
import * as ypatApi from '@/api/modules/ypat'
import { filePathToBase64 } from '@/utils/file-base64'
import { PHOTO_STYLES } from '@/constants/enums'
import KeepIcon from './KeepIcon.vue'

const userStore = useUserStore()
const today = new Date().toISOString().slice(0, 10)
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
  patdate: today,
  province: '上海市',
  city: '上海市',
  area: '徐汇区',
  patarea: '',
  chargeway: '0',
  chargeamt: 0,
  patstyle: '',
  realnameflag: '0',
  creditflag: '0',
})

const regionValue = computed(() => [model.province, model.city, model.area])
const regionText = computed(() => [model.city.replace('市', ''), model.area].filter(Boolean).join(' · '))
const chargeAmountValid = computed(() => {
  if (model.chargeway !== '1' && model.chargeway !== '2') return true
  return Number(model.chargeamt) > 0
})
const canSubmit = computed(() => Boolean(
  title.value.trim()
  && model.describ.trim()
  && model.patdate
  && model.province.trim()
  && model.city.trim()
  && localPaths.value.length
  && chargeAmountValid.value
))

function changeRegion(event: { detail: { value: string[] } }): void {
  const [province = '', city = '', area = ''] = event.detail.value
  model.province = province
  model.city = city
  model.area = area
}

function selectChargeWay(value: string): void {
  model.chargeway = value
  if (value === '0' || value === '3') model.chargeamt = 0
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
      if (!base64Cache.value[index]) base64Cache.value[index] = await filePathToBase64(localPaths.value[index])
      processedCount.value += 1
    }
    return base64Cache.value.filter(Boolean)
  } finally {
    processing.value = false
  }
}

async function submit(): Promise<void> {
  if (!canSubmit.value || submitting.value || processing.value) return
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

function reset(): void {
  Object.assign(model, {
    target: '0',
    describ: '',
    patdate: today,
    province: '上海市',
    city: '上海市',
    area: '徐汇区',
    patarea: '',
    chargeway: '0',
    chargeamt: 0,
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
  min-height: 100vh;
  padding: 24rpx 36rpx 180rpx;
  background: $color-bg-page;
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
  height: 164rpx;
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
  min-height: 208rpx;
}

.field-card__label {
  display: block;
  margin-bottom: 20rpx;
  color: $color-text-helper;
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

.field-card__textarea {
  width: 100%;
  height: 128rpx;
  color: $color-text-primary;
  font-size: 32rpx;
  line-height: 1.5;
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

.price-input {
  height: 76rpx;
  margin-top: 22rpx;
  padding: 0 24rpx;
  border-radius: 24rpx;
  background: $color-bg-chip;
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
  position: fixed;
  right: 0;
  bottom: 0;
  left: 0;
  padding: 28rpx 36rpx calc(36rpx + env(safe-area-inset-bottom));
  background: linear-gradient(transparent, #fff 28%);
}

.publish-submit__button {
  @include keep-primary-button;
  width: 100%;
  line-height: 104rpx;
}

.publish-submit__button[disabled] {
  opacity: 0.45;
}
</style>
