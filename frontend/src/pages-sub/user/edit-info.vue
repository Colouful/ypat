<template>
  <view class="edit-info-page">
    <!-- 自定义导航栏 -->
    <view class="navbar" :style="{ paddingTop: statusBarHeight + 'px' }">
      <view class="navbar__content">
        <view class="navbar__back" @tap="handleBack">
          <text class="navbar__back-icon">&#xe60b;</text>
        </view>
        <text class="navbar__title">编辑资料</text>
        <view class="navbar__placeholder" />
      </view>
    </view>

    <!-- 主内容 -->
    <view class="form-content" :style="{ paddingTop: navBarHeight + 'px' }">
      <!-- 头像区域 -->
      <view class="avatar-section" @tap="chooseAvatar">
        <view class="avatar-wrapper">
          <image
            class="avatar-preview"
            :src="formData.avatarUrl || '/static/images/default-avatar.png'"
            mode="aspectFill"
          />
          <view class="avatar-overlay">
            <text class="avatar-overlay-text">更换头像</text>
          </view>
        </view>
        <text class="avatar-tip">点击更换头像</text>
      </view>

      <!-- 表单区域 -->
      <view class="form-section">
        <!-- 昵称 -->
        <view class="form-item">
          <text class="form-label">昵称</text>
          <view class="form-input-wrapper">
            <input
              v-model="formData.nickName"
              class="form-input"
              type="text"
              placeholder="请输入昵称"
              :maxlength="10"
              placeholder-class="form-placeholder"
            />
            <text class="form-counter">{{ formData.nickName.length }}/10</text>
          </view>
          <text v-if="errors.nickName" class="form-error">{{ errors.nickName }}</text>
        </view>

        <!-- 性别 -->
        <view class="form-item">
          <text class="form-label">性别</text>
          <picker :range="genderOptions" :value="genderIndex" @change="onGenderChange">
            <view class="form-picker">
              <text :class="['form-picker-text', { 'is-placeholder': !genderDisplay }]">
                {{ genderDisplay || '请选择性别' }}
              </text>
              <text class="form-picker-arrow">&#xe614;</text>
            </view>
          </picker>
        </view>

        <!-- 职业 -->
        <view class="form-item">
          <text class="form-label">职业</text>
          <picker :range="professOptions" :value="professIndex" @change="onProfessChange">
            <view class="form-picker">
              <text :class="['form-picker-text', { 'is-placeholder': !professDisplay }]">
                {{ professDisplay || '请选择职业' }}
              </text>
              <text class="form-picker-arrow">&#xe614;</text>
            </view>
          </picker>
        </view>

        <!-- 生日 -->
        <view class="form-item">
          <text class="form-label">生日</text>
          <picker mode="date" :value="formData.birthday" :end="today" @change="onBirthdayChange">
            <view class="form-picker">
              <text :class="['form-picker-text', { 'is-placeholder': !formData.birthday }]">
                {{ formData.birthday || '请选择生日' }}
              </text>
              <text class="form-picker-arrow">&#xe614;</text>
            </view>
          </picker>
        </view>

        <!-- 城市 -->
        <view class="form-item">
          <text class="form-label">城市</text>
          <view class="form-input-wrapper">
            <input
              v-model="formData.city"
              class="form-input"
              type="text"
              placeholder="请输入所在城市"
              placeholder-class="form-placeholder"
            />
          </view>
        </view>
      </view>

      <!-- 保存按钮 -->
      <view class="save-section">
        <button class="save-btn" :loading="saving" :disabled="saving" @tap="handleSave">
          保存修改
        </button>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, reactive, computed } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { useAppStore } from '@/stores/app'
import { useUserStore } from '@/stores/user'
import * as userApi from '@/api/modules/user'
import { PROFESS_LABELS, GENDER_LABELS, UserGender } from '@/constants/enums'

const appStore = useAppStore()
const userStore = useUserStore()

const statusBarHeight = computed(() => appStore.statusBarHeight)
const navBarHeight = computed(() => appStore.navBarHeight)

// 表单数据
const formData = reactive({
  nickName: '',
  avatarUrl: '',
  gender: '',
  profess: '',
  birthday: '',
  city: '',
})

// 错误信息
const errors = reactive({
  nickName: '',
})

const saving = ref(false)

// 性别选项
const genderOptions = ['男', '女']
const genderIndex = ref(-1)
const genderDisplay = computed(() => {
  if (!formData.gender) return ''
  return GENDER_LABELS[formData.gender] || ''
})

// 职业选项
const professOptions = Object.values(PROFESS_LABELS)
const professKeys = Object.keys(PROFESS_LABELS)
const professIndex = ref(-1)
const professDisplay = computed(() => {
  if (!formData.profess) return ''
  return PROFESS_LABELS[formData.profess] || ''
})

// 今日日期
const today = computed(() => {
  const d = new Date()
  const year = d.getFullYear()
  const month = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
})

// 初始化表单
onLoad(() => {
  const info = userStore.userInfo
  if (info) {
    formData.nickName = info.nickname || ''
    formData.avatarUrl = info.avatarurl || ''
    formData.gender = String(info.gender || '')
    formData.profess = info.profess || ''
    formData.city = info.city || ''

    // 设置 picker 索引
    if (formData.gender === UserGender.MALE) {
      genderIndex.value = 0
    } else if (formData.gender === UserGender.FEMALE) {
      genderIndex.value = 1
    }

    const pIdx = professKeys.indexOf(formData.profess)
    if (pIdx >= 0) {
      professIndex.value = pIdx
    }
  }
})

// 选择头像
function chooseAvatar() {
  uni.chooseImage({
    count: 1,
    sizeType: ['compressed'],
    sourceType: ['album', 'camera'],
    success: (res) => {
      const tempFilePath = res.tempFilePaths[0]
      formData.avatarUrl = tempFilePath
      // 实际项目中需要先上传到服务器获得URL
      uploadAvatar(tempFilePath)
    },
  })
}

// 上传头像
async function uploadAvatar(filePath: string) {
  try {
    const uploadRes = await new Promise<UniApp.UploadFileSuccessCallbackResult>((resolve, reject) => {
      uni.uploadFile({
        url: '/api/upload/image',
        filePath,
        name: 'file',
        success: resolve,
        fail: reject,
      })
    })
    const data = JSON.parse(uploadRes.data)
    if (data.success) {
      formData.avatarUrl = data.data.url
    }
  } catch {
    // 上传失败保留本地预览路径
  }
}

// 性别变更
function onGenderChange(e: any) {
  const index = Number(e.detail.value)
  genderIndex.value = index
  formData.gender = index === 0 ? UserGender.MALE : UserGender.FEMALE
}

// 职业变更
function onProfessChange(e: any) {
  const index = Number(e.detail.value)
  professIndex.value = index
  formData.profess = professKeys[index]
}

// 生日变更
function onBirthdayChange(e: any) {
  formData.birthday = e.detail.value
}

// 表单校验
function validate(): boolean {
  errors.nickName = ''

  if (!formData.nickName.trim()) {
    errors.nickName = '请输入昵称'
    return false
  }

  if (formData.nickName.trim().length < 2) {
    errors.nickName = '昵称至少2个字符'
    return false
  }

  if (formData.nickName.trim().length > 10) {
    errors.nickName = '昵称最多10个字符'
    return false
  }

  return true
}

// 保存
async function handleSave() {
  if (!validate()) return
  if (!userStore.userInfo) {
    uni.showToast({ title: '请先登录', icon: 'none' })
    return
  }

  saving.value = true
  try {
    const params: any = {
      id: Number(userStore.userInfo.id),
      nickname: formData.nickName.trim(),
    }

    if (formData.avatarUrl) {
      params.imgpath = formData.avatarUrl
    }
    if (formData.gender) {
      params.gender = Number(formData.gender)
    }
    if (formData.city) {
      params.city = formData.city
    }

    const res = await userApi.updateUser(params)
    if (res.success) {
      // 更新本地 store
      userStore.updateUserInfo({
        nickname: formData.nickName.trim(),
        avatarurl: formData.avatarUrl,
        gender: Number(formData.gender) || 0,
        profess: formData.profess,
        city: formData.city,
      })

      uni.showToast({
        title: '保存成功',
        icon: 'success',
        duration: 1500,
      })

      setTimeout(() => {
        uni.navigateBack({ delta: 1 })
      }, 1500)
    } else {
      uni.showToast({
        title: res.message || '保存失败',
        icon: 'none',
      })
    }
  } catch (e: any) {
    uni.showToast({
      title: e?.message || '网络异常，请稍后重试',
      icon: 'none',
    })
  } finally {
    saving.value = false
  }
}

// 返回
function handleBack() {
  uni.navigateBack({ delta: 1 })
}
</script>

<style lang="scss">
@import '@/styles/tokens.scss';

.edit-info-page {
  min-height: 100vh;
  background-color: $color-bg-page;
}

// 导航栏
.navbar {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: $z-index-navbar;
  background: $color-bg-card;
  box-shadow: $shadow-sm;

  &__content {
    display: flex;
    align-items: center;
    justify-content: space-between;
    height: 88rpx;
    padding: 0 $spacing-lg;
  }

  &__back {
    width: 64rpx;
    height: 64rpx;
    display: flex;
    align-items: center;
    justify-content: center;
  }

  &__back-icon {
    font-family: 'iconfont';
    font-size: $font-size-xl;
    color: $color-text-primary;
  }

  &__title {
    font-size: $font-size-lg;
    font-weight: $font-weight-semibold;
    color: $color-text-primary;
  }

  &__placeholder {
    width: 64rpx;
  }
}

// 头像区域
.avatar-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: $spacing-xxl 0;
  background: $color-bg-card;
  margin-bottom: $spacing-lg;
}

.avatar-wrapper {
  position: relative;
  width: 160rpx;
  height: 160rpx;
  border-radius: 50%;
  overflow: hidden;
}

.avatar-preview {
  width: 100%;
  height: 100%;
}

.avatar-overlay {
  position: absolute;
  left: 0;
  right: 0;
  bottom: 0;
  height: 48rpx;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
}

.avatar-overlay-text {
  font-size: $font-size-xs;
  color: #ffffff;
}

.avatar-tip {
  margin-top: $spacing-sm;
  font-size: $font-size-sm;
  color: $color-text-helper;
}

// 表单区域
.form-section {
  background: $color-bg-card;
  padding: 0 $spacing-xl;
}

.form-item {
  padding: $spacing-xl 0;
  border-bottom: 1rpx solid $color-divider;

  &:last-child {
    border-bottom: none;
  }
}

.form-label {
  display: block;
  margin-bottom: $spacing-sm;
  font-size: $font-size-base;
  font-weight: $font-weight-medium;
  color: $color-text-primary;
}

.form-input-wrapper {
  display: flex;
  align-items: center;
}

.form-input {
  flex: 1;
  height: 72rpx;
  font-size: $font-size-base;
  color: $color-text-primary;
}

.form-placeholder {
  color: $color-text-helper;
}

.form-counter {
  font-size: $font-size-sm;
  color: $color-text-helper;
  margin-left: $spacing-sm;
}

.form-error {
  display: block;
  margin-top: $spacing-xs;
  font-size: $font-size-sm;
  color: $color-accent-red;
}

// Picker
.form-picker {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 72rpx;
}

.form-picker-text {
  font-size: $font-size-base;
  color: $color-text-primary;

  &.is-placeholder {
    color: $color-text-helper;
  }
}

.form-picker-arrow {
  font-family: 'iconfont';
  font-size: $font-size-base;
  color: $color-text-helper;
}

// 保存按钮
.save-section {
  padding: $spacing-xxl $spacing-xl;
  padding-bottom: calc(#{$spacing-xxl} + env(safe-area-inset-bottom));
}

.save-btn {
  width: 100%;
  height: 96rpx;
  line-height: 96rpx;
  text-align: center;
  font-size: $font-size-lg;
  font-weight: $font-weight-semibold;
  color: #ffffff;
  background: $color-primary;
  border-radius: $radius-round;
  border: none;

  &::after {
    border: none;
  }

  &[disabled] {
    background: $color-primary-light;
    color: rgba(255, 255, 255, 0.7);
  }

  &:active {
    background: $color-primary-dark;
  }
}
</style>
