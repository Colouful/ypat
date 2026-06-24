<template>
  <view class="publish-page">
    <view v-if="!isLoggedIn" class="login-prompt">
      <text class="login-prompt__text">登录后发布约拍</text>
      <button class="login-prompt__btn" @tap="goLogin">
        <text class="login-prompt__btn-text">去登录</text>
      </button>
    </view>

    <view v-else class="publish-form">
      <view class="form-section">
        <text class="form-section__title">约拍对象</text>
        <view class="form-tags">
          <view v-for="item in targetOptions" :key="item.value" class="form-tag" :class="{ 'form-tag--active': form.target === item.value }" @tap="form.target = item.value">
            <text class="form-tag__text">{{ item.label }}</text>
          </view>
        </view>
      </view>

      <view class="form-section">
        <text class="form-section__title">约拍说明</text>
        <textarea v-model="form.describ" class="form-textarea" placeholder="描述你的约拍需求、时间、地点偏好等" maxlength="500" :show-confirm-bar="false" />
        <text class="form-section__count">{{ form.describ.length }}/500</text>
      </view>

      <view class="form-section">
        <text class="form-section__title">拍摄日期</text>
        <picker mode="date" :value="form.patdate" :start="today" @change="handleDateChange">
          <view class="form-picker">
            <text class="form-picker__text" :class="{ 'form-picker__text--placeholder': !form.patdate }">
              {{ form.patdate || '请选择拍摄日期' }}
            </text>
            <text class="form-picker__arrow">›</text>
          </view>
        </picker>
      </view>

      <view class="form-section">
        <text class="form-section__title">拍摄城市</text>
        <view class="form-picker" @tap="handleCityPick">
          <text class="form-picker__text" :class="{ 'form-picker__text--placeholder': !form.city }">
            {{ form.city ? `${form.province} ${form.city}` : '请选择城市' }}
          </text>
          <text class="form-picker__arrow">›</text>
        </view>
      </view>

      <view class="form-section">
        <text class="form-section__title">收费方式</text>
        <view class="form-tags">
          <view v-for="item in chargeOptions" :key="item.value" class="form-tag" :class="{ 'form-tag--active': form.chargeway === item.value }" @tap="form.chargeway = item.value">
            <text class="form-tag__text">{{ item.label }}</text>
          </view>
        </view>
      </view>

      <view class="form-section">
        <text class="form-section__title">拍摄风格</text>
        <view class="form-tags form-tags--wrap">
          <view v-for="style in photoStyles" :key="style" class="form-tag form-tag--small" :class="{ 'form-tag--active': selectedStyles.includes(style) }" @tap="toggleStyle(style)">
            <text class="form-tag__text">{{ style }}</text>
          </view>
        </view>
      </view>

      <view class="form-section">
        <text class="form-section__title">约拍图片</text>
        <view class="image-upload">
          <view v-for="(img, idx) in form.pics" :key="idx" class="image-upload__item">
            <image class="image-upload__image" :src="img" mode="aspectFill" />
            <view class="image-upload__remove" @tap="removeImage(idx)">
              <text class="image-upload__remove-icon">×</text>
            </view>
          </view>
          <view v-if="form.pics.length < 9" class="image-upload__add" @tap="chooseImage">
            <text class="image-upload__add-icon">+</text>
            <text class="image-upload__add-text">{{ form.pics.length }}/9</text>
          </view>
        </view>
      </view>

      <view class="form-section form-section--checkbox">
        <view class="form-checkbox" @tap="form.realnameflag = form.realnameflag === '1' ? '0' : '1'">
          <view class="form-checkbox__box" :class="{ 'form-checkbox__box--checked': form.realnameflag === '1' }">
            <text v-if="form.realnameflag === '1'" class="form-checkbox__mark">✓</text>
          </view>
          <text class="form-checkbox__label">要求对方已实名认证</text>
        </view>
        <view class="form-checkbox" @tap="form.creditflag = form.creditflag === '1' ? '0' : '1'">
          <view class="form-checkbox__box" :class="{ 'form-checkbox__box--checked': form.creditflag === '1' }">
            <text v-if="form.creditflag === '1'" class="form-checkbox__mark">✓</text>
          </view>
          <text class="form-checkbox__label">要求对方已缴纳保证金</text>
        </view>
      </view>

      <view class="form-tip">
        <text class="form-tip__text">发布约拍将消耗 3 拍拍豆（当前余额：{{ userPPD }} 豆）</text>
      </view>

      <button class="submit-btn" :disabled="!canSubmit || submitting" @tap="handleSubmit">
        <text class="submit-btn__text">{{ submitting ? '提交中...' : '发布约拍' }}</text>
      </button>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, reactive, computed } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { useUserStore } from '@/stores/user'
import * as ypatApi from '@/api/modules/ypat'
import { PHOTO_STYLES } from '@/constants/enums'

const userStore = useUserStore()

const isLoggedIn = computed(() => userStore.isLoggedIn)
const userPPD = computed(() => userStore.userInfo?.ppd || 0)

const today = new Date().toISOString().split('T')[0]
const submitting = ref(false)

const form = reactive({
  target: '1',
  describ: '',
  patdate: '',
  province: '',
  city: '',
  area: '',
  chargeway: '0',
  patstyle: '',
  pics: [] as string[],
  realnameflag: '0',
  creditflag: '0',
})

const selectedStyles = ref<string[]>([])
const photoStyles = PHOTO_STYLES

const targetOptions = [
  { value: '0', label: '约摄影师' },
  { value: '1', label: '约模特' },
]

const chargeOptions = [
  { value: '0', label: '互免' },
  { value: '1', label: '我要收费' },
  { value: '2', label: '可付费' },
  { value: '3', label: '费用协商' },
]

const canSubmit = computed(() => {
  return form.target && form.describ.trim() && form.patdate && form.city && form.chargeway && form.pics.length > 0
})

function toggleStyle(style: string) {
  const idx = selectedStyles.value.indexOf(style)
  if (idx > -1) {
    selectedStyles.value.splice(idx, 1)
  } else {
    selectedStyles.value.push(style)
  }
  form.patstyle = selectedStyles.value.join(',')
}

function handleDateChange(e: any) {
  form.patdate = e.detail.value
}

function handleCityPick() {
  // #ifdef MP-WEIXIN
  uni.chooseLocation({
    success(res) {
      form.city = res.name || res.address || ''
      form.province = ''
    },
  })
  // #endif
  // #ifndef MP-WEIXIN
  uni.showToast({ title: '请手动输入城市', icon: 'none' })
  // #endif
}

function chooseImage() {
  const remaining = 9 - form.pics.length
  uni.chooseImage({
    count: remaining,
    sizeType: ['compressed'],
    sourceType: ['album', 'camera'],
    success(res) {
      form.pics.push(...res.tempFilePaths)
    },
  })
}

function removeImage(idx: number) {
  form.pics.splice(idx, 1)
}

async function handleSubmit() {
  if (!canSubmit.value || submitting.value) return
  if (userPPD.value < 3) {
    uni.showModal({
      title: '拍拍豆不足',
      content: '发布约拍需要 3 个拍拍豆，是否去充值？',
      confirmText: '去充值',
      success(res) {
        if (res.confirm) {
          uni.navigateTo({ url: '/pages-sub/user/recharge' })
        }
      },
    })
    return
  }

  submitting.value = true
  try {
    const userId = userStore.userInfo?.id
    if (!userId) return

    const res = await ypatApi.submit({
      ...form,
      userid: userId,
    })

    if (res.success) {
      uni.showToast({ title: '发布成功，等待审核', icon: 'success' })
      setTimeout(() => {
        Object.assign(form, { target: '1', describ: '', patdate: '', province: '', city: '', area: '', chargeway: '0', patstyle: '', pics: [], realnameflag: '0', creditflag: '0' })
        selectedStyles.value = []
      }, 1500)
    } else {
      uni.showToast({ title: res.message || '发布失败', icon: 'none' })
    }
  } catch (err: any) {
    uni.showToast({ title: err?.message || '网络异常，请重试', icon: 'none' })
  } finally {
    submitting.value = false
  }
}

function goLogin() {
  uni.navigateTo({ url: '/pages/login/index' })
}

onShow(() => {
  if (isLoggedIn.value) {
    userStore.updateUserInfo()
  }
})
</script>

<style lang="scss">
@import '@/styles/tokens.scss';

.publish-page {
  min-height: 100vh;
  background-color: $color-bg-page;
  padding-bottom: calc(env(safe-area-inset-bottom) + 120rpx);
}

.login-prompt {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding-top: 300rpx;

  &__text {
    font-size: $font-size-lg;
    color: $color-text-secondary;
    margin-bottom: $spacing-lg;
  }

  &__btn {
    background-color: $color-primary;
    border-radius: $radius-round;
    padding: $spacing-sm $spacing-xxl;
    border: none;
    &::after { border: none; }
  }

  &__btn-text { color: #fff; font-size: $font-size-base; }
}

.publish-form {
  padding: $spacing-md;
}

.form-section {
  background-color: #fff;
  border-radius: $radius-md;
  padding: $spacing-md $spacing-lg;
  margin-bottom: $spacing-md;

  &__title {
    font-size: $font-size-base;
    font-weight: $font-weight-semibold;
    color: $color-text-primary;
    margin-bottom: $spacing-md;
  }

  &__count {
    font-size: $font-size-xs;
    color: $color-text-helper;
    text-align: right;
    margin-top: $spacing-xs;
  }

  &--checkbox {
    display: flex;
    flex-direction: column;
    gap: $spacing-md;
  }
}

.form-tags {
  display: flex;
  gap: $spacing-sm;

  &--wrap {
    flex-wrap: wrap;
  }
}

.form-tag {
  padding: $spacing-sm $spacing-md;
  background-color: $color-bg-page;
  border-radius: $radius-round;
  border: 2rpx solid transparent;

  &--active {
    background-color: $color-primary-light;
    border-color: $color-primary;
  }

  &--small {
    padding: $spacing-xs $spacing-sm;
  }

  &__text {
    font-size: $font-size-sm;
    color: $color-text-secondary;
  }

  &--active &__text {
    color: $color-primary;
  }
}

.form-textarea {
  width: 100%;
  min-height: 200rpx;
  font-size: $font-size-base;
  color: $color-text-primary;
  line-height: 1.6;
}

.form-picker {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: $spacing-sm 0;

  &__text {
    font-size: $font-size-base;
    color: $color-text-primary;

    &--placeholder {
      color: $color-text-helper;
    }
  }

  &__arrow {
    font-size: $font-size-xl;
    color: $color-text-helper;
  }
}

.image-upload {
  display: flex;
  flex-wrap: wrap;
  gap: $spacing-sm;

  &__item {
    position: relative;
    width: 200rpx;
    height: 200rpx;
    border-radius: $radius-sm;
    overflow: hidden;
  }

  &__image {
    width: 100%;
    height: 100%;
  }

  &__remove {
    position: absolute;
    top: 4rpx;
    right: 4rpx;
    width: 40rpx;
    height: 40rpx;
    background-color: rgba(0, 0, 0, 0.5);
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
  }

  &__remove-icon {
    color: #fff;
    font-size: $font-size-base;
  }

  &__add {
    width: 200rpx;
    height: 200rpx;
    border: 2rpx dashed $color-border;
    border-radius: $radius-sm;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
  }

  &__add-icon {
    font-size: 60rpx;
    color: $color-text-helper;
    line-height: 1;
  }

  &__add-text {
    font-size: $font-size-xs;
    color: $color-text-helper;
    margin-top: $spacing-xs;
  }
}

.form-checkbox {
  display: flex;
  align-items: center;

  &__box {
    width: 40rpx;
    height: 40rpx;
    border: 2rpx solid $color-border;
    border-radius: $radius-sm;
    display: flex;
    align-items: center;
    justify-content: center;
    margin-right: $spacing-sm;

    &--checked {
      background-color: $color-primary;
      border-color: $color-primary;
    }
  }

  &__mark {
    font-size: $font-size-sm;
    color: #fff;
  }

  &__label {
    font-size: $font-size-base;
    color: $color-text-primary;
  }
}

.form-tip {
  padding: $spacing-md;

  &__text {
    font-size: $font-size-sm;
    color: $color-accent-orange;
  }
}

.submit-btn {
  width: 100%;
  height: 96rpx;
  background-color: $color-primary;
  border-radius: $radius-xl;
  display: flex;
  align-items: center;
  justify-content: center;
  border: none;
  margin-top: $spacing-lg;

  &[disabled] {
    background-color: $color-primary-light;
  }

  &__text {
    font-size: $font-size-lg;
    font-weight: $font-weight-medium;
    color: #fff;
  }

  &::after { border: none; }
}
</style>
