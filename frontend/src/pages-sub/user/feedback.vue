<template>
  <view class="feedback-page">
    <KeepPageNav title="意见反馈" />

    <view class="feedback-card">
      <view class="feedback-card__header">
        <text class="feedback-card__title">反馈类型</text>
        <text class="feedback-card__required">*</text>
      </view>
      <view class="type-grid">
        <view
          v-for="item in feedbackTypes"
          :key="item.value"
          class="type-grid__item"
          :class="{ 'type-grid__item--active': selectedType === item.value }"
          @tap="selectedType = item.value"
        >
          {{ item.label }}
        </view>
      </view>
    </view>

    <view class="feedback-card">
      <view class="feedback-card__header">
        <text class="feedback-card__title">反馈内容</text>
        <text class="feedback-card__required">*</text>
      </view>
      <textarea v-model="content" class="feedback-textarea" placeholder="请描述您遇到的问题或建议..." :maxlength="500" />
      <view class="feedback-card__footer">
        <text class="feedback-card__count">{{ content.length }}/500</text>
      </view>
    </view>

    <view class="feedback-card">
      <view class="feedback-card__header feedback-card__header--between">
        <text class="feedback-card__title">反馈图片</text>
        <text class="feedback-card__hint">最多 3 张</text>
      </view>
      <view class="image-grid">
        <view v-for="(item, index) in images" :key="item.localPath" class="image-grid__item">
          <image class="image-grid__image" :src="item.url || item.localPath" mode="aspectFill" @tap="previewFeedbackImage(index)" />
          <view v-if="item.uploading" class="image-grid__mask">上传中</view>
          <view v-else-if="item.error" class="image-grid__mask image-grid__mask--error">失败</view>
          <view class="image-grid__remove" @tap.stop="removeFeedbackImage(index)">×</view>
        </view>
        <view v-if="images.length < 3" class="image-grid__add" @tap="chooseFeedbackImages">
          <text class="image-grid__plus">+</text>
          <text>上传图片</text>
        </view>
      </view>
    </view>

    <view class="contact-card">
      <text class="contact-card__title">联系方式（选填）</text>
      <input v-model="contact" class="contact-input" placeholder="手机号、微信号或邮箱" />
    </view>

    <view class="submit-section" @tap="handleSubmit">
      <view class="submit-btn" :class="{ 'submit-btn--disabled': !isValid || submitting }">
        <text class="submit-btn__text">{{ submitting ? '提交中...' : '提交反馈' }}</text>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import KeepPageNav from '@/components/business/KeepPageNav.vue'
import * as feedbackApi from '@/api/modules/feedback'
import type { FeedbackType } from '@/api/types'
import { useUserStore } from '@/stores/user'
import { chooseImages } from '@/utils/media-uploader'

interface FeedbackImage {
  localPath: string
  url?: string
  uploading: boolean
  error?: string
}

const feedbackTypes: Array<{ label: string; value: FeedbackType }> = [
  { label: '功能异常', value: 'function' },
  { label: '体验建议', value: 'experience' },
  { label: '账号/资料', value: 'account' },
  { label: '支付/订单', value: 'payment' },
  { label: '内容/用户举报', value: 'content' },
  { label: '其他', value: 'other' },
]

const userStore = useUserStore()
const selectedType = ref<FeedbackType>('function')
const content = ref('')
const contact = ref('')
const images = ref<FeedbackImage[]>([])
const submitting = ref(false)

const hasUploadingImage = computed(() => images.value.some((item) => item.uploading))
const hasFailedImage = computed(() => images.value.some((item) => item.error))
const isValid = computed(() => !getValidationMessage())

function getValidationMessage(): string {
  const trimmed = content.value.trim()
  if (!selectedType.value) return '请选择反馈类型'
  if (trimmed.length < 10) return '反馈内容至少输入 10 个字'
  if (trimmed.length > 500) return '反馈内容不能超过 500 个字'
  if (contact.value.trim().length > 100) return '联系方式不能超过 100 个字'
  if (hasUploadingImage.value) return '图片正在上传，请稍后再提交'
  if (hasFailedImage.value) return '请删除上传失败的图片后再提交'
  return ''
}

async function chooseFeedbackImages(): Promise<void> {
  const remain = 3 - images.value.length
  if (remain <= 0) return
  try {
    const files = await chooseImages({ count: remain })
    const pending = files.map((file) => ({ localPath: file.path, uploading: true }))
    images.value = images.value.concat(pending)
    await Promise.all(pending.map(uploadOneImage))
  } catch {
    uni.showToast({ title: '未选择图片', icon: 'none' })
  }
}

async function uploadOneImage(item: FeedbackImage): Promise<void> {
  try {
    const res = await feedbackApi.uploadFeedbackImage(item.localPath)
    item.url = res.data?.url
    item.error = item.url ? undefined : '图片上传失败'
  } catch (error) {
    item.error = error instanceof Error ? error.message : '图片上传失败'
  } finally {
    item.uploading = false
  }
}

function removeFeedbackImage(index: number): void {
  images.value.splice(index, 1)
}

function previewFeedbackImage(index: number): void {
  const urls = images.value.map((item) => item.url || item.localPath).filter(Boolean)
  uni.previewImage({ urls, current: urls[index] })
}

async function handleSubmit(): Promise<void> {
  if (submitting.value) return
  const validationMessage = getValidationMessage()
  if (validationMessage) {
    uni.showToast({ title: validationMessage, icon: 'none' })
    return
  }
  if (!userStore.isLoggedIn) {
    uni.showToast({ title: '请先登录', icon: 'none' })
    uni.navigateTo({ url: '/pages/login/index' })
    return
  }
  submitting.value = true
  uni.showLoading({ title: '提交中...' })
  try {
    await feedbackApi.addFeedback({
      type: selectedType.value,
      content: content.value.trim(),
      contact: contact.value.trim(),
      pics: images.value.map((item) => item.url).filter(Boolean).join(','),
    })
    uni.showToast({ title: '提交成功，感谢反馈', icon: 'success' })
    setTimeout(() => uni.navigateBack(), 1200)
  } catch (error) {
    uni.showToast({ title: error instanceof Error ? error.message : '提交失败', icon: 'none' })
  } finally {
    uni.hideLoading()
    submitting.value = false
  }
}
</script>

<style scoped lang="scss">
.feedback-page { min-height: 100vh; padding: 28rpx; background: $color-bg-page; }
.feedback-card, .contact-card { margin-bottom: 22rpx; padding: 28rpx; border-radius: $radius-keep-card; background: $color-bg-card; box-shadow: $shadow-keep-card; }
.feedback-card__header { display: flex; margin-bottom: 18rpx; }
.feedback-card__header--between { align-items: center; justify-content: space-between; }
.feedback-card__title, .contact-card__title { color: $color-text-primary; font-size: 28rpx; font-weight: 600; }
.feedback-card__required { margin-left: 8rpx; color: #e5484d; }
.feedback-card__hint { color: $color-text-helper; font-size: 23rpx; }
.type-grid { display: grid; grid-template-columns: repeat(2, minmax(0, 1fr)); gap: 16rpx; }
.type-grid__item { padding: 22rpx 12rpx; border: 2rpx solid transparent; border-radius: 18rpx; color: $color-text-secondary; background: $color-bg-chip; font-size: 26rpx; font-weight: 700; text-align: center; }
.type-grid__item--active { border-color: $color-primary; color: $color-primary-dark; background: $color-primary-soft; }
.feedback-textarea { width: 100%; min-height: 280rpx; }
.feedback-card__footer { text-align: right; }
.feedback-card__count { color: $color-text-helper; font-size: 23rpx; }
.image-grid { display: flex; flex-wrap: wrap; gap: 16rpx; }
.image-grid__item, .image-grid__add { position: relative; width: 182rpx; height: 182rpx; overflow: hidden; border-radius: 22rpx; background: $color-bg-chip; }
.image-grid__image { width: 100%; height: 100%; }
.image-grid__add { display: flex; flex-direction: column; align-items: center; justify-content: center; color: $color-text-secondary; font-size: 24rpx; font-weight: 700; }
.image-grid__plus { margin-bottom: 8rpx; font-size: 44rpx; line-height: 1; }
.image-grid__mask { position: absolute; inset: 0; display: flex; align-items: center; justify-content: center; color: #fff; background: rgba(0, 0, 0, .48); font-size: 24rpx; font-weight: 700; }
.image-grid__mask--error { background: rgba(229, 72, 77, .76); }
.image-grid__remove { position: absolute; top: 8rpx; right: 8rpx; width: 38rpx; height: 38rpx; border-radius: 50%; color: #fff; background: rgba(0, 0, 0, .5); font-size: 30rpx; line-height: 34rpx; text-align: center; }
.contact-input { width: 100%; height: 88rpx; box-sizing: border-box; margin-top: 18rpx; padding: 0 22rpx; border-radius: 16rpx; background: $color-bg-page; line-height: 88rpx; }
.submit-btn { margin-top: 40rpx; padding: 26rpx; border-radius: 999rpx; color: #fff; background: $color-primary; text-align: center; }
.submit-btn--disabled { opacity: .45; }
.submit-btn__text { color: #fff; }
</style>
