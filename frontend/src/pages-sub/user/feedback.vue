<template>
  <view class="feedback-page">
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

    <view class="contact-card">
      <text class="contact-card__title">联系方式（选填）</text>
      <input v-model="contact" class="contact-input" placeholder="手机号、微信号或邮箱" />
    </view>

    <view class="submit-section">
      <view class="submit-btn" :class="{ 'submit-btn--disabled': !isValid || submitting }" @tap="handleSubmit">
        <text class="submit-btn__text">{{ submitting ? '提交中...' : '提交反馈' }}</text>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { post } from '@/api/request'

const content = ref('')
const contact = ref('')
const submitting = ref(false)
const isValid = computed(() => content.value.trim().length >= 10 && content.value.length <= 500)

async function handleSubmit(): Promise<void> {
  if (!isValid.value || submitting.value) return
  submitting.value = true
  uni.showLoading({ title: '提交中...' })
  try {
    await post('/feedback/add', {
      content: content.value.trim(),
      contact: contact.value.trim(),
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
.feedback-page { min-height: 100vh; padding: 28rpx; background: #f7f8fa; }
.feedback-card, .contact-card { margin-bottom: 22rpx; padding: 28rpx; border-radius: 24rpx; background: #fff; }
.feedback-card__header { display: flex; margin-bottom: 18rpx; }
.feedback-card__title, .contact-card__title { color: #1d2433; font-size: 28rpx; font-weight: 600; }
.feedback-card__required { margin-left: 8rpx; color: #e5484d; }
.feedback-textarea { width: 100%; min-height: 280rpx; }
.feedback-card__footer { text-align: right; }
.feedback-card__count { color: #9098a5; font-size: 23rpx; }
.contact-input { margin-top: 18rpx; padding: 20rpx; border-radius: 16rpx; background: #f7f8fa; }
.submit-btn { margin-top: 40rpx; padding: 26rpx; border-radius: 44rpx; color: #fff; background: #23c268; text-align: center; }
.submit-btn--disabled { opacity: .45; pointer-events: none; }
.submit-btn__text { color: #fff; }
</style>
