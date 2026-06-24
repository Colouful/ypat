<template>
  <view class="realname-page">
    <!-- 加载状态 -->
    <view v-if="loading" class="loading-container">
      <view class="loading-spinner"></view>
      <text class="loading-text">正在查询认证状态...</text>
    </view>

    <!-- 审核中 status=1 -->
    <view v-else-if="authStatus === 1" class="status-container">
      <view class="status-icon status-icon--pending">
        <text class="iconfont icon-clock"></text>
      </view>
      <text class="status-title">审核中</text>
      <text class="status-desc">您的实名认证正在审核中，请耐心等待</text>
      <text class="status-time">提交时间：{{ authDetail?.credate || '' }}</text>
    </view>

    <!-- 已认证 status=2 -->
    <view v-else-if="authStatus === 2" class="status-container">
      <view class="status-icon status-icon--verified">
        <text class="iconfont icon-check"></text>
      </view>
      <text class="status-title status-title--verified">已认证</text>
      <text class="status-desc">实名认证已通过</text>
      <view class="verified-info">
        <view class="verified-info__item">
          <text class="verified-info__label">姓名</text>
          <text class="verified-info__value">{{ maskedName }}</text>
        </view>
        <view class="verified-info__item">
          <text class="verified-info__label">身份证号</text>
          <text class="verified-info__value">{{ maskedIdCard }}</text>
        </view>
      </view>
    </view>

    <!-- 未通过 status=3 -->
    <view v-else-if="authStatus === 3" class="status-container">
      <view class="status-icon status-icon--rejected">
        <text class="iconfont icon-close"></text>
      </view>
      <text class="status-title status-title--rejected">审核未通过</text>
      <view class="reject-reason">
        <text class="reject-reason__label">原因：</text>
        <text class="reject-reason__text">{{ authDetail?.reason || '未知原因' }}</text>
      </view>
      <view class="reject-action">
        <button class="btn-resubmit" @tap="handleResubmit">重新提交</button>
      </view>
    </view>

    <!-- 表单 - 未提交或重新提交 -->
    <view v-else-if="showForm" class="form-container">
      <!-- 身份证正面 -->
      <view class="upload-section">
        <text class="upload-section__title">身份证正面（人像面）</text>
        <view class="upload-area" @tap="chooseImage('front')">
          <image
            v-if="formData.idCardFront"
            :src="formData.idCardFront"
            class="upload-area__preview"
            mode="aspectFill"
          />
          <view v-else class="upload-area__placeholder">
            <text class="iconfont icon-camera"></text>
            <text class="upload-area__text">点击上传身份证正面</text>
          </view>
        </view>
      </view>

      <!-- 身份证反面 -->
      <view class="upload-section">
        <text class="upload-section__title">身份证反面（国徽面）</text>
        <view class="upload-area" @tap="chooseImage('back')">
          <image
            v-if="formData.idCardBack"
            :src="formData.idCardBack"
            class="upload-area__preview"
            mode="aspectFill"
          />
          <view v-else class="upload-area__placeholder">
            <text class="iconfont icon-camera"></text>
            <text class="upload-area__text">点击上传身份证反面</text>
          </view>
        </view>
      </view>

      <!-- OCR 识别结果 / 手动输入 -->
      <view class="form-fields">
        <view class="form-item">
          <text class="form-item__label">真实姓名</text>
          <input
            v-model="formData.realName"
            class="form-item__input"
            placeholder="请输入真实姓名"
            placeholder-class="form-item__placeholder"
          />
        </view>
        <view class="form-item">
          <text class="form-item__label">身份证号</text>
          <input
            v-model="formData.idCard"
            class="form-item__input"
            placeholder="请输入身份证号码"
            placeholder-class="form-item__placeholder"
            maxlength="18"
          />
        </view>
      </view>

      <!-- 提交按钮 -->
      <button
        class="btn-submit"
        :disabled="submitDisabled"
        :class="{ 'btn-submit--disabled': submitDisabled }"
        @tap="handleSubmit"
      >
        提交认证
      </button>

      <!-- 隐私提示 -->
      <view class="privacy-notice">
        <text class="privacy-notice__text">您的信息仅用于身份验证，我们将严格保护您的隐私</text>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, reactive, computed } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { useUserStore } from '@/stores/user'
import * as oauthApi from '@/api/modules/oauth'
import type { OauthInfo } from '@/api/types'

const userStore = useUserStore()

const loading = ref(true)
const authStatus = ref<number>(0)
const authDetail = ref<OauthInfo | null>(null)
const showForm = ref(false)
const submitting = ref(false)

const formData = reactive({
  realName: '',
  idCard: '',
  idCardFront: '',
  idCardBack: '',
})

const submitDisabled = computed(() => {
  return (
    submitting.value ||
    !formData.realName.trim() ||
    !formData.idCard.trim() ||
    !formData.idCardFront ||
    !formData.idCardBack
  )
})

const maskedName = computed(() => {
  if (!authDetail.value?.name) return ''
  const name = authDetail.value.name
  if (name.length <= 1) return name
  return name[0] + '*'.repeat(name.length - 1)
})

const maskedIdCard = computed(() => {
  if (!authDetail.value?.certcode) return ''
  const id = authDetail.value.certcode
  if (id.length <= 6) return id
  return id.substring(0, 3) + '****' + id.substring(id.length - 4)
})

onLoad(async () => {
  await fetchAuthStatus()
})

async function fetchAuthStatus() {
  loading.value = true
  try {
    const userId = Number(userStore.userInfo?.id)
    if (!userId) {
      showForm.value = true
      loading.value = false
      return
    }

    const res = await oauthApi.getAuthDetail(userId)
    if (res.success && res.data) {
      authDetail.value = res.data
      authStatus.value = res.data.status

      if (res.data.status === 0 || !res.data.status) {
        showForm.value = true
      }
    } else {
      showForm.value = true
    }
  } catch {
    showForm.value = true
  } finally {
    loading.value = false
  }
}

function handleResubmit() {
  authStatus.value = 0
  showForm.value = true
  formData.realName = ''
  formData.idCard = ''
  formData.idCardFront = ''
  formData.idCardBack = ''
}

function chooseImage(side: 'front' | 'back') {
  uni.chooseImage({
    count: 1,
    sizeType: ['compressed'],
    sourceType: ['album', 'camera'],
    success: async (res) => {
      const tempFilePath = res.tempFilePaths[0]

      if (side === 'front') {
        formData.idCardFront = tempFilePath
      } else {
        formData.idCardBack = tempFilePath
      }

      await performOcr(tempFilePath, side)
    },
  })
}

async function performOcr(filePath: string, side: 'front' | 'back') {
  if (side !== 'front') return

  uni.showLoading({ title: '识别中...' })
  try {
    const res = await oauthApi.ocrIdCard(filePath)
    if (res.success && res.data) {
      if (res.data.name) {
        formData.realName = res.data.name
      }
      if (res.data.certcode) {
        formData.idCard = res.data.certcode
      }
      uni.showToast({ title: '识别成功', icon: 'success' })
    } else {
      uni.showToast({ title: '识别失败，请手动填写', icon: 'none' })
    }
  } catch {
    uni.showToast({ title: '识别失败，请手动填写', icon: 'none' })
  } finally {
    uni.hideLoading()
  }
}

async function handleSubmit() {
  if (submitDisabled.value) return

  if (!formData.realName.trim()) {
    uni.showToast({ title: '请输入真实姓名', icon: 'none' })
    return
  }

  if (!formData.idCard.trim() || formData.idCard.trim().length < 15) {
    uni.showToast({ title: '请输入正确的身份证号', icon: 'none' })
    return
  }

  if (!formData.idCardFront) {
    uni.showToast({ title: '请上传身份证正面照片', icon: 'none' })
    return
  }

  if (!formData.idCardBack) {
    uni.showToast({ title: '请上传身份证反面照片', icon: 'none' })
    return
  }

  submitting.value = true
  uni.showLoading({ title: '提交中...' })

  try {
    const userId = Number(userStore.userInfo?.id)
    const res = await oauthApi.submitAuth({
      userid: userId,
      name: formData.realName.trim(),
      certcode: formData.idCard.trim(),
      pics: [formData.idCardFront, formData.idCardBack].filter(Boolean),
    })

    if (res.success) {
      uni.showToast({ title: '提交成功', icon: 'success' })
      setTimeout(() => {
        fetchAuthStatus()
      }, 1500)
    } else {
      uni.showToast({ title: res.message || '提交失败', icon: 'none' })
    }
  } catch {
    uni.showToast({ title: '网络错误，请重试', icon: 'none' })
  } finally {
    submitting.value = false
    uni.hideLoading()
  }
}
</script>

<style lang="scss">
@import '@/styles/tokens.scss';

.realname-page {
  min-height: 100vh;
  background-color: $color-bg-page;
  padding: $spacing-xl;
  padding-bottom: calc(#{$spacing-xxl} + env(safe-area-inset-bottom));
}

.loading-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding-top: 200rpx;

  .loading-spinner {
    width: 64rpx;
    height: 64rpx;
    border: 4rpx solid $color-border;
    border-top-color: $color-primary;
    border-radius: 50%;
    animation: spin 0.8s linear infinite;
  }

  .loading-text {
    margin-top: $spacing-lg;
    font-size: $font-size-base;
    color: $color-text-secondary;
  }
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

.status-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding-top: 120rpx;
}

.status-icon {
  width: 120rpx;
  height: 120rpx;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: $spacing-lg;

  .iconfont {
    font-size: 60rpx;
    color: #fff;
  }

  &--pending {
    background-color: $color-accent-orange;
  }

  &--verified {
    background-color: $color-primary;
  }

  &--rejected {
    background-color: $color-accent-red;
  }
}

.status-title {
  font-size: $font-size-xl;
  font-weight: $font-weight-semibold;
  color: $color-accent-orange;
  margin-bottom: $spacing-sm;

  &--verified {
    color: $color-primary;
  }

  &--rejected {
    color: $color-accent-red;
  }
}

.status-desc {
  font-size: $font-size-base;
  color: $color-text-secondary;
  margin-bottom: $spacing-md;
}

.status-time {
  font-size: $font-size-sm;
  color: $color-text-helper;
}

.verified-info {
  width: 100%;
  margin-top: $spacing-xxl;
  background-color: $color-bg-card;
  border-radius: $radius-lg;
  padding: $spacing-xl;
  box-shadow: $shadow-sm;

  &__item {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: $spacing-md 0;

    &:not(:last-child) {
      border-bottom: 1rpx solid $color-divider;
    }
  }

  &__label {
    font-size: $font-size-base;
    color: $color-text-secondary;
  }

  &__value {
    font-size: $font-size-base;
    color: $color-text-primary;
    font-weight: $font-weight-medium;
  }
}

.reject-reason {
  display: flex;
  align-items: flex-start;
  background-color: rgba($color-accent-red, 0.06);
  border-radius: $radius-md;
  padding: $spacing-lg;
  margin-top: $spacing-xl;
  width: 100%;

  &__label {
    font-size: $font-size-base;
    color: $color-accent-red;
    flex-shrink: 0;
  }

  &__text {
    font-size: $font-size-base;
    color: $color-accent-red;
    flex: 1;
  }
}

.reject-action {
  margin-top: $spacing-xxl;
  width: 100%;
}

.btn-resubmit {
  width: 100%;
  height: 88rpx;
  line-height: 88rpx;
  background-color: $color-primary;
  color: #fff;
  font-size: $font-size-lg;
  font-weight: $font-weight-medium;
  border-radius: $radius-xl;
  border: none;
  text-align: center;

  &::after {
    border: none;
  }
}

.form-container {
  padding-top: $spacing-lg;
}

.upload-section {
  margin-bottom: $spacing-xl;

  &__title {
    font-size: $font-size-base;
    color: $color-text-primary;
    font-weight: $font-weight-medium;
    margin-bottom: $spacing-md;
    display: block;
  }
}

.upload-area {
  width: 100%;
  height: 360rpx;
  background-color: $color-bg-card;
  border-radius: $radius-lg;
  border: 2rpx dashed $color-border;
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;

  &__preview {
    width: 100%;
    height: 100%;
  }

  &__placeholder {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;

    .iconfont {
      font-size: 72rpx;
      color: $color-text-helper;
      margin-bottom: $spacing-md;
    }
  }

  &__text {
    font-size: $font-size-sm;
    color: $color-text-helper;
  }
}

.form-fields {
  background-color: $color-bg-card;
  border-radius: $radius-lg;
  padding: 0 $spacing-xl;
  margin-bottom: $spacing-xl;
  box-shadow: $shadow-sm;
}

.form-item {
  display: flex;
  align-items: center;
  height: 100rpx;

  &:not(:last-child) {
    border-bottom: 1rpx solid $color-divider;
  }

  &__label {
    font-size: $font-size-base;
    color: $color-text-primary;
    font-weight: $font-weight-medium;
    width: 160rpx;
    flex-shrink: 0;
  }

  &__input {
    flex: 1;
    font-size: $font-size-base;
    color: $color-text-primary;
    text-align: right;
  }

  &__placeholder {
    color: $color-text-helper;
  }
}

.btn-submit {
  width: 100%;
  height: 88rpx;
  line-height: 88rpx;
  background-color: $color-primary;
  color: #fff;
  font-size: $font-size-lg;
  font-weight: $font-weight-medium;
  border-radius: $radius-xl;
  border: none;
  text-align: center;
  margin-top: $spacing-lg;

  &::after {
    border: none;
  }

  &--disabled {
    opacity: 0.5;
  }
}

.privacy-notice {
  margin-top: $spacing-xl;
  text-align: center;

  &__text {
    font-size: $font-size-xs;
    color: $color-text-helper;
    line-height: 1.6;
  }
}
</style>
