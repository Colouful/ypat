<template>
  <view class="page">
    <view v-if="loading" class="state">正在查询认证状态...</view>

    <view v-else-if="authInfo?.status === '1'" class="state card">
      <text class="title">审核中</text>
      <text class="desc">资料已提交，请耐心等待审核。</text>
    </view>

    <view v-else-if="authInfo?.status === '2'" class="state card">
      <text class="title success">已认证</text>
      <text class="desc">{{ maskedName }} {{ maskedCode }}</text>
    </view>

    <view v-else class="card">
      <view v-if="authInfo?.status === '3'" class="warning">审核未通过，请核对资料后重新提交。</view>

      <text class="label">证件正面</text>
      <view class="picker" @tap="chooseImage('front')">
        <image v-if="frontPath" :src="frontPath" mode="aspectFill" />
        <text v-else>点击选择照片</text>
      </view>

      <text class="label">证件反面</text>
      <view class="picker" @tap="chooseImage('back')">
        <image v-if="backPath" :src="backPath" mode="aspectFill" />
        <text v-else>点击选择照片</text>
      </view>

      <text class="label">真实姓名</text>
      <input v-model="form.name" class="input" maxlength="30" placeholder="请输入真实姓名" />

      <text class="label">证件号码</text>
      <input v-model="form.certcode" class="input" maxlength="18" placeholder="请输入证件号码" />

      <button class="submit" :disabled="submitDisabled" :loading="submitting" @tap="submit">
        {{ submitting ? '提交中...' : '提交认证' }}
      </button>
      <text class="privacy">页面退出后会清理本地临时资料。</text>
    </view>
  </view>
</template>

<script setup lang="ts">
import { computed, reactive, ref } from 'vue'
import { onLoad, onUnload } from '@dcloudio/uni-app'
import * as oauthApi from '@/api/modules/oauth'
import type { OauthInfo } from '@/api/types'

const loading = ref(true)
const submitting = ref(false)
const authInfo = ref<OauthInfo | null>(null)
const frontPath = ref('')
const backPath = ref('')
const form = reactive({ name: '', certcode: '' })

const submitDisabled = computed(() => (
  submitting.value
  || !form.name.trim()
  || !/^\d{15}$|^\d{17}[\dXx]$/.test(form.certcode.trim())
  || !frontPath.value
  || !backPath.value
))

const maskedName = computed(() => {
  const value = authInfo.value?.name || ''
  return value.length > 1 ? `${value[0]}${'*'.repeat(value.length - 1)}` : value
})

const maskedCode = computed(() => {
  const value = authInfo.value?.certcode || ''
  return value.length > 8 ? `${value.slice(0, 3)}***********${value.slice(-4)}` : value
})

async function loadDetail(): Promise<void> {
  loading.value = true
  try {
    authInfo.value = (await oauthApi.getAuthDetail()).data || null
  } catch {
    authInfo.value = null
  } finally {
    loading.value = false
  }
}

function chooseImage(side: 'front' | 'back'): void {
  uni.chooseImage({
    count: 1,
    sizeType: ['compressed'],
    sourceType: ['album', 'camera'],
    success: async ({ tempFilePaths }) => {
      const path = tempFilePaths[0]
      if (!path) return
      if (side === 'front') {
        frontPath.value = path
        uni.showLoading({ title: '识别中...' })
        try {
          const result = await oauthApi.ocrIdCard(path)
          form.name = result.data?.name || ''
          form.certcode = result.data?.certcode || ''
        } catch {
          uni.showToast({ title: '识别失败，请手动填写', icon: 'none' })
        } finally {
          uni.hideLoading()
        }
      } else {
        backPath.value = path
      }
    },
  })
}

async function submit(): Promise<void> {
  if (submitDisabled.value) return
  submitting.value = true
  try {
    await oauthApi.submitAuth({
      name: form.name.trim(),
      certcode: form.certcode.trim().toUpperCase(),
      pics: [frontPath.value, backPath.value],
    })
    clearForm()
    uni.showToast({ title: '提交成功', icon: 'success' })
    await loadDetail()
  } catch (error) {
    uni.showToast({ title: error instanceof Error ? error.message : '提交失败', icon: 'none' })
  } finally {
    submitting.value = false
  }
}

function clearForm(): void {
  form.name = ''
  form.certcode = ''
  frontPath.value = ''
  backPath.value = ''
}

onLoad(loadDetail)
onUnload(clearForm)
</script>

<style scoped lang="scss">
.page { min-height: 100vh; box-sizing: border-box; padding: 28rpx; background: #f7f8fa; }
.card { padding: 32rpx; border-radius: 26rpx; background: #fff; }
.state { padding: 180rpx 30rpx; color: #7c8593; text-align: center; }
.title { display: block; color: #1d2433; font-size: 36rpx; font-weight: 600; }
.success { color: #23a85f; }
.desc, .privacy { display: block; margin-top: 18rpx; color: #7c8593; font-size: 25rpx; text-align: center; }
.warning { margin-bottom: 24rpx; padding: 20rpx; border-radius: 16rpx; color: #b4232c; background: #fff1f0; }
.label { display: block; margin: 28rpx 0 12rpx; font-weight: 600; }
.picker { height: 260rpx; display: flex; align-items: center; justify-content: center; overflow: hidden; border: 2rpx dashed #cfd5dd; border-radius: 20rpx; color: #8b94a3; background: #fafbfc; }
.picker image { width: 100%; height: 100%; }
.input { height: 88rpx; box-sizing: border-box; padding: 0 22rpx; border-radius: 16rpx; background: #f7f8fa; }
.submit { margin-top: 38rpx; height: 92rpx; line-height: 92rpx; border-radius: 46rpx; color: #fff; background: #23c268; }
.submit[disabled] { opacity: .45; }
.submit::after { border: 0; }
</style>
