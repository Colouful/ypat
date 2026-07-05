<template>
  <view class="page">
    <KeepPageNav title="完善资料" />
    <view class="header">
      <text class="title">完善个人资料</text>
      <text class="subtitle">这些信息用于同城推荐与联系，标 * 为必填，可在个人中心修改。</text>
    </view>

    <view class="card">
      <view class="field">
        <text class="label"><text class="required">*</text>昵称</text>
        <input
          v-model="form.nickname"
          class="input"
          maxlength="20"
          placeholder="请输入 2-20 字昵称"
          placeholder-class="input-placeholder"
        />
      </view>

      <view class="field">
        <text class="label"><text class="required">*</text>性别</text>
        <view class="options">
          <view
            v-for="item in genders"
            :key="item.value"
            class="option"
            :class="{ active: form.gender === item.value }"
            @tap="form.gender = item.value"
          >{{ item.label }}</view>
        </view>
      </view>

      <view class="field">
        <text class="label"><text class="required">*</text>职业身份</text>
        <view class="options">
          <view
            v-for="item in professions"
            :key="item.value"
            class="option option--compact"
            :class="{ active: form.profess === item.value }"
            @tap="form.profess = item.value"
          >{{ item.label }}</view>
        </view>
      </view>

      <view class="field">
        <text class="label"><text class="required">*</text>生日</text>
        <picker mode="date" :value="form.birthday" :end="today" @change="changeBirthday">
          <view class="picker" :class="{ 'picker--placeholder': !form.birthday }">
            {{ form.birthday || '请选择生日' }}
          </view>
        </picker>
      </view>

      <view class="field">
        <text class="label"><text class="required">*</text>所在地区</text>
        <picker mode="region" :value="regionValue" @change="changeRegion">
          <view class="picker" :class="{ 'picker--placeholder': !regionText }">
            {{ regionText || '请选择省、市、区' }}
          </view>
        </picker>
      </view>

      <view class="field">
        <text class="label">微信号</text>
        <input
          v-model="form.wx"
          class="input"
          maxlength="40"
          placeholder="选填，方便对方联系（可稍后在个人中心补充）"
          placeholder-class="input-placeholder"
        />
      </view>
    </view>

    <button class="submit" :loading="submitting" :disabled="submitting" @tap="submit">
      {{ submitting ? '保存中...' : submitLabel }}
    </button>
    <view class="footnote">
      <text class="footnote__text">资料用于匹配同城拍摄伙伴；提交后可在「个人资料」继续完善头像、微信、收支等。</text>
    </view>
  </view>
</template>

<script setup lang="ts">
import KeepPageNav from '@/components/business/KeepPageNav.vue'
import { computed, reactive, ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { useUserStore } from '@/stores/user'
import * as userApi from '@/api/modules/user'
import { GENDER_LABELS, PROFESS_LABELS, UserGender } from '@/constants/enums'
import { goRootTab, isRootTabUrl } from '@/utils/tab-navigation'
import type { UpdateUserParams } from '@/api/types'

const userStore = useUserStore()
const submitting = ref(false)
const redirectUrl = ref('')
const today = new Date().toISOString().slice(0, 10)
const genders = Object.entries(GENDER_LABELS)
  .filter(([value]) => value === UserGender.MALE || value === UserGender.FEMALE)
  .map(([value, label]) => ({ value, label }))
const professions = Object.entries(PROFESS_LABELS).map(([value, label]) => ({ value, label }))

const existing = userStore.userInfo

const form = reactive({
  nickname: existing?.nickname || '',
  wx: existing?.wx || '',
  gender: existing?.gender && (existing.gender === UserGender.MALE || existing.gender === UserGender.FEMALE)
    ? existing.gender
    : '',
  profess: existing?.profess || '',
  birthday: existing?.birthday || '',
  province: existing?.province || '',
  city: existing?.city || '',
  area: existing?.area || '',
})

const regionValue = computed(() => [form.province, form.city, form.area])
const regionText = computed(() => regionValue.value.filter(Boolean).join(' '))
const submitLabel = computed(() => (redirectUrl.value ? '保存并继续' : '保存并进入首页'))

function changeBirthday(event: { detail: { value: string } }): void {
  form.birthday = event.detail.value
}

function changeRegion(event: { detail: { value: string[] } }): void {
  const [province = '', city = '', area = ''] = event.detail.value
  form.province = province
  form.city = city
  form.area = area
}

function validate(): string | null {
  const nickname = form.nickname.trim()
  if (nickname.length < 2 || nickname.length > 20) return '昵称需为 2-20 字'
  if (form.gender !== UserGender.MALE && form.gender !== UserGender.FEMALE) return '请选择性别'
  if (!form.profess) return '请选择职业身份'
  if (!form.birthday) return '请选择生日'
  if (!form.province || !form.city) return '请选择省、市'
  return null
}

async function submit(): Promise<void> {
  if (submitting.value) return

  const id = userStore.userInfo?.id
  if (!id) {
    uni.showToast({ title: '登录状态已失效，请重新登录', icon: 'none' })
    setTimeout(() => uni.redirectTo({ url: '/pages/login/index' }), 800)
    return
  }

  const error = validate()
  if (error) {
    uni.showToast({ title: error, icon: 'none' })
    return
  }

  submitting.value = true
  const params: UpdateUserParams = {
    id,
    nickname: form.nickname.trim(),
    gender: form.gender,
    profess: form.profess,
    birthday: form.birthday,
    province: form.province,
    city: form.city,
    area: form.area || undefined,
    wx: form.wx.trim() || undefined,
  }
  try {
    await userApi.updateUser(params)
    await userStore.updateUserInfo(params)
    uni.showToast({ title: '资料保存成功', icon: 'success' })
    setTimeout(goNext, 700)
  } catch (e) {
    uni.showToast({ title: e instanceof Error ? e.message : '保存失败', icon: 'none' })
  } finally {
    submitting.value = false
  }
}

function goNext(): void {
  const target = redirectUrl.value
  if (target) {
    if (isRootTabUrl(target)) goRootTab(target)
    else uni.redirectTo({ url: target })
    return
  }
  goRootTab('/pages/home/index')
}

onLoad((query) => {
  redirectUrl.value = decodeURIComponent(String(query?.redirect || ''))
})
</script>

<style scoped lang="scss">
.page {
  min-height: 100vh;
  box-sizing: border-box;
  padding: 28rpx 28rpx calc(80rpx + env(safe-area-inset-bottom));
  background: $color-bg-page;
}

.header {
  margin: 16rpx 0 28rpx;
}

.title {
  display: block;
  color: $color-text-primary;
  font-size: 44rpx;
  font-weight: 900;
}

.subtitle {
  display: block;
  margin-top: 14rpx;
  color: $color-text-secondary;
  font-size: 26rpx;
  line-height: 1.6;
}

.card {
  padding: 16rpx 30rpx 30rpx;
  border-radius: $radius-keep-card;
  background: $color-bg-card;
  box-shadow: $shadow-keep-card;
}

.field {
  padding: 22rpx 0;
  border-bottom: 1rpx solid $color-border;
}

.field:last-child {
  border-bottom: 0;
}

.label {
  display: block;
  margin-bottom: 16rpx;
  color: $color-text-primary;
  font-size: 28rpx;
  font-weight: 800;
}

.required {
  margin-right: 6rpx;
  color: $color-accent-red;
}

.input {
  width: 100%;
  box-sizing: border-box;
  padding: 22rpx;
  border-radius: 16rpx;
  color: $color-text-primary;
  background: $color-bg-page;
  font-size: 30rpx;
  font-weight: 700;
  line-height: 1.4;
  min-height: 88rpx;
}

.input-placeholder {
  color: $color-text-helper;
  font-weight: 600;
}

.options {
  display: flex;
  flex-wrap: wrap;
  gap: 14rpx;
}

.option {
  padding: 14rpx 28rpx;
  border-radius: $radius-round;
  color: $color-text-secondary;
  background: $color-bg-chip;
  font-size: 26rpx;
  font-weight: 700;
}

.option--compact {
  padding: 12rpx 22rpx;
  font-size: 24rpx;
}

.option.active {
  color: #fff;
  background: $color-primary;
}

.picker {
  padding: 22rpx;
  border-radius: 16rpx;
  color: $color-text-primary;
  background: $color-bg-page;
  font-size: 30rpx;
  font-weight: 700;
}

.picker--placeholder {
  color: $color-text-helper;
  font-weight: 600;
}

.submit {
  margin-top: 38rpx;
  height: 92rpx;
  border-radius: 999rpx;
  color: #fff;
  background: $color-primary;
  font-size: 30rpx;
  font-weight: 900;
  line-height: 92rpx;
}

.submit[disabled] {
  opacity: 0.6;
}

.submit::after {
  border: 0;
}

.footnote {
  margin-top: 28rpx;
  padding: 0 18rpx;
}

.footnote__text {
  color: $color-text-helper;
  font-size: 24rpx;
  line-height: 1.6;
}
</style>
