<template>
  <view class="publish-form">
    <view class="section">
      <text class="title">约拍对象</text>
      <view class="options">
        <view v-for="item in targets" :key="item.value" class="option" :class="{ active: model.target === item.value }" @tap="model.target = item.value">
          {{ item.label }}
        </view>
      </view>
    </view>

    <view class="section">
      <text class="title">约拍说明</text>
      <textarea v-model="model.describ" class="textarea" maxlength="500" placeholder="请描述拍摄需求、时间和地点" />
    </view>

    <view class="section">
      <text class="title">拍摄日期</text>
      <picker mode="date" :start="today" :value="model.patdate" @change="changeDate">
        <view class="picker">{{ model.patdate || '请选择日期' }}</view>
      </picker>
    </view>

    <view class="section">
      <text class="title">拍摄地区</text>
      <picker mode="region" :value="regionValue" @change="changeRegion">
        <view class="picker">{{ regionText || '请选择省、市、区' }}</view>
      </picker>
      <input v-model="model.patarea" class="input address-input" maxlength="100" placeholder="详细拍摄地点（选填）" />
    </view>

    <view class="section">
      <text class="title">收费方式</text>
      <view class="options">
        <view v-for="item in chargeWays" :key="item.value" class="option" :class="{ active: model.chargeway === item.value }" @tap="selectChargeWay(item.value)">
          {{ item.label }}
        </view>
      </view>
      <input v-if="model.chargeway === '1' || model.chargeway === '2'" v-model.number="model.chargeamt" class="input price-input" type="digit" placeholder="请输入参考金额" />
    </view>

    <view class="section">
      <text class="title">约拍图片</text>
      <view class="images">
        <view v-for="(path, index) in localPaths" :key="path" class="image-item">
          <image :src="path" mode="aspectFill" />
          <view class="remove" @tap.stop="removeImage(index)">×</view>
        </view>
        <view v-if="localPaths.length < 9" class="add" @tap="chooseImages">+</view>
      </view>
      <text v-if="processing" class="progress">正在处理 {{ processedCount }}/{{ localPaths.length }}</text>
    </view>

    <text class="tip">发布需要 3 个拍拍豆，当前余额 {{ userStore.userInfo?.ppd || 0 }}</text>
    <button class="submit" :disabled="!canSubmit || submitting || processing" :loading="submitting" @tap="submit">
      {{ submitting ? '提交中...' : '发布约拍' }}
    </button>
  </view>
</template>

<script setup lang="ts">
import { computed, reactive, ref } from 'vue'
import { useUserStore } from '@/stores/user'
import * as ypatApi from '@/api/modules/ypat'
import { filePathToBase64 } from '@/utils/file-base64'

const userStore = useUserStore()
const today = new Date().toISOString().slice(0, 10)
const submitting = ref(false)
const processing = ref(false)
const processedCount = ref(0)
const localPaths = ref<string[]>([])
const base64Cache = ref<string[]>([])

const targets = [{ value: '0', label: '约摄影师' }, { value: '1', label: '约模特' }]
const chargeWays = [
  { value: '0', label: '互免' },
  { value: '1', label: '我要收费' },
  { value: '2', label: '可以付费' },
  { value: '3', label: '费用协商' },
]

const model = reactive({
  target: '1',
  describ: '',
  patdate: '',
  province: '',
  city: '',
  area: '',
  patarea: '',
  chargeway: '0',
  chargeamt: 0,
  patstyle: '',
  realnameflag: '0',
  creditflag: '0',
})

const regionValue = computed(() => [model.province, model.city, model.area])
const regionText = computed(() => [model.province, model.city, model.area].filter(Boolean).join(' '))
const chargeAmountValid = computed(() => {
  if (model.chargeway !== '1' && model.chargeway !== '2') return true
  return Number(model.chargeamt) > 0
})
const canSubmit = computed(() => Boolean(
  model.describ.trim()
  && model.patdate
  && model.province.trim()
  && model.city.trim()
  && localPaths.value.length
  && chargeAmountValid.value
))

function changeDate(event: { detail: { value: string } }): void {
  model.patdate = event.detail.value
}

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
    await ypatApi.submit({ ...model, pics })
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
    target: '1', describ: '', patdate: '', province: '', city: '', area: '', patarea: '',
    chargeway: '0', chargeamt: 0, patstyle: '', realnameflag: '0', creditflag: '0',
  })
  localPaths.value = []
  base64Cache.value = []
  processedCount.value = 0
}
</script>

<style scoped lang="scss">
.publish-form { padding: 28rpx; padding-bottom: 140rpx; background: #f7f8fa; }
.section { margin-bottom: 22rpx; padding: 28rpx; border-radius: 24rpx; background: #fff; }
.title { display: block; margin-bottom: 18rpx; color: #1d2433; font-size: 29rpx; font-weight: 600; }
.options { display: flex; flex-wrap: wrap; gap: 14rpx; }
.option { padding: 14rpx 24rpx; border-radius: 28rpx; color: #606a78; background: #f1f3f5; }
.option.active { color: #fff; background: #23c268; }
.textarea, .picker, .input { box-sizing: border-box; width: 100%; padding: 20rpx; border-radius: 16rpx; background: #f7f8fa; }
.textarea { min-height: 210rpx; }
.address-input, .price-input { margin-top: 16rpx; }
.images { display: flex; flex-wrap: wrap; gap: 14rpx; }
.image-item, .add { position: relative; width: 190rpx; height: 190rpx; overflow: hidden; border-radius: 18rpx; background: #eef1f4; }
.image-item image { width: 100%; height: 100%; }
.add { display: flex; align-items: center; justify-content: center; color: #929aa7; font-size: 64rpx; }
.remove { position: absolute; top: 8rpx; right: 8rpx; width: 40rpx; height: 40rpx; border-radius: 50%; color: #fff; background: rgba(0,0,0,.55); text-align: center; }
.progress, .tip { display: block; margin: 18rpx 0; color: #7c8593; font-size: 25rpx; }
.submit { height: 92rpx; line-height: 92rpx; border-radius: 46rpx; color: #fff; background: #23c268; }
.submit[disabled] { opacity: .45; }
.submit::after { border: 0; }
</style>
