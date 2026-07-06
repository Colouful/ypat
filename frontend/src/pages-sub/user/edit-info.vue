<template>
  <view class="page">
    <KeepPageNav title="编辑资料" />
    <view class="avatar-section" @tap="chooseAvatar">
      <image class="avatar" :src="avatarPreview || '/static/default-avatar.png'" mode="aspectFill" />
      <text class="avatar-tip">点击更换头像</text>
    </view>

    <view class="form-card">
      <text class="label">昵称</text>
      <view class="form-field">
        <input v-model="form.nickname" class="input" maxlength="10" placeholder="请输入昵称" />
        <view v-if="form.nickname" class="field-clear" @tap.stop="form.nickname = ''">
          <KeepIcon name="close" :size="22" color="#83888F" :stroke-width="3" />
        </view>
      </view>

      <text class="label">微信号</text>
      <view class="form-field">
        <input v-model="form.wx" class="input" maxlength="30" placeholder="请输入微信号（用于对方联系你）" />
        <view v-if="form.wx" class="field-clear" @tap.stop="form.wx = ''">
          <KeepIcon name="close" :size="22" color="#83888F" :stroke-width="3" />
        </view>
      </view>

      <text class="label">性别</text>
      <picker :range="genderOptions" :value="genderIndex" @change="changeGender">
        <view class="form-field picker-field">
          <text class="picker-text" :class="{ 'picker-text--placeholder': !genderText }">{{ genderText || '请选择性别' }}</text>
          <view v-if="form.gender" class="field-clear" @tap.stop="form.gender = ''">
            <KeepIcon name="close" :size="22" color="#83888F" :stroke-width="3" />
          </view>
        </view>
      </picker>

      <text class="label">职业</text>
      <picker :range="professionLabels" :value="professionIndex" @change="changeProfession">
        <view class="form-field picker-field">
          <text class="picker-text" :class="{ 'picker-text--placeholder': !professionText }">{{ professionText || '请选择职业' }}</text>
          <view v-if="form.profess" class="field-clear" @tap.stop="form.profess = ''">
            <KeepIcon name="close" :size="22" color="#83888F" :stroke-width="3" />
          </view>
        </view>
      </picker>

      <text class="label">生日</text>
      <picker mode="date" :value="form.birthday" :end="today" @change="changeBirthday">
        <view class="form-field picker-field">
          <text class="picker-text" :class="{ 'picker-text--placeholder': !form.birthday }">{{ form.birthday || '请选择生日' }}</text>
          <view v-if="form.birthday" class="field-clear" @tap.stop="form.birthday = ''">
            <KeepIcon name="close" :size="22" color="#83888F" :stroke-width="3" />
          </view>
        </view>
      </picker>

      <text class="label">所在地区</text>
      <picker mode="region" :value="regionValue" @change="changeRegion">
        <view class="form-field picker-field">
          <text class="picker-text" :class="{ 'picker-text--placeholder': !regionText }">{{ regionText || '请选择省、市、区' }}</text>
          <view v-if="regionText" class="field-clear" @tap.stop="clearRegion">
            <KeepIcon name="close" :size="22" color="#83888F" :stroke-width="3" />
          </view>
        </view>
      </picker>
    </view>

    <button class="save" :disabled="saving" :loading="saving" @tap="save">
      {{ saving ? '保存中...' : '保存修改' }}
    </button>
  </view>
</template>

<script setup lang="ts">
import KeepPageNav from '@/components/business/KeepPageNav.vue'
import { computed, reactive, ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { useUserStore } from '@/stores/user'
import * as userApi from '@/api/modules/user'
import { uploadImage } from '@/api/modules/media'
import { GENDER_LABELS, PROFESS_LABELS } from '@/constants/enums'
import KeepIcon from '@/components/business/KeepIcon.vue'
import type { UpdateUserParams } from '@/api/types'

const userStore = useUserStore()
const saving = ref(false)
const avatarPreview = ref('')
const avatarFilePath = ref('')

const genderKeys = Object.keys(GENDER_LABELS)
const genderOptions = Object.values(GENDER_LABELS)
const professionKeys = Object.keys(PROFESS_LABELS)
const professionLabels = Object.values(PROFESS_LABELS)

const form = reactive({
  nickname: '',
  gender: '',
  profess: '',
  birthday: '',
  province: '',
  city: '',
  area: '',
  wx: '',
})

const today = new Date().toISOString().slice(0, 10)
const genderIndex = computed(() => Math.max(0, genderKeys.indexOf(form.gender)))
const professionIndex = computed(() => Math.max(0, professionKeys.indexOf(form.profess)))
const genderText = computed(() => GENDER_LABELS[form.gender] || '')
const professionText = computed(() => PROFESS_LABELS[form.profess] || '')
const regionValue = computed(() => [form.province, form.city, form.area])
const regionText = computed(() => regionValue.value.filter(Boolean).join(' '))

function initForm(): void {
  const info = userStore.userInfo
  if (!info) return
  form.nickname = info.nickname || ''
  form.gender = info.gender || ''
  form.profess = info.profess || ''
  form.birthday = info.birthday || ''
  form.province = info.province || ''
  form.city = info.city || ''
  form.area = info.area || ''
  form.wx = info.wx || ''
  avatarPreview.value = info.imgpath || info.avatarurl || ''
}

function chooseAvatar(): void {
  uni.chooseImage({
    count: 1,
    sizeType: ['compressed'],
    sourceType: ['album', 'camera'],
    success: async ({ tempFilePaths }) => {
      const path = tempFilePaths[0]
      if (!path) return
      avatarPreview.value = path
      avatarFilePath.value = path
    },
  })
}

function changeGender(event: { detail: { value: string | number } }): void {
  form.gender = genderKeys[Number(event.detail.value)] || ''
}

function changeProfession(event: { detail: { value: string | number } }): void {
  form.profess = professionKeys[Number(event.detail.value)] || ''
}

function changeBirthday(event: { detail: { value: string } }): void {
  form.birthday = event.detail.value
}

function changeRegion(event: { detail: { value: string[] } }): void {
  const [province = '', city = '', area = ''] = event.detail.value
  form.province = province
  form.city = city
  form.area = area
}

function clearRegion(): void {
  form.province = ''
  form.city = ''
  form.area = ''
}

async function save(): Promise<void> {
  const id = userStore.userInfo?.id
  if (!id) {
    uni.showToast({ title: '请先登录', icon: 'none' })
    return
  }
  if (form.nickname.trim().length < 2) {
    uni.showToast({ title: '昵称至少需要 2 个字符', icon: 'none' })
    return
  }

  saving.value = true
  try {
    const avatarUrl = avatarFilePath.value ? (await uploadImage(avatarFilePath.value)).url : ''
    const params: UpdateUserParams = {
      id,
      nickname: form.nickname.trim(),
      gender: form.gender || undefined,
      profess: form.profess || undefined,
      birthday: form.birthday || undefined,
      province: form.province || undefined,
      city: form.city || undefined,
      area: form.area || undefined,
      wx: form.wx.trim() || undefined,
      pics: avatarUrl || undefined,
    }
    await userApi.updateUser(params)
    await userStore.updateUserInfo({
      nickname: params.nickname,
      gender: params.gender,
      profess: params.profess,
      birthday: params.birthday,
      province: params.province,
      city: params.city,
      area: params.area,
      wx: params.wx,
      imgpath: avatarUrl || userStore.userInfo?.imgpath,
    })
    uni.showToast({ title: '保存成功', icon: 'success' })
    setTimeout(() => uni.navigateBack(), 1000)
  } catch (error) {
    uni.showToast({ title: error instanceof Error ? error.message : '保存失败', icon: 'none' })
  } finally {
    saving.value = false
  }
}

onLoad(initForm)
</script>

<style scoped lang="scss">

.page { min-height: 100vh; box-sizing: border-box; padding: 28rpx; background: $color-bg-page; }
.avatar-section { display: flex; flex-direction: column; align-items: center; padding: 40rpx 0; }
.avatar { width: 160rpx; height: 160rpx; border-radius: 50%; background: #e9edf2; }
.avatar-tip { margin-top: 16rpx; color: $color-text-secondary; font-size: 25rpx; }
.form-card { padding: 30rpx; border-radius: $radius-keep-card; background: $color-bg-card; box-shadow: $shadow-keep-card; }
.label { display: block; margin: 28rpx 0 12rpx; color: $color-text-primary; font-weight: 600; }
.form-field {
  display: flex;
  align-items: center;
  box-sizing: border-box;
  width: 100%;
  height: 88rpx;
  padding: 0 22rpx;
  border-radius: 16rpx;
  background: $color-bg-page;
}
.input {
  flex: 1;
  min-width: 0;
  height: 88rpx;
  color: $color-text-primary;
  font-size: 28rpx;
}
.picker-field {
  justify-content: space-between;
}
.picker-text {
  flex: 1;
  min-width: 0;
  color: $color-text-primary;
  font-size: 28rpx;
  @include ellipsis;
}
.picker-text--placeholder {
  color: $color-text-helper;
}
.field-clear {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 44rpx;
  height: 44rpx;
  margin-left: 12rpx;
}
.save { margin-top: 36rpx; height: 92rpx; line-height: 92rpx; border-radius: 999rpx; color: #fff; background: $color-primary; }
.save[disabled] { opacity: .5; }
.save::after { border: 0; }
</style>
