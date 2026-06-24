<template>
  <view class="page">
    <view class="header">
      <text class="title">完善个人资料</text>
      <text class="subtitle">这些信息用于同城推荐，可稍后在个人中心修改。</text>
    </view>

    <view class="card">
      <text class="label">性别</text>
      <view class="options">
        <view v-for="item in genders" :key="item.value" class="option" :class="{ active: form.gender === item.value }" @tap="form.gender = item.value">
          {{ item.label }}
        </view>
      </view>

      <text class="label">职业</text>
      <view class="options">
        <view v-for="item in professions" :key="item.value" class="option" :class="{ active: form.profess === item.value }" @tap="form.profess = item.value">
          {{ item.label }}
        </view>
      </view>

      <text class="label">生日</text>
      <picker mode="date" :value="form.birthday" :end="today" @change="changeBirthday">
        <view class="picker">{{ form.birthday || '请选择生日' }}</view>
      </picker>

      <text class="label">所在地区</text>
      <picker mode="region" :value="regionValue" @change="changeRegion">
        <view class="picker">{{ regionText || '请选择省、市、区' }}</view>
      </picker>
    </view>

    <button class="submit" :loading="submitting" :disabled="submitting" @tap="submit">
      {{ submitting ? '保存中...' : '保存并进入首页' }}
    </button>
    <text class="skip" @tap="goHome">暂时跳过</text>
  </view>
</template>

<script setup lang="ts">
import { computed, reactive, ref } from 'vue'
import { useUserStore } from '@/stores/user'
import * as userApi from '@/api/modules/user'
import { GENDER_LABELS, PROFESS_LABELS } from '@/constants/enums'
import type { UpdateUserParams } from '@/api/types'

const userStore = useUserStore()
const submitting = ref(false)
const today = new Date().toISOString().slice(0, 10)
const genders = Object.entries(GENDER_LABELS).map(([value, label]) => ({ value, label }))
const professions = Object.entries(PROFESS_LABELS).map(([value, label]) => ({ value, label }))

const form = reactive({
  gender: '',
  profess: '',
  birthday: '',
  province: '',
  city: '',
  area: '',
})

const regionValue = computed(() => [form.province, form.city, form.area])
const regionText = computed(() => regionValue.value.filter(Boolean).join(' '))

function changeBirthday(event: { detail: { value: string } }): void {
  form.birthday = event.detail.value
}

function changeRegion(event: { detail: { value: string[] } }): void {
  const [province = '', city = '', area = ''] = event.detail.value
  form.province = province
  form.city = city
  form.area = area
}

async function submit(): Promise<void> {
  const id = userStore.userInfo?.id
  if (!id) {
    uni.showToast({ title: '登录状态已失效', icon: 'none' })
    return
  }

  submitting.value = true
  try {
    const params: UpdateUserParams = {
      id,
      gender: form.gender || undefined,
      profess: form.profess || undefined,
      birthday: form.birthday || undefined,
      province: form.province || undefined,
      city: form.city || undefined,
      area: form.area || undefined,
    }
    await userApi.updateUser(params)
    await userStore.updateUserInfo(params)
    uni.showToast({ title: '资料保存成功', icon: 'success' })
    setTimeout(goHome, 800)
  } catch (error) {
    uni.showToast({ title: error instanceof Error ? error.message : '保存失败', icon: 'none' })
  } finally {
    submitting.value = false
  }
}

function goHome(): void {
  uni.switchTab({ url: '/pages/home/index' })
}
</script>

<style scoped lang="scss">
.page { min-height: 100vh; box-sizing: border-box; padding: 56rpx 28rpx; background: #f7f8fa; }
.header { margin-bottom: 36rpx; }
.title { display: block; color: #1d2433; font-size: 42rpx; font-weight: 700; }
.subtitle { display: block; margin-top: 14rpx; color: #7c8593; font-size: 26rpx; }
.card { padding: 30rpx; border-radius: 26rpx; background: #fff; }
.label { display: block; margin: 28rpx 0 14rpx; font-weight: 600; }
.options { display: flex; flex-wrap: wrap; gap: 14rpx; }
.option { padding: 14rpx 24rpx; border-radius: 28rpx; color: #596270; background: #f1f3f5; }
.option.active { color: #fff; background: #23c268; }
.picker { padding: 22rpx; border-radius: 16rpx; background: #f7f8fa; }
.submit { margin-top: 38rpx; height: 92rpx; line-height: 92rpx; border-radius: 46rpx; color: #fff; background: #23c268; }
.submit::after { border: 0; }
.skip { display: block; margin-top: 28rpx; color: #7c8593; text-align: center; }
</style>
