<template>
  <view class="complete-info-page">
    <!-- 进度指示器 -->
    <view class="progress-bar">
      <view
        v-for="step in totalSteps"
        :key="step"
        class="progress-bar__dot"
        :class="{ 'progress-bar__dot--active': step <= currentStep, 'progress-bar__dot--current': step === currentStep }"
      />
      <view class="progress-bar__line">
        <view
          class="progress-bar__line-fill"
          :style="{ width: ((currentStep - 1) / (totalSteps - 1)) * 100 + '%' }"
        />
      </view>
    </view>

    <!-- 步骤内容区域 -->
    <view class="step-container">
      <!-- 步骤 1: 性别选择 -->
      <view v-if="currentStep === 1" class="step step--gender">
        <view class="step__header">
          <text class="step__title">选择你的性别</text>
          <text class="step__subtitle">让我们更好地为你推荐内容</text>
        </view>
        <view class="gender-cards">
          <view
            class="gender-card"
            :class="{ 'gender-card--active': formData.gender === UserGender.MALE }"
            @tap="selectGender(UserGender.MALE)"
          >
            <text class="gender-card__icon">👨</text>
            <text class="gender-card__label">男</text>
          </view>
          <view
            class="gender-card"
            :class="{ 'gender-card--active': formData.gender === UserGender.FEMALE }"
            @tap="selectGender(UserGender.FEMALE)"
          >
            <text class="gender-card__icon">👩</text>
            <text class="gender-card__label">女</text>
          </view>
        </view>
      </view>

      <!-- 步骤 2: 职业选择 -->
      <view v-if="currentStep === 2" class="step step--profess">
        <view class="step__header">
          <text class="step__title">选择你的职业</text>
          <text class="step__subtitle">帮助我们了解你的身份</text>
        </view>
        <view class="profess-grid">
          <view
            v-for="(label, key) in PROFESS_LABELS"
            :key="key"
            class="profess-item"
            :class="{ 'profess-item--active': formData.profess === key }"
            @tap="selectProfess(key)"
          >
            <text class="profess-item__label">{{ label }}</text>
          </view>
        </view>
      </view>

      <!-- 步骤 3: 生日 -->
      <view v-if="currentStep === 3" class="step step--birthday">
        <view class="step__header">
          <text class="step__title">选择你的生日</text>
          <text class="step__subtitle">我们不会公开你的年龄信息</text>
        </view>
        <view class="birthday-picker">
          <picker
            mode="date"
            :value="formData.birthday"
            :start="'1950-01-01'"
            :end="todayDate"
            @change="onBirthdayChange"
          >
            <view class="birthday-picker__trigger">
              <text
                class="birthday-picker__value"
                :class="{ 'birthday-picker__value--placeholder': !formData.birthday }"
              >
                {{ formData.birthday || '请选择出生日期' }}
              </text>
              <text class="birthday-picker__arrow">›</text>
            </view>
          </picker>
        </view>
      </view>

      <!-- 步骤 4: 城市 -->
      <view v-if="currentStep === 4" class="step step--city">
        <view class="step__header">
          <text class="step__title">你在哪座城市</text>
          <text class="step__subtitle">方便为你推荐本地摄影师和模特</text>
        </view>
        <view class="city-input-wrapper">
          <input
            v-model="formData.city"
            class="city-input"
            type="text"
            placeholder="请输入你所在的城市"
            :placeholder-style="'color: ' + '#B3B8BE'"
            maxlength="20"
          />
        </view>
      </view>
    </view>

    <!-- 底部操作按钮 -->
    <view class="action-bar">
      <text class="action-bar__skip" @tap="handleSkip">跳过</text>
      <view
        class="action-bar__next"
        :class="{ 'action-bar__next--disabled': submitting }"
        @tap="handleNext"
      >
        <text class="action-bar__next-text">
          {{ currentStep === totalSteps ? '完成' : '下一步' }}
        </text>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, reactive, computed } from 'vue'
import { useUserStore } from '@/stores/user'
import * as userApi from '@/api/modules/user'
import { PROFESS_LABELS, UserGender } from '@/constants/enums'

const userStore = useUserStore()

const totalSteps = 4
const currentStep = ref(1)
const submitting = ref(false)

const formData = reactive({
  gender: '' as string,
  profess: '' as string,
  birthday: '' as string,
  city: '' as string,
})

/** 今天的日期，用于日期选择器的最大值 */
const todayDate = computed(() => {
  const now = new Date()
  const year = now.getFullYear()
  const month = String(now.getMonth() + 1).padStart(2, '0')
  const day = String(now.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
})

/** 选择性别 */
function selectGender(gender: string) {
  formData.gender = gender
}

/** 选择职业 */
function selectProfess(key: string) {
  formData.profess = key
}

/** 选择生日 */
function onBirthdayChange(e: any) {
  formData.birthday = e.detail.value
}

/** 下一步 */
async function handleNext() {
  if (submitting.value) return

  if (currentStep.value < totalSteps) {
    currentStep.value++
    return
  }

  // 最后一步，提交数据
  await submitInfo()
}

/** 跳过当前步骤 */
function handleSkip() {
  if (currentStep.value < totalSteps) {
    currentStep.value++
    return
  }
  // 最后一步跳过直接返回首页
  navigateToHome()
}

/** 提交用户资料 */
async function submitInfo() {
  submitting.value = true

  try {
    const userId = Number(userStore.userInfo?.id)
    if (!userId) {
      uni.showToast({ title: '用户信息异常', icon: 'none' })
      return
    }

    const updateData: Record<string, any> = { id: userId }

    if (formData.gender) {
      updateData.gender = Number(formData.gender)
    }
    if (formData.profess) {
      updateData.profess = formData.profess
    }
    if (formData.city) {
      updateData.city = formData.city
    }

    await userApi.updateUser(updateData as any)

    // 更新本地 store
    userStore.updateUserInfo({
      gender: formData.gender ? Number(formData.gender) : undefined,
      profess: formData.profess || undefined,
      city: formData.city || undefined,
    } as any)

    uni.showToast({ title: '资料保存成功', icon: 'success' })

    setTimeout(() => {
      navigateToHome()
    }, 1500)
  } catch (error) {
    uni.showToast({ title: '保存失败，请重试', icon: 'none' })
  } finally {
    submitting.value = false
  }
}

/** 跳转到首页 */
function navigateToHome() {
  uni.switchTab({ url: '/pages/home/index' })
}
</script>

<style lang="scss">
@import '@/styles/tokens.scss';

.complete-info-page {
  min-height: 100vh;
  background-color: $color-bg-page;
  display: flex;
  flex-direction: column;
  padding-bottom: $safe-area-inset-bottom;
}

// 进度指示器
.progress-bar {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: $spacing-xxl $spacing-xl $spacing-lg;
  position: relative;
  gap: $spacing-xl;

  &__dot {
    width: 20rpx;
    height: 20rpx;
    border-radius: 50%;
    background-color: $color-border;
    position: relative;
    z-index: 2;
    transition: all $duration-normal ease;

    &--active {
      background-color: $color-primary;
    }

    &--current {
      width: 28rpx;
      height: 28rpx;
      background-color: $color-primary;
      box-shadow: 0 0 0 8rpx $color-primary-light;
    }
  }

  &__line {
    position: absolute;
    top: 50%;
    left: 15%;
    right: 15%;
    height: 4rpx;
    background-color: $color-border;
    transform: translateY(-50%);
    z-index: 1;
  }

  &__line-fill {
    height: 100%;
    background-color: $color-primary;
    transition: width $duration-normal ease;
    border-radius: 2rpx;
  }
}

// 步骤容器
.step-container {
  flex: 1;
  padding: 0 $spacing-xl;
}

.step {
  &__header {
    margin-bottom: $spacing-xxl;
  }

  &__title {
    display: block;
    font-size: $font-size-title;
    font-weight: $font-weight-bold;
    color: $color-text-primary;
    margin-bottom: $spacing-sm;
  }

  &__subtitle {
    display: block;
    font-size: $font-size-base;
    color: $color-text-secondary;
  }
}

// 性别选择卡片
.gender-cards {
  display: flex;
  gap: $spacing-lg;
}

.gender-card {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: $spacing-xxl $spacing-lg;
  background-color: $color-bg-card;
  border-radius: $radius-lg;
  border: 4rpx solid $color-border;
  box-shadow: $shadow-sm;
  transition: all $duration-normal ease;

  &--active {
    border-color: $color-primary;
    background-color: rgba(35, 194, 104, 0.05);
    box-shadow: $shadow-md;
  }

  &__icon {
    font-size: 100rpx;
    margin-bottom: $spacing-lg;
  }

  &__label {
    font-size: $font-size-xl;
    font-weight: $font-weight-semibold;
    color: $color-text-primary;
  }
}

// 职业选择网格
.profess-grid {
  display: flex;
  flex-wrap: wrap;
  gap: $spacing-md;
}

.profess-item {
  width: calc(25% - #{$spacing-md} * 3 / 4);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: $spacing-lg $spacing-sm;
  background-color: $color-bg-card;
  border-radius: $radius-md;
  border: 2rpx solid $color-border;
  transition: all $duration-normal ease;

  &--active {
    border-color: $color-primary;
    background-color: rgba(35, 194, 104, 0.08);
  }

  &__label {
    font-size: $font-size-base;
    color: $color-text-primary;
    font-weight: $font-weight-medium;
  }

  &--active &__label {
    color: $color-primary;
    font-weight: $font-weight-semibold;
  }
}

// 生日选择器
.birthday-picker {
  &__trigger {
    display: flex;
    align-items: center;
    justify-content: space-between;
    background-color: $color-bg-card;
    padding: $spacing-lg;
    border-radius: $radius-md;
    border: 2rpx solid $color-border;
  }

  &__value {
    font-size: $font-size-lg;
    color: $color-text-primary;
    font-weight: $font-weight-medium;

    &--placeholder {
      color: $color-text-helper;
      font-weight: $font-weight-regular;
    }
  }

  &__arrow {
    font-size: $font-size-xl;
    color: $color-text-helper;
  }
}

// 城市输入
.city-input-wrapper {
  background-color: $color-bg-card;
  border-radius: $radius-md;
  border: 2rpx solid $color-border;
  padding: $spacing-lg;
}

.city-input {
  width: 100%;
  font-size: $font-size-lg;
  color: $color-text-primary;
}

// 底部操作栏
.action-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: $spacing-lg $spacing-xl;
  padding-bottom: calc(#{$spacing-lg} + #{$safe-area-inset-bottom});

  &__skip {
    font-size: $font-size-base;
    color: $color-text-secondary;
    padding: $spacing-sm $spacing-md;
  }

  &__next {
    background-color: $color-primary;
    border-radius: $radius-round;
    padding: $spacing-md $spacing-xxl;
    box-shadow: $shadow-md;
    transition: opacity $duration-fast ease;

    &--disabled {
      opacity: 0.6;
    }
  }

  &__next-text {
    font-size: $font-size-base;
    font-weight: $font-weight-semibold;
    color: #ffffff;
  }
}
</style>
