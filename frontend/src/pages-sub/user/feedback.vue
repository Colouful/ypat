<template>
  <view class="feedback-page">
    <view class="feedback-card">
      <view class="feedback-card__header">
        <text class="feedback-card__title">反馈内容</text>
        <text class="feedback-card__required">*</text>
      </view>
      <textarea
        class="feedback-textarea"
        v-model="content"
        placeholder="请描述您遇到的问题或建议..."
        :maxlength="500"
        placeholder-class="feedback-textarea__placeholder"
      />
      <view class="feedback-card__footer">
        <text class="feedback-card__count" :class="{ 'feedback-card__count--warn': content.length > 0 && content.length < 10 }">
          {{ content.length }}/500
        </text>
      </view>
    </view>

    <view class="contact-card">
      <view class="contact-card__header">
        <text class="contact-card__title">联系方式</text>
        <text class="contact-card__optional">选填</text>
      </view>
      <input
        class="contact-input"
        v-model="contact"
        placeholder="请留下联系方式(选填)"
        placeholder-class="contact-input__placeholder"
      />
    </view>

    <view class="submit-section">
      <view
        class="submit-btn"
        :class="{ 'submit-btn--disabled': !isValid }"
        @tap="handleSubmit"
      >
        <text class="submit-btn__text">提交反馈</text>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'

const content = ref('')
const contact = ref('')
const submitting = ref(false)

const isValid = computed(() => {
  return content.value.length >= 10 && content.value.length <= 500
})

async function handleSubmit() {
  if (!isValid.value || submitting.value) return

  submitting.value = true
  uni.showLoading({ title: '提交中...' })

  try {
    await uni.$http.post('/feedback/add', {
      content: content.value,
      contact: contact.value,
    })
    uni.hideLoading()
    uni.showToast({
      title: '提交成功，感谢您的反馈',
      icon: 'none',
      duration: 2000,
    })
    setTimeout(() => {
      uni.navigateBack()
    }, 1500)
  } catch {
    uni.hideLoading()
    uni.showToast({ title: '提交失败，请稍后重试', icon: 'none' })
  } finally {
    submitting.value = false
  }
}
</script>

<style lang="scss">
@import '@/styles/tokens.scss';

.feedback-page {
  min-height: 100vh;
  background-color: $color-bg-page;
  padding: $spacing-lg;
  padding-bottom: calc(env(safe-area-inset-bottom) + 40rpx);
}

.feedback-card {
  background-color: $color-bg-card;
  border-radius: $radius-md;
  padding: $spacing-lg;
  margin-bottom: $spacing-lg;
  box-shadow: $shadow-sm;

  &__header {
    display: flex;
    align-items: center;
    margin-bottom: $spacing-md;
  }

  &__title {
    font-size: $font-size-base;
    font-weight: $font-weight-medium;
    color: $color-text-primary;
  }

  &__required {
    font-size: $font-size-base;
    color: $color-accent-red;
    margin-left: $spacing-xs;
  }

  &__footer {
    display: flex;
    justify-content: flex-end;
    margin-top: $spacing-sm;
  }

  &__count {
    font-size: $font-size-xs;
    color: $color-text-helper;

    &--warn {
      color: $color-accent-orange;
    }
  }
}

.feedback-textarea {
  width: 100%;
  height: 300rpx;
  font-size: $font-size-base;
  color: $color-text-primary;
  line-height: 1.6;
  padding: 0;

  &__placeholder {
    color: $color-text-helper;
    font-size: $font-size-base;
  }
}

.contact-card {
  background-color: $color-bg-card;
  border-radius: $radius-md;
  padding: $spacing-lg;
  margin-bottom: $spacing-xl;
  box-shadow: $shadow-sm;

  &__header {
    display: flex;
    align-items: center;
    margin-bottom: $spacing-md;
  }

  &__title {
    font-size: $font-size-base;
    font-weight: $font-weight-medium;
    color: $color-text-primary;
  }

  &__optional {
    font-size: $font-size-xs;
    color: $color-text-helper;
    margin-left: $spacing-sm;
  }
}

.contact-input {
  width: 100%;
  font-size: $font-size-base;
  color: $color-text-primary;
  padding: $spacing-sm 0;
  border-bottom: 1rpx solid $color-border;

  &__placeholder {
    color: $color-text-helper;
    font-size: $font-size-base;
  }
}

.submit-section {
  margin-top: $spacing-xxl;
}

.submit-btn {
  background-color: $color-primary;
  border-radius: $radius-md;
  padding: $spacing-lg;
  display: flex;
  align-items: center;
  justify-content: center;

  &:active {
    background-color: $color-primary-dark;
  }

  &--disabled {
    background-color: $color-primary-light;
    pointer-events: none;
  }

  &__text {
    font-size: $font-size-base;
    font-weight: $font-weight-medium;
    color: #FFFFFF;
  }
}
</style>
